
package mobi.my2cents;

import java.util.Date;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import mobi.my2cents.utils.GpsManager;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.NetworkManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public final class ProductActivity extends ListActivity {
	
	private final int DIALOG_PRODUCT_DETAILS = 0;
	
	private SharedPreferences settings;
	private InputMethodManager inputManager;
	
	private ProductUpdaterReceiver productUpdaterReceiver;
	private SyncReceiver syncReceiver;
	private ImageDownloaderReceiver imageDownloaderReceiver;
	
	private View productPanel;
	private ImageView productImageView;
	private TextView productNameTextView;
	private TextView affiliateTextView;
	private Button buttonLikes;
	private Button buttonDislikes;
	
	private EditText commentEditor;
	private PopupWindow productPopup;
	
	private ProgressBar progressBar;
	private TextView statusTextView;
	
	private static Uri product;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		productUpdaterReceiver = new ProductUpdaterReceiver();
		syncReceiver = new SyncReceiver();
		imageDownloaderReceiver = new ImageDownloaderReceiver();
		
		prepareUI();

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);		
				
		handleIntent(getIntent());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(productUpdaterReceiver, ProductGetterService.FILTER);
		registerReceiver(syncReceiver, SyncService.FILTER);
		registerReceiver(imageDownloaderReceiver, ImageDownloaderService.FILTER);
	}
	
	@Override
	public void onPause() {
		unregisterReceiver(productUpdaterReceiver);
		unregisterReceiver(syncReceiver);
		unregisterReceiver(imageDownloaderReceiver);
		super.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		hideVirtualKeyboard();
		product = intent.getData();
		bindAdapter();
		displayProduct();
		getProductInfo();
	}
	
	private void bindAdapter() {
		final Cursor cursor = managedQuery(Uri.withAppendedPath(product, "comments"), null, null, null, null);
		setListAdapter(new ProductAdapter(this, cursor));
	}
	
	private void displayProduct()
	{
		final Cursor cursor = managedQuery(product, null, null, null, null);
		if (cursor.moveToFirst()) {
			
			final String name = cursor.getString(cursor.getColumnIndex(Product.NAME));
			final String affiliateName = cursor.getString(cursor.getColumnIndex(Product.AFFILIATE_NAME));
			final String affiliateUrl = cursor.getString(cursor.getColumnIndex(Product.AFFILIATE_URL));
			final String imageUrl = cursor.getString(cursor.getColumnIndex(Product.IMAGE_URL));
			final int likes = cursor.getInt(cursor.getColumnIndex(Product.RATING_LIKES));
			final int dislikes = cursor.getInt(cursor.getColumnIndex(Product.RATING_DISLIKES));
			final String personalRating = cursor.getString(cursor.getColumnIndex(Product.RATING_PERSONAL));
			
			productNameTextView.setText(name);			
			
			if (TextUtils.isEmpty(affiliateName) || affiliateName.equals("null")) {
				affiliateTextView.setVisibility(View.GONE);
			}
			else {
				affiliateTextView.setText(Html.fromHtml("productinfo by <a href='" + affiliateUrl + "'>" + affiliateName + "</a>"));
				affiliateTextView.setVisibility(View.VISIBLE);
			}
			
			buttonLikes.setText(Integer.toString(likes));
			buttonDislikes.setText(Integer.toString(dislikes));
			
			if (!TextUtils.isEmpty(personalRating)) {
				if (personalRating.equals("like")) {
					buttonLikes.setEnabled(false);
					buttonDislikes.setEnabled(true);
				}
				else if (personalRating.equals("dislike")) {
					buttonLikes.setEnabled(true);
					buttonDislikes.setEnabled(false);
				}
			}

			productImageView.setImageBitmap(ImageManager.getImage(imageUrl));
			
			productPanel.setVisibility(View.VISIBLE);
		}
		else {
			productPanel.setVisibility(View.GONE);
		}
	}
	
	private void getProductInfo() {
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			showStatus(getString(R.string.message_product_info_loading));
			final Intent intent = new Intent(this, ProductGetterService.class);
			intent.putExtra(Product.KEY, product.getPathSegments().get(2));
			startService(intent);
		}
	}
	
	private void prepareUI() {
		setContentView(R.layout.product_activity);
		
		progressBar = (ProgressBar) findViewById(R.id.StatusProgressBar);
		statusTextView = (TextView) findViewById(R.id.StatusTextView);
		statusTextView.setText(R.string.product_status);
		
		productPanel = findViewById(R.id.ProductInfoPanel);
		productImageView = (ImageView) findViewById(R.id.ProductImageView);
		productNameTextView = (TextView) findViewById(R.id.ProductNameTextView);
		
		affiliateTextView = (TextView) findViewById(R.id.AffiliateTextView);
		affiliateTextView.setOnClickListener(affiliateListener);
		
		buttonLikes = (Button) findViewById(R.id.ButtonLikes);
		buttonLikes.setOnClickListener(likeListener);
		
		buttonDislikes = (Button) findViewById(R.id.ButtonDislikes);
		buttonDislikes.setOnClickListener(dislikeListener);
		
		commentEditor = (EditText) findViewById(R.id.CommentEditText);
		commentEditor.setOnEditorActionListener(sendCommentActionListener);
		
//		findViewById(R.id.ProductInfoPanel).setOnClickListener(productQuickActionsListener);
		
		findViewById(R.id.LoginButton).setOnClickListener(loginListener);
		findViewById(R.id.SendButton).setOnClickListener(sendCommentListener);
		
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setOnClickListener(historyListener);
	}
	
	private void showStatus(String message) {
		progressBar.setVisibility(View.VISIBLE);
		statusTextView.setText(message);
	}
	
	private void hideStatus() {
		progressBar.setVisibility(View.GONE);
		statusTextView.setText(R.string.product_status);
	}
	
	private final View.OnClickListener homeListener = new View.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), MainActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener scanListener = new View.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener historyListener = new View.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener streamListener = new View.OnClickListener() {
		public void onClick(View view) {
			final Intent intent = new Intent(getBaseContext(), FeedActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener productImageListener = new View.OnClickListener() {
		public void onClick(View view) {
			if (product != null) {
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
				contentView.findViewById(R.id.ProductBarButtonLike).setOnClickListener(likeListener);
				contentView.findViewById(R.id.ProductBarButtonDislike).setOnClickListener(dislikeListener);
				
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
			if (product != null) {
				final Cursor cursor = managedQuery(product, null, null, null, null);
				if (cursor.moveToFirst()) {			
					final String url = cursor.getString(cursor.getColumnIndex(Product.AFFILIATE_URL));
					final Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));  
					startActivity(intent);
				}
			}
		}
	};
	
	private final View.OnClickListener likeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (product != null) {
				rateProduct("like");
			}
		}
	};
	
	private final View.OnClickListener dislikeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
			if (product != null) {
				rateProduct("dislike");
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
			postComment(commentEditor.getText().toString());
		}
	};
	
	private final TextView.OnEditorActionListener sendCommentActionListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEND) {
				postComment(commentEditor.getText().toString());
				return true;
			}			
			return false;
		}
	};
	
	private void postComment(String body) {
		if (product == null) return;		
		final Cursor cursor = managedQuery(product, null, null, null, null);
		if (cursor.moveToFirst()) {			
			ContentValues values = new ContentValues();
			values.put(Comment.PRODUCT_KEY, cursor.getString(cursor.getColumnIndex(Product.KEY)));
			values.put(Comment.PRODUCT_NAME, cursor.getString(cursor.getColumnIndex(Product.NAME)));
			values.put(Comment.BODY, body);
			values.put(Comment.CREATED_AT, new Date().getTime());
			values.put(Comment.PRODUCT_IMAGE_URL, cursor.getString(cursor.getColumnIndex(Product.IMAGE_URL)));
			values.put(Comment.USER_NAME, "Me");
			
			values.put(Comment.PENDING, true);
			
			final boolean shareLocation = settings.getBoolean(getString(R.string.settings_share_location), false);
			if (shareLocation) {
				Location location = GpsManager.getLocation();
				if (location != null) {
					values.put(Comment.LATITUDE, location.getLatitude());
					values.put(Comment.LONGITUDE, location.getLongitude());
				}
			}
			hideVirtualKeyboard();
			
			getContentResolver().insert(Comment.CONTENT_URI, values);
			
			((CursorAdapter) getListAdapter()).getCursor().requery();
			
			final Intent intent = new Intent(this, SyncService.class);
			startService(intent);
		}
	}
	
	private void rateProduct(String rating) {
		if (product == null) return;		
		final Cursor cursor = managedQuery(product, null, null, null, null);
		if (cursor.moveToFirst()) {
			
			final ContentValues values = new ContentValues();			
			values.put(Product.RATING_PERSONAL, rating);			
			values.put(Product.PENDING, true);
			
			getContentResolver().update(product, values, null, null);
			
			final Intent intent = new Intent(this, SyncService.class);
			startService(intent);
		}
	}
	
	private void hideVirtualKeyboard() {
		inputManager.hideSoftInputFromWindow(commentEditor.getWindowToken(), 0);
        commentEditor.clearFocus();
        commentEditor.setText("");
        productImageView.requestFocus();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.product_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
		closeProductPopupBar();
		prepareUI();
		if (product != null) {
			displayProduct();
		}
	}
	
	private final class ProductUpdaterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			hideStatus();
			displayProduct();
			((CursorAdapter) getListAdapter()).getCursor().requery();
		}
		
	}
	
	private final class SyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			displayProduct();
			((CursorAdapter) getListAdapter()).getCursor().requery();
		}
		
	}
	
	private final class ImageDownloaderReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		}
		
	}
}
