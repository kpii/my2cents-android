
package at.my2c;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import at.my2c.data.HistoryColumns;
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
		
		showTips();
	}
	
	private void showTips() {
		boolean toOpen = settings.getBoolean(getString(R.string.settings_helpful_tips), true);
		if(toOpen) {
			settings.edit().putBoolean(getResources().getString(R.string.settings_helpful_tips), false).commit();
			Intent intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
		}
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
				intent.putExtra(HistoryColumns.GTIN, gtin);
				startActivity(intent);
			}
		}
	};

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
