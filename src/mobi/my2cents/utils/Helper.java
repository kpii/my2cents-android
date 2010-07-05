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
import android.text.TextUtils;
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
	
	public static final String getClientID(Context context) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final String key = context.getString(R.string.client_token);
		String clientToken = settings.getString(key, "");
		if(TextUtils.isEmpty(clientToken)) {
			clientToken = UUID.randomUUID().toString();
			settings.edit().putString(key, clientToken).commit();
		}	
		return clientToken;
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
