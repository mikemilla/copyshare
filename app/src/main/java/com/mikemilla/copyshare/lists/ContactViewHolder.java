package com.mikemilla.copyshare.lists;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.activity.MainActivity;
import com.mikemilla.copyshare.data.ContactModel;
import com.mikemilla.copyshare.data.Defaults;
import com.mikemilla.copyshare.views.StyledTextView;

import java.util.List;

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

            if (Build.VERSION.SDK_INT >= 16) {
                contactLetter.setBackground(MainActivity.contactColors.get(model.getColor()));
            } else {
                contactLetter.setBackgroundDrawable(MainActivity.contactColors.get(model.getColor()));
            }
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Setup the list of items
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                final View contactMeansView = inflater.inflate(R.layout.add_contact_list, null);
                ListView listView = (ListView) contactMeansView.findViewById(R.id.list_view);
                ContactDialogAdapter adapter = new ContactDialogAdapter(
                        itemView.getContext(), R.layout.dialog_means_item, model.getNumbers());
                listView.setAdapter(adapter);

                // Setup Dialog
                final AlertDialog mDialog = new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Add " + model.getName())
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
                            contactList.add(new ContactModel(null, model.getName(), null, model.getNumbers().get(position)));
                        }
                        Defaults.storeContacts(itemView.getContext(), contactList);
                        mDialog.dismiss();
                    }
                });

                // Show the dialog
                mDialog.show();
            }
        });
    }
}
