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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * This class replaces the old {@link ShellStream}.<br /><br />
 * 
 * This works as an extended version of Java's {@link Process} class. 
 * It will establish a shell connection that, unlike {@link Process}, can handle multiple 
 * streams without the need to connect/disconnect each time. <br /><br />
 * 
 * It also introduces simple listener interfaces that makes it much easier to execute commands, collect data and 
 * handle the connections.<br /><br />
 * 
 * This class provides a constant I/O stream to the shell. If you want to run single commands and collect the output for each, 
 * you should instead look at the {@link Shell} class. 
 */
public class ShellStreamer {
	public static final String TAG = Common.TAG + ".ShellStream";
	
	protected final Object mConncetionLock = new Object();
	
	protected static int mThreadCount = 0;
	protected static String mCommandEnd = "EOL:a00c38d8:EOL";
	
	protected volatile boolean mIsRoot = false;
	protected volatile boolean mIsBusy = false;
	protected volatile boolean mRepeatStream = false;

	protected volatile Process mConnection;
	
	protected volatile DataOutputStream mStdInput;
	protected volatile BufferedReader mStdOutput;
	
	protected volatile QueueHandler mQueueHandler;
	
	protected final Set<ConnectionListener> mConnectionListeners = new HashSet<ConnectionListener>();
	protected final Set<StreamListener> mStreamListeners = new HashSet<StreamListener>();
	
	/**
	 * Listener interface that will provide notice about this streams connection state.
	 * 
	 * @see ShellStreamer#addConnectionListener(ConnectionListener)
	 * @see ShellStreamer#removeConnectionListener(ConnectionListener)
	 */
	public static interface ConnectionListener {
		/**
		 * Called when a shell connection has been established
		 * 
		 * @param shell
		 * 		The {@link ShellStreamer} instance that called this method
		 */
		public void onShellConnected(ShellStreamer shell);
		
		/**
		 * Called when a shell connection has been closed or lost
		 * 		The {@link ShellStreamer} instance that called this method
		 */
		public void onShellDisconnected(ShellStreamer shell);
	}
	
	/**
	 * Listener interface that will provide details and data from streams<br /><br />
	 * 
	 * This interface can be added both locally or globally. This means that it can be added for 
	 * all streams that is started in this instance, or it can be added per stream individually. 
	 * 
	 * @see ShellStreamer#addStreamListener(StreamListener)
	 * @see ShellStreamer#removeStreamListener(StreamListener)
	 * 
	 * @see ShellStreamer#startStream(StreamListener)
	 */
	public static interface StreamListener {
		/**
		 * Called when a new stream has been started (Added from the queue)<br /><br />
		 * 
		 * All streams that is started using {@link ShellStreamer#startStream(StreamListener)} 
		 * is put into a queue. This method is called when the stream is pulled from the queue and started. 
		 * It is not called when added to the queue using {@link ShellStreamer#startStream(StreamListener)}.
		 * 
		 * @param shell
		 * 		The {@link ShellStreamer} instance that handles this stream
		 */
		public void onStreamStart(ShellStreamer shell);
		
		/**
		 * Called each time a new line has been printed to the shell's output stream (stdout).<br /><br />
		 * 
		 * At this point {@link #onStreamStart(ShellStreamer)} has been called. 
		 * 
		 * @param shell
		 * 		The {@link ShellStreamer} instance that handles this stream
		 * 
		 * @param outputLine
		 * 		The output line from the shell's output stream (stdout)
		 */
		public void onStreamInput(ShellStreamer shell, String outputLine);
		
		/**
		 * Called when a stream is stopped
		 * 
		 * @param shell
		 * 		The {@link ShellStreamer} instance that handles this stream
		 * 
		 * @param resultCode
		 * 		The result code from the last executed command in the stream
		 */
		public void onStreamStop(ShellStreamer shell, int resultCode);
	}
	
	/**
	 * Internal class that is used to handle the stream queue
	 */
	private final class QueueHandler extends Handler {
		public final int MSG_DISCONNECTED = -1;
		public final int MSG_CONNECTED = 1;
		public final int MSG_EXECUTE = 2;
		
        public QueueHandler(Looper looper) {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg) {
        	mIsBusy = msg.what == MSG_EXECUTE;
        	
        	switch (msg.what) {
	        	case MSG_DISCONNECTED: {
	        		getLooper().quit();
	        		
	        		for (ConnectionListener listener : mConnectionListeners) {
	        			listener.onShellDisconnected(ShellStreamer.this);
	        		}
	        		
	        		break;
	        	}
	        	
	        	case MSG_CONNECTED: {
	        		for (ConnectionListener listener : mConnectionListeners) {
	        			listener.onShellConnected(ShellStreamer.this);
	        		}
	        		
	        		break;
	        	}
	        	
	        	case MSG_EXECUTE: {
	        		String output = null;
	        		StreamListener listener = (StreamListener) msg.obj;
	        		int resultCode = 0;
	        		
	        		try {
	        			do {
	        				mRepeatStream = false;
	        				
			        		if (listener != null) {
			        			listener.onStreamStart(ShellStreamer.this);
			        		}
			        		
			        		for (StreamListener streamListener : mStreamListeners) {
			        			streamListener.onStreamStart(ShellStreamer.this);
			        		}
		        			
		        			while (mStdOutput != null && (output = mStdOutput.readLine()) != null) {
								if (output.contains(mCommandEnd)) {
									try {
										resultCode = 1;
										
										if (output.startsWith(mCommandEnd)) {
											resultCode = Integer.parseInt(output.substring(mCommandEnd.length()+1));
										}
										
									} catch (Throwable e) {
										Log.w(TAG, e.getMessage(), e);
									}
									
									break;
									
								} else {
					        		for (StreamListener streamListener : mStreamListeners) {
					        			streamListener.onStreamInput(ShellStreamer.this, output);
					        		}
					        		
					        		if (listener != null) {
					        			listener.onStreamInput(ShellStreamer.this, output);
					        		}
								}
		        			}
		        			
			        		for (StreamListener streamListener : mStreamListeners) {
			        			streamListener.onStreamStop(ShellStreamer.this, resultCode);
			        		}
			        		
			        		if (listener != null) {
			        			listener.onStreamStop(ShellStreamer.this, resultCode);
			        		}
			        		
			        		if (!isConnected()) {
			        			mRepeatStream = false;
			        			disconnect();
			        		}
			        	
	        			} while (mRepeatStream);
	        			
	        		} catch (IOException e) {
        				if (mConnection != null) {
        					disconnect();
        				}
	        		}
	        	}
        	}
        	
        	mIsBusy = false;
        }
	}
	
	/**
	 * Add a new {@link ShellStreamer.ConnectionListener} listener to this instance
	 * 
	 * @param listener
	 */
	public void addConnectionListener(ConnectionListener listener) {
		synchronized(mConnectionListeners) {
			mConnectionListeners.add(listener);
		}
	}
	
	/**
	 * Remove a {@link ShellStreamer.ConnectionListener} listener from this instance
	 * 
	 * @param listener
	 */
	public void removeConnectionListener(ConnectionListener listener) {
		synchronized(mConnectionListeners) {
			mConnectionListeners.remove(listener);
		}
	}
	
	/**
	 * Add a new {@link ShellStreamer.StreamListener} listener to this instance<br /><br />
	 * 
	 * Note that this will add a global listener that will return details and data from all 
	 * streams that is started in this instance. Consider using a local listener parsed to {@link #startStream(StreamListener)}
	 * 
	 * @param listener
	 * 
	 * @see #startStream(StreamListener)
	 */
	public void addStreamListener(StreamListener listener) {
		synchronized(mStreamListeners) {
			mStreamListeners.add(listener);
		}
	}
	
	/**
	 * Remove a {@link ShellStreamer.StreamListener} listener from this instance
	 * 
	 * @param listener
	 */
	public void removeStreamListener(StreamListener listener) {
		synchronized(mStreamListeners) {
			mStreamListeners.remove(listener);
		}
	}
	
	/**
	 * Establish a connection to a shell and start the stream queue
	 * 
	 * @param asRoot
	 * 		Whether to use a root shell or a normal user shell
	 * 
	 * @return
	 * 		<code>TRUE</code> if the shell was successfully established
	 */
	public boolean connect(boolean asRoot) {
		synchronized(mConncetionLock) {
			if (!isConnected()) {
				try {
					ProcessBuilder builder = new ProcessBuilder(asRoot ? "su" : "sh");
					builder.redirectErrorStream(true);

					mConnection = builder.start();
					mIsRoot = asRoot;
					mStdInput = new DataOutputStream(mConnection.getOutputStream());
					mStdOutput = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
					
					HandlerThread thread = new HandlerThread( "ShellStream_" + (++mThreadCount) );
					thread.start();

					mQueueHandler = new QueueHandler( thread.getLooper() );
					mQueueHandler.sendEmptyMessage(mQueueHandler.MSG_CONNECTED);
					
				} catch (IOException e) {
					Log.w(TAG, e.getMessage(), e);
					
					if (mConnection != null) {
						mConnection.destroy();
						mConnection = null;
					}
					
					return false;
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Destroy the shell connection and stop the stream queue. <br /><br />
	 * 
	 * Unless you are running a daemon or similar that occupies the stream, consider 
	 * using a more clean {@link #writeLine(String)} by parsing <code>exit 0</code><br /><br />
	 * 
	 * A clean exit will also ensure that current streams get's to finish before the shell is terminated. 
	 */
	public void disconnect() {
		synchronized(mConncetionLock) {
			if (mConnection != null) {
				mQueueHandler.removeMessages(mQueueHandler.MSG_EXECUTE);
				
				mConnection.destroy();
				mConnection = null;
				
				try {
					mStdInput.close();
					mStdInput = null;
					
					mStdOutput.close();
					mStdOutput = null;
					
				} catch (IOException e) {}
				
				mQueueHandler.sendEmptyMessage(mQueueHandler.MSG_DISCONNECTED);
			}
		}
	}
	
	/**
	 * Check if a connection to the shell has been established
	 * 
	 * @return
	 * 		<code>TRUE</code> if the connection is established
	 */
	public boolean isConnected() {
		synchronized(mConncetionLock) {
			if (mConnection != null) {
				try {
					mConnection.exitValue();
					
				} catch (IllegalThreadStateException e) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	/**
	 * Check if the queue is in the process of running shell streams, in which case the shell is currently busy.
	 * 
	 * @return
	 * 		<code>TRUE</code> if the queue is processing streams
	 */
	public boolean isBusy() {
		return mIsBusy || mQueueHandler.hasMessages(mQueueHandler.MSG_EXECUTE);
	}
	
	/**
	 * Add a new Stream Request to the queue. It is important to note that this will send a stream request 
	 * to a stream queue handler asynchronized. This means that the stream cannot be ensured to have been started 
	 * when this method returns. Alsways use the {@link StreamListener} to handle the actual stream work.<br /><br />
	 * 
	 * A stream is not equel to a shell connection. You must in any case first establish a connection using {@link #connect(boolean)}. 
	 * A stream will just start a new listener on <code>stdout</code> and allow writing to be made on <code>stdin</code>. Each new line from 
	 * <code>stdout</code> will be sent to the {@link StreamListener} that was parsed to this specific stream. <br /><br />
	 * 
	 * A stream will continue to be active until a call is made to {@link #stopStream()}. Stopping a stream will not disconnect from the shell. 
	 * It will just stop monitoring <code>stdout</code> for this {@link StreamListener} and allow others in the queue to start theirs. Note however 
	 * that a stream is stopped by writing a specific pattern to the shell. This means that if the shell currently has a process running in it (daemon or similar), 
	 * the {@link #stopStream()} call will not work as <code>stdout</code> will be busy and will not receive the pattern unless/until the process stops. It is up to the user of the stream 
	 * to ensure that the stream can be stopped. For example if you use <code>cat</code> to add content to a file, you can use <code>"cat << 'EOF' > file"</code> 
	 * to do so and end it using <code>"EOF"</code> at the end, before calling {@link #stopStream()}. If the <code>ShellStreamer</code> is not meant to be reused for other work, 
	 * you can always just call {@link #disconnect()} to destroy the connection. 
	 * 
	 * @param listener
	 * 		A {@link StreamListener} that should receive status and content of the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if the request was sent to the queue
	 */
	public boolean startStream(StreamListener listener) {
		synchronized(mConncetionLock) {
			if (isConnected()) {
				mQueueHandler.obtainMessage(mQueueHandler.MSG_EXECUTE, listener).sendToTarget(); return true;
			}
			
			return false;
		}
	}
	
	/**
	 * Send a stop request to the current running stream.<br /><br />
	 * 
	 * If you need/want to target a specific stream, call this method from inside a {@link StreamListener} belonging to that stream. 
	 * Otherwise you will just target a random stream, or none if one is still in the process of being started. 
	 */
	public boolean stopStream() {
		return writeLine("echo " + mCommandEnd + " $?");
	}
	
	/**
	 * Once a stream is finished and have called {@link StreamListener#onStreamStop(ShellStreamer, int)}, if 
	 * this has been called in the mean time, the stream will return to the beginning and be processed once more. <br /><br />
	 * 
	 * Be careful not to start an un-ending loop as this will also block other streams in the queue from being processed. <br /><br />
	 * 
	 * If you need/want to target a specific stream, call this method from inside a {@link StreamListener} belonging to that stream. 
	 * Otherwise you will just target a random stream, or none if one is still in the process of being started. 
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean repeatStream() {
		if (isBusy()) {
			return (mRepeatStream = true);
		}
		
		return false;
	}
	
	/**
	 * This is a simple help method for writing lines to the stream. 
	 * The difference being that this method will append a line break to each parsed line. 
	 * 
	 * @see {@link #write(byte[])}
	 * 
	 * @param out
	 * 		Line to be written to the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean writeLine(String line) {
		return write( (line + "\n").getBytes() );
	}
	
	/**
	 * @see {@link #write(byte[])}
	 * 
	 * @param out
	 * 		String to be written to the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean write(String out) {
		return write(out.getBytes());
	}
	
	/**
	 * @see {@link #write(byte[])}
	 * 
	 * @param out
	 * 		Strings to be written to the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean write(String[] out) {
		synchronized(mConncetionLock) {
			if (isBusy()) {
				try {
					for (String str : out) {
						mStdInput.write(str.getBytes());
					}
					
					if (mStdInput != null) {
						mStdInput.flush();
					}
					
					return true;
					
				} catch (IOException e) {
					Log.w(TAG, e.getMessage(), e);
				}
			}
			
			return false;
		}
	}
	
	/**
	 * @see {@link #write(byte[])}
	 * 
	 * @param out
	 * 		Byte to be written to the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean write(byte out) {
		return write( new byte[]{out} );
	}
	
	/**
	 * Write output to the stream <br /><br />
	 * 
	 * Note that this method does not append line breaks<br /><br />
	 * 
	 * If you need/want to target a specific stream, call this method from inside a {@link StreamListener} belonging to that stream. 
	 * Otherwise you will just target a random stream, or none if one is still in the process of being started. 
	 * 
	 * @param out
	 * 		Bytes to be written to the stream
	 * 
	 * @return
	 * 		<code>TRUE</code> if there is a stream to target
	 */
	public boolean write(byte[] out) {
		synchronized(mConncetionLock) {
			if (isBusy()) {
				try {
					mStdInput.write(out);

					if (mStdInput != null) {
						mStdInput.flush();
					}
					
					return true;
					
				} catch (IOException e) {
					Log.w(TAG, e.getMessage(), e);
				}
			}
			
			return false;
		}
	}
}
