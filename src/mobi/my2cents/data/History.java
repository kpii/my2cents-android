package mobi.my2cents.data;

import java.util.HashMap;

import mobi.my2cents.My2Cents;

import android.net.Uri;
import android.provider.BaseColumns;

public final class History implements BaseColumns {
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + My2Cents.AUTHORITY + "/history");
	
	public static final String TIME				= "time";
	public static final String PRODUCT_KEY		= "product_key";
	public static final String NAME				= "name";
	public static final String AFFILIATE_NAME	= "affiliate_name";
	public static final String AFFILIATE_URL	= "affiliate_url";
	public static final String IMAGE			= "image";
	
	public static HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( History._ID,				History._ID);
		projectionMap.put( History.PRODUCT_KEY, 	History.PRODUCT_KEY);
		projectionMap.put( History.NAME, 			History.NAME);
		projectionMap.put( History.TIME, 			History.TIME);
		projectionMap.put( History.AFFILIATE_NAME,	History.AFFILIATE_NAME);
		projectionMap.put( History.AFFILIATE_URL, 	History.AFFILIATE_URL);
		projectionMap.put( History.IMAGE, 			History.IMAGE);
	}	
	
}
