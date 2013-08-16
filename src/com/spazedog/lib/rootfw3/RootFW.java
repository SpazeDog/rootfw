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

package com.spazedog.lib.rootfw3;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw3.extenders.InstanceExtender;
import com.spazedog.lib.rootfw3.extenders.ShellExtender;

/**
 * <dl>
 * <dt><span class="strong">Example:</span></dt>
 * <dd><code><pre>
 * RootFW root = new RootFW();
 * 
 * if (root.connect()) {
 *     ...
 *     
 *     root.disconnect();
 * }
 * </pre></code></dd>
 * </dl>
 */
public class RootFW {
	public final static String TAG = "RootFW";
	
	public final static Integer E_DEBUG = 2;
	public final static Integer E_INFO = 4;
	public final static Integer E_WARNING = 8;
	public final static Integer E_ERROR = 16;
	
	/**
	 * A static configuration class where some configs can be changed for the framework.
	 * 
	 * <dl>
	 * <dt><span class="strong">Example:</span></dt>
	 * <dd><code><pre>RootFW.Config.PATH.add("/data/local/bin");</pre></code></dd>
	 * </dl>
	 */
	public static class Config {
		/**
		 * Used to extend the shell PATH variable. Each index in the ArrayList should contain a path to one or more binaries. Each index will be added in front of the standard variables.
		 */
		public static final List<String> PATH = new ArrayList<String>();
		
		/**
		 * This property contains all of the all-in-one binaries that an be used on a device. By default, it contains busybox and toolbox if available in the PATH variable paths
		 */
		public static final List<String> BINARIES = new ArrayList<String>();
		
		/**
		 * This property decides what should be logged. You can add one or more of the RootFW.E properties to this one to enable logging.
		 */
		public static Integer LOG = RootFW.E_ERROR|RootFW.E_WARNING;
	}
	
	static {
		Config.BINARIES.add("busybox");
		Config.BINARIES.add("toolbox");
	}
	
	protected Boolean mRootUser = false;
	
	protected Process mShell = null;
	
	protected BufferedReader mInputStream;
	protected DataOutputStream mOutputStream;
	
	/**
	 * Get an root instance of RootFW.
	 * <br />
	 * This instance is globally shared instance. If no connection is currently opened, a new instance is created and a connection is auto established. Otherwise, the current instance is returned.
	 * <br />
	 * This makes it possible for multiple classes and methods to share the same shell connection.
	 * 
	 * @return
	 *     A new InstanceExtender instance containing the shared shell connection
	 */
	public static InstanceExtender rootInstance() {
		return new InstanceExtender(true);
	}
	
	/**
	 * Get an user instance of RootFW.
	 * <br />
	 * This instance is globally shared instance. If no connection is currently opened, a new instance is created and a connection is auto established. Otherwise, the current instance is returned.
	 * <br />
	 * This makes it possible for multiple classes and methods to share the same shell connection.
	 * 
	 * @return
	 *     A new InstanceExtender instance containing the shared shell connection
	 */
	public static InstanceExtender userInstance() {
		return new InstanceExtender(false);
	}
	
	/**
	 * Create a new root instance of RootFW
	 */
	public RootFW() {
		this(true);
	}

	/**
	 * Create a new instance of RootFW
	 *    
	 * @param aRoot
	 *     Whether to create a root shell or a regular user shell
	 */
	public RootFW(Boolean root) {
		mRootUser = root;
	}
	
	/**
	 * Establish a connection to the shell
	 *    
	 * @return
	 *     <code>True</code> on successful connection
	 */
	public Boolean connect() {
		if (!isConnected()) {
			ProcessBuilder builder = new ProcessBuilder( mRootUser ? "su" : "sh" );
			builder.redirectErrorStream(true);
			
			try {
				mShell = builder.start();
				
				mInputStream = new BufferedReader( new InputStreamReader(mShell.getInputStream()) );
				mOutputStream = new DataOutputStream(mShell.getOutputStream());
				
				if(isConnected()) {
					if (Config.PATH.size() > 0) {
						shell("PATH=\"" + TextUtils.join(":", Arrays.asList(Config.PATH)) + ":$PATH\"");
					}
					
					return true;
					
				} else if (mShell != null) {
					mShell.destroy();
					mShell = null;
				}
				
			} catch (Throwable e) {}
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Close the connection to the shell
	 */
	public void disconnect() {
		if (isConnected()) {
			shell("exit");
			
			try {
				mInputStream.close();
				mOutputStream.close();
				
			} catch (IOException e) {}
			
			mShell.destroy();
			mShell = null;
		}
	}
	
	/**
	 * Check whether a shell connection has been established
	 *    
	 * @return
	 *     <code>True</code> if the connection is open
	 */
	public Boolean isConnected() {
		return mShell != null && "connected".equals( shell("echo 'connected'") );
	}
	
	/**
	 * Check whether this shell is a root shell or a reglar user shell
	 *    
	 * @return
	 *     <code>True</code> if this is a root shell
	 */
	public Boolean isRoot() {
		return mRootUser && isConnected();
	}
	
	/**
	 * Return a new ShellExtender instance which can be used to communicate with the shell
	 *    
	 * @return
	 *     A new ShellExtender instance
	 */
	public ShellExtender shell() {
		return new ShellExtender(mInputStream, mOutputStream);
	}
	
	/**
	 * This is a quick shell executer, which can be used for small fast shell work. It allows you to execute one single command, and then returns the last line of the output.
	 * 
	 * <dl><dt><span class="strong">Example:</span></dt><dd><code>String line = rootfwInstance.shell("df -h /dev/block/mmcblk0p1");</code></dd></dl>
	 * 
	 * @param aRoot
	 *     Whether to connect as root or as a regular user
	 *    
	 * @return
	 *     The last line of the shell output
	 */
	public String shell(String command) {
		return shell().run(command).getLine();
	}
	
	/**
	 * @see RootFW#log(String, String, Integer, Throwable)
	 */
	public static void log(String aTag, String aMsg) {
		log(aTag, aMsg, E_INFO, null);
	}
	
	/**
	 * @see RootFW#log(String, String, Integer, Throwable)
	 */
	public static void log(String aTag, String aMsg, Integer aLevel) {
		log(aTag, aMsg, aLevel, null);
	}
	
	/**
	 * This method is used to add logs to the logcat. Unlike using logcat directly, this method is controlled using RootFW.Config.LOG, which determines 
	 * whether or not to display a log, and which types to display.
	 * 
	 * @param tag
	 *     The tag to use in logcat
	 *     
	 * @param msg
	 *     The message to send to logcat
	 *     
	 * @param level
	 *     The log level defined by RootFW.E_*
	 *     
	 * @param e
	 *     A Throwable instance
	 */
	public static void log(String tag, String msg, Integer level, Throwable e) {
		if ((Config.LOG & level) != 0) {
			switch (level) {
				case 2: Log.d(tag, msg, e); break;
				case 4: Log.i(tag, msg, e); break;
				case 8: Log.w(tag, msg, e); break;
				case 16: Log.e(tag, msg, e);
			}
		}
	}
}
