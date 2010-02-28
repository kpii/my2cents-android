
package at.my2c.comments;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
import twitter4j.http.AccessToken;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;
import at.my2c.SettingsActivity;

public final class CommentsManager {

	public static HashMap<String, Bitmap> imagesMap = new HashMap<String, Bitmap>();
	
	private static Twitter provider;
	private final static int numberOfResults = 30;
	

	public final static void InitializeBasic(String username, String password)
	{
		TwitterFactory factory = new TwitterFactory();
		provider = factory.getInstance(username, password);
	}
	
	public final static void InitializeOAuth(AccessToken accessToken)
	{
		TwitterFactory factory = new TwitterFactory();
		provider = factory.getOAuthAuthorizedInstance(SettingsActivity.CONSUMER_KEY, SettingsActivity.CONSUMER_SECRET, accessToken);
	}

	public final static Comment sendComment(String text, Location location) {
		try {
			Status status = (location != null) ? 
				provider.updateStatus(text, new GeoLocation(location.getLatitude(), location.getLongitude())) :
				provider.updateStatus(text);
			
			return new Comment(
					status.getId(),
					status.getUser().getScreenName(),
					status.getUser().getProfileImageURL(),
					status.getSource(),
					status.getText(),
					status.getCreatedAt(),
					location);
			
		} catch (TwitterException e) {
			Log.e(CommentsManager.class.getName(), e.toString());
			return null;
		}
	}
	
	public final static List<Comment> searchComments(String searchTerm){
		Query query = new Query();
		query.setQuery(searchTerm);
		query.setRpp(numberOfResults);
		
		try {
			QueryResult queryResult = provider.search(query);
			List<Tweet> tweets = queryResult.getTweets();
			List<Comment> comments = new ArrayList<Comment>(tweets.size());
			for (Tweet tweet : tweets) {
				URL profileImageUrl = null;
				try {
					profileImageUrl = new URL(tweet.getProfileImageUrl());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					Log.e(CommentsManager.class.getName(), e.getMessage());
				}
				
				Location location = null;
				if (tweet.getGeoLocation() != null) {
					location = new Location("my2cents");
					location.setLatitude(tweet.getGeoLocation().getLatitude());
					location.setLongitude(tweet.getGeoLocation().getLongitude());
				}
	
				Comment item = new Comment(
						tweet.getId(),
						tweet.getFromUser(),
						profileImageUrl,
						tweet.getSource(),
						tweet.getText(),
						tweet.getCreatedAt(),
						location);
				comments.add(item);
			}
			return comments;
		} catch (TwitterException e) {
			Log.e(CommentsManager.class.getName(), e.toString());
			return null;
		}
	}
	
	public final static boolean verifyCredentials() {
		try {
			User user = provider.verifyCredentials();
			return true;
		} catch (TwitterException e) {
			if (e.getStatusCode() != 401)
				Log.e(CommentsManager.class.getName(), e.toString());
			return false;
		}
	}

	public final static Twitter getProvider() {
		return provider;
	}
}
