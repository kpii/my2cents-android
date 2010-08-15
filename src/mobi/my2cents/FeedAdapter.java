package mobi.my2cents;

import mobi.my2cents.data.Comment;
import mobi.my2cents.utils.ImageManager;
import mobi.my2cents.utils.RelativeTime;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class FeedAdapter extends ResourceCursorAdapter {
	
	public FeedAdapter(Context context, Cursor cursor) {
		super(context, R.layout.feed_item, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		final TextView userTextView = (TextView) view.findViewById(R.id.StreamCommentAuthorTextView);
		userTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_NAME)));

		final TextView messageTextView = (TextView) view.findViewById(R.id.StreamCommentTextView);
		messageTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));

		final TextView sentTextView = (TextView) view.findViewById(R.id.StreamCommentSentTextView);
		final long time = cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT));
		sentTextView.setText(RelativeTime.getDifference(time));
		
		final TextView productNameTextView = (TextView) view.findViewById(R.id.StreamProductTextView);
		final String productName = cursor.getString(cursor.getColumnIndex(Comment.PRODUCT_NAME));
		productNameTextView.setText(productName);

		ImageView productImageView = (ImageView) view.findViewById(R.id.StreamProductImageView);
		final String url = cursor.getString(cursor.getColumnIndex(Comment.PRODUCT_IMAGE_URL));
		final Bitmap bitmap = ImageManager.getImage(url);
		productImageView.setImageBitmap(bitmap);
		
		final ProgressBar stateProgressBar = (ProgressBar) view.findViewById(R.id.StreamStateProgressBar);
		final int state = cursor.getInt(cursor.getColumnIndex(Comment.PENDING));
		if (state == 1) {
			stateProgressBar.setVisibility(View.VISIBLE);
		}
		else {
			stateProgressBar.setVisibility(View.GONE);
		}
	}

}
