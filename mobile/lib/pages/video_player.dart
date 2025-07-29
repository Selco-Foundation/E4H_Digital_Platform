// import 'dart:io';
//
// import 'package:auto_route/annotations.dart';
// import 'package:digit_ui_components/digit_components.dart';
// import 'package:digit_ui_components/theme/digit_extended_theme.dart';
// import 'package:flutter/material.dart';
// import 'package:video_player/video_player.dart';
//
// import '../utils/utils.dart';
// import '../widgets/header/back_navigation_help_header.dart';
//
// @RoutePage()
// class VideoPlayerPage extends StatefulWidget {
//   final String path;
//   const VideoPlayerPage({required this.path, super.key});
//
//   @override
//   State<VideoPlayerPage> createState() => _VideoPlayerPageState();
// }
//
// class _VideoPlayerPageState extends State<VideoPlayerPage> {
//   late VideoPlayerController _controller;
//   @override
//   void initState() {
//     super.initState();
//     if (isValidUuid(widget.path)) {
//       _controller =
//           VideoPlayerController.network('$fileStoreFileUrl${widget.path}');
//     } else {
//       _controller = VideoPlayerController.file(File(widget.path));
//     }
//     _controller
//       ..initialize().then((_) => setState(() {}))
//       ..setLooping(true)
//       ..play();
//   }
//
//   @override
//   void dispose() {
//     _controller.dispose();
//     super.dispose();
//   }
//
//   @override
//   Widget build(BuildContext context) {
//     final theme = Theme.of(context);
//     return Scaffold(
//       body: ScrollableContent(
//         backgroundColor: theme.colorTheme.generic.background,
//         header: const BackNavigationHelpHeaderWidget(
//           showBackNavigation: true,
//           showHelp: false,
//         ),
//         children: [
//           const SizedBox(height: spacer10),
//           Center(
//             child: _controller.value.isInitialized
//                 ? AspectRatio(
//                     aspectRatio: _controller.value.aspectRatio,
//                     child: VideoPlayer(_controller),
//                   )
//                 : const CircularProgressIndicator(),
//           )
//         ],
//       ),
//       floatingActionButton: FloatingActionButton(
//         onPressed: () => setState(() {
//           _controller.value.isPlaying
//               ? _controller.pause()
//               : _controller.play();
//         }),
//         child:
//             Icon(_controller.value.isPlaying ? Icons.pause : Icons.play_arrow),
//       ),
//     );
//   }
// }

import 'dart:io';

import 'package:auto_route/annotations.dart';
import 'package:digit_ui_components/digit_components.dart';
import 'package:digit_ui_components/theme/digit_extended_theme.dart';
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

import '../utils/utils.dart';
import '../widgets/header/back_navigation_help_header.dart';

@RoutePage()
class VideoPlayerPage extends StatefulWidget {
  final String path;
  const VideoPlayerPage({required this.path, super.key});

  @override
  State<VideoPlayerPage> createState() => _VideoPlayerPageState();
}

class _VideoPlayerPageState extends State<VideoPlayerPage> {
  late VideoPlayerController _controller;
  bool _isSeeking = false;
  Duration _latestValue = Duration.zero;

  @override
  void initState() {
    super.initState();
    // initialize controller from network or file
    _controller = isValidUuid(widget.path)
        ? VideoPlayerController.network('$fileStoreFileUrl${widget.path}')
        : VideoPlayerController.file(File(widget.path));

    _controller
      ..initialize().then((_) => setState(() {}))
      ..setLooping(true)
      ..play();

    // listen so we can update our slider even while dragging
    _controller.addListener(() {
      if (!_isSeeking) {
        setState(() {
          _latestValue = _controller.value.position;
        });
      }
    });
  }

  @override
  void dispose() {
    _controller.removeListener(() {});
    _controller.dispose();
    super.dispose();
  }

  String _format(Duration d) {
    final twoDigits = (int n) => n.toString().padLeft(2, '0');
    final minutes = twoDigits(d.inMinutes.remainder(60));
    final seconds = twoDigits(d.inSeconds.remainder(60));
    return '${twoDigits(d.inHours)}:$minutes:$seconds';
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      body: ScrollableContent(
        backgroundColor: theme.colorTheme.generic.background,
        header: const BackNavigationHelpHeaderWidget(
          showBackNavigation: true,
          showHelp: false,
        ),
        children: [
          const SizedBox(height: spacer10),
          Center(
            child: _controller.value.isInitialized
                ? AspectRatio(
                    aspectRatio: _controller.value.aspectRatio,
                    child: VideoPlayer(_controller),
                  )
                : const CircularProgressIndicator(),
          ),

          // --- progress indicator + slider + timestamps ---
          if (_controller.value.isInitialized) ...[
            const SizedBox(height: spacer4),

            // built‑in progress bar
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: spacer4),
              child: VideoProgressIndicator(
                _controller,
                allowScrubbing: true,
                padding: const EdgeInsets.symmetric(vertical: spacer2),
              ),
            ),

            // custom slider + time labels
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: spacer4),
              child: Row(
                children: [
                  // elapsed
                  Text(_format(_latestValue), style: theme.textTheme.bodyLarge),
                  const Spacer(),
                  // Expanded(
                  //   child: Slider(
                  //     min: 0,
                  //     max: _controller.value.duration.inMilliseconds.toDouble(),
                  //     value: _latestValue.inMilliseconds
                  //         .clamp(0, _controller.value.duration.inMilliseconds)
                  //         .toDouble(),
                  //     onChangeStart: (_) => setState(() => _isSeeking = true),
                  //     onChanged: (value) {
                  //       setState(() {
                  //         _latestValue = Duration(milliseconds: value.toInt());
                  //       });
                  //     },
                  //     onChangeEnd: (value) {
                  //       _controller
                  //           .seekTo(Duration(milliseconds: value.toInt()));
                  //       setState(() => _isSeeking = false);
                  //     },
                  //   ),
                  // ),
                  // total
                  Text(_format(_controller.value.duration),
                      style: theme.textTheme.bodyLarge),
                ],
              ),
            ),
          ],
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => setState(() {
          _controller.value.isPlaying
              ? _controller.pause()
              : _controller.play();
        }),
        child:
            Icon(_controller.value.isPlaying ? Icons.pause : Icons.play_arrow),
      ),
    );
  }
}
