package edu.stanford.junction.sample.partyware.gallery;

import edu.stanford.junction.sample.partyware.gallery.util.BitmapManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.widget.ImageView;
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;

import java.util.*;


abstract public class RichActivity extends Activity {

	private BitmapManager mgr = new BitmapManager(1);
	private IPropChangeListener mPropListener;

	protected void lazyLoadImage(final ImageView im, final Uri uri){
		im.setImageResource(R.drawable.ellipsis);
		mgr.getBitmap(uri.toString(), new Handler(){
				public void handleMessage(Message msg){
					super.handleMessage(msg);
					Bitmap bm = (Bitmap)msg.obj;
					if(bm != null){
						im.setImageBitmap(bm);
					}
				}
			});
	}

	protected void onAnyPropChange(){}

	protected void listenForAnyPropChange(){
		final Handler refreshHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					onAnyPropChange();
				}
			};
		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		mPropListener = new IPropChangeListener(){
				public String getType(){ return Prop.EVT_ANY; }
				public void onChange(Object data){
					refreshHandler.sendEmptyMessage(0);
				}
			};
		prop.addChangeListener(mPropListener);
	}

	protected void toastShort(String str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	protected void toastShort(int str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	protected void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

	public void onDestroy(){
		super.onDestroy();

		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		prop.removeChangeListener(mPropListener);

		mgr.recycle();
	}

}



