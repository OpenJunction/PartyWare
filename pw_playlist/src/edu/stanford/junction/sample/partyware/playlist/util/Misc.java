package edu.stanford.junction.sample.partyware.playlist.util;

import java.util.ArrayList;

public class Misc{

	// see http://stackoverflow.com/questions/1910608/android-action-image-capture-intent

	public static boolean hasImageCaptureBug() {
		// list of known devices that have the bug
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");
		return devices.contains(android.os.Build.BRAND + "/" + 
								android.os.Build.PRODUCT + "/" + 
								android.os.Build.DEVICE);
	}


}