package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

public class ProductUpdaterService extends IntentService {
	
	public final static String PRODUCT_UPDATED = "mobi.my2cents.action.PRODUCT_UPDATED";
	
	public ProductUpdaterService() {
		super("ProductUpdater");
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		
		String key = intent.getStringExtra(Product.KEY);
		if (TextUtils.isEmpty(key)) return;
		
		String data = null;
		try {
			data = NetworkManager.getProduct(key);
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		Intent broadcastIntent = new Intent(PRODUCT_UPDATED);
		broadcastIntent.putExtra(Product.KEY, key);
		try {
			if (data != null) {
				
				final JSONObject json = new JSONObject(data).getJSONObject("product");
				
				final ContentValues product = Product.ParseJson(json);				
				getContentResolver().insert(Product.CONTENT_URI, product);
				
				final String url = product.getAsString(Product.IMAGE_URL);
				if (!TextUtils.isEmpty(url) && !url.equals("null")) {
					if (!ImageManager.hasImage(key)) {
						final Bitmap bitmap = NetworkManager.getRemoteImage(url);
						if (bitmap != null) {
							ImageManager.putImage(key, bitmap);
						}
					}
				}
				
				sendBroadcast(broadcastIntent);
				
				
				JSONArray comments = json.getJSONArray("comments");
				if (comments.length() > 0) {					
					ContentValues[] values = new ContentValues[comments.length()];
					for (int i=0; i<comments.length(); i++) {						
		            	JSONObject comment = comments.getJSONObject(i);
		            	values[i] = Comment.ParseJson(comment);
		            }
					
					final int rows = getContentResolver().bulkInsert(Comment.CONTENT_URI, values);
					sendBroadcast(broadcastIntent);
					
					for (int i=0; i<values.length; i++) {
						final String userImageUrl = values[i].getAsString(Comment.USER_IMAGE_URL);						
						if (!TextUtils.isEmpty(userImageUrl) && !userImageUrl.equals("null")) {
							final String username = values[i].getAsString(Comment.USER_NAME);
							if (!ImageManager.hasImage(username)) {
								final Bitmap bitmap = NetworkManager.getRemoteImage(userImageUrl);
								if (bitmap != null) {
									ImageManager.putImage(username, bitmap);
									sendBroadcast(broadcastIntent);
								}
							}
						}					
					}
				}
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} finally {
			sendBroadcast(broadcastIntent);
		}
	}
}
