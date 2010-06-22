package mobi.my2cents;

import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.NetworkManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

public class ImageDownloaderService extends IntentService {
	
	public final static String IMAGE_DOWNLOADED = "mobi.my2cents.action.IMAGE_DOWNLOADED";
	public final static IntentFilter FILTER = new IntentFilter(IMAGE_DOWNLOADED);
	
	private final static Intent broadcastIntent = new Intent(IMAGE_DOWNLOADED);
	
	public ImageDownloaderService() {
		super("ImageDownloader");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		final String url = intent.getDataString();
		final Bitmap bitmap = NetworkManager.getRemoteImage(url);
		if (bitmap != null) {
			ImageManager.putImage(url, bitmap);
			sendBroadcast(broadcastIntent);
		}
		
	}
}
