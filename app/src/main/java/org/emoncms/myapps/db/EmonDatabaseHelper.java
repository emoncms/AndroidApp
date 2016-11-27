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

    private static EmonDatabaseHelper instance;

    private static final String DATABASE_NAME = "emon.db";
    private static final int SCHEMA = 1;

    public synchronized static EmonDatabaseHelper getInstance(Context ctxt) {
        if (instance == null) {
            instance = new EmonDatabaseHelper(ctxt.getApplicationContext());
        }

        return (instance);
    }

    public EmonDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE emon_page (id integer primary key, account TEXT, page_type TEXT, page_def TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public List<MyElectricSettings> getPages(String account) {

        List<MyElectricSettings> pageList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select id, page_def from emon_page where account = ? order by id", new String[]{ account });
        try {
            if (cursor.moveToFirst()) {
                do {

                    MyElectricSettings settings = MyElectricSettings.fromJson(cursor.getInt(0),new JSONObject(cursor.getString(1)));

                    pageList.add(settings);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("emon", "Error while trying to get posts from database: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return pageList;
    }

    public void deletePage(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {

            db.delete("emon_page", "id = ? ", new String[] { String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("emon", "Error deleting page");
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