package com.iflytek.myapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 电子书的数据库类
 * 第一次创建数据库的时候会执行onCreate函数，若是数据库版本号升级则会执行onUpGrade函数
 * 该类是一个数据库类，继承自SqliteOpenHelper，数据库查询就需要通过这个类。
 *
 * 主要思路是：若是第一次创建数据库，就执行OnCreate函数，将assets目录下的所有书籍都存到数据库中，
 * 安卓自带一个轻量级数据库sqlite3，数据库的名称为book，一本书对应一张数据表，表名称为每本书的每个字首字母，
 * 在assets/book目录下可以看到newbook，里面的内容就是中文书名和其数据库名称的对应。之所以不直接用书名是怕
 * 数据库中文乱码，所以弄了个英文对应。在创建数据库的过程中就将中文书名和英文书名对应关系存储到本地了，以便
 * 电子书模块直接判断是否存在书籍，以及获取其对应的数据表table名。
 *
 * 代码逻辑比较简单，阅读一遍就知道。首先传入书名和数据库名的对应文件，也就是newbook或bookmap，初始创建数据库
 * 的时候执行的是newbook，若是升级数据库执行的就是bookmap。将里面的键值对存储起来，然后通过键将所有的电子书
 * 存储到数据库中，格式为：（id，title，content），id是章节（页码），title是章节的名称，content是内容。
 * 我放的两本电子书都经过预处理，每个章节都用####进行了分割，以便电子书的读取创建。
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {


    private Context mContext;

    //将书存在assets目录，需要用AssetManager打开assets的目录文件流
    AssetManager assets;

    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        assets = mContext.getAssets();
    }

    /**
     * 若数据库不存在，创建数据库，然后导入书籍
     *
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            initDataBase(sqLiteDatabase, "bookmap");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化数据库
     * 其中的map是为了以后增添新书用的，在assets目录下有一个book目录，
     * 里面有两个map，一个是bookmap一个是newbook，初始化用的是bookmap，
     * 以后增加新书使用newbook，将书名和拼音放在newbook中，然后在book目录下放入对应的书籍，就可以导入书籍了
     * @param sqLiteDatabase
     * @param map
     * @throws IOException
     */
    private void initDataBase(SQLiteDatabase sqLiteDatabase, String map) throws IOException {
        //需要将book的中文名和拼音存到SharedPreferences
        SharedPreferences.Editor editor = mContext.getSharedPreferences(map, Context.MODE_PRIVATE).edit();
        InputStream inputStream = assets.open("book/" + map);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] s = line.split(" ");
            editor.putString(s[0], s[1]);
            //创建书，传入书名和拼音
            createBook(sqLiteDatabase, s[0], s[1]);
        }
        reader.close();
        editor.apply();
    }

    /**
     * 书名用来读取txt，拼音用来插入数据库
     * @param sqLiteDatabase
     * @param book
     * @param bookName
     * @throws IOException
     */
    private void createBook(SQLiteDatabase sqLiteDatabase, String book, String bookName) throws IOException {
        String bookPath = "book" + "/" + book + ".txt";
        //创建该表
        String sql = "create table " + bookName + "(" +
                "id integer primary key autoincrement, " +
                "chapter text, " +
                "content text)";
        sqLiteDatabase.execSQL(sql);
        //填入内容
        fillBook(bookPath, bookName, sqLiteDatabase);
    }

    private void fillBook(String bookPath, String bookName, SQLiteDatabase sqLiteDatabase) throws IOException {
        InputStream inputStream = assets.open(bookPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String title = reader.readLine();
        StringBuilder content = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.equals("#####")) {
                String sql = "insert into " + bookName + "(chapter, content) values ('" + title + "', '" + content + "')";
                try {
                    //可能会有编码问题，防止出错
                    sqLiteDatabase.execSQL(sql);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                title = reader.readLine();
                if (title == null) break;
                content = new StringBuilder();
            } else {
                content.append(line);
            }
        }
        reader.close();
    }

    //需要新增书的时候就将版本号加一，插入newbook中的书的数据
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            initDataBase(sqLiteDatabase, "newbook");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Upgrade Failed", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(mContext, "Upgrade succeed", Toast.LENGTH_SHORT).show();
    }
}
