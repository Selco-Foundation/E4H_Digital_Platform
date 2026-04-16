import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/cache_installation_image/cache_installation_image.dart';
import '../blocs/installation_images/installation_images.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_installation_image.dart';
import '../model/installation_images/installation_images.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/utils.dart';
import '../widgets/customized_digit_widget/image_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InstallationImagesPage extends StatefulWidget {
  const InstallationImagesPage({
    super.key,
    required this.origin,
  });

  final FormOrigin origin;

  @override
  State<InstallationImagesPage> createState() => _InstallationImagesPageState();
}

class _InstallationImagesPageState extends State<InstallationImagesPage> {
  final Map<String, List<File>> _selectedImagesByRequirement = {};
  final Map<String, String> _sectionMessages = {};
  String? _currentActivityFacilityId;
  String? _userType;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  bool _hasAttemptedValidation = false;
  bool _isSaving = false;
  bool _hydratedFromCache = false;

  bool get _isViewOnly =>
      widget.origin == FormOrigin.inboxSummary ||
      widget.origin == FormOrigin.submitted;

  @override
  void initState() {
    super.initState();
    final locBloc = context.read<LocationBloc>();
    locBloc.add(const LocationEvent.requestPermission());
    locBloc.add(const LocationEvent.requestService());
    _locSub = locBloc.stream.listen((st) {
      if (st.latitude != null && st.longitude != null) {
        setState(() {
          _latitude = st.latitude;
          _longitude = st.longitude;
        });
      }
    });

    _userType = context.read<UserTypeBloc>().state.maybeWhen(
          supervisor: () => USER_TYPES.SUPERVISOR.name,
          orElse: () => USER_TYPES.FIELD_STAFF.name,
        );

    context.read<SelectedActivityFacilityBloc>().state.whenOrNull(
      selected: (proj) {
        _currentActivityFacilityId = proj.activityFacility.id;
      },
    );

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) return;
      context
          .read<InstallationImagesBloc>()
          .add(const InstallationImagesEvent.fetch());
      _loadCachedImages();
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  Future<bool> _ensureLocationLoaded({
    Duration timeout = const Duration(seconds: 10),
  }) async {
    final locBloc = context.read<LocationBloc>();
    if (locBloc.state.latitude != null && locBloc.state.longitude != null) {
      return true;
    }
    try {
      final st = await locBloc.stream
          .firstWhere((s) => s.latitude != null && s.longitude != null)
          .timeout(timeout);
      setState(() {
        _latitude = st.latitude;
        _longitude = st.longitude;
      });
      return true;
    } catch (_) {
      return false;
    }
  }

  void _loadCachedImages() {
    if (_currentActivityFacilityId == null || _userType == null) return;
    context.read<CacheInstallationImageBloc>().add(
          CacheInstallationImageEvent.get(
            _currentActivityFacilityId!,
            _userType!,
          ),
        );
  }

  Future<void> _populateFromCache(List<CacheInstallationImage> entries) async {
    if (_hydratedFromCache) return;
    final grouped = <String, List<File>>{};

    final futures = entries.map((entry) async {
      final file = await getCachedFile(entry.photoPath);
      if (file == null) return null;
      return (code: entry.code, file: file);
    }).toList();

    final results = await Future.wait(futures);
    for (final result in results) {
      if (result == null) continue;
      grouped.putIfAbsent(result.code, () => <File>[]).add(result.file);
    }

    if (!mounted) return;
    setState(() {
      _selectedImagesByRequirement
        ..clear()
        ..addAll(grouped);
      _hydratedFromCache = true;
    });
  }

  void _popUntilThenRefreshOrigin(BuildContext context, FormOrigin origin) {
    final root = context.router.root;

    late final PageRouteInfo targetRoute;
    switch (origin) {
      case FormOrigin.overallSummary:
      case FormOrigin.submitted:
        targetRoute = OverallAssetSummaryRoute(
          refresh: DateTime.now().millisecondsSinceEpoch,
        );
        break;
      case FormOrigin.inboxSummary:
        targetRoute = InboxAssetSummaryRoute(
          refresh: DateTime.now().millisecondsSinceEpoch,
        );
        break;
      case FormOrigin.submitForApproval:
        targetRoute = SubmitForApprovalRoute(
          refresh: DateTime.now().millisecondsSinceEpoch,
        );
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

  int _selectedCountFor(InstallationImageItem requirement) {
    return _selectedImagesByRequirement[requirement.code]?.length ?? 0;
  }

  bool _isRequirementComplete(InstallationImageItem requirement) {
    return _selectedCountFor(requirement) == requirement.requiredCount;
  }

  bool _hasAnyInvalidRequirement(List<InstallationImageItem> requirements) {
    return requirements
        .any((requirement) => !_isRequirementComplete(requirement));
  }

  String? _validationErrorFor(InstallationImageItem requirement) {
    if (!_hasAttemptedValidation) return null;
    if (_isRequirementComplete(requirement)) return null;
    return 'Required ${requirement.requiredCount} '
        '${requirement.requiredCount == 1 ? 'image' : 'images'}, '
        'selected ${_selectedCountFor(requirement)}';
  }

  String? _feedbackMessageFor(InstallationImageItem requirement) {
    final customMessage = _sectionMessages[requirement.code];
    if (customMessage != null && customMessage.isNotEmpty) {
      return customMessage;
    }

    if (_isRequirementComplete(requirement)) {
      return '${requirement.requiredCount} of '
          '${requirement.requiredCount} images selected';
    }

    if (_hasAttemptedValidation) {
      return _validationErrorFor(requirement);
    }

    return null;
  }

  bool _isErrorMessage(
    InstallationImageItem requirement,
    String? message,
  ) {
    if (message == null || message.isEmpty) return false;
    if (_sectionMessages.containsKey(requirement.code)) return true;
    return !_isRequirementComplete(requirement);
  }

  void _handleImagesSelected(
    InstallationImageItem requirement,
    List<File> files,
  ) {
    final previousFiles =
        _selectedImagesByRequirement[requirement.code] ?? const <File>[];

    if (files.length > requirement.requiredCount) {
      setState(() {
        _sectionMessages[requirement.code] =
            'Maximum of ${requirement.requiredCount} '
            '${requirement.requiredCount == 1 ? 'image' : 'images'} reached';
        _selectedImagesByRequirement[requirement.code] =
            List<File>.from(previousFiles);
      });
      return;
    }

    setState(() {
      _sectionMessages.remove(requirement.code);
      _selectedImagesByRequirement[requirement.code] = List<File>.from(files);
    });
  }

  void _validateAndContinue(List<InstallationImageItem> requirements) {
    if (_isViewOnly) {
      context.router.pop();
      return;
    }

    if (_isSaving) return;
    setState(() {
      _hasAttemptedValidation = true;
    });

    if (_hasAnyInvalidRequirement(requirements)) {
      return;
    }

    _submitSelections();
  }

  Future<void> _submitSelections() async {
    if (_currentActivityFacilityId == null || _userType == null) return;
    final ok = await _ensureLocationLoaded();
    if (!ok) {
      if (!mounted) return;
      context.showSnackBar(
        const SnackBar(content: Text('Could not fetch location')),
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    context.read<CacheInstallationImageBloc>().add(
          CacheInstallationImageEvent.saveAll(
            activityFacilityId: _currentActivityFacilityId!,
            userType: _userType!,
            selectedImages: Map<String, List<File>>.from(
              _selectedImagesByRequirement.map(
                (key, value) => MapEntry(key, List<File>.from(value)),
              ),
            ),
            latitude: _latitude.toString(),
            longitude: _longitude.toString(),
          ),
        );
  }

  bool _isSubmitDisabled(List<InstallationImageItem> requirements) {
    if (_isViewOnly) return false;
    if (_isSaving) return true;
    if (_currentActivityFacilityId == null || _userType == null) return true;
    if (requirements.isEmpty) return true;
    return _hasAnyInvalidRequirement(requirements);
  }

  Widget _buildBody(
    BuildContext context,
    ThemeData theme,
    dynamic textTheme,
    InstallationImagesState state,
  ) {
    return state.when(
      initial: () => const DigitCard(
        children: [
          SizedBox(height: spacer4),
          Center(child: CircularProgressIndicator()),
          SizedBox(height: spacer4),
        ],
      ),
      loading: () => const DigitCard(
        children: [
          SizedBox(height: spacer4),
          Center(child: CircularProgressIndicator()),
          SizedBox(height: spacer4),
        ],
      ),
      error: (message) => DigitCard(
        children: [
          Text(
            message,
            style: textTheme.bodyL.copyWith(
              color: theme.colorTheme.alert.error,
            ),
          ),
          const SizedBox(height: spacer3),
          DigitButton(
            label: 'Retry',
            mainAxisSize: MainAxisSize.max,
            type: DigitButtonType.primary,
            size: DigitButtonSize.large,
            onPressed: () {
              context
                  .read<InstallationImagesBloc>()
                  .add(const InstallationImagesEvent.fetch());
            },
          ),
        ],
      ),
      loaded: (items) {
        final requirements = items.where((item) => item.active).toList();

        if (requirements.isEmpty) {
          return DigitCard(
            children: [
              Text(
                'No installation image configuration found.',
                style: textTheme.bodyL.copyWith(
                  color: theme.colorTheme.alert.error,
                ),
              ),
            ],
          );
        }

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ...requirements.map((requirement) {
              final feedbackMessage = _feedbackMessageFor(requirement);
              final isError = _isErrorMessage(requirement, feedbackMessage);
              return Padding(
                padding: const EdgeInsets.only(bottom: spacer4),
                child: DigitCard(
                  children: [
                    const SizedBox(width: double.infinity),
                    Text(
                      '${requirement.code}. ${requirement.description}',
                      style: textTheme.bodyL.copyWith(
                        color: theme.colorTheme.primary.primary2,
                      ),
                    ),
                    Text(
                      requirement.requiredLabel,
                      style: textTheme.bodyS.copyWith(
                        color: theme.colorTheme.text.secondary,
                      ),
                    ),
                    ImageUploader(
                      label: 'Upload Images',
                      allowMultiples: requirement.allowMultiples,
                      isDisabled: _isViewOnly,
                      errorMessage:
                          _isViewOnly ? null : _validationErrorFor(requirement),
                      initialImages:
                          _selectedImagesByRequirement[requirement.code],
                      onImagesSelected: (files) =>
                          _handleImagesSelected(requirement, files),
                    ),
                    if (feedbackMessage != null) ...[
                      Text(
                        feedbackMessage,
                        style: textTheme.bodyS.copyWith(
                          color: isError
                              ? theme.colorTheme.alert.error
                              : theme.colorTheme.alert.success,
                        ),
                      ),
                    ],
                  ],
                ),
              );
            }),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);
    return BlocConsumer<CacheInstallationImageBloc,
        CacheInstallationImageState>(
      listener: (context, cacheState) async {
        await cacheState.whenOrNull(
          loaded: (entries) => _populateFromCache(entries),
          saved: () async {
            if (!mounted) return;
            setState(() {
              _isSaving = false;
            });
            _popUntilThenRefreshOrigin(context, widget.origin);
          },
          notFound: () async {
            if (!mounted) return;
            setState(() {
              _isSaving = false;
            });
          },
          error: (message) async {
            if (!mounted) return;
            setState(() {
              _isSaving = false;
            });
            context.showSnackBar(SnackBar(content: Text(message)));
          },
        );
      },
      builder: (context, cacheState) {
        return BlocBuilder<InstallationImagesBloc, InstallationImagesState>(
          builder: (context, state) {
            final loadedRequirements = state.maybeWhen(
              loaded: (items) => items,
              orElse: () => const <InstallationImageItem>[],
            );

            return Scaffold(
              body: ScrollableContent(
                enableFixedDigitButton: true,
                backgroundColor: theme.colorTheme.generic.background,
                header: const BackNavigationHelpHeaderWidget(
                  showBackNavigation: true,
                  showHelp: false,
                ),
                footer: DigitCard(
                  margin: const EdgeInsets.only(top: spacer2),
                  children: [
                    DigitButton(
                      isDisabled: _isSubmitDisabled(loadedRequirements),
                      label: _isViewOnly
                          ? 'Back'
                          : (_isSaving ? 'Loading...' : 'Submit'),
                      onPressed: () => _validateAndContinue(loadedRequirements),
                      type: DigitButtonType.primary,
                      size: DigitButtonSize.large,
                      mainAxisSize: MainAxisSize.max,
                    ),
                  ],
                ),
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(
                      vertical: spacer2,
                      horizontal: spacer4,
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Installation Images',
                          style: textTheme.headingXl.copyWith(
                            color: theme.colorTheme.primary.primary2,
                          ),
                        ),
                        const SizedBox(height: spacer4),
                        _buildBody(context, theme, textTheme, state),
                      ],
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }
}
