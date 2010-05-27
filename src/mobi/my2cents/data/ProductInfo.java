package mobi.my2cents.data;

import java.util.ArrayList;

import android.graphics.Bitmap;

public final class ProductInfo {

	private final String gtin;
	private String name;
	private String affiliateName;
	private String affiliateUrl;
	private String imageUrl;
	private Bitmap image;
	private int likes;
	private int dislikes;
	private ArrayList<Comment> comments;
	

	public ProductInfo(String gtin) {
		this.gtin = gtin;
		comments = new ArrayList<Comment>();
	}

	
	public String getGtin() {
		return gtin;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAffiliateUrl(String affiliateUrl) {
		this.affiliateUrl = affiliateUrl;
	}

	public String getAffiliateUrl() {
		return affiliateUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}

	public ArrayList<Comment> getComments() {
		return comments;
	}


	public void setAffiliateName(String affiliateName) {
		this.affiliateName = affiliateName;
	}


	public String getAffiliateName() {
		return affiliateName;
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
}
