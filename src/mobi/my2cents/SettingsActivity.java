
package mobi.my2cents;

import mobi.my2cents.utils.Helper;
import mobi.my2cents.utils.NetworkManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "SettingsActivity";
	
	private static boolean shareOnTwitter;
	
	private SharedPreferences settings;
	private CheckBoxPreference loginCheckBoxPreference;
	
	private static final int AUTH_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		addPreferencesFromResource(R.xml.preferences);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		loginCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference(getString(R.string.settings_login));
		
		// Intent feedback
        PreferenceScreen feedbackPreference = (PreferenceScreen)getPreferenceManager().findPreference(getString(R.string.settings_send_feedback));
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"feedback@my2cents.mobi"});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "my2cents user feedback [" + Helper.getClientID(this) + "]");
		feedbackPreference.setIntent(emailIntent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		settings.registerOnSharedPreferenceChangeListener(this);
        if (!isAccessTokenStored())
        	unsetTokens();
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	public boolean isAccessTokenStored() {
		String token = settings.getString(getString(R.string.settings_token),"");
		if (token.equals(""))
			return false;
		return true;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {        
		if (key.equals(getString(R.string.settings_login))) {
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
	                	String authToken = intent.getStringExtra(getString(R.string.settings_token));
	                	if ((authToken != null) && (authToken != "")) {
	                		onSetToken();
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
		settings.unregisterOnSharedPreferenceChangeListener(this);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getString(R.string.settings_token), "");
		editor.putBoolean(getString(R.string.settings_login), false);
		editor.commit();
		
		NetworkManager.setAuthToken("");
		
		CookieSyncManager.getInstance().sync();
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		CookieSyncManager.getInstance().sync();
		
		loginCheckBoxPreference.setChecked(false);
		
		settings.registerOnSharedPreferenceChangeListener(this);
		Log.i(TAG, "Tokens unset");
	}
	
	private void onSetToken() {
		settings.unregisterOnSharedPreferenceChangeListener(this);
		loginCheckBoxPreference.setChecked(true);
		settings.registerOnSharedPreferenceChangeListener(this);
	}

	public static void setShareOnTwitter(boolean shareOnTwitter) {
		SettingsActivity.shareOnTwitter = shareOnTwitter;
	}

	public static boolean isShareOnTwitter() {
		return shareOnTwitter;
	}
}
