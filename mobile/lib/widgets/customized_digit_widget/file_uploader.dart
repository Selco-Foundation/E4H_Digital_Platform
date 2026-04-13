import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/utils/fileService/file_service.dart';
import 'package:digit_ui_components/utils/utils.dart';
import 'package:digit_ui_components/utils/validators/file_validator.dart';
import 'package:digit_ui_components/widgets/atoms/digit_chip.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';

typedef OnFilesSelectedCallback = Map<PlatformFile, String?> Function(
    List<PlatformFile> files);

class FileUploadWidget extends StatefulWidget {
  final OnFilesSelectedCallback onFilesSelected;
  final String label;
  final bool showPreview;
  final bool openFile;
  final bool allowMultiples;
  final String? errorMessage;
  final List<FileValidator>? validators;
  final List<String>? allowedExtensions;
  final String noFileSelectedText;
  final String uploadText;
  final List<PlatformFile>? initialFiles;
  final String reUploadText;
  final Function(PlatformFile)? onFileTap;

  const FileUploadWidget({
    super.key,
    required this.onFilesSelected,
    required this.label,
    this.showPreview = false,
    this.allowMultiples = false,
    this.openFile = false,
    this.errorMessage,
    this.validators,
    this.initialFiles,
    this.allowedExtensions,
    this.noFileSelectedText = 'No File Selected',
    this.uploadText = 'Upload',
    this.reUploadText = 'Re-Upload',
    this.onFileTap,
  });

  @override
  _FileUploadWidgetState createState() => _FileUploadWidgetState();
}

class _FileUploadWidgetState extends State<FileUploadWidget>
    with SingleTickerProviderStateMixin {
  List<PlatformFile> files = [];
  List<String> fileNames = [];
  List<Uint8List> fileBytesList = [];
  List<bool> uploadingStatus = [];
  String fileErrorMessage = '';
  Map<PlatformFile, String?> fileErrors = {};
  late DigitTypography currentTypography;

  @override
  void initState() {
    super.initState();
    _initializeFiles();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }

  @override
  void didUpdateWidget(FileUploadWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Check if the initialFiles have changed
    final incoming = widget.initialFiles ?? const <PlatformFile>[];
    final previous = oldWidget.initialFiles ?? const <PlatformFile>[];
    if (!listEquals(incoming, previous)) {
      _initializeFiles();
    }
  }

  // Initialize files with the ones provided by initialFiles
  void _initializeFiles() {
    files.clear();
    fileNames.clear();
    fileBytesList.clear();

    if (widget.initialFiles != null && widget.initialFiles!.isNotEmpty) {
      for (var platformFile in widget.initialFiles!) {
        files.add(platformFile);
        fileNames.add(platformFile.name);

        // If file bytes are provided, use them; otherwise, add 0 bytes
        if (platformFile.bytes != null) {
          fileBytesList.add(platformFile.bytes!);
        } else {
          // Provide 0 bytes if the actual file bytes are missing
          fileBytesList.add(Uint8List(0));
        }
      }
    }
    if (files.isNotEmpty) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) return;
        setState(() {
          fileErrors = widget.onFilesSelected(List<PlatformFile>.from(files));
        });
      });
    }
  }

  void _openFileExplorer() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles(
      allowMultiple: widget.allowMultiples,
      type: FileType.custom,
      allowedExtensions: widget.allowedExtensions ??
          ['xlsx', 'xls', 'pdf', 'jpg', 'jpeg', 'png'],
    );

    if (!mounted) return;

    if (result != null) {
      if (widget.validators != null) {
        for (var platformFile in result.files) {
          String? validationError =
              validateFile(platformFile, widget.validators!);
          if (validationError != null) {
            setState(() {
              fileErrorMessage = validationError;
            });
            return;
          }
        }
      }

      if (kIsWeb) {
        /// Handle file picking for web where paths are unavailable

        for (var file in result.files) {
          if (widget.allowMultiples) {
            setState(() {
              fileBytesList.add(file.bytes ?? Uint8List(0));
              fileNames.add(file.name);
              files.add(file);
            });
          } else {
            fileBytesList.clear();
            fileNames.clear();
            files.clear();
            setState(() {
              files.add(file);
              fileBytesList.add(file.bytes ?? Uint8List(0));
              fileNames.add(file.name);
            });
          }
        }
      } else {
        //
        /// Handle file picking for mobile/desktop where paths are available
        for (var file in result.files) {
          if (widget.allowMultiples) {
            setState(() {
              fileBytesList.add(file.bytes ?? Uint8List(0));
              fileNames.add(file.name);
              files.add(file);
            });
          } else {
            fileBytesList.clear();
            files.clear();
            fileNames.clear();
            setState(() {
              files.add(file);
              fileBytesList.add(file.bytes ?? Uint8List(0));
              fileNames.add(file.name);
            });
          }
        }
      }
    }
    if (files.isNotEmpty) {
      /// Call onFilesSelected with the selected files
      if (!mounted) return;
      setState(() {
        fileErrors = widget.onFilesSelected(List<PlatformFile>.from(files));
      });
    }
  }

  void _openFile(Uint8List fileBytes, String fileName) async {
    final fileService = FileService();
    final result = await fileService.writeToTemporaryFile(fileBytes, fileName);
  }

  Widget _buildFilePreview(int index, double width) {
    if (fileBytesList.length > index) {
      if (fileNames[index].toLowerCase().endsWith('.jpg') ||
          fileNames[index].toLowerCase().endsWith('.png')) {
        return _buildImageWidget(index, width);
      } else {
        return _buildFileWidget(index, width);
      }
    } else {
      return const SizedBox.shrink();
    }
  }

  Widget _buildImageWidget(int index, double width) {
    bool showOverlay = false;

    return StatefulBuilder(builder: (context, setState) {
      bool hasValidBytes = fileBytesList[index].isNotEmpty;

      return SizedBox(
        width: widget.allowMultiples ? Base.imageSize : width,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              children: [
                InkWell(
                  onTap: widget.onFileTap != null
                      ? () => widget.onFileTap!(files[index])
                      : widget.openFile
                          ? () {
                              _openFile(fileBytesList[index], fileNames[index]);
                            }
                          : null,
                  onHover: widget.openFile
                      ? (hovering) {
                          setState(() {
                            showOverlay = hovering;
                          });
                        }
                      : null,
                  child: Container(
                    width: widget.allowMultiples ? Base.imageSize : width,
                    height: widget.allowMultiples ? Base.imageSize : 150,
                    decoration: BoxDecoration(
                        border: Border.all(
                      color: fileErrors.containsKey(files[index])
                          ? const DigitColors().light.alertError
                          : const DigitColors().light.genericDivider,
                      width: 1,
                    )),
                    child: ClipRRect(
                      borderRadius: Base.radius,
                      child: Stack(
                        fit: StackFit.expand,
                        children: [
                          hasValidBytes
                              ? Image.memory(
                                  fileBytesList[index],
                                  fit: BoxFit.cover,
                                  errorBuilder: (context, error, stackTrace) {
                                    // Fallback in case of error
                                    return _buildPlaceholderImage();
                                  },
                                )
                              : _buildPlaceholderImage(),
                          if (!showOverlay)
                            Container(
                              color: fileErrors.containsKey(files[index])
                                  ? const DigitColors()
                                      .light
                                      .alertError
                                      .withOpacity(.20)
                                  : const DigitColors()
                                      .background
                                      .withOpacity(.70), // Light overlay color
                            ),
                        ],
                      ),
                    ),
                  ),
                ),
                Positioned(
                  top: 0,
                  right: 0,
                  child: InkWell(
                    hoverColor: const DigitColors().transparent,
                    highlightColor: const DigitColors().transparent,
                    splashColor: const DigitColors().transparent,
                    onTap: () {
                      _removeFile(index);
                    },
                    onHover: widget.openFile
                        ? (hovering) {
                            setState(() {
                              showOverlay = hovering;
                            });
                          }
                        : null,
                    child: Container(
                      width: spacer6,
                      height: spacer6,
                      decoration: BoxDecoration(
                        color: fileErrors.containsKey(files[index])
                            ? const DigitColors().light.alertError
                            : const DigitColors().light.primary2,
                      ),
                      child: Icon(
                        Icons.close,
                        size: spacer4,
                        color: const DigitColors().light.paperPrimary,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(
              height: spacer1,
            ),
            fileErrors.containsKey(files[index])
                ? Row(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Column(
                        children: [
                          const SizedBox(
                            height: 1.5,
                          ),
                          Icon(
                            Icons.info,
                            color: const DigitColors().light.alertError,
                            size: spacer4,
                          ),
                        ],
                      ),
                      const SizedBox(width: spacer1),
                      Flexible(
                          fit: FlexFit.tight,
                          child: Text(
                            fileErrors[files[index]] ?? '',
                            style: currentTypography.bodyXS.copyWith(
                                color: const DigitColors().light.alertError),
                          )),
                    ],
                  )
                : Text(
                    fileNames[index],
                    style: currentTypography.bodyXS
                        .copyWith(color: const DigitColors().light.textPrimary),
                  ),
          ],
        ),
      );
    });
  }

  Widget _buildPlaceholderImage() {
    return Container(
      color: const DigitColors().background.withOpacity(0.5),
      child: Center(
        child: Icon(
          Icons.image_not_supported,
          size: 40,
          color: const DigitColors().light.genericDivider,
        ),
      ),
    );
  }

  Widget _buildFileWidget(int index, double width) {
    SvgPicture viewIcon;
    String fileType = fileNames[index].split('.').last.toLowerCase();
    bool showOverlay = false;

    switch (fileType) {
      case 'pdf':
        viewIcon = SvgPicture.asset(
          Base.pdfSvg,
        );
        break;
      case 'doc':
      case 'docx':
        viewIcon = SvgPicture.asset(
          Base.docSvg,
        );
        break;
      case 'xlsx':
      case 'xls':
        viewIcon = SvgPicture.asset(
          Base.xlsxSvg,
        );
        break;
      default:
        viewIcon = SvgPicture.asset(
          Base.fileSvg,
        );
        break;
    }

    return StatefulBuilder(builder: (context, setState) {
      return SizedBox(
        width: widget.allowMultiples ? Base.imageSize : width,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              children: [
                InkWell(
                  onTap: widget.onFileTap != null
                      ? () => widget.onFileTap!(files[index])
                      : widget.openFile
                          ? () {
                              _openFile(fileBytesList[index], fileNames[index]);
                            }
                          : null,
                  onHover: widget.openFile
                      ? (hovering) {
                          setState(() {
                            showOverlay = hovering;
                          });
                        }
                      : null,
                  child: Container(
                    width: widget.allowMultiples ? Base.imageSize : width,
                    height: widget.allowMultiples ? Base.imageSize : 150,
                    decoration: BoxDecoration(
                        border: Border.all(
                      color: fileErrors.containsKey(files[index])
                          ? const DigitColors().light.alertError
                          : const DigitColors().light.genericDivider,
                      width: 1,
                    )),
                    child: ClipRRect(
                      borderRadius: Base.radius,
                      child: Stack(
                        fit: StackFit.expand,
                        children: [
                          SizedBox(
                              width: Base.imageSize,
                              height: Base.imageSize,
                              child: Container(
                                  padding: const EdgeInsets.all(spacer6),
                                  child: viewIcon)),
                          if (!showOverlay)
                            Container(
                              color: fileErrors.containsKey(files[index])
                                  ? const DigitColors()
                                      .light
                                      .alertError
                                      .withOpacity(.20)
                                  : const DigitColors()
                                      .background
                                      .withOpacity(.70), // Light overlay color
                            ),
                        ],
                      ),
                    ),
                  ),
                ),
                Positioned(
                  top: 0,
                  right: 0,
                  child: InkWell(
                    hoverColor: const DigitColors().transparent,
                    highlightColor: const DigitColors().transparent,
                    splashColor: const DigitColors().transparent,
                    onTap: () {
                      _removeFile(index);
                    },
                    onHover: widget.openFile
                        ? (hovering) {
                            setState(() {
                              showOverlay = hovering;
                            });
                          }
                        : null,
                    child: Container(
                      width: spacer6,
                      height: spacer6,
                      decoration: BoxDecoration(
                        color: fileErrors.containsKey(files[index])
                            ? const DigitColors().light.alertError
                            : const DigitColors().light.primary2,
                      ),
                      child: Icon(
                        Icons.close,
                        size: spacer4,
                        color: const DigitColors().light.paperPrimary,
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(
              height: spacer1,
            ),
            fileErrors.containsKey(files[index])
                ? Row(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Column(
                        children: [
                          const SizedBox(
                            height: 1.5,
                          ),
                          Icon(
                            Icons.info,
                            color: const DigitColors().light.alertError,
                            size: spacer4,
                          ),
                        ],
                      ),
                      const SizedBox(width: spacer1),
                      Flexible(
                          fit: FlexFit.tight,
                          child: Text(
                            fileErrors[files[index]] ?? '',
                            style: currentTypography.bodyXS.copyWith(
                                color: const DigitColors().light.alertError),
                          )),
                    ],
                  )
                : Text(
                    fileNames[index],
                    style: currentTypography.bodyXS
                        .copyWith(color: const DigitColors().light.textPrimary),
                  ),
          ],
        ),
      );
    });
  }

  void _removeFile(int index) {
    setState(() {
      fileBytesList.removeAt(index);
      fileNames.removeAt(index);
      files.removeAt(index);
    });
    widget.onFilesSelected(List<PlatformFile>.from(files));
  }

  @override
  Widget build(BuildContext context) {
    currentTypography = getTypography(context, false);
    String? capitalizedErrorMessage =
        convertInToSentenceCase(widget.errorMessage);

    double minWidth = AppView.isMobileView(MediaQuery.of(context).size)
        ? MediaQuery.of(context).size.width
        : AppView.isTabletView(MediaQuery.of(context).size)
            ? BaseConstants.tabInputMaxWidth
            : BaseConstants.desktopInputMaxWidth;

    double minInputWidth = AppView.isMobileView(MediaQuery.of(context).size)
        ? 198
        : AppView.isTabletView(MediaQuery.of(context).size)
            ? 304
            : 468;

    return Container(
      constraints: BoxConstraints(
        maxWidth: minWidth,
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Expanded(
                child: Container(
                  height: spacer10,
                  constraints: BoxConstraints(
                    maxWidth: minInputWidth,
                  ),
                  decoration: BoxDecoration(
                    border: Border.all(
                      color:
                          widget.errorMessage != null || fileErrorMessage != ''
                              ? const DigitColors().light.alertError
                              : const DigitColors().light.textPrimary,
                      width:
                          widget.errorMessage != null || fileErrorMessage != ''
                              ? Base.errorBorderWidth
                              : Base.defaultBorderWidth,
                    ),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.only(top: spacer2, left: spacer2),
                    child: fileBytesList.isEmpty
                        ? Text(
                            widget.noFileSelectedText,
                            style: currentTypography.bodyS.copyWith(
                              color: const DigitColors().light.textDisabled,
                            ),
                          )
                        : Text(
                            '${fileBytesList.length} Selected',
                            style: currentTypography.bodyS.copyWith(
                              color: const DigitColors().light.textPrimary,
                            ),
                          ),
                  ),
                ),
              ),
              const SizedBox(
                width: spacer3,
              ),
              DigitButton(
                //contentPadding: const EdgeInsets.symmetric(horizontal: spacer5),
                label: !widget.allowMultiples && fileBytesList.isNotEmpty
                    ? widget.reUploadText
                    : widget.uploadText,
                onPressed: () {
                  fileErrorMessage = '';
                  _openFileExplorer();
                },
                type: DigitButtonType.secondary,
                prefixIcon: Icons.file_upload,
                size: DigitButtonSize.large,
              ),
            ],
          ),
          if (widget.errorMessage != null || fileErrorMessage != '')
            const SizedBox(
              height: spacer1,
            ),
          if (widget.errorMessage != null || fileErrorMessage != '')
            Row(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Column(
                  children: [
                    const SizedBox(
                      height: spacer1 / 2,
                    ),
                    Icon(
                      Icons.info,
                      color: const DigitColors().light.alertError,
                      size: BaseConstants.errorIconSize,
                    ),
                  ],
                ),
                const SizedBox(width: spacer1),
                Flexible(
                  fit: FlexFit.tight,
                  child: fileErrorMessage != ''
                      ? Text(
                          fileErrorMessage.length > 256
                              ? '${fileErrorMessage.substring(0, 256)}...'
                              : fileErrorMessage,
                          style: currentTypography.bodyS.copyWith(
                            color: const DigitColors().light.alertError,
                          ),
                        )
                      : Text(
                          capitalizedErrorMessage!.length > 256
                              ? '${capitalizedErrorMessage.substring(0, 256)}...'
                              : capitalizedErrorMessage,
                          style: currentTypography.bodyS.copyWith(
                            color: const DigitColors().light.alertError,
                          ),
                        ),
                ),
              ],
            ),
          const SizedBox(height: spacer3),
          widget.showPreview
              ? Wrap(
                  spacing: spacer3,
                  children: List.generate(fileNames.length, (index) {
                    return Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: _buildFilePreview(index, minWidth),
                    );
                  }),
                )
              : Wrap(
                  spacing: spacer3,
                  children: List.generate(fileNames.length, (index) {
                    return Padding(
                      padding: const EdgeInsets.only(bottom: spacer3),
                      child: fileErrors.containsKey(files[index])
                          ? DigitChip(
                              onClick: widget.onFileTap != null
                                  ? () => widget.onFileTap!(files[index])
                                  : widget.openFile
                                      ? () {
                                          _openFile(fileBytesList[index],
                                              fileNames[index]);
                                        }
                                      : null,
                              errorMessage: fileErrors[files[index]],
                              label: fileNames[index],
                              onItemDelete: () {
                                setState(() {
                                  fileNames.removeAt(index);
                                  fileBytesList.removeAt(index);
                                  files.removeAt(index);
                                });
                                widget.onFilesSelected(files);
                              },
                            )
                          : DigitChip(
                              label: fileNames[index],
                              onClick: widget.onFileTap != null
                                  ? () => widget.onFileTap!(files[index])
                                  : widget.openFile
                                      ? () {
                                          _openFile(fileBytesList[index],
                                              fileNames[index]);
                                        }
                                      : null,
                              onItemDelete: () {
                                setState(() {
                                  fileNames.removeAt(index);
                                  fileBytesList.removeAt(index);
                                  files.removeAt(index);
                                });
                                widget.onFilesSelected(files);
                              },
                            ),
                    );
                  }),
                ),
        ],
      ),
    );
  }
}
