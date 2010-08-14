package edu.stanford.junction.sample.partyware.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.*;
import java.net.*;
import java.io.*;


public class BitmapManager{

	private final float hashTableLoadFactor = 0.75f;
	final LinkedHashMap<String, Bitmap> cache;
	final int cacheSize;

	public BitmapManager(int cacheSize){
		this.cacheSize = cacheSize;
		int hashTableCapacity = (int)Math.ceil(cacheSize / hashTableLoadFactor) + 1;
		cache = new LinkedHashMap<String,Bitmap>( 
			hashTableCapacity, hashTableLoadFactor, true) {

			@Override protected boolean removeEldestEntry 
			(Map.Entry<String,Bitmap> eldest) {
				if(size() > BitmapManager.this.cacheSize){
					String url = eldest.getKey();
					remove(url);
				}

				// We already handled it manually, so
				// return false.
				return false;
			}

		}; 
	}

	public Bitmap getBitmap(String url){
		Bitmap bm = cache.get(url);
		if(bm != null) {
			return bm;
		}
		else{
			try{
				URL aURL = new URL(url);
				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				/* Buffered is always good for a performance plus. */
				BufferedInputStream bis = new BufferedInputStream(is);

				/* Decode url-data to a bitmap. */
				Bitmap newBm = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
				cache.put(url, newBm);
				return newBm;
			}
			catch(IOException e){
				e.printStackTrace(System.err);
				return null;
			}
		}
	}

	public void dispose(){
		Iterator<Bitmap> it = cache.values().iterator();
		while(it.hasNext()){
			(it.next()).recycle();
		}
		cache.clear();
	}


}