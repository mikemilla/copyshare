package com.mikemilla.copyshare;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikemilla.copyshare.helper.ItemTouchHelperAdapter;
import com.mikemilla.copyshare.helper.ItemTouchHelperViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    private final List<String> mItems = new ArrayList<>();
    private final Context mContext;

    public RecyclerAdapter(Context context) {
        super();
        mContext = context;
        mItems.addAll(Arrays.asList(context.getResources().getStringArray(R.array.dummy_items)));
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        printOrder();
        return true;
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        LinearLayout container;
        CircleImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            container = (LinearLayout) itemView.findViewById(R.id.container);
            imageView = (CircleImageView) itemView.findViewById(R.id.contact_image);
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        final ViewHolder item = (ViewHolder) viewHolder;

        item.textView.setText(mItems.get(i));

        // Example Click
        item.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(item.itemView.getContext(), "Clicked item with name "
                        + item.textView.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void printOrder() {
        Log.d("Order", mItems.toString());
    }

}
