package com.iflytek.myapp.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 学四史的数据库类，与电子书的类差不多，只是四史的每个文档格式不一样，并且写死了内容。
 */
public class StudyDatabaseHelper extends SQLiteOpenHelper {

    private Context mContext;
    Map<String, String> map;

    //将书存在assets目录，需要用AssetManager打开assets的目录文件流
    AssetManager assets;

    public StudyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        assets = mContext.getAssets();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        setParam();
        try {
            initDatabases(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDatabases(SQLiteDatabase db) throws IOException {
        String[] list = assets.list("sishi");
        for (String dir : list) {
            createBookDatebase(dir, db);
        }
    }

    private void createBookDatebase(String dir, SQLiteDatabase db) throws IOException {
        String sql = "create table " + map.get(dir) + "(" +
                "id integer primary key, " +
                "title text, " +
                "content text)";
        db.execSQL(sql);
        String[] list = assets.list("sishi/" + dir);
        for (String chapter : list) {
            makeChapter("sishi", dir, chapter, db);
        }
    }

    private void makeChapter(String dir, String type, String chapter, SQLiteDatabase db) throws IOException {
        String[] split = chapter.split("\\.");
        String id = split[0];
        String title = split[1];
        BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(dir + "/" + type + "/" + chapter)));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        String sql = "insert into " + map.get(type) + "(id, title, content) values (" + id + ", '" + title + "', '" + builder + "')";
        db.execSQL(sql);
    }

    private void setParam() {
        map = new HashMap<>();
        map.put("党史", "dangshi");
        map.put("新中国史", "xinzhongguoshi");
        map.put("改革开放史", "gaigekaifangshi");
        map.put("社会主义发展史", "shehuizhuyifazhanshi");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
