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
import '../data/secure_storage/secureStore.dart';
import '../model/appconfig/mdmsRequest.dart';
import '../repositories/app_init_Repo.dart';
import '../repositories/bom_repo.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DynamicFormsPage extends StatefulWidget {
  final String pageName;
  final String? schemaName; // e.g. "AssetForm"
  final String? uniqueIdentifier; // e.g. "AssetForm.SELCO"
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

  String? _currentSchemaKey(FormsState state) {
    // Prefer a schema that actually contains this page
    for (final entry in state.cachedSchemas.entries) {
      final s = entry.value;
      if (s.pages.containsKey(widget.pageName)) return entry.key;
    }
    // Otherwise, use active if present
    if (state.activeSchemaKey != null &&
        state.cachedSchemas.containsKey(state.activeSchemaKey)) {
      return state.activeSchemaKey;
    }
    // Or the first one
    if (state.cachedSchemas.isNotEmpty) {
      return state.cachedSchemas.keys.first;
    }
    return null;
  }

  Future<void> _ensureSchemaLoaded() async {
    final bloc = context.read<FormsBloc>();

    // Figure out which schema key we want to use in the cache.
    final requestedKey = widget.schemaName ?? widget.uniqueIdentifier;

    // If we already have the requested schema cached, just activate it.
    if (requestedKey != null &&
        bloc.state.cachedSchemas.containsKey(requestedKey)) {
      bloc.add(FormsUpdateEvent(
        schema: bloc.state.cachedSchemas[requestedKey]!,
        schemaKey: requestedKey,
      ));
      return;
    }

    // Otherwise, load it from storage / MDMS (same as you had)
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

          // carry uniqueIdentifier
          final uid = chosen['uniqueIdentifier']?.toString();
          if (uid != null && uid.isNotEmpty) {
            transformed['uniqueIdentifier'] = uid;
          }

          // Store for future runs (non-blocking)
          // ignore: unawaited_futures
          _repo.upsertTransformedSchema(transformed);

          schemaJson = transformed;
        }
      } catch (_) {
        // ignore; we'll fallback to snackbar below if still null
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

    await SecureStore()
        .setRawSchemaDoc(cacheKey, Map<String, dynamic>.from(schemaJson));

    bloc.add(FormsUpdateEvent(schema: schemaObj, schemaKey: cacheKey));
  }

  bool _isLastPage(SchemaObject schema) {
    final lastKey = schema.pages.keys.isEmpty ? null : schema.pages.keys.last;
    return lastKey == widget.pageName;
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_loadedOnce) return;
    _loadedOnce = true;
    // kick off loading without holding onto BuildContext across the await
    Future(() => _ensureSchemaLoaded());
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: BlocConsumer<FormsBloc, FormsState>(
        listener: (context, state) async {
          if (state is FormsSubmittedState) {
            final isLast = state.schema.pages.keys.last == widget.pageName;
            if (!isLast) return;

            // 1) Take current page values into a flat map
            final Map<String, dynamic> flatValues = {};
            state.schema.pages.forEach((pageKey, pageSchema) {
              pageSchema.properties?.forEach((propKey, propSchema) {
                flatValues[propKey] = propSchema.value;
              });
            });

            // 2) Identify projectschema key
            final projectId = widget.projectId;
            final schemaKey = widget.schemaName ??
                widget.uniqueIdentifier ??
                state.schema.name;

            // 3) Load the RAW schema doc AS-IS (you saved this when opening the screen)
            final rawDoc = await SecureStore().getRawSchemaDoc(schemaKey);
            if (rawDoc != null) {
              // 4) Merge values into RAW doc (keeps same shape as your mock/pages/properties)
              final withValues = injectValuesIntoRawDoc(
                  rawDoc: rawDoc, flatValues: flatValues);

              // 5) Save to Isar as a BOM draft unique per (projectId, schemaKey)
              final isar = context.read<ProjectBloc>().isar;

              // asset_submission.dart before submitAllDirtyForProject
              print(
                  '[BOM:form] isarInstance=${identityHashCode(isar)} project=$projectId schema=$schemaKey');
              final assignUserUuid =
                  await SecureStore().getSelectedIndividual();

              await BomRepository().saveLocal(
                isar: isar,
                projectId: projectId,
                schemaKey: schemaKey,
                rawDocWithValues: withValues,
                facilityId: null,
                assignUserUuid: assignUserUuid,
                bomName: schemaKey, // or a prettier label
              );
            }

            final key = state.activeSchemaKey ?? state.schema.name;
            context.read<FormsBloc>().add(FormsEvent.clearForm(schemaKey: key));
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
          print("widget.pageName ${widget.pageName}");
          print("schemaObject.pages ${schemaObject.pages}");
          final pageSchema = schemaObject.pages[widget.pageName];
          print(
              "schemaObject.pages[widget.pageName] ${schemaObject.pages[widget.pageName]}");
          if (pageSchema == null) {
            return const Center(child: Text('Loading Form page'));
          }

          final pageIndex =
              schemaObject.pages.keys.toList().indexOf(widget.pageName);

          return ReactiveFormBuilder(
            form: () => fb.group(
              JsonForms.getFormControls(pageSchema, defaultValues: const {}),
            ),
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
                        try {
                          // 1) Validate current page with feedback
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
                                      'Form config mismatch. Missing control: ${missing.first}')),
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

                          // 2) Merge CURRENT page values into the bloc schema
                          final values =
                              JsonForms.getFormValues(form, pageSchema);
                          final updatedPage = pageSchema.copyWith(
                            properties: Map.fromEntries(
                              pageSchema.properties?.entries.map(
                                    (e) => values.containsKey(e.key)
                                        ? MapEntry(
                                            e.key,
                                            e.value
                                                .copyWith(value: values[e.key]))
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

                          // 3) Decide using the SAME logic as your listener
                          final lastPage = _isLastPage(schemaObject);

                          if (!lastPage) {
                            // Go to next page by ordered key
                            final keysOrdered =
                                schemaObject.pages.keys.toList(growable: false);
                            final idx = keysOrdered.indexOf(widget.pageName);
                            final nextKey =
                                (idx >= 0 && idx < keysOrdered.length - 1)
                                    ? keysOrdered[idx + 1]
                                    : null;

                            if (nextKey == null) {
                              // Fallback: submit if we can’t find a next key
                              context.read<FormsBloc>().add(
                                  FormsEvent.submit(schemaKey: currentKey));
                            } else {
                              context.router.push(DynamicFormsRoute(
                                  pageName: nextKey,
                                  projectId: widget.projectId));
                            }
                            return;
                          }

                          // 4) Last page → submit
                          context
                              .read<FormsBloc>()
                              .add(FormsEvent.submit(schemaKey: currentKey));
                        } catch (e, st) {
                          debugPrint('[DynamicForms] onPressed error: $e\n$st');
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                                content: Text(
                                    'Something went wrong while submitting this page')),
                          );
                        }
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
                            .copyWith(color: theme.colorTheme.primary.primary2),
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
                      defaultValues: const {},
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
