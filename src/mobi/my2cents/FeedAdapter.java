package mobi.my2cents;

import java.util.Date;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.RelativeTime;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
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
		
//		TextView userTextView = (TextView) view.findViewById(R.id.StreamCommentAuthorTextView);
//		userTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_NAME)));

		TextView messageTextView = (TextView) view.findViewById(R.id.StreamCommentTextView);
		messageTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));

		TextView sentTextView = (TextView) view.findViewById(R.id.StreamCommentSentTextView);
		long time = cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT));
		sentTextView.setText(RelativeTime.getDifference(time));
		
//		TextView productNameTextView = (TextView) view.findViewById(R.id.StreamProductTextView);
//		if ((comment.getProductName() != null) && (!comment.getProductName().equals(""))) {	
//			productNameTextView.setText(comment.getProductName());
//		}
//		else {
//			productNameTextView.setText("Unknown product [" + comment.getGtin() + "]");
//		}

		String profileUrl = cursor.getString(cursor.getColumnIndex(Comment.URI));
		if (DataManager.profileImageCache.containsKey(profileUrl)) {
			ImageView productImageView = (ImageView) view.findViewById(R.id.StreamProductImageView);
			Bitmap bitmap = DataManager.profileImageCache.get(profileUrl);
			productImageView.setImageBitmap(bitmap);
//			if (comment.getProductImageUrl() != null) {	
//				productImageView.setImageBitmap(DataManager.productImageCache.get(comment.getGtin()));
//			}
//			else {
//				productImageView.setImageResource(R.drawable.unknown_product_icon);
//			}
		}		

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.stream_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}

}
