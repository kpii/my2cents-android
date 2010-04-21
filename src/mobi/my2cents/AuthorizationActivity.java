package mobi.my2cents;

import mobi.my2cents.utils.NetworkManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public final class AuthorizationActivity extends Activity {
	
	private final static String TAG = "AuthorizationActivity";
	
	private WebView webView;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.authorization);
		
		webView = (WebView) findViewById(R.id.AuthWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new AuthWebViewClient());
		webView.getSettings().setUserAgentString("Android my2cents");
	}
	
	private class AuthWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	view.loadUrl(url);
	        return true;
	    }
	    
	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
	    	if (progressDialog != null && progressDialog.isShowing()) {
	    		progressDialog.dismiss();
	    	}
	    	progressDialog = ProgressDialog.show(webView.getContext(), null, getString(R.string.progress_dialog_loading), true);
	    }
	    
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	progressDialog.dismiss();
	        if (url.equals(NetworkManager.BASE_URL + "/auth/success")) {
	        	Toast.makeText(view.getContext(), R.string.message_successful_authorization, Toast.LENGTH_LONG).show();
	        	
	        	CookieSyncManager.getInstance().sync();
	    		CookieManager cookieManager = CookieManager.getInstance();
	    		String cookie = cookieManager.getCookie(NetworkManager.BASE_URL + "/remember_token");
	    		CookieSyncManager.getInstance().sync();
	    		
	    		storeAuthToken(cookie);
	    		
	    		Intent intent = getIntent();
	    		intent.putExtra(getString(R.string.settings_token), cookie);
	    		setResult(RESULT_OK, intent);

				finish();
	        }
	        else if (url.equals(NetworkManager.BASE_URL + "/auth/failure")) {
	        	Toast.makeText(view.getContext(), R.string.message_unsuccessful_authorization, Toast.LENGTH_LONG).show();
	        	
	        	Intent intent = getIntent();
	    		setResult(RESULT_CANCELED, intent);

				finish();
	        }
	    }
	}
	
	private void storeAuthToken(String token) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(getString(R.string.settings_token), token);
		editor.putBoolean(getString(R.string.settings_login), true);
		editor.commit();
		
		NetworkManager.setAuthToken(token);
		
		Log.i(TAG, "Tokens stored");
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
		webView.loadUrl(NetworkManager.BASE_URL + "/login");
	}
	
	@Override
	protected void onPause() {
		CookieSyncManager.getInstance().stopSync();
		super.onPause();
	}
}
