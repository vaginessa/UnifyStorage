package org.cryse.unifystorage.explorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;

import com.github.clans.fab.FloatingActionButton;

public class ResourceUtils {
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
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
