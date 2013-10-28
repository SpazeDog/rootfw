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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw3.extenders.BinaryExtender;
import com.spazedog.lib.rootfw3.extenders.FileExtender;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender.SharedRootFW;
import com.spazedog.lib.rootfw3.extenders.MemoryExtender;
import com.spazedog.lib.rootfw3.extenders.PackageExtender;
import com.spazedog.lib.rootfw3.extenders.ProcessExtender;
import com.spazedog.lib.rootfw3.extenders.PropertyExtender;
import com.spazedog.lib.rootfw3.extenders.ShellExtender;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class RootFW {
	public final static String TAG = "RootFW";
	
	private static Integer oCounter = 1;
	
	public final static Integer E_DEBUG = (oCounter *= 2);
	public final static Integer E_INFO = (oCounter *= 2);
	public final static Integer E_WARNING = (oCounter *= 2);
	public final static Integer E_ERROR = (oCounter *= 2);
	
	public final static Integer BROADCAST_RETRIEVE = (oCounter *= 2);
	public final static Integer BROADCAST_EXTENDER = (oCounter *= 2);
	
	private static SharedRootFW oSharedRootInstance;
	private static SharedRootFW oSharedUserInstance;
	
	private static final AtomicLong oIdGenerator = new AtomicLong(0);
	
	private final Long mInstanceId = RootFW.oIdGenerator.incrementAndGet();
	private final String mInstanceName = "reference-" + mInstanceId;
	
	private final static Object oClassLock = new Object();
	protected final Object mInstanceLock = new Object();
	protected final Object mConnectionLock = new Object();
	
	private final static Map<String, RootFW> oRootFWInstances = new WeakHashMap<String, RootFW>();
	protected final Map<String, ExtenderGroup> mExternderInstances = new WeakHashMap<String, ExtenderGroup>();
	private final static List<ConnectionListener> oListenerInstances = new ArrayList<ConnectionListener>();
	private final static List<ConnectionController> oControllerInstances = new ArrayList<ConnectionController>();
	
	protected Process mConnectionProcess;
	protected Boolean mConnectionIsRoot;
	protected Boolean mConnectionEstablished = false;
	protected ShellInputStream mConnectionInputStream;
	protected ShellOutputStream mConnectionOutputStream;
	
	protected ExtenderGroup mTmpExtender;
	
	/**
	 * Add a {@link #ConnectionListener} to the RootFW. This will allow you to 
	 * get a notice every time the connection state changes on a RootFW instance. 
	 */
	public static ConnectionListener addConnectionListener(ConnectionListener listener) {
		synchronized (RootFW.oClassLock) {
			if (!RootFW.oListenerInstances.contains(listener)) {
				RootFW.oListenerInstances.add(listener);
			}
			
			return listener;
		}
	}
	
	/**
	 * Remove a {@link #ConnectionListener} from the stack.
	 * 
	 * @see #addConnectionListener(ConnectionListener)
	 */
	public static void removeConnectionListener(ConnectionListener listener) {
		synchronized (RootFW.oClassLock) {
			RootFW.oListenerInstances.remove(listener);
		}
	}
	
	/**
	 * Add a {@link #ConnectionController} to the RootFW.
	 * This will allow you to control whether or not an instance is allowed to connect or disconnect.
	 */
	public static ConnectionController addConnectionController(ConnectionController controller) {
		synchronized (RootFW.oClassLock) {
			if (!RootFW.oControllerInstances.contains(controller)) {
				RootFW.oControllerInstances.add(controller);
			}
			
			return controller;
		}
	}
	
	/**
	 * Remove a {@link #ConnectionController} from the stack.
	 * 
	 * @see #addConnectionController(controller)
	 */
	public static void removeConnectionController(ConnectionController controller) {
		synchronized (RootFW.oClassLock) {
			RootFW.oControllerInstances.remove(controller);
		}
	}
	
	/**
	 * @deprecated
	 * This method is deprecated as of version 1.1.0
	 *
	 * @see #getSharedRoot()
	 */
	public static InstanceExtender.Instance rootInstance() {
		return (InstanceExtender.Instance) getSharedRoot();
	}
	
	/**
	 * @deprecated
	 * This method is deprecated as of version 1.1.0
	 *
	 * @see #getSharedUser()
	 */
	public static InstanceExtender.Instance userInstance() {
		return (InstanceExtender.Instance) getSharedUser();
	}
	
	/**
	 * This will return a shared and extended version of RootFW connected as root. 
	 * This will return the same instance across all classes and methods which will allow you to use one single 
	 * instance across all of your code and avoid constant connects and disconnects. 
	 * 
	 * @see getSharedUser()
	 */
	public static InstanceExtender.SharedRootFW getSharedRoot() {
		synchronized (RootFW.oClassLock) {
			if (RootFW.oSharedRootInstance == null) {
				RootFW.oSharedRootInstance = (SharedRootFW) InstanceExtender.getInstance(null, null, new ExtenderGroupTransfer((Object) true)).instance;
			}
			
			((ExtenderGroup) RootFW.oSharedRootInstance).onBroadcastReceive(BROADCAST_RETRIEVE, null);
			
			return RootFW.oSharedRootInstance;
		}
	}
	
	/**
	 * This will return a shared and extended version of RootFW connected as a regular user. 
	 * This will return the same instance across all classes and methods which will allow you to use one single 
	 * instance across all of your code and avoid constant connects and disconnects. 
	 * 
	 * @see getSharedRoot()
	 */
	public static InstanceExtender.SharedRootFW getSharedUser() {
		synchronized (RootFW.oClassLock) {
			if (RootFW.oSharedUserInstance == null) {
				RootFW.oSharedUserInstance = (SharedRootFW) InstanceExtender.getInstance(null, null, new ExtenderGroupTransfer((Object) false)).instance;
			}
			
			((ExtenderGroup) RootFW.oSharedUserInstance).onBroadcastReceive(BROADCAST_RETRIEVE, null);
			
			return RootFW.oSharedUserInstance;
		}
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
	 * @param useRoot
	 * 		Whether to create a root shell or a regular user shell
	 */
	public RootFW(Boolean useRoot) {
		mConnectionIsRoot = useRoot;
	}
	
	/**
	 * Establish a connection to the shell
	 */
	public Boolean connect() {
		synchronized (mInstanceLock) {
			if (!mConnectionEstablished) {
				try {
					synchronized (oClassLock) {
						for (ConnectionController controller : RootFW.oControllerInstances) {
							if(!controller.onConnectionEstablishing(this)) {
								throw new Throwable();
							}
						}
					}
					
					ProcessBuilder builder;
					builder = new ProcessBuilder(mConnectionIsRoot ? "su" : "sh");
					builder.redirectErrorStream(true);
				
					mConnectionProcess = builder.start();
					mConnectionInputStream = new ShellInputStream(mConnectionProcess.getInputStream());
					mConnectionOutputStream = new ShellOutputStream(mConnectionProcess.getOutputStream());
					mConnectionEstablished = "connected".equals( shell("echo 'connected'") );
					
					if(mConnectionEstablished) {
						if (Config.PATH.size() > 0) {
							shell("PATH=\"" + TextUtils.join(":", Config.PATH) + ":$PATH\"");
						}
						
						RootFW.oRootFWInstances.put(mInstanceName, this);
						
						synchronized (oClassLock) {
							for (ConnectionListener listener : RootFW.oListenerInstances) {
								listener.onConnectionEstablished(this);
							}
						}
						
						return true;
						
					} else if (mConnectionProcess != null) {
						throw new Throwable();
					}
					
				} catch (Throwable e) {
					synchronized (RootFW.oClassLock) {
						for (ConnectionListener listener : RootFW.oListenerInstances) {
							listener.onConnectionFailed(this);
						}
					}
					
					if (mConnectionProcess != null) {
						mConnectionProcess.destroy();
					}
				}
				
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
			if (mConnectionProcess != null) {
				synchronized (oClassLock) {
					for (ConnectionController controller : RootFW.oControllerInstances) {
						if(!controller.onConnectionClosing(this)) {
							return;
						}
					}
				}
				
				/* If mShell is not null, then the connection should be active. 
				 * If not, then something is wrong and we will destroy it instead.
				 */
				if(mConnectionEstablished && shell().run("exit 0").wasSuccessful()) {
					try {
						mConnectionInputStream.close();
						mConnectionOutputStream.close();
						
					} catch (IOException e) {}
					
					mConnectionProcess.destroy();
					
					cleanup();
					
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
			if (mConnectionProcess != null) {
				mConnectionProcess.destroy();
				
				cleanup();
			}
		}
	}
	
	/**
	 * Internal method to clean up after disconnecting from the shell
	 */
	protected void cleanup() {
		synchronized (mInstanceLock) {
			mConnectionProcess = null;
			mConnectionInputStream = null;
			mConnectionOutputStream = null;
			mConnectionEstablished = false;
			
			oRootFWInstances.remove(mInstanceName);
			mExternderInstances.clear();
			
			synchronized (RootFW.oClassLock) {
				for (ConnectionListener listener : RootFW.oListenerInstances) {
					listener.onConnectionClosed(this);
				}
			}
		}
	}
	
	/**
	 * Internal method used by extenders to send broadcasts to one another
	 */
	public void _sendGlobalBroadcast(Object lock, Integer broadcastType, Bundle arguments) {
		if (lock == mInstanceLock) {
			for (String instanceKey : oRootFWInstances.keySet()) {
				RootFW instance = oRootFWInstances.get(instanceKey);
						
				try {
					for (String extenderKey : instance.mExternderInstances.keySet()) {
						instance.mExternderInstances.get(extenderKey).onBroadcastReceive(broadcastType, arguments);
					}
					
				} catch(ConcurrentModificationException e) {}
			}
		}
	}
	
	/**
	 * Internal method used by extenders to send broadcasts to one another
	 */
	public void _sendLocalBroadcast(Object lock, Integer broadcastType, Bundle arguments) {
		if (lock == mInstanceLock) {
			for (String key : mExternderInstances.keySet()) {
				mExternderInstances.get(key).onBroadcastReceive(broadcastType, arguments);
			}
		}
	}
	
	/**
	 * Internal method used by extenders to get the shell InputStream
	 */
	public ShellInputStream _getInputStream(Object lock) {
		return lock == mInstanceLock ? 
				mConnectionInputStream : null;
	}
	
	/**
	 * Internal method used by extenders to get the shell OutputStream
	 */
	public ShellOutputStream _getOutputStream(Object lock) {
		return lock == mInstanceLock ? 
				mConnectionOutputStream : null;
	}
	
	/**
	 * Get the unique id for this instance
	 */
	public Long getInstanceId() {
		return mInstanceId;
	}
	
	/**
	 * Check whether a shell connection has been established
	 */
	public Boolean isConnected() {
		synchronized (mInstanceLock) {
			return mConnectionEstablished;
		}
	}
	
	/**
	 * Check whether this shell is a root shell or a reglar user shell
	 */
	public Boolean isRoot() {
		return mConnectionIsRoot;
	}
	
	/**
	 * Internal extender control method
	 */
	private Boolean checkExtender(String name) {
		synchronized (mInstanceLock) {
			return mExternderInstances.containsKey(name)
					&& (mTmpExtender = mExternderInstances.get(name)) != null;
		}
	}
	
	/**
	 * Internal extender control method
	 */
	private ExtenderGroup addExtender(String name, ExtenderGroup extender) {
		synchronized (mInstanceLock) {
			mExternderInstances.put(name, extender);
			
			extender.onBroadcastReceive(BROADCAST_RETRIEVE, null);
			
			return extender;
		}
	}
	
	/**
	 * Internal extender control method
	 */
	private ExtenderGroup getExtender(String name) {
		synchronized (mInstanceLock) {
			ExtenderGroup extender = mTmpExtender != null ? mTmpExtender : mExternderInstances.get(name);
			
			mTmpExtender = null;
			extender.onBroadcastReceive(BROADCAST_RETRIEVE, null);
			
			return extender;
		}
	}
	
	/**
	 * Get an instance of {@link ShellExtender.Shell}
	 */
	public ShellExtender.Shell shell() {
		return (ShellExtender.Shell) ShellExtender.Shell.getInstance(this, mInstanceLock, new ExtenderGroupTransfer(mConnectionLock)).instance;
	}
	
	/**
	 * Execute one shell command and return the last output line
	 */
	public String shell(String command) {
		return shell().run(command).getLine();
	}
	
	/**
	 * Get an instance of {@link FileExtender.FileUtil}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public FileExtender.FileUtil file() {
		synchronized (mInstanceLock) {
			return (FileExtender.FileUtil) (checkExtender("FileExtender.FileUtil") ? 
					getExtender("FileExtender.FileUtil") : 
						addExtender("FileExtender.FileUtil", FileExtender.FileUtil.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link FileExtender.File}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same file will not provide multiple instances.
	 */
	public FileExtender.File file(String file) {
		synchronized (mInstanceLock) {
			String path = Common.resolveFilePath(file);
			
			return (FileExtender.File) (checkExtender("FileExtender.File:" + path) ? 
					getExtender("FileExtender.File:" + path) : 
						addExtender("FileExtender.File:" + path, FileExtender.File.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) file )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link MemoryExtender.Memory}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public MemoryExtender.Memory memory() {
		synchronized (mInstanceLock) {
			return (MemoryExtender.Memory) (checkExtender("MemoryExtender.Memory") ? 
					getExtender("MemoryExtender.Memory") : 
						addExtender("MemoryExtender.Memory", MemoryExtender.Memory.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link MemoryExtender.Device}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same device will not provide multiple instances.
	 */
	public MemoryExtender.Device memory(String device) {
		synchronized (mInstanceLock) {
			String path = Common.resolveFilePath(device);
			
			return (MemoryExtender.Device) (checkExtender("MemoryExtender.Device:" + path) ? 
					getExtender("MemoryExtender.Device:" + path) : 
						addExtender("MemoryExtender.Device:" + path, MemoryExtender.Device.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) path )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link PropertyExtender.Properties}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public PropertyExtender.Properties property() {
		synchronized (mInstanceLock) {
			return (PropertyExtender.Properties) (checkExtender("PropertyExtender.Properties") ? 
					getExtender("PropertyExtender.Properties") : 
						addExtender("PropertyExtender.Properties", PropertyExtender.Properties.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link PropertyExtender.File}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same file will not provide multiple instances.
	 */
	public PropertyExtender.File property(String file) {
		synchronized (mInstanceLock) {
			String path = Common.resolveFilePath(file);
			
			return (PropertyExtender.File) (checkExtender("PropertyExtender.File:" + path) ? 
					getExtender("PropertyExtender.File:" + path) : 
						addExtender("PropertyExtender.File:" + path, PropertyExtender.File.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) path )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link FilesystemExtender.Filesystem}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public FilesystemExtender.Filesystem filesystem() {
		synchronized (mInstanceLock) {
			return (FilesystemExtender.Filesystem) (checkExtender("FilesystemExtender.Filesystem") ? 
					getExtender("FilesystemExtender.Filesystem") : 
						addExtender("FilesystemExtender.Filesystem", FilesystemExtender.Filesystem.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link FilesystemExtender.Device}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same device will not provide multiple instances.
	 */
	public FilesystemExtender.Device filesystem(String device) {
		synchronized (mInstanceLock) {
		String path = Common.resolveFilePath(device);
		
		return (FilesystemExtender.Device) (checkExtender("FilesystemExtender.Device:" + path) ? 
				getExtender("FilesystemExtender.Device:" + path) : 
					addExtender("FilesystemExtender.Device:" + path, FilesystemExtender.Device.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) path )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link BinaryExtender.Busybox}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public BinaryExtender.Busybox busybox() {
		synchronized (mInstanceLock) {
			return (BinaryExtender.Busybox) (checkExtender("BinaryExtender.Busybox") ? 
					getExtender("BinaryExtender.Busybox") : 
						addExtender("BinaryExtender.Busybox", BinaryExtender.Busybox.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) "busybox" )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link BinaryExtender.Busybox}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same file will not provide multiple instances.
	 */
	public BinaryExtender.Busybox busybox(String file) {
		synchronized (mInstanceLock) {
			String path = Common.resolveFilePath(file);
			
			return (BinaryExtender.Busybox) (checkExtender("BinaryExtender.Busybox:" + path) ? 
					getExtender("BinaryExtender.Busybox:" + path) : 
						addExtender("BinaryExtender.Busybox:" + path, BinaryExtender.Busybox.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) path )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link BinaryExtender.Binary}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same binary will not provide multiple instances.
	 */
	public BinaryExtender.Binary binary(String binary) {
		synchronized (mInstanceLock) {
			return (BinaryExtender.Binary) (checkExtender("BinaryExtender.Binary:" + binary) ? 
					getExtender("BinaryExtender.Binary:" + binary) : 
						addExtender("BinaryExtender.Binary:" + binary, BinaryExtender.Binary.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) binary )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link ProcessExtender.Processes}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same file will not provide multiple instances.
	 */
	public ProcessExtender.Processes processes() {
		synchronized (mInstanceLock) {
			return (ProcessExtender.Processes) (checkExtender("ProcessExtender.Processes") ? 
					getExtender("ProcessExtender.Processes") : 
						addExtender("ProcessExtender.Processes", ProcessExtender.Processes.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link ProcessExtender.Process}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same process will not provide multiple instances.
	 */
	public ProcessExtender.Process process(String process) {
		synchronized (mInstanceLock) {
			return (ProcessExtender.Process) (checkExtender("ProcessExtender.Process:" + process) ? 
					getExtender("ProcessExtender.Process:" + process) : 
						addExtender("ProcessExtender.Process:" + process, ProcessExtender.Process.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) process )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link ProcessExtender.Process}.
	 * <br />
	 * This method uses caching which means that multiple calls to the same process id will not provide multiple instances.
	 */
	public ProcessExtender.Process process(Integer pid) {
		synchronized (mInstanceLock) {
			return (ProcessExtender.Process) (checkExtender("ProcessExtender.Process:" + pid) ? 
					getExtender("ProcessExtender.Process:" + pid) : 
						addExtender("ProcessExtender.Process:" + pid, ProcessExtender.Process.getInstance(this, mInstanceLock, new ExtenderGroupTransfer( (Object) pid )).instance));
		}
	}
	
	/**
	 * Get an instance of {@link PackageExtender.Packages}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public PackageExtender.Packages packages() {
		synchronized (mInstanceLock) {
			return (PackageExtender.Packages) (checkExtender("PackageExtender.Packages") ? 
					getExtender("PackageExtender.Packages") : 
						addExtender("PackageExtender.Packages", PackageExtender.Packages.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * Get an instance of {@link ProcessExtender.Power}.
	 * <br />
	 * This method uses caching which means that multiple calls will not provide multiple instances.
	 */
	public ProcessExtender.Power power() {
		synchronized (mInstanceLock) {
			return (ProcessExtender.Power) (checkExtender("ProcessExtender.Power") ? 
					getExtender("ProcessExtender.Power") : 
						addExtender("ProcessExtender.Power", ProcessExtender.Power.getInstance(this, mInstanceLock, new ExtenderGroupTransfer()).instance));
		}
	}
	
	/**
	 * A small method to get the shell environment variable.
	 */
	public String[] getEnvironmentVariable() {
		String variable = shell("echo $PATH");
		
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
	
	public final static class ExtenderGroupTransfer {
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
	
	public final static class Config {
		/**
		 * Used to extend the shell PATH variable. Each index in the ArrayList should contain a path to one or more binaries. Each index will be added in front of the standard variables.
		 */
		public static final List<String> PATH = new ArrayList<String>();
		
		/**
		 * This property contains all of the all-in-one binaries that an be used on a device. By default, it contains busybox and toolbox if available in the PATH variable paths
		 */
		public static final List<String> BINARIES = new ArrayList<String>();
		
		static {
			BINARIES.add("busybox");
			BINARIES.add("toolbox");
		}
		
		/**
		 * This property decides what should be logged. You can add one or more of the RootFW.E properties to this one to enable logging.
		 */
		public static Integer LOG = E_ERROR|E_WARNING;
		
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
	
	public final static class ShellInputStream extends BufferedReader {
		public ShellInputStream(InputStream in) {
			super( new BufferedReader( new InputStreamReader(in) ) );
		}
		
		public ShellInputStream(Reader in) {
			super(in);
		}
	}
	
	public final static class ShellOutputStream extends DataOutputStream {
		public ShellOutputStream(OutputStream out) {
			super(out);
		}
	}
	
	public static interface ConnectionListener {
		public void onConnectionEstablished(RootFW instance);
		public void onConnectionFailed(RootFW instance);
		public void onConnectionClosed(RootFW instance);
	}
	
	public static interface ConnectionController {
		public Boolean onConnectionEstablishing(RootFW instance);
		public Boolean onConnectionClosing(RootFW instance);
	}
}
