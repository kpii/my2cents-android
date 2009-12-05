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

public final class HistoryActivity extends ListActivity {

	private final static int MANUAL_INPUT_CODE = 0;

	private HistoryAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Cursor cursor = DataManager.getHistoryDatabase().getHistory();		
		adapter = new HistoryAdapter(this,
        		R.layout.history_item,
        		cursor,
                new String[] { "code", "time", "name" },
                new int[] { R.id.history_code, R.id.history_time, R.id.history_name });
		
        setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Cursor cursor = (Cursor) adapter.getItem(position);
		String selectedCode = cursor.getString(1);
		ProductInfo productInfo = new ProductInfo(selectedCode);
		DataManager.setProductInfo(productInfo);
		
		Intent intent = new Intent(this, CommentsActivity.class);
		intent.setAction(Intents.ACTION);
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
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, ManualInputActivity.class);
				startActivityForResult(intent, MANUAL_INPUT_CODE);
				break;
			}
			case R.id.clearHistoryMenuItem: {
				DataManager.getHistoryDatabase().clearHistory();
				adapter.getCursor().requery();
				break;
			}
			case R.id.preferencesMenuItem: {
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.infoMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case MANUAL_INPUT_CODE: {
				if (resultCode == RESULT_OK) {
					String barcode = data.getStringExtra("PRODUCT_CODE");
					if (barcode != null && !barcode.equals("")) {
						ProductInfo productInfo = new ProductInfo(barcode);
						
						DataManager.setProductInfo(productInfo);
	
						Intent intent = new Intent(this, CommentsActivity.class);
						intent.setAction(Intents.ACTION);
						startActivity(intent);
					}
				}
				break;
			}
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
