package at.my2c.data;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import at.my2c.util.Helper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
	/** The name of the database file on the file system */
    private static final String DATABASE_NAME = "My2CentsDb";
    /** The version of the database that this class understands. */
    private static final int DATABASE_VERSION = 2;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {        
        db.execSQL("CREATE TABLE history ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "productCode TEXT,"
                + "time TEXT,"
                + "name TEXT,"
                + "manufacturer TEXT,"
                + "image BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }
    
    
    public void addHistoryItem(ProductInfo product) {
    	if ((product == null) || (product.getProductCode() == null) || (product.getProductCode().equals(""))) return;
    	
    	SQLiteDatabase db = getWritableDatabase();
    	
    	db.delete("history", "productCode = '" + product.getProductCode() + "'", null);
    	
    	Cursor cursor = db.rawQuery("SELECT * FROM history", null);
    	if (cursor.getCount() > 100) {
    		db.delete("history", "_id = (SELECT MIN(_id) FROM history)", null);
    	}
    	cursor.close();
        
        ContentValues map = new ContentValues();
        
        map.put("productCode", product.getProductCode());
        map.put("time", new Date().toLocaleString());
        
        if (product.getProductName() != null) {
        	map.put("name", product.getProductName());
        }
        
        Bitmap image = product.getProductImage();
        if (image != null) {
        	map.put("image", Helper.getBitmapAsByteArray(image));
        }
        
        try{
            db.insert("history", null, map);
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
    }
    
    public Cursor getHistory() {
    	SQLiteDatabase db = getWritableDatabase();
    	String query = "SELECT * FROM history ORDER BY _id DESC";
    	return db.rawQuery(query, null);
    }
    
    public void clearHistory() {
    	SQLiteDatabase db = getWritableDatabase();
    	db.delete("history", null, null);
    }
}
