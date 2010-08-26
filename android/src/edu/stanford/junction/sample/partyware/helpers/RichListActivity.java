package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.IPropChangeListener;
import java.util.*;




abstract public class RichListActivity extends ListActivity {

	private IPropChangeListener mPropListener;

	protected void toastShort(String str){
		Toast.makeText(this, str,Toast.LENGTH_SHORT).show();
	}

	protected void showDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
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


	public void onDestroy(){
		super.onDestroy();

		JunctionApp app = (JunctionApp)getApplication();
		Prop prop = app.getProp();
		prop.removeChangeListener(mPropListener);
	}

}



