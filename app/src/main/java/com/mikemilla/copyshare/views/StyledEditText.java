package com.mikemilla.copyshare.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.EditText;

import com.mikemilla.copyshare.R;

/**
 * Created by Mike Miller on 4/7/16.
 * Because he needed it to be created for some reason
 */
public class StyledEditText extends EditText {

    public StyledEditText(Context context) {
        super(context);
        init();
    }

    public StyledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StyledEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.font));
            setTypeface(tf);
            setTextColor(ContextCompat.getColor(getContext(), R.color.text_color));
        }
    }

}
