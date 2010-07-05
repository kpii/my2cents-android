package mobi.my2cents;

 import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.Product;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ProductActivity extends ListActivity {

	private static final String TAG = My2Cents.TAG+" ProductActivity";
	
 
	
	private View statusLayout;
	private ImageView productImageView;
	private TextView productNameTextView;
	private TextView affiliateTextView;
	private TextView likesTextView;
	private TextView dislikesTextView;
	private EditText commentEditor;
	private Gallery tagsGallery;

	private Uri uri;

	private Cursor product;

    private ServiceConnection conn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    setContentView(R.layout.comment);
	    
	    AlarmManager mgr=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(this, OnAlarmReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(this, 0,
                                                                                            i, 0);
		statusLayout = findViewById(R.id.StatusRelativeLayout);
		
		productImageView = (ImageView) findViewById(R.id.ProductImageView);
		productImageView.setOnClickListener(productImageListener);
		
		productNameTextView = (TextView) findViewById(R.id.ProductNameTextView);
		affiliateTextView = (TextView) findViewById(R.id.AffiliateTextView);
//		likesTextView = (TextView) findViewById(R.id.LikesTextView);
//		dislikesTextView = (TextView) findViewById(R.id.DislikesTextView);
		
		commentEditor = (EditText) findViewById(R.id.CommentEditText);
		commentEditor.setOnEditorActionListener(sendCommentActionListener);
		
		tagsGallery = (Gallery) findViewById(R.id.TagsGallery);
		// tagsGallery.setAdapter(tagsAdapter);
		// tagsGallery.setOnItemLongClickListener(tagsLongClickListener);
		
		findViewById(R.id.ProductInfoPanel).setOnClickListener(productQuickActionsListener);
		findViewById(R.id.LoginButton).setOnClickListener(loginListener);
		findViewById(R.id.SendButton).setOnClickListener(sendCommentListener);
		
		
		findViewById(R.id.NavigationButtonHome).setOnClickListener(homeListener);
		findViewById(R.id.NavigationButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.NavigationButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.NavigationButtonHistory).setOnClickListener(historyListener);
	
	    uri = getIntent().getData();
	        
        if (uri.getScheme().equals("http")) {
            Matcher m = Pattern.compile(".*my2cents.mobi/products/(\\d*)$").matcher(getIntent().getDataString());
            if (m.matches()) {
                Log.d(TAG, "matched htttp uri. product_key: "+m.group(1));
                uri = Uri.withAppendedPath(Product.CONTENT_URI, m.group(1));
            } 
        }
      
	    product = managedQuery(uri, null, null, null, null);
	    if (product.getCount() > 0) {
	    	product.moveToFirst();
	    	Log.d(TAG, product.getString(1));
			productNameTextView.setText(product.getString(product.getColumnIndex(Product.NAME)));
			
		} else {
			Log.d(TAG, "product not in offline cached");
			startService(new Intent(this, My2centsService.class).putExtra(Product.KEY, uri.getLastPathSegment()));
		}
	    product.registerContentObserver(new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				product.requery();
				if (product.getCount() > 0) {
					product.moveToFirst();
					productNameTextView.setText(product.getString(product.getColumnIndex(Product.NAME)));
				}
				super.onChange(selfChange);
			}
		});
	    setListAdapter(new CommentsAdapter(this, managedQuery(
	    		Uri.withAppendedPath(uri, "comments"), null, null,null, null)));
	    
	    // we dont need this connection for now but its clearer
//	    conn = new ServiceConnection() {
//            
//	    	@Override
//	    	public void onServiceConnected(ComponentName name, IBinder service) {
//	    	}
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//            }
//            
//        };
	    
	}
	
	private class CommentsAdapter extends CursorAdapter {

		public CommentsAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		protected void onContentChanged() {
			// TODO Auto-generated method stub
			Log.d(TAG,"content changed");
			super.onContentChanged();
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView authorTextView = (TextView) view.findViewById(R.id.CommentAuthorTextView);
//			authorTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_ID)));
			
			TextView messageTextView = (TextView) view.findViewById(R.id.CommentTextView);
			messageTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));
			
			TextView sentTextView = (TextView) view.findViewById(R.id.CommentSentTextView);
			//sentTextView.setText(RelativeTime.getDifference(cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT))));
			// "2010-06-05T15:33:33Z"
			
			ImageView avatarImageView = (ImageView) view.findViewById(R.id.CommentImageView);
			//avatarImageView.setImageBitmap(DataManager.profileImageCache.get(comment.getUser()));
			
			if (cursor.getInt(cursor.getColumnIndex(Comment.TRANSITION_ACTIVE)) == 1) {
				view.findViewById(R.id.NetworkState).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.NetworkState).setVisibility(View.INVISIBLE);
			}
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.comment_item, parent, false);
		}
		
	}
	

	@Override
    protected void onResume() {
        bindService(new Intent(this, My2centsService.class), conn, Service.BIND_AUTO_CREATE);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        unbindService(conn);
        super.onPause();
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        final Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
        final String key = cursor.getString(cursor.getColumnIndex(Comment.USER_KEY));
        String user_url = "http://my2cents.mobi/users/"+key;
        Log.d(TAG, key);
        if (!TextUtils.isEmpty(key)) {
//          Intent intent = new Intent(this, CommentActivity.class);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(user_url), this, WebViewActivity.class);
            startActivity(intent);
        }
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
//			if (productInfo != null) {
//				showDialog(DIALOG_PRODUCT_DETAILS);
//			}
		}
	};
	protected PopupWindow productPopup;
	
	private final View.OnClickListener productQuickActionsListener = new View.OnClickListener() {

		public void onClick(View view) {
//			if (p.count != null) {
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
//		}
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
//			if (productInfo != null) {
//				if (productInfo.getAffiliateUrl() != null) {
//					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(productInfo.getAffiliateUrl()));  
//					startActivity(viewIntent);
//				}
			}
//		}
	};
	
	private final View.OnClickListener likeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
//			if (productInfo != null) {
//				rateProduct(view.getContext(), "like");
//			}
		}
	};
	
	private final View.OnClickListener dislikeListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
//			if (productInfo != null) {
//				rateProduct(view.getContext(), "dislike");
//			}
		}
	};
	
	private final View.OnClickListener editProductInfoListener = new View.OnClickListener() {
		public void onClick(View view) {
			closeProductPopupBar();
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
			ContentValues cv = new ContentValues();
			cv.put(Comment.BODY, commentEditor.getText().toString());
			cv.put(Comment.POST_TRANSITIONAL_STATE, true);
			getContentResolver().insert(Uri.withAppendedPath(uri, "comments"), cv);
		}
	};
	
	private final TextView.OnEditorActionListener sendCommentActionListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//			if (actionId == EditorInfo.IME_ACTION_SEND) {
//				postComment(view.getContext());
//				return true;
//			}			
			return false;
		}
	};

	
}
