package at.m2c;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import at.m2c.data.DataManager;
import at.m2c.data.ProductInfo;
import at.m2c.data.ProductInfoManager;
import at.m2c.util.NetworkManager;

public final class ProductsActivity extends ListActivity {

	private volatile boolean shutdownRequested;
	
	private ProgressDialog progressDialog;
	private List<ProductInfo> products;
	private ProductInfoAdapter productInfoAdapter;

	private HashMap<String, Bitmap> avatarMap = new HashMap<String, Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.products);

		products = new ArrayList<ProductInfo>();
		productInfoAdapter = new ProductInfoAdapter(this, R.layout.product_item, products);
		setListAdapter(productInfoAdapter);
		
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		NetworkManager.checkNetworkAvailability(this);
		if (!NetworkManager.isNetworkAvailable()) {
			Toast.makeText(this, "No Network Connection", Toast.LENGTH_LONG).show();
			return;
		}
		
		shutdownRequested = false;

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		if (intent != null && action != null) {
			if (action.equals(Intents.ACTION)) {
				setTitle("my2cents :: " + DataManager.getSearchTerm());
				
				new GetProductInfo().execute(DataManager.getSearchTerm());
			}
		}
	}
	
	private class GetProductInfo extends AsyncTask<String, Void, List<ProductInfo>> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(ProductsActivity.this, null, "Loading products...", true);
	    }
		
		@Override
		protected List<ProductInfo> doInBackground(String... params) {
			return ProductInfoManager.getProductsFromAmazon(params[0]);
		}
		
		@Override
		protected void onPostExecute(List<ProductInfo> result) {
			products = result;
	         
	        ViewGroup notificationLayout = (ViewGroup) findViewById(R.id.ProductsNotificationLayout);
				
			productInfoAdapter.clear();
			if (products != null && products.size() > 0) {
				notificationLayout.setVisibility(View.GONE);
				
				for (ProductInfo product : products) {
					productInfoAdapter.add(product);
				}
				
				productInfoAdapter.notifyDataSetChanged();
				progressDialog.dismiss();
				
				new GetProductImages().execute(productInfoAdapter.items);
			}
			else {
				progressDialog.dismiss();
				
				TextView notificationTextView = (TextView) findViewById(R.id.productsNotificationTextView);
				notificationTextView.setText("No comments yet. Be the first one to add your 2 cents on this product! Click on 'Add my 2 cents'");
				notificationLayout.setVisibility(View.VISIBLE);
			}
	    }
	}
	
	private class GetProductImages extends AsyncTask<List<ProductInfo>, Void, Void> {
		
		@Override
		protected Void doInBackground(List<ProductInfo>... params) {
			URL url = null;
			for (ProductInfo product : params[0]) {
				if (avatarMap.containsKey(product.getProductName())) {
					product.setProductImage(avatarMap.get(product.getProductName()));
				} else {
					try {
						url = new URL(product.getProductImageUrl());
					} catch (MalformedURLException e) {
						Log.e(this.toString(), e.toString());
					}
					product.setProductImage(NetworkManager.getRemoteImage(url));
					avatarMap.put(product.getProductName(), product.getProductImage());
				}
				publishProgress();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... progress) {
			productInfoAdapter.notifyDataSetChanged();
	    }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (!NetworkManager.isNetworkAvailable()) {
			return;
		}
		
		if (productInfoAdapter.getCount() > 0)
			productInfoAdapter.notifyDataSetChanged();
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.product_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.productDetailContextMenuItem: {
				ProductInfo selectedProduct = productInfoAdapter.items.get(info.position);
				
				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(selectedProduct.getDetailPageUrl()));  
				startActivity(viewIntent);
				return true;
			}
			case R.id.productAddToFavoritesContextMenuItem: {
				ProductInfo selectedProduct = productInfoAdapter.items.get(info.position);
				
				DataManager.getDatabase().addFavoriteItem(selectedProduct);
				return true;
			}
			default:
				return super.onContextItemSelected(item);
		}		
	}

	@Override
	protected void onPause() {
		super.onPause();
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
			case R.id.scanMenuItem: {
				Intent intent = new Intent(this, CaptureActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.searchMenuItem: {
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.preferencesMenuItem: {
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.infoMenuItem: {
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private class ProductInfoAdapter extends ArrayAdapter<ProductInfo> {

		private List<ProductInfo> items;

		public ProductInfoAdapter(Context context, int textViewResourceId, List<ProductInfo> products) {
			super(context, textViewResourceId, products);
			this.items = products;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.product_item, null);
			}

			ProductInfo product = items.get(position);
			if (product != null) {
				TextView productNameTextView = (TextView) view.findViewById(R.id.product_name_textview);
				productNameTextView.setText(product.getProductId());

				TextView productInfoProviderTextView = (TextView) view.findViewById(R.id.product_info_provider_textview);
				productInfoProviderTextView.setText(product.getProductInfoProvider().toString());

				TextView productDescriptionTextView = (TextView) view.findViewById(R.id.product_description_textview);
				productDescriptionTextView.setText(product.getProductName());

				ImageView productImageView = (ImageView) view.findViewById(R.id.product_imageview);
				productImageView.setImageBitmap(product.getProductImage());
			}
			return view;
		}
	}
}
