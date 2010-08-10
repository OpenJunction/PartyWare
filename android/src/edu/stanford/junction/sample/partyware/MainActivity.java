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
import android.net.Uri;

import org.json.*;

import java.net.*;
import java.io.*;
import java.util.*;


public class MainActivity extends Activity{

	private static final int SCAN_URL = 0;
	private static final int ADD_PIC = 1;
	private static final int EXIT = 2;

	public final static int REQUEST_CODE_SCAN_URL = 0;
	public final static int REQUEST_CODE_ADD_PIC = 1;

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
		menu.add(0,ADD_PIC,0, "Add Picture");
		menu.add(0,EXIT,0,"Exit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		switch (item.getItemId()){
		case SCAN_URL:
			scanURL();
			return true;
		case ADD_PIC:
			addPic();
			return true;
		case EXIT:
			Process.killProcess(Process.myPid());
		}
		return false;
	}


	protected void scanURL(){
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, REQUEST_CODE_SCAN_URL);		
	}

	protected void addPic(){
		Intent intent = new Intent(AddPictureActivity.LAUNCH_INTENT);
		startActivityForResult(intent, REQUEST_CODE_ADD_PIC);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case REQUEST_CODE_SCAN_URL:
			if(resultCode == RESULT_OK){
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				showDialog("Format: " + format + "\nContents: " + contents);
			}
			break;
		case REQUEST_CODE_ADD_PIC:
			if(resultCode == RESULT_OK){
				Uri uri = intent.getData();
				String comment = intent.getStringExtra(AddPictureActivity.EXTRA_COMMENT);
				showDialog("URL: " + uri + "\nComment: " + comment);				
			}
			break;
		}
	}

	private void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

}



