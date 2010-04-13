
package mobi.my2cents;

import mobi.my2cents.data.DataManager;
import android.app.Application;

public class My2CentsApplication extends Application {

	public My2CentsApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DataManager.initialize(this);
	}
}
