package mobi.my2cents;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

	private static final String TAG = "WebViewActivity";
    private WebView webview;



    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    webview = ((My2Cents)getApplication()).getWebViewPool().getWebView(getIntent().getDataString());
	    setContentView(webview);
	}

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

	
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        ((ViewGroup)webview.getParent()).removeAllViews();
        super.onDestroy();
    }
	
}
