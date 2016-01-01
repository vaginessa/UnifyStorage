package org.cryse.unifystorage.explorer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.afollestad.materialdialogs.MaterialDialog;

import org.cryse.unifystorage.explorer.R;

import java.io.File;
import java.util.List;

public class OpenFileUtils {
    public static void openFile(Context context, String filePath, boolean useSystemSelector) {
        MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        String mime = mimeMap.getMimeTypeFromExtension(extension);
        File target = new File(filePath);
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        if (mime != null) {
            intent.setDataAndType(Uri.fromFile(target), mime);
            List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
            if(!resolveInfos.isEmpty()) {
                if(useSystemSelector)
                    context.startActivity(intent);
                else {
                    buildCustomOpenChooser(context, filePath, resolveInfos);
                }
                return;
            }
        }
        openUnknownFile(context, filePath);
    }

    public static void openUnknownFile(Context context, String filePath) {
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_title_open_as_type)
                .items(R.array.array_open_as_type)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    }
                })
                .show();
    }

    public static void buildCustomOpenChooser(Context context, String filePath, List<ResolveInfo> resolveInfos) {
        String[] items = new String[resolveInfos.size()];
        for (int i = 0; i < resolveInfos.size(); i++) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            items[i] = resolveInfo.activityInfo.name;
        }
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_title_open_as_type)
                .items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    }
                })
                .show();
    }
}
