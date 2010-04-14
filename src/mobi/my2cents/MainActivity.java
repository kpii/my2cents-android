
package mobi.my2cents;

import mobi.my2cents.utils.NetworkManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public final class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";	
	private SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		findViewById(R.id.HomeScanReadLayout).setOnClickListener(scanReadListener);
		findViewById(R.id.HomeScanCommentLayout).setOnClickListener(scanCommentListener);
		findViewById(R.id.HomeStreamLayout).setOnClickListener(streamListener);
		findViewById(R.id.HomeHistoryLayout).setOnClickListener(historyListener);
		findViewById(R.id.HomeTwitterLayout).setOnClickListener(loginListener);
		
        settings = PreferenceManager.getDefaultSharedPreferences(this);
		NetworkManager.setAuthToken(settings.getString(getString(R.string.settings_token),""));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		boolean isLoggedIn = settings.getBoolean(getString(R.string.settings_login), false);
		boolean isSharingOnTwitter = settings.getBoolean(getString(R.string.settings_twitter), false);
		
		if (isLoggedIn && isSharingOnTwitter) {
			findViewById(R.id.HomeTwitterLayout).setVisibility(View.GONE);
		}
		else {
			findViewById(R.id.HomeTwitterLayout).setVisibility(View.VISIBLE);
			TextView headerTextView = (TextView) findViewById(R.id.HomeTwitterHeaderTextView);
			if (isLoggedIn && !isSharingOnTwitter) {
				headerTextView.setText(R.string.home_button_twitter_share_header);
			}
			else {
				headerTextView.setText(R.string.home_button_twitter_login_header);
			}
		}
	}
	
	private final View.OnClickListener scanReadListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			intent.putExtra(getString(R.string.show_virtual_keyboard), false);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener scanCommentListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), ScanActivity.class);
			intent.putExtra(getString(R.string.show_virtual_keyboard), true);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener streamListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), StreamActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener historyListener = new View.OnClickListener() {
		public void onClick(View view) {
			Intent intent = new Intent(getBaseContext(), HistoryActivity.class);
			startActivity(intent);
		}
	};
	
	private final View.OnClickListener loginListener = new View.OnClickListener() {
		public void onClick(View view) {
			boolean isLoggedIn = settings.getBoolean(getString(R.string.settings_login), false);
			boolean isSharingOnTwitter = settings.getBoolean(getString(R.string.settings_twitter), false);
			if (isLoggedIn && !isSharingOnTwitter) {
				Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
				startActivity(intent);
			}
			else {
				Intent intent = new Intent(getBaseContext(), AuthorizationActivity.class);
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
