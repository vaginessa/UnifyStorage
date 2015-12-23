package org.cryse.unifystorage.credential;

public abstract class SimpleAuthCredential extends Credential {
    protected String userName;
    protected String password;

    public SimpleAuthCredential(String savedCredential) {
        super(savedCredential);
    }

    public SimpleAuthCredential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    public SimpleAuthCredential(String accountName, String accountType, String userName, String password) {
        super(accountName, accountType);
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
