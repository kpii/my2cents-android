
package mobi.my2cents;

import mobi.my2cents.utils.AuthenticationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private SharedPreferences settings;
	private CheckBoxPreference loginCheckBoxPreference;
	
	private static final int AUTH_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		
		addPreferencesFromResource(R.xml.preferences);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		loginCheckBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.settings_login));
		
		// Intent feedback
        final PreferenceScreen feedbackPreference = (PreferenceScreen)getPreferenceManager().findPreference(getString(R.string.settings_send_feedback));
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"feedback@my2cents.mobi"});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "my2cents user feedback [" + AuthenticationManager.getClientToken() + "]");
		feedbackPreference.setIntent(emailIntent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
		settings.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		settings.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {        
		if (key.equals(getString(R.string.settings_login))) {
        	if(loginCheckBoxPreference.isChecked()) {
        		startAuthorization();
        	} else {
        		logout();
        	}
        }
    }
	
	private void startAuthorization() {
		Toast.makeText(this, R.string.message_authorize, Toast.LENGTH_LONG).show();
		final Intent intent = new Intent(this, AuthorizationActivity.class);
		startActivityForResult(intent, AUTH_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTH_REQUEST) {
        	switch (resultCode) {
				case RESULT_OK: {
					login();
					return;
				}
				case RESULT_CANCELED: {
					logout();
					return;
				}
        	}
        }
    }
		
	private void logout() {
		settings.unregisterOnSharedPreferenceChangeListener(this);
		
		AuthenticationManager.logout();
		
		final SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(getString(R.string.settings_login), false);
		editor.commit();
		
		loginCheckBoxPreference.setChecked(false);
		
		settings.registerOnSharedPreferenceChangeListener(this);
	}
	
	private void login() {
		settings.unregisterOnSharedPreferenceChangeListener(this);
		loginCheckBoxPreference.setChecked(true);
		settings.registerOnSharedPreferenceChangeListener(this);
	}
}
