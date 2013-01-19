package com.spazedog.rootfw.helpers;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellResult;

public final class Busybox {
	private RootFW ROOTFW;
	
	public Busybox(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public Boolean exist() {
		ShellResult result = ROOTFW.runShell("busybox 1>/dev/null 2>/dev/null");
		
		return result.getResultCode() == 0 ? true : false;
	}
	
	public String[] getApplets() {
		ShellResult result = ROOTFW.runShell("busybox --list 2>/dev/null");
		
		return result.getResult().getLength() > 0 ? result.getResult().getData() : new String[] {};
	}
	
	public String getVersion() {
		ShellResult result = ROOTFW.runShell("busybox 2>/dev/null");
		
		if (result.getResultCode() == 0) {
			return RootFW.replaceAll(result.getResult().getFirstLine(), "  ", " ").trim().split(" ")[1];
		}
		
		return null;
	}
}
