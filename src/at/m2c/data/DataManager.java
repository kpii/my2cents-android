package at.m2c.data;

import android.content.Context;

public final class DataManager {
	private static DatabaseHelper database;
	private static String searchTerm;

	public static void initDatabase(Context context) {
		database = new DatabaseHelper(context);
	}
	
	public static DatabaseHelper getDatabase() {
		return database;
	}

	public static void setSearchTerm(String searchTerm) {
		DataManager.searchTerm = searchTerm;
	}

	public static String getSearchTerm() {
		return searchTerm;
	}
}
