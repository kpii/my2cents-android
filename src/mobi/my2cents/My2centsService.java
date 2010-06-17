/***
	Copyright (c) 2009 CommonsWare, LLC
	
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package mobi.my2cents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.Product;
import mobi.my2cents.data.ProductInfo;
import mobi.my2cents.data.TransitionalStateColumns;
import mobi.my2cents.utils.NetworkManager;


public class My2centsService extends WakefulIntentService {
	private static final String TAG = My2Cents.TAG+" ContentProvider";
	private boolean activityVisible;
	private NotificationManager notificationManagr;

    public My2centsService() {
		super("My2centsService");
		Log.d(TAG,"My2centsService constructor.");
	}

 
    		
	// search cp for active transactions
	// executing them via cached httpclient with etag-support from server
	// updating cp and deleting transcation flags
	// updating ui 
	// 
	// asking the server for notifications
	// getting data updating cp
	// 
	// notifying my2c activities
	// 	therefor we need to register broadcastreceiver for "my2cents/updates"? 
	//  and reacting in the ui.
	// else sending a system notification or updating existing.
	
	
	@Override
	protected void doWakefulWork(Intent intent) {
		
		// search product
		if (intent.hasExtra(Product.KEY)) {
			
			String key = intent.getExtras().getString(Product.KEY);
			Log.d(TAG, "searching product "+key+"...");

			ContentValues cv = new ContentValues();
			cv.put(Product.KEY, key);
			cv.put(Product.TRANSITION_ACTIVE, true);
			Uri uri = getContentResolver().insert(Product.CONTENT_URI, cv);
		
			ProductInfo product = getProduct(key, null);
			
			cv = product.toContentValues();
			cv.put(Product.TRANSITION_ACTIVE, false);
			cv.put(Product.GET_TRANSITIONAL_STATE, false);
			getContentResolver().update(Uri.withAppendedPath(Product.CONTENT_URI, product.getGtin()), cv, null, null);
			
			// insert comments
			for (Comment c : product.getComments()) {
//				getContentResolver().insert(Uri.withAppendedPath(uri, "comments"), c.toContentValues());
			}
			Log.d(TAG, "->Done.");
		
		// check for product updates and/or new comments
		} else if (intent.hasExtra("poll")) {
			
			Log.d(TAG, "polling... ");
			Cursor products = getContentResolver().query(Product.CONTENT_URI, 
					new String[] {Product.KEY, Product.ETAG}, null, null, null);
			while (!products.isLast()) {
				products.moveToNext();
				String key = products.getString(0);
				ProductInfo product = getProduct(key, products.getString(1));
				if (product != null) {
					getContentResolver().update(Uri.withAppendedPath(Product.CONTENT_URI, key), 
										product.toContentValues(), null, null);
					Log.d(TAG, "updated product");
					
					// reinsert all comments (for now)
					getContentResolver().delete(Comment.CONTENT_URI, Comment.PRODUCT_KEY+"=?", new String[]{key});
					for (Comment c : product.getComments())
//						getContentResolver().insert(Uri.withAppendedPath(Product.CONTENT_URI, key+"/comments"), c.toContentValues());
					
					if (activityVisible) {
						Log.d(TAG, "  -> activity is visible");
					} else {
						Log.d(TAG, "  -> activity NOT visible");
						Notification n = new Notification();
						n.icon = R.drawable.birdy;
						n.setLatestEventInfo(this, "new Comments!", "super many cool new comments for products you care for",
								PendingIntent.getActivity(this, 0, new Intent(), Notification.FLAG_ONLY_ALERT_ONCE));
						notificationManagr.notify(1, n);
						
					}
				} else
					Log.d(TAG, "no updates.");
			}
			products.close();
			Log.d(TAG, "->Done.");
			
		// sync changes up to server
		} else if (intent.hasExtra("push")) {

			try {
				Log.d(TAG, "My2centsService started.");
				// getTransferableItemsFromContentProvider() begin
				Cursor comments2sync = getContentResolver().query(
						Comment.CONTENT_URI, null,
						TransitionalStateColumns.SELECTION, null, null);
				// getTransferableItemsFromContentProvider() ende
				while (comments2sync.getPosition() < comments2sync.getCount()-1) {
					Log.d(TAG, "he lets do some work");
					comments2sync.moveToNext();
					ContentValues cv = new ContentValues();
					cv.put(Comment.TRANSITION_ACTIVE, true);
					getContentResolver().update(ContentUris.withAppendedId(Comment.CONTENT_URI,
									comments2sync.getInt(0)), cv, null, null);
					// TODO rest post to server
				}
				comments2sync.close();
			} catch (Exception e) {
				Log.e(TAG, "My2centsService crashed.", e);
			}
		}
	}


	// this method is somehow redundant/duplicate :/
	// found no better approach to get the ETag header out of the http response
	private ProductInfo getProduct(String key, String etag) {
//		try {
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			HttpGet get = new HttpGet(NetworkManager.BASE_URL+"/products/"+key+".json");
//			if (etag != null)
//				get.setHeader("ETag", etag);
//			HttpResponse result = httpClient.execute(get);
//			if (result.getStatusLine().getStatusCode() != 304) {
//				ProductInfo p = DataManager.getProductInfoFromJsonString(
//								NetworkManager.convertStreamToString(
//								result.getEntity().getContent()));
//				p.setEtag(result.getFirstHeader("ETag").getValue());
//				Log.d(TAG, "etag: "+result.getFirstHeader("ETag").getValue());
//				return p;
//			}
//		} catch (Exception e) {
//			Log.e(TAG, "Mist!", e);
//			e.printStackTrace();
//		}
		return null;
	}



	@Override
	public void onCreate() {
		
		
	    Log.d(TAG,"My2centsService onCreate(). cleaning all transition-active Flags");
	    //cleaning all TRANSITION_ACTIVE FLAGS
	    ContentValues cv = new ContentValues();
	    cv.put(Comment.TRANSITION_ACTIVE, false);
	    getContentResolver().update(Comment.CONTENT_URI, cv, null, null);
	    getContentResolver().notifyChange(Comment.CONTENT_URI, null);
	    // Log.d(TAG,"hibernation on");
	    // SystemClock.sleep(10000);
	    //  Log.d(TAG,"hibernation off");
	    notificationManagr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    Notification n = new Notification();
	    n.icon = R.drawable.birdy;
	    n.setLatestEventInfo(this, "title", "text", PendingIntent.getActivity(this, 23, new Intent(this, ProductActivity.class), 0));
	    notificationManagr.notify(0, n);
	    super.onCreate();
	}
	
    @Override
    public IBinder onBind(Intent intent) {
        activityVisible = true;
        Log.d(TAG,"My2centsService onBind().");
        return super.onBind(intent);
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        activityVisible = false;
        Log.d(TAG,"My2centsService onUnBind().");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"My2centsService onDestroy().");
        notificationManagr.cancel(0);
        super.onDestroy();
    }
	
	
	
	
}
