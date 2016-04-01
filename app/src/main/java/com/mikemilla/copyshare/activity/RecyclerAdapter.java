package com.mikemilla.copyshare.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikemilla.copyshare.R;
import com.mikemilla.copyshare.data.Contact;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter {

    private List<Contact> mContactsList = new ArrayList<>();
    private MainActivity mMainActivity;

    public RecyclerAdapter(MainActivity activity, List<Contact> contactList) {
        super();
        mMainActivity = activity;
        mContactsList = contactList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {

        final ViewHolder item = (ViewHolder) viewHolder;

        item.imageView.setImageBitmap(mContactsList.get(i).getPicture());
        item.textView.setText(mContactsList.get(i).getName());

        // Example Click
        item.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMainActivity.getContactToShareWith() == mContactsList.get(i)) {
                    mMainActivity.setContactToShareWith(null);
                } else {
                    mMainActivity.setContactToShareWith(mContactsList.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContactsList.size();
    }

}
