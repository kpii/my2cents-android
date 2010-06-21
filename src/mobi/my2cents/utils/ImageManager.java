package mobi.my2cents.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import mobi.my2cents.My2Cents;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

public final class ImageManager {
	
	private static Context context;
	
	public static void initialize(Context appContext) {
		context = appContext;
	}
	
	public static void putImage(String name, Bitmap bitmap) {
		final File file = new File(context.getCacheDir(), name);
		try {
			final FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, stream);
		} catch (FileNotFoundException e) {
			Log.e(My2Cents.TAG, e.toString());
		}
	}
	
	public static Bitmap getImage(String name) {
		final File file = new File(context.getCacheDir(), name);
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}
	
	public static boolean hasImage(String name) {
		if (TextUtils.isEmpty(name)) return false;
		final File file = new File(context.getCacheDir(), name);
		return file.exists();
	}
}
