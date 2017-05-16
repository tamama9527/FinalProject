package devlight.io.sample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zeus on 2017/5/12.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    private static SQLiteDatabase database;
    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists info  ( id INTEGER PRIMARY KEY AUTOINCREMENT ,account VARCHAR(50) NOT NULL , password VARCHAR(50) NOT NULL)");
        db.execSQL("CREATE TABLE if not exists message  ( id INTEGER PRIMARY KEY AUTOINCREMENT ,user_from VARCHAR(50),mes_group VARCHAR(50) , text TEXT,date DATETIME NOT NULL)");
        db.execSQL("CREATE TABLE if not exists friend  ( id INTEGER PRIMARY KEY AUTOINCREMENT ,name VARCHAR(50) NOT NULL , token VARCHAR(50) NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
