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

import java.util.HashMap;
import java.util.Map;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.Data;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.iface.Extender;

public class Property implements Extender {
	public final static String TAG = RootFW.TAG + "::Property";
	
	private RootFW mParent;
	
	private Map<String, Map<String, String>> mFiles = new HashMap<String, Map<String, String>>();
	private Map<String, String> mProperties = new HashMap<String, String>();
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.property</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW::Property
	 */
	public Property(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Get all properties registered on the device
	 *    
	 * @return
	 *     A Map containing the names and values of all the properties
	 */
	public Map<String, String> get() {
		if (mProperties.size() == 0) {
			ShellResult lResult = mParent.shell.execute("getprop");
			
			if (lResult.code() == 0 && lResult.output().length() > 0) {
				String[] lines = lResult.output().raw();
				
				for (int i=0; i < lines.length; i++) {
					String key = lines[i].substring(1, lines[i].indexOf("]"));
					String value = lines[i].substring(lines[i].lastIndexOf("[")+1, lines[i].length()-1);
					
					mProperties.put(key, value);
				}
			}
		}
		
		return mProperties;
	}
	
	/**
	 * Get the value from a specific registered property
	 * 
	 * @param name
	 *     The name of the property
	 *    
	 * @return
	 *     The value of the specified property
	 */
	public String get(String name) {
		return get().get(name);
	}
	
	/**
	 * Get all properties defined in a prop file
	 * 
	 * @param file
	 *     The path to the prop file
	 *    
	 * @return
	 *     A Map containing the names and values of all the properties defined in the specified file
	 */
	public Map<String, String> getFromFile(String file) {
		if (mFiles.get(file) == null) {
			mFiles.put(file, new HashMap<String, String>());
			Data data = mParent.file.read(file);
			
			if (data != null && data.length() > 0) {
				String[] lines = data.raw();
				Map<String, String> map = mFiles.get(file);
				
				for (int i=0; i < lines.length; i++) {
					if (lines[i].contains("=") && !lines[i].contains("#")) {
						String key = lines[i].substring(0, lines[i].indexOf("=")).trim();
						String value = lines[i].substring(lines[i].indexOf("=")+1).trim();
						
						map.put(key, value);
					}
				}
			}
		}
		
		return mFiles.get(file);
	}
	
	/**
	 * Get the value from a specific registered property
	 * 
	 * @param file
	 *     The path to the prop file
	 *     
	 * @param name
	 *     The name of the property
	 *    
	 * @return
	 *     The value of the specified property in the specified file
	 */
	public String getFromFile(String file, String name) {
		return getFromFile(file).get(name);
	}
	
	/**
	 * Change the value of a registered property
	 * 
	 * @param name
	 *     The name of the property
	 *     
	 * @param value
	 *     The value to assign to the property
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean set(String name, String value) {
		Map<String, String> map = get();
		
		map.put(name, value);
		
		ShellResult lResult = mParent.shell.execute("setprop '" + name + "' '" + value + "'");
		
		return lResult.code() == 0;
	}
	
	/**
	 * Change the value of a property in a prop file
	 * 
	 * @param file
	 *     The path to the prop file
	 * 
	 * @param name
	 *     The name of the property
	 *     
	 * @param value
	 *     The value to assign to the property
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean setInFile(String file, String name, String value) {
		Map<String, String> map = getFromFile(file);
		
		map.put(name, value);
		
		String[] lines = new String[ map.size() ];
		Integer i = 0;
		for (String key : map.keySet()) {
			lines[i] = key + "=" + map.get(key); i++;
		}
		
		return mParent.file.write(file, lines);
	}
	
	/**
	 * Remove a property from a prop file
	 * 
	 * @param file
	 *     The path to the prop file
	 * 
	 * @param name
	 *     The name of the property
	 *    
	 * @return
	 *     <code>True</code> on success
	 */
	public Boolean removeFromFile(String file, String name) {
		Map<String, String> map = getFromFile(file);
		
		if (map.containsKey(name)) {
			map.remove(name);
			
			String[] lines = new String[ map.size() ];
			Integer i = 0;
			for (String key : map.keySet()) {
				lines[i] = key + "=" + map.get(key); i++;
			}
			
			return mParent.file.write(file, lines);
		}
		
		return true;
	}
	
	/**
	 * Check to see if a property has been registered
	 * 
	 * @param name
	 *     The name of the property
	 *    
	 * @return
	 *     <code>True</code> if the property exist
	 */
	public Boolean exist(String name) {
		return get().containsKey(name);
	}
	
	/**
	 * Check to see if a property exist in a prop file
	 * 
	 * @param file
	 *     The path to the prop file
	 * 
	 * @param name
	 *     The name of the property
	 *    
	 * @return
	 *     <code>True</code> if the property exist
	 */
	public Boolean existInFile(String file, String name) {
		return getFromFile(file).containsKey(name);
	}
}
