package com.spazedog.rootfw.helpers;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellCommand;
import com.spazedog.rootfw.containers.ShellResult;

public final class Binaries {
	private RootFW ROOTFW;
	
	public Binaries(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public String getPath(String argBinary) {
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("which " + argBinary));
		ShellResult result2 = null;
		
		if (result.getResultCode() == 0) {
			result2 = ROOTFW.runShell(ShellCommand.makeCompatibles("readlink -f $(" + result.getResult().getLastLine() + ")"));
		}
		
		String path = result.getResultCode() == 0 ? (result2.getResultCode() == 0 ? result2.getResult() : result.getResult()).getLastLine().trim() : null;
		
		if (path != null && path.length() > 0) {
			return path;
		}
		
		return null;
	}
}
