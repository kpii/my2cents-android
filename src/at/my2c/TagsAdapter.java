package at.my2c;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagsAdapter extends ArrayAdapter<String> {

	public TagsAdapter(Context context, int textViewResourceId, ArrayList<String> tags) {
		super(context, textViewResourceId, tags);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.tag_item, null);
		}

		String tag = getItem(position);
		if (tag != null) {
			TextView tagTextView = (TextView) view.findViewById(R.id.TagTextView);
			tagTextView.setText(tag);
		}
		return view;
	}
}
