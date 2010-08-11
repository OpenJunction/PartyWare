package edu.stanford.junction.sample.partyware;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONException;
import edu.stanford.junction.props2.*;
import android.util.Log;

public class PartyProp extends Prop {

	public PartyProp(String propName){
		super(propName, propName + (new Random()).nextInt(), new PartyState());
	}

	protected IPropState reifyState(JSONObject obj){
		return new PartyState();
	}

	static class PartyState implements IPropState {
		public IPropState applyOperation(JSONObject operation){
			return this;
		}
		public JSONObject toJSON(){
			return new JSONObject();
		}
		public IPropState copy(){
			return this;
		}
	}

}