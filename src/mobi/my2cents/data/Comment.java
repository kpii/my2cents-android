package mobi.my2cents.data;

import java.net.URL;
import java.util.Date;
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
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";


	public static HashMap<String, String> projectionMap = new HashMap<String, String>();
	static {
		projectionMap.put( Comment._ID,							Comment._ID);
		projectionMap.put( Comment.KEY,							Comment.KEY);
		projectionMap.put( Comment.URI, 						Comment.URI);
		projectionMap.put( Comment.BODY, 						Comment.BODY);
		projectionMap.put( Comment.CREATED_AT, 					Comment.CREATED_AT);
		projectionMap.put( Comment.PRODUCT_KEY, 				Comment.PRODUCT_KEY);
		projectionMap.put( Comment.USER_ID, 					Comment.USER_ID);
		projectionMap.put( Comment.USER_NAME, 					Comment.USER_NAME);
		projectionMap.put( Comment.LATITUDE, 					Comment.LATITUDE);
		projectionMap.put( Comment.LONGITUDE, 					Comment.LONGITUDE);
		projectionMap.put( Comment.TRANSITION_ACTIVE, 			Comment.TRANSITION_ACTIVE);
		projectionMap.put( Comment.TRANSITION_ACTIVE, 			Comment.TRANSITION_ACTIVE);
		projectionMap.put( Comment.POST_TRANSITIONAL_STATE,		Comment.POST_TRANSITIONAL_STATE);
		projectionMap.put( Comment.GET_TRANSITIONAL_STATE, 		Comment.GET_TRANSITIONAL_STATE);
		projectionMap.put( Comment.PUT_TRANSITIONAL_STATE, 		Comment.PUT_TRANSITIONAL_STATE);
		projectionMap.put( Comment.DEL_TRANSITIONAL_STATE, 		Comment.DEL_TRANSITIONAL_STATE);
	}



	private String user;
	private URL userProfileImageUrl;
	private String text;
    private Date createdAt;
    
    private String gtin;
    private String productName;
    private URL productImageUrl;
    
    public Comment() {
    }

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setUserProfileImageUrl(URL userProfileImageUrl) {
		this.userProfileImageUrl = userProfileImageUrl;
	}

	public URL getUserProfileImageUrl() {
		return userProfileImageUrl;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductImageUrl(URL productImageUrl) {
		this.productImageUrl = productImageUrl;
	}

	public URL getProductImageUrl() {
		return productImageUrl;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getGtin() {
		return gtin;
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Comment.BODY, text);
		cv.put(Comment.PRODUCT_KEY, gtin);
		return cv;
	}
	
	public final static ContentValues ParseJson(JSONObject json) throws JSONException {
		if (json == null) return null;
		
		ContentValues values = new ContentValues();
		values.put(Comment.BODY, json.getString("body"));
		values.put(Comment.KEY, json.getString("id"));
		values.put(Comment.CREATED_AT, Helper.parseDate(json.getString("created_at")));
		
		// User may be anonymous
		if (json.has("user")) {
			JSONObject user = json.getJSONObject("user");
			
//			values.put(Comment.USER_ID, user.getString("id"));
			values.put(Comment.USER_NAME, user.getString("name"));
			values.put(Comment.URI, user.getString("profile_image_url"));
		}
		else {
			
		}
		
		return values;
	}	
	
}
