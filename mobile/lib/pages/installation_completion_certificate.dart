import 'dart:async';
import 'dart:io';

import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/activity_facility/activity_facility.dart';
import '../blocs/cache_installation_completion_certificate/cache_installation_completion_certificate.dart';
import '../blocs/user_type/user_type.dart';
import '../data/nosql/cache_installation_completion_certificate.dart';
import '../repositories/activity_facility_repo.dart';
import '../repositories/installation_completion_certificate_repo.dart';
import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../utils/utils.dart';
import '../widgets/customized_digit_widget/file_uploader.dart';
import '../widgets/customized_digit_widget/image_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';
import '../widgets/summary/existing_or_loader.dart';
import '../widgets/summary/summary.dart';

@RoutePage()
class InstallationCompletionCertificatePage extends StatefulWidget {
  const InstallationCompletionCertificatePage({
    super.key,
    required this.origin,
    required this.activityFacilityId,
  });

  final FormOrigin origin;
  final String activityFacilityId;

  @override
  State<InstallationCompletionCertificatePage> createState() =>
      _InstallationCompletionCertificatePageState();
}

class _InstallationCompletionCertificatePageState
    extends State<InstallationCompletionCertificatePage> {
  static const int _maxFiles = 3;

  List<ExistingReport> _existingCertificateFiles = [];
  List<File> _selectedImages = [];
  List<PlatformFile> _selectedPdfFiles = [];
  Map<String, String> _cachedSourceByLocalPath = {};

  late final String _currentActivityFacilityId;
  String? _userType;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;
  bool _hasAttemptedValidation = false;
  bool _isInitialCertificateLoading = false;
  bool _isSaving = false;
  String? _errorMessage;

  bool get _isViewOnly =>
      widget.origin == FormOrigin.inboxSummary ||
      widget.origin == FormOrigin.submitted;

  int get _selectedCount => _selectedImages.length + _selectedPdfFiles.length;

  int get _totalSelectedCount =>
      _existingCertificateFiles.length + _selectedCount;

  bool get _hasValidPdfFiles => _selectedPdfFiles.every(
        (file) {
          final path = file.path ?? '';
          if (path.isEmpty) return false;
          return _isPdfPlatformFile(file);
        },
      );

  bool get _hasValidSelection =>
      _totalSelectedCount > 0 &&
      _totalSelectedCount <= _maxFiles &&
      _hasValidPdfFiles;

  int get _remainingImageSlots {
    final remaining =
        _maxFiles - _existingCertificateFiles.length - _selectedPdfFiles.length;
    return remaining < 0 ? 0 : remaining;
  }

  int get _remainingPdfSlots {
    final remaining =
        _maxFiles - _existingCertificateFiles.length - _selectedImages.length;
    return remaining < 0 ? 0 : remaining;
  }

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
      _loadCachedFiles();
    });
  }

  @override
  void dispose() {
    _locSub?.cancel();
    super.dispose();
  }

  void _loadCachedFiles() {
    setState(() {
      _isInitialCertificateLoading = true;
    });
    context.read<CacheInstallationCompletionCertificateBloc>().add(
          CacheInstallationCompletionCertificateEvent.load(
            _currentActivityFacilityId,
          ),
        );
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

  Future<void> _populateFromCache(
    List<CacheInstallationCompletionCertificate> entries,
  ) async {
    final sorted = List<CacheInstallationCompletionCertificate>.from(entries)
      ..sort((a, b) => (a.index ?? 0).compareTo(b.index ?? 0));

    final existingFiles = <ExistingReport>[];
    final cachedSources = <String, String>{};

    for (final entry in sorted) {
      var fileType = normalizeCertificateFileType(entry.fileType);
      if (fileType == 'unknown') {
        fileType = normalizeCertificateFileType(
          extensionOfFileName(entry.fileName ?? entry.filePath),
        );
      }
      final file = await getCachedFile(entry.filePath);
      if (file == null) continue;

      cachedSources[file.path] = entry.filePath;
      existingFiles.add(
        ExistingReport(
          isarId: entry.id,
          source: file.path,
          fileName: _displayNameFor(entry.fileName, file.path, fileType),
          fileType:
              fileType == 'pdf' || fileType == 'image' ? fileType : 'unknown',
        ),
      );
    }

    if (!mounted) return;
    setState(() {
      _existingCertificateFiles = existingFiles;
      _selectedImages = [];
      _selectedPdfFiles = [];
      _cachedSourceByLocalPath = cachedSources;
      _hasAttemptedValidation = false;
      _isInitialCertificateLoading = false;
      _errorMessage = null;
    });
  }

  void _clearHydratedFiles() {
    if (!mounted) return;
    setState(() {
      _existingCertificateFiles = [];
      _selectedImages = [];
      _selectedPdfFiles = [];
      _cachedSourceByLocalPath = {};
      _hasAttemptedValidation = false;
      _isInitialCertificateLoading = false;
      _errorMessage = null;
    });
  }

  String _basename(String pathOrId) {
    final norm = pathOrId.replaceAll('\\', '/');
    final idx = norm.lastIndexOf('/');
    return idx == -1 ? norm : norm.substring(idx + 1);
  }

  String _displayNameFor(String? fileName, String path, String fileType) {
    final trimmed = (fileName ?? '').trim();
    if (trimmed.isNotEmpty) return trimmed;
    final base = _basename(path);
    if (fileType == 'pdf' && extensionOfFileName(base) != 'pdf') {
      return '$base.pdf';
    }
    return base;
  }

  bool _isPdfPlatformFile(PlatformFile file) {
    final nameExt = extensionOfFileName(file.name);
    if (nameExt == 'pdf') return true;

    final path = file.path;
    if (path == null || path.isEmpty) return false;
    return extensionOfFileName(path) == 'pdf';
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

  String? _validationMessage() {
    if (_errorMessage != null) return _errorMessage;
    if (!_hasAttemptedValidation) return null;
    if (_totalSelectedCount == 0) {
      return context
          .translate(i18.installationCompletionCertificate.uploadRequired);
    }
    if (_totalSelectedCount > _maxFiles) {
      return context
          .translate(i18.installationCompletionCertificate.maxFilesAllowed);
    }
    return null;
  }

  Map<PlatformFile, String?> _handlePdfFilesSelected(
    List<PlatformFile> files,
  ) {
    final errors = <PlatformFile, String?>{};
    final total = _existingCertificateFiles.length +
        _selectedImages.length +
        files.length;

    for (var i = 0; i < files.length; i++) {
      final file = files[i];
      if ((file.path ?? '').isEmpty) {
        errors[file] = context.translate(
          i18.installationCompletionCertificate.filePathUnavailable,
        );
      } else if (!_isPdfPlatformFile(file)) {
        errors[file] = context.translate(
          i18.installationCompletionCertificate.onlyPdfAllowed,
        );
      } else if (_existingCertificateFiles.length +
              _selectedImages.length +
              i +
              1 >
          _maxFiles) {
        errors[file] = context.translate(
          i18.installationCompletionCertificate.maxFilesAllowed,
        );
      }
    }

    setState(() {
      _selectedPdfFiles = List<PlatformFile>.from(files);
      _errorMessage = total > _maxFiles
          ? context.translate(
              i18.installationCompletionCertificate.maxFilesAllowed,
            )
          : null;
      _hasAttemptedValidation = false;
    });

    return errors;
  }

  void _handleImagesSelected(List<File> files) {
    setState(() {
      _selectedImages = List<File>.from(files);
      _errorMessage = _totalSelectedCount > _maxFiles
          ? context.translate(
              i18.installationCompletionCertificate.maxFilesAllowed,
            )
          : null;
      _hasAttemptedValidation = false;
    });
  }

  Future<void> _validateAndContinue() async {
    if (_isViewOnly) {
      context.router.maybePop();
      return;
    }

    if (_isSaving) return;
    setState(() {
      _hasAttemptedValidation = true;
      _errorMessage = _totalSelectedCount > _maxFiles
          ? context.translate(
              i18.installationCompletionCertificate.maxFilesAllowed,
            )
          : null;
    });

    if (!_hasValidSelection) return;
    await _submitSelections();
  }

  Future<void> _submitSelections() async {
    if (_userType == null) return;
    final ok = await _ensureLocationLoaded();
    if (!mounted) return;
    if (!ok) {
      context.showSnackBar(
        SnackBar(
          content: Text(context.translate(i18.common.couldNotFetchLocation)),
        ),
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    final inputs = <InstallationCompletionCertificateInput>[];
    var index = 0;

    for (final existing in _existingCertificateFiles) {
      final original = _cachedSourceByLocalPath[existing.source];
      inputs.add(
        InstallationCompletionCertificateInput(
          activityFacilityId: _currentActivityFacilityId,
          userType: _userType!,
          filePath: original ?? existing.source,
          fileName: existing.fileName.isEmpty
              ? _basename(existing.source)
              : existing.fileName,
          fileType: existing.fileType,
          latitude: _latitude.toString(),
          longitude: _longitude.toString(),
          index: index++,
        ),
      );
    }

    for (final image in _selectedImages) {
      final original = _cachedSourceByLocalPath[image.path];
      final path = original ??
          (isValidUuid(image.path)
              ? image.path
              : await copyFileToLocalDir(image));
      inputs.add(
        InstallationCompletionCertificateInput(
          activityFacilityId: _currentActivityFacilityId,
          userType: _userType!,
          filePath: path,
          fileName: _basename(image.path),
          fileType: 'image',
          latitude: _latitude.toString(),
          longitude: _longitude.toString(),
          index: index++,
        ),
      );
    }

    for (final pdf in _selectedPdfFiles) {
      final pdfPath = pdf.path;
      if (pdfPath == null || pdfPath.isEmpty) continue;
      final original = _cachedSourceByLocalPath[pdfPath];
      final path = original ??
          (isValidUuid(pdfPath)
              ? pdfPath
              : await copyFileToLocalDir(File(pdfPath)));
      inputs.add(
        InstallationCompletionCertificateInput(
          activityFacilityId: _currentActivityFacilityId,
          userType: _userType!,
          filePath: path,
          fileName: pdf.name,
          fileType: 'pdf',
          latitude: _latitude.toString(),
          longitude: _longitude.toString(),
          index: index++,
        ),
      );
    }

    if (!mounted) return;
    context.read<CacheInstallationCompletionCertificateBloc>().add(
          CacheInstallationCompletionCertificateEvent.replaceAllForProject(
            activityFacilityId: _currentActivityFacilityId,
            files: inputs,
          ),
        );
  }

  bool _isSubmitDisabled() {
    if (_isViewOnly) return false;
    if (_isSaving) return true;
    if (_userType == null) return true;
    return !_hasValidSelection;
  }

  void _removeExistingCertificateFile(ExistingReport report) {
    setState(() {
      _existingCertificateFiles = _existingCertificateFiles
          .where((entry) => entry.source != report.source)
          .toList();
      _errorMessage = _totalSelectedCount > _maxFiles
          ? context.translate(
              i18.installationCompletionCertificate.maxFilesAllowed,
            )
          : null;
      _hasAttemptedValidation = false;
    });
  }

  Widget _buildExistingFilesSection() {
    return ExistingFilesOrLoader(
      existingReports: _existingCertificateFiles,
      workflowDocuments: const [],
      isLoading: _isInitialCertificateLoading,
      readOnly: _isViewOnly,
      onRemove: _removeExistingCertificateFile,
    );
  }

  Widget _buildUploadControls(ThemeData theme, dynamic textTheme) {
    final message = _validationMessage();

    return DigitCard(
      children: [
        const SizedBox(width: double.infinity),
        if (!_isViewOnly)
          Text(
            context
                .translate(i18.installationCompletionCertificate.uploadPrompt),
            style: textTheme.bodyL.copyWith(
              color: theme.colorTheme.primary.primary2,
            ),
          ),
        if (!_isViewOnly) ...[
          ImageUploader(
            label: context.translate(i18.common.uploadImages),
            allowMultiples: true,
            maxImages: _remainingImageSlots,
            isDisabled: _remainingImageSlots == 0,
            initialImages: _selectedImages,
            onImagesSelected: _handleImagesSelected,
          ),
          const SizedBox(height: spacer1),
          if (_remainingPdfSlots > 0 || _selectedPdfFiles.isNotEmpty)
            FileUploadWidget(
              label: context
                  .translate(i18.installationCompletionCertificate.uploadPdf),
              allowedExtensions: const ['pdf'],
              allowMultiples: true,
              showPreview: true,
              initialFiles: _selectedPdfFiles,
              errorMessage: message,
              onFilesSelected: _handlePdfFilesSelected,
              onFileTap: (file) {
                final path = file.path;
                if (path == null || path.isEmpty) return;
                context.router.push(PdfViewerRoute(path: path));
              },
            ),
        ],
        _buildExistingFilesSection(),
        if (message != null) ...[
          const SizedBox(height: spacer2),
          Text(
            message,
            style: textTheme.bodyS.copyWith(
              color: theme.colorTheme.alert.error,
            ),
          ),
        ],
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return BlocConsumer<CacheInstallationCompletionCertificateBloc,
        CacheInstallationCompletionCertificateState>(
      listener: (context, state) async {
        await state.whenOrNull(
          loaded: (files) async => _populateFromCache(files),
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
              _isInitialCertificateLoading = false;
            });
            _clearHydratedFiles();
          },
          error: (message) async {
            if (!mounted) return;
            setState(() {
              _isSaving = false;
              _isInitialCertificateLoading = false;
            });
            context.showSnackBar(SnackBar(content: Text(message)));
          },
        );
      },
      builder: (context, state) {
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
                  isDisabled: _isSubmitDisabled(),
                  label: _isViewOnly
                      ? context.translate(i18.installationImages.back)
                      : (_isSaving
                          ? context.translate(i18.common.loading)
                          : context.translate(i18.common.coreCommonSubmit)),
                  onPressed: _validateAndContinue,
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
                      context.translate(
                        i18.installationCompletionCertificate.title,
                      ),
                      style: textTheme.headingXl.copyWith(
                        color: theme.colorTheme.primary.primary2,
                      ),
                    ),
                    const SizedBox(height: spacer4),
                    _buildUploadControls(theme, textTheme),
                    const SizedBox(height: spacer4),
                    InfoCard(
                      title: "",
                      type: InfoType.warning,
                      capitalizedLetter: false,
                      description: context.translate(
                        i18.installationCompletionCertificate.acceptedFormats,
                      ),
                      additionalWidgets: [
                        Text(
                          context.translate(
                            i18.installationCompletionCertificate.maxFileSize,
                          ),
                          style: textTheme.bodyS
                              .copyWith(color: theme.colorTheme.text.secondary),
                        ),
                        const SizedBox(height: spacer1),
                        Text(
                            context.translate(i18
                                .installationCompletionCertificate
                                .maxFilesAllowed),
                            style: textTheme.bodyS.copyWith(
                                color: theme.colorTheme.text.secondary))
                      ],
                    )
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
