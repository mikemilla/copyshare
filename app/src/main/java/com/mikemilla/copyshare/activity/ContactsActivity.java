package com.mikemilla.copyshare.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.ContactModel;
import com.mikemilla.copyshare.lists.ContactAddingAdapter;
import com.mikemilla.copyshare.lists.RecyclerViewFastScroller;
import com.mikemilla.copyshare.views.StyledSearchView;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements StyledSearchView.OnQueryTextListener {

    public static int numberOfContactsAdded = 0;

    private RecyclerView mRecyclerView;
    private List<ContactModel> mModels;
    private ContactAddingAdapter mAdapter;
    private RecyclerViewFastScroller fastScroller;
    private android.support.v7.widget.Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primaryDark));
        }

        // Add back button to action bar (Toolbar)
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Add Contacts to the lists
        mModels = new ArrayList<>();
        if (getContacts() != null) {
            for (ContactModel contact : getContacts()) {
                mModels.add(new ContactModel(contact.getPicture(), contact.getName(), contact.getNumbers(), null));
            }
        }

        // Setup the recycler view and adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mAdapter = new ContactAddingAdapter(this, mModels);
        mRecyclerView.setAdapter(mAdapter);

        // Setup Fast Scroll Bar
        fastScroller = (RecyclerViewFastScroller) findViewById(R.id.fastscroller);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1) {
                        fastScroller.setVisibility(View.GONE);
                    }
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                fastScroller.setVisibility(mAdapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
            }
        });
        fastScroller.setRecyclerView(mRecyclerView);
        fastScroller.setViewsToUse(R.layout.fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final StyledSearchView searchView = (StyledSearchView) MenuItemCompat.getActionView(item);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<ContactModel> filteredModelList = filter(mModels, query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    /**
     * Filters contacts to show queried results
     *
     * @param models
     * @param query
     * @return
     */
    private List<ContactModel> filter(List<ContactModel> models, String query) {
        query = query.toLowerCase();

        final List<ContactModel> filteredModelList = new ArrayList<>();
        for (ContactModel model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // back button press
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkContactsReadPermission() {
        String permission = "android.permission.READ_CONTACTS";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private List<ContactModel> getContacts() {
        if (checkContactsReadPermission()) {

            String mLastContactName = "";
            int index = 0;
            List<ContactModel> contacts = new ArrayList<>();

            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (phones != null) {
                while (phones.moveToNext()) {

                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String picture = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (!Character.isDigit(name.charAt(0))) {
                        if (name.equals(mLastContactName)) {
                            contacts.get(index - 1).getNumbers().add(phoneNumber);
                        } else {
                            List<String> numbers = new ArrayList<>();
                            numbers.add(phoneNumber);
                            contacts.add(new ContactModel(picture, name, numbers, null));
                            index++;
                        }
                    }

                    mLastContactName = name;
                }
                phones.close();
            }
            return contacts;
        }
        return null;
    }

    @Override
    public void onBackPressed() {

        if (numberOfContactsAdded > 0) {
            Intent intent = new Intent();
            intent.putExtra(MainActivity.CONTACT_CHANGE, true);
            setResult(RESULT_OK, intent);
        }

        super.onBackPressed();
    }
}
