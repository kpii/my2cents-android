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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import at.m2c.util.ProviderManager;

public final class AccountActivity extends Activity {
	private boolean credentialsCorrect;
	private ProgressDialog progressDialog;
	
	private String provider;
	private String username;
	private String password;
	private String customApiUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_dialog);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		provider = preferences.getString(PreferencesActivity.TWITTER_PROVIDER, "Twitter");
		username = preferences.getString(PreferencesActivity.TWITTER_USERNAME, "");
		password = preferences.getString(PreferencesActivity.TWITTER_PASSWORD, "");
		customApiUrl = preferences.getString(PreferencesActivity.TWITTER_CUSTOM_API_URL, "");
		
		Spinner providerSpinner = (Spinner) findViewById(R.id.providerSpinner);
		providerSpinner.setOnItemSelectedListener(providerListener);
		int index = 0;
		if (provider.equals("Identi.ca"))
			index = 1;
		else if (provider.equals("Custom"))
			index = 2;
		providerSpinner.setSelection(index);
		
		EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
		usernameEditText.setText(username);
		
		EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
		passwordEditText.setText(password);
		
		EditText customApiEditText = (EditText) findViewById(R.id.customApiEditText);
		customApiEditText.setText(customApiUrl);

		Button accountDoneButton = (Button) findViewById(R.id.accountDoneButton);
		accountDoneButton.setOnClickListener(doneListener);
		
		Button accountCancelButton = (Button) findViewById(R.id.accountCancelButton);
		accountCancelButton.setOnClickListener(cancelListener);
		
		CheckBox showPasswordCheckBox = (CheckBox) findViewById(R.id.showPasswordCheckBox);
		showPasswordCheckBox.setOnCheckedChangeListener(showPasswordListener);
	}
	
	private final OnItemSelectedListener providerListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			String value = parent.getItemAtPosition(position).toString();
			if (value.equals("Custom")) {
				TextView customApiTextView = (TextView) findViewById(R.id.customApiTextView);
				customApiTextView.setVisibility(View.VISIBLE);
				
				EditText customApiEditText = (EditText) findViewById(R.id.customApiEditText);
				customApiEditText.setVisibility(View.VISIBLE);
			}
			else
			{
				TextView customApiTextView = (TextView) findViewById(R.id.customApiTextView);
				customApiTextView.setVisibility(View.GONE);
				
				EditText customApiEditText = (EditText) findViewById(R.id.customApiEditText);
				customApiEditText.setVisibility(View.GONE);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub			
		}		
	};
	
	private Runnable checkCredentials = new Runnable() {
		public void run() {
			credentialsCorrect = ProviderManager.verifyCredentials();
			runOnUiThread(checkingComplete);
		}
	};
	
	private Runnable checkingComplete = new Runnable() {
		public void run() {			
			progressDialog.dismiss();
			
			if (credentialsCorrect) {
				Toast.makeText(AccountActivity.this, "Login Correct.", Toast.LENGTH_SHORT).show();
				
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AccountActivity.this);
				SharedPreferences.Editor editor = sharedPreferences.edit();

				editor.putString(PreferencesActivity.TWITTER_PROVIDER, provider);
				editor.putString(PreferencesActivity.TWITTER_USERNAME, username);
				editor.putString(PreferencesActivity.TWITTER_PASSWORD, password);
				editor.putString(PreferencesActivity.TWITTER_CUSTOM_API_URL, customApiUrl);
				
				editor.putBoolean(PreferencesActivity.IS_COMMENTING_POSSIBLE, ProviderManager.isCommentingPossible());

			    editor.commit();
				
				Intent result = new Intent(getIntent().getAction());
				setResult(RESULT_OK, result);
				finish();
			} else {
				Toast.makeText(AccountActivity.this, "Login Incorrect.", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	};

	private final OnClickListener doneListener = new OnClickListener() {
		public void onClick(View view) {
			Spinner providerSpinner = (Spinner) findViewById(R.id.providerSpinner);
			provider = providerSpinner.getSelectedItem().toString();
			
			EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
			username = usernameEditText.getText().toString();
			
			EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
			password = passwordEditText.getText().toString();
			
			EditText customApiEditText = (EditText) findViewById(R.id.customApiEditText);
			customApiUrl = customApiEditText.getText().toString();
			
			ProviderManager.Initialize(provider, username, password, customApiUrl);
			
			progressDialog = ProgressDialog.show(AccountActivity.this, "User Account", "Checking Credentials...", true);
			new Thread(null, checkCredentials, "CredentialsVerifier").start();
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
}
