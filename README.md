# bms_video_player

当团队准备着手做 APP 时，我们把目标对准了 Flutter，尤其近期 Flutter 的使用热度一直不断攀升。由于第一次使用 Flutter，就想通过自己的实践去提升自己的能力。

在做 APP 时，我们用到了视频播放器，当前使用官方提供的插件「video_player」[https://github.com/flutter/plugins/tree/master/packages/video_player](https://github.com/flutter/plugins/tree/master/packages/video_player)，可能该插件在国外没什么问题，但国内很多视频播放器做的很精良，自定义功能很齐全。

举一个例子：国内的 APP 全屏播放视频时，几乎都是横向全屏的，但官方提供的插件在 iOS 端是竖向直播的，效果很不好。

![](http://image.coding01.cn/2019/03/20/15530857812029.jpg)

因此萌生了自己想做一个视频播放插件：

> **要求**
> 
> 1. Android 和 iOS 端都是使用原生开发，体验效果好；
> 2. 尽可能使用 GitHub Star 靠前的第三方开源插件，减轻自己的开发工作量；

根据以上的「2」要求，我主要找到了 `lipangit/JiaoZiVideoPlayer` 和 `newyjp/JPVideoPlayer`

![](http://image.coding01.cn/2019/03/20/15530867971433.jpg)

![](http://image.coding01.cn/2019/03/20/15530869471641.jpg)


好了，所有铺垫都做好了，我们开始一步步实现插件开发吧～

**1. 创建插件**

```
flutter create --org com.***.test --template=plugin bms_video_player
```

**2. 创建关联类**

在 `lib/bms_video_player.dart` 文件中创建 `BmsVideoPlayerController` 类，用于和原生代码关联：

```
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
```
这里存在的 `MethodChannel` 有待于下一次好好研究研究。

**3. 创建 Callback**

```
typedef void BmsVideoPlayerCreatedCallback(BmsVideoPlayerController controller);
```

**4. 创建 Widget 布局**

创建 Widget，用于添加原生布局：

```
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
```
这里的 `AndroidView` 和 `UiKitView` 字如其意，不同的系统使用不同的 widget。

其中，`AndroidView` 和 `UiKitView` 都自带几个参数，如：

1. viewType：用于区分不同的插件名称和来源；
2. onPlatformViewCreated：用于在 widget 创建后，调用其函数 (`onPlatformViewCreated`)；
3. creationParams：用于将参数传递给原生控件。

下面开始，根据 iOS 和 Android 分别注册插件和实现功能，首先是 Android。

**5.1 注册 ViewFactory**

在 `BmsVideoPlayerPlugin` 类中注册 `ViewFactory`：`new VideoViewFactory(registrar)`，并命名为 「plugins.bms_video_player/view」：

```java
public static void registerWith(Registrar registrar) {
    registrar.platformViewRegistry()
             .registerViewFactory("plugins.bms_video_player/view", new VideoViewFactory(registrar));
  }
```

**5.2 创建 VideoViewFactory**

该 `VideoViewFactory` 类需要集成类 `PlatformViewFactory`，实现函数：`create(Context context, int viewId, Object args)`：

```java
public class VideoViewFactory extends PlatformViewFactory {
    private final Registrar registrar;

    public VideoViewFactory(Registrar registrar) {
        super(StandardMessageCodec.INSTANCE);
        this.registrar = registrar;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new VideoView(context, viewId, args, this.registrar);
    }
}
```

开始我们的正餐了，创建实现类 `VideoView`。

**5.3 VideoView**

```java
public class VideoView implements PlatformView, MethodCallHandler  {
    private final JzvdStd jzvdStd;
    private final MethodChannel methodChannel;
    private final Registrar registrar;

    VideoView(Context context, int viewId, Object args, Registrar registrar) {
        this.registrar = registrar;
        this.jzvdStd = getJzvStd(registrar, args);
        this.methodChannel = new MethodChannel(registrar.messenger(), "bms_video_player_" + viewId);
        this.methodChannel.setMethodCallHandler(this);
    }

    @Override
    public View getView() {
        return jzvdStd;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "loadUrl":
                String url = methodCall.arguments.toString();
                jzvdStd.setUp(url, "", Jzvd.SCREEN_NORMAL);
                break;
            default:
                result.notImplemented();
        }

    }

    @Override
    public void dispose() {}

    private JzvdStd getJzvStd(Registrar registrar, Object args) {
        JzvdStd view = (JzvdStd) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
        return view;
    }
}
```

直接分析代码：

1. 实现接口：PlatformView 和 MethodCallHandler，第一个接口「PlatformView」，用于 `return` 原生 View，也就是我们使用的第三方插件：JzvdStd。第二个接口「MethodCallHandler」，用于处理从 Dart 发过来的请求函数，如本文创建的函数：`loadUrl`
2. 这里 `return` 的 `JzvdStd`，使用 xml：

```xml
<?xml version="1.0" encoding="utf-8"?>
<cn.jzvd.JzvdStd
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/jz_video"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**5.4 引入第三方插件**

当然，我们需要在 `build.gradle` 最后加入插件：

```
dependencies {
    implementation 'cn.jzvd:jiaozivideoplayer:7.0_preview'
}
```

至此，我们的 Android 端就算完成了，接下来看看 iOS 端。

**6.1 注册 ViewFactory**
同样的，在类 `BmsVideoPlayerPlugin` 中注册 `VideoViewFactory`

```
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  VideoViewFactory* factory =
      [[VideoViewFactory alloc] initWithMessenger:registrar.messenger];
  [registrar registerViewFactory:factory withId:@"plugins.bms_video_player/view"];
}
```

**6.2 创建 VideoViewFactory**

```
#import "VideoViewFactory.h"
#import "BMSVideoPlayerViewController.h"

@implementation VideoViewFactory {
  NSObject<FlutterBinaryMessenger>* _messenger;
}

- (instancetype)initWithMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
  self = [super init];
  if (self) {
    _messenger = messenger;
  }
  return self;
}

- (NSObject<FlutterMessageCodec>*)createArgsCodec {
  return [FlutterStandardMessageCodec sharedInstance];
}

- (nonnull NSObject<FlutterPlatformView> *)createWithFrame:(CGRect)frame
                                            viewIdentifier:(int64_t)viewId
                                                 arguments:(id _Nullable)args {
    BMSVideoPlayerViewController* viewController =
      [[BMSVideoPlayerViewController alloc] initWithWithFrame:frame
                                       viewIdentifier:viewId
                                            arguments:args
                                      binaryMessenger:_messenger];
    return viewController;
}

@end
```

代码还是很简单，重点往下看 `BMSVideoPlayerViewController`

**6.3 BMSVideoPlayerViewController**

```
#import "BMSVideoPlayerViewController.h"
#import <JPVideoPlayer/JPVideoPlayerKit.h>

@interface BMSVideoPlayerViewController ()<JPVideoPlayerDelegate>

@end

@implementation BMSVideoPlayerViewController {
    UIView * _videoView;
    int64_t _viewId;
    FlutterMethodChannel* _channel;
}

#pragma mark - life cycle

- (instancetype)initWithWithFrame:(CGRect)frame
                   viewIdentifier:(int64_t)viewId
                        arguments:(id _Nullable)args
                  binaryMessenger:(NSObject<FlutterBinaryMessenger>*)messenger {
  if ([super init]) {
    _viewId = viewId;
    _videoView = [UIView new];
    _videoView.backgroundColor = [UIColor greenColor];
    NSDictionary *dic = args;
    CGFloat x = [dic[@"x"] floatValue];
    CGFloat y = [dic[@"y"] floatValue];
    CGFloat width = [dic[@"width"] floatValue];
    CGFloat height = [dic[@"height"] floatValue];
    _videoView.frame = CGRectMake(x, y, width, height);
    _videoView.jp_videoPlayerDelegate = self;
    NSString* channelName = [NSString stringWithFormat:@"bms_video_player_%lld", viewId];
    _channel = [FlutterMethodChannel methodChannelWithName:channelName binaryMessenger:messenger];
    __weak __typeof__(self) weakSelf = self;
    [_channel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
      [weakSelf onMethodCall:call result:result];
    }];

  }
  return self;
}

- (nonnull UIView *)view {
    return _videoView;
}

- (void)onMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([[call method] isEqualToString:@"loadUrl"]) {
    [self onLoadUrl:call result:result];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)onLoadUrl:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSString* url = [call arguments];
  if (![self loadUrl:url]) {
    result([FlutterError errorWithCode:@"loadUrl_failed"
                               message:@"Failed parsing the URL"
                               details:[NSString stringWithFormat:@"URL was: '%@'", url]]);
  } else {
    result(nil);
  }
}

- (bool)loadUrl:(NSString*)url {
  NSURL* nsUrl = [NSURL URLWithString:url];
  if (!nsUrl) {
    return false;
  }
  
  [_videoView jp_playVideoWithURL:nsUrl
                           bufferingIndicator:nil
                                  controlView:nil
                                 progressView:nil
                                configuration:^(UIView *view, JPVideoPlayerModel *playerModel) {
                                    // self.muteSwitch.on = ![self.videoContainer jp_muted];
                                }];
  return true;
}

#pragma mark - JPVideoPlayerDelegate

- (BOOL)shouldAutoReplayForURL:(nonnull NSURL *)videoURL {
    return true;
}
@end
```
其实，代码实现都很简单，唯一和 Android 端不一样的就是控件的创建不一样，Android 的我直接用 xml，iOS 的主要是需要定义 Frame 大小，我尝试使用函数传递的 frame 值，貌似不管用。如果有人知道问题所在，欢迎告知我！

最后，和 Android 一样，引入我们使用的第三方插件：

**6.4 引入 JPVideoPlayer**

在文件 `bms_video_player.podspec` 引入：

```
s.dependency 'JPVideoPlayer'
```

**7. 链接调用**

看「4」的创建 widget 后的回调函数：

```
Future<void> onPlatformViewCreated(id) async {
    if (widget.onCreated == null) {
      return;
      }
    
    widget.onCreated(new BmsVideoPlayerController.init(id));
  }
```
直接 `new BmsVideoPlayerController.init(id)`，即创建了 `channel`：

```
MethodChannel _channel;
  
BmsVideoPlayerController.init(int id) {
_channel =  new MethodChannel('bms_video_player_$id');
}

Future<void> loadUrl(String url) async {
assert(url != null);
return _channel.invokeMethod('loadUrl', url);
}
```

有了 `channel` 自然和原生代码串联起来了，同时创建 `loadUrl` 函数供外界调用。

**8. 测试使用**

藉此，我们的插件实现了基本功能了，写个 demo，测试下效果：

```
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
    this.viewPlayerController.loadUrl("https://www.****.com/****.mp4");
  }
}
```
相信这代码不用多解释了，引入我们的插件 widget，然后调用 `loadUrl` 函数，传入我们的视频链接，即可开始播放了。

**iOS 效果**

![](http://image.coding01.cn/2019/03/20/15530723766227.jpg)

**Android 效果**

![](http://image.coding01.cn/2019/03/20/15530724023308.jpg)

## 总结

第一次使用 Flutter，第一次实现基本的插件功能，写的比较粗糙，但相信基本的写法都在里面了。接下来就是实现播放视频的所有功能，如：暂停/播放，小窗口播放、全屏播放、缓存、静音等。

还有，就是如何实现 Dart 和原生代码进行通讯的。

![](http://image.coding01.cn/2019/01/12/15472793450210.png)

**未完待续，敬请期待**
