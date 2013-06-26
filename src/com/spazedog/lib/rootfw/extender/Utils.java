package com.spazedog.lib.rootfw.extender;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.Data;
import com.spazedog.lib.rootfw.container.DiskStat;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.iface.Extender;

public final class Utils implements Extender {
	public final static String TAG = RootFW.TAG + "::Utils";
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.utils</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Utils(RootFW aInstance) {
		mParent = aInstance;
	}

	/**
	 * Extract an Update.zip Recovery Package from Application resources
	 * and reboot into the recovery auto installation for auto flashing.
	 * The package is extracted to /data/local/tmp.
	 * <p/>
	 * This method will first make sure to locate the correct device locations. 
	 * Because many users install scripts and apps which moves mount locations, 
	 * you cannot depend on /data and /cache being the correct locations for the
	 * internal partitions. If the partitions is not mounted at all, the method will
	 * mount them manually to other locations. 
	 * 
	 * @param aContext
	 *     The Application or Activity Context
	 *    
	 * @param aPackageResourceId
	 *     Resource ID for the Recovery Installation Package
	 *    
	 * @return
	 *     <code>True</code> if successful
	 */
	public Boolean recoveryInstall(Context aContext, Integer aPackageResourceId) {
		/* The easiest way of searching for these files, and which is supported in all shell types without busybox or toolbox support */
		ShellResult lResult = mParent.shell.execute("for DIR in /fstab.* /fstab /init.*.rc /init.rc /proc/mtd; do echo $DIR; done");
		
		if (lResult != null) {
			Map<String, String> lDevices = new HashMap<String, String>();
			String[] lSection, lContent, lFiles = lResult.output().raw();
			Data lOutout;
			String lLocationData = "/data", lLocationCache = "/cache";
			
			/*
			 * There is a lot of scripts for Android that moves mount points around for different reasons.
			 * Because of this, you can never be sure that /cache and /data are attached to the internal partitions.
			 * So what we will do, is search the init.rc files, fstab, /proc/mtd until we locate the entries which points
			 * to the true device files. We can then locate the correct locations or mount them.
			 * 
			 * This is a slow process, but since this method is not one to be used multiple times during one root session,
			 * it does not really matter. The important thing is that we locate the correct /data and /cache/recovery
			 */
			MainLoop:
			for (int i=0; i < lFiles.length; i++) {
				lOutout = mParent.file.read(lFiles[i]);
				
				if (lOutout != null) {
					lContent = lOutout.raw();
					
					for (int x=0; x < lContent.length; x++) {
						if ((lDevices.get("data") == null && lContent[x].contains("data")) || (lDevices.get("cache") == null && lContent[x].contains("cache"))) {
							lSection = oPatternSpaceSearch.split(lContent[x].trim());
							
							RootFW.log(TAG + ".recoveryInstall", "File " + lFiles[i] + " (" + lContent[x].trim() + ")");
							
							if (!lSection[0].equals("#")) {
								if (lFiles[i].contains("fstab") && lSection.length > 1 && (lSection[1].equals("/data") || lSection[1].equals("/cache"))) {
									lDevices.put(lSection[1].substring(1), lSection[0]);
									
								} else if (lFiles[i].contains("init") && lSection.length > 4 && lSection[2].contains("/dev/") && (lSection[3].equals("/data") || lSection[3].equals("/cache"))) {
									lDevices.put(lSection[3].substring(1), lSection[2]);
									
								} else if (lFiles[i].equals("/proc/mtd") && lSection.length > 3 && (lSection[3].equals("\"userdata\"") || lSection[3].equals("\"cache\""))) {
									lDevices.put(lSection[3].equals("\"userdata\"") ? "data" : "cache", "/dev/block/mtdblock" + lSection[0].substring(lSection[0].length()-2, lSection[0].length()-1));
								}
							}
						}
						
						if (lDevices.get("data") != null && lDevices.get("cache") != null) {
							break MainLoop;
						}
					}
				}
			}
			
			DiskStat lStatData = mParent.filesystem.statDisk(lDevices.get("data"));
			DiskStat lStatCache = mParent.filesystem.statDisk(lDevices.get("cache"));
			
			if (lStatData != null && lStatData.location() != null) {
				lLocationData = lStatData.location();
			}
			
			if (lStatCache != null && lStatCache.location() != null) {
				lLocationCache = lStatCache.location();
				
			} else {
				mParent.file.create("/cache-int");
				
				if (mParent.filesystem.mount(lDevices.get("cache"), "/cache-int")) {
					lLocationCache = "/cache-int";
				}
			}
			
			mParent.file.create(lLocationData + "/local/tmp");
			mParent.file.create(lLocationCache + "/recovery");
			
			if(mParent.file.copyResource(aContext, aPackageResourceId, lLocationData + "/local/tmp/update.zip")) {
				if(mParent.file.write(lLocationCache + "/recovery/command", "--update_package=" + lLocationData + "/local/tmp/update.zip")) {
					return mParent.processes.recoveryReboot();
				}
			}
		}
		
		return false;
	}
}
