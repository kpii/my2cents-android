package at.my2c.util;

import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import twitter4j.http.AccessToken;
import android.util.Log;

public final class ProviderManager {

	private final static String TAG = "ProviderManager";

	private static Twitter twitter;
	private static AccessToken accessToken;
	
	private static boolean isCommentingPossible;
	
	private final static int numberOfResults = 30;
	

	public final static void InitializeBasic(String username, String password)
	{
		System.setProperty("http.keepAlive", "false");
		TwitterFactory factory = new TwitterFactory();
		twitter = factory.getInstance(username, password);
	}
	
	public final static void InitializeOAuth()
	{
		System.setProperty("http.keepAlive", "false");
		TwitterFactory factory = new TwitterFactory();
		Configuration config = ConfigurationContext.getInstance();
		twitter = factory.getInstance();
//		twitter.setOAuthConsumer(config.getOAuthConsumerKey(), config.getOAuthConsumerSecret());
	}

	public final static Status updateStatus(String statusText, GeoLocation location) {
		try {
			if (location != null)
				return twitter.updateStatus(statusText, location);
			else
				return twitter.updateStatus(statusText);
		} catch (TwitterException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public final static List<Tweet> search(String searchTerm){
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

	public static void setAccessToken(AccessToken accessToken) {
		ProviderManager.accessToken = accessToken;
		twitter.setOAuthAccessToken(accessToken);
	}

	public static AccessToken getAccessToken() {
		return accessToken;
	}
}
