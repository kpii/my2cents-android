package at.my2c.utils;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TwitterException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;


public final class Helper {
	
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	bitmap.compress(CompressFormat.PNG, 100, outputStream);
    	return outputStream.toByteArray(); 
    }
	
	public static Bitmap getByteArrayAsBitmap(byte[] bitmapArray) {
		return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length); 
    }
	
	public static Location getLocationInstance(JSONObject json) throws TwitterException {
        try {
            if (!json.isNull("geo")) {
                String coordinates = json.getJSONObject("geo").getString("coordinates");
                coordinates = coordinates.substring(1, coordinates.length() - 1);
                String[] point = coordinates.split(",");
                Location location = new Location("my2cents");
                location.setLatitude(Double.parseDouble(point[0]));
                location.setLongitude(Double.parseDouble(point[1]));
                return location;
            }
        } catch (JSONException jsone) {
            throw new TwitterException(jsone);
        }
        return null;
    }
}
