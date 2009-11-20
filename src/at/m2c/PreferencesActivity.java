package at.m2c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;

public final class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private final static int ACCOUNT_ACTIVITY_CODE = 0;
	
	private static final String TAG = "PreferencesActivity";
	
	public static String ProductCodePrefix = "#";

	public static final String TWITTER_PROVIDER = "twitter_provider";
	public static final String TWITTER_USERNAME = "twitter_username";
	public static final String TWITTER_PASSWORD = "twitter_password";
	public static final String TWITTER_CUSTOM_API_URL = "twitter_custom_api_url";

	static final String KEY_PLAY_BEEP = "preferences_play_beep";
	static final String KEY_VIBRATE = "preferences_vibrate";
	static final String KEY_COPY_TO_CLIPBOARD = "preferences_copy_to_clipboard";

	static final String KEY_PREFIX = "preferences_prefix";
	public static final String GPS_ENABLED = "gps_enabled";
	public static final String RESEARCH_ENABLED = "research_enabled";
	public static final String NUMBER_OF_SEARCH_RESULTS = "number_of_search_results";
	
	static final String ACCOUNT_PREFERENCE = "account_preference";
	static final String IS_COMMENTING_POSSIBLE = "is_commenting_possible";

	static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceScreen preferences = getPreferenceScreen();
		preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		Preference accountPreference = this.findPreference(ACCOUNT_PREFERENCE);
		accountPreference.setOnPreferenceClickListener(onAccountPreferenceClickListener);
	}
	
	private final OnPreferenceClickListener onAccountPreferenceClickListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			Intent intent = new Intent(getBaseContext(), AccountActivity.class);
			startActivityForResult(intent, ACCOUNT_ACTIVITY_CODE);
			return true;
		}
	};

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub	
	}
}
