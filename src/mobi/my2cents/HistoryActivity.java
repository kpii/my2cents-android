package mobi.my2cents;

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
import android.widget.ListView;

public final class HistoryActivity extends ListActivity {
	
	private HistoryAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prepareUI();
		handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		bindAdapter();
	}
	
	private void bindAdapter() {
		final Cursor cursor = managedQuery(Product.CONTENT_URI, null, null, null, null);
		adapter = new HistoryAdapter(this, cursor);
		setListAdapter(adapter);
	}
	
	private void prepareUI() {
		setContentView(R.layout.history);
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setEnabled(false);
	}
	
	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener streamListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), FeedActivity.class);
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
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Cursor cursor = (Cursor) adapter.getItem(position);
		String key = cursor.getString(cursor.getColumnIndex(Product.KEY));
		
		Intent intent = new Intent(this, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.setData(Uri.withAppendedPath(Product.CONTENT_URI, key));
		intent.putExtra(CommentActivity.UPDATE_HISTORY, false);
		intent.putExtra(Product.KEY, key);
		
//		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Product.CONTENT_URI, key));
		
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
