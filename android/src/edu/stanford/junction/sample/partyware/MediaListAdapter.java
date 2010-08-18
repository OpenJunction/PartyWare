package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.sample.partyware.util.BitmapManager;

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

import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;


public class MediaListAdapter extends ArrayAdapter<JSONObject> {

	private BitmapManager mgr = new BitmapManager(3);
	private final DateFormat dateFormat = DateFormat.getTimeInstance();


	public MediaListAdapter(Context context, int resource, List<JSONObject> objects){
		super(context, resource, objects);
	}

	/** 
	 *  Returns a new ImageView to be displayed, depending on 
	 *	the position passed. 
	 */
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

			Date d = new Date(o.optLong("time"));
			String time = dateFormat.format(d); 
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			bt.setText(" " + time);

			final ImageView icon = (ImageView)v.findViewById(R.id.icon);
			icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
			final String url = o.optString("thumbUrl");

			icon.setImageResource(R.drawable.ellipsis);

			mgr.getBitmap(url, new Handler(){
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
				});
		}
		return v;

	}


}
