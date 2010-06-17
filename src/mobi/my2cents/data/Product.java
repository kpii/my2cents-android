package mobi.my2cents.data;

import java.util.HashMap;

import mobi.my2cents.My2Cents;

import android.net.Uri;
import android.provider.BaseColumns;

public class Product implements BaseColumns, TransitionalStateColumns{

	public static final Uri CONTENT_URI = Uri.parse("content://" + My2Cents.AUTHORITY + "/products");
	
	public static final String KEY 			= "key";
	public static final String NAME 		= "name";
	public static final String IMAGE 		= "image";
	public static final String URI 			= "uri";
	public static final String ETAG  		= "etag";
	
	public static final HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( Product._ID,							Product._ID);
		projectionMap.put( Product.KEY, 						Product.KEY);
		projectionMap.put( Product.IMAGE, 						Product.IMAGE);
		projectionMap.put( Product.NAME, 						Product.NAME);
		projectionMap.put( Product.URI, 						Product.URI);
		projectionMap.put( Product.TRANSITION_ACTIVE,			Product.TRANSITION_ACTIVE);
		projectionMap.put( Product.POST_TRANSITIONAL_STATE,		Product.POST_TRANSITIONAL_STATE);
		projectionMap.put( Product.GET_TRANSITIONAL_STATE, 		Product.GET_TRANSITIONAL_STATE);
		projectionMap.put( Product.PUT_TRANSITIONAL_STATE, 		Product.PUT_TRANSITIONAL_STATE);
		projectionMap.put( Product.DEL_TRANSITIONAL_STATE, 		Product.DEL_TRANSITIONAL_STATE);
	}

}
