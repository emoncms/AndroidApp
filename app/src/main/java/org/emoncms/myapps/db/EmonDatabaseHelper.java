package org.emoncms.myapps.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.emoncms.myapps.myelectric.MyElectricSettings;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Database class for accessing pages in database
 */
public class EmonDatabaseHelper extends SQLiteOpenHelper {

    private static EmonDatabaseHelper singleton;

    private static final String DATABASE_NAME = "emon.db";
    private static final int SCHEMA = 1;

    public synchronized static EmonDatabaseHelper getInstance(Context ctxt) {
        if (singleton == null) {
            singleton = new EmonDatabaseHelper(ctxt.getApplicationContext());
        }

        return (singleton);
    }

    public EmonDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("db","creating database!");

        db.execSQL("CREATE TABLE emon_page (id integer primary key, account TEXT, page_type TEXT, page_def TEXT)");

        /*
        //home
        ContentValues cvPage = new ContentValues();

        cvPage.put("account", "ac2daae5-b818-40b4-be56-b36a339508a0");
        cvPage.put("page_type", "me");
        cvPage.put("page_def", "{\"name\":\"Electric\", \"powerFeedId\":3,\"useFeedId\":4,\"unitCost\": 0.14,\"costSymbol\": \"£\"}");
        db.insert("emon_page", "na", cvPage);
        //paul

        cvPage.put("account", "fc39982e-3530-44ee-a360-6ef3bdc57a6e");
        cvPage.put("page_type", "me");
        cvPage.put("page_def", "{\"name\":\"Electric\", \"powerFeedId\":136162,\"useFeedId\":136164,\"unitCost\": 0.14,\"costSymbol\": \"£\"}");
        db.insert("emon_page", "na", cvPage);

        cvPage.put("account", "fc39982e-3530-44ee-a360-6ef3bdc57a6e");
        cvPage.put("page_type", "me");
        cvPage.put("page_def", "{\"name\":\"Solar\", \"powerFeedId\":136158,\"useFeedId\":136159,\"unitCost\": 0.14,\"costSymbol\": \"£\"}");
        db.insert("emon_page", "na", cvPage);

        cvPage.put("account", "fc39982e-3530-44ee-a360-6ef3bdc57a6e");
        cvPage.put("page_type", "me");
        cvPage.put("page_def", "{\"name\":\"Import\", \"powerFeedId\":136165,\"useFeedId\":136166,\"unitCost\": 0.14,\"costSymbol\": \"£\"}");
        db.insert("emon_page", "na", cvPage);
        */

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public List<MyElectricSettings> getPages(String account) {
        Log.d("db","getting pages for account " + account);
        List<MyElectricSettings> pageList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select id, page_def from emon_page where account = ?", new String[]{ account });
        try {
            if (cursor.moveToFirst()) {
                do {
                    Log.d("db","definition:  " + cursor.getString(1));
                    MyElectricSettings settings = MyElectricSettings.fromJson(cursor.getInt(0),new JSONObject(cursor.getString(1)));

                    pageList.add(settings);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("db", "Error while trying to get posts from database: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        Log.d("db","found accounts " + pageList.size());
        return pageList;
    }

    public void deletePage(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {

            db.delete("emon_page", "id = ? ", new String[] { String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("db", "Error deleting page");
        } finally {
            db.endTransaction();
        }
    }

    public void updatePage(int id, MyElectricSettings page) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cvPage = new ContentValues();

            cvPage.put("page_def", page.toJson());
            int rows = db.update("emon_page", cvPage, "id = ?",new String[] { String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("db", "Error updating page");
        } finally {
            db.endTransaction();
        }
    }

    public int addPage(String account, MyElectricSettings page) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int id = 0;
        try {
            Log.d("db","inserting account " + account);
            ContentValues cvPage = new ContentValues();
            cvPage.put("account", account);
            cvPage.put("page_type", "me");
            cvPage.put("page_def", page.toJson());

            db.insert("emon_page", "na", cvPage);
            id = getHighestID(db);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("db", "Error adding page");
        } finally {
            db.endTransaction();
        }
        return id;
    }

    public int getHighestID(SQLiteDatabase db) {
        final String sql = "SELECT last_insert_rowid()";
        Cursor cur = db.rawQuery(sql, null);
        cur.moveToFirst();
        int id = cur.getInt(0);
        cur.close();
        return id;
    }
}