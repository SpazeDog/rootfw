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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.Data;
import com.spazedog.lib.rootfw.container.MemStat;
import com.spazedog.lib.rootfw.container.SwapStat;
import com.spazedog.lib.rootfw.iface.Extender;

public final class Memory implements Extender {
	public final static String TAG = RootFW.TAG + "::Memory";
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.memory</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Memory(RootFW aInstance) {
		mParent = aInstance;
	}

	/**
	 * Get a stat of the memory usage on the device
	 *    
	 * @return
	 *     A MemStat instance with all the memory information
	 */
	public MemStat usage() {
		Data lMemInfo = mParent.file.read("/proc/meminfo");
		
		if (lMemInfo != null) {
			Map<String, Long> lMemLines = new HashMap<String, Long>();
			String[] lMemSections, lMemOutput = lMemInfo.raw();
			
			for (int i=0; i < lMemOutput.length; i++) {
				lMemSections = oPatternSpaceSearch.split(lMemOutput[i]);
				
				try {
					lMemLines.put(lMemSections[0].substring(0, lMemSections[0].length()-1), Long.parseLong(lMemSections[1]) * 1024L);
					
				} catch(Throwable e) {}
			}
			
			return new MemStat(lMemLines.get("MemTotal"), lMemLines.get("MemFree"), lMemLines.get("Cached"), lMemLines.get("SwapTotal"), lMemLines.get("SwapFree"), lMemLines.get("SwapCached"));
		}
		
		return null;
	}
	
	/**
	 * Get a list of all active swap devices, their sizes and usage
	 *    
	 * @return
	 *     An ArrayList with SwapStat containers
	 */
	public ArrayList<SwapStat> swaps() {
		Data lSwapInfo = mParent.file.read("/proc/swaps");
		
		if (lSwapInfo != null && lSwapInfo.length() > 1) {
			String[] lSections, lSwaps = lSwapInfo.raw();
			ArrayList<SwapStat> list = new ArrayList<SwapStat>();
			
			for (int i=1; i < lSwaps.length; i++) {
				try {
					lSections = oPatternSpaceSearch.split(lSwaps[i].trim());
					
					list.add( new SwapStat(lSections[0], Long.parseLong(lSections[2]) * 1024L, Long.parseLong(lSections[3]) * 1024L) );
					
				} catch(Throwable e) {}
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
}
