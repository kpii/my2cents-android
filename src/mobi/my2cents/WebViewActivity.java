package mobi.my2cents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.my2cents.data.Product;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	private static final String TAG = "WebViewActivity";
    private WebView webview;



    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    webview = ((My2Cents)getApplication()).getWebViewPool().getWebView(getIntent().getDataString());
	    setContentView(webview);
	    
	    webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                
                
                Matcher m = Pattern.compile(".*my2cents.mobi/products/(\\d*)$").matcher(url);
                if (m.matches()) {
                    Log.d(TAG, "matched product_key: "+m.group(1));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url), WebViewActivity.this, ProductActivity.class));
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Product.CONTENT_URI, m.group(1))));
                } else {
                    Log.d(TAG, "NOT matched product_key: ");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
                
//                Uri uri = Uri.parse(url);
//                if (uri.getHost().equals("my2cents.mobi")) {
//                    String product_key = uri.getPathSegments().get(1);
//                    if (uri.getPathSegments().get(0).equals("products") &&
//                            Character.isDigit(product_key.charAt(product_key.length()-1))) {
//                        Log.d(TAG, "matched regex for product url");
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Product.CONTENT_URI, product_key)));
//                        
//                    }
                    
//                }
                return true;
            }});
	    
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
