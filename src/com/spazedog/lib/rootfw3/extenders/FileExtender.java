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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.containers.BasicContainer;
import com.spazedog.lib.rootfw3.containers.Data;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.DiskStat;
import com.spazedog.lib.rootfw3.extenders.FilesystemExtender.MountStat;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class FileExtender {
	
	private final static Pattern oPatternEscape = Pattern.compile("([\"\'`\\\\])");
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	private final static Pattern oPatternStatSplitter = Pattern.compile("\\|");
	private final static Pattern oPatternStatSearch = Pattern.compile("^([a-z-]+)(?:[ \t]+([0-9]+))?[ \t]+([0-9a-z_]+)[ \t]+([0-9a-z_]+)(?:[ \t]+(?:([0-9]+),[ \t]+)?([0-9]+))?[ \t]+([A-Za-z]+[ \t]+[0-9]+[ \t]+[0-9:]+|[0-9-/]+[ \t]+[0-9:]+)[ \t]+(?:(.*) -> )?(.*)$");
	
	public static String resolvePath(String path) {
		if (path.contains(".")) {
			String[] directories = ("/".equals(path) ? path : path.endsWith("/") ? path.substring(1, path.length() - 1) : path.substring(1)).split("/");
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
		
		return path;
	}
	
	/**
	 * This is kind of an extension of {@link java.io.File}, only this does not just come with a lot more tools, 
	 * it also switches to the <code>root/shell</code> whenever a file operation is not possible using the regular applications permissions.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#file(String)} to retrieve an instance. 
	 */
	public static class File implements ExtenderGroup {
		
		private RootFW mParent;
		private ShellExtender.Shell mShell;
		
		private java.io.File mFile;
		
		protected Boolean iExists = false;
		protected Boolean iIsFolder = false;
		protected Boolean iIsRestricted = false;
		protected Integer iIsLink = -1;
		
		private Object mLock = new Object();
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new File(parent, (java.io.File) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 *     
		 * @param file
		 *     A {@link java.io.File} object
		 */
		private File(RootFW parent, java.io.File file) {
			mParent = parent;
			mFile = file;
			mShell = parent.shell();
			
			createFileValidation();
		}
		
		/**
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onExtenderReconfigure() {}
		
		/**
		 * This is used internally to check whether or not the file exists and whether or not it's a directory or a file. 
		 */
		private void createFileValidation() {
			synchronized (mLock) {
				// A file/folder can be so restricted that we are not even allowed to check whether it exists without root
				iExists = mFile.exists() ? true : "true".equals( mShell.buildCommands("( %binary test -e '" + getAbsolutePath() + "' && echo true ) || ( %binary test ! -e '" + getAbsolutePath() + "' && echo false )").run().getLine() );
	
				// This can be used in other methods of this class to determine if we can use pure java to get information about it, or if root has to be used for everything. 
				iIsRestricted = iExists != mFile.exists();
				
				if (iExists) {
					iIsFolder = !iIsRestricted ? mFile.isDirectory() : "true".equals( mShell.buildCommands("( %binary test -d '" + getAbsolutePath() + "' && echo true ) || ( %binary test ! -d '" + getAbsolutePath() + "' && echo false )").run().getLine() );
				}
			}
		}
		
		/**
		 * This is used internally to change the current validation settings.
		 * 
		 * @see #createFileValidation()
		 */
		private void createFileValidation(Boolean exists, Boolean folder, Boolean restricted, Integer link) {
			synchronized (mLock) {
				iExists = exists;
				iIsFolder = folder;
				iIsRestricted = restricted;
				iIsLink = link;
			}
		}
		
		/**
		 * @return
		 *     <code>True</code> if the file cannot even be checked whether it exists without root, <code>False</code> otherwise
		 */
		public Boolean isRestricted() {
			synchronized (mLock) {
				return iIsRestricted;
			}
		}
		
		/**
		 * @return
		 *     <code>True</code> if the item exists and if it is an file, <code>False</code> otherwise
		 */
		public Boolean isFile() {
			synchronized (mLock) {
				return iExists && !iIsFolder;
			}
		}
		
		/**
		 * @return
		 *     <code>True</code> if the item exists and if it is an folder, <code>False</code> otherwise
		 */
		public Boolean isDirectory() {
			synchronized (mLock) {
				return iExists && iIsFolder;
			}
		}
		
		/**
		 * @return
		 *     <code>True</code> if the item exists, <code>False</code> otherwise
		 */
		public Boolean exists() {
			synchronized (mLock) {
				return iExists;
			}
		}
		
		/**
		 * @return
		 *     <code>True</code> if the file is a link, <code>False</code> otherwise
		 */
		public Boolean isLink() {
			synchronized (mLock) {
				/* Checking whether or not a file is a link, uses more resources than just checking if it exists, 
				 * and also this information is not all that much used. So we wait to check it until it is needed. 
				 * Then we can cache the information for reuse. 
				 */
				if (iExists) {
					if (iIsLink < 0) {
						FileStat stat = getDetails();
						
						if (stat != null) {
							iIsLink = "l".equals(stat.type()) ? 1 : 0;
						}
					}
					
					return iIsLink == 1;
				}
				
				return false;
			}
		}
		
		/**
		 * Extract the content from the file and return it.
		 * 
		 * @return
		 *     The entire file content wrapped in a {@link FileData} object
		 */
		public FileData read() {
			synchronized (mLock) {
				if (isFile()) {
					if (!isRestricted() && mFile.canRead()) {
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
						ShellResult result = mShell.buildCommands("%binary cat '" + getAbsolutePath() + "' 2> /dev/null").run();
						
						if (result.wasSuccessful()) {
							return new FileData( result.getArray() );
						}
					}
				}
				
				return null;
			}
		}
		
		/**
		 * Extract the first line of the file.
		 * 
		 * @return
		 *     The first line of the file as a string
		 */
		public String readOneLine() {
			synchronized (mLock) {
				if (isFile()) {
					if (!isRestricted() && mFile.canRead()) {
						try {
							BufferedReader reader = new BufferedReader(new FileReader(mFile));
							String line = reader.readLine();
							reader.close();
							
							return line;
							
						} catch (Throwable e) {}
						
					} else {
						ShellResult result = mShell.buildAttempts("%binary sed -n '1p' '" + mFile.getAbsolutePath() + "' 2> /dev/null", "%binary cat '" + getAbsolutePath() + "' 2> /dev/null").run();
						
						return result.wasSuccessful() ? result.getLine(0) : null;
					}
				}
				
				return null;
			}
		}
		
		/**
		 * Search the file line by line to find a match for a specific pattern and return the first match found in the file.
		 * 
		 * @see #readMatches(String, Boolean)
		 * 
		 * @param match
		 *     The pattern to search for
		 * 
		 * @return
		 *     The first matched line from the file as a string
		 */
		public String readOneMatch(String match) {
			synchronized (mLock) {
				FileData data = readMatches(match, true);
				
				return data != null ? data.getLine() : null;
			}
		}
		
		/**
		 * @see #readMatches(String, Boolean)
		 */
		public FileData readMatches(String match) {
			return readMatches(match, false);
		}
		
		/**
		 * Search the file line by line to find a match for a specific pattern and return all of the matched lines.
		 * <br />
		 * It is also possible to have it stop at the first found match.
		 * 
		 * @see #readOneMatch(String)
		 * 
		 * @param match
		 *     The pattern to search for
		 *     
		 * @param firstMatch
		 *     Whether or not to quit on the first found match
		 * 
		 * @return
		 *     All of the matched lines wrapped in a {@link FileData} object
		 */
		public FileData readMatches(String match, Boolean firstMatch) {
			synchronized (mLock) {
				if (isFile()) {
					if (!isRestricted() && mFile.canRead()) {
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
						String escapedMatch = oPatternEscape.matcher(match).replaceAll("\\\\$1");
						
						String[] attempts = firstMatch ? 
								new String[]{"%binary sed -n '/" + escapedMatch + "/{p;q;}' '" + mFile.getAbsolutePath() + "'", "%binary grep -m 1 '" + escapedMatch + "' '" + getAbsolutePath() + "'", "%binary grep '" + escapedMatch + "' '" + getAbsolutePath() + "'"} : 
									new String[]{"%binary grep '" + escapedMatch + "' '" + getAbsolutePath() + "'"};
								
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
		}

		/**
		 * @see #write(String[], Boolean)
		 */
		public Boolean write(String input) {
			return write(input.split("\n"), false);
		}
		
		/**
		 * @see #write(String[], Boolean)
		 */
		public Boolean write(String input, Boolean append) {
			return write(input.split("\n"), append);
		}
		
		/**
		 * @see #write(String[], Boolean)
		 */
		public Boolean write(String[] input) {
			return write(input, false);
		}
		
		/**
		 * Write data to the file. The data should be an array where each index is a line that should be written to the file. 
		 * <br />
		 * If the file does not already exist, it will be created. 
		 * 
		 * @param input
		 *     The data that should be written to the file
		 *     
		 * @param append
		 *     Whether or not to append the data to the existing content in the file
		 * 
		 * @return
		 *     <code>True</code> if the data was successfully written to the file, <code>False</code> otherwise
		 */
		public Boolean write(String[] input, Boolean append) {
			synchronized (mLock) {
				if (!isDirectory()) {
					if (!isRestricted() && mFile.canWrite()) {
						try {
							BufferedWriter output = new BufferedWriter(new FileWriter(mFile, append));
							
							for (int i=0; i < input.length; i++) {
								output.write(input[i]);
								output.newLine();
							}
							
							output.close();
							
							createFileValidation(mFile.exists(), false, false, iIsLink);
							
							return exists();
							
						} catch(Throwable e) {}
						
					} else {
						ShellResult result = null;
						String redirect = append ? ">>" : ">";
						
						for (int i=0; i < input.length; i++) {
							String escapedMatch = oPatternEscape.matcher(input[i]).replaceAll("\\\\$1");
							
							if ( !(result = mShell.run("echo '" + escapedMatch + "' " + redirect + " '" + getAbsolutePath() + "' 2> /dev/null")).wasSuccessful() ) {
								break;
							}
							
							redirect = ">>";
						}
						
						createFileValidation(result.wasSuccessful() || exists(), false, false, iIsLink);
						
						return result.wasSuccessful();
					}
				}
				
				return false;
			}
		}
		
		/**
		 * Remove the file. 
		 * Folders will be recursively cleaned before deleting. 
		 * 
		 * @return
		 *     <code>True</code> if the file was deleted or did not exist to begin with, <code>False</code> otherwise
		 */
		public Boolean remove() {
			synchronized (mLock) {
				if (exists()) {
					if (isDirectory() && !isRestricted() && mFile.canWrite() && mFile.canRead()) {
						String[] fileList = mFile.list();
						
						if (fileList != null && fileList.length > 0) {
							for (int i=0; i < fileList.length; i++) {
								open(fileList[i]).remove();
							}
						}
					}
					
					if (!mFile.delete()) {
						ShellResult result = mShell.buildCommands("%binary rm -rf '" + getAbsolutePath() + "' 2> /dev/null").run();
						
						if (result.wasSuccessful()) {
							createFileValidation(false, false, false, -1);
							
							return true;
						}
						
					} else {
						createFileValidation(false, false, false, -1);
						
						return true;
					}
					
				} else {
					return true;
				}
				
				return false;
			}
		}
		
		/**
		 * Create a new empty file based on the path from this file object.
		 * 
		 * @return
		 *     <code>True</code> if the file was created successfully or if it existed to begin with, <code>False</code> oherwise
		 */
		public Boolean createFile() {
			synchronized (mLock) {
				if (!exists()) {
					try {
						mFile.createNewFile();
						
					} catch (Throwable e) {
						ShellResult result = mShell.run("echo '' > '" + getAbsolutePath() + "' 2> /dev/null");
						
						if (!result.wasSuccessful() || !"true".equals( mShell.buildCommands("( %binary test -f '" + getAbsolutePath() + "' && echo true ) || ( %binary test ! -f '" + getAbsolutePath() + "' && echo false )").run().getLine() )) {
							return false;
						}
					}
					
					createFileValidation(true, false, false, 0);
					
					return true;
					
				} else if (!isDirectory()) {
					return true;
				}
				
				return true;
			}
		}
		
		/**
		 * Create a new directory based on the path from this file object.
		 * 
		 * @see #createDirectories()
		 * 
		 * @return
		 *     <code>True</code> if the directory was created successfully or if it existed to begin with, <code>False</code> oherwise
		 */
		public Boolean createDirectory() {
			synchronized (mLock) {
				if (!exists()) {
					if (!mFile.mkdir()) {
						ShellResult result = mShell.buildCommands("%binary mkdir '" + getAbsolutePath() + "' 2> /dev/null").run();
						
						if (!result.wasSuccessful() || !"true".equals( mShell.buildCommands("( %binary test -d '" + getAbsolutePath() + "' && echo true ) || ( %binary test ! -d '" + getAbsolutePath() + "' && echo false )").run().getLine() )) {
							return false;
						}
					}
					
					createFileValidation(true, true, false, 0);
					
					return true;
					
				} else if (isDirectory()) {
					return true;
				}
				
				return false;
			}
		}
		
		/**
		 * Create a new directory based on the path from this file object. 
		 * The method will also create any missing parent directories.  
		 * 
		 * @see #createDirectory()
		 * 
		 * @return
		 *     <code>True</code> if the directory was created successfully
		 */
		public Boolean createDirectories() {
			synchronized (mLock) {
				if (!exists()) {
					if (!mFile.mkdirs()) {
						if (!mShell.buildAttempts("%binary mkdir -p '" + getAbsolutePath() + "' 2> /dev/null").run().wasSuccessful()) {
							/* Not all busybox and toolbox versions support the "-p" argument in their "mkdir" command.
							 * In these cases, we will iterate through the tree manually
							 */
							String resolvedPath = getResolvedPath();
							String[] directories = ("/".equals(resolvedPath) ? resolvedPath : resolvedPath.endsWith("/") ? resolvedPath.substring(1, resolvedPath.length() - 1) : resolvedPath.substring(1)).split("/");
							FileExtender.File current = openNew("/");
							
							for (int i=0; i < directories.length; i++) {
								if ((current = open(directories[i])) == null || !current.createDirectory()) {
									return false;
								}
							}
						}
					}
					
					createFileValidation(true, true, false, 0);
					
					return true;
					
				} else if (isDirectory()) {
					return true;
				}
				
				return false;
			}
		}
		
		/**
		 * Create a link to this file.
		 * 
		 * @param linkPath
		 *     Path (Including name) to the link which should be created
		 * 
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean createLink(String linkPath) {
			synchronized (mLock) {
				if (exists()) {
					FileExtender.File destFile = openNew(linkPath);
					
					synchronized (destFile.mLock) {
						if (!destFile.exists()) {
							if(mShell.buildAttempts("%binary ln -s '" + getAbsolutePath() + "' '" + destFile.getAbsolutePath() + "' 2> /dev/null").run().wasSuccessful()) {
								/* Someone could have a copy of this instance created somewhere else */
								destFile.createFileValidation(iExists, iIsFolder, iIsRestricted, 1);
								
								return true;
							}
						}
					}
				}
				
				return false;
			}
		}
		
		/**
		 * @see #move(String, Boolean)
		 */
		public Boolean move(String dstPath) {
			return move(dstPath, false);
		}
		
		/**
		 * Move the file to another location. 
		 * <br />
		 * Note that moving the file, will not change this objects file pointer. 
		 * This means that if the move was successful, this object will point to a file that no longer exist. 
		 * So in order to continue working with the file, you will need to change the pointer to point at the new location. 
		 * You can use {@link #openNew(String)} or {@link RootFW#file(String)}.
		 * 
		 * @param dstPath
		 *     The destination path
		 * 
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean move(String dstPath, Boolean overwrite) {
			synchronized (mLock) {
				if (exists()) {
					FileExtender.File destFile = openNew(dstPath);
					
					synchronized (destFile.mLock) {
						if (!destFile.exists() || (overwrite && destFile.remove())) {
							if (!mFile.renameTo(destFile.mFile)) {
								if (!mShell.buildAttempts("%binary mv -f '" + getAbsolutePath() + "' '" + destFile.getAbsolutePath() + "'").run().wasSuccessful()) {
									return false;
								}
							}
							
							destFile.createFileValidation(iExists, iIsFolder, iIsRestricted, iIsLink);
							
							createFileValidation(false, false, false, -1);
							
							return true;
						}
					}
				}
				
				return false;
			}
		}
		
		/**
		 * Rename the file. 
		 * <br />
		 * Note that since renaming is actually the same as moving (Both is actually just renaming the path), 
		 * the rules in {@link #move(String)} also apply here. 
		 * In order to continue working with the file after renaming it, you will need to change the pointer to point at the new location, 
		 * as this object will point to a file that no longer exist. 
		 * You can use {@link #openNew(String)} or {@link RootFW#file(String)}.
		 * 
		 * @param name
		 *     The new name to use
		 * 
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean rename(String name) {
			return move( (getParentPath() == null ? "" : getParentPath()) + "/" + name );
		}
		
		public Boolean copy(String dstPath) {
			return copy(dstPath, false);
		}
		
		/**
		 * Copy the file to another location.
		 * 
		 * @param dstPath
		 *     The destination path
		 * 
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean copy(String dstPath, Boolean overwrite) {
			synchronized (mLock) {
				if (exists()) {
					FileExtender.File destFile = openNew(dstPath);
					
					synchronized (destFile.mLock) {
						if (!destFile.exists() || (overwrite && destFile.remove())) {
							Boolean status = false;
							
							if (!isRestricted() && mFile.canRead()) {
								if (isDirectory() && destFile.createDirectories()) {
									String[] list = getList();
									
									if (list != null) {
										for (int i=0; i < list.length; i++) {
											if (!(status = open(list[i]).copy( destFile.getAbsolutePath() + "/" + list[i] , overwrite))) {
												break;
											}
										}
									}
									
								} else if (!isDirectory()) {
									try {
										InputStream input = new FileInputStream(mFile);
										OutputStream output = new FileOutputStream(destFile.mFile);
										
										byte[] buffer = new byte[1024];
										Integer length;
										
										while ((length = input.read(buffer)) > 0) {
											output.write(buffer, 0, length);
										}
										
										input.close();
										output.close();
										
										status = true;
										
									} catch(Throwable e) {  }
								}
							}

							if (status || mShell.buildAttempts("%binary cp -fa '" + getAbsolutePath() + "' '" + destFile.getAbsolutePath() + "'").run().wasSuccessful()) {
								destFile.createFileValidation(iExists, iIsFolder, iIsRestricted, iIsLink);
								
								return true;
							}
							
							return status;
						}
					}
				}
				
				return false;
			}
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
			synchronized (mLock) {
				if (!exists()) {
					FileExtender.File srcFile = openNew(context.getFilesDir().getAbsolutePath() + "/rootfw.tmp.raw");
					
					synchronized (srcFile.mLock) {
						try {
							FileOutputStream output = context.openFileOutput(srcFile.getName(), 0);
							
							byte[] buffer = new byte[1024];
							Integer loc = 0;
							
							while ((loc = resource.read(buffer)) > 0) {
								output.write(buffer, 0, loc);
							}
							
							output.close();
							
							srcFile.createFileValidation(true, false, false, 0);
							
						} catch(Throwable e) { return false; }
						
						if (srcFile.move( getAbsolutePath() , true)) {
							if ((permissions == null || (setPermissions(permissions)) && (user == null || setOwner(user)) && (group == null || setGroup(group)))) {
								return true;
							}
						}
					}
				}
				
				return false;
			}
		}
		
		/**
		 * @see #setPermissions(String, Boolean)
		 */
		public Boolean setPermissions(String mod) {
			return setPermissions(mod, false);
		}
		
		/**
		 * Change the permissions on the file.
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
			synchronized (mLock) {
				if (exists()) {
					return mShell.buildAttempts("%binary chmod" + (recursive ? " -R" : "") + " '" + mod + "' '" + getAbsolutePath() + "'").run().wasSuccessful();
				}
				
				return false;
			}
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
		 * Change the ownership of the file.
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
			synchronized (mLock) {
				if (exists()) {
					return mShell.buildAttempts("%binary chown" + (recursive ? " -R" : "") + " '" + owner + (group != null ? "." + group : "") + "' '" + getAbsolutePath() + "'").run().wasSuccessful();
				}
				
				return false;
			}
		}
		
		/**
		 * @see #setGroup(String, Boolean)
		 */
		public Boolean setGroup(String group) {
			return setGroup(group, false);
		}
		
		/**
		 * Change the group of the file.
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
			synchronized (mLock) {
				if (exists()) {
					return mShell.buildAttempts("%binary chgrp" + (recursive ? " -R" : "") + " '" + group + "' '" + getAbsolutePath() + "'").run().wasSuccessful();
				}
				
				return false;
			}
		}
		
		/**
		 * Open a new {@link FileExtender.File} object with another file or folder relative to the current directory.
		 * 
		 * @param fileName
		 *     The relative file to point at
		 * 
		 * @return
		 *     A new instance of this class representing another relative file or folder
		 */
		public FileExtender.File open(String fileName) {
			if (isDirectory()) {
				return mParent.file((getParentPath() == null ? "" : getAbsolutePath()) + "/" + (fileName.startsWith("/") ? fileName.substring(1) : fileName));
			}
			
			return null;
		}
		
		/**
		 * Open a new {@link FileExtender.File} object pointed at another file.
		 * 
		 * @param fileName
		 *     The file to point at
		 * 
		 * @return
		 *     A new instance of this class representing another file
		 */
		public FileExtender.File openNew(String file) {
			return mParent.file( file );
		}
		
		/**
		 * Open a new {@link FileExtender.File} object with the parent of this file.
		 * 
		 * @return
		 *     A new instance of this class representing the parent directory
		 */
		public FileExtender.File openParent() {
			return mFile.getParent() == null ? null : mParent.file( getParentPath() );
		}
		
		/**
		 * If this is a link, this method will return a new {@link FileExtender.File} object with the real path attached. 
		 * 
		 * @return
		 *     A new instance of this class representing the real path of a link
		 */
		public FileExtender.File openCanonical() {
			return mParent.file( getCanonicalPath() );
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
			synchronized (mLock) {
				if (exists()) {
					try {
						if (!isRestricted() && mFile.canRead() && mFile.getCanonicalPath() != null) {
							return mFile.getCanonicalPath();
						}
						
					} catch(Throwable e) {}
					
					ShellResult result = mShell.buildAttempts("%binary readlink -f '" + getAbsolutePath() + "' 2> /dev/null").run();
					
					if (result.wasSuccessful()) {
						return result.getLine();
						
					} else {
						FileStat stat = getDetails();
						
						if (stat != null && stat.link() != null) {
							String realPath = stat.link();
							
							while ((stat = openNew(realPath).getDetails()) != null && stat.link() != null) {
								realPath = stat.link();
							}
							
							return realPath;
						}
						
						return getAbsolutePath();
					}
				}
				
				return null;
			}
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
			return resolvePath(getAbsolutePath());
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
		 * @return
		 *     The name of the file
		 */
		public String getName() {
			return mFile.getName();
		}
		
		/**
		 * Get information about this file or folder. This will return information like 
		 * size (on files), path to linked file (on links), permissions, group, user etc.
		 * 
		 * @return
		 *     A new {@link FileStat} object with all the file information
		 */
		public FileStat getDetails() {
			synchronized (mLock) {
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
			synchronized (mLock) {
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
								FileStat stat = new FileStat();
								
								stat.mType = parts[0].substring(0, 1).equals("-") ? "f" : parts[0].substring(0, 1);
								stat.mAccess = parts[0];
								stat.mUser = Integer.parseInt(parts[1]);
								stat.mGroup = Integer.parseInt(parts[2]);
								stat.mSize = parts[4].equals("null") || !parts[3].equals("null") ? 0L : Long.parseLong(parts[4]);
								stat.mMM = parts[3].equals("null") ? null : parts[3] + ":" + parts[4];
								stat.mName = parts[5].equals("null") ? parts[6].substring( parts[6].lastIndexOf("/") + 1 ) : parts[5].substring( parts[5].lastIndexOf("/") + 1 );
								stat.mLink = parts[5].equals("null") ? null : parts[6];
								stat.mPermission = "";
								String[] partModAssambler = new String[] {stat.mAccess.substring(1), stat.mAccess.substring(1, 4), stat.mAccess.substring(4, 7), stat.mAccess.substring(7, 10)};
								
								for (int x=0; x < partModAssambler.length; x++) {
									Integer mod = (x == 0 && partModAssambler[x].charAt(2) == 's') || (x > 0 && partModAssambler[x].charAt(0) == 'r') ? 4 : 0;
									mod += (x == 0 && partModAssambler[x].charAt(5) == 's') || (x > 0 && partModAssambler[x].charAt(1) == 'w') ? 2 : 0;
									mod += (x == 0 && partModAssambler[x].charAt(8) == 't') || (x > 0 && partModAssambler[x].charAt(2) == 'x') ? 1 : 0;
									
									stat.mPermission += "" + mod;
								}
								
								if (stat.mName.contains("/")) {
									stat.mName = stat.mName.substring( stat.mName.lastIndexOf("/")+1 );
								}
								
								list.add(stat);
								
								indexCount++;
							}
						}
						
						return list.toArray( new FileStat[ list.size() ] );
					}
				}
				
				return null;
			}
		}
		
		/**
		 * This will provide a simple listing of a directory.
		 * For a more detailed listing, use {@link #getDetailedList()} instead. 
		 * 
		 * @return
		 *     An array with the names of all the items in the directory
		 */
		public String[] getList() {
			synchronized (mLock) {
				if (isDirectory()) {
					if (!isRestricted() && mFile.canRead()) {
						return mFile.list();
						
					} else {
						ShellResult result = mShell.buildAttempts("%binary ls -1 '" + mFile.getAbsolutePath() + "'").run();
						
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
									if (!stat[i].name().equals(".") && !stat[i].name().equals("..")) {
										output[i] = stat[i].name();
									}
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
		}
		
		/**
		 * This is the same as {@link #getList()}, only this will provide an array of 
		 * {@link FileExtender} objects for each item in the directory.
		 * 
		 * @return
		 *     An array of {@link FileExtender} objects
		 */
		public FileExtender.File[] getObjectList() {
			if (exists()) {
				String[] list = getList();
				FileExtender.File[] output = new FileExtender.File[ list.length ];
				
				for (int i=0; i < list.length; i++) {
					output[i] = open( list[i] );
				}
				
				return output;
			}
			
			return null;
		}
		
		/**
		 * Calculates the size of a file or folder. 
		 * 
		 * @return
		 *     0 if the file does not exist
		 */
		public Long fileSize() {
			if (exists()) {
				if (!isRestricted() && mFile.canRead()) {
					if (isDirectory()) {
						String[] list = getList();
						Long size = 0L;
						
						if (list != null) {
							for (int i=0; i < list.length; i++) {
								size += open(list[i]).fileSize();
							}
						}
						
						return size;
						
					} else {
						return mFile.length();
					}
				}
				
				ShellResult result = isDirectory() ? 
						mShell.buildAttempts("%binary du -sbx '" + getAbsolutePath() + "'", "%binary du -skx '" + getAbsolutePath() + "'").run() : 
							mShell.buildAttempts("%binary wc -c < '" + getAbsolutePath() + "'", "%binary wc < '" + getAbsolutePath() + "'").run();
						
				if (result.wasSuccessful()) {
					if (isDirectory()) {
						try {
							return result.getCommandNumber(0) > RootFW.Config.BINARIES.size() ? 
									Long.parseLong( oPatternSpaceSearch.split(result.getLine().trim())[0] ) * 1024L : 
										Long.parseLong( oPatternSpaceSearch.split(result.getLine().trim())[0] );
							
						} catch (Throwable e) {}
						
					} else {
						try {
							return result.getCommandNumber(0) > RootFW.Config.BINARIES.size() ? 
									Long.parseLong( oPatternSpaceSearch.split(result.getLine().trim())[2] )  : 
										Long.parseLong( result.getLine() );
							
						} catch (Throwable e) {}
					}
				}
			}
			
			return 0L;
		}
		
		/**
		 * Get the md5sum of this file
		 */
		public String getChecksum() {
			if (isFile()) {
				ShellResult result = mShell.buildAttempts("%binary md5sum '" + getAbsolutePath() + "' 2> /dev/null").run();
				
				if (result.wasSuccessful()) {
					try {
						return oPatternSpaceSearch.split(result.getLine().trim())[0];
						
					} catch (Throwable e) {}
				}
			}
			
			return null;
		}
		
		/**
		 * Reboot into recovery and run this file/package
		 * <br />
		 * This method will add a command file in /cache/recovery which will tell the recovery the location of this 
		 * package. The recovery will then run the package and then automatically reboot back into Android. 
		 * <br />
		 * Note that this will also work on ROM's that changes the cache location or device. The method will 
		 * locate the real internal cache partition, and it will also mount it at a second location 
		 * if it is not already mounted. 
		 * 
		 * @param args
		 *     Arguments which will be parsed to the recovery package. 
		 *     Each argument equels one prop line.
		 *     <br />
		 *     Each prop line is added to /cache/recovery/rootfw.prop and named (argument[argument number] = [value]).
		 *     For an example, if first argument is "test", it will be written to rootfw.prop as (argument1 = test).
		 * 
		 * @return
		 *     <code>False if it failed</code>
		 */
		public Boolean runInRecovery(String... args) {
			if (isFile()) {
				String cacheLocation = "/cache";
				MountStat mountStat = mParent.filesystem(cacheLocation).statFstab();
				
				if (mountStat != null) {
					DiskStat diskStat = mParent.filesystem( mountStat.device() ).statDisk();
					
					if (diskStat == null || !cacheLocation.equals(diskStat.location())) {
						if (diskStat == null) {
							mParent.filesystem("/").addMount(new String[]{"remount", "rw"});
							cacheLocation = "/cache-int";
							
							if (!openNew(cacheLocation).createDirectory()) {
								return false;
								
							} else if (!mParent.filesystem(mountStat.device()).addMount(cacheLocation)) {
								return false;
							}
							
							mParent.filesystem("/").addMount(new String[]{"remount", "ro"});
							
						} else {
							cacheLocation = diskStat.location();
						}
					}
				}
				
				if (openNew(cacheLocation + "/recovery").createDirectory()) {
					if (openNew(cacheLocation + "/recovery/command").write("--update_package=" + getResolvedPath())) {
						if (args != null && args.length > 0) {
							String[] lines = new String[ args.length ];
							
							for (int i=0; i < args.length; i++) {
								lines[i] = "argument" + (i+1) + "=" + args[i];
							}
							
							if (!openNew(cacheLocation + "/recovery/rootfw.prop").write(lines)) {
								openNew(cacheLocation + "/recovery/command").remove(); 
								
								return false;
							}
						}
						
						if (mParent.power().recoveryReboot()) {
							return true;
						}
						
						openNew(cacheLocation + "/recovery/command").remove();
					}
				}
			}
			
			return false;
		}
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
	public static class FileStat extends BasicContainer {
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
