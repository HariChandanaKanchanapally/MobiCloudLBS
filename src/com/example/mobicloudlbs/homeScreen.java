package com.example.mobicloudlbs;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.conn.util.InetAddressUtils;

import com.example.mobicloudlbs.R.id;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class homeScreen extends Activity implements LocationListener {

	Button srvSocBtn;
	TextView srvrmsg, clientmsg, srvrMsgInfo, clientMsgInfo, locationTextView,
			userName;
	// default IP address of the emulator server
	public static String SERVERIP = "10.0.2.15";
	EditText srvIPfield;
	Button clientSocBtn;
	private int SERVERPORT = 9090;
	private final IntentFilter inf = new IntentFilter();
	Channel mChannel;
	WifiP2pManager mManager;
	MyBroadcastReceiver receiver;
	Thread server;
	Thread client;
	Button browse, getLocation;
	ServerSocket serverSocket;
	String message, uname;
	public Location location;
	boolean locationInfoNeeded = false;
	LocationManager locationManager;
	String lat, lon, fileToUpload;
	android.view.View.OnClickListener clientConnect;
	private String Filename_to_Up, Filepath_to_Up;
	private List<String> itemlist = null;
	private List<String> pathlist = null;
	private String level = "/system/etc";
	ImageButton upload;
	String fileToDownload;
	String[] files = { "File1", "File2", "File3", "File4", "File5", "File6",
			"File7", "File8", "File9", "File10", "File11", "File12", "File13",
			"File14", "File15" };
	ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		srvrmsg = (TextView) findViewById(R.id.srvrmsg);
		srvSocBtn = (Button) findViewById(R.id.srvrSocBtn);
		srvIPfield = (EditText) findViewById(R.id.srvrIP);
		clientmsg = (TextView) findViewById(R.id.clientmsg);
		clientSocBtn = (Button) findViewById(R.id.clientSocBtn);
		srvrMsgInfo = (TextView) findViewById(R.id.srvrMsgInfo);
		clientMsgInfo = (TextView) findViewById(R.id.clientMsgInfo);
		lv = (ListView) findViewById(R.id.listView);
		locationTextView = (TextView) findViewById(R.id.location);
		upload = (ImageButton) findViewById(R.id.uploadButton);
		userName = (TextView) findViewById(R.id.userName);

		uname = getIntent().getStringExtra("UserName");
		userName.setText("Hi " + uname);

		location = getLocation();
		if (location != null) {
			String lat = Double.toString(location.getLatitude());
			String lon = Double.toString(location.getLongitude());
			locationTextView.setText("Location : " + lat + " " + lon);
		} else {
			locationTextView.setText("Cant get the location");
		}

		ArrayAdapter<String> Ad = new ArrayAdapter<String>(this,
				R.layout.list_row, R.id.file, files);
		lv.setAdapter(Ad);

		upload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// A new intent to upload the contents on to the server using
				// FTP
				Intent i = new Intent(homeScreen.this, browse.class);
				i.putExtra("String", "FileName");
				startActivityForResult(i, 1);
			}
		});

		srvSocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// get the IP address of the server
				try {
					SERVERIP = getIPAddress();
				} catch (SocketException e) {
					// catch the exception
					e.printStackTrace();
				}
				// locationTextView.setText("Server started, listen on IP:" +
				// SERVERIP + " " + locationTextView.getText().toString());

				// start the server
				SocketServerThread s = new SocketServerThread();
				server = new Thread(s);
				server.start();
				srvIPfield.setEnabled(false);
				clientSocBtn.setEnabled(false);
			}
		});

		clientSocBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// get the IP address of the server from the text view.
				String serverIP = srvIPfield.getText().toString();
				srvSocBtn.setEnabled(false);
				if (!serverIP.equals("")) {
					String msgToServer;
					String myIp = "";
					try {
						// get the IP address of the client
						myIp = getIPAddress();
					} catch (SocketException e) {
						// catch the exception
						e.printStackTrace();
					}
					// get the location of the client
					Location l = getLocation();
					// start the client
					SocketClientThread c = new SocketClientThread(uname,
							serverIP, SERVERPORT, l);
					c.execute();
					// display the client IP and the port of the server it is
					// connecting to
					clientmsg.setText("Client started, my IP: " + myIp
							+ " connecting to" + serverIP + " : " + SERVERPORT);
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// process the results of the file chosen in the upload menu
		super.onActivityResult(requestCode, resultCode, data);
		if (data.getExtras().containsKey("FileNameInfo")) {
			String fileToUpload = data.getStringExtra("FileNameInfo");
			locationTextView.setText(fileToUpload + " selected");
		}
	}

	// returns the location of the instance
	public Location getLocation() {
		LocationManager m = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = m.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean nw = m.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		// if GPS and Network Provider is not enabled
		if (!gps && !nw) {
			Toast x = Toast.makeText(getApplicationContext(),
					"No provider enabled", Toast.LENGTH_SHORT);
			x.show();
		} else {
			// if the network provider is enabled
			if (nw) {
				m.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
						0, this);
				if (m != null) {
					location = m
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
			}
			// if GPS is enabled
			if (gps) {
				if (location == null) {
					m.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
							0, this);
					if (m != null) {
						location = m
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}
				}
			}
		}
		return location;
	}

	// Layout of the files available on the server
	public void ClickHandler(View v) {
		LinearLayout ClickedRow = (LinearLayout) v.getParent();

		int c = Color.YELLOW;

		ClickedRow.setBackgroundColor(c);
		int count = ClickedRow.getChildCount();
		TextView fname = (TextView) ClickedRow.getChildAt(0).findViewById(
				R.id.file);
		fileToDownload = fname.getText().toString();
		locationTextView.setText(locationTextView.getText().toString() + ", "
				+ fileToDownload + " selected");
		try {
			// get the IP address of the server
			SERVERIP = getIPAddress();
		} catch (SocketException e) {
			// handle the exception
			e.printStackTrace();
		}
		locationTextView.setText("Server started, listen on IP:" + SERVERIP
				+ ", " + locationTextView.getText().toString());
		// get the location of the instance
		Location loc = getLocation();
		if (loc != null) {
			locationTextView.setText("selected : " + loc.getLatitude()
					+ loc.getLongitude());
		}
		// start the server
		SocketServerThread s = new SocketServerThread(loc, fileToDownload);
		server = new Thread(s);
		server.start();
		/*
		 * } else { // download the file from the server if the location is not
		 * available. System.out.println("Reached inside FTP download");
		 * MyTaskParams params = new MyTaskParams(fileToDownload); FileDownload
		 * myTask = new FileDownload(); System.out.println("Calling the class");
		 * myTask.execute(params); AlertDialog alertDialog = new
		 * AlertDialog.Builder( getApplicationContext()).create();
		 * alertDialog.setTitle("[" + fname + "]");
		 * alertDialog.setMessage("File downloaded successfullly!");
		 * alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) { //
		 * TODO Auto-generated method stub } }); alertDialog.show(); }
		 */

	}

	// code to get the IP address of the instance
	private String getIPAddress() throws SocketException {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "SiteLocalAddress: "
								+ inetAddress.getHostAddress() + "\n";
					}

				}

			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}

		return ip;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		receiver = new MyBroadcastReceiver(mManager, mChannel, this);
		registerReceiver(receiver, inf);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(receiver);
	}

	// Server side of the code.
	private class SocketServerThread extends Thread {
		// port number assigned for the application.
		static final int SocketServerPORT = 9090;
		int count = 0;
		String requiredFile;
		Location l;
		String messageToUI = "";

		public SocketServerThread(Location loc, String fileToUpload) {
			// TODO Auto-generated constructor stub
			this.l = loc;
			this.requiredFile = fileToUpload;
		}

		public SocketServerThread() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			Socket socket = null;
			DataInputStream din = null;
			DataOutputStream dout = null;
			String msgReply = "";
			boolean isLocAvailable = true;

			try {
				// start the Server Socket
				serverSocket = new ServerSocket(SocketServerPORT);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						srvrmsg.setText("I'm waiting here: " + SERVERIP + " : "
								+ serverSocket.getLocalPort());
					}
				});

				while (true) {
					// listening for the connection from the client
					socket = serverSocket.accept();
					System.out.println(". Connected to Client");
					// creating Streams for reading and writing data between
					// clients
					din = new DataInputStream(socket.getInputStream());
					dout = new DataOutputStream(socket.getOutputStream());
					String messageFromClient = "";
					String encodedMsg;

					encodedMsg = din.readUTF();
					messageFromClient = Decode(encodedMsg);
					System.out.println(encodedMsg);
					if (l == null
							|| messageFromClient
									.contentEquals("LOCATION NOT AVAILABLE")) {
						isLocAvailable = false;
						if (l == null)
							System.out.println("My location not available");
						else
							System.out.println("Remote location not available");

						System.out.println("Downloading file from FTP server");
						encodedMsg = din.readUTF();
						String file = Decode(encodedMsg);
					}
					if (isLocAvailable == false) {
						// download the file from the server if the location is
						// not available.
						System.out.println("Reached inside FTP download");
						MyTaskParams params = new MyTaskParams(fileToDownload);
						FileDownload myTask = new FileDownload();
						System.out.println("Calling the class");
						myTask.execute(params);
						AlertDialog alertDialog = new AlertDialog.Builder(
								getApplicationContext()).create();
						alertDialog.setTitle("[" + fileToDownload + "]");
						alertDialog
								.setMessage("File downloaded successfullly!");
						alertDialog.setButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								});
						alertDialog.show();
					} else {
						// tokenize and get location of remote
						System.out.println(messageFromClient);
						String[] tok = messageFromClient.split(" ");
						String username = tok[0];
						String latitude = tok[2];
						String longitude = tok[3];
						System.out.println("Lat : " + latitude);
						System.out.println("Lon : " + longitude);
						System.out.println("My location:");
						System.out.println("Lat : " + l.getLatitude());
						System.out.println("Lon : " + l.getLongitude());
						float[] results = new float[1];

						// calculate the distance between the mobile devices
						Location.distanceBetween(l.getLatitude(),
								l.getLongitude(), Double.parseDouble(latitude),
								Double.parseDouble(longitude), results);
						System.out.println("Distance between the phones : "
								+ results[0]);
						messageToUI = username + " at " + results[0] + " apart";
						msgReply = requiredFile;
						System.out.println(msgReply);
						dout.writeUTF(Encode(msgReply));

						// get the filename to download
						encodedMsg = din.readUTF();
						String fname = Decode(encodedMsg);
						if (fname.contentEquals("FILE NOT FOUND")) {
							System.out
									.println("File not found at the remote location");
							messageToUI = "File not found at " + messageToUI;
						} else {
							message = "Receive file : ";
							System.out.println(message + fname);
							File f = new File(fname);
							System.out.println("File Created. ");

							// create the file and the folder structure
							File dirs = new File(f.getParent());
							if (!dirs.exists())
								dirs.mkdirs();

							if (!f.exists()) {
								message += "Creating file";
								System.out.println("File Created. ");
								messageToUI = "Downloading file from "
										+ messageToUI;
								f.createNewFile();
							}
							System.out.println("1. " + messageToUI);
							// open the file output stream to write into the
							// file
							FileOutputStream fout = new FileOutputStream(f);
							while (true) {
								int ch;
								// read character by character and write into
								// the file
								while ((ch = din.read()) != -1) {
									fout.write((char) ch);
									System.out.print((char) ch);
								}
								System.out.println("2. " + messageToUI);
								encodedMsg = din.readUTF();
								String temp = Decode(encodedMsg);
								if (temp.contentEquals("FILE SEND COMPLETE")) {
									dout.writeUTF(Encode("FILE TRANSFERRED"));
									System.out
											.println(Encode("FILE TRANSFERRED"));
								}
								System.out.println("3. " + messageToUI);
								System.out.println("File downloaded");
								break;
							}
						}
					}
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// srvrMsgInfo.setText(message);
							Toast s = Toast.makeText(getApplicationContext(),
									messageToUI, Toast.LENGTH_SHORT);
							s.show();
						}
					});

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				final String errMsg = e.toString();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						srvrMsgInfo.setText(errMsg);
					}
				});

			} finally {
				if (socket != null) {
					try {
						socket.close();
						clientSocBtn.setEnabled(true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (din != null) {
					try {
						din.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (dout != null) {
					try {
						dout.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	// Client side of the code
	private class SocketClientThread extends AsyncTask<Void, Void, Void> {

		String serverAddress;
		int serverPort;
		String response = "";
		String encodedMsg;
		Location location;
		String user;

		public SocketClientThread(String uname, String serverIP,
				int sERVERPORT, Location location) {
			// TODO Auto-generated constructor stub
			this.serverAddress = serverIP;
			this.serverPort = sERVERPORT;
			this.user = uname;
			this.location = location;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			Socket socket = null;
			DataOutputStream dout = null;
			DataInputStream din = null;
			File f;

			try {
				// Create a socket
				socket = new Socket(serverAddress, serverPort);
				// opening the streams to communicate with the mobile devices
				dout = new DataOutputStream(socket.getOutputStream());
				din = new DataInputStream(socket.getInputStream());
				// check if the location is available and send the appropriate
				// message to the server
				if (location != null) {
					dout.writeUTF(Encode(user + " Location: "
							+ location.getLatitude() + " "
							+ location.getLongitude()));
				} else
					dout.writeUTF(Encode("LOCATION NOT AVAILABLE"));
				encodedMsg = din.readUTF();
				response = Decode(encodedMsg);
				// Check if the file is available on the client side
				f = new File(Environment.getExternalStorageDirectory()
						.toString() + "/" + response + ".txt");
				if (f.exists())
					dout.writeUTF(Encode(Environment
							.getExternalStorageDirectory().toString()
							+ "/"
							+ response + ".txt"));
				else
					dout.writeUTF(Encode("FILE NOT FOUND"));
				// opening the input stream to read the file and send it to the
				// server
				FileInputStream fin = new FileInputStream(f);
				int ch;
				// read character by character and write into the output stream
				while ((ch = fin.read()) != -1) {
					dout.write(ch);
					// content += ch;
				}
				dout.writeUTF(Encode("FILE SEND COMPLETE"));
				encodedMsg = din.readUTF();
				String temp = Decode(encodedMsg);
				if (temp.equalsIgnoreCase("FILE TRANSFERRED"))
					response = "File Sent Successfully";
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response = "UnknownHostException: " + e.toString();
				// clientMsgInfo.setText(clientMsgInfo.getText().toString() +
				// " " + response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response = "IOException: " + e.toString();
				// clientMsgInfo.setText(clientMsgInfo.getText().toString() +
				// " " + response);
			} finally {
				if (socket != null) {
					try {
						socket.close();
						srvSocBtn.setEnabled(true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (dout != null) {
				try {
					dout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (din != null) {
				try {
					din.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			clientMsgInfo.setText(response);
			super.onPostExecute(result);
		}
	}

	class MyTaskParams {
		String filename;

		MyTaskParams(String filetoDownload) {
			this.filename = filetoDownload;
		}
	}

	public class FileDownload extends AsyncTask<MyTaskParams, Void, Void> {

		protected Void doInBackground(MyTaskParams... params) {
			// File file = new File(pathlist.get(params[0].filename)));
			// Filename_to_Up=file.getName();
			String file = params[0].filename;
			System.out.println(file + " is the file");
			/*****************/
			FTPClient con = null;
			try {
				// String path;
				// String localFilepath="/mnt/";
				con = new FTPClient();
				con.setControlEncoding("UTF-8");
				con.connect("192.168.0.16", 83);
				OutputStream outputStream = null;
				File path = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				// path=Environment.getExternalStorageDirectory().getAbsolutePath();
				File name = new File(path, "/" + file);
				// if (con.login("FTPServerFolder", "cloud"))
				if (con.login("MobiCloud", "cloud")) {
					con.enterLocalPassiveMode(); // important!
					con.setFileType(FTP.BINARY_FILE_TYPE);
					// String data = file.getName();
					// con.changeWorkingDirectory("/myPHPprograms");
					String filepath = "/myPHPprograms/" + file;
					outputStream = new BufferedOutputStream(
							new FileOutputStream(name), 8 * 1024);
					boolean result = con.retrieveFile(filepath, outputStream);

					// FileInputStream in = new FileInputStream(file);
					// InputStream in =
					// getClass().getClassLoader().getResourceAsStream("aq/img/sample.png");
					// boolean result = con.storeFile(file.getName(), in);
					outputStream.close();
					if (result)
						Log.v("download result", "succeeded");
					con.logout();
					con.disconnect();
				}
			} catch (Exception e) {
				System.out.println("Running Exception");
				e.printStackTrace();

			}
			return null;
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	// encode the data
	public String Encode(String input) {
		String output = Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
		return output;
	}

	// decode the data
	public String Decode(String input) {
		String output = new String(Base64.decode(input, Base64.DEFAULT));
		return output;
	}
}
