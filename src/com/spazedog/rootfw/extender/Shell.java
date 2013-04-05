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

package com.spazedog.rootfw.extender;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.container.ShellProcess;
import com.spazedog.rootfw.container.ShellResult;
import com.spazedog.rootfw.iface.Extender;

public final class Shell implements Extender {
	public final static String TAG = RootFW.TAG + "::Shell";
	
	private final static Pattern oPatternResultSearch = Pattern.compile("^.*\\|([0-9]+)\\|.*$");
	private final static Pattern oPatternLTrim = Pattern.compile("^[\n]+");
	private final static Pattern oPatternRTrim = Pattern.compile("[\n]+$");
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.shell</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Shell(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Add multiple command entries and execute.
	 * <p/>
	 * It will stop at the first entry to return 0
	 * 
	 * @param arg
	 *     Command strings that should be added to ShellProcess
	 *    
	 * @return
	 *     Returns a ShellResult containing result code and output from the shell
	 */
	public ShellResult execute(String... arg) {
		ShellProcess lCommands = new ShellProcess();
		
		for (int i=0; i < arg.length; i++) {
			lCommands.addCommand(arg[i]);
		}
		
		return execute(lCommands);
	}

	/**
	 * Add multiple command entries and execute.
	 * <p/>
	 * It will stop at the first entry to return 0
	 * 
	 * @param arg
	 *     Command strings that should be added to ShellProcess
	 *    
	 * @return
	 *     Returns a ShellResult containing result code and output from the shell
	 */
	public ShellResult execute(String[]... arg) {
		ShellProcess lCommands = new ShellProcess();
		
		for (int i=0; i < arg.length; i++) {
			lCommands.addCommand(arg[i]);
		}
		
		return execute(lCommands);
	}
	
	/**
	 * Execute a ShellProcess and return the result code and shell output.
	 * <p/>
	 * The method will stop executing at the first command entry that returns
	 * a valid return code which are defined in the ShellProcess.
	 * 
	 * @param aCommands
	 *     A ShellProcess container which contains all of the commands entries
	 *    
	 * @return
	 *     Returns a ShellResult containing result code and output from the shell
	 */
	public ShellResult execute(ShellProcess aCommands) {
		try {
			DataOutputStream lOutputStream = new DataOutputStream(mParent.process().getOutputStream());
			BufferedReader lInputStream = new BufferedReader(new InputStreamReader(mParent.process().getInputStream()));
			
			Integer lCommandCount, lResultCount, lCommandPartCount, lResultCode = -1;
			String lCommandString, lInputLine, lInputData = null, lInputResult = null;
			String[] lCurrentCommands;
			
			commandLoop:
			for (lCommandCount=0; lCommandCount < aCommands.commandLength(); lCommandCount++) {
				lCurrentCommands = aCommands.command(lCommandCount);
				lCommandString = "";
				lInputData = "";
				
				for (lCommandPartCount=0; lCommandPartCount < lCurrentCommands.length; lCommandPartCount++) {
					lCommandString += lCurrentCommands[lCommandPartCount] + "\n";
				}
				
				if ((RootFW.LOG & RootFW.E_DEBUG) != 0) {
					RootFW.log(TAG + ".execute", "Executing commands {" + lCommandString.substring(0, lCommandString.length()-1).replaceAll("\n", "}, {") + "}", RootFW.E_DEBUG);
				}
				
				lCommandString += "\n";
				lCommandString += "status=$? && EOL:a00c38d8:EOL\n";
				lCommandString += "\\|$status\\|\n";
				lCommandString += "EOL:a00c38d8:EOL\n";
				
				lOutputStream.write(lCommandString.getBytes());
				lOutputStream.flush();
				
				try {
					while ((lInputLine = lInputStream.readLine()) != null) {
						if (!lInputLine.contains("EOL:a00c38d8:EOL")) {
							lInputData += lInputLine + "\n";
							
						} else {
							while ((lInputLine = lInputStream.readLine()) != null && !lInputLine.contains("EOL:a00c38d8:EOL")) {
								if (lInputLine.length() > 0 && oPatternResultSearch.matcher(lInputLine).matches()) {
									lInputResult = oPatternResultSearch.matcher(lInputLine).replaceAll("$1");
								}
							}
							
							break;
						}
					}
					
				} catch(Throwable e) { RootFW.log(TAG + ".execute", "Failed reading input stream", RootFW.E_ERROR, e); }
				
				if (lInputData.length() > 0) {
					lInputData = oPatternRTrim.matcher( oPatternLTrim.matcher(lInputData).replaceAll("") ).replaceAll("");
				}
				
				try { lResultCode = Integer.parseInt(lInputResult); } catch(Throwable e) { lResultCode = -1; }
				
				if (lCommandCount >= (aCommands.commandLength() - 1)) {
					break;
				}
				
				for (lResultCount=0; lResultCount < aCommands.resultCodeLength(); lResultCount++) {
					if (aCommands.resultCode(lResultCount) == lResultCode) {
						break commandLoop;
					}
				}
			}
			
			RootFW.log(TAG + ".execute", "Done executing shell. Finished at command number " + (lCommandCount+1) + " with result code " + lResultCode + "");

			return new ShellResult(lInputData, lResultCode, lCommandCount+1);
			
		} catch(Throwable e) { RootFW.log(TAG + ".execute", "Failed executing shell commands", RootFW.E_ERROR, e); }
		
		return null;
	}
}
