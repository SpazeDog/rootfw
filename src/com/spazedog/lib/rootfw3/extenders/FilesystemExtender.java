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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.extenders.FileExtender.FileData;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class FilesystemExtender {
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	private final static Pattern oPatternSeparatorSearch = Pattern.compile(",");
	private final static Pattern oPatternPrefixSearch = Pattern.compile("^.*[A-Za-z]$");
	
	/**
	 * This class is used to collect different file system related information. This information are things like defined file systems, 
	 * currently mounted file systems etc.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#filesystem()} to retrieve an instance.
	 */
	public static class Filesystem implements ExtenderGroup {
		protected static MountStat[] mFstabList;
		
		protected static Object oLock = new Object();
		
		protected ShellExtender.Shell mShell;
		protected RootFW mParent;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Filesystem(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Filesystem(RootFW parent) {
			mParent = parent;
			mShell = parent.shell();
		}
		
		/**
		 * This will return a list of all currently mounted file systems, with information like 
		 * device path, mount location, file system type and mount options.
		 *     
		 * @return
		 *     An array of {@link MountStat} objects
		 */
		public MountStat[] getMountList() {
			FileData data = mParent.file("/proc/mounts").read();
			
			if (data != null) {
				String[] lines = data.trim().getArray();
				MountStat[] list = new MountStat[ lines.length ];
				
				for (int i=0; i < lines.length; i++) {
					try {
						String[] parts = oPatternSpaceSearch.split(lines[i].trim());
						
						list[i] = new MountStat();
						list[i].mDevice = parts[0];
						list[i].mFstype = parts[2];
						list[i].mLocation = parts[1];
						list[i].mOptions = oPatternSeparatorSearch.split(parts[3]);
						
					} catch(Throwable e) {}
				}
				
				return list;
			}
			
			return null;
		}
		
		/**
		 * This will provide the same information as {@link FilesystemExtender#getMountList()}
		 * only this will not provide information about currently mounted file systems. Instead, 
		 * it will return a list of all the file systems defined in every fstab and init.rc file in the ramdisk.
		 * <br />
		 * It can be useful in situations where a file system might have been moved by a script, and you need the original defined location. 
		 * Or perhaps you need the original device of a specific mount location.
		 *     
		 * @return
		 *     An array of {@link MountStat} objects
		 */
		public MountStat[] getFstabList() {
			synchronized(oLock) {
				if (mFstabList == null) {
					ShellResult result = mShell.run("for DIR in /fstab.* /fstab /init.*.rc /init.rc; do echo $DIR; done");
					
					if (result.wasSuccessful()) {
						Set<String> cache = new HashSet<String>();
						List<MountStat> list = new ArrayList<MountStat>();
						String[] dirs = result.trim().getArray();
						
						for (int i=0; i < dirs.length; i++) {
							Boolean isFstab = dirs[i].contains("fstab");
							FileData data = mParent.file(dirs[i]).readMatches( isFstab ? "/dev/" : "mount " );
							
							if (data != null) {
								String[] lines = data.assort("#").getArray();
								
								for (int x=0; x < lines.length; x++) {
									try {
										String[] parts = oPatternSpaceSearch.split(lines[x].trim(), 5);
										String options = isFstab || parts.length > 4 ? parts[ isFstab ? 3 : 4 ].replaceAll(",", " ") : "";
										
										if (parts.length > 3 && !cache.contains(parts[ isFstab ? 1 : 3 ])) {
											if (!isFstab && parts[2].contains("mtd@")) {
												FileData mtd = mParent.file("/proc/mtd").readMatches("\\\"" + parts[2].substring(4) + "\\\"");
												
												if (mtd != null && mtd.size() > 0) {
													parts[2] = "/dev/block/mtdblock" + mtd.getLine().substring(3, mtd.getLine().indexOf(":"));
												}
												
											} else if (!isFstab && parts[2].contains("loop@")) {
												parts[2] = parts[2].substring(5);
												options += " loop";
											}
											
											MountStat stat = new MountStat();
											
											stat.mDevice = parts[ isFstab ? 0 : 2 ];
											stat.mFstype = parts[ isFstab ? 2 : 1 ];
											stat.mLocation = parts[ isFstab ? 1 : 3 ];
											stat.mOptions = oPatternSpaceSearch.split(options);
											
											list.add(stat);
											cache.add(parts[ isFstab ? 1 : 3 ]);
										}
										
									} catch(Throwable e) {}
								}
							}
						}
						
						mFstabList = list.toArray( new MountStat[ list.size() ] );
					}
				}
				
				return mFstabList;
			}
		}
		
		/**
		 * Check whether a file system type is supported by the kernel.
		 */
		public Boolean hasTypeSupport(String fstype) {
			FileData data = mParent.file("/proc/filesystems").readMatches(fstype);
			
			if (data != null && data.size() > 0) {
				String[] lines = data.getArray();
				
				for (int i=0; i < lines.length; i++) {
					try {
						String[] parts = oPatternSpaceSearch.split(lines[i].trim());
						
						if (parts[ parts.length-1 ].equals(fstype)) {
							return true;
						}
						
					} catch (Throwable e) {}
				}
			}
			
			return false;
		}
	}
		
	/**
	 * This class is used to handle devices and mount points. It can be used to get data about a device like size, mount point, file system type etc. 
	 * It can also be used to move a device mount point, remount a device, unmount, mount etc.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#filesystem(String)} to retrieve an instance.
	 */
	public static class Device implements ExtenderGroup {
		protected ShellExtender.Shell mShell;
		protected RootFW mParent;
		
		protected FileExtender.File mDevice;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Device(parent, (java.io.File) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Device(RootFW parent, java.io.File device) {
			mShell = parent.shell();
			mDevice = parent.file(device.getAbsolutePath());
			mParent = parent;
		}
		
		/**
		 * This is a short method for adding additional options to a mount location or device. 
		 * For an example, parsing remount instructions. 
		 * 
		 * @see #addMount(String, String, String[])
		 * 
		 * @param options
		 *     A string array containing all of the mount options to parse
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean addMount(String[] options) {
			return addMount(null, null, options);
		}
		
		/**
		 * This is a short method for attaching a device or folder to a location, without any options or file system type specifics. 
		 * 
		 * @see #addMount(String, String, String[])
		 * 
		 * @param location
		 *     The location where the device or folder should be attached to
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean addMount(String location) {
			return addMount(location, null, null);
		}
		
		/**
		 * This is a short method for attaching a device or folder to a location, without any file system type specifics. 
		 * 
		 * @see #addMount(String, String, String[])
		 * 
		 * @param location
		 *     The location where the device or folder should be attached to
		 *     
		 * @param options
		 *     A string array containing all of the mount options to parse
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean addMount(String location, String[] options) {
			return addMount(location, null, options);
		}
		
		/**
		 * This is a short method for attaching a device or folder to a location, without any options. 
		 * 
		 * @see #addMount(String, String, String[])
		 * 
		 * @param location
		 *     The location where the device or folder should be attached to
		 *     
		 * @param type
		 *     The file system type to mount a device as
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean addMount(String location, String type) {
			return addMount(location, type, null);
		}
		
		/**
		 * This is used for attaching a device or folder to a location, 
		 * or to change any mount options on a current mounted file system. 
		 * <br />
		 * Note that if the device parsed to the constructor {@link #Device(ShellExtender, String)} or via {@link RootFW#filesystem(String)} 
		 * is a folder, this method will use the <code>--bind</code> option to attach it to the location. Also note that when attaching folders to a location, 
		 * the <code>type</code> and <code>options</code> arguments will not be used and should just be parsed as <code>NULL</code>.
		 * 
		 * @param location
		 *     The location where the device or folder should be attached to
		 *     
		 * @param type
		 *     The file system type to mount a device as
		 *     
		 * @param options
		 *     A string array containing all of the mount options to parse
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean addMount(String location, String type, String[] options) {
			String cmd = location != null && mDevice.isDirectory() ? 
					"%binary mount --bind '" + mDevice + "' '" + location + "'" : 
						"%binary mount" + (type != null ? " -t '" + type + "'" : "") + (options != null ? " -o '" + TextUtils.join(",", Arrays.asList(options)) + "'" : "") + " '" + mDevice.getAbsolutePath() + "'" + (location != null ? " '" + location + "'" : "");
			
			ShellResult result = mShell.buildAttempts(cmd).run();
			
			return result.wasSuccessful();
		}
		
		/**
		 * This method is used to remove an attachment of a device or folder (unmount). 
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean removeMount() {
			ShellResult result = mShell.buildAttempts("%binary umount '" + mDevice.getAbsolutePath() + "'", "%binary -f umount '" + mDevice.getAbsolutePath() + "'").run();
			
			return result.wasSuccessful();
		}
		
		/**
		 * This method is used to move a mount location to another location.
		 *     
		 * @return
		 *     <code>True</code> on success, <code>False</code> otherwise
		 */
		public Boolean moveMount(String destination) {
			ShellResult result = mShell.buildAttempts("%binary mount --move '" + mDevice.getAbsolutePath() + "' '" + destination + "'").run();
			
			if (!result.wasSuccessful()) {
				MountStat stat = statMount();
				
				if (stat != null && removeMount()) {
					return mParent.filesystem(stat.device()).addMount(stat.location(), stat.fstype(), stat.options());
				}
			}
			
			return result.wasSuccessful();
		}
		
		/**
		 * This is the same as {@link FilesystemExtender#getMountList()}, 
		 * only this method will just return the mount information for this specific device or mount location. 
		 *     
		 * @return
		 *     A single {@link MountStat} object
		 */
		public MountStat statMount() {
			MountStat[] list = mParent.filesystem().getMountList();
			
			if (list != null) {
				if (!mDevice.isDirectory()) {
					for (int i=0; i < list.length; i++) {
						if (list[i].device().equals(mDevice.getAbsolutePath())) {
							return list[i];
						}
					}
					
				} else {
					String path = !mDevice.getAbsolutePath().equals("/") && mDevice.getAbsolutePath().endsWith("/") ? mDevice.getAbsolutePath().substring(0, mDevice.getAbsolutePath().length()-1) : mDevice.getAbsolutePath();
					
					do {
						for (int i=0; i < list.length; i++) {
							if (list[i].location().equals(path)) {
								return list[i];
							}
						}
						
					} while (path.lastIndexOf("/") > 0 && !(path = path.substring(0, path.lastIndexOf("/"))).equals(""));
				}
			}
			
			return null;
		}
		
		/**
		 * This is the same as {@link FilesystemExtender#getFstabList()}, 
		 * only this method will just return the mount information for this specific device or mount location. 
		 *     
		 * @return
		 *     A single {@link MountStat} object
		 */
		public MountStat statFstab() {
			MountStat[] list = mParent.filesystem().getFstabList();
			
			if (list != null) {
				if (!mDevice.isDirectory()) {
					for (int i=0; i < list.length; i++) {
						if (list[i].device().equals(mDevice.getAbsolutePath())) {
							return list[i];
						}
					}
					
				} else {
					String path = !mDevice.getAbsolutePath().equals("/") && mDevice.getAbsolutePath().endsWith("/") ? mDevice.getAbsolutePath().substring(0, mDevice.getAbsolutePath().length()-1) : mDevice.getAbsolutePath();
					
					do {
						for (int i=0; i < list.length; i++) {
							if (list[i].location().equals(path)) {
								return list[i];
							}
						}
						
					} while (path.lastIndexOf("/") > 0 && !(path = path.substring(0, path.lastIndexOf("/"))).equals(""));
				}
			}
			
			return null;
		}
		
		/**
		 * Like with {@link #statMount()}, this will also return information like device path and mount location. 
		 * However, it will not return information like file system type or mount options, but instead 
		 * information about the disk size, remaining bytes, used bytes and usage percentage. 
		 *     
		 * @return
		 *     A single {@link DiskStat} object
		 */
		public DiskStat statDisk() {
			ShellResult result = mShell.buildAttempts("%binary df -k '" + mDevice.getAbsolutePath() + "'", "%binary df '" + mDevice.getAbsolutePath() + "'").run();
			
			if (result.wasSuccessful() && result.size() > 1) {
				/* Depending on how long the line is, the df command some times breaks a line into two */
				String[] parts = oPatternSpaceSearch.split(result.sort(1).trim().getString(" ").trim());
				
				String pDevice=null, pLocation=null, prefix, prefixList[] = {"k", "m", "g", "t"};
				Integer pPercentage=null;
				Long pUsage, pSize, pRemaining;
				Double[] pUsageSections = new Double[3];
				
				if (parts.length > 5) {
					/* Busybox output */
					
					pDevice = parts[0];
					pLocation = parts[5];
					pPercentage = Integer.parseInt(parts[4].substring(0, parts[4].length()-1));
					
				} else {
					/* Toolbox output */
					
					/* Depending on Toolbox version, index 0 can be both the device or the mount location */
					MountStat stat = statMount();
					
					if (stat != null) {
						pDevice = stat.device();
						pLocation = stat.location();
					}
				}
				
				/* Make sure that the sizes of usage, capacity etc does not have a prefix. Not all toolbox and busybox versions supports the '-k' argument, 
				 * and some does not produce an error when parsed */
				for (int i=1; i < 4; i++) {
					if (oPatternPrefixSearch.matcher(parts[i]).matches()) {
						pUsageSections[i-1] = Double.parseDouble( parts[i].substring(0, parts[i].length()-1) );
						prefix = parts[i].substring(parts[i].length()-1).toLowerCase(Locale.US);
						
						for (int x=0; x < prefixList.length; x++) {
							pUsageSections[i-1] = pUsageSections[i-1] * 1024D;
							
							if (prefixList[x].equals(prefix)) {
								break;
							}
						}
						
					} else {
						pUsageSections[i-1] = Double.parseDouble(parts[i]) * 1024D;
					}
				}
				
				pSize = pUsageSections[0].longValue();
				pUsage = pUsageSections[1].longValue();
				pRemaining = pUsageSections[2].longValue();
				
				if (pPercentage == null) {
					pPercentage = ((Long) ((pUsage * 100L) / pSize)).intValue();
				}
				
				DiskStat info = new DiskStat();
				info.mDevice = pDevice;
				info.mLocation = pLocation;
				info.mSize = pSize;
				info.mUsage = pUsage;
				info.mAvailable = pRemaining;
				info.mPercentage = pPercentage;

				return info;
			}
			
			return null;
		}
		
		/**
		 * This is used to check whether the current device or folder is attached to a location (Mounted).
		 *     
		 * @return
		 *     <code>True</code> if mounted, <code>False</code> otherwise
		 */
		public Boolean isMounted() {
			return statMount() != null;
		}
		
		/**
		 * This is used to check if a mounted file system was mounted with a specific mount option. 
		 * Note that options like <code>mode=xxxx</code> can also be checked by just parsing <code>mode</code> as the argument.
		 *     
		 * @param option
		 *     The name of the option to find
		 *     
		 * @return
		 *     <code>True</code> if the options was used to attach the device, <code>False</code> otherwise
		 */
		public Boolean hasOption(String option) {
			MountStat stat = statMount();
			
			if (stat != null) {
				String[] options = stat.options();
				
				if (options != null && options.length > 0) {
					for (int i=0; i < options.length; i++) {
						if (options[i].equals(option) || options[i].startsWith(option + "=")) {
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
		/**
		 * This can be used to get the value of a specific mount option that was used to attach the file system. 
		 * Note that options like <code>noexec</code>, <code>nosuid</code> and <code>nodev</code> does not have any values and will return <code>NULL</code>. 
		 * This method is used to get values from options like <code>gid=xxxx</code>, <code>mode=xxxx</code> and <code>size=xxxx</code> where <code>xxxx</code> is the value. 
		 *     
		 * @param option
		 *     The name of the option to find
		 *     
		 * @return
		 *     <code>True</code> if the options was used to attach the device, <code>False</code> otherwise
		 */
		public String getOption(String option) {
			MountStat stat = statMount();
			
			if (stat != null) {
				String[] options = stat.options();
				
				if (options != null && options.length > 0) {
					for (int i=0; i < options.length; i++) {
						if (options[i].startsWith(option + "=")) {
							return options[i].substring( options[i].indexOf("=")+1 );
						}
					}
				}
			}
			
			return null;
		}
		
		/**
		 * @see #fsType(Boolean)
		 */
		public String fsType() {
			return fsType(false);
		}
		
		/**
		 * This can be used to get the real file system type of a device. 
		 * Although methods like {@link #statMount()} can also return the file system type, it can only return the name of the file system driver being used. 
		 * In most cases this will be enough, as a specific driver often is used on a specific type. However, file system drivers like EXT4 can also be, and is often, 
		 * used on EXT2 and EXT3 file systems. 
		 * <br />
		 * Note however that this method requires a busybox version that supports <code>blkid</code> with type information. As a fail-safe, the method will 
		 * get the type from the mount information if a supported <code>blkid</code> is not available. In this case however, it will only work on a mounted file systems.
		 *     
		 * @param realType
		 *     If <code>True</code>, it will not use the mount information when a supported <code>blkid</code> is not available
		 *     
		 * @return
		 *     The device file system type
		 */
		public String fsType(Boolean realType) {
			if (!mDevice.isDirectory()) {
				/* Note that some older busybox binaries does not provide accurate information. Some could report ext4 as ext3 or ext3 as ext2. Also some does not provide the type at all. */
				ShellResult result = mShell.buildAttempts("%binary blkid '" + mDevice.getAbsolutePath() + "'").run();
				
				if (result.wasSuccessful()) {
					String line = result.getLine();
					
					if (line.contains("TYPE")) {
						String parts[] = oPatternSpaceSearch.split(line.trim());
						Integer index = parts.length-1;
						
						return parts[index].substring(6, parts[index].length()-1);
					}
				}
			}
			
			/* If the first attempt failed, we try to get this from the mount option. 
			 * This of cause, requires the device to be mounted. */
			if (!realType) {
				MountStat stat = statMount();
				
				if (stat != null) {
					return stat.fstype();
				}
			}
			
			return null;
		}
	}
	
	/**
	 * This is a container used to store disk information. 
	 * It is used by the {@link FilesystemExtender.Device#statDisk()} method.
	 */
	public static class DiskStat {
		private String mDevice;
		private String mLocation;
		private Long mSize;
		private Long mUsage;
		private Long mAvailable;
		private Integer mPercentage;
		
		/** 
		 * @return
		 *     Device path
		 */
		public String device() {
			return mDevice;
		}
		
		/** 
		 * @return
		 *     Mount location
		 */
		public String location() {
			return mLocation;
		}
		
		/** 
		 * @return
		 *     Disk size in bytes
		 */
		public Long size() {
			return mSize;
		}
		
		/** 
		 * @return
		 *     Disk usage size in bytes
		 */
		public Long usage() {
			return mUsage;
		}
		
		/** 
		 * @return
		 *     Disk available size in bytes
		 */
		public Long available() {
			return mAvailable;
		}
		
		/** 
		 * @return
		 *     Disk usage percentage
		 */
		public Integer percentage() {
			return mPercentage;
		}
	}
	
	/**
	 * This is a container used to store mount information. 
	 * It is used by the {@link FilesystemExtender#getFstabList()}, {@link FilesystemExtender#getMountList()}, {@link FilesystemExtender.Device#statFstab()} and {@link FilesystemExtender.Device#statMount()} methods.
	 */
	public static class MountStat {
		private String mDevice;
		private String mLocation;
		private String mFstype;
		private String[] mOptions;
		
		/** 
		 * @return
		 *     The device path
		 */
		public String device() {
			return mDevice;
		}
		
		/** 
		 * @return
		 *     The mount location
		 */
		public String location() {
			return mLocation;
		}
		
		/** 
		 * @return
		 *     The device file system type
		 */
		public String fstype() {
			return mFstype;
		}
		
		/** 
		 * @return
		 *     The options used at mount time
		 */
		public String[] options() {
			return mOptions;
		}
	}
}
