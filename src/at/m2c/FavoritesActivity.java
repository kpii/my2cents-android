package at.m2c;

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
import at.m2c.data.DataManager;
import at.m2c.data.ProductInfo;
import at.m2c.util.Helper;

public final class FavoritesActivity extends ListActivity {

	private HistoryAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorites);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Cursor cursor = DataManager.getDatabase().getFavorites();		
		adapter = new HistoryAdapter(this,
        		R.layout.favorite_item,
        		cursor,
                new String[] { "productId", "provider", "name" },
                new int[] { R.id.history_code, R.id.history_time, R.id.history_name });
		
        setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Cursor cursor = (Cursor) adapter.getItem(position);
		String searchTerm = cursor.getString(1);
		DataManager.setSearchTerm(searchTerm);
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(Intents.ACTION);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.favorites_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.clearFavoritesMenuItem: {
				DataManager.getDatabase().clearFavorites();
				adapter.getCursor().requery();
				return true;
			}
			case R.id.preferencesMenuItem: {
				Intent intent = new Intent(this, PreferencesActivity.class);
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
			 Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
			 ImageView imageView = (ImageView) view.findViewById(R.id.history_image);
			 imageView.setImageBitmap(bitmap);
			 
			 super.bindView(view, context, cursor);
		}
	}
}
