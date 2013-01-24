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
import com.spazedog.rootfw.containers.ShellCommand;
import com.spazedog.rootfw.containers.ShellResult;

public final class Binaries {
	public final static String TAG = RootFW.TAG + "::Binaries";
	
	private RootFW ROOTFW;
	
	public Binaries(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getPath(String argBinary) {
		RootFW.log(TAG + ".getPath", "Locating the binary " + argBinary);
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("which " + argBinary));
		ShellResult result2 = null;
		
		if (result == null || result.getResultCode() == 0) {
			RootFW.log(TAG + ".getPath", "The binary was located at " + result.getResult().getLastLine() + " now fallowing link");
			
			result2 = ROOTFW.runShell(ShellCommand.makeCompatibles("readlink -f $(" + result.getResult().getLastLine() + ")"));
		}
		
		String path;
		
		path = result2 != null && result2.getResultCode() == 0 && (path = result2.getResult().getLastLine()) != null ? path.trim() : 
			result != null && result.getResultCode() == 0 && (path = result.getResult().getLastLine()) != null ? path.trim() : null;
		
		if (path != null && path.length() > 0) {
			RootFW.log(TAG + ".getPath", "The true path was located at " + path);
			
			return path;
			
		} else {
			RootFW.log(TAG + ".getPath", "The binary could not be located", RootFW.LOG_WARNING);
		}
		
		return null;
	}
}
