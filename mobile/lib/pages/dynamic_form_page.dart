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

import '../blocs/project/project.dart';
import '../blocs/project_bom/project_bom.dart';
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../repositories/app_init_Repo.dart';
import '../repositories/bom_repo.dart';
import '../repositories/project_repo.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DynamicFormsPage extends StatefulWidget {
  final String pageName;
  final String? schemaName;
  final String? uniqueIdentifier;
  final String projectId;

  const DynamicFormsPage({
    super.key,
    @PathParam() required this.pageName,
    this.schemaName,
    this.uniqueIdentifier,
    required this.projectId,
  });

  @override
  State<DynamicFormsPage> createState() => _DynamicFormsPageState();
}

class _DynamicFormsPageState extends State<DynamicFormsPage> {
  final _repo = AppInitRepo();
  bool _loadedOnce = false;

  /// Track when we switched projects so we can clear & rebuild
  String? _lastProjectId;

  /// Initial KV (flat map: fieldName -> value) pulled from CacheProjectBomValues
  Map<String, dynamic> _projectInitialKV = const {};

  /// Change this to force ReactiveFormBuilder to rebuild controls with new defaults
  int _formSeed = 0;

  static const String _initialUserType = 'SUPERVISOR';

  /// Keep only keys that exist on a given page (by fieldName)
  Map<String, dynamic> _subsetForPage(
    SchemaObject schema,
    String pageName,
    Map<String, dynamic> kv,
  ) {
    final page = schema.pages[pageName];
    if (page == null || page.properties == null) return const {};
    final allowed = page.properties!.keys.toSet();
    final out = <String, dynamic>{};
    for (final entry in kv.entries) {
      if (allowed.contains(entry.key)) {
        out[entry.key] = entry.value;
      }
    }
    return out;
  }

  Future<void> _loadInitialKVForProject() async {
    final isar = context.read<ProjectBloc>().isar;
    final kv = await BomRepository().getProjectBomKV(
      isar: isar,
      projectId: widget.projectId,
      userType: _initialUserType,
    );
    setState(() {
      _projectInitialKV = kv ?? const {};
      _formSeed++; // force form controls to be recreated with new defaults
    });
  }

  String? _currentSchemaKey(FormsState state) {
    final requested = widget.schemaName ?? widget.uniqueIdentifier;
    if (requested != null && state.cachedSchemas.containsKey(requested)) {
      return requested;
    }
    final active = state.activeSchemaKey;
    if (active != null && state.cachedSchemas.containsKey(active)) {
      return active;
    }
    for (final e in state.cachedSchemas.entries) {
      if (e.value.pages.containsKey(widget.pageName)) return e.key;
    }
    return state.cachedSchemas.isEmpty ? null : state.cachedSchemas.keys.first;
  }

  Future<void> _ensureSchemaLoaded() async {
    final bloc = context.read<FormsBloc>();
    final requestedKey = widget.schemaName ?? widget.uniqueIdentifier;

    if (requestedKey != null &&
        bloc.state.cachedSchemas.containsKey(requestedKey)) {
      bloc.add(FormsUpdateEvent(
        schema: bloc.state.cachedSchemas[requestedKey]!,
        schemaKey: requestedKey,
      ));
      return;
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
          // ignore: unawaited_futures
          _repo.upsertTransformedSchema(transformed);
          schemaJson = transformed;
        }
      } catch (_) {
        // fallback
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
    final cacheKey =
        widget.schemaName ?? widget.uniqueIdentifier ?? schemaObj.name;

    await SecureStore().setRawSchemaDoc(cacheKey, Map.from(schemaJson));
    bloc.add(FormsUpdateEvent(schema: schemaObj, schemaKey: cacheKey));
  }

  bool _isLastPage(SchemaObject schema) {
    final lastKey = schema.pages.keys.isEmpty ? null : schema.pages.keys.last;
    return lastKey == widget.pageName;
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // First time: load schema, then load initial KV for this project
    if (!_loadedOnce) {
      _loadedOnce = true;
      Future(() async {
        await _ensureSchemaLoaded();
        await _loadInitialKVForProject();
        setState(() {
          _lastProjectId = widget.projectId;
        });
      });
      return;
    }

    // If project changed: clear current schema and reload KV
    if (_lastProjectId != widget.projectId) {
      final formsBloc = context.read<FormsBloc>();
      final currentKey = _currentSchemaKey(formsBloc.state);
      if (currentKey != null) {
        // Clear any values associated with the previous project/schema
        formsBloc.add(FormsEvent.clearForm(schemaKey: currentKey));
      }
      Future(() async {
        await _loadInitialKVForProject();
        setState(() {
          _lastProjectId = widget.projectId;
        });
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // If BOM sync completes and writes CacheProjectBomValues,
    // refresh our local KV and rebuild the controls so defaults apply.
    return BlocListener<ProjectBomBloc, ProjectBomState>(
      listener: (context, state) async {
        state.maybeWhen(
          success: (_) async {
            await _loadInitialKVForProject(); // updates _projectInitialKV + bumps _formSeed
            final formsBloc = context.read<FormsBloc>();
            final currentKey = _currentSchemaKey(formsBloc.state);
            if (currentKey != null) {
              formsBloc.add(FormsEvent.clearForm(schemaKey: currentKey));
            }
            // setState already done in _loadInitialKVForProject
          },
          orElse: () {},
        );
      },
      child: Scaffold(
        body: BlocConsumer<FormsBloc, FormsState>(
          listener: (context, state) async {
            if (state is FormsSubmittedState) {
              final isLast = state.schema.pages.keys.last == widget.pageName;
              if (!isLast) return;

              // 1) Flat values
              final Map<String, dynamic> flatValues = {};
              state.schema.pages.forEach((pageKey, pageSchema) {
                pageSchema.properties?.forEach((propKey, propSchema) {
                  flatValues[propKey] = propSchema.value;
                });
              });

              final projectId = widget.projectId;
              final schemaKey = widget.schemaName ??
                  widget.uniqueIdentifier ??
                  state.schema.name;

              final rawDoc = await SecureStore().getRawSchemaDoc(schemaKey);
              if (rawDoc != null) {
                final withValues = injectValuesIntoRawDoc(
                  rawDoc: rawDoc,
                  flatValues: flatValues,
                );
                final isar = context.read<ProjectBloc>().isar;
                final assignUserUuid =
                    await SecureStore().getSelectedIndividual();

                await BomRepository().saveLocal(
                  isar: isar,
                  projectId: projectId,
                  schemaKey: schemaKey,
                  rawDocWithValues: withValues,
                  facilityId: null,
                  assignUserUuid: assignUserUuid,
                  bomName: schemaKey,
                );

                // Merge just the non-empty page KV into CacheProjectBomValues
                final kvFromThisPage =
                    BomRepository().extractKVFromRawDoc(withValues);
                final filtered = Map<String, dynamic>.from(kvFromThisPage)
                  ..removeWhere((k, v) => v is String && v.trim().isEmpty);

                // Load existing KV for this project (all keys)
                final existingAllKV = await BomRepository().getProjectBomKV(
                      isar: isar,
                      projectId: projectId,
                      userType: USER_TYPES.SUPERVISOR.name,
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
                    final prevS = prev?.toString() ?? '';
                    final nextS = v?.toString() ?? '';
                    if (prevS != nextS) changed = true;
                  }
                });

                await BomRepository().mergeKvForEntryKey(
                  isar: isar,
                  projectId: projectId,
                  userType: USER_TYPES.SUPERVISOR.name,
                  kvUpdate: filtered,
                );

                // Mark as prefilled only if something actually changed
                if (changed) {
                  await PrefilledProjectRepository(isar).addOrTouch(
                    projectId: widget.projectId,
                    userType: _initialUserType, // 'SUPERVISOR'
                  );
                }
              }

              // Clear this schema’s form state so next project doesn’t inherit values
              final key = state.activeSchemaKey ?? state.schema.name;
              context
                  .read<FormsBloc>()
                  .add(FormsEvent.clearForm(schemaKey: key));

              context.router.popAndPush(const OverallAssetSummaryRoute());
            }
          },
          builder: (context, state) {
            final currentKey = _currentSchemaKey(state);
            if (currentKey == null) {
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

            // Defaults only for controls on this page (flat map)
            final pageDefaults = _subsetForPage(
              schemaObject,
              widget.pageName,
              _projectInitialKV,
            );

            return ReactiveFormBuilder(
              // Force rebuild of controls when defaults change:
              key: ValueKey(
                  '${widget.projectId}::$currentKey::$pageIndex::$_formSeed'),
              // ---- CRITICAL PART: build controls, then enforce defaults/nulls ----
              form: () {
                // Build controls without defaults first
                final controls = JsonForms.getFormControls(pageSchema,
                    defaultValues: const {});
                final form = fb.group(controls);

                final propertyKeys =
                    (pageSchema.properties?.keys.toList() ?? const <String>[]);

                if (_projectInitialKV.isEmpty) {
                  // No KV for this project: hard-reset every control on this page to null
                  for (final k in propertyKeys) {
                    if (form.contains(k)) {
                      form.control(k).reset(
                          value: null, updateParent: true, emitEvent: false);
                    }
                  }
                } else {
                  // Apply only available defaults; clear everything else to null
                  // 1) set provided defaults
                  pageDefaults.forEach((k, v) {
                    if (form.contains(k)) {
                      form
                          .control(k)
                          .updateValue(v, updateParent: true, emitEvent: false);
                    }
                  });
                  // 2) clear missing keys
                  for (final k in propertyKeys) {
                    if (!pageDefaults.containsKey(k) && form.contains(k)) {
                      form.control(k).reset(
                          value: null, updateParent: true, emitEvent: false);
                    }
                  }
                }
                return form;
              },
              // -------------------------------------------------------------------
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
                          // validation & logic ...
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
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                  content: Text(
                                      'Form config mismatch: missing ${missing.first}')),
                            );
                            return;
                          }
                          if (invalid.isNotEmpty) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(
                                  content:
                                      Text('Please correct: ${invalid.first}')),
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

                          final lastPage = _isLastPage(schemaObject);
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
                                projectId: widget.projectId,
                                schemaName: currentKey,
                              ));
                            }
                            return;
                          }

                          context
                              .read<FormsBloc>()
                              .add(FormsEvent.submit(schemaKey: currentKey));
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
                        // also pass the defaults to JsonForms (in case it uses them for rendering)
                        defaultValues: pageDefaults,
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
