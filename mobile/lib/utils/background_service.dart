import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:ui' show DartPluginRegistrant;

import 'package:flutter/material.dart';
import 'package:flutter_background_service/flutter_background_service.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:isar/isar.dart';

import '../data/nosql/cache_activity_facility_workflow.dart';
import '../data/nosql/cache_add_new_asset.dart';
import '../data/nosql/cache_amc_installation_form.dart';
import '../data/nosql/cache_amc_media_upload.dart';
import '../data/nosql/cache_asset_detail.dart';
import '../data/nosql/cache_asset_handover_document.dart';
import '../data/nosql/cache_completion_report.dart';
import '../data/nosql/cache_installation_completion_certificate.dart';
import '../data/nosql/cache_installation_image.dart';
import '../data/nosql/cache_media_upload.dart';
import '../data/nosql/cache_operation_checkpoint.dart';
import '../data/nosql/cache_schedule_visit_form_values.dart';
import '../data/nosql/cache_specification.dart';
import '../data/nosql/cache_submission_job.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/asset/asset.dart';
import '../model/audit_details/audit_details.dart';
import '../model/document/document.dart';
import '../model/transaction/transaction.dart';
import '../repositories/activity_facility_repo.dart';
import '../repositories/activity_facility_workflow_repo.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/asset_handover_document_repo.dart';
import '../repositories/asset_repo.dart';
import '../repositories/dynamic_form_repo.dart';
import '../repositories/installation_completion_certificate_repo.dart';
import '../repositories/installation_images_repo.dart';
import '../repositories/operation_progress_repo.dart';
import '../repositories/scheduled_visit_repo.dart';
import '../utils/operation_progress.dart';
import '../utils/utils.dart';
import 'app_logger.dart';
import 'constants.dart';

const String kMethodSubmit = 'submit_project';
const String kEvtProgress = 'submission_progress';
const String kEvtError = 'submission_error';
const String kEvtDone = 'submission_done';
const String kCmdStop = 'stopService';

const String kMethodReject = 'reject_project';
const String kEvtRejectDone = 'rejection_done';
const String kEvtRejectError = 'rejection_error';

const String kMethodSendBack = 'send_back_project';
const String kEvtSendBackDone = 'send_back_done';
const String kEvtSendBackError = 'send_back_error';

const String kMethodSubmitVisit = 'submit_schedule_visit';
const String kEvtScheduleVisitDone = 'schedule_visit_done';
const String kEvtScheduleVisitError = 'schedule_visit_error';

const String kEvtReady = 'bg_ready';

const String kCmdForeground = 'bring_to_foreground';

const String _svcChannelId = 'asset_submission_channel';
const String _svcChannelName = 'Asset Submission';
const int _svcNotifId = 728331;
const String _svcNotifIcon = '@mipmap/ic_launcher';

String installationReportBom = "INSTALLATION_REPORT_BOM";

final FlutterLocalNotificationsPlugin _fln = FlutterLocalNotificationsPlugin();

StreamSubscription? _uiErrSub;
StreamSubscription? _uiDoneSub;
StreamSubscription? _uiRejErrSub;
StreamSubscription? _uiRejDoneSub;
StreamSubscription? _uiSendBackErrSub;
StreamSubscription? _uiSendBackDoneSub;

Future<void> ensureAndroidNotificationPermission() async {
  if (!Platform.isAndroid) return;

  final androidPlugin = _fln.resolvePlatformSpecificImplementation<
      AndroidFlutterLocalNotificationsPlugin>();

  final granted = await androidPlugin?.areNotificationsEnabled() ?? true;
  if (!granted) {
    await androidPlugin?.requestNotificationsPermission();
  }
}

Future<void> setupBackgroundService() async {
  WidgetsFlutterBinding.ensureInitialized();
  await envConfig.initialize();
  final isar = await Constants().isar;

  const androidInit = AndroidInitializationSettings(_svcNotifIcon);
  const iosInit = DarwinInitializationSettings();
  await _fln.initialize(const InitializationSettings(
    android: androidInit,
    iOS: iosInit,
  ));
  const androidChannel = AndroidNotificationChannel(
    _svcChannelId,
    _svcChannelName,
    description: 'Submitting assets in background',
    importance: Importance.low,
  );
  await _fln
      .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>()
      ?.createNotificationChannel(androidChannel);

  await BackgroundServiceController.I.init(isar: isar);

  await FlutterBackgroundService().configure(
    androidConfiguration: AndroidConfiguration(
      onStart: onStart,
      isForegroundMode: true,
      autoStart: false,
      notificationChannelId: _svcChannelId,
      initialNotificationTitle: 'Submitting assets',
      initialNotificationContent: 'Preparing…',
      foregroundServiceNotificationId: _svcNotifId,
    ),
    iosConfiguration: IosConfiguration(
      onForeground: onStart,
      onBackground: _onIosBackground,
    ),
  );

  final uiService = FlutterBackgroundService();

  _uiErrSub?.cancel();
  _uiErrSub = uiService.on(kEvtError).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    final msg = data?['message']?.toString();
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.submit,
      status: 'failed',
      stageKey: (data?['stageKey'] as String?) ?? 'preparing_submission',
      error: msg,
    );
  });

  _uiDoneSub?.cancel();
  _uiDoneSub = uiService.on(kEvtDone).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.submit,
      status: 'success',
      stageKey: 'submission_successful',
    );
  });

  _uiRejErrSub?.cancel();
  _uiRejErrSub = uiService.on(kEvtRejectError).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    final msg = data?['message']?.toString();
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.reject,
      status: 'failed',
      stageKey: (data?['stageKey'] as String?) ?? 'submitting_rejection',
      error: msg,
    );
  });

  _uiRejDoneSub?.cancel();
  _uiRejDoneSub = uiService.on(kEvtRejectDone).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.reject,
      status: 'success',
      stageKey: 'rejection_successful',
    );
  });

  _uiSendBackErrSub?.cancel();
  _uiSendBackErrSub = uiService.on(kEvtSendBackError).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    final msg = data?['message']?.toString();
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.sendBack,
      status: 'failed',
      stageKey: (data?['stageKey'] as String?) ?? 'sending_report_back',
      error: msg,
    );
  });

  _uiSendBackDoneSub?.cancel();
  _uiSendBackDoneSub = uiService.on(kEvtSendBackDone).listen((data) async {
    final pid = data?['activityFacilityId'] as String?;
    if (pid == null) return;
    final uiIsar = await Constants().isar;
    await writeJobStatus(
      isar: uiIsar,
      activityFacilityId: pid,
      operationType: OperationTypes.sendBack,
      status: 'success',
      stageKey: 'send_back_successful',
    );
  });
}

class BackgroundServiceController {
  BackgroundServiceController._();
  static final BackgroundServiceController I = BackgroundServiceController._();

  late Isar _isar;
  Future<void> init({required Isar isar}) async {
    _isar = isar;
  }

  Future<void> enqueueSubmission({
    required String activityFacilityId,
    required String facilityId,
    required String userType,
    required bool fromDraft,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      AppLogger.instance
          .info('[UI] service already running -> invoke directly');

      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {
        'title': operationTitle(OperationTypes.submit),
        'content': 'Preparing…',
      });

      await _forceStartJobImmediately(
        service: service,
        method: kMethodSubmit,
        payload: {
          'activityFacilityId': activityFacilityId,
          'facilityId': facilityId,
          'userType': userType,
          'fromDraft': fromDraft,
        },
        logTag: '[UI]',
        readyStream: null,
      );
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    await service.isRunning();

    await _forceStartJobImmediately(
      service: service,
      method: kMethodSubmit,
      payload: {
        'activityFacilityId': activityFacilityId,
        'facilityId': facilityId,
        'userType': userType,
        'fromDraft': fromDraft,
      },
      logTag: '[UI]',
      readyStream: readyStream,
    );
  }

  Future<void> enqueueRejection({
    required String activityFacilityId,
    required String userType,
    required List<Map<String, dynamic>> transactions,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      AppLogger.instance
          .info('[UI] service already running -> invoke REJECTION directly');

      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {
        'title': operationTitle(OperationTypes.reject),
        'content': 'Preparing rejection…',
      });

      await _forceStartJobImmediately(
        service: service,
        method: kMethodReject,
        payload: <String, dynamic>{
          'activityFacilityId': activityFacilityId,
          'userType': userType,
          'transactions': transactions,
        },
        logTag: '[UI]',
        readyStream: null,
      );
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    await service.isRunning();

    await _forceStartJobImmediately(
      service: service,
      method: kMethodReject,
      payload: <String, dynamic>{
        'activityFacilityId': activityFacilityId,
        'userType': userType,
        'transactions': transactions,
      },
      logTag: '[UI]',
      readyStream: readyStream,
    );
  }

  Future<void> enqueueSendBack({
    required String activityFacilityId,
    required String userType,
  }) async {
    final service = FlutterBackgroundService();

    if (await service.isRunning()) {
      await ensureAndroidNotificationPermission();
      service.invoke(kCmdForeground, {
        'title': operationTitle(OperationTypes.sendBack),
        'content': 'Preparing send back…',
      });
      await _forceStartJobImmediately(
        service: service,
        method: kMethodSendBack,
        payload: <String, dynamic>{
          'activityFacilityId': activityFacilityId,
          'userType': userType,
        },
        logTag: '[UI]',
        readyStream: null,
      );
      return;
    }

    final readyStream = service.on(kEvtReady);
    await service.startService();
    await service.isRunning();

    await _forceStartJobImmediately(
      service: service,
      method: kMethodSendBack,
      payload: <String, dynamic>{
        'activityFacilityId': activityFacilityId,
        'userType': userType,
      },
      logTag: '[UI]',
      readyStream: readyStream,
    );
  }

  Future<void> enqueueScheduleVisitSubmission({
    required String scheduledVisitId,
    required String userType,
  }) async {
    final svc = FlutterBackgroundService();

    if (await svc.isRunning()) {
      AppLogger.instance.info(
        '[BG-CTL] service already running -> submit_schedule_visit directly',
      );

      await ensureAndroidNotificationPermission();
      svc.invoke(kCmdForeground, {
        'title': 'Submitting visit report',
        'content': 'Preparing visit submission…',
      });

      await _forceStartJobImmediately(
        service: svc,
        method: kMethodSubmitVisit,
        payload: {
          'scheduledVisitId': scheduledVisitId,
          'userType': userType,
        },
        logTag: '[BG-CTL]',
        readyStream: null,
      );
      return;
    }

    final readyStream = svc.on(kEvtReady);

    await svc.startService();
    await svc.isRunning();

    svc.invoke(kCmdForeground, {
      'title': 'Submitting visit report',
      'content': 'Preparing visit submission…',
    });

    await _forceStartJobImmediately(
      service: svc,
      method: kMethodSubmitVisit,
      payload: {
        'scheduledVisitId': scheduledVisitId,
        'userType': userType,
      },
      logTag: '[BG-CTL]',
      readyStream: readyStream,
    );
  }

  Future<void> stopNow() async {
    final service = FlutterBackgroundService();
    if (await service.isRunning()) {
      service.invoke(kCmdStop);
    }
  }

  Future<void> _forceStartJobImmediately({
    required FlutterBackgroundService service,
    required String method,
    required Map<String, dynamic> payload,
    required String logTag,
    Stream<dynamic>? readyStream,
  }) async {
    final reqId = DateTime.now().microsecondsSinceEpoch.toString();
    final nextPayload = <String, dynamic>{...payload, '_reqId': reqId};

    AppLogger.instance
        .info('$logTag forcing immediate start for $method reqId=$reqId');

    // 1) Fire immediately (no wait)
    service.invoke(method, nextPayload);

    // 2) Fire again when BG signals ready (no wait / just a re-kick)
    if (readyStream != null) {
      unawaited(
        readyStream.first.then((_) {
          service.invoke(method, nextPayload);
        }).catchError((_) {}),
      );
    }

    // 3) Extra tiny safety re-kick (non-blocking). This does NOT delay UI.
    //    Helps when `kEvtReady` arrives before the listener is attached.
    Timer(const Duration(milliseconds: 200), () {
      service.invoke(method, nextPayload);
    });
  }
}

String _pretty(Object? e) {
  final s = e?.toString() ?? 'Failed.';
  final lower = s.toLowerCase();

  if (lower.contains('session_expired') ||
      lower.contains('status code of 401') ||
      lower.contains('status code: 401')) {
    return 'SESSION_EXPIRED';
  }
  return normalizeFriendlyNetworkErrorMessage(s);
}

@pragma('vm:entry-point')
bool _onIosBackground(ServiceInstance service) {
  WidgetsFlutterBinding.ensureInitialized();
  return true;
}

@pragma('vm:entry-point')
void onStart(ServiceInstance service) async {
  WidgetsFlutterBinding.ensureInitialized();

  DartPluginRegistrant.ensureInitialized();
  final envFuture = envConfig.initialize();
  final isarFuture = Constants().isar;

  if (service is AndroidServiceInstance) {
    service.setAsForegroundService();
    await service.setForegroundNotificationInfo(
      title: 'Submitting assets',
      content: 'Preparing…',
    );
  }

  // --- Request de-dupe (for "invoke now + re-invoke on kEvtReady" reliability) ---
  // UI sends a `_reqId` so that if the same request is invoked multiple times
  // (e.g., first invoke dropped before BG listeners attach), BG only runs it once.
  final List<String> _handledReqIdsOrder = <String>[];
  final Set<String> _handledReqIds = <String>{};

  bool _shouldDropDuplicate(dynamic event) {
    if (event is! Map) return false;
    final id = (event['_reqId'] ?? '').toString();
    if (id.isEmpty) return false;

    if (_handledReqIds.contains(id)) {
      AppLogger.instance.info('[BG] duplicate request dropped _reqId=$id');
      return true;
    }

    _handledReqIds.add(id);
    _handledReqIdsOrder.add(id);

    // prevent unbounded growth
    if (_handledReqIdsOrder.length > 200) {
      final old = _handledReqIdsOrder.removeAt(0);
      _handledReqIds.remove(old);
    }
    return false;
  }

  service.on(kMethodSubmit).listen((payload) async {
    if (_shouldDropDuplicate(payload)) return;
    final isar = await isarFuture;
    await envFuture;

    final activityFacilityId = payload?['activityFacilityId'] as String?;
    final facilityId = payload?['facilityId'] as String?;
    final userType = payload?['userType'] as String?;
    if (activityFacilityId == null || userType == null) return;

    try {
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        status: OperationStatuses.running,
        stageKey: 'starting_secure_upload_service',
        completedSteps: 3,
        service: service,
      );

      await _performSubmissionForActivityFacility(
        isar: isar,
        activityFacilityId: activityFacilityId,
        facilityId: facilityId!,
        userType: userType,
        service: service,
      );

      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        status: OperationStatuses.success,
        stageKey: 'submission_successful',
        completedSteps: submitStages.length,
        service: service,
      );

      service.invoke(kEvtDone, {'activityFacilityId': activityFacilityId});
      await _stopServiceIfIdle(isar: isar, service: service);
    } catch (e, st) {
      AppLogger.instance.info('$e\n$st', title: "[BG] ERROR:");

      final msg = _pretty(e);
      final stageKey =
          (payload?['stageKey'] as String?) ?? 'preparing_submission';
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        status: OperationStatuses.failed,
        stageKey: stageKey,
        completedSteps: 0,
        error: msg,
        service: service,
      );
      service.invoke(kEvtError, {
        'activityFacilityId': activityFacilityId,
        'message': msg,
        'stageKey': stageKey
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    }
  });

  service.on(kMethodReject).listen((payload) async {
    if (_shouldDropDuplicate(payload)) return;
    final isar = await isarFuture;
    await envFuture;
    final activityFacilityId = payload?['activityFacilityId'] as String?;
    final userType = payload?['userType'] as String?;
    final txList = (payload?['transactions'] as List?)?.cast<Map>() ?? const [];
    if (activityFacilityId == null || userType == null) return;

    try {
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        status: OperationStatuses.running,
        stageKey: 'preparing_rejection',
        completedSteps: 1,
        service: service,
      );

      await _performRejectionForActivityFacility(
        isar: isar,
        activityFacilityId: activityFacilityId,
        userType: userType,
        transactions: txList.map((m) => Map<String, dynamic>.from(m)).toList(),
        service: service,
      );

      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        status: OperationStatuses.success,
        stageKey: 'rejection_successful',
        completedSteps: rejectionStages.length,
        service: service,
      );
      service
          .invoke(kEvtRejectDone, {'activityFacilityId': activityFacilityId});
      await _stopServiceIfIdle(isar: isar, service: service);
    } catch (e, st) {
      AppLogger.instance.info('$e\n$st', title: "[BG][REJECT] ERROR: ");

      final msg = _pretty(e);
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        status: OperationStatuses.failed,
        stageKey: 'submitting_rejection',
        completedSteps: 0,
        error: msg,
        service: service,
      );

      service.invoke(kEvtRejectError, {
        'activityFacilityId': activityFacilityId,
        'message': msg,
        'stageKey': 'submitting_rejection',
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    }
  });

  service.on(kMethodSendBack).listen((payload) async {
    if (_shouldDropDuplicate(payload)) return;
    final isar = await isarFuture;
    await envFuture;

    final activityFacilityId = payload?['activityFacilityId'] as String?;
    final userType = payload?['userType'] as String?;
    if (activityFacilityId == null || userType == null) return;

    try {
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.sendBack,
        status: OperationStatuses.running,
        stageKey: 'preparing_send_back',
        completedSteps: 1,
        service: service,
      );

      await _performSendBackForActivityFacility(
        isar: isar,
        activityFacilityId: activityFacilityId,
        userType: userType,
        service: service,
      );

      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.sendBack,
        status: OperationStatuses.success,
        stageKey: 'send_back_successful',
        completedSteps: sendBackStages.length,
        service: service,
      );
      service.invoke(kEvtSendBackDone, {
        'activityFacilityId': activityFacilityId,
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    } catch (e, st) {
      AppLogger.instance.info('$e\n$st', title: "[BG][SEND_BACK] ERROR: ");
      final msg = _pretty(e);
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.sendBack,
        status: OperationStatuses.failed,
        stageKey: 'sending_report_back',
        completedSteps: 0,
        error: msg,
        service: service,
      );
      service.invoke(kEvtSendBackError, {
        'activityFacilityId': activityFacilityId,
        'message': msg,
        'stageKey': 'sending_report_back',
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    }
  });

  service.on(kMethodSubmitVisit).listen((payload) async {
    if (_shouldDropDuplicate(payload)) return;
    final isar = await isarFuture;
    await envFuture;

    final visitId = payload?['scheduledVisitId'] as String?;
    final userType = payload?['userType'] as String?;

    if (visitId == null || userType == null) {
      return;
    }

    try {
      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        operationType: OperationTypes.submitVisit,
        status: 'queued',
        stageKey: 'preparing_submission',
      );

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        operationType: OperationTypes.submitVisit,
        status: 'running',
        stageKey: 'starting_secure_upload_service',
      );

      await _performScheduleVisitSubmission(
        isar: isar,
        scheduledVisitId: visitId,
        userType: userType,
      );

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        operationType: OperationTypes.submitVisit,
        status: 'success',
        stageKey: 'submission_successful',
      );

      service.invoke(kEvtScheduleVisitDone, {
        'scheduledVisitId': visitId,
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    } catch (e, st) {
      AppLogger.instance.error(
        title: '[BG] _performScheduleVisitSubmission failed',
        message: e.toString(),
        stackTrace: st,
      );

      String msg;
      final str = e.toString();
      if (str.contains('FORM_NOT_FILLED')) {
        msg =
            'Visit form is not filled yet. Please fill the AMC form before submitting.';
      } else {
        msg = 'Something went wrong while submitting the visit report.';
      }

      await writeJobStatus(
        isar: isar,
        activityFacilityId: visitId,
        operationType: OperationTypes.submitVisit,
        status: 'failed',
        stageKey: 'finalizing_workflow_submission',
        error: msg,
      );

      service.invoke(kEvtScheduleVisitError, {
        'scheduledVisitId': visitId,
        'message': msg,
      });
      await _stopServiceIfIdle(isar: isar, service: service);
    }
  });

  service.on(kCmdStop).listen((_) async {
    if (service is AndroidServiceInstance) {
      service.setAsBackgroundService();
    }
    await service.stopSelf();
  });

  service.on(kCmdForeground).listen((data) async {
    if (service is AndroidServiceInstance) {
      service.setAsForegroundService();
      await service.setForegroundNotificationInfo(
        title: (data?['title'] as String?) ?? 'Submitting assets',
        content: (data?['content'] as String?) ?? 'Working…',
      );
    }
  });

  service.invoke(kEvtReady);
}

Future<void> writeJobStatus({
  required Isar isar,
  required String activityFacilityId,
  required String operationType,
  required String status,
  required String stageKey,
  String? error,
}) async {
  final stages = stagesForOperation(operationType);
  final completed = status == OperationStatuses.success ? stages.length : 0;
  await _writeOperationStage(
    isar: isar,
    activityFacilityId: activityFacilityId,
    operationType: operationType,
    status: status,
    stageKey: stageKey,
    completedSteps: completed,
    error: error,
  );
}

Future<void> _writeOperationStage({
  required Isar isar,
  required String activityFacilityId,
  required String operationType,
  required String status,
  required String stageKey,
  required int completedSteps,
  int stageProgressCurrent = 0,
  int stageProgressTotal = 0,
  String? error,
  bool incrementRetry = false,
  ServiceInstance? service,
}) async {
  final repo = OperationProgressRepository(isar);
  final stages = stagesForOperation(operationType);
  await repo.upsertJob(
    activityFacilityId: activityFacilityId,
    operationType: operationType,
    status: status,
    stageKey: stageKey,
    completedSteps: completedSteps,
    totalSteps: stages.length,
    stageProgressCurrent: stageProgressCurrent,
    stageProgressTotal: stageProgressTotal,
    errorMessage: error,
    incrementRetry: incrementRetry,
  );

  final label = stageForKey(operationType, stageKey).label;
  if (service is AndroidServiceInstance) {
    await service.setForegroundNotificationInfo(
      title: operationTitle(operationType),
      content: label,
    );
  } else if (service != null) {
    service.invoke(kCmdForeground, {
      'title': operationTitle(operationType),
      'content': label,
    });
  }

  service?.invoke(kEvtProgress, {
    'activityFacilityId': activityFacilityId,
    'operationType': operationType,
    'status': status,
    'stageKey': stageKey,
    'stageLabel': label,
    'completedSteps': completedSteps,
    'totalSteps': stages.length,
    'stageProgressCurrent': stageProgressCurrent,
    'stageProgressTotal': stageProgressTotal,
    'progressPercent': progressPercent(
      completedSteps: completedSteps,
      totalSteps: stages.length,
      stageProgressCurrent: stageProgressCurrent,
      stageProgressTotal: stageProgressTotal,
    ),
    if (error != null) 'message': error,
  });
}

Future<void> _stopServiceIfIdle({
  required Isar isar,
  required ServiceInstance service,
}) async {
  final jobs = await isar.cacheSubmissionJobs.where().findAll();
  final hasActiveJobs = jobs.any(
    (job) =>
        job.status == OperationStatuses.queued ||
        job.status == OperationStatuses.running ||
        job.status == OperationStatuses.partial,
  );
  if (hasActiveJobs) {
    return;
  }
  if (service is AndroidServiceInstance) {
    service.setAsBackgroundService();
  }
  await service.stopSelf();
}

Future<void> _saveCheckpoint({
  required Isar isar,
  required String activityFacilityId,
  required String operationType,
  required String checkpointKey,
  required String itemKey,
  required String status,
  String? remoteId,
  String? error,
  Map<String, dynamic>? payload,
}) async {
  await OperationProgressRepository(isar).saveCheckpoint(
    activityFacilityId: activityFacilityId,
    operationType: operationType,
    checkpointKey: checkpointKey,
    itemKey: itemKey,
    status: status,
    remoteId: remoteId,
    error: error,
    payload: payload,
  );
}

Future<CacheOperationCheckpoint?> _getCheckpoint({
  required Isar isar,
  required String activityFacilityId,
  required String operationType,
  required String checkpointKey,
  required String itemKey,
}) {
  return OperationProgressRepository(isar).getCheckpoint(
    activityFacilityId: activityFacilityId,
    operationType: operationType,
    checkpointKey: checkpointKey,
    itemKey: itemKey,
  );
}

Future<List<R>> _runBatches<T, R>({
  required List<T> items,
  required Future<R> Function(T item) run,
  int concurrency = 2,
}) async {
  final out = <R>[];
  for (var i = 0; i < items.length; i += concurrency) {
    final batch = items.skip(i).take(concurrency).toList();
    final result = await Future.wait(batch.map(run));
    out.addAll(result);
  }
  return out;
}

Future<void> _runUploadStage<T>({
  required Isar isar,
  required String activityFacilityId,
  required String stageKey,
  required int completedSteps,
  required List<T> items,
  required int totalItems,
  required ServiceInstance service,
  required Future<void> Function(T item) run,
  int initialStageProgressCurrent = 0,
  int concurrency = 3,
}) async {
  var completedItems = initialStageProgressCurrent;

  Future<void> markProgress() async {
    completedItems += 1;
    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.running,
      stageKey: stageKey,
      completedSteps: completedSteps,
      stageProgressCurrent: completedItems,
      stageProgressTotal: totalItems,
      service: service,
    );
  }

  await _writeOperationStage(
    isar: isar,
    activityFacilityId: activityFacilityId,
    operationType: OperationTypes.submit,
    status: OperationStatuses.running,
    stageKey: stageKey,
    completedSteps: completedSteps,
    stageProgressCurrent: initialStageProgressCurrent,
    stageProgressTotal: totalItems,
    service: service,
  );

  if (items.isEmpty) {
    return;
  }

  await _runBatches<T, void>(
    items: items,
    concurrency: concurrency,
    run: (item) async {
      await run(item);
      await markProgress();
    },
  );
}

bool _isInstallBomPdfNameOrPath(String value) {
  final v = value.trim();
  if (v.isEmpty) return false;

  final normalized = normalizeReportNameType(v);
  if ((normalized ?? '').toUpperCase() == installationReportBom) {
    return true;
  }

  final s = v.toLowerCase();
  return s.contains('installation') && s.contains('bom');
}

Future<void> _performSubmissionForActivityFacility({
  required Isar isar,
  required String activityFacilityId,
  required String facilityId,
  required String userType,
  required ServiceInstance service,
}) async {
  try {
    final repo = AssetRepository();
    const types = ['inverter', 'battery', 'panel'];
    final now = DateTime.now().toUtc();
    final currentUserId = await SecureStore().getSelectedIndividual() ?? '';
    final remoteRepo = ActivityFacilityRemoteRepository();
    final workflowRepo = ActivityFacilityWorkflowRepository();

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.running,
      stageKey: 'checking_saved_asset_data',
      completedSteps: 2,
      service: service,
    );

    final assetsByType = <String, List<CacheAddNewAsset>>{};
    final specByType = <String, CacheSpecification>{};
    final detailByType = <String, CacheAssetDetail>{};

    for (final type in types) {
      final assets = await isar.cacheAddNewAssets
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findAll();
      if (assets.isEmpty) {
        throw Exception("No cached assets found for type $type.");
      }

      final spec = await isar.cacheSpecifications
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findFirst();
      final detail = await isar.cacheAssetDetails
          .where()
          .activityFacilityIdEqualTo(activityFacilityId)
          .filter()
          .assetTypeEqualTo(type)
          .findFirst();
      if (spec == null || detail == null) {
        throw Exception("Missing specification or detail for type $type.");
      }

      assetsByType[type] = assets;
      specByType[type] = spec;
      detailByType[type] = detail;
    }

    final workflowMedia = await isar.cacheMediaUploads
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
    final installationImages = await isar.cacheInstallationImages
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
    final completionCertificates = await isar
        .cacheInstallationCompletionCertificates
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
    final assetHandoverDocuments = await isar.cacheAssetHandoverDocuments
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();

    final workflowMediaItems =
        workflowMedia.where((item) => item.filePath.isNotEmpty).toList();
    final installationImageItems =
        installationImages.where((item) => item.photoPath.isNotEmpty).toList();
    final completionCertificateItems = completionCertificates
        .where((item) => item.filePath.isNotEmpty)
        .toList();
    final assetHandoverDocumentItems = assetHandoverDocuments
        .where((item) => item.filePath.isNotEmpty)
        .toList();
    final assetPhotoItems = assetsByType.values
        .expand((items) => items)
        .where((a) => a.photoPath.isNotEmpty)
        .toList();
    final totalAssetMediaUploads = workflowMediaItems.length +
        installationImageItems.length +
        completionCertificateItems.length +
        assetHandoverDocumentItems.length +
        assetPhotoItems.length;

    await _runUploadStage<CacheMediaUpload>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_asset_media',
      completedSteps: 4,
      items: workflowMediaItems,
      totalItems: totalAssetMediaUploads,
      service: service,
      concurrency: 3,
      run: (media) async {
        final itemKey = media.id.toString();
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_media_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(media.filePath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_media_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    await _runUploadStage<CacheAddNewAsset>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_asset_media',
      completedSteps: 4,
      items: assetPhotoItems,
      totalItems: totalAssetMediaUploads,
      service: service,
      initialStageProgressCurrent: workflowMediaItems.length,
      concurrency: 3,
      run: (asset) async {
        final itemKey = asset.id.toString();
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_photo_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(asset.photoPath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_photo_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    await _runUploadStage<CacheInstallationImage>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_asset_media',
      completedSteps: 4,
      items: installationImageItems,
      totalItems: totalAssetMediaUploads,
      service: service,
      initialStageProgressCurrent:
          workflowMediaItems.length + assetPhotoItems.length,
      concurrency: 3,
      run: (entry) async {
        final itemKey = entry.id.toString();
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'installation_image_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(entry.photoPath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'installation_image_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    await _runUploadStage<CacheInstallationCompletionCertificate>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_asset_media',
      completedSteps: 4,
      items: completionCertificateItems,
      totalItems: totalAssetMediaUploads,
      service: service,
      initialStageProgressCurrent: workflowMediaItems.length +
          assetPhotoItems.length +
          installationImageItems.length,
      concurrency: 3,
      run: (entry) async {
        final itemKey = entry.id.toString();
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'installation_completion_certificate_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(entry.filePath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'installation_completion_certificate_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    await _runUploadStage<CacheAssetHandoverDocument>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_asset_media',
      completedSteps: 4,
      items: assetHandoverDocumentItems,
      totalItems: totalAssetMediaUploads,
      service: service,
      initialStageProgressCurrent: workflowMediaItems.length +
          assetPhotoItems.length +
          installationImageItems.length +
          completionCertificateItems.length,
      concurrency: 3,
      run: (entry) async {
        final itemKey = entry.id.toString();
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_handover_document_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(entry.filePath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_handover_document_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    final workflowDocuments = <Document>[];
    for (final media
        in workflowMedia.where((item) => item.filePath.isNotEmpty)) {
      final checkpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'asset_media_upload',
        itemKey: media.id.toString(),
      );
      final remoteId = checkpoint?.remoteId;
      if (remoteId == null || remoteId.isEmpty) continue;
      workflowDocuments.add(
        Document(
          documentType: '${media.assetType}-${media.itemType}',
          fileStore: remoteId,
          documentUid: 'DOC-${media.assetType}-${media.itemType}-${media.id}',
          geoLocation: GeoLocation(
            latitude: media.latitude,
            longitude: media.longitude,
          ),
        ),
      );
    }

    final installationImageDocuments = <Document>[];
    for (final entry in installationImageItems) {
      final checkpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'installation_image_upload',
        itemKey: entry.id.toString(),
      );
      final remoteId = checkpoint?.remoteId;
      if (remoteId == null || remoteId.isEmpty) continue;
      installationImageDocuments.add(
        Document(
          documentType: 'INSTALLATION_IMAGE-${entry.code}',
          fileStore: remoteId,
          documentUid: 'INSTALLATION-IMAGE-${entry.code}-${entry.id}',
          geoLocation: GeoLocation(
            latitude: entry.latitude,
            longitude: entry.longitude,
          ),
        ),
      );
    }

    final completionCertificateDocuments = <Document>[];
    for (final entry in completionCertificateItems) {
      final checkpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'installation_completion_certificate_upload',
        itemKey: entry.id.toString(),
      );
      final remoteId = checkpoint?.remoteId;
      if (remoteId == null || remoteId.isEmpty) continue;
      completionCertificateDocuments.add(
        Document(
          documentType: 'INSTALLATION_COMPLETION_CERTIFICATE',
          fileStore: remoteId,
          documentUid:
              'INSTALLATION-COMPLETION-CERTIFICATE-${entry.fileType}-${entry.id}',
          geoLocation: GeoLocation(
            latitude: entry.latitude,
            longitude: entry.longitude,
          ),
        ),
      );
    }

    final assetHandoverDocumentsPayload = <Document>[];
    for (final entry in assetHandoverDocumentItems) {
      final checkpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'asset_handover_document_upload',
        itemKey: entry.id.toString(),
      );
      final remoteId = checkpoint?.remoteId;
      if (remoteId == null || remoteId.isEmpty) continue;
      assetHandoverDocumentsPayload.add(
        Document(
          documentType: 'ASSET_HANDOVER_DOCUMENT',
          fileStore: remoteId,
          documentUid: 'ASSET-HANDOVER-DOCUMENT-${entry.fileType}-${entry.id}',
          geoLocation: GeoLocation(
            latitude: entry.latitude,
            longitude: entry.longitude,
          ),
        ),
      );
    }

    final completionReports = await isar.cacheCompletionReports
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findAll();
    final usableReports = completionReports.where((report) {
      if (report.filePath.isEmpty) return false;
      final fileName = (report.fileName ?? '').trim().isNotEmpty
          ? report.fileName!.trim()
          : report.filePath;
      return !_isInstallBomPdfNameOrPath(fileName);
    }).toList();

    await _runUploadStage<CacheCompletionReport>(
      isar: isar,
      activityFacilityId: activityFacilityId,
      stageKey: 'uploading_completion_reports',
      completedSteps: 5,
      items: usableReports,
      totalItems: usableReports.length,
      service: service,
      concurrency: 3,
      run: (report) async {
        final itemKey = report.entryId;
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'completion_report_upload',
          itemKey: itemKey,
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success &&
            (checkpoint?.remoteId?.isNotEmpty ?? false)) {
          return;
        }
        final remoteId = await getFilestoreUrl(report.filePath);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'completion_report_upload',
          itemKey: itemKey,
          status: OperationCheckpointStatuses.success,
          remoteId: remoteId,
        );
      },
    );

    final completionDocuments = <Document>[];
    for (final report in usableReports) {
      final checkpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'completion_report_upload',
        itemKey: report.entryId,
      );
      final remoteId = checkpoint?.remoteId;
      if (remoteId == null || remoteId.isEmpty) continue;
      completionDocuments.add(
        Document(
          documentType: 'INSTALLATION_REPORT',
          fileStore: remoteId,
          documentUid: 'INSTALLATION-REPORT-${report.fileType}-$remoteId',
          geoLocation: GeoLocation(
            latitude: report.latitude,
            longitude: report.longitude,
          ),
        ),
      );
    }

    final resolvedBomUserType = await BomRepository().resolveBomUserType(
      isar: isar,
      activityFacilityId: activityFacilityId,
      userType: userType,
    );

    if (resolvedBomUserType != null) {
      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        status: OperationStatuses.running,
        stageKey: 'generating_bom_pdf',
        completedSteps: 6,
        service: service,
      );

      final bomCheckpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'bom_pdf',
        itemKey: activityFacilityId,
      );
      String bomFileStoreId = bomCheckpoint?.remoteId ?? '';
      if (bomFileStoreId.isEmpty) {
        final installationReportPdfDocuments = <Document>[
          ...installationImageDocuments,
          ...assetHandoverDocumentsPayload,
          ...completionCertificateDocuments,
        ];
        bomFileStoreId = await BomRepository().generateBomPdf(
          isar: isar,
          activityFacilityId: activityFacilityId,
          userType: userType,
          documents: installationReportPdfDocuments,
        );
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'bom_pdf',
          itemKey: activityFacilityId,
          status: OperationCheckpointStatuses.success,
          remoteId: bomFileStoreId,
        );
      }

      workflowDocuments.removeWhere((d) =>
          (d.documentType ?? '').toUpperCase().contains(installationReportBom));
      final lat = workflowDocuments.isNotEmpty
          ? workflowDocuments.first.geoLocation?.latitude ?? ''
          : '';
      final lon = workflowDocuments.isNotEmpty
          ? workflowDocuments.first.geoLocation?.longitude ?? ''
          : '';
      workflowDocuments.add(
        Document(
          documentType: installationReportBom,
          fileStore: bomFileStoreId,
          documentUid:
              'BOM-$activityFacilityId-${DateTime.now().millisecondsSinceEpoch}',
          geoLocation: GeoLocation(latitude: lat, longitude: lon),
        ),
      );

      await _writeOperationStage(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        status: OperationStatuses.running,
        stageKey: 'submitting_bom',
        completedSteps: 7,
        service: service,
      );

      final bomSubmissionCheckpoint = await _getCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'bom_submit',
        itemKey: activityFacilityId,
      );
      if (bomSubmissionCheckpoint?.status !=
          OperationCheckpointStatuses.success) {
        final tenantId = envConfig.variables.tenantId;
        await BomRepository().submitMergedForProject(
          isar: isar,
          activityFacilityId: activityFacilityId,
          tenantId: tenantId,
          facilityId: facilityId,
          assignUserUuid: currentUserId,
          userType: userType,
        );
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'bom_submit',
          itemKey: activityFacilityId,
          status: OperationCheckpointStatuses.success,
        );
      }
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.running,
      stageKey: 'submitting_assets',
      completedSteps: 8,
      service: service,
    );

    for (final type in types) {
      final spec = specByType[type]!;
      final detail = detailByType[type]!;
      for (final saved in assetsByType[type]!) {
        final checkpoint = await _getCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_submit',
          itemKey: saved.id.toString(),
        );
        if (checkpoint?.status == OperationCheckpointStatuses.success) {
          continue;
        }

        final documents = <Document>[];
        if (saved.photoPath.isNotEmpty) {
          final photoCheckpoint = await _getCheckpoint(
            isar: isar,
            activityFacilityId: activityFacilityId,
            operationType: OperationTypes.submit,
            checkpointKey: 'asset_photo_upload',
            itemKey: saved.id.toString(),
          );
          final photoId = photoCheckpoint?.remoteId ?? saved.photoPath;
          documents.add(
            Document(
              id: saved.documentId,
              documentType: 'ASSET',
              fileStore: photoId,
              documentUid: 'DOC-ASSET-${saved.serialNumber}',
              additionalDetailsJson: null,
              geoLocation: GeoLocation(
                latitude: saved.latitude,
                longitude: saved.longitude,
              ),
            ),
          );
        }

        final years = parseWarrantyYears(detail.warranty);
        final startIso = years > 0 ? now.toIso8601String() : '';
        final endIso = years > 0
            ? now.add(Duration(days: 365 * years)).toIso8601String()
            : '';

        final assetDetails = AssetDetails(
          totalCapacity: spec.totalCapacity,
          totalCapacityUnit: spec.totalCapacityUnit,
          totalCapacityUOM: spec.totalCapacityUnit,
          currentUnit:
              type == ASSET_TYPES.INVERTER.name.toLowerCase() ? '1' : null,
          capacityUnit: (type == ASSET_TYPES.BATTERY.name.toLowerCase() ||
                  type == ASSET_TYPES.PANEL.name.toLowerCase())
              ? saved.capacityUnit
              : null,
          panelCapacity: type == ASSET_TYPES.PANEL.name.toLowerCase()
              ? double.parse(saved.panelCapacity!)
              : null,
          batteryCapacity: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? double.parse(saved.batteryCapacity!)
              : null,
          batteryVoltage: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? double.parse(saved.batteryVoltage!)
              : null,
          batteryType: type == ASSET_TYPES.BATTERY.name.toLowerCase()
              ? saved.batteryType
              : null,
          voltageUnit: (type == ASSET_TYPES.BATTERY.name.toLowerCase() ||
                  type == ASSET_TYPES.INVERTER.name.toLowerCase())
              ? saved.voltageUnit
              : null,
          inverterCapacity: type == ASSET_TYPES.INVERTER.name.toLowerCase()
              ? double.parse(saved.inverterCapacity!)
              : null,
          inverterCapacityUnit: type == ASSET_TYPES.INVERTER.name.toLowerCase()
              ? saved.inverterCapacityUnit
              : null,
        );

        final audit =
            AuditDetails(lastModifiedBy: currentUserId, lastModified: now);
        final assetModel = Asset(
          assetId: saved.assetId,
          tenantId: envConfig.variables.tenantId,
          activityFacilityID: activityFacilityId,
          facilityID: facilityId,
          assetTypeID: type.toUpperCase(),
          system: spec.system,
          serialNumber: saved.serialNumber,
          brandID: detail.brand,
          assetDetails: assetDetails,
          warrantyStartDate: startIso,
          warrantyDuration: years,
          warrantyEndDate: endIso,
          modelNumber: '',
          wfStatus: 'CREATED',
          isActive: true,
          documents: documents,
          auditDetails: (saved.assetId?.isNotEmpty ?? false) ? audit : null,
        );

        final updatedAsset =
            await repo.createOrUpdateAsset(asset: assetModel, isar: isar);
        await _saveCheckpoint(
          isar: isar,
          activityFacilityId: activityFacilityId,
          operationType: OperationTypes.submit,
          checkpointKey: 'asset_submit',
          itemKey: saved.id.toString(),
          status: OperationCheckpointStatuses.success,
          remoteId: updatedAsset.assetId,
        );
      }
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.running,
      stageKey: 'finalizing_workflow_submission',
      completedSteps: 9,
      service: service,
    );

    final workflowCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      checkpointKey: 'workflow_finalize',
      itemKey: activityFacilityId,
    );
    if (workflowCheckpoint?.status != OperationCheckpointStatuses.success) {
      await remoteRepo.updateActivityFacilityWorkflow(
        activityFacilityId: activityFacilityId,
        action: userType == USER_TYPES.FIELD_STAFF.name
            ? WORKFLOW_ACTIONS.SUBMIT_REPORT_A.name
            : WORKFLOW_ACTIONS.SUBMIT_REPORT_B.name,
        documents: [
          ...workflowDocuments,
          ...installationImageDocuments,
          ...completionCertificateDocuments,
          ...assetHandoverDocumentsPayload,
          ...completionDocuments,
        ],
      );
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'workflow_finalize',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      status: OperationStatuses.running,
      stageKey: 'cleaning_up_local_cache',
      completedSteps: 10,
      service: service,
    );

    final cleanupCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.submit,
      checkpointKey: 'cleanup',
      itemKey: activityFacilityId,
    );
    if (cleanupCheckpoint?.status != OperationCheckpointStatuses.success) {
      await UnsubmittedActivityFacilityRepository(isar)
          .delete(activityFacilityId, userType);
      await UnsubmittedActivityFacilityRepository(isar)
          .deleteAddNewAsset(activityFacilityId);
      await PrefilledActivityFacilityRepository(isar)
          .delete(activityFacilityId: activityFacilityId, userType: userType);
      await CompletionReportRepository(isar)
          .delete(projectId: activityFacilityId);
      await BomRepository()
          .delete(isar: isar, activityFacilityId: activityFacilityId);
      await BomRepository()
          .deleteAllBomDocs(isar: isar, activityFacilityId: activityFacilityId);
      await InstallationImagesRepository(isar).deleteAllCachedImages(
        activityFacilityId: activityFacilityId,
      );
      await InstallationCompletionCertificateRepository(isar).clearProject(
        activityFacilityId: activityFacilityId,
      );
      await AssetHandoverDocumentRepository(isar).clearProject(
        activityFacilityId: activityFacilityId,
      );
      await workflowRepo.deleteWorkflowMediaDocs(
        isar: isar,
        activityFacilityId: activityFacilityId,
      );
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.submit,
        checkpointKey: 'cleanup',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }
  } catch (e) {
    AppLogger.instance.info("e ${e.toString()}");
    throw PlainError(_pretty(e));
  }
}

Future<void> _performRejectionForActivityFacility({
  required Isar isar,
  required String activityFacilityId,
  required String userType,
  required List<Map<String, dynamic>> transactions,
  required ServiceInstance service,
}) async {
  try {
    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      status: OperationStatuses.running,
      stageKey: 'validating_rejection_reasons',
      completedSteps: 2,
      service: service,
    );
    await _saveCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      checkpointKey: 'rejection_payload',
      itemKey: activityFacilityId,
      status: OperationCheckpointStatuses.success,
      payload: <String, dynamic>{'transactions': transactions},
    );

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      status: OperationStatuses.running,
      stageKey: 'loading_workflow_documents',
      completedSteps: 3,
      service: service,
    );
    final docsCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      checkpointKey: 'workflow_documents',
      itemKey: activityFacilityId,
    );
    final workflowDocuments = await ActivityFacilityWorkflowRepository()
        .collectWorkflowDocsForRejection(
      isar: isar,
      activityFacilityId: activityFacilityId,
    );
    if (docsCheckpoint?.status != OperationCheckpointStatuses.success) {
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        checkpointKey: 'workflow_documents',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      status: OperationStatuses.running,
      stageKey: 'submitting_rejection',
      completedSteps: 4,
      service: service,
    );
    final submitCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      checkpointKey: 'rejection_submit',
      itemKey: activityFacilityId,
    );
    if (submitCheckpoint?.status != OperationCheckpointStatuses.success) {
      await AssetRepository().submitRejection(
        activityFacilityId: activityFacilityId,
        transactions: transactions.map((m) => Transaction.fromJson(m)).toList(),
        documents: workflowDocuments,
      );
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        checkpointKey: 'rejection_submit',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      status: OperationStatuses.running,
      stageKey: 'cleaning_up_local_cache',
      completedSteps: 5,
      service: service,
    );
    final cleanupCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.reject,
      checkpointKey: 'cleanup',
      itemKey: activityFacilityId,
    );
    if (cleanupCheckpoint?.status != OperationCheckpointStatuses.success) {
      await UnsubmittedActivityFacilityRepository(isar)
          .delete(activityFacilityId, userType);
      await PrefilledActivityFacilityRepository(isar)
          .delete(activityFacilityId: activityFacilityId, userType: userType);
      await CompletionReportRepository(isar)
          .delete(projectId: activityFacilityId);
      await BomRepository()
          .delete(isar: isar, activityFacilityId: activityFacilityId);
      await BomRepository()
          .deleteAllBomDocs(isar: isar, activityFacilityId: activityFacilityId);
      await InstallationImagesRepository(isar).deleteAllCachedImages(
        activityFacilityId: activityFacilityId,
      );
      await InstallationCompletionCertificateRepository(isar).clearProject(
        activityFacilityId: activityFacilityId,
      );
      await AssetHandoverDocumentRepository(isar).clearProject(
        activityFacilityId: activityFacilityId,
      );
      await ActivityFacilityWorkflowRepository().deleteWorkflowMediaDocs(
        isar: isar,
        activityFacilityId: activityFacilityId,
      );
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.reject,
        checkpointKey: 'cleanup',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }
  } catch (e) {
    throw PlainError(_pretty(e));
  }
}

Future<void> _performSendBackForActivityFacility({
  required Isar isar,
  required String activityFacilityId,
  required String userType,
  required ServiceInstance service,
}) async {
  try {
    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      status: OperationStatuses.running,
      stageKey: 'validating_current_workflow_state',
      completedSteps: 2,
      service: service,
    );

    final workflowRow = await isar.cacheActivityFacilityWorkflows
        .where()
        .activityFacilityIdEqualTo(activityFacilityId)
        .findFirst();
    if (workflowRow == null) {
      throw Exception('No workflow found for send back.');
    }

    await _saveCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      checkpointKey: 'validation',
      itemKey: activityFacilityId,
      status: OperationCheckpointStatuses.success,
    );

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      status: OperationStatuses.running,
      stageKey: 'sending_report_back',
      completedSteps: 3,
      service: service,
    );
    final sendBackCheckpoint = await _getCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      checkpointKey: 'workflow_update',
      itemKey: activityFacilityId,
    );
    if (sendBackCheckpoint?.status != OperationCheckpointStatuses.success) {
      await ActivityFacilityRemoteRepository().sendBackActivityFacilityWorkflow(
        activityFacilityWorkflow: ActivityFacilityWorkflow(
          activityFacility: workflowRow.activityFacility,
          status: workflowRow.status,
          transactions: workflowRow.transactions,
          workflow: workflowRow.workflow,
        ),
        userType: userType,
        isar: isar,
      );
      await _saveCheckpoint(
        isar: isar,
        activityFacilityId: activityFacilityId,
        operationType: OperationTypes.sendBack,
        checkpointKey: 'workflow_update',
        itemKey: activityFacilityId,
        status: OperationCheckpointStatuses.success,
      );
    }

    await _writeOperationStage(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      status: OperationStatuses.running,
      stageKey: 'refreshing_local_data',
      completedSteps: 4,
      service: service,
    );
    await _saveCheckpoint(
      isar: isar,
      activityFacilityId: activityFacilityId,
      operationType: OperationTypes.sendBack,
      checkpointKey: 'refresh_local',
      itemKey: activityFacilityId,
      status: OperationCheckpointStatuses.success,
    );
  } catch (e) {
    throw PlainError(_pretty(e));
  }
}

Future<void> _performScheduleVisitSubmission({
  required Isar isar,
  required String scheduledVisitId,
  required String userType,
}) async {
  final form = await isar.cacheScheduleVisitFormValues
      .where()
      .scheduledVisitIdEqualTo(scheduledVisitId)
      .filter()
      .userTypeEqualTo(userType)
      .findFirst();

  if (form == null || form.dataJson.trim().isEmpty) {
    throw Exception('Form not filled');
  }

  final responses = jsonDecode(form.dataJson) as Map<String, dynamic>;
  final amcFormRepo = AmcDynamicFormRepository();

  final pdfFileStoreId = await amcFormRepo.generateFormPdf(
      isar: isar, scheduledVisitId: scheduledVisitId, userType: userType);

  if (pdfFileStoreId == null || pdfFileStoreId.isEmpty) {
    throw Exception('Failed to generate AMC PDF');
  }

  final cachedMedia = await isar.cacheAmcMediaUploads
      .where()
      .scheduledVisitIdEqualTo(scheduledVisitId)
      .filter()
      .userTypeEqualTo(userType)
      .findAll();

  final visitDocuments = <Document>[];

  for (final media in cachedMedia) {
    if (media.filePath.isEmpty) continue;

    final fileStoreId = await getFilestoreUrl(media.filePath);

    visitDocuments.add(
      Document(
        documentType: media.itemType,
        fileStore: fileStoreId,
        documentUid:
            'DOC-AMC-${media.itemType}-${DateTime.now().millisecondsSinceEpoch}',
        geoLocation: GeoLocation(
          latitude: media.latitude,
          longitude: media.longitude,
        ),
      ),
    );
  }

  final workflowDocuments = <Document>[];

  final String? mediaLat =
      cachedMedia.isNotEmpty ? cachedMedia.first.latitude : null;
  final String? mediaLon =
      cachedMedia.isNotEmpty ? cachedMedia.first.longitude : null;

  workflowDocuments.add(
    Document(
      documentType: 'AMC_INSTALLATION_FORM',
      fileStore: pdfFileStoreId,
      documentUid:
          'AMC-FORM-$scheduledVisitId-${DateTime.now().millisecondsSinceEpoch}',
      geoLocation: GeoLocation(
        latitude: mediaLat,
        longitude: mediaLon,
      ),
    ),
  );

  final remote = ScheduledVisitRemoteRepository();
  await remote.updateVisitWorkflow(
    visitId: scheduledVisitId,
    schemaCode: "12345678",
    version: 1,
    responses: responses,
    workflowDocuments: workflowDocuments,
    visitDocuments: visitDocuments,
  );

  await PrefilledScheduledVisitRepository(isar)
      .addOrTouch(scheduledVisitId: scheduledVisitId, userType: userType);
  final installationForm = workflowDocuments.first;
  await ScheduledVisitRepository(isar).upsertCacheAmcInstallationForm(
      isar,
      new CacheAmcInstallationForm(
        scheduledVisitId: scheduledVisitId,
        filePath: installationForm.fileStore ?? '',
        latitude: installationForm.geoLocation?.latitude ?? '',
        longitude: installationForm.geoLocation?.longitude ?? '',
        userType: userType,
      ));
}

class PlainError implements Exception {
  final String message;
  PlainError(this.message);
  @override
  String toString() => message;
}
