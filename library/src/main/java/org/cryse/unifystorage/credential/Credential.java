package org.cryse.unifystorage.credential;

import android.os.Parcelable;

public abstract class Credential implements Parcelable {
    public static final String RESULT_KEY = "unify_storage_credential";
    protected String accountName;
    protected String accountType;

    protected Credential() {

    }

    public Credential(String savedCredential) {
        this.restore(savedCredential);
    }

    public Credential(String accountName, String accountType) {
        this.accountName = accountName;
        this.accountType = accountType;
    }

    public abstract boolean isAvailable();

    public abstract String persist();

    public abstract void restore(String stored);

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
