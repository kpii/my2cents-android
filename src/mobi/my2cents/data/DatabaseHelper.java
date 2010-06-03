package mobi.my2cents.data;

import java.util.Date;

import mobi.my2cents.utils.Helper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "DatabaseHelper";
	
    private static final String DATABASE_NAME = "My2CentsDb";
    /** The version of the database that this class understands. */
    private static final int DATABASE_VERSION = 9;
    
    public static final String HISTORY_TABLE = "history";
    private static final String CREATE_HISTORY_TABLE =
    	"CREATE TABLE " + HISTORY_TABLE + " ("
        + History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + History.TIME + " TEXT,"
        + History.GTIN + " TEXT,"
        + History.NAME + " TEXT,"
        + History.AFFILIATE_NAME + " TEXT,"
        + History.AFFILIATE_URL + " TEXT,"
        + History.IMAGE + " BLOB);";
    
    private static final int historyLimit = 100;

    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {        
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }
    
    public ProductInfo getCachedProductInfo(String gtin) {
    	SQLiteDatabase db = getReadableDatabase();
    	String query = "SELECT * FROM " + HISTORY_TABLE + " WHERE " + History.GTIN + " = '" + gtin + "'";
    	Cursor cursor = db.rawQuery(query, null);
    	
    	ProductInfo productInfo = null;
    	if (cursor.moveToFirst()) {
	    	productInfo = new ProductInfo(cursor.getString(cursor.getColumnIndex(History.GTIN)));
	    	productInfo.setName(cursor.getString(cursor.getColumnIndex(History.NAME)));
	    	productInfo.setAffiliateName(cursor.getString(cursor.getColumnIndex(History.AFFILIATE_NAME)));
	    	productInfo.setAffiliateUrl(cursor.getString(cursor.getColumnIndex(History.AFFILIATE_URL)));
	    	
	    	byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(History.IMAGE));
			if (bitmapArray != null) {
				Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
				productInfo.setImage(bitmap);
			}
    	}
    	cursor.close();
		return productInfo;
    }
}
