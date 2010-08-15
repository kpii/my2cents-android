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


public class ProductAdapter extends ResourceCursorAdapter
 {
	
	public ProductAdapter(Context context, Cursor cursor) {
		super(context, R.layout.comment_item, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		final TextView usernameTextView = (TextView) view.findViewById(R.id.CommentAuthorTextView);
		usernameTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_NAME)));

		final TextView bodyTextView = (TextView) view.findViewById(R.id.CommentTextView);
		bodyTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));

		final TextView sentTextView = (TextView) view.findViewById(R.id.CommentSentTextView);
		long time = cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT));
		sentTextView.setText(RelativeTime.getDifference(time));

		final ImageView profileImageView = (ImageView) view.findViewById(R.id.CommentImageView);
		final String url = cursor.getString(cursor.getColumnIndex(Comment.USER_IMAGE_URL));
		final Bitmap bitmap = ImageManager.getImage(url);
		profileImageView.setImageBitmap(bitmap);
		
		final ProgressBar stateProgressBar = (ProgressBar) view.findViewById(R.id.CommentStateProgressBar);
		final int state = cursor.getInt(cursor.getColumnIndex(Comment.PENDING));
		if (state == 1) {
			stateProgressBar.setVisibility(View.VISIBLE);
		}
		else {
			stateProgressBar.setVisibility(View.GONE);
		}
	}

}
