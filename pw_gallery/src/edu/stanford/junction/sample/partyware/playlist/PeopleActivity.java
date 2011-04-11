package edu.stanford.junction.sample.partyware.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import org.json.JSONObject;
import java.util.*;


public class PeopleActivity extends RichListActivity implements OnItemClickListener{

	private PeopleAdaptor mPeople;
	private Map<String,List<String>> mPaths;

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

		listenForAnyPropChange();
		refresh();
	}

	protected void onAnyPropChange(){
		refresh();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		JSONObject o = mPeople.getItem(position);
		String userId = o.optString("id");
		Intent intent = new Intent(ViewProfileActivity.LAUNCH_INTENT);
		intent.putExtra("user_id", userId);
		startActivity(intent);
	}

	protected void updateProfile(){
		JunctionApp app = (JunctionApp)getApplication();
		Intent intent = new Intent(UpdateProfileActivity.LAUNCH_INTENT);
		intent.putExtra("name", app.getUserName());
		intent.putExtra("email", app.getUserEmail());
		intent.putExtra("image_url", app.getUserImageUrl());
		startActivity(intent);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}


	private void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();

		mPaths = prop.computeShortestPaths(app.getUserId());

		List<JSONObject> users = prop.getUsers();
		mPeople.clear();
		for(JSONObject a : users){
			mPeople.add(a);
		}


	}


	public void onDestroy(){
		super.onDestroy();
		mPeople.clear();
		mPeople.recycle();
	}

	class DeleteListener implements OnClickListener{
		public String id;
		public DeleteListener(String id){
			this.id = id;
		}
		public void onClick(View v) {
			JunctionApp app = (JunctionApp)getApplication();
			PartyProp prop = app.getProp();
			prop.deleteObj(this.id);
		}
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
				v = vi.inflate(R.layout.profile_picture_item, null);
			}
			JSONObject o = getItem(position);
			if (o != null) {
				String id = o.optString("id");
				JunctionApp app = (JunctionApp)getApplication();
				String selfId = app.getUserId();

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				String name = o.optString("name");
				tt.setText(name);

				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				List<String> path = mPaths.get(id);
				if(path != null){
					if(path.size() == 0){
						bt.setText("");
					}
					else{
						bt.setText(path.size() + " degree(s) away");
					}
				}
				else{
					bt.setText("No relationship.");
				}

				final ImageView icon = (ImageView)v.findViewById(R.id.icon);
				final String url = o.optString("imageUrl");
				loadImage(icon, url);

				Button delete = (Button) v.findViewById(R.id.delete_button);
				
				if(!(app.getUserId().equals(id))){
					delete.setVisibility(View.GONE);
				}
				else{
					delete.setVisibility(View.VISIBLE);
					delete.setOnClickListener(new DeleteListener(id));
				}


			}
			return v;
		}
	}



}



