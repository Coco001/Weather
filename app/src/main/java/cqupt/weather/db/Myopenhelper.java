package cqupt.weather.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Myopenhelper extends SQLiteOpenHelper {
    public Myopenhelper(Context context) {
        super(context, "china", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table Province (_id integer primary key autoincrement, code integer(10), name char(30))");
        db.execSQL("create table City (_id integer primary key autoincrement, code integer(10), name char(30), provinceId integer(10))");
        db.execSQL("create table County (_id integer primary key autoincrement, code integer(10), name char(30), weatherId char(30), cityId integer(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Province");
        db.execSQL("drop table if exists City");
        db.execSQL("drop table if exists County");
        onCreate(db);
    }
}
