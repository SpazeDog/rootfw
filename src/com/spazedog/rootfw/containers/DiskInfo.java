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

package com.spazedog.rootfw.containers;

public final class DiskInfo {
	String DEVICE;
	String MOUNTPOINT;
	Integer PERCENTAGE;
	Long SIZE;
	Long USAGE;
	Long REMAINING;
	
	public DiskInfo(String argDevice, Long argSize, Long argUsage, Long argRemaning, Integer argPercentage, String argMountPoint) {
		DEVICE = argDevice;
		MOUNTPOINT = argMountPoint;
		PERCENTAGE = argPercentage;
		SIZE = argSize;
		USAGE = argUsage;
		REMAINING = argRemaning;
	}
	
	public String getDevice() {
		return DEVICE;
	}
	
	public String getMountPoint() {
		return MOUNTPOINT;
	}
	
	public Long getSize() {
		return SIZE;
	}
	
	public Long getUsage() {
		return USAGE;
	}
	
	public Long getRemaining() {
		return REMAINING;
	}
	
	public Integer getPercentage() {
		return PERCENTAGE;
	}
}
