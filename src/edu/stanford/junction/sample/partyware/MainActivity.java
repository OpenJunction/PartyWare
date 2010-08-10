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

import android.content.ServiceConnection;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Process;
import android.content.Intent;
import android.app.Service;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.*;

import java.net.*;
import java.io.*;
import java.util.*;


public class MainActivity extends Activity{

	private static final int SCAN_URL = 0;
	private static final int EXIT = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}

	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu){
		menu.clear();
		menu.add(0,SCAN_URL,0, "Join Session");
		menu.add(0,EXIT,0,"Exit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch (item.getItemId()){
		case SCAN_URL:
			scanURL();
			return true;
		case EXIT:
			Process.killProcess(Process.myPid());
		}
		return false;
	}


	protected void scanURL(){
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				showDialog("Format: " + format + "\nContents: " + contents);
			} else if (resultCode == RESULT_CANCELED) {
				showDialog("failed!");
			}
		}
	}

	private void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}




}



