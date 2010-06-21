package mobi.my2cents;

import mobi.my2cents.data.Product;
import mobi.my2cents.utils.ImageManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class HistoryAdapter extends CursorAdapter {
	
	public HistoryAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		String key = cursor.getString(cursor.getColumnIndex(Product.KEY));
		
		TextView keyTextView = (TextView) view.findViewById(R.id.HistoryProductCodeTextView);
		keyTextView.setText(key);

		TextView nameTextView = (TextView) view.findViewById(R.id.HistoryProductNameTextView);
		nameTextView.setText(cursor.getString(cursor.getColumnIndex(Product.NAME)));

//		TextView timeTextView = (TextView) view.findViewById(R.id.HistoryTimeTextView);
//		timeTextView.setText(cursor.getString(cursor.getColumnIndex(History.TIME)));
		
		ImageView imageView = (ImageView) view.findViewById(R.id.HistoryImageView);
		if (ImageManager.hasImage(key)) {
			final Bitmap bitmap = ImageManager.getImage(key);
			imageView.setImageBitmap(bitmap);
		}
		else {
			imageView.setImageResource(R.drawable.unknown_product_icon);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.history_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}

}
