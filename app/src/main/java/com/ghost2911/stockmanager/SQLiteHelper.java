package com.ghost2911.stockmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper(Context context) {
        // STOCK MANAGER DB
        super(context, "SMDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("logs", "--- СОЗДАНИЕ ТАБЛИЦЫ В БАЗЕ ---");
        // создаем таблицу с полями
        db.execSQL("create table products ("
                + "idProduct integer primary key autoincrement,"
                + "idRoot integer,"
                + "name text,"
                + "[desc] text,"
                + "price integer,"
                + "count integer,"
                + "barcode text,"
                + "storage text,"
                + "analog text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}