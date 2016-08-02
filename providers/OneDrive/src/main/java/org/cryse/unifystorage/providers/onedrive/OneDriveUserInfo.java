package org.cryse.unifystorage.providers.onedrive;

import org.cryse.unifystorage.StorageUserInfo;
import org.cryse.unifystorage.providers.onedrive.model.Identity;
import org.cryse.unifystorage.providers.onedrive.model.IdentitySet;

public class OneDriveUserInfo extends StorageUserInfo {
    public OneDriveUserInfo(String savedUserInfo) {
        this.restore(savedUserInfo);
    }

    public OneDriveUserInfo(IdentitySet account){
        Identity owner;
        if(account.user != null) {
            owner = account.user;
        } else if(account.device != null) {
            owner = account.device;
        } else if(account.application != null) {
            owner = account.application;
        } else {
            owner = new Identity();
            owner.displayName = "Unknown";
        }
        name = owner.id;
        displayName = owner.displayName;
    }

    @Override
    public String persist() {
        return null;
    }

    @Override
    public void restore(String stored) {

    }
}
