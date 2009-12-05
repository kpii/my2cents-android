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

import twitter4j.Status;
import twitter4j.Tweet;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemLongClickListener;
import at.m2c.data.DataManager;
import at.m2c.data.ProductInfo;
import at.m2c.data.ProductInfoManager;
import at.m2c.scanner.CaptureActivityHandler;
import at.m2c.util.GpsManager;
import at.m2c.util.NetworkManager;
import at.m2c.util.ProviderManager;

public final class CommentActivity extends ListActivity {

	private final static int MANUAL_INPUT_CODE = 0;
	private final static int ACCOUNT_ACTIVITY_CODE = 1;

	private volatile boolean shutdownRequested;
	
	private boolean gpsEnabled;
	
	private final static int numberOfResults = 30;
	private LocationManager locationManager;

	public CaptureActivityHandler mHandler;

	private Gallery tagsGallery;

	private ProgressDialog progressDialog;
	private List<Tweet> comments;
	private CommentAdapter commentsAdapter;

	private ArrayList<String> tags;
	private ArrayAdapter<String> tagsAdapter;

	private HashMap<String, Bitmap> avatarMap = new HashMap<String, Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		comments = new ArrayList<Tweet>();
		commentsAdapter = new CommentAdapter(this, R.layout.row, comments);
		setListAdapter(commentsAdapter);

		tags = new ArrayList<String>();
		tagsAdapter = new TagAdapter(this, R.layout.tag, tags);
		tagsGallery = (Gallery) findViewById(R.id.comment_tag_gallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);

		ToggleButton productToggleButton = (ToggleButton) findViewById(R.id.productToggleButton);
		productToggleButton.setOnClickListener(productToggleListener);

		ToggleButton commentToggleButton = (ToggleButton) findViewById(R.id.commentToggleButton);
		commentToggleButton.setOnClickListener(commentToggleListener);
		
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
				setTitle("my2cents :: " + DataManager.getProductInfo().getProductCode());
				
				TextView productNameTextView = (TextView) findViewById(R.id.productNameTextView);
				productNameTextView.setText("Loading product information...");
				
				new Thread(null, updateProductInfo, "ProductInfoUpdater").start();
				new Thread(null, viewComments, "CommentsLoader").start();

				progressDialog = ProgressDialog.show(CommentActivity.this, null, "Loading data...", true);
			}
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
		Tweet selectedComment = commentsAdapter.comments.get(position);
		
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

	private final Button.OnClickListener productToggleListener = new Button.OnClickListener() {
		public void onClick(View view) {
			ViewGroup layout = (ViewGroup) findViewById(R.id.ProductLayout);
			if (((ToggleButton) view).isChecked())
				layout.setVisibility(View.VISIBLE);
			else
				layout.setVisibility(View.GONE);
		}
	};

	private final Button.OnClickListener commentToggleListener = new Button.OnClickListener() {
		public void onClick(View view) {
			updateCommentUI(((ToggleButton) view).isChecked());
		}
	};
	
	private void updateCommentUI(boolean isVisible) {
		ViewGroup commentLayout = (ViewGroup) findViewById(R.id.CommentLayout);
		ViewGroup loginLayout = (ViewGroup) findViewById(R.id.LoginLayout);
		
		if (isVisible) {
			if (!ProviderManager.isCommentingPossible()) {
				commentLayout.setVisibility(View.GONE);
				loginLayout.setVisibility(View.VISIBLE);
			}
			else {
				loginLayout.setVisibility(View.GONE);
				commentLayout.setVisibility(View.VISIBLE);
			}
		}
		else {
			commentLayout.setVisibility(View.GONE);
			loginLayout.setVisibility(View.GONE);
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
					Toast.makeText(CommentActivity.this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
				}
			};
			
			Runnable postComment = new Runnable() {
				public void run() {
					EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
					String message = commentEditor.getText().toString() + " " + PreferencesActivity.ProductCodePrefix
							+ DataManager.getProductInfo().getProductCode();
					
					Location l = null;
					if (gpsEnabled && (locationManager != null)) {
						l = GpsManager.getGPS(locationManager);
					}
					
					Status status = ProviderManager.updateStatus(message, l);
					if (status != null) {
						Tweet comment = new Tweet(status);
						if (comment != null) {
							if (avatarMap.containsKey(comment.getFromUser())) {
								comment.setProfileImage(avatarMap.get(comment.getFromUser()));
							} else {
								URL url = null;
								try {
									url = new URL(comment.getProfileImageUrl());
								} catch (MalformedURLException e) {
									Log.e(this.toString(), e.toString());
								}
								comment.setProfileImage(NetworkManager.getRemoteImage(url));
								avatarMap.put(comment.getFromUser(), comment.getProfileImage());
							}
							comments.add(0, comment);
							runOnUiThread(displayComments);
						}
						runOnUiThread(commentPosted);
					}

					runOnUiThread(dismissProgressDialog);
				}
			};
			
			new Thread(null, postComment, "CommentSender").start();
			progressDialog = ProgressDialog.show(CommentActivity.this, null, "Sending...", true);
		}
	};

	private Runnable updateProductInfo = new Runnable() {
		public void run() {
			ProductInfo product = DataManager.getProductInfo();
			ProductInfoManager.updateProductInfo(product);
			DataManager.getHistoryDatabase().AddEntry(product);
			runOnUiThread(displayProductInfo);
		}
	};

	private Runnable displayProductInfo = new Runnable() {
		public void run() {
			ProductInfo productInfo = DataManager.getProductInfo();
			
			ViewGroup layout = (ViewGroup) findViewById(R.id.ProductLayout);
			TextView productNameTextView = (TextView) findViewById(R.id.productNameTextView);
			ToggleButton toggleButton = (ToggleButton) findViewById(R.id.productToggleButton);
			
			if ((productInfo == null) || (productInfo.getProductName() == null) || (productInfo.getProductName().equals(""))) {
				Toast.makeText(CommentActivity.this, "Product information was not found.", Toast.LENGTH_SHORT).show();
				productNameTextView.setText("Product information was not found.");
				toggleButton.setChecked(false);
				layout.setVisibility(View.GONE);
				
				return;
			}
			else {
				productNameTextView.setText(productInfo.getProductName());
				toggleButton.setChecked(true);
				layout.setVisibility(View.VISIBLE);
				
				if (productInfo.getProductImage() != null) {
					ImageView productImageView = (ImageView) findViewById(R.id.productImageView);
					productImageView.setImageBitmap(productInfo.getProductImage());
				}
			}
		}
	};

	private Runnable viewComments = new Runnable() {
		public void run() {
			searchComments(PreferencesActivity.TagPrefix + DataManager.getProductInfo().getProductCode());
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
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
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, ManualInputActivity.class);
				startActivityForResult(intent, MANUAL_INPUT_CODE);
				break;
			}
			case R.id.historyMenuItem: {
				Intent intent = new Intent(this, HistoryActivity.class);
				startActivity(intent);
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
	
						Intent intent = new Intent(this, CommentActivity.class);
						intent.setAction(Intents.ACTION);
						startActivity(intent);
					}
				}
				break;
			}
			case ACCOUNT_ACTIVITY_CODE: {
				if (resultCode == RESULT_OK) {
					ToggleButton commentToggleButton = (ToggleButton) findViewById(R.id.commentToggleButton);
					updateCommentUI(commentToggleButton.isChecked());
				}
				break;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
	}

	private class TagAdapter extends ArrayAdapter<String> {

		private ArrayList<String> tags;

		public TagAdapter(Context context, int textViewResourceId, ArrayList<String> tags) {
			super(context, textViewResourceId, tags);
			this.tags = tags;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.tag, null);
			}

			String tag = tags.get(position);
			if (tag != null) {
				TextView tagTextView = (TextView) view.findViewById(R.id.tag_textview);
				if (tagTextView != null) {
					tagTextView.setText(tag);
				}
			}
			return view;
		}
	}

	private class CommentAdapter extends ArrayAdapter<Tweet> {

		private List<Tweet> comments;

		public CommentAdapter(Context context, int textViewResourceId, List<Tweet> comments) {
			super(context, textViewResourceId, comments);
			this.comments = comments;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.row, null);
			}

			Tweet comment = comments.get(position);
			if (comment != null) {
				TextView authorTextView = (TextView) view.findViewById(R.id.tweet_author);
				authorTextView.setText(comment.getFromUser());

				TextView messageTextView = (TextView) view.findViewById(R.id.tweet_message);
				if (messageTextView != null) {
					messageTextView.setText(comment.getText());
				}

				TextView sentTextView = (TextView) view.findViewById(R.id.tweet_sent);
				if (sentTextView != null) {
					sentTextView.setText(comment.getRelativeTime());
				}

				ImageView avatarImageView = (ImageView) view.findViewById(R.id.tweet_avatar);
				if (avatarImageView != null) {
					avatarImageView.setImageBitmap(comment.getProfileImage());
				}
			}
			return view;
		}
	}

	private void searchComments(String searchTerm) {
		try {
			comments = ProviderManager.search(searchTerm, numberOfResults);
		} catch (Exception e) {
			Log.e(this.toString(), e.getMessage());
		}
		runOnUiThread(displayComments);
	}

	private Runnable dismissProgressDialog = new Runnable() {
		public void run() {
			EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
			commentEditor.setText("");
			progressDialog.dismiss();
		}
	};

	private Runnable displayComments = new Runnable() {
		public void run() {
			ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.NotificationLayout);
			
			commentsAdapter.clear();
			tagsAdapter.clear();
			
			String productTag = PreferencesActivity.ProductCodePrefix + DataManager.getProductInfo().getProductCode();
			if (comments != null && comments.size() > 0) {
				notificationLayout.setVisibility(View.GONE);
				
				Set<String> tags = new TreeSet<String>();
				for (Tweet comment : comments) {
					commentsAdapter.add(comment);
					
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
				
				commentsAdapter.notifyDataSetChanged();
				
				progressDialog.dismiss();
				new Thread(null, loadAvatars, "AvatarLoader").start();
			}
			else {
				progressDialog.dismiss();
				
				TextView notificationTextView = (TextView) findViewById(R.id.notificationTextView);
				notificationTextView.setText("No comments yet. Be the first one to add your 2 cents on this product! Click on 'Add my 2 cents'");
				notificationLayout.setVisibility(View.VISIBLE);
			}
		}
	};

	private Runnable loadAvatars = new Runnable() {
		public void run() {
			URL url = null;
			for (Tweet comment : commentsAdapter.comments) {
				if (shutdownRequested)
					return;
				if (avatarMap.containsKey(comment.getFromUser())) {
					comment.setProfileImage(avatarMap.get(comment.getFromUser()));
				} else {
					try {
						url = new URL(comment.getProfileImageUrl());
					} catch (MalformedURLException e) {
						Log.e(this.toString(), e.toString());
					}
					comment.setProfileImage(NetworkManager.getRemoteImage(url));
					avatarMap.put(comment.getFromUser(), comment.getProfileImage());
				}
				runOnUiThread(refreshComments);
			}
		}
	};

	private Runnable refreshComments = new Runnable() {
		public void run() {
			commentsAdapter.notifyDataSetChanged();
		}
	};
}
