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

package com.spazedog.rootfw.container;

import com.spazedog.rootfw.iface.Container;

public class Data implements Container {
	private String[] mData;
	
	/**
	 * Create a new Data instance
	 * <p/>
	 * The data will be split by line separator 
	 * 
	 * @param aData
	 *     Data which should be added to the container
	 */
	public Data(String aData) {
		if (aData != null) {
			mData = aData.split("\n");
		}
	}
	
	/**
	 * Create a new Data instance
	 * 
	 * @param aData
	 *     Data which should be added to the container
	 */
	public Data(String[] aData) {
		mData = aData;
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString() {
		return toString("\n", 0, false);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(String aSeperator, Integer aIndex) {
		return toString(aSeperator, aIndex, false);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(String aSeperator, Boolean aSkipEmpty) {
		return toString(aSeperator, 0, aSkipEmpty);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(Integer aIndex, Boolean aSkipEmpty) {
		return toString("\n", aIndex, aSkipEmpty);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(String aSeperator) {
		return toString(aSeperator, 0, false);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(Integer aIndex) {
		return toString("\n", aIndex, false);
	}
	
	/**
	 * See <code>toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty)</code>
	 */
	public String toString(Boolean aSkipEmpty) {
		return toString("\n", 0, aSkipEmpty);
	}
	
	/**
	 * Convert the data array into a string and return it
	 * 
	 * @param aSeperator
	 *     A separator used to separate each line
	 *     
	 * @param aIndex
	 *     The line number to start from, can also be a negative
	 *     
	 * @param aSkipEmpty
	 *     Do not include empty lines
	 *    
	 * @return
	 *     A string containing the data
	 */
	public String toString(String aSeperator, Integer aIndex, Boolean aSkipEmpty) {
		if (mData != null) {
			Integer count = aIndex < 0 ? (mData.length + aIndex) : aIndex;
			String output = "";

			for (int i=count; i < mData.length; i++) {
				if (!aSkipEmpty || mData[i].trim().length() > 0) {
					output += (i > 0 ? aSeperator : "") + mData[i];
				}
			}
			
			return output;
		}
		
		return null;
	}
	
	/**
	 * Return the data as is in it's original container (array)
	 *    
	 * @return 
	 *     The data array
	 */
	public String[] raw() {
		return mData;
	}
	
	/**
	 * Return the last line of the data.
	 * <p/>
	 * Note that it will sort out any empty lines and return the last line containing any data
	 *    
	 * @return
	 *     The last line or NULL if no none-empty lines exists
	 */
	public String line() {
		return line(-1, true);
	}
	
	/**
	 * Return one line of the data
	 * 
	 * @param aLineNumber
	 *     The line number to return. This also takes negative values which will start from the bottom
	 *    
	 * @return
	 *     The specified line
	 */
	public String line(Integer aLineNumber) {
		return line(aLineNumber, false);
	}
	
	/**
	 * Return one line of the data
	 * 
	 * @param aLineNumber
	 *     The line number to return. This also takes negative values which will start from the bottom
	 *    
	 * @param aSkipEmpty
	 *     Do not include empty lines
	 *    
	 * @return
	 *     The specified line
	 */
	public String line(Integer aLineNumber, Boolean aSkipEmpty) {
		if (length() > 0) {
			Integer count = aLineNumber < 0 ? (mData.length + aLineNumber) : aLineNumber;
			
			while(count >= 0 && count < mData.length) {
				if (!aSkipEmpty || mData[count].trim().length() > 0) {
					return mData[count].trim();
				}
				
				count = aLineNumber < 0 ? (count - 1) : (count + 1);
			}
		}
		
		return null;
	}
	
	/**
	 * Return the number of lines within the container
	 *    
	 * @return
	 *     The number of lines
	 */
	public Integer length() {
		return mData.length == 1 && mData[0].trim().length() == 0 ? 0 : mData.length;
	}
}
