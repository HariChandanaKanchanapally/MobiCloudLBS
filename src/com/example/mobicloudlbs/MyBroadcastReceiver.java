package com.example.mobicloudlbs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
	
	WifiP2pManager mManager;
	Channel channel;
	Activity intent;
	public MyBroadcastReceiver(WifiP2pManager mManager2, Channel mChannel,
			Activity intent) {
		// TODO Auto-generated constructor stub
		this.mManager = mManager2;
		this.channel = mChannel;
		this.intent = intent;
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		
		if(mManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
			int state = intent.getIntExtra(mManager.EXTRA_WIFI_STATE,-1);
			if(state == mManager.WIFI_P2P_STATE_ENABLED){
				Toast message = Toast.makeText(context, "WIFI ENABLED", Toast.LENGTH_LONG);
				message.show();
			}
			else{
				Toast message = Toast.makeText(context, "WIFI DISABLED", Toast.LENGTH_LONG);
				message.show();
			}
		}
		
	}

}
