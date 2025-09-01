import 'dart:convert';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/atoms/digit_stepper.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:forms_engine/blocs/forms/forms.dart';
import 'package:forms_engine/json_forms.dart';
import 'package:forms_engine/models/schema_object/schema_object.dart';
import 'package:reactive_forms/reactive_forms.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../model/appconfig/mdmsRequest.dart';
import '../repositories/app_init_Repo.dart';
import '../router/app_router.dart';
import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class DynamicFormsPage extends StatefulWidget {
  final String pageName;

  /// Provide one of these (or both). If both are null and nothing is in FormsBloc,
  /// the page will show an error.
  final String? schemaName; // e.g. "AssetForm"
  final String? uniqueIdentifier; // e.g. "AssetForm.SELCO"
  final String? projectId;

  const DynamicFormsPage({
    super.key,
    @PathParam() required this.pageName,
    this.schemaName,
    this.uniqueIdentifier,
    this.projectId,
  });

  @override
  State<DynamicFormsPage> createState() => _DynamicFormsPageState();
}

class _DynamicFormsPageState extends State<DynamicFormsPage> {
  final _repo = AppInitRepo();
  bool _loadedOnce = false;

  /// Choose the currently active schema key. If none explicitly set,
  /// fall back to the first cached one.
  String? _currentSchemaKey(FormsState state) {
    if (state.activeSchemaKey != null &&
        state.cachedSchemas.containsKey(state.activeSchemaKey)) {
      return state.activeSchemaKey;
    }
    if (state.cachedSchemas.isNotEmpty) {
      return state.cachedSchemas.keys.first;
    }
    return null;
  }

  Future<void> _ensureSchemaLoaded() async {
    final bloc = context.read<FormsBloc>();
    if (bloc.state.cachedSchemas.isNotEmpty) return;

    Map<String, dynamic>? schemaJson;

    // 1) Try secure storage
    if (widget.schemaName != null) {
      schemaJson = await _repo.loadByName(widget.schemaName!);
    }
    schemaJson ??= (widget.uniqueIdentifier != null)
        ? await _repo.loadByUniqueIdentifier(widget.uniqueIdentifier!)
        : null;

    // 2) Fallback: fetch raw -> transform -> save -> USE NOW
    if (schemaJson == null) {
      try {
        final rawDocs = await _repo.searchFormConfigsRaw(
          const MdmsRequestModel(
            mdmsCriteria: MdmsCriteriaModel(
              tenantId: 'in', // or envConfig.variables.tenantId
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

          // Persist for future launches (non-blocking for UI)
          // ignore: unawaited_futures
          _repo.upsertTransformedSchema(transformed);

          // Use immediately (don’t wait to read back from storage)
          schemaJson = transformed;
        }
      } catch (_) {
        // swallow; we'll show snackbar if still null
      }
    }

    if (!mounted) return;

    if (schemaJson == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Form schema not found')),
      );
      return;
    }

    final schemaObj = SchemaObject.fromJson(schemaJson);
    final key = schemaObj.name;
    bloc.add(FormsUpdateEvent(schema: schemaObj, schemaKey: key));
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

            // ⬇️ NEW: gather all page values into a flat map
            final Map<String, dynamic> bomValues = {};
            state.schema.pages.forEach((pageKey, pageSchema) {
              pageSchema.properties?.forEach((propKey, propSchema) {
                bomValues[propKey] = propSchema.value;
              });
            });

            // ⬇️ NEW: persist as a string for current project (fallback to 'default')
            final prefs = await SharedPreferences.getInstance();
            final projKey = widget.projectId ?? 'default';
            await prefs.setString(
                'bom_form_values_$projKey', jsonEncode(bomValues));

            final key = state.activeSchemaKey ?? state.schema.name;
            context.read<FormsBloc>().add(FormsEvent.clearForm(schemaKey: key));

            // context.router.push(const AcknowledgementRoute(isDirectCreate: true));
            // context.router.popUntilRouteWithName(OverallAssetSummaryRoute.name);
            context.router.popAndPush(const OverallAssetSummaryRoute());
          }
        },
        builder: (context, state) {
          final currentKey = _currentSchemaKey(state);

          if (currentKey == null) {
            // Still loading schema or none found
            return const Center(child: CircularProgressIndicator());
          }

          final schemaObject = state.cachedSchemas[currentKey];
          if (schemaObject == null) {
            return const Center(child: Text('Form schema missing.'));
          }

          final pageSchema = schemaObject.pages[widget.pageName];
          if (pageSchema == null) {
            return const Center(child: Text('Form page not found.'));
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
                      onPressed: () {
                        // Validate only current page
                        final keys = pageSchema.properties?.keys ?? [];
                        for (final k in keys) {
                          final c = form.control(k);
                          c.markAsTouched();
                          c.updateValueAndValidity();
                        }
                        final isValid =
                            keys.every((k) => form.control(k).valid);
                        if (!isValid) return;

                        // Extract values & update page schema
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

                        // Push back to bloc: replace only this page
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

                        // Next page or submit
                        if (pageIndex < schemaObject.pages.length - 1) {
                          final nextKey = schemaObject.pages.entries
                              .elementAt(pageIndex + 1)
                              .key;
                          context.router
                              .push(DynamicFormsRoute(pageName: nextKey));
                        } else {
                          context
                              .read<FormsBloc>()
                              .add(FormsEvent.submit(schemaKey: currentKey));
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
                      propertySchema: pageSchema,
                      childrens: const [], // add custom widgets if any
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
