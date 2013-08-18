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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.containers.Data;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

/**
 * This class is used to handle files and folder. Each new instance is associated with one file or folder, where all of the tools within can be used to write, read, create, list, remove etc.
 * <br />
 * For the most parts, this extension will use Java Native Code to handle all file operations. A shell is only used whenever the current process does not have permissions to perform the operation.
 * <br />
 * As this class is an extender, is should not be called directly. Instead use RootFW.file().
 * 
 * <dl>
 * <dt><span class="strong">Example:</span></dt>
 * <dd><code><pre>
 * RootFW root = new RootFW();
 * 
 * if (root.connect()) {
 *     root.file("/data/local/tmp").remove();
 *     
 *     root.disconnect();
 * }
 * </pre></code></dd>
 * </dl>
 */
public class FileExtender implements ExtenderGroup {
	
	private final static Pattern oPatternStatSplitter = Pattern.compile("\\|");
	private final static Pattern oPatternStatSearch = Pattern.compile("^([a-z-]+)(?:[ \t]+([0-9]+))?[ \t]+([0-9a-z_]+)[ \t]+([0-9a-z_]+)(?:[ \t]+(?:([0-9]+),[ \t]+)?([0-9]+))?[ \t]+([A-Za-z]+[ \t]+[0-9]+[ \t]+[0-9:]+|[0-9-/]+[ \t]+[0-9:]+)[ \t]+(?:(.*) -> )?(.*)$");
	
	protected ShellExtender mShell;
	
	protected File mFile;
	
	protected Boolean iExists = false;
	protected Boolean iIsFolder = false;
	protected Boolean iRestricted = false;

	/**
	 * Create a new FileExtender instance.
	 * 
	 * @param shell
	 *     An ShellExtender instance from the RootFW connection
	 *     
	 * @param file
	 *     A File object of the file or folder to perform operations on
	 */
	public FileExtender(ShellExtender shell, File file) {
		mShell = shell;
		mFile = file;
		
		// A file/folder can be so restricted that we are not even allowed to check whether it exists without root
		iExists = mFile.exists() ? true : "true".equals( mShell.buildCommands("( %binary test -e '" + mFile.getAbsolutePath() + "' && echo true ) || ( %binary test ! -e '" + mFile.getAbsolutePath() + "' && echo false )").run().getLine() );

		// This can be used in other methods of this class to determine if we can use pure java to get information about it, or if root has to be used for everything. 
		iRestricted = iExists != mFile.exists();
		
		if (iExists) {
			iIsFolder = !iRestricted ? mFile.isDirectory() : "true".equals( mShell.buildCommands("( %binary test -d '" + mFile.getAbsolutePath() + "' && echo true ) || ( %binary test ! -d '" + mFile.getAbsolutePath() + "' && echo false )").run().getLine() );
		}
	}
	
	/**
	 * @return
	 *     <code>True</code> if the item exists and if it is an file
	 */
	public Boolean isFile() {
		return iExists && !iIsFolder;
	}
	
	/**
	 * @return
	 *     <code>True</code> if the item exists and if it is an folder
	 */
	public Boolean isDirectory() {
		return iExists && iIsFolder;
	}
	
	/**
	 * @return
	 *     <code>True</code> if the item exists
	 */
	public Boolean exists() {
		return iExists;
	}
	
	/**
	 * Extract the content from the file and return it
	 * 
	 * @return
	 *     An FileData object containing the file content
	 */
	public FileData read() {
		if (isFile()) {
			if (!iRestricted && mFile.canRead()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(mFile));
					List<String> content = new ArrayList<String>();
					String line;
					
					while ((line = reader.readLine()) != null) {
						content.add(line);
					}
					
					reader.close();
					
					return new FileData( content.toArray( new String[ content.size() ] ) );
					
				} catch(Throwable e) {}
				
			} else {
				ShellResult result = mShell.buildCommands("%binary cat '" + mFile.getAbsolutePath() + "' 2> /dev/null").run();
				
				if (result.wasSuccessful()) {
					return new FileData( result.getArray() );
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Extract the first line of the file
	 * 
	 * @return
	 *     The first line of the file
	 */
	public String readOneLine() {
		if (isFile()) {
			if (!iRestricted && mFile.canRead()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(mFile));
					String line = reader.readLine();
					reader.close();
					
					return line;
					
				} catch (Throwable e) {}
				
			} else {
				ShellResult result = mShell.buildAttempts("%binary sed -n '1p' '" + mFile.getAbsolutePath() + "' 2> /dev/null", "%binary cat '" + mFile.getAbsolutePath() + "' 2> /dev/null").run();
				
				return result.wasSuccessful() ? result.getLine(0) : null;
			}
		}
		
		return null;
	}
	
	/**
	 * Search the file line by line to find a match for a specific pattern and return the first match found in the file
	 * 
	 * @param match
	 *     The pattern to search for
	 * 
	 * @return
	 *     The first matched line from the file
	 */
	public String readOneMatch(String match) {
		FileData data = readMatches(match, true);
		
		return data != null ? data.getLine() : null;
	}
	
	/**
	 * @see FileExtender#readMatches(String, Boolean)
	 */
	public FileData readMatches(String match) {
		return readMatches(match, false);
	}
	
	/**
	 * Search the file line by line to find a match for a specific pattern and return all of the matched lines
	 * 
	 * @param match
	 *     The pattern to search for
	 *     
	 * @param firstMatch
	 *     Whether or not to quit on the first found match
	 * 
	 * @return
	 *     An FileData object containing all of the matched lines
	 */
	public FileData readMatches(String match, Boolean firstMatch) {
		if (isFile()) {
			if (!iRestricted && mFile.canRead()) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(mFile));
					List<String> content = new ArrayList<String>();
					String line;
					
					while ((line = reader.readLine()) != null) {
						if (line.contains(match)) {
							content.add(line);
							
							if (firstMatch) {
								break;
							}
						}
					}
					
					reader.close();
					
					return new FileData( content.toArray( new String[ content.size() ] ) );
					
				} catch (Throwable e) {}
				
			} else {
				String[] attempts = firstMatch ? 
						new String[]{"%binary sed -n '/" + match + "/{p;q;}' '" + mFile.getAbsolutePath() + "'", "%binary grep -m 1 '" + match + "' '" + mFile.getAbsolutePath() + "'", "%binary grep '" + match + "' '" + mFile.getAbsolutePath() + "'"} : 
							new String[]{"%binary grep '" + match + "' '" + mFile.getAbsolutePath() + "'"};
						
				ShellResult result = mShell.buildAttempts(attempts).run();
				
				if (result.wasSuccessful()) {
					if (firstMatch) {
						result.sort(0);
					}
					
					return new FileData( result.getArray() );
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see FileExtender#write(String[], Boolean)
	 */
	public Boolean write(String input) {
		return write(input.split("\n"), false);
	}
	
	/**
	 * @see FileExtender#write(String[], Boolean)
	 */
	public Boolean write(String input, Boolean append) {
		return write(input.split("\n"), append);
	}
	
	/**
	 * @see FileExtender#write(String[], Boolean)
	 */
	public Boolean write(String[] input) {
		return write(input, false);
	}
	
	/**
	 * Write data to a file. The data should be an array where each index is a line that should be written to the file. 
	 * <br />
	 * If the file does not already exist, it will created. 
	 * 
	 * @param input
	 *     The data that should be written to the file
	 *     
	 * @param append
	 *     Whether or not to append the data to the existing content in the file
	 * 
	 * @return
	 *     <code>True</code> if the data was successfully written to the file
	 */
	public Boolean write(String[] input, Boolean append) {
		if (!isDirectory()) {
			if (!iRestricted && mFile.canWrite()) {
				try {
					BufferedWriter output = new BufferedWriter(new FileWriter(mFile.getAbsolutePath(), append));
					
					for (int i=0; i < input.length; i++) {
						output.write(input[i]);
						output.newLine();
					}
					
					output.close();
					
					return (iExists = mFile.exists());
					
				} catch(Throwable e) {}
				
			} else {
				ShellResult result = null;
				String redirect = append ? ">>" : ">";
				
				for (int i=0; i < input.length; i++) {
					if ( !(result = mShell.run("echo '' " + redirect + " '" + mFile.getAbsolutePath() + "' 2> /dev/null")).wasSuccessful() ) {
						break;
					}
					
					redirect = ">>";
				}
				
				return iExists && !result.wasSuccessful() ? false : (iExists = result.wasSuccessful());
			}
		}
		
		return false;
	}
	
	/**
	 * Remove a file or folder. This method will remove any content with a folder if the folder is not already empty.
	 * 
	 * @return
	 *     <code>True</code> if the file or folder was deleted
	 */
	public Boolean remove() {
		if (exists()) {
			if (isDirectory() && !iRestricted && mFile.canWrite() && mFile.canRead()) {
				String[] fileList = mFile.list();
				
				if (fileList != null && fileList.length > 0) {
					for (int i=0; i < fileList.length; i++) {
						open(fileList[i]).remove();
					}
				}
			}
			
			if (!mFile.delete()) {
				ShellResult result = mShell.buildCommands("%binary rm -rf '" + mFile.getAbsolutePath() + "'").run();
				
				if (result.wasSuccessful()) {
					iIsFolder = false;
					iExists = false;
					
					return true;
				}
				
			} else {
				iIsFolder = false;
				iExists = false;
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Create a new directory based on the path parsed to this file object
	 * 
	 * @return
	 *     <code>True</code> if the directory was created successfully
	 */
	public Boolean createDirectory() {
		if (!exists()) {
			if (!mFile.mkdir()) {
				ShellResult result = mShell.buildCommands("%binary mkdir '" + mFile.getAbsolutePath() + "'").run();
				
				if (!result.wasSuccessful() || !"true".equals( mShell.buildCommands("( %binary test -d '" + mFile.getAbsolutePath() + "' && echo true ) || ( %binary test ! -d '" + mFile.getAbsolutePath() + "' && echo false )").run().getLine() )) {
					return false;
				}
			}
			
			iIsFolder = true;
			iExists = true;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Open a new FileExtender object with another file or folder relative to the current directory
	 * 
	 * @return
	 *     A new instance of this class representing another relative file or folder
	 */
	public FileExtender open(String file) {
		if (isDirectory()) {
			String path = mFile.getAbsolutePath().endsWith("/") ? mFile.getAbsolutePath().substring(0, mFile.getAbsolutePath().length()-1) : mFile.getAbsolutePath();
			String fileName = file.startsWith("/") ? file.substring(0) : file;
			
			return new FileExtender(mShell, new File(path + "/" + fileName));
		}
		
		return null;
	}
	
	/**
	 * Get the parent directory of this file or folder
	 * 
	 * @return
	 *     A new instance of this class representing the parent directory
	 */
	public FileExtender openParent() {
		return mFile.getParent() != null ? new FileExtender(mShell, new File(mFile.getParent())) : null;
	}
	
	/**
	 * Get information about this file or folder. This will return information like 
	 * size (on files), path to linked file (on links), permissions, group, user etc.
	 * 
	 * @return
	 *     A new {@link FileStat} object with all the file information
	 */
	public FileStat getDetails() {
		if (exists()) {
			FileStat[] stat = getDetailedList(1);
			
			if (stat != null && stat.length > 0) {
				String name = mFile.getName();
				
				if (stat[0].name().equals(".")) {
					stat[0].mName = name;
					
					return stat[0];
					
				} else if (stat[0].name().equals(name)) {
					return stat[0];
					
				} else {
					/* On devices without busybox, we could end up using limited toolbox versions
					 * that does not support the "-a" argument in it's "ls" command. In this case,
					 * we need to do a more manual search for folders.
					 */
					stat = open("../").getDetailedList();
					
					if (stat != null && stat.length > 0) {
						for (int i=0; i < stat.length; i++) {
							if (stat[i].name().equals(name)) {
								return stat[i];
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @see #getDetailedList(Integer)
	 */
	public FileStat[] getDetailedList() {
		return getDetailedList(0);
	}
	
	/**
	 * This is the same as {@link #getDetails()}, only this will provide a whole list 
	 * with information about each item in a directory.
	 * 
	 * @param maxLines
	 *     The max amount of lines to return. This also excepts negative numbers. 
	 * 
	 * @return
	 *     An array of {@link FileStat} object
	 */
	public FileStat[] getDetailedList(Integer maxLines) {
		if (exists()) {
			ShellResult result = mShell.buildAttempts("%binary ls -lna '" + mFile + "'", "%binary ls -la '" + mFile + "'", "%binary ls -ln '" + mFile + "'", "%binary ls -l '" + mFile + "'").run();
			
			if (result.wasSuccessful()) {
				List<FileStat> list = new ArrayList<FileStat>();
				String[] lines = result.trim().getArray();
				Integer maxIndex = (maxLines == null || maxLines == 0 ? lines.length : (maxLines < 0 ? lines.length + maxLines : maxLines));
				
				for (int i=0,indexCount=1; i < lines.length && indexCount <= maxIndex; i++) {
					/* There are a lot of different output from the ls command, depending on the arguments supported, whether we used busybox or toolbox and the versions of the binaries. 
					 * We need some serious regexp help to sort through all of the different output options. 
					 */
					String[] parts = oPatternStatSplitter.split( oPatternStatSearch.matcher(lines[i]).replaceAll("$1|$3|$4|$5|$6|$8|$9") );
					
					if (parts.length == 7) {
						String partType = parts[0].substring(0, 1).equals("-") ? "f" : parts[0].substring(0, 1);
						String partAccess = parts[0];
						Integer partUser = Integer.parseInt(parts[1]);
						Integer partGroup = Integer.parseInt(parts[2]);
						Long partSize = parts[4].equals("null") || !parts[3].equals("null") ? 0L : Long.parseLong(parts[4]);
						String partMM = parts[3].equals("null") ? null : parts[3] + ":" + parts[4];
						String partName = parts[5].equals("null") ? parts[6].substring( parts[6].lastIndexOf("/") + 1 ) : parts[5].substring( parts[5].lastIndexOf("/") + 1 );
						String partLink = parts[5].equals("null") ? null : parts[6];
						String partPermission = "";
						String[] partModAssambler = new String[] {partAccess.substring(1), partAccess.substring(1, 4), partAccess.substring(4, 7), partAccess.substring(7, 10)};
						
						for (int x=0; x < partModAssambler.length; x++) {
							Integer mod = (x == 0 && partModAssambler[x].charAt(2) == 's') || (x > 0 && partModAssambler[x].charAt(0) == 'r') ? 4 : 0;
							mod += (x == 0 && partModAssambler[x].charAt(5) == 's') || (x > 0 && partModAssambler[x].charAt(1) == 'w') ? 2 : 0;
							mod += (x == 0 && partModAssambler[x].charAt(8) == 't') || (x > 0 && partModAssambler[x].charAt(2) == 'x') ? 1 : 0;
							
							partPermission += "" + mod;
						}
						
						if (partName.contains("/")) {
							partName = partName.substring( partName.lastIndexOf("/")+1 );
						}
						
						FileStat stat = new FileStat();
						stat.mAccess = partAccess;
						stat.mPermission = partPermission;
						stat.mUser = partUser;
						stat.mGroup = partGroup;
						stat.mLink = partLink;
						stat.mMM = partMM;
						stat.mName = partName;
						stat.mSize = partSize;
						stat.mType = partType;
						
						list.add(stat);
						
						indexCount++;
					}
				}
				
				return list.toArray( new FileStat[ list.size() ] );
			}
		}
		
		return null;
	}
	
	/**
	 * This will provide a simple listing of a directory.
	 * For a more detailed listing, use {@link #getDetailedList()} instead. 
	 * 
	 * @return
	 *     An array with the names of all the items in the directory
	 */
	public String[] getList() {
		if (isDirectory()) {
			if (!iRestricted && mFile.canRead()) {
				return mFile.list();
				
			} else {
				ShellResult result = mShell.buildAttempts("%binary ls -1a '" + mFile.getAbsolutePath() + "'", "%binary ls -1 '" + mFile.getAbsolutePath() + "'").run();
				
				if (result.wasSuccessful()) {
					return result.trim().getArray();
					
				} else {
					/* Most toolbox versions does not support the "-1" argument in the "ls" command.
					 * In these cases without busybox, we fall back to using the statList() method
					 * and manually create our list.
					 */
					FileStat[] stat = getDetailedList();
					
					if (stat != null) {
						String[] output = new String[ stat.length ];
						
						for (int i=0; i < output.length; i++) {
							output[i] = stat[i].name();
						}
						
						return output;
					}
				}
			}
			
		} else if (exists()) {
			return new String[]{ mFile.getName() };
		}
		
		return null;
	}
	
	/**
	 * This is the same as {@link #getList()}, only this will provide an array of 
	 * {@link FileExtender} objects for each item in the directory.
	 * 
	 * @return
	 *     An array of {@link FileExtender} objects
	 */
	public FileExtender[] getObjectList() {
		if (exists()) {
			String[] list = getList();
			FileExtender[] output = new FileExtender[ list.length ];
			
			for (int i=0; i < list.length; i++) {
				output[i] = open( list[i] );
			}
			
			return output;
		}
		
		return null;
	}
	
	/**
	 * This class is extended from the Data class. As for now, there is nothing custom added to this class. But it might differ from the Data class at some point.
	 */
	public static class FileData extends Data<FileData> {
		public FileData(String[] lines) {
			super(lines);
		}
	}
	
	/**
	 * This class is a container which is used by {@link FileExtender#getDetails()} and {@link FileExtender#getDetailedList(Integer)}
	 */
	public static class FileStat {
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
}
