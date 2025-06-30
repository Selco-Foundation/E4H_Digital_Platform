// lib/pages/asset_summary_page.dart

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
import 'package:selco/blocs/user_type/user_type.dart';
import 'package:selco/model/project_workflow/project_workflow.dart';

import '../blocs/asset_summary/asset_summary.dart';
import '../blocs/asset_type/asset_type.dart';
import '../blocs/project/project.dart';
import '../blocs/report_type/report_type.dart';
import '../blocs/selected_project/selected_project.dart';
import '../model/asset_summary/asset_summary.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';

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

  @override
  void initState() {
    super.initState();

    // Fire the “load” event as soon as page is pushed:
    WidgetsBinding.instance.addPostFrameCallback((_) {
      assetType = context.read<AssetTypeBloc>().state.when(
            initial: () => '',
            inverter: () => 'inverter',
            battery: () => 'battery',
            panel: () => 'panel',
          );

      userType = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => USER_TYPES.SUPERVISOR.name,
          orElse: () => USER_TYPES.FIELD_STAFF.name);

      // Assumes SelectedProjectBloc holds the current project
      final selectedProjectState = context.read<SelectedProjectBloc>().state;
      selectedProjectState.whenOrNull(selected: (proj) {
        final projectId = proj.project.id;
        selectedProject = proj;
        projectName = proj.project.name ?? '---';
        status = proj.status ?? "---";
        context.read<AssetSummaryBloc>().add(
              AssetSummaryEvent.load(
                projectId: projectId,
                assetType: assetType,
              ),
            );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, assetTypeState) {
        final assetType = assetTypeState.when(
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
            footer: BlocBuilder<ReportTypeBloc, ReportTypeState>(
              builder: (context, reportState) {
                if (reportState is ReportTypeSendBack) {
                  return FooterButton(
                    showSuffixIcon: false,
                    text: "Send back",
                    onPress: () => showCustomPopup(
                      context: context,
                      builder: (ctx) => Popup(
                        onCrossTap: () => Navigator.of(ctx).pop(),
                        title: "Send back",
                        onOutsideTap: () => Navigator.of(ctx).pop(),
                        type: PopUpType.simple,
                        actionAlignment: MainAxisAlignment.center,
                        actions: const [],
                        additionalWidgets: [
                          LabeledField(
                            label: 'Reason',
                            child: DigitDropdown(
                              onSelect: (DropdownItem sel) {
                                // handle reason selection
                              },
                              items: const [
                                DropdownItem(name: 'Option A', code: 'a'),
                                DropdownItem(name: 'Option B', code: 'b'),
                                DropdownItem(name: 'Option C', code: 'c'),
                              ],
                            ),
                          ),
                          InputField(
                            type: InputType.textArea,
                            controller: TextEditingController(),
                            innerLabel:
                                'Additional Details for Selected Reason',
                            textAreaScroll: TextAreaScroll.vertical,
                          ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              GestureDetector(
                                onTap: () {},
                                child: Text(
                                  'Add Reason',
                                  style: textTheme.headingM.copyWith(
                                      color: theme.colorTheme.primary.primary1),
                                ),
                              ),
                            ],
                          ),
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Expanded(
                                flex: 1,
                                child: DigitButton(
                                  label: "Back",
                                  onPressed: () {
                                    Navigator.of(ctx).pop();
                                  },
                                  type: DigitButtonType.secondary,
                                  size: DigitButtonSize.large,
                                  mainAxisSize: MainAxisSize.min,
                                ),
                              ),
                              const SizedBox(width: spacer5),
                              Expanded(
                                flex: 1,
                                child: DigitButton(
                                  label: "Submit",
                                  onPressed: () {
                                    Navigator.of(ctx).pop();
                                  },
                                  type: DigitButtonType.primary,
                                  size: DigitButtonSize.large,
                                  mainAxisSize: MainAxisSize.min,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                } else {
                  return FooterButton(
                    showSuffixIcon: false,
                    text: context.translate(i18.common.coreCommonNext),
                    onPress: () {
                      // final SecureStore storage = SecureStore();
                      // storage.addToDraftProjects(selectedProject!);
                      final project = selectedProject!; // your ProjectWorkflow
                      context.read<ProjectBloc>().add(
                            ProjectEvent.addUnSubmitted(project, userType),
                          );
                      context.router.push(const DataSaveSuccessRoute());
                    },
                  );
                }
              },
            ),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    vertical: spacer2, horizontal: spacer4),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const SizedBox(height: spacer2),
                    Text(
                      '$assetType Summary',
                      style: textTheme.headingXl
                          .copyWith(color: theme.colorTheme.primary.primary2),
                    ),
                    const SizedBox(height: spacer2),

                    // Use BlocBuilder<AssetSummaryBloc, AssetSummaryState> to fill in cached values
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
                            return _buildSummaryCards(summary, assetType);
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
    );
  }

  Widget _buildSummaryCards(
    AssetSummaryModel summary,
    String heading,
  ) {
    final textTheme = Theme.of(context).digitTextTheme(context);

    final countValue = summary.countEntry?.count.toString() ?? '—';
    final warrantyStart = DateFormat('yyyy-MM-dd HH:mm').format(DateTime.now());
    final warrantyDuration = summary.detailEntry?.warranty ?? '—';
    final brand = summary.detailEntry?.brand ?? '—';
    final model = summary.detailEntry?.model ?? '—';

    final system = summary.specEntry?.system ?? '—';
    final capacity = summary.specEntry?.totalCapacity.toString() ?? '—';
    final capacityUnit = summary.specEntry?.totalCapacityUnit ?? '-';

    final assetCards = summary.addedAssets.asMap().entries.map((entryPair) {
      final index = entryPair.key;
      final asset = entryPair.value;
      final title = '$heading ${index + 1}';

      return Padding(
        padding: const EdgeInsets.only(top: spacer4),
        child: DigitCard(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  "$title",
                  style: textTheme.headingM.copyWith(
                    color: Theme.of(context).colorTheme.primary.primary2,
                  ),
                ),
                editButton(
                    context: context,
                    onTap: () => context.router.push(const AddNewAssetRoute())),
              ],
            ),
            const SizedBox(height: spacer2),
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Left column of labels:
                const KeyColumn(keys: ['Serial Number', 'Capacity', 'Image']),
                // Right column of values:
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: Text(
                        asset.serialNumber,
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
                    // Show the saved image:
                    if (asset.photoPath.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(bottom: spacer3),
                        child: Image.file(
                          File(asset.photoPath),
                          width: 100,
                          height: 100,
                          fit: BoxFit.cover,
                        ),
                      )
                    else
                      const SizedBox.shrink(),
                  ],
                ),
              ],
            ),
          ],
        ),
      );
    }).toList();

    // Build media list as download links
    final imageWidgets =
        summary.mediaEntries.where((m) => m.itemType == 'image').map((m) {
      return Padding(
        padding: const EdgeInsets.symmetric(horizontal: spacer1),
        child: Image.file(
          File(m.filePath),
          width: 100,
          height: 100,
          fit: BoxFit.cover,
        ),
      );
    }).toList();

    final videoWidgets =
        summary.mediaEntries.where((m) => m.itemType == 'video').map((m) {
      final fileName = m.itemNumber;
      return Padding(
        padding: const EdgeInsets.only(bottom: spacer3),
        child: Row(
          children: [
            Icon(Icons.play_circle_fill,
                color: Theme.of(context).colorTheme.primary.primary1),
            const SizedBox(width: spacer2),
            Text(
              fileName,
              style: textTheme.bodyS.copyWith(
                color: Theme.of(context).colorTheme.text.primary,
              ),
            ),
          ],
        ),
      );
    }).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        DigitCard(
          children: [
            Text(
              'Health Facility Details',
              style: textTheme.headingM.copyWith(
                  color: Theme.of(context).colorTheme.primary.primary2),
            ),
            Row(
              children: [
                const KeyColumn(keys: ['Name', 'Status']),
                ValueColumn(values: [
                  truncateText('$projectName', maxLength: 18),
                  truncateText('$status', maxLength: 19),
                ]),
              ],
            ),
          ],
        ),
        const SizedBox(height: spacer4),
        // Health Facility Details (Count)
        DigitCard(
          children: [
            Text(
              'Count',
              style: textTheme.headingM.copyWith(
                  color: Theme.of(context).colorTheme.primary.primary2),
            ),
            Row(
              children: [
                KeyColumn(keys: [assetType.titleCase]),
                ValueColumn(values: [countValue]),
              ],
            ),
          ],
        ),
        const SizedBox(height: spacer4),

        // Specifications
        DigitCard(
          children: [
            Text(
              'Specifications',
              style: textTheme.headingM.copyWith(
                  color: Theme.of(context).colorTheme.primary.primary2),
            ),
            Row(
              children: [
                const KeyColumn(keys: ['System', 'Capacity']),
                ValueColumn(values: [system, '$capacity$capacityUnit']),
              ],
            ),
          ],
        ),
        const SizedBox(height: spacer4),

        // Details: Warranty, Brand, Model
        DigitCard(
          children: [
            Row(
              children: [
                Text(
                  'Details',
                  style: textTheme.headingM.copyWith(
                      color: Theme.of(context).colorTheme.primary.primary2),
                ),
                const Spacer(),
                editButton(
                    context: context,
                    onTap: () =>
                        context.router.push(const AssetTypeDetailRoute())),
              ],
            ),
            Row(
              children: [
                const KeyColumn(keys: [
                  'Warranty Start Date',
                  'Warranty Duration',
                  'Brand',
                  'Model No.'
                ]),
                ValueColumn(
                  values: [warrantyStart, warrantyDuration, brand, model],
                ),
              ],
            ),
          ],
        ),
        const SizedBox(height: spacer4),
        ...assetCards,
        if (imageWidgets.isNotEmpty) ...[
          const SizedBox(height: spacer4),
          DigitCard(
            children: [
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
                      onTap: () =>
                          context.router.push(const MediaUploadRoute())),
                ],
              ),
              const SizedBox(height: spacer2),
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(children: imageWidgets),
              ),
            ],
          ),
        ],

        // ─────── Inverter Videos ───────
        if (videoWidgets.isNotEmpty) ...[
          const SizedBox(height: spacer4),
          DigitCard(
            children: [
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
                      onTap: () =>
                          context.router.push(const MediaUploadRoute())),
                ],
              ),
              const SizedBox(height: spacer2),
              Column(children: videoWidgets),
            ],
          ),
        ],
      ],
    );
  }
}

Widget editButton({required BuildContext context, required Function() onTap}) {
  return GestureDetector(
    onTap: onTap,
    child: Row(
      children: [
        Icon(Icons.edit, size: spacer5, color: const Light().alertError),
        const SizedBox(width: spacer1),
        Text(
          context.translate(i18.common.coreCommonEdit),
          style: TextStyle(color: const Light().alertError),
        ),
      ],
    ),
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
            .map((key) => Padding(
                  padding: const EdgeInsets.only(bottom: spacer3),
                  child: Text(
                    key,
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
          .map((value) => Padding(
                padding: const EdgeInsets.only(bottom: spacer3),
                child: Text(
                  value,
                  style: textTheme.bodyS.copyWith(
                    color: Theme.of(context).colorTheme.text.primary,
                  ),
                ),
              ))
          .toList(),
    );
  }
}
