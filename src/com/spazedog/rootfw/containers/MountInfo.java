package com.spazedog.rootfw.containers;

public final class MountInfo {
	private String DEVICE;
	private String MOUNT;
	private String FSTYPE;
	private String[] FLAGS;
	
	public MountInfo(String argDevice, String argMount, String argFstype, String[] argFlags) {
		DEVICE = argDevice;
		MOUNT = argMount;
		FSTYPE = argFstype;
		FLAGS = argFlags;
	}
	
	public String getDevice() {
		return DEVICE;
	}
	
	public String getMountPoint() {
		return MOUNT;
	}
	
	public String getFsType() {
		return FSTYPE;
	}
	
	public String[] getFlags() {
		return FLAGS;
	}
}