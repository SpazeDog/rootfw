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
