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

public class FileStat implements Container {
	
	private String mName;
	private String mLink;
	private String mType;
	private Integer mUser;
	private Integer mGroup;
	private String mAccess;
	private String mPermission;
	private String mMM;
	private Long mSize;
	
	/**
	 * Create a new FileStat instance
	 * 
	 * @param aName
	 *     The name of the file
	 *    
	 * @param aLink
	 *     Path to the original file if this is a symbolic link
	 *    
	 * @param aType
	 *     The type of file ('d'=>Directory, 'f'=>File, 'b'=>Block Device, 'c'=>Character Device, 'l'=>Symbolic Link)
	 *    
	 * @param aUser
	 *     User ID of the owner
	 *    
	 * @param aGroup
	 *     Group ID of the owner
	 *    
	 * @param aAccess
	 *     The access string like (drwxrwxr-x)
	 *    
	 * @param aPermission
	 *     The file permissions like (0755)
	 *    
	 * @param aMm
	 *     The file Major:Minor number (If this is a Block or Character device file)
	 *    
	 * @param aSize
	 *     The file size in bytes
	 */
	public FileStat(String aName, String aLink, String aType, Integer aUser, Integer aGroup, String aAccess, String aPermission, String aMm, Long aSize) {
		mName = aName;
		mLink = aLink;
		mType = aType;
		mUser = aUser;
		mGroup = aGroup;
		mAccess = aAccess;
		mPermission = aPermission;
		mMM = aMm;
		mSize = aSize;
	}
	
	/** 
	 * @return
	 *     The filename
	 */
	public String name() {
		return mName;
	}
	
	/** 
	 * @return
	 *     The path to the original file if this is a symbolic link
	 */
	public String link() {
		return mLink;
	}
	
	/** 
	 * @return
	 *     The file type ('d'=>Directory, 'f'=>File, 'b'=>Block Device, 'c'=>Character Device, 'l'=>Symbolic Link)
	 */
	public String type() {
		return mType;
	}
	
	/** 
	 * @return
	 *     The owners user id
	 */
	public Integer user() {
		return mUser;
	}
	
	/** 
	 * @return
	 *     The owners group id
	 */
	public Integer group() {
		return mGroup;
	}
	
	/** 
	 * @return
	 *     The files access string like (drwxrwxr-x)
	 */
	public String access() {
		return mAccess;
	}
	
	/** 
	 * @return
	 *     The file permissions like (0755)
	 */
	public String permission() {
		return mPermission;
	}
	
	/** 
	 * @return
	 *     The file Major:Minor number (If this is a Block or Character device file)
	 */
	public String mm() {
		return mMM;
	}
	
	/** 
	 * @return
	 *     The file size in bytes
	 */
	public Long size() {
		return mSize;
	}
}
