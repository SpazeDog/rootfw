package com.spazedog.rootfw.helpers;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellResult;

public final class Binaries {
	private RootFW ROOTFW;
	
	public Binaries(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getPath(String argBinary) {
		ShellResult result = ROOTFW.runShell(RootFW.mkCmd("readlink -f $(" + RootFW.mkCmd("which " + argBinary) + ")"));
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell(RootFW.mkCmd("which " + argBinary));
		}
		
		String path = result.getResult().getLastLine().trim();
		
		if (result.getResultCode() == 0 && path.length() > 0) {
			return path;
		}
		
		return null;
	}
}
