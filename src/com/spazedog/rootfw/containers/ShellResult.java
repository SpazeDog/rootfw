package com.spazedog.rootfw.containers;


public final class ShellResult {
	private Integer RESULTCODE;
	private FileData RESULT;
	
	public ShellResult(Integer argResult, String[] argData) {
		RESULTCODE = argResult;
		RESULT = new FileData(argData);
	}
	
	public FileData getResult() {
		return RESULT;
	}
	
	public Integer getResultCode() {
		return RESULTCODE;
	}
}