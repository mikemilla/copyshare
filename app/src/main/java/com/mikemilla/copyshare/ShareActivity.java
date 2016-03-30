package com.mikemilla.copyshare;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShareActivity extends AppCompatActivity {

    private Animation slideUp, slideDown, fadeIn, fadeOut;
    private LinearLayout mShareSheet;
    private View mBackgroundView;
    private BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Load the animations
        createAnimations();

        final View background = findViewById(R.id.background);
        final CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        assert mCoordinatorLayout != null;
        FrameLayout mBottomSheet = (FrameLayout) mCoordinatorLayout.findViewById(R.id.bottom_sheet);

        // Listens to bottom sheet changes
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // Finishes activity when hidden
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    finish();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                // Animates the background on slide
                if (background != null) {
                    background.setAlpha(1 + slideOffset);
                }
            }
        });

        // Closes sheet on background click
        assert background != null;
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        mBottomSheet.setAnimation(slideUp);
        background.setAnimation(fadeIn);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerAdapter adapter = new RecyclerAdapter();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }

        TextView mActionButton = (TextView) findViewById(R.id.action_button);
        if (mActionButton != null) {
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            });
        }

    }

    private void createAnimations() {
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //mBackgroundView.startAnimation(fadeOut);
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
