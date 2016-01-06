package org.cryse.unifystorage.explorer.ui.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.materialcab.Util;

import org.cryse.unifystorage.explorer.R;

public abstract class AbstractFragment extends Fragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply theming to the Fragment view
        ATE.apply(this, Util.resolveString(getActivity(), R.attr.ate_key));
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
