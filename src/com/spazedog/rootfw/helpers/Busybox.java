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

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellResult;

public final class Busybox {
	public final static String TAG = RootFW.TAG + "::Busybox";
	
	private RootFW ROOTFW;
	
	public Busybox(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public Boolean exist() {
		RootFW.log(TAG + ".exist", "Checking if busybox exists");
		
		ShellResult result = ROOTFW.runShell("busybox test true > /dev/null 2>&1");
		Boolean status = result != null && result.getResultCode() == 0 ? true : false;
		
		RootFW.log(TAG + ".exist", "Busybox " + (status ? "was" : "could not be") + " located");
		
		return status;
	}
	
	public String[] getApplets() {
		RootFW.log(TAG + ".getApplets", "Getting busybox applets");
		
		ShellResult result = ROOTFW.runShell("busybox --list 2>/dev/null");
		
		if (result != null && result.getResultCode() == 0 && result.getResult().getLength() > 0) {
			return result.getResult().getData();
			
		} else {
			RootFW.log(TAG + ".getApplets", "It was not possible to get busybox applets", RootFW.LOG_WARNING);
		}
		
		return new String[] {};
	}
	
	public String getVersion() {
		RootFW.log(TAG + ".getVersion", "Getting busybox version");
		
		ShellResult result = ROOTFW.runShell("busybox 2>/dev/null");
		
		String version;
		
		if (result != null && result.getResultCode() == 0 && (version = result.getResult().getFirstLine()) != null) {
			version = RootFW.replaceAll(version, "  ", " ").trim().split(" ")[1];
			
			RootFW.log(TAG + ".getVersion", "Found busybox version " + version);
			
			return version;
			
		} else {
			RootFW.log(TAG + ".getVersion", "It was not possible to get busybox version", RootFW.LOG_WARNING);
		}
		
		return null;
	}
}
