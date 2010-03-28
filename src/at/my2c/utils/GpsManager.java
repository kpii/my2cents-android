
package at.my2c.utils;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;

public class GpsManager {
	public static final Location getGPS(LocationManager lm) {  
		List<String> providers = lm.getProviders(true);

		/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
		Location l = null;
		
		for (int i=providers.size()-1; i>=0; i--) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null) break;
		}
		
		return l;
	}
}
