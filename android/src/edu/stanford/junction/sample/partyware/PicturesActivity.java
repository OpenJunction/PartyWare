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


public class PicturesActivity extends ListActivity implements OnItemClickListener{

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

		for(int i = 0; i < 20; i++) mPics.add(randomTestObj());

		Button button = (Button)findViewById(R.id.add_picture_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					addPic();
				}
			});

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
				Uri uri = intent.getData();
				String comment = intent.getStringExtra(AddPictureActivity.EXTRA_COMMENT);
				Toast.makeText(this, 
							   "URL: " + uri + "\nComment: " + comment, 
							   Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	private void refresh(){
	}

	private void refreshAdverts(Set<JSONObject> adverts){
		mPics.setNotifyOnChange(false);
		mPics.clear();
		for(JSONObject a : adverts){
			JSONObject advert = new JSONObjWrapper(a){
					public String toString(){ 
						String name = optString("name");
						Date d = new Date(optLong("time"));
						return  name + " - " + dateFormat.format(d); 
					}
				};
			mPics.add(advert);
		}
		mPics.setNotifyOnChange(true);
		mPics.notifyDataSetChanged();
	}


	public void onDestroy(){
		super.onDestroy();
	}

}



