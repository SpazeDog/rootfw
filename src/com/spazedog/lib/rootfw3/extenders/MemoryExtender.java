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

import android.os.Bundle;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.containers.BasicContainer;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
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
		public static ExtenderGroupTransfer getInstance(RootFW parent, Object instanceLock, ExtenderGroupTransfer transfer) {
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
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onBroadcastReceive(Integer broadcastType, Bundle arguments) {}
		
		/**
		 * Get memory information like ram usage, ram total, cached memory, swap total etc.
		 */
		public MemStat getUsage() {
			FileData data = mParent.file("/proc/meminfo").read();
			
			if (data != null && data.size() > 0) {
				String[] lines = data.getArray();
				MemStat stat = new MemStat();
				
				for (int i=0; i < lines.length; i++) {
					String[] parts = oPatternSpaceSearch.split(lines[i]);
					
					if (parts[0].equals("MemTotal:")) {
						stat.mMemTotal = Long.parseLong(parts[1]) * 1024L;
						
					} else if (parts[0].equals("MemFree:")) {
						stat.mMemFree = Long.parseLong(parts[1]) * 1024L;
						
					} else if (parts[0].equals("Cached:")) {
						stat.mMemCached = Long.parseLong(parts[1]) * 1024L;
						
					} else if (parts[0].equals("SwapTotal:")) {
						stat.mSwapTotal = Long.parseLong(parts[1]) * 1024L;
						
					} else if (parts[0].equals("SwapFree:")) {
						stat.mSwapFree = Long.parseLong(parts[1]) * 1024L;
						
					} else if (parts[0].equals("SwapCached:")) {
						stat.mSwapCached = Long.parseLong(parts[1]) * 1024L;
						
					}
				}
				
				return stat;
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
				
				if (data != null && data.length > 0) {
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
	 * This class is used to get information about memory devices like a SWAP or ZRAM device
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#memory(String)} to retrieve an instance.
	 */
	public static class Device implements ExtenderGroup {
		private RootFW mParent;
		private FileExtender.File mDevice;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, Object instanceLock, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Device(parent, (FileExtender.File) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Device(RootFW parent, FileExtender.File device) {
			mParent = parent;
			mDevice = device;
		}
		
		/**
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onBroadcastReceive(Integer broadcastType, Bundle arguments) {}
		
		/**
		 * Get information like size and usage of a specific SWAP device. This method will return null if the device does not exist, or if it has not been activated.
		 * 
		 * @param device
		 *     The specific SWAP device path to get infomation about
		 *     
		 * @return
		 *     An SwapStat object containing information about the requested SWAP device
		 */
		public SwapStat statSwap() {
			if (mDevice.exists()) {
				FileExtender.File file = mParent.file("/proc/swaps");
				
				if (file.exists()) {
					String data = file.readMatches(mDevice.getAbsolutePath()).getLine();
					
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
			}
			
			return null;
		}
	}
	
	/**
	 * This is a container which is used to store information about a SWAP device
	 */
	public static class SwapStat extends BasicContainer {
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
	
	public static class MemStat extends BasicContainer {
		private Long mMemTotal = 0L;
		private Long mMemFree = 0L;
		private Long mMemCached = 0L;
		private Long mSwapTotal = 0L;
		private Long mSwapFree = 0L;
		private Long mSwapCached = 0L;
		
		/** 
		 * @return
		 *     Total amount of memory in bytes, including SWAP space
		 */
		public Long total() {
			return mMemTotal + mSwapTotal;
		}
		
		/** 
		 * @return
		 *     Free amount of memory in bytes, including SWAP space and cached memory
		 */
		public Long free() {
			return mMemFree + mSwapFree + (mMemCached + mSwapCached);
		}
		
		/** 
		 * @return
		 *     Amount of cached memory including SWAP space
		 */
		public Long cached() {
			return mMemCached + mSwapCached;
		}
		
		/** 
		 * @return
		 *     Amount of used memory including SWAP (Cached memory not included)
		 */
		public Long usage() {
			return total() - free();
		}
		
		/** 
		 * @return
		 *     Memory usage in percentage, including SWAP space (Cached memory not included)
		 */
		public Integer percentage() {
			return ((Long) ((usage() * 100L) / total())).intValue();
		}
		
		/** 
		 * @return
		 *     Total amount of memory in bytes
		 */
		public Long memTotal() {
			return mMemTotal;
		}
		
		/** 
		 * @return
		 *     Free amount of memory in bytes, including cached memory
		 */
		public Long memFree() {
			return mMemFree + mMemCached;
		}
		
		/** 
		 * @return
		 *     Amount of cached memory
		 */
		public Long memCached() {
			return mMemCached;
		}
		
		/** 
		 * @return
		 *     Amount of used memory (Cached memory not included)
		 */
		public Long memUsage() {
			return memTotal() - memFree();
		}
		
		/** 
		 * @return
		 *     Memory usage in percentage (Cached memory not included)
		 */
		public Integer memPercentage() {
			return ((Long) ((memUsage() * 100L) / memTotal())).intValue();
		}
		
		/** 
		 * @return
		 *     Total amount of SWAP space in bytes
		 */
		public Long swapTotal() {
			return mSwapTotal;
		}
		
		/** 
		 * @return
		 *     Free amount of SWAP space in bytes, including cached memory
		 */
		public Long swapFree() {
			return mSwapFree + mSwapCached;
		}
		
		/** 
		 * @return
		 *     Amount of cached SWAP space
		 */
		public Long swapCached() {
			return mSwapCached;
		}
		
		/** 
		 * @return
		 *     Amount of used SWAP space (Cached memory not included)
		 */
		public Long swapUsage() {
			return swapTotal() - swapFree();
		}
		
		/** 
		 * @return
		 *     SWAP space usage in percentage (Cached memory not included)
		 */
		public Integer swapPercentage() {
			return ((Long) ((swapUsage() * 100L) / swapTotal())).intValue();
		}
	}
}
