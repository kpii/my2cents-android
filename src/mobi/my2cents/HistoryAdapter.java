package mobi.my2cents;

import mobi.my2cents.data.DataManager;
import mobi.my2cents.data.History;
import mobi.my2cents.utils.Helper;
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
		
		String key = cursor.getString(cursor.getColumnIndex(History.PRODUCT_KEY));
		
		TextView keyTextView = (TextView) view.findViewById(R.id.HistoryProductCodeTextView);
		keyTextView.setText(key);

		TextView nameTextView = (TextView) view.findViewById(R.id.HistoryProductNameTextView);
		nameTextView.setText(cursor.getString(cursor.getColumnIndex(History.NAME)));

		TextView timeTextView = (TextView) view.findViewById(R.id.HistoryTimeTextView);
		timeTextView.setText(cursor.getString(cursor.getColumnIndex(History.TIME)));
		
		ImageView imageView = (ImageView) view.findViewById(R.id.HistoryImageView);
		if (DataManager.productImageCache.containsKey(key)) {
			imageView.setImageBitmap(DataManager.productImageCache.get(key));
		}
		else {
			byte[] bitmapArray = cursor.getBlob(cursor.getColumnIndex(History.IMAGE));
			if (bitmapArray != null) {
				Bitmap bitmap = Helper.getByteArrayAsBitmap(bitmapArray);
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
					DataManager.productImageCache.put(key, bitmap);
				}
			}
			else {
				imageView.setImageResource(R.drawable.unknown_product_icon);
			}
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
