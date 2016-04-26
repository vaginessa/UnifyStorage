package org.cryse.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cryse.unifystorage.explorer.R;

public class StateView extends LinearLayout {
    private ImageView mImageView;
    private TextView mTextView;
    private Button mButton;
    private State mState;
    private OnStateChangeListener mOnStateChangeListener;

    public StateView(Context context) {
        super(context);
        initializeViews(context);
    }

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public StateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        this.setOrientation(VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_state_view, this);
        hide();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.widget_state_view_image);
        mTextView = (TextView) findViewById(R.id.widget_state_view_text);
        mButton = (Button) findViewById(R.id.widget_state_view_button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnStateChangeListener != null)
                    mOnStateChangeListener.onButtonClick(mState);
            }
        });
    }

    public void hide() {
        mState = State.HIDE;
        this.setVisibility(GONE);
    }

    public void showEmptyViewByRes(@DrawableRes int imageId, @StringRes int textId) {
        mState = State.EMPTY;
        showInfoOrEmptyRes(imageId, textId);
    }

    public void showEmptyView(Drawable drawable, String text) {
        mState = State.EMPTY;
        showInfoOrEmpty(drawable, text);
    }

    public void showInfoViewByRes(@DrawableRes int imageId, @StringRes int textId) {
        mState = State.INFO;
        showInfoOrEmptyRes(imageId, textId);
    }

    public void showInfoView(Drawable drawable, String text) {
        mState = State.INFO;
        showInfoOrEmpty(drawable, text);
    }

    public void showErrorView(Drawable drawable, String text, String buttonText) {
        mState = State.ERROR;
        this.setVisibility(VISIBLE);
        mImageView.setVisibility(VISIBLE);
        mTextView.setVisibility(VISIBLE);
        mButton.setVisibility(VISIBLE);
        mImageView.setImageDrawable(drawable);
        mTextView.setText(text);
        mButton.setText(buttonText);
        if(mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChange(mState);
    }

    public void showErrorViewByRes(@DrawableRes int imageId, @StringRes int textId, @StringRes int buttonTextId) {
        mState = State.ERROR;
        this.setVisibility(VISIBLE);
        mImageView.setVisibility(VISIBLE);
        mTextView.setVisibility(VISIBLE);
        mButton.setVisibility(VISIBLE);
        mImageView.setImageResource(imageId);
        mTextView.setText(textId);
        mButton.setText(buttonTextId);
        if(mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChange(mState);
    }

    private void showInfoOrEmpty(Drawable drawable, String text) {
        this.setVisibility(VISIBLE);
        mImageView.setVisibility(VISIBLE);
        mTextView.setVisibility(VISIBLE);
        mButton.setVisibility(GONE);
        mImageView.setImageDrawable(drawable);
        mTextView.setText(text);
        if(mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChange(mState);
    }

    private void showInfoOrEmptyRes(@DrawableRes int imageId, @StringRes int textId) {
        this.setVisibility(VISIBLE);
        mImageView.setVisibility(VISIBLE);
        mTextView.setVisibility(VISIBLE);
        mButton.setVisibility(GONE);
        mImageView.setImageResource(imageId);
        mTextView.setText(textId);
        if(mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChange(mState);
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.mOnStateChangeListener = onStateChangeListener;
    }

    public interface OnStateChangeListener {
        void onStateChange(State state);
        void onButtonClick(State state);
    }

    public enum State {
        HIDE, EMPTY, ERROR, INFO
    }
}
