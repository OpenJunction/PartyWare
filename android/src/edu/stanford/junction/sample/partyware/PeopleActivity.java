package edu.stanford.junction.sample.partyware;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;

import org.json.JSONObject;

import java.util.*;


public class PeopleActivity extends RichListActivity implements OnItemClickListener{

	public final static int REQUEST_CODE_UPDATE_USER = 0;
	private PeopleAdaptor mPeople;
	private IPropChangeListener mPropListener;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.people);
		mPeople = new PeopleAdaptor(this);
		setListAdapter(mPeople);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 

		Button button = (Button)findViewById(R.id.update_profile_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					updateProfile();
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

	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		JSONObject o = mPeople.getItem(position);
	}

	protected void updateProfile(){
		final JunctionApp app = (JunctionApp)getApplication();
		app.updateUser();
		// Intent intent = new Intent(UpdateProfileActivity.LAUNCH_INTENT);
		// startActivityForResult(i, REQUEST_CODE_UPDATE_USER);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case REQUEST_CODE_UPDATE_USER:
			if(resultCode == RESULT_OK){
				//
			}
			break;
		}
	}


	private void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		List<JSONObject> users = prop.getUsers();
		mPeople.clear();
		for(JSONObject a : users){
			mPeople.add(a);
		}
	}


	public void onDestroy(){
		super.onDestroy();

		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		prop.removeChangeListener(mPropListener);

		mPeople.clear();
		mPeople.recycle();
	}


	class PeopleAdaptor extends MediaListAdapter<JSONObject> {

		public PeopleAdaptor(Context context){
			super(context, R.layout.person_item);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)(getContext().getSystemService(
														 Context.LAYOUT_INFLATER_SERVICE));
				v = vi.inflate(R.layout.picture_item, null);
			}
			JSONObject o = getItem(position);
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				String name = o.optString("name");
				tt.setText(name);

				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				String email = o.optString("email");
				bt.setText(email);

				final ImageView icon = (ImageView)v.findViewById(R.id.icon);
				final String url = o.optString("imageUrl");
				loadImage(icon, url);
			}
			return v;
		}
	}



}



