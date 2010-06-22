package mobi.my2cents.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.URLEncoder;
import java.util.HashMap;

import mobi.my2cents.ImageDownloaderService;
import mobi.my2cents.My2Cents;
import mobi.my2cents.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public final class ImageManager {
	
	private final static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	
	private static Bitmap defaultBitmap;
	private static Context context;
	
	public static void initialize(Context appContext) {
		context = appContext;
		defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_product_icon);
	}
	
	public static void putImage(String url, Bitmap bitmap) {
		if (bitmap == null) return;
		try {
			final String key = URLEncoder.encode(url, "UTF-8");
			cache.put(key, new SoftReference<Bitmap>(bitmap));
			
			final File file = new File(context.getCacheDir(), key);			
			final FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, stream);
			stream.close();
		} catch (FileNotFoundException e) {
			Log.e(My2Cents.TAG, e.toString());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.toString());
		}
	}
	
	public static Bitmap getImage(String url) {
		if (TextUtils.isEmpty(url) || url.equals("null")) return defaultBitmap;
		
		Bitmap bitmap = defaultBitmap;
		try {
			String key = URLEncoder.encode(url, "UTF-8");
			
			if (cache.containsKey(key)) {
				final SoftReference<Bitmap> reference = cache.get(key);
				if (reference != null) {
					final Bitmap image = reference.get();
					if (image != null) {
						return image;
					}
				}
				cache.remove(key);
			}
			
			final File file = new File(context.getCacheDir(), key);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
				if (bitmap != null) {
					cache.put(key, new SoftReference<Bitmap>(bitmap));
				}
			}
			else {
				Intent intent = new Intent(context, ImageDownloaderService.class);
				intent.setData(Uri.parse(url));
				context.startService(intent);
			}
			
		} catch (UnsupportedEncodingException e) {
			Log.e(My2Cents.TAG, e.toString());
		}		
		return bitmap;
	}
}
