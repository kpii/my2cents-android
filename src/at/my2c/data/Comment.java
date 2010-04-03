package at.my2c.data;

import java.net.URL;
import java.util.Date;

public class Comment {
	private String user;
	private URL userProfileImageUrl;
	private String text;
    private Date createdAt;
    
    private int productId;
    private String gtin;
    private String productName;
    private URL productImageUrl;
    
    public Comment() {
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

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductImageUrl(URL productImageUrl) {
		this.productImageUrl = productImageUrl;
	}

	public URL getProductImageUrl() {
		return productImageUrl;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getProductId() {
		return productId;
	}

	public void setGtin(String gtin) {
		this.gtin = gtin;
	}

	public String getGtin() {
		return gtin;
	}
}
