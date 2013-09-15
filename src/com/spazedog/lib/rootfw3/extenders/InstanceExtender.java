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

import android.os.Bundle;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ConnectionListener;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class InstanceExtender implements ExtenderGroup {
	private static InstanceRootFW[] oInstances = new InstanceRootFW[2];
	
	private InstanceExtender() {}
	
	/**
	 * This is used internally by {@link RootFW} to get a new instance of this class. 
	 */
	public static ExtenderGroupTransfer getInstance(RootFW parent, Object instanceLock, ExtenderGroupTransfer transfer) {
		Integer index = (Boolean) transfer.arguments[0] ? 1 : 0;
		InstanceRootFW instance = oInstances[index] != null ? oInstances[index] : new InstanceRootFW(index == 1);
		
		return transfer.setInstance((ExtenderGroup) instance);
	}
	
	@Override
	public void onBroadcastReceive(Integer broadcastType, Bundle arguments) {}
	
	/**
	 * This interface is used to keep compatibility with older implementations of {@link InstanceExtender}
	 * 
	 * @see SharedRootFW
	 */
	public static interface Instance {
		public void addCallback(InstanceCallback callback);
		public RootFW get();
		public void destroy();
		public InstanceExtender.Instance lock();
		public InstanceExtender.Instance unlock();
		public Boolean isLocked();
	}
	
	/**
	 * This interface is used to implement additional features to the main {@link RootFW} class
	 */
	public static interface SharedRootFW extends Instance {
		public Boolean connect();
		public void disconnect();
		public void destroy();
		public Boolean isConnected();
		public Boolean isRoot();
		public ShellExtender.Shell shell();
		public String shell(String command);
		public FileExtender.File file(String file);
		public MemoryExtender.Memory memory();
		public MemoryExtender.Device memory(String device);
		public PropertyExtender.Properties property();
		public PropertyExtender.File property(String file);
		public FilesystemExtender.Filesystem filesystem();
		public FilesystemExtender.Device filesystem(String device);
		public BinaryExtender.Busybox busybox();
		public BinaryExtender.Busybox busybox(String binary);
		public BinaryExtender.Binary binary(String binary);
		public ProcessExtender.Processes processes();
		public ProcessExtender.Process process(String process);
		public ProcessExtender.Process process(Integer pid);
		public PackageExtender.Packages packages();
		public ProcessExtender.Power power();
		public String[] getEnvironmentVariable();
		public Long getInstanceId();
	}
	
	/**
	 * This interface is used when adding connection callbacks via {@link InstanceRootFW#addCallback}
	 * 
	 * @deprecated
	 *     As of version 1.3.0, this class is deprecated. You should instead make use of {@link RootFW#ConnectionListener}
	 */
	public static abstract class InstanceCallback {
		/**
		 * Called when the shared connection was connected
		 */
		public void onConnect(RootFW instance) {}
		
		/**
		 * Called when the shared connection was disconnected
		 */
		public void onDisconnect(RootFW instance) {}
		
		/**
		 * Called when the shared connection failed to connect
		 */
		public void onFailed(RootFW instance) {}
	}
	
	private final static class InstanceRootFW extends RootFW implements ExtenderGroup, Instance, SharedRootFW, ConnectionListener {
		protected List<InstanceCallback> mCallbacks = new ArrayList<InstanceCallback>();
		protected List<ConnectionListener> mConnectionListeners = new ArrayList<ConnectionListener>();
		
		protected Integer mLockCount = 0;
		
		/**
		 * Create a new instance of this class.
		 */
		private InstanceRootFW(Boolean useRoot) {
			super(useRoot);
		}
		
		@Override
		public void onBroadcastReceive(Integer broadcastType, Bundle arguments) {
			if (broadcastType == RootFW.BROADCAST_RETRIEVE && !mConnectionEstablished) {
				connect();
			}
		}
		
		/**
		 * Cast this object to RootFW.
		 * 
		 * @deprecated
		 *     This is deprecated as of version 1.1.0. You should instead use the {@link SharedRootFW} data type to gain access to both the {@link Instance} and {@link RootFW} features. 
		 */
		@Override
		public RootFW get() {
			return (RootFW) this;
		}
		
		/**
		 * Add a lock to the connection. This will make sure that the connection cannot be closed until the connection is unlocked()
		 */
		@Override
		public Instance lock() {
			synchronized (mInstanceLock) {
				mLockCount += 1;
				
				return this;
			}
		}
		
		/**
		 * Remove the lock on the connection. Note that although you remove this lock, others might also have a lock added to the connection. These must also be removed in order to close the connection.
		 */
		@Override
		public Instance unlock() {
			synchronized (mInstanceLock) {
				if (mLockCount > 0) {
					mLockCount -= 1;
				}
				
				return this;
			}
		}
		
		/**
		 * Check whether there is any locks added to the connection
		 * 
		 * @return
		 *     <code>True</code> if there is locks added
		 */
		@Override
		public Boolean isLocked() {
			synchronized (mInstanceLock) {
				return mLockCount > 0;
			}
		}
		
		/**
		 * This will overwrite the disconnect() method in the main RootFW and implement feature of locking the connection 
		 * and add callback features.
		 */
		@Override
		public void disconnect() {
			synchronized (mInstanceLock) {
				if (mLockCount == 0) {
					super.disconnect();
				}
			}
		}
		
		/**
		 * This will overwrite the destroy() method in the main RootFW and implement feature of locking the connection 
		 * and add callback features.
		 */
		@Override
		public void destroy() {
			synchronized (mInstanceLock) {
				mLockCount = 0;
				super.destroy();
			}
		}
		
		/**
		 * Add a {@link InstanceCallback} object to this shared instance.
		 * 
		 * @deprecated
		 *     As of version 1.3.0 this method has been deprecated. Instead you should use {@link #addInstanceListener(ConnectionListener)}
		 * 
		 * @param callback
		 *     A {@link InstanceCallback} object
		 */
		public void addCallback(InstanceCallback callback) {
			synchronized (mInstanceLock) {
				mCallbacks.add(callback);
			}
		}
		
		/**
		 * This is used to add a connection listener to this instance. Unlike {@link RootFW#addConnectionListener(ConnectionListener)}, 
		 * this will only set the listener on this specific instance. 
		 * 
		 * @param listener
		 *     A {@link RootFW#ConnectionListener} object
		 */
		public ConnectionListener addInstanceListener(ConnectionListener listener) {
			synchronized (mInstanceLock) {
				if (!mConnectionListeners.contains(listener)) {
					mConnectionListeners.add(listener);
				}
				
				return listener;
			}
		}
		
		/**
		 * Remove an {@link RootFW#ConnectionListener} from this instance
		 * 
		 * @see #addInstanceListener(ConnectionListener)
		 */
		public void removeInstanceListener(ConnectionListener listener) {
			synchronized (mInstanceLock) {
				mConnectionListeners.remove(listener);
			}
		}
		
		/**
		 * Remove all {@link RootFW#ConnectionListener} from this instance
		 * 
		 * @see #addInstanceListener(ConnectionListener)
		 */
		public void removeInstanceListeners() {
			synchronized (mInstanceLock) {
				mConnectionListeners.clear();
			}
		}

		@Override
		public void onConnectionEstablished(RootFW instance) {
			synchronized (mInstanceLock) {
				if (instance.getInstanceId() == getInstanceId()) {
					for (ConnectionListener listener : mConnectionListeners) {
						listener.onConnectionEstablished(this);
					}
					
					for (InstanceCallback callback : mCallbacks) {
						callback.onConnect(this);
					}
				}
			}
		}

		@Override
		public void onConnectionFailed(RootFW instance) {
			synchronized (mInstanceLock) {
				if (instance.getInstanceId() == getInstanceId()) {
					for (ConnectionListener listener : mConnectionListeners) {
						listener.onConnectionFailed(this);
					}
					
					for (InstanceCallback callback : mCallbacks) {
						callback.onFailed(this);
					}
				}
			}
		}

		@Override
		public void onConnectionClosed(RootFW instance) {
			synchronized (mInstanceLock) {
				if (instance.getInstanceId() == getInstanceId()) {
					for (ConnectionListener listener : mConnectionListeners) {
						listener.onConnectionClosed(this);
					}
					
					for (InstanceCallback callback : mCallbacks) {
						callback.onDisconnect(this);
					}
				}
			}
		}
	}
}
