package edu.stanford.junction.sample.partyware;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Spinner;
import android.net.Uri;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_profile);

		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();

		Intent intent = getIntent();
		mId = intent.getStringExtra("user_id");
		mUser = prop.getUser(mId);

		mNameText = (TextView)findViewById(R.id.name_text);
		String name = mUser.optString("name");
		mNameText.setText(name);

		mEmailText = (TextView)findViewById(R.id.email_text);
		String email = mUser.optString("email");
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
				mSpinner.setSelection(index);
			}
		}
	}

	protected void confirm(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		String rel = (String)mSpinner.getSelectedItem();
		if(rel != null && !(rel.equals("none"))){
			String[] rels = getResources().getStringArray(R.array.relationships);
			String[] revRels = getResources().getStringArray(R.array.reverse_relationships);
			prop.addRelationship(rels, revRels, app.getUserId(), mId, rel);
		}
		finish();
	}

	public void onDestroy(){
		super.onDestroy();
	}

}



