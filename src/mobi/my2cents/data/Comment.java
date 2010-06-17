package mobi.my2cents.data;

import java.util.HashMap;

import mobi.my2cents.My2Cents;
import mobi.my2cents.utils.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class Comment implements BaseColumns, TransitionalStateColumns{
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + My2Cents.AUTHORITY + "/comments");
	
	public static final String KEY = "key";
	public static final String URI = "uri";
	public static final String BODY = "body";
	public static final String CREATED_AT = "created_at";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String PRODUCT_KEY = "product_key";
	public static final String PRODUCT_NAME = "product_name";
	public static final String PRODUCT_IMAGE_URL = "product_image_url";
	public static final String USER_KEY = "user_key";
	public static final String USER_NAME = "user_name";
	public static final String USER_IMAGE_URL = "user_image_url";


	public static HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( Comment._ID,							Comment._ID);
		projectionMap.put( Comment.KEY,							Comment.KEY);
		projectionMap.put( Comment.URI, 						Comment.URI);
		projectionMap.put( Comment.BODY, 						Comment.BODY);
		projectionMap.put( Comment.CREATED_AT, 					Comment.CREATED_AT);
		projectionMap.put( Comment.LATITUDE, 					Comment.LATITUDE);
		projectionMap.put( Comment.LONGITUDE, 					Comment.LONGITUDE);
		
		projectionMap.put( Comment.PRODUCT_KEY, 				Comment.PRODUCT_KEY);
		projectionMap.put( Comment.PRODUCT_NAME, 				Comment.PRODUCT_NAME);
		projectionMap.put( Comment.PRODUCT_IMAGE_URL, 			Comment.PRODUCT_IMAGE_URL);
		
		projectionMap.put( Comment.USER_KEY, 					Comment.USER_KEY);
		projectionMap.put( Comment.USER_NAME, 					Comment.USER_NAME);
		projectionMap.put( Comment.USER_IMAGE_URL, 				Comment.USER_IMAGE_URL);

		projectionMap.put( Comment.TRANSITION_ACTIVE, 			Comment.TRANSITION_ACTIVE);
		projectionMap.put( Comment.POST_TRANSITIONAL_STATE,		Comment.POST_TRANSITIONAL_STATE);
		projectionMap.put( Comment.GET_TRANSITIONAL_STATE, 		Comment.GET_TRANSITIONAL_STATE);
		projectionMap.put( Comment.PUT_TRANSITIONAL_STATE, 		Comment.PUT_TRANSITIONAL_STATE);
		projectionMap.put( Comment.DEL_TRANSITIONAL_STATE, 		Comment.DEL_TRANSITIONAL_STATE);
	}
	
	public final static ContentValues ParseJson(JSONObject json) throws JSONException {
		
		if (json == null) return null;
		
		ContentValues values = new ContentValues();
		values.put(Comment.KEY, json.getString("id"));
		values.put(Comment.BODY, json.getString("body"));
		values.put(Comment.CREATED_AT, Helper.parseDate(json.getString("created_at")));
		
		if (json.has("product")) {
			JSONObject product = json.getJSONObject("product");
			values.put(Comment.PRODUCT_KEY, product.getString("key"));
			values.put(Comment.PRODUCT_NAME, product.getString("name"));
			values.put(Comment.PRODUCT_IMAGE_URL, product.getString("image_url"));
		}
		
		// User may be anonymous
		if (json.has("user")) {
			JSONObject user = json.getJSONObject("user");
			values.put(Comment.USER_NAME, user.getString("name"));
			values.put(Comment.USER_IMAGE_URL, user.getString("profile_image_url"));
		}
		
		return values;
	}	
	
}
