package com.spazedog.lib.rootfw3.extenders;

import java.util.regex.Pattern;

import android.text.TextUtils;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.extenders.InstanceExtender.InstanceRootFW;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class BinaryExtender {
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	/**
	 * This class is used to handle binaries located in one of the <code>$PATH</code> variable directories. 
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#binary(String)} to retrieve an instance.
	 */
	public static class Binary implements ExtenderGroup {
		private ShellExtender.Shell mShell;
		private RootFW mParent;
		
		private String mBinary;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Binary(parent, (String) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Binary(RootFW parent, String binary) {
			mParent = parent;
			mShell = parent.shell();
			mBinary = binary;
		}
		
		/**
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onExtenderReconfigure() {}
		
		/**
		 * Check whether a binary exists in one of the <code>$PATH</code> variable directories.
		 * 
		 * @param parent
		 *     <code>True if the binary exists</code>, <code>False</code> otherwise
		 */
		public Boolean exists() {
			String[] attempts = new String[ RootFW.Config.BINARIES.size()+1 ];
			
			for (int i=0; i < attempts.length-1; i++) {
				/* We need to build the attempts manually because 'which' needs a binary to test for, which means that 'busybox which which' will have a negative affect' */
				attempts[i] = "( " + RootFW.Config.BINARIES.get(i) + " which " + RootFW.Config.BINARIES.get(i) + " > /dev/null 2>&1 ) && ( " + RootFW.Config.BINARIES.get(i) + " which '" + mBinary + "' && echo true || echo false )";
			}
			attempts[attempts.length-1] = "( which which > /dev/null 2>&1 ) && ( which '' && echo true || echo false )";
			
			ShellResult result = mShell.addAttempts(attempts).run();
			
			if (!result.wasSuccessful()) {
				/* Devices without busybox (Yes they do exist), does not always have 'which' available. So here we need to manually check the $PATH variable */
				result = mShell.buildAttempts("( %binary test true > /dev/null 2>&1 ) && ( for i in " + TextUtils.join(" ", mParent.getEnvironmentVariable()) + "; do %binary test -e \"$i/" + mBinary + "\" && echo true && break; done )").run();
			}
			
			return result.wasSuccessful() && "true".equals(result.getLine());
		}
	}
	
	/**
	 * This class is used to collect information about either a specific busybox binary, or the one found in the PATH environment variable.
	 * <br />
	 * Note that this implements the {@link ExtenderGroup} interface, which means that it does not allow anything outside {@link RootFW} to create an instance of it. Use {@link RootFW#busybox()} or {@link RootFW#busybox(String)} to retrieve an instance.
	 */
	public static class Busybox implements ExtenderGroup {
		private ShellExtender.Shell mShell;
		
		private String mBinary;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Busybox(parent, (String) transfer.arguments[0]));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Busybox(RootFW parent, String binary) {
			mShell = parent.shell();
			mBinary = binary;
		}
		
		/**
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onExtenderReconfigure() {}
		
		/**
		 * Check whether or not the busybox binary exists and is executable.
		 *     
		 * @return
		 *     <code>True</code> if it exists, <code>False</code> otherwise
		 */
		public Boolean exists() {
			return mShell.run(mBinary + " test true > /dev/null 2>&1").wasSuccessful();
		}
		
		/**
		 * Get a list of all supported applets from this binary.
		 *     
		 * @return
		 *     An array with all supported applets
		 */
		public String[] getApplets() {
			ShellResult result = mShell.run(mBinary + " --list 2>/dev/null");
			
			if (result.wasSuccessful()) {
				return result.trim().getArray();
			}
			
			return null;
		}
		
		/**
		 * Check whether a specific applet is supported by this binary.
		 * 
		 * @param applet
		 *     The name of the applet to look for
		 *     
		 * @return
		 *     <code>True</code> if the applet exists, <code>False</code> otherwise
		 */
		public Boolean hasApplet(String applet) {
			String[] list = getApplets();
			
			if (list != null) {
				for (int i=0; i < list.length; i++) {
					if (list[i].equals(applet)) {
						return true;
					}
				}
			}
			
			return false;
		}
		
		/**
		 * Get the version number of this binary. 
		 * Note that versions like <code>v1.21.0-jb</code> will be trimmed to <code>1.21.0</code>.
		 *     
		 * @return
		 *     The version of this binary
		 */
		public String version() {
			ShellResult result = mShell.run(mBinary + " 2>/dev/null");
			
			if (result.wasSuccessful() && result.size() > 0) {
				String version = oPatternSpaceSearch.split(result.getLine(0))[1].substring(1);
				
				return version.contains("-") ? 
						version.substring(0, version.indexOf("-")) : 
							version;
			}
			
			return null;
		}
	}
}
