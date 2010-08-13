package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.sample.partyware.util.Misc;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;

import org.json.*;

import java.net.*;
import java.io.*;
import java.util.*;


public class AddYoutubeVideoActivity extends RichActivity{

	public final static String EXTRA_CAPTION = "edu.stanford.junction.sample.partyware.CAPTION";
	public final static String EXTRA_VIDEO_ID = "edu.stanford.junction.sample.partyware.VIDEO_ID";
	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.ADD_VIDEO";

	public final static int REQUEST_CODE_PICK_FROM_LIBRARY = 0;

	private TextView mUriView;
	private String mVideoId;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_youtube_video);

		EditText txt = (EditText)findViewById(R.id.caption_text);
		txt.setHint(R.string.add_caption);
		String caption = txt.getText().toString();

		mUriView = (TextView)findViewById(R.id.uri_view);

		Button button = (Button)findViewById(R.id.find_video_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pickFromLibrary();
				}
			});

		button = (Button)findViewById(R.id.finished_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					confirm();
				}
			});

		button = (Button)findViewById(R.id.cancel_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					cancel();
				}
			});
	}


	protected void pickFromLibrary(){
		// Intent i = new Intent(Intent.ACTION_PICK, 
		// 					  android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		// startActivityForResult(i, REQUEST_CODE_PICK_FROM_LIBRARY);

		mVideoId = "lksjdfdf";
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case REQUEST_CODE_PICK_FROM_LIBRARY:
			if(resultCode == RESULT_OK){
				// do stuff here
			}
			break;
		}
	}


	protected void confirm(){
		if(mVideoId == null){
			toastShort(R.string.no_video_selected);
		}
		else{
			// return the video id with caption
			Intent intent = new Intent();
			EditText txt = (EditText)findViewById(R.id.caption_text);
			String caption = txt.getText().toString();
			intent.putExtra(EXTRA_CAPTION, caption);
			intent.putExtra(EXTRA_VIDEO_ID, mVideoId);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	protected void cancel(){
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}


}



