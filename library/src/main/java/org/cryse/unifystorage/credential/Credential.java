package org.cryse.unifystorage.credential;

public abstract class Credential {
    protected String accountName;
    protected String accountType;

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
