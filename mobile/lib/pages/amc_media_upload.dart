import 'dart:async';

import 'package:digit_ui_components/services/location_bloc.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:digit_ui_components/theme/spacers.dart';
import 'package:digit_ui_components/widgets/molecules/digit_card.dart';
import 'package:digit_ui_components/widgets/scrollable_content.dart';
import 'package:file_picker/src/platform_file.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../router/app_router.dart';
import '../utils/extensions.dart';
import '../utils/i18_key_constants.dart' as i18;
import '../widgets/button/footer_button.dart';
import '../widgets/customized_digit_widget/file_uploader.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class AmcMediaUploadPage extends StatefulWidget {
  const AmcMediaUploadPage({super.key});

  @override
  State<AmcMediaUploadPage> createState() => _AmcMediaUploadPageState();
}

class _AmcMediaUploadPageState extends State<AmcMediaUploadPage> {
  List<PlatformFile> _selectedImages = [];
  bool _isImagesInitLoading = false;
  double? _latitude;
  double? _longitude;
  StreamSubscription<LocationState>? _locSub;

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

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.digitTextTheme(context);

    return Scaffold(
      body: ScrollableContent(
        enableFixedDigitButton: true,
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        footer: FooterButton(
          // isDisabled: false,
          showSuffixIcon: false,
          text: context.translate(i18.common.coreCommonNext),
          onPress: () async {
            context.router.push(const AmcOtpRoute());
          },
        ),
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(
                vertical: spacer2, horizontal: spacer4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: spacer4),
                DigitCard(children: [
                  Text(
                    'Images',
                    style: textTheme.headingXl
                        .copyWith(color: theme.colorTheme.primary.primary2),
                  ),
                  const SizedBox(height: spacer2),
                  FileUploadWidget(
                    allowedExtensions: const [
                      "jpg",
                      'jpeg',
                      "png",
                      'JPG',
                      'JPEG',
                      'PNG'
                    ],
                    label: 'Upload Images',
                    allowMultiples: true,
                    showPreview: true,
                    initialFiles: _selectedImages,
                    onFilesSelected: (files) {
                      setState(() {
                        _selectedImages = files;
                      });
                      _ensureLocationLoaded().then((ok) {
                        if (!ok) {
                          context.showSnackBar(const SnackBar(
                              content: Text('Could not fetch location')));
                        }
                      });
                      return <PlatformFile, String?>{};
                    },
                  ),
                  if (_isImagesInitLoading)
                    const Center(child: CircularProgressIndicator())
                ]),
                const SizedBox(height: spacer4),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
