package edu.stanford.junction.sample.partyware.gallery;

import edu.stanford.junction.sample.partyware.gallery.util.BitmapManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.widget.ImageView;


public class ImageViewerActivity extends RichActivity{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.gallery.VIEW_PICTURE";

	private BitmapManager mgr = new BitmapManager(1);
	private ProgressDialog mProgressDialog;
	private ImageView im;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer);

		im = (ImageView)findViewById(R.id.image);
		im.setScaleType(ImageView.ScaleType.FIT_CENTER);

		Intent intent = getIntent();
		String url = intent.getStringExtra("image_url");

		mgr.getBitmap(url, new Handler(){
				public void handleMessage(Message msg){
					super.handleMessage(msg);
					Bitmap bm = (Bitmap)msg.obj;
					if(bm != null){
						im.setImageBitmap(bm);
					}
					if(mProgressDialog != null){
						mProgressDialog.dismiss();
					}
				}
			});

		mProgressDialog = ProgressDialog.show(this,"",
											  "Loading...", true);

	}


    public void onDestroy() {
		super.onDestroy();
		mgr.recycle();
	}

}



