package mobi.my2cents;

import mobi.my2cents.data.Comment;
import mobi.my2cents.utils.ImageManager;
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


public class CommentsAdapter extends CursorAdapter {
	
	public CommentsAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView usernameTextView = (TextView) view.findViewById(R.id.CommentAuthorTextView);
		usernameTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.USER_NAME)));

		TextView bodyTextView = (TextView) view.findViewById(R.id.CommentTextView);
		bodyTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.BODY)));

		TextView sentTextView = (TextView) view.findViewById(R.id.CommentSentTextView);
		long time = cursor.getLong(cursor.getColumnIndex(Comment.CREATED_AT));
		sentTextView.setText(RelativeTime.getDifference(time));

		final ImageView profileImageView = (ImageView) view.findViewById(R.id.CommentImageView);
		final String username = cursor.getString(cursor.getColumnIndex(Comment.USER_NAME));
		if (ImageManager.hasImage(username)) {
			final Bitmap bitmap = ImageManager.getImage(username);
			profileImageView.setImageBitmap(bitmap);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.comment_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}

}
