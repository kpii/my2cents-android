package at.my2c.comments;

import java.net.URL;
import java.util.Date;

import android.location.Location;

public class Comment {
	private long id;
	private String user;
	private URL userProfileImageUrl;
    private String source;
	private String text;
    private Date createdAt;
    private Location location;
    
    public Comment(
    		long id,
    		String user,
    		URL userProfileImageUrl,
    		String source,
    		String text,
    		Date createdAt,
    		Location location) {
    	this.setId(id);
    	this.setUser(user);
    	this.setUserProfileImageUrl(userProfileImageUrl);
    	this.setSource(source);
    	this.setText(text);
    	this.setCreatedAt(createdAt);
    	this.setLocation(location);
    }

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setUserProfileImageUrl(URL userProfileImageUrl) {
		this.userProfileImageUrl = userProfileImageUrl;
	}

	public URL getUserProfileImageUrl() {
		return userProfileImageUrl;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}
}
