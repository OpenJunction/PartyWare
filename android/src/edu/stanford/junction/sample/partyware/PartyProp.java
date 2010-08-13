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

	protected JSONObject newAddObjOp(JSONObject item){
		JSONObject obj = new JSONObject();
		try{
			obj.put("type", "addObj");
			obj.put("item", item);
		}catch(JSONException e){}
		return obj;
	}

	protected void addObj(JSONObject item){
		addOperation(newAddObjOp(item));
	}


	/////////////////////////
    // Conveniance helpers //
    /////////////////////////

	public List<JSONObject> getImages(){
		PartyState s = (PartyState)getState();
		return s.getImages();
	}

	public List<JSONObject> getYoutubeVids(){
		PartyState s = (PartyState)getState();
		return s.getYoutubeVids();
	}

	public void addImage(String userId, String url, String thumbUrl, String caption, long time){
		addObj(newImageObj(userId, url, thumbUrl, caption, time));
	}

	public void addYoutube(String userId, String videoId, String caption, long time){
		addObj(newYoutubeObj(userId, videoId, caption, time));
	}

	protected JSONObject newImageObj(String userId, String url, String thumbUrl, String caption, long time){
		JSONObject obj = newHTTPResourceObj("image", userId, url, caption, time);
		try{
			obj.put("thumbUrl", thumbUrl);
		}catch(JSONException e){}
		return obj;
	}

	protected JSONObject newYoutubeObj(String userId, String videoId, 
									   String caption, long time){
		JSONObject obj = newHTTPResourceObj("youtube", userId, null, caption, time);
		try{
			obj.put("videoId", videoId);
		}catch(JSONException e){}
		return obj;
	}

	protected JSONObject newHTTPResourceObj(String type, String userId, 
											String url, String caption, long time){
		JSONObject obj = new JSONObject();
		try{
			Random r = new Random();
			obj.put("id", (UUID.randomUUID()).toString());
			obj.put("type", type);
			obj.put("url", url);
			obj.put("time", time);
			obj.put("caption", caption);
			obj.put("owner", userId);
		}catch(JSONException e){}
		return obj;
	}


	//////////////////////////
    // The State definition //
    //////////////////////////

	static class PartyState implements IPropState {

		private HashMap<String, JSONObject> objects = new HashMap<String, JSONObject>();

		public PartyState(PartyState other){
			if(other != null){
				Iterator it = other.objects.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String,JSONObject> pair = 
						(Map.Entry<String,JSONObject>)it.next();
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
						String id = eaObj.optString("id");
						objects.put(id, new JSONObjWrapper(eaObj));
					}
				}
			}
		}

		public PartyState(){
			this((PartyState)null);
		}
		
		public IPropState applyOperation(JSONObject op){
			String type = op.optString("type");
			if(type.equals("addObj")){
				JSONObject item = op.optJSONObject("item");
				String id = item.optString("id");
				objects.put(id, (new JSONObjWrapper(item)));
			}
			else{
				throw new IllegalStateException("Unrecognized operation: " + type);
			}
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
				Map.Entry<String,JSONObject> pair = 
					(Map.Entry<String,JSONObject>)it.next();
				try{
					JSONObjWrapper wrapper = (JSONObjWrapper)pair.getValue();
					jsonObjects.put(String.valueOf(pair.getKey()), 
									wrapper.getRaw());
				}
				catch(JSONException e){}
			}
			return obj;
		}


		public List<JSONObject> getImages(){
			ArrayList<JSONObject> images = new ArrayList<JSONObject>();
			Iterator<JSONObject> it = objects.values().iterator();
			while (it.hasNext()) {
				JSONObject ea = it.next();
				String type = ea.optString("type");
				if(type.equals("image")){
					images.add(ea);
				}
			}
			return Collections.unmodifiableList(images);
		}


		public List<JSONObject> getYoutubeVids(){
			ArrayList<JSONObject> vids = new ArrayList<JSONObject>();
			Iterator<JSONObject> it = objects.values().iterator();
			while (it.hasNext()) {
				JSONObject ea = it.next();
				String type = ea.optString("type");
				if(type.equals("youtube")){
					vids.add(ea);
				}
			}
			return Collections.unmodifiableList(vids);
		}

		public IPropState copy(){
			return new PartyState(this);
		}
	}

}