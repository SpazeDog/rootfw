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

package com.spazedog.lib.rootfw.extender;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.ShellProcess;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.iface.Extender;

public final class Binary implements Extender {
	public final static String TAG = RootFW.TAG + "::Binary";
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.binary</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Binary(RootFW aInstance) {
		mParent = aInstance;
	}

	/**
	 * Check if a binary exists on the device
	 * 
	 * @param aBinary
	 *     The name of the binary to look for
	 *    
	 * @return
	 *     <code>True</code> if the binary exist
	 */
	public Boolean exists(String aBinary) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary which '" + aBinary + "'") );
		
		if (lResult != null && lResult.code() == 0) {
			return true;
			
		} else {
			String[] lPaths = variable();
			
			if (lPaths != null) {
				for (int i=0; i < lPaths.length; i++) {
					if (mParent.file.check(lPaths[i] + "/" + aBinary, "e")) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Return the shells PATH variable
	 *    
	 * @return
	 *     An array with all the locations defined in the variable
	 */
	public String[] variable() {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary echo $PATH") );
		
		if (lResult != null && lResult.code() == 0 && lResult.output().length() > 0) {
			return lResult.output().line().split(":");
		}
		
		return null;
	}
}
