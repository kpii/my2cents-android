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

public class ScanPosterService extends IntentService {
	
	public final static String SCAN_POSTED = "mobi.my2cents.action.SCAN_POSTED";
	public final static IntentFilter FILTER = new IntentFilter(SCAN_POSTED);
	
	public ScanPosterService() {
		super("ScanPoster");
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		
		final String gtin = intent.getStringExtra(Product.GTIN);
		if (TextUtils.isEmpty(gtin)) return;
		
		String data = null;
		try {
			final String content = Product.getScanJson(gtin).toString();
			data = NetworkManager.postScan(content);
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		try {
			if (data != null) {
				
				final JSONObject json = new JSONObject(data).getJSONObject("scan");
				
				final ContentValues product = Product.ParseJson(json);				
				getContentResolver().insert(Product.CONTENT_URI, product);
				
				if (json.has("comment")) {
					final JSONArray comments = json.getJSONArray("comments");
					if (comments.length() > 0) {
						final ContentValues[] values = new ContentValues[comments.length()];
						for (int i=0; i<comments.length(); i++) {
			            	values[i] = Comment.parseJson(comments.getJSONObject(i));
			            }					
						getContentResolver().bulkInsert(Comment.CONTENT_URI, values);
					}
				}
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.toString());
		} finally {
			final Intent broadcastIntent = new Intent(SCAN_POSTED);
			broadcastIntent.putExtra(Product.GTIN, gtin);
			sendBroadcast(broadcastIntent);
		}
	}
}
