
package at.my2c;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.http.AccessToken;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import at.my2c.comments.CommentsManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public final static String CONSUMER_KEY = "LAFxqUB51z5j5zBp2qYCFA";
	public final static String CONSUMER_SECRET = "y2IhpsBaovR96tUVqplOHbTd7UxRUUmfrxFNaevlzA";
	
	private final static String CALLBACK_URL = "myapp://oauth";
	
	public final static String TagPrefix = "#";
	public final static String ProductCodePrefix = "#my2c #";

	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;
	private SharedPreferences settings;
	
	private String twitter_login_key;
	private CheckBoxPreference twitter_login;
	
	private final int DIALOG_CHECK = 1; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
			
		addPreferencesFromResource(R.xml.preferences);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		twitter_login_key = getResources().getString(R.string.settings_login_twitter);
		twitter_login = (CheckBoxPreference)getPreferenceScreen().findPreference(twitter_login_key);
		
		updateUI();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateUI();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {        
		if (key.equals(twitter_login_key)) {
        	if(twitter_login.isChecked()) {
        		askOAuth();
        	} else {
        		unsetTokens();
        	}
        }         
    }
	
	/**
	 * Open the browser and asks the user to authorize the app.
	 * Afterwards, we redirect the user back here!
	 */
	private void askOAuth() {
		try {
			consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			provider = new DefaultOAuthProvider("http://twitter.com/oauth/request_token",
												"http://twitter.com/oauth/access_token",
												"http://twitter.com/oauth/authorize");
			String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
			Toast.makeText(this, R.string.message_authorize, Toast.LENGTH_LONG).show();
			Intent oauth = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			startActivity(oauth);
		} catch (Exception e) {
			Log.e(SettingsActivity.class.getName(), e.getMessage());
			Toast.makeText(this, R.string.message_connection_error, Toast.LENGTH_LONG).show();
			settings.edit().putBoolean(getResources().getString(R.string.settings_login_twitter), false).commit();
		}
	}
	
	/**
	 * As soon as the user successfully authorized the app, we are notified
	 * here. Now we need to get the verifier from the callback URL, retrieve
	 * token and token_secret and feed them to twitter4j (as well as
	 * consumer key and secret).
	 */
	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);

		Uri uri = intent.getData();
		
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {

			String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
			Log.i(SettingsActivity.class.getName(), "Verifier: " + verifier);
			
			try {
				// this will populate token and token_secret in consumer
				provider.retrieveAccessToken(consumer, verifier);
				Log.i(SettingsActivity.class.getName(), "Tokens retrieved");
				
				// initialize Twitter4J
				CommentsManager.InitializeOAuth(new AccessToken(consumer.getToken(), consumer.getTokenSecret()));
				
				// store/update tokens in settings
				storeTokens(consumer.getToken(), consumer.getTokenSecret());
				
//				new CheckAccountTask().execute();
				
			} catch (Exception e) {
				Log.e(SettingsActivity.class.getName(), e.getMessage());
				Toast.makeText(this, R.string.message_connection_error, Toast.LENGTH_LONG).show();
				settings.edit().putBoolean(getResources().getString(R.string.settings_login_twitter), false).commit();
			}

		} else {
			Log.w(SettingsActivity.class.getName(), "Callback URL not from OAUTH");
		}
	}
	
	private void updateUI() {
		String token = settings.getString(this.getResources().getString(R.string.settings_token),"");
		String tokenSecret = settings.getString(this.getResources().getString(R.string.settings_token_secret),"");
		
		if (token.equals("") && tokenSecret.equals("")) {
			twitter_login.setTitle(R.string.settings_login_twitter);
			twitter_login.setChecked(false);
		} else {
			twitter_login.setTitle(R.string.settings_login_twitter_checked);
			twitter_login.setChecked(true);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CHECK: {
	            ProgressDialog dialog = new ProgressDialog(this);
	            dialog.setMessage(SettingsActivity.this.getText(R.string.message_please_wait));
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(false);
	            return dialog;
	        }
		}
		
		return super.onCreateDialog(id);
	}
		
	private void unsetTokens() {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getResources().getString(R.string.settings_token), "");
		editor.putString(getResources().getString(R.string.settings_token_secret), "");
		editor.putString(getResources().getString(R.string.settings_user), "");
		editor.commit();
		
		Log.i(SettingsActivity.class.getName(), "Tokens unset");
		updateUI();
	}
	
	private void storeTokens(String token, String tokenSecret) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getResources().getString(R.string.settings_token), token).commit();
		editor.putString(getResources().getString(R.string.settings_token_secret), tokenSecret).commit();
		editor.commit();
		
		Log.i(SettingsActivity.class.getName(), "Tokens stored");
		updateUI();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToMain();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void backToMain() {
		Intent main = new Intent(this, SettingsActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(main);
		finish();
	}
}
