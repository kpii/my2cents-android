package mobi.my2cents.data;

import mobi.my2cents.My2Cents;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
    private static final String DATABASE_NAME = "My2Cents.db";
    private static final int DATABASE_VERSION = 18;
    
    
    private static final String TRANSITION_FLAGS = 
    	Comment.TRANSITION_ACTIVE + " BOOLEAN, "
    	+ Comment.GET_TRANSITIONAL_STATE + " BOOLEAN, "
    	+ Comment.POST_TRANSITIONAL_STATE + " BOOLEAN, "
    	+ Comment.DEL_TRANSITIONAL_STATE + " BOOLEAN, "
    	+ Comment.PUT_TRANSITIONAL_STATE + " BOOLEAN ";
    
    
    public static final String COMMENTS_TABLE = "comments";
    private static final String CREATE_COMMENTS_TABLE =
    	"CREATE TABLE " + COMMENTS_TABLE + " ("
		+ Comment._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ Comment.KEY + " INTEGER UNIQUE, "
		+ Comment.URI + " TEXT, "
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
		
		+ TRANSITION_FLAGS + ");"
		+ "CREATE INDEX comments_key_index ON " + COMMENTS_TABLE + "(" + Comment.KEY + ");";
    
    
    public static final String PRODUCTS_TABLE = "products";
    private static final String CREATE_PRODUCTS_TABLE =
    	"CREATE TABLE " + PRODUCTS_TABLE + " ("
    	+ Product._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
    	+ Product.KEY + " TEXT, "
    	+ Product.URI + " TEXT, "
    	+ Product.NAME + " TEXT, "
    	+ Product.IMAGE + " BLOB, "
    	+ Product.ETAG + " TEXT, "
    	+ TRANSITION_FLAGS + ");";
    
    
    public static final String HISTORY_TABLE = "history";
    private static final String CREATE_HISTORY_TABLE =
    	"CREATE TABLE " + HISTORY_TABLE + " ("
        + History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + History.TIME + " TEXT, "
        + History.PRODUCT_KEY + " TEXT ,"
        + History.NAME + " TEXT, "
        + History.AFFILIATE_NAME + " TEXT, "
        + History.AFFILIATE_URL + " TEXT, "
        + History.IMAGE + " BLOB);";

    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	db.execSQL(CREATE_PRODUCTS_TABLE);
    	db.execSQL(CREATE_COMMENTS_TABLE);
    	db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(My2Cents.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COMMENTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }
}
