package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.sample.partyware.util.BitmapManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import edu.stanford.junction.extra.JSONObjWrapper;
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;

import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;

import com.google.api.client.util.*;
import com.google.api.client.googleapis.*;
import com.google.api.client.http.*;
import com.google.api.data.youtube.v2.*;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.AtomParser;


public class YoutubeSearchActivity extends RichListActivity implements OnItemClickListener{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.YOUTUBE_SEARCH";

	private YoutubeEntryAdapter mVids;
	private ProgressDialog mSearchProgressDialog;

	private final Handler searchHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(mSearchProgressDialog != null){
					mSearchProgressDialog.dismiss();
				}
				Object o = msg.obj;
				if(o instanceof VideoFeed){
					mVids.clear();
					for (VideoEntry videoEntry : ((VideoFeed)o).videos) {
						mVids.add(videoEntry);
					}
				}
				else if(o instanceof Throwable){
					((Throwable)o).printStackTrace(System.err);
					toastShort("Sorry, youtube search failed. See debug log.");
				}
			}
		};


    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube_search);
		mVids = new YoutubeEntryAdapter(this);
		setListAdapter(mVids);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 


		final EditText txt = (EditText)findViewById(R.id.query_text);
		txt.setHint("Enter keywords...");

		Button button = (Button)findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String q = txt.getText().toString();
					newQuery(q);
				}
			});
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		VideoEntry entry = mVids.getItem(position);
		Intent intent = new Intent();
		intent.putExtra("title", entry.title);
		intent.putExtra("video_id", entry.getVideoId());
		intent.putExtra("thumb_url", entry.getThumbUrl());
		setResult(RESULT_OK, intent);
		finish();
	}

	public static class VideoFeed {
		@Key("entry") public List<VideoEntry> videos;
	}

	public static class VideoEntry implements Cloneable{
		@Key public String title;
		@Key public String id;

		public String getThumbUrl(){
			return "http://img.youtube.com/vi/" + 
				this.getVideoId() + "/default.jpg";
		}
		
		public String getVideoId(){
			String id = this.id;
			if(id == null) return "NA";
			String patternStr = "video:(.+)$";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(id);
			boolean matchFound = matcher.find();
			if(matchFound){
				return matcher.group(1); 
			} 
			return "NA";
		}

		@Override
		protected VideoEntry clone() {
			return DataUtil.clone(this);
		}
	}

	public static class YouTubeUrl extends GoogleUrl {
		@Key public String orderby;
		@Key public String q;
		@Key public String safeSearch;
		public YouTubeUrl(String encodedUrl) {
			super(encodedUrl);
		}
	}

	public static class Util {
		public static final XmlNamespaceDictionary NAMESPACE_DICTIONARY = new XmlNamespaceDictionary();
		static {
			Map<String, String> map = NAMESPACE_DICTIONARY.namespaceAliasToUriMap;
			map.put("", "http://www.w3.org/2005/Atom");
			map.put("atom", "http://www.w3.org/2005/Atom");
			map.put("exif", "http://schemas.google.com/photos/exif/2007");
			map.put("gd", "http://schemas.google.com/g/2005");
			map.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
			map.put("georss", "http://www.georss.org/georss");
			map.put("gml", "http://www.opengis.net/gml");
			map.put("gphoto", "http://schemas.google.com/photos/2007");
			map.put("media", "http://search.yahoo.com/mrss/");
			map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
			map.put("xml", "http://www.w3.org/XML/1998/namespace");
		}
		private Util() {}
	}


	protected void newQuery(final String query){
		if(mSearchProgressDialog != null) {
			mSearchProgressDialog.dismiss();
		}
		mSearchProgressDialog = ProgressDialog.show(
			this,"",
			"Searching YouTube. Please wait...", true);

		new Thread(){
			public void run(){
				HttpTransport transport = GoogleTransport.create();
				GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
				headers.setApplicationName("partyware-prototype-1.0");
				headers.gdataVersion = "2";
				AtomParser parser = new AtomParser();
				parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;
				transport.addParser(parser);

				YouTubeUrl url = new YouTubeUrl("http://gdata.youtube.com/feeds/api/videos");
				url.orderby = "relevance";
				url.q = query;
				url.safeSearch = "none";
				HttpRequest request = transport.buildGetRequest();
				request.url = url;

				Message m = searchHandler.obtainMessage();
				try {
					HttpResponse response = request.execute();
					// 	System.out.println("FEED: " + response.parseAsString());
					VideoFeed videoFeed = response.parseAs(VideoFeed.class);
					m.obj = videoFeed;
					searchHandler.sendMessage(m);
				} 
				catch (Exception e) {
					m.obj = e;
					searchHandler.sendMessage(m);
				}
			}
		}.start();
	}



	public void onDestroy(){
		super.onDestroy();
	}


	class YoutubeEntryAdapter extends MediaListAdapter<VideoEntry> {

		public YoutubeEntryAdapter(Context context){
			super(context, R.layout.youtube_item);
		}

		/** 
		 *  Returns a new ImageView to be displayed, depending on 
		 *	the position passed. 
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.youtube_item, null);
			}
			VideoEntry entry = getItem(position);
			if (entry != null) {
				String title = entry.title;
				String thumbUrl = entry.getThumbUrl();

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				tt.setText(title);

				ImageView icon = (ImageView)v.findViewById(R.id.icon);
				loadImage(icon, thumbUrl);
			}
			return v;

		}


	}


}










