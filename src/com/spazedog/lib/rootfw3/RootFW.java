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
import java.util.Map;
import java.util.WeakHashMap;

import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw3.extenders.BinaryExtender;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender;
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
	
	private static Map<String, FileExtender.File> mExtenderFileCollection = new WeakHashMap<String, FileExtender.File>();
	private static Map<String, PropertyExtender.File> mExtenderPropertyCollection = new WeakHashMap<String, PropertyExtender.File>();
	private static Map<String, BinaryExtender.Busybox> mExtenderBusyboxCollection = new WeakHashMap<String, BinaryExtender.Busybox>();
	private static Map<String, FilesystemExtender.Device> mExtenderDeviceCollection = new WeakHashMap<String, FilesystemExtender.Device>();
	private static Map<String, BinaryExtender.Binary> mExtenderBinaryCollection = new WeakHashMap<String, BinaryExtender.Binary>();
	private static Map<String, ProcessExtender.Process> mExtenderProcessCollection = new WeakHashMap<String, ProcessExtender.Process>();
	
	private static Map<String, ExtenderGroup> mExtenderSingles = new WeakHashMap<String, ExtenderGroup>();
	
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
	 *     A new {@link InstanceExtender.Instance} containing the shared shell connection
	 */
	public static InstanceExtender.Instance rootInstance() {
		if (!mExtenderSingles.containsKey("rootInstance")) {
			InstanceExtender.Instance extender = (InstanceExtender.Instance) InstanceExtender.Instance.getInstance(null, new ExtenderGroupTransfer((Object) true)).instance;
			
			mExtenderSingles.put("rootInstance", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (InstanceExtender.Instance) mExtenderSingles.get("rootInstance");
	}
	
	/**
	 * Get an user instance of RootFW.
	 * <br />
	 * This instance is globally shared instance. If no connection is currently opened, a new instance is created and a connection is auto established. Otherwise, the current instance is returned.
	 * <br />
	 * This makes it possible for multiple classes and methods to share the same shell connection.
	 * 
	 * @return
	 *     A new {@link InstanceExtender.Instance} instance containing the shared shell connection
	 */
	public static InstanceExtender.Instance userInstance() {
		if (!mExtenderSingles.containsKey("userInstance")) {
			InstanceExtender.Instance extender = (InstanceExtender.Instance) InstanceExtender.Instance.getInstance(null, new ExtenderGroupTransfer((Object) false)).instance;
			
			mExtenderSingles.put("userInstance", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (InstanceExtender.Instance) mExtenderSingles.get("userInstance");
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
	 * Return a new {@link ShellExtender.Shell} instance which can be used to communicate with the shell.
	 * 
	 * @see ShellExtender.Shell
	 */
	public ShellExtender.Shell shell() {
		return (ShellExtender.Shell) ShellExtender.Shell.getInstance(this, new ExtenderGroupTransfer( (BufferedReader) mInputStream, (DataOutputStream) mOutputStream )).instance;
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
	 * Return a new instance of the {@link FileExtender.File} class for the file defined in the argument. 
	 * <br />
	 * Note that this method keeps track of already existing instances. This means that if an instance already exist with the same file 
	 * (If it is being stored in another variable and therefore not yet been GC'd), a reference to the same instance is returned instead.
	 * 
	 * @see FileExtender.File
	 */
	public FileExtender.File file(String file) {
		java.io.File fileObject = new java.io.File(file);
		String filePath = FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!mExtenderFileCollection.containsKey(filePath)) {
			FileExtender.File extender = (FileExtender.File) FileExtender.File.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			mExtenderFileCollection.put(filePath, extender);
			
			return extender;
		}
		
		return mExtenderFileCollection.get(filePath);
	}
	
	/**
	 * Return a new instance of the {@link MemoryExtender.Memory} class.
	 *    
	 * @see MemoryExtender.Memory
	 */
	public MemoryExtender.Memory memory() {
		if (!mExtenderSingles.containsKey("memory")) {
			MemoryExtender.Memory extender = (MemoryExtender.Memory) MemoryExtender.Memory.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			mExtenderSingles.put("memory", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (MemoryExtender.Memory) mExtenderSingles.get("memory");
	}
	
	/**
	 * Return a new instance of the {@link PropertyExtender.Properties} class.
	 *    
	 * @see PropertyExtender.Properties
	 */
	public PropertyExtender.Properties property() {
		if (!mExtenderSingles.containsKey("properties")) {
			PropertyExtender.Properties extender = (PropertyExtender.Properties) PropertyExtender.Properties.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			mExtenderSingles.put("properties", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (PropertyExtender.Properties) mExtenderSingles.get("properties");
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
		String filePath = FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!mExtenderPropertyCollection.containsKey(filePath)) {
			PropertyExtender.File extender = (PropertyExtender.File) PropertyExtender.File.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			mExtenderPropertyCollection.put(filePath, extender);
			
			return extender;
		}
		
		return mExtenderPropertyCollection.get(filePath);
	}
	
	/**
	 * Return a new instance of the {@link FilesystemExtender.Filesystem} class.
	 *    
	 * @see FilesystemExtender.Filesystem
	 */
	public FilesystemExtender.Filesystem filesystem() {
		if (!mExtenderSingles.containsKey("filesystem")) {
			FilesystemExtender.Filesystem extender = (FilesystemExtender.Filesystem) FilesystemExtender.Filesystem.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			mExtenderSingles.put("filesystem", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (FilesystemExtender.Filesystem) mExtenderSingles.get("filesystem");
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
		String filePath = FileExtender.resolvePath( fileObject.getAbsolutePath() );
		
		if (!mExtenderDeviceCollection.containsKey(filePath)) {
			FilesystemExtender.Device extender = (FilesystemExtender.Device) FilesystemExtender.Device.getInstance(this, new ExtenderGroupTransfer( (Object) fileObject )).instance;
			
			mExtenderDeviceCollection.put(filePath, extender);
			
			return extender;
		}
		
		return mExtenderDeviceCollection.get(filePath);
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
		
		if (!mExtenderBusyboxCollection.containsKey(path)) {
			BinaryExtender.Busybox extender = (BinaryExtender.Busybox) BinaryExtender.Busybox.getInstance(this, new ExtenderGroupTransfer( (Object) path )).instance;
			
			mExtenderBusyboxCollection.put(path, extender);
			
			return extender;
		}
		
		return mExtenderBusyboxCollection.get(path);
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
		String name = binary.contains("/") ? binary.substring( binary.lastIndexOf("/")+1 ) : binary;
		
		if (!mExtenderBinaryCollection.containsKey(name)) {
			BinaryExtender.Binary extender = (BinaryExtender.Binary) BinaryExtender.Binary.getInstance(this, new ExtenderGroupTransfer( (Object) name )).instance;
			
			mExtenderBinaryCollection.put(name, extender);
			
			return extender;
		}
		
		return mExtenderBinaryCollection.get(name);
	}
	
	/**
	 * Return a new instance of the {@link ProcessExtender.Processes} class.
	 *    
	 * @see ProcessExtender.Processes
	 */
	public ProcessExtender.Processes processes() {
		if (!mExtenderSingles.containsKey("processes")) {
			ProcessExtender.Processes extender = (ProcessExtender.Processes) ProcessExtender.Processes.getInstance(this, new ExtenderGroupTransfer()).instance;
			
			mExtenderSingles.put("processes", (ExtenderGroup) extender);
			
			return extender;
		}
		
		return (ProcessExtender.Processes) mExtenderSingles.get("processes");
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
		if (!mExtenderProcessCollection.containsKey(process)) {
			ProcessExtender.Process extender = (ProcessExtender.Process) ProcessExtender.Process.getInstance(this, new ExtenderGroupTransfer( (Object) process )).instance;
			
			mExtenderProcessCollection.put(process, extender);
			
			return extender;
		}
		
		return mExtenderProcessCollection.get(process);
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
		if (!mExtenderProcessCollection.containsKey("pid:" + pid)) {
			ProcessExtender.Process extender = (ProcessExtender.Process) ProcessExtender.Process.getInstance(this, new ExtenderGroupTransfer( (Object) pid )).instance;
			
			mExtenderProcessCollection.put("pid:" + pid, extender);
			
			return extender;
		}
		
		return mExtenderProcessCollection.get("pid:" + pid);
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
