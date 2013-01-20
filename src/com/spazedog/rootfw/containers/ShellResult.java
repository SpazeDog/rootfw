package com.spazedog.rootfw.containers;


public final class ShellResult {
	private Integer RESULTCODE;
	private FileData RESULT;
	private Integer CMDNUM;
	
	public ShellResult(Integer argCommandNumber, Integer argResult, String[] argData) {
		CMDNUM = argCommandNumber;
		RESULTCODE = argResult;
		RESULT = new FileData(argData);
	}
	
	public FileData getResult() {
		return RESULT;
	}
	
	public Integer getResultCode() {
		return RESULTCODE;
	}
	
	public Integer getCommandNumber() {
		return CMDNUM;
	}
}