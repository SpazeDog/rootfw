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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;

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
	protected Boolean iIsLink;

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
	 * @return
	 *     <code>True</code> if the item is a link
	 */
	public Boolean isLink() {
		/* Checking whether or not a file is a link, uses more resources than just checking if it exists, 
		 * and also this information is not all that much used. So we wait to check it until it is needed. 
		 * Then we can cache the information. 
		 */
		if (iExists) {
			if (iIsLink == null) {
				FileStat stat = getDetails();
				
				if (stat != null) {
					iIsLink = "l".equals(stat.type());
				}
			}
			
			return iIsLink;
		}
		
		return false;
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
					iIsLink = null;
					
					return true;
				}
				
			} else {
				iIsFolder = false;
				iExists = false;
				iIsLink = null;
				
				return true;
			}
		}
		
		return true;
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
		
		return true;
	}
	
	/**
	 * This is the same as {@link #createDirectory()}, only this will also
	 * make sure to create any missing parent directories. 
	 * 
	 * @return
	 *     <code>True</code> if the directory was created successfully
	 */
	public Boolean createDirectories() {
		if (!exists()) {
			if (!mFile.mkdirs()) {
				if (!mShell.buildAttempts("%binary mkdir -p '" + mFile.getAbsolutePath() + "'").run().wasSuccessful()) {
					/* Not all busybox and toolbox versions support the "-p" argument in their "mkdir" command.
					 * In these cases, we will iterate through the tree manually
					 */
					String resolvedPath = getResolvedPath();
					String[] directories = ("/".equals(resolvedPath) ? resolvedPath : resolvedPath.endsWith("/") ? resolvedPath.substring(1, resolvedPath.length() - 1) : resolvedPath.substring(1)).split("/");
					FileExtender root = new FileExtender(mShell, new File("/"));
					
					for (int i=0; i < directories.length-1; i++) {
						root = root.open(directories[i]);
						
						if (root == null || !root.createDirectory()) {
							return false;
						}
					}
					
					if (createDirectory()) {
						return false;
					}
				}
			}
			
			iIsFolder = true;
			iExists = true;
			
			return true;
		}
		
		return true;
	}
	
	/**
	 * Create a link to this file or folder.
	 * 
	 * @param linkPath
	 *     Path to the link which should be created
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean createLink(String linkPath) {
		if (exists()) {
			return mShell.buildAttempts("%binary ln -s '" + mFile.getAbsolutePath() + "' '" + linkPath + "' 2> /dev/null").run().wasSuccessful();
		}
		
		return false;
	}
	
	/**
	 * Move a file or folder to another location. 
	 * If the move was successful, the object will change it pointer to the new location. 
	 * 
	 * @param dstPath
	 *     The destination path
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean move(String dstPath) {
		File file = new File(dstPath);
		
		if (exists()) {
			if (!mFile.renameTo(file)) {
				if (!mShell.buildAttempts("%binary mv -f '" + mFile.getAbsolutePath() + "' '" + file.getAbsolutePath() + "'").run().wasSuccessful()) {
					return false;
				}
			}
			
			mFile = file;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Rename a file or folder.
	 * 
	 * @param name
	 *     The new name to use
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean rename(String name) {
		if (exists() && mFile.getParent() != null) {
			String newPath = mFile.getParent() + "/" + name;
			
			if (!new FileExtender(mShell, new File(newPath)).exists()) {
				return move( mFile.getParent() + "/" + name );
			}
		}
		
		return false;
	}
	
	/**
	 * Make a copy of a file or folder. 
	 * 
	 * @param dstPath
	 *     The destination path
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean copy(String dstPath) {
		if (exists()) {
			FileExtender file = new FileExtender(mShell, new File(dstPath));
			Boolean status = true;
			
			if (!iRestricted && mFile.canRead() && !file.iRestricted && file.mFile.canWrite()) {
				if (isDirectory()) {
					if (!file.isDirectory()) {
						status = file.createDirectories();
					}
					
					String[] list = getList();
					
					if (list != null) {
						for (int i=0; i < list.length; i++) {
							if (!(status = open(list[i]).copy( file.getAbsolutePath() + "/" + list[i] ))) {
								break;
							}
						}
					}
					
				} else {
					try {
						InputStream input = new FileInputStream(mFile);
						OutputStream output = new FileOutputStream(file.mFile);
						
						byte[] buffer = new byte[1024];
						Integer length;
						
						while ((length = input.read(buffer)) > 0) {
							output.write(buffer, 0, length);
						}
						
						input.close();
						output.close();
						
					} catch(Throwable e) { status = false; }
				}
				 
			} else {
				status = false;
			}
			
			if (!status) {
				status = mShell.buildAttempts("%binary cp -fa '" + getAbsolutePath() + "' '" + file.getAbsolutePath() + "'").run().wasSuccessful();
			}
			
			return status;
		}
		
		return false;
	}
	
	/**
	 * @see #extractFromResource(Context, String, String, String, String)
	 */
	public Boolean extractFromResource(Context context, String asset) {
		return extractFromResource(context, asset, null, null, null);
	}
	
	/**
	 * Extract data from an Android Assets Path (files located in /assets/) and add it to the current file location.
	 * If the file already exist, it will be overwritten. Otherwise the file will be created. 
	 * 
	 * @param context
	 *     An android Context object
	 *     
	 * @param asset
	 *     The assets path
	 *     
	 * @param permissions
	 *     Permissions to add to the file
	 *     
	 * @param user
	 *     The owner to add to the file
	 *     
	 * @param group
	 *     The group to add to the file
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean extractFromResource(Context context, String asset, String permissions, String user, String group) {
		try {
			InputStream input = context.getAssets().open(asset);
			Boolean status = extractFromResource(context, input, permissions, user, group);
			input.close();
			
			return status;
			
		} catch(Throwable e) { return false; }
	}
	
	/**
	 * @see #extractFromResource(Context, Integer, String, String, String)
	 */
	public Boolean extractFromResource(Context context, Integer resourceid) {
		return extractFromResource(context, resourceid, null, null, null);
	}
	
	/**
	 * Extract data from an Android resource id (files located in /res/) and add it to the current file location.
	 * If the file already exist, it will be overwritten. Otherwise the file will be created. 
	 * 
	 * @param context
	 *     An android Context object
	 *     
	 * @param resourceid
	 *     The InputStream to read from
	 *     
	 * @param permissions
	 *     Permissions to add to the file
	 *     
	 * @param user
	 *     The owner to add to the file
	 *     
	 * @param group
	 *     The group to add to the file
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean extractFromResource(Context context, Integer resourceid, String permissions, String user, String group) {
		try {
			InputStream input = context.getResources().openRawResource(resourceid);
			Boolean status = extractFromResource(context, input, permissions, user, group);
			input.close();
			
			return status;
			
		} catch(Throwable e) { return false; }
	}
	
	/**
	 * @see #extractFromResource(Context, InputStream, String, String, String)
	 */
	public Boolean extractFromResource(Context context, InputStream resource) {
		return extractFromResource(context, resource, null, null, null);
	}
	
	/**
	 * Extract data from an InputStream and add it to the current file location.
	 * If the file already exist, it will be overwritten. Otherwise the file will be created. 
	 * 
	 * @param context
	 *     An android Context object
	 *     
	 * @param resource
	 *     The InputStream to read from
	 *     
	 * @param permissions
	 *     Permissions to add to the file
	 *     
	 * @param user
	 *     The owner to add to the file
	 *     
	 * @param group
	 *     The group to add to the file
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean extractFromResource(Context context, InputStream resource, String permissions, String user, String group) {
		try {
			FileOutputStream output = context.openFileOutput("rootfw.tmp.raw", 0);
			
			byte[] buffer = new byte[1024];
			Integer loc = 0;
			
			while ((loc = resource.read(buffer)) > 0) {
				output.write(buffer, 0, loc);
			}
			
			output.close();
			
		} catch(Throwable e) { return false; }
		
		FileExtender file = new FileExtender(mShell, new File(context.getFilesDir().getAbsolutePath() + "/rootfw.tmp.raw"));
		
		if (file.move( getAbsolutePath() )) {
			iExists = true;
			
			if ((permissions == null || setPermissions(permissions)) && (user == null || setOwner(user)) && (group == null || setGroup(group))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @see #setPermissions(String, Boolean)
	 */
	public Boolean setPermissions(String mod) {
		return setPermissions(mod, false);
	}
	
	/**
	 * Set the permissions on a file or folder. 
	 * 
	 * @param mod
	 *     The permission string, like <code>0755</code>
	 *     
	 * @param recursive
	 *     Whether or not to change all the content of a folder
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean setPermissions(String mod, Boolean recursive) {
		return mShell.buildAttempts("%binary chmod" + (recursive ? " -R" : "") + " '" + mod + "' '" + mFile.getAbsolutePath() + "'").run().wasSuccessful();
	}
	
	/**
	 * @see #setOwner(String, String, Boolean)
	 */
	public Boolean setOwner(String owner) {
		return setOwner(owner, null, false);
	}
	
	/**
	 * @see #setOwner(String, String, Boolean)
	 */
	public Boolean setOwner(String owner, Boolean recursive) {
		return setOwner(owner, null, recursive);
	}
	
	/**
	 * @see #setOwner(String, String, Boolean)
	 */
	public Boolean setOwner(String owner, String group) {
		return setOwner(owner, group, false);
	}
	
	/**
	 * Set the ownership of a file or folder. 
	 * 
	 * @param owner
	 *     The owner, either id or name
	 *     
	 * @param group
	 *     The group, either id or name
	 *     
	 * @param recursive
	 *     Whether or not to change all the content of a folder
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean setOwner(String owner, String group, Boolean recursive) {
		return mShell.buildAttempts("%binary chown" + (recursive ? " -R" : "") + " '" + owner + (group != null ? "." + group : "") + "' '" + mFile.getAbsolutePath() + "'").run().wasSuccessful();
	}
	
	/**
	 * @see #setGroup(String, Boolean)
	 */
	public Boolean setGroup(String group) {
		return setGroup(group, false);
	}
	
	/**
	 * Set the group of a file or folder. 
	 * 
	 * @param group
	 *     The group, either id or name
	 *     
	 * @param recursive
	 *     Whether or not to change all the content of a folder
	 * 
	 * @return
	 *     <code>True</code> on success, <code>False</code> otherwise
	 */
	public Boolean setGroup(String group, Boolean recursive) {
		return mShell.buildAttempts("%binary chgrp" + (recursive ? " -R" : "") + " '" + group + "' '" + mFile.getAbsolutePath() + "'").run().wasSuccessful();
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
	 * If this is a link, this method will return a new object with the real path attached. 
	 * 
	 * @return
	 *     A new instance of this class representing the parent directory
	 */
	public FileExtender openCanonical() {
		String canonicalPath = getCanonicalPath();

		return canonicalPath != null ? new FileExtender(mShell, new File(canonicalPath)) : null;
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
	 * Get the canonical path of this file or folder. 
	 * This means that if this is a link, you will get the path to the target, no matter how many links are in between.
	 * It also means that things like <code>/folder1/../folder2</code> will be resolved to <code>/folder2</code>.
	 * 
	 * @return
	 *     The canonical path
	 */
	public String getCanonicalPath() {
		if (exists()) {
			try {
				if (!iRestricted && mFile.canRead() && mFile.getCanonicalPath() != null) {
					return mFile.getCanonicalPath();
				}
				
			} catch(Throwable e) {}
			
			ShellResult result = mShell.buildAttempts("%binary readlink -f '" + mFile.getAbsolutePath() + "' 2> /dev/null").run();
			
			if (result.wasSuccessful()) {
				return result.getLine();
				
			} else {
				FileStat stat = getDetails();
				
				if (stat != null && stat.link() != null) {
					String realPath = stat.link();
					
					while ((stat = new FileExtender(mShell, new File(realPath)).getDetails()) != null && stat.link() != null) {
						realPath = stat.link();
					}
					
					return realPath;
				}
				
				return mFile.getAbsolutePath();
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the absolute path. An absolute path is a path that starts at a root of the file system.
	 * 
	 * @return
	 *     The absolute path
	 */
	public String getAbsolutePath() {
		return mFile.getAbsolutePath();
	}
	
	/**
	 * Like {@link #getCanonicalPath()}, this will resolve any <code>/folder1/../folder2</code> and rewrite it as <code>/folder2</code>. 
	 * Unlike {@link #getCanonicalPath()}, this will not resolve any links. It will only rewrite and return a more clean version of the current path.
	 * 
	 * @return
	 *     The absolute path
	 */
	public String getResolvedPath() {
		String[] directories = ("/".equals(mFile.getAbsolutePath()) ? mFile.getAbsolutePath() : mFile.getAbsolutePath().endsWith("/") ? mFile.getAbsolutePath().substring(1, mFile.getAbsolutePath().length() - 1) : mFile.getAbsolutePath().substring(1)).split("/");
		List<String> resolved = new ArrayList<String>();
		
		for (int i=0; i < directories.length; i++) {
			if (directories[i].equals("..")) {
				if (resolved.size() > 0) {
					resolved.remove( resolved.size()-1 );
				}
				
			} else if (!directories[i].equals(".")) {
				resolved.add(directories[i]);
			}
		}
		
		return resolved.size() > 0 ? "/" + TextUtils.join("/", resolved) : "/";
	}
	
	/**
	 * Returns the path used to create this object.
	 * 
	 * @return
	 *     The parsed path
	 */
	public String getPath() {
		return mFile.getPath();
	}
	
	/**
	 * Returns the parent path. Note that on folders, this means the parent folder. 
	 * However, on files, it will return the folder path that the file resides in.
	 * 
	 * @return
	 *     The parent path
	 */
	public String getParentPath() {
		return mFile.getParent();
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
