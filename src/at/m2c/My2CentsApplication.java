package at.m2c;


import android.app.Application;
import at.m2c.data.DataManager;

public class My2CentsApplication extends Application {

	public My2CentsApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DataManager.initHistoryDatabase(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
