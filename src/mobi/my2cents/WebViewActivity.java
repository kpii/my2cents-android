package mobi.my2cents;

import android.app.Activity;
import android.os.Bundle;

public class WebViewActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(((My2CentsApplication)getApplication()).getWebViewPool().getWebView(getIntent().getDataString()));
}

}
