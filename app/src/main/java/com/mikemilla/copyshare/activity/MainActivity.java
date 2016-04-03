package com.mikemilla.copyshare.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;
import com.mikemilla.copyshare.data.Defaults;
import com.mikemilla.copyshare.data.FrequentContactAmount;
import com.mikemilla.copyshare.service.ClipboardService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Init
    private Animation slideUp, fadeIn;
    private View background;
    private BottomSheetBehavior<FrameLayout> mBottomSheetBehavior;
    private FrameLayout mBottomSheet;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private TextView mActionButton, mShareToTextView;
    private Contact mContactToShareWith = null;
    private EditText mEditText;
    public int selectedContactIndex;

    // Handle the clicks depending on data provided
    View.OnClickListener mCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    View.OnClickListener mSendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            List<Contact> updatedContactList = Defaults.loadContacts(MainActivity.this);
            if (updatedContactList != null) {
                updatedContactList.remove(selectedContactIndex);
                updatedContactList.add(0, mContactToShareWith);
            }
            Defaults.storeContacts(MainActivity.this, updatedContactList);

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(mContactToShareWith.getNumber(), null,
                    mEditText.getText().toString(), null, null);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Toast.makeText(MainActivity.this, "Sent copy to " + mContactToShareWith.getName(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the animations
        createAnimations();

        // Setup Bottom Sheet
        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        background = findViewById(R.id.background);
        mBottomSheet = (FrameLayout) (mCoordinatorLayout != null ? mCoordinatorLayout.findViewById(R.id.bottom_sheet) : null);
        assert mBottomSheet != null;
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        // Closes sheet on background click
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Get the height of the peak
        // Based on the height of the content within the bottom sheet
        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBottomSheetBehavior.setPeekHeight(mBottomSheet.getMeasuredHeight());
            }
        });

        // Listens to bottom sheet changes
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

        // Recycler List
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Text View at top
        mShareToTextView = (TextView) findViewById(R.id.share_to_text_view);

        // Setup the edit text
        mEditText = (EditText) findViewById(R.id.edit_text);
        if (getIntent().getExtras() != null) {
            String link = getIntent().getExtras().getString(ClipboardService.GET_LINK);
            mEditText.setText(link);
        }

        // Text View as Button
        mActionButton = (TextView) findViewById(R.id.action_button);
        setButtonViewContactInfo();

        // Switch
        Switch mServiceSwitch = (Switch) findViewById(R.id.service_switch);
        if (mServiceSwitch != null) {

            // Starts the service by default
            mServiceSwitch.setChecked(true);
            if (!isServiceRunning(ClipboardService.class)) {
                startService(new Intent(getBaseContext(), ClipboardService.class));
            }

            if (Defaults.loadContacts(this) == null) {
                Toast.makeText(MainActivity.this, "Set", Toast.LENGTH_SHORT).show();
                createInitialContactList();
            } else {
                Toast.makeText(MainActivity.this, "Get", Toast.LENGTH_SHORT).show();
                SendingRecyclerAdapter adapter = new SendingRecyclerAdapter(this, Defaults.loadContacts(this));
                if (mRecyclerView != null) {
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                }
            }

            // Listen to check changes
            mServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        startService(new Intent(getBaseContext(), ClipboardService.class));
                    } else {
                        if (isServiceRunning(ClipboardService.class)) {
                            stopService(new Intent(getBaseContext(), ClipboardService.class));
                        }
                    }
                }
            });
        }

        // Animate first load
        mBottomSheet.setAnimation(slideUp);
        background.setAnimation(fadeIn);

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
    public void setButtonViewContactInfo() {
        if (mContactToShareWith != null) {
            mShareToTextView.setText(mContactToShareWith.getName() + ", " + mContactToShareWith.getNumber());
            mActionButton.setOnClickListener(mSendClick);
            mActionButton.setText(R.string.send);
        } else {
            mShareToTextView.setText(null);
            mActionButton.setOnClickListener(mCancelClick);
            mActionButton.setText(R.string.cancel);
        }
    }

    /**
     * Sets the contact that was selected
     *
     * @param contact
     */
    public void setContactToShareWith(Contact contact) {
        mContactToShareWith = contact;
        setButtonViewContactInfo();
    }

    /**
     * Returns the contact that was selected
     *
     * @return
     */
    public Contact getContactToShareWith() {
        return mContactToShareWith;
    }

    /**
     * Begins running the copy service
     * Service Triggers when a user copies anything while it is running
     */
    private void createInitialContactList() {
        // Here, thisActivity is the current activity
        /*
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

        }
        */

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG},
                0);

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
        List<Contact> contactList = new ArrayList<>();

        // Display contacts in reverse order
        for (int i = frequentContactsList.size() - 1; i >= 0; i--) {

            String contactName = getContactName(frequentContactsList.get(i).getNumber());
            if (contactName != null) {
                String contactNumber = frequentContactsList.get(i).getNumber();
                contactList.add(new Contact(contactName, contactNumber, null));

                //Log.d("Contact", contactName + " : " + contactNumber + " : " + frequentContactsList.get(i).getAmount());
            }
        }

        // Set Default contact list
        Defaults.storeContacts(this, contactList);

        // Set the adapter based on the most popular connections
        SendingRecyclerAdapter adapter = new SendingRecyclerAdapter(this, contactList);
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {

            // Check all contacts to get most popular connections
            Date date = new Date();
            ArrayList<String> allNumbers = new ArrayList<>();
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
            }

            // Send this list to a new method
            // For sorting
            searchAndDisplay(allNumbers);
        }

    }

    /**
     * Returns the name of the contact
     *
     * @param phoneNumber
     * @return
     */
    public String getContactName(String phoneNumber) {

        ContentResolver cr = this.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }

        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * Returns the profile image on the contact
     *
     * @param phoneNumber
     * @return
     */
    public Bitmap getContactPhoto(String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Uri photoUri;
        ContentResolver cr = this.getContentResolver();
        Cursor contact = cr.query(phoneUri, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        assert contact != null;
        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            return BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
        }

        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        } else {
            return BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
        }

        contact.close();

        return BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
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
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
