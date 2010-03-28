package at.my2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import at.my2c.utils.NetworkManager;

public final class AuthorizationActivity extends Activity {
	
	private final static String TAG = "AuthorizationActivity";
	
	private WebView webView;
	
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
	    public void onPageFinished(WebView view, String url) {
	        if (url.equals(NetworkManager.BASE_URL + "/auth/success")) {
	        	Toast.makeText(view.getContext(), R.string.message_successful_authorization, Toast.LENGTH_LONG).show();
	        	
	        	CookieSyncManager.getInstance().sync();
	    		CookieManager cookieManager = CookieManager.getInstance();
	    		String cookie = cookieManager.getCookie(NetworkManager.BASE_URL + "/remember_token");
	    		CookieSyncManager.getInstance().sync();
	    		
	    		Intent intent = getIntent();
	    		intent.putExtra(SettingsActivity.AUTH_TOKEN, cookie);
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
