package org.cryse.unifystorage.explorer.ui.common;

import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.ATEActivity;

public abstract class AbstractActivity extends ATEActivity {
    @Nullable
    @Override
    protected final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }
}
