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

public class DeviceList implements Container {

	private String lType;
	private String lName;
	private Integer lMajor;
	
	/**
	 * Create a new DeviceList instance
	 * 
	 * @param aName
	 *     Device name
	 *    
	 * @param aMajor
	 *     Device Major number
	 *    
	 * @param aType
	 *     Device type (Block 'b' or Character 'c')
	 */
	public DeviceList(String aName, Integer aMajor, String aType) {
		lType = aType;
		lName = aName;
		lMajor = aMajor;
	}
	
	/** 
	 * @return
	 *     Device type (Block 'b' or Character 'c')
	 */
	public String type() {
		return lType;
	}
	
	/** 
	 * @return
	 *     Device name
	 */
	public String name() {
		return lName;
	}
	
	/** 
	 * @return
	 *     Device Major number
	 */
	public Integer major() {
		return lMajor;
	}
}
