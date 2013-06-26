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

package com.spazedog.lib.rootfw.container;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw.iface.Container;

public class ShellProcess implements Container {
	private ArrayList<String[]> mCommands = new ArrayList<String[]>();
	private ArrayList<Integer> mCodes = new ArrayList<Integer>();
	
	private final static Pattern mPatternBinarySearch = Pattern.compile("%binary([ ]*)");

	/**
	 * All-in-one binaries like toolbox and busybox that is used to generate command entries
	 * <p/>
	 * Default: {"busybox", "toolbox"}
	 */
	public static String[] BINARIES = {"busybox", "toolbox"};

	/**
	 * See <code>generate(String[] aCommands, Integer[] aResultCodes)</code>
	 */
	public static ShellProcess generate(String aCommands) {
		return generate(new String[] {aCommands}, null);
	}
	
	/**
	 * See <code>generate(String[] aCommands, Integer[] aResultCodes)</code>
	 */
	public static ShellProcess generate(String[] aCommands) {
		return generate(aCommands, null);
	}
	
	/**
	 * See <code>generate(String[] aCommands, Integer[] aResultCodes)</code>
	 */
	public static ShellProcess generate(String aCommands, Integer[] aResultCodes) {
		return generate(new String[] {aCommands}, aResultCodes);
	}
	
	/**
	 * Auto generate command entries per each binary defined in <code>com.spazedog.rootfw.container.ShellProcess.BINARIES</code>. 
	 * <p/>
	 * {"%binary df -h"} => {"busybox df -h", "toolbox df -h", "df -h"}
	 * 
	 * @param aCommands
	 *     Command to generate entries for
	 *    
	 * @param aResultCodes
	 *     Result codes which defines successful execution (Defaults to 0)
	 *    
	 * @return
	 *     A ShellProcess container with all generated entries
	 */
	public static ShellProcess generate(String[] aCommands, Integer[] aResultCodes) {
		ShellProcess lProcess = new ShellProcess();
		
		for (int i=0; i < aCommands.length; i++) {
			for (int x=0; x < BINARIES.length; x++) {
				lProcess.addCommand( mPatternBinarySearch.matcher(aCommands[i]).replaceAll( BINARIES[x] + " " ) + " 2>/dev/null" );
			}
			
			lProcess.addCommand( mPatternBinarySearch.matcher(aCommands[i]).replaceAll("") + " 2>/dev/null" );
		}
		
		if (aResultCodes != null) {
			for (int i=0; i < aResultCodes.length; i++) {
				lProcess.addResultCode(aResultCodes[i]);
			}
		}
		
		return lProcess;
	}
	
	/**
	 * Create a new ShellProcess instance with a default result code 0
	 */
	public ShellProcess() {
		mCodes.add(0);
	}

	/**
	 * See <code>ShellProcess(String[] aCommand, Integer aResultCode)</code>
	 */
	public ShellProcess(Integer aResultCode) {
		mCodes.add(aResultCode);
	}

	/**
	 * See <code>ShellProcess(String[] aCommand, Integer aResultCode)</code>
	 */
	public ShellProcess(String aCommand) {
		mCodes.add(0);
		mCommands.add(new String[] {aCommand});
	}
	
	/**
	 * See <code>ShellProcess(String[] aCommand, Integer aResultCode)</code>
	 */
	public ShellProcess(String[] aCommand) {
		mCodes.add(0);
		mCommands.add(aCommand);
	}

	/**
	 * See <code>ShellProcess(String[] aCommand, Integer aResultCode)</code>
	 */
	public ShellProcess(String aCommand, Integer aResultCode) {
		mCodes.add(aResultCode);
		mCommands.add(new String[] {aCommand});
	}
	
	/**
	 * Create a new ShellProcess instance
	 * 
	 * @param aCommand
	 *     Default command entry
	 *    
	 * @param aResultCode
	 *     Default result code which defines successful execution
	 */
	public ShellProcess(String[] aCommand, Integer aResultCode) {
		mCodes.add(aResultCode);
		mCommands.add(aCommand);
	}

	/**
	 * See <code>addCommand(String[] aCommand)</code>
	 */
	public void addCommand(String aCommand) {
		mCommands.add(new String[] {aCommand});
	}
	
	/**
	 * Add a new command entry
	 * 
	 * @param aCommand
	 *     New command entry
	 */
	public void addCommand(String[] aCommand) {
		mCommands.add(aCommand);
	}
	
	/**
	 * Add a new successful result code
	 * 
	 * @param aResultCode
	 *     New result code
	 */
	public void addResultCode(Integer aResultCode) {
		mCodes.add(aResultCode);
	}
	
	/**
	 * Get the number of command entries
	 *    
	 * @return
	 *     The number of entries
	 */
	public Integer commandLength() {
		return mCommands.size();
	}
	
	/**
	 * Get the number of result codes
	 *    
	 * @return
	 *     The number of result codes
	 */
	public Integer resultCodeLength() {
		return mCodes.size();
	}
	
	/**
	 * Get all of the command entries
	 *    
	 * @return 
	 *     All of the command entries
	 */
	public ArrayList<String[]> commands() {
		return mCommands;
	}
	
	/**
	 * Get all of the result codes
	 *    
	 * @return 
	 *     All of the result codes
	 */
	public ArrayList<Integer> resultCodes() {
		return mCodes;
	}
	
	/**
	 * Get the commands in a specified command entry
	 *    
	 * @return 
	 *     The commands in the specified command entry
	 */
	public String[] command(Integer aIndex) {
		return mCommands.get(aIndex);
	}
	
	/**
	 * Get the result code in a specified index
	 *    
	 * @return
	 *     The result code in the specified index
	 */
	public Integer resultCode(Integer aIndex) {
		return mCodes.get(aIndex);
	}
}
