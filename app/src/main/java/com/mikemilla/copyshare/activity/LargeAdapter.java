package com.mikemilla.copyshare.activity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public final class LargeAdapter extends RecyclerView.Adapter<LargeAdapter.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    //private final List<String> items;
    private final List<Contact> contacts;

    /*
    public LargeAdapter(int numberOfItems, List<Contact> contacts) {
        List<String> items = new ArrayList<>();
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < numberOfItems; i++)
            items.add(((char) ('A' + r.nextInt('Z' - 'A'))) + " " + Integer.toString(i));
        java.util.Collections.sort(items);
        this.items = items;
    }
    */

    public LargeAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (contacts.get(position).getPicture() != null) {
            Uri image = Uri.parse(contacts.get(position).getPicture());
            holder.setImage(image);
        } else {
            holder.setEmptyImage();
        }

        String text = contacts.get(position).getName();
        holder.setText(text);
    }

    @Override
    public String getTextToShowInBubble(final int pos) {
        return Character.toString(contacts.get(pos).getName().charAt(0));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView imageView;
        private final TextView textView;
        private final Context context;

        private ViewHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            this.imageView = (CircleImageView) itemView.findViewById(R.id.contact_image);
            this.textView = (TextView) itemView.findViewById(R.id.text_view);
        }

        public void setImage(Uri uri) {
            imageView.setImageURI(uri);
        }

        public void setEmptyImage() {
            imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_report_image));
        }

        public void setText(CharSequence text) {
            textView.setText(text);
        }
    }
}