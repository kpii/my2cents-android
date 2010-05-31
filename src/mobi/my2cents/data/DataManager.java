package mobi.my2cents.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import mobi.my2cents.SettingsActivity;
import mobi.my2cents.utils.GpsManager;
import mobi.my2cents.utils.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

public final class DataManager {
	
	private static final String TAG = "DataManager";
	
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private static DatabaseHelper database;
	
	public static String UnknownProductName;
	
	public final static HashMap<String, Bitmap> profileImageCache = new HashMap<String, Bitmap>();
	public final static HashMap<String, Bitmap> productImageCache = new HashMap<String, Bitmap>();
	

	public static void initialize(Context context) {
		database = new DatabaseHelper(context);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	public static DatabaseHelper getDatabase() {
		return database;
	}
	
	public final static ProductInfo getProductInfo(String gtin){
		
		String jsonString = NetworkManager.getProductJsonString(gtin);
        return getProductInfoFromJsonString(jsonString);
	}
	
	private final static ProductInfo getProductInfoFromJsonString(String jsonString){
		if (jsonString == null) return null;
		try {
			JSONObject json = new JSONObject(jsonString);
			JSONObject jsonProduct = json.getJSONObject("product");
			
			String gtin = jsonProduct.getString("key");
			ProductInfo productInfo = new ProductInfo(gtin);
			
			String name = jsonProduct.getString("name");
			if ((name != null) && (!name.equals("null")) && (!name.equals("")))
				productInfo.setName(name);
			else
				productInfo.setName(DataManager.UnknownProductName);
			
			if (jsonProduct.has("affiliates")) {
				JSONArray jsonLinks = jsonProduct.getJSONArray("affiliates");
				if (jsonLinks.length() > 0) {
					JSONObject affiliate = jsonLinks.getJSONObject(0);
					productInfo.setAffiliateName(affiliate.getString("text"));
					productInfo.setAffiliateUrl(affiliate.getString("href"));
				}
			}
			
			if (jsonProduct.has("rating")) {
				JSONObject jsonRating = jsonProduct.getJSONObject("rating");
				if (jsonRating != null) {
					productInfo.setLikes(jsonRating.getInt("likes"));
					productInfo.setDislikes(jsonRating.getInt("dislikes"));
				}
			}
            
            productInfo.setImageUrl(jsonProduct.getString("image_url"));
            
            if (productImageCache.containsKey(gtin) && productImageCache.get(gtin) != null) {
            	productInfo.setImage(productImageCache.get(gtin));
            }
            else {
            	try {
    				String urlString = productInfo.getImageUrl();
    				if ((urlString != null) && (!urlString.equals("")) && (!urlString.equals("null"))) {
    					URL imageUrl = new URL(urlString);
    					Bitmap productImage = NetworkManager.getRemoteImage(imageUrl);
    					if (productImage != null) {
    						productInfo.setImage(productImage);
        					productImageCache.put(gtin, productInfo.getImage());
    					}
    				}
    			} catch (MalformedURLException e) {
    				Log.e(TAG, e.getMessage());
    			}
            }
            
            JSONArray jsonComments = jsonProduct.getJSONArray("comments");
            for (int i=0; i<jsonComments.length(); i++) {
            	JSONObject jsonComment = jsonComments.getJSONObject(i);            	
            	Comment comment = Json2Comment(jsonComment);
				if (comment != null) {
					productInfo.getComments().add(comment);
				}
            }        
            return productInfo;
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	
	public final static ProductInfo getProductComments(ProductInfo productInfo){
		String jsonString = NetworkManager.getProductJsonString(productInfo.getGtin());
        if (jsonString == null) return null;
		try {
			JSONObject json = new JSONObject(jsonString).getJSONObject("product");
            
            JSONArray jsonComments = json.getJSONArray("comments");
            for (int i=0; i<jsonComments.length(); i++) {
            	JSONObject jsonComment = jsonComments.getJSONObject(i);            	
            	Comment comment = Json2Comment(jsonComment);
				if (comment != null) {
					productInfo.getComments().add(comment);
				}
            }
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		return productInfo;
	}
	
	private final static Comment Json2Comment(JSONObject json) {
		if (json == null) return null;
		
		Comment comment = new Comment();
    	
    	try {
    		comment.setText(json.getString("body"));
    		comment.setCreatedAt(dateFormatter.parse(json.getString("created_at")));
    		
    		// User may be anonymous
    		if (json.has("user")) {
    			JSONObject jsonUser = json.getJSONObject("user");
            	comment.setUser(jsonUser.getString("name"));
            	
            	String urlString = jsonUser.getString("profile_image_url");
            	if ((urlString != null) && (!urlString.equals("")) && (!urlString.equals("null"))) {
            		URL url = new URL(urlString);
            		comment.setUserProfileImageUrl(url);
            	}
    		}
    		
    		if (json.has("product")) {
    			JSONObject jsonProduct = json.getJSONObject("product");
            	comment.setProductName(jsonProduct.getString("name"));
            	comment.setGtin(jsonProduct.getString("key"));
            	
            	String urlString = jsonProduct.getString("image_url");
            	if ((urlString != null) && (!urlString.equals("")) && (!urlString.equals("null"))) {
            		URL url = new URL(urlString);
            		comment.setProductImageUrl(url);
            	}
    		}
    	} catch (JSONException e) {
    		Log.e(TAG, e.getMessage());
    	} catch (MalformedURLException e) {
    		Log.e(TAG, e.getMessage());
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage());
		}
    	
    	return comment;
	}
	
	public final static ArrayList<Comment> getCommentsStream(){
		
		String jsonString = NetworkManager.getCommentsStreamJSONString();
		if (jsonString == null) return null;
		
		try {
			JSONArray json = new JSONArray(jsonString);
			ArrayList<Comment> comments = new ArrayList<Comment>();
            for (int i=0; i<json.length(); i++) {
            	JSONObject jsonComment = json.getJSONObject(i).getJSONObject("comment");            	
            	Comment comment = Json2Comment(jsonComment);
				if (comment != null) {
					comments.add(comment);
				}
            }	
            return comments;
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	
	public final static Comment postComment(String gtin, String body) {
		String content = null;
	    try {
	    	JSONObject jsonComment = new JSONObject();
	    	jsonComment.put("product_key", gtin);
	    	jsonComment.put("body", body);
	    	if (SettingsActivity.isShareLocation()) {
				Location location = GpsManager.getLocation();
				if (location != null) {
					jsonComment.put("latitude", location.getLatitude());
					jsonComment.put("longitude", location.getLongitude());
				}
			}

			JSONObject json = new JSONObject();
			json.put("comment", jsonComment);
			String tmp = SettingsActivity.isShareOnTwitter() ? "1" : "0";
			json.put("publish_to_twitter", tmp);
			content = json.toString();
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		
		String jsonString = NetworkManager.postComment(content);
		if (jsonString != null) {
			JSONObject json;
			try {
				json = new JSONObject(jsonString);
				return Json2Comment(json.getJSONObject("comment"));
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return null;
	}
	
	public final static String rateProduct(String gtin, String value) {
		String content = null;
	    try {
	    	JSONObject jsonRating = new JSONObject();
	    	jsonRating.put("value", value);
	    	
	    	JSONObject json = new JSONObject();
	    	json.put("rating", jsonRating);
			content = json.toString();
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		
		if (content != null) {
			boolean wasSend = NetworkManager.putRating(gtin, content);
			if (wasSend) {
				return value;
			}
		}
		return null;
	}
}
