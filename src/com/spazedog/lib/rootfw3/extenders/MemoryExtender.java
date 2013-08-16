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

import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

/**
 * This class is used to get information about memory and SWAP devices
 * <br />
 * As this class is an extender, is should not be called directly. Instead use RootFW.memory()
 */
public class MemoryExtender implements ExtenderGroup {
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");

	FileExtender mProc;
	
	/**
	 * Create a new MemoryExtender instance.
	 * 
	 * @param file
	 *     A FileExtender instance pointed at /proc
	 */
	public MemoryExtender(FileExtender file) {
		mProc = file;
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
		FileExtender file = mProc.open("swaps");
		
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
		FileExtender file = mProc.open("swaps");
		
		if (file.exists()) {
			String[] data = file.readMatches("/dev/").trim().getArray();
			SwapStat[] stat = new SwapStat[ data.length ];
			
			if (data.length > 0) {
				for (int i=1; i < data.length; i++) {
					try {
						String[] sections = oPatternSpaceSearch.split(data[i].trim());
						
						stat[i] = new SwapStat();
						stat[i].mDevice = sections[0];
						stat[i].mSize = Long.parseLong(sections[2]) * 1024L;
						stat[i].mUsage = Long.parseLong(sections[3]) * 1024L;
						
					} catch(Throwable e) {}
				}
				
				return stat;
			}
		}
		
		return null;
	}
	
	/**
	 * This is a container which is used to store information about a SWAP device
	 */
	public class SwapStat {
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
