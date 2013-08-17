package com.spazedog.lib.rootfw3.extenders;

import java.util.regex.Pattern;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.extenders.ShellExtender.ShellResult;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

/**
 * This class is used to collect information about either a specific busybox binary, or the one found in the PATH environment variable.
 * <br />
 * <strong>As this class is an {@link ExtenderGroup}, it should not be called directly. Instead use {@link RootFW#busybox()} or {@link RootFW#busybox(String)}</strong>
 * 
 * <dl>
 * <dt><span class="strong">Example:</span></dt>
 * <dd><code><pre>
 * RootFW root = new RootFW();
 * 
 * if (root.isConnected()) {
 *     if( root.busybox().exists() && root.busybox().hasApplet("blkid") ) {
 *         ...
 *     }
 *     
 *     root.disconnect();
 * }
 * </pre></code></dd>
 * </dl>
 */
public class BusyboxExtender implements ExtenderGroup {
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	protected ShellExtender mShell;
	
	protected String mBinary = "busybox";
	
	/**
	 * Create a new {@link BusyboxExtender} instance.
	 * This constructor will use the busybox binary located in the PATH environment variable.
	 * 
	 * @see RootFW#busybox()
	 * 
	 * @param shell
	 *     A {@link ShellExtender} object
	 */
	public BusyboxExtender(ShellExtender shell) {
		mShell = shell;
	}
	
	/**
	 * Create a new {@link BusyboxExtender} instance.
	 * This constructor will use a specific defined binary.
	 * 
	 * @see RootFW#busybox(String)
	 * 
	 * @param shell
	 *     A {@link ShellExtender} object
	 *     
	 * @param binary
	 *     Path to the busybox binary
	 */
	public BusyboxExtender(ShellExtender shell, String binary) {
		this(shell);
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
