package at.my2c.data;

import android.graphics.Bitmap;

public final class ProductInfo {

	private final String productId;
	private String productCode;
	private String productName;
	private String productDescription;
	private ProductInfoProvider productInfoProvider;
	private String detailPageUrl;
	private String productImageUrl;
	private Bitmap productImage;

	public ProductInfo(String productId) {
		this.productId = productId;
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

	public void setProductInfoProvider(ProductInfoProvider productInfoProvider) {
		this.productInfoProvider = productInfoProvider;
	}

	public ProductInfoProvider getProductInfoProvider() {
		return productInfoProvider;
	}

	public void setProductImageUrl(String productImageUrl) {
		this.productImageUrl = productImageUrl;
	}

	public String getProductImageUrl() {
		return productImageUrl;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setDetailPageUrl(String detailPageUrl) {
		this.detailPageUrl = detailPageUrl;
	}

	public String getDetailPageUrl() {
		return detailPageUrl;
	}
}
