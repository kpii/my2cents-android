
package mobi.my2cents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.History;
import mobi.my2cents.data.Product;
import mobi.my2cents.data.ProductInfo;
import mobi.my2cents.data.Rating;
import mobi.my2cents.utils.GpsManager;
import mobi.my2cents.utils.Helper;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.NetworkManager;
import mobi.my2cents.utils.WeakAsyncTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public final class CommentActivity extends ListActivity {
	
	private boolean updateHistory;
	public final static String UPDATE_HISTORY = "UpdateHistory";
	
	private final int DIALOG_PRODUCT_DETAILS = 0;
	
	private SharedPreferences settings;
	private InputMethodManager inputManager;

	private CommentsAdapter adapter;
	
	private IntentFilter filter;
	private ProductUpdaterReceiver receiver;

	private ArrayList<String> tags;
	private TagsAdapter tagsAdapter;
	
	private ProductInfo productInfo;
	private String gtin;
	
	private ProgressDialog progressDialog;
	private Gallery tagsGallery;
	
	private ImageView productImageView;
	private TextView productNameTextView;
	private TextView affiliateTextView;
	private TextView likesTextView;
	private TextView dislikesTextView;
	
	private EditText commentEditor;
	private View statusLayout;
	private View productPanel;
	private PopupWindow productPopup;
	
	private AsyncTask<String, Void, ProductInfo> getProductInfoTask;
	private AsyncTask<List<Comment>, Void, Void> getProfileImagesTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prepareUI();
		
		filter = new IntentFilter(ProductUpdaterService.PRODUCT_UPDATED);
		receiver = new ProductUpdaterReceiver();
		
		DataManager.UnknownProductName = getString(R.string.unknown_product);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		tags = new ArrayList<String>();
		tagsAdapter = new TagsAdapter(this, R.layout.tag_item, tags);		
		
		handleIntent(getIntent());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, filter);
		
		SettingsActivity.setShareOnTwitter(settings.getBoolean(getString(R.string.settings_twitter), false));
		boolean shareLocation = settings.getBoolean(getString(R.string.settings_share_location), false);
		SettingsActivity.setShareLocation(shareLocation);
		if (shareLocation) {
			GpsManager.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		hideVirtualKeyboard();
		String key = intent.getStringExtra(Product.KEY);
		bindAdapter(key);
		getProductInfo(key);		
	}
	
	private void bindAdapter(String key) {		
		Cursor cursor = managedQuery(Uri.withAppendedPath(Product.CONTENT_URI, key + "/comments"), null, null, null, null);
		adapter = new CommentsAdapter(this, cursor);
		setListAdapter(adapter);
	}
	
	private void displayProduct(String key)
	{
		final Cursor cursor = managedQuery(Uri.withAppendedPath(Product.CONTENT_URI, key), null, null, null, null);
		if (cursor.moveToFirst()) {
			productPanel.setVisibility(View.VISIBLE);
			
			productNameTextView.setText(cursor.getString(cursor.getColumnIndex(Product.NAME)));			
			affiliateTextView.setText(cursor.getString(cursor.getColumnIndex(Product.AFFILIATE_NAME)));
			likesTextView.setText("Likes: " + cursor.getInt(cursor.getColumnIndex(Product.RATING_LIKES)));
			dislikesTextView.setText("Dislikes: " + cursor.getInt(cursor.getColumnIndex(Product.RATING_DISLIKES)));
			
			if (ImageManager.hasImage(key)) {
				productImageView.setImageBitmap(ImageManager.getImage(key));
			}			
			else {
				productImageView.setImageResource(R.drawable.unknown_product_icon_inverted);
			}
		}
	}
	
	private void getProductInfo(String key) {
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			productPanel.setVisibility(View.GONE);
			statusLayout.setVisibility(View.VISIBLE);
			Intent intent = new Intent(this, ProductUpdaterService.class);
			intent.putExtra(Product.KEY, key);
			startService(intent);
		}		
	}
	
//	private void updateProduct(Intent intent) {
//		if (!NetworkManager.isNetworkAvailable(this)) {
//			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
//		}
//		else {
//			String action = intent == null ? null : intent.getAction();
//			if (intent != null && action != null) {
//				if (action.equals(Intents.ACTION)) {
//					gtin = intent.getStringExtra(Product.KEY);
//					updateHistory = intent.getBooleanExtra(UPDATE_HISTORY, true);
//					
//					getProductInfoTask = new GetProductInfoTask(this).execute(gtin);
//				}
//			}
//		}
//	}
	
	private void prepareUI() {
		setContentView(R.layout.comment);
		
		statusLayout = findViewById(R.id.StatusRelativeLayout);
		
		productImageView = (ImageView) findViewById(R.id.ProductImageView);
		productImageView.setOnClickListener(productImageListener);
		
		productNameTextView = (TextView) findViewById(R.id.ProductNameTextView);
		affiliateTextView = (TextView) findViewById(R.id.AffiliateTextView);
		likesTextView = (TextView) findViewById(R.id.LikesTextView);
		dislikesTextView = (TextView) findViewById(R.id.DislikesTextView);
		
		commentEditor = (EditText) findViewById(R.id.CommentEditText);
		commentEditor.setOnEditorActionListener(sendCommentActionListener);
		
		tagsGallery = (Gallery) findViewById(R.id.TagsGallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		productPanel = findViewById(R.id.ProductInfoPanel);
		productPanel.setOnClickListener(productQuickActionsListener);
		findViewById(R.id.LoginButton).setOnClickListener(loginListener);
		findViewById(R.id.SendButton).setOnClickListener(sendCommentListener);
		
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setOnClickListener(historyListener);
	}
	
	private final View.OnClickListener homeListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener scanListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener historyListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener streamListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), FeedActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener productImageListener = new View.OnClickListener() {
		public void onClick(View view) {
			if (productInfo != null) {
				showDialog(DIALOG_PRODUCT_DETAILS);
			}
		}
	};
	
	private final View.OnClickListener productQuickActionsListener = new View.OnClickListener() {
		public void onClick(View view) {
			if (productPopup == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				View contentView = inflater.inflate(R.layout.product_popup, null, false);
				contentView.setOnTouchListener(closePopupListener);
				contentView.findViewById(R.id.ProductBarButtonDetails).setOnClickListener(affiliateListener);
				contentView.findViewById(R.id.ProductBarButtonLike).setOnClickListener(likeListener);
				contentView.findViewById(R.id.ProductBarButtonDislike).setOnClickListener(dislikeListener);
				contentView.findViewById(R.id.ProductBarButtonEdit).setOnClickListener(editProductInfoListener);
				
				productPopup = new PopupWindow(
						contentView, 
						view.getWidth(), 
						R.dimen.product_bar_height, 
						false);
			    // The code below assumes that the root container has an id called 'main'
				productPopup.setOutsideTouchable(true);
				productPopup.setTouchInterceptor(closePopupListener);
				productPopup.showAsDropDown(view);
			}
		}
	};
	
	private void closeProductPopupBar() {
		if (productPopup != null) {
			productPopup.dismiss();
			productPopup = null;
			
		}
	}
	
	private final View.OnTouchListener closePopupListener = new View.OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				closeProductPopupBar();
				return true;
			}
			return false;
		}
	};
	
	private final View.OnClickListener affiliateListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (productInfo != null) {
				if (productInfo.getAffiliateUrl() != null) {
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(productInfo.getAffiliateUrl()));  
					startActivity(viewIntent);
				}
			}
		}
	};
	
	private final View.OnClickListener likeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (productInfo != null) {
				rateProduct(view.getContext(), "like");
			}
		}
	};
	
	private final View.OnClickListener dislikeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (productInfo != null) {
				rateProduct(view.getContext(), "dislike");
			}
		}
	};
	
	private final View.OnClickListener editProductInfoListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (productInfo != null) {
				
			}
		}
	};
	
	private final View.OnClickListener loginListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(intent);
		}
	};

	private final View.OnClickListener sendCommentListener = new View.OnClickListener() {
		public void onClick(View view) {
			postComment(view.getContext());
		}
	};
	
	private final TextView.OnEditorActionListener sendCommentActionListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				postComment(view.getContext());
				return true;
			}			
			return false;
		}
	};
	
	private void postComment(Context context) {
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			String message = commentEditor.getText().toString();
			new PostComment(context).execute(message);
		}
	}
	
	private void rateProduct(Context context, String value) {
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			new PutRating(context).execute(value);
		}
	}
	
	private void hideVirtualKeyboard() {
		inputManager.hideSoftInputFromWindow(commentEditor.getWindowToken(), 0);
        commentEditor.clearFocus();
        productImageView.requestFocus();		
	}
	
	private void addHistoryItem(ProductInfo item) {
		
		ContentResolver resolver = getContentResolver();
		resolver.delete(Uri.withAppendedPath(History.CONTENT_URI, item.getGtin()), null, null);
		
		ContentValues values = new ContentValues();
		values.put( History.PRODUCT_KEY, item.getGtin());
		values.put( History.NAME, item.getName());
		values.put( History.TIME, new Date().toLocaleString());
		values.put( History.AFFILIATE_NAME, item.getAffiliateName());
		values.put( History.AFFILIATE_URL, item.getAffiliateUrl());		
        if (item.getImage() != null) {
        	values.put(History.IMAGE, Helper.getBitmapAsByteArray(item.getImage()));
        }
		
		resolver.insert(History.CONTENT_URI, values);
	}
	
	private ProductInfo getHistoryItem(String gtin) {
		ProductInfo item = null;
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(Uri.withAppendedPath(History.CONTENT_URI, gtin), null, null, null, null);
    	if (cursor.moveToFirst()) {
    		item = new ProductInfo(gtin);
    		item.setName(cursor.getString(cursor.getColumnIndex(History.NAME)));
    		item.setAffiliateName(cursor.getString(cursor.getColumnIndex(History.AFFILIATE_NAME)));
    		item.setAffiliateUrl(cursor.getString(cursor.getColumnIndex(History.AFFILIATE_URL)));
	    	
	    	byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(History.IMAGE));
			if (bitmapArray != null) {
				Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
				item.setImage(bitmap);
			}
    	}
    	cursor.close();
    	return item;
	}
	
	private class GetProductInfoTask extends WeakAsyncTask<String, Void, ProductInfo, Context> {
		public GetProductInfoTask(Context target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Context target) {
			statusLayout.setVisibility(View.VISIBLE);
			productInfo = getHistoryItem(gtin);
			if (productInfo != null) {
//				displayProductFound(productInfo);
			}
	    }

		@Override
		protected ProductInfo doInBackground(Context target, String... params) {
			ProductInfo result = DataManager.getProductInfo(gtin);
			
			if (updateHistory) {
				if (result != null) {
					addHistoryItem(result);
				}
				else {
					ProductInfo dummy = new ProductInfo(params[0]);
					addHistoryItem(dummy);
				}
			}
			return result;
		}
		
//		@Override
//		protected void onPostExecute(Context target, ProductInfo product) {
//			statusLayout.setVisibility(View.GONE);
//			//progressDialog.dismiss();
//			
//	        if (product != null) {
//	        	displayProductFound(product);
//	        	
//	        	commentsAdapter.clear();
//				tagsAdapter.clear();
//	        	ArrayList<Comment> comments = product.getComments();
//				if (comments.size() > 0) {
//					Set<String> tags = new TreeSet<String>();
//					for (Comment comment : comments) {
//						commentsAdapter.add(comment);
//						
//						String text = comment.getText();
//						if (text.contains("#")) {
//							Pattern p = Pattern.compile("#[A-Za-z0-9]+");						
//							Matcher m = p.matcher(text);
//							while (m.find()) {
//								tags.add(m.group());
//							}
//						}
//					}
//					
//					if (tags.size() > 0) {
//						for (String tag : tags) {
//							tagsAdapter.add(tag);
//						}
//					}
//					
//					commentsAdapter.notifyDataSetChanged();
//					
//					getProfileImagesTask = new GetProfileImagesTask(target).execute(comments);
//				}
//			}
//			else {
//				Toast.makeText(target, R.string.error_message_no_server_connection, Toast.LENGTH_LONG).show();
//			}
//	        
//	        productInfo = product;
//	        getProductInfoTask = null;
//	        
//	        hideVirtualKeyboard();
//	    }
	}
	
	private class GetProfileImagesTask extends WeakAsyncTask<List<Comment>, Void, Void, Context> {
		
		public GetProfileImagesTask(Context target) {
			super(target);
		}

		@Override
		protected Void doInBackground(Context target, List<Comment>... params) {			
//			for (Comment comment : params[0]) {
//				if (!DataManager.profileImageCache.containsKey(comment.getUser())) {
//					DataManager.profileImageCache.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
//				}
//				publishProgress();
//			}
//			getProfileImagesTask = null;
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
//			commentsAdapter.notifyDataSetChanged();
	    }
	}

	private final OnItemLongClickListener tagsLongClickListener = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
			String text = tagsAdapter.getItem(position);
			EditText commentEditor = (EditText) findViewById(R.id.CommentEditText);
			commentEditor.append(text);
			return true;
		}
	};

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
//		Comment selectedComment = commentsAdapter.getItem(position);
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage(selectedComment.getText())
//		       .setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) {
//		                dialog.dismiss();
//		           }
//		       });
//		
//		if ((selectedComment.getUser() == null) || (selectedComment.getUser() == "")) {
//			builder.setTitle(R.string.anonymous_username);
//		}
//		else {
//			builder.setTitle(selectedComment.getUser());
//		}
//		
//		AlertDialog alert = builder.create();
//		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.comments_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
			case R.id.thumbsUpMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				Toast.makeText(getApplicationContext(), "you like it", 3000).show();
				return true;
			}
			case R.id.thumbsDownMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				// vote product
				Toast.makeText(getApplicationContext(), "you dont like it", 3000).show();
				return true;
			}
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	
	private class PostComment extends WeakAsyncTask<String, Void, Comment, Context> {

		public PostComment(Context target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Context target) {
			progressDialog = ProgressDialog.show(CommentActivity.this, null, getString(R.string.progress_dialog_sending), true);
	    }
		
		@Override
		protected Comment doInBackground(Context target, String... params) {
			
			Comment comment = DataManager.postComment(gtin, params[0]);
//			if (comment != null) {
//				if (!DataManager.profileImageCache.containsKey(comment.getUser())) {
//					DataManager.profileImageCache.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
//				}
//			}
			return comment;
		}
		
//		@Override
//		protected void onPostExecute(Context target, Comment comment) {
//			progressDialog.dismiss();
//			if (comment != null) {
//				hideVirtualKeyboard();
//				
//				commentsAdapter.insert(comment, 0);
//				commentsAdapter.notifyDataSetChanged();
//				
//				commentEditor.setText("");
//				Toast.makeText(CommentActivity.this, R.string.message_comment_posted_successfully, Toast.LENGTH_SHORT).show();
//			}
//			else {
//				Toast.makeText(target, R.string.error_message_no_server_connection, Toast.LENGTH_LONG).show();
//			}
//	    }
	}
	
	private class PutRating extends WeakAsyncTask<String, Void, Rating, Context> {

		public PutRating(Context target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Context target) {
			progressDialog = ProgressDialog.show(target, null, "Rating product...", true);
	    }
		
		@Override
		protected Rating doInBackground(Context target, String... params) {
			return DataManager.rateProduct(gtin, params[0]);
		}
		
		@Override
		protected void onPostExecute(Context target, Rating result) {
			progressDialog.dismiss();
			if (result == null) {
				Toast.makeText(target, R.string.rating_unsuccesful, Toast.LENGTH_LONG).show();
			}
			else {
				productInfo.setRating(result);
//				displayProductFound(productInfo);
				Toast.makeText(target, R.string.rating_successful, Toast.LENGTH_SHORT).show();
			}
	    }
	}
	
	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
		closeProductPopupBar();
		prepareUI();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		switch (id) {
			case DIALOG_PRODUCT_DETAILS: {
				LayoutInflater factory = LayoutInflater.from(this);
		        final View view = factory.inflate(R.layout.product_details_dialog, null);
		        ImageView imageView = (ImageView) view.findViewById(R.id.ProductImageView);
		        imageView.setImageBitmap(productInfo.getImage());
		        return new AlertDialog.Builder(this)
		            .setIcon(android.R.drawable.ic_dialog_info)
		            .setTitle(productInfo.getGtin())
		            .setView(view)
		            .setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() {
		                public void onClick(DialogInterface dialog, int whichButton) {
		                	
		                }
		            })
		            .create();
	        }
		}
		return super.onCreateDialog(id);
	}
	
	private final class ProductUpdaterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			statusLayout.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			final String key = intent.getStringExtra(Product.KEY);
			displayProduct(key);
		}
		
	}
}
