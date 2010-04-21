
package mobi.my2cents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.HistoryColumns;
import mobi.my2cents.data.ProductInfo;
import mobi.my2cents.utils.NetworkManager;
import mobi.my2cents.utils.WeakAsyncTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public final class CommentActivity extends ListActivity {
	
	private static final String TAG = "CommentActivity";
	
	private boolean isProductInfoAvailable;
	
	private boolean updateHistory;
	public final static String UPDATE_HISTORY = "UpdateHistory";
	
	private final int DIALOG_PRODUCT_DETAILS = 0;
	
	private SharedPreferences settings;
	private InputMethodManager inputManager;

	private CommentsAdapter commentsAdapter;

	private ArrayList<String> tags;
	private TagsAdapter tagsAdapter;
	
	private ProductInfo productInfo;
	private String gtin;
	
	private ProgressDialog progressDialog;
	private Gallery tagsGallery;
	private View productInfoLayout;
	
	private ImageView productImageView;
	private TextView productNameTextView;
	private TextView productManufacturerTextView;
	
	private EditText commentEditor;
	
	private AsyncTask<String, Void, ProductInfo> getProductInfoTask;
	private AsyncTask<List<Comment>, Void, Void> getProfileImagesTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.comment);

		DataManager.UnknownProductName = getString(R.string.unknown_product);
		
		productImageView = (ImageView) findViewById(R.id.ProductImageView);
		productImageView.setOnClickListener(productDetailsListener);
		
		productNameTextView = (TextView) findViewById(R.id.ProductNameTextView);
		productManufacturerTextView = (TextView) findViewById(R.id.ProductManufacturerTextView);
		
		commentEditor = (EditText) findViewById(R.id.CommentEditText);
		
		commentsAdapter = new CommentsAdapter(this, R.layout.comment_item, new ArrayList<Comment>());
		setListAdapter(commentsAdapter);

		tags = new ArrayList<String>();
		tagsAdapter = new TagsAdapter(this, R.layout.tag_item, tags);
		tagsGallery = (Gallery) findViewById(R.id.TagsGallery);
		tagsGallery.setAdapter(tagsAdapter);
		tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		productInfoLayout = findViewById(R.id.ProductInfoLayout);
		
		findViewById(R.id.LoginButton).setOnClickListener(loginListener);
		
		findViewById(R.id.SendButton).setOnClickListener(sendCommentListener);
		commentEditor.setOnEditorActionListener(sendCommentActionListener);
		
		findViewById(R.id.ImageButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.ImageButtonHistory).setOnClickListener(historyListener);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener productDetailsListener = new View.OnClickListener() {
		public void onClick(View view) {
			if (productInfo != null) {
				showDialog(DIALOG_PRODUCT_DETAILS);
//				if (productInfo.getDetailPageUrl() != null) {
//					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(productInfo.getDetailPageUrl()));  
//					startActivity(viewIntent);
//				}
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
	
	@Override
	protected void onStart() {
		super.onStart();
		
		hideVirtualKeyboard();
		
		if (!NetworkManager.isNetworkAvailable(this)) {
			Toast.makeText(this, R.string.error_message_no_network_connection, Toast.LENGTH_LONG).show();
		}
		else {
			Intent intent = getIntent();
			String action = intent == null ? null : intent.getAction();
			if (intent != null && action != null) {
				if (action.equals(Intents.ACTION)) {
					gtin = intent.getStringExtra(HistoryColumns.GTIN);
					updateHistory = intent.getBooleanExtra(UPDATE_HISTORY, true);
					
					getProductInfoTask = new GetProductInfoTask(this).execute(gtin);
				}
			}
		}
	}
	
	private void hideVirtualKeyboard() {
		inputManager.hideSoftInputFromWindow(commentEditor.getWindowToken(), 0);
	}
	
	private class GetProductInfoTask extends WeakAsyncTask<String, Void, ProductInfo, Context> {
		public GetProductInfoTask(Context target) {
			super(target);
		}

		@Override
		protected void onPreExecute(Context target) {
			productInfo = DataManager.getDatabase().getCachedProductInfo(gtin);
			if (productInfo != null) {
				isProductInfoAvailable = true;
				displayProductFound(productInfo);
				productInfoLayout.setVisibility(View.VISIBLE);
			}
			else {
				isProductInfoAvailable = false;
				productInfoLayout.setVisibility(View.GONE);
			}
			progressDialog = ProgressDialog.show(CommentActivity.this, null, getString(R.string.progress_dialog_loading), true);
	    }

		@Override
		protected ProductInfo doInBackground(Context target, String... params) {
			ProductInfo result = null;
			if (isProductInfoAvailable) {
				result = DataManager.getProductComments(productInfo);
			}
			else {
				result = DataManager.getProductInfo(gtin);
			}
			
			if (updateHistory) {
				if (result != null) {
					DataManager.getDatabase().addHistoryItem(result);
				}
				else {
					ProductInfo dummyProduct = new ProductInfo(params[0]);
					DataManager.getDatabase().addHistoryItem(dummyProduct);
				}
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Context target, ProductInfo product) {
			
			progressDialog.dismiss();
			
	        if (product != null) {
	        	displayProductFound(product);
	        	
	        	commentsAdapter.clear();
				tagsAdapter.clear();
	        	ArrayList<Comment> comments = product.getComments();
				if (comments.size() > 0) {
					Set<String> tags = new TreeSet<String>();
					for (Comment comment : comments) {
						commentsAdapter.add(comment);
						
						String text = comment.getText();
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
					
					getProfileImagesTask = new GetProfileImagesTask(target).execute(comments);
				}
			}
			else {
				Toast.makeText(target, R.string.error_message_no_server_connection, Toast.LENGTH_LONG).show();
			}
	        
	        productInfo = product;
	        getProductInfoTask = null;
	        commentEditor.clearFocus();
	        productImageView.requestFocus();
	    }
	}
	
	private class GetProfileImagesTask extends WeakAsyncTask<List<Comment>, Void, Void, Context> {
		
		public GetProfileImagesTask(Context target) {
			super(target);
		}

		@Override
		protected Void doInBackground(Context target, List<Comment>... params) {			
			for (Comment comment : params[0]) {
				if (!DataManager.profileImageCache.containsKey(comment.getUser())) {
					DataManager.profileImageCache.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
				}
				publishProgress();
			}
			getProfileImagesTask = null;
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
		
		SettingsActivity.setShareOnTwitter(settings.getBoolean(getString(R.string.settings_twitter), false));
		
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
		Comment selectedComment = commentsAdapter.getItem(position);
		
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void displayProductFound(ProductInfo product)
	{
		productNameTextView.setText(product.getName());
		
		if (product.getImage() != null) 
			productImageView.setImageBitmap(product.getImage());
		else
			productImageView.setImageResource(R.drawable.unknown_product_icon_inverted);
		
		productManufacturerTextView.setText(product.getManufacturer());
		productInfoLayout.setVisibility(View.VISIBLE);
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
			if (comment != null) {
				if (!DataManager.profileImageCache.containsKey(comment.getUser())) {
					DataManager.profileImageCache.put(comment.getUser(), NetworkManager.getRemoteImage(comment.getUserProfileImageUrl()));
				}
			}
			return comment;
		}
		
		@Override
		protected void onPostExecute(Context target, Comment comment) {
			progressDialog.dismiss();
			if (comment != null) {
				hideVirtualKeyboard();
				
				commentsAdapter.insert(comment, 0);
				commentsAdapter.notifyDataSetChanged();
				
				commentEditor.setText("");
				Toast.makeText(CommentActivity.this, R.string.message_comment_posted_successfully, Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(target, R.string.error_message_no_server_connection, Toast.LENGTH_LONG).show();
			}
	    }
	}
	
	private final void cancelAsyncTasks() {
		if (getProductInfoTask != null && getProductInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
        	getProductInfoTask.cancel(true);
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
}
