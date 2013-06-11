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

package com.spazedog.rootfw.extender;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.container.Data;
import com.spazedog.rootfw.container.FileStat;
import com.spazedog.rootfw.container.ShellProcess;
import com.spazedog.rootfw.container.ShellResult;
import com.spazedog.rootfw.iface.Extender;

public final class File implements Extender {
	public final static String TAG = RootFW.TAG + "::File";
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	private final static Pattern oPatternStatSplitter = Pattern.compile("\\|");
	private final static Pattern oPatternStatSearch = Pattern.compile("^([a-z-]+)(?:[ \t]+([0-9]+))?[ \t]+([0-9a-z_]+)[ \t]+([0-9a-z_]+)(?:[ \t]+(?:([0-9]+),[ \t]+)?([0-9]+))?[ \t]+([A-Za-z]+[ \t]+[0-9]+[ \t]+[0-9:]+|[0-9-/]+[ \t]+[0-9:]+)[ \t]+(?:(.*) -> )?(.*)$");
	
	private static Map<String, String> oFileTypes = new HashMap<String, String>();
	
	static {
		oFileTypes.put("d", "d");
		oFileTypes.put("b", "b");
		oFileTypes.put("f", "f");
		oFileTypes.put("c", "c");
		oFileTypes.put("l", "L");
		oFileTypes.put("e", "e");
	}
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.file</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public File(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Get the md5sum of a file
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     An md5sum of the file
	 */
	public String md5sum(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary md5sum '" + aFile + "'") );
		String lMD5Sum = null;
		
		if (lResult != null && lResult.code() == 0) {
			lMD5Sum = lResult.output().line(-1);
			
			if (lMD5Sum != null) {
				lMD5Sum = lMD5Sum.split(" ")[0].trim();
			}
		}
		
		RootFW.log(TAG + ".md5sum", "Returning md5sum '" + lMD5Sum + "'");
		
		return lMD5Sum;
	}
	
	/**
	 * Get information about a file or folder like size, type, 
	 * symlink, mm, access, permissions, ownership etc
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     A FileStat container with all of the file/folder information or NULL if no such file or folder exists
	 */
	public FileStat stat(String aFile) {
		ArrayList<FileStat> list = statList(aFile, 1);
		
		if (list != null) {
			String lFullPath = !aFile.equals("/") ? (aFile.endsWith("/") ? aFile.substring(0, aFile.length() - 1) : aFile) : aFile;
			String lPath = !aFile.equals("/") ? lFullPath.substring(0, lFullPath.lastIndexOf("/")) : lFullPath;
			String lName = !aFile.equals("/") ? lFullPath.substring(lFullPath.lastIndexOf("/") + 1) : lFullPath;
			
			RootFW.log(TAG + ".stat", "Collecting stat on " + lName + " located in " + lPath, RootFW.E_DEBUG);
			
			if (".".equals(list.get(0).name())) {
				/* We do not want to return a list containing name="." */
				return new FileStat(lName, list.get(0).link(), list.get(0).type(), list.get(0).user(), list.get(0).group(), list.get(0).access(), list.get(0).permission(), list.get(0).mm(), list.get(0).size());
				
			} else if (lName.equals(list.get(0).name())) {
				return list.get(0);
				
			} else if (!aFile.equals("/")) {
				list = statList(aFile);
				
				/* On devices without busybox, we could end up using limited toolbox versions
				 * that does not support the "-a" argument in it's "ls" command. In this case,
				 * we need to do a more manual search for folders.
				 */
				
				if (list != null) {
					for (int i=0; i < list.size(); i++) {
						if (lName.equals(list.get(i).name())) {
							return list.get(i);
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * See <code>statList(String aFile, Integer aMaxLines)</code>
	 */
	public ArrayList<FileStat> statList(String aFile) {
		return statList(aFile, null);
	}
	
	/**
	 * Get a complete list of folder items.
	 * <p/>
	 * The method will return a FileStat container per each item in the folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @param aMaxLines
	 *     Max items to return. This also takes negative values to cut from the bottom
	 *    
	 * @return 
	 *     An ArrayList with all FileStat items or NULL if no such folder exists
	 */
	public ArrayList<FileStat> statList(String aFile, Integer aMaxLines) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary ls -lna '" + aFile + "'", "%binary ls -la '" + aFile + "'", "%binary ls -ln '" + aFile + "'", "%binary ls -l '" + aFile + "'"} ) );
		
		if (lResult != null && lResult.code() == 0 && lResult.output().length() > 0) {
			ArrayList<FileStat> list = new ArrayList<FileStat>();
			Integer x, i = 0, lMod, lCount = 1, lMax = (aMaxLines == null ? lResult.output().length() : (aMaxLines < 0 ? lResult.output().length() + aMaxLines : aMaxLines));

			String[] lSection, lSectionModAssambler;
			String lSectionType, lSectionAccess, lSectionMM, lSectionName, lSectionLink, lSectionPermission;
			Integer lSectionUser, lSectionGroup;
			Long lSectionSize;
			
			while (lCount <= lMax && i < lResult.output().length()) {
				try {
					lSection = oPatternStatSplitter.split( oPatternStatSearch.matcher(lResult.output().line(i)).replaceAll("$1|$3|$4|$5|$6|$8|$9") );
					
					if (lSection.length == 7) {
						lSectionType = lSection[0].substring(0, 1).equals("-") ? "f" : lSection[0].substring(0, 1);
						lSectionAccess = lSection[0];
						lSectionUser = Integer.parseInt(lSection[1]);
						lSectionGroup = Integer.parseInt(lSection[2]);
						lSectionSize = lSection[4].equals("null") || !lSection[3].equals("null") ? 0L : Long.parseLong(lSection[4]);
						lSectionMM = lSection[3].equals("null") ? null : lSection[3] + ":" + lSection[4];
						lSectionName = lSection[5].equals("null") ? lSection[6] : lSection[5];
						lSectionLink = lSection[5].equals("null") ? null : lSection[6];
						lSectionPermission = "";
						lSectionModAssambler = new String[] {lSectionAccess.substring(1), lSectionAccess.substring(1, 4), lSectionAccess.substring(4, 7), lSectionAccess.substring(7, 10)};
						
						lSectionName = lSectionName.substring( lSectionName.lastIndexOf("/") + 1 );
						
						for (x=0; x < lSectionModAssambler.length; x++) {
							lMod = (x == 0 && lSectionModAssambler[x].charAt(2) == 's') || (x > 0 && lSectionModAssambler[x].charAt(0) == 'r') ? 4 : 0;
							lMod += (x == 0 && lSectionModAssambler[x].charAt(5) == 's') || (x > 0 && lSectionModAssambler[x].charAt(1) == 'w') ? 2 : 0;
							lMod += (x == 0 && lSectionModAssambler[x].charAt(8) == 't') || (x > 0 && lSectionModAssambler[x].charAt(2) == 'x') ? 1 : 0;
							
							lSectionPermission += lMod;
						}
						
						RootFW.log(TAG + ".statList", "Adding item " + lCount + " {name=" + lSectionName + ", link=" + lSectionLink + ", type=" + lSectionType + ", user=" + lSectionUser + ", group=" + lSectionGroup + ", access=" + lSectionAccess + ", permission=" + lSectionPermission + ", mm=" + lSectionMM + ", size=" + lSectionSize + "}", RootFW.E_DEBUG);
						
						list.add( new FileStat(lSectionName, lSectionLink, lSectionType, lSectionUser, lSectionGroup, lSectionAccess, lSectionPermission, lSectionMM, lSectionSize) );
						
						lCount += 1;	
					}
					
				} catch(Throwable e) { RootFW.log(TAG + ".statList", "Failed while adding item " + lCount + " to the list", RootFW.E_WARNING); }
				
				i += 1;
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
	
	/**
	 * Get a complete list of folder items.
	 * <p/>
	 * This list only contains the names of the folder content. 
	 * Use <code>statList()</code> for full stats
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     An Array with all the folder content or NULL if no such folder exists
	 */
	public String[] list(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary ls -1a '" + aFile + "'", "%binary ls -1 '" + aFile + "'"} ) );
		
		if (lResult != null && lResult.code() == 0) {
			return lResult.output().raw();
			
		} else {
			ArrayList<FileStat> list = statList(aFile);
			
			/* Most toolbox versions does not support the "-1" argument in the "ls" command.
			 * In these cases without busybox, we fall back to using the statList() method
			 * and manually create our list.
			 */
			
			if (list != null) {
				String[] lLines = new String[list.size()];
				
				for (int i=0; i < list.size(); i++) {
					lLines[i] = list.get(i).name();
				}
				
				return lLines.length > 0 ? lLines : null;
			}
		}
		
		return null;
	}
	
	/**
	 * Check the type of a file or folder or whether it exists or not
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @param aType
	 *     The type to check for ('d'=>Directory, 'f'=>File, 'b'=>Block Device, 'c'=>Character Device, 'l'=>Symbolic Link, 'e'=>Exists)
	 *    
	 * @return
	 *     <code>True</code> if the type matches
	 */
	public Boolean check(String aFile, String aType) {
		if (oFileTypes.get(aType) != null) {
			String lTypeSearch = oFileTypes.get(aType);
			ShellResult lResult = mParent.shell.execute( ShellProcess.generate("( %binary test -" + lTypeSearch + " '" + aFile + "' && %binary echo true ) || ( %binary test ! -" + lTypeSearch + " '" + aFile + "' && %binary echo false )") );
			
			if (lResult != null && lResult.code() == 0) {
				return "true".equals( lResult.output().line() );
				
			} else {
				FileStat lStat = stat(aFile);
				
				return lStat != null && (lTypeSearch.equals("e") || lTypeSearch.equals( lStat.type() ) || aType.equals( lStat.type() ));
			}
		}
		
		return false;
	}
	
	/**
	 * Copy a file or folder to another location
	 * 
	 * @param aSrcFile
	 *     Full path to the file
	 *    
	 * @param aDstFile
	 *     Full path to the destination
	 *    
	 * @return
	 *     <code>True</code> if it copied successfully 
	 */
	public Boolean copy(String aSrcFile, String aDstFile) {
		ShellResult lResult = null;
		
		if (check(aSrcFile, "d")) {
			lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary cp -a '" + aSrcFile + "' '" + aDstFile + "'", "%binary cp -fa '" + aSrcFile + "' '" + aDstFile + "'"} ) );
			
		} else if (!check(aDstFile, "d")) {
			lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary cp '" + aSrcFile + "' '" + aDstFile + "'", "%binary cp -f '" + aSrcFile + "' '" + aDstFile + "'"} ) );
			
			if (lResult == null || lResult.code() != 0) {
				FileStat stat = stat(aSrcFile);
				
				lResult = mParent.shell.execute( ShellProcess.generate("%binary cat '" + aSrcFile + "' > '" + aDstFile + "'") );
				
				if (lResult == null || lResult.code() != 0 || stat == null || !setOwner(aDstFile, ""+stat.user(), ""+stat.group()) || !setPermission(aDstFile, stat.permission())) {
					return false;
				}
			}
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * See <code>copyResource(Context aContext, Integer aResourceId, String aDestination, String aPermission, String aUser, String aGroup)</code>
	 */
	public Boolean copyResource(Context aContext, Integer aResourceId, String aDestination) {
		return copyResource(aContext, aResourceId, aDestination, null, null, null);
	}
	
	/**
	 * See <code>copyResource(Context aContext, String aAssetsPath, String aDestination, String aPermission, String aUser, String aGroup)</code>
	 */
	public Boolean copyResource(Context aContext, String aAssetsPath, String aDestination) {
		return copyResource(aContext, aAssetsPath, aDestination, null, null, null);
	}
	
	/**
	 * See <code>copyResource(Context aContext, InputStream aResource, String aDestination, String aPermission, String aUser, String aGroup)</code>
	 */
	public Boolean copyResource(Context aContext, InputStream aResource, String aDestination) {
		return copyResource(aContext, aResource, aDestination, null, null, null);
	}
	
	/**
	 * See <code>copyResource(Context aContext, InputStream aResource, String aDestination, String aPermission, String aUser, String aGroup)</code>
	 */
	public Boolean copyResource(Context aContext, Integer aResourceId, String aDestination, String aPermission, String aUser, String aGroup) {
		try {
			InputStream lInputStream = aContext.getResources().openRawResource(aResourceId);
			Boolean status = copyResource(aContext, lInputStream, aDestination, aPermission, aUser, aGroup);
			lInputStream.close();
			
			return status;
			
		} catch(Throwable e) { return false; }
	}
	
	/**
	 * See <code>copyResource(Context aContext, InputStream aResource, String aDestination, String aPermission, String aUser, String aGroup)</code>
	 */
	public Boolean copyResource(Context aContext, String aAssetsPath, String aDestination, String aPermission, String aUser, String aGroup) {
		try {
			InputStream lInputStream = aContext.getAssets().open(aAssetsPath);
			Boolean status = copyResource(aContext, lInputStream, aDestination, aPermission, aUser, aGroup);
			lInputStream.close();
			
			return status;
			
		} catch(Throwable e) { return false; }
	}
	
	/**
	 * Copy a file from application resources into the file system on the device
	 * 
	 * @param aContext
	 *     The Application or Activity context
	 *    
	 * @param aResourceId
	 *     The resource id for the file
	 *     
	 * @param aDestination
	 *     Full destination path including the new file name
	 *    
	 * @param aPermission
	 *     Permissions to set after copy
	 *     
	 * @param aUser
	 *     User name or user id to set the ownership after copy
	 *    
	 * @param aGroup
	 *     Group name or group id to set the ownership after copy
	 *    
	 * @return
	 *     <code>True</code> if it copied successfully 
	 */
	public Boolean copyResource(Context aContext, InputStream aResource, String aDestination, String aPermission, String aUser, String aGroup) {
		if (!check(aDestination, "d")) {
			try {
				FileOutputStream lOutputStream = aContext.openFileOutput("rootfw.tmp.raw", 0);
				
				byte[] lBuffer = new byte[1024];
				Integer lLocation = 0;
				
				while ((lLocation = aResource.read(lBuffer)) > 0) {
					lOutputStream.write(lBuffer, 0, lLocation);
				}
				
				lOutputStream.close();
				
			} catch(Throwable e) { return false; }
			
			if (move(aContext.getFilesDir().getAbsolutePath() + "/rootfw.tmp.raw", aDestination)) {
				if ((aPermission == null || setPermission(aDestination, aPermission)) && ((aUser == null || aGroup == null) || setOwner(aDestination, aUser, aGroup))) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Move a file or folder to another location
	 * 
	 * @param aSrcFile
	 *     Full path to the file
	 *    
	 * @param aDstFile
	 *     Full path to the destination
	 *    
	 * @return
	 *     <code>True</code> if it copied successfully 
	 */
	public Boolean move(String aSrcFile, String aDstFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary mv '" + aSrcFile + "' '" + aDstFile + "'", "%binary mv -f '" + aSrcFile + "' '" + aDstFile + "'"} ) );
		
		if ((lResult == null || lResult.code() != 0) && !check(aDstFile, "d") && check(aSrcFile, "d")) {
			FileStat stat = stat(aSrcFile);
			
			lResult = mParent.shell.execute( ShellProcess.generate("%binary cat '" + aSrcFile + "' > '" + aDstFile + "' && %binary unlink '" + aSrcFile + "'") );
			
			if (lResult == null || lResult.code() != 0 || stat == null || !setOwner(aDstFile, ""+stat.user(), ""+stat.group()) || !setPermission(aDstFile, stat.permission())) {
				return false;
			}
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Remove all content from a file or folder
	 * <p/>
	 * Note that this does not delete the file or folder, 
	 * it just removes all content
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     <code>True</code> if it cleared successfully 
	 */
	public Boolean clear(String aFile) {
		ShellResult lResult = null;
		
		if (check(aFile, "d")) {
			String[] lFiles = list(aFile);
			String lCommand = "%binary rm -rf";
			String lPath = aFile.endsWith("/") ? aFile.substring(0, aFile.length() - 1) : aFile;
			
			/* Doing a simple "rm -rf path/*" will not be enough,
			 * as it will not delete any hidden files and folders
			 */
			
			for (int i=0; i < lFiles.length; i++) {
				if (!".".equals(lFiles[i]) && !"..".equals(lFiles[i])) {
					lCommand += " '" + lPath + "/" + lFiles[i] + "'";
				}
			}
			
			lResult = mParent.shell.execute( ShellProcess.generate(lCommand) );
			
		} else {
			lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary echo -n '' > '" + aFile + "'", "%binary echo '' > '" + aFile + "'"} ) );
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Delete a file or folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     <code>True</code> if it deleted successfully 
	 */
	public Boolean delete(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary rm -rf '" + aFile + "'") );
				
		if ((lResult == null || lResult.code() != 0) && !check(aFile, "d")) {
			lResult = mParent.shell.execute( ShellProcess.generate("%binary unlink '" + aFile + "'") );
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Create a new folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     <code>True</code> if it was created successfully 
	 */
	public Boolean create(String aFile) {
		ShellResult lResult = null;
		
		if (!check(aFile, "e")) {
			lResult = mParent.shell.execute( ShellProcess.generate("%binary mkdir -p '" + aFile + "'") );
			
			if (lResult == null || lResult.code() != 0) {
				String[] lFolders = ("/".equals(aFile) ? aFile : aFile.endsWith("/") ? aFile.substring(1, aFile.length() - 1) : aFile.substring(1)).split("/");
				String lPath = "";
				
				/* Not all busybox and toolbox versions support the "-p" argument in their "mkdir" command.
				 * In these cases, we will iterate through the tree manually
				 */
				
				for (int i=0; i < lFolders.length; i++) {
					lPath += "/" + lFolders[i];
					
					if (!check(lPath, "d")) {
						lResult = mParent.shell.execute( ShellProcess.generate("%binary mkdir '" + lPath + "'") );
						
						if (lResult == null || lResult.code() != 0) {
							break;
						}
					}
				}
			}
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * See <code>write(String aFile, String[] aData, Boolean aAppend)</code>
	 */
	public Boolean write(String aFile, String aData) {
		return write(aFile, aData.split("\n"), false);
	}
	
	/**
	 * See <code>write(String aFile, String[] aData, Boolean aAppend)</code>
	 */
	public Boolean write(String aFile, String[] aData) {
		return write(aFile, aData, false);
	}
	
	/**
	 * See <code>write(String aFile, String[] aData, Boolean aAppend)</code>
	 */
	public Boolean write(String aFile, String aData, Boolean aAppend) {
		return write(aFile, aData.split("\n"), aAppend);
	}
	
	/**
	 * Write data to a file
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @param aData
	 *     The data that should be written to the file
	 *    
	 * @param aAppend
	 *     Append the data to the already existing file content
	 *    
	 * @return
	 *     <code>True</code> if it was written successfully 
	 */
	public Boolean write(String aFile, String[] aData, Boolean aAppend) {
		ShellResult lResult = null;
		
		if (!check(aFile, "d")) {
			for (int i=0; i < aData.length; i++) {
				lResult = mParent.shell.execute( ShellProcess.generate("%binary echo '" + aData[i] + "' " + (aAppend || i > 0 ? ">>" : ">") + " '" + aFile + "'") );
				
				if (lResult == null || lResult.code() != 0) {
					break;
				}
			}
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Read the content from a file
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     A Data container with all of the file content or NULL on failure
	 */
	public Data read(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary cat '" + aFile + "'") );
		
		return lResult != null && lResult.code() == 0 ? lResult.output() : null;
	}
	
	/**
	 * Read the first line from a file
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     The first line from the file or NULL on failure
	 */
	public String readLine(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary sed -n '1p' '" + aFile + "'", "%binary cat '" + aFile + "'"} ) );
		
		return lResult != null && lResult.code() == 0 ? lResult.output().line(0) : null;
	}
	
	/**
	 * Convert a path into a real path.
	 * <p/>
	 * This could either be a link which would return the path to
	 * the real item or something line ../file which would be converted into /file
	 * 
	 * @param aFile
	 *     Path to the file
	 *    
	 * @return
	 *     The complete path to the real item or NULL on failure
	 */
	public String realPath(String aFile) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary readlink -f '" + aFile + "'") );
		String lPath = null;
		
		if (lResult != null && lResult.code() == 0) {
			lPath = lResult.output().line();
			
		} else {
			FileStat lStat = stat(aFile);
			
			if (lStat != null) {
				if (lStat.link() != null) {
					lPath = lStat.link();
					
				} else {
					if (!"d".equals(lStat.type())) {
						lResult = mParent.shell.execute( ShellProcess.generate("REALPATH=$(cd '" + aFile + "' && %binary pwd) && %binary echo $REALPATH") );
						
					} else {
						lResult = mParent.shell.execute( ShellProcess.generate("REALPATH=$(cd '" + aFile.substring(0, aFile.lastIndexOf("/")) + "' && %binary pwd) && %binary echo $REALPATH") );
					}
					
					if (lResult != null && lResult.code() == 0) {
						lPath = lResult.output().line() + (!"d".equals(lStat.type()) ? "" : "/" + lStat.name());
					}
				}
			}
		}
		
		return lPath;
	}
	
	/**
	 * See <code>setOwner(String aFile, String aUser, String aGroup, Boolean aRecursive)</code>
	 */
	public Boolean setOwner(String aFile, String aUser, String aGroup) {
		return setOwner(aFile, aUser, aGroup, false);
	}
	
	/**
	 * Change the ownership on a file or folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @param aUser
	 *     User Name or ID
	 *    
	 * @param aGroup
	 *     Group Name or ID
	 *    
	 * @param aRecursive
	 *     Change folders recursively
	 *    
	 * @return
	 *     <code>True</code> if it was changed successfully 
	 */
	public Boolean setOwner(String aFile, String aUser, String aGroup, Boolean aRecursive) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary chown " + (aRecursive && check(aFile, "d") ? "-R" : "") + " '" + (aUser + "." + aGroup) + "' '" + aFile + "'") );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * See <code>setPermission(String aFile, String aPermission, Boolean aRecursive)</code>
	 */
	public Boolean setPermission(String aFile, String aPermission) {
		return setPermission(aFile, aPermission, false);
	}
	
	/**
	 * Change the permissions on a file or folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @param aPermission
	 *     Permission string like (0755)
	 *    
	 * @param aRecursive
	 *     Change folders recursively
	 *    
	 * @return
	 *     <code>True</code> if it was changed successfully 
	 */
	public Boolean setPermission(String aFile, String aPermission, Boolean aRecursive) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary chmod " + (aRecursive && check(aFile, "d") ? "-R" : "") + " '" + aPermission + "' '" + aFile + "'") );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * See <code>diskUsage(String aFile)</code>
	 */
	public Long diskUsage(String[] aFiles) {
		Long lUsage = null;
		
		for (int i=0; i < aFiles.length; i++) {
			Long tUsage = diskUsage(aFiles[i]);
			
			if (tUsage != null) {
				if (lUsage != null) {
					lUsage += tUsage;
					
				} else {
					lUsage = tUsage;
				}
			}
		}
		
		return lUsage;
	}
	
	/**
	 * Get the disk usage of a file or folder
	 * 
	 * @param aFile
	 *     Full path to the file
	 *    
	 * @return
	 *     The disk usage in bytes or NULL on failure
	 */
	public Long diskUsage(String aFile) {
		if (check(aFile, "d")) {
			ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary du -sbx '" + aFile + "'", "%binary du -skx '" + aFile + "'"} ) );
			
			if (lResult != null && lResult.code() == 0) {
				String lOutout = lResult.output().line();
				Long lUsage = Long.parseLong( oPatternSpaceSearch.split(lOutout.trim())[0] );
				
				if (lResult.command() > (ShellProcess.BINARIES.length + 1)) {
					lUsage = lUsage * 1024L;
				}
				
				return lUsage;
			}
			
		} else {
			ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary wc -c < '" + aFile + "'", "%binary wc < '" + aFile + "'"} ) );
			
			if (lResult != null && lResult.code() == 0) {
				String lOutout = lResult.output().line();
				
				if (lResult.command() > (ShellProcess.BINARIES.length + 1)) {
					return Long.parseLong( oPatternSpaceSearch.split(lOutout.trim())[2] );
					
				} else {
					return Long.parseLong(lOutout);
				}
			}
		}
		
		return null;
	}
}
