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

public class FstabEntry implements Container {
	
	private String mDevice;
	private String mLocation;
	private String mFstype;
	private String[] mOptions;

	/**
	 * Create a new FstabEntry instance
	 * 
	 * @param aDevice
	 *     Mounted device
	 *    
	 * @param aLocation
	 *     Mount location
	 *    
	 * @param aFsType
	 *     The device file system type
	 *     
	 * @param aOptions
	 *     Options used at mount time
	 */
	public FstabEntry(String aDevice, String aLocation, String aFsType, String[] aOptions) {
		mDevice = aDevice;
		mLocation = aLocation;
		mFstype = aFsType;
		mOptions = aOptions;
	}
	
	/** 
	 * @return
	 *     The device path
	 */
	public String device() {
		return mDevice;
	}
	
	/** 
	 * @return
	 *     The mount location
	 */
	public String location() {
		return mLocation;
	}
	
	/** 
	 * @return
	 *     The device file system type
	 */
	public String fstype() {
		return mFstype;
	}
	
	/** 
	 * @return
	 *     The options used at mount time
	 */
	public String[] options() {
		return mOptions;
	}
}
