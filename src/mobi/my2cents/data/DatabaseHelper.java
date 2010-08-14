package mobi.my2cents.data;

import mobi.my2cents.My2Cents;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
    private static final String DATABASE_NAME = "My2Cents.db";
    private static final int DATABASE_VERSION = 29;
    
    
    public static final String COMMENTS_TABLE = "comments";
    private static final String CREATE_COMMENTS_TABLE =
    	"CREATE TABLE " + COMMENTS_TABLE + " ("
		
    	+ Comment.KEY + " INTEGER PRIMARY KEY, "
		+ Comment.BODY + " TEXT, "
		+ Comment.CREATED_AT + " BIGINT, "
		
		+ Comment.LATITUDE + " INTEGER, "
		+ Comment.LONGITUDE + " INTEGER, "
		
		+ Comment.PRODUCT_KEY + " TEXT, "
		+ Comment.PRODUCT_NAME + " TEXT, "
		+ Comment.PRODUCT_IMAGE_URL + " TEXT, "
		
		+ Comment.USER_KEY + " INTEGER, "
		+ Comment.USER_NAME + " TEXT, "
		+ Comment.USER_IMAGE_URL + " TEXT, "
		
		+ Comment.PENDING + " BOOLEAN "
		
		+ ");";
    
    
    public static final String PRODUCTS_TABLE = "products";
    private static final String CREATE_PRODUCTS_TABLE =
    	"CREATE TABLE " + PRODUCTS_TABLE + " ("
    	
    	+ Product.KEY + " TEXT UNIQUE, "
    	+ Product.GTIN + " TEXT, "
    	+ Product.NAME + " TEXT, "
    	+ Product.IMAGE_URL + " TEXT, "
    	
    	+ Product.AFFILIATE_NAME + " TEXT, "
    	+ Product.AFFILIATE_URL + " TEXT, "

    	+ Product.RATING_LIKES + " INTEGER, "
    	+ Product.RATING_DISLIKES + " INTEGER, "
    	+ Product.RATING_PERSONAL + " TEXT, "
    	
    	+ Product.PENDING + " BOOLEAN "

    	+ ");"
    	+ "CREATE INDEX products_gtin_index ON " + PRODUCTS_TABLE + "(" + Product.GTIN + ");";
    
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL(CREATE_PRODUCTS_TABLE);
    	db.execSQL(CREATE_COMMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(My2Cents.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMMENTS_TABLE);
        onCreate(db);
    }
}
