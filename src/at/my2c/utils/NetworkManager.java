
package at.my2c.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class NetworkManager {

	private static final String TAG = "NetworkManager";
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		return true;
	}

	public static Bitmap getRemoteImage(final URL url) {
		if (url != null) {
			try {
				final URLConnection connection = url.openConnection();
				connection.connect();
				final BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
				final Bitmap bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				return bitmap;
			} catch (IOException e) {
				Log.d(TAG, "Cannot load remote image.");
			}
		}
		return null;
	}

	public static Bitmap getRemoteImage(final URL url, final String username, final String password) {
		try {
			final URLConnection connection = url.openConnection();
			setBasicAuthentication(connection, username, password);
			connection.connect();
			final BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
			final Bitmap bitmap = BitmapFactory.decodeStream(stream);
			stream.close();
			return bitmap;
		} catch (IOException e) {
			Log.d(TAG, "Cannot load remote image.");
		}
		return null;
	}

	private static void setBasicAuthentication(URLConnection connection, String name, String password) {
		assert name != null && password != null;
		String token = name + ":" + password;
		String encoding = Base64.encodeToString(token.getBytes(), false);
		connection.setRequestProperty("Authorization", "Basic " + encoding);
	}
	
	public static String getRemotePageAsString(String url, String name, String password) {
		URLConnection connection;
		try {
			connection = new URL(url).openConnection();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return null;
		}
		
		setBasicAuthentication(connection, name, password);
		connection.setRequestProperty("User-Agent", "my2cents");
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		
		InputStreamReader inputStreamReader;
		try {
			inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			inputStreamReader = new InputStreamReader(inputStream);
		}
		
		BufferedReader reader = new BufferedReader(inputStreamReader);
		
		StringBuilder output = new StringBuilder();
		while (true) {
			int c;
			try {
				c = reader.read();
			} catch (IOException e) {
				return null;
			}
			if (c == -1)
				break;
			output.append((char) c);
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		
		return output.toString();
	}
}
