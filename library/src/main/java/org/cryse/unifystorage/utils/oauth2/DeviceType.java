package org.cryse.unifystorage.utils.oauth2;

import org.cryse.unifystorage.utils.oauth2.OAuth;

public enum DeviceType {
    PHONE {
        @Override
        public OAuth.DisplayType getDisplayParameter() {
            return OAuth.DisplayType.ANDROID_PHONE;
        }
    },
    TABLET {
        @Override
        public OAuth.DisplayType getDisplayParameter() {
            return OAuth.DisplayType.ANDROID_TABLET;
        }
    };

    abstract public OAuth.DisplayType getDisplayParameter();
}
