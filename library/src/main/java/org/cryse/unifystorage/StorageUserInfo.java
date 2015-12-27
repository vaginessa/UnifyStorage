package org.cryse.unifystorage;

public abstract class StorageUserInfo {
    protected String name;
    protected String displayName;
    protected String emailAddress;

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public abstract String persist();

    public abstract void restore(String stored);

    @Override
    public String toString() {
        return "StorageUserInfo{" +
                "emailAddress='" + emailAddress + '\'' + ", " +
                "displayName='" + displayName + '\'' + ", " +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof StorageUserInfo){
            if(name != null && name.equalsIgnoreCase(((StorageUserInfo) o).getName()))
                return true;
            if(displayName != null && displayName.equalsIgnoreCase(((StorageUserInfo) o).getDisplayName()))
                return true;
            if(emailAddress != null && emailAddress.equalsIgnoreCase(((StorageUserInfo) o).getEmailAddress()))
                return true;
        }
        return false;
    }
}
