package mobi.my2cents;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import mobi.my2cents.data.Comment;
import mobi.my2cents.utils.NetworkManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
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
		
		final Cursor cursor = getContentResolver().query(Uri.withAppendedPath(Comment.CONTENT_URI, "post"), null, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					final JSONObject json = Comment.getJson(cursor);
					if (json != null) {
						try {
							final String response = NetworkManager.postComment(json.toString());
							if (response != null) {
								final String id = cursor.getString(cursor.getColumnIndex(Comment._ID)); 
								getContentResolver().delete(Uri.withAppendedPath(Comment.CONTENT_URI, id), null, null);
							}
						} catch (ClientProtocolException e) {
							Log.e(My2Cents.TAG, e.toString());
						} catch (IOException e) {
							Log.e(My2Cents.TAG, e.toString());
						}
					}
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
			sendBroadcast(broadcastIntent);
		}		
	}
}
