package at.m2c;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.Tweet;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemLongClickListener;
import at.m2c.data.DataManager;
import at.m2c.data.ProductInfo;
import at.m2c.data.ProductInfoManager;
import at.m2c.scanner.CaptureActivityHandler;
import at.m2c.util.GpsManager;
import at.m2c.util.NetworkManager;
import at.m2c.util.ProviderManager;

public final class ProductsActivity extends ListActivity {

	private final static int MANUAL_INPUT_CODE = 0;
	private final static int ACCOUNT_ACTIVITY_CODE = 1;

	private volatile boolean shutdownRequested;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.products);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!NetworkManager.isNetworkAvailable()) {
			return;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		shutdownRequested = false;

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		if (intent != null && action != null) {
			if (action.equals(Intents.ACTION)) {
				setTitle("my2cents :: " + DataManager.getProductInfo().getProductCode());
				
				TextView productNameTextView = (TextView) findViewById(R.id.productNameTextView);
				productNameTextView.setText("Loading product information...");
				
				new Thread(null, updateProductInfo, "ProductInfoUpdater").start();

				progressDialog = ProgressDialog.show(ProductsActivity.this, null, "Loading data...", true);
			}
		}
	}

	private Runnable updateProductInfo = new Runnable() {
		public void run() {
			ProductInfo product = DataManager.getProductInfo();
			ProductInfoManager.updateProductInfo(product);
			DataManager.getHistoryDatabase().AddEntry(product);
			runOnUiThread(displayProductInfo);
		}
	};

	private Runnable displayProductInfo = new Runnable() {
		public void run() {
			ProductInfo productInfo = DataManager.getProductInfo();
			
			ViewGroup layout = (ViewGroup) findViewById(R.id.ProductLayout);
			TextView productNameTextView = (TextView) findViewById(R.id.productNameTextView);
			
			if ((productInfo == null) || (productInfo.getProductName() == null) || (productInfo.getProductName().equals(""))) {
				Toast.makeText(ProductsActivity.this, "Product information was not found.", Toast.LENGTH_SHORT).show();
				productNameTextView.setText("Product information was not found.");
				layout.setVisibility(View.GONE);
			}
			else {
				productNameTextView.setText(productInfo.getProductName());
				layout.setVisibility(View.VISIBLE);
				
				if (productInfo.getProductImage() != null) {
					ImageView productImageView = (ImageView) findViewById(R.id.productImageView);
					productImageView.setImageBitmap(productInfo.getProductImage());
				}
			}
			
			progressDialog.dismiss();
		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		shutdownRequested = true;
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
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, ManualInputActivity.class);
				startActivityForResult(intent, MANUAL_INPUT_CODE);
				break;
			}
			case R.id.historyMenuItem: {
				Intent intent = new Intent(this, HistoryActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.preferencesMenuItem: {
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
				break;
			}
			case R.id.infoMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case MANUAL_INPUT_CODE: {
				if (resultCode == RESULT_OK) {
					String barcode = data.getStringExtra("PRODUCT_CODE");
					if (barcode != null && !barcode.equals("")) {
						ProductInfo productInfo = new ProductInfo(barcode);
						DataManager.setProductInfo(productInfo);
	
						Intent intent = new Intent(this, ProductsActivity.class);
						intent.setAction(Intents.ACTION);
						startActivity(intent);
					}
				}
				break;
			}
			case ACCOUNT_ACTIVITY_CODE: {
				if (resultCode == RESULT_OK) {
					
				}
				break;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		// Do nothing, this is to prevent the activity from being restarted when
		// the keyboard opens.
		super.onConfigurationChanged(config);
	}
}
