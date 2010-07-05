package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
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
		
		Cursor cursor = getContentResolver().query(Uri.withAppendedPath(Comment.CONTENT_URI, "post"), null, null, null, null);
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
		}
		
		cursor = getContentResolver().query(Uri.withAppendedPath(Product.CONTENT_URI, "put"), null, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					final JSONObject json = Product.getRatingJson(cursor);
					if (json != null) {
						try {
							final String key = cursor.getString(cursor.getColumnIndex(Product.KEY)); 
							final String response = NetworkManager.putRating(key, json.toString());
							if (response != null) {								
								final JSONObject responseJson = new JSONObject(response);
								if (responseJson.has("rating")) {
									final JSONObject ratingJson = responseJson.getJSONObject("rating");
									if (ratingJson != null) {
										final ContentValues values = new ContentValues();
										values.put(Product.RATING_DISLIKES, ratingJson.getInt("dislikes"));
										values.put(Product.RATING_LIKES, ratingJson.getInt("likes"));
										if (ratingJson.has("me")) {
											values.put(Product.RATING_PERSONAL, ratingJson.getString("me"));
										}								
										values.put(Product.TRANSITION_ACTIVE, false);
										values.put(Product.PUT_TRANSITIONAL_STATE, false);
										getContentResolver().update(Uri.withAppendedPath(Product.CONTENT_URI, key), values, null, null);
									}
								}
							}
						} catch (JSONException e) {
							Log.e(My2Cents.TAG, e.toString());
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
		}
		
		sendBroadcast(broadcastIntent);
	}
}