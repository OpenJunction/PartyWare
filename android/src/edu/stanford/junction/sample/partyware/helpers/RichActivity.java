package edu.stanford.junction.sample.partyware;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import java.util.*;


public class RichActivity extends Activity {

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

}


