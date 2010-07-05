package mobi.my2cents.utils;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import mobi.my2cents.My2Cents;
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
	
	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	}
}
