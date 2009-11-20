package at.m2c.data;

import android.content.Context;

public final class DataManager {
	private static HistoryDatabase historyDatabase;
	private static ProductInfo productInfo;

	public static void setProductInfo(ProductInfo productInfo) {
		DataManager.productInfo = productInfo;
	}

	public static ProductInfo getProductInfo() {
		return productInfo;
	}

	public static void initHistoryDatabase(Context context) {
		historyDatabase = new HistoryDatabase(context);
	}
	
	public static HistoryDatabase getHistoryDatabase() {
		return historyDatabase;
	}
}
