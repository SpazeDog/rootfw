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

package com.spazedog.lib.rootfw3.extenders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.containers.BasicContainer;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class ProcessExtender {
	private final static Pattern oPatternPidMatch = Pattern.compile("^[0-9]+$");
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	/**
	 * This class is used to collect information about all processes on the system. 
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#processes()} to retrieve an instance.
	 */
	public static class Processes implements ExtenderGroup {
		private RootFW mParent;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Processes(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Processes(RootFW parent) {
			mParent = parent;
		}
		
		/**
		 * Return a list of all active processes.
		 */
		public ProcessInfo[] getList() {
			return getMatches(null);
		}
		
		/**
		 * Return a list of all active processes which names matches a pattern.
		 * Note that this does not provide an advanced search, it just checks whether or not the pattern exists in the process name. 
		 * 
		 * @param pattern
		 *     The pattern to search for
		 */
		public ProcessInfo[] getMatches(String pattern) {
			String[] files = mParent.file("/proc").getList();
			
			if (files != null) {
				List<ProcessInfo> processes = new ArrayList<ProcessInfo>();
				String process = null;
				String path = null;
				
				for (int i=0; i < files.length; i++) {
					if (oPatternPidMatch.matcher(files[i]).matches()) {
						if ((process = mParent.file("/proc/" + files[i] + "/cmdline").readOneLine()) == null) {
							if ((process = mParent.file("/proc/" + files[i] + "/stat").readOneLine()) != null) {
								try {
									if (pattern == null || process.contains(pattern)) {
										process = oPatternSpaceSearch.split(process.trim())[1];
										process = process.substring(1, process.length()-1);
										
									} else {
										continue;
									}
									
								} catch(Throwable e) { process = null; }
							}
							
						} else if (pattern == null || process.contains(pattern)) {
							if (process.contains("/")) {
								try {
									path = process.substring(process.indexOf("/"), process.contains("-") ? process.indexOf("-", process.lastIndexOf("/", process.indexOf("-"))) : process.length());
								} catch (Throwable e) { path = null; }
									
								if (!process.startsWith("/")) {
									process = process.substring(0, process.indexOf("/"));
									
								} else {
									try {
										process = process.substring(process.lastIndexOf("/", process.contains("-") ? process.indexOf("-") : process.length())+1, process.contains("-") ? process.indexOf("-", process.lastIndexOf("/", process.indexOf("-"))) : process.length());
										
									} catch (Throwable e) { process = null; }
								}
								
							} else if (process.contains("-")) {
								process = process.substring(0, process.indexOf("-"));
							}
							
						} else {
							continue;
						}
						
						if (pattern == null || (process != null && process.contains(pattern))) {
							ProcessInfo stat = new ProcessInfo();
							stat.mPath = path;
							stat.mProcess = process;
							stat.mProcessId = Integer.parseInt(files[i]);
							
							processes.add(stat);
						}
					}
				}
				
				return processes.toArray( new ProcessInfo[ processes.size() ] );
			}
			
			return null;
		}
	}
	
	/**
	 * This class is used to handle a single process, like getting teh pid, the process name, killing the process etc.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#process(String)} or {@link RootFW#process(Integer)} to retrieve an instance.
	 */
	public static class Process implements ExtenderGroup {
		private RootFW mParent;
		private ShellExtender.Shell mShell;
		
		private Integer mPid;
		private String mProcess;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			if (transfer.arguments[0] instanceof String) {
				return transfer.setInstance((ExtenderGroup) new Process(parent, (String) transfer.arguments[0]));
				
			} else {
				return transfer.setInstance((ExtenderGroup) new Process(parent, (Integer) transfer.arguments[0]));
			}
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Process(RootFW parent, String process) {
			mParent = parent;
			mShell = parent.shell();
			mProcess = process;
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Process(RootFW parent, Integer pid) {
			mParent = parent;
			mShell = parent.shell();
			mPid = pid;
		}
		
		/**
		 * Get the pid of the current process. 
		 * If you initialized this object using a process id, this method will return that id. 
		 * Otherwise it will return the first found pid in /proc.
		 */
		public Integer getPid() {
			if (mPid != null) {
				return mPid;
			}
			
			/* We can use pidof (when available) to speed things up whenever the process exist. 
			 * If not, the method will still look trough /proc since we cannot easily check if the error result code 
			 * is a result of a missing process or the missing pidof. Some binaries like busybox returns 1 either way, 
			 * and sorting this out will mostly use more resources than just browsing through /proc. 
			 * Especially if we are ending up doing that any ways if the pidof really did not exist. 
			 */
			ShellResult result = mShell.buildAttempts("%binary pidof '" + mProcess + "'").run();
			
			if (result.wasSuccessful()) {
				String pids = result.getLine();
				
				if (pids != null) {
					try {
						return Integer.parseInt(oPatternSpaceSearch.split(pids.trim())[0]);
					
					} catch (Throwable e) {}
				}
				
			} else {
				ProcessInfo[] processes = mParent.processes().getList();
				
				if (processes != null) {
					for (int i=0; i < processes.length; i++) {
						if (mProcess.equals(processes[i].name())) {
							return processes[i].pid();
						}
					}
				}
			}
			
			return 0;
		}
		
		/**
		 * Get a list of all pid's for this process name. 
		 */
		public Integer[] getPidList() {
			String name = getName();
			
			ShellResult result = mShell.buildAttempts("%binary pidof '" + name + "'").run();
			
			if (result.wasSuccessful()) {
				String pids = result.getLine();
				
				if (pids != null) {
					String[] parts = oPatternSpaceSearch.split(pids.trim());
					Integer[] values = new Integer[ parts.length ];
							
					for (int i=0; i < parts.length; i++) {
						try {
							values[i] = Integer.parseInt(parts[i]);
							
						} catch(Throwable e) {}
					}
					
					return values;
				}
				
			} else {
				ProcessInfo[] processes = mParent.processes().getList();
				
				if (name != null && processes != null && processes.length > 0) {
					List<Integer> list = new ArrayList<Integer>();
					
					for (int i=0; i < processes.length; i++) {
						if (name.equals(processes[i].name())) {
							list.add(processes[i].pid());
						}
					}
					
					return list.toArray( new Integer[ list.size() ] );
				}
			}
			
			return null;
		}
		
		/**
		 * Get the process name of the current process. 
		 * If you initialized this object using a process name, this method will return that name. 
		 * Otherwise it will locate it in /proc based on the pid.
		 */
		public String getName() {
			if (mProcess != null) {
				return mProcess;
			}
			
			String process = null;
			
			if ((process = mParent.file("/proc/" + mPid + "/cmdline").readOneLine()) == null) {
				if ((process = mParent.file("/proc/" + mPid + "/stat").readOneLine()) != null) {
					try {
						process = oPatternSpaceSearch.split(process.trim())[1];
						process = process.substring(1, process.length()-1);
						
					} catch(Throwable e) { process = null; }
				}
				
			} else if (process.contains("/")) {
				if (!process.startsWith("/")) {
					process = process.substring(0, process.indexOf("/"));
					
				} else {
					try {
						process = process.substring(process.lastIndexOf("/", process.contains("-") ? process.indexOf("-") : process.length())+1, process.contains("-") ? process.indexOf("-", process.lastIndexOf("/", process.indexOf("-"))) : process.length());
						
					} catch (Throwable e) { process = null; }
				}
				
			} else if (process.contains("-")) {
				process = process.substring(0, process.indexOf("-"));
			}
			
			return process;
		}
		
		/**
		 * This will return true if you initialized this object using a pid. 
		 * If you used a process name, this will return false. 
		 */
		public Boolean isPidProcess() {
			return mPid != null;
		}
		
		/**
		 * Kill this process. 
		 * If you initialized this object using a pid, only this single process will be killed. 
		 * If you used a process name, all processes with this process name will be killed.  
		 */
		public Boolean kill() {
			String cmd = mPid != null ? "%binary kill -9 '" + mPid + "'" : "%binary killall '" + mProcess + "'";
			
			return mShell.buildAttempts(cmd).run().wasSuccessful();
		}
	}
	
	/**
	 * This class is used control the device power like reboot shutdown etc. 
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#power()} to retrieve an instance.
	 */
	public static class Power implements ExtenderGroup {
		private ShellExtender.Shell mShell;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Power(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Power(RootFW parent) {
			mShell = parent.shell();
		}
		
		/**
		 * Trigger sysrq to sync all file systems and perform a soft reboot (Kill all processes except for init)
		 *    
		 * @return
		 *     <code>True</code> on success
		 */
		public Boolean softReboot() {
			return mShell.run("echo 1 > /proc/sys/kernel/sysrq && echo s > /proc/sysrq-trigger && echo e > /proc/sysrq-trigger").wasSuccessful();
		}
		
		/**
		 * Call toolbox to reboot the device into recovery.
		 * Note that a few toolbox version does not support this feature. 
		 *    
		 * @return
		 *     <code>True</code> on success
		 */
		public Boolean recoveryReboot() {
			/* TODO: Add a sysrq trigger fallback for this event */
			return mShell.run("toolbox reboot recovery").wasSuccessful();
		}
		
		/**
		 * Call toolbox to reboot the device (Uses sysrg as fallback)
		 *    
		 * @return
		 *     <code>True</code> on success
		 */
		public Boolean reboot() {
			if (!mShell.run("toolbox reboot").wasSuccessful()) {
				return mShell.run("echo 1 > /proc/sys/kernel/sysrq && echo s > /proc/sysrq-trigger && echo b > /proc/sysrq-trigger").wasSuccessful();
			}
			
			return true;
		}
		
		/**
		 * Trigger sysrq to sync all file systems and shutdown the device
		 *    
		 * @return
		 *     <code>True</code> on success
		 */
		public Boolean shutdown() {
			return mShell.run("echo 1 > /proc/sys/kernel/sysrq && echo s > /proc/sysrq-trigger && echo o > /proc/sysrq-trigger").wasSuccessful();
		}
	}
	
	/**
	 * This is a container class used to store information about a process.
	 */
	public static class ProcessInfo extends BasicContainer {
		private String mPath;
		private String mProcess;
		private Integer mProcessId;
		
		/** 
		 * @return
		 *     The process path (Could be NULL) as not all processes has a path assigned
		 */
		public String path() {
			return mPath;
		}
		
		/** 
		 * @return
		 *     The name of the process
		 */
		public String name() {
			return mProcess;
		}
		
		/** 
		 * @return
		 *     The pid of the process
		 */
		public Integer pid() {
			return mProcessId;
		}
	}
}
