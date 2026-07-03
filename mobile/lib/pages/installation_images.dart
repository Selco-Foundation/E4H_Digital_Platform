import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/cache_installation_image/cache_installation_image.dart';
import '../blocs/installation_images/installation_images.dart';
import '../blocs/selected_activity_facility/selected_activity_facility.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_installation_image.dart';
import '../model/comment/comment.dart';
import '../model/installation_images/installation_images.dart';
import '../repositories/activity_facility_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/customized_digit_widget/image_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class InstallationImagesPage extends StatefulWidget {
  const InstallationImagesPage({
    super.key,
    required this.origin,
    required this.activityFacilityId,
  });

  final FormOrigin origin;
  final String activityFacilityId;

  @override
  State<InstallationImagesPage> createState() => _InstallationImagesPageState();
}

class _InstallationImagesPageState extends State<InstallationImagesPage> {
  final Map<String, List<File>> _selectedImagesByRequirement = {};
  final Map<String, String> _sectionMessages = {};
  late final String _currentActivityFacilityId;
  String? _userType;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  bool _hasAttemptedValidation = false;
  bool _isSaving = false;

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

    _currentActivityFacilityId = widget.activityFacilityId;

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
    if (_userType == null) return;
    context.read<CacheInstallationImageBloc>().add(
          CacheInstallationImageEvent.get(
            _currentActivityFacilityId,
            _userType!,
          ),
        );
  }

  Future<void> _populateFromCache(List<CacheInstallationImage> entries) async {
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
      _sectionMessages.clear();
      _hasAttemptedValidation = false;
    });
  }

  void _clearHydratedImages() {
    if (!mounted) return;
    setState(() {
      _selectedImagesByRequirement.clear();
      _sectionMessages.clear();
      _hasAttemptedValidation = false;
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
    if (_userType == null) return;
    final ok = await _ensureLocationLoaded();
    if (!mounted) return;
    if (!ok) {
      context.showSnackBar(
        SnackBar(
            content: Text(context.translate(i18.common.couldNotFetchLocation))),
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    context.read<CacheInstallationImageBloc>().add(
          CacheInstallationImageEvent.saveAll(
            activityFacilityId: _currentActivityFacilityId,
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
    if (_userType == null) return true;
    if (requirements.isEmpty) return true;
    return _hasAnyInvalidRequirement(requirements);
  }

  Map<String, List<Comment>> _installationImageCommentsByCode(
    BuildContext context,
  ) {
    final workflow = context
        .watch<SelectedActivityFacilityBloc>()
        .state
        .whenOrNull(selected: (wf) => wf);
    final commentsByCode = <String, List<Comment>>{};
    final latestTransaction = latestTransactionWithComments(workflow);

    for (final comment in latestTransaction?.comments ?? const <Comment>[]) {
      final assetType = comment.assetType?.trim().toUpperCase();
      if (assetType == null || !assetType.startsWith('INSTALLATION_IMAGE_')) {
        continue;
      }

      final code = assetType.substring('INSTALLATION_IMAGE_'.length);
      if (code.isEmpty) continue;
      commentsByCode.putIfAbsent(code, () => <Comment>[]).add(comment);
    }

    return commentsByCode;
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
            label: context.translate(i18.common.retry),
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
                context.translate(i18.installationImages.noConfigurationFound),
                style: textTheme.bodyL.copyWith(
                  color: theme.colorTheme.alert.error,
                ),
              ),
            ],
          );
        }

        final commentsByCode = _installationImageCommentsByCode(context);
        final showRejectionCards =
            context.watch<SelectedActivityFacilityBloc>().state.whenOrNull(
                      selected: (workflow) =>
                          workflow.status ==
                          WORKFLOW_STATUS_FIELD_STAFF.REJECTED_BY_QC_SPOC.name,
                    ) ??
                false;

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ...requirements.map((requirement) {
              final feedbackMessage = _feedbackMessageFor(requirement);
              final isError = _isErrorMessage(requirement, feedbackMessage);
              final rejectionComments =
                  commentsByCode[requirement.code.trim().toUpperCase()];
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
                      label: context.translate(i18.common.uploadImages),
                      allowMultiples: requirement.allowMultiples,
                      maxImages: requirement.requiredCount,
                      isDisabled: _isViewOnly,
                      errorMessage:
                          _isViewOnly ? null : _validationErrorFor(requirement),
                      initialImages:
                          _selectedImagesByRequirement[requirement.code],
                      onImagesSelected: (files) =>
                          _handleImagesSelected(requirement, files),
                    ),
                    if (showRejectionCards &&
                        rejectionComments != null &&
                        rejectionComments.isNotEmpty)
                      _InstallationImageRejectionReasonsCard(
                        comments: rejectionComments,
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
            if (_userType != null) {
              final isar = context.read<ActivityFacilityBloc>().isar;
              await PrefilledActivityFacilityRepository(isar).addOrTouch(
                activityFacilityId: _currentActivityFacilityId,
                userType: _userType!,
              );
            }
            if (!mounted) return;
            setState(() {
              _isSaving = false;
            });
            _popUntilThenRefreshOrigin(this.context, widget.origin);
          },
          notFound: () async {
            if (!mounted) return;
            setState(() {
              _isSaving = false;
            });
            _clearHydratedImages();
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
                          ? context.translate(i18.installationImages.back)
                          : (_isSaving
                              ? context.translate(i18.common.loading)
                              : context.translate(i18.common.coreCommonSubmit)),
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
                          context.translate(i18.installationImages.title),
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

class _InstallationImageRejectionReasonsCard extends StatelessWidget {
  const _InstallationImageRejectionReasonsCard({
    required this.comments,
  });

  final List<Comment> comments;

  @override
  Widget build(BuildContext context) {
    if (comments.isEmpty) return const SizedBox.shrink();

    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Column(
      children: [
        const SizedBox(height: spacer2),
        Container(
          width: double.infinity,
          decoration: BoxDecoration(
            color: theme.colorTheme.paper.secondary,
            border: Border.all(color: theme.colorTheme.generic.divider),
            borderRadius: BorderRadius.circular(spacer1),
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(
              horizontal: spacer3,
              vertical: spacer4,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  context.translate(i18.submitForApproval.rejectionReasons),
                  style: textTheme.headingS.copyWith(
                    color: theme.colorTheme.text.primary,
                  ),
                ),
                const SizedBox(height: spacer5),
                for (var i = 0; i < comments.length; i++) ...[
                  _InstallationImageRejectionReason(
                    comment: comments[i],
                    index: i + 1,
                  ),
                  if (i < comments.length - 1) const SizedBox(height: spacer4),
                ],
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _InstallationImageRejectionReason extends StatelessWidget {
  const _InstallationImageRejectionReason({
    required this.comment,
    required this.index,
  });

  final Comment comment;
  final int index;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final labelStyle = theme.digitTextTheme(context).label.copyWith(
          color: theme.colorTheme.primary.primary2,
        );
    final valueStyle = theme.digitTextTheme(context).label.copyWith(
          color: theme.colorTheme.text.primary,
        );
    final reason = comment.reason;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          decoration: BoxDecoration(
            border: Border.all(color: theme.colorTheme.primary.primary2),
            borderRadius: BorderRadius.circular(spacer2),
            color: theme.colorTheme.paper.primary,
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(
              vertical: spacer1,
              horizontal: spacer3,
            ),
            child: Text(
              reason == null || reason.isEmpty
                  ? '${context.translate(i18.submitForApproval.reason)} $index'
                  : reason,
              style: labelStyle,
            ),
          ),
        ),
        const SizedBox(height: spacer2),
        Text(comment.displayComment, style: valueStyle),
      ],
    );
  }
}
