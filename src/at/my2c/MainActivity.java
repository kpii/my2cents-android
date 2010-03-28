
package at.my2c;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import at.my2c.data.BrandedProductColumns;
import at.my2c.data.DataManager;
import at.my2c.utils.Helper;
import at.my2c.utils.NetworkManager;

public final class MainActivity extends ListActivity {
	
	private static final String TAG = "MainActivity";
	
	private SharedPreferences settings;
	private ProductAdapter adapter;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		findViewById(R.id.SearchImageButton).setOnClickListener(searchListener);
		
		findViewById(R.id.ImageButtonHome).setEnabled(false);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.ImageButtonHistory).setOnClickListener(historyListener);
		
        settings = PreferenceManager.getDefaultSharedPreferences(this);
		NetworkManager.setAuthToken(settings.getString(getString(R.string.settings_token),""));
		
		onFirstLaunch();
		
		cursor = DataManager.getDatabase().getBrandedProducts();		
		adapter = new ProductAdapter(this,
        		R.layout.product_item,
        		cursor,
                new String[] { BrandedProductColumns.GTIN, BrandedProductColumns.NAME, BrandedProductColumns.MANUFACTURER },
                new int[] { R.id.ProductItemCodeTextView, R.id.ProductItemNameTextView, R.id.ProductItemManufacturerTextView });
		
        setListAdapter(adapter);
	}
	
	private final ImageButton.OnClickListener scanListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener streamListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener historyListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener searchListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.SearchEditText);
			String gtin = editor.getText().toString();
			Intent intent = new Intent(view.getContext(), CommentActivity.class);
			intent.setAction(Intents.ACTION);
			intent.putExtra(DataManager.GTIN_KEY, gtin);
			startActivity(intent);
		}
	};
	
	private class ProductAdapter extends SimpleCursorAdapter {

		public ProductAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
			super(context, layout, cursor, from, to);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView imageView = (ImageView) view.findViewById(R.id.ProductItemImageView);
			String gtin = cursor.getString(cursor.getColumnIndex(BrandedProductColumns.GTIN));
			if (DataManager.productImageCache.containsKey(gtin)) {
				imageView.setImageBitmap(DataManager.productImageCache.get(gtin));
			}
			else {
				byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(BrandedProductColumns.IMAGE));
				if (bitmapArray != null) {
					Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
					if (bitmap != null) {
						imageView.setImageBitmap(bitmap);
						DataManager.productImageCache.put(gtin, bitmap);
					}
				}
			}
			super.bindView(view, context, cursor);
		}
	}
	
	/**
	 * We want the help screen to be shown automatically the first time a new
	 * version of the app is run. The easiest way to do this is to check
	 * android:versionCode from the manifest, and compare it to a value stored
	 * as a preference.
	 */
	private void onFirstLaunch() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			int currentVersion = info.versionCode;
			int lastVersion = settings.getInt(getString(R.string.settings_first_start), 0);
			if (currentVersion > lastVersion) {
				settings.edit().putInt(getString(R.string.settings_first_start), currentVersion).commit();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(this, HelpActivity.class.getName());
				startActivity(intent);
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, e);
		}
	}
	

	@Override
	protected void onDestroy() {
        cursor.close();
        super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Cursor cursor = (Cursor) adapter.getItem(position);
		String gtin = cursor.getString(1);
		
		Intent intent = new Intent(this, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.putExtra(CommentActivity.UPDATE_HISTORY, false);
		intent.putExtra(CommentActivity.IS_PRODUCT_BRANDED, true);
		intent.putExtra(DataManager.GTIN_KEY, gtin);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
}
