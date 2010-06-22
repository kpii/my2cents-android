package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class SyncService extends IntentService {
	
	public final static String SYNC_COMPLETE = "mobi.my2cents.action.SYNC_COMPLETE";
	public final static IntentFilter FILTER = new IntentFilter(SYNC_COMPLETE);
	
	private final static Intent broadcastIntent = new Intent(SYNC_COMPLETE);
	
	public SyncService() {
		super("SyncService");
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
		
		try {
			if (data != null) {
				JSONArray json = new JSONArray(data);
				if (json.length() > 0) {
								
					ContentValues[] values = new ContentValues[json.length()];
					for (int i=0; i<json.length(); i++) {
						
		            	JSONObject item = json.getJSONObject(i).getJSONObject("comment");
		            	values[i] = Comment.parseJson(item);
		            }
					
					getContentResolver().bulkInsert(Comment.CONTENT_URI, values);
				}
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} finally {
			sendBroadcast(broadcastIntent);
		}
	}
}
