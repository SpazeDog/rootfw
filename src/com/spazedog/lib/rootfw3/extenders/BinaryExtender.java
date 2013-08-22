package com.spazedog.lib.rootfw3.extenders;

import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class BinaryExtender {
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
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
