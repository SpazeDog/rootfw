/*
 * This file is part of the RootFW Project: https://github.com/spazedog/rootfw
 *  
 * Copyright (c) 2013 Daniel Bergløv
 *
 * RootFW is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RootFW is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with RootFW. If not, see <http://www.gnu.org/licenses/>
 */

package com.spazedog.lib.rootfw.extender;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.os.SystemClock;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.ProcessList;
import com.spazedog.lib.rootfw.container.ShellProcess;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.iface.Extender;

public final class Processes implements Extender {
	public final static String TAG = RootFW.TAG + "::Processes";
	
	private final static Pattern oPatternPidMatch = Pattern.compile("^[0-9]+$");
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	private static Long oZygoteId;
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.processes</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Processes(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Returns an unique id of the current zygote process.
	 * This will allow you to determine if the device has been rebooted, by comparing it to a stored value.
	 *    
	 * @return
	 *     A long value representing the unique ID
	 */
	public Long zygoteId() {
		if (oZygoteId == null) {
			String id = mParent.file.readLine("/zygote.id");
			
			if (id == null) {
				oZygoteId = System.currentTimeMillis() - SystemClock.elapsedRealtime();
				
				mParent.filesystem.mount("/", new String[]{"remount", "rw"});
				mParent.file.write("/zygote.id", "" + oZygoteId);
				mParent.filesystem.mount("/", new String[]{"remount", "ro"});
				
			} else {
				oZygoteId = Long.valueOf(id);
			}
		}
		
		return oZygoteId;
	}

	/**
	 * Return a list of all active processes. Each process is stored in a 
	 * ProcessList container with both it's name and pid
	 *    
	 * @return
	 *     A list of all processes
	 */
	public ArrayList<ProcessList> list() {
		String[] lFiles = mParent.file.list("/proc");
		
		if (lFiles != null) {
			ArrayList<ProcessList> list = new ArrayList<ProcessList>();
			String lProcess;
			
			for (int i=0; i < lFiles.length; i++) {
				if (oPatternPidMatch.matcher(lFiles[i]).matches()) {
					if ((lProcess = mParent.file.readLine("/proc/" + lFiles[i] + "/cmdline")) == null) {
						if ((lProcess = mParent.file.readLine("/proc/" + lFiles[i] + "/stat")) != null) {
							try {
								lProcess = oPatternSpaceSearch.split(lProcess.trim())[1];
								lProcess = lProcess.substring(1, lProcess.length()-1);
								
							} catch(Throwable e) { lProcess = null; }
							
						} else {
							lProcess = null;
						}
					}
					
					list.add( new ProcessList(lProcess, Integer.parseInt(lFiles[i])) );
				}
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
	
	/**
	 * Get the pid of a process
	 * 
	 * @param aProcess
	 *     The name of the process
	 *    
	 * @return
	 *     The pid of the first found process or 0 if none was found
	 */
	public Integer pidof(String aProcess) {
		ArrayList<ProcessList> processes = list();
		
		if (processes != null) {
			for (int i=0; i < processes.size(); i++) {
				String name = processes.get(i).name();
				
				if (name != null && (name.equals(aProcess) || (name.contains("/") && name.substring(name.lastIndexOf("/")+1).equals(aProcess)))) {
					return processes.get(i).pid();
				}
			}
		}
		
		return 0;
	}
	
	/**
	 * Kill a process based on it's name
	 * 
	 * @param aProcess
	 *     The name of the process
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean kill(String aProcess) {
		if (!oPatternPidMatch.matcher(aProcess).matches()) {
			String lName = aProcess.startsWith("/") ? aProcess.substring( aProcess.lastIndexOf("/")+1 ) : aProcess;
			
			ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary killall '" + lName + "'") );
			
			return lResult != null && lResult.code() == 0;
		}
		
		return kill( Integer.parseInt(aProcess) );
	}
	
	/**
	 * Kill a process based on it's pid
	 * 
	 * @param aPid
	 *     The pid of the process
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean kill(Integer aPid) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary kill -9 '" + aPid + "'") );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Trigger sysrq to sync all file systems and perform a soft reboot (Kill all processes except for init)
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean softReboot() {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo e > /proc/sysrq-trigger") );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Call toolbox to reboot the device into recovery
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean recoveryReboot() {
		ShellResult lResult = mParent.shell.execute("toolbox reboot recovery");
		
		/* TODO: Add a sysrq trigger fallback for this event */
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Call toolbox to reboot the device (Uses sysrg as fallback)
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean reboot() {
		ShellResult lResult = mParent.shell.execute("toolbox reboot");
		
		if (lResult == null || lResult.code() != 0) {
			lResult = mParent.shell.execute( ShellProcess.generate("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo b > /proc/sysrq-trigger") );
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Trigger sysrq to sync all file systems and shutdown the device
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean shutdown() {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary echo 1 > /proc/sys/kernel/sysrq && %binary echo s > /proc/sysrq-trigger && %binary echo o > /proc/sysrq-trigger") );
		
		return lResult != null && lResult.code() == 0;
	}
}
