package at.my2c.utils;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.preference.PreferenceManager;
import at.my2c.R;


public final class Helper {
	
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
}
