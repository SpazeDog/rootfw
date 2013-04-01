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
	
	/*
	 * This function will send the SIGTERM signal to all processes except init.
	 * This will produce a soft-reboot which will reboot all Android's core and main services, 
	 * but without rebooting the actual device. 
	 */
	public Boolean killAll() {
		RootFW.log(TAG + ".killAll", "Sending SIGTERM signal to all processes");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo e > /proc/sysrq-trigger"));
		Boolean status = result.getResultCode() == 0 ? true : false;
		
		if (!status) {
			RootFW.log(TAG + ".killAll", "It was not possible to send SIGTERM to all processes", RootFW.LOG_WARNING);
		}
		
		return status;
	}
	
	/*
	 * This function will reboot the device. It will use the toolbox reboot command when possible,
	 * or trigger sysrg if toolbox does not support reboot. We cannot use busybox as it's reboot command does not work.
	 */
	public Boolean reboot() {
		RootFW.log(TAG + ".reboot", "Preparing to reboot the device");
		
		ShellResult result = ROOTFW.runShell("toolbox reboot");
		
		if (result.getResultCode() > 0) {
			RootFW.log(TAG + ".reboot", "The device does not support the reboot command. Triggering sysrq instead");
			result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo b > /proc/sysrq-trigger"));
		}
		
		return result.getResultCode() == 0;
	}
	
	/*
	 * This function will reboot the device into recovery using the toolbox reboot command.
	 * The function still needs an sysrg trigger as failsafe
	 */
	public Boolean rebootRecovery() {
		RootFW.log(TAG + ".rebootRecovery", "Preparing to reboot the device into recovery");
		
		ShellResult result = ROOTFW.runShell("toolbox reboot recovery");
		
		if (result.getResultCode() > 0) {
			/* TODO: Add a sysrq trigger for this event */
			RootFW.log(TAG + ".rebootRecovery", "The device does not support the reboot command", RootFW.LOG_WARNING);
		}
		
		return result.getResultCode() == 0;
	}
	
	/*
	 * This function will trigger sysrq into shutting down the device. 
	 * Note that this will not proper unmount the filesystems or send any SIGTERM to any processes
	 */
	public Boolean shutdown() {
		RootFW.log(TAG + ".shutdown", "Preparing to shut down the device");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo o > /proc/sysrq-trigger"));
		
		if (result.getResultCode() > 0) {
			RootFW.log(TAG + ".shutdown", "It was not possible to shut down the device", RootFW.LOG_WARNING);
		}
		
		return result.getResultCode() == 0;
	}
}
