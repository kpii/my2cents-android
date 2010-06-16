package mobi.my2cents;

import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.NetworkManager;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class My2Cents extends Application {
	
	public final static String TAG = "My2Cents";
	public static PackageInfo packageInfo;
	
	public My2Cents() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		CookieSyncManager.createInstance(this);
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			NetworkManager.setUserAgent("Android my2cents [" + My2Cents.packageInfo.versionName + "]");
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		DataManager.initialize(this);
	}
}
