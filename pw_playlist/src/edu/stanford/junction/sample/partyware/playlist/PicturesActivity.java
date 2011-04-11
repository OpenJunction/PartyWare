package edu.stanford.junction.sample.partyware.playlist;

import edu.stanford.junction.sample.partyware.playlist.util.*;
import edu.stanford.junction.sample.partyware.playlist.helpers.*;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import org.json.JSONObject;

import java.io.*;
import java.util.*;


public class PicturesActivity extends RichListActivity implements OnItemClickListener{

	public final static int REQUEST_CODE_PICK_FROM_LIBRARY = 0;
	public final static int REQUEST_CODE_TAKE_PICTURE = 1;
	private PicAdapter mPics;
	private BroadcastReceiver mUriReceiver;
	private BroadcastReceiver mErrorReceiver;
	private ProgressDialog mUploadProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pictures);

		mPics = new PicAdapter(this);
		setListAdapter(mPics);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 

		Button button = (Button)findViewById(R.id.use_camera_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Helpers.takeSmallPicture(PicturesActivity.this, 
											 REQUEST_CODE_TAKE_PICTURE);
				}
			});

		button = (Button)findViewById(R.id.pick_from_library_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					pickFromLibrary();
				}
			});

		listenForAnyPropChange();
		refresh();
	}

	protected void onAnyPropChange(){
		refresh();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		JSONObject o = mPics.getItem(position);
		Intent intent = new Intent();
		intent.setAction(ImageViewerActivity.LAUNCH_INTENT);
		String url = o.optString("url");
		intent.putExtra("image_url", url);
		startActivity(intent);
	}


	protected void pickFromLibrary(){
		Intent i = new Intent(
			Intent.ACTION_PICK, 
			android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_CODE_PICK_FROM_LIBRARY);
	}

	protected void uploadFinishedHandler(final Intent intent){
		final String url = intent.getStringExtra("image_url");
		final String thumbUrl = intent.getStringExtra("thumb_url");

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Caption");  
		alert.setMessage("Please enter a caption for your picture: ");

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.pic_info, null);
		alert.setView(layout);
		final EditText input = (EditText)(layout.findViewById(R.id.caption_text));

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton){
					long time = (long)(System.currentTimeMillis()/1000.0);
					try{
						String caption = input.getText().toString();
						JunctionApp app = (JunctionApp)getApplication();
						PartyProp prop = app.getProp();
						String userId = app.getUserId();
						prop.addImage(userId, url, thumbUrl, caption, time);
					}
					catch(IllegalStateException e){
						toastShort("Oops! Not connected to any party.");
						e.printStackTrace(System.err);
					}
					catch(Exception e){
						toastShort("Oops! Failed to add picture to party.");
						e.printStackTrace(System.err);
					}
				}
			});  
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}  
			});  
		alert.show();  
	}

	protected void startUpload(Uri localUri){

		mUriReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					unregisterReceiver(this);
					mUploadProgressDialog.dismiss();
					uploadFinishedHandler(intent);
				}
			};

		IntentFilter intentFilter = new IntentFilter(ImgurUploadService.BROADCAST_FINISHED);
		registerReceiver(mUriReceiver, intentFilter); 

		mErrorReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					unregisterReceiver(this);
					mUploadProgressDialog.dismiss();
					String error = intent.getStringExtra("error");
					showDialog(error);
				}
			};
		intentFilter = new IntentFilter(ImgurUploadService.BROADCAST_FAILED);
		registerReceiver(mErrorReceiver, intentFilter);

		Intent i = new Intent(this, ImgurUploadService.class);
		i.setAction(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_STREAM, localUri);
		startService(i);

		mUploadProgressDialog = ProgressDialog.show(this,"","Uploading. Please wait...",true);

	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case REQUEST_CODE_PICK_FROM_LIBRARY:
			if(resultCode == RESULT_OK){
				Uri localUri = data.getData();
				startUpload(localUri);
			}
			break;
		case REQUEST_CODE_TAKE_PICTURE:
			if(resultCode == RESULT_OK){
				Uri localUri;
				if (Misc.hasImageCaptureBug()) {
					File fi = new File("/sdcard/tmp");
					try {
						localUri = Uri.parse(
							android.provider.MediaStore.Images.Media.insertImage(
								getContentResolver(), 
								fi.getAbsolutePath(), null, null));
						if (!fi.delete()) {
							Log.i("AddPictureActivity", "Failed to delete " + fi);
						}
						startUpload(localUri);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				} else {
					localUri = data.getData();
					startUpload(localUri);
                }
				
			}
			break;
		}
	}


	private void refresh(){
		JunctionApp app = (JunctionApp)getApplication();
		PartyProp prop = app.getProp();
		List<JSONObject> images = prop.getImages();
		refreshImages(images);
	}

	private void refreshImages(List<JSONObject> images){
		mPics.clear();
		for(JSONObject a : images){
			mPics.add(a);
		}
	}


	public void onDestroy(){
		super.onDestroy();
		try{
			unregisterReceiver(mUriReceiver);
			unregisterReceiver(mErrorReceiver);
			Intent i = new Intent(this, ImgurUploadService.class);
			stopService(i);
		}
		catch(IllegalArgumentException e){}

		mPics.clear();
		mPics.recycle();
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


	class PicAdapter extends MediaListAdapter<JSONObject> {

		public PicAdapter(Context context){
			super(context, R.layout.picture_item);
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
				String caption = o.optString("caption");
				tt.setText(caption);

				Date d = new Date(o.optLong("time") * 1000);
				String time = dateFormat.format(d); 
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				bt.setText(" " + time);

				final ImageView icon = (ImageView)v.findViewById(R.id.icon);
				final String url = o.optString("thumbUrl");
				loadImage(icon, url);

				String id = o.optString("id");
				String owner = o.optString("owner");

				Button delete = (Button) v.findViewById(R.id.delete_button);

				JunctionApp app = (JunctionApp)getApplication();

				
				if(!(app.getUserId().equals(owner))){
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



