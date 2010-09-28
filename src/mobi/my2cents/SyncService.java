package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SyncService extends IntentService {
	
	public final static String SYNC_COMPLETE = "mobi.my2cents.action.SYNC_COMPLETE";
	public final static IntentFilter FILTER = new IntentFilter(SYNC_COMPLETE);
	
	public SyncService() {
		super("SyncService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final boolean shareLocation = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_share_location), false);
		final boolean shareOnTwitter = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_twitter), false);
		final Cursor commentsCursor = getContentResolver().query(Uri.withAppendedPath(Comment.CONTENT_URI, "pending"), null, null, null, null);
		try {
			if (commentsCursor.moveToFirst()) {
				do {
					final JSONObject json = Comment.getJson(commentsCursor, shareLocation, shareOnTwitter);
					if (json != null) {
						try {
							final String response = NetworkManager.postComment(json.toString());
							if (response != null) {
								final long id = commentsCursor.getLong(commentsCursor.getColumnIndex(Comment._ID)); 
								getContentResolver().delete(ContentUris.withAppendedId(Comment.CONTENT_URI, id), null, null);
								ContentValues postedValues;
								try {
									final JSONObject responseJson = new JSONObject(response).getJSONObject("comment");
									postedValues = Comment.parsePostedJson(responseJson);
									getContentResolver().insert(Comment.CONTENT_URI, postedValues);
								} catch (JSONException e) {
									Log.e(My2Cents.TAG, e.toString());
								}
							}
						} catch (ClientProtocolException e) {
							Log.e(My2Cents.TAG, e.toString());
						} catch (IOException e) {
							Log.e(My2Cents.TAG, e.toString());
						}
					}
				} while (commentsCursor.moveToNext());
			}
		} finally {
			commentsCursor.close();
		}
		
		final Cursor productsCursor = getContentResolver().query(Uri.withAppendedPath(Product.CONTENT_URI, "pending"), null, null, null, null);
		try {
			if (productsCursor.moveToFirst()) {
				do {
					final JSONObject json = Product.getRatingJson(productsCursor);
					if (json != null) {
						try {
							final String key = productsCursor.getString(productsCursor.getColumnIndex(Product.KEY)); 
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
										values.put(Product.PENDING, false);
										getContentResolver().update(Uri.withAppendedPath(Product.CONTENT_URI, "key/" + key), values, null, null);
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
				} while (productsCursor.moveToNext());
			}
		} finally {
			productsCursor.close();
		}
		
		final Intent broadcastIntent = new Intent(SYNC_COMPLETE);
		sendBroadcast(broadcastIntent);
	}
}
