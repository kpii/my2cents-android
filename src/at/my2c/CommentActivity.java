
package at.my2c;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import at.my2c.comments.Comment;
import at.my2c.comments.CommentsAdapter;
import at.my2c.comments.CommentsManager;
import at.my2c.comments.TagsAdapter;
import at.my2c.data.DataManager;
import at.my2c.data.ProductInfo;
import at.my2c.data.ProductInfoManager;
import at.my2c.utils.GpsManager;
import at.my2c.utils.NetworkManager;

public final class CommentActivity extends ListActivity {
	
	private boolean updateHistory;
	public final static String UPDATE_HISTORY = "UpdateHistory";
	
	private SharedPreferences settings;
	
	private boolean shareLocation;
	
	private LocationManager locationManager;

	private List<Comment> comments;
	private CommentsAdapter commentsAdapter;

	private ArrayList<String> tags;
	private TagsAdapter tagsAdapter;
	
	private ProductInfo productInfo;
	
	private ProgressDialog progressDialog;
	private Gallery tagsGallery;
	private View productInfoLayout;
	
	private ImageView productImageView;
	private TextView productNameTextView;
	private TextView productManufacturerTextView;
	private TextView productDetailsTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.comment);
		
		ProductInfoManager.UnknownProductName = getString(R.string.unknown_product);

		productImageView = (ImageView) findViewById(R.id.ProductImageView);
		productNameTextView = (TextView) findViewById(R.id.ProductNameTextView);
		productManufacturerTextView = (TextView) findViewById(R.id.ProductManufacturerTextView);
		
		comments = new ArrayList<Comment>();
		commentsAdapter = new CommentsAdapter(this, R.layout.comment_item, comments);
		setListAdapter(commentsAdapter);

		tags = new ArrayList<String>();
		tagsAdapter = new TagsAdapter(this, R.layout.tag_item, tags);
		tagsGallery = (Gallery) findViewById(R.id.TagsGallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		productInfoLayout = findViewById(R.id.ProductInfoLayout);
		productDetailsTextView = (TextView) findViewById(R.id.ProductDetailsTextView);
		productDetailsTextView.setOnClickListener(productDetailsListener);
		
		Button loginButton = (Button) findViewById(R.id.LoginButton);
		loginButton.setOnClickListener(loginListener);

		Button sendCommentButton = (Button) findViewById(R.id.SendButton);
		sendCommentButton.setOnClickListener(sendCommentListener);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	private final TextView.OnClickListener productDetailsListener = new TextView.OnClickListener() {
		public void onClick(View view) {
			if (productInfo != null) {
				if (productInfo.getDetailPageUrl() != null) {
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(productInfo.getDetailPageUrl()));  
					startActivity(viewIntent);
				}					
			}
		}
	};
	
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
				updateHistory = intent.getBooleanExtra(UPDATE_HISTORY, true);
				
				new GetProductInfoTask().execute(DataManager.getSearchTerm());
				new GetCommentsTask().execute(SettingsActivity.TAG_PREFIX + DataManager.getSearchTerm());
			}
		}
	}
	
	private class GetCommentsTask extends AsyncTask<String, Void, List<Comment>> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(CommentActivity.this, null, getString(R.string.progress_dialog_loading_comments), true);
	    }
		
		@Override
		protected List<Comment> doInBackground(String... params) {
			return CommentsManager.searchComments(params[0]);
		}
		
		@Override
		protected void onPostExecute(List<Comment> result) {
			if (result == null) {
				progressDialog.dismiss();
				Toast.makeText(CommentActivity.this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
			}
			else {
				comments = result;
				
				commentsAdapter.clear();
				tagsAdapter.clear();
				
				String productTag = SettingsActivity.PRODUCT_CODE_PREFIX + DataManager.getSearchTerm();
				if (comments.size() > 0) {
					
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
					
					if (tags.size() > 0) {
						for (String tag : tags) {
							tagsAdapter.add(tag);
						}
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
		
		shareLocation = settings.getBoolean(getString(R.string.settings_location), false);
		
		checkCommenting();
		
		if (commentsAdapter.getCount() > 0)
			commentsAdapter.notifyDataSetChanged();
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
	
	private void checkCommenting() {
		ViewGroup commentLayout = (ViewGroup) findViewById(R.id.CommentEditorLayout);
		ViewGroup loginLayout = (ViewGroup) findViewById(R.id.LoginLayout);
		
		String token = settings.getString(this.getResources().getString(R.string.settings_token),"");
		String tokenSecret = settings.getString(this.getResources().getString(R.string.settings_token_secret),"");
		
		if (token.equals("") && tokenSecret.equals("")) {
			commentLayout.setVisibility(View.GONE);
			loginLayout.setVisibility(View.VISIBLE);
		} else {
			loginLayout.setVisibility(View.GONE);
			commentLayout.setVisibility(View.VISIBLE);
		}
	}
	
	private final Button.OnClickListener loginListener = new Button.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
			startActivity(intent);
		}
	};

	private final Button.OnClickListener sendCommentListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText commentEditor = (EditText) findViewById(R.id.CommentEditText);
			String message = commentEditor.getText().toString() +
				" " +
				SettingsActivity.PRODUCT_CODE_PREFIX +
				DataManager.getSearchTerm();
			
			new PostComment().execute(message);
		}
	};

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
			case R.id.scanMenuItem: {
				Intent intent = new Intent(this, ScanActivity.class);
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
	
	private void displayProductNotFound()
	{
		productNameTextView.setText(R.string.unknown_product);
		productImageView.setImageResource(R.drawable.warning64px);
		productManufacturerTextView.setVisibility(View.GONE);
		productDetailsTextView.setVisibility(View.GONE);
		productInfoLayout.setVisibility(View.VISIBLE);
	}
	
	private void displayProductFound(ProductInfo product)
	{
		productNameTextView.setText(product.getProductName());
		
		if (product.getProductImage() != null) 
			productImageView.setImageBitmap(product.getProductImage());
		else
			productImageView.setImageResource(R.drawable.warning64px);
		
		productManufacturerTextView.setText(product.getManufacturer());
		productManufacturerTextView.setVisibility(View.VISIBLE);
		productDetailsTextView.setVisibility(View.VISIBLE);
		productInfoLayout.setVisibility(View.VISIBLE);
	}
	
	private class GetProductInfoTask extends AsyncTask<String, Void, ProductInfo> {
		@Override
		protected void onPreExecute() {
			productInfoLayout.setVisibility(View.GONE);
	    }

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
				
				if (updateHistory) {
					DataManager.getDatabase().addHistoryItem(product);
				}
			}
			else {
				if (updateHistory) {
					ProductInfo dummyProduct = new ProductInfo(params[0]);
					dummyProduct.setProductCode(params[0]);
					DataManager.getDatabase().addHistoryItem(dummyProduct);
				}
			}
			
			return product;
		}
		
		@Override
		protected void onPostExecute(ProductInfo product) {				
	        if (product != null) {
	        	productInfo = product;
	        	displayProductFound(product);
			}
			else {
				productInfo = null;
				displayProductNotFound();
			}
	    }
	}
	
	
	private class PostComment extends AsyncTask<String, Void, Comment> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(CommentActivity.this, null, getString(R.string.progress_dialog_sending), true);
	    }
		
		@Override
		protected Comment doInBackground(String... params) {
			
			Comment comment = (shareLocation) ? 
				CommentsManager.sendComment(params[0], GpsManager.getGPS(locationManager)) :
				CommentsManager.sendComment(params[0], null);
			
			if (comment != null) {
				if (!CommentsManager.imagesMap.containsKey(comment.getUser())) {
					CommentsManager.imagesMap.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
				}
			}
			
			return comment;
		}
		
		@Override
		protected void onPostExecute(Comment comment) {
			if (comment != null) {
				
				commentsAdapter.insert(comment, 0);
				commentsAdapter.notifyDataSetChanged();
				
				EditText commentEditor = (EditText) findViewById(R.id.CommentEditText);
				commentEditor.setText("");
				Toast.makeText(CommentActivity.this, R.string.message_comment_posted_successfully, Toast.LENGTH_SHORT).show();
			}

			progressDialog.dismiss();
	    }
	}
}
