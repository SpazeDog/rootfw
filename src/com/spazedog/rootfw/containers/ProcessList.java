package com.spazedog.rootfw.containers;

public class ProcessList {
	private String NAME;
	private Integer PID;
	
	public ProcessList(String argName, Integer argPid) {
		NAME = argName;
		PID = argPid;
	}
	
	public String getName() {
		return NAME;
	}
	
	public Integer getPid() {
		return PID;
	}
}
