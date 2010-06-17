package mobi.my2cents;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
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
			try {
				JSONArray json = new JSONArray(data);
				if (json.length() > 0) {
					
					ContentResolver resolver = getContentResolver();
//					resolver.delete(Comment.CONTENT_URI, null, null);
					
					ContentValues[] values = new ContentValues[json.length()];
					for (int i=0; i<json.length(); i++) {
						
		            	JSONObject item = json.getJSONObject(i).getJSONObject("comment");
		            	values[i] = Comment.ParseJson(item);
		            }
					
					int rows = resolver.bulkInsert(Comment.CONTENT_URI, values);
					
					Intent broadcastIntent = new Intent(FEED_UPDATED);
					sendBroadcast(broadcastIntent);
					
					for (int i=0; i<values.length; i++) {
						String url = values[i].getAsString(Comment.URI);
						if (!DataManager.profileImageCache.containsKey(url)) {
							Bitmap bitmap = NetworkManager.getRemoteImage(url);
							DataManager.profileImageCache.put(url, bitmap);
							sendBroadcast(broadcastIntent);
			            }
					}
				}
			} catch (JSONException e) {
				Log.e(My2Cents.TAG, e.getMessage());
			}
		}
	}
}
