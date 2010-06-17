
package mobi.my2cents;

import mobi.my2cents.utils.Helper;
import mobi.my2cents.utils.NetworkManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "SettingsActivity";
	
	private static boolean shareOnTwitter;
	private static boolean shareLocation;
	
	private SharedPreferences settings;
	private CheckBoxPreference loginCheckBoxPreference;
	
	private static final int AUTH_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		addPreferencesFromResource(R.xml.preferences);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		ListPreference listpreference = ((ListPreference)findPreference("polling_interval"));
		listpreference.setSummary(listpreference.getEntry());
		
		loginCheckBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.settings_login));
		
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
        } else if (key.equals("polling_interval")) {
        	int newValue = Integer.valueOf(sharedPreferences.getString("polling_interval", "30000"));
        	Log.d(My2Cents.TAG, "polling setting changed "+newValue);
			AlarmManager mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
			Intent i=new Intent(SettingsActivity.this, OnAlarmReceiver.class);
			PendingIntent pi=PendingIntent.getBroadcast(SettingsActivity.this, 0,i, 0);
			mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
												SystemClock.elapsedRealtime()+newValue,newValue,pi);
			ListPreference listpreference = ((ListPreference)findPreference("polling_interval"));
			listpreference.setSummary(listpreference.getEntry());
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

	public static void setShareLocation(boolean shareLocation) {
		SettingsActivity.shareLocation = shareLocation;
	}

	public static boolean isShareLocation() {
		return shareLocation;
	}
}
