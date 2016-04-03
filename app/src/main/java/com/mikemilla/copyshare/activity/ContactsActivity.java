package com.mikemilla.copyshare.activity;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;

import java.util.ArrayList;
import java.util.List;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private VerticalRecyclerViewFastScroller mFastScroller;
    private List<Contact> contacts = new ArrayList<>();
    private ArrayList<String> elements;
    private ListView mListView;

    public int numberOfItems = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryDark));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        final LargeAdapter adapter = new LargeAdapter(getContacts());
        recyclerView.setAdapter(adapter);
        final RecyclerViewFastScroller fastScroller = (RecyclerViewFastScroller) findViewById(R.id.fastscroller);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                //TODO if the items are filtered, considered hiding the fast scroller here
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1)
                        //not initialized, or no items shown, so hide fast-scroller
                        fastScroller.setVisibility(View.GONE);
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                //if all items are shown, hide the fast-scroller
                fastScroller.setVisibility(adapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
            }
        });
        fastScroller.setRecyclerView(recyclerView);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

        /*
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (phones != null) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d("Contacts", name);
                contacts.add(new Contact(name, phoneNumber, null));

            }
            phones.close();
        }

        mListView = (ListView) findViewById(R.id.list_view);
        ContactsAdapter adapter = new ContactsAdapter(this, R.layout.recycler_contact, contacts);
        mListView.setAdapter(adapter);
        */

        /*
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mFastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        assert mFastScroller != null;
        mFastScroller.setRecyclerView(mRecyclerView);

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        mRecyclerView.setOnScrollListener(mFastScroller.getOnScrollListener());

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (phones != null) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d("Contacts", name);
                contacts.add(new Contact(name, phoneNumber, null));

            }
            phones.close();
        }

        // elements
        String s = "MNBVCXZLKJHGFDSAQWERTYUIOP";
        Random r = new Random();
        elements = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            elements.add(s.substring(r.nextInt(s.length())));
        }
        Collections.sort(elements);
        Log.d("Elements", elements.toString());

        //ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(this, contacts);
        ContactsRecyclerAdapter adapter = new ContactsRecyclerAdapter(this, elements);
        if (mRecyclerView != null) {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            SectionTitleIndicator sectionTitleIndicator = (SectionTitleIndicator) findViewById(R.id.fast_scroller_section_title_indicator);
            mFastScroller.setSectionIndicator(sectionTitleIndicator);

            mRecyclerView.setAdapter(adapter);
        }
        */

    }

    private List<Contact> getContacts() {
        if (checkContactsReadPermission()) {
            List<Contact> contacts = new ArrayList<>();
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (phones != null) {
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String picture = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    contacts.add(new Contact(picture, name, phoneNumber, null));
                }
                phones.close();
            }
            return contacts;
        }
        return null;
    }

    private boolean checkContactsReadPermission() {
        String permission = "android.permission.READ_CONTACTS";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

}
