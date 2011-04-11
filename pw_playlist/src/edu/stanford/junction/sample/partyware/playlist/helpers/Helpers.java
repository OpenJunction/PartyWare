package edu.stanford.junction.sample.partyware.playlist.helpers;

import edu.stanford.junction.sample.partyware.playlist.util.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.Toast;
import android.widget.ImageView;
import java.util.*;
import java.io.*;

public class Helpers {
	
	public static void takeSmallPicture(Activity context, int requestCode){
		Camera camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		Camera.Size sz = sizes.get(0);
		parameters.setPictureSize(sz.width, sz.height);
		camera.setParameters(parameters);
		camera.release();

		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (Misc.hasImageCaptureBug()) {
			i.putExtra(MediaStore.EXTRA_OUTPUT, 
					   Uri.fromFile(new File("/sdcard/tmp")));
		} 
		else {
			i.putExtra(MediaStore.EXTRA_OUTPUT, 
					   MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		context.startActivityForResult(i, requestCode);
	}

	
}



