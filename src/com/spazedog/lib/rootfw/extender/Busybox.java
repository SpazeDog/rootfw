package com.spazedog.lib.rootfw.extender;

import java.util.regex.Pattern;

import com.spazedog.lib.rootfw.RootFW;
import com.spazedog.lib.rootfw.container.ShellResult;
import com.spazedog.lib.rootfw.iface.Extender;

public final class Busybox implements Extender {
	public final static String TAG = RootFW.TAG + "::Busybox";
	
	private final static Pattern oPatternSpaceSearch = Pattern.compile("[ \t]+");
	
	private RootFW mParent;
	
	/**
	 * This should only be used internally.
	 * <p/>
	 * Use <code>RootFW.busybox</code> instead
	 * 
	 * @param aInstance
	 *     An instance of RootFW
	 */
	public Busybox(RootFW aInstance) {
		mParent = aInstance;
	}
	
	/**
	 * Check whether busybox is available on the device or not
	 *    
	 * @return
	 *     <code>True</code> if busybox is available on the device
	 */
	public Boolean exists() {
		ShellResult lResult = mParent.shell.execute("busybox test true > /dev/null 2>&1");
		
		return lResult != null && lResult.code() == 0;
	}
	
	/**
	 * Get a list of all available applets in the current busybox binary
	 *    
	 * @return
	 *     An array with all the applet names
	 */
	public String[] applets() {
		ShellResult lResult = mParent.shell.execute("busybox --list 2>/dev/null");
		
		if (lResult != null && lResult.code() == 0 && lResult.output().length() > 0) {
			return lResult.output().raw();
		}
		
		return null;
	}
	
	/**
	 * Check if a specified applet is available in the current busybox binary
	 * 
	 * @param aApplet
	 *     The name of the applet to check for
	 *    
	 * @return
	 *     <code>True</code> if the applet exist
	 */
	public Boolean hasApplet(String aApplet) {
		String[] lApplets = applets();
		
		if (lApplets != null) {
			for (int i=0; i < lApplets.length; i++) {
				if (lApplets[i].equals(aApplet)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Get the version number of the current busybox binary
	 * <p/>
	 * A version string like <code>v1.21.0-jb</code> will be returned as <code>1.21.0</code>
	 *    
	 * @return
	 *     The version number
	 */
	public String version() {
		ShellResult lResult = mParent.shell.execute("busybox 2>/dev/null");
		
		if (lResult != null && lResult.code() == 0 && lResult.output().length() > 0) {
			try {
				String lVersion = oPatternSpaceSearch.split(lResult.output().line(0))[1].substring(1);
				
				if (lVersion.contains("-")) {
					lVersion = lVersion.substring(0, lVersion.indexOf("-"));
				}
				
				return lVersion;
				
			} catch(Throwable e) {}
		}
		
		return null;
	}
}
