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

package at.my2c;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.my2c.util.ProviderManager;

public final class AccountActivity extends Activity {
	private ProgressDialog progressDialog;
	
	private String username;
	private String password;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_dialog);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		username = preferences.getString(PreferencesActivity.TWITTER_USERNAME, "");
		password = preferences.getString(PreferencesActivity.TWITTER_PASSWORD, "");
		
		EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
		usernameEditText.setText(username);
		
		EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		passwordEditText.setText(password);

		Button accountDoneButton = (Button) findViewById(R.id.accountDoneButton);
		accountDoneButton.setOnClickListener(doneListener);
		
		Button accountCancelButton = (Button) findViewById(R.id.accountCancelButton);
		accountCancelButton.setOnClickListener(cancelListener);
		
		CheckBox showPasswordCheckBox = (CheckBox) findViewById(R.id.showPasswordCheckBox);
		showPasswordCheckBox.setOnCheckedChangeListener(showPasswordListener);
	}

	private final OnClickListener doneListener = new OnClickListener() {
		public void onClick(View view) {
			
			EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
			username = usernameEditText.getText().toString();
			
			EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
			password = passwordEditText.getText().toString();
			
			ProviderManager.InitializeBasic(username, password);
			
			new CheckAccount().execute();
		}
	};
	
	private final OnClickListener cancelListener = new OnClickListener() {
		public void onClick(View view) {
			Intent result = new Intent(getIntent().getAction());
			setResult(Activity.RESULT_CANCELED, result);
			finish();
		}
	};
	
	private final OnCheckedChangeListener showPasswordListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
			if (isChecked) {
				passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
			}
			else
			{
				passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
		}
	};
	
	
	private class CheckAccount extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(AccountActivity.this, getString(R.string.progress_dialog_account_title), getString(R.string.progress_dialog_checking_credentials), true);
	    }
		
		@Override
		protected Boolean doInBackground(Void... params) {
			return ProviderManager.verifyCredentials();
		}
		
		@Override
		protected void onPostExecute(Boolean isLoginCorrect) {
			progressDialog.dismiss();
			
			if (isLoginCorrect) {
				Toast.makeText(AccountActivity.this, R.string.message_login_correct, Toast.LENGTH_SHORT).show();
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AccountActivity.this);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString(PreferencesActivity.TWITTER_USERNAME, username);
				editor.putString(PreferencesActivity.TWITTER_PASSWORD, password);
				
				editor.putBoolean(PreferencesActivity.IS_COMMENTING_POSSIBLE, ProviderManager.isCommentingPossible());

			    editor.commit();
				
				Intent result = new Intent(getIntent().getAction());
				setResult(RESULT_OK, result);
				finish();
			} else {
				Toast.makeText(AccountActivity.this, R.string.message_login_incorrect, Toast.LENGTH_SHORT).show();
			}
	    }
	}
}
