/*
 * Copyright (C) 2009 Anton Rau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.my2c;

import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import at.my2c.util.ProviderManager;

public final class AuthorizationActivity extends Activity {
	
	private final static String TAG = "AuthorizationActivity";
	public final String CALLBACK_URL = "myapp://oauth";
	private static final String OAUTH_VERIFIER = "oauth_verifier";
	private RequestToken requestToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorization);

		Button doneButton = (Button) findViewById(R.id.enterPinButton);
		doneButton.setOnClickListener(mDoneListener);
		
		ProviderManager.InitializeOAuth();
		try {
			requestToken = ProviderManager.getTwitter().getOAuthRequestToken(CALLBACK_URL);

			WebView webView = (WebView) findViewById(R.id.oauthWebView);
			webView.loadUrl(requestToken.getAuthorizationURL());
			
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
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

			String verifier = uri.getQueryParameter(OAUTH_VERIFIER);
			AccessToken accessToken = null;
			try {
				accessToken = ProviderManager.getTwitter().getOAuthAccessToken(verifier);
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProviderManager.getTwitter().setOAuthAccessToken(accessToken);
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AuthorizationActivity.this);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(PreferencesActivity.OAUTH_TOKEN, accessToken.getToken());
			editor.putString(PreferencesActivity.OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
		    editor.commit();
		}
		
		finish();
	}

	private final Button.OnClickListener mDoneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText pinEditor = (EditText) findViewById(R.id.pinEditText);
			String pin = pinEditor.getText().toString();
			
			try {
				AccessToken accessToken = ProviderManager.getTwitter().getOAuthAccessToken(requestToken, pin);
				ProviderManager.setAccessToken(accessToken);
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AuthorizationActivity.this);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(PreferencesActivity.OAUTH_TOKEN, accessToken.getToken());
				editor.putString(PreferencesActivity.OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
			    editor.commit();
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
			}
			finish();
		}
	};
}
