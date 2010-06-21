package mobi.my2cents;

import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.NetworkManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class My2Cents extends Application {
	
	public final static String TAG = "My2Cents";
	public final static String AUTHORITY = "mobi.my2cents";
	
	public static PackageInfo packageInfo;
	
	public My2Cents() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
//		AlarmManager mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//		Intent i=new Intent(this, OnAlarmReceiver.class);
//		PendingIntent pi=PendingIntent.getBroadcast(this, 0,i, 0);
//		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//											SystemClock.elapsedRealtime()+60000,
//											60*1000,
//											pi);
//		Log.d(TAG, "registered polling");
		
		
		CookieSyncManager.createInstance(this);
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			NetworkManager.setUserAgent("Android my2cents [" + My2Cents.packageInfo.versionName + "]");
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		DataManager.initialize(this);
		ImageManager.initialize(this);
	}
}
