
package mobi.my2cents.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import mobi.my2cents.My2Cents;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

public final class NetworkManager {

	private static final String TAG = "NetworkManager";
	private static String authToken;
	private static String sessionToken;
	public static final String BASE_URL = "http://my2cents.mobi";
	private static final HttpParams httpParams;
	private static final int TIMEOUT = 10 * 1000; // 10 seconds
	
	static {
		System.setProperty("http.keepAlive", "false");
		
		httpParams = new BasicHttpParams();

	    // Turn off stale checking.  Our connections break all the time anyway,
	    // and it's not worth it to pay the penalty of checking every time.
	    HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);

	    // Default connection and socket timeout of 10 seconds.  Tweak to taste.
	    HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        ConnManagerParams.setTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
	}
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return false;
		}
		return true;
	}
	
	public static Bitmap getRemoteImage(final URL url) {
		if (url == null) return null;
		try {
			return BitmapFactory.decodeStream(url.openStream());
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
			return null;
		}
	}
	
	public static Bitmap getRemoteImage(final String urlString) {
		if (TextUtils.isEmpty(urlString)) return null;		
		try {
			return BitmapFactory.decodeStream(new URL(urlString).openStream());
		} catch (MalformedURLException e) {
			Log.e(My2Cents.TAG, e.toString());
			return null;
		} catch (IOException e) {
			Log.e(My2Cents.TAG, e.getMessage());
			return null;
		}
	}

	public static void setAuthToken(String authToken) {
		NetworkManager.authToken = authToken;
	}

	public static String getAuthToken() {
		return authToken;
	}
	
	
	public static String getREST(String url) throws ClientProtocolException, IOException {
		String result = null;
		final HttpGet httpGet = new HttpGet(url);
		final AndroidHttpClient client = AndroidHttpClient.newInstance(getUserAgent());
		try {
			final HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				if (sessionToken == null) {
					Header sessionCookie = response.getFirstHeader("Set-Cookie");
					if (sessionCookie != null) {
						sessionToken = sessionCookie.getValue();
					}
				}		
				
				final HttpEntity entity = response.getEntity();
				result = EntityUtils.toString(entity);
			}
		}
		finally {
			client.close();
		}		
		return result;
	}
	
	public static String postREST(String url, String content) throws ClientProtocolException, IOException {
		String result = null;
		final HttpPost httpPost = new HttpPost(url);
		
		if ((authToken != null) && (!authToken.equals(""))) {
	    	httpPost.setHeader("Cookie", authToken);
	    }
	    else {
	    	if (sessionToken != null) {
	    		httpPost.setHeader("Cookie", sessionToken);
	    	}
	    }
		
		StringEntity entity = new StringEntity(content, "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        
        final AndroidHttpClient client = AndroidHttpClient.newInstance(getUserAgent());
        try {
        	final HttpResponse response = client.execute(httpPost);
    		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
    			result = EntityUtils.toString(response.getEntity());
    		}
        }
        finally {
        	client.close();
        }		
		return result;
	}
	
	public static String putREST(String url, String content) throws ClientProtocolException, IOException {
		String result = null;
		final HttpPut httpPut = new HttpPut(url);
		
		if ((authToken != null) && (!authToken.equals(""))) {
			httpPut.setHeader("Cookie", authToken);
	    }
	    else {
	    	if (sessionToken != null) {
	    		httpPut.setHeader("Cookie", sessionToken);
	    	}
	    }
		
		StringEntity entity = new StringEntity(content, "UTF-8");
        entity.setContentType("application/json");
        httpPut.setEntity(entity);
		
        final AndroidHttpClient client = AndroidHttpClient.newInstance(getUserAgent());
        try {
        	final HttpResponse response = client.execute(httpPut);
    		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    			result = EntityUtils.toString(response.getEntity());
    		}
        }
        finally {
        	client.close();
        }
		return result;
	}
	
	public static String putRating(String gtin, String content) throws ClientProtocolException, IOException {
		final String url = BASE_URL + "/products/" + gtin + "/rating.json";
		return putREST(url, content);
	}
	
	public static String postComment(String content) throws ClientProtocolException, IOException {
		final String url = BASE_URL + "/comments.json";
		return postREST(url, content);
	}
	
	public static String getFeed() throws ClientProtocolException, IOException {
		final String url = BASE_URL + "/comments.json";
        return getREST(url);
	}
	
	public static String getProduct(String key) throws ClientProtocolException, IOException {
		final String url = BASE_URL + "/products/" + key + ".json";
		return getREST(url);
	}

	public static void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(httpParams, userAgent);
	}
	
	public static String getUserAgent() {
		return HttpProtocolParams.getUserAgent(httpParams);
	}
}
