package at.my2c;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.my2c.data.Comment;
import at.my2c.data.DataManager;
import at.my2c.utils.RelativeTime;

public class StreamAdapter extends ArrayAdapter<Comment> {
	
	public StreamAdapter(Context context, int textViewResourceId, List<Comment> comments) {
		super(context, textViewResourceId, comments);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.stream_item, null);
		}

		Comment comment = getItem(position);
		if (comment != null) {
			TextView authorTextView = (TextView) view.findViewById(R.id.StreamCommentAuthorTextView);
			authorTextView.setText(comment.getUser());

			TextView messageTextView = (TextView) view.findViewById(R.id.StreamCommentTextView);
			messageTextView.setText(comment.getText());

			TextView sentTextView = (TextView) view.findViewById(R.id.StreamCommentSentTextView);
			sentTextView.setText(RelativeTime.getDifference(comment.getCreatedAt().getTime()));
			
			TextView productNameTextView = (TextView) view.findViewById(R.id.StreamProductTextView);
			if ((comment.getProductName() != null) && (!comment.getProductName().equals(""))) {	
				productNameTextView.setText(comment.getProductName());
			}
			else {
				productNameTextView.setText(comment.getGtin());
			}

			ImageView productImageView = (ImageView) view.findViewById(R.id.StreamProductImageView);
			if (comment.getProductImageUrl() != null) {	
				productImageView.setImageBitmap(DataManager.productImageCache.get(comment.getGtin()));
			}
			else {
				productImageView.setImageResource(R.drawable.unknown_product_icon);
			}
		}
		return view;
	}
}
