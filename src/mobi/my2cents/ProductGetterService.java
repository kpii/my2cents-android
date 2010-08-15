package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

public class ProductGetterService extends IntentService {
	
	public final static String PRODUCT_UPDATED = "mobi.my2cents.action.PRODUCT_UPDATED";
	public final static IntentFilter FILTER = new IntentFilter(PRODUCT_UPDATED);
	
	public ProductGetterService() {
		super("ProductGetter");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		final String key = intent.getStringExtra(Product.KEY);
		if (TextUtils.isEmpty(key)) return;
		
		String data = null;
		try {
			data = NetworkManager.getProduct(key);
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		try {
			if (data != null) {
				
				final JSONObject json = new JSONObject(data).getJSONObject("product");
				
				final ContentValues product = Product.ParseJson(json);				
				getContentResolver().insert(Product.CONTENT_URI, product);
				
				if (json.has("comments")) {
					final JSONArray comments = json.getJSONArray("comments");
					if (comments.length() > 0) {
						final ContentValues[] values = new ContentValues[comments.length()];
						for (int i=0; i<comments.length(); i++) {
			            	values[i] = Comment.parseProductJson(comments.getJSONObject(i), product.getAsString(Product.KEY), product.getAsString(Product.NAME), product.getAsString(Product.IMAGE_URL));
			            }					
						getContentResolver().bulkInsert(Comment.CONTENT_URI, values);
					}
				}
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.toString());
		} finally {
			final Intent broadcastIntent = new Intent(PRODUCT_UPDATED);
			sendBroadcast(broadcastIntent);
		}
	}
}
