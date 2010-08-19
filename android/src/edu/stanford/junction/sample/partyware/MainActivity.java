package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.activity.JunctionExtra;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
import edu.stanford.junction.provider.xmpp.ConnectionTimeoutException;
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.sample.ListState;
import edu.stanford.junction.props2.IPropChangeListener;

import android.app.Service;
import android.app.Activity;
import android.app.TabActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import org.json.*;

import java.net.*;
import java.io.*;
import java.util.*;


public class MainActivity extends TabActivity{

	private static final int SCAN_URL = 0;
	private static final int ADD_PIC = 1;
	private static final int EXIT = 2;

	private TextView mJunctionStatus;
	private ImageView mJunctionStatusLight;
	private BroadcastReceiver mJunctionStatusReceiver;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mJunctionStatus = (TextView)findViewById(R.id.junction_status_text);
		mJunctionStatusLight = (ImageView)findViewById(R.id.junction_status_icon);

		mJunctionStatusReceiver = new BroadcastReceiver(){
				public void onReceive(Context context, Intent intent) {
					String statusText = intent.getStringExtra("status_text");
					int status = intent.getIntExtra("status", 0);
					updateJunctionStatus(status, statusText);
				}
			};
		IntentFilter intentFilter = new IntentFilter(JunctionService.BROADCAST_STATUS);
		registerReceiver(mJunctionStatusReceiver, intentFilter);


		// Create top-level tabs
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost();  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PartyActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("party").setIndicator(
			"Party",
			res.getDrawable(R.drawable.party_icon)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, YoutubePlaylistActivity.class);
		spec = tabHost.newTabSpec("playlist").setIndicator(
			"Playlist",
			res.getDrawable(R.drawable.playlist_icon)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, PicturesActivity.class);
		spec = tabHost.newTabSpec("pictures").setIndicator(
			"Pictures",
			res.getDrawable(R.drawable.pictures_icon)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);


		// Start the junction service
		Intent i = new Intent(this, JunctionService.class);
		i.putExtra("userId", (UUID.randomUUID()).toString());
		startService(i);
	}

	private void updateJunctionStatus(int status, String statusText){
		if(status == 0){
			mJunctionStatusLight.setImageResource(R.drawable.led_red);
		}
		else if(status == 1){
			mJunctionStatusLight.setImageResource(R.drawable.led_yellow);
		}
		else if(status == 2){
			mJunctionStatusLight.setImageResource(R.drawable.led_green);
		}
		mJunctionStatus.setText(statusText);
	}

	public void onDestroy(){
		super.onDestroy();
		// Start the junction service
		Intent i = new Intent(this, JunctionService.class);
		stopService(i);

		try{
			unregisterReceiver(mJunctionStatusReceiver);
		} catch(IllegalArgumentException e){}
	}




}



