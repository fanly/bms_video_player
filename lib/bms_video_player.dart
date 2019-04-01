import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

typedef void BmsVideoPlayerCreatedCallback(BmsVideoPlayerController controller);

class BmsVideoPlayerController {

  MethodChannel _channel;
  
  BmsVideoPlayerController.init(int id) {
    _channel =  new MethodChannel('bms_video_player_$id');
  }

  Future<void> loadUrl(String url) async {
    assert(url != null);
    return _channel.invokeMethod('loadUrl', url);
  }
}

class BmsVideoPlayer extends StatefulWidget {

  final BmsVideoPlayerCreatedCallback onCreated;
  final x;
  final y;
  final width;
  final height;

  BmsVideoPlayer({
    Key key,
    @required this.onCreated,
    @required this.x,
    @required this.y,
    @required this.width,
    @required this.height,
  });

  @override
  State<StatefulWidget> createState() => _VideoPlayerState();

}
  
class _VideoPlayerState extends State<BmsVideoPlayer> {

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      child: nativeView(),
      onHorizontalDragStart: (DragStartDetails details) {
        print("onHorizontalDragStart: ${details.globalPosition}");
        // if (!controller.value.initialized) {
        //   return;
        // }
        // _controllerWasPlaying = controller.value.isPlaying;
        // if (_controllerWasPlaying) {
        //   controller.pause();
        // }
      },
      onHorizontalDragUpdate: (DragUpdateDetails details) {
        print("onHorizontalDragUpdate: ${details.globalPosition}");
        print(details.globalPosition);
        // if (!controller.value.initialized) {
        //   return;
        // }
        // seekToRelativePosition(details.globalPosition);
      },
      onHorizontalDragEnd: (DragEndDetails details) {
        print("onHorizontalDragEnd");
        // if (_controllerWasPlaying) {
        //   controller.play();
        // }
      },
      onTapDown: (TapDownDetails details) {
        print("onTapDown: ${details.globalPosition}");
      },
    );
  }

  nativeView() {
    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
        viewType: 'plugins.bms_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String,dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: 'plugins.bms_video_player/view',
        onPlatformViewCreated: onPlatformViewCreated,
        creationParams: <String,dynamic>{
          "x": widget.x,
          "y": widget.y,
          "width": widget.width,
          "height": widget.height,
        },
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }

  Future<void> onPlatformViewCreated(id) async {
    if (widget.onCreated == null) {
      return;
      }
    
    widget.onCreated(new BmsVideoPlayerController.init(id));
  }
}
