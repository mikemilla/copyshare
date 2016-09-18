package com.mikemilla.copyshare.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikemilla.copyshare.BuildConfig;
import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.ContactModel;
import com.mikemilla.copyshare.data.CopyShareApplication;
import com.mikemilla.copyshare.data.Defaults;
import com.mikemilla.copyshare.data.FrequentContactAmount;
import com.mikemilla.copyshare.lists.ContactSendingAdapter;
import com.mikemilla.copyshare.service.ClipboardService;
import com.mikemilla.copyshare.views.StyledEditText;
import com.mikemilla.copyshare.views.StyledTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String CONTACT_CHANGE = "CONTACT_CHANGE";
    public Tracker mTracker;
    public boolean didPressAddContact = false;
    public List<Integer> selectedIndexes = new ArrayList<>();
    public List<ContactModel> mSendingQueue = new ArrayList<>();

    private Animation slideUp, fadeIn;
    private View background, divider;
    private BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;
    private FrameLayout mBottomSheet, mActionButton;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private StyledTextView mCopiedTextView, mActionTextView, mShareToTextView;
    private StyledEditText mEditText;
    private CoordinatorLayout mCoordinatorLayout;
    private Typeface typeface;
    private boolean didPressSend = false;

    // Handle the clicks depending on data provided
    View.OnClickListener mCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Google analytic
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Cancel Button Click")
                    .build());

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    View.OnClickListener mSendClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            // Pressed the send button
            didPressSend = true;

            // Check SMS Permissions
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                // Send SMS
                sendSMS();

            } else {

                // Request SMS
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 0);
            }
        }
    };

    /**
     * Sends the SMS and closes the view
     */
    private void sendSMS() {

        // Update the contact list order
        List<ContactModel> updatedContactList = Defaults.loadContacts(MainActivity.this);

        // Odd bug that happens when removing and scrolling
        try {
            for (int i = 0; i < selectedIndexes.size(); i++) {
                int index = selectedIndexes.get(i);
                if (updatedContactList != null) {
                    updatedContactList.remove(index);
                    updatedContactList.get(0).setSelected(false);
                    updatedContactList.add(0, mSendingQueue.get(i));
                    updatedContactList.get(0).setSelected(false);
                }
            }
            Defaults.storeContacts(MainActivity.this, updatedContactList);
        } catch (Exception e) {
            Log.e("Index Issue", e.toString());
        }

        // Close Bottom Sheet
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Loop through "queue" and send sms
        for (int i = 0; i < mSendingQueue.size(); i++) {

            // Google analytic
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Sent SMS Copy to (" + mSendingQueue.size() + ")")
                    .build());

            if (mCopiedTextView.getText() == null
                    || mCopiedTextView.getText().equals("")
                    || mCopiedTextView.getText().equals(getResources().getString(R.string.nothing_copied))) {

                // No text was copied
                try {
                    SmsManager.getDefault().sendTextMessage(
                            mSendingQueue.get(i).getNumber(),
                            null,
                            mEditText.getText().toString(),
                            null,
                            null);
                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                }

            } else {

                // Something was copied
                SmsManager.getDefault().sendTextMessage(
                        mSendingQueue.get(i).getNumber(),
                        null,
                        mCopiedTextView.getText().toString() + " " + mEditText.getText().toString(),
                        null,
                        null);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the service on load
        if (!isServiceRunning(ClipboardService.class)) {
            startService(new Intent(getBaseContext(), ClipboardService.class));
        }

        // Google Analytics
        CopyShareApplication application = (CopyShareApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Load the animations
        createAnimations();

        // Init the type face
        typeface = Typeface.createFromAsset(MainActivity.this.getAssets(), getResources().getString(R.string.font));

        // Setup Bottom Sheet
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        background = findViewById(R.id.background);
        mBottomSheet = (FrameLayout) (mCoordinatorLayout != null ? mCoordinatorLayout.findViewById(R.id.bottom_sheet) : null);

        // Populate to create the contact list
        if (Defaults.loadContacts(this) == null) {
            mBottomSheet.setVisibility(View.GONE);
        }

        assert mBottomSheet != null;
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        divider = findViewById(R.id.button_divider);

        ImageView mMoreButton = (ImageView) findViewById(R.id.more_button);
        assert mMoreButton != null;
        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytic
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("More Button Click")
                        .build());

                // Inflate the alert view
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View dialogTitle = inflater.inflate(R.layout.dialog_about, null);

                // Init Dialog Views
                LinearLayout dialogRateButton = (LinearLayout) dialogTitle.findViewById(R.id.dialog_rate_button);
                dialogRateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Google analytic
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Rate App Button Click")
                                .build());

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.mikemilla.copyshare"));
                        startActivity(browserIntent);
                    }
                });

                LinearLayout dialogAboutButton = (LinearLayout) dialogTitle.findViewById(R.id.dialog_about_button);
                dialogAboutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Google analytic
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("About Me Button Click")
                                .build());

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mikemilla.com"));
                        startActivity(browserIntent);
                    }
                });

                StyledTextView versionNumber = (StyledTextView) dialogTitle.findViewById(R.id.dialog_about_version);
                versionNumber.setText("v" + BuildConfig.VERSION_NAME);

                // Setup Dialog
                final AlertDialog mDialog = new AlertDialog.Builder(MainActivity.this)
                        .setCustomTitle(dialogTitle)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        }).create();

                // Show the dialog
                mDialog.show();

                // Change the dialog button text
                mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTypeface(typeface);
                mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(
                        ContextCompat.getColor(MainActivity.this, R.color.accent));

            }
        });

        // Closes sheet on background click
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Google analytic
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Background Cancel Click")
                        .build());

                didPressSend = false;
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Get the height of the peak
        // Based on the height of the content within the bottom sheet
        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBottomSheetBehavior.setPeekHeight(mBottomSheet.getMeasuredHeight()
                        + (int) getResources().getDimension(R.dimen.edit_text_height_fix));
            }
        });

        // Detect keyboard changes
        mCoordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mCoordinatorLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mCoordinatorLayout.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;
                //Log.d("keypadHeight", "" + keypadHeight);

                // 0.15 ratio is perhaps enough to determine keypad height.
                if (keypadHeight > screenHeight * 0.15) {
                    mEditText.setCursorVisible(true);
                    mEditText.requestFocus();
                } else {
                    mEditText.setCursorVisible(false);
                    mEditText.clearFocus();
                }
            }
        });

        // Listens to bottom sheet changes
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // Finishes activity when hidden
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                    // Toast the sending info
                    if (mSendingQueue.size() > 0 && didPressSend) {

                        Toast toast;

                        if (mSendingQueue.size() == 1) {
                            toast = Toast.makeText(MainActivity.this,
                                    "Sent copy to " + mSendingQueue.get(0).getName(), Toast.LENGTH_SHORT);
                        } else {
                            toast = Toast.makeText(MainActivity.this,
                                    "Sent copy to " + mSendingQueue.size() + " people", Toast.LENGTH_SHORT);
                        }

                        // Change toast font
                        LinearLayout toastLayout = (LinearLayout) toast.getView();
                        TextView textView = (TextView) toastLayout.getChildAt(0);
                        textView.setTypeface(typeface);
                        toast.show();
                    }

                    // Finish the app
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

        // Recycler List
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Text View at top
        mCopiedTextView = (StyledTextView) findViewById(R.id.copied_text_view);
        mShareToTextView = (StyledTextView) findViewById(R.id.share_with_tip);

        // Setup the edit text
        mEditText = (StyledEditText) findViewById(R.id.edit_text);

        // Always set cursor at end of view
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditText.setSelection(mEditText.getText().length());
                }
            }
        });

        // Allow edit text to scroll in the bottoms sheet
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP: {
                        mEditText.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }
                return false;
            }
        });

        if (getIntent().getExtras() != null) {
            String link = getIntent().getExtras().getString(ClipboardService.GET_LINK);
            mCopiedTextView.setText(link);
        } else {
            ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipBoard.hasPrimaryClip()) {
                String copy = clipBoard.getPrimaryClip().getItemAt(0).getText().toString();
                mCopiedTextView.setText(copy);
            } else {
                mCopiedTextView.setText(getResources().getString(R.string.nothing_copied));
            }
        }

        // Text View as Button
        mActionButton = (FrameLayout) findViewById(R.id.action_button);
        mActionTextView = (StyledTextView) findViewById(R.id.action_text_view);
        updateUI();

        // Populate to create the contact list
        if (Defaults.loadContacts(this) == null) {

            // Check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS,
                                Manifest.permission.READ_CALL_LOG},
                        0);
            } else {
                searchAndDisplay(getMostPopularContacts());
            }

        } else {

            // We have contacts, so display them
            ContactSendingAdapter adapter = new ContactSendingAdapter(this, Defaults.loadContacts(this));
            if (mRecyclerView != null) {
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.setLayoutManager(mLayoutManager);
            }
        }

        // Animate first load
        if (Defaults.loadContacts(this) != null) {
            mBottomSheet.setAnimation(slideUp);
            background.setAnimation(fadeIn);
        }
    }

    /**
     * Init for animations
     */
    private void createAnimations() {
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    }

    /**
     * Sets button status and text areas
     */
    public void updateUI() {

        // Check the copied text
        if (mCopiedTextView.getText() == null
                || mCopiedTextView.getText().equals("")
                || mCopiedTextView.getText().equals(getResources().getString(R.string.nothing_copied))) {
            mCopiedTextView.setText(getResources().getString(R.string.nothing_copied));
        }

        if (mSendingQueue.size() > 0) {

            // Get selected names
            List<String> selectedNames = new ArrayList<>();
            for (int i = 0; i < mSendingQueue.size(); i++) {

                String fullName = mSendingQueue.get(i).getName();
                if (fullName.contains(" ")) {
                    String name = fullName.substring(0, fullName.indexOf(" "));
                    selectedNames.add(name);
                }
            }

            // Style the list
            String listOfNames = selectedNames.toString()
                    .replace("[", "")  //remove the right bracket
                    .replace("]", "")  //remove the left bracket
                    .trim();

            // Set the text
            mShareToTextView.setText(getResources().getString(R.string.share_to_text)
                    + " " + listOfNames);

            divider.setVisibility(View.GONE);

            // Show edit text if the view is not visible
            if (mEditText.getVisibility() != View.VISIBLE) {
                mEditText.setVisibility(View.VISIBLE);
                mEditText.startAnimation(fadeIn);
            }

            mActionButton.setOnClickListener(mSendClick);
            mActionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_send));

            mActionTextView.setText(R.string.send);
            mActionTextView.setTextColor(ContextCompat.getColor(this, R.color.white));

        } else {
            mShareToTextView.setText(getResources().getString(R.string.share_to_text));
            divider.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.GONE);

            mActionButton.setOnClickListener(mCancelClick);
            mActionButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_cancel));

            mActionTextView.setText(R.string.cancel);
            mActionTextView.setTextColor(ContextCompat.getColor(this, R.color.text_color));
        }
    }

    /**
     * Sets up list based on most contacted contacts
     *
     * @param callLogArray
     */
    public void searchAndDisplay(ArrayList<String> callLogArray) {

        // Add numbers to list
        // Add amounts to amounts
        List<String> numbersList = new ArrayList<>();
        List<Integer> callAmountList = new ArrayList<>();

        // Loop through contacts and add them to two lists
        // Keeps track of contact connection amount
        for (int i = 0; i < callLogArray.size(); i++) {
            int index = numbersList.indexOf(callLogArray.get(i));
            if (index != -1) {
                int newCount = callAmountList.get(index) + 1;
                callAmountList.set(index, newCount);
            } else {
                numbersList.add(callLogArray.get(i));
                callAmountList.add(1);
            }
        }

        // Add Numbers and Amounts to new list
        List<FrequentContactAmount> frequentContactsList = new ArrayList<>();
        for (int i = 0; i < numbersList.size(); i++) {
            frequentContactsList.add(new FrequentContactAmount(numbersList.get(i), callAmountList.get(i)));
        }

        // Sort the list (Reversed)
        Collections.sort(frequentContactsList);

        // Create initial Contact of most popular list
        List<ContactModel> contactList = new ArrayList<>();

        // Display contacts in reverse order
        for (int i = frequentContactsList.size() - 1; i >= 0; i--) {

            String contactName = getContactName(frequentContactsList.get(i).getNumber());
            if (contactName != null) {
                String contactNumber = frequentContactsList.get(i).getNumber();
                contactList.add(new ContactModel(null, contactName, null, contactNumber));
                //Log.d("Contact", contactName + " : " + contactNumber + " : " + frequentContactsList.get(i).getAmount());
            }
        }

        // Set Default contact list
        Defaults.storeContacts(this, contactList);

        // Google analytic
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Generated Popular Contacts List (" + contactList.size() + ")")
                .build());

        // Set the adapter based on the most popular connections
        ContactSendingAdapter adapter = new ContactSendingAdapter(this, contactList);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }

        // Set view to visible and slide up
        mBottomSheet.setVisibility(View.VISIBLE);
        mBottomSheet.startAnimation(slideUp);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Create the list when we have permission
        if (Defaults.loadContacts(MainActivity.this) == null) {
            searchAndDisplay(getMostPopularContacts());
        }

        // Send SMS if they give it on allow send
        if (didPressSend) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

                // Send SMS
                sendSMS();
            }
        }

        // Open Contacts if allowed to
        if (didPressAddContact) {
            didPressAddContact = false;

            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ContactsActivity.class);
                startActivityForResult(intent, 1); // For sending if there is a change back to main
            }
        }

    }

    private ArrayList<String> getMostPopularContacts() {

        ArrayList<String> allNumbers = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

            // Check all contacts to get most popular connections
            Date date = new Date();
            Cursor c = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    CallLog.Calls.TYPE + " AND " + CallLog.Calls.INCOMING_TYPE
                            + " AND " + CallLog.Calls.DATE + ">=" + date.getDate(),
                    null, CallLog.Calls.NUMBER);

            allNumbers.clear();

            // Add Most popular to a list
            if (c != null) {

                c.moveToFirst();

                for (int i = 0; c.getCount() > i; i++) {

                    String number = c.getString(0);

                    if (number != null) {

                        // Remove all misc characters
                        String digits = number.replaceAll("[^0-9.]", "");

                        // Remove the 1 if the county code was added
                        if (digits.length() > 10 && digits.startsWith("1")) {
                            digits = digits.substring(1);
                        }

                        // Add numbers to a list
                        allNumbers.add(digits);
                    }

                    c.moveToNext();
                }

                c.close();
            }
        }

        return allNumbers;
    }

    /**
     * Returns the name of the contact
     *
     * @param phoneNumber
     * @return
     */
    public String getContactName(String phoneNumber) {

        String contactName = null;

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            ContentResolver cr = this.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        return contactName;
    }

    /**
     * Check if the copy service is running
     *
     * @param serviceClass
     * @return
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        // Google analytic
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Back Button Press")
                .build());

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra(CONTACT_CHANGE, false)) {

                    // Update the view
                    ContactSendingAdapter adapter = new ContactSendingAdapter(this, Defaults.loadContacts(this));
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.setLayoutManager(mLayoutManager);

                    // Remove the lists and update UI
                    selectedIndexes.clear();
                    mSendingQueue.clear();
                    updateUI();

                    // May want to use this for toggling to share with newly added users
                    ContactsActivity.numberOfContactsAdded = 0;
                }
            }
        }
    }
}
