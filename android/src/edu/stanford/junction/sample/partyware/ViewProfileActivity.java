package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.sample.partyware.util.*;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.ProgressDialog;
import android.graphics.PixelFormat;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Spinner;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import org.json.*;


public class ViewProfileActivity extends RichActivity{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.VIEW_PROFILE";

	private TextView mNameText;
	private TextView mEmailText;
	private ImageView mPortraitView;
	private JSONObject mUser;
	private String mId;
	private Spinner mSpinner;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_profile);

		Intent intent = getIntent();
		mId = intent.getStringExtra("user_id");
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
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
		ArrayAdapter adapter = ArrayAdapter.createFromResource(
            this, R.array.relationships, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);

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
		
	}

	protected void confirm(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		String rel = (String)mSpinner.getSelectedItem();
		if(rel != null && !(rel.equals("none"))){
			prop.addRelationship(app.getUserId(), mId, rel);
		}
		finish();
	}

	public void onDestroy(){
		super.onDestroy();
	}

}



