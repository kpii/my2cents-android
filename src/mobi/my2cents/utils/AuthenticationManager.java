package mobi.my2cents.utils;

import java.util.UUID;

import mobi.my2cents.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public final class AuthenticationManager {
	
	private static Context context;
	private static String clientToken;
	
	public static final void initialize(Context appContext) {
		context = appContext;
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final String key = context.getString(R.string.client_token);
		clientToken = settings.getString(key, "");
		if(TextUtils.isEmpty(clientToken)) {
			clientToken = UUID.randomUUID().toString();
			settings.edit().putString(key, clientToken).commit();
		}
	}
	
	public static final void logout() {
		clientToken = UUID.randomUUID().toString();
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final String key = context.getString(R.string.client_token);
		settings.edit().putString(key, clientToken).commit();
	}

	public static final String getClientToken() {
		return clientToken;
	}
}
