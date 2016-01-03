package org.cryse.unifystorage.explorer.utils;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class DebugUtils {
    public static void showDialog(Context context, String title, String message) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(message)
                .positiveText(android.R.string.ok)
                .show();
    }

}
