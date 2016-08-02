package org.cryse.unifystorage.explorer.event;

public class FrontUIDismissEvent extends AbstractEvent {
    private String mToken;

    public FrontUIDismissEvent(String token) {
        this.mToken = token;
    }

    public String getToken() {
        return mToken;
    }

    @Override
    public int eventId() {
        return EventConst.EVENT_ID_FRONT_UI_DISMISS;
    }
}
