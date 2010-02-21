package at.my2c;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import twitter4j.http.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import at.my2c.comments.CommentsManager;

public final class AuthorizationActivity extends Activity {
	
	private final static String TAG = "AuthorizationActivity";

	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;
	private AccessToken accessToken;
	private ProgressDialog progressDialog;

	public final static String CONSUMER_KEY = "LAFxqUB51z5j5zBp2qYCFA";
	public final static String CONSUMER_SECRET = "y2IhpsBaovR96tUVqplOHbTd7UxRUUmfrxFNaevlzA";
	private final static String CALLBACK_URL = "myapp://oauth";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authorization);

		Button doneButton = (Button) findViewById(R.id.enterPinButton);
		doneButton.setOnClickListener(mDoneListener);
		
		askOAuth();
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
			
			WebView webView = (WebView) findViewById(R.id.oauthWebView);
			webView.loadUrl(authUrl);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
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

			// this will populate token and token_secret in consumer
			try {
				provider.retrieveAccessToken(consumer, verifier);
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// TODO: you might want to store token and token_secret in you app settings!!!!!!!!
			accessToken = new AccessToken(consumer.getToken(), consumer.getTokenSecret());
			
			// initialize Twitter4J
			CommentsManager.InitializeOAuth(accessToken);
			
			new CheckAccount().execute();
		    
		}
	}

	private final Button.OnClickListener mDoneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			finish();
		}
	};
	
	private class CheckAccount extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(AuthorizationActivity.this, getString(R.string.progress_dialog_account_title), getString(R.string.progress_dialog_checking_credentials), true);
	    }
		
		@Override
		protected Boolean doInBackground(Void... params) {
			return CommentsManager.verifyCredentials();
		}
		
		@Override
		protected void onPostExecute(Boolean isLoginCorrect) {
			progressDialog.dismiss();
			
			if (isLoginCorrect) {
				Toast.makeText(AuthorizationActivity.this, R.string.message_login_correct, Toast.LENGTH_SHORT).show();
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AuthorizationActivity.this);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(PreferencesActivity.OAUTH_TOKEN, accessToken.getToken());
				editor.putString(PreferencesActivity.OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
				editor.putBoolean(PreferencesActivity.IS_COMMENTING_POSSIBLE, CommentsManager.isCommentingPossible());
				editor.commit();
				
				Intent result = new Intent(getIntent().getAction());
				setResult(RESULT_OK, result);
			} else {
				Toast.makeText(AuthorizationActivity.this, R.string.message_login_incorrect, Toast.LENGTH_SHORT).show();
			}
			finish();
	    }
	}
}
