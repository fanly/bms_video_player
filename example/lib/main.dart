import 'package:flutter/material.dart';

import 'package:bms_video_player/bms_video_player.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  var viewPlayerController;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    var x = 0.0;
    var y = 0.0;
    var width = 400.0;
    var height = width * 9.0 / 16.0;

    BmsVideoPlayer videoPlayer = new BmsVideoPlayer(
      onCreated: onViewPlayerCreated,
      x: x,
      y: y,
      width: width,
      height: height
    );
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          child: videoPlayer,
          width: width,
          height: height
        )
      ),
    );
  }

  void onViewPlayerCreated(viewPlayerController) {
    this.viewPlayerController = viewPlayerController;
    this.viewPlayerController.loadUrl("https://apissources.bamasoso.com/video/rvz3YaO8Gbxq/0e5fdab9c4935093877390e6db94443c.mp4");
  }
}
