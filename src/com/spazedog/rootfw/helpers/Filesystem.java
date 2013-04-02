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

package com.spazedog.rootfw.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import android.content.Context;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.DiskInfo;
import com.spazedog.rootfw.containers.FileData;
import com.spazedog.rootfw.containers.FileInfo;
import com.spazedog.rootfw.containers.FileStat;
import com.spazedog.rootfw.containers.MountInfo;
import com.spazedog.rootfw.containers.ShellCommand;
import com.spazedog.rootfw.containers.ShellResult;

public final class Filesystem {
	public final static String TAG = RootFW.TAG + "::Filesystem";
	
	public final static Pattern FILELIST_PATTERN = Pattern.compile("^([a-z-]+)(?:[ \t]+([0-9]+))?[ \t]+([0-9a-z_]+)[ \t]+([0-9a-z_]+)(?:[ \t]+([0-9]+[a-z]?))?[ \t]+([A-Za-z]+[ \t]+[0-9]+[ \t]+[0-9:]+|[0-9-/]+[ \t]+[0-9:]+)[ \t]+(?:(.*) -> )?(.*)$");
	public final static Pattern FILELIST_SPLITTER = Pattern.compile("\\|");
	
	private RootFW ROOTFW;
	
	public Filesystem(RootFW argAccount) {
		ROOTFW = argAccount;
	}

	public FileInfo getFileInfo(String argPath) {
		ArrayList<FileInfo> fileList = getFileListInfo(argPath, 1);
		String name = argPath;
		
		if (!argPath.equals("/")) {
			if (argPath.endsWith("/")) {
				argPath = argPath.substring(0, argPath.length() - 1);
			}
			
			name = argPath.substring(argPath.lastIndexOf("/") + 1);
		}
		
		if (fileList != null && (fileList.get(0).getName().equals(".") || fileList.get(0).getName().equals(name))) {
			if (fileList.get(0).getName().equals(".")) {
				FileInfo fileInfo = fileList.get(0);
				
				return new FileInfo(name, fileInfo.getType(), fileInfo.getUser(), fileInfo.getGroup(), fileInfo.getPermissions(), fileInfo.getPermissionString(), fileInfo.getLink(), fileInfo.getSize());
			}
			
			return fileList.get(0);
			
		} else if (fileList != null && !argPath.equals("/")) {
			/* Some toolbox versions does not support the "-a" argument in "ls". 
			 * In this case we need to loop through all the content in the parent in order to find the correct one
			 */
			String path = argPath.substring(0, argPath.lastIndexOf("/"));
			fileList = getFileListInfo(path);
			Integer i;
			
			if (fileList != null) {
				for (i=0; i < fileList.size(); i++) {
					if (fileList.get(i).getName().equals(name)) {
						return fileList.get(i);
					}
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<FileInfo> getFileListInfo(String argPath) {
		return getFileListInfo(argPath, null);
	}
	
	public ArrayList<FileInfo> getFileListInfo(String argPath, Integer argMax) {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"%binary ls -lna '" + argPath + "'", "%binary ls -la '" + argPath + "'", "%binary ls -ln '" + argPath + "'", "%binary ls -l '" + argPath + "'"} ));
		
		if (result != null && result.getResultCode() == 0) {
			String[] lines = result.getResult().getData(), lineParts;
			String partUser=null, partGroup=null, partPermission=null, partLink=null, partType=null, partName=null, partMod=null;
			Long partSize = 0L;
			Integer i = 0, x, z, count = 1, max = argMax == null ? lines.length : (argMax > 0 ? argMax : lines.length - argMax);
			
			while (count <= max && i < lines.length) {
				RootFW.log(TAG + ".getFileListInfo", "Checking (" + lines[i] + ")");
				
				lineParts = FILELIST_SPLITTER.split( FILELIST_PATTERN.matcher(lines[i]).replaceAll("$1|$3|$4|$5|$7|$8") );
				
				if (lineParts.length == 6) {
					partType = lineParts[0].substring(0, 1);
					partPermission = lineParts[0];
					partUser = lineParts[1];
					partGroup = lineParts[2];
					partSize = lineParts[3].equals("null") ? 0L : Long.parseLong(lineParts[3]);
					partName = lineParts[4].equals("null") ? lineParts[5] : lineParts[4];
					partLink = lineParts[4].equals("null") ? null : lineParts[5];
					
					String[] permsPart = {partPermission.substring(1), partPermission.substring(1, 4), partPermission.substring(4, 7), partPermission.substring(7, 10)};
					partMod="";
					for (x=0; x < permsPart.length; x++) {
						z = (x == 0 && permsPart[x].charAt(2) == 's') || (x > 0 && permsPart[x].charAt(0) == 'r') ? 4 : 0;
						z += (x == 0 && permsPart[x].charAt(5) == 's') || (x > 0 && permsPart[x].charAt(1) == 'w') ? 2 : 0;
						z += (x == 0 && permsPart[x].charAt(8) == 't') || (x > 0 && permsPart[x].charAt(2) == 'x') ? 1 : 0;
						
						partMod += z;
					}
					
					RootFW.log(TAG + ".getFileListInfo", "Adding FileInfo(Name=" + partName + ", Type=" + partType + ", User=" + partUser + ", Group=" + partGroup + ", Mod=" + partMod + ", Permissions=" + partPermission + ", Link=" + partLink + ", Size=" + partSize + ")");
					
					list.add( new FileInfo(partName, partType, partUser, partGroup, partMod, partPermission, partLink, partSize) );
					
					count += 1;
				}
				
				i += 1;
			}
		}
		
		if (list.size() == 0) {
			RootFW.log(TAG + ".getFileListInfo", "No items was added to FileInfo");
		}
		
		return list.size() > 0 ? list : null;
	}
	
	public Boolean exist(String argPath) {
		RootFW.log(TAG + ".exist", "Checking existence of " + argPath);
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -e '" + argPath + "' && %binary echo true ) || ( %binary test ! -e '" + argPath + "' && %binary echo false )"));

		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = getFileInfo(argPath) != null ? true : false;
		}
		
		RootFW.log(TAG + ".exist", argPath + " was " + (status ? "" : "not") + " found");
		
		return status;
	}
	
	public Boolean isFile(String argPath) {
		RootFW.log(TAG + ".isFile", "Checking if " + argPath + " is a file");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -f '" + argPath + "' && %binary echo true ) || ( %binary test ! -f '" + argPath + "' && %binary echo false )"));
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("-") ? true : false;
		}

		RootFW.log(TAG + ".isFile", argPath + " was " + (status ? "" : "not") + " identified as a file");
		
		return status;
	}
	
	public Boolean isDir(String argPath) {
		RootFW.log(TAG + ".isDir", "Checking if " + argPath + " is a directory");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -d '" + argPath + "' && %binary echo true ) || ( %binary test ! -d '" + argPath + "' && %binary echo false )"));
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("d") ? true : false;
		}

		RootFW.log(TAG + ".isDir", argPath + " was " + (status ? "" : "not") + " identified as a directory");
		
		return status;
	}
	
	public Boolean isLink(String argPath) {
		RootFW.log(TAG + ".isLink", "Checking if " + argPath + " is a symbolic link");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -L '" + argPath + "' && %binary echo true ) || ( %binary test ! -L '" + argPath + "' && %binary echo false )"));
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("l") ? true : false;
		}
		
		RootFW.log(TAG + ".isLink", argPath + " was " + (status ? "" : "not") + " identified as a symbolic link");
		
		return status;
	}
	
	public Boolean isBlockDevice(String argPath) {
		RootFW.log(TAG + ".isBlockDevice", "Checking if " + argPath + " is a block device");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -b '" + argPath + "' && %binary echo true ) || ( %binary test ! -b '" + argPath + "' && %binary echo false )"));
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("b") ? true : false;
		}
		
		RootFW.log(TAG + ".isBlockDevice", argPath + " was " + (status ? "" : "not") + " identified as a block device");
		
		return status;
	}
	
	public Boolean isCharacterDevice(String argPath) {
		RootFW.log(TAG + ".isCharacterDevice", "Checking if " + argPath + " is a character device");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("( %binary test -c '" + argPath + "' && %binary echo true ) || ( %binary test ! -c '" + argPath + "' && %binary echo false )"));
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = "true".equals(result.getResult().getLastLine()) ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("c") ? true : false;
		}
		
		RootFW.log(TAG + ".isCharacterDevice", argPath + " was " + (status ? "" : "not") + " identified as a character device");
		
		return status;
	}
	
	public Boolean copyFileResource(Context argContext, Integer argSrc, String argDes, String argPerms, String argUser, String argGroup) {
		RootFW.log(TAG + ".copyFileResource", "Starting file copy from " + argContext.getPackageName() + " resources into " + argDes);
		
		if (!isDir(argDes)) {
			InputStream in = argContext.getResources().openRawResource( argSrc );
			FileOutputStream out = null;
			
			try {
				out = argContext.openFileOutput("tmp.raw", 0);
				
			} catch (FileNotFoundException e) {
				RootFW.log(TAG + ".copyFileResource", "Could not copy " + argContext.getPackageName() + " resources into " + argDes, RootFW.LOG_ERROR, e);
				
				return false; 
			}
			
			byte[] buff = new byte[1024];
			Integer read = 0;
			
			try {
				while ((read = in.read(buff)) > 0) {
					out.write(buff, 0, read);
				}
				
			} catch (IOException e) {
				RootFW.log(TAG + ".copyFileResource", "Could not copy " + argContext.getPackageName() + " resources into " + argDes, RootFW.LOG_ERROR, e);
				
				return false; 
			}
			
			try {
				in.close();
				out.close();
				
			} catch (IOException e) {}
			
			if(moveFile(argContext.getFilesDir().getAbsolutePath() + "/tmp.raw", argDes)) {
				if (setPermissions(argDes, argPerms) && setOwner(argDes, argUser, argGroup)) {
					
					RootFW.log(TAG + ".copyFileResource", "" + argContext.getPackageName() + " resource was successfully copied to " + argDes);
					
					return true;
				}
			}	
		}
		
		RootFW.log(TAG + ".copyFileResource", "Could not copy " + argContext.getPackageName() + " resources into " + argDes, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public Boolean copyFile(String argSrc, String argDes) {
		return copyFile(argSrc, argDes, null, null, null);
	}
	
	public Boolean copyFile(String argSrc, String argDes, String argPerms, String argUser, String argGroup) {
		RootFW.log(TAG + ".copyFile", "Start copying " + argSrc + " to " + argDes);
		
		if (argPerms == null || argUser == null || argGroup == null) {
			FileInfo fi;

			if ((fi = (FileInfo) getFileStat(argSrc)) != null || (fi = getFileInfo(argSrc)) != null) {
				argPerms = fi.getPermissions();
				argUser = fi.getUser();
				argGroup = fi.getGroup();
			}
		}
		
		if (argPerms != null && argUser != null && argGroup != null) {
			try {
				if (isFile(argSrc)) {
					String dest = !exist(argDes) || !isDir(argDes) ? argDes : 
						(argDes.endsWith("/") ? argDes.substring(0, argDes.length() - 1) : argDes) + "/" + argSrc.substring(argSrc.lastIndexOf("/") + 1);

					ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"%binary cp '" + argSrc + "' '" + dest + "'", "%binary cat '" + argSrc + "' > '" + dest + "'"} ));
					
					if (result != null && result.getResultCode() == 0) {
						if (setPermissions(dest, argPerms) && setOwner(dest, argUser, argGroup)) {
							RootFW.log(TAG + ".copyFile", "Successfully copied " + argSrc + " to " + dest);
							
							return true;
						}
					}
				}
				
			} catch(Throwable e) { RootFW.log(TAG + ".copyFile", "Could not copy " + argSrc + " to " + argDes, RootFW.LOG_ERROR, e); return false; }
		}
		
		RootFW.log(TAG + ".copyFile", "Could not copy " + argSrc + " to " + argDes, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public Boolean moveFile(String argSrc, String argDes) {
		RootFW.log(TAG + ".moveFile", "Start moving " + argSrc + " to " + argDes);
		
		try {
			if (isFile(argSrc)) {
				String dest = !exist(argDes) || !isDir(argDes) ? argDes : 
					(argDes.endsWith("/") ? argDes.substring(0, argDes.length() - 1) : argDes) + "/" + argSrc.substring(argSrc.lastIndexOf("/") + 1);
				
				Boolean status = false;
				ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"%binary mv '" + argSrc + "' '" + dest + "'", "%binary cat '" + argSrc + "' > '" + dest + "'"} ));
				
				if (result != null && result.getCommandNumber() > ShellCommand.getCompatibleBinaries().length) {
					FileInfo fi;
					
					if (result.getResultCode() == 0 && (fi = getFileInfo(argSrc)) != null) {
						if (setPermissions(argDes, fi.getPermissions()) && setOwner(argDes, fi.getUser(), fi.getGroup()) && rmFile(argSrc)) {
							status = true;
						}
					}
					
				} else if (result != null && result.getResultCode() == 0) {
					status = true;
				}
				
				if (status) {
					RootFW.log(TAG + ".moveFile", "Successfully moved " + argSrc + " to " + dest);
				
					return true;
				}
			}
			
		} catch(Throwable e) { RootFW.log(TAG + ".moveFile", "Could not move " + argSrc + " to " + argDes, RootFW.LOG_WARNING, e); return false; }
		
		RootFW.log(TAG + ".moveFile", "Could not move " + argSrc + " to " + argDes, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public FileData readFile(String argFile) {
		RootFW.log(TAG + ".readFile", "Reading content from " + argFile);
		
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary cat '" + argFile + "'"));
			
			if (result != null && result.getResultCode() == 0) {
				return result.getResult();
			}
		}
		
		RootFW.log(TAG + ".readFile", "Could not read content from " + argFile, RootFW.LOG_WARNING);
		
		return null;
	}
	
	public String readFileLine(String argFile) {
		RootFW.log(TAG + ".readFileLine", "Reading one line from " + argFile);
		
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary sed -n '1p' '" + argFile + "'", "%binary cat '" + argFile + "'"}));
			
			if (result != null && result.getResultCode() == 0) {
				RootFW.log(TAG + ".readFileLine", "Returning line content '" + result.getResult().getFirstLine() + "' from " + argFile);
				
				return result.getResult().getFirstLine();
			}
		}
		
		RootFW.log(TAG + ".readFileLine", "Could not read content from " + argFile, RootFW.LOG_WARNING);
		
		return null;
	}
	
	public Boolean putFileLine(String argFile, String argLineData) {
		return putFileLine(argFile, argLineData, false);
	}
	
	public Boolean putFileLine(String argFile, String argLineData, Boolean argAppend) {
		RootFW.log(TAG + ".putFileLine", "Appending data to " + argFile);
		
		if (!isDir(argFile)) {
			String path = argFile.substring(0, argFile.lastIndexOf("/"));

			if (mkDir(path)) {
				ShellResult result; 
				
				if (argAppend) {
					result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary echo '" + argLineData + "' >> '" + argFile + "'"));
				
				} else {
					result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary echo '" + argLineData + "' > '" + argFile + "'"));
				}
				
				if (result != null && result.getResultCode() == 0) {
					return true;
				}
			}
		}
		
		RootFW.log(TAG + ".putFileLine", "Could not append data to " + argFile);
		
		return false;
	}
	
	public Boolean setPermissions(String argPath, String argPerms) {
		return setPermissions(argPath, argPerms, false);
	}
	
	public Boolean setPermissions(String argPath, String argPerms, Boolean argRecursive) {
		RootFW.log(TAG + ".setPermissions", "Setting permissions on " + argPath + " to " + argPerms + (argRecursive ? " recursively" : ""));
		
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) { 
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary chmod -R " + argPerms + " '" + argPath + "'"));
				
			} else {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary chmod " + argPerms + " '" + argPath + "'"));
			}
			
			if (result != null && result.getResultCode() == 0) {
				return true;
			}
		}
		
		RootFW.log(TAG + ".setPermissions", "Could not set permissions on " + argPath, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup) {
		return setOwner(argPath, argUser, argGroup, false);
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup, Boolean argRecursive) {
		RootFW.log(TAG + ".setOwner", "Setting owner on " + argPath + " to " + argUser + "." + argGroup + (argRecursive ? " recursively" : ""));
		
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary chown -R " + (argUser + "." + argGroup) + " '" + argPath + "'"));
			
			} else {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary chown " + (argUser + "." + argGroup) + " '" + argPath + "'"));
			}
			
			if (result != null && result.getResultCode() == 0) {
				return true;
			}
		}
		
		RootFW.log(TAG + ".setOwner", "Could not set owner on " + argPath, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public Boolean rmFile(String argPath) {
		RootFW.log(TAG + ".rmFile", "Deleting file " + argPath);
		
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary unlink '" + argPath + "'", "%binary rm -rf '" + argPath + "'"}));
			
			if (result == null || result.getResultCode() != 0) {
				RootFW.log(TAG + ".rmFile", "Could not delete the file " + argPath, RootFW.LOG_WARNING); return false;
			}
		}
		
		return true;
	}
	
	public Boolean rmDir(String argPath) {
		RootFW.log(TAG + ".rmDir", "Deleting directory " + argPath);
		
		if (argPath.length() > 0 && isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary rmdir '" + argPath + "'", "%binary rm -rf '" + argPath + "'"}));
			
			if (result == null || result.getResultCode() != 0) {
				RootFW.log(TAG + ".rmDir", "Could not delete the directory " + argPath, RootFW.LOG_WARNING); return false;
			}
		}
		
		return true;
	}
	
	public Boolean mkDir(String argPath) {
		RootFW.log(TAG + ".mkDir", "Creating directory " + argPath);
		
		if (argPath.length() > 0 && !isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary mkdir -p '" + argPath + "'"));
			
			if (result != null && result.getResultCode() != 0) {
				String path = argPath.endsWith("/") ? argPath.substring(0, -1) : argPath;
				path = path.startsWith("/") ? path.substring(1) : path;
				
				String[] paths = path.split("/");
				path = "/";
				for (int i=0; i < paths.length; i++) {
					path += paths[i] + "/";
					
					if (!isDir(path)) {
						result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary mkdir '" + argPath + "'"));
	
						if (result.getResultCode() != 0) {
							break;
						}
					}
				}
			}
			
			if(result == null || result.getResultCode() != 0) {
				RootFW.log(TAG + ".mkDir", "Could not create the directory " + argPath, RootFW.LOG_WARNING); return false;
			}
			
		}
			
		return true;
	}
	
	public Boolean emptyDir(String argPath) {
		RootFW.log(TAG + ".emptyDir", "Cleaning out the directory " + argPath);
		
		if (isDir(argPath)) {
			String dir = argPath.endsWith("/") ? argPath.substring(0, argPath.length() - 1) : argPath;
			
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary rm -rf '" + dir + "/*'"));
			
			if(result != null && result.getResultCode() == 0) {
				return true;
			}
		}
		
		RootFW.log(TAG + ".emptyDir", "Could not clean out the directory " + argPath, RootFW.LOG_WARNING);
		
		return false;
	}
	
	public Boolean mvmount(String argOldMountPoint, String argNewMountPoint) {
		RootFW.log(TAG + ".mvmount", "Moving mount point " + argOldMountPoint + " to " + argNewMountPoint);
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary mount --move '" + argOldMountPoint + "' '" + argNewMountPoint + "'"));
		
		if(result != null && result.getResultCode() == 0) {
			return true;
			
		} else {
			RootFW.log(TAG + ".mvmount", "Failed to move the mount point, trying manunal unmount/remount", RootFW.LOG_WARNING);
			
			MountInfo mountInfo = getDiskMount(argOldMountPoint);
			
			if (mountInfo != null && unmount(argOldMountPoint)) {
				return mount(mountInfo.getDevice(), argNewMountPoint, mountInfo.getFsType(), mountInfo.getFlags().toString());
			}
		}
		
		return false;
	}
	
	public Boolean remount(String argMountPoint, String argOptions) {
		return mount(argMountPoint, "remount," + argOptions);
	}
	
	public Boolean unmount(String argDevice) {
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary umount '" + argDevice + "'", "%binary umount -f '" + argDevice + "'"}));
		
		return result != null && result.getResultCode() == 0 ? true : false;
	}
	
	public Boolean mount(String argMountPoint, String argOptions) {
		return mount(null, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argOptions) {
		return mount(argDevice, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argFileSystem, String argOptions) {
		String mount = "%binary mount" + (argDevice != null ? " '" + argDevice : "'") + (argFileSystem != null ? " -t " + argFileSystem : "") + (argOptions != null ? " -o " + argOptions : "") + " '" + argMountPoint + "'";
		
		RootFW.log(TAG + ".mount", "Running mount '" + mount + "'");
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(mount));
		
		if(result != null && result.getResultCode() == 0) {
			return true;
		}
		
		RootFW.log(TAG + ".mount", "Mounting failed", RootFW.LOG_WARNING);
		
		return false;
	}
	
	public ArrayList<MountInfo> getMounts() {
		RootFW.log(TAG + ".getMounts", "Collecting mount information");
		
		FileData filedata = readFile("/proc/mounts");
		
		if (filedata != null) {
			String[] mounts = filedata.getData(), line;
			
			ArrayList<MountInfo> list = new ArrayList<MountInfo>();
			
			if (mounts != null) {
				for (int i=0; i < mounts.length; i++) {
					line = RootFW.replaceAll(mounts[i], "  ", " ").trim().split(" ");
					
					RootFW.log(TAG + ".getMounts", "Adding MountInfo(Device=" + line[0] + ", MountPoint=" + line[1] + ", FSType=" + line[2] + ", Flags=" + line[3] + ")");
					
					list.add(new MountInfo(line[0], line[1], line[2], line[3].split(",")));
				}
				
				return list;
			}
		}
		
		RootFW.log(TAG + ".getMounts", "Could not collect any mount information", RootFW.LOG_WARNING);
		
		return null;
	}
	
	public MountInfo getDiskMount(String argDevice) {
		RootFW.log(TAG + ".getDiskMount", "Getting mount information on " + argDevice);
		
		Boolean blockdevice;
		
		if ((blockdevice = isBlockDevice(argDevice)) || isDir(argDevice)) {
			ArrayList<MountInfo> mounts = getMounts();
			
			if (mounts != null) {
				String path = null;
				
				if (!blockdevice) {
					path = argDevice.endsWith("/") ? argDevice.substring(0, argDevice.length()-1) : argDevice;
					
					do {
						for (int i=0; i < mounts.size(); i++) {
							if (mounts.get(i).getMountPoint().equals(path)) {
								return mounts.get(i);
							}
						}
						
					} while (path.lastIndexOf("/") > 0 && !(path = path.substring(0, path.lastIndexOf("/"))).equals(""));
					
				} else {
					for (int i=0; i < mounts.size(); i++) {
						if (mounts.get(i).getDevice().equals(argDevice)) {
							return mounts.get(i);
						}
					}
				}
			}
		}
		
		RootFW.log(TAG + ".getDiskMount", "Could not get mount information on " + argDevice);
		
		return null;
	}
	
	public Boolean isMounted(String argDevice) {
		RootFW.log(TAG + ".isMounted", "Checking whether or not " + argDevice + " is mounted");
		
		Boolean blockdevice;
		
		if ((blockdevice = isBlockDevice(argDevice)) || isDir(argDevice)) {
			ArrayList<MountInfo> mounts = getMounts();
			String path = null;
			
			if (!blockdevice) {
				path = argDevice.endsWith("/") ? argDevice.substring(0, argDevice.length()-1) : argDevice;
			}
			
			if (mounts != null) {
				for (int i=0; i < mounts.size(); i++) {
					if ((blockdevice && mounts.get(i).getDevice().equals(argDevice)) || (!blockdevice && mounts.get(i).getMountPoint().equals(path))) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public Boolean hasMountFlag(String argDevice, String argOption) {
		RootFW.log(TAG + ".hasMountFlag", "Checking if " + argDevice + " has the mount flag '" + argOption + "'");
		
		MountInfo mi = getDiskMount(argDevice);
		
		if (mi != null) {
			String[] mo = mi.getFlags();
			
			for (int i=0; i < mo.length; i++) {
				if (mo[i].equals(argOption)) {
					RootFW.log(TAG + ".hasMountFlag", "The mount flag '" + argOption + "' exists on " + argDevice);
					
					return true;
				}
			}
			
			RootFW.log(TAG + ".hasMountFlag", "The mount flag '" + argOption + "' does not exist on " + argDevice);
			
		} else {
			RootFW.log(TAG + ".hasMountFlag", "Could not check mount flags on " + argDevice, RootFW.LOG_WARNING);
		}
		
		return false;
	}
	
	/*
	 * This method will provide disk information from a specific path.
	 * The information provided are device path, mountpoint, disk usage, 
	 * disk size, disk remaining space and usage percentage.
	 * 
	 * The information provided are the same as a proper supported busybox df command,
	 * even on systems with low supported toolbox df command.
	 */
	public DiskInfo getDiskInfo(String argPath) {
		RootFW.log(TAG + ".getDiskInfo", "Getting disk information on " + argPath);
		
		Boolean blockdevice;
		
		if ((blockdevice = isBlockDevice(argPath)) || isDir(argPath)) {
			MountInfo mountinfo = null;
			
			if (blockdevice) {
				mountinfo = getDiskMount(argPath);
				
				if (mountinfo != null) {
					argPath = mountinfo.getMountPoint();
				}
			}
			
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary df -k '" + argPath + "'", "%binary df '" + argPath + "'"}));

			String[] parts = null, data = null;
			Long size=null, usage=null, remaining=null;
			String device=null, mountpoint=null, repacked="";
			Integer percentage;
			
			if (result != null && result.getCommandNumber() > ShellCommand.getCompatibleBinaries().length) {
				if (result.getResultCode() == 0) {
					data = result.getResult().getData();
					for (int y=1; y < data.length; y++) {
						repacked = data[y] + " ";
					}
					parts = RootFW.replaceAll(repacked, "  ", " ").trim().split(" ");
					
					String prefix;
					String[] prefixes = {"k", "m", "g", "t"};
					Double tmpSize;
					for(int i=1; i < 4; i++) {
						if (parts[i].matches("^.*[A-Za-z]$")) {
							prefix = parts[i].substring(parts[i].length()-1).toLowerCase(Locale.US);
							tmpSize = Double.parseDouble(parts[i].substring(0, parts[i].length()-1));

							if (!prefix.equals("b") && tmpSize > 0) {
								for (int x=0; x < prefixes.length; x++) {
									tmpSize = tmpSize * 1024D;
									
									if (prefixes[x].equals(prefix)) {
										break;
									}
								}
							}
							
							switch(i) {
								case 1: size = tmpSize.longValue(); break;
								case 2: usage = tmpSize.longValue(); break;
								case 3: remaining = tmpSize.longValue(); break;
							}
							
						} else {
							switch(i) {
								case 1: size = (Long.parseLong(parts[i]) * 1024L); break;
								case 2: usage = (Long.parseLong(parts[i]) * 1024L); break;
								case 3: remaining = (Long.parseLong(parts[i]) * 1024L); break;
							}
						}
					}
				}
				
			} else if (result != null) {
				data = result.getResult().getData();
				for (int y=1; y < data.length; y++) {
					repacked = data[y] + " ";
				}
				parts = RootFW.replaceAll(repacked, "  ", " ").trim().split(" ");
				
				size = (Long.parseLong(parts[1]) * 1024L);
				usage = (Long.parseLong(parts[2]) * 1024L);
				remaining = (Long.parseLong(parts[3]) * 1024L);
			}
			
			if (result != null && result.getResultCode() == 0) {
				if (parts.length >= 6 && isBlockDevice(parts[0])) {
					percentage = Integer.parseInt(parts[4].endsWith("%") ? parts[4].substring(0, parts[4].length()-1) : parts[4]);
					device = parts[0];
					mountpoint = parts[5];

				} else {
					percentage = ((Long) ((usage * 100L) / size)).intValue();
					
					if (mountinfo == null) {
						mountinfo = getDiskMount(argPath);
					}
					
					if (mountinfo != null) {
						device = mountinfo.getDevice();
						mountpoint = mountinfo.getMountPoint();
					}
				}
				
				RootFW.log(TAG + ".getDiskInfo", "Extracting information string (" + repacked + ")");
				RootFW.log(TAG + ".getDiskInfo", "Adding DiskInfo(Device=" + device + ", Size=" + size.toString() + ", Usage=" + usage.toString() + ", Remaining=" + remaining.toString() + ", Percentage=" + percentage + ", MountPoint=" + mountpoint + ")");
				
				return new DiskInfo(device, size, usage, remaining, percentage, mountpoint);
			}
		}
		
		RootFW.log(TAG + ".getDiskInfo", "Could not get disk information on " + argPath, RootFW.LOG_WARNING);
		
		return null;
	}
	
	public Long getFolderSize(String argPath) {
		RootFW.log(TAG + ".getFolderSize", "Getting folder size on " + argPath);
		
		if (argPath.length() > 0 && isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary du -skx '" + argPath + "'"));
			
			String output;
			
			if (result != null && result.getResultCode() == 0 && (output = result.getResult().getLastLine()) != null) {
				output = RootFW.replaceAll(output, "  ", " ").trim().split(" ")[0];
				
				/*
				 * Remove any size prefix like k or kb
				 */
				while (output.length() > 0 && !output.matches("^[0-9]+$")) {
					output = output.substring(0, output.length()-1);
				}
				
				try {
					Long size = (Long) (Long.parseLong(output) * 1024);
							
					RootFW.log(TAG + ".getFolderSize", "Calculated folder size on " + argPath + " to " + size.toString());
					
					return size;
					
				} catch(Throwable e) { RootFW.log(TAG + ".getFolderSize", "Could not get folder size on " + argPath, RootFW.LOG_ERROR, e); return null; }
			}
		}
		
		RootFW.log(TAG + ".getFolderSize", "Could not get folder size on " + argPath);
		
		return null;
	}
	
	public Long getFileSize(String argFile) {
		RootFW.log(TAG + ".getFileSize", "Getting file size on " + argFile);
		
		if (argFile.length() > 0 && isFile(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary wc -c < '" + argFile + "'", "%binary wc < '" + argFile + "'"}));
			
			String output;
			
			if (result != null && result.getResultCode() == 0 && (output = result.getResult().getLastLine()) != null) {
				if (result.getCommandNumber() > ShellCommand.getCompatibleBinaries().length) {
					output = RootFW.replaceAll(output, "  ", " ").trim().split(" ")[2];
				}
				
				try {
					Long size = Long.parseLong(output);
							
					RootFW.log(TAG + ".getFileSize", "Calculated file size on " + argFile + " to " + size.toString());
					
					return size;
					
				} catch(Throwable e) { RootFW.log(TAG + ".getFileSize", "Could not get folder size on " + argFile, RootFW.LOG_ERROR, e); return null; }
			}
		}
		
		RootFW.log(TAG + ".getFileSize", "Could not get file size on " + argFile);
		
		return null;
	}
	
	public String[] getFileList(String argPath) {
		RootFW.log(TAG + ".getFileList", "Getting getting file list for " + argPath);
		
		if (argPath.length() > 0) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"%binary ls -1a '" + argPath + "'", "%binary ls -1 '" + argPath + "'"}));
			
			if (result != null && result.getResultCode() == 0) {
				return result.getResult().getData();
				
			} else {
				ArrayList<FileInfo> fileList = getFileListInfo(argPath);
				
				if (fileList != null) {
					String[] lines = new String[fileList.size()];
					
					for (int i=0; i < fileList.size(); i++) {
						lines[i] = fileList.get(i).getName();
					}
					
					return fileList.size() > 0 ? lines : null;
				}
			}
		}
		
		RootFW.log(TAG + ".getFileList", "Could not get file list for " + argPath, RootFW.LOG_WARNING);
		
		return null;
	}
	
	private ArrayList<FileStat> fileStatBuilder(String[] argPaths) {
		if (argPaths != null && argPaths.length > 0) {
			
			ArrayList<FileStat> list = new ArrayList<FileStat>();
			
			ShellResult result;
			String output, lines[], part[], partFile, partType, partLink, partPerm, partMod, partGid, partUid, partGname, partUname, partAtime, partMtime, partCtime;
			Long partSize;
			Integer i, partBlocks, partIOBlock, partINode;
			
			for (int y=0; y < argPaths.length; y++) {
				if (argPaths[y].length() > 0 && !argPaths[y].endsWith(".")) {
					result = ROOTFW.runShell(ShellCommand.makeCompatibles("%binary stat '" + argPaths[y] + "'"));
					
					partBlocks = partIOBlock = partINode = null;
					partFile = partType = partLink = partPerm = partMod = partGid = partUid = partGname = partUname = partAtime = partMtime = partCtime = null;
					partSize = null;
				
					if (result != null && result.getResultCode() == 0 && (output = result.getResult().getAssembled()) != null) {
						lines = output.replaceAll("[ \t\n]+([A-Za-z ]+:)", "\n$1").split("\n");
						
						for (i=0; i < lines.length; i++) {
							if (partFile == null && lines[i].startsWith("File")) {
								part = lines[i].substring(6).split(" -> ");
								
								partFile = part[0].substring(1, part[0].length()-1);
								
								if (part.length > 1) {
									partLink = part[1].substring(1, part[1].length()-1);
								}
								
							} else if (partSize == null && lines[i].startsWith("Size")) {
								partSize = Long.parseLong(lines[i].substring(6)) * 1024L;
								
							} else if (partBlocks == null && lines[i].startsWith("Blocks")) {
								partBlocks = Integer.parseInt(lines[i].substring(8));
								
							} else if (partIOBlock == null && lines[i].startsWith("IO")) {
								partIOBlock = Integer.parseInt(lines[i].substring(10, lines[i].indexOf(" ", 10)));
								
							} else if (partINode == null && lines[i].startsWith("Inode")) {
								partINode = Integer.parseInt(lines[i].substring(7));
								
							} else if (partPerm == null && lines[i].startsWith("Access")) {
								part = lines[i].substring(9, lines[i].length()-1).split("/");
								
								partMod = part[0].trim();
								partPerm = part[1].trim();
								partType = partPerm.substring(0, 1);
								
							} else if (partUid == null && lines[i].startsWith("Uid")) {
								part = lines[i].substring(6, lines[i].length()-1).split("/");
								
								partUid = part[0].trim();
								partUname = part[1].trim();
								
							} else if (partGid == null && lines[i].startsWith("Gid")) {
								part = lines[i].substring(6, lines[i].length()-1).split("/");
								
								partGid = part[0].trim();
								partGname = part[1].trim();
								
							} else if (partAtime == null && lines[i].startsWith("Access")) {
								partAtime = lines[i].substring(8);
								
							} else if (partMtime == null && lines[i].startsWith("Modify")) {
								partMtime = lines[i].substring(8);
								
							} else if (partCtime == null && lines[i].startsWith("Change")) {
								partCtime = lines[i].substring(8);
							}
						}
						
						RootFW.log(TAG + ".FileStat", "Adding FileStat(Name=" + partFile + ", Type=" + partType + ", UID=" + partUid + ", GID=" + partGid + ", UserName=" + partUname + ", GroupName=" + partGname + ", Mod=" + partMod + ", Permissions=" + partPerm + ", Link=" + partLink + ", Size=" + partSize.toString() + ", ATime=" + partAtime + ", MTime=" + partMtime + ", CTime=" + partCtime + ", Blocks=" + partBlocks + ", IOBlock=" + partIOBlock + ", Inode=" + partINode + ")");
						
						list.add( new FileStat(partFile, partType, partUid, partGid, partUname, partGname, partMod, partPerm, partLink, partSize, partAtime, partMtime, partCtime, partBlocks, partIOBlock, partINode) );
					}
				}
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
	
	public FileStat getFileStat(String argPath) {
		RootFW.log(TAG + ".getFileStat", "Getting file stat on " + argPath);
		
		ArrayList<FileStat> stat = fileStatBuilder( new String[] {argPath} );
		
		if (stat != null) {
			return stat.get(0);
		}
		
		RootFW.log(TAG + ".getFileStat", "Could not get file stat on " + argPath, RootFW.LOG_WARNING);
		
		return null;
	}
	
	public ArrayList<FileStat> getFileStatList(String argPath) {
		RootFW.log(TAG + ".getFileStatList", "Getting file stat list on " + argPath);
		
		String[] files = getFileList(argPath);
		String path = argPath.endsWith("/") ? argPath.substring(0, argPath.length()-1) : argPath;
		
		if (files != null) {
			for (int i=0; i < files.length; i++) {
				files[i] = path + "/" + files[i];
			}
		
			return fileStatBuilder(files);
		}
		
		RootFW.log(TAG + ".getFileStatList", "Could not get file stat list on " + argPath, RootFW.LOG_WARNING);
		
		return null;
	}
}
