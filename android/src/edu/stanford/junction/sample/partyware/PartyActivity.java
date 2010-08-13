package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.ComponentName;
import android.net.Uri;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import edu.stanford.junction.extra.JSONObjWrapper;

import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.text.DateFormat;


public class PartyActivity extends RichActivity{

	public final static int REQUEST_CODE_SCAN_URL = 0;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.party);

		Button button = (Button)findViewById(R.id.join_party_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					scanURL();
				}
			});
	}

	protected void scanURL(){
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, REQUEST_CODE_SCAN_URL);		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case REQUEST_CODE_SCAN_URL:
			if(resultCode == RESULT_OK){
				String url = intent.getStringExtra("SCAN_RESULT");
				Toast.makeText(this, 
							   "Connecting to party at: " + url, 
							   Toast.LENGTH_SHORT).show();
				JunctionService.connectToSession(Uri.parse(url));
			}
			break;
		}
	}

	public void onDestroy(){
		super.onDestroy();
	}

}



