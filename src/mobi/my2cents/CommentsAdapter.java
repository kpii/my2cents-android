package mobi.my2cents;

import mobi.my2cents.data.Comment;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
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
		sentTextView.setText(cursor.getString(cursor.getColumnIndex(Comment.CREATED_AT)));

//		ImageView profileImageView = (ImageView) view.findViewById(R.id.CommentImageView);
//		Bitmap bitmap = Helper.getByteArrayAsBitmap(cursor.getBlob(cursor.getColumnIndex(Comment.USER_ID)));
//		profileImageView.setImageBitmap(bitmap);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.comment_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}

}
