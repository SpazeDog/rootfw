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

package com.spazedog.lib.rootfw.container;

import com.spazedog.lib.rootfw.iface.Container;

public class SwapStat implements Container {

	private String mDevice;
	private Long mSize;
	private Long mUsage;
	
	/**
	 * Create a new SwapStat instance
	 * 
	 * @param aDevice
	 *     Swap device
	 *    
	 * @param aSize
	 *     Swap size in bytes
	 *    
	 * @param aUsage
	 *     Swap usage in bytes
	 */
	public SwapStat(String aDevice, Long aSize, Long aUsage) {
		mDevice = aDevice;
		mSize = aSize;
		mUsage = aUsage;
	}
	
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
