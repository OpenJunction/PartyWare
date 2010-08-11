package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import edu.stanford.junction.extra.JSONObjWrapper;

import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.text.DateFormat;


public class YoutubePlaylistActivity extends ListActivity implements OnItemClickListener{

    private Handler mMainHandler;
    private ArrayAdapter<JSONObject> mVids;

	private final DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mVids = new ArrayAdapter<JSONObject>(
			this, 
			android.R.layout.simple_list_item_1,
			new ArrayList<JSONObject>());
		setListAdapter(mVids);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 

		mVids.add(randomTestObj());
		mVids.add(randomTestObj());
		mVids.add(randomTestObj());
		mVids.add(randomTestObj());
		mVids.add(randomTestObj());
	}

	private JSONObject randomTestObj(){
		try{
			JSONObject o = new JSONObject();
			o.put("name", "dudeface");
			o.put("time", (new Date()).getTime());
			return o;
		}
		catch(Exception e){
			return new JSONObject();
		}
	}

	public void onItemClick(AdapterView parent, View v, int position, long id){
		// Intent intent = new Intent();
		// JSONObject advert = (JSONObject)mVids.getItem(position);
		// String url = advert.optString("url");
		// intent.putExtra(WhiteboardIntents.EXTRA_SESSION_URL, url);
		// setResult(RESULT_OK, intent);
		// finish();
	}

	private void refreshAdverts(Set<JSONObject> adverts){
		mVids.setNotifyOnChange(false);
		mVids.clear();
		for(JSONObject a : adverts){
			JSONObject advert = new JSONObjWrapper(a){
					public String toString(){ 
						String name = optString("name");
						Date d = new Date(optLong("time"));
						return  name + " - " + dateFormat.format(d); 
					}
				};
			mVids.add(advert);
		}
		mVids.setNotifyOnChange(true);
		mVids.notifyDataSetChanged();
	}


	public void onDestroy(){
		super.onDestroy();
	}

}



