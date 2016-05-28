package org.cryse.unifystorage.explorer.message;

public class BasicMessage {
    protected static final int MSG_TYPE_BASIC = 0;
    public static final int MSG_TYPE = MSG_TYPE_BASIC;
    public enum MessageAction {
        CREATE, UPDATE, DISMISS
    }
    protected long mId;
    protected MessageAction mAction;
    protected String mTitle;
    protected String mContent;
    protected boolean mHide;

    public BasicMessage(long id, String title, String content) {
        this.mId = id;
        this.mTitle = title;
        this.mContent = content;
    }

    public BasicMessage(long id, String title, String content, boolean hide) {
        this.mId = id;
        this.mTitle = title;
        this.mContent = content;
        this.mHide = hide;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public MessageAction getAction() {
        return mAction;
    }

    public void setAction(MessageAction action) {
        this.mAction = action;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public boolean isHide() {
        return mHide;
    }

    public void setHide(boolean hide) {
        this.mHide = hide;
    }

    public int getMsgType() {
        return MSG_TYPE_BASIC;
    }
}
