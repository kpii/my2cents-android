package at.m2c;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.Tweet;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import at.m2c.data.DataManager;
import at.m2c.util.GpsManager;
import at.m2c.util.NetworkManager;
import at.m2c.util.ProviderManager;
import at.m2c.util.RelativeTime;

public final class CommentsActivity extends ListActivity {
	
	private GetComments asyncTask;

	private final static int ACCOUNT_ACTIVITY_CODE = 0;

	private volatile boolean shutdownRequested;
	
	private boolean gpsEnabled;
	
	private LocationManager locationManager;

	private Gallery tagsGallery;

	private ProgressDialog progressDialog;
	private List<Tweet> tweets;
	private TweetsAdapter tweetsAdapter;

	private ArrayList<String> tags;
	private TagAdapter tagsAdapter;

	private HashMap<String, Bitmap> avatarMap = new HashMap<String, Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);

		tweets = new ArrayList<Tweet>();
		tweetsAdapter = new TweetsAdapter(this, R.layout.comment_item, tweets);
		setListAdapter(tweetsAdapter);

		tags = new ArrayList<String>();
		tagsAdapter = new TagAdapter(this, R.layout.tag_item, tags);
		tagsGallery = (Gallery) findViewById(R.id.comment_tag_gallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		Button loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(loginListener);

		Button sendCommentButton = (Button) findViewById(R.id.comment_send_button);
		sendCommentButton.setOnClickListener(sendCommentListener);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String username = preferences.getString(PreferencesActivity.TWITTER_USERNAME, "");
		String password = preferences.getString(PreferencesActivity.TWITTER_PASSWORD, "");
		ProviderManager.Initialize(username, password);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		NetworkManager.checkNetworkAvailability(this);
		if (!NetworkManager.isNetworkAvailable()) {
			Toast.makeText(this, "No Network Connection", Toast.LENGTH_LONG).show();
			return;
		}
		
		shutdownRequested = false;

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		if (intent != null && action != null) {
			if (action.equals(Intents.ACTION)) {
//				if ((asyncTask != null) && (!asyncTask.isCancelled())) {
//					asyncTask.cancel(true);
//					asyncTask = null;
//				}
				
				setTitle("my2cents :: " + DataManager.getSearchTerm());				
				asyncTask = (GetComments) new GetComments().execute(PreferencesActivity.TagPrefix + DataManager.getSearchTerm());
			}
		}
	}
	
	private class GetComments extends AsyncTask<String, Void, List<Tweet>> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(CommentsActivity.this, null, "Loading comments...", true);
	    }
		
		@Override
		protected List<Tweet> doInBackground(String... params) {
			return ProviderManager.search(params[0]);
		}
		
		@Override
		protected void onPostExecute(List<Tweet> result) {
			tweets = result;
	         
			ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.CommentsNotificationLayout);
			
			tweetsAdapter.clear();
			tagsAdapter.clear();
			
			String productTag = PreferencesActivity.ProductCodePrefix + DataManager.getSearchTerm();
			if (tweets != null && tweets.size() > 0) {
				notificationLayout.setVisibility(View.GONE);
				
				Set<String> tags = new TreeSet<String>();
				for (Tweet comment : tweets) {
					tweetsAdapter.add(comment);
					
					String text = comment.getText().replace(productTag, "");
					if (text.contains("#")) {
						Pattern p = Pattern.compile("#[A-Za-z0-9]+");						
						Matcher m = p.matcher(text);
						while (m.find()) {
							tags.add(m.group());
						}
					}
				}
				
				for (String tag : tags) {
					tagsAdapter.add(tag);
				}
				
				tweetsAdapter.notifyDataSetChanged();
				
				progressDialog.dismiss();
				
				new GetProfileImages().execute(tweetsAdapter.items);
			}
			else {
				progressDialog.dismiss();
				
				TextView notificationTextView = (TextView) findViewById(R.id.commentsNotificationTextView);
				notificationTextView.setText("No comments yet. Be the first one to add your 2 cents on this product!");
				notificationLayout.setVisibility(View.VISIBLE);
			}
	    }
	}
	
	private class GetProfileImages extends AsyncTask<List<Tweet>, Void, Void> {
		
		@Override
		protected Void doInBackground(List<Tweet>... params) {			
			URL url = null;
			for (Tweet tweet : params[0]) {
				if (!avatarMap.containsKey(tweet.getFromUser())) {
					try {
						url = new URL(tweet.getProfileImageUrl());
					} catch (MalformedURLException e) {
						Log.e(this.toString(), e.toString());
					}
					avatarMap.put(tweet.getFromUser(), NetworkManager.getRemoteImage(url));
				}
				publishProgress();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			tweetsAdapter.notifyDataSetChanged();
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!NetworkManager.isNetworkAvailable()) {
			return;
		}
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		gpsEnabled = preferences.getBoolean(PreferencesActivity.GPS_ENABLED, false);
		
		boolean isCommentingPossible = preferences.getBoolean(PreferencesActivity.IS_COMMENTING_POSSIBLE, false);
		ProviderManager.setCommentingPossible(isCommentingPossible);
		
		updateCommentUI();
		
		if (tweetsAdapter.getCount() > 0)
			tweetsAdapter.notifyDataSetChanged();
	}

	private final OnItemLongClickListener tagsLongClickListener = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			String text = tagsAdapter.getItem(position);
			EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
			commentEditor.append(text);
			return true;
		}
	};

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		Tweet selectedComment = tweetsAdapter.items.get(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(selectedComment.getText())
		       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void updateCommentUI() {
		ViewGroup commentLayout = (ViewGroup) findViewById(R.id.CommentLayout);
		ViewGroup loginLayout = (ViewGroup) findViewById(R.id.LoginLayout);
		
		if (!ProviderManager.isCommentingPossible()) {
			commentLayout.setVisibility(View.GONE);
			loginLayout.setVisibility(View.VISIBLE);
		}
		else {
			loginLayout.setVisibility(View.GONE);
			commentLayout.setVisibility(View.VISIBLE);
		}
	}
	
	private final Button.OnClickListener loginListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), AccountActivity.class);
			startActivityForResult(intent, ACCOUNT_ACTIVITY_CODE);
		}
	};

	private final Button.OnClickListener sendCommentListener = new Button.OnClickListener() {
		public void onClick(View view) {
			final Runnable commentPosted = new Runnable() {
				public void run() {
					tweetsAdapter.notifyDataSetChanged();
					Toast.makeText(CommentsActivity.this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
				}
			};
			
			Runnable postComment = new Runnable() {
				public void run() {
					EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
					String message = commentEditor.getText().toString() + " " + PreferencesActivity.ProductCodePrefix
							+ DataManager.getSearchTerm();
					
					Location l = null;
					if (gpsEnabled && (locationManager != null)) {
						l = GpsManager.getGPS(locationManager);
					}
					
					GeoLocation location = new GeoLocation(l.getLatitude(), l.getLongitude());
					Status status = ProviderManager.updateStatus(message, location);
//					if (status != null) {
//						Tweet comment = new TweetJSONImpl(status);
//						if (comment != null) {
//							if (avatarMap.containsKey(comment.getFromUser())) {
//								comment.setProfileImage(avatarMap.get(comment.getFromUser()));
//							} else {
//								URL url = null;
//								try {
//									url = new URL(comment.getProfileImageUrl());
//								} catch (MalformedURLException e) {
//									Log.e(this.toString(), e.toString());
//								}
//								comment.setProfileImage(NetworkManager.getRemoteImage(url));
//								avatarMap.put(comment.getFromUser(), comment.getProfileImage());
//							}
//							tweets.add(0, comment);
//						}
//						runOnUiThread(commentPosted);
//					}

					runOnUiThread(dismissProgressDialog);
				}
			};
			
			new Thread(null, postComment, "CommentSender").start();
			progressDialog = ProgressDialog.show(CommentsActivity.this, null, "Sending...", true);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		shutdownRequested = true;
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
			case R.id.scanMenuItem: {
				Intent intent = new Intent(this, CaptureActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACCOUNT_ACTIVITY_CODE: {
				if (resultCode == RESULT_OK) {
					updateCommentUI();
				}
				break;
			}
		}
	}

	private class TagAdapter extends ArrayAdapter<String> {

		private ArrayList<String> items;

		public TagAdapter(Context context, int textViewResourceId, ArrayList<String> tags) {
			super(context, textViewResourceId, tags);
			this.items = tags;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.tag_item, null);
			}

			String tag = items.get(position);
			if (tag != null) {
				TextView tagTextView = (TextView) view.findViewById(R.id.tag_textview);
				if (tagTextView != null) {
					tagTextView.setText(tag);
				}
			}
			return view;
		}
	}

	private class TweetsAdapter extends ArrayAdapter<Tweet> {

		private List<Tweet> items;

		public TweetsAdapter(Context context, int textViewResourceId, List<Tweet> tweets) {
			super(context, textViewResourceId, tweets);
			this.items = tweets;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.comment_item, null);
			}

			Tweet comment = items.get(position);
			if (comment != null) {
				TextView authorTextView = (TextView) view.findViewById(R.id.tweet_author);
				authorTextView.setText(comment.getFromUser());

				TextView messageTextView = (TextView) view.findViewById(R.id.tweet_message);
				if (messageTextView != null) {
					messageTextView.setText(comment.getText());
				}

				TextView sentTextView = (TextView) view.findViewById(R.id.tweet_sent);
				if (sentTextView != null) {
					sentTextView.setText(RelativeTime.getDifference(comment.getCreatedAt().getTime()));
				}

				ImageView avatarImageView = (ImageView) view.findViewById(R.id.tweet_avatar);
				if (avatarImageView != null) {
					avatarImageView.setImageBitmap(avatarMap.get(comment.getFromUser()));
				}
			}
			return view;
		}
	}

	private Runnable dismissProgressDialog = new Runnable() {
		public void run() {
			EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
			commentEditor.setText("");
			progressDialog.dismiss();
		}
	};
}
