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
	public final static String TAG = RootFW.TAG + "::Utils";
	
	private RootFW ROOTFW;
	
	public Utils(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getMd5(String argFile) {
		RootFW.log(TAG + ".getMd5", "Getting md5sum on the file " + argFile);
		
		/* It's very important to make sure that the string contains something. 
		 * Otherwise the md5sum binary will start a process and everything will hang forever */
		if (argFile.length() > 0 && ROOTFW.filesystem.exist(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary md5sum " + argFile));
			
			String md5;
			
			if (result != null && result.getResultCode() == 0 && (md5 = result.getResult().getLastLine()) != null) {
				RootFW.log(TAG + ".getMd5", "Returning md5sum '" + md5 + "' on '" + argFile + "'");
				
				return md5.split(" ")[0].trim();
			}
		}
		
		RootFW.log(TAG + ".getMd5", "Could not get the md5sum on the file " + argFile, RootFW.LOG_WARNING);
		
		return null;
	}
	
	public Boolean matchMd5(String argFile, String argSum) {
		String md5 = getMd5(argFile);
		
		RootFW.log(TAG + ".matchMd5", "Comparing argument '" + argSum + "' with file sum '" + md5 + "' on '" + argFile + "'");
		
		return md5 == null ? false : md5.equals(argSum);
	}
	
	public Boolean recoveryInstall(Context argContext, Integer argResource) {
		RootFW.log(TAG + ".recoveryInstall", "Starting recovery install process using resource " + argResource);
		
		if (ROOTFW.busybox.exist()) {
			Boolean isWriteable = ROOTFW.filesystem.hasMountFlag("/", "rw");
			
			if (!isWriteable) {
				ROOTFW.filesystem.remount("/", "rw");
			}
			
			if(ROOTFW.filesystem.copyFileResource(argContext, argContext.getResources().getIdentifier("recovery_install_sh", "raw", argContext.getPackageName()), "/recoveryInstall.sh", "0770", "0", "0")) {
				if(ROOTFW.filesystem.copyFileResource(argContext, argResource, "/update.zip", "0655", "0", "0")) {
					RootFW.log(TAG + ".recoveryInstall", "Executing recoveryInstall.sh in order to prepare recovery resources and reboot the system");
					
					ShellResult result = ROOTFW.runShell("/recoveryInstall.sh");
					
					if (result != null && result.getResultCode() == 0) {
						return true;
					}
				}
			}
			
			if (!isWriteable) {
				ROOTFW.filesystem.remount("/", "ro");
			}
		}
		
		RootFW.log(TAG + ".recoveryInstall", "Was not able to start recovery install", RootFW.LOG_WARNING);
		
		return false;
	}
}
