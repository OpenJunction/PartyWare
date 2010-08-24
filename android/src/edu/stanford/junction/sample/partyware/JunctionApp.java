package edu.stanford.junction.sample.partyware;

import java.net.*;
import java.util.*;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.activity.JunctionExtra;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

import org.json.JSONObject;

public class JunctionApp extends Application {

    private JunctionActor jxActor;
    private Junction jx;
    private PartyProp partyProp;
    private Thread connectionThread;
    private String mUserId;
    private Handler mHandler = new Handler();
	private int mConnectionStatus = 0;
	private String mConnectionStatusText = "Disconnected";
	private HashSet<String> voteHistory = new HashSet<String>();
    public static final String BROADCAST_STATUS = "edu.stanford.junction.sample.partyware.JunctionStatus";

	public PartyProp getProp() {
		return partyProp;
	}

	public void connectToSession(Uri uri) {
		initJunction(uri);
	}

	public String getUserId(){
		return mUserId;
	}

	public void upvoteVideo(String id){
		if(!(voteHistory.contains(id))){
			partyProp.upvoteVideo(id);
			voteHistory.add(id);
		}
	}

	public void downvoteVideo(String id){
		if(!(voteHistory.contains(id))){
			partyProp.downvoteVideo(id);
			voteHistory.add(id);
		}
	}

	public boolean alreadyVotedFor(String id){
		return voteHistory.contains(id);
	}

	public String getConnectionStatusText(){
		return mConnectionStatusText;
	}

	public int getConnectionStatus(){
		return mConnectionStatus;
	}

	// Called once on initial creation
	@Override
	public void onCreate() {
		super.onCreate();
		partyProp = new PartyProp("party_prop");
		mUserId = (UUID.randomUUID()).toString();

		// Maybe auto-connect
		SharedPreferences mPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
		String url = mPrefs.getString("last_party_url", null);
		if(url != null){
			updateStatus(1, "Reconnecting to previous part...");
			connectToSession(Uri.parse(url));				
		}
	}

	private void updateStatus(int status, String msg){
		final Intent i = new Intent(BROADCAST_STATUS);
		mConnectionStatus = status;
		mConnectionStatusText = msg;
		i.putExtra("status", status);
		i.putExtra("status_text", msg);
		sendBroadcast(i);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
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

		updateStatus(1, "Connecting...");

		if(connectionThread != null){
			connectionThread.interrupt();
		}

		PartyProp oldProp = partyProp;
		partyProp = (PartyProp)oldProp.newKeepingListeners();
		oldProp.removeAllChangeListeners();
		partyProp.forceChangeEvent();

		Thread t = new Thread(){
				public void run(){
					if(jxActor != null){
						try{
							jxActor.leave();
						}
						catch(IllegalStateException e){
							// We were'nt connected
						}
					}
					JunctionActor actor = new JunctionActor("participant") {
							public void onActivityJoin() {
								Log.i("JunctionApp", "Joined activity!");
								updateStatus(2, "Joined Party");
							}
							public void onActivityCreate(){
								Log.i("JunctionApp", "You created the activity.");
								updateStatus(2, "Created Party");
							}
							public void onMessageReceived(MessageHeader header, JSONObject msg){
								mHandler.post(new Runnable(){
										public void run(){
											Log.i("JunctionApp", "Got msg.");
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
						Log.e("JunctionApp", "Failed to parse uri: " + uri.toString());
						return;
					}

					try{
						Junction jx = AndroidJunctionMaker.getInstance(sb).newJunction(url, actor);
						if(!isInterrupted()){
							JunctionApp.this.jx = jx;
							JunctionApp.this.jxActor = actor;
							updateStatus(2, "Connected");
							SharedPreferences mPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
							SharedPreferences.Editor ed = mPrefs.edit();
							ed.putString("last_party_url", uri.toString());
							ed.commit();
						}
					}
					catch(JunctionException e){
						Log.e("JunctionApp","Failed to connect to junction activity!");
						updateStatus(0, "Failed to connect");
						e.printStackTrace(System.err);
					}
					catch(Exception e){
						Log.e("JunctionApp","Failed to connect to junction activity!");
						updateStatus(0, "Failed to connect");
						e.printStackTrace(System.err);
					}
				}
			};
		
		t.start();
		connectionThread = t;
	}
}