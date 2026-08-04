import 'dart:convert';

import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:digit_forms_engine/json_forms.dart';
import 'package:digit_forms_engine/models/property_schema/property_schema.dart';
import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_ui_components/enum/app_enums.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/atoms/digit_button.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../repositories/assessment_mock_form_repo.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentDynamicFormPage extends StatefulWidget {
  final String pageName;
  final String schemaName;

  const AssessmentDynamicFormPage({
    super.key,
    @PathParam() required this.pageName,
    required this.schemaName,
  });

  @override
  State<AssessmentDynamicFormPage> createState() =>
      _AssessmentDynamicFormPageState();
}

class _AssessmentDynamicFormPageState extends State<AssessmentDynamicFormPage> {
  final _repository = AssessmentMockFormRepository();
  bool _isLoading = true;
  bool _isSubmitting = false;
  String? _loadError;

  static final Map<String, dynamic> _dummyDefaults = {
    'assessorName': 'Remote Assessor',
    'callDate': DateTime.now(),
    'facilityName': 'Digar Kashipur',
    'facilityType': 'Health Facility',
    'facilityAddress': 'Cedharban, Cachar, Assam',
    'facilityCode': 'HF-0001',
    'facilityInChargeName': 'Facility In-charge',
    'facilityInChargeContact': '9876543210',
    'alternateContactName': 'Alternative Contact',
    'alternateContactNumber': '9876543211',
  };

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _ensureSchemaLoaded());
  }

  Future<void> _ensureSchemaLoaded() async {
    final formsBloc = context.read<FormsBloc>();
    if (formsBloc.state.cachedSchemas.containsKey(widget.schemaName)) {
      if (mounted) setState(() => _isLoading = false);
      return;
    }

    try {
      final schema = await _repository.loadFormSchema();
      final loaded = formsBloc.stream.firstWhere(
        (state) => state.cachedSchemas.containsKey(widget.schemaName),
      );
      formsBloc.add(FormsEvent.load(schemas: [jsonEncode(schema)]));
      await loaded;
      if (mounted) setState(() => _isLoading = false);
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _loadError = error.toString();
      });
    }
  }

  FormGroup _buildForm(
    PropertySchema pageSchema,
    Map<String, dynamic> pageDefaults,
  ) {
    final controls = JsonForms.getFormControls(
      pageSchema,
      defaultValues: pageDefaults,
    );
    final form = fb.group(controls);

    final properties = pageSchema.properties;
    if (properties == null) return form;
    for (final entry in properties.entries) {
      if (!form.contains(entry.key)) continue;
      final schemaValue = entry.value.value;
      if (schemaValue != null && schemaValue.toString().isNotEmpty) {
        form.control(entry.key).updateValue(
              coerceForControl(form.control(entry.key), schemaValue),
              emitEvent: false,
            );
      } else if (pageDefaults.containsKey(entry.key)) {
        form.control(entry.key).updateValue(
              coerceForControl(
                  form.control(entry.key), pageDefaults[entry.key]),
              emitEvent: false,
            );
      }
    }
    return form;
  }

  Future<void> _continue(
    FormGroup form,
    SchemaObject schema,
    PropertySchema pageSchema,
  ) async {
    if (_isSubmitting) return;

    final invalid = <String>[];
    for (final key in pageSchema.properties?.keys ?? const <String>[]) {
      if (!form.contains(key)) continue;
      final control = form.control(key);
      control.markAsTouched();
      control.updateValueAndValidity();
      if (!control.valid) invalid.add(key);
    }

    if (invalid.isNotEmpty) {
      final label = labelForKey(pageSchema, invalid.first);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Please correct: $label')),
      );
      return;
    }

    setState(() => _isSubmitting = true);
    try {
      final values = JsonForms.getFormValues(form, pageSchema);
      final updatedPage = pageSchema.copyWith(
        properties: Map.fromEntries(
          pageSchema.properties?.entries.map(
                (entry) => MapEntry(
                  entry.key,
                  values.containsKey(entry.key)
                      ? entry.value.copyWith(value: values[entry.key])
                      : entry.value,
                ),
              ) ??
              const [],
        ),
      );
      final updatedSchema = schema.copyWith(
        pages: Map.fromEntries(
          schema.pages.entries.map(
            (entry) => MapEntry(
              entry.key,
              entry.key == widget.pageName ? updatedPage : entry.value,
            ),
          ),
        ),
      );
      final formsBloc = context.read<FormsBloc>();
      formsBloc.add(
        FormsUpdateEvent(schema: updatedSchema, schemaKey: widget.schemaName),
      );

      final pageNames = schema.pages.keys.toList();
      final pageIndex = pageNames.indexOf(widget.pageName);
      if (pageIndex >= 0 && pageIndex < pageNames.length - 1) {
        context.router.push(
          AssessmentDynamicFormRoute(
            pageName: pageNames[pageIndex + 1],
            schemaName: widget.schemaName,
          ),
        );
        return;
      }

      final submitted = formsBloc.stream.firstWhere(
        (state) =>
            state is FormsSubmittedState &&
            state.activeSchemaKey == widget.schemaName,
      );
      formsBloc.add(FormsEvent.submit(schemaKey: widget.schemaName));
      await submitted;
      if (!mounted) return;
      context.router.replaceAll([
        AssessmentSubmissionSuccessRoute(schemaName: widget.schemaName),
      ]);
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: BlocBuilder<FormsBloc, FormsState>(
        builder: (context, state) {
          if (_isLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (_loadError != null) {
            return const Center(
              child: Text('Unable to load assessment form.'),
            );
          }

          final schema = state.cachedSchemas[widget.schemaName];
          final pageSchema = schema?.pages[widget.pageName];
          if (schema == null || pageSchema == null) {
            return const Center(child: Text('Assessment form is unavailable.'));
          }

          final pageIndex = schema.pages.keys.toList().indexOf(widget.pageName);
          final pageDefaults = subsetForPage(
            schema,
            widget.pageName,
            _dummyDefaults,
          );

          return ReactiveFormBuilder(
            key: ValueKey('${widget.schemaName}:${widget.pageName}'),
            form: () => _buildForm(pageSchema, pageDefaults),
            builder: (context, form, child) => ScrollableContent(
              backgroundColor: theme.colorTheme.generic.background,
              enableFixedDigitButton: true,
              header: const Padding(
                padding: EdgeInsets.all(spacer2),
                child: BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
              ),
              footer: DigitCard(
                margin: const EdgeInsets.only(top: spacer2),
                children: [
                  DigitButton(
                    isDisabled: _isSubmitting,
                    mainAxisSize: MainAxisSize.max,
                    label: _isSubmitting
                        ? 'Please wait...'
                        : (pageSchema.actionLabel ?? 'Next'),
                    onPressed: () => _continue(form, schema, pageSchema),
                    type: DigitButtonType.primary,
                    size: DigitButtonSize.large,
                  ),
                ],
              ),
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: spacer4),
                  child: SizedBox(
                    height: spacer8,
                    child: DigitStepper(
                      activeIndex: pageIndex,
                      stepperList: List.generate(
                        schema.pages.length,
                        (_) => const StepperData(),
                      ),
                      stepperDirection: Axis.horizontal,
                      inverted: true,
                    ),
                  ),
                ),
                const SizedBox(height: spacer3),
                DigitCard(
                  margin: const EdgeInsets.symmetric(horizontal: spacer2),
                  children: [
                    if (pageSchema.label != null)
                      Text(
                        pageSchema.label!,
                        style: theme
                            .digitTextTheme(context)
                            .headingXl
                            .copyWith(color: theme.colorTheme.primary.primary2),
                      ),
                    if (pageSchema.description != null)
                      Text(
                        pageSchema.description!,
                        style: theme.digitTextTheme(context).bodyS.copyWith(
                              color: theme.colorTheme.text.secondary,
                            ),
                      ),
                    JsonForms(
                      currentSchemaKey: widget.schemaName,
                      propertySchema: pageSchema,
                      pageName: widget.pageName,
                      childrens: const [],
                      defaultValues: pageDefaults,
                      isView: false,
                    ),
                  ],
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
