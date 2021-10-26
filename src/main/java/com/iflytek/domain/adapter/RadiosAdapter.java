package com.iflytek.domain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.myapp.R;

import java.util.List;

public class RadiosAdapter extends ArrayAdapter<Radios> {

    private int resourceId;

    public RadiosAdapter(@NonNull Context context, int resource, List<Radios> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Radios radios = getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = view.findViewById(R.id.radio_img);
            viewHolder.textView = view.findViewById(R.id.radio_title);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.imageView.setImageResource(radios.getImageId());
        viewHolder.textView.setText(radios.getTitle());
        return view;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
