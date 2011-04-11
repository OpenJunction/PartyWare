package edu.stanford.junction.sample.partyware.playlist;

import edu.stanford.junction.sample.partyware.playlist.util.BitmapManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.AdapterView;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;


public class MediaListAdapter<T> extends ArrayAdapter<T> {

	protected final BitmapManager mgr = new BitmapManager(10);
	protected final DateFormat dateFormat = DateFormat.getTimeInstance();

	public MediaListAdapter(Context context, int resource){
		super(context, resource, new ArrayList<T>());
	}

	class BitmapHandler extends Handler{
		private ImageView icon;
		public BitmapHandler(ImageView icon){
			this.icon = icon;
		}
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			Bitmap bm = (Bitmap)msg.obj;
			if(bm != null){
				icon.setImageBitmap(bm);
			}
			else{
				icon.setImageResource(R.drawable.ellipsis);
			}
		}
	}

	protected void loadImage(final ImageView icon, String url){
		icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
		icon.setImageResource(R.drawable.ellipsis);
		mgr.getBitmap(url, new BitmapHandler(icon));
	}

	public void recycle(){
		mgr.recycle();
	}

}
