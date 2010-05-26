
package mobi.my2cents.utils;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;

public class GpsManager {
	
	public static LocationManager locationManager;
	
	public static final Location getLocation() { 
		if (locationManager == null) return null;
		List<String> providers = locationManager.getProviders(true);
		/* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
		Location location = null;		
		for (int i=providers.size()-1; i>=0; i--) {
			location = locationManager.getLastKnownLocation(providers.get(i));
			if (location != null) break;
		}		
		return location;
	}
}
