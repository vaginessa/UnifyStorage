package org.cryse.unifystorage.explorer.event;

public class CancelTaskEvent extends AbstractEvent {
    private String mToken;
    public CancelTaskEvent(String token) {
        mToken = token;
    }

    public String getToken() {
        return mToken;
    }

    @Override
    public int eventId() {
        return 0;
    }
}
