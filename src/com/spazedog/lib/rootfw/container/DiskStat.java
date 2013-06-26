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

public class DiskStat implements Container {
	
	private String mDevice;
	private String mLocation;
	private Long mSize;
	private Long mUsage;
	private Long mAvailable;
	private Integer mPercentage;
	
	/**
	 * Create a new DiskStat instance
	 * 
	 * @param aDevice
	 *     Device path
	 *    
	 * @param aLocation
	 *     Mount location
	 *    
	 * @param aSize
	 *     Disk size in bytes
	 *    
	 * @param aUsage
	 *     Disk usage size in bytes
	 *     
	 * @param aAvailable
	 *     Disk available size in bytes
	 *    
	 * @param aPercentage
	 *     Disk usage percentage
	 */
	public DiskStat(String aDevice, String aLocation, Long aSize, Long aUsage, Long aAvailable, Integer aPercentage) {
		mDevice = aDevice;
		mLocation = aLocation;
		mSize = aSize;
		mUsage = aUsage;
		mAvailable = aAvailable;
		mPercentage = aPercentage;
	}
	
	/** 
	 * @return
	 *     Device path
	 */
	public String device() {
		return mDevice;
	}
	
	/** 
	 * @return
	 *     Mount location
	 */
	public String location() {
		return mLocation;
	}
	
	/** 
	 * @return
	 *     Disk size in bytes
	 */
	public Long size() {
		return mSize;
	}
	
	/** 
	 * @return
	 *     Disk usage size in bytes
	 */
	public Long usage() {
		return mUsage;
	}
	
	/** 
	 * @return
	 *     Disk available size in bytes
	 */
	public Long available() {
		return mAvailable;
	}
	
	/** 
	 * @return
	 *     Disk usage percentage
	 */
	public Integer percentage() {
		return mPercentage;
	}
}
