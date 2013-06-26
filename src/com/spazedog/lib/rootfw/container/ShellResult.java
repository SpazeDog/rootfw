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

import com.spazedog.lib.rootfw.iface.Container;

public class ShellResult implements Container {
	private Integer mCode;
	private Integer mCommand;
	private Data mOutput;
	
	/**
	 * Create a new ShellResult instance
	 * 
	 * @param aData
	 *     Shell output which will be added to the Data container
	 *    
	 * @param aCode
	 *     Result code from the shell
	 *    
	 * @param aCommand
	 *     The command entry number which exited successfully
	 */
	public ShellResult(String aData, Integer aCode, Integer aCommand) {
		mCode = aCode;
		mCommand = aCommand;
		mOutput = new Data(aData);
	}
	
	/**
	 * Get the shell output
	 * 
	 * @return
	 *     The Data container with the shell output
	 */
	public Data output() {
		return mOutput;
	}
	
	/**
	 * Get the shell result code
	 * 
	 * @return
	 *     The result code
	 */
	public Integer code() {
		return mCode;
	}
	
	/**
	 * Get the command entry number
	 * 
	 * @return
	 *     The command number
	 */
	public Integer command() {
		return mCommand;
	}
}
