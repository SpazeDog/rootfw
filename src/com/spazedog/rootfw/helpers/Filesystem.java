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

import android.content.Context;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.DiskInfo;
import com.spazedog.rootfw.containers.FileData;
import com.spazedog.rootfw.containers.FileInfo;
import com.spazedog.rootfw.containers.MountInfo;
import com.spazedog.rootfw.containers.ShellCommand;
import com.spazedog.rootfw.containers.ShellResult;

public final class Filesystem {
	private final static String TAG = "RootFW:Filesystem";
	
	private RootFW ROOTFW;
	
	public Filesystem(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	private ArrayList<FileInfo> fileInfoBuilder(String argPath, String argItem) {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"ls -lna " + argPath, "ls -la " + argPath, "ls -ln " + argPath, "ls -l " + argPath} ));
		
		if (result != null && result.getResultCode() == 0) {
			String[] resultLines = result.getResult().getData(), lineParts;
			String partUser=null, partGroup=null, partPermission=null, partLink=null, partType=null, partName=null, partMod=null;
			Integer y, x, i, z;
			
			for (i=0; i < resultLines.length; i++) {
				lineParts = RootFW.replaceAll(resultLines[i], "  ", " ").trim().split(" ");
				
				if (lineParts.length > 4) {
					y = lineParts[ lineParts.length-2 ].equals("->") ? 3 : 1;
					
					if (argItem == null || lineParts[ lineParts.length-y ].equals(argItem)) {
						x = lineParts[4].matches("^[0-9]+$") ? 0 : 1;
						
						partPermission = lineParts[0];
						partUser = lineParts[ 2-x ];
						partGroup = lineParts[ 3-x ];
						partType = partPermission.substring(0, 1);
						partLink = partType.equals("l") ? lineParts[ lineParts.length-1 ] : null;
						partName = partType.equals("l") ? lineParts[ lineParts.length-3 ] : lineParts[ lineParts.length-1 ];
						
						String[] permsPart = {partPermission.substring(1), partPermission.substring(1, 4), partPermission.substring(4, 7), partPermission.substring(7, 10)};
						partMod="";
						for (x=0; x < permsPart.length; x++) {
							z = (x == 0 && permsPart[x].charAt(2) == 's') || (x > 0 && permsPart[x].charAt(0) == 'r') ? 4 : 0;
							z += (x == 0 && permsPart[x].charAt(5) == 's') || (x > 0 && permsPart[x].charAt(1) == 'w') ? 2 : 0;
							z += (x == 0 && permsPart[x].charAt(8) == 't') || (x > 0 && permsPart[x].charAt(2) == 'x') ? 1 : 0;
							
							partMod += z;
						}
					}
					
					list.add( new FileInfo(partName, partType, partUser, partGroup, partMod, partPermission, partLink) );
				}
			}
			
			return list;
		}
		
		return null;
	}
	
	public FileInfo getFileInfo(String argPath) {
		if (!"".equals(argPath)) {
			String path, dir=null, item=null;
			
			if (!argPath.equals("/")) {
				path = argPath.endsWith("/") ? argPath.substring(0, argPath.length() - 1) : argPath;
				dir = path.substring(0, path.lastIndexOf("/"));
				item = path.substring(path.lastIndexOf("/") + 1);
				
			} else {
				dir = "/";
				item = ".";
			}
			
			ArrayList<FileInfo> list = fileInfoBuilder(dir, item);
			
			if (list != null) {
				return list.get(0);
			}
		}
		
		return null;
	}
	
	public ArrayList<FileInfo> getFileList(String argPath) {
		if (!"".equals(argPath)) {
			return fileInfoBuilder(argPath, null);
		}
		
		return null;
	}
	
	public Boolean exist(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -e " + argPath + " ] && echo true || echo false", "[ -e " + argPath + " ] && echo true || echo false");

		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = getFileInfo(argPath) != null ? true : false;
		}
		
		RootFW.log(TAG, "exist(): Checking if " + argPath + " exists which it " + (status ? "seams" : "does not seam") + " to do");
		
		return status;
	}
	
	public Boolean isFile(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -f " + argPath + " ] && echo true || echo false", "[ -f " + argPath + " ] && echo true || echo false");
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("-") ? true : false;
		}

		RootFW.log(TAG, "isFile(): Checking type of " + argPath + " which " + (status ? "seams" : "does not seam") + " to be a file");
		
		return status;
	}
	
	public Boolean isDir(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -d " + argPath + " ] && echo true || echo false", "[ -d " + argPath + " ] && echo true || echo false");
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("d") ? true : false;
		}

		RootFW.log(TAG, "isDir(): Checking type of " + argPath + " which " + (status ? "seams" : "does not seam") + " to be a directory");
		
		return status;
	}
	
	public Boolean isLink(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -L " + argPath + " ] && echo true || echo false", "[ -L " + argPath + " ] && echo true || echo false");
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("l") ? true : false;
		}
		
		RootFW.log(TAG, "isLink(): Checking type of " + argPath + " which " + (status ? "seams" : "does not seam") + " to be a link");
		
		return status;
	}
	
	public Boolean isBlockDevice(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -b " + argPath + " ] && echo true || echo false", "[ -b " + argPath + " ] && echo true || echo false");
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("b") ? true : false;
		}
		
		RootFW.log(TAG, "isBlockDevice(): Checking type of " + argPath + " which " + (status ? "seams" : "does not seam") + " to be a block device");
		
		return status;
	}
	
	public Boolean isCharacterDevice(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -c " + argPath + " ] && echo true || echo false", "[ -c " + argPath + " ] && echo true || echo false");
		
		FileInfo fi;
		Boolean status = false;
		
		if (result != null && result.getResultCode() == 0) {
			status = result.getResult().getLastLine().equals("true") ? true : false;
			
		} else {
			status = (fi = getFileInfo(argPath)) != null && fi.getType().equals("c") ? true : false;
		}
		
		RootFW.log(TAG, "isCharacterDevice(): Checking type of " + argPath + " which " + (status ? "seams" : "does not seam") + " to be a character device");
		
		return status;
	}
	
	public Boolean copyFileResource(Context argContext, Integer argSrc, String argDes, String argPerms, String argUser, String argGroup) {
		RootFW.log(TAG, "copyFileResource(): Trying to copy resource into " + argDes);
		
		if (!isDir(argDes)) {
			InputStream in = argContext.getResources().openRawResource( argSrc );
			FileOutputStream out = null;
			
			try {
				out = argContext.openFileOutput("tmp.raw", 0);
				
			} catch (FileNotFoundException e) { e.printStackTrace(); return false; }
			
			byte[] buff = new byte[1024];
			Integer read = 0;
			
			try {
				while ((read = in.read(buff)) > 0) {
					out.write(buff, 0, read);
				}
				
			} catch (IOException e) { e.printStackTrace(); return false; }
			
			try {
				in.close();
				out.close();
				
			} catch (IOException e) { e.printStackTrace(); }
			
			if(moveFile("/data/data/" + argContext.getPackageName() + "/files/tmp.raw", argDes)) {
				if (setPermissions(argDes, argPerms) && setOwner(argDes, argUser, argGroup)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Boolean copyFile(String argSrc, String argDes) {
		FileInfo fi = getFileInfo(argSrc);
		
		if (fi != null) {
			return copyFile(argSrc, argDes, fi.getPermissions(), fi.getUser(), fi.getGroup());
		}
		
		return false;
	}
	
	public Boolean copyFile(String argSrc, String argDes, String argPerms, String argUser, String argGroup) {
		try {
			if (isFile(argSrc)) {
				String dest = !exist(argDes) || !isDir(argDes) ? argDes : 
					(argDes.endsWith("/") ? argDes.substring(0, argDes.length() - 1) : argDes) + "/" + argSrc.substring(argSrc.lastIndexOf("/") + 1);
				
				Boolean status = false;
				ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"cp " + argSrc + " " + dest + "", "cat " + argSrc + " > " + dest + ""} ));
				
				if (result != null && result.getResultCode() == 0) {
					if (setPermissions(dest, argPerms) && setOwner(dest, argUser, argGroup)) {
						status = true;
					}
				}
				
				return status;
			}
			
		} catch(Throwable e) { e.printStackTrace(); }
		
		return false;
	}
	
	public Boolean moveFile(String argSrc, String argDes) {
		try {
			if (isFile(argSrc)) {
				String dest = !exist(argDes) || !isDir(argDes) ? argDes : 
					(argDes.endsWith("/") ? argDes.substring(0, argDes.length() - 1) : argDes) + "/" + argSrc.substring(argSrc.lastIndexOf("/") + 1);
				
				Boolean status = false;
				ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles( new String[] {"mv " + argSrc + " " + dest + "", "cat " + argSrc + " > " + dest + ""} ));
				
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
				
				RootFW.log(TAG, "moveFile(): " + (status ? "moved " + argSrc + " into " + argDes : "could not move " + argSrc + " into " + argDes), status ? RootFW.LOG_INFO : RootFW.LOG_WARNING);
				
				return status;
			}
			
		} catch(Throwable e) { e.printStackTrace(); }
		
		return false;
	}
	
	public FileData readFile(String argFile) {
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("cat " + argFile));
			
			if (result != null && result.getResultCode() == 0) {
				return result.getResult();
			}
		}
		
		return null;
	}
	
	public String readFileLine(String argFile) {
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"sed -n '1p' " + argFile + "", "cat " + argFile + ""}));
			
			if (result != null && result.getResultCode() == 0) {
				return result.getResult().getFirstLine();
			}
		}
		
		return null;
	}
	
	public Boolean putFileLine(String argFile, String argLineData) {
		return putFileLine(argFile, argLineData, false);
	}
	
	public Boolean putFileLine(String argFile, String argLineData, Boolean argAppend) {
		if (!isDir(argFile)) {
			String path = argFile.substring(0, argFile.lastIndexOf("/"));

			if (mkDir(path)) {
				ShellResult result; 
				
				if (argAppend) {
					result = ROOTFW.runShell(ShellCommand.makeCompatibles("echo " + argLineData + " >> " + argFile));
				
				} else {
					result = ROOTFW.runShell(ShellCommand.makeCompatibles("echo " + argLineData + " > " + argFile));
				}
				
				if (result != null && result.getResultCode() == 0) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Boolean setPermissions(String argPath, String argPerms) {
		return setPermissions(argPath, argPerms, false);
	}
	
	public Boolean setPermissions(String argPath, String argPerms, Boolean argRecursive) {
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) { 
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("chmod -R " + argPerms + " " + argPath));
				
			} else {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("chmod " + argPerms + " " + argPath));
			}
			
			RootFW.log(TAG, "setPermissions(): " + (result != null && result.getResultCode() == 0 ? "changed permission on " + argPath + " to '" + argPerms + "'" : "could not set permission on " + argPath + " to '" + argPerms + "'"), result.getResultCode() == 0 ? RootFW.LOG_INFO : RootFW.LOG_WARNING);
			
			return result != null && result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup) {
		return setOwner(argPath, argUser, argGroup, false);
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup, Boolean argRecursive) {
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("chown -R " + (argUser + "." + argGroup) + " " + argPath));
			
			} else {
				result = ROOTFW.runShell(ShellCommand.makeCompatibles("chown " + (argUser + "." + argGroup) + " " + argPath));
			}
			
			RootFW.log(TAG, "setPermissions(): " + (result != null && result.getResultCode() == 0 ? "changed owner on " + argPath + " to '" + argUser + "." + argGroup + "'" : "could not set owner on " + argPath + " to '" + argUser + "." + argGroup + "'"), result.getResultCode() == 0 ? RootFW.LOG_INFO : RootFW.LOG_WARNING);
			
			return result != null && result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean rmFile(String argPath) {
		if (argPath.length() > 0 && exist(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"unlink " + argPath + "", "rm -rf " + argPath + ""}));
			
			return result != null && result.getResultCode() == 0 ? true : false;
		}
		
		return true;
	}
	
	public Boolean rmDir(String argPath) {
		if (argPath.length() > 0 && isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"rmdir " + argPath + "", "rm -rf " + argPath + ""}));
			
			return result != null && result.getResultCode() == 0 ? true : false;
		}
		
		return true;
	}
	
	public Boolean mkDir(String argPath) {
		if (argPath.length() > 0 && !isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("mkdir -p " + argPath));
			
			if (result != null && result.getResultCode() != 0) {
				String path = argPath.endsWith("/") ? argPath.substring(0, -1) : argPath;
				path = path.startsWith("/") ? path.substring(1) : path;
				
				String[] paths = path.split("/");
				path = "/";
				for (int i=0; i < paths.length; i++) {
					path += paths[i] + "/";
					
					if (!isDir(path)) {
						result = ROOTFW.runShell(ShellCommand.makeCompatibles("mkdir " + argPath));
	
						if (result.getResultCode() != 0) {
							break;
						}
					}
				}
			}
			
			return result != null && result.getResultCode() == 0 ? true : false;
			
		} else {
			return true;
		}
	}
	
	public Boolean emptyDir(String argPath) {
		if (isDir(argPath)) {
			String dir = argPath.endsWith("/") ? argPath.substring(0, argPath.length() - 1) : argPath;
			
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles("rm -rf " + dir + "/*"));
			
			return result != null && result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean remount(String argMountPoint, String argOptions) {
		return mount(argMountPoint, "remount," + argOptions);
	}
	
	public Boolean unmount(String argDevice) {
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"umount " + argDevice + "", "umount -f " + argDevice + ""}));
		
		return result != null && result.getResultCode() == 0 ? true : false;
	}
	
	public Boolean mount(String argMountPoint, String argOptions) {
		return mount(null, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argOptions) {
		return mount(argDevice, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argFileSystem, String argOptions) {
		String mount = "mount" + (argDevice != null ? " " + argDevice : "") + (argFileSystem != null ? " -t " + argFileSystem : "") + (argOptions != null ? " -o " + argOptions : "") + " " + argMountPoint;
		
		ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(mount));
		
		return result != null && result.getResultCode() == 0 ? true : false;
	}
	
	public ArrayList<MountInfo> getMounts() {
		FileData filedata = readFile("/proc/mounts");
		
		if (filedata != null) {
			String[] mounts = filedata.getData(), line;
			
			ArrayList<MountInfo> list = new ArrayList<MountInfo>();
			
			if (mounts != null) {
				for (int i=0; i < mounts.length; i++) {
					line = RootFW.replaceAll(mounts[i], "  ", " ").trim().split(" ");
					
					list.add(new MountInfo(line[0], line[1], line[2], line[3].split(",")));
				}
				
				return list;
			}
		}
		
		return null;
	}
	
	public MountInfo getDiskMount(String argDevice) {
		Boolean blockdevice;
		
		if ((blockdevice = isBlockDevice(argDevice)) || isDir(argDevice)) {
			ArrayList<MountInfo> mounts = getMounts();
			
			if (mounts != null) {
				String path = null;
				
				if (!blockdevice) {
					path = argDevice.endsWith("/") ? argDevice.substring(0, argDevice.length()-1) : argDevice;
				}
				
				for (int i=0; i < mounts.size(); i++) {
					if (blockdevice) {
						if (mounts.get(i).getDevice().equals(argDevice)) {
							return mounts.get(i);
						}
						
					} else {
						do {
							if (mounts.get(i).getMountPoint().equals(path)) {
								return mounts.get(i);
							}
							
						} while (path.lastIndexOf("/") > 0 && !(path = path.substring(0, path.lastIndexOf("/"))).equals(""));
					}
				}
			}
		}
		
		return null;
	}
	
	public Boolean hasMountFlag(String argDevice, String argOption) {
		MountInfo mi = getDiskMount(argDevice);
		
		if (mi != null) {
			String[] mo = mi.getFlags();
			
			for (int i=0; i < mo.length; i++) {
				if (mo[i].equals(argOption)) {
					return true;
				}
			}
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
		Boolean blockdevice;
		
		if ((blockdevice = isBlockDevice(argPath)) || isDir(argPath)) {
			MountInfo mountinfo = null;
			
			if (blockdevice) {
				mountinfo = getDiskMount(argPath);
				
				if (mountinfo != null) {
					argPath = mountinfo.getMountPoint();
				}
			}
			
			ShellResult result = ROOTFW.runShell(ShellCommand.makeCompatibles(new String[] {"df -k " + argPath + "", "df " + argPath + ""}));

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
							
							RootFW.log(TAG, "getDiskInfo(): Removing prefix '" + prefix + "' and calculating '" + parts[i] + "' into bytes");
							
							if (!prefix.equals("b") && tmpSize > 0) {
								for (int x=0; x < prefixes.length; x++) {
									RootFW.log(TAG, "getDiskInfo(): Converting " + tmpSize.toString() + prefixes[x]);
									
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
				RootFW.log(TAG, "getDiskInfo(): Found disk info string '" + repacked + "'");
				
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
				
				RootFW.log(TAG, "getDiskInfo(): Found disk info Device(" + device + "), MountPoint(" + mountpoint + "), Size(" + size.intValue() + "), Usage(" + usage.intValue() + "), Remaining(" + remaining.intValue() + "), Percentage(" + percentage + ")");
				
				return new DiskInfo(device, size, usage, remaining, percentage, mountpoint);
			}
		}
		
		return null;
	}
}
