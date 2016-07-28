package org.cryse.unifystorage.explorer.files;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.cryse.unifystorage.explorer.DataContract;

public class FileOperationReceiver extends BroadcastReceiver {
    private static boolean isRegistered;
    private static FileOperationReceiver instance;

    private FileOperationReceiver() {

    }

    public static synchronized void registerSelf(Context context) {
        if (instance == null) {
            synchronized(FileOperationReceiver.class) {
                if (instance == null) {
                    instance = new FileOperationReceiver();
                }
            }
        }
        if(!isRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(DataContract.Action.NewOperation);
            intentFilter.addAction(DataContract.Action.ShowOperationDialog);
            intentFilter.addAction(DataContract.Action.OpenFile);
            context.registerReceiver(instance, intentFilter);
        }
    }

    public static synchronized void unregisterSelft(Context context) {
        context.unregisterReceiver(instance);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case DataContract.Action.NewOperation:
                String token = intent.getStringExtra(DataContract.Argument.OperationToken);
                OperationProgressDialog dialog = OperationProgressDialog.create(token);
                dialog.show(getChildFragmentManager(), null);
                break;
            case DataContract.Action.OpenFile:
                Log.e("DDDD", "OpenFile");
                synchronized (sOpenFileLock) {
                    if(!intent.hasExtra(DataContract.Argument.Opened)) {
                        String savePath = intent.getStringExtra(DataContract.Argument.SavePath);
                        intent.putExtra(DataContract.Argument.Opened, true);
                        openFileByPath(savePath, true);
                    }
                }
                break;
        }
    }
}
