package com.mikemilla.copyshare.lists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikemilla.copyshare.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/05/15
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {

    private final LayoutInflater mInflater;
    private final List<ContactModel> mModels;

    public ContactAdapter(Context context, List<ContactModel> models) {
        mInflater = LayoutInflater.from(context);
        mModels = new ArrayList<>(models);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.recycler_contact, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        final ContactModel model = mModels.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mModels.size();
    }

    public void animateTo(List<ContactModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ContactModel> newModels) {
        for (int i = mModels.size() - 1; i >= 0; i--) {
            final ContactModel model = mModels.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ContactModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ContactModel model = newModels.get(i);
            if (!mModels.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ContactModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ContactModel model = newModels.get(toPosition);
            final int fromPosition = mModels.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public ContactModel removeItem(int position) {
        final ContactModel model = mModels.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ContactModel model) {
        mModels.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ContactModel model = mModels.remove(fromPosition);
        mModels.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(mModels.get(pos).getName().charAt(0));
    }
}
