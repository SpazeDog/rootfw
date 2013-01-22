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
	private RootFW ROOTFW;
	
	public Binaries(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getPath(String argBinary) {
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("which " + argBinary));
		ShellResult result2 = null;
		
		if (result == null || result.getResultCode() == 0) {
			result2 = ROOTFW.runShell(ShellCommand.makeCompatibles("readlink -f $(" + result.getResult().getLastLine() + ")"));
		}
		
		String path;
		
		path = result2 != null && result2.getResultCode() == 0 && (path = result2.getResult().getLastLine()) != null ? path.trim() : 
			result != null && result.getResultCode() == 0 && (path = result.getResult().getLastLine()) != null ? path.trim() : null;
		
		if (path != null && path.length() > 0) {
			return path;
		}
		
		return null;
	}
}
