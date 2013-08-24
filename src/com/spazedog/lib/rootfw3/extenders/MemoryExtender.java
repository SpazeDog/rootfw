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

package com.spazedog.lib.rootfw3.extenders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class MemoryExtender {
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	/**
	 * This class is used to get information about memory and SWAP devices
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#memory()} to retrieve an instance.
	 */
	public static class Memory implements ExtenderGroup {
		private RootFW mParent;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Memory(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Memory(RootFW parent) {
			mParent = parent;
		}
		
		/**
		 * Get information like size and usage of a specific SWAP device. This method will return null if the device does not exist, or if it has not been activated.
		 * 
		 * @param device
		 *     The specific SWAP device path to get infomation about
		 *     
		 * @return
		 *     An SwapStat object containing information about the requested SWAP device
		 */
		public SwapStat statSwap(String device) {
			FileExtender.File file = mParent.file("/proc/swaps");
			
			if (file.exists()) {
				String data = file.readMatches(device).getLine();
				
				if (data != null && data.length() > 0) {
					try {
						String[] sections = oPatternSpaceSearch.split(data);
						
						SwapStat stat = new SwapStat();
						stat.mDevice = sections[0];
						stat.mSize = Long.parseLong(sections[2]) * 1024L;
						stat.mUsage = Long.parseLong(sections[3]) * 1024L;
						
						return stat;
						
					} catch(Throwable e) {}
				}
			}
			
			return null;
		}
		
		/**
		 * Get a list of all active SWAP devices.
		 *     
		 * @return
		 *     An SwapStat array of all active SWAP devices
		 */
		public SwapStat[] listSwaps() {
			FileExtender.File file = mParent.file("/proc/swaps");
			
			if (file.exists()) {
				String[] data = file.readMatches("/dev/").trim().getArray();
				List<SwapStat> statList = new ArrayList<SwapStat>();
				
				if (data.length > 0) {
					for (int i=0; i < data.length; i++) {
						try {
							String[] sections = oPatternSpaceSearch.split(data[i].trim());
							
							SwapStat stat = new SwapStat();
							stat.mDevice = sections[0];
							stat.mSize = Long.parseLong(sections[2]) * 1024L;
							stat.mUsage = Long.parseLong(sections[3]) * 1024L;
							
							statList.add(stat);
							
						} catch(Throwable e) {}
					}
					
					return statList.size() > 0 ? statList.toArray( new SwapStat[ statList.size() ] ) : null;
				}
			}
			
			return null;
		}
	}
	
	/**
	 * This is a container which is used to store information about a SWAP device
	 */
	public static class SwapStat {
		private String mDevice;
		private Long mSize;
		private Long mUsage;
		
		/** 
		 * @return
		 *     Path to the SWAP device
		 */
		public String device() {
			return mDevice;
		}
		
		/** 
		 * @return
		 *     SWAP size in bytes
		 */
		public Long size() {
			return mSize;
		}
		
		/** 
		 * @return
		 *     SWAP usage in bytes
		 */
		public Long usage() {
			return mUsage;
		}
	}
}
