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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.container.Data;
import com.spazedog.rootfw.container.DeviceList;
import com.spazedog.rootfw.container.DiskStat;
import com.spazedog.rootfw.container.FstabEntry;
import com.spazedog.rootfw.container.MountStat;
import com.spazedog.rootfw.container.ShellProcess;
import com.spazedog.rootfw.container.ShellResult;
import com.spazedog.rootfw.iface.Extender;

public final class Filesystem implements Extender {
	public final static String TAG = RootFW.TAG + "::Filesystem";
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	private final static Pattern oPatternSeparatorSearch = Pattern.compile(",");
	private final static Pattern oPatternPrefixSearch = Pattern.compile("^.*[A-Za-z]$");
	
	private ArrayList<FstabEntry> mFstabEntry;
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.filesystem</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Filesystem(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Execute a mount command on an already mounted location or device.
	 * <p/>
	 * Example: <code>mount("/system", new String[]{"remount", "rw"})</code>
	 * 
	 * @param aLocation
	 *     Mounted device or location
	 *     
	 * @param aOptions
	 *     Mount options
	 *    
	 * @return
	 *     <code>True</code> if mount was successful
	 */
	public Boolean mount(String aLocation, String[] aOptions) {
		return mount(null, aLocation, null, aOptions);
	}
	
	/**
	 * See <code>mount(String aDevice, String aLocation, String aFstype, String[] aOptions)</code>
	 */
	public Boolean mount(String aDevice, String aLocation) {
		return mount(aDevice, aLocation, null, null);
	}
	
	/**
	 * See <code>mount(String aDevice, String aLocation, String aFstype, String[] aOptions)</code>
	 */
	public Boolean mount(String aDevice, String aLocation, String[] aOptions) {
		return mount(aDevice, aLocation, null, aOptions);
	}
	
	/**
	 * See <code>mount(String aDevice, String aLocation, String aFstype, String[] aOptions)</code>
	 */
	public Boolean mount(String aDevice, String aLocation, String aFstype) {
		return mount(aDevice, aLocation, aFstype, null);
	}

	/**
	 * Mount a device to a location
	 * <p/>
	 * If the both the device and location are directories, then the method will invoke the "--bind" option. 
	 * In these cases, both aFstype and aOptions are ignored
	 * 
	 * @param aDevice
	 *     Device
	 *     
	 * @param aLocation
	 *     Mount location
	 *     
	 * @param aFstype
	 *     File System Type
	 *     
	 * @param aOptions
	 *     Mount options
	 *    
	 * @return
	 *     <code>True</code> if mount was successful
	 */
	public Boolean mount(String aDevice, String aLocation, String aFstype, String[] aOptions) {
		String lOptions = "", lCommand = null;
		
		if (aOptions != null) {
			for (int i=0; i < aOptions.length; i++) {
				lOptions += (i > 0 ? "," : "") + aOptions[i];
			}
		}
		
		if (aDevice == null || !mParent.file.check(aDevice, "d")) {
			lCommand = "%binary mount" + (aDevice != null ? " '" + aDevice + "'" : "") + (aFstype != null ? " -t '" + aFstype + "'" : "") + (aOptions != null ? " -o '" + lOptions + "'" : "") + " '" + aLocation + "'";
		
		} else {
			lCommand = "%binary mount --bind '" + aDevice + "' '" + aLocation + "'";
		}
			
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate(lCommand) );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Unmount a device or location
	 *     
	 * @param aLocation
	 *     Mounted device or location
	 *    
	 * @return
	 *     <code>True</code> if unmounting was successful
	 */
	public Boolean unMount(String aLocation) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary umount '" + aLocation + "'", "%binary umount -f '" + aLocation + "'"} ) );
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Move the mount location to another directory
	 *
	 * @param aLocation
	 *     Mount location
	 *     
	 * @param aDestination
	 *     New mount location
	 *    
	 * @return
	 *     <code>True</code> if moving was successful
	 */
	public Boolean moveMount(String aLocation, String aDestination) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate("%binary mount --move '" + aLocation + "' '" + aDestination + "'") );
		
		if (lResult == null || lResult.code() != 0) {
			MountStat stat = statMount(aLocation);
			
			if (stat != null && unMount(aLocation)) {
				return mount(stat.device(), stat.location(), stat.fstype(), stat.options());
			}
		}
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Get stat on a device, file or location
	 * <p/>
	 * This cannot be used to check whether a location is mounted or not. What this do, is searches for the nearest 
	 * location in /proc/mounts and return the located part. So if you have a mounted file system on /data, 
	 * and you stat /data/property/file, then you will get mount stat on /data. 
	 *
	 * @param aLocation
	 *     Device, file, location
	 *    
	 * @return
	 *     The stat on the nearest match from /proc/mounts
	 */
	public MountStat statMount(String aLocation) {
		ArrayList<MountStat> lMounts = listMounts();
		
		if (lMounts != null) {
			if (mParent.file.check(aLocation, "b")) {
				for (int i=0; i < lMounts.size(); i++) {
					if (lMounts.get(i).device().equals(aLocation)) {
						return lMounts.get(i);
					}
				}
				
			} else {
				String lPath = !aLocation.equals("/") && aLocation.endsWith("/") ? aLocation.substring(0, aLocation.length()-1) : aLocation;
				
				do {
					for (int i=0; i < lMounts.size(); i++) {
						if (lMounts.get(i).location().equals(lPath)) {
							return lMounts.get(i);
						}
					}
					
				} while (lPath.lastIndexOf("/") > 0 && !(lPath = lPath.substring(0, lPath.lastIndexOf("/"))).equals(""));
			}
		}
		
		return null;
	}
	
	/**
	 * This provides a MountStat list of all the entries in /proc/mounts
	 *    
	 * @return
	 *     A complete list of mounted file systems
	 */
	public ArrayList<MountStat> listMounts() {
		Data lOutput = mParent.file.read("/proc/mounts");
		
		if (lOutput != null && lOutput.length() > 0) {
			ArrayList<MountStat> list = new ArrayList<MountStat>();
			String[] lLine, lMounts = lOutput.raw();
			
			for (int i=0; i < lMounts.length; i++) {
				try {
					lLine = oPatternSpaceSearch.split(lMounts[i].trim());
					
					list.add( new MountStat(lLine[0], lLine[1], lLine[2], oPatternSeparatorSearch.split(lLine[3])) );
					
				} catch(Throwable e) {}
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
	
	/**
	 * Check if a device or location is mounted
	 * 
	 * @param aLocation
	 *     Device or location to check
	 *    
	 * @return
	 *     <code>True</code> if the device or location is mounted
	 */
	public Boolean checkMount(String aLocation) {
		ArrayList<MountStat> lMounts = listMounts();
		
		if (lMounts != null) {
			if (!mParent.file.check(aLocation, "d")) {
				for (int i=0; i < lMounts.size(); i++) {
					if (lMounts.get(i).device().equals(aLocation)) {
						return true;
					}
				}
				
			} else {
				String lPath = !aLocation.equals("/") && aLocation.endsWith("/") ? aLocation.substring(0, aLocation.length()-1) : aLocation;
				
				for (int i=0; i < lMounts.size(); i++) {
					if (lMounts.get(i).location().equals(lPath)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Return disk information on a device or location. 
	 * This includes things like device path, mount location, size, usage etc...
	 * 
	 * @param aDevice
	 *     A device or a location
	 *    
	 * @return
	 *     A DiskStat container with all the disk information
	 */
	public DiskStat statDisk(String aDevice) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary df -k '" + aDevice + "'", "%binary df '" + aDevice + "'"} ) );
		
		if (lResult != null && lResult.code() == 0) {
			try {
				String[] lStatSections = oPatternSpaceSearch.split(lResult.output().toString(" ", 1, true).trim());
				
				String lDevice=null, lLocation=null, lPrefix, lPrefixes[] = {"k", "m", "g", "t"};
				Integer lPercentage=null;
				Long lUsage, lSize, lRemaining;
				Double[] lUsageSections = new Double[3];
				
				if (lStatSections.length > 5) {
					lDevice = lStatSections[0];
					lLocation = lStatSections[5];
					lPercentage = Integer.parseInt(lStatSections[4].substring(0, lStatSections[4].length()-1));
					
				} else {
					/* Depending on Toolbox version, index 0 can be both the device or the mount location */
					MountStat stat = statMount(lStatSections[0]);
					
					if (stat != null) {
						lDevice = stat.device();
						lLocation = stat.location();
					}
				}
				
				for (int i=1; i < 4; i++) {
					if (oPatternPrefixSearch.matcher(lStatSections[i]).matches()) {
						lUsageSections[i-1] = Double.parseDouble( lStatSections[i].substring(0, lStatSections[i].length()-1) );
						lPrefix = lStatSections[i].substring(lStatSections[i].length()-1).toLowerCase(Locale.US);
						
						for (int x=0; x < lPrefixes.length; x++) {
							lUsageSections[i-1] = lUsageSections[i-1] * 1024D;
							
							if (lPrefixes[x].equals(lPrefix)) {
								break;
							}
						}
						
					} else {
						lUsageSections[i-1] = Double.parseDouble(lStatSections[i]) * 1024D;
					}
				}
				
				lSize = lUsageSections[0].longValue();
				lUsage = lUsageSections[1].longValue();
				lRemaining = lUsageSections[2].longValue();
				
				if (lPercentage == null) {
					lPercentage = ((Long) ((lUsage * 100L) / lSize)).intValue();
				}
				
				return new DiskStat(lDevice, lLocation, lSize, lUsage, lRemaining, lPercentage);
				
			} catch(Throwable e) { RootFW.log(TAG + ".statDisk", "Failed while grabbing disk stat", RootFW.E_ERROR, e); }
		}
		
		return null;
	}
	
	/**
	 * Return a list of all device types from /proc/devices 
	 * containing type (Block Device 'b' or Character Device 'c'), name and major number
	 *    
	 * @return
	 *     An ArrayList of DeviceList containers with all the device information
	 */
	public ArrayList<DeviceList> devices() {
		Data lOutput = mParent.file.read("/proc/devices");
		
		if (lOutput != null && lOutput.length() > 0) {
			String[] lSections, lDevices = lOutput.raw();
			String lType = null;
			ArrayList<DeviceList> list = new ArrayList<DeviceList>();
			
			for (int i=0; i < lDevices.length; i++) {
				if (lDevices[i].length() > 0) {
					if (lDevices[i].startsWith("B") || lDevices[i].startsWith("C")) {
						lType = lDevices[i].substring(0, 1).toLowerCase(Locale.US);
						
					} else {
						try {
							lSections = oPatternSpaceSearch.split(lDevices[i].trim());
							
							list.add( new DeviceList(lSections[1], Integer.parseInt(lSections[0]), lType) );
							
						} catch(Throwable e) {}
					}
				}
			}
			
			return list.size() > 0 ? list : null;
		}
		
		return null;
	}
	
	/**
	 * Check if a specific file system type is supported on the device
	 * 
	 * @param aFstype
	 *     The type of file system to check for
	 *    
	 * @return
	 *     <code>True</code> if the file system is supported
	 */
	public Boolean typeSupported(String aFstype) {
		Data lOutput = mParent.file.read("/proc/filesystems");
		
		if (lOutput != null && lOutput.length() > 0) {
			String[] lSections, lTypes = lOutput.raw();
			
			for (int i=0; i < lTypes.length; i++) {
				lSections = oPatternSpaceSearch.split(lTypes[i].trim());
				
				if (lSections[ lSections.length-1 ].equals(aFstype)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Get the file system type of a device
	 * <code>Note that some older busybox binaries does not provide accurate information. Some could report ext4 as ext3 or ext3 as ext2. Also some does not provide the type at all.</code>
	 * 
	 * @param aDevice
	 *     The device to check the file system type on
	 *    
	 * @return
	 *     The device file system type
	 */
	public String getType(String aDevice) {
		ShellResult lResult = mParent.shell.execute( ShellProcess.generate( new String[] {"%binary blkid '" + aDevice + "'"} ) );
		
		if (lResult != null && lResult.code() == 0 && lResult.output().length() > 0) {
			if (lResult.output().line().contains("TYPE")) {
				String lParts[] = oPatternSpaceSearch.split(lResult.output().line());
				Integer lIndex = lParts.length-1;
				
				return lParts[lIndex].substring(6, lParts[lIndex].length()-1);
			}
		}
		
		return null;
	}
	
	/**
	 * This is almost the same as <code>listMounts</code>.
	 * However, this does not provide information about currently mounted devices,
	 * instead it lists devices and locations defined in the available fstab and init.rc files.
	 * 
	 * It is a great way of making sure that you get a correct device for a correct location,
	 * even if a script or similar has changed it since the device was booted. 
	 *    
	 * @return
	 *     A complete list of defined file systems
	 */
	public ArrayList<FstabEntry> listFstab() {
		if (mFstabEntry == null) {
			/* The easiest way of searching for these files, and which is supported in all shell types without busybox or toolbox support */
			ShellResult lResult = mParent.shell.execute("for DIR in /fstab.* /fstab /init.*.rc /init.rc; do echo $DIR; done");
			
			if (lResult != null) {
				ArrayList<FstabEntry> mFstabEntry = new ArrayList<FstabEntry>();
				String[] lFiles = lResult.output().raw();
				Set<String> lCache = new HashSet<String>();
				
				for (int i=0; i < lFiles.length; i++) {
					Data lData = mParent.file.read(lFiles[i]);
					
					if (lData != null) {
						String[] lLines = lData.raw();
						
						for (int x=0; x < lLines.length; x++) {
							String[] lSection;
							
							if (!lLines[x].contains("#") && (lSection = oPatternSpaceSearch.split(lLines[x].trim(), 5)).length > 3) {
								if (lFiles[i].contains("fstab")) {
									if (!lCache.contains(lSection[1])) {
										mFstabEntry.add( new FstabEntry(lSection[0], lSection[1], lSection[2], oPatternSeparatorSearch.split(lSection[3])) );
									}
									
									lCache.add(lSection[1]);
									
								} else {
									if (!lCache.contains(lSection[3]) && lSection[0].equals("mount")) {
										if (lSection[2].contains("mtd@")) {
											if (mParent.file.check("/dev/mtd", "d")) {
												ShellResult lInnerResult = mParent.shell.execute( ShellProcess.generate("%binary grep -e '\\\"" + lSection[2].substring(4) + "\\\"' /proc/mtd") );
												
												if (lInnerResult != null) {
													String lLine = lInnerResult.output().line();
													mFstabEntry.add( new FstabEntry("/dev/block/mtdblock" + lLine.substring(3, lLine.indexOf(":")), lSection[3], lSection[1], lSection.length > 4 ? oPatternSeparatorSearch.split(lSection[4].replace(" ", ",")) : null) );
												}
											}
											
										} else {
											String lOptions = lSection.length > 4 ? lSection[4] : "";
											
											if (lSection[2].contains("loop@")) {
												lSection[2] = lSection[2].substring(5);
												lOptions += " loop";
											}
											
											mFstabEntry.add( new FstabEntry(lSection[2], lSection[3], lSection[1], lOptions.length() > 0 ? oPatternSeparatorSearch.split(lOptions.trim().replace(" ", ",")) : null) );
										}
										
										lCache.add(lSection[3]);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return mFstabEntry != null && mFstabEntry.size() > 0 ? mFstabEntry : null;
	}
	
	/**
	 * Return an fstab entry of a device or mount location 
	 * This includes things like device path, mount location, file system and mount options etc...
	 * 
	 * @param aDevice
	 *     A device or a location
	 *    
	 * @return
	 *     A FstabEntry container with all the fstab information
	 */
	public FstabEntry statFstab(String aDevice) {
		ArrayList<FstabEntry> lFstab = listFstab();
		
		if (lFstab != null) {
			for (int i=0; i < lFstab.size(); i++) {
				if (lFstab.get(i).device().equals(aDevice) || lFstab.get(i).location().equals(aDevice)) {
					return lFstab.get(i);
				}
			}
		}
		
		return null;
	}
}
