import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import 'package:recase/recase.dart';

import '../blocs/asset_rejection/asset_rejection.dart';
import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/inbox_type/inbox_type.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../blocs/user_type/user_type.dart';
import '../model/asset_summary/asset_summary.dart';
import '../model/comment/comment.dart';
import '../model/project_workflow/project_workflow.dart';
import '../model/transaction/transaction.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/files/video_card.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/images/cached_image.dart';

/// Simple holder for one reason + its accompanying text controller.
class _ReasonEntry {
  String? selectedCode;
  final TextEditingController controller = TextEditingController();
}

@RoutePage()
class AssetSummaryPage extends StatefulWidget {
  const AssetSummaryPage({super.key});

  @override
  State<AssetSummaryPage> createState() => _AssetSummaryPageState();
}

class _AssetSummaryPageState extends State<AssetSummaryPage> {
  String projectName = "";
  String status = "";
  String assetType = "";
  String userType = "";
  ProjectWorkflow? selectedProject;

  /// Holds all the dynamic reason rows in the popup.
  final List<_ReasonEntry> _reasons = [];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
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

      final sel = context.read<SelectedProjectBloc>().state;
      sel.whenOrNull(selected: (proj) {
        selectedProject = proj;
        projectName = proj.project.name ?? '---';
        status = proj.status ?? '---';

        context.read<AssetSummaryBloc>().add(
              AssetSummaryEvent.load(
                projectId: proj.project.id,
                assetType: assetType,
              ),
            );

        context.read<ProjectBloc>().add(
              ProjectEvent.checkIfInCache(
                projectId: proj.project.id,
                userType: userType,
              ),
            );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocListener<RejectionBloc, RejectionState>(
      listener: (context, state) {
        state.maybeWhen(
          loading: () {
            showDialog(
              context: context,
              barrierDismissible: false,
              builder: (_) => const Center(
                child: CircularProgressIndicator(),
              ),
            );
          },
          success: () {
            Navigator.of(context, rootNavigator: true).pop(); // Remove loader
            Navigator.of(context).maybePop(); // Pop summary page
          },
          failure: (message) {
            Navigator.of(context, rootNavigator: true).pop(); // Remove loader
            context.showSnackBar(SnackBar(content: Text(message)));
          },
          orElse: () {},
        );
      },
      child: BlocBuilder<AssetTypeBloc, AssetTypeState>(
        builder: (context, assetTypeState) {
          final heading = assetTypeState.when(
            initial: () => '',
            inverter: () => 'Inverter',
            battery: () => 'Battery',
            panel: () => 'Panel',
          );

          return Scaffold(
            body: ScrollableContent(
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
                        '$heading Summary',
                        style: textTheme.headingXl.copyWith(
                          color: theme.colorTheme.primary.primary2,
                        ),
                      ),
                      const SizedBox(height: spacer2),
                      BlocBuilder<AssetSummaryBloc, AssetSummaryState>(
                        builder: (context, state) {
                          return state.when(
                            initial: () => const Center(
                              child: Text('Loading summary...'),
                            ),
                            loading: () => const Center(
                              child: CircularProgressIndicator(),
                            ),
                            error: (msg) => Center(
                              child: Text('Error loading summary:\n$msg'),
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
          );
        },
      ),
    );
  }

  /// Builds the footer area, switching between “Send Back” and “Next”
  Widget _buildFooter() {
    return BlocBuilder<ProjectBloc, ProjectState>(
      builder: (context, projectState) {
        return BlocBuilder<ReportTypeBloc, ReportTypeState>(
          builder: (context, reportState) {
            return BlocBuilder<InboxTypeBloc, InboxTypeState>(
              builder: (context, inboxState) {
                final isApproved = inboxState.maybeWhen(
                  approved: () => true,
                  orElse: () => false,
                );
                final isSubmitted = reportState.maybeWhen(
                  submitted: () => true,
                  orElse: () => false,
                );
                final isInCache = projectState.maybeWhen(
                  inCache: (cached) => cached,
                  orElse: () => false,
                );

                if (isApproved || (isSubmitted && !isInCache)) {
                  return const SizedBox.shrink();
                }

                return reportState.maybeWhen(
                  sendBack: () => FooterButton(
                    showSuffixIcon: false,
                    text: "Reject",
                    onPress: () => _showSendBackPopup(context),
                  ),
                  orElse: () => FooterButton(
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonNext),
                    onPress: () {
                      final proj = selectedProject!;
                      context.read<ProjectBloc>().add(
                            ProjectEvent.addUnSubmitted(proj, userType),
                          );
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

  void _showSendBackPopup(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    _reasons
      ..clear()
      ..add(_ReasonEntry());

    showCustomPopup(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setStatePopup) {
          return Popup(
            onCrossTap: () => Navigator.of(ctx).pop(),
            title: "Send back",
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
                          label: 'Reason',
                          child: DigitDropdown(
                            items: const [
                              DropdownItem(name: 'Option A', code: 'a'),
                              DropdownItem(name: 'Option B', code: 'b'),
                              DropdownItem(name: 'Option C', code: 'c'),
                            ],
                            onSelect: (sel) => setStatePopup(
                              () => entry.selectedCode = sel.code,
                            ),
                            selectedOption: entry.selectedCode == null
                                ? null
                                : DropdownItem(
                                    name: '', code: entry.selectedCode!),
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
                                });
                              },
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: spacer2),
                    LabeledField(
                      label: 'Additional Details',
                      child: InputField(
                        type: InputType.textArea,
                        controller: entry.controller,
                        innerLabel: 'Details for the selected reason',
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
                      'Add Reason',
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
                      label: "Back",
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
                          label: "Submit",
                          type: DigitButtonType.primary,
                          size: DigitButtonSize.large,
                          mainAxisSize: MainAxisSize.min,
                          onPressed: () {
                            final selected = selectedProject;
                            if (selected == null) return;

                            final reasons = _reasons
                                .where((e) => e.selectedCode != null)
                                .map((e) => {
                                      'reason': e.selectedCode!,
                                      'details': e.controller.text,
                                    })
                                .toList();

                            final transactions = [
                              Transaction(
                                projectId: selected.project.id,
                                comments: reasons
                                    .map((e) => Comment(
                                          commentMessage: e['details'],
                                          assetType: assetType.titleCase,
                                        ))
                                    .toList(),
                              )
                            ];

                            Navigator.of(ctx).pop();
                            context.read<RejectionBloc>().add(
                                  RejectionEvent.submitRejection(
                                    projectId: selected.project.id.trim(),
                                    transactions: transactions,
                                  ),
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

    // Facility details
    final countValue = summary.countEntry?.count.toString() ?? '—';
    final warrantyStart = DateFormat('yyyy-MM-dd HH:mm').format(DateTime.now());
    final warrantyDuration = summary.detailEntry?.warranty ?? '—';
    final brand = summary.detailEntry?.brand ?? '—';
    final model = summary.detailEntry?.model ?? '—';
    final system = summary.specEntry?.system ?? '—';
    final capacity = summary.specEntry?.totalCapacity.toString() ?? '—';
    final capacityUnit = summary.specEntry?.totalCapacityUnit ?? '-';

    // Cards for each asset
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
                const KeyColumn(keys: ['Serial Number', 'Capacity', 'Image']),
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
                        '$capacity$capacityUnit',
                        style: textTheme.bodyS.copyWith(
                          color: Theme.of(context).colorTheme.text.primary,
                        ),
                      ),
                    ),
                    if (asset.photoPath.isNotEmpty)
                      Padding(
                          padding: const EdgeInsets.only(bottom: spacer3),
                          child: assetImageCard(filePath: asset.photoPath))
                  ],
                ),
              ],
            ),
          ],
        ),
      );
    }).toList();

    // Media thumbnails
    final imageWidgets = summary.mediaEntries
        .where((m) => m.itemType == 'image')
        .map((m) => Padding(
              padding: const EdgeInsets.symmetric(horizontal: spacer1),
              child: assetImageCard(filePath: m.filePath),
            ))
        .toList();

    final videoWidgets = summary.mediaEntries
        .where((m) => m.itemType == 'video')
        .map(
          (m) => videoCard(context: context, filePath: m.itemNumber),
        )
        .toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        DigitCard(children: [
          Text(
            'Health Facility Details',
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            const KeyColumn(keys: ['Name', 'Status']),
            ValueColumn(values: [
              truncateText(projectName, maxLength: 18),
              truncateText(status, maxLength: 18),
            ]),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Text(
            'Count',
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            KeyColumn(keys: [heading.titleCase]),
            ValueColumn(values: [countValue]),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Text(
            'Specifications',
            style: textTheme.headingM.copyWith(
              color: Theme.of(context).colorTheme.primary.primary2,
            ),
          ),
          Row(children: [
            const KeyColumn(keys: ['System', 'Capacity']),
            ValueColumn(values: [system, '$capacity$capacityUnit']),
          ]),
        ]),
        const SizedBox(height: spacer4),
        DigitCard(children: [
          Row(children: [
            Text(
              'Details',
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
            const KeyColumn(keys: [
              'Warranty Start Date',
              'Warranty Duration',
              'Brand',
              'Model No.'
            ]),
            ValueColumn(
                values: [warrantyStart, warrantyDuration, brand, model]),
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
                  '$heading Images',
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
                  '$heading Videos',
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

/// Renders either a network‐cached filestore image or a local file.
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

/// Simple “Edit” button used throughout.
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

          final bool hideEditButton = isSubmittedReport ||
              (isInboxReport && isApprovedReport) ||
              isSendBackReport;
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
