package mobi.my2cents.utils;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import mobi.my2cents.My2Cents;
import mobi.my2cents.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.preference.PreferenceManager;
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
	
	public static String getClientID(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String key = context.getString(R.string.settings_client_id);
		String clientId = settings.getString(key, "");
		if(clientId == null || clientId.equals("")) {
			clientId = UUID.randomUUID().toString();
			settings.edit().putString(key, clientId).commit();
		}	
		return clientId;
	}
	
	public static long parseDate(String data) {
		try {
			return formatter.parse(data).getTime();
		} catch (ParseException e) {
			Log.e(My2Cents.TAG, e.toString());
			return 0;
		}
    }
	
	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}
}
