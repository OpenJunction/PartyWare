package edu.stanford.junction.sample.partyware;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.net.Uri;

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
    private Handler jxMessageHandler;
    private PartyProp partyProp;

    private static JunctionService instance;
    
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

	// Called once on initial creation
	@Override
	public void onCreate() {
		super.onCreate();
		partyProp = new PartyProp("party_prop");
		instance = this;


		// For debugging
		initJunction(Uri.parse("junction://openjunction.org/partyware"));
    }

	@Override
	public IBinder onBind(Intent intent) {return null;}

	// Called for each call to Context.startService
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		jxActor.leave();
	}

	protected void initJunction(final Uri uri){

		jxMessageHandler = new Handler() {
				public void handleMessage(Message msgFromChild) {
					JSONObject msg = (JSONObject) msgFromChild.obj;
					try {
						Log.i("JunctionService", "Got junction msg: " + msg);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			};

		jxActor = new JunctionActor("participant") {
				public void onActivityJoin() {
					Log.i("JunctionService", "Joined activity!");
				}
				public void onActivityCreate(){
					Log.i("JunctionService", "You created the activity.");
				}
				public void onMessageReceived(MessageHeader header, JSONObject msg) {
					Message toMain = jxMessageHandler.obtainMessage();
					toMain.obj = msg;
					jxMessageHandler.sendMessage(toMain);
				}
				public List<JunctionExtra> getInitialExtras(){
					ArrayList<JunctionExtra> l = new ArrayList<JunctionExtra>();
					l.add(partyProp);
					return l;
				}
			};

		final XMPPSwitchboardConfig sb = new XMPPSwitchboardConfig("openjunction.org");

		Thread t = new Thread(){
				public void run(){

					URI url = null;
					try{
						url = new URI(uri.toString());
					}
					catch(URISyntaxException e){
						Log.e("JunctionService", "Failed to parse uri: " + uri.toString());
						return;
					}


					try{
						jx = AndroidJunctionMaker.getInstance(sb).newJunction(url, jxActor);
					}
					catch(JunctionException e){
						Log.e("JunctionService","Failed to connect to junction activity!");
						e.printStackTrace(System.err);
					}
					catch(Exception e){
						Log.e("JunctionService","Failed to connect to junction activity!");
					}
				}
			};
		t.start();
			
	}
}