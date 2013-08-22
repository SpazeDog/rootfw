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

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.Filesystem;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class InstanceExtender {
	
	/**
	 * This class is used to share the same RootFW instance across multiple classes and methods. It stores the instance in a static property, until some method disconnects from the shell.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#userInstance()} or {@link RootFW#rootInstance()} to retrieve an instance.
	 */
	public static class Instance implements ExtenderGroup {
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Instance((Boolean) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Instance(Boolean useRoot) {
			mInstance = useRoot ? 1 : 0;
			
			if (oInstances[mInstance] == null) {
				oInstances[mInstance] = new InstanceRootFW( mInstance == 1 );
				oInstances[mInstance].connect();
			}
		}
		
		/**
		 * Stores the user and root instances when created
		 */
		protected static InstanceRootFW[] oInstances = new InstanceRootFW[2];
		protected static List<InstanceCallback>[] oCallbacks = new ArrayList[]{new ArrayList<InstanceCallback>(), new ArrayList<InstanceCallback>()};
		
		protected Integer mInstance;
		
		/**
		 * Used to execute callbacks on connection established
		 */
		protected static void onConnect(Boolean root) {
			Integer x = root ? 1 : 0;
			
			if (oCallbacks[x].size() > 0) {
				for (int i=0; i < oCallbacks[x].size(); i++) {
					oCallbacks[x].get(i).onConnect(oInstances[x]);
				}
			}
		}
		
		/**
		 * Used to execute callbacks after disconnecting
		 */
		protected static void onDisconnect(Boolean root) {
			Integer x = root ? 1 : 0;
			oInstances[x] = null;
			
			if (oCallbacks[x].size() > 0) {
				for (int i=0; i < oCallbacks[x].size(); i++) {
					oCallbacks[x].get(i).onDisconnect(oInstances[x]);
				}
			}
		}
		
		/**
		 * Used to execute callbacks on connection failed
		 */
		protected static void onFailed(Boolean root) {
			Integer x = root ? 1 : 0;
			
			if (oCallbacks[x].size() > 0) {
				for (int i=0; i < oCallbacks[x].size(); i++) {
					oCallbacks[x].get(i).onFailed(oInstances[x]);
				}
			}
		}
		
		/**
		 * Add a {@link InstanceCallback} object to this shared instance.
		 * 
		 * @param callback
		 *     A {@link InstanceCallback} object
		 */
		public void addCallback(InstanceCallback callback) {
			oCallbacks[mInstance].add(callback);
		}
		
		/**
		 * Get the shared RootFW instance
		 * 
		 * @return
		 *     The stored RootFW instance
		 */
		public RootFW get() {
			return (RootFW) oInstances[mInstance];
		}
		
		/**
		 * Add a lock to the connection. This will make sure that the connection cannot be closed until the connection is unlocked()
		 * 
		 * @return
		 *     This instance
		 */
		public InstanceExtender.Instance lock() {
			if (oInstances[mInstance] != null) {
				oInstances[mInstance].mLocks += 1;
			}
			
			return this;
		}
		
		/**
		 * Remove the lock on the connection. Note that although you remove this lock, others might also have a lock added to the connection. These must also be removed in order to close the connection.
		 * 
		 * @return
		 *     This instance
		 */
		public InstanceExtender.Instance unlock() {
			if (oInstances[mInstance] != null && oInstances[mInstance].mLocks > 0) {
				oInstances[mInstance].mLocks -= 1;
			}
			
			return this;
		}
		
		/**
		 * Check whether there is any locks added to the connection
		 * 
		 * @return
		 *     <code>True</code> if there is locks added
		 */
		public Boolean isLocked() {
			return oInstances[mInstance] != null && oInstances[mInstance].mLocks > 0;
		}
		
		/**
		 * If you ever need to close a connection no matter how many locks has been added to it, you can use this method. It will remove all of the locks and force close the connection.
		 * <br />
		 * Note that this could provide issues if other methods are using this connection.
		 */
		public void destroy() {
			oInstances[mInstance].mLocks = 0;
			oInstances[mInstance].disconnect();
			oInstances[mInstance] = null;
		}
	}
	
	/**
	 * This is an extended class of the RootFW class.
	 * It's job is to implement the feature of not disconnecting locked connections. 
	 * The class is protected, so it is not possible to store instances as InstanceRootFW but only as RootFW, as the changes are only meant for internal purposes.
	 */
	protected static class InstanceRootFW extends RootFW {
		public Integer mLocks = 0;
		
		public InstanceRootFW(Boolean useRoot) {
			super(useRoot);
		}
		
		@Override
		public Boolean connect() {
			if (super.connect()) {
				InstanceExtender.Instance.onConnect(isRoot());
				
				return true;
			}
			
			InstanceExtender.Instance.onFailed(isRoot());
			
			return false;
		}
		
		/**
		 * This will overwrite the disconnect() method in the main RootFW and implement feature of locking a connection.
		 */
		@Override
		public void disconnect() {
			if (mLocks == 0) {
				super.disconnect();
				InstanceExtender.Instance.onDisconnect(isRoot());
			}
		}
	}
	
	/**
	 * This interface is used when adding connection callbacks via {@link #addCallback}
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
}
