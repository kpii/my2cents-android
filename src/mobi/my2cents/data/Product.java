package mobi.my2cents.data;

import java.util.HashMap;

import mobi.my2cents.My2Cents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class Product implements BaseColumns, TransitionalStateColumns{

	public static final Uri CONTENT_URI = Uri.parse("content://" + My2Cents.AUTHORITY + "/products");
	
	public static final String KEY 				= "key";
	public static final String NAME 			= "name";
	public static final String IMAGE_URL 		= "image_url";
	
	public static final String AFFILIATE_NAME 	= "affiliate_name";
	public static final String AFFILIATE_URL 	= "affiliate_url";
	
	public static final String RATING_LIKES 	= "rating_likes";
	public static final String RATING_DISLIKES 	= "rating_dislikes";
	public static final String RATING_PERSONAL 	= "rating_personal";
	
	public static final String IMAGE 			= "image";
	public static final String URI 				= "uri";
	public static final String ETAG  			= "etag";
	
	
	public static final HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( Product._ID,							Product._ID);
		projectionMap.put( Product.KEY, 						Product.KEY);
		projectionMap.put( Product.NAME, 						Product.NAME);
		projectionMap.put( Product.IMAGE_URL, 					Product.IMAGE_URL);
		
		projectionMap.put( Product.AFFILIATE_NAME, 				Product.AFFILIATE_NAME);
		projectionMap.put( Product.AFFILIATE_URL, 				Product.AFFILIATE_URL);
		
		projectionMap.put( Product.RATING_LIKES, 				Product.RATING_LIKES);
		projectionMap.put( Product.RATING_DISLIKES, 			Product.RATING_DISLIKES);
		projectionMap.put( Product.RATING_PERSONAL, 			Product.RATING_PERSONAL);
		
		projectionMap.put( Product.IMAGE, 						Product.IMAGE);		
		projectionMap.put( Product.URI, 						Product.URI);
		projectionMap.put( Product.ETAG, 						Product.ETAG);
		projectionMap.put( Product.TRANSITION_ACTIVE,			Product.TRANSITION_ACTIVE);
		projectionMap.put( Product.POST_TRANSITIONAL_STATE,		Product.POST_TRANSITIONAL_STATE);
		projectionMap.put( Product.GET_TRANSITIONAL_STATE, 		Product.GET_TRANSITIONAL_STATE);
		projectionMap.put( Product.PUT_TRANSITIONAL_STATE, 		Product.PUT_TRANSITIONAL_STATE);
		projectionMap.put( Product.DEL_TRANSITIONAL_STATE, 		Product.DEL_TRANSITIONAL_STATE);
	}
	
	
	public final static ContentValues ParseJson(JSONObject json) throws JSONException {
		
		if (json == null) return null;
		
		ContentValues values = new ContentValues();
		
		values.put(Product.KEY, json.getString("key"));
		values.put(Product.NAME, json.getString("name"));
		values.put(Product.IMAGE_URL, json.getString("image_url"));
		
		
		if (json.has("affiliates")) {
			JSONArray jsonLinks = json.getJSONArray("affiliates");
			if (jsonLinks.length() > 0) {
				JSONObject affiliate = jsonLinks.getJSONObject(0);
				values.put(Product.AFFILIATE_NAME, affiliate.getString("text"));
				values.put(Product.AFFILIATE_URL, affiliate.getString("href"));
			}
		}
		
		if (json.has("rating")) {
			JSONObject rating = json.getJSONObject("rating");
			if (rating != null) {
				values.put(Product.RATING_LIKES, rating.getInt("likes"));
				values.put(Product.RATING_DISLIKES, rating.getInt("dislikes"));
				if (rating.has("me")) {
					values.put(Product.RATING_PERSONAL, rating.getString("me"));
				}
			}
		}
	    
//	    JSONArray jsonComments = jsonProduct.getJSONArray("comments");
//	    for (int i=0; i<jsonComments.length(); i++) {
//	    	JSONObject jsonComment = jsonComments.getJSONObject(i);
//	    	Comment comment = Json2Comment(jsonComment);
//			if (comment != null) {
//				productInfo.getComments().add(comment);
//			}
//	    }
		
		return values;
	}
	
}
