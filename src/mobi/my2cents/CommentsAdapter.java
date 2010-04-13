package mobi.my2cents;

import java.util.List;

import mobi.my2cents.data.Comment;
import mobi.my2cents.data.DataManager;
import mobi.my2cents.utils.RelativeTime;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsAdapter extends ArrayAdapter<Comment> {
	
	public CommentsAdapter(Context context, int textViewResourceId, List<Comment> comments) {
		super(context, textViewResourceId, comments);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.comment_item, null);
		}

		Comment comment = getItem(position);
		if (comment != null) {
			TextView authorTextView = (TextView) view.findViewById(R.id.CommentAuthorTextView);
			authorTextView.setText(comment.getUser());

			TextView messageTextView = (TextView) view.findViewById(R.id.CommentTextView);
			messageTextView.setText(comment.getText());

			TextView sentTextView = (TextView) view.findViewById(R.id.CommentSentTextView);
			sentTextView.setText(RelativeTime.getDifference(comment.getCreatedAt().getTime()));

			ImageView avatarImageView = (ImageView) view.findViewById(R.id.CommentImageView);
			avatarImageView.setImageBitmap(DataManager.profileImageCache.get(comment.getUser()));
		}
		return view;
	}
}
