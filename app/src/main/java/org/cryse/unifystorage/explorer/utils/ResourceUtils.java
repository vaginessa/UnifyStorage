package org.cryse.unifystorage.explorer.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;

import com.github.clans.fab.FloatingActionButton;

import org.cryse.unifystorage.explorer.R;

public class ResourceUtils {
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int makeColorDarken(@ColorInt int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor; // value component
        return Color.HSVToColor(hsv);
    }

    public static int makeColorLighten(@ColorInt int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 0.2f + 0.8f * hsv[2];
        return Color.HSVToColor(hsv);
    }

    public static int primaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int toolbarTextColor(Context context) {
        return isColorLight(primaryColor(context)) ? Color.BLACK : Color.WHITE;
    }

    public static boolean isColorLight(@ColorInt int color) {
        if (color == Color.BLACK) return false;
        else if (color == Color.WHITE || color == Color.TRANSPARENT) return true;
        final double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.4;
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
