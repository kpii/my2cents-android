package at.my2c;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import at.my2c.data.DataManager;
import at.my2c.data.HistoryColumns;
import at.my2c.utils.Helper;

public final class HistoryActivity extends ListActivity {

	private static final String TAG = "HistoryActivity";
	
	private HistoryAdapter adapter;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
		
		findViewById(R.id.ImageButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.ImageButtonHistory).setEnabled(false);
	}
	
	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener streamListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener homeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		
		cursor = DataManager.getDatabase().getHistory();		
		adapter = new HistoryAdapter(this,
        		R.layout.history_item,
        		cursor,
                new String[] { HistoryColumns.GTIN, HistoryColumns.TIME, HistoryColumns.NAME },
                new int[] { R.id.HistoryProductCodeTextView, R.id.HistoryTimeTextView, R.id.HistoryProductNameTextView });
		
        setListAdapter(adapter);
	}
	
	@Override
	protected void onStop() {
        cursor.close();
        super.onStop();
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Cursor cursor = (Cursor) adapter.getItem(position);
		String gtin = cursor.getString(cursor.getColumnIndex(HistoryColumns.GTIN));
		
		Intent intent = new Intent(this, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.putExtra(CommentActivity.UPDATE_HISTORY, false);
		intent.putExtra(DataManager.GTIN_KEY, gtin);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.history_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.clearHistoryMenuItem: {
				DataManager.getDatabase().clearHistory();
				adapter.getCursor().requery();
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
	
	
	private class HistoryAdapter extends SimpleCursorAdapter {

		public HistoryAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
			super(context, layout, cursor, from, to);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView imageView = (ImageView) view.findViewById(R.id.HistoryImageView);
			String gtin = cursor.getString(cursor.getColumnIndex(HistoryColumns.GTIN));
			if (DataManager.productImageCache.containsKey(gtin)) {
				imageView.setImageBitmap(DataManager.productImageCache.get(gtin));
			}
			else {
				byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(HistoryColumns.IMAGE));
				if (bitmapArray != null) {
					Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
					if (bitmap != null) {
						imageView.setImageBitmap(bitmap);
						DataManager.productImageCache.put(gtin, bitmap);
					}
				}
				else {
					imageView.setImageResource(R.drawable.unknown_product_icon);
				}
			}
			super.bindView(view, context, cursor);
		}
	}
}
