
package at.my2c;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * An HTML-based help screen with Back and Done buttons at the bottom.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class HelpActivity extends Activity {
	
	private static final String TAG = "HelpActivity";
	private static final String DEFAULT_URL = "file:///android_asset/html/index.html";

	private WebView webView;
	private Button backButton;

	private final Button.OnClickListener backListener = new Button.OnClickListener() {
		public void onClick(View view) {
			webView.goBack();
		}
	};

	private final Button.OnClickListener doneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.help);

		webView = (WebView) findViewById(R.id.help_contents);
		webView.setWebViewClient(new HelpClient());
		if (icicle != null) {
			webView.restoreState(icicle);
		} else {
			webView.loadUrl(DEFAULT_URL);
		}

		backButton = (Button) findViewById(R.id.back_button);
		backButton.setOnClickListener(backListener);

		Button doneButton = (Button) findViewById(R.id.done_button);
		doneButton.setOnClickListener(doneListener);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		webView.saveState(state);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private final class HelpClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			setTitle(view.getTitle());
			backButton.setEnabled(view.canGoBack());
		}
	}

}
