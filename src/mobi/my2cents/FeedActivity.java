
package mobi.my2cents;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.NetworkManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public final class FeedActivity extends ListActivity {
	
	private FeedAdapter adapter;
	
	private View statusLayout;
	
	private FeedUpdaterReceiver feedUpdaterReceiver;
	private ImageDownloaderReceiver imageDownloaderReceiver;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		feedUpdaterReceiver = new FeedUpdaterReceiver();
		imageDownloaderReceiver = new ImageDownloaderReceiver();
		
		bindAdapter();		
		prepareUI();
		
		handleIntent(getIntent());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(feedUpdaterReceiver, FeedUpdaterService.FILTER);
		registerReceiver(imageDownloaderReceiver, ImageDownloaderService.FILTER);
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(feedUpdaterReceiver);
		unregisterReceiver(imageDownloaderReceiver);
		super.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		updateFeed();
	}
	
	private void bindAdapter() {
		final Cursor cursor = managedQuery(Comment.CONTENT_URI, null, null, null, null);
		adapter = new FeedAdapter(this, cursor);
		setListAdapter(adapter);
	}
	
	private void updateFeed() {
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			statusLayout.setVisibility(View.VISIBLE);
			Intent intent = new Intent(this, FeedUpdaterService.class);
			startService(intent);
		}		
	}
	
	private void prepareUI() {
		setContentView(R.layout.stream);

		statusLayout = findViewById(R.id.StatusRelativeLayout);
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setEnabled(false);
		findViewById(R.id.NavigationButtonHistory).setOnClickListener(historyListener);
	}
	
	
	
	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener homeListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener historyListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};


	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		final Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
		final String key = cursor.getString(cursor.getColumnIndex(Comment.PRODUCT_KEY));
		if (!TextUtils.isEmpty(key)) {
			Intent intent = new Intent(this, CommentActivity.class);
			intent.setData(Uri.withAppendedPath(Product.CONTENT_URI, key));
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stream_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refreshMenuItem: {
				updateFeed();
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
		prepareUI();
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
	
	private final class FeedUpdaterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			statusLayout.setVisibility(View.GONE);
			adapter.getCursor().requery();
			adapter.notifyDataSetChanged();
		}
		
	}
	
	private final class ImageDownloaderReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.getCursor().requery();
			adapter.notifyDataSetChanged();
		}
		
	}
}
