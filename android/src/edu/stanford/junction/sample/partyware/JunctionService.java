package edu.stanford.junction.sample.partyware;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.activity.JunctionExtra;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

import org.json.JSONObject;


public class JunctionService extends Service {
    private JunctionActor jxActor;
    private Junction jx;
    private PartyProp partyProp;
    private Thread connectionThread;
    private String userId;
    private Handler mHandler = new Handler();

    public static final String BROADCAST_STATUS = "edu.stanford.junction.sample.partyware.JunctionStatus";

    private static JunctionService instance;

	public static JunctionService getService() {
		if (instance == null) {
			throw new IllegalStateException("Could not get party prop instance!");
		}
		return instance;
	}
    
	public static PartyProp getProp() {
		if (instance == null || instance.partyProp == null) {
			throw new IllegalStateException("Could not get party prop instance!");
		}
		return instance.partyProp;
	}

	public static void connectToSession(Uri uri) {
		if (instance == null) {
			throw new IllegalStateException("Could not get service instance!");
		}
		instance.initJunction(uri);
	}

	public static String getUserId(){
		return (getService()).userId;
	}

	// Called once on initial creation
	@Override
	public void onCreate() {
		super.onCreate();
		partyProp = new PartyProp("party_prop");
		instance = this;
    }

	private void notifyStatus(int status, String msg){
		final Intent i = new Intent(BROADCAST_STATUS);
		i.putExtra("status", status);
		i.putExtra("status_text", msg);
		sendBroadcast(i);
	}

	@Override
	public IBinder onBind(Intent intent) {return null;}

	// Called for each call to Context.startService
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			Bundle extras = intent.getExtras();
			if(extras !=null){
				userId = extras.getString("userId");
			}
		}
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(jxActor != null){
			try{
				jxActor.leave();
			}catch(IllegalStateException e){
				// We were'nt connected
			}
		}
		if(connectionThread != null){
			connectionThread.interrupt();
		}
	}

	protected void initJunction(final Uri uri){

		notifyStatus(1, "Connecting...");

		if(connectionThread != null){
			connectionThread.interrupt();
		}

		Thread t = new Thread(){

				public void run(){

					if(jxActor != null){
						try{
							jxActor.leave();
						}catch(IllegalStateException e){
							// We were'nt connected
						}
					}

					JunctionActor actor = new JunctionActor("participant") {
							public void onActivityJoin() {
								Log.i("JunctionService", "Joined activity!");
								notifyStatus(2, "Joined Party");
							}
							public void onActivityCreate(){
								Log.i("JunctionService", "You created the activity.");
								notifyStatus(2, "Created Party");
							}
							public void onMessageReceived(MessageHeader header, JSONObject msg){
								mHandler.post(new Runnable(){
										public void run(){
											Log.i("JunctionService", "Got msg.");
										}
									});
							}
							public List<JunctionExtra> getInitialExtras(){
								ArrayList<JunctionExtra> l = new ArrayList<JunctionExtra>();
								l.add(partyProp);
								return l;
							}
						};

					final XMPPSwitchboardConfig sb = new XMPPSwitchboardConfig("openjunction.org");
					sb.setConnectionTimeout(20000); // 20 secs

					URI url = null;
					try{
						url = new URI(uri.toString());
					}
					catch(URISyntaxException e){
						Log.e("JunctionService", "Failed to parse uri: " + uri.toString());
						return;
					}

					try{
						Junction jx = AndroidJunctionMaker.getInstance(sb).newJunction(url, actor);
						if(!isInterrupted()){
							JunctionService.this.jx = jx;
							JunctionService.this.jxActor = actor;
							notifyStatus(2, "Connected");
						}
					}
					catch(JunctionException e){
						Log.e("JunctionService","Failed to connect to junction activity!");
						notifyStatus(0, "Failed to connect");
						e.printStackTrace(System.err);
					}
					catch(Exception e){
						Log.e("JunctionService","Failed to connect to junction activity!");
						notifyStatus(0, "Failed to connect");
						e.printStackTrace(System.err);
					}
				}
			};
		
		t.start();
		connectionThread = t;
	}
}