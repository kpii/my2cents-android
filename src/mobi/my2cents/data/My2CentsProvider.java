package mobi.my2cents.data;

import mobi.my2cents.My2Cents;
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

public class My2CentsProvider extends ContentProvider {
	
    private static final int PRODUCT_DIR = 1;
    private static final int PRODUCT_ITEM = 2;
    private static final int PRODUCT_COMMENTS = 3;
    
    private static final int COMMENT_DIR = 4;
    private static final int COMMENT_ITEM = 5;
    
    private static final UriMatcher uriMatcher;    
	private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    @Override
	public String getType(Uri uri) {
    	switch (uriMatcher.match(uri)) {    		
	    	case PRODUCT_DIR: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.product";
	    	}
	    		
	    	case PRODUCT_ITEM: {
	    		return "vnd.android.cursor.item/mobi.my2cents.product";
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.comment";
	    	}
	    	
	    	case COMMENT_DIR: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.comment";
	    	}
	    		
	    	case COMMENT_ITEM: {
	    		return "vnd.android.cursor.item/mobi.my2cents.comment";
	    	}
	    	
	    	default: {
	    		return null;
	    	}    	
    	}
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        
        switch (uriMatcher.match(uri)) {    		
	    	case PRODUCT_DIR: {
	    		count = db.delete(DatabaseHelper.PRODUCTS_TABLE, where, whereArgs);
	    		break;
	    	}
	    		
	    	case PRODUCT_ITEM: {
	    		String key = uri.getPathSegments().get(1);
	    		String whereClause = 
	    			Product.KEY + "=" + key
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.delete(DatabaseHelper.PRODUCTS_TABLE, whereClause, whereArgs);
	    		break;
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		break;
	    	}
	    	
	    	case COMMENT_DIR: {
	    		count = db.delete(DatabaseHelper.COMMENTS_TABLE, where, whereArgs);
	    		break;
	    	}
	    		
	    	case COMMENT_ITEM: {
	    		final String key = uri.getPathSegments().get(1);
	    		String whereClause = 
	    			Comment.KEY + "=" + key
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.delete(DatabaseHelper.COMMENTS_TABLE, whereClause, whereArgs);
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;

	}
	

	@Override
	public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        switch (uriMatcher.match(uri)) {    		
	    	case PRODUCT_DIR: {
//	    		values.put(Comment.GET_TRANSITIONAL_STATE, true);
	    		long rowId = db.replace(DatabaseHelper.PRODUCTS_TABLE, "", values);
	            if (rowId > 0) {	
	                final Uri eventUri = ContentUris.withAppendedId(Product.CONTENT_URI, rowId);
//	                getContext().getContentResolver().notifyChange(eventUri, null);
	                return eventUri;
	            }
	    		break;
	    	}
	    		
	    	case PRODUCT_ITEM: {
	    		break;
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		String key = uri.getPathSegments().get(1);
	    		values.put(Comment.TRANSITION_ACTIVE, false);
				values.put(Comment.PRODUCT_KEY, key);
	    		long rowId = db.replace(DatabaseHelper.COMMENTS_TABLE, "", values);
	            if (rowId > 0) {	
	                Uri eventUri = ContentUris.withAppendedId(Product.CONTENT_URI, rowId);
	                getContext().getContentResolver().notifyChange(eventUri, null);
	                return eventUri;
	            }
	    		break;
	    	}
	    	
	    	case COMMENT_DIR: {
	    		long rowId = db.replace(DatabaseHelper.COMMENTS_TABLE, "", values);
	            if (rowId > 0) {	
	                final Uri eventUri = ContentUris.withAppendedId(Comment.CONTENT_URI, rowId);
//	                getContext().getContentResolver().notifyChange(eventUri, null);
	                return eventUri;
	            }
	    		break;
	    	}
	    		
	    	case COMMENT_ITEM: {
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
	    }
        
        throw new SQLException("Failed to insert row into " + uri);
        
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		// If no sort order is specified use the default
        String orderBy = sortOrder;
		
		switch (uriMatcher.match(uri)) {    		
	    	case PRODUCT_DIR: {
	    		qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            orderBy = Product._ID + " DESC";
	    		break;
	    	}
	    		
	    	case PRODUCT_ITEM: {
	    		final String key = uri.getLastPathSegment();
	        	qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            qb.appendWhere(Product.KEY + "='" + key + "'");
	    		break;
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		final String key = uri.getPathSegments().get(1);
				qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	            qb.appendWhere(Comment.PRODUCT_KEY + "='" + key + "'");
	    		break;
	    	}
	    	
	    	case COMMENT_DIR: {
	    		qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	            orderBy = Comment.KEY + " DESC";
	    		break;
	    	}
	    		
	    	case COMMENT_ITEM: {
	    		qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
	    }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        
        switch (uriMatcher.match(uri)) {    		
	    	case PRODUCT_DIR: {
	    		break;
	    	}
	    		
	    	case PRODUCT_ITEM: {
	    		String key = uri.getPathSegments().get(1);
	    		String whereClause = 
	    			Product.KEY + "=" + key
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.update(DatabaseHelper.PRODUCTS_TABLE, values, whereClause, whereArgs);
	    		break;
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		break;
	    	}
	    	
	    	case COMMENT_DIR: {
	    		break;
	    	}
	    		
	    	case COMMENT_ITEM: {
	    		final String key = uri.getPathSegments().get(1);
	    		String whereClause = 
	    			Comment.KEY + "=" + key
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.update(DatabaseHelper.COMMENTS_TABLE, values, whereClause, whereArgs);
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
	    }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
        
	}
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        
        uriMatcher.addURI(My2Cents.AUTHORITY, "products", PRODUCT_DIR);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/*", PRODUCT_ITEM);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/*/comments", PRODUCT_COMMENTS);
        
        uriMatcher.addURI(My2Cents.AUTHORITY, "comments", COMMENT_DIR);
        uriMatcher.addURI(My2Cents.AUTHORITY, "comments/*", COMMENT_ITEM);
	}
}
