
package at.my2c;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import at.my2c.data.DataManager;
import at.my2c.utils.NetworkManager;

public final class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		findViewById(R.id.SearchImageButton).setOnClickListener(searchListener);
		
		findViewById(R.id.ImageButtonHome).setEnabled(false);
		findViewById(R.id.ImageButtonScan).setOnClickListener(scanListener);
		findViewById(R.id.ImageButtonStream).setOnClickListener(streamListener);
		findViewById(R.id.ImageButtonHistory).setOnClickListener(historyListener);
		
        settings = PreferenceManager.getDefaultSharedPreferences(this);
		NetworkManager.setAuthToken(settings.getString(getString(R.string.settings_token),""));
		
		onFirstLaunch();
	}
	
	private final ImageButton.OnClickListener scanListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener streamListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener historyListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};
	
	private final ImageButton.OnClickListener searchListener = new ImageButton.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.SearchEditText);
			String gtin = editor.getText().toString();
			if ((gtin != null) && (!gtin.equals(""))) {
				Intent intent = new Intent(view.getContext(), CommentActivity.class);
				intent.setAction(Intents.ACTION);
				intent.putExtra(DataManager.GTIN_KEY, gtin);
				startActivity(intent);
			}
		}
	};
	
	/**
	 * We want the help screen to be shown automatically the first time a new
	 * version of the app is run. The easiest way to do this is to check
	 * android:versionCode from the manifest, and compare it to a value stored
	 * as a preference.
	 */
	private void onFirstLaunch() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			int currentVersion = info.versionCode;
			int lastVersion = settings.getInt(getString(R.string.settings_first_start), 0);
			if (currentVersion > lastVersion) {
				settings.edit().putInt(getString(R.string.settings_first_start), currentVersion).commit();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(this, HelpActivity.class.getName());
				startActivity(intent);
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(TAG, e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settingsMenuItem: {
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.infoMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			}
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
