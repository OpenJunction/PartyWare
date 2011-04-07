package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TabHost;
import android.widget.Toast;
import android.nfc.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;

public class MainActivity extends TabActivity{

	private TextView mJunctionStatus;
	private ImageView mJunctionStatusLight;
	private BroadcastReceiver mJunctionStatusReceiver;
	private IPropChangeListener mSyncListener;
	private NfcAdapter mNfcAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Monitor state of the junction connection

		mJunctionStatus = (TextView)findViewById(R.id.junction_status_text);
		mJunctionStatusLight = (ImageView)findViewById(R.id.junction_status_icon);

		final JunctionApp app = (JunctionApp)getApplication();
		updateJunctionStatus(app.getConnectionStatus(), app.getConnectionStatusText());

		mJunctionStatusReceiver = new BroadcastReceiver(){
				public void onReceive(Context context, Intent intent) {
					String statusText = intent.getStringExtra("status_text");
					int status = intent.getIntExtra("status", 0);
					updateJunctionStatus(status, statusText);
				}
			};
		IntentFilter intentFilter = new IntentFilter(JunctionApp.BROADCAST_STATUS);
		registerReceiver(mJunctionStatusReceiver, intentFilter);


		final Handler refreshHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					app.updateUser();
				}
			};
		Prop prop = app.getProp();
		mSyncListener = new IPropChangeListener(){
				public String getType(){ return Prop.EVT_SYNC; }
				public void onChange(Object data){
					refreshHandler.sendEmptyMessage(0);
				}
			};
		prop.addChangeListener(mSyncListener);


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

		intent = new Intent().setClass(this, PeopleActivity.class);
		spec = tabHost.newTabSpec("people").setIndicator(
			"People",
			res.getDrawable(R.drawable.people_icon)).setContent(intent);
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

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


		// Check for uri intent
		Intent creationItent = getIntent();
		final String scheme=creationItent.getScheme();
		if(scheme != null && scheme.equals(JunctionApp.SHARE_PARTY_SCHEME)){
			final Uri uri=creationItent.getData();
			if(uri != null){
				Uri.Builder builder = uri.buildUpon();
				Uri jxUri = builder.scheme(JunctionApp.JUNCTION_SCHEME).build();
				app.connectToSession(jxUri);
			}
			else{
				Toast.makeText(this, "Received null url...", 
							   Toast.LENGTH_SHORT).show();
			}
		}
		else{
			Toast.makeText(this, "Failed to receive party invite :(", 
						   Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onPause() {
        super.onPause();
		mNfcAdapter.disableForegroundNdefPush(this);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return true;
	}

	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		menu.clear();
		menu.add(0, 0, 0, "Invite with NFC");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 0: {
			shareActivity();
			return true;
		}
		default: return false;
		}
	}

	protected void shareActivity(){
		JunctionApp app = (JunctionApp)getApplication();
		Uri uri = app.currentNfcSharingUri();
		if(uri != null){
			NdefRecord urlRecord = new NdefRecord(
				NdefRecord.TNF_ABSOLUTE_URI, 
				NdefRecord.RTD_URI, new byte[] {}, 
				uri.toString().getBytes());
			NdefMessage ndef = new NdefMessage(new NdefRecord[] { urlRecord });
			mNfcAdapter.enableForegroundNdefPush(this, ndef);
			Toast.makeText(this, "Sharing " + uri + ". Swipe with your friend!", 
						   Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(this, "Oops! no junction url.", Toast.LENGTH_SHORT).show();
		}
	}

	public void onDestroy(){
		super.onDestroy();
		try{
			unregisterReceiver(mJunctionStatusReceiver);
		} catch(IllegalArgumentException e){}

		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		prop.removeChangeListener(mSyncListener);
	}




}



