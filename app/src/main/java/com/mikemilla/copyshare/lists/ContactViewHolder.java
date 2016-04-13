package com.mikemilla.copyshare.lists;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.activity.ContactsActivity;
import com.mikemilla.copyshare.data.ContactModel;
import com.mikemilla.copyshare.data.Defaults;
import com.mikemilla.copyshare.views.StyledTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactViewHolder extends RecyclerView.ViewHolder {

    private final StyledTextView tvText;
    private final ImageView imageView;
    private final StyledTextView contactLetter;

    public ContactViewHolder(View itemView) {
        super(itemView);

        tvText = (StyledTextView) itemView.findViewById(R.id.text_view);
        imageView = (ImageView) itemView.findViewById(R.id.contact_image);
        contactLetter = (StyledTextView) itemView.findViewById(R.id.contact_letter);
    }

    public void bind(final ContactModel model) {
        tvText.setText(model.getName());

        if (model.getPicture() != null) {
            Uri image = Uri.parse(model.getPicture());
            imageView.setImageURI(image);
            imageView.setVisibility(View.VISIBLE);
            contactLetter.setVisibility(View.GONE);
        } else {
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
            contactLetter.setVisibility(View.VISIBLE);
            contactLetter.setText(model.getNameLetter());

            // Drawable Array Reference
            Resources res = itemView.getContext().getResources();
            TypedArray circles = res.obtainTypedArray(R.array.circles);

            if (Build.VERSION.SDK_INT >= 16) {
                contactLetter.setBackground(circles.getDrawable(
                        model.getColor()));
            } else {
                contactLetter.setBackgroundDrawable(circles.getDrawable(
                        model.getColor()));
            }

            // Recycle the array
            circles.recycle();
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(),
                        itemView.getContext().getResources().getString(R.string.font));

                // Setup the list of items
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                final View contactMeansView = inflater.inflate(R.layout.dialog_list, null);
                ListView listView = (ListView) contactMeansView.findViewById(R.id.list_view);
                ContactDialogAdapter adapter = new ContactDialogAdapter(
                        itemView.getContext(), R.layout.dialog_list_item, model.getNumbers());
                listView.setAdapter(adapter);

                // Get the dialog Title View
                final View dialogTitle = inflater.inflate(R.layout.dialog_title, null);
                StyledTextView dialogTextView = (StyledTextView) dialogTitle.findViewById(R.id.dialog_text_view);
                CircleImageView dialogImageView = (CircleImageView) dialogTitle.findViewById(R.id.dialog_image_view);
                StyledTextView dialogLetter = (StyledTextView) dialogTitle.findViewById(R.id.dialog_letter);

                // Set the dialog title info
                dialogTextView.setText(model.getName());

                // Set Image or Other
                if (model.getPicture() != null) {
                    Uri image = Uri.parse(model.getPicture());
                    dialogImageView.setImageURI(image);
                    dialogImageView.setVisibility(View.VISIBLE);
                    dialogLetter.setVisibility(View.INVISIBLE);
                } else {
                    dialogImageView.setImageBitmap(null);
                    dialogImageView.setVisibility(View.INVISIBLE);
                    dialogLetter.setVisibility(View.VISIBLE);
                    dialogLetter.setText(model.getNameLetter());

                    // Drawable Array Reference
                    Resources res = itemView.getContext().getResources();
                    TypedArray circles = res.obtainTypedArray(R.array.circles);

                    if (Build.VERSION.SDK_INT >= 16) {
                        dialogLetter.setBackground(circles.getDrawable(
                                model.getColor()));
                    } else {
                        dialogLetter.setBackgroundDrawable(circles.getDrawable(
                                model.getColor()));
                    }

                    // Recycle the array
                    circles.recycle();
                }

                // Setup Dialog
                final AlertDialog mDialog = new AlertDialog.Builder(itemView.getContext())
                        .setCustomTitle(dialogTitle)
                        .setView(contactMeansView)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        }).create();

                // List to item Click
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        List<ContactModel> contactList = Defaults.loadContacts(itemView.getContext());
                        if (contactList != null) {
                            contactList.add(0, new ContactModel(null, model.getName(), null, model.getNumbers().get(position)));
                        }

                        Defaults.storeContacts(itemView.getContext(), contactList);
                        mDialog.dismiss();

                        // Add contacts to the list of added new contacts
                        ContactsActivity.numberOfContactsAdded += 1;

                        // Create the styled toast
                        Toast toast = Toast.makeText(itemView.getContext(),
                                "Added " + model.getName() + " to share list",
                                Toast.LENGTH_SHORT);
                        LinearLayout toastLayout = (LinearLayout) toast.getView();
                        TextView textView = (TextView) toastLayout.getChildAt(0);
                        textView.setTypeface(typeface);
                        toast.show();
                    }
                });

                // Show the dialog
                mDialog.show();

                // Change the dialog button text
                mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTypeface(typeface);
                mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.accent));
            }
        });
    }
}
