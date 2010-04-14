package mobi.my2cents;

import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.NetworkManager;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class My2CentsApplication extends Application {
	
	private final static String TAG = "My2CentsApplication";
	public static PackageInfo packageInfo;
	
	public My2CentsApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			NetworkManager.userAgent = "Android my2cents [" + My2CentsApplication.packageInfo.versionName + "]";
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		DataManager.initialize(this);
	}
}
