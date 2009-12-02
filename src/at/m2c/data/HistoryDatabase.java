package at.m2c.data;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import at.m2c.util.Helper;


public class HistoryDatabase extends SQLiteOpenHelper {

    private static final String TAG = "HistoryDatabase";
	/** The name of the database file on the file system */
    private static final String DATABASE_NAME = "History";
    /** The version of the database that this class understands. */
    private static final int DATABASE_VERSION = 1;
    
    public HistoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "code TEXT,"
                + "time TEXT,"
                + "name TEXT,"
                + "image BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }
    
    public void AddEntry(ProductInfo product) {
    	if ((product == null) || (product.getProductName() == null) || (product.getProductName().equals(""))) return;
    	
    	SQLiteDatabase db = getWritableDatabase();
    	
    	db.delete("products", "code = '" + product.getProductCode() + "'", null);
        
    	Cursor cursor = db.rawQuery("SELECT * FROM products", null);
    	if (cursor.getCount() > 100) {
    		db.delete("products", "_id = (SELECT MIN(_id) FROM products)", null);
    	}
        
        ContentValues map = new ContentValues();
        
        map.put("code", product.getProductCode());
        map.put("time", new Date().toLocaleString());
        map.put("name", product.getProductName());
        
        Bitmap image = product.getProductImage();
        if (image != null) {
        	map.put("image", Helper.getBitmapAsByteArray(image));
        }
        
        try{
            db.insert("products", null, map);
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
    }
    
    public Cursor getHistory() {
    	SQLiteDatabase db = getWritableDatabase();
    	String query = "SELECT * FROM products ORDER BY _id DESC";
    	return db.rawQuery(query, null);
    }
    
    public void clearHistory() {
    	SQLiteDatabase db = getWritableDatabase();
    	db.delete("products", null, null);
    }
}
