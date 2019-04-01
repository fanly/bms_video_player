package com.xueyoubangedu.bmsvideoplayer;

import android.content.Context;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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