import 'dart:convert';
import 'dart:io';

import 'package:collection/collection.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:recase/recase.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/app_init/app_init.dart';
import '../blocs/asset_rejection/asset_rejection.dart';
import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../model/activity_facility_workflow/activity_facility_workflow.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../model/asset_summary/asset_summary.dart';
import '../model/brand/brand.dart';
import '../model/comment/comment.dart';
import '../model/mdms/mdms.dart';
import '../model/system/system.dart';
import '../model/transaction/transaction.dart';
import '../repositories/app_init_repo.dart' hide envConfig;
import '../router/app_router.dart';
import '../utils/envConfig.dart' as env;
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/files/video_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/images/cached_image.dart';
import '../widgets/progress_indicator/operation_progress_overlay.dart';

class _ReasonEntry {
  String? selectedCode;
  String? selectedName;
  final TextEditingController controller = TextEditingController();
}

@RoutePage()
class AssetSummaryPage extends StatefulWidget {
  const AssetSummaryPage({super.key});

  @override
  State<AssetSummaryPage> createState() => _AssetSummaryPageState();
}

class _AssetSummaryPageState extends State<AssetSummaryPage> {
  String activityFacilityName = "";
  String status = "";
  String assetType = "";
  String userType = "";
  ActivityFacilityWorkflow? selectedActivityFacility;

  final List<_ReasonEntry> _reasons = [];
  List<DropdownItem> _rejectionReasonItems = const [];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadRejectionReasons();

      assetType = context.read<AssetTypeBloc>().state.when(
            initial: () => '',
            inverter: () => 'inverter',
            battery: () => 'battery',
            panel: () => 'panel',
          );

      userType = context.read<UserTypeBloc>().state.maybeWhen(
            supervisor: () => USER_TYPES.SUPERVISOR.name,
            orElse: () => USER_TYPES.FIELD_STAFF.name,
          );

      final sel = context.read<SelectedActivityFacilityBloc>().state;
      sel.whenOrNull(selected: (proj) {
        selectedActivityFacility = proj;
        activityFacilityName =
            proj.activityFacility.facility?.facilityName ?? '---';
        status = proj.status ?? '---';
        context
            .read<RejectionBloc>()
            .add(RejectionEvent.watch(proj.activityFacility.id));

        context.read<AssetSummaryBloc>().add(
              AssetSummaryEvent.load(
                activityFacilityId: proj.activityFacility.id,
                assetType: assetType,
              ),
            );

        context.read<ActivityFacilityBloc>().add(
              ActivityFacilityEvent.checkIfInCache(
                activityFacilityId: proj.activityFacility.id,
                userType: userType,
              ),
            );
      });
    });
  }

  @override
  void dispose() {
    for (final entry in _reasons) {
      entry.controller.dispose();
    }
    super.dispose();
  }

  Future<void> _loadRejectionReasons() async {
    try {
      final docs = await AppInitRepo().searchRejectionReasons(
        MdmsRequestModel(
          mdmsCriteria: MdmsCriteriaModel(
            tenantId: env.envConfig.variables.tenantId,
            schemaCode: "Installation.RejectionReasons",
            moduleDetails: [],
          ),
        ),
        useCacheRead: true,
      );

      final items = docs
          .where((doc) => doc.isActive)
          .map((doc) => DropdownItem(
                name: doc.data.name,
                code: doc.data.code,
              ))
          .toList();

      if (!mounted) return;
      setState(() {
        _rejectionReasonItems = items;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _rejectionReasonItems = const [];
      });
    }
  }

  void _openImage(String path) {
    context.router.push(ImageViewerRoute(path: path));
  }

  void _openVideo(String path) {
    context.router.push(VideoPlayerRoute(path: path));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<RejectionBloc, RejectionState>(
      listener: (context, state) {
        state.maybeWhen(
          success: () {
            Navigator.of(context).maybePop();
          },
          failure: (progress) {
            final message = progress.errorMessage ?? 'Failed.';
            if (isSessionExpiredMessage(message)) {
              handleSessionExpired(context);
            }
          },
          orElse: () {},
        );
      },
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (context, assetTypeState) {
          final heading = assetTypeState.when(
            initial: () => '',
            inverter: () => 'Inverter / PCU',
            battery: () => 'Battery',
            panel: () => 'Panel',
          );

          return Scaffold(
            body: Stack(
              children: [
                ScrollableContent(
                  enableFixedDigitButton: true,
                  backgroundColor: theme.colorTheme.generic.background,
                  header: const BackNavigationHelpHeaderWidget(
                    showBackNavigation: true,
                    showHelp: false,
                  ),
                  footer: _buildFooter(),
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(
                          vertical: spacer2, horizontal: spacer4),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const SizedBox(height: spacer2),
                          Text(
                            '$heading ${context.translate(i18.assetSummary.summary)}',
                            style: textTheme.headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2,
                            ),
                          ),
                          const SizedBox(height: spacer2),
                          BlocBuilder<AssetSummaryBloc, AssetSummaryState>(
                            builder: (context, state) {
                              return state.when(
                                initial: () => Center(
                                  child: Text(context.translate(
                                      i18.assetSummary.loadingSummary)),
                                ),
                                loading: () => const Center(
                                  child: CircularProgressIndicator(),
                                ),
                                error: (msg) => Center(
                                  child: Text(
                                      '${context.translate(i18.assetSummary.errorLoadingSummary)}:\n$msg'),
                                ),
                                loaded: (summary) {
                                  return _buildSummaryCards(summary, heading);
                                },
                              );
                            },
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                BlocBuilder<RejectionBloc, RejectionState>(
                  builder: (context, rejectionState) {
                    final progress = rejectionState.maybeWhen(
                      inProgress: (progress) => progress,
                      failure: (progress) => progress,
                      orElse: () => null,
                    );
                    return OperationProgressOverlay(
                      progress: progress,
                      onClose: progress?.isFailure == true
                          ? () => context
                              .read<RejectionBloc>()
                              .add(const RejectionEvent.dismiss())
                          : null,
                    );
                  },
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _buildFooter() {
    return BlocBuilder<ActivityFacilityBloc, ActivityFacilityState>(
      builder: (context, projectState) {
        return BlocBuilder<ReportTypeBloc, ReportTypeState>(
          builder: (context, reportState) {
            return BlocBuilder<InboxTypeBloc, InboxTypeState>(
              builder: (context, inboxState) {
                final rejectionProgress =
                    context.watch<RejectionBloc>().state.maybeWhen(
                          inProgress: (progress) => progress,
                          failure: (progress) => progress,
                          orElse: () => null,
                        );
                final rejecting = rejectionProgress?.isActive ?? false;
                final isApproved = inboxState.maybeWhen(
                  approved: () => true,
                  orElse: () => false,
                );
                final isSubmitted = reportState.maybeWhen(
                  submitted: () => true,
                  orElse: () => false,
                );
                final isNewReport = reportState.maybeWhen(
                  newReport: () => true,
                  orElse: () => false,
                );
                final isInCache = projectState.maybeWhen(
                  inCache: (cached) => cached,
                  orElse: () => false,
                );
                if (!isNewReport &&
                    (isApproved || (isSubmitted && !isInCache))) {
                  return const SizedBox.shrink();
                }

                return reportState.maybeWhen(
                  sendBack: () => FooterButton(
                    showSuffixIcon: false,
                    text: rejecting ? "Rejecting..." : "Reject",
                    isDisabled: rejecting,
                    onPress: () => _showSendBackPopup(context),
                  ),
                  orElse: () => FooterButton(
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonNext),
                    onPress: () {
                      context.router.push(const DataSaveSuccessRoute());
                    },
                  ),
                );
              },
            );
          },
        );
      },
    );
  }

  Future<void> _showSendBackPopup(BuildContext context) async {
    await _loadRejectionReasons();
    if (!context.mounted) return;

    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    for (final entry in _reasons) {
      entry.controller.dispose();
    }
    _reasons
      ..clear()
      ..add(_ReasonEntry());

    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setStatePopup) {
          return Popup(
            onCrossTap: () => Navigator.of(ctx).pop(),
            title: context.translate(i18.assetSummary.rejectionReason),
            type: PopUpType.simple,
            actionAlignment: MainAxisAlignment.center,
            additionalWidgets: [
              ..._reasons.map((entry) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Stack(
                      clipBehavior: Clip.none,
                      children: [
                        LabeledField(
                          label: context.translate(i18.assetSummary.reason),
                          child: DigitDropdown(
                            sentenceCaseEnabled: false,
                            items: _rejectionReasonItems,
                            onSelect: (sel) => setStatePopup(() {
                              entry.selectedCode = sel.code;
                              entry.selectedName = sel.name;
                            }),
                            selectedOption: DropdownItem(
                              name: entry.selectedName ?? '',
                              code: entry.selectedCode ?? '',
                            ),
                          ),
                        ),
                        Positioned(
                          top: -spacer3,
                          right: -spacer3,
                          child: Material(
                            color: Colors.transparent,
                            child: IconButton(
                              iconSize: 20,
                              padding: EdgeInsets.zero,
                              splashRadius: 20,
                              icon: Icon(
                                Icons.delete,
                                color:
                                    Theme.of(context).colorTheme.text.primary,
                              ),
                              onPressed: () {
                                setStatePopup(() {
                                  _reasons.remove(entry);
                                  entry.controller.dispose();
                                });
                              },
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: spacer2),
                    LabeledField(
                      label:
                          context.translate(i18.assetSummary.additionalDetails),
                      child: InputField(
                        type: InputType.textArea,
                        controller: entry.controller,
                        innerLabel: context.translate(
                            i18.assetSummary.detailsForSelectedReason),
                        textAreaScroll: TextAreaScroll.vertical,
                      ),
                    ),
                    const SizedBox(height: spacer2),
                  ],
                );
              }),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  GestureDetector(
                    onTap: () {
                      setStatePopup(() => _reasons.add(_ReasonEntry()));
                    },
                    child: Text(
                      context.translate(i18.assetSummary.addReason),
                      style: textTheme.headingM.copyWith(
                        color: theme.colorTheme.primary.primary1,
                      ),
                    ),
                  ),
                ],
              ),
              Row(
                children: [
                  Expanded(
                    child: DigitButton(
                      label: context.translate(i18.assetSummary.back),
                      type: DigitButtonType.secondary,
                      onPressed: () => Navigator.of(ctx).pop(),
                      size: DigitButtonSize.large,
                      mainAxisSize: MainAxisSize.min,
                    ),
                  ),
                  const SizedBox(width: spacer5),
                  Expanded(
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        DigitButton(
                          label: context.translate(i18.assetSummary.submit),
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.large,
                          mainAxisSize: MainAxisSize.min,
                          onPressed: () async {
                            final selected = selectedActivityFacility;
                            if (selected == null) return;

                            final reasons = _reasons
                                .where((e) =>
                                    (e.selectedName?.isNotEmpty ?? false) ||
                                    e.controller.text.trim().isNotEmpty)
                                .map((e) => {
                                      'reason':
                                          (e.selectedName ?? '').trim(),
                                      'comment': e.controller.text.trim(),
                                    })
                                .toList();

                            if (reasons.isEmpty) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(
                                    context.translate(i18.assetSummary
                                        .selectReasonOrEnterAdditionalDetails),
                                  ),
                                ),
                              );
                              return;
                            }

                            final transactions = [
                              Transaction(
                                activityFacilityId:
                                    selected.activityFacility.id,
                                comments: reasons.map((e) {
                                  final message = jsonEncode({
                                    'reason': e['reason'],
                                    'comment': e['comment'],
                                  });

                                  return Comment(
                                    commentMessage: message,
                                    assetType: assetType.titleCase,
                                  );
                                }).toList(),
                              )
                            ];

                            Navigator.of(ctx).pop();
                            context.read<RejectionBloc>().add(
                                  RejectionEvent.submitRejection(
                                      activityFacilityId:
                                          selected.activityFacility.id,
                                      transactions: transactions,
                                      userType: userType),
                                );
                          },
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _buildSummaryCards(AssetSummaryModel summary, String heading) {
    final textTheme = Theme.of(context).digitTextTheme(context);

    final initState = context.read<AppInitialization>().state;

    final List<Mdms<SystemData>> systemMdmsList = initState.maybeWhen(
      initialized: (appConfig, assetCount, assetType, system, warranty, brand,
              solutionDesign, _) =>
          system,
      orElse: () => <Mdms<SystemData>>[],
    );

    final List<Mdms<BrandData>> brandMdmsList = initState.maybeWhen(
      initialized: (appConfig, assetCount, assetType, system, warranty, brand,
              solutionDesign, _) =>
          brand,
      orElse: () => <Mdms<BrandData>>[],
    );

    final countValue = summary.countEntry?.count.toString() ?? '—';
    final warrantyStart =
        buildWarrantyStart(summary.detailEntry?.warrantyStartDate);

    final warrantyDuration = summary.detailEntry?.warranty ?? '—';
    final brandCode = summary.detailEntry?.brand ?? '—';
    final systemCode = summary.specEntry?.system ?? '—';
    final capacity = summary.specEntry?.totalCapacity.toString() ?? '—';
    final capacityUnit = summary.specEntry?.totalCapacityUnit ?? '-';

    final brand = brandMdmsList.first.data.brand
            .map((m) => m)
            .firstWhereOrNull((b) => b.code == (brandCode ?? ''))
            ?.name ??
        (brandCode ?? '—');

    final system = systemMdmsList.first.data.system
            .map((m) => m)
            .firstWhereOrNull((s) => s.code == (systemCode ?? ''))
            ?.name ??
        (systemCode ?? '—');

    final assetCards = summary.addedAssets.asMap().entries.map((e) {
      final index = e.key;
      final asset = e.value;
      return Padding(
        padding: const EdgeInsets.only(top: spacer4),
        child: DigitCard(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '$heading ${index + 1}',
                  style: textTheme.headingM.copyWith(
                    color: Theme.of(context).colorTheme.primary.primary2,
                  ),
                ),
                editButton(
                  context: context,
                  onTap: () => context.router.push(const AddNewAssetRoute()),
                ),
              ],
            ),
            const SizedBox(height: spacer2),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                KeyColumn(keys: [
                  context.translate(i18.common.serialNumber),
                  context.translate(i18.common.capacity),
                  context.translate(i18.common.images),
                ]),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: Text(
                        truncateText(asset.serialNumber, maxLength: 18),
                        style: textTheme.bodyS.copyWith(
                          color: Theme.of(context).colorTheme.text.primary,
                        ),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: Text(
                        // Unit suffix commented out, not deleted: '$capacity$capacityUnit'
                        capacity,
                        style: textTheme.bodyS.copyWith(
                          color: Theme.of(context).colorTheme.text.primary,
                        ),
                      ),
                    ),
                    if (asset.photoPath.isNotEmpty)
                      GestureDetector(
                        onTap: () => _openImage(asset.photoPath),
                        child: Padding(
                            padding: const EdgeInsets.only(bottom: spacer3),
                            child: assetImageCard(filePath: asset.photoPath)),
                      )
                  ],
                ),
              ],
            ),
          ],
        ),
      );
    }).toList();

    final imageWidgets = summary.mediaEntries
        .where((m) => m.itemType == 'image')
        .map((m) => GestureDetector(
              onTap: () => _openImage(m.filePath),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: spacer1),
                child: assetImageCard(filePath: m.filePath),
              ),
            ))
        .toList();

    final videoWidgets = summary.mediaEntries
        .where((m) => m.itemType == 'video')
        .map(
          (m) => GestureDetector(
              onTap: () => _openVideo(m.filePath),
              child: videoCard(context: context, filePath: m.filePath)),
        )
        .toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        DigitCard(children: [
          Text(
            context.translate(i18.assetSummary.healthFacilityDetails),
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            Expanded(
                flex: 1,
                child: KeyColumn(keys: [
                  context.translate(i18.assetSummary.name),
                  context.translate(i18.common.status),
                ])),
            Expanded(
              flex: 1,
              child: ValueColumn(values: [
                truncateText(activityFacilityName, maxLength: 18),
                context.translate(status),
              ]),
            ),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Text(
            context.translate(i18.assetSummary.count),
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            KeyColumn(keys: [heading]),
            ValueColumn(values: [countValue]),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Text(
            context.translate(i18.assetSummary.specifications),
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            KeyColumn(keys: [
              context.translate(i18.assetSummary.system),
              context.translate(i18.common.capacity),
            ]),
            ValueColumn(values: [system, '$capacity$capacityUnit']),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Row(children: [
            Text(
              context.translate(i18.assetSummary.details),
              style: textTheme.headingM.copyWith(
                color: Theme.of(context).colorTheme.primary.primary2,
              ),
            ),
            const Spacer(),
            editButton(
              context: context,
              onTap: () => context.router.push(const AssetTypeDetailRoute()),
            ),
          ]),
          Row(children: [
            KeyColumn(keys: [
              context.translate(i18.assetSummary.warrantyStartDate),
              context.translate(i18.assetSummary.warrantyDuration),
              context.translate(i18.assetSummary.brand),
            ]),
            ValueColumn(values: [
              warrantyStart,
              parseWarrantyYears(warrantyDuration).toString() ?? '',
              brand,
            ]),
          ]),
        ]),
        ...assetCards,
        if (imageWidgets.isNotEmpty) ...[
          const SizedBox(height: spacer4),
          DigitCard(children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '$heading ${context.translate(i18.common.images)}',
                  style: textTheme.headingM.copyWith(
                    color: Theme.of(context).colorTheme.primary.primary2,
                  ),
                ),
                editButton(
                  context: context,
                  onTap: () => context.router.push(const MediaUploadRoute()),
                ),
              ],
            ),
            const SizedBox(height: spacer2),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(children: imageWidgets),
            ),
          ]),
        ],
        if (videoWidgets.isNotEmpty) ...[
          const SizedBox(height: spacer4),
          DigitCard(children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '$heading ${context.translate(i18.common.videos)}',
                  style: textTheme.headingM.copyWith(
                    color: Theme.of(context).colorTheme.primary.primary2,
                  ),
                ),
                editButton(
                  context: context,
                  onTap: () => context.router.push(const MediaUploadRoute()),
                ),
              ],
            ),
            const SizedBox(height: spacer2),
            Column(children: videoWidgets),
          ]),
        ],
      ],
    );
  }
}

Widget assetImageCard({required String filePath}) {
  return isValidUuid(filePath)
      ? CachedImage("$fileStoreFileUrl$filePath", width: 100, height: 100)
      : Image.file(
          File(filePath),
          width: 100,
          height: 100,
          fit: BoxFit.cover,
        );
}

Widget editButton({
  required BuildContext context,
  required VoidCallback onTap,
}) {
  return BlocBuilder<ReportTypeBloc, ReportTypeState>(
    builder: (context, reportState) {
      return BlocBuilder<InboxTypeBloc, InboxTypeState>(
        builder: (context, inboxState) {
          final bool isSubmittedReport =
              reportState.maybeWhen(submitted: () => true, orElse: () => false);

          final bool isInboxReport =
              reportState.maybeWhen(inbox: () => true, orElse: () => false);

          final bool isSendBackReport =
              reportState.maybeWhen(sendBack: () => true, orElse: () => false);

          final isApprovedReport =
              inboxState.maybeWhen(approved: () => true, orElse: () => false);

          final userState = context.read<UserTypeBloc>().state;
          bool isRejectedByQc = false;
          bool isFieldStaff =
              userState.maybeWhen(staff: () => true, orElse: () => false);

          final selState = context.read<SelectedActivityFacilityBloc>().state;

          selState.whenOrNull(selected: (project) {
            isRejectedByQc = project.status ==
                WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name;
          });

          final bool hideEditButton = isSubmittedReport ||
              (isInboxReport && isApprovedReport) ||
              isSendBackReport ||
              (isFieldStaff && isRejectedByQc);
          return hideEditButton
              ? const SizedBox.shrink()
              : GestureDetector(
                  onTap: onTap,
                  child: Row(
                    children: [
                      Icon(Icons.edit,
                          size: spacer5,
                          color: Theme.of(context).colorTheme.alert.error),
                      const SizedBox(width: spacer1),
                      Text(
                        context.translate(i18.common.coreCommonEdit),
                        style: TextStyle(
                            color: Theme.of(context).colorTheme.alert.error),
                      ),
                    ],
                  ),
                );
        },
      );
    },
  );
}

class KeyColumn extends StatelessWidget {
  final List<String> keys;
  const KeyColumn({super.key, required this.keys});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).digitTextTheme(context);
    return SizedBox(
      width: spacer9 * 5,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: keys
            .map((k) => Padding(
                  padding: const EdgeInsets.only(bottom: spacer3),
                  child: Text(
                    k,
                    style: textTheme.headingS.copyWith(
                      color: Theme.of(context).colorTheme.text.primary,
                    ),
                  ),
                ))
            .toList(),
      ),
    );
  }
}

class ValueColumn extends StatelessWidget {
  final List<String> values;
  const ValueColumn({super.key, required this.values});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).digitTextTheme(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: values
          .map((v) => Padding(
                padding: const EdgeInsets.only(bottom: spacer3),
                child: Text(
                  v,
                  style: textTheme.bodyS.copyWith(
                    color: Theme.of(context).colorTheme.text.primary,
                  ),
                ),
              ))
          .toList(),
    );
  }
}
