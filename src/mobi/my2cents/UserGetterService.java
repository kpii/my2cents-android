package mobi.my2cents;

import java.io.IOException;

import mobi.my2cents.utils.NetworkManager;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class UserGetterService extends IntentService {
	
	public final static String USER_RETRIEVED = "mobi.my2cents.action.USER_RETRIEVED";
	public final static IntentFilter FILTER = new IntentFilter(USER_RETRIEVED);
	
	public UserGetterService() {
		super("UserGetter");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		String data = null;
		try {
			data = NetworkManager.getUser();
		} catch (ClientProtocolException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		}
		
		try {
			if (data != null) {
				final JSONObject json = new JSONObject(data).getJSONObject("me");
				final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
				editor.putString(getString(R.string.user_name), json.getString("name"));
				editor.putString(getString(R.string.user_avatar_url), json.getString("profile_image_url"));
				editor.commit();
			}			
		} catch (JSONException e) {
			Log.e(My2Cents.TAG, e.getMessage());
		} finally {
			final Intent broadcastIntent = new Intent(USER_RETRIEVED);
			sendBroadcast(broadcastIntent);
		}
	}
}
