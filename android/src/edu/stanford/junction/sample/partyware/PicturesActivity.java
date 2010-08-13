package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
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
import android.widget.Button;
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


public class PicturesActivity extends RichListActivity implements OnItemClickListener{

    private Handler mMainHandler;
    private ArrayAdapter<JSONObject> mPics;

	public final static int REQUEST_CODE_ADD_PIC = 0;

	private final DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pictures);

		mPics = new ArrayAdapter<JSONObject>(
			this, 
			android.R.layout.simple_list_item_1,
			new ArrayList<JSONObject>());
		setListAdapter(mPics);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 

		Button button = (Button)findViewById(R.id.add_picture_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					addPic();
				}
			});

		try{
			Prop prop = JunctionService.getProp();
			prop.addChangeListener(new IPropChangeListener(){
					public String getType(){ return Prop.EVT_CHANGE; }
					public void onChange(Object data){
						refresh();
					}
				});

			prop.addChangeListener(new IPropChangeListener(){
					public String getType(){ return Prop.EVT_SYNC; }
					public void onChange(Object data){
						refresh();
					}
				});
		}
		catch(IllegalStateException e){
			toastShort("Failed to get info from service! See debug log.");
			e.printStackTrace(System.err);
		}

		refresh();
	}

	protected void addPic(){
		Intent intent = new Intent(AddPictureActivity.LAUNCH_INTENT);
		startActivityForResult(intent, REQUEST_CODE_ADD_PIC);
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
		// JSONObject advert = (JSONObject)mPics.getItem(position);
		// String url = advert.optString("url");
		// intent.putExtra(WhiteboardIntents.EXTRA_SESSION_URL, url);
		// setResult(RESULT_OK, intent);
		// finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case REQUEST_CODE_ADD_PIC:
			if(resultCode == RESULT_OK){
				String url = intent.getStringExtra(AddPictureActivity.EXTRA_URL);
				String thumbUrl = intent.getStringExtra(AddPictureActivity.EXTRA_THUMB_URL);
				String caption = intent.getStringExtra(AddPictureActivity.EXTRA_CAPTION);
				try{
					PartyProp prop = JunctionService.getProp();
					String userId = JunctionService.getUserId();
					long time = (new Date()).getTime();
					prop.addImage(userId, url, thumbUrl, caption, time);
				}
				catch(IllegalStateException e){
					toastShort("Failed to get info from service! See debug log.");
					e.printStackTrace(System.err);
				}
				
			}
			break;
		}
	}

	private void refresh(){
		try{
			PartyProp prop = JunctionService.getProp();
			List<JSONObject> images = prop.getImages();
			refreshImages(images);
		}
		catch(IllegalStateException e){
			toastShort("Failed to get info from service! See debug log.");
			e.printStackTrace(System.err);
		}
	}

	private void refreshImages(List<JSONObject> images){
		mPics.setNotifyOnChange(false);
		mPics.clear();
		for(JSONObject a : images){
			JSONObject image = new JSONObjWrapper(a){
					public String toString(){
						return optString("caption"); 
					}
				};
			mPics.add(image);
		}
		mPics.setNotifyOnChange(true);
		mPics.notifyDataSetChanged();
	}


	public void onDestroy(){
		super.onDestroy();
	}

}



