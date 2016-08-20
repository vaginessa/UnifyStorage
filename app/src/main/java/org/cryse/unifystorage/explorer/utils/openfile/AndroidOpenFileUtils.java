package org.cryse.unifystorage.explorer.utils.openfile;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.utils.MimeUtils;

import java.io.File;
import java.util.List;

public class AndroidOpenFileUtils implements OpenFileUtils {
    private Context mContext;

    public AndroidOpenFileUtils(Context context) {
        this.mContext = context;
    }

    @Override
    public void openFileByPath(String filePath, boolean useSystemSelector) {
        Uri uri = null;
        try {
            uri = FileProvider.getUriForFile(mContext, mContext.getString(R.string.authority_file_provider), new File(filePath));
        } catch (IllegalArgumentException exception) {
            uri = Uri.fromFile(new File(filePath));
        }
        openFileByUri(uri.toString(), useSystemSelector);
    }

    @Override
    public void openFileByUri(String uriString, boolean useSystemSelector) {
        Uri uri = Uri.parse(uriString);
        String mime = MimeUtils.getMime(uri.getPath().replace(" ",""));
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (mime != null) {
            intent.setDataAndType(uri, mime);
            List<ResolveInfo> resolveInfos = mContext.getPackageManager().queryIntentActivities(intent, 0);
            if (!resolveInfos.isEmpty()) {
                if (useSystemSelector)
                    mContext.startActivity(intent);
                else {
                    buildCustomOpenChooser(mContext, uri, resolveInfos);
                }
                return;
            }
        }
        openUnknownFile(uri);
    }

    private void openUnknownFile(final Uri uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        new MaterialDialog.Builder(mContext)
                .title(R.string.dialog_title_open_as_type)
                .items(R.array.array_open_as_type)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        String mime;
                        switch (which) {
                            case 0:
                                mime = "text/*";

                                break;
                            case 1:
                                mime = "image/*";
                                break;
                            case 2:
                                mime = "video/*";
                                break;
                            case 3:
                                mime = "audio/*";
                                break;
                            default:
                            case 4:
                                mime = "*/*";
                                break;
                        }

                        intent.setDataAndType(uri, mime);
                        mContext.startActivity(intent);
                    }
                })
                .show();
    }

    private void buildCustomOpenChooser(Context context, Uri uri, List<ResolveInfo> resolveInfos) {
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
