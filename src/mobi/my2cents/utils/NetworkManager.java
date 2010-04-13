
package mobi.my2cents.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public final class NetworkManager {

	private static final String TAG = "NetworkManager";
	private static String authToken;
	private static final String USER_AGENT = "Android my2cents";
	public static final String BASE_URL = "http://my2cents.mobi";
	
	static {
		System.setProperty("http.keepAlive", "false");
		
		HttpParams params = new BasicHttpParams();

	    // Turn off stale checking.  Our connections break all the time anyway,
	    // and it's not worth it to pay the penalty of checking every time.
	    HttpConnectionParams.setStaleCheckingEnabled(params, false);

	    // Default connection and socket timeout of 3 seconds.  Tweak to taste.
	    HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
	    HttpConnectionParams.setSoTimeout(params, 3 * 1000);
	    HttpConnectionParams.setSocketBufferSize(params, 8192);
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
		if (url != null) {
			try {
				final URLConnection connection = url.openConnection();
				connection.connect();
				final BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
				final Bitmap bitmap = BitmapFactory.decodeStream(stream);
				stream.close();
				return bitmap;
			} catch (IOException e) {
				Log.d(TAG, "Cannot load remote image.");
			}
		}
		return null;
	}
	
	private static String convertStreamToString(InputStream stream) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
            	stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
        	Log.e(TAG, e.getMessage());
        } finally {
            try {
            	stream.close();
            } catch (IOException e) {
            	Log.e(TAG, e.getMessage());
            }
        }
        return stringBuilder.toString();
    }
	
	public static String queryREST(String url) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent", USER_AGENT);
		
		try {
			HttpResponse response = httpClient.execute(httpGet);			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream stream = entity.getContent();
				String result = convertStreamToString(stream);				
				stream.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "There was a protocol based error", e);
		} catch (IOException e) {
			Log.e(TAG, "There was an IO Stream related error", e);
		}
		return null;
	}
	
	
	
	public static String getProductJsonString(String gtin) {
		String url = BASE_URL + "/products/" + gtin + ".json";
        return queryREST(url);
	}
	
	public static String getCommentsStreamJSONString() {
		String url = BASE_URL + "/comments.json";
		return queryREST(url);
	}
	
	private static HttpResponse postJSON(String url, String content) {
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();  
	    HttpPost httpPost = new HttpPost(url);
	    httpPost.setHeader("User-Agent", USER_AGENT);
	    
	    if ((authToken != null) && (!authToken.equals(""))) {
	    	httpPost.setHeader("Cookie", authToken);
	    }
	    
	    try {
	    	StringEntity entity = new StringEntity(content, "UTF-8");
	        entity.setContentType("application/json");
	        httpPost.setEntity(entity);
	        
	        // Execute HTTP Post Request
	        HttpResponse response = httpClient.execute(httpPost);
	        return response;	        
	    } catch (ClientProtocolException e) {
	        Log.e(TAG, e.getMessage());
	    } catch (IOException e) {
	    	Log.e(TAG, e.getMessage());
	    }
		return null;
	}
	
	public static String postComment(String content) {
		String url = BASE_URL + "/comments.json";
	    
	    HttpResponse response = postJSON(url, content);
	    if ((response.getStatusLine() != null) && (response.getStatusLine().getStatusCode() == 201)) {
	    	try {
    			HttpEntity entity = response.getEntity();
    			if (entity != null) {
    				InputStream stream = entity.getContent();
    				String result = convertStreamToString(stream);				
    				stream.close();
    				return result;
    			}
			} catch (IllegalStateException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
	    }	    
		return null;
	}

	public static void setAuthToken(String authToken) {
		NetworkManager.authToken = authToken;
	}

	public static String getAuthToken() {
		return authToken;
	}
}
