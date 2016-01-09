package org.cryse.unifystorage.explorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.Util;
import com.github.clans.fab.FloatingActionButton;

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

    public static boolean isColorLight(int color) {
        return Util.isColorLight(color);
    }

    public static int makeColorDarken(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor; // value component
        return Color.HSVToColor(hsv);
    }

    public static int makeColorLighten(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 0.2f + 0.8f * hsv[2];
        return Color.HSVToColor(hsv);
    }

    public static Drawable makeTintedDrawable(Context context, @DrawableRes int drawableId, int tintColor) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableId, null).mutate();
        DrawableCompat.setTint(drawable, tintColor);
        return drawable;
    }

    public static void applyColorToFab(
            FloatingActionButton fab,
            int colorNormal,
            int colorPressed,
            int colorRipple,
            @DrawableRes int colorDrawable,
            int colorDrawableTint
    ) {

        fab.setColorNormal(colorNormal);
        fab.setColorPressed(colorPressed);
        fab.setColorRipple(colorRipple);
        fab.setImageDrawable(ResourceUtils.makeTintedDrawable(fab.getContext(), colorDrawable, colorDrawableTint));
    }
}
