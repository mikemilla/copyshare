package com.mikemilla.copyshare.lists;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.activity.ContactsActivity;
import com.mikemilla.copyshare.activity.MainActivity;
import com.mikemilla.copyshare.data.ContactModel;
import com.mikemilla.copyshare.data.Defaults;
import com.mikemilla.copyshare.views.StyledTextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactSendingAdapter extends RecyclerView.Adapter {

    private static final int FOOTER_VIEW = 1;

    public List<ContactModel> mContactsList = new ArrayList<>();
    private MainActivity mMainActivity;

    public ContactSendingAdapter(MainActivity activity, List<ContactModel> contactList) {
        super();
        mMainActivity = activity;
        mContactsList = contactList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView imageView;
        private final StyledTextView textView;
        private final StyledTextView contactLetterText;
        private final ImageView check;
        private final Animation scaleIn, scaleOut;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (CircleImageView) itemView.findViewById(R.id.contact_image);
            contactLetterText = (StyledTextView) itemView.findViewById(R.id.contact_letter_text);
            textView = (StyledTextView) itemView.findViewById(R.id.text_view);
            check = (ImageView) itemView.findViewById(R.id.contact_check);
            scaleIn = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_in);
            scaleOut = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_out);

            scaleOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    check.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        public void removeAt(int position) {
            mContactsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mContactsList.size());
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(mMainActivity, ContactsActivity.class);
                    mMainActivity.startActivityForResult(intent, 1); // For sending if there is a change back to main
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view;
        if (i == FOOTER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_send_plus, parent, false);
            return new FooterViewHolder(view);
        }
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_send_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

        try {
            if (viewHolder instanceof ViewHolder) {

                final ViewHolder item = (ViewHolder) viewHolder;

                if (mContactsList.get(i).getSelected()) {
                    item.check.setVisibility(View.VISIBLE);
                } else {
                    item.check.setVisibility(View.GONE);
                }

                item.textView.setText(mContactsList.get(i).getName());
                if (getContactPhoto(mContactsList.get(i).getNumber()) != null) {
                    item.imageView.setVisibility(View.VISIBLE);
                    item.imageView.setImageBitmap(getContactPhoto(mContactsList.get(i).getNumber()));
                    item.contactLetterText.setVisibility(View.GONE);
                } else {
                    item.imageView.setVisibility(View.GONE);
                    item.contactLetterText.setVisibility(View.VISIBLE);
                    item.contactLetterText.setText(mContactsList.get(i).getNameLetter());

                    // Drawable Array Reference
                    Resources res = item.itemView.getResources();
                    TypedArray circles = res.obtainTypedArray(R.array.circles);

                    if (Build.VERSION.SDK_INT >= 16) {
                        item.contactLetterText.setBackground(circles.getDrawable(
                                mContactsList.get(i).getColor()));
                    } else {
                        item.contactLetterText.setBackgroundDrawable(circles.getDrawable(
                                mContactsList.get(i).getColor()));
                    }

                    // Recycle the array
                    circles.recycle();
                }

                // Touches
                item.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                v.setAlpha(0.7f);
                                break;
                            }
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL: {
                                v.setAlpha(1f);
                                break;
                            }
                        }
                        return false;
                    }
                });

                // Clicks
                item.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Remember the index of the selected item
                        if (mMainActivity.selectedIndexes.contains(i)) {
                            mMainActivity.selectedIndexes.remove(Integer.valueOf(i));
                        } else {
                            mMainActivity.selectedIndexes.add(i);
                        }

                        // Remember the contact in another list
                        if (mMainActivity.mSendingQueue.contains(mContactsList.get(i))) {
                            mMainActivity.mSendingQueue.remove(mContactsList.get(i));
                            mContactsList.get(i).setSelected(false);
                        } else {
                            mMainActivity.mSendingQueue.add(mContactsList.get(i));
                            mContactsList.get(i).setSelected(true);
                        }

                        if (mContactsList.get(i).getSelected()) {
                            item.check.setVisibility(View.VISIBLE);
                            item.check.startAnimation(item.scaleIn);
                        } else {
                            item.check.startAnimation(item.scaleOut);
                        }

                        // Update the UI
                        mMainActivity.setButtonViewContactInfo();

                    }
                });

                // Long Press
                item.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        // Little vibrate
                        Vibrator vibrate = (Vibrator) mMainActivity.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrate.vibrate(20);

                        final Typeface typeface = Typeface.createFromAsset(mMainActivity.getAssets(),
                                mMainActivity.getResources().getString(R.string.font));

                        // Get the dialog Title View
                        LayoutInflater inflater = LayoutInflater.from(mMainActivity);
                        final View dialogTitle = inflater.inflate(R.layout.dialog_title, null);
                        StyledTextView dialogTextView = (StyledTextView) dialogTitle.findViewById(R.id.dialog_text_view);
                        CircleImageView dialogImageView = (CircleImageView) dialogTitle.findViewById(R.id.dialog_image_view);
                        StyledTextView dialogLetter = (StyledTextView) dialogTitle.findViewById(R.id.dialog_letter);

                        // Set the dialog title info
                        dialogTextView.setText("Remove " + mContactsList.get(i).getName() + " from share list?");

                        // Set Image or Other
                        if (getContactPhoto(mContactsList.get(i).getNumber()) != null) {
                            dialogImageView.setVisibility(View.VISIBLE);
                            dialogImageView.setImageBitmap(getContactPhoto(mContactsList.get(i).getNumber()));
                            dialogLetter.setVisibility(View.INVISIBLE);
                        } else {
                            dialogImageView.setVisibility(View.INVISIBLE);
                            dialogLetter.setVisibility(View.VISIBLE);
                            dialogLetter.setText(mContactsList.get(i).getNameLetter());

                            // Drawable Array Reference
                            Resources res = item.itemView.getResources();
                            TypedArray circles = res.obtainTypedArray(R.array.circles);

                            if (Build.VERSION.SDK_INT >= 16) {
                                dialogLetter.setBackground(circles.getDrawable(
                                        mContactsList.get(i).getColor()));
                            } else {
                                dialogLetter.setBackgroundDrawable(circles.getDrawable(
                                        mContactsList.get(i).getColor()));
                            }

                            // Recycle the array
                            circles.recycle();
                        }

                        // Setup Dialog
                        final AlertDialog mDialog = new AlertDialog.Builder(mMainActivity)
                                .setCustomTitle(dialogTitle)
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //
                                    }
                                })
                                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // Create the styled toast
                                        Toast toast = Toast.makeText(mMainActivity,
                                                "Removed " + mContactsList.get(i).getName() + " from share list",
                                                Toast.LENGTH_SHORT);
                                        LinearLayout toastLayout = (LinearLayout) toast.getView();
                                        TextView textView = (TextView) toastLayout.getChildAt(0);
                                        textView.setTypeface(typeface);
                                        toast.show();

                                        // Remove from lists
                                        List<ContactModel> updatedContactList = Defaults.loadContacts(mMainActivity);
                                        if (updatedContactList != null) {
                                            updatedContactList.remove(i);
                                        }

                                        if (mMainActivity.mSendingQueue.contains(mContactsList.get(i))) {
                                            mMainActivity.mSendingQueue.remove(mContactsList.get(i));
                                        }

                                        item.removeAt(i);
                                        Defaults.storeContacts(mMainActivity, updatedContactList);

                                        mMainActivity.setButtonViewContactInfo();
                                    }
                                }).create();

                        // Show the dialog
                        mDialog.show();

                        // Change the dialog button text
                        mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTypeface(typeface);
                        mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(mMainActivity, R.color.accent));
                        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTypeface(typeface);
                        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(mMainActivity, R.color.accent));

                        return false;
                    }
                });

            } else if (viewHolder instanceof FooterViewHolder) {

                FooterViewHolder item = (FooterViewHolder) viewHolder;

                // Touches
                item.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                v.setAlpha(0.7f);
                                break;
                            }
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL: {
                                v.setAlpha(1f);
                                break;
                            }
                        }
                        return false;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    @Override
    public int getItemViewType(int position) {
        if (position == mContactsList.size()) {
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
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
        ContentResolver cr = mMainActivity.getContentResolver();
        Cursor contact = cr.query(phoneUri, new String[]{ContactsContract.Contacts._ID}, null, null, null);

        assert contact != null;
        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        } else {
            contact.close();
            return null;
        }

        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            if (input != null) {
                contact.close();
                return BitmapFactory.decodeStream(input);
            }
        } else {
            contact.close();
            return null;
        }

        contact.close();

        return null;
    }

}
