package edu.stanford.junction.sample.partyware.playlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;

import java.util.*;

import com.google.api.client.util.*;
import com.google.api.client.googleapis.*;
import com.google.api.client.http.*;
import com.google.api.client.googleapis.json.JsonCParser;

public class YoutubeSearchActivity extends RichListActivity implements OnItemClickListener{

	public final static String LAUNCH_INTENT = "edu.stanford.junction.sample.partyware.playlist.YOUTUBE_SEARCH";

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
				if(o instanceof VideoFeed ){
					VideoFeed feed = (VideoFeed)o; 
					if(feed.items != null){
						mVids.clear();
						for (VideoEntry videoEntry : feed.items) {
							if(videoEntry.isEmbedable()){
								mVids.add(videoEntry);
							}
						}
					}
				}
				else if(o instanceof Throwable){
					((Throwable)o).printStackTrace(System.err);
					toastShort("Sorry, youtube search failed. Please try again.");
				}
				else{
					toastShort("Sorry, youtube search failed. Please try again.");
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

		final Button button = (Button)findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
					// Dismiss the soft keyboard.
					InputMethodManager manager = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
					IBinder binder = button.getApplicationWindowToken();
					if (binder != null) {
						manager.hideSoftInputFromWindow(binder, 0);
					}

					String q = txt.getText().toString();
					newQuery(q);

				}
			});
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
		VideoEntry entry = mVids.getItem(position);
		Intent intent = new Intent();
		intent.putExtra("title", entry.title);
		intent.putExtra("video_id", entry.id);
		intent.putExtra("thumb_url", entry.getThumbUrl());
		setResult(RESULT_OK, intent);
		finish();
	}

	public static class Feed {
		@Key public int itemsPerPage;
		@Key public int startIndex;
		@Key public int totalItems;
		@Key public DateTime updated;
	}

	public static class VideoFeed extends Feed{
		@Key public List<VideoEntry> items;
	}

	public static class Player {
		// "default" is a Java keyword, so need to specify the JSON key manually
		@Key("default")
		public String defaultUrl;
	}

	public static class AccessControl {
		@Key public String embed;
		@Key public String syndicate;
	}

	public static class Item {
		@Key public String id;
		@Key public String title;
		@Key public DateTime updated;
	}

	public static class VideoEntry extends Item{
		@Key public String embed;
		@Key public String description;
		@Key public List<String> tags;
		@Key public Player player;
		@Key public AccessControl accessControl;
		public String getThumbUrl(){
			return "http://img.youtube.com/vi/" + this.id + "/default.jpg";
		}
		public boolean isEmbedable(){
			return (accessControl != null) && 
				accessControl.syndicate.equals("allowed") && 
				accessControl.embed.equals("allowed");
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
			this.alt = "jsonc";
		}
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
				headers.setApplicationName("partyware-prototype-1.0" + UUID.randomUUID());
				headers.gdataVersion = "2";
				transport.addParser(new JsonCParser());

				YouTubeUrl url = new YouTubeUrl("http://gdata.youtube.com/feeds/api/videos");
				url.orderby = "relevance";
				url.q = query;
				url.safeSearch = "none";
				HttpRequest request = transport.buildGetRequest();
				request.url = url;

				Message m = searchHandler.obtainMessage();
				Throwable error = null;

				// For some crazy reason, _every_ _other_ search fails,
				// so we try twice.
				for(int i = 0; i < 2; i++){
					try {
						HttpResponse response = request.execute();
						VideoFeed videoFeed = response.parseAs(VideoFeed.class);
						m.obj = videoFeed;
						searchHandler.sendMessage(m);
						return;
					} 
					catch (Exception e) {
						error = e;
						e.printStackTrace(System.err);
						continue;
					}
				}
				m.obj = error;
				searchHandler.sendMessage(m);
			}
		}.start();
	}



	public void onDestroy(){
		super.onDestroy();
		mVids.clear();
		mVids.recycle();
	}


	class YoutubeEntryAdapter extends MediaListAdapter<VideoEntry> {

		public YoutubeEntryAdapter(Context context){
			super(context, R.layout.youtube_search_item);
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
				v = vi.inflate(R.layout.youtube_search_item, null);
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










