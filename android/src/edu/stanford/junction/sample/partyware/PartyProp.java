package edu.stanford.junction.sample.partyware;

import java.util.*;
import org.json.JSONObject;
import org.json.JSONException;
import edu.stanford.junction.props2.*;
import edu.stanford.junction.extra.JSONObjWrapper;
import android.util.Log;

public class PartyProp extends Prop {

	public PartyProp(String propName){
		super(propName, propName + (new Random()).nextInt(), new PartyState());
	}

	protected IPropState reifyState(JSONObject obj){
		return new PartyState(obj);
	}

	static class PartyState implements IPropState {

		private HashMap<Integer, JSONObject> objects = new HashMap<Integer, JSONObject>();

		public PartyState(PartyState other){
			if(other != null){
				Iterator it = objects.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Integer,JSONObject> pair = 
						(Map.Entry<Integer,JSONObject>)it.next();
					if(pair.getValue() instanceof JSONObjWrapper){
						JSONObjWrapper obj = (JSONObjWrapper)pair.getValue();
						objects.put(pair.getKey(), (JSONObject)obj.clone());
					}
				}
			}
		}

		public PartyState(JSONObject obj){
			JSONObject jsonObjects = obj.optJSONObject("objects");
			if(jsonObjects != null){
				Iterator it = jsonObjects.keys();
				while(it.hasNext()){
					Object key = it.next();
					Object val = jsonObjects.opt(key.toString());
					if(val instanceof JSONObject){
						JSONObject eaObj = (JSONObject)val;
						int id = eaObj.optInt("id", 0);
						objects.put(id, new JSONObjWrapper(eaObj));
					}
				}
			}
		}

		public PartyState(){
			this((PartyState)null);
		}
		
		public IPropState applyOperation(JSONObject operation){
			return this;
		}
		public JSONObject toJSON(){
			JSONObject obj = new JSONObject();
			JSONObject jsonObjects = new JSONObject();
			try{
				obj.put("objects", jsonObjects);
			}
			catch(JSONException e){
				return obj;
			}
			Iterator it = objects.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer,JSONObject> pair = 
					(Map.Entry<Integer,JSONObject>)it.next();
				try{
					jsonObjects.put(String.valueOf(pair.getKey()), 
									pair.getValue());
				}
				catch(JSONException e){}
			}
			return obj;
		}
		public IPropState copy(){
			return new PartyState(this);
		}
	}

}