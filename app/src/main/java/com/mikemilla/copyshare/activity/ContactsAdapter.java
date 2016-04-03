package com.mikemilla.copyshare.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Mike Miller on 4/2/16.
 * Because he needed it to be created for some reason
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {

    private final List<Contact> contacts;
    private final Context context;
    private final int resource;

    public ContactsAdapter(Context context, int resource, List<Contact> contacts) {
        super(context, resource, contacts);
        this.context = context;
        this.contacts = contacts;
        this.resource = resource;
    }

    public int getCount() {
        return contacts.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(resource, null);
        }

        Contact contact = getItem(position);

        if (contact != null) {
            ImageView imageView = (ImageView) view.findViewById(R.id.contact_image);
            TextView textView = (TextView) view.findViewById(R.id.text_view);

            if (imageView != null) {
                imageView.setImageBitmap(getContactPhoto(contact.getNumber()));
            }

            if (textView != null) {
                textView.setText(contact.getName());
            }
        }

        return view;

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
        ContentResolver cr = context.getContentResolver();
        Cursor contact = cr.query(phoneUri, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        assert contact != null;
        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            return BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
        }

        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        } else {
            return BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
        }

        contact.close();

        return BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image);
    }

}
