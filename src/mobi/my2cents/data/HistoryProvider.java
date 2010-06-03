package mobi.my2cents.data;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class HistoryProvider extends ContentProvider {

    public static final String DEFAULT_SORT_ORDER = History.TIME + " DESC";
	
    private static final int ITEMS = 1;
    private static final int ITEMS_GTIN = 2;
    
    private static final UriMatcher uriMatcher;
    
	private DatabaseHelper dbHelper;
	private static HashMap<String, String> projectionMap;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
        case ITEMS:
            count = db.delete(DatabaseHelper.HISTORY_TABLE, where, whereArgs);
            break;
            
        case ITEMS_GTIN:
            String gtin = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.HISTORY_TABLE, History.GTIN + "=" + gtin
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;

	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(DatabaseHelper.HISTORY_TABLE, "", values);
        if (rowId > 0) {	
            Uri eventUri = ContentUris.withAppendedId(History.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(eventUri, null);
            return eventUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DatabaseHelper.HISTORY_TABLE);
        qb.setProjectionMap(projectionMap);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		// If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
	        case ITEMS:
	            //Currently not needed!
	            break;
	
	        case ITEMS_GTIN:
	            String gtin = uri.getPathSegments().get(1);
	            count = db.update(DatabaseHelper.HISTORY_TABLE, values, History.GTIN + "=" + gtin
	            		+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(History.AUTHORITY, "history", ITEMS);
        uriMatcher.addURI(History.AUTHORITY, "history/*", ITEMS_GTIN);
	
		projectionMap = new HashMap<String, String>();
		projectionMap.put( History._ID,				History._ID);
		projectionMap.put( History.GTIN, 			History.GTIN);
		projectionMap.put( History.NAME, 			History.NAME);
		projectionMap.put( History.TIME, 			History.TIME);
		projectionMap.put( History.AFFILIATE_NAME,	History.AFFILIATE_NAME);
		projectionMap.put( History.AFFILIATE_URL, 	History.AFFILIATE_URL);
		projectionMap.put( History.IMAGE, 			History.IMAGE);
	}
}
