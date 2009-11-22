/*
 * Copyright (C) 2009 Anton Rau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.m2c;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public final class ManualInputActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setTitle("my2cents - Search for products");
		
		setContentView(R.layout.input_dialog);

		Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(mDoneListener);
	}

	private final Button.OnClickListener mDoneListener = new Button.OnClickListener() {
		public void onClick(View view) {
			EditText editor = (EditText) findViewById(R.id.barcodeInputEditText);

			Intent result = new Intent(getIntent().getAction());
			String productCode = editor.getText().toString();
			result.putExtra("PRODUCT_CODE", productCode);
			setResult(RESULT_OK, result);

			finish();
		}
	};
}
