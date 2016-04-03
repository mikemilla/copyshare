package com.mikemilla.copyshare.lists;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mikemilla.copyshare.R;

import java.util.List;

/**
 * Created by Mike Miller on 4/3/16.
 * Because he needed it to be created for some reason
 */

public class ContactDialogAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    List<String> data = null;

    public ContactDialogAdapter(Context context, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        MeansHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new MeansHolder();
            holder.meansText = (TextView) row.findViewById(R.id.contact_means);

            row.setTag(holder);
        } else {
            holder = (MeansHolder) row.getTag();
        }

        holder.meansText.setText(data.get(position));

        return row;
    }

    static class MeansHolder {
        TextView meansText;
    }

    @Override
    public int getCount() {
        return data.size();
    }
}