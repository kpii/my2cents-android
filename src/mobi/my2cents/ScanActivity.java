package mobi.my2cents;

import java.io.IOException;
import java.util.Vector;

import mobi.my2cents.data.History;
import mobi.my2cents.scanner.CameraManager;
import mobi.my2cents.scanner.CaptureActivityHandler;
import mobi.my2cents.scanner.ViewfinderView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

public final class ScanActivity extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = "ScanActivity";

	private static final float BEEP_VOLUME = 0.15f;
	private static final long VIBRATE_DURATION = 200L;
	
	private final int DIALOG_SEARCH	= 0;

	private SharedPreferences settings;

	private CaptureActivityHandler handler;

	private ViewfinderView viewfinderView;
	private MediaPlayer mediaPlayer;
	private Result lastResult;
	private boolean hasSurface;
	private boolean playBeep;
	private boolean vibrate;
	private boolean showVirtualKeyboard;
	private final OnCompletionListener beepListener = new BeepListener();

	public static final Vector<BarcodeFormat> PRODUCT_FORMATS;
	static {
		PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
		PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
		PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
		PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.scan);

		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setEnabled(false);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setOnClickListener(historyListener);
		
		findViewById(R.id.ManualInputButton).setOnClickListener(manualInputListener);

		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.ViewfinderView);
	}

	private final View.OnClickListener homeListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};

	private final View.OnClickListener historyListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};

	private final View.OnClickListener streamListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener manualInputListener = new View.OnClickListener() {
		public void onClick(View view) {
			showDialog(DIALOG_SEARCH);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.PreviewSurfaceView);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		resetStatusView();

		playBeep = settings.getBoolean(getString(R.string.settings_play_beep), true);
		vibrate = settings.getBoolean(getString(R.string.settings_vibrate), false);
		
		showVirtualKeyboard = getIntent().getBooleanExtra(getString(R.string.show_virtual_keyboard), false);
		
		initBeepSound();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.capture_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.searchMenuItem: {
				showDialog(DIALOG_SEARCH);
				return true;
			}
			case R.id.settingsMenuItem: {
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.infoMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		lastResult = rawResult;
		playBeepSoundAndVibrate();
		drawResultPoints(barcode, rawResult);

		handleDecodeInternally(rawResult, barcode);
	}

	/**
	 * Superimpose a line for 1D or dots for 2D to highlight the key features of
	 * the barcode.
	 * 
	 * @param barcode
	 *            A bitmap of the captured image.
	 * @param rawResult
	 *            The decoded results which contains the points to draw.
	 */
	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);

			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				canvas.drawLine(points[0].getX(), points[0].getY(), points[1].getX(), points[1].getY(), paint);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	// Put up our own UI for how to handle the decoded contents.
	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		ParsedResult result = ResultParser.parseResult(rawResult);
		String gtin = result.getDisplayResult().replace("\r", "");
		if (rawResult.getBarcodeFormat().equals(BarcodeFormat.UPC_A)) {
			gtin = "0" + gtin;
		}
		
		showProductDetails(this, gtin);
	}

	/**
	 * Creates the beep MediaPlayer in advance so that the sound can be
	 * triggered with the least latency possible.
	 */
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
			return;
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.e(TAG, e.toString());
			displayFrameworkBugMessageAndExit();
			return;
		}
		if (handler == null) {
			boolean beginScanning = lastResult == null;
			handler = new CaptureActivityHandler(this, PRODUCT_FORMATS, beginScanning);
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.message_camera_problem);
		builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				finish();
			}
		});
		builder.show();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);

		TextView textView = (TextView) findViewById(R.id.StatusTextView);
		textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		textView.setTextSize(14.0f);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private static class BeepListener implements OnCompletionListener {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch (id) {
			case DIALOG_SEARCH: {
				LayoutInflater factory = LayoutInflater.from(this);
		        final View view = factory.inflate(R.layout.search_dialog, null);
		        return new AlertDialog.Builder(this)
		            .setIcon(android.R.drawable.ic_menu_search)
		            .setTitle(R.string.search_title)
		            .setView(view)
		            .setPositiveButton(R.string.button_search, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                	EditText editor = (EditText)view.findViewById(R.id.InputEditText);
		                	String gtin = editor.getText().toString();
		        			if ((gtin != null) && (!gtin.equals(""))) {
		        				showProductDetails(view.getContext(), gtin);
		        			}
		                }
		            })
		            .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
		            	public void onClick(DialogInterface dialog, int whichButton) {
                	
		            	}
		            })
		            .create();
	        }
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			setResult(RESULT_CANCELED);
	        finish();
	        
			Intent back = new Intent(this, MainActivity.class);
	    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(back);	
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void showProductDetails(Context context, String gtin) {
		Intent intent = new Intent(context, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.putExtra(History.GTIN, gtin);
		intent.putExtra(getString(R.string.show_virtual_keyboard), showVirtualKeyboard);
		startActivity(intent);
	}
}
