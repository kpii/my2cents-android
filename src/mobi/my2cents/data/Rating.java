package mobi.my2cents.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public final class Rating {
	
	private static final String TAG = "Rating";
	
	private int likes;
	private int dislikes;
	private String myOpinion;
	
	public Rating() {
		
	}

	public Rating(int likes, int dislikes, String myOpinion) {
		this.setLikes(likes);
		this.setDislikes(dislikes);
		this.setMyOpinion(myOpinion);
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getLikes() {
		return likes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setMyOpinion(String myOpinion) {
		this.myOpinion = myOpinion;
	}

	public String getMyOpinion() {
		return myOpinion;
	}
	
	public JSONObject getJson() {
		JSONObject result = null;
		try {
			result = new JSONObject("rating");
			result.put("likes", likes);
			result.put("dislikes", dislikes);
			result.put("me", myOpinion);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
	
	public static Rating ParseJson(JSONObject json) {
		Rating result = new Rating();
		try {
			result.setLikes(json.getInt("likes"));
			result.setDislikes(json.getInt("dislikes"));
			if (json.has("me")) {
				result.setMyOpinion(json.getString("me"));
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return result;
	}
}
