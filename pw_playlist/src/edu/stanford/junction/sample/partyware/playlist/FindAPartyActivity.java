package edu.stanford.junction.sample.partyware.playlist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.os.Handler;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.content.Intent;
import edu.stanford.junction.extra.JSONObjWrapper;
import org.json.JSONObject;
import java.util.*;
import java.text.DateFormat;

public class FindAPartyActivity extends RichListActivity implements OnItemClickListener{

    private Handler mMainHandler;
    private ArrayAdapter<JSONObject> mAdverts;

	private final DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdverts = new ArrayAdapter<JSONObject>(this, 
												android.R.layout.simple_list_item_1,
												new ArrayList<JSONObject>());
		setListAdapter(mAdverts);
		getListView().setTextFilterEnabled(true);
		getListView().setOnItemClickListener(this); 
		refreshAdverts(new HashSet<JSONObject>());
	}

    public void onItemClick(AdapterView parent, View v, int position, long id){
		Intent intent = new Intent();
		JSONObject advert = (JSONObject)mAdverts.getItem(position);
		String url = advert.optString("url");
		intent.putExtra("session_url", url);
		setResult(RESULT_OK, intent);
		Toast.makeText(this, "Connecting to '" + advert.optString("name") + 
					   "', please wait...", Toast.LENGTH_SHORT).show();
		finish();
    }

	private void refreshAdverts(Set<JSONObject> adverts){
		mAdverts.setNotifyOnChange(false);
		mAdverts.clear();
		for(JSONObject a : adverts){
			JSONObject advert = new JSONObjWrapper(a){
					public String toString(){ 
						String name = optString("name");
						Date d = new Date(optLong("time"));
						return  name + " - " + dateFormat.format(d); 
					}
				};
			mAdverts.add(advert);
		}
		mAdverts.setNotifyOnChange(true);
		mAdverts.notifyDataSetChanged();
	}


	public void onDestroy(){
		super.onDestroy();
	}

}




