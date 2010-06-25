package mobi.my2cents;

import mobi.my2cents.utils.NetworkManager;
import mobi.my2cents.utils.WebViewPool;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class My2CentsApplication extends Application {
	
	private final static String TAG = "My2CentsApplication";
	public static PackageInfo packageInfo;
	private WebViewPool webViewPool;
	
	public My2CentsApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		CookieSyncManager.createInstance(this);
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			NetworkManager.userAgent = "Android my2cents [" + My2CentsApplication.packageInfo.versionName + "]";
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		
		webViewPool = new WebViewPool(this);
	}
	
	public WebViewPool getWebViewPool() {
		return webViewPool;
	}
}
