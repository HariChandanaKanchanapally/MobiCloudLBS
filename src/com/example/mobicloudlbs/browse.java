package com.example.mobicloudlbs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;



import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class browse extends ListActivity {
	private static final String UPLOAD_URL = "http://10.0.2.2/MobiCloudLBS/upload.php";
	private String Filename_to_Up,Filepath_to_Up;
	 private List<String> itemlist = new ArrayList<String>();
	 private List<String> pathlist = new ArrayList<String>();
	 private String level="/acct/";
	 private TextView pathview;
	 
	 @Override
	    public void onCreate(Bundle SavedState) {
	        super.onCreate(SavedState);
	        setContentView(R.layout.browse);
	        pathview = (TextView)findViewById(R.id.path);
	        getDirectory(level);

	    }

	private void getDirectory(String directoryPath) {
		// TODO Auto-generated method stub
		//String root_sd = Environment.getExternalStorageDirectory().toString();
		//System.out.println();
	
		/*pathview.setText("Directory: \n" + directoryPath);   
	    itemlist = new ArrayList<String>();
	    pathlist = new ArrayList<String>();
	    File fd = new File(directoryPath);
	    File[] list_of_files = fd.listFiles();*/
		
		 File fd = new File(directoryPath);
		 File[] list_of_files = fd.listFiles();
		 if(list_of_files == null)return;
	     /*if(!directoryPath.equals(level))
	     {
	      itemlist.add(level);
	      pathlist.add(level);
	      pathlist.add(fd.getParent());
	      pathview.setText("Path: " + pathlist);
	     }*/
	     for(int i=0; i < list_of_files.length; i++)

	     {

	    	  File f = list_of_files[i];
	       
	       if(f.isDirectory())
	       {
	    	   pathlist.add(f.getPath());
	    	   	itemlist.add(f.getName() + "/");
	                    getDirectory( f.getAbsolutePath() );
	                    
	       }
	                else {
	                    System.out.println( "File:" + f.getAbsoluteFile() );
	                    pathlist.add(f.getPath());
	                    itemlist.add("  "+f.getName());
	                }
	    	   
	      
	    	   

	     }
	     
	     ArrayAdapter<String> fileList =

	      new ArrayAdapter<String>(this, R.layout.browse,R.id.path, itemlist);
	     if(fileList!=null)
	     setListAdapter(fileList);
	    
	    }
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		File file = new File(pathlist.get(position));
		File f1 = null;
		Filename_to_Up=file.getName();
		//Filepath_to_Up=file.getPath();

		FTPClient con = null;
		 try
	        {
			 
	            con = new FTPClient();
	            con.connect("192.168.0.16",83);

	           // if (con.login("FTPServerFolder", "cloud"))
	            if (con.login("MobiCloud", "cloud"))
	            {
	                con.enterLocalPassiveMode(); // important!
	                con.setFileType(FTP.BINARY_FILE_TYPE);
	                String data = file.getName();
	                con.changeWorkingDirectory("/myPHPprograms");

	                FileInputStream in = new FileInputStream(file);
	                //InputStream in = getClass().getClassLoader().getResourceAsStream("aq/img/sample.png");
	                boolean result = con.storeFile(file.getName(), in);
	                in.close();
	                if (result) { 
	                	Filepath_to_Up = "/Applications/XAMPP/xamppfiles/htdocs/MobiCloudLBS"+file.getName();
	                	Log.v("upload result", "succeeded");
	                	}
	                con.logout();
	                con.disconnect();
	            }
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }

		if(!file.isDirectory())
		{
		new AlertDialog.Builder(this)

	    .setTitle("[" + file.getName() + "]")

	    .setPositiveButton("OK", 

	      new DialogInterface.OnClickListener() {

	       

	       @Override

	       public void onClick(DialogInterface dialog, int which) {

	        // TODO Auto-generated method stub

	       }

	      }).show();
		}
		/*if(!f1.isDirectory() && f1!= null)
		{
			new AlertDialog.Builder(this)

		    .setTitle("[" + f1.getName() + "]")

		    .setPositiveButton("OK", 

		      new DialogInterface.OnClickListener() {

		       

		       @Override

		       public void onClick(DialogInterface dialog, int which) {

		        // TODO Auto-generated method stub

		       }

		      }).show();	
		}
		}*/
		if(!file.isDirectory() )
		new UploadFile().execute();
		

	}
	
	

class UploadFile extends AsyncTask<String, String, String> {

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
       Toast message = Toast.makeText(getApplicationContext(), "Uploading File...", Toast.LENGTH_LONG);
		message.show();
   }

	@Override
	protected String doInBackground(String... args) {
		// TODO Auto-generated method stub
		 // Check for success tag
       int success;
       String filename = Filename_to_Up;
       String filepath = Filepath_to_Up;
       
       try {
           // Building Parameters
           List<NameValuePair> params = new ArrayList<NameValuePair>();
           params.add(new BasicNameValuePair("filename", filename));
           params.add(new BasicNameValuePair("filepath", filepath));
         
           
           //Log.d("request!", "starting");

           //Posting user data to script
           JSONObject json = jsonParser.makeHttpRequest(
                  UPLOAD_URL, "POST", params);

           // full json response
           //Log.d("Login attempt", json.toString());

           // json success element
           success = json.getInt(TAG_SUCCESS);
           if (success == 1) {
           	return json.getString(TAG_MESSAGE);
           	
           }else{
           	Log.d("File failed to upload!", json.getString(TAG_MESSAGE));
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
	   Toast message = Toast.makeText(getApplicationContext(), "File uploaded...", Toast.LENGTH_LONG);
 		message.show();

   }

}


}


