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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import at.my2c.data.DataManager;
import at.my2c.util.Helper;

public final class HistoryActivity extends ListActivity {

	private HistoryAdapter adapter;
	private Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		cursor = DataManager.getDatabase().getHistory();		
		adapter = new HistoryAdapter(this,
        		R.layout.history_item,
        		cursor,
                new String[] { "productCode", "time", "name" },
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
		String searchTerm = cursor.getString(1);
		DataManager.setSearchTerm(searchTerm);
		
		Intent intent = new Intent(this, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.putExtra(CommentActivity.UPDATE_HISTORY, false);
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
			case R.id.scanMenuItem: {
				Intent intent = new Intent(this, ScanActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				return true;
			}
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
			 byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex("image"));
			 if (bitmapArray != null) {
				 Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
				 ImageView imageView = (ImageView) view.findViewById(R.id.HistoryImageView);
				 imageView.setImageBitmap(bitmap);
			 }
			 super.bindView(view, context, cursor);
		}
	}
}
