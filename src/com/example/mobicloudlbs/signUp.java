package com.example.mobicloudlbs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class signUp extends ActionBarActivity{
	 private EditText user;
	 private EditText pwd;
	 private EditText repwd;
	 EditText emailId;
	 EditText fullname;
	 TextView login;
	 
	 private static final String LOGIN_URL = "http://10.0.2.2:1234/MobiCloudLBS/register.php";
	 
	@SuppressLint("NewApi")
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.signup);
	        
	    	user = (EditText) findViewById(R.id.reg_username);
	    	pwd = (EditText) findViewById(R.id.reg_password);
	    	repwd = (EditText) findViewById(R.id.reg_repassword);
	    	login = (TextView) findViewById(R.id.login_link);
	    	emailId = (EditText) findViewById(R.id.reg_email);
	    	fullname = (EditText) findViewById(R.id.reg_name);
	    	
	    	
	    	repwd.setCustomSelectionActionModeCallback(new Callback() {

	            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	                return false;
	            }

	            public void onDestroyActionMode(ActionMode mode) {                  
	            }

	            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	                return false;
	            }

	            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	                return false;
	            }
	        });

	    	login.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent i = new Intent(signUp.this, MainActivity.class);
					finish();
					startActivity(i);
					
				}
			});
	    	
	    	
	    	findViewById(R.id.reg_button).setOnClickListener(new OnClickListener(){
	    		String username;
	    		String password;
	    		String repassword;
				@Override
				public void onClick(View arg0) {
			    	
			    	
					// TODO Auto-generated method stub
					username = user.getText().toString();
					
					if(isValidUsername(username) == false)
					{
						user.setError("The email entered is invalid");
					}
					
					password = pwd.getText().toString();
					
					if(isValidPassword(password) == false)
					{
						pwd.setError("The password entered is invalid");
					}
					repassword = repwd.getText().toString();
					if(password.equals("repassword"))
					{
						repwd.setError("Password mismatch");
					}
					else{
						new CreateUser().execute();
					}
				}
	    });
	

}
	private boolean isValidUsername(String username) {
		// TODO Auto-generated method stub
		String RegExpression = "^([A-Za-z0-9])+([A-Za-z0-9._])*@+([A-Za-z0-9]*).([A-Za-z]{2,})$";
		
		Pattern p = Pattern.compile(RegExpression);
		Matcher m = p.matcher(username);
		if(m.matches() == false)
			return false;
		else
			return true;
	}

	private boolean isValidPassword(String password) {
		// TODO Auto-generated method stub
		if(password.length()<=6 || password == null)
			return false;
		else
			return true;
	}
	class CreateUser extends AsyncTask<String, String, String> {

		JSONParser jsonParser = new JSONParser();
		private static final String TAG_SUCCESS = "success";
		private static final String TAG_MESSAGE = "message";
		/**
        * Before starting background thread Show Progress Dialog
        * */
		boolean failure = false;

       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           Toast message = Toast.makeText(getApplicationContext(), "Creating user...", Toast.LENGTH_LONG);
			message.show();
       }

		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			 // Check for success tag
           int success;
           String username = user.getText().toString();
           String name = fullname.getText().toString();
           String email = emailId.getText().toString();
           String password = pwd.getText().toString();
           
           
           try {
               // Building Parameters
               List<NameValuePair> params = new ArrayList<NameValuePair>();
               params.add(new BasicNameValuePair("username", username));
               params.add(new BasicNameValuePair("fullname", name));
               params.add(new BasicNameValuePair("email", email));
               params.add(new BasicNameValuePair("password", password));

               //Log.d("request!", "starting");

               //Posting user data to script
               JSONObject json = jsonParser.makeHttpRequest(
                      LOGIN_URL, "POST", params);

               // full json response
               //Log.d("Login attempt", json.toString());

               // json success element
               success = json.getInt(TAG_SUCCESS);
               if (success == 1) {
               	Log.d("User Created!", json.toString());
               	Intent i = new Intent(signUp.this, homeScreen.class);
               	finish();
               	startActivity(i);
               	return json.getString(TAG_MESSAGE);
               }else{
               	Log.d("Login Failure!", json.getString(TAG_MESSAGE));
               	return json.getString(TAG_MESSAGE);

               }
           } catch (JSONException e) {
               e.printStackTrace();
           }

           return null;

		}
		/**
        * After completing background task Dismiss the progress dialog
        * **/
       protected void onPostExecute(String file_url) {
           // dismiss the dialog once product deleted

       }

	}

	
}

