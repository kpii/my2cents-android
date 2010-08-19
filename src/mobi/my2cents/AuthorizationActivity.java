package mobi.my2cents;

import mobi.my2cents.utils.AuthenticationManager;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public final class AuthorizationActivity extends Activity {
	
	private WebView webView;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.authorization_activity);
		
		webView = (WebView) findViewById(R.id.AuthWebView);
		webView.setWebViewClient(new AuthWebViewClient());
	}
	
	private class AuthWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	view.loadUrl(url);
	        return true;
	    }
	    
	    @Override
	    public void onPageStarted(WebView view, String url, Bitmap favicon) {
	    	closeProgressDialog();
	    	progressDialog = ProgressDialog.show(AuthorizationActivity.this, null, getString(R.string.progress_dialog_loading), true);
	    }
	    
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	closeProgressDialog();
	    	
	        if (url.equals(NetworkManager.BASE_URL + "/auth/success")) {
	        	
	        	authorized();
	    		
	    		final Intent intent = getIntent();
	    		setResult(RESULT_OK, intent);

				finish();
	        }
	        else if (url.equals(NetworkManager.BASE_URL + "/auth/failure")) {
	        	
	        	nonAuthorized();
	        	
	        	final Intent intent = getIntent();
	    		setResult(RESULT_CANCELED, intent);

				finish();
	        }
	    }
	}
	
	private void closeProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
	}
	
	private void authorized() {
		Toast.makeText(this, R.string.message_successful_authorization, Toast.LENGTH_LONG).show();
		
		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean(getString(R.string.settings_login), true);
		editor.commit();
		
		final Intent intent = new Intent(this, UserGetterService.class);
		startService(intent);
	}
	
	private void nonAuthorized() {
		Toast.makeText(this, R.string.message_unsuccessful_authorization, Toast.LENGTH_LONG).show();
		
		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean(getString(R.string.settings_login), false);
		editor.commit();
	}
		
	@Override
	protected void onResume() {
		super.onResume();
		CookieManager.getInstance().removeAllCookie();
		webView.loadUrl(NetworkManager.BASE_URL + "/login?client_token=" + AuthenticationManager.getClientToken());
	}
	
	@Override
	protected void onPause() {
		closeProgressDialog();
		super.onPause();
	}
}
