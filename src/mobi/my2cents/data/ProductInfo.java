package mobi.my2cents.data;

import java.util.ArrayList;

import mobi.my2cents.My2Cents;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;

public final class ProductInfo {

	
	private final String gtin;
	private String name;
	private String affiliateName;
	private String affiliateUrl;
	private String imageUrl;
	private Bitmap image;
	private Rating rating;
	private ArrayList<Comment> comments;
	private String etag;
	

	public ProductInfo(String gtin) {
		this.gtin = gtin;
		rating = new Rating();
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
	

	public void setRating(Rating rating) {
		this.rating = rating;
	}


	public Rating getRating() {
		return rating;
	}
	
	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	
	public String getEtag() {
		return etag;
	}
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Product.NAME, name);
		cv.put(Product.KEY, gtin);
		return cv;
	}
	
}
