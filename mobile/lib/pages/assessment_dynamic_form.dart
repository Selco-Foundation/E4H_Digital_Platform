import 'dart:async';
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

import '../blocs/auth/authbloc.dart';
import '../model/assessment/assessment_form.dart';
import '../model/assessment/assessment_form_type.dart';
import '../model/assessment/assessment_mode.dart';
import '../model/assessment/assessment_queue.dart';
import '../repositories/assessment_draft_repo.dart';
import '../repositories/assessment_form_repo.dart';
import '../router/app_router.dart';
import '../utils/assessment_form_mapper.dart';
import '../utils/app_logger.dart';
import '../utils/constants.dart';
import '../utils/envConfig.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AssessmentDynamicFormPage extends StatefulWidget {
  final AssessmentQueueFacility facility;
  final AssessmentMode assessmentMode;
  final VoidCallback onSubmissionSucceeded;
  final AssessmentSubmissionRequest? draftRequest;
  final Map<String, dynamic>? draftFacilityDefaults;

  const AssessmentDynamicFormPage({
    super.key,
    required this.facility,
    required this.assessmentMode,
    required this.onSubmissionSucceeded,
    this.draftRequest,
    this.draftFacilityDefaults,
  });

  @override
  State<AssessmentDynamicFormPage> createState() =>
      _AssessmentDynamicFormPageState();
}

class _AssessmentDynamicFormPageState extends State<AssessmentDynamicFormPage> {
  final AssessmentFormRepository _repository = AssessmentFormRepository();
  int _pageIndex = 0;
  bool _isLoading = true;
  bool _isSubmitting = false;
  bool _isFrozenDraft = false;
  String? _error;
  AssessmentSubmissionRequest? _pendingRequest;
  AssessmentFormType? _resolvedFormType;
  AssessmentFacilityDetails? _facilityDetails;

  String get _schemaKey =>
      '${_resolvedFormType?.schemaName ?? 'Assessment.Pending'}:'
      '${widget.facility.planFacilityId ?? 'missing'}';

  @override
  void initState() {
    super.initState();
    _pendingRequest = widget.draftRequest;
    _isFrozenDraft = widget.draftRequest != null;
    WidgetsBinding.instance.addPostFrameCallback((_) => _load());
  }

  Future<void> _load() async {
    final draftRequest = widget.draftRequest;
    final planFacilityId =
        (draftRequest?.planFacilityId ?? widget.facility.planFacilityId)
            ?.trim();
    final category =
        (draftRequest?.facilityCategory ?? widget.facility.facilityCategory)
            ?.trim();
    if (planFacilityId == null ||
        planFacilityId.isEmpty ||
        category == null ||
        category.isEmpty) {
      setState(() {
        _isLoading = false;
        _error = context.translate(i18.assessmentForm.missingFacilityData);
      });
      return;
    }
    setState(() {
      _isLoading = true;
      _error = null;
      _facilityDetails = null;
    });
    try {
      late final AssessmentFormType formType;
      late final Map<String, dynamic> schema;
      if (draftRequest != null) {
        final draftMode = draftRequest.assessmentPhase == AssessmentPhase.PHONE
            ? AssessmentMode.remote
            : AssessmentMode.onSite;
        final resolved = AssessmentFormType.expectedFor(
          facilityCategory: category,
          mode: draftMode,
        );
        if (resolved == null) {
          throw const FormatException(
            'Unsupported assessment facility category',
          );
        }
        formType = resolved;
        schema = await _repository.loadCachedMobileSchema(formType);
      } else {
        final facilityDetails = _getFacilityDetails(
          widget.facility.facilityId,
        );
        final resolution = await _repository.resolveForm(
          planFacilityId: planFacilityId,
          facilityCategory: category,
          assessmentMode: widget.assessmentMode,
        );
        formType = resolution.formType;
        _facilityDetails = await facilityDetails;
        schema = await _repository.loadMobileSchema(formType);
      }
      _resolvedFormType = formType;
      if (!mounted) return;
      schema['name'] = _schemaKey;
      final bloc = context.read<FormsBloc>();
      if (bloc.state.cachedSchemas.containsKey(_schemaKey)) {
        if (mounted) setState(() => _isLoading = false);
        return;
      }
      final loaded = bloc.stream.firstWhere(
        (state) => state.cachedSchemas.containsKey(_schemaKey),
      );
      bloc.add(FormsEvent.load(schemas: [jsonEncode(schema)]));
      await loaded;
      if (mounted) setState(() => _isLoading = false);
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _error = error.toString();
      });
    }
  }

  Future<AssessmentFacilityDetails?> _getFacilityDetails(
    String? facilityId,
  ) async {
    final normalized = facilityId?.trim();
    if (normalized == null || normalized.isEmpty) return null;
    try {
      return await _repository.getFacilityDetails(facilityId: normalized);
    } catch (error) {
      AppLogger.instance.error(
        title: 'Assessment facility details',
        message: error.toString(),
      );
      return null;
    }
  }

  Map<String, dynamic> _defaults() {
    final user = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, userRequest) => userRequest,
          orElse: () => null,
        );
    String display(String? value) {
      final normalized = value?.trim();
      return normalized == null || normalized.isEmpty ? '---' : normalized;
    }

    String editable(String? value) => value?.trim() ?? '';
    final formType = _resolvedFormType;

    final draftRequest = widget.draftRequest;
    return {
      'assessorName': display(user?.name ?? user?.userName),
      'assessorContact': editable(user?.mobileNumber),
      'callDate': DateTime.now(),
      if (formType != null)
        ...buildAssessmentFacilityDefaults(
          facility: _facilityDetails,
          formType: formType,
          fallbackFacility: widget.facility,
        ),
      if (draftRequest != null)
        ...buildAssessmentDraftDefaults(
          request: draftRequest,
          facility: widget.facility,
          facilityDefaults: widget.draftFacilityDefaults,
        ),
    };
  }

  FormGroup _buildForm(PropertySchema page, Map<String, dynamic> defaults) {
    final controlDefaults = _isFrozenDraft
        ? normalizeAssessmentDraftDefaultsForPage(
            page: page,
            defaults: defaults,
          )
        : defaults;
    final controls = JsonForms.getFormControls(
      page,
      defaultValues: controlDefaults,
    );
    final form = fb.group(controls);
    for (final entry
        in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[]) {
      if (!form.contains(entry.key)) continue;
      final value = entry.value.value ?? controlDefaults[entry.key];
      if (value != null) {
        form.control(entry.key).updateValue(
              coerceForControl(form.control(entry.key), value),
              emitEvent: false,
            );
      }
    }
    if (_isFrozenDraft) form.markAsDisabled();
    return form;
  }

  SchemaObject _withPageValues(
    SchemaObject schema,
    String pageKey,
    PropertySchema page,
    FormGroup form,
  ) {
    final values = JsonForms.getFormValues(form, page);
    final updatedPage = page.copyWith(
      properties: {
        for (final entry
            in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
          entry.key: values.containsKey(entry.key)
              ? entry.value.copyWith(value: values[entry.key])
              : entry.value,
      },
    );
    return schema.copyWith(
      pages: {
        for (final entry in schema.pages.entries)
          entry.key: entry.key == pageKey ? updatedPage : entry.value,
      },
    );
  }

  String _visibilitySignature(
    SchemaObject schema,
    String pageKey,
    PropertySchema page,
    FormGroup form,
  ) {
    final values = <String, dynamic>{
      for (final pageEntry in schema.pages.entries)
        pageEntry.key: <String, dynamic>{
          for (final property in pageEntry.value.properties?.entries ??
              <MapEntry<String, PropertySchema>>[])
            property.key: property.value.value,
        },
    };
    values[pageKey] = <String, dynamic>{
      if (values[pageKey] is Map)
        ...Map<String, dynamic>.from(values[pageKey] as Map),
      ...form.rawValue,
    };
    return (page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
        .where(
          (entry) => isAssessmentPropertyVisible(
            pageKey: pageKey,
            property: entry.value,
            values: values,
          ),
        )
        .map((entry) => entry.key)
        .join('|');
  }

  Future<void> _continue(
    FormGroup form,
    SchemaObject schema,
    String pageKey,
    PropertySchema page,
  ) async {
    if (_isSubmitting) return;
    if (_isFrozenDraft) {
      if (_pageIndex < schema.pages.length - 1) {
        setState(() => _pageIndex++);
      } else {
        await _submit(schema);
      }
      return;
    }
    final updated = _withPageValues(schema, pageKey, page, form);
    final allValues = {
      for (final pageEntry in updated.pages.entries)
        pageEntry.key: {
          for (final property in pageEntry.value.properties?.entries ??
              <MapEntry<String, PropertySchema>>[])
            property.key: property.value.value,
        },
    };
    String? invalid;
    for (final entry
        in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[]) {
      if (entry.value.readOnly == true || !form.contains(entry.key)) continue;
      if (!isAssessmentPropertyVisible(
        pageKey: pageKey,
        property: entry.value,
        values: allValues,
      )) {
        continue;
      }
      final control = form.control(entry.key);
      control.markAsTouched();
      control.updateValueAndValidity();
      if (!control.valid) {
        invalid = entry.key;
        break;
      }
    }
    if (invalid != null) {
      final label = labelForKey(page, invalid);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            '${context.translate(i18.common.pleaseCorrect)}: $label',
          ),
        ),
      );
      return;
    }
    context.read<FormsBloc>().add(
          FormsUpdateEvent(schema: updated, schemaKey: _schemaKey),
        );
    if (_pageIndex < updated.pages.length - 1) {
      setState(() => _pageIndex++);
      return;
    }
    await _submit(updated);
  }

  void _previousPage(
    FormGroup form,
    SchemaObject schema,
    String pageKey,
    PropertySchema page,
  ) {
    if (_pageIndex == 0) return;
    if (_isFrozenDraft) {
      setState(() => _pageIndex--);
      return;
    }
    final updated = _withPageValues(schema, pageKey, page, form);
    context.read<FormsBloc>().add(
          FormsUpdateEvent(schema: updated, schemaKey: _schemaKey),
        );
    setState(() => _pageIndex--);
  }

  Future<void> _submit(SchemaObject schema) async {
    final planFacilityId = widget.facility.planFacilityId!;
    final user = context.read<AuthBloc>().state.maybeWhen(
          authenticated: (_, __, value) => value,
          orElse: () => null,
        );
    final formType = _resolvedFormType;
    if (formType == null) return;
    final request = _pendingRequest ??
        AssessmentSubmissionRequest(
          planFacilityId: planFacilityId,
          tenantId: envConfig.variables.tenantId,
          facilityCategory: formType.facilityCategory,
          assessmentPhase: formType.phase,
          submissionData: Map<String, dynamic>.from(
            jsonSafe(buildAssessmentSubmissionData(schema)) as Map,
          ),
          submittedByName: (user?.name ?? user?.userName ?? '').trim(),
          clientSubmissionTime: DateTime.now().millisecondsSinceEpoch,
        );
    setState(() {
      _isSubmitting = true;
      _error = null;
    });
    try {
      await _repository.submitAssessment(request);
      final isar = await Constants().isar;
      await AssessmentDraftRepository(isar).delete(
        request.tenantId,
        request.planFacilityId,
        request.assessmentPhase,
      );
      if (!mounted) return;
      context.read<FormsBloc>().add(
            FormsEvent.clearForm(schemaKey: _schemaKey),
          );
      try {
        widget.onSubmissionSucceeded();
      } catch (_) {
        // Queue refresh must never block a confirmed-success transition.
      }
      if (!mounted) return;
      unawaited(
        context.router.replace<void>(
          AssessmentSubmissionSuccessRoute(schemaName: _schemaKey),
        ),
      );
    } on AssessmentApiException catch (error) {
      if (_isFrozenDraft) {
        await _saveDraftFailure(
          request,
          user?.uuid ?? user?.userName ?? '',
          error.message,
          blocked: error.isAuthorizationFailure || error.isConflict,
        );
        if (error.code == 'ASSESSMENT_INVALID_FORM_DATA') {
          _openBackendError(schema, error);
        } else if (mounted) {
          setState(() => _error = error.message);
        }
      } else if (error.code == 'ASSESSMENT_INVALID_FORM_DATA') {
        _openBackendError(schema, error);
      } else if (error.isRetryable ||
          error.isAuthorizationFailure ||
          error.isConflict) {
        final isar = await Constants().isar;
        final assessorId = user?.uuid ?? user?.userName ?? '';
        await AssessmentDraftRepository(isar).save(
          request: request,
          facility: widget.facility,
          assessorId: assessorId,
          blocked: error.isAuthorizationFailure || error.isConflict,
          error: error.message,
          facilityDefaults: _facilityDefaultsForDraft(formType),
        );
        if (mounted) {
          setState(() {
            _pendingRequest = request;
            _isFrozenDraft = true;
            _error = error.isConflict
                ? context.translate(i18.assessmentForm.conflictSaved)
                : context.translate(i18.assessmentForm.savedToDrafts);
          });
        }
      } else if (mounted) {
        setState(() => _error = error.message);
      }
    } catch (error) {
      if (_isFrozenDraft) {
        await _saveDraftFailure(
          request,
          user?.uuid ?? user?.userName ?? '',
          error.toString(),
        );
      }
      if (mounted) setState(() => _error = error.toString());
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  Future<void> _saveDraftFailure(
    AssessmentSubmissionRequest request,
    String assessorId,
    String error, {
    bool blocked = false,
  }) async {
    final isar = await Constants().isar;
    await AssessmentDraftRepository(isar).save(
      request: request,
      facility: widget.facility,
      assessorId: assessorId,
      blocked: blocked,
      error: error,
      facilityDefaults: _facilityDefaultsForDraft(_resolvedFormType),
    );
  }

  Map<String, dynamic>? _facilityDefaultsForDraft(
    AssessmentFormType? formType,
  ) {
    final cached = widget.draftFacilityDefaults;
    if (cached != null) return Map<String, dynamic>.from(cached);
    if (formType == null) return null;
    return buildAssessmentFacilityDefaults(
      facility: _facilityDetails,
      formType: formType,
      fallbackFacility: widget.facility,
    );
  }

  List<Map<String, Widget>> _customComponents(
    PropertySchema page,
    FormGroup form,
  ) {
    return [
      for (final entry
          in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
        if (entry.value.format == PropertySchemaFormat.custom &&
            (entry.key == 'facilityOpeningTime' ||
                entry.key == 'facilityClosingTime'))
          {
            entry.key: ReactiveTextField<String>(
              key: ValueKey(entry.key),
              formControlName: entry.key,
              readOnly: true,
              validationMessages: {
                ValidationMessage.required: (_) => 'This field is required',
                ValidationMessage.pattern: (_) => 'Enter a valid value',
              },
              decoration: InputDecoration(
                labelText: entry.value.label,
                suffixIcon: const Icon(Icons.access_time),
              ),
              onTap: (control) async {
                final parts = control.value?.split(':') ?? const <String>[];
                final initial = parts.length == 2
                    ? TimeOfDay(
                        hour: int.tryParse(parts[0]) ?? TimeOfDay.now().hour,
                        minute:
                            int.tryParse(parts[1]) ?? TimeOfDay.now().minute,
                      )
                    : TimeOfDay.now();
                final selected = await showTimePicker(
                  context: context,
                  initialTime: initial,
                  helpText: entry.key == 'facilityOpeningTime'
                      ? 'Select opening time'
                      : 'Select closing time',
                  initialEntryMode: TimePickerEntryMode.dialOnly,
                  builder: (context, child) => MediaQuery(
                    data: MediaQuery.of(context)
                        .copyWith(alwaysUse24HourFormat: true),
                    child: child!,
                  ),
                );
                if (selected == null) return;
                control.updateValue(
                  '${selected.hour.toString().padLeft(2, '0')}:'
                  '${selected.minute.toString().padLeft(2, '0')}',
                );
                control.markAsTouched();
              },
            ),
          },
      for (final entry
          in page.properties?.entries ?? <MapEntry<String, PropertySchema>>[])
        if (entry.value.format == PropertySchemaFormat.custom &&
            entry.value.schemaCode?.startsWith('ASSESSMENT_OTHER_FOR:') == true)
          {
            entry.key: ReactiveValueListenableBuilder<String>(
              formControlName: entry.value.schemaCode!
                  .substring('ASSESSMENT_OTHER_FOR:'.length),
              builder: (context, control, _) {
                final show = control.value
                        ?.split('.')
                        .map((value) => value.trim().toUpperCase())
                        .contains('OTHER') ==
                    true;
                if (!show) return const SizedBox.shrink();
                return ReactiveTextField<String>(
                  formControlName: entry.key,
                  readOnly: _isFrozenDraft,
                  validationMessages: {
                    ValidationMessage.required: (_) => 'This field is required',
                  },
                  decoration: InputDecoration(
                    labelText: entry.value.label,
                  ),
                );
              },
            ),
          },
    ];
  }

  void _openBackendError(
    SchemaObject schema,
    AssessmentApiException error,
  ) {
    final field = error.fields.isEmpty ? null : error.fields.first;
    if (field != null) {
      final index = schema.pages.values.toList().indexWhere(
            (page) => page.properties?.containsKey(field) == true,
          );
      if (index >= 0) _pageIndex = index;
    }
    setState(() => _error = error.message);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    if (_error != null &&
        context.read<FormsBloc>().state.cachedSchemas[_schemaKey] == null) {
      return Scaffold(
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(context.translate(i18.assessmentForm.unableToLoad)),
              const SizedBox(height: spacer2),
              DigitButton(
                label: context.translate(i18.common.retry),
                onPressed: _load,
                type: DigitButtonType.secondary,
                size: DigitButtonSize.medium,
              ),
            ],
          ),
        ),
      );
    }
    return Scaffold(
      body: BlocBuilder<FormsBloc, FormsState>(
        builder: (context, state) {
          final schema = state.cachedSchemas[_schemaKey];
          if (schema == null || schema.pages.isEmpty) {
            return Center(
              child: Text(context.translate(i18.assessmentForm.unavailable)),
            );
          }
          final pageEntry = schema.pages.entries.elementAt(_pageIndex);
          final defaults = subsetForPage(schema, pageEntry.key, _defaults());
          return ReactiveFormBuilder(
            key: ValueKey('$_schemaKey:${pageEntry.key}:$_isFrozenDraft'),
            form: () => _buildForm(pageEntry.value, defaults),
            builder: (context, form, _) => ScrollableContent(
              backgroundColor: theme.colorTheme.generic.background,
              enableFixedDigitButton: true,
              header: Padding(
                padding: const EdgeInsets.all(spacer2),
                child: BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                  defaultPopRoute: _pageIndex == 0,
                  handleback: _pageIndex == 0
                      ? null
                      : () => _previousPage(
                            form,
                            schema,
                            pageEntry.key,
                            pageEntry.value,
                          ),
                ),
              ),
              footer: DigitCard(
                margin: const EdgeInsets.only(top: spacer2),
                children: [
                  if (_error != null) ...[
                    Text(
                      _error!,
                      style: TextStyle(color: theme.colorTheme.alert.error),
                    ),
                    const SizedBox(height: spacer2),
                  ],
                  DigitButton(
                    isDisabled: _isSubmitting,
                    mainAxisSize: MainAxisSize.max,
                    label: _isSubmitting
                        ? context.translate(i18.assessmentForm.submitting)
                        : _isFrozenDraft
                            ? context.translate(
                                _pageIndex == schema.pages.length - 1
                                    ? i18.dynamicForm.submit
                                    : i18.common.coreCommonNext,
                              )
                            : pageEntry.value.actionLabel ??
                                context.translate(
                                  _pageIndex == schema.pages.length - 1
                                      ? i18.dynamicForm.submit
                                      : i18.common.coreCommonNext,
                                ),
                    onPressed: () => _continue(
                      form,
                      schema,
                      pageEntry.key,
                      pageEntry.value,
                    ),
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
                      activeIndex: _pageIndex,
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
                    if (pageEntry.value.label != null)
                      Text(
                        pageEntry.value.label!,
                        style: theme.digitTextTheme(context).headingXl.copyWith(
                              color: theme.colorTheme.primary.primary2,
                            ),
                      ),
                    if (pageEntry.value.description != null)
                      Text(pageEntry.value.description!),
                    StreamBuilder<Object?>(
                      stream: form.valueChanges,
                      builder: (context, _) {
                        final visibilitySignature = _visibilitySignature(
                          schema,
                          pageEntry.key,
                          pageEntry.value,
                          form,
                        );
                        return IgnorePointer(
                          ignoring: _isFrozenDraft,
                          child: JsonForms(
                            key: ValueKey(
                              '$_schemaKey:${pageEntry.key}:'
                              '$visibilitySignature',
                            ),
                            currentSchemaKey: _schemaKey,
                            propertySchema: pageEntry.value,
                            pageName: pageEntry.key,
                            childrens: _customComponents(
                              pageEntry.value,
                              form,
                            ),
                            defaultValues: defaults,
                            isView: _isFrozenDraft,
                          ),
                        );
                      },
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
