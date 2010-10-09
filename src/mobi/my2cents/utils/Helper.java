package mobi.my2cents.utils;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import mobi.my2cents.My2Cents;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;


public final class Helper {
	
	private static SimpleDateFormat formatter;
	
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	bitmap.compress(CompressFormat.PNG, 100, outputStream);
    	return outputStream.toByteArray(); 
    }
	
	public static Bitmap getByteArrayAsBitmap(byte[] bitmapArray) {
		return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length); 
    }
	
	public static long parseDate(String data) {
		try {
			return formatter.parse(data).getTime();
		} catch (ParseException e) {
			Log.e(My2Cents.TAG, e.toString());
			return 0;
		}
    }
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}
}
