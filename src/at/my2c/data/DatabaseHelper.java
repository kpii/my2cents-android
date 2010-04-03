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
import at.my2c.utils.Helper;


public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "DatabaseHelper";
	
	/** The name of the database file on the file system */
    private static final String DATABASE_NAME = "My2CentsDb";
    /** The version of the database that this class understands. */
    private static final int DATABASE_VERSION = 5;
    
    private static final String HISTORY_TABLE = "history";
    
    private static final int historyLimit = 100;

    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {        
        db.execSQL("CREATE TABLE " + HISTORY_TABLE + " ("
                + HistoryColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HistoryColumns.TIME + " TEXT,"
                + HistoryColumns.PRODUCT_ID + " INTEGER,"
                + HistoryColumns.GTIN + " TEXT,"
                + HistoryColumns.NAME + " TEXT,"
                + HistoryColumns.MANUFACTURER + " TEXT,"
                + HistoryColumns.IMAGE + " BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }
    
    
    public void addHistoryItem(ProductInfo product) {
    	if ((product == null) || (product.getGtin() == null) || (product.getGtin().equals(""))) return;
    	
    	SQLiteDatabase db = getWritableDatabase();
    	
    	db.delete(HISTORY_TABLE, HistoryColumns.GTIN + " = '" + product.getGtin() + "'", null);
    	
    	Cursor cursor = db.rawQuery("SELECT * FROM " + HISTORY_TABLE, null);
    	if (cursor.getCount() > historyLimit) {
    		db.delete(HISTORY_TABLE, HistoryColumns.ID + " = (SELECT MIN(" + HistoryColumns.ID + ") FROM " + HISTORY_TABLE + ")", null);
    	}
    	cursor.close();
        
        ContentValues map = new ContentValues();
        
        map.put(HistoryColumns.GTIN, product.getGtin());
        map.put(HistoryColumns.TIME, new Date().toLocaleString());
        
        String name = product.getName();
        if ((name != null) && (!name.equals(""))) {
        	map.put(HistoryColumns.NAME, name);
        }
        else {
        	map.put(HistoryColumns.NAME, DataManager.UnknownProductName);
        }
        
        Bitmap image = product.getImage();
        if (image != null) {
        	map.put(HistoryColumns.IMAGE, Helper.getBitmapAsByteArray(image));
        }
        
        try{
            db.insert(HISTORY_TABLE, null, map);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
    public Cursor getHistory() {
    	SQLiteDatabase db = getReadableDatabase();
    	String query = "SELECT * FROM " + HISTORY_TABLE + " ORDER BY " + HistoryColumns.ID + " DESC";
    	return db.rawQuery(query, null);
    }
    
    public void clearHistory() {
    	SQLiteDatabase db = getWritableDatabase();
    	db.delete(HISTORY_TABLE, null, null);
    }
    
    public ProductInfo getCachedProductInfo(String gtin) {
    	SQLiteDatabase db = getReadableDatabase();
    	String query = "SELECT * FROM " + HISTORY_TABLE + " WHERE " + HistoryColumns.GTIN + " = '" + gtin + "'";
    	Cursor cursor = db.rawQuery(query, null);
    	
    	ProductInfo productInfo = null;
    	if (cursor.moveToFirst()) {
	    	productInfo = new ProductInfo(cursor.getString(cursor.getColumnIndex(HistoryColumns.GTIN)));
	    	productInfo.setName(cursor.getString(cursor.getColumnIndex(HistoryColumns.NAME)));
	    	productInfo.setManufacturer(cursor.getString(cursor.getColumnIndex(HistoryColumns.MANUFACTURER)));
	    	
	    	byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(HistoryColumns.IMAGE));
			if (bitmapArray != null) {
				Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
				productInfo.setImage(bitmap);
			}
    	}
    	cursor.close();
		return productInfo;
    }
}
