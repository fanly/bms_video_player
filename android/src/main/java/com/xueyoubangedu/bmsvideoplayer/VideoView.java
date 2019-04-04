package com.xueyoubangedu.bmsvideoplayer;

import android.content.Context;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import static io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import static io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.platform.PlatformView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;

public class VideoView implements PlatformView, MethodCallHandler {
    private StandardGSYVideoPlayer bmsVideo;
    private OrientationUtils orientationUtils;
    private GSYVideoOptionBuilder gsyVideoOption;
    private final MethodChannel methodChannel;
    private final Registrar registrar;

    VideoView(Context context, int viewId, Object args, Registrar registrar) {
        this.registrar = registrar;
        bmsVideo = (StandardGSYVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
        this.methodChannel = new MethodChannel(registrar.messenger(), "bms_video_player_" + viewId);
        this.methodChannel.setMethodCallHandler(this);
    }

    @Override
    public View getView() {
        return bmsVideo;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "loadUrl":
                String url = methodCall.arguments.toString();
                getBmsVideo(url);
                break;
            default:
                result.notImplemented();
        }

    }

    @Override
    public void dispose() {}

    private void getBmsVideo(String url) {
        // 初始化
        //设置旋转
        orientationUtils = new OrientationUtils(registrar.activity(), bmsVideo);
        
        //是否可以滑动调整
        bmsVideo.setIsTouchWiget(true);
        //设置返回按键
        bmsVideo.getBackButton().setVisibility(View.GONE);

        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(url)
            .setCacheWithPlay(true)
            .setVideoAllCallBack(new GSYSampleCallBack() {
                @Override
                public void onPrepared(String url, Object... objects) {
                    super.onPrepared(url, objects);
                    //开始播放了才能旋转和全屏
                    orientationUtils.setEnable(true);
                    // isPlay = true;
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                    if (orientationUtils != null) {
                        orientationUtils.backToProtVideo();
                    }
                }
            }).setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        //配合下方的onConfigurationChanged
                        orientationUtils.setEnable(!lock);
                    }
                }
            }).build(bmsVideo);

            bmsVideo.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                // 第一个 true 是否需要隐藏 actionbar，第二个 true 是否需要隐藏 statusbar
                // todo 暂时都设置为 false
                bmsVideo.startWindowFullscreen(registrar.activity(), false, false);
            }
        });

        bmsVideo.startPlayLogic();
    }
}