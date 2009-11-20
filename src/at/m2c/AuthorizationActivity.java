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

package at.m2c;

import twitter4j.Configuration;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import at.m2c.util.ProviderManager;

public final class AuthorizationActivity extends Activity {
	
	private final static String TAG = "AuthorizationActivity";
	
	private RequestToken requestToken;
	private AccessToken accessToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorization);

		Button doneButton = (Button) findViewById(R.id.enterPinButton);
		doneButton.setOnClickListener(mDoneListener);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//		ProviderManager.Initialize(preferences);
		ProviderManager.getTwitter().setOAuthConsumer(Configuration.getOAuthConsumerKey(), Configuration.getOAuthConsumerSecret());
		try {
			requestToken = ProviderManager.getTwitter().getOAuthRequestToken();

			WebView webView = (WebView) findViewById(R.id.oauthWebView);
			webView.loadUrl(requestToken.getAuthorizationURL());
			
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
		}
	}

	private final Button.OnClickListener mDoneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.pinEditText);
			String pin = editor.getText().toString();
			
			try {
				accessToken = ProviderManager.getTwitter().getOAuthAccessToken(requestToken, pin);
				ProviderManager.getTwitter().setOAuthAccessToken(accessToken);
			} catch (TwitterException e) {
				Log.e(TAG, e.toString());
			}

			finish();
		}
	};
}
