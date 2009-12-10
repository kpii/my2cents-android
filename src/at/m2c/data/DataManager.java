package at.m2c.data;

import android.content.Context;

public final class DataManager {
	private static HistoryDatabase historyDatabase;
	private static String searchTerm;

	public static void initHistoryDatabase(Context context) {
		historyDatabase = new HistoryDatabase(context);
	}
	
	public static HistoryDatabase getHistoryDatabase() {
		return historyDatabase;
	}

	public static void setSearchTerm(String searchTerm) {
		DataManager.searchTerm = searchTerm;
	}

	public static String getSearchTerm() {
		return searchTerm;
	}
}
