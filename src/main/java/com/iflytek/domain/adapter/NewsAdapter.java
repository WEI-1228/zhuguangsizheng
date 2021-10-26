package com.iflytek.domain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iflytek.domain.MyView.MyImageView;
import com.iflytek.myapp.R;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {
    int resource_with_img;
    int resource_without_img;

    public NewsAdapter(@NonNull Context context, int resource_with_img, int resource_without_img, List<News> objects) {
        super(context, resource_with_img, objects);
        this.resource_with_img = resource_with_img;
        this.resource_without_img = resource_without_img;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        News news = getItem(position);
        View view;
        if (news.getHavePic()) {
            view = LayoutInflater.from(getContext()).inflate(resource_with_img, parent, false);

            TextView title_textview = view.findViewById(R.id.tx_news_simple_photos_title);
            MyImageView imageView = view.findViewById(R.id.tx_news_simple_photos_photo);
            TextView date_textview = view.findViewById(R.id.tx_news_simple_photos_time);
            TextView source_textview = view.findViewById(R.id.img_news_simple_photos_author);

            title_textview.setText(news.getTitle());
            imageView.setImageURL(news.getImageUrl());
            date_textview.setText(news.getDate());
            source_textview.setText(news.getSource());
        } else {
            view = LayoutInflater.from(getContext()).inflate(resource_without_img, parent, false);

            TextView title_text = view.findViewById(R.id.tx_news_simple_photos_title_without_img);
            TextView date_text = view.findViewById(R.id.tx_news_simple_photos_time_without_img);
            TextView source_text = view.findViewById(R.id.img_news_simple_photos_author_without_img);

            title_text.setText(news.getTitle());
            date_text.setText(news.getDate());
            source_text.setText(news.getSource());
        }

        return view;
    }
}
