package org.cryse.unifystorage.explorer.ui.common;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.ATEActivity;

public abstract class AbstractActivity extends ATEActivity {
    protected String mATEKey;

    @Nullable
    @Override
    protected final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mATEKey = getATEKey();
    }
}
