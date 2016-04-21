package org.cryse.unifystorage.explorer.base;

import android.content.Context;

public class AndroidContext implements IContext<Context> {
    private final Context mContext;
    public AndroidContext(Context context) {
        this.mContext = context;
    }

    @Override
    public Context context() {
        return mContext;
    }
}
