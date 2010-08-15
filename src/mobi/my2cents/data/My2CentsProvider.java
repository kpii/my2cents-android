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
	
    private static final int PRODUCTS = 1;
    private static final int PRODUCT_ID = 2;
    private static final int PRODUCT_KEY = 3;
    private static final int PRODUCT_GTIN = 4;    
    private static final int PRODUCT_COMMENTS = 5;
    private static final int PRODUCTS_PENDING = 6;
    
    private static final int COMMENTS = 7;
    private static final int COMMENT_ID = 8;
    private static final int COMMENTS_PENDING = 9;
    
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
	    	case PRODUCTS: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.product";
	    	}
	    	
	    	case PRODUCTS_PENDING: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.product";
	    	}
	    		
	    	case PRODUCT_KEY: {
	    		return "vnd.android.cursor.item/mobi.my2cents.product";
	    	}
	    	
	    	case PRODUCT_GTIN: {
	    		return "vnd.android.cursor.item/mobi.my2cents.product";
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.comment";
	    	}
	    	
	    	case COMMENTS: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.comment";
	    	}
	    		
	    	case COMMENT_ID: {
	    		return "vnd.android.cursor.item/mobi.my2cents.comment";
	    	}
	    	
	    	case COMMENTS_PENDING: {
	    		return "vnd.android.cursor.dir/mobi.my2cents.comment";
	    	}
	    	
	    	default: {
	    		return null;
	    	}    	
    	}
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        
        switch (uriMatcher.match(uri)) {    		
	    	case PRODUCTS: {
	    		count = db.delete(DatabaseHelper.PRODUCTS_TABLE, where, whereArgs);
	    		break;
	    	}
	    		
	    	case PRODUCT_KEY: {
	    		final String key = uri.getLastPathSegment();
	    		final String whereClause = 
	    			Product.KEY + "=" + key
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.delete(DatabaseHelper.PRODUCTS_TABLE, whereClause, whereArgs);
	    		break;
	    	}
	    	
	    	case COMMENTS: {
	    		count = db.delete(DatabaseHelper.COMMENTS_TABLE, where, whereArgs);
	    		break;
	    	}
	    		
	    	case COMMENT_ID: {
	    		final String id = uri.getLastPathSegment();
	    		final String whereClause = 
	    			"rowid = " + id
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

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        switch (uriMatcher.match(uri)) {    		
	    	case PRODUCTS: {
	    		long rowId = db.replace(DatabaseHelper.PRODUCTS_TABLE, "", values);
	            if (rowId > 0) {	
	                final Uri resultUri = ContentUris.withAppendedId(Product.CONTENT_URI, rowId);
//	                getContext().getContentResolver().notifyChange(resultUri, null);
	                return resultUri;
	            }
	    		break;
	    	}
	    	
	    	case COMMENTS: {
	    		long rowId = db.replace(DatabaseHelper.COMMENTS_TABLE, "", values);
	            if (rowId > 0) {	
	                final Uri resultUri = ContentUris.withAppendedId(Comment.CONTENT_URI, rowId);
//	                getContext().getContentResolver().notifyChange(resultUri, null);
	                return resultUri;
	            }
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
	    }
        
        throw new SQLException("Failed to insert row into: " + uri);
        
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		// If no sort order is specified use the default
        String orderBy = sortOrder;
		
		switch (uriMatcher.match(uri)) {    		
	    	case PRODUCTS: {
	    		qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            orderBy = Product._ID + " DESC";
	    		break;
	    	}
	    	
	    	case PRODUCTS_PENDING: {
	    		qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            qb.appendWhere(Product.PENDING + "=1");
	    		break;
	    	}
	    		
	    	case PRODUCT_KEY: {
	    		final String key = uri.getLastPathSegment();
	        	qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            qb.appendWhere(Product.KEY + "='" + key + "'");
	    		break;
	    	}
	    	
	    	case PRODUCT_GTIN: {
	    		final String gtin = uri.getLastPathSegment();
	        	qb.setTables(DatabaseHelper.PRODUCTS_TABLE);
	            qb.setProjectionMap(Product.projectionMap);
	            qb.appendWhere(Product.GTIN + "='" + gtin + "'");
	    		break;
	    	}
	    	
	    	case PRODUCT_COMMENTS: {
	    		final String key = uri.getPathSegments().get(2);
				qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	            qb.appendWhere(Comment.PRODUCT_KEY + "='" + key + "'");
	            orderBy = Comment.KEY + " DESC";
	    		break;
	    	}
	    	
	    	case COMMENTS: {
	    		qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	            orderBy = Comment.KEY + " DESC";
	    		break;
	    	}
	    		
	    	case COMMENT_ID: {
	    		qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	    		break;
	    	}
	    	
	    	case COMMENTS_PENDING: {
	    		qb.setTables(DatabaseHelper.COMMENTS_TABLE);
	            qb.setProjectionMap(Comment.projectionMap);
	            qb.appendWhere(Comment.PENDING + "=1");
	    		break;
	    	}
	    	
	    	default: {
	    		throw new IllegalArgumentException("Unknown URI: " + uri);
	    	}
	    }

        final Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        
        switch (uriMatcher.match(uri)) {    		
	    		
	    	case PRODUCT_KEY: {
	    		final String key = uri.getLastPathSegment();
	    		final String whereClause = 
	    			Product.KEY + "='" + key + "'"
	    			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
	            count = db.update(DatabaseHelper.PRODUCTS_TABLE, values, whereClause, whereArgs);
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
        
        uriMatcher.addURI(My2Cents.AUTHORITY, "products", PRODUCTS);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/#", PRODUCT_ID);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/pending", PRODUCTS_PENDING);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/key/*", PRODUCT_KEY);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/gtin/*", PRODUCT_GTIN);
        uriMatcher.addURI(My2Cents.AUTHORITY, "products/key/*/comments", PRODUCT_COMMENTS);
        
        uriMatcher.addURI(My2Cents.AUTHORITY, "comments", COMMENTS);
        uriMatcher.addURI(My2Cents.AUTHORITY, "comments/pending", COMMENTS_PENDING);
        uriMatcher.addURI(My2Cents.AUTHORITY, "comments/#", COMMENT_ID);
	}
}
