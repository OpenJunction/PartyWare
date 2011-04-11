package edu.stanford.junction.sample.partyware.playlist;

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

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.playlist.VIEW_PROFILE";

	private TextView mNameText;
	private TextView mEmailText;
	private ImageView mPortraitView;
	private JSONObject mUser;
	private String mId;
	private Spinner mSpinner;
	private ArrayAdapter mSpinnerAdapter;
	private boolean mEnableSpinnerEvents = false;

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
		relPrompt.setText("What is " + name + " to you?");


		mSpinner = (Spinner) findViewById(R.id.relationship_spinner);
		mSpinnerAdapter = ArrayAdapter.createFromResource(
            this, R.array.relationships, android.R.layout.simple_spinner_item);
		mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mSpinnerAdapter);

		// Set currently selected relationship
		JSONObject rel = prop.getRelationship(app.getUserId(), mId);
		if(rel != null){
			String relType = rel.optString("relType");
			int index = mSpinnerAdapter.getPosition(relType);
			if(index > -1){
				mSpinner.setSelection(index);
			}
			else{
				mSpinner.setSelection(0);
			}
		}
		else{
			mSpinner.setSelection(0);
		}

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(
					AdapterView<?> parentView,
					View selectedItemView,
					int position, long id){

					// YUCK Ignore the first event (triggered by setSelection)
					if(mEnableSpinnerEvents){
						PartyProp prop = app.getProp();
						String rel = (String)mSpinnerAdapter.getItem(position);
						if(rel == null || rel.equals("none")){
							prop.deleteRelationship(app.getUserId(), mId);
						}
						else{
							System.out.println("Adding rel: " + rel);
							String[] rels = getResources().getStringArray(R.array.relationships);
							String[] revRels = getResources().getStringArray(R.array.reverse_relationships);
							prop.addRelationship(rels, revRels, app.getUserId(), mId, rel);
						}
					}
					else{
						mEnableSpinnerEvents = true;
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> parentView) {}
			});

		TextView pathTitle = (TextView)findViewById(R.id.path_title);
		pathTitle.setText("Shortest path to " + name + ":");

		Button button = (Button)findViewById(R.id.finished_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					confirm();
				}
			});

		TextView pathText = (TextView)findViewById(R.id.path);

		if(mId.equals(app.getUserId())){
			mSpinner.setVisibility(View.GONE);
			pathTitle.setVisibility(View.GONE);
			pathText.setVisibility(View.GONE);
			relPrompt.setVisibility(View.GONE);
		}

		listenForAnyPropChange();
		refresh();
	}

	protected void onAnyPropChange(){
		refresh();
	}

	protected void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
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



