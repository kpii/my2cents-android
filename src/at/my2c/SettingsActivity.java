
package at.my2c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;
import at.my2c.utils.NetworkManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "SettingsActivity";
	
	public static final String AUTH_TOKEN = "AUTH_TOKEN";
	private static boolean shareOnTwitter;
	
	private SharedPreferences settings;
	private String settings_login_key;
	private CheckBoxPreference loginCheckBoxPreference;
	
	private static final int AUTH_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		CookieSyncManager.createInstance(this);
		
		addPreferencesFromResource(R.xml.preferences);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		settings_login_key = getString(R.string.settings_login);
		loginCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference(settings_login_key);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (!isAccessTokenStored())
        	unsetTokens();
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	public boolean isAccessTokenStored() {
		String token = settings.getString(getResources().getString(R.string.settings_token),"");
		if (token.equals(""))
			return false;
		return true;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {        
		if (key.equals(settings_login_key)) {
        	if(loginCheckBoxPreference.isChecked()) {
        		askOAuth();
        	} else {
        		unsetTokens();
        	}
        }         
    }
	
	private void askOAuth() {
		Toast.makeText(this, R.string.message_authorize, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, AuthorizationActivity.class);
		startActivityForResult(intent, AUTH_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTH_REQUEST) {
        	switch (resultCode) {
				case RESULT_OK: {
					if (intent != null) {
	                	String authToken = intent.getStringExtra(AUTH_TOKEN);
	                	if ((authToken != null) && (authToken != "")) {
	                		storeAuthToken(authToken);
	                	}
	                }
					return;
				}
				case RESULT_CANCELED: {
					unsetTokens();
					return;
				}
        	}
        }
    }
		
	private void unsetTokens() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getResources().getString(R.string.settings_token), "");
		editor.putBoolean(settings_login_key, false);
		editor.commit();
		
		NetworkManager.setAuthToken("");
		
		CookieSyncManager.getInstance().sync();
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		CookieSyncManager.getInstance().sync();
		
		loginCheckBoxPreference.setChecked(false);
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "Tokens unset");
	}
	
	private void storeAuthToken(String token) {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getResources().getString(R.string.settings_token), token);
		editor.putBoolean(settings_login_key, true);
		editor.commit();
		
		NetworkManager.setAuthToken(token);
		
		loginCheckBoxPreference.setChecked(true);
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "Tokens stored");
	}

	public static void setShareOnTwitter(boolean shareOnTwitter) {
		SettingsActivity.shareOnTwitter = shareOnTwitter;
	}

	public static boolean isShareOnTwitter() {
		return shareOnTwitter;
	}
}
