package mobi.my2cents;

import mobi.my2cents.data.Comment;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.RelativeTime;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedAdapter extends CursorAdapter {
	
	public FeedAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView userTextView = (TextView) view.findViewById(R.id.StreamCommentAuthorTextView);
		userTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_NAME)));

		TextView messageTextView = (TextView) view.findViewById(R.id.StreamCommentTextView);
		messageTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));

		TextView sentTextView = (TextView) view.findViewById(R.id.StreamCommentSentTextView);
		long time = cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT));
		sentTextView.setText(RelativeTime.getDifference(time));
		
		TextView productNameTextView = (TextView) view.findViewById(R.id.StreamProductTextView);
		String productName = cursor.getString(cursor.getColumnIndex(Comment.PRODUCT_NAME));
		if (!TextUtils.isEmpty(productName)) {
			productNameTextView.setText(productName);
		}

		ImageView productImageView = (ImageView) view.findViewById(R.id.StreamProductImageView);
		final String productKey = cursor.getString(cursor.getColumnIndex(Comment.PRODUCT_KEY));		
		if (ImageManager.hasImage(productKey)) {
			final Bitmap bitmap = ImageManager.getImage(productKey);
			productImageView.setImageBitmap(bitmap);
		}
//		else {
//			productImageView.setImageResource(R.drawable.unknown_product_icon);
//		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.stream_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}

}
