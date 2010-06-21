package mobi.my2cents.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import mobi.my2cents.My2Cents;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

public final class ImageManager {
	
	private final static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	
	private static Context context;
	
	public static void initialize(Context appContext) {
		context = appContext;
	}
	
	public static void putImage(String name, Bitmap bitmap) {
		final File file = new File(context.getCacheDir(), name);
		try {
			cache.put(name, new SoftReference<Bitmap>(bitmap));
			final FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			Log.e(My2Cents.TAG, e.toString());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.toString());
		}
	}
	
	public static Bitmap getImage(String name) {
		if (cache.containsKey(name)) {
			if (cache.get(name) != null) {
				return cache.get(name).get();
			}
		}
		final File file = new File(context.getCacheDir(), name);
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		cache.put(name, new SoftReference<Bitmap>(bitmap));
		return bitmap;
	}
	
	public static boolean hasImage(String name) {
		if (TextUtils.isEmpty(name)) return false;
		final File file = new File(context.getCacheDir(), name);
		return file.exists();
	}
}
