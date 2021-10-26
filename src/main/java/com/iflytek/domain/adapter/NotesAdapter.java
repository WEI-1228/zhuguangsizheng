package com.iflytek.domain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iflytek.myapp.R;

import java.util.List;

public class NotesAdapter extends ArrayAdapter<Notes> {

    int resourceId;

    public NotesAdapter(@NonNull Context context, int resource, List<Notes> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        Notes item = getItem(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.note_title_text);
            viewHolder.date = view.findViewById(R.id.note_date_text);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.date.setText(item.getDate());
        viewHolder.title.setText(item.getTitle());
        return view;
    }

    class ViewHolder {
        TextView title;
        TextView date;
    }
}
