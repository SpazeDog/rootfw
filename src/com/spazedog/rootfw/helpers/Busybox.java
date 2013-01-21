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
	private RootFW ROOTFW;
	
	public Busybox(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public Boolean exist() {
		ShellResult result = ROOTFW.runShell("busybox 1>/dev/null 2>/dev/null");
		
		return result != null && result.getResultCode() == 0 ? true : false;
	}
	
	public String[] getApplets() {
		ShellResult result = ROOTFW.runShell("busybox --list 2>/dev/null");
		
		return result != null && result.getResult().getLength() > 0 ? result.getResult().getData() : new String[] {};
	}
	
	public String getVersion() {
		ShellResult result = ROOTFW.runShell("busybox 2>/dev/null");
		
		if (result != null && result.getResultCode() == 0) {
			return RootFW.replaceAll(result.getResult().getFirstLine(), "  ", " ").trim().split(" ")[1];
		}
		
		return null;
	}
}
