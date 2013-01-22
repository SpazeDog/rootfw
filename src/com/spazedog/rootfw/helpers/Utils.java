/*
 * This file is part of the RootFW Project: https://github.com/spazedog/rootfw
 *  
 * Copyright (c) 2013 Daniel Bergløv
 *
 * RootFW is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RootFW is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with RootFW. If not, see <http://www.gnu.org/licenses/>
 */

package com.spazedog.rootfw.helpers;

import android.content.Context;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellCommand;
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
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("md5sum " + argFile));
			
			String md5;
			
			if (result != null && result.getResultCode() == 0 && (md5 = result.getResult().getLastLine()) != null) {
				RootFW.log(TAG, "getMd5(): Returning md5sum '" + md5 + "' on '" + argFile + "'");
				
				return md5.split(" ")[0].trim();
			}
		}
		
		return null;
	}
	
	public Boolean matchMd5(String argFile, String argSum) {
		String md5 = getMd5(argFile);
		
		RootFW.log(TAG, "matchMd5(): Comparing argument '" + argSum + "' with file sum '" + md5 + "' on '" + argFile + "'");
		
		return md5 == null ? false : md5.equals(argSum);
	}
	
	public Boolean recoveryInstall(Context argContext, Integer argResource) {
		if (ROOTFW.busybox.exist()) {
			Boolean isWriteable = ROOTFW.filesystem.hasMountFlag("/", "rw");
			
			if (!isWriteable) {
				ROOTFW.filesystem.remount("/", "rw");
			}
			
			if(ROOTFW.filesystem.copyFileResource(argContext, argContext.getResources().getIdentifier("recovery_install_sh", "raw", argContext.getPackageName()), "/recoveryInstall.sh", "0770", "0", "0")) {
				if(ROOTFW.filesystem.copyFileResource(argContext, argResource, "/update.zip", "0655", "0", "0")) {
					ShellResult result = ROOTFW.runShell("/recoveryInstall.sh");
					
					if (!isWriteable) {
						ROOTFW.filesystem.remount("/", "ro");
					}
					
					return result != null && result.getResultCode() == 0 ? true : false;
				}
			}
			
			if (!isWriteable) {
				ROOTFW.filesystem.remount("/", "ro");
			}
		}
		
		return false;
	}
}
