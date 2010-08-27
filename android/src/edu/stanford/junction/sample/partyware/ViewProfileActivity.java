package edu.stanford.junction.sample.partyware;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.net.Uri;

import java.util.*;
import org.json.*;


public class ViewProfileActivity extends RichActivity{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.VIEW_PROFILE";

	private TextView mNameText;
	private TextView mEmailText;
	private ImageView mPortraitView;
	private JSONObject mUser;
	private String mId;
	private Spinner mSpinner;
	private ArrayAdapter mAdapter;
	private boolean mDisableSpinnerEvents = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_profile);

		final JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();

		Intent intent = getIntent();
		mId = intent.getStringExtra("user_id");
		mUser = prop.getUser(mId);

		mNameText = (TextView)findViewById(R.id.name_text);
		String name = mUser.optString("name");
		if(name == null) name = "...";
		mNameText.setText(name);

		mEmailText = (TextView)findViewById(R.id.email_text);
		String email = mUser.optString("email");
		if(email == null) email = "...";
		mEmailText.setText(email);

		mPortraitView = (ImageView)findViewById(R.id.image);
		mPortraitView.setImageResource(R.drawable.ellipsis);
		String uri = mUser.optString("imageUrl");
		if(uri != null){
			lazyLoadImage(mPortraitView, Uri.parse(uri));
		}

		TextView relPrompt = (TextView)findViewById(R.id.relationship_prompt);
		relPrompt.setText("What is your relationship with " + name + "?");

		mSpinner = (Spinner) findViewById(R.id.relationship_spinner);
		mAdapter = ArrayAdapter.createFromResource(
            this, R.array.relationships, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mAdapter);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(
					AdapterView<?> parentView,
					View selectedItemView,
					int position, long id){
					if(!mDisableSpinnerEvents){
						PartyProp prop = app.getProp();
						String rel = (String)mSpinner.getSelectedItem();
						if(rel == null || rel.equals("none")){
							prop.deleteRelationship(app.getUserId(), mId);
						}
						else{
							String[] rels = getResources().getStringArray(R.array.relationships);
							String[] revRels = getResources().getStringArray(R.array.reverse_relationships);
							prop.addRelationship(rels, revRels, app.getUserId(), mId, rel);
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> parentView) {}
			});


		TextView pathTitle = (TextView)findViewById(R.id.path_title);
		pathTitle.setText("Path to " + name + ":");

		Button button = (Button)findViewById(R.id.finished_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					confirm();
				}
			});

		listenForAnyPropChange();
		refresh();
	}

	protected void onAnyPropChange(){
		refresh();
	}

	protected void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		// Set currently selected relationship
		JSONObject rel = prop.getRelationship(mUser.optString("id"), mId);
		if(rel != null){
			String relType = rel.optString("relType");
			int index = mAdapter.getPosition(relType);
			if(index > -1){
				mDisableSpinnerEvents = true;
				mSpinner.setSelection(index);
				mDisableSpinnerEvents = false;
			}
		}
		String selfId = app.getUserId();
		Map<String,List<String>> paths = prop.computeShortestPaths(selfId);
		List<String> path = paths.get(mId);
		TextView tv = (TextView)findViewById(R.id.path);
		if(path != null){
			String pathStr = prop.prettyPathString(selfId, path);
			tv.setText(pathStr);
		}
		else{
			tv.setText("No path");
		}
		
	}

	protected void confirm(){
		finish();
	}

	public void onDestroy(){
		super.onDestroy();
	}

}



