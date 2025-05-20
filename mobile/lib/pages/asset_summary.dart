import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/models/DropdownModels.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_dropdown_input.dart';
import 'package:digit_ui_components/widgets/atoms/input_wrapper.dart';
import 'package:digit_ui_components/widgets/atoms/labelled_fields.dart';
import 'package:digit_ui_components/widgets/atoms/pop_up_card.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/molecules/show_pop_up.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/asset_type/asset_type.dart';
import '../blocs/report_type/report_type.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/navigation/drawer.dart';
import '../widgets/navigation/navbar.dart';

@RoutePage()
class AssetSummaryPage extends StatefulWidget {
  const AssetSummaryPage({super.key});

  @override
  State<AssetSummaryPage> createState() {
    return _AssetSummaryPageState();
  }
}

class _AssetSummaryPageState extends State<AssetSummaryPage> {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    final List<String> mediaItems = [
      'Video 1',
      'Video 2',
      'Image 1',
      'Image 2'
    ];

    return BlocBuilder<AssetTypeBloc, AssetTypeState>(
      builder: (context, state) {
        final heading = state.when(
          initial: () => '',
          inverter: () => 'Inverter',
          battery: () => 'Battery',
          panel: () => 'Panel',
        );
        return Scaffold(
          appBar: const Navbar(),
          drawer: const CustomDrawer(),
          body: ScrollableContent(
            enableFixedDigitButton: true,
            backgroundColor: theme.colorTheme.generic.background,
            footer: BlocBuilder<ReportTypeBloc, ReportTypeState>(
              builder: (context, state) {
                if (state is ReportTypeInbox) {
                  return FooterButton(
                    showSuffixIcon: false,
                    text: "Send back",
                    // onPress: () {
                    //   context.router.push(const DataSaveSuccessRoute());
                    // },
                    onPress: () => showCustomPopup(
                      context: context,
                      builder: (ctx) => Popup(
                        onCrossTap: () {
                          Navigator.of(ctx).pop();
                        },
                        title: "Send back",
                        onOutsideTap: () {
                          Navigator.of(ctx).pop();
                        },
                        type: PopUpType.simple,
                        actionAlignment: MainAxisAlignment.center,
                        actions: [],
                        additionalWidgets: [
                          LabeledField(
                            label: 'Reason 1',
                            labelStyle: textTheme.label.copyWith(
                              color: theme.colorTheme.text.primary,
                            ),
                            capitalizedFirstLetter: false,
                            child: DigitDropdown(
                              onSelect: (DropdownItem selected) {
                                context.read<AssetTypeBloc>().add(
                                    AssetTypeEvent.typeSelected(selected.code));
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
                                    // context.read<AssetTypeBloc>().add(
                                    //     const AssetTypeEvent.typeSelected(
                                    //         "inverter"));
                                    // Navigator.of(ctx).pop();
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
                    text: context.translate(i18.common.coreCommonSave),
                    onPress: () {
                      context.router.push(const DataSaveSuccessRoute());
                    },
                  );
                }
              },
            ),
            children: [
              const BackNavigationHelpHeaderWidget(
                showBackNavigation: true,
                showHelp: false,
              ),
              Padding(
                  padding: const EdgeInsets.symmetric(
                      vertical: spacer2, horizontal: spacer4),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '$heading Summary',
                        style: textTheme.headingXl
                            .copyWith(color: theme.colorTheme.primary.primary2),
                      ),
                      const SizedBox(height: spacer2),
                      DigitCard(children: [
                        Text(
                          'Health Facility Details',
                          style: textTheme.headingM.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const Row(
                          children: [
                            KeyColumn(keys: ['Health Facility Name', 'Status']),
                            ValueColumn(
                                values: ['Alkod', 'Pending Installation'])
                          ],
                        )
                      ]),
                      const SizedBox(
                        height: spacer4,
                      ),
                      DigitCard(children: [
                        Text(
                          'Specifications',
                          style: textTheme.headingM.copyWith(
                              color: theme.colorTheme.primary.primary2),
                        ),
                        const Row(
                          mainAxisAlignment: MainAxisAlignment.start,
                          children: [
                            KeyColumn(keys: ['System', 'Capacity']),
                            ValueColumn(values: ['AC', '1 KVA'])
                          ],
                        )
                      ]),
                      const SizedBox(
                        height: spacer4,
                      ),
                      DigitCard(children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              'Details',
                              style: textTheme.headingM.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            Icon(Icons.edit,
                                color: theme.colorTheme.primary.primary1),
                          ],
                        ),
                        const Row(
                          children: [
                            KeyColumn(keys: [
                              'Count',
                              'Warranty Start Date',
                              'Warranty Duration',
                              'Brand',
                              'Model No.'
                            ]),
                            ValueColumn(values: [
                              '1',
                              '21/03/25',
                              '15 Years',
                              'Brand 1',
                              'Model 1'
                            ]),
                          ],
                        )
                      ]),
                      const SizedBox(
                        height: spacer4,
                      ),
                      DigitCard(children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              'Media',
                              style: textTheme.headingM.copyWith(
                                  color: theme.colorTheme.primary.primary2),
                            ),
                            Icon(Icons.edit,
                                color: theme.colorTheme.primary.primary1),
                          ],
                        ),
                        Column(
                          children: mediaItems
                              .map((item) => MediaDownloadItem(
                                    label: item,
                                    onDownload: () {},
                                  ))
                              .toList(),
                        ),
                      ])
                    ],
                  )),
            ],
          ),
        );
      },
    );
  }
}

class KeyColumn extends StatelessWidget {
  final List<String> keys;

  const KeyColumn({super.key, required this.keys});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

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
                      color: theme.colorTheme.text.primary,
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
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: values
          .map((value) => Padding(
                padding: const EdgeInsets.only(bottom: spacer3),
                child: Text(
                  value,
                  style: textTheme.bodyS.copyWith(
                    color: theme.colorTheme.text.primary,
                  ),
                ),
              ))
          .toList(),
    );
  }
}

class MediaDownloadItem extends StatelessWidget {
  final String label;
  final VoidCallback onDownload;

  const MediaDownloadItem({
    super.key,
    required this.label,
    required this.onDownload,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Row(
      mainAxisAlignment: MainAxisAlignment.start,
      children: [
        Text(
          label,
          style: textTheme.bodyS.copyWith(
            color: theme.colorTheme.text.primary,
          ),
        ),
        IconButton(
          onPressed: onDownload,
          icon: const Icon(Icons.download_rounded),
          color: theme.colorTheme.text.secondary,
        ),
      ],
    );
  }
}
