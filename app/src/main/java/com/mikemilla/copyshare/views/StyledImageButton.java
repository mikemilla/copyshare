package com.mikemilla.copyshare.views;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageButton;

import com.mikemilla.copyshare.R;

public class StyledImageButton extends ImageButton {

    public StyledImageButton(Context context) {
        super(context);
        init(context);
    }

    public StyledImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StyledImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk <= Build.VERSION_CODES.KITKAT) {
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            setBackgroundDrawable(ContextCompat.getDrawable(context, outValue.resourceId));
        } else {
            setBackground(ContextCompat.getDrawable(context, R.drawable.button_ripple));
        }
    }

}
