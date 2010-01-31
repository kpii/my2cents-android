package at.my2c;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TabHost host = getTabHost();
		
		host.addTab(host.newTabSpec("products").setIndicator("Products", getResources().getDrawable(R.drawable.products)).setContent(new Intent(this, ProductsActivity.class).setAction(Intents.ACTION)));
		host.addTab(host.newTabSpec("comments").setIndicator("Comments", getResources().getDrawable(R.drawable.comments)).setContent(new Intent(this, CommentsActivity.class).setAction(Intents.ACTION)));
		host.addTab(host.newTabSpec("favorites").setIndicator("Favorites", getResources().getDrawable(R.drawable.love)).setContent(new Intent(this, FavoritesActivity.class)));
		host.addTab(host.newTabSpec("history").setIndicator("History", getResources().getDrawable(android.R.drawable.ic_menu_recent_history)).setContent(new Intent(this, HistoryActivity.class)));
	}
}
