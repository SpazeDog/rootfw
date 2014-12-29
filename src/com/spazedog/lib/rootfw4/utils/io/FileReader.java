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

package com.spazedog.lib.rootfw4.utils.io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;

import android.util.Log;

import com.spazedog.lib.rootfw4.Common;
import com.spazedog.lib.rootfw4.Shell;
import com.spazedog.lib.rootfw4.Shell.Result;

/**
 * This class allows you to open a file as root, if needed. 
 * Files that are not protected will be handled by a regular {@link java.io.FileReader} while protected files 
 * will use a shell streamer instead. Both of which will act as a normal reader that can be used together with other classes like {@link BufferedReader} and such. 
 */
public class FileReader extends Reader {
	public static final String TAG = Common.TAG + ".FileReader";

	protected InputStreamReader mStream;
	
	public FileReader(String file) throws FileNotFoundException {
		this(new Shell(true), file);
	}
	
	public FileReader(Shell shell, String file) throws FileNotFoundException {
		File fileObj = new File(file);
		String filePath = fileObj.getAbsolutePath();
		String cmd = "( %binary test -e '" + filePath + "' && echo true ) || ( %binary test ! -e '" + filePath + "' && echo false )";
		Result shellResult = shell.createAttempts(cmd).execute();
		
		if (shellResult != null && "true".equals(shellResult.getLine())) {
			if (fileObj.exists() && fileObj.canRead()) {
				if(Common.DEBUG)Log.d(TAG, "Construct: Opening file '" + filePath + "' using native FileReader");
				
				try {
					mStream = new java.io.FileReader(filePath);
					
				} catch (FileNotFoundException e) {
					throw new FileNotFoundException(e.getMessage());
				}
				
			} else {
				if(Common.DEBUG)Log.d(TAG, "Construct: Opening file '" + filePath + "' using a SuperUser shell InputReader");
				
				ProcessBuilder builder = new ProcessBuilder("su");
				builder.redirectErrorStream(true);
				
				try {
					Process process = builder.start();
					mStream = new InputStreamReader(process.getInputStream());
					
					DataOutputStream stdIn = new DataOutputStream(process.getOutputStream());
					stdIn.write( (shell.getBinary("cat") + " '" + filePath + "'\n").getBytes() );
					stdIn.write( ("exit $?\n").getBytes() );
					stdIn.flush();
					stdIn.close();
					
					try {
						Integer resultCode = process.waitFor();
						
						if (!resultCode.equals(0)) {
							throw new FileNotFoundException("Problems loading the content of the file " + filePath);
						}
						
					} catch (InterruptedException e) {}
					
				} catch (IOException e) {
					throw new FileNotFoundException(e.getMessage());
				}
			}
			
		} else {
			throw new FileNotFoundException("Could not find the file " + filePath);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mark(int readLimit) throws IOException {
		mStream.mark(readLimit);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean markSupported() {
		return mStream.markSupported();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		mStream.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(char[] buffer, int offset, int count) throws IOException {
		return mStream.read(buffer, offset, count);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(CharBuffer target) throws IOException {
		return mStream.read(target);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(char[] buffer) throws IOException {
		return mStream.read(buffer);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		return mStream.read();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long charCount) throws IOException {
		return mStream.skip(charCount);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() throws IOException {
		mStream.reset();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ready() throws IOException {
		return mStream.ready();
	}
}
