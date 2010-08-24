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
import android.net.Uri;
import android.preference.PreferenceManager;
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
		
		final boolean shareLocation = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.settings_share_location), false);
		
		String postResponse = null;
		try {
			final String content = Product.getScanJson(gtin, shareLocation).toString();
			postResponse = NetworkManager.postScan(content);
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		try {
			if (postResponse != null) {
				
				final JSONObject postResponseJson = new JSONObject(postResponse).getJSONObject("scan");
				final String id = postResponseJson.getString("id");
				
				for (byte i=0; i<10; i++) {
					
					String getResponse = null;
					try {
						getResponse = NetworkManager.getScan(id);
					} catch (ClientProtocolException e) {
						Log.e(My2Cents.TAG, e.getMessage());
					} catch (IOException e) {
						Log.e(My2Cents.TAG, e.getMessage());
					}
					
					if (getResponse != null) {
						final JSONObject getResponseJson = new JSONObject(getResponse).getJSONObject("scan");
						if (getResponseJson.isNull("product")) {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								Log.e(My2Cents.TAG, e.getMessage());
							}
							
							continue;
						}
						else {
							final String key = getResponseJson.getJSONObject("product").getString("key");
							final String name = getResponseJson.getJSONObject("product").getString("name");
							
							final Intent broadcastIntent = new Intent(SCAN_POSTED);
							broadcastIntent.putExtra(Product.KEY, key);
							sendBroadcast(broadcastIntent);
							
							break;
						}
					}

				}
				
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.toString());
		} finally {
			
		}
	}
}
