package org.cryse.unifystorage.explorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.Util;

public class ResourceUtils {
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int toolbarTextColor(Context context, String key, Toolbar toolbar) {
        boolean isLightMode;
        @Config.LightToolbarMode
        final int lightToolbarMode = Config.lightToolbarMode(context, key, toolbar);
        final int toolbarColor = Config.toolbarColor(context, key, toolbar);
        switch (lightToolbarMode) {
            case Config.LIGHT_TOOLBAR_ON:
                isLightMode = true;
                break;
            case Config.LIGHT_TOOLBAR_OFF:
                isLightMode = false;
                break;
            default:
            case Config.LIGHT_TOOLBAR_AUTO:
                isLightMode = Util.isColorLight(toolbarColor);
                break;
        }

        return isLightMode ? Color.BLACK : Color.WHITE;
    }
}
