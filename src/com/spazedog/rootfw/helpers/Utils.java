package com.spazedog.rootfw.helpers;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellResult;

public class Utils {
	private final static String TAG = "RootFW:Utils";
	
	private RootFW ROOTFW;
	
	public Utils(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getMd5(String argFile) {
		/* It's very important to make sure that the string contains something. 
		 * Otherwise the md5sum binary will start a process and everything will hang forever */
		if (argFile.length() > 0 && ROOTFW.filesystem.exist(argFile)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("md5sum " + argFile));
			
			if (result.getResultCode() == 0) {
				String md5 = result.getResult().getLastLine().split(" ")[0].trim();
				
				RootFW.log(TAG, "getMd5(): Returning md5sum '" + md5 + "' on '" + argFile + "'");
				
				return md5;
			}
		}
		
		return null;
	}
	
	public Boolean matchMd5(String argFile, String argSum) {
		String md5 = getMd5(argFile);
		
		RootFW.log(TAG, "matchMd5(): Comparing argument '" + argSum + "' with file sum '" + md5 + "' on '" + argFile + "'");
		
		return md5 == null ? false : md5.equals(argSum);
	}
}
