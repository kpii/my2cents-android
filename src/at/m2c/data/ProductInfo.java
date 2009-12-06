package at.m2c.data;

import android.graphics.Bitmap;

public final class ProductInfo {

	private final String productCode;
	private String productName;
	private String productDescription;
	private String productInfoProvider;
	private String productImageUrl;
	private Bitmap productImage;

	public ProductInfo(String productCode) {
		this.productCode = productCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductImage(Bitmap productImage) {
		this.productImage = productImage;
	}

	public Bitmap getProductImage() {
		return productImage;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductInfoProvider(String productInfoProvider) {
		this.productInfoProvider = productInfoProvider;
	}

	public String getProductInfoProvider() {
		return productInfoProvider;
	}

	public void setProductImageUrl(String productImageUrl) {
		this.productImageUrl = productImageUrl;
	}

	public String getProductImageUrl() {
		return productImageUrl;
	}
}
