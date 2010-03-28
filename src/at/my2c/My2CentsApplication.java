
package at.my2c;

import android.app.Application;
import at.my2c.data.DataManager;

public class My2CentsApplication extends Application {

	public My2CentsApplication() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DataManager.initDatabase(this);
	}
}
