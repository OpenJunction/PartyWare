package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.sample.partyware.util.BitmapManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import edu.stanford.junction.extra.JSONObjWrapper;

import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.text.DateFormat;


public class ImageViewerActivity extends RichActivity{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.VIEW_PICTURE";

	private BitmapManager mgr = new BitmapManager(1);

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer);

		ImageView im = (ImageView)findViewById(R.id.image);


		Intent intent = getIntent();
		String url = intent.getStringExtra("image_url");
		Bitmap bm = mgr.getBitmap(url);
		im.setImageBitmap(bm);

		im.setScaleType(ImageView.ScaleType.FIT_CENTER);
	}


    public void onDestroy() {
		super.onDestroy();
		mgr.dispose();
	}

}



