package mobi.my2cents.utils;

import java.util.HashMap;

import android.content.Context;
import android.webkit.WebView;

public class WebViewPool {
	private static final int DEFAULT_POOLSIZE = 5;

	private int poolsize;

	private HashMap<String, WebView> pool;
	private Context ctx;

	public WebViewPool(Context ctx) {
		poolsize = DEFAULT_POOLSIZE;
		new WebViewPool(ctx, poolsize);
	}

	public WebViewPool(Context ctx, int poolsize) {
		this.ctx = ctx; 
		this.poolsize = poolsize;
		this.pool = new HashMap<String, WebView>(poolsize);
	}

	public WebView getWebView(String url) {
		
		if (pool.containsKey(url))
			return pool.get(url);
		else {
			WebView wv;
			if (pool.entrySet().size() < poolsize) {
				wv = new WebView(ctx);
				wv.getSettings().setJavaScriptEnabled(true);
				pool.put(url, wv);
			} else {
				wv = pool.values().iterator().next();
			}
			wv.loadUrl(url);
			return wv;
		}
	}
	
	
	

}
