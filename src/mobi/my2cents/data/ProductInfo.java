package mobi.my2cents.data;

import java.util.ArrayList;

import android.graphics.Bitmap;

public final class ProductInfo {

	private final String gtin;
	private String name;
	private String manufacturer;
	private String detailPageUrl;
	private String imageUrl;
	private Bitmap image;
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

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setDetailPageUrl(String detailPageUrl) {
		this.detailPageUrl = detailPageUrl;
	}

	public String getDetailPageUrl() {
		return detailPageUrl;
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
}
