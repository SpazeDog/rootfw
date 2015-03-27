/*
 * This file is part of the RootFW Project: https://github.com/spazedog/rootfw
 *  
 * Copyright (c) 2015 Daniel Bergløv
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

package com.spazedog.lib.rootfw4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import android.os.Bundle;
import android.util.Log;

import com.spazedog.lib.rootfw4.ShellStreamer.ConnectionListener;
import com.spazedog.lib.rootfw4.ShellStreamer.StreamListener;
import com.spazedog.lib.rootfw4.containers.Data;
import com.spazedog.lib.rootfw4.utils.Device;
import com.spazedog.lib.rootfw4.utils.Device.Process;
import com.spazedog.lib.rootfw4.utils.File;
import com.spazedog.lib.rootfw4.utils.Filesystem;
import com.spazedog.lib.rootfw4.utils.Filesystem.Disk;
import com.spazedog.lib.rootfw4.utils.Memory;
import com.spazedog.lib.rootfw4.utils.Memory.CompCache;
import com.spazedog.lib.rootfw4.utils.Memory.Swap;
import com.spazedog.lib.rootfw4.utils.io.FileReader;
import com.spazedog.lib.rootfw4.utils.io.FileWriter;

/**
 * This class is a front-end to {@link ShellStreamer} which makes it easier to work
 * with normal shell executions. If you need to execute a consistent command (one that never ends, like a daemon or similar), 
 * you should work with {@link ShellStreamer} directly. 
 */
public class Shell {
	public static final String TAG = Common.TAG + ".Shell";
	
	protected static Set<Shell> mInstances = Collections.newSetFromMap(new WeakHashMap<Shell, Boolean>());
	protected static Map<String, String> mBinaries = new HashMap<String, String>();
	
	protected Set<OnShellBroadcastListener> mBroadcastRecievers = Collections.newSetFromMap(new WeakHashMap<OnShellBroadcastListener, Boolean>());
	protected Set<OnShellConnectionListener> mConnectionRecievers = new HashSet<OnShellConnectionListener>();
	
	protected Object mLock = new Object();
	protected ShellStreamer mStream;
	protected Boolean mAllowDisconnect = false;
	protected Boolean mIsConnected = false;
	protected Boolean mIsRoot = false;
	protected List<String> mOutput = new ArrayList<String>();
	protected Integer mResultCode = 0;
	protected Integer mShellTimeout = 15000;
	protected Set<Integer> mResultCodes = new HashSet<Integer>();
	
	/**
	 * This interface is used internally across utility classes.
	 */
	public static interface OnShellBroadcastListener {
		public void onShellBroadcast(String key, Bundle data);
	}
	
	/**
	 * This interface is for use with {@link Shell#executeAsync(String[], OnShellResultListener)}.
	 */
	public static interface OnShellResultListener {
		/**
		 * Called when an asynchronous execution has finished.
		 * 
		 * @param result
		 *     The result from the asynchronous execution
		 */
		public void onShellResult(Result result);
	}
	
	/**
	 * This interface is for use with the execute methods. It can be used to validate an attempt command, if that command 
	 * cannot be validated by result code alone. 
	 */
	public static interface OnShellValidateListener {
		/**
		 * Called at the end of each attempt in order to validate it. If this method returns false, then the next attempt will be executed. 
		 * 
		 * @param command
		 *     The command that was executed during this attempt
		 *     
		 * @param result
		 *     The result code from this attempt
		 *     
		 * @param output
		 *     The output from this attempt
		 *     
		 * @param resultCodes
		 *     All of the result codes that has been added as successful ones
		 *     
		 * @return 
		 *     False to continue to the next attempts, or True to stop the current execution
		 */
		public Boolean onShellValidate(String command, Integer result, List<String> output, Set<Integer> resultCodes);
	}
	
	/**
	 * This interface is used to get information about connection changes.
	 */
	public static interface OnShellConnectionListener {
		/**
		 * Called when the connection to the shell is lost.
		 */
		public void onShellDisconnect();
	}
	
	/**
	 * This class is used to store the result from shell executions. 
	 * It extends the {@link Data} class.
	 */
	public static class Result extends Data<Result> {
		private Integer mResultCode;
		private Integer[] mValidResults;
		private Integer mCommandNumber;
		
		public Result(String[] lines, Integer result, Integer[] validResults, Integer commandNumber) {
			super(lines);
			
			mResultCode = result;
			mValidResults = validResults;
			mCommandNumber = commandNumber;
		}

		/**
		 * Get the result code from the shell execution.
		 */
		public Integer getResultCode() {
			return mResultCode;
		}

		/**
		 * Compare the result code with {@link Shell#addResultCode(Integer)} to determine 
		 * whether or not the execution was a success. 
		 */
		public Boolean wasSuccessful() {
			for (int i=0; i < mValidResults.length; i++) {
				if ((int) mValidResults[i] == (int) mResultCode) {
					return true;
				}
			}
			
			return false;
		}
		
		/**
		 * Get the command number that produced a successful result. 
		 */
		public Integer getCommandNumber() {
			return mCommandNumber;
		}
	}
	
	/**
	 * This is mostly an internal class that is used to collect data from the 
	 * {@link ShellStreamer}. But since there really is no reason for making this private, it's not.
	 * But should this class be extended and parsed to methods in {@link Shell}, it might be a good idea to review the source 
	 * for it first. It is used to make synchronized calls to {@link ShellStreamer} that normally works asynchronized.
	 */
	public static class StreamCollector implements StreamListener {
		protected final Object mLock = new Object();
		
		protected String[] mAttempts;
		protected Set<Integer> mValidResults;
		protected OnShellValidateListener mListener;
		
		protected volatile boolean mHasResult = false;
		protected volatile int mAttemptNumber = -1;
		protected volatile int mResultCode = 0;
		protected volatile List<String> mOutputLines = new ArrayList<String>();
		
		public StreamCollector(String[] attempts, Set<Integer> resultCodes, OnShellValidateListener validater) {
			mAttempts = attempts;
			mValidResults = resultCodes;
			mListener = validater;
			
			if (mValidResults == null || mValidResults.size() == 0) {
				mValidResults = new HashSet<Integer>();
				mValidResults.add(0);
			}
		}

		@Override
		public void onStreamStart(ShellStreamer shell) {
			mAttemptNumber += 1;
			mOutputLines.clear();
			
			if(Common.DEBUG)Log.d(TAG, "onStreamStart: Executing attempt " + (mAttemptNumber + 1) + " of " + mAttempts.length);
			
			if (mAttemptNumber < mAttempts.length) {
				if(Common.DEBUG)Log.d(TAG, "onStreamStart: Executing the command '" + mAttempts[ mAttemptNumber ] + "'");
				
				shell.writeLine(mAttempts[ mAttemptNumber ]);
			}
			
			shell.stopStream();
		}

		@Override
		public void onStreamInput(ShellStreamer shell, String outputLine) {
			if(Common.DEBUG)Log.d(TAG, "onStreamInput: " + (outputLine != null ? (outputLine.length() > 50 ? outputLine.substring(0, 50) + " ..." : outputLine) : "NULL"));
			
			mOutputLines.add(outputLine);
		}

		@Override
		public void onStreamStop(ShellStreamer shell, int resultCode) {
			if(Common.DEBUG)Log.d(TAG, "onStreamStop: The command finished with the result code '" + resultCode + "'");
			
			mResultCode = resultCode;
			
			if (mAttemptNumber < mAttempts.length) {
				if ((mListener != null && mListener.onShellValidate(mAttempts[ mAttemptNumber ], mResultCode, mOutputLines, mValidResults)) || mValidResults.contains(mResultCode)) {
					/*
					 * The validater might have it's own result code that is not in the array
					 */
					mValidResults.add(mResultCode);
					
				} else {
					shell.repeatStream(); return;
				}
			}
			
			releaseResult();
		}
		
		protected void releaseResult() {
			if (!mHasResult) {
				mHasResult = true;
				
				synchronized (mLock) {
					mLock.notifyAll();
				}
			}
		}
		
		public boolean waitForResult(long timeout) {
			synchronized(mLock) {
				while (!mHasResult) {
					long timeoutMilis = timeout > 0 ? System.currentTimeMillis() + timeout : 0l;
					
					try {
						if(Common.DEBUG)Log.d(TAG, "waitForResult: Waiting for result to be released");
						
						mLock.wait(timeout);
						
					} catch (InterruptedException e) {}
					
					if (timeout > 0 && (timeoutMilis - System.currentTimeMillis()) < 0) {
						if(Common.DEBUG)Log.d(TAG, "waitForResult: Result was not released. ShellStreamer timedout after " + (timeout / 1000) + " seconds");
						
						return false;
					}
				}
				
				if(Common.DEBUG)Log.d(TAG, "waitForResult: Releasing result");
				
				return true;
			}
		}
		
		public Result getResult() {
			if (mHasResult) {
				return new Result(mOutputLines.toArray(new String[mOutputLines.size()]), mResultCode, mValidResults.toArray(new Integer[mValidResults.size()]), mAttemptNumber);
			}
			
			return null;
		}
	}
	
	/**
	 * A class containing automatically created shell attempts and links to both {@link Shell#executeAsync(String[], Integer[], OnShellResultListener)} and {@link Shell#execute(String[], Integer[])} <br /><br />
	 * 
	 * All attempts are created based on {@link Common#BINARIES}. <br /><br />
	 * 
	 * Example: String("ls") would become String["ls", "busybox ls", "toolbox ls"] if {@link Common#BINARIES} equals String[null, "busybox", "toolbox"].<br /><br />
	 * 
	 * You can also apply the keyword %binary if you need to apply the binaries to more than the beginning of a command. <br /><br />
	 * 
	 * Example: String("(%binary test -d '%binary pwd') || exit 1") would become String["(test -d 'pwd') || exit 1", "(busybox test -d 'busybox pwd') || exit 1", "(toolbox test -d 'toolbox pwd') || exit 1"]
	 * 
	 * @see Shell#createAttempts(String)
	 */
	public class Attempts {
		protected String[] mAttempts;
		protected Integer[] mResultCodes;
		protected OnShellValidateListener mValidateListener;
		protected OnShellResultListener mResultListener;
		
		protected Attempts(String command) {
			if (command != null) {
				Integer pos = 0;
				mAttempts = new String[ Common.BINARIES.length ];
				
				for (String binary : Common.BINARIES) {
					if (command.contains("%binary ")) {
						mAttempts[pos] = command.replaceAll("%binary ", (binary != null && binary.length() > 0 ? binary + " " : ""));
						
					} else {
						mAttempts[pos] = (binary != null && binary.length() > 0 ? binary + " " : "") + command;
					}
					
					pos += 1;
				}
			}
		}
		
		public Attempts setValidateListener(OnShellValidateListener listener) {
			mValidateListener = listener; return this;
		}
		
		public Attempts setResultListener(OnShellResultListener listener) {
			mResultListener = listener; return this;
		}
		
		public Attempts setResultCodes(Integer... resultCodes) {
			mResultCodes = resultCodes; return this;
		}
		
		public Result execute(OnShellValidateListener listener) {
			return setValidateListener(listener).execute();
		}
		
		public Result execute() {
			return Shell.this.execute(mAttempts, mResultCodes, mValidateListener);
		}
		
		public void executeAsync(OnShellResultListener listener) {
			setResultListener(listener).executeAsync();
		}
		
		public void executeAsync() {
			Shell.this.executeAsync(mAttempts, mResultCodes, mValidateListener, mResultListener);
		}
	}
	
	/**
	 * Establish a {@link ShellStreamer} connection.
	 * 
	 * @param requestRoot
	 *     Whether or not to request root privileges for the shell connection
	 */
	public Shell(final Boolean requestRoot) {
		mResultCodes.add(0);
		mAllowDisconnect = false;
		mIsRoot = requestRoot;
		mStream = new ShellStreamer();
		mStream.addConnectionListener(new ConnectionListener(){
			@Override
			public void onShellConnected(ShellStreamer shell) {
				if(Common.DEBUG)Log.d(TAG, "Listener: ShellStreamer connection has been established");
				
				mInstances.add(Shell.this);
			}

			@Override
			public void onShellDisconnected(ShellStreamer shell) {
				if(Common.DEBUG)Log.d(TAG, "Listener: ShellStreamer connection has been disconnected");
				
				mInstances.remove(Shell.this);
				
				if (mIsConnected && !mAllowDisconnect) {
					mStream = shell;

					if (mStream.connect(requestRoot)) {
						return;
					}
				}
				
				shell.removeConnectionListener(this);
				
				mIsConnected = false;
				
				for (OnShellConnectionListener reciever : mConnectionRecievers) {
					reciever.onShellDisconnect();
				}
			}
		});
		
		/*
		 * Kutch superuser daemon mode sometimes has problems connecting the first time.
		 * So we will give it two tries before giving up.
		 */
		for (int i=0; i < 2; i++) {
			if(Common.DEBUG)Log.d(TAG, "Construct: Running connection attempt number " + (i+1));
			
			if (mStream.connect(requestRoot)) {
				mIsConnected = true; break;
			}
		}
	}
	
	/**
	 * Execute a shell command.
	 * 
	 * @see Shell#execute(String[], Integer[])
	 * 
	 * @param command
	 *     The command to execute
	 */
	public Result execute(String command) {
		return execute(new String[]{command}, null, null);
	}
	
	/**
	 * Execute a range of commands until one is successful.
	 * 
	 * @see Shell#execute(String[], Integer[])
	 * 
	 * @param commands
	 *     The commands to try
	 */
	public Result execute(String[] commands) {
		return execute(commands, null, null);
	}
	
	/**
	 * Execute a range of commands until one is successful.<br /><br />
	 * 
	 * Android shells differs a lot from one another, which makes it difficult to program shell scripts for it. 
	 * This method can help with that by trying different commands until one works. <br /><br />
	 * 
	 * <code>Shell.execute( new String(){"cat file", "toolbox cat file", "busybox cat file"} );</code><br /><br />
	 * 
	 * Whether or not a command was successful, depends on {@link Shell#addResultCode(Integer)} which by default only contains '0'. 
	 * The command number that was successful can be checked using {@link Result#getCommandNumber()}.
	 * 
	 * @param commands
	 *     The commands to try
	 *     
	 * @param resultCodes
	 *     Result Codes representing successful execution. These will be temp. merged with {@link Shell#addResultCode(Integer)}. 
	 *     
	 * @param validater
	 *     A {@link OnShellValidateListener} instance or NULL
	 */	
	public Result execute(String[] commands, Integer[] resultCodes, OnShellValidateListener validater) {
		if (mIsConnected) {
			Set<Integer> codes = new HashSet<Integer>(mResultCodes);
			
			if (resultCodes != null) {
				Collections.addAll(codes, resultCodes);
			}

			return execute( new StreamCollector(commands, codes, validater) );
		}
		
		return null;
	}
	
	/**
	 * Start a stream on this instance's {@link ShellStreamer} using the default or a custom version of 
	 * {@link StreamCollector}.
	 * 
	 * @return
	 * 		{@link Result} collected by the parsed {@link StreamCollector}
	 */
	public Result execute(StreamCollector collector) {
		if (mIsConnected) {
			if (mStream.startStream(collector)) {
				if (collector.waitForResult(mShellTimeout)) {
					return collector.getResult();
					
				} else {
					if(Common.DEBUG)Log.d(TAG, "execute: The shell timedout, doing a force reconnect");
					
					/*
					 * Something is wrong, reconnect to the shell.
					 */
					mStream.disconnect();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Execute a shell command asynchronous.
	 * 
	 * @see Shell#executeAsync(String[], Integer[], OnShellResultListener)
	 * 
	 * @param command
	 *     The command to execute
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellResultListener} callback instance
	 */
	public void executeAsync(String command, OnShellResultListener listener) {
		executeAsync(new String[]{command}, null, null, listener);
	}
	
	/**
	 * Execute a range of commands asynchronous until one is successful.
	 * 
	 * @see Shell#executeAsync(String[], Integer[], OnShellResultListener)
	 * 
	 * @param commands
	 *     The commands to try
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellResultListener} callback instance
	 */
	public void executeAsync(String[] commands, OnShellResultListener listener) {
		executeAsync(commands, null, null, listener);
	}
	
	/**
	 * Execute a range of commands asynchronous until one is successful.
	 * 
	 * @see Shell#execute(String[], Integer[])
	 * 
	 * @param commands
	 *     The commands to try
	 *     
	 * @param resultCodes
	 *     Result Codes representing successful execution. These will be temp. merged with {@link Shell#addResultCode(Integer)}.
	 *     
	 * @param validater
	 *     A {@link OnShellValidateListener} instance or NULL
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellResultListener} callback instance
	 */
	public synchronized void executeAsync(final String[] commands, final Integer[] resultCodes, final OnShellValidateListener validater, final OnShellResultListener listener) {
		if(Common.DEBUG)Log.d(TAG, "executeAsync: Starting an async shell execution");

		new Thread() {
			@Override
			public void run() {
				Result result = Shell.this.execute(commands, resultCodes, validater);
				
				listener.onShellResult(result);
			}
			
		}.start();
	}
	
	/**
	 * Start a stream on this instance's {@link ShellStreamer} asynchronous using the default or a custom version of 
	 * {@link StreamCollector}.
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellResultListener} callback instance
	 */
	public synchronized void executeAsync(final StreamCollector collector, final OnShellResultListener listener) {
		if(Common.DEBUG)Log.d(TAG, "executeAsync: Starting an async shell execution");

		new Thread() {
			@Override
			public void run() {
				Result result = Shell.this.execute(collector);
				
				listener.onShellResult(result);
			}
			
		}.start();
	}
	
	/**
	 * For internal usage
	 */
	public static void sendBroadcast(String key, Bundle data) {
		for (Shell instance : mInstances) {
			instance.broadcastReciever(key, data);
		}
	}
	
	/**
	 * For internal usage
	 */
	protected void broadcastReciever(String key, Bundle data) {
		for (OnShellBroadcastListener recievers : mBroadcastRecievers) {
			recievers.onShellBroadcast(key, data);
		}
	}
	
	/**
	 * For internal usage
	 */
	public void addBroadcastListener(OnShellBroadcastListener listener) {
		mBroadcastRecievers.add(listener);
	}
	
	/**
	 * Add a shell connection listener. This callback will be invoked whenever the connection to
	 * the shell changes. 
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellConnectionListener} callback instance
	 */
	public void addShellConnectionListener(OnShellConnectionListener listener) {
		mConnectionRecievers.add(listener);
	}
	
	/**
	 * Remove a shell connection listener from the stack. 
	 * 
	 * @param listener
	 *     A {@link Shell.OnShellConnectionListener} callback instance
	 */
	public void removeShellConnectionListener(OnShellConnectionListener listener) {
		mConnectionRecievers.remove(listener);
	}
	
	/**
	 * Check whether or not root was requested for this shell.
	 */
	public Boolean isRoot() {
		return mIsRoot;
	}
	
	/**
	 * Check whether or not a shell connection was established. 
	 */
	public Boolean isConnected() {
		return mIsConnected;
	}
	
	/**
	 * Get the current shell execution timeout. 
	 * This is the time in milliseconds from which an execution is killed in case it has stalled. 
	 */
	public Integer getTimeout() {
		return mShellTimeout;
	}
	
	/**
	 * Change the shell execution timeout. This should be in milliseconds. 
	 * If this is set to '0', there will be no timeout. 
	 */
	public void setTimeout(Integer timeout) {
		if (timeout >= 0) {
			mShellTimeout = timeout;
		}
	}
	/**
	 * Add another result code that represent a successful execution. By default only '0' is used, since 
	 * most shell commands uses '0' for success and '1' for error. But some commands uses different values, like 'cat' 
	 * that uses '130' as success when piping content. 
	 * 
	 * @param resultCode
	 *     The result code to add to the stack
	 */
	public void addResultCode(Integer resultCode) {
		mResultCodes.add(resultCode);
	}
	
	/**
	 * Remove a result code from the stack.
	 * 
	 * @see Shell#addResultCode(Integer)
	 * 
	 * @param resultCode
	 *     The result code to remove from the stack
	 */
	public void removeResultCode(Integer resultCode) {
		mResultCodes.remove(resultCode);
	}
	
	/**
	 * Reset the stack containing result codes and set it back to default only containing '0'.
	 * 
	 * @see Shell#addResultCode(Integer)
	 */
	public void resetResultCodes() {
		mResultCodes.clear();
		mResultCodes.add(0);
	}
	
	/**
	 * Close the shell connection using 'exit 0' if possible, or by force and release all data stored in this instance. 
	 */
	public void destroy() {
		if (mStream != null) {
			/*
			 * Do not auto-reconnect
			 */
			mAllowDisconnect = true;
			
			if (mStream.isBusy()) {
				if(Common.DEBUG)Log.d(TAG, "destroy: Destroying the stream");
				
				mStream.disconnect();
				
			} else {
				if(Common.DEBUG)Log.d(TAG, "destroy: Making a clean exit on the stream");
				
				execute("exit 0");
			}
			
			mIsConnected = false;
			mStream = null;
			mInstances.remove(this);
			mBroadcastRecievers.clear();
		}
	}
	
	/**
	 * Locate whichever toolbox in {@value Common#BINARIES} that supports a specific command.<br /><br />
	 * 
	 * Example: String("cat") might return String("busybox cat") or String("toolbox cat")
	 * 
	 * @param bin
	 *     The command to check
	 */
	public String findCommand(String bin) {
		if (!mBinaries.containsKey(bin)) {
			for (String toolbox : Common.BINARIES) {
				String cmd = toolbox != null && toolbox.length() > 0 ? toolbox + " " + bin : bin;
				Result result = execute( cmd + " -h" );
					
				if (result != null) {
					String line = result.getLine();
					
					if (!line.endsWith("not found") && !line.endsWith("such tool")) {
						mBinaries.put(bin, cmd); break;
					}
				}
			}
		}
		
		return mBinaries.get(bin);
	}
	
	/**
	 * Create a new instance of {@link Attempts}
	 * 
	 * @param command
	 *     The command to convert into multiple attempts
	 */
	public Attempts createAttempts(String command) {
		if (command != null) {
			return new Attempts(command);
		}
		
		return null;
	}
	
	/**
	 * Open a new RootFW {@link FileReader}. This is the same as {@link FileReader#FileReader(Shell, String)}.
	 * 
	 * @param file
	 *     Path to the file
	 *     
	 * @return
	 *     NULL if the file could not be opened
	 */
	public FileReader getFileReader(String file) {
		try {
			return new FileReader(this, file);
			
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Open a new RootFW {@link FileWriter}. This is the same as {@link FileWriter#FileWriter(Shell, String, boolean)}.
	 * 
	 * @param file
	 *     Path to the file
	 *     
	 * @param append
	 *     Whether or not to append new content to existing content
	 *     
	 * @return
	 *     NULL if the file could not be opened
	 */
	public FileWriter getFileWriter(String file, Boolean append) {
		try {
			return new FileWriter(this, file, append);
			
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Get a new {@link File} instance.
	 * 
	 * @param file
	 *     Path to the file or directory
	 */
	public File getFile(String file) {
		return new File(this, file);
	}
	
	/**
	 * Get a new {@link Filesystem} instance.
	 */
	public Filesystem getFilesystem() {
		return new Filesystem(this);
	}
	
	/**
	 * Get a new {@link Disk} instance.
	 * 
	 * @param disk
	 *     Path to a disk, partition or a mount point
	 */
	public Disk getDisk(String disk) {
		return new Disk(this, disk);
	}
	
	
	/**
	 * Get a new {@link Device} instance.
	 */
	public Device getDevice() {
		return new Device(this);
	}
	
	/**
	 * Get a new {@link Process} instance.
	 * 
	 * @param process
	 *     The name of the process
	 */
	public Process getProcess(String process) {
		return new Process(this, process);
	}
	
	/**
	 * Get a new {@link Process} instance.
	 * 
	 * @param pid
	 *     The process id
	 */
	public Process getProcess(Integer pid) {
		return new Process(this, pid);
	}
	
	/**
	 * Get a new {@link Memory} instance.
	 */
	public Memory getMemory() {
		return new Memory(this);
	}
	
	/**
	 * Get a new {@link CompCache} instance.
	 */
	public CompCache getCompCache() {
		return new CompCache(this);
	}
	
	/**
	 * Get a new {@link Swap} instance.
	 * 
	 * @param device
	 *     The /dev/ swap device
	 */
	public Swap getSwap(String device) {
		return new Swap(this, device);
	}
}
