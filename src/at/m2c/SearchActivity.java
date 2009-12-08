
package at.m2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import at.m2c.data.DataManager;
import at.m2c.data.ProductInfo;

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
			
			ProductInfo productInfo = new ProductInfo(searchTerm);
			DataManager.setProductInfo(productInfo);

			Intent intent = new Intent(view.getContext(), MainActivity.class);
			intent.setAction(Intents.ACTION);
			startActivity(intent);

			finish();
		}
	};
}
