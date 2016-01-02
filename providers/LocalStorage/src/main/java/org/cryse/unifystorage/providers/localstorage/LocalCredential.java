package org.cryse.unifystorage.providers.localstorage;

import android.os.Parcel;

import org.cryse.unifystorage.credential.Credential;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalCredential extends Credential {

    public LocalCredential(String savedCredential) {
        super(savedCredential);
    }

    public LocalCredential(String accountName, String accountType) {
        super(accountName, accountType);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String persist() {
        JSONObject object = new JSONObject();
        try {
            object.put("accountName", accountName);
            object.put("accountType", accountType);
        } catch (JSONException ex) {

        }
        return object.toString();
    }

    @Override
    public void restore(String stored) {

        try {
            JSONObject object = new JSONObject(stored);
            if (object.has("accountName"))
                accountName = object.getString("accountName");
            if (object.has("accountType"))
                accountType = object.getString("accountType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.accountName);
        dest.writeString(this.accountType);
    }

    public LocalCredential() {
    }

    protected LocalCredential(Parcel in) {
        this.accountName = in.readString();
        this.accountType = in.readString();
    }

    public static final Creator<LocalCredential> CREATOR = new Creator<LocalCredential>() {
        public LocalCredential createFromParcel(Parcel source) {
            return new LocalCredential(source);
        }

        public LocalCredential[] newArray(int size) {
            return new LocalCredential[size];
        }
    };
}
