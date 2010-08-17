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
	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.ADD_VIDEO";

	public final static int REQUEST_CODE_SEARCH_YOUTUBE = 0;

	private TextView mTitleView;
	private Intent mVid;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_youtube_video);

		EditText txt = (EditText)findViewById(R.id.caption_text);
		txt.setHint(R.string.add_caption);
		String caption = txt.getText().toString();

		mTitleView = (TextView)findViewById(R.id.uri_view);

		Button button = (Button)findViewById(R.id.find_video_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					searchYoutube();
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

	protected void searchYoutube(){
		Intent intent = new Intent(YoutubeSearchActivity.LAUNCH_INTENT);
		startActivityForResult(intent, REQUEST_CODE_SEARCH_YOUTUBE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case REQUEST_CODE_SEARCH_YOUTUBE:
			if(resultCode == RESULT_OK){
				mVid = data;
				mTitleView.setText(data.getStringExtra("title"));
			}
			break;
		}
	}

	protected void confirm(){
		if(mVid == null){
			toastShort(R.string.no_video_selected);
		}
		else{
			EditText txt = (EditText)findViewById(R.id.caption_text);
			String caption = txt.getText().toString();

			Intent intent = mVid;
			intent.putExtra(EXTRA_CAPTION, caption);
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



