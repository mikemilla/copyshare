package com.mikemilla.copyshare.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mikemilla.copyshare.R;

/**
 * Created by Mike Miller on 4/7/16.
 * Because he needed it to be created for some reason
 */
public class StyledTextView extends TextView {

    public StyledTextView(Context context) {
        super(context);
        init();
    }

    public StyledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StyledTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.font));
            setTypeface(tf);
        }
    }

}
