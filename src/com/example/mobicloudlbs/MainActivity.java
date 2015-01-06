package com.example.mobicloudlbs;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	Button loginButton;
	EditText username;
	EditText password;
	TextView err;

	// address to the web service
	private static final String LOGIN_URL = "http://10.0.2.2:1234/MobiCloudLBS/login.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		username = (EditText) findViewById(R.id.txt_Username);
		password = (EditText) findViewById(R.id.txt_Password);
		loginButton = (Button) findViewById(R.id.loginButton);

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (username.getText().toString().trim().length() == 0
						|| password.getText().toString().trim().length() == 0) {
					// if the user name or password fields are left empty.
					Toast message = Toast.makeText(getApplicationContext(),
							"Oops! Can't leave the fields empty.",
							Toast.LENGTH_LONG);
					message.show();
				} else {
					// attempt login, verify the user and log in to the system
					new AttemptLogin().execute();
				}
			}
		});

		TextView signUp = (TextView) findViewById(R.id.signUp);

		signUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// open a new intent to register the user
				Intent i = new Intent(MainActivity.this, signUp.class);
				startActivity(i);
			}
		});
	}

	class AttemptLogin extends AsyncTask<String, String, String> {

		JSONParser jsonParser = new JSONParser();

		private static final String TAG_SUCCESS = "success";
		private static final String TAG_MESSAGE = "message";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast message = Toast.makeText(getApplicationContext(),
					"Trying to login...", Toast.LENGTH_LONG);
			message.show();
		}

		@Override
		protected String doInBackground(String... args) {
			int success;
			String userName = username.getText().toString();
			String passWord = password.getText().toString();
			try {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", userName));
				params.add(new BasicNameValuePair("password", passWord));
				JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
						params);
				success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					Log.d("Login Successful!", json.toString());
					//go to home screeen if the login is successful
					Intent i = new Intent(MainActivity.this, homeScreen.class);
					i.putExtra("UserName", username.getText().toString());
					finish();
					startActivity(i);
					return json.getString(TAG_MESSAGE);
				} else {
					Log.d("Login Failure!", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*
			 * finally{ Intent i = new Intent(MainActivity.this,
			 * homeScreen.class); startActivity(i); }
			 */
			return null;
		}

		protected void onPostExecute(String file_url) {

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
