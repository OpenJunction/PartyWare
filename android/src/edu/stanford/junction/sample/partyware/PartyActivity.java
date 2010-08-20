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
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;

import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.text.DateFormat;


public class PartyActivity extends RichActivity{

	public final static int REQUEST_CODE_SCAN_URL = 0;

	private TextView mPartyName;
	private IPropChangeListener mPropListener;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.party);

		mPartyName = (TextView)findViewById(R.id.party_name);

		Button button = (Button)findViewById(R.id.join_party_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					scanURL();
				}
			});

		button = (Button)findViewById(R.id.join_debug_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					JunctionApp app = (JunctionApp)getApplication();
					app.connectToSession(
						Uri.parse("junction://openjunction.org/partyware"));
				}
			});

		final Handler refreshHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					refresh();
				}
			};

		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		mPropListener = new IPropChangeListener(){
				public String getType(){ return Prop.EVT_ANY; }
				public void onChange(Object data){
					refreshHandler.sendEmptyMessage(0);
				}
			};
		prop.addChangeListener(mPropListener);

		refresh();
	}

	private void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		mPartyName.setText(prop.getName());
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
				toastShort("Connecting to party at: " + url);
				JunctionApp app = (JunctionApp)getApplication();
				app.connectToSession(Uri.parse(url));
			}
			break;
		}
	}

	public void onDestroy(){
		super.onDestroy();
		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		prop.removeChangeListener(mPropListener);
	}

}



