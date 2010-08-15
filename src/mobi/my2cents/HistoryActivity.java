package mobi.my2cents;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;

public final class HistoryActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prepareUI();
		bindAdapter();
	}
	
	private void bindAdapter() {
		final Cursor cursor = managedQuery(Product.CONTENT_URI, null, null, null, null);
		setListAdapter(new HistoryAdapter(this, cursor));
	}
	
	private void prepareUI() {
		setContentView(R.layout.history_activity);
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setEnabled(false);
	}
	
	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener streamListener = new Button.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), FeedActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener homeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		final Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
		final String key = cursor.getString(cursor.getColumnIndex(Product.KEY));
		
		final Intent intent = new Intent(this, ProductActivity.class);
		intent.setData(Uri.withAppendedPath(Product.CONTENT_URI, "key/" + key));
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
				getContentResolver().delete(Product.CONTENT_URI, null, null);
				getContentResolver().delete(Comment.CONTENT_URI, null, null);
				((CursorAdapter) getListAdapter()).getCursor().requery();
				return true;
			}
			case R.id.settingsMenuItem: {
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.infoMenuItem: {
				final Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	Intent back = new Intent(this, MainActivity.class);
	    	back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(back);
			finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
