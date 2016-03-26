package org.cryse.unifystorage.explorer.ui.common;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;

public abstract class AbstractActivity extends ATEActivity {
    protected String mATEKey;
    private int mPrimaryColor;
    private int mTextPrimaryColor;
    private int mPrimaryDarkColor;
    private int mAccentColor;

    @Nullable
    @Override
    protected final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mATEKey = getATEKey();
        super.onCreate(savedInstanceState);
        mPrimaryColor = Config.primaryColor(this, mATEKey);
        mPrimaryDarkColor = Config.primaryColorDark(this, mATEKey);
        mTextPrimaryColor = Config.textColorPrimary(this, mATEKey);
        mAccentColor = Config.accentColor(this, mATEKey);
    }

    protected int getPrimaryColor() {
        return mPrimaryColor;
    }

    protected int getPrimaryDarkColor() {
        return mPrimaryDarkColor;
    }

    protected int getTextPrimaryColor() {
        return mTextPrimaryColor;
    }

    protected int getAccentColor() {
        return mAccentColor;
    }
}
