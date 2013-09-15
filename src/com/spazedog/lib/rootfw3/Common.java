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

package com.spazedog.lib.rootfw3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Build;
import android.text.TextUtils;

public final class Common {
	private static Boolean mEmulator = false;
	
	private final static Map<String, String> oResolvedPaths = new HashMap<String, String>();
	
	static {
		mEmulator = Build.BRAND.equalsIgnoreCase("generic") || 
				Build.MODEL.contains("google_sdk") || 
				Build.MODEL.contains("Emulator") || 
				Build.MODEL.contains("Android SDK");
	}
	
	/**
	 * Check if the current device is an emulator 
	 */
	public static Boolean isEmulator() {
		return mEmulator;
	}
	
	/**
	 * Remove any . and .. from a path and return an absolute resolved version
	 */
	public static String resolveFilePath(String path) {
		if (!oResolvedPaths.containsKey(path)) {
			String fullPath = path.startsWith("/") ? 
					path : 
						new java.io.File(path).getAbsolutePath();
			
			if (fullPath.contains(".")) {
				String[] directories = ("/".equals(fullPath) ? fullPath : fullPath.endsWith("/") ? fullPath.substring(1, fullPath.length() - 1) : fullPath.substring(1)).split("/");
				List<String> resolved = new ArrayList<String>();
				
				for (int i=0; i < directories.length; i++) {
					if (directories[i].equals("..")) {
						if (resolved.size() > 0) {
							resolved.remove( resolved.size()-1 );
						}
						
					} else if (!directories[i].equals(".")) {
						resolved.add(directories[i]);
					}
				}
				
				fullPath = resolved.size() > 0 ? "/" + TextUtils.join("/", resolved) : "/";
			}
			
			oResolvedPaths.put(path, fullPath);
		}
		
		return oResolvedPaths.get(path);
	}
}
