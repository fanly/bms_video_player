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
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

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