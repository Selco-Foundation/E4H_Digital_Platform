import 'dart:async';

import 'package:digit_forms_engine/blocs/forms/forms.dart';
import 'package:digit_forms_engine/json_forms.dart';
import 'package:digit_forms_engine/models/schema_object/schema_object.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:reactive_forms/reactive_forms.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/activity_facility_bom/activity_facility_bom.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../repositories/activity_facility_repo.dart';
import '../repositories/app_init_repo.dart';
import '../repositories/dynamic_form_repo.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DynamicFormsPage extends StatefulWidget {
  final String pageName;
  final String? schemaName;
  final String? uniqueIdentifier;
  final String activityFacilityId;
  final FormOrigin origin;
  final String userType;

  const DynamicFormsPage({
    super.key,
    @PathParam() required this.pageName,
    this.schemaName,
    this.uniqueIdentifier,
    required this.activityFacilityId,
    required this.origin,
    required this.userType,
  });

  @override
  State<DynamicFormsPage> createState() => _DynamicFormsPageState();
}

class _DynamicFormsPageState extends State<DynamicFormsPage> {
  final _repo = AppInitRepo();
  bool _loadedOnce = false;

  String? _lastActivityFacilityId;
  Map<String, dynamic> _activityFacilityInitialKV = const {};
  int _formSeed = 0;
  static final Map<String, String> _schemaOwnerByFacility = {};
  String? get _baseSchemaKey => widget.schemaName ?? widget.uniqueIdentifier;

  late FormsBloc _formsBloc;
  bool _isPreparingForm = false;

  Future<void> _loadInitialKVForActivityFacility() async {
    final isar = context.read<ActivityFacilityBloc>().isar;
    final kv = await BomRepository().getProjectBomKV(
      isar: isar,
      activityFacilityId: widget.activityFacilityId,
      userType: widget.userType,
    );
    if (!mounted) return;
    setState(() {
      _activityFacilityInitialKV = kv ?? const {};
      _formSeed++;
    });
  }

  Future<void> _ensureSchemaLoaded() async {
    final bloc = context.read<FormsBloc>();

    final baseKey = _baseSchemaKey;

    if (baseKey != null) {
      final owner = _schemaOwnerByFacility[baseKey];
      final isSameFacility =
          owner != null && owner == widget.activityFacilityId;

      if (isSameFacility && bloc.state.cachedSchemas.containsKey(baseKey)) {
        bloc.add(
          FormsUpdateEvent(
            schema: bloc.state.cachedSchemas[baseKey]!,
            schemaKey: baseKey,
          ),
        );
        return;
      }
    }

    Map<String, dynamic>? schemaJson;
    if (widget.schemaName != null) {
      schemaJson = await _repo.loadByName(widget.schemaName!);
    }
    schemaJson ??= (widget.uniqueIdentifier != null)
        ? await _repo.loadByUniqueIdentifier(widget.uniqueIdentifier!)
        : null;

    if (schemaJson == null) {
      try {
        final rawDocs = await _repo.searchFormConfigsRaw(
          const MdmsRequestModel(
            mdmsCriteria: MdmsCriteriaModel(
              tenantId: 'in',
              moduleDetails: [
                MdmsModuleDetailsModel(
                  moduleName: 'SELCO',
                  masterDetails: [MdmsMasterDetailsModel('FormConfig')],
                ),
              ],
            ),
          ),
        );

        Map<String, dynamic>? chosen;
        if (widget.uniqueIdentifier != null) {
          chosen = rawDocs.firstWhere(
            (d) => d['uniqueIdentifier']?.toString() == widget.uniqueIdentifier,
            orElse: () => {},
          );
          if (chosen.isEmpty) chosen = null;
        }
        chosen ??= (widget.schemaName != null)
            ? (rawDocs.firstWhere(
                (d) => (d['data']?['name']?.toString() == widget.schemaName),
                orElse: () => {},
              ))
            : null;

        if (chosen != null && chosen.isNotEmpty) {
          final transformed = transformSelcoFormMdmsDocToSchema(chosen);
          final uid = chosen['uniqueIdentifier']?.toString();
          if (uid != null && uid.isNotEmpty) {
            transformed['uniqueIdentifier'] = uid;
          }
          _repo.upsertTransformedSchema(transformed);
          schemaJson = transformed;
        }
      } catch (_) {
      }
    }

    if (!mounted) return;

    if (schemaJson == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Loading schema')),
      );
      return;
    }

    final schemaObj = SchemaObject.fromJson(schemaJson);

    final cacheKey = _baseSchemaKey ?? schemaObj.name;
    _schemaOwnerByFacility[cacheKey] = widget.activityFacilityId;
    await SecureStore().setRawSchemaDoc(cacheKey, Map.from(schemaJson));
    bloc.add(FormsUpdateEvent(schema: schemaObj, schemaKey: cacheKey));
  }

  void _popUntilThenRefreshOrigin(BuildContext context, FormOrigin origin) {
    final root = context.router.root;

    late final PageRouteInfo targetRoute;
    switch (origin) {
      case FormOrigin.overallSummary:
      case FormOrigin.submitted:
        targetRoute = OverallAssetSummaryRoute(
            refresh: DateTime.now().millisecondsSinceEpoch);
        break;
      case FormOrigin.inboxSummary:
        targetRoute = InboxAssetSummaryRoute(
            refresh: DateTime.now().millisecondsSinceEpoch);
        break;
      case FormOrigin.submitForApproval:
        targetRoute = SubmitForApprovalRoute(
            refresh: DateTime.now().millisecondsSinceEpoch);
        break;
    }

    root.navigate(targetRoute);

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!context.mounted) return;
      final topName = root.current.name;
      final expected = targetRoute.routeName;
      if (topName == expected) {
        root.replace(targetRoute);
      }
    });
  }

  void _prepareFormForCurrentActivityFacility() {
    final formsBloc = context.read<FormsBloc>();

    final currentKey = currentSchemaKey(
      state: formsBloc.state,
      pageName: widget.pageName,
      schemaName: widget.schemaName,
      uniqueIdentifier: widget.uniqueIdentifier,
    );

    setState(() {
      _isPreparingForm = true;
      _activityFacilityInitialKV = const {};
      _formSeed++;
    });

    if (currentKey != null) {
      formsBloc.add(FormsEvent.clearForm(schemaKey: currentKey));
    }

    Future(() async {
      await _ensureSchemaLoaded();
      await _loadInitialKVForActivityFacility();
      if (!mounted) return;
      setState(() {
        _lastActivityFacilityId = widget.activityFacilityId;
        _isPreparingForm = false;
      });
    });
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _formsBloc = context.read<FormsBloc>();

    if (!_loadedOnce) {
      _loadedOnce = true;
      _prepareFormForCurrentActivityFacility();
      return;
    }

    if (_lastActivityFacilityId != widget.activityFacilityId) {
      _prepareFormForCurrentActivityFacility();
    }
  }

  @override
  void dispose() {
    final key = currentSchemaKey(
      state: _formsBloc.state,
      pageName: widget.pageName,
      schemaName: widget.schemaName,
      uniqueIdentifier: widget.uniqueIdentifier,
    );
    if (key != null) {
      _formsBloc.add(FormsEvent.clearForm(schemaKey: key));
    }
    super.dispose();
  }

  Future<void> _finalizeAndReturn({
    required SchemaObject schema,
    required String schemaKey,
  }) async {
    final formsBloc = context.read<FormsBloc>();
    final projectBloc = context.read<ActivityFacilityBloc>();
    final projectId = widget.activityFacilityId;

    final Map<String, dynamic> flatValues = {};
    schema.pages.forEach((_, pageSchema) {
      pageSchema.properties?.forEach((propKey, propSchema) {
        flatValues[propKey] = propSchema.value;
      });
    });

    final rawDoc = await SecureStore().getRawSchemaDoc(schemaKey);
    if (rawDoc != null) {
      final withValues = injectValuesIntoRawDoc(
        rawDoc: rawDoc,
        flatValues: flatValues,
      );

      final isar = projectBloc.isar;
      final assignUserUuid = await SecureStore().getSelectedIndividual();

      await BomRepository().saveLocal(
        isar: isar,
        activityFacilityId: projectId,
        schemaKey: schemaKey,
        rawDocWithValues: withValues,
        facilityId: null,
        assignUserUuid: assignUserUuid,
        bomName: schemaKey,
      );

      final kvFromThisPage = extractKVFromRawDoc(withValues);
      final filtered = Map<String, dynamic>.from(kvFromThisPage)
        ..removeWhere((k, v) => v is String && v.trim().isEmpty);

      final existingAllKV = await BomRepository().getProjectBomKV(
            isar: isar,
            activityFacilityId: projectId,
            userType: widget.userType,
          ) ??
          <String, dynamic>{};

      bool changed = false;
      filtered.forEach((k, v) {
        if (!existingAllKV.containsKey(k)) {
          changed = true;
          return;
        }
        final prev = existingAllKV[k];
        if (prev is num && v is num) {
          if (prev != v) changed = true;
        } else {
          if ((prev?.toString() ?? '') != (v?.toString() ?? '')) changed = true;
        }
      });

      await BomRepository().mergeKvForEntryKey(
        isar: isar,
        projectId: projectId,
        userType: widget.userType,
        kvUpdate: filtered,
      );

      if (changed) {
        await PrefilledActivityFacilityRepository(isar).addOrTouch(
          activityFacilityId: projectId,
          userType: widget.userType,
        );
      }
    }

    formsBloc.add(FormsEvent.clearForm(schemaKey: schemaKey));
    if (!context.mounted) return;
    _popUntilThenRefreshOrigin(context, widget.origin);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return BlocListener<ActivityFacilityBomBloc, ActivityFacilityBomState>(
      listener: (context, state) async {
        state.maybeWhen(
          success: (_) async {
            await _loadInitialKVForActivityFacility();
            if (!mounted) return;
            final formsBloc = context.read<FormsBloc>();
            final currentKey = currentSchemaKey(
                state: formsBloc.state,
                pageName: widget.pageName,
                schemaName: widget.schemaName,
                uniqueIdentifier: widget.uniqueIdentifier);
            if (currentKey != null) {
              formsBloc.add(FormsEvent.clearForm(schemaKey: currentKey));
            }
          },
          orElse: () {},
        );
      },
      child: Scaffold(
        body: BlocConsumer<FormsBloc, FormsState>(
          listener: (context, state) async {
            if (state is FormsSubmittedState) return;
          },
          builder: (context, state) {
            final currentKey = currentSchemaKey(
                state: state,
                pageName: widget.pageName,
                schemaName: widget.schemaName,
                uniqueIdentifier: widget.uniqueIdentifier);
            if (_isPreparingForm || currentKey == null) {
              return const Center(child: CircularProgressIndicator());
            }
            final schemaObject = state.cachedSchemas[currentKey];
            if (schemaObject == null) {
              return const Center(child: Text('Form schema missing.'));
            }
            final pageSchema = schemaObject.pages[widget.pageName];
            if (pageSchema == null) {
              Future.microtask(_ensureSchemaLoaded);
              return const Center(child: CircularProgressIndicator());
            }

            final pageIndex =
                schemaObject.pages.keys.toList().indexOf(widget.pageName);

            final pageDefaults = subsetForPage(
              schemaObject,
              widget.pageName,
              _activityFacilityInitialKV,
            );

            return ReactiveFormBuilder(
              key: ValueKey(
                  '${widget.activityFacilityId}::$currentKey::$pageIndex::$_formSeed'),
              form: () {
                final controls = JsonForms.getFormControls(pageSchema,
                    defaultValues: const {});
                final form = fb.group(controls);

                final propertyKeys =
                    (pageSchema.properties?.keys.toList() ?? const <String>[]);

                if (_activityFacilityInitialKV.isEmpty) {
                  for (final k in propertyKeys) {
                    if (!form.contains(k)) continue;
                    final ctrl = form.control(k);

                    final schemaVal = pageSchema.properties?[k]?.value;
                    ctrl.reset(
                        value: (schemaVal != null &&
                                schemaVal.toString().isNotEmpty)
                            ? schemaVal
                            : null,
                        updateParent: true,
                        emitEvent: false);
                  }
                  return form;
                }

                for (final k in propertyKeys) {
                  if (!form.contains(k)) continue;
                  final ctrl = form.control(k);

                  if (pageDefaults.containsKey(k)) {
                    final raw = pageDefaults[k];
                    final coerced = coerceForControl(ctrl, raw);
                    ctrl.updateValue(coerced,
                        updateParent: true, emitEvent: false);
                    continue;
                  } else {
                    final schemaVal = pageSchema.properties?[k]?.value;
                    if (schemaVal != null && schemaVal.toString().isNotEmpty) {
                      ctrl.updateValue(schemaVal,
                          updateParent: true, emitEvent: false);
                    } else {
                      ctrl.reset(
                          value: null, updateParent: true, emitEvent: false);
                    }
                  }
                }
                return form;
              },
              builder: (context, formGroup, child) => ScrollableContent(
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
                    ReactiveFormConsumer(
                      builder: (context, form, child) => DigitButton(
                        label: (pageIndex) < schemaObject.pages.length - 1
                            ? (pageSchema.actionLabel ?? 'Next')
                            : (pageSchema.actionLabel ?? 'Submit'),
                        onPressed: () async {
                          final propKeys =
                              (pageSchema.properties?.keys.toList() ??
                                  <String>[]);
                          final missing = <String>[];
                          final invalid = <String>[];

                          for (final k in propKeys) {
                            if (form.contains(k)) {
                              final c = form.control(k);
                              c.markAsTouched();
                              c.updateValueAndValidity();
                              if (!c.valid) invalid.add(k);
                            } else {
                              missing.add(k);
                            }
                          }

                          if (missing.isNotEmpty) {
                            final first = missing.first;
                            final label = labelForKey(pageSchema, first);
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('$label is required')),
                            );
                            return;
                          }
                          if (invalid.isNotEmpty) {
                            final first = invalid.first;
                            final label = labelForKey(pageSchema, first);

                            final c = form.control(first);
                            String? reason;
                            final errors = c.errors;
                            if (errors
                                .containsKey(ValidationMessage.required)) {
                              reason = 'is required';
                            } else if (errors
                                .containsKey(ValidationMessage.pattern)) {
                              reason = 'has an invalid format';
                            } else if (errors
                                .containsKey(ValidationMessage.number)) {
                              reason = 'must be a number';
                            } else if (errors
                                .containsKey(ValidationMessage.min)) {
                              reason = 'is below the minimum';
                            } else if (errors
                                .containsKey(ValidationMessage.max)) {
                              reason = 'is above the maximum';
                            }

                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                  content: Text(reason == null
                                      ? 'Please correct: $label'
                                      : '$label $reason')),
                            );
                            return;
                          }

                          final values =
                              JsonForms.getFormValues(form, pageSchema);
                          final updatedPage = pageSchema.copyWith(
                            properties: Map.fromEntries(
                              pageSchema.properties?.entries.map(
                                    (e) => values.containsKey(e.key)
                                        ? MapEntry(
                                            e.key,
                                            e.value
                                                .copyWith(value: values[e.key]),
                                          )
                                        : MapEntry(e.key, e.value),
                                  ) ??
                                  [],
                            ),
                          );

                          context.read<FormsBloc>().add(
                                FormsUpdateEvent(
                                  schema: schemaObject.copyWith(
                                    pages: Map.fromEntries(
                                      schemaObject.pages.entries.map(
                                        (entry) => MapEntry(
                                          entry.key,
                                          entry.key == widget.pageName
                                              ? updatedPage
                                              : entry.value,
                                        ),
                                      ),
                                    ),
                                  ),
                                  schemaKey: currentKey,
                                ),
                              );

                          final lastPage = isLastPage(
                              schema: schemaObject, pageName: widget.pageName);
                          if (!lastPage) {
                            final keys = schemaObject.pages.keys.toList();
                            final idx = keys.indexOf(widget.pageName);
                            final next = (idx >= 0 && idx < keys.length - 1)
                                ? keys[idx + 1]
                                : null;
                            if (next == null) {
                              context.read<FormsBloc>().add(
                                  FormsEvent.submit(schemaKey: currentKey));
                            } else {
                              context.router.push(DynamicFormsRoute(
                                pageName: next,
                                projectId: widget.activityFacilityId,
                                schemaName: currentKey,
                                origin: widget.origin,
                                userType: widget.userType,
                              ));
                            }
                            return;
                          }
                          final updatedSchema = schemaObject.copyWith(
                            pages: Map.fromEntries(
                              schemaObject.pages.entries.map(
                                (entry) => MapEntry(
                                  entry.key,
                                  entry.key == widget.pageName
                                      ? updatedPage
                                      : entry.value,
                                ),
                              ),
                            ),
                          );

                          context.read<FormsBloc>().add(
                                FormsUpdateEvent(
                                    schema: updatedSchema,
                                    schemaKey: currentKey),
                              );
                          await _finalizeAndReturn(
                              schema: updatedSchema, schemaKey: currentKey);
                        },
                        type: DigitButtonType.primary,
                        size: DigitButtonSize.large,
                        mainAxisSize: MainAxisSize.max,
                      ),
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
                          schemaObject.pages.length,
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
                          style: Theme.of(context)
                              .digitTextTheme(context)
                              .headingXl
                              .copyWith(
                                  color: theme.colorTheme.primary.primary2),
                        ),
                      if (pageSchema.description != null)
                        Text(
                          pageSchema.description!,
                          style: Theme.of(context)
                              .digitTextTheme(context)
                              .bodyS
                              .copyWith(color: theme.colorTheme.text.secondary),
                        ),
                      JsonForms(
                        currentSchemaKey: currentKey,
                        propertySchema: pageSchema,
                        pageName: widget.pageName,
                        childrens: const [],
                        defaultValues: pageDefaults,
                        isView: (widget.origin == FormOrigin.inboxSummary ||
                                widget.origin == FormOrigin.submitted)
                            ? true
                            : false,
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
