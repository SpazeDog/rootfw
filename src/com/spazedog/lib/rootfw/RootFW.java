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

package com.spazedog.lib.rootfw;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.extender.Binary;
import com.spazedog.lib.rootfw.extender.Busybox;
import com.spazedog.lib.rootfw.extender.File;
import com.spazedog.lib.rootfw.extender.Filesystem;
import com.spazedog.lib.rootfw.extender.Memory;
import com.spazedog.lib.rootfw.extender.Processes;
import com.spazedog.lib.rootfw.extender.Property;
import com.spazedog.lib.rootfw.extender.Shell;
import com.spazedog.lib.rootfw.extender.Utils;

public final class RootFW {
	public final static String TAG = "RootFW";
	
	public final static Integer E_DEBUG = 2;
	public final static Integer E_INFO = 4;
	public final static Integer E_WARNING = 8;
	public final static Integer E_ERROR = 16;
	
	/**
	 * Define which log levels should be enabled.
	 * <p/>
	 * Default: <code>E_ERROR|E_WARNING</code>
	 */
	public static Integer LOG = E_ERROR|E_WARNING;
	
	/**
	 * This property can be used to extend the shells PATH variable. Just create a new String[] containing the paths that you need.
	 * <p/>
	 * The original locations will not be deleted, but your new once will have main priority. Just note that this needs to be set BEFORE you create the RootFW instance.
	 */
	public static String[] PATH = null;
	
	private static Map<String, RootFW> oInstance = new HashMap<String, RootFW>();
	
	private Process mProcess = null;
	private Boolean mRootAccount = false;
	private Boolean mIsCopy = false;
	private String mName = null;
	private Object mLock = new Object();
	
	/**
	 * An instance of the Shell extender class
	 */
	public final Shell shell = new Shell(this);
	
	/**
	 * An instance of the File extender class
	 */
	public final File file = new File(this);
	
	/**
	 * An instance of the Busybox extender class
	 */
	public final Busybox busybox = new Busybox(this);
	
	/**
	 * An instance of the Binary extender class
	 */
	public final Binary binary = new Binary(this);
	
	/**
	 * An instance of the Processes extender class
	 */
	public final Processes processes = new Processes(this);
	
	/**
	 * An instance of the Filesystem extender class
	 */
	public final Filesystem filesystem = new Filesystem(this);
	
	/**
	 * An instance of the Memory extender class
	 */
	public final Memory memory = new Memory(this);
	
	/**
	 * An instance of the Utils extender class
	 */
	public final Utils utils = new Utils(this);
	
	/**
	 * An instance of the Property extender class
	 */
	public final Property property = new Property(this);
	
	/**
	 * This is a hack which allows the class it self to create an 
	 * instance with no shell connection.
	 */
	private RootFW(Integer hack) {}
	
	/**
	 * Create a new instance of RootFW and establish a root connection to the shell
	 */
	public RootFW() {
		this(true);
	}
	
	/**
	 * Create a new instance of RootFW and establish a connection to the shell
	 *    
	 * @param aRoot
	 *     Whether to connect as root or as a regular user
	 */
	public RootFW(Boolean aRoot) {
		mRootAccount = aRoot;
		
		establish();
	}
	
	/**
	 * Internal method for establishing a connection
	 *    
	 * @return
	 *     <code>True</code> on successful connection
	 */
	private Boolean establish() {
		ProcessBuilder lBuilder = new ProcessBuilder( mRootAccount ? "su" : "sh" );
		lBuilder.redirectErrorStream(true);
		
		try {
			log(TAG, "Starting a new process");
			
			mProcess = lBuilder.start();
			
			if (PATH != null && PATH.length > 0) {
				String lPathVariable = "$PATH";
				
				for (int i=0; i < PATH.length; i++) {
					lPathVariable = PATH[i] + ":" + lPathVariable;
				}
				
				shell.execute("PATH=\"" + lPathVariable + "\"");
			}
			
			return true;
			
		} catch (Throwable e) { log(TAG, "Failed while starting the new process", E_ERROR, e); }
		
		return false;
	}
	
	/**
	 * See <code>instance(String aName, Boolean aRoot, Boolean aOwner)</code>
	 */
	public static RootFW instance(String aName) {
		return instance(aName, oInstance.get(aName + ":user") == null, false);
	}
	
	/**
	 * See <code>instance(String aName, Boolean aRoot, Boolean aOwner)</code>
	 */
	public static RootFW instance(String aName, Boolean aRoot) {
		return instance(aName, aRoot, false);
	}
	
	/**
	 * This is the same as using "RootFW(Boolean aRoot)", only this one will
	 * create a named instance. If an instance with the parsed name already exist,
	 * a clone will be provided instead using the same shell connection as the main instance.
	 * This will allow you to reuse connections across classes and methods without the
	 * need to manually parse them.
	 * <p/>
	 * Note: Clones are not allows to close a connection. So calling "close()" from a cloned
	 * instance will not close the shell connection. It will not be closed until "close()"
	 * is called from the main instance. 
	 * 
	 * @param aName
	 *     The name of the instance
	 *    
	 * @param aRoot
	 *     Whether to connect as root or as a regular user
	 *     
	 * @param aOwner
	 *     Whether to return the owner instance (Instead of a copy)
	 *    
	 * @return
	 *     Returns a new or cloned instance of RootFW
	 */
	public static RootFW instance(String aName, Boolean aRoot, Boolean aOwner) {
		String lName = aName + (aRoot ? ":root" : ":user");
		RootFW lInstance = null;
		
		if (oInstance.get(lName) == null || !oInstance.get(lName).connected(true)) {
			log(TAG + ".instance", "Creating new instance " + lName);
			
			lInstance = new RootFW(aRoot);
			lInstance.mName = aName;
			
			oInstance.put(lName, lInstance);
			
		} else if(aOwner) {
			lInstance = oInstance.get(lName);

		} else {
			log(TAG + ".instance", "Cloning the instance " + lName);
			
			lInstance = new RootFW(0);
			lInstance.mRootAccount = aRoot;
			lInstance.mName = lName;
			lInstance.mIsCopy = true;
			
			/* Give the clone access to the connected process */
			lInstance.mProcess = oInstance.get(lName).mProcess;
			lInstance.mLock = oInstance.get(lName).mLock;
		}
		
		return lInstance;
	}
	
	/**
	 * Close the shell connection.
	 * <p/>
	 * Note: that this will be ignored if called from a cloned instance.
	 * Only the main instance are allowed to close the shell connection.
	 */
	public void close() {
		if (!mIsCopy) {
			try {
				shell.execute("exit");
				mProcess.destroy();
				
			} catch (Throwable e) {}
			
			if (mName != null) {
				String lName = mName + (mRootAccount ? ":root" : ":user");
				
				if (oInstance.get(mName) != null) {
					oInstance.remove(mName);
				}
				
				log(TAG + ".close", "Closing instance " + lName);
				
			} else {
				log(TAG + ".close", "Closing instance");
			}
			
			mProcess = null;
		}
	}
	
	/**
	 * See <code>connected(Boolean aForceReconnect)</code>
	 */
	public Boolean connected() {
		return connected(false);
	}
	
	/**
	 * Check to see if the instance has successfully established
	 * a connection to the shell. If you created the instance with root
	 * permissions, it will also check to make sure that the connection
	 * has these permissions.
	 * 
	 * @param aForceReconnect
	 *     Whether to force a reconnect if the connection is not established
	 * 
	 * @return
	 *     Whether or not a shell connection has been established
	 */
	public Boolean connected(Boolean aForceReconnect) {
		if (mProcess != null) {
			Integer lTries = aForceReconnect ? 2 : 1;
			String lName = mName + (mRootAccount ? ":root" : ":user");
			
			for (int i=0; i < lTries; i++) {
				if (i == 0 || (oInstance.get(lName) != null && oInstance.get(lName).establish())) {
					ShellResult lResult;
					
					if (mRootAccount) {
						// Return true even though the 'id' command is missing as we are still connected using 'su'
						lResult = shell.execute("id", "echo 'uid=0'");
						
						if (lResult != null && lResult.output().line() != null && lResult.output().line().contains("uid=0")) {
							return true;
						}
						
					} else { 
						lResult = shell.execute("echo 'uid=unknown'");
						
						if (lResult != null && lResult.output().line() != null && lResult.output().line().contains("uid=unknown")) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Return the process belonging to the shell connection
	 * 
	 * @return
	 *     The shell process of the instance
	 */
	public Process process() {
		return mProcess;
	}
	
	/**
	 * Return the lock object which can tell whether the connection is being used or not
	 * 
	 * @return
	 *     The lock object
	 */
	public Object lock() {
		return mLock;
	}
	
	/**
	 * See <code>log(String aTag, String aMsg, Integer aLevel, Throwable e)</code>
	 */
	public static void log(String aTag, String aMsg) {
		log(aTag, aMsg, E_INFO, null);
	}
	
	/**
	 * See <code>log(String aTag, String aMsg, Integer aLevel, Throwable e)</code>
	 */
	public static void log(String aTag, String aMsg, Integer aLevel) {
		log(aTag, aMsg, aLevel, null);
	}
	
	/**
	 * Generate logcat output.
	 * The method will only generate logcat output for levels
	 * enabled in <code>com.spazedog.rootfw.RootFW.LOG</code>
	 * 
	 * @param aTag
	 *     The name of the logger
	 *    
	 * @param aMsg
	 *     The message to output
	 *    
	 * @param aLevel
	 *     The level of the log (<code>E_INFO</code>, <code>E_WARNING</code>, <code>E_ERROR</code>, <code>E_DEBUG</code>)
	 *    
	 * @param e
	 *     A <code>Throwable</code> error output
	 */
	public static void log(String aTag, String aMsg, Integer aLevel, Throwable e) {
		if ((LOG & aLevel) != 0) {
			String tag = aTag == null ? TAG : aTag;
			
			switch (aLevel) {
				case 2: Log.d(tag, aMsg, e); break;
				case 4: Log.i(tag, aMsg, e); break;
				case 8: Log.w(tag, aMsg, e); break;
				case 16: Log.e(tag, aMsg, e); break;
			}
		}
	}
}
