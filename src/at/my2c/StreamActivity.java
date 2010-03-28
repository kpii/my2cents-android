
package at.my2c;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.my2c.data.Comment;
import at.my2c.data.DataManager;
import at.my2c.utils.NetworkManager;
import at.my2c.utils.WeakAsyncTask;

public final class StreamActivity extends ListActivity {
	
	private static final String TAG = "StreamActivity";
	
	private ArrayList<Comment> comments;
	private StreamAdapter streamAdapter;
	
	private String lastUpdateStatus;
	private ProgressDialog progressDialog;
	private TextView statusTextView;
	
	private AsyncTask<Void, Void, ArrayList<Comment>> getCommentsStreamTask;
	private AsyncTask<List<Comment>, Void, Void> getProfileImagesTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stream);

		lastUpdateStatus = getString(R.string.status_last_update) + " ";
		statusTextView = (TextView) findViewById(R.id.StatusTextView);
		
		findViewById(R.id.ImageButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setEnabled(false);
		findViewById(R.id.ImageButtonHistory).setOnClickListener(historyListener);
		
		comments = new ArrayList<Comment>();
		streamAdapter = new StreamAdapter(this, R.layout.stream_item, comments);
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
			progressDialog = ProgressDialog.show(target, null, getString(R.string.progress_dialog_loading), true);
	    }
		
		@Override
		protected ArrayList<Comment> doInBackground(Context target, Void... params) {
			return DataManager.getCommentsStream();
		}
		
		@Override
		protected void onPostExecute(Context target, ArrayList<Comment> result) {
			if (result == null) {
				progressDialog.dismiss();
				Toast.makeText(target, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			}
			else {
				comments = result;
				
				Date date = new Date(System.currentTimeMillis());
      		  	statusTextView.setText(lastUpdateStatus + date.toLocaleString());
				
				streamAdapter.clear();
				
				if (comments.size() > 0) {
					for (Comment comment : comments) {
						streamAdapter.add(comment);
					}
					streamAdapter.notifyDataSetChanged();
					progressDialog.dismiss();
					
					getProfileImagesTask = new GetImagesTask(target).execute(comments);
				}
				else {
					progressDialog.dismiss();
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
//				if (!DataManager.profileImageCache.containsKey(comment.getUser())) {
//					DataManager.profileImageCache.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
//				}
				
				if (comment.getProductImageUrl() != null) {
					if (!DataManager.productImageCache.containsKey(comment.getProductImageUrl().toString())) {
						DataManager.productImageCache.put(comment.getProductImageUrl().toString(), NetworkManager.getRemoteImage(comment.getProductImageUrl()));
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
		Comment selectedComment = comments.get(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(selectedComment.getText())
		       .setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
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
}
