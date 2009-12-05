package at.m2c;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import at.m2c.util.NetworkManager;

public class MainActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TabHost host = getTabHost();
		
		host.addTab(host.newTabSpec("products").setIndicator("Products").setContent(new Intent(this, ProductsActivity.class).setAction(Intents.ACTION)));
		host.addTab(host.newTabSpec("comments").setIndicator("Comments").setContent(new Intent(this, CommentsActivity.class).setAction(Intents.ACTION)));
		host.addTab(host.newTabSpec("history").setIndicator("History").setContent(new Intent(this, HistoryActivity.class)));
	}
}
