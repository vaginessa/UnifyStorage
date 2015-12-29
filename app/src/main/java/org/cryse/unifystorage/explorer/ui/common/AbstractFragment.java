package org.cryse.unifystorage.explorer.ui.common;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public abstract class AbstractFragment extends Fragment {
    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
