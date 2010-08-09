package edu.stanford.junction.sample.partyware;

import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.activity.JunctionExtra;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
import edu.stanford.junction.provider.xmpp.ConnectionTimeoutException;
import edu.stanford.junction.props2.Prop;
import edu.stanford.junction.props2.sample.ListState;
import edu.stanford.junction.props2.IPropChangeListener;

import android.content.ServiceConnection;
import android.content.ContentValues;
import android.os.Bundle;
import android.app.Service;
import android.app.Activity;

import org.json.*;

import java.net.*;
import java.io.*;
import java.util.*;


public class MainActivity extends Activity{

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onDestroy(){
		super.onDestroy();
	}


}



