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

import java.util.HashMap;
import java.util.Map;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.containers.Data.DataReplace;
import com.spazedog.lib.rootfw3.containers.Data.DataSorting;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class PropertyExtender {
	
	/**
	 * This class is used to work with global properties, which means a Java front-end for <code>getProp</code> and <code>setProp</code>.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#property()} to retrieve an instance. 
	 */
	public static class Properties implements ExtenderGroup {
		protected ShellExtender.Shell mShell;
		
		private Map<String, String> mProperties = new HashMap<String, String>();
		
		private Object mLock = new Object();
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Properties(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Properties(RootFW parent) {
			mShell = parent.shell();
		}
		
		/**
		 * Get a list of all the global properties that is registered on the device.
		 * 
		 * @return
		 *     A Map containing all property names and values
		 */
		public Map<String, String> getAll() {
			synchronized (mLock) {
				if (mProperties.size() == 0) {
					ShellResult result = mShell.run("getprop 2> /dev/null").trim();
					
					if (result.wasSuccessful() && result.size() > 0) {
						String[] props = result.getArray();
						
						for (int i=0; i < props.length; i++) {
							String key = props[i].substring(1, props[i].indexOf("]"));
							String value = props[i].substring(props[i].lastIndexOf("[")+1, props[i].length()-1);
							
							mProperties.put(key, value);
						}
					}
				}
				
				return mProperties;
			}
		}
		
		/**
		 * Get the value from a property.
		 * 
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     The property value
		 */
		public String get(String name) {
			return getAll().get(name);
		}
		
		/**
		 * Set/Change the value of a property.
		 * 
		 * @param name
		 *     The name of the property
		 *     
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     <code>True</code> if the value was successfully set
		 */
		public Boolean set(String name, String value) {
			synchronized (mLock) {
				ShellResult result = mShell.run("setprop '" + name + "' '" + value + "' 2> /dev/null");
				
				if (result.wasSuccessful()) {
					mProperties.put(name, value);
				}
				
				return result.wasSuccessful();
			}
		}
		
		/**
		 * Check whether a property exists or not.
		 * 
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     <code>True</code> if the property exists
		 */
		public Boolean exists(String name) {
			synchronized (mLock) {
				return mProperties.containsKey(name);
			}
		}
	}

	/**
	 * This class is used to work with property files, like /system/build.prop.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#property(String)} to retrieve an instance. 
	 */
	public static class File implements ExtenderGroup {
		
		protected FileExtender.File mFile;
		
		private Map<String, String> mProperties = new HashMap<String, String>();
		
		private Object mLock = new Object();
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new File(parent, (java.io.File) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 *     
		 * @param file
		 *     A {@link java.io.File} object
		 */
		private File(RootFW parent, java.io.File file) {
			mFile = parent.file(file.getAbsolutePath());
		}
		
		/**
		 * Get a list of all the properties that is defined in the property file.
		 * 
		 * @return
		 *     A Map containing all property names and values
		 */
		public Map<String, String> getAll() {
			synchronized (mLock) {
				if (mProperties.size() == 0 && mFile.exists()) {
					FileData data = mFile.readMatches("=");
					
					if (data != null) {
						String[] props = data.assort("#").trim().getArray();
						
						for (int i=0; i < props.length; i++) {
							String key = props[i].substring(0, props[i].indexOf("=")).trim();
							String value = props[i].substring(props[i].indexOf("=")+1).trim();
							
							mProperties.put(key, value);
						}
					}
				}
				
				return mProperties;
			}
		}
		
		/**
		 * Get the value from a property.
		 * 
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     The property value
		 */
		public String get(String name) {
			return getAll().get(name);
		}
		
		/**
		 * Set/Change the value of a property.
		 * 
		 * @param name
		 *     The name of the property
		 *     
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     <code>True</code> if the value was successfully set
		 */
		public Boolean set(final String name, final String value) {
			synchronized (mLock) {
				Boolean status = false;
				
				if (mProperties.containsKey(name)) {
					FileData data = mFile.read();
					
					if (data != null) {
						data.replace(new DataReplace() {
							@Override
							public String replace(String input) {
								if (input != null && input.contains(name) && ( input.trim().startsWith(name + "=") || input.trim().startsWith(name + " ") )) {
									return name + "=" + value;
								}
								
								return input;
							}
						});
						
						status = mFile.write( data.getArray() );
					}
					
				} else {
					status = mFile.write(name + "=" + value, true);
				}
				
				if (status) {
					mProperties.put(name, value);
				}
				
				return status;
			}
		}
		
		/**
		 * Remove a property from the property file.
		 * 
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     <code>True</code> if the property was successfully removed
		 */
		public Boolean remove(final String name) {
			synchronized (mLock) {
				if (mProperties.containsKey(name)) {
					FileData data = mFile.read();
					
					if (data != null) {
						data.assort(new DataSorting() {
							@Override
							public Boolean test(String input) {
								if (input != null && input.contains(name) && ( input.trim().startsWith(name + "=") || input.trim().startsWith(name + " ") )) {
									return true;
								}
								
								return false;
							}
						});
						
						return mFile.write( data.getArray() );
					}
				}
				
				return false;
			}
		}
		
		/**
		 * Check whether a property exists or not.
		 * 
		 * @param name
		 *     The name of the property
		 * 
		 * @return
		 *     <code>True</code> if the property exists
		 */
		public Boolean exists(String name) {
			synchronized (mLock) {
				return mProperties.containsKey(name);
			}
		}
	}
}
