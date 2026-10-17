import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../lib/blocs/cache_amc_media_upload/cache_amc_media_upload.dart';
import '../../lib/blocs/scheduled_visit_submission/scheduled_visit_submission.dart';
import '../../lib/blocs/selected_amc_origin/selected_amc_origin.dart';
import '../../lib/blocs/selected_scheduled_visit/selected_scheduled_visit.dart';
import '../../lib/model/workflow/workflow.dart';
import '../../lib/pages/amc_media_upload.dart';
import '../../lib/pages/amc_rejection_reasons.dart';
import '../../lib/repositories/report_bookmark_repo.dart';
import '../../lib/router/app_router.dart';
import '../../lib/utils/utils.dart';
import '../repositories/report_bookmark_test.dart' show ReportStore, visit;

class OfflineLocation extends Bloc<LocationEvent, LocationState>
    implements LocationBloc {
  OfflineLocation() : super(const LocationState());
  @override
  void add(LocationEvent event) {}
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected location access');
}

class OfflineMedia
    extends Bloc<CacheAmcMediaUploadEvent, CacheAmcMediaUploadState>
    implements CacheAmcMediaUploadBloc {
  OfflineMedia() : super(const CacheAmcMediaUploadState.initial());
  @override
  void add(CacheAmcMediaUploadEvent event) {}
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected media access');
}

class IdleSubmission
    extends Bloc<ScheduleVisitSubmitEvent, ScheduleVisitSubmitState>
    implements ScheduleVisitSubmitBloc {
  IdleSubmission() : super(const ScheduleVisitSubmitState.initial());
  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw StateError('Unexpected submission');
}

void main() {
  for (final media in [false, true]) {
    for (final empty in [false, true]) {
      testWidgets(
          '${media ? 'AMC media' : 'AMC rejection'} restores offline reasons; empty=$empty',
          (tester) async {
        final repo = AmcBookmarkRepository(
            storage: ReportStore(),
            tenantId: 'tenant',
            userId: 'user',
            userType: 'AMC');
        await repo.save(visit('one').copyWith(
            processInstances: empty
                ? []
                : [
                    Workflow.fromJson({
                      'comment': [
                        {'reason': 'IMAGE_UNCLEAR', 'comment': 'Retake selfie'}
                      ]
                    })
                  ]));
        final restored = (await repo.list()).single;
        final selected = SelectedScheduledVisitBloc()
          ..add(SelectedScheduledVisitEvent.select(restored));
        final origin = SelectedAmcOriginBloc()
          ..add(const SelectedAmcOriginEvent.select(
              FormOrigin.submitForApproval));
        final location = OfflineLocation();
        final cache = OfflineMedia();
        final submission = IdleSubmission();
        final router = AppRouter();
        addTearDown(() async {
          await selected.close();
          await origin.close();
          await location.close();
          await cache.close();
          await submission.close();
          router.dispose();
        });
        await tester.pump();
        await tester.pumpWidget(MultiBlocProvider(
            providers: [
              BlocProvider<SelectedScheduledVisitBloc>.value(value: selected),
              BlocProvider<SelectedAmcOriginBloc>.value(value: origin),
              BlocProvider<LocationBloc>.value(value: location),
              BlocProvider<CacheAmcMediaUploadBloc>.value(value: cache),
              BlocProvider<ScheduleVisitSubmitBloc>.value(value: submission),
            ],
            child: MaterialApp(
                theme: DigitTheme.instance.mobileTheme,
                home: StackRouterScope(
                    controller: router,
                    stateHash: 0,
                    child: Scaffold(
                        body: media
                            ? const AmcMediaUploadPage()
                            : const AmcRejctionReasonsPage())))));
        await tester.pumpAndSettle();
        if (media) {
          await tester.drag(
              find.byType(Scrollable).first, const Offset(0, -600));
          await tester.pumpAndSettle();
        }
        if (!empty) {
          if (media) {
            expect(
                find.byWidgetPredicate((widget) =>
                    widget is DigitCheckbox && widget.label == 'IMAGE_UNCLEAR'),
                findsOneWidget);
          } else {
            expect(find.textContaining('Retake selfie'), findsWidgets);
          }
        }
        expect(tester.takeException(), isNull);
      });
    }
  }
}
