package com.spazedog.rootfw.helpers;

import java.util.ArrayList;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ProcessList;
import com.spazedog.rootfw.containers.ShellCommand;
import com.spazedog.rootfw.containers.ShellResult;

public final class Processes {
	public final static String TAG = RootFW.TAG + "::Processes";
	
	private RootFW ROOTFW;
	
	public Processes(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public ArrayList<ProcessList> getList() {
		RootFW.log(TAG + ".getList", "Getting process list");
		
		String[] proc = ROOTFW.filesystem.getFileList("/proc"), temp;
		String process;
		ArrayList<ProcessList> list = new ArrayList<ProcessList>();
		
		if (proc != null) {
			for (int i=0; i < proc.length; i++) {
				if (proc[i].matches("[0-9]+")) {
					if ((process = ROOTFW.filesystem.readFileLine("/proc/" + proc[i] + "/cmdline")) == null) {
						if ((process = ROOTFW.filesystem.readFileLine("/proc/" + proc[i] + "/stat")) != null) {
							temp = process.replaceAll("[ ]+", " ").split(" ");
							process = temp[1].substring(1, temp[1].length()-1);
							
						} else {
							process = "unknown";
						}
					}
					
					RootFW.log(TAG + ".getList", "Adding ProcessList(Name=" + process + ", Pid=" + proc[i] + ")");
					
					list.add( new ProcessList(process, Integer.parseInt(proc[i])) );
				}
			}
			
		} else {
			RootFW.log(TAG + ".getList", "Could not get the process list", RootFW.LOG_WARNING); return null;
		}
		
		return list;
	}
	
	public Boolean kill(String argProcess) {
		if (!argProcess.matches("^[0-9]+$")) {
			String process = argProcess.startsWith("/") ? argProcess.substring( argProcess.lastIndexOf("/")+1 ) : argProcess;
			
			RootFW.log(TAG + ".kill<Process>", "Killing the process " + process);
			
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary killall " + process));
			Boolean status = result.getResultCode() == 0 ? true : false;
			
			if (!status) {
				RootFW.log(TAG + ".kill<Process>", "Was not able to kill the process " + process, RootFW.LOG_WARNING);
			}
			
			return status;
		}
		
		return kill( Integer.parseInt(argProcess) );
	}
	
	public Boolean kill(Integer argPid) {
		RootFW.log(TAG + ".kill<PID>", "Killing the process id " + argPid);
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary kill -9 " + argPid));
		Boolean status = result.getResultCode() == 0 ? true : false;
		
		if (!status) {
			RootFW.log(TAG + ".kill<PID>", "Was not able to kill the process id " + argPid, RootFW.LOG_WARNING);
		}
		
		return status;
	}
}
