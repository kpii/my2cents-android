
package at.my2c;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.http.AccessToken;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
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
import at.my2c.comments.Comment;
import at.my2c.comments.CommentsManager;
import at.my2c.data.DataManager;
import at.my2c.data.ProductInfo;
import at.my2c.data.ProductInfoManager;
import at.my2c.util.GpsManager;
import at.my2c.util.NetworkManager;
import at.my2c.util.RelativeTime;

public final class MainActivity extends ListActivity {

	private final static int ACCOUNT_ACTIVITY_CODE = 0;
	
	private SharedPreferences preferences;
	
	private boolean locationEnabled;
	
	private LocationManager locationManager;

	private Gallery tagsGallery;

	private ProgressDialog progressDialog;
	private List<Comment> comments;
	private CommentsAdapter commentsAdapter;

	private ArrayList<String> tags;
	private TagAdapter tagsAdapter;

	private HashMap<String, Bitmap> avatarMap = new HashMap<String, Bitmap>();
	
	private ProductInfo productInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentsAdapter(this, R.layout.comment_item, comments);
		setListAdapter(commentsAdapter);

		tags = new ArrayList<String>();
		tagsAdapter = new TagAdapter(this, R.layout.tag_item, tags);
		tagsGallery = (Gallery) findViewById(R.id.comment_tag_gallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		Button loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(loginListener);

		Button sendCommentButton = (Button) findViewById(R.id.comment_send_button);
		sendCommentButton.setOnClickListener(sendCommentListener);
		
		ViewGroup productInfoLayout = (ViewGroup) findViewById(R.id.ProductInfoLayout);
		registerForContextMenu(productInfoLayout);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useOauth = preferences.getBoolean(PreferencesActivity.USE_OAUTH, true);
		if (useOauth) {
			String token = preferences.getString(PreferencesActivity.OAUTH_TOKEN, "");
			String tokenSecret = preferences.getString(PreferencesActivity.OAUTH_TOKEN_SECRET, "");
			CommentsManager.InitializeOAuth(new AccessToken(token, tokenSecret));
		}
		else {
			String username = preferences.getString(PreferencesActivity.TWITTER_USERNAME, "");
			String password = preferences.getString(PreferencesActivity.TWITTER_PASSWORD, "");
			CommentsManager.InitializeBasic(username, password);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.product_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.productDetailContextMenuItem: {
				if (productInfo != null) {
					if (productInfo.getDetailPageUrl() != null) {
						Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(productInfo.getDetailPageUrl()));  
						startActivity(viewIntent);
					}					
				}				
				return true;
			}
//			case R.id.productAddToFavoritesContextMenuItem: {
//				if (productInfo != null) {
//					DataManager.getDatabase().addFavoriteItem(productInfo);
//				}				
//				return true;
//			}
			default:
				return super.onContextItemSelected(item);
		}		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			return;
		}

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		if (intent != null && action != null) {
			if (action.equals(Intents.ACTION)) {
				setTitle(getString(R.string.main_activity_title_prefix) + DataManager.getSearchTerm());
				
				new GetProductInfo().execute(DataManager.getSearchTerm());
				new GetComments().execute(PreferencesActivity.TagPrefix + DataManager.getSearchTerm());
			}
		}
	}
	
	private class GetComments extends AsyncTask<String, Void, List<Comment>> {

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
		         
				ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.CommentsNotificationLayout);
				
				commentsAdapter.clear();
				tagsAdapter.clear();
				
				String productTag = PreferencesActivity.ProductCodePrefix + DataManager.getSearchTerm();
				if (comments.size() > 0) {
					notificationLayout.setVisibility(View.GONE);
					
					Set<String> tags = new TreeSet<String>();
					for (Comment comment : comments) {
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
					
					new GetProfileImages().execute(commentsAdapter.items);
				}
				else {
					progressDialog.dismiss();
					
					TextView notificationTextView = (TextView) findViewById(R.id.commentsNotificationTextView);
					notificationTextView.setText(R.string.notification_message_no_comments);
					notificationLayout.setVisibility(View.VISIBLE);
				}
			}
	    }
	}
	
	private class GetProfileImages extends AsyncTask<List<Comment>, Void, Void> {
		
		@Override
		protected Void doInBackground(List<Comment>... params) {			
			for (Comment comment : params[0]) {
				if (!avatarMap.containsKey(comment.getUser())) {
					avatarMap.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
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
		
		
		locationEnabled = preferences.getBoolean(PreferencesActivity.GPS_ENABLED, false);
		
		boolean isCommentingPossible = preferences.getBoolean(PreferencesActivity.IS_COMMENTING_POSSIBLE, false);
		CommentsManager.setCommentingPossible(isCommentingPossible);
		
		updateCommentUI();
		
		if (commentsAdapter.getCount() > 0)
			commentsAdapter.notifyDataSetChanged();
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
		Comment selectedComment = commentsAdapter.items.get(position);
		
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
	
	private void updateCommentUI() {
		ViewGroup commentLayout = (ViewGroup) findViewById(R.id.CommentLayout);
		ViewGroup loginLayout = (ViewGroup) findViewById(R.id.LoginLayout);
		
		if (!CommentsManager.isCommentingPossible()) {
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
			boolean useOauth = preferences.getBoolean(PreferencesActivity.USE_OAUTH, true);
			if (useOauth) {
				Intent intent = new Intent(getBaseContext(), AuthorizationActivity.class);
				startActivityForResult(intent, ACCOUNT_ACTIVITY_CODE);
			}
			else {
				Intent intent = new Intent(getBaseContext(), AccountActivity.class);
				startActivityForResult(intent, ACCOUNT_ACTIVITY_CODE);
			}
		}
	};

	private final Button.OnClickListener sendCommentListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
			String message = commentEditor.getText().toString() +
				" " +
				PreferencesActivity.ProductCodePrefix +
				DataManager.getSearchTerm();
			
			new PostComment().execute(message);
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
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
			case R.id.historyMenuItem: {
				Intent intent = new Intent(this, HistoryActivity.class);
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

	private class CommentsAdapter extends ArrayAdapter<Comment> {

		private List<Comment> items;

		public CommentsAdapter(Context context, int textViewResourceId, List<Comment> comments) {
			super(context, textViewResourceId, comments);
			this.items = comments;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.comment_item, null);
			}

			Comment comment = items.get(position);
			if (comment != null) {
				TextView authorTextView = (TextView) view.findViewById(R.id.tweet_author);
				authorTextView.setText(comment.getUser());

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
					avatarImageView.setImageBitmap(avatarMap.get(comment.getUser()));
				}
			}
			return view;
		}
	}
	
	
	private class GetProductInfo extends AsyncTask<String, Void, ProductInfo> {

		@Override
		protected ProductInfo doInBackground(String... params) {
			ProductInfo product = ProductInfoManager.getProductFromAmazon(params[0]);
			
			if (product != null) {
				URL url = null;
				try {
					url = new URL(product.getProductImageUrl());
				} catch (MalformedURLException e) {
					Log.e(this.toString(), e.toString());
				}
				product.setProductImage(NetworkManager.getRemoteImage(url));
				
				DataManager.getDatabase().addHistoryItem(product);
			}
			else {
				ProductInfo emptyProduct = new ProductInfo(params[0]);
				emptyProduct.setProductCode(params[0]);
				DataManager.getDatabase().addHistoryItem(emptyProduct);
			}
			
			return product;
		}
		
		@Override
		protected void onPostExecute(ProductInfo product) {
	         
	        ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.NotificationLayout);
	        ViewGroup productInfoLayout = (ViewGroup) findViewById(R.id.ProductInfoLayout);
				
	        if (product != null) {
	        	productInfo = product;
	        	
	        	notificationLayout.setVisibility(View.GONE);
	        	productInfoLayout.setVisibility(View.VISIBLE);
				
	        	TextView productNameTextView = (TextView) findViewById(R.id.product_name_textview);
				productNameTextView.setText(product.getProductId());

				TextView productInfoProviderTextView = (TextView) findViewById(R.id.product_info_provider_textview);
				productInfoProviderTextView.setText(product.getProductInfoProvider().toString());

				TextView productDescriptionTextView = (TextView) findViewById(R.id.product_description_textview);
				productDescriptionTextView.setText(product.getProductName());
				
				ImageView productImageView = (ImageView) findViewById(R.id.product_imageview);
				productImageView.setImageBitmap(product.getProductImage());
			}
			else {
				productInfo = null;
				
				TextView notificationTextView = (TextView) findViewById(R.id.notificationTextView);
				notificationTextView.setText(R.string.notification_message_no_products_found);
				notificationLayout.setVisibility(View.VISIBLE);
				productInfoLayout.setVisibility(View.GONE);
			}
	    }
	}
	
	
	private class PostComment extends AsyncTask<String, Void, Comment> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, null, getString(R.string.progress_dialog_sending), true);
	    }
		
		@Override
		protected Comment doInBackground(String... params) {
			
			Comment comment = (locationEnabled) ? 
				CommentsManager.sendComment(params[0], GpsManager.getGPS(locationManager)) :
				CommentsManager.sendComment(params[0], null);
			
			if (comment != null) {
				if (!avatarMap.containsKey(comment.getUser())) {
					avatarMap.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
				}
			}
			
			return comment;
		}
		
		@Override
		protected void onPostExecute(Comment comment) {
			if (comment != null) {
				ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.CommentsNotificationLayout);
				notificationLayout.setVisibility(View.GONE);
				
				commentsAdapter.insert(comment, 0);
				commentsAdapter.notifyDataSetChanged();
				
				EditText commentEditor = (EditText) findViewById(R.id.comment_edittext);
				commentEditor.setText("");
				Toast.makeText(MainActivity.this, R.string.message_comment_posted_successfully, Toast.LENGTH_SHORT).show();
			}

			progressDialog.dismiss();
	    }
	}
}
