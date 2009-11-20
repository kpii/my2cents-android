package at.m2c.data;

import android.graphics.Bitmap;

public final class ProductInfo {

	private final String productCode;
	private String productName;
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
}
