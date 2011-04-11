package edu.stanford.junction.sample.partyware.gallery;
import android.util.Log;

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
import android.net.Uri;
import mobisocial.nfc.Nfc;

import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.activity.JunctionExtra;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

public class MainActivity extends TabActivity{

	private TextView mJunctionStatus;
	private ImageView mJunctionStatusLight;
	private BroadcastReceiver mJunctionStatusReceiver;
	private IPropChangeListener mSyncListener;
    private Nfc mNfc;

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

		// spec = tabHost.newTabSpec("party").setIndicator(
		// 	"Party",
		// 	res.getDrawable(R.drawable.party_icon)).setContent(intent);
		// tabHost.addTab(spec);

		// intent = new Intent().setClass(this, PeopleActivity.class);
		// spec = tabHost.newTabSpec("people").setIndicator(
		// 	"People",
		// 	res.getDrawable(R.drawable.people_icon)).setContent(intent);
		// tabHost.addTab(spec);

		// intent = new Intent().setClass(this, YoutubePlaylistActivity.class);
		// spec = tabHost.newTabSpec("playlist").setIndicator(
		// 	"Playlist",
		// 	res.getDrawable(R.drawable.playlist_icon)).setContent(intent);
		// tabHost.addTab(spec);

		intent = new Intent().setClass(this, PicturesActivity.class);
		spec = tabHost.newTabSpec("pictures").setIndicator(
			"Pictures",
			res.getDrawable(R.drawable.pictures_icon)).setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);


		XMPPSwitchboardConfig xmppConfig = new XMPPSwitchboardConfig("prpl.stanford.edu");
		Uri jxUri = Uri.parse(AndroidJunctionMaker.getInstance(xmppConfig).generateSessionUri().toString());

		if (getIntent() != null && getIntent().hasExtra("android.intent.extra.APPLICATION_ARGUMENT")) {
            String appArgument = getIntent().getStringExtra("android.intent.extra.APPLICATION_ARGUMENT");
            Log.i("JXWhiteboard", "Got app argument: " + appArgument);
            jxUri = Uri.parse(appArgument);
		}
		else if(getIntent().getScheme() != null && 
                getIntent().getScheme().equals(JunctionApp.SHARE_PARTY_SCHEME) &&
                getIntent().getData() != null){
            Uri.Builder builder = getIntent().getData().buildUpon();
            jxUri = builder.scheme(JunctionApp.JUNCTION_SCHEME).build();
            app.connectToSession(jxUri);
        }

        if(jxUri != null){
            app.connectToSession(jxUri);
        }

        mNfc = new Nfc(this);

        mNfc.addNdefHandler(new Nfc.NdefHandler(){
                public int handleNdef(final NdefMessage[] messages){
                    MainActivity.this.runOnUiThread(new Runnable(){
                            public void run(){
                                doHandleNdef(messages);
                            }
                        });
                    return NDEF_CONSUME;
                }
            });

        shareActivity(jxUri);
    }

    protected void doHandleNdef(NdefMessage[] messages){
        if(messages.length != 1 || messages[0].getRecords().length != 1){
            Toast.makeText(this, "Oops! expected a single Uri record. ",
                           Toast.LENGTH_SHORT).show();
            return;
        }
        String uriStr = new String(messages[0].getRecords()[0].getPayload());
        Uri myUri = Uri.parse(uriStr);
        if(myUri == null || !myUri.getScheme().equals(JunctionApp.SHARE_PARTY_SCHEME)){
            Toast.makeText(this, "Received record without valid Uri!", Toast.LENGTH_SHORT).show();
            return;
        }
        JunctionApp app = (JunctionApp)getApplication();
        app.connectToSession(myUri);
        shareActivity(myUri);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfc.onPause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNfc.onResume(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mNfc.onNewIntent(this, intent)) {
            return;
        }
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

    protected void shareActivity(Uri uriIn){
        Uri.Builder builder = uriIn.buildUpon();
        Uri uri = builder.scheme(JunctionApp.SHARE_PARTY_SCHEME).build();
        if(uriIn == null){
            JunctionApp app = (JunctionApp)getApplication();
            uri = app.currentNfcSharingUri();
        }
        if(uri != null){
            NdefRecord urlRecord = new NdefRecord(
                NdefRecord.TNF_ABSOLUTE_URI, 
                NdefRecord.RTD_URI, new byte[] {}, 
                uri.toString().getBytes());
            NdefMessage ndef = new NdefMessage(new NdefRecord[] { urlRecord });
            mNfc.share(ndef);
            Toast.makeText(this, "Share this activity with Nfc!", Toast.LENGTH_SHORT).show();
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



