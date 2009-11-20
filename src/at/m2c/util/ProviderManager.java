package at.m2c.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.location.Location;
import android.util.Log;

public final class ProviderManager {

	private final static String TAG = "ProviderManager";

	final static String TWITTER_API_ROOT = "http://twitter.com/";
	final static String TWITTER_SEARCH_API_ROOT = "http://search.twitter.com/";
	final static String IDENTICA_API_ROOT = "http://identi.ca/api/";
	final static String JEEJEE_API_ROOT = "http://jeejee.wanted.base45.de/api/";

	static String apiRoot = TWITTER_API_ROOT;
	static String searchApiRoot = TWITTER_SEARCH_API_ROOT;
	
	private final static String RESEARCH_URL = "http://cocoa.ethz.ch/productpeep";

	private static Twitter twitter;
	
	private static boolean isCommentingPossible;
	

	public final static void Initialize(String provider, String username, String password, String customApiUrl)
	{		
		twitter = new Twitter(username, password);
		
		if (provider.equals("Twitter")) {
			twitter.setBaseURL(TWITTER_API_ROOT);
			twitter.setSearchBaseURL(TWITTER_SEARCH_API_ROOT);
		}
		else if (provider.equals("Identi.ca")) {
			twitter.setBaseURL(IDENTICA_API_ROOT);
			twitter.setSearchBaseURL(IDENTICA_API_ROOT);
		}
		else {
			twitter.setBaseURL(customApiUrl);
			twitter.setSearchBaseURL(customApiUrl);
		}
	}

	public final static Status updateStatus(String statusText, Location l) {
		try {
			if (l != null)
				return twitter.updateStatus(statusText, l.getLatitude(), l.getLongitude());
			else
				return twitter.updateStatus(statusText);
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public final static boolean postResearchData(String ean, String username, String comment) {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(RESEARCH_URL);

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	        nameValuePairs.add(new BasicNameValuePair("ean", ean));
	        nameValuePairs.add(new BasicNameValuePair("user", username));
	        nameValuePairs.add(new BasicNameValuePair("comment", comment));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	    } catch (ClientProtocolException e) {
	        return false;
	    } catch (IOException e) {
	    	return false;
	    }
	    
	    return true;
	}
	
	public final static List<Tweet> search(String searchTerm, int numberOfResults){
		Query query = new Query();
		query.setQuery(searchTerm);
		query.setRpp(numberOfResults);
		
		QueryResult queryResult = null;
		try {
			queryResult = twitter.search(query);
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
		}
		
		List<Tweet> items = queryResult.getTweets();
		
		return items;
	}
	
	public final static boolean verifyCredentials() {
		try {
			User user = twitter.verifyCredentials();
			setCommentingPossible(!user.isProtected());
			return true;
		} catch (TwitterException e) {
			if (e.getStatusCode() != 401)
				Log.e(TAG, e.toString());
			setCommentingPossible(false);
			return false;
		}
	}

	public final static Twitter getTwitter() {
		return twitter;
	}

	public static void setCommentingPossible(boolean isPossible) {
		isCommentingPossible = isPossible;
	}

	public static boolean isCommentingPossible() {
		return isCommentingPossible;
	}
}
