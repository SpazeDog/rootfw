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

package com.spazedog.rootfw.container;

import com.spazedog.rootfw.iface.Container;

public class MemStat implements Container {
	
	private Long mMemTotal;
	private Long mMemFree;
	private Long mMemCached;
	private Long mSwapTotal;
	private Long mSwapFree;
	private Long mSwapCached;
	
	/**
	 * Create a new DiskStat instance
	 * 
	 * @param aMemTotal
	 *     Total amount of memory in bytes
	 *    
	 * @param aMemFree
	 *     Free amount of memory in bytes
	 *    
	 * @param aMemCached
	 *     The amount of cached memory in bytes
	 *    
	 * @param aSwapTotal
	 *     Total amount of swap space in bytes
	 *     
	 * @param aSwapFree
	 *     Free amount of swap space in bytes
	 *    
	 * @param aSwapCached
	 *     The amount of cached swap space in bytes
	 */
	public MemStat(Long aMemTotal, Long aMemFree, Long aMemCached, Long aSwapTotal, Long aSwapFree, Long aSwapCached) {
		mMemTotal = aMemTotal;
		mMemFree = aMemFree;
		mMemCached = aMemCached;
		mSwapTotal = aSwapTotal;
		mSwapFree = aSwapFree;
		mSwapCached = aSwapCached;
	}
	
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
