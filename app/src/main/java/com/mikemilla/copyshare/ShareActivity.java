package com.mikemilla.copyshare;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShareActivity extends AppCompatActivity {

    private Animation slideUp, slideDown, fadeIn, fadeOut;
    private LinearLayout mShareSheet;
    private View mBackgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Load the animations
        createAnimations();

        // Check if the version of Android is Lollipop or higher
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBackgroundView = findViewById(R.id.background_view);
        if (mBackgroundView != null) {
            mBackgroundView.startAnimation(fadeIn);
            mBackgroundView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBackgroundView.startAnimation(fadeOut);
                }
            });
        }

        mShareSheet = (LinearLayout) findViewById(R.id.share_sheet);

        TextView mActionButton = (TextView) findViewById(R.id.action_button);
        if (mActionButton != null) {
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundView.startAnimation(fadeOut);
                        }
                    }, 200);
                }
            });
        }
    }


    private void createAnimations() {
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mShareSheet.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Listeners
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mShareSheet.startAnimation(slideUp);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mShareSheet.startAnimation(slideDown);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        mBackgroundView.startAnimation(fadeOut);
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
