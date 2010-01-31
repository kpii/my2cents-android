
package at.my2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.my2c.data.DataManager;

public final class SearchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search_dialog);

		Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(doneListener);
	}

	private final Button.OnClickListener doneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.barcodeInputEditText);

			String searchTerm = editor.getText().toString();
			DataManager.setSearchTerm(searchTerm);

			Intent intent = new Intent(view.getContext(), MainActivity.class);
			intent.setAction(Intents.ACTION);
			startActivity(intent);

			finish();
		}
	};
}
