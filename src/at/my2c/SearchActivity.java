
package at.my2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import at.my2c.data.DataManager;
import at.my2c.data.HistoryColumns;

public final class SearchActivity extends Activity {
	
	private static final String TAG = "SearchActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search_dialog);
		
		RadioGroup searchRadioGroup = (RadioGroup) findViewById(R.id.SearchRadioGroup);
		searchRadioGroup.setOnCheckedChangeListener(searchModeListener);

		Button searchButton = (Button) findViewById(R.id.SearchButton);
		searchButton.setOnClickListener(searchListener);
	}
	
	private final RadioGroup.OnCheckedChangeListener searchModeListener = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			EditText editor = (EditText) findViewById(R.id.InputEditText);
			if (checkedId == R.id.TitleRadioButton) {
				editor.setHint(R.string.search_by_title);
				editor.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
			}
			else {
				editor.setHint(R.string.search_by_code);
				editor.setInputType(InputType.TYPE_CLASS_NUMBER);
			}
		}
	};

	private final Button.OnClickListener searchListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.InputEditText);

			String gtin = editor.getText().toString();

			Intent intent = new Intent(view.getContext(), CommentActivity.class);
			intent.setAction(Intents.ACTION);
			intent.putExtra(HistoryColumns.GTIN, gtin);
			startActivity(intent);

			finish();
		}
	};
}
