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
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw3.extenders.BinaryExtender;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender;
import com.spazedog.lib.rootfw3.extenders.PackageExtender;
import com.spazedog.lib.rootfw3.extenders.ProcessExtender;
import com.spazedog.lib.rootfw3.extenders.PropertyExtender;
import com.spazedog.lib.rootfw3.extenders.ShellExtender;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

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
	
	private static Map<String, ExtenderGroup> mExternderInstances = new WeakHashMap<String, ExtenderGroup>();
	
	/**
	 * A static configuration class where some configs can be changed for the framework.
	 * 
	 * <dl>
	 * <dt><span class="strong">Example:</span></dt>
	 * <dd><code><pre>RootFW.Config.PATH.add("/data/local/bin");</pre></code></dd>
	 * </dl>
	 */
	public final static class Config {
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
		
		/**
		 * This class defines connection configs.
		 */
		public final static class Connection {
			/**
			 * Amount of milliseconds the shell can take before it is killed.
			 */
			public static Integer TIMEOUT = 15000;
			
			/**
			 * Amount attempts to retry after a connection is killed.
			 * 2 attempts is at least recommended to fix issues with Kutch superuser daemon mode.
			 */
			public static Integer TRIES = 2;
		}
	}
	
	static {
		Config.BINARIES.add("busybox");
		Config.BINARIES.add("toolbox");
	}
	
	protected Boolean mRootUser = false;
	
	protected Process mShell = null;
	
	protected BufferedReader mInputStream;
	protected DataOutputStream mOutputStream;
	
	protected final Object mInstanceLock = new Object();
	protected final Object mConnectionLock = new Object();
	
	/**
	 * @deprecated
	 *     This method is deprecated as of version 1.1.0
	 *     
	 * @see #getSharedRoot()
	 */
	public static InstanceExtender.Instance rootInstance() {
		return (InstanceExtender.Instance) getSharedRoot();
	}
	
	/**
	 * @deprecated
	 *     This method is deprecated as of version 1.1.0
	 *     
	 * @see #getSharedUser()
	 */
	public static InstanceExtender.Instance userInstance() {
		return (InstanceExtender.Instance) getSharedUser();
	}
	
	/**
	 * Get an extended root instance of RootFW.
	 * <br />
	 * This instance is globally shared instance. If no connection is currently opened, a new instance is created and a connection is auto established. Otherwise, the current instance is returned.
	 * <br />
	 * This makes it possible for multiple classes and methods to share the same shell connection
	 */
	public static InstanceExtender.SharedRootFW getSharedRoot() {
		if (!RootFW.mExternderInstances.containsKey("InstanceExtender:Root")) {
			RootFW.mExternderInstances.put("InstanceExtender:Root", (ExtenderGroup) InstanceExtender.getInstance(null, new ExtenderGroupTransfer((Object) true)).instance);
		}
		
		RootFW.mExternderInstances.get("InstanceExtender:Root").onExtenderReconfigure();
		
		return (InstanceExtender.SharedRootFW) RootFW.mExternderInstances.get("InstanceExtender:Root");
	}
	
	/**
	 * Get an extended user instance of RootFW.
	 * <br />
	 * This instance is globally shared instance. If no connection is currently opened, a new instance is created and a connection is auto established. Otherwise, the current instance is returned.
	 * <br />
	 * This makes it possible for multiple classes and methods to share the same shell connection
	 */
	public static InstanceExtender.SharedRootFW getSharedUser() {
		if (!RootFW.mExternderInstances.containsKey("InstanceExtender:User")) {
			RootFW.mExternderInstances.put("InstanceExtender:User", (ExtenderGroup) InstanceExtender.getInstance(null, new ExtenderGroupTransfer((Object) false)).instance);
		}
		
		RootFW.mExternderInstances.get("InstanceExtender:User").onExtenderReconfigure();
		
		return (InstanceExtender.SharedRootFW) RootFW.mExternderInstances.get("InstanceExtender:User");
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
		synchronized (mInstanceLock) {
			if (!isConnected()) {
				ProcessBuilder builder;
				builder = new ProcessBuilder(mRootUser ? "su" : "sh");
				builder.redirectErrorStream(true);
	
				try {
					mShell = builder.start();
					
					mInputStream = new BufferedReader( new InputStreamReader(mShell.getInputStream()) );
					mOutputStream = new DataOutputStream(mShell.getOutputStream());
					
					if(isConnected()) {
						if (Config.PATH.size() > 0) {
							shell("PATH=\"" + TextUtils.join(":", Config.PATH) + ":$PATH\"");
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
	}
	
	/**
	 * Close the connection to the shell
	 */
	public void disconnect() {
		synchronized (mInstanceLock) {
			if (mShell != null) {
				/* If mShell is not null, then the connection should be active. 
				 * If not, then something is wrong and we will destroy it instead.
				 */
				if(isConnected() && shell().run("exit 0").wasSuccessful()) {
					try {
						mInputStream.close();
						mOutputStream.close();
						
					} catch (IOException e) {}
					
					mShell.destroy();
					mShell = null;
					
				} else {
					destroy();
				}
			}
		}
	}
	
	/**
	 * Destroys a shell connection. 
	 * It's better to use {{@link #disconnect()} to close the connection in a more clean manner. 
	 */
	public void destroy() {
		synchronized (mInstanceLock) {
			if (mShell != null) {
				mInputStream = null;
				mOutputStream = null;
				
				mShell.destroy();
				mShell = null;
			}
		}
	}
	
	/**
	 * This is used internally by extenders to get the connection input stream.
	 */
	public BufferedReader getInputStream(Object lock) {
		return lock == mConnectionLock ? 
				mInputStream : null;
	}
	
	/**
	 * This is used internally by extenders to get the connection output stream.
	 */
	public DataOutputStream getOutputStream(Object lock) {
		return lock == mConnectionLock ? 
				mOutputStream : null;
	}
	
	/**
	 * Check whether a shell connection has been established
	 *    
	 * @return
	 *     <code>True</code> if the connection is open
	 */
	public Boolean isConnected() {
		synchronized (mInstanceLock) {
			return mShell != null && "connected".equals( shell("echo 'connected'") );
		}
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
	 * Return a new {@link ShellExtender.Shell} instance which can be used to communicate with the shell.
	 * 
	 * @see ShellExtender.Shell
	 */
	public ShellExtender.Shell shell() {
		return (ShellExtender.Shell) ShellExtender.Shell.getInstance(this, new ExtenderGroupTransfer( mConnectionLock )).instance;
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
	 * Return a new instance of the {@link FileExtender.FileUtil} class. 
	 * 
	 * @see FileExtender.FileUtil
	 */
	public FileExtender.FileUtil file() {
		if (!RootFW.mExternderInstances.containsKey("FileExtender.FileUtil")) {
			FileExtender.FileUtil extender = (FileExtender.FileUtil) FileExtender.FileUtil.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("FileExtender.FileUtil", extender);
			
			return extender;
		}
		
		return (FileExtender.FileUtil) RootFW.mExternderInstances.get("FileExtender.FileUtil");
	}
	
	/**
	 * Return a new instance of the {@link FileExtender.File} class for the file defined in the argument. 
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see FileExtender.File
	 */
	public FileExtender.File file(String file) {
		java.io.File fileObject = new java.io.File(file);
		String name = "FileExtender.File:" + FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			FileExtender.File extender = (FileExtender.File) FileExtender.File.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (FileExtender.File) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link MemoryExtender.Memory} class.
	 *    
	 * @see MemoryExtender.Memory
	 */
	public MemoryExtender.Memory memory() {
		if (!RootFW.mExternderInstances.containsKey("MemoryExtender.Memory")) {
			MemoryExtender.Memory extender = (MemoryExtender.Memory) MemoryExtender.Memory.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("MemoryExtender.Memory", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (MemoryExtender.Memory) RootFW.mExternderInstances.get("MemoryExtender.Memory");
	}
	
	/**
	 * Return a new instance of the {@link MemoryExtender.Device} class for the device defined in the argument. 
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see MemoryExtender.Device
	 */
	public MemoryExtender.Device memory(String device) {
		java.io.File fileObject = new java.io.File(device);
		String name = "MemoryExtender.Device:" + FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			MemoryExtender.Device extender = (MemoryExtender.Device) MemoryExtender.Device.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (MemoryExtender.Device) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link PropertyExtender.Properties} class.
	 *    
	 * @see PropertyExtender.Properties
	 */
	public PropertyExtender.Properties property() {
		if (!RootFW.mExternderInstances.containsKey("PropertyExtender.Properties")) {
			PropertyExtender.Properties extender = (PropertyExtender.Properties) PropertyExtender.Properties.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("PropertyExtender.Properties", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (PropertyExtender.Properties) RootFW.mExternderInstances.get("PropertyExtender.Properties");
	}
	
	/**
	 * Return a new instance of the {@link PropertyExtender.File} class for the file defined in the argument. 
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see PropertyExtender.File
	 */
	public PropertyExtender.File property(String file) {
		java.io.File fileObject = new java.io.File(file);
		String name = "PropertyExtender.File:" + FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			PropertyExtender.File extender = (PropertyExtender.File) PropertyExtender.File.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (PropertyExtender.File) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link FilesystemExtender.Filesystem} class.
	 *    
	 * @see FilesystemExtender.Filesystem
	 */
	public FilesystemExtender.Filesystem filesystem() {
		if (!RootFW.mExternderInstances.containsKey("FilesystemExtender.Filesystem")) {
			FilesystemExtender.Filesystem extender = (FilesystemExtender.Filesystem) FilesystemExtender.Filesystem.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("FilesystemExtender.Filesystem", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (FilesystemExtender.Filesystem) RootFW.mExternderInstances.get("FilesystemExtender.Filesystem");
	}
	
	/**
	 * Return a new FilesystemExtender.Device instance which can be used to alter a device mount state, get mount information etc.
	 * 
	 * @param device
	 *     The device or mount location to work with
	 *    
	 * @return
	 *     A new FilesystemExtender.Device instance
	 */
	public FilesystemExtender.Device filesystem(String device) {
		java.io.File fileObject = new java.io.File(device);
		String name = "FilesystemExtender.Device:" + FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			FilesystemExtender.Device extender = (FilesystemExtender.Device) FilesystemExtender.Device.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (FilesystemExtender.Device) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link BinaryExtender.Busybox} class set for the first busybox binary in the PATH environment variable. 
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see BinaryExtender.Busybox
	 * @see #busybox(String)
	 */
	public BinaryExtender.Busybox busybox() {
		return busybox("busybox");
	}
	
	/**
	 * Return a new instance of the {@link BinaryExtender.Busybox} class set for the defined busybox binary.
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see BinaryExtender.Busybox
	 * @see #busybox()
	 */
	public BinaryExtender.Busybox busybox(String binary) {
		String path = binary.contains("/") ? FileExtender.resolvePath( new java.io.File(binary).getAbsolutePath() ) : binary;
		String name = "BinaryExtender.Busybox:" + path;
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			BinaryExtender.Busybox extender = (BinaryExtender.Busybox) BinaryExtender.Busybox.getInstance(this, new ExtenderGroupTransfer( (Object) path )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (BinaryExtender.Busybox) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link BinaryExtender.Binary} class set for the defined binary.
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same binary 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see BinaryExtender.Binary
	 */
	public BinaryExtender.Binary binary(String binary) {
		String binaryName = binary.contains("/") ? binary.substring( binary.lastIndexOf("/")+1 ) : binary;
		String name = "BinaryExtender.Binary:" + binary;
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			BinaryExtender.Binary extender = (BinaryExtender.Binary) BinaryExtender.Binary.getInstance(this, new ExtenderGroupTransfer( (Object) binaryName )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (BinaryExtender.Binary) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link ProcessExtender.Processes} class.
	 *    
	 * @see ProcessExtender.Processes
	 */
	public ProcessExtender.Processes processes() {
		if (!RootFW.mExternderInstances.containsKey("ProcessExtender.Processes")) {
			ProcessExtender.Processes extender = (ProcessExtender.Processes) ProcessExtender.Processes.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("ProcessExtender.Processes", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (ProcessExtender.Processes) RootFW.mExternderInstances.get("ProcessExtender.Processes");
	}

	/**
	 * Return a new instance of the {@link ProcessExtender.Process} class set for the defined process.
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same process 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see ProcessExtender.Process
	 */
	public ProcessExtender.Process process(String process) {
		String name = "ProcessExtender.Process:" + process;
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			ProcessExtender.Process extender = (ProcessExtender.Process) ProcessExtender.Process.getInstance(this, new ExtenderGroupTransfer( (Object) process )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (ProcessExtender.Process) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link ProcessExtender.Process} class set for the defined process.
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same process id 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see ProcessExtender.Process
	 */
	public ProcessExtender.Process process(Integer pid) {
		String name = "ProcessExtender.Process:" + pid;
		
		if (!RootFW.mExternderInstances.containsKey(name)) {
			ProcessExtender.Process extender = (ProcessExtender.Process) ProcessExtender.Process.getInstance(this, new ExtenderGroupTransfer( (Object) pid )).instance;
			
			RootFW.mExternderInstances.put(name, extender);
			
			return extender;
		}
		
		return (ProcessExtender.Process) RootFW.mExternderInstances.get(name);
	}
	
	/**
	 * Return a new instance of the {@link PackageExtender.Packages} class.
	 *    
	 * @see PackageExtender.Packages
	 */
	public PackageExtender.Packages packages() {
		if (!RootFW.mExternderInstances.containsKey("PackageExtender.Packages")) {
			PackageExtender.Packages extender = (PackageExtender.Packages) PackageExtender.Packages.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			RootFW.mExternderInstances.put("PackageExtender.Packages", extender);
			
			return extender;
		}
		
		return (PackageExtender.Packages) RootFW.mExternderInstances.get("PackageExtender.Packages");
	}
	
	/**
	 * Return a new instance of the {@link ProcessExtender.Power} class set for the defined process.
	 * 
	 * @see ProcessExtender.Power
	 */
	public ProcessExtender.Power power() {
		return (ProcessExtender.Power) ProcessExtender.Power.getInstance(this, new ExtenderGroupTransfer()).instance;
	}
	
	/**
	 * A small method to get the shell environment variable.
	 */
	public String[] getEnvironmentVariable() {
		String variable = shell().run("echo $PATH").getLine();
		
		if (variable != null) {
			return variable.split(":");
		}
		
		return null;
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
	
	/**
	 * This class is used by RootFW when getting new instances of {@link ExtenderGroup} classes.
	 * The constructor of this class is private as this is not meant to be used outside of the RootFW internal environment.
	 */
	public static class ExtenderGroupTransfer {
		public ExtenderGroup instance;
		public Object[] arguments;
		
		private ExtenderGroupTransfer() {}
		
		private ExtenderGroupTransfer(Object... objects) {
			arguments = objects;
		}
		
		public ExtenderGroupTransfer setInstance(ExtenderGroup extender) {
			instance = extender;
			
			return this;
		}
	}
}
