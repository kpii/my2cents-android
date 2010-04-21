package mobi.my2cents;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.help);
		
		webView = (WebView) findViewById(R.id.help_contents);
		webView.setWebViewClient(new HelpClient());

		backButton = (Button) findViewById(R.id.back_button);
		backButton.setOnClickListener(backListener);

		findViewById(R.id.done_button).setOnClickListener(doneListener);

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		}
		else {
			webView.loadUrl(DEFAULT_URL);
		}
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
			backButton.setEnabled(view.canGoBack());
		}
	}

}
