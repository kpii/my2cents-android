package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
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

public class FeedUpdaterService extends IntentService {
	
	public final static String FEED_UPDATED = "mobi.my2cents.action.FEED_UPDATED";
	
	public FeedUpdaterService() {
		super("FeedUpdater");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		String data = null;
		try {
			data = NetworkManager.getFeed();
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		if (data != null) {
			Intent broadcastIntent = new Intent(FEED_UPDATED);
			try {
				JSONArray json = new JSONArray(data);
				if (json.length() > 0) {
								
					ContentValues[] values = new ContentValues[json.length()];
					for (int i=0; i<json.length(); i++) {
						
		            	JSONObject item = json.getJSONObject(i).getJSONObject("comment");
		            	values[i] = Comment.ParseJson(item);
		            }
					
					final int rows = getContentResolver().bulkInsert(Comment.CONTENT_URI, values);
					sendBroadcast(broadcastIntent);
					
					for (int i=0; i<values.length; i++) {
						final String url = values[i].getAsString(Comment.PRODUCT_IMAGE_URL);
						
						if (!TextUtils.isEmpty(url) && !url.equals("null")) {
							final String key = values[i].getAsString(Comment.PRODUCT_KEY);
							if (!ImageManager.hasImage(key)) {
								final Bitmap bitmap = NetworkManager.getRemoteImage(url);
								if (bitmap != null) {
									ImageManager.putImage(key, bitmap);
									sendBroadcast(broadcastIntent);
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
}
