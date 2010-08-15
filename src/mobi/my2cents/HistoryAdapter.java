package mobi.my2cents;

import mobi.my2cents.data.Product;
import mobi.my2cents.utils.ImageManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;


public class HistoryAdapter extends ResourceCursorAdapter {
	
	public HistoryAdapter(Context context, Cursor cursor) {
		super(context, R.layout.history_item, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		final String key = cursor.getString(cursor.getColumnIndex(Product.KEY));
		
		TextView keyTextView = (TextView) view.findViewById(R.id.HistoryProductCodeTextView);
		keyTextView.setText(key);

		TextView nameTextView = (TextView) view.findViewById(R.id.HistoryProductNameTextView);
		nameTextView.setText(cursor.getString(cursor.getColumnIndex(Product.NAME)));

//		TextView timeTextView = (TextView) view.findViewById(R.id.HistoryTimeTextView);
//		timeTextView.setText(cursor.getString(cursor.getColumnIndex(History.TIME)));
		
		ImageView imageView = (ImageView) view.findViewById(R.id.HistoryImageView);
		final String url = cursor.getString(cursor.getColumnIndex(Product.IMAGE_URL));
		final Bitmap bitmap = ImageManager.getImage(url);
		imageView.setImageBitmap(bitmap);
	}

}
