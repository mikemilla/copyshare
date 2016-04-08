package com.mikemilla.copyshare.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;

import com.mikemilla.copyshare.R;

/**
 * Created by Mike Miller on 4/7/16.
 * Because he needed it to be created for some reason
 */
public class StyledSearchView extends SearchView {

    public StyledSearchView(Context context) {
        super(context);
        init();
    }

    public StyledSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.font));
            AutoCompleteTextView searchTextView = (AutoCompleteTextView) this.findViewById(getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
            searchTextView.setTypeface(tf);
        }
    }
}
