package org.cryse.unifystorage.explorer.files;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.cryse.unifystorage.explorer.DataContract;
import org.cryse.unifystorage.explorer.R;
import org.cryse.unifystorage.explorer.service.OperationService;
import org.cryse.unifystorage.explorer.service.operation.base.OnOperationListener;
import org.cryse.unifystorage.explorer.service.operation.base.Operation;
import org.cryse.unifystorage.explorer.service.operation.base.OperationState;
import org.cryse.unifystorage.explorer.service.operation.base.RemoteOperationResult;
import org.cryse.unifystorage.explorer.ui.MainActivity;

import java.util.concurrent.ConcurrentHashMap;

public class OperationProgressDialog extends DialogFragment implements OnOperationListener {
    private static ConcurrentHashMap<String, OperationProgressDialog> mDialogMap = new ConcurrentHashMap<>();


    private String mToken;
    private OperationService.OperationBinder mOperationBinder;
    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private TextView mContent;
    private TextView mContent1;
    private TextView mContent2;
    private Handler mHandler;



    public static OperationProgressDialog create(String token) {
        synchronized (mDialogMap) {
            Log.e("CREATE_D", token);
            OperationProgressDialog dialog = mDialogMap.get(token);
            if(dialog == null) {
                dialog = new OperationProgressDialog();
                Bundle args = new Bundle();
                args.putString(DataContract.Argument.OperationToken, token);
                dialog.setArguments(args);
                mDialogMap.put(token, dialog);
            }
            return dialog;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        Bundle arguments = getArguments();
        if(arguments != null) {
            mToken = arguments.getString(DataContract.Argument.OperationToken);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDialogMap.remove(mToken);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customView;
        try {
            customView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_operation_progress, null);
        } catch (InflateException e) {
            throw new IllegalStateException("This device does not support Web Views.");
        }

        Operation operation = getOperationBinder().getOperation(mToken);

        MaterialDialog dialog;
        if(operation == null) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .theme(getArguments().getBoolean("dark_theme") ? Theme.DARK : Theme.LIGHT)
                    .title(getString(R.string.operation_title_background_task))
                    .progress(true, 100)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .build();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            });
        } else if(operation.isIndeterminate()) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .theme(getArguments().getBoolean("dark_theme") ? Theme.DARK : Theme.LIGHT)
                    .title(operation.getSummaryTitle(getActivity(), false))
                    .progress(true, 100)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .build();
        } else {

            dialog = new MaterialDialog.Builder(getActivity())
                    .theme(getArguments().getBoolean("dark_theme") ? Theme.DARK : Theme.LIGHT)
                    .title(operation == null ? getString(R.string.operation_title_background_task) : operation.getSummaryTitle(getActivity(), false))
                    .customView(customView, false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .build();
        }

        mProgressBar1 = (ProgressBar)customView.findViewById(R.id.dialog_operation_progress_bar1);
        mProgressBar2 = (ProgressBar)customView.findViewById(R.id.dialog_operation_progress_bar2);
        mContent = (TextView)customView.findViewById(R.id.dialog_operation_progress_content);
        mContent1 = (TextView)customView.findViewById(R.id.dialog_operation_progress_content1);
        mContent2 = (TextView)customView.findViewById(R.id.dialog_operation_progress_content2);

        mProgressBar1.setMax(100);
        mProgressBar2.setMax(100);
        return dialog;
    }

    private OperationService.OperationBinder getOperationBinder() {
        if(mOperationBinder == null) {
            mOperationBinder = ((MainActivity)getActivity()).getOperationBinder();
        }
        return mOperationBinder;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getOperationBinder().addOnOperationListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getOperationBinder().removeOnOperationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Operation operation = getOperationBinder().getOperation(mToken);
        if(operation != null) {
            onOperationStateChanged(operation, operation.getState());
            onOperationProgress(
                    operation,
                    operation.getSummary().currentItemReadSize,
                    operation.getSummary().currentItemSize,
                    operation.getSummary().currentSpeed,
                    operation.getSummary().itemIndex,
                    operation.getSummary().itemCount,
                    operation.getSummary().totalReadSize,
                    operation.getSummary().totalSize
            );
        }
    }

    @Override
    public void onOperationStateChanged(Operation operation, OperationState state) {
        if(state == OperationState.COMPLETED) {
            this.dismiss();
        } else if(state == OperationState.FAILED) {
            RemoteOperationResult result = (RemoteOperationResult) operation.getResult();
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_title_error)
                    .content(result.getLogMessage())
                    .show();
            this.dismiss();
        } else {
            getDialog().setTitle(operation.getSummaryTitle(getActivity(), false));
            if(mContent != null) {
                mContent.setText(operation.getSummaryContent(getActivity(), false));
            }
        }
    }

    @Override
    public void onOperationProgress(Operation operation, long currentItemRead, long currentItemSize, long currentSpeed, long itemIndex, long itemCount, long totalRead, long totalSize) {
        int totalProgress = (int)(operation.getSummary().totalCountPercent * 100d);
        int currentProgress = (int)(operation.getSummary().currentSizePercent * 100d);
        if(mProgressBar1 != null && mProgressBar2 != null && mContent1 != null && mContent2 != null) {
            mProgressBar1.setProgress(totalProgress);
            mContent1.setText(operation.getSummary().totalCountProgressDesc(getActivity()));
            mProgressBar2.setProgress(currentProgress);
            mContent2.setText(operation.getSummary().currentProgressDesc(getActivity()));
        }
    }
}
