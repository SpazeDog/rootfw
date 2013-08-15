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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.containers.Data;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

/**
 * This class is used to communicate with the shell. 
 * <br />
 * As this class is an extender, is should not be called directly. Instead use RootFW.shell()
 * 
 * <dl>
 * <dt><span class="strong">Example:</span></dt>
 * <dd><code><pre>
 * RootFW root = new RootFW();
 * 
 * if (root.isConnected()) {
 *     ShellResult result = root.shell().addCommands("command1", "command2").run();
 *     
 *     if (result.wasSuccessfull) {
 *         String lastLine = result.line();
 *     }
 *     
 *     root.disconnect();
 * }
 * </pre></code></dd>
 * </dl>
 */
public class ShellExtender implements ExtenderGroup {
	
	protected BufferedReader mInput;
	protected DataOutputStream mOutput;
	
	protected Integer[] mResultCodes = new Integer[]{0};
	protected List<String[]> mCommands = new ArrayList<String[]>();
	
	private final static Pattern mPatternBinarySearch = Pattern.compile("%binary([ ]*)");
	
	/**
	 * Create a new ShellExtender instance.
	 * 
	 * @param input
	 *     The InputStream from the RootFW connection
	 *     
	 * @param output
	 *     The OutputStream from the RootFW connection
	 */
	public ShellExtender(InputStream input, OutputStream output) {
		mInput = new BufferedReader( new InputStreamReader(input) );
		mOutput = new DataOutputStream(output);
	}
	
	/**
	 * This is the same as addCommands(), only this one will auto create multiple attempts using each defined binary in RootFW.Config.BINARY. You can use the prefix %binary to represent where the binary should be injected. 
	 * <br />
	 * For an example, if you add the command <code>buildCommands('%binary df -h')</code>, this method will create <code>new String[]{'busybox df -h', 'toolbox df -h', 'df -h'}</code>. This makes it easier to add multiple attempts without having to type in each attempt via <code>addCommands()</code>.
	 * 
	 * @param commands
	 *     A string containing a command with %binary prefix
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender buildCommands(String... commands) {
		for (int y=0; y < commands.length; y++) {
			buildCommands( new String[]{ commands[y] } );
		}
		
		return this;
	}
	
	/**
	 * This is the same as buildCommands().
	 * <br />
	 * If you add the command <code>buildCommands(new String[]{'%binary df -h', '%binary df'})</code>, this method will create <code>new String[]{'busybox df -h', 'toolbox df -h', 'df -h', 'busybox df', 'toolbox df', 'df'}</code>.
	 * 
	 * @param commands
	 *     An array containing multiple attempts with %binary prefix
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender buildCommands(String[]... commands) {
		for (int y=0; y < commands.length; y++) {
			List<String> cmd = new ArrayList<String>();
			
			for (int i=0; i < commands[y].length; i++) {
				for (int x=0; x < RootFW.Config.BINARIES.size(); x++) {
					cmd.add( mPatternBinarySearch.matcher(commands[y][i]).replaceAll( RootFW.Config.BINARIES.get(x) + " " ) + " 2>/dev/null" );
				}
				
				cmd.add( mPatternBinarySearch.matcher(commands[y][i]).replaceAll("") + " 2>/dev/null" );
			}
			
			mCommands.add( cmd.toArray( new String[ cmd.size() ] ) );
		}
		
		return this;
	}
	
	/**
	 * This is used to add multiple commands to be executed in the shell. Each argument should be a separate command to be executed. Each command output will be merged in the ShellResult.
	 * <br />
	 * If one command fails (Does not return a valid result code), it will stop executing the rest of the commands. 
	 * 
	 * @param commands
	 *     A string containing a command
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender addCommands(String... commands) {
		for (int i=0; i < commands.length; i++) {
			mCommands.add( new String[]{ commands[i] } );
		}
		
		return this;
	}
	
	/**
	 * This is used to add multiple commands with multiple attempt in each. Each array is treated as one command, the class will execute each item in the array until one is successful, after which it will continue to the next argument. Only the output from each successful array item is returned in the ShellResult.
	 * 
	 * @param commands
	 *     An array with multiple attempts of the same command
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender addCommands(String[]... commands) {
		for (int i=0; i < commands.length; i++) {
			mCommands.add( commands[i] );
		}
		
		return this;
	}
	
	/**
	 * Whether or not a command was successful or not, depends on the result code returned by the shell. This result code determines the result of <code>ShellResult.wasSuccessfull()</code> and it also controls when to stop executing multiple command attempts.
	 * <br />
	 * By default, 0 is considered successful, but you might need other result codes to be considered successful as well. This method allows you to add more result codes to be considered successful.
	 * 
	 * @param result
	 *     A result code to be considered successful
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender setResultCodes(Integer... result) {
		mResultCodes = result;
		
		return this;
	}
	
	/**
	 * Whenever you add commands using <code>builtCommands()</code> and <code>addCommands()</code>, or adds more result codes via <code>setResultCodes()</code>, this is all cached until you execute <code>run()</code>. 
	 * <br >
	 * This method will reset this cache without having to execute <code>run()</code>.
	 *     
	 * @return
	 *     This instance
	 */
	public ShellExtender reset() {
		mCommands.clear();
		mResultCodes = new Integer[]{0};
		
		return this;
	}
	
	/**
	 * This method will execute all of the commands that has been added via <code>addCommands()</code> and <code>builtCommands()</code>.
	 *     
	 * @return
	 *     a new instance of ShellResult with all of the shell output and information like the result code. 
	 */
	public ShellResult run() {
		List<String> output = new ArrayList<String>();
		List<Integer> cmdNumber = new ArrayList<Integer>();
		Integer resultCode = -1;
		Integer[] codes = mResultCodes;
		
		try {			
			commandLoop:
			for (int i=0; i < mCommands.size(); i++) {
				String[] commandTries = mCommands.get(i);
				
				for (int x=0; x < commandTries.length; x++) {
					String command = commandTries[x] + "\n\n";
					
					command += "status=$? && echo EOL:a00c38d8:EOL\n";
					command += "echo $status\n";
					
					mOutput.write( command.getBytes() );
					mOutput.flush();
					
					String input;
					List<String> lines = new ArrayList<String>();
					
					try {
						while ((input = mInput.readLine()) != null) {
							if (!input.contains("EOL:a00c38d8:EOL")) {
								lines.add(input);
								
							} else {
								try { 
									resultCode = Integer.parseInt( mInput.readLine() );
									
								} catch(Throwable e) { resultCode = -1; }
								
								break;
							}
						}
						
						for (int y=0; y < mResultCodes.length; y++) {
							if ((int) resultCode == (int) mResultCodes[y]) {
								if (mCommands.size() == 1) {
									output = lines;
									
								} else {
									output.addAll(lines);
								}
								
								cmdNumber.add(x);
								
								break;
								
							} else if (y == mResultCodes.length-1 && x == commandTries.length-1) {
								break commandLoop;
							}
						}
						
					} catch (Throwable e) {}
				}
			}
		
		} catch (Throwable e) {}
		
		reset();
		
		return new ShellResult(output.toArray(new String[output.size()]), resultCode, codes, cmdNumber.toArray(new Integer[cmdNumber.size()]));
	}
	
	/**
	 * This method allows you to directly execute one single command without having to use <code>addCommands()</code> and <code>builtCommands()</code>. 
	 * <br />
	 * Note that if you have already used <code>addCommands()</code> or <code>builtCommands()</code>, this argument will just be added to the stack. It does not reset anything before executing.
	 * 
	 * @param command
	 *     One single command to be executed in the shell
	 *     
	 * @return
	 *     a new instance of ShellResult with all of the shell output and information like the result code. 
	 */
	public ShellResult run(String command) {
		return addCommands( command ).run();
	}

	/**
	 * This class get's returned after running shell commands. It contains all of the information returned by the shell after executing the stack of commands. 
	 * <br />
	 * Note that this class is extended from the Data class, so it will contain all of the data tools available in that class as well.
	 */
	public static class ShellResult extends Data {
		private Integer mResultCode;
		private Integer[] mValidResults;
		private Integer[] mCommandNumber;
		
		public ShellResult(String[] lines, Integer result, Integer[] validResults, Integer[] commandNumber) {
			super(lines);
			
			mResultCode = result;
			mValidResults = validResults;
			mCommandNumber = commandNumber;
		}
		
		/**
		 * @return
		 *     The result code returned by the shell
		 */
		public Integer getResultCode() {
			return mResultCode;
		}
		
		/**
		 * This method will compare the result code returned by the shell, with the stack of result codes added via <code>ShellExtender.setResultCodes()</code>.
		 *     
		 * @return
		 *     <code>True</code> if the result code was found in the stack
		 */
		public Boolean wasSuccessfull() {
			for (int i=0; i < mValidResults.length; i++) {
				if ((int) mValidResults[i] == (int) mResultCode) {
					return true;
				}
			}
			
			return false;
		}
		
		/**
		 * This method will return the number of each successful command attempts. Since each command can be multiple attempts, this can be used to determine which of the attempts was successful. 
		 * 
		 * @param cmdNum
		 *     The number of the command to check (Not the attempt number)
		 *     
		 * @return
		 *     The number of the successful attempt for the defined command
		 */
		public Integer getCommandNumber(Integer cmdNum) {
			return cmdNum > mCommandNumber.length ? -1 : mCommandNumber[cmdNum];
		}
	}
}
