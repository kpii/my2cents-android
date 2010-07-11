package mobi.my2cents.data;

import java.util.HashMap;

import mobi.my2cents.My2Cents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class Product implements BaseColumns, TransitionalStateColumns{

	public static final Uri CONTENT_URI = Uri.parse("content://" + My2Cents.AUTHORITY + "/products");
	
	public static final String KEY 				= "key";
	public static final String GTIN 			= "gtin";
	public static final String NAME 			= "name";
	public static final String IMAGE_URL 		= "image_url";
	
	public static final String AFFILIATE_NAME 	= "affiliate_name";
	public static final String AFFILIATE_URL 	= "affiliate_url";
	
	public static final String RATING_LIKES 	= "rating_likes";
	public static final String RATING_DISLIKES 	= "rating_dislikes";
	public static final String RATING_PERSONAL 	= "rating_personal";
	
	
	public static final HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( Product._ID,							"rowid AS " + Product._ID);
		
		projectionMap.put( Product.KEY, 						Product.KEY);
		projectionMap.put( Product.GTIN, 						Product.GTIN);
		projectionMap.put( Product.NAME, 						Product.NAME);
		projectionMap.put( Product.IMAGE_URL, 					Product.IMAGE_URL);
		
		projectionMap.put( Product.AFFILIATE_NAME, 				Product.AFFILIATE_NAME);
		projectionMap.put( Product.AFFILIATE_URL, 				Product.AFFILIATE_URL);
		
		projectionMap.put( Product.RATING_LIKES, 				Product.RATING_LIKES);
		projectionMap.put( Product.RATING_DISLIKES, 			Product.RATING_DISLIKES);
		projectionMap.put( Product.RATING_PERSONAL, 			Product.RATING_PERSONAL);

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
		
		if (!json.isNull("image_url"))
			values.put(Product.IMAGE_URL, json.getString("image_url"));
		
		
		if (json.has("affiliates")) {
			final JSONArray affiliates = json.getJSONArray("affiliates");
			if (affiliates.length() > 0) {
				final JSONObject affiliate = affiliates.getJSONObject(0);
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
		
		return values;
	}
	
	public final static JSONObject getRatingJson(Cursor cursor) {		
    	try {
    		final JSONObject jsonRating = new JSONObject();
    		jsonRating.put("value", cursor.getString(cursor.getColumnIndex(Product.RATING_PERSONAL)));
			
			final JSONObject json = new JSONObject();
			json.put("rating", jsonRating);
			return json;
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.toString());
			return null;
		}
	}
	
	public final static JSONObject getScanJson(String gtin) {		
    	try {
    		final JSONObject jsonScan = new JSONObject();
    		jsonScan.put("gtin", gtin);
			
			final JSONObject json = new JSONObject();
			json.put("scan", jsonScan);
			return json;
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.toString());
			return null;
		}
	}
	
}
