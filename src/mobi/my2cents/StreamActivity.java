
package mobi.my2cents;

import java.util.ArrayList;
import java.util.List;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.HistoryColumns;
import mobi.my2cents.utils.NetworkManager;
import mobi.my2cents.utils.WeakAsyncTask;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public final class StreamActivity extends ListActivity {
	
	private static final String TAG = "StreamActivity";
	
	private StreamAdapter streamAdapter;
	private static ArrayList<Comment> commentsArray = new ArrayList<Comment>();
	
	private View statusLayout;
	
	private AsyncTask<Void, Void, ArrayList<Comment>> getCommentsStreamTask;
	private AsyncTask<List<Comment>, Void, Void> getProfileImagesTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stream);

		statusLayout = findViewById(R.id.StatusRelativeLayout);
		
		findViewById(R.id.ImageButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setEnabled(false);
		findViewById(R.id.ImageButtonHistory).setOnClickListener(historyListener);
		
		streamAdapter = new StreamAdapter(this, R.layout.stream_item, commentsArray);
		setListAdapter(streamAdapter);
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
	protected void onStart() {
		super.onStart();
		
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			return;
		}

		getCommentsStreamTask = new GetCommentsStreamTask(this).execute();
	}
	
	private class GetCommentsStreamTask extends WeakAsyncTask<Void, Void, ArrayList<Comment>, Context> {

		public GetCommentsStreamTask(Context target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Context target) {
			statusLayout.setVisibility(View.VISIBLE);
	    }
		
		@Override
		protected ArrayList<Comment> doInBackground(Context target, Void... params) {
			return DataManager.getCommentsStream();
		}
		
		@Override
		protected void onPostExecute(Context target, ArrayList<Comment> result) {
			if (result == null) {
				statusLayout.setVisibility(View.GONE);
				Toast.makeText(target, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			}
			else {				
				streamAdapter.clear();				
				if (result.size() > 0) {
					for (Comment comment : result) {
						streamAdapter.add(comment);
					}
					streamAdapter.notifyDataSetChanged();
					statusLayout.setVisibility(View.GONE);
					
					getProfileImagesTask = new GetImagesTask(target).execute(result);
				}
				else {
					statusLayout.setVisibility(View.GONE);
				}
			}
			getCommentsStreamTask = null;
	    }
	}
	
	private class GetImagesTask extends WeakAsyncTask<List<Comment>, Void, Void, Context> {
		
		public GetImagesTask(Context target) {
			super(target);
		}

		@Override
		protected Void doInBackground(Context target, List<Comment>... params) {			
			
			for (Comment comment : params[0]) {				
				String gtin = comment.getGtin(); 
				if ((gtin != null) && (!gtin.equals(""))) {
					if (!DataManager.productImageCache.containsKey(gtin)) {
						DataManager.productImageCache.put(gtin, NetworkManager.getRemoteImage(comment.getProductImageUrl()));
					}
				}
				publishProgress();
			}
			
			getProfileImagesTask = null;
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			streamAdapter.notifyDataSetChanged();
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!NetworkManager.isNetworkAvailable(this)) {
			return;
		}
		
		if (streamAdapter.getCount() > 0)
			streamAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Comment selectedComment = streamAdapter.getItem(position);
		
		Intent intent = new Intent(this, CommentActivity.class);
		intent.setAction(Intents.ACTION);
		intent.putExtra(CommentActivity.UPDATE_HISTORY, true);
		intent.putExtra(HistoryColumns.GTIN, selectedComment.getGtin());
		startActivity(intent);
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
				cancelAsyncTasks();
				getCommentsStreamTask = new GetCommentsStreamTask(this).execute();
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
	
	private final void cancelAsyncTasks() {
		if (getCommentsStreamTask != null && getCommentsStreamTask.getStatus() == AsyncTask.Status.RUNNING) {
        	getCommentsStreamTask.cancel(true);
        }
        
        if (getProfileImagesTask != null && getProfileImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
        	getProfileImagesTask.cancel(true);
        }
	}
	
	@Override
    protected void onDestroy() {
		cancelAsyncTasks();
        super.onDestroy();
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
