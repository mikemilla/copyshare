package com.mikemilla.copyshare.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter implements SectionIndexer {

    private final HashMap<String, Integer> azIndexer;
    private final ArrayList<String> myElements;
    private List<Contact> mContactsList = new ArrayList<>();
    private ContactsActivity mActivity;
    String[] sections;

    //public ContactsRecyclerAdapter(ContactsActivity activity, List<Contact> contactList) {
    public ContactsRecyclerAdapter(ContactsActivity activity, List<String> elements) {
        super();
        mActivity = activity;
        //mContactsList = contactList;

        myElements = (ArrayList<String>) elements;
        azIndexer = new HashMap<>(); //stores the positions for the start of each letter

        int size = elements.size();
        for (int i = size - 1; i >= 0; i--) {
            String element = elements.get(i);
            azIndexer.put(element.substring(0, 1), i);
        }

        Set<String> keys = azIndexer.keySet(); // set of letters 

        Iterator<String> it = keys.iterator();
        ArrayList<String> keyList = new ArrayList<String>();

        while (it.hasNext()) {
            String key = it.next();
            keyList.add(key);
        }
        Collections.sort(keyList);//sort the keylist
        sections = new String[keyList.size()]; // simple conversion to array            
        keyList.toArray(sections);
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        String letter = sections[sectionIndex];
        return azIndexer.get(letter);
    }

    @Override
    public int getSectionForPosition(int position) {
        Log.v("getSectionForPosition", "called");
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout container;
        CircleImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            imageView = (CircleImageView) itemView.findViewById(R.id.contact_image);
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

        final ViewHolder item = (ViewHolder) viewHolder;

        //item.imageView.setImageBitmap(getContactPhoto(mContactsList.get(i).getNumber()));
        //item.textView.setText(mContactsList.get(i).getName());

        item.textView.setText(myElements.get(i));

        // Clicks
        item.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mActivity, "Dope", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return myElements.size();
    }

    /*
    @Override
    public int getItemCount() {
        if (mContactsList == null) {
            return 0;
        }

        if (mContactsList.size() == 0) {
            return 1;
        }

        return mContactsList.size() + 1;
    }
    */

    /**
     * Returns the profile image on the contact
     *
     * @param phoneNumber
     * @return
     */
    public Bitmap getContactPhoto(String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Uri photoUri;
        ContentResolver cr = mActivity.getContentResolver();
        Cursor contact = cr.query(phoneUri, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        assert contact != null;
        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            return BitmapFactory.decodeResource(mActivity.getResources(), android.R.drawable.ic_menu_report_image);
        }

        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        } else {
            return BitmapFactory.decodeResource(mActivity.getResources(), android.R.drawable.ic_menu_report_image);
        }

        contact.close();

        return BitmapFactory.decodeResource(mActivity.getResources(), android.R.drawable.ic_menu_report_image);
    }

}
