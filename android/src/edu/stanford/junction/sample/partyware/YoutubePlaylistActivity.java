package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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


public class YoutubePlaylistActivity extends RichListActivity implements OnItemClickListener{

    private Handler mMainHandler;
    private VidAdapter mVids;

	public final static int REQUEST_CODE_ADD_VIDEO = 0;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.youtube);

		mVids = new VidAdapter(this);
		setListAdapter(mVids);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 

		Button button = (Button)findViewById(R.id.add_video_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					addVideo();
				}
			});

		final Handler refreshHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					refresh();
				}
			};

		try{
			Prop prop = JunctionService.getProp();
			prop.addChangeListener(new IPropChangeListener(){
					public String getType(){ return Prop.EVT_CHANGE; }
					public void onChange(Object data){
						refreshHandler.sendEmptyMessage(0);
					}
				});

			prop.addChangeListener(new IPropChangeListener(){
					public String getType(){ return Prop.EVT_SYNC; }
					public void onChange(Object data){
						refreshHandler.sendEmptyMessage(0);
					}
				});
		}
		catch(IllegalStateException e){
			toastShort("Oops! Not connected to any party.");
			e.printStackTrace(System.err);
		}

		refresh();
	}

	protected void addVideo(){
		Intent intent = new Intent(YoutubeSearchActivity.LAUNCH_INTENT);
		startActivityForResult(intent, REQUEST_CODE_ADD_VIDEO);
	}


	public void onItemClick(AdapterView parent, View v, int position, long id){
		JSONObject o = mVids.getItem(position);
		String videoId = o.optString("videoId");
		Intent i =  new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:"  + videoId));
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
			i, PackageManager.MATCH_DEFAULT_ONLY);
		if(list.size() > 0) {
			startActivity(i);
		} 
		else{
			toastShort("Youtube player not available!");
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case REQUEST_CODE_ADD_VIDEO:
			if(resultCode == RESULT_OK){
				String caption = intent.getStringExtra("title");
				String videoId = intent.getStringExtra("video_id");
				String thumbUrl = intent.getStringExtra("thumb_url");
				try{
					PartyProp prop = JunctionService.getProp();
					String userId = JunctionService.getUserId();
					long time = (long)(System.currentTimeMillis()/1000.0);
					prop.addYoutube(userId, videoId, thumbUrl, caption, time);
				}
				catch(IllegalStateException e){
					toastShort("Oops! Not connected to any party.");
					e.printStackTrace(System.err);
				}
				catch(Exception e){
					toastShort("Oops! Failed to add video to party.");
					e.printStackTrace(System.err);
				}

			}
			break;
		}
	}

	private void refresh(){
		try{
			PartyProp prop = JunctionService.getProp();
			List<JSONObject> videos = prop.getYoutubeVids();
			refreshVideos(videos);
		}
		catch(IllegalStateException e){
			toastShort("Failed to get info from service! See debug log.");
			e.printStackTrace(System.err);
		}
	}

	private void refreshVideos(List<JSONObject> videos){
		mVids.clear();
		for(JSONObject a : videos){
			mVids.add(a);
		}
	}


	public void onDestroy(){
		super.onDestroy();
	}



	class VidAdapter extends MediaListAdapter<JSONObject> {

		public VidAdapter(Context context){
			super(context, R.layout.youtube_item);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)(getContext().getSystemService(
														 Context.LAYOUT_INFLATER_SERVICE));
				v = vi.inflate(R.layout.youtube_item, null);
			}
			JSONObject o = getItem(position);
			if (o != null) {

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				Date d = new Date(o.optLong("time") * 1000);
				String time = dateFormat.format(d); 

				if(position == 0){
					// Now playing this video!
					tt.setText("Now Playing: ");
					tt.setTextColor(0xffffdd00);
					v.setBackgroundColor(0xff222222);
					bt.setText(o.optString("caption"));
				}
				else{
					tt.setText(o.optString("caption"));
					tt.setTextColor(0xffffffff);
					v.setBackgroundColor(0xff000000);
					bt.setText(" " + time);					
				}

				final ImageView icon = (ImageView)v.findViewById(R.id.icon);
				final String url = o.optString("thumbUrl");
				loadImage(icon, url);
			}
			return v;
		}
	}

}



