
package mobi.my2cents;

import mobi.my2cents.utils.AuthenticationManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private SharedPreferences settings;
	private PreferenceScreen loginPreference;

	private UserGetterReceiver userGetterReceiver;
	
	private static final int AUTH_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		userGetterReceiver = new UserGetterReceiver();
		
		setContentView(R.layout.settings_activity);
		addPreferencesFromResource(R.xml.preferences);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		loginPreference = (PreferenceScreen) findPreference(getString(R.string.settings_login));
		loginPreference.setOnPreferenceClickListener(authorizationListener);
		
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
		displayUser();
		registerReceiver(userGetterReceiver, UserGetterService.FILTER);
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(userGetterReceiver);
		super.onPause();
	}
	
	private final OnPreferenceClickListener authorizationListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			final boolean isAuthorized = settings.getBoolean(getString(R.string.settings_login), false);
			if (isAuthorized) {
				askLogout();
			}
			else {
				startAuthorization();
			}
			return true;
		}
	};
	
	private void askLogout() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to logout?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                logout();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		final AlertDialog alert = builder.create();
		alert.show();
	}

	
	private void startAuthorization() {
		final Intent intent = new Intent(this, AuthorizationActivity.class);
		startActivityForResult(intent, AUTH_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == AUTH_REQUEST) {
        	switch (resultCode) {
				case RESULT_OK: {
					displayUser();
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
		AuthenticationManager.logout();
		
		final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean(getString(R.string.settings_login), false);
		editor.putString(getString(R.string.user_name), getString(R.string.anonymous_username));
		editor.putString(getString(R.string.user_avatar_url), "");
		editor.commit();
		
		displayUser();
	}
	
	private void displayUser() {
		final boolean isAuthorized = settings.getBoolean(getString(R.string.settings_login), false);
		if (isAuthorized) {
			loginPreference.setTitle(R.string.settings_logout);
		}
		else {
			loginPreference.setTitle(R.string.settings_login);
		}
		
		loginPreference.setSummary(settings.getString(getString(R.string.user_name), getString(R.string.anonymous_username)));
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		
	}
	
	private final class UserGetterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			displayUser();
		}
		
	}
}
