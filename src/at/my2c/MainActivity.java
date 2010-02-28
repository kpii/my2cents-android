
package at.my2c;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import twitter4j.http.AccessToken;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.my2c.comments.Comment;
import at.my2c.comments.CommentsAdapter;
import at.my2c.comments.CommentsManager;
import at.my2c.util.NetworkManager;

public final class MainActivity extends ListActivity {
	
	private SharedPreferences settings;

	private List<Comment> comments;
	private CommentsAdapter commentsAdapter;
	
	private String lastUpdateStatus;
	private ProgressDialog progressDialog;
	private TextView statusTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		lastUpdateStatus = getString(R.string.status_last_update) + " ";
		statusTextView = (TextView) findViewById(R.id.StatusTextView);
		
		Button scanButton = (Button) findViewById(R.id.ScanButton);
		scanButton.setOnClickListener(scanListener);
		
		Button searchButton = (Button) findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(searchListener);
		
		Button historyButton = (Button) findViewById(R.id.HistoryButton);
		historyButton.setOnClickListener(historyListener);
		
		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentsAdapter(this, R.layout.comment_item, comments);
		setListAdapter(commentsAdapter);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		String token = settings.getString(getString(R.string.settings_token),"");
		String tokenSecret = settings.getString(getString(R.string.settings_token_secret),"");
		
		CommentsManager.InitializeOAuth(new AccessToken(token, tokenSecret));
		
		showHelpOnFirstLaunch();
	}
	
	private final Button.OnClickListener scanListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final Button.OnClickListener searchListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), SearchActivity.class);
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

		new GetCommentsStreamTask().execute(SettingsActivity.MY_2CENTS_TAG);
	}
	
	/**
	 * We want the help screen to be shown automatically the first time a new
	 * version of the app is run. The easiest way to do this is to check
	 * android:versionCode from the manifest, and compare it to a value stored
	 * as a preference.
	 */
	private void showHelpOnFirstLaunch() {
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
			Log.w(ScanActivity.class.getName(), e);
		}
	}
	
	private class GetCommentsStreamTask extends AsyncTask<String, Void, List<Comment>> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, null, getString(R.string.progress_dialog_loading_comments), true);
	    }
		
		@Override
		protected List<Comment> doInBackground(String... params) {
			return CommentsManager.searchComments(params[0]);
		}
		
		@Override
		protected void onPostExecute(List<Comment> result) {
			if (result == null) {
				progressDialog.dismiss();
				Toast.makeText(MainActivity.this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			}
			else {
				comments = result;
				
				Date date = new Date(System.currentTimeMillis());
      		  	statusTextView.setText(lastUpdateStatus + date.toLocaleString());
				
				commentsAdapter.clear();
				
				if (comments.size() > 0) {
					for (Comment comment : comments) {
						commentsAdapter.add(comment);
					}
					commentsAdapter.notifyDataSetChanged();
					progressDialog.dismiss();
					
					new GetProfileImagesTask().execute(comments);
				}
				else {
					progressDialog.dismiss();
				}
			}
	    }
	}
	
	private class GetProfileImagesTask extends AsyncTask<List<Comment>, Void, Void> {
		
		@Override
		protected Void doInBackground(List<Comment>... params) {			
			for (Comment comment : params[0]) {
				if (!CommentsManager.imagesMap.containsKey(comment.getUser())) {
					CommentsManager.imagesMap.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
				}
				publishProgress();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			commentsAdapter.notifyDataSetChanged();
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!NetworkManager.isNetworkAvailable(this)) {
			return;
		}
		
		if (commentsAdapter.getCount() > 0)
			commentsAdapter.notifyDataSetChanged();
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
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refreshMenuItem: {
				new GetCommentsStreamTask().execute(SettingsActivity.MY_2CENTS_TAG);
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
}
