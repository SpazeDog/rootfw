package com.spazedog.rootfw.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.FileData;
import com.spazedog.rootfw.containers.FileInfo;
import com.spazedog.rootfw.containers.MountCollection;
import com.spazedog.rootfw.containers.ShellResult;

public final class Filesystem {
	private final static String TAG = "RootFW:Filesystem";
	
	private RootFW ROOTFW;
	
	public Filesystem(RootFW argAccount) {
		ROOTFW = argAccount;
	}
	
	public FileInfo getFileInfo(String argPath) {
		if (argPath.equals("/")) {
			return null;
		}
		
		String path = argPath.endsWith("/") ? argPath.substring(0, argPath.length() - 1) : argPath;
		String dir = path.substring(0, path.lastIndexOf("/"));
		String item = path.substring(path.lastIndexOf("/") + 1);
		ShellResult result = ROOTFW.runShell(RootFW.mkCmd("ls -ln " + dir));
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell(RootFW.mkCmd("ls -l " + dir));
		}
		
		if (result.getResultCode() == 0) {
			String[] lines = result.getResult().getData(), parts;
			Integer y;
			
			for (int i=0; i < lines.length; i++) {
				parts = lines[i].replaceAll("  ", " ").trim().split(" ");
				y = parts[parts.length-2].equals("->") ? 3 : 1;
				
				if (parts[parts.length-y].equals(item)) {
					y = parts[4].matches("^[0-9]+$") ? 0 : 1;
					
					String permissions = parts[0];
					String user = parts[2-y];
					String group = parts[3-y];
					String type = permissions.substring(0, 1);
					String link=null;
					
					if (type.equals("l")) {
						link = parts[parts.length-1];
					}
					
					String[] tmpPerms = {permissions.substring(1), permissions.substring(1, 4), permissions.substring(4, 7), permissions.substring(7, 10)};
					Integer tmpNum;
					String perms="";
					for (int x=0; x < tmpPerms.length; x++) {
						tmpNum = (x == 0 && tmpPerms[x].charAt(2) == 's') || (x > 0 && tmpPerms[x].charAt(0) == 'r') ? 4 : 0;
						tmpNum += (x == 0 && tmpPerms[x].charAt(5) == 's') || (x > 0 && tmpPerms[x].charAt(1) == 'w') ? 2 : 0;
						tmpNum += (x == 0 && tmpPerms[x].charAt(8) == 't') || (x > 0 && tmpPerms[x].charAt(2) == 'x') ? 1 : 0;
						
						perms += tmpNum;
					}
					
					return new FileInfo(type, user, group, perms, permissions, link);
				}
			}
		}
		
		return null;
	}
	
	public Boolean exist(String argPath) {
		ShellResult result = ROOTFW.runShell("busybox [ -e " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -e " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return getFileInfo(argPath) != null ? true : false;
	}
	
	public Boolean isFile(String argPath) {
		FileInfo fi;
		ShellResult result = ROOTFW.runShell("busybox [ -f " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -f " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return (fi = getFileInfo(argPath)) != null && fi.getType().equals("-") ? true : false;
	}
	
	public Boolean isDir(String argPath) {
		FileInfo fi;
		ShellResult result = ROOTFW.runShell("busybox [ -d " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -d " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return (fi = getFileInfo(argPath)) != null && fi.getType().equals("d") ? true : false;
	}
	
	public Boolean isLink(String argPath) {
		FileInfo fi;
		ShellResult result = ROOTFW.runShell("busybox [ -L " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -L " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return (fi = getFileInfo(argPath)) != null && fi.getType().equals("l") ? true : false;
	}
	
	public Boolean isBlockDevice(String argPath) {
		FileInfo fi;
		ShellResult result = ROOTFW.runShell("busybox [ -b " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -b " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return (fi = getFileInfo(argPath)) != null && fi.getType().equals("b") ? true : false;
	}
	
	public Boolean isCharacterDevice(String argPath) {
		FileInfo fi;
		ShellResult result = ROOTFW.runShell("busybox [ -c " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell("[ -c " + argPath + " ] && " + RootFW.mkCmdEcho("true") + " || " + RootFW.mkCmdEcho("false") + " 2>/dev/null");
		}
		
		if (result.getResultCode() == 0) {
			return result.getResult().getLastLine().equals("true") ? true : false;
		}
		
		return (fi = getFileInfo(argPath)) != null && fi.getType().equals("c") ? true : false;
	}
	
	public Boolean copyFileResource(Context argContext, Integer argSrc, String argDes, String argPerms, String argUser, String argGroup) {
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
				ShellResult result = ROOTFW.runShell(RootFW.mkCmd("cp " + argSrc + " " + dest));
				
				if (result.getResultCode() != 0) {
					result = ROOTFW.runShell(RootFW.mkCmd("cat " + argSrc + " > " + dest));
				}
				
				if (result.getResultCode() == 0) {
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
				ShellResult result = ROOTFW.runShell(RootFW.mkCmd("mv " + argSrc + " " + dest));
				
				if (result.getResultCode() != 0) {
					result = ROOTFW.runShell(RootFW.mkCmd("cat " + argSrc + " > " + dest));
					FileInfo fi;
					
					if (result.getResultCode() == 0 && (fi = getFileInfo(argSrc)) != null) {
						if (setPermissions(argDes, fi.getPermissions()) && setOwner(argDes, fi.getUser(), fi.getGroup()) && rmFile(argSrc)) {
							status = true;
						}
					}
					
				} else {
					status = true;
				}
				
				return status;
			}
			
		} catch(Throwable e) { e.printStackTrace(); }
		
		return false;
	}
	
	public FileData readFile(String argFile) {
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("cat " + argFile));
			
			if (result.getResultCode() == 0) {
				return result.getResult();
			}
		}
		
		return null;
	}
	
	public String readFileLine(String argFile) {
		if (exist(argFile)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("sed -n '1p' " + argFile));
			
			if (result.getResultCode() != 0) {
				result = ROOTFW.runShell(RootFW.mkCmd("cat " + argFile));
			}
			
			if (result.getResultCode() == 0) {
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
					result = ROOTFW.runShell(RootFW.mkCmd("echo " + argLineData + " >> " + argFile));
				
				} else {
					result = ROOTFW.runShell(RootFW.mkCmd("echo " + argLineData + " > " + argFile));
				}
				
				if (result.getResultCode() == 0) {
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
		if (argPath.length() > 0 && !exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) { 
				result = ROOTFW.runShell(RootFW.mkCmd("chmod -R " + argPerms + " " + argPath));
				
			} else {
				result = ROOTFW.runShell(RootFW.mkCmd("chmod " + argPerms + " " + argPath));
			}
			
			return result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup) {
		return setOwner(argPath, argUser, argGroup, false);
	}
	
	public Boolean setOwner(String argPath, String argUser, String argGroup, Boolean argRecursive) {
		if (argPath.length() > 0 && !exist(argPath)) {
			ShellResult result;
			
			if (argRecursive) {
				result = ROOTFW.runShell(RootFW.mkCmd("chown -R " + (argUser + "." + argGroup) + " " + argPath));
			
			} else {
				result = ROOTFW.runShell(RootFW.mkCmd("chown -R " + (argUser + "." + argGroup) + " " + argPath));
			}
			
			return result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean rmFile(String argPath) {
		if (argPath.length() > 0 && !exist(argPath)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("unlink " + argPath));
			
			if (result.getResultCode() != 0) {
				result = ROOTFW.runShell(RootFW.mkCmd("rm -rf " + argPath));
			}
			
			return result.getResultCode() == 0 ? true : false;
		}
		
		return true;
	}
	
	public Boolean rmDir(String argPath) {
		if (argPath.length() > 0 && !isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("rmdir " + argPath));
			
			if (result.getResultCode() != 0) {
				result = ROOTFW.runShell(RootFW.mkCmd("rm -rf " + argPath));
			}
			
			return result.getResultCode() == 0 ? true : false;
		}
		
		return true;
	}
	
	public Boolean mkDir(String argPath) {
		if (argPath.length() > 0 && !isDir(argPath)) {
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("mkdir -p " + argPath));
			
			if (result.getResultCode() != 0) {
				String path = argPath.endsWith("/") ? argPath.substring(0, -1) : argPath;
				path = path.startsWith("/") ? path.substring(1) : path;
				
				String[] paths = path.split("/");
				path = "/";
				for (int i=0; i < paths.length; i++) {
					path += paths[i] + "/";
					
					if (!isDir(path)) {
						result = ROOTFW.runShell(RootFW.mkCmd("mkdir " + path));
	
						if (result.getResultCode() != 0) {
							break;
						}
					}
				}
			}
			
			return result.getResultCode() == 0 ? true : false;
			
		} else {
			return true;
		}
	}
	
	public Boolean emptyDir(String argPath) {
		if (isDir(argPath)) {
			String dir = argPath.endsWith("/") ? argPath.substring(0, argPath.length() - 1) : argPath;
			ShellResult result = ROOTFW.runShell(RootFW.mkCmd("rm -rf " + dir + "/*"));
			
			return result.getResultCode() == 0 ? true : false;
		}
		
		return false;
	}
	
	public Boolean remount(String argMountPoint, String argOptions) {
		return mount(argMountPoint, "remount," + argOptions);
	}
	
	public Boolean unmount(String argDevice) {
		ShellResult result = ROOTFW.runShell(RootFW.mkCmd("umount " + argDevice));
		
		if (result.getResultCode() != 0) {
			result = ROOTFW.runShell(RootFW.mkCmd("umount -f " + argDevice));
		}
		
		return result.getResultCode() == 0 ? true : false;
	}
	
	public Boolean mount(String argMountPoint, String argOptions) {
		return mount(null, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argOptions) {
		return mount(argDevice, argMountPoint, null, argOptions);
	}
	
	public Boolean mount(String argDevice, String argMountPoint, String argFileSystem, String argOptions) {
		String mount = "mount" + (argDevice != null ? " " + argDevice : "") + (argFileSystem != null ? " -t " + argFileSystem : "") + (argOptions != null ? " -o " + argOptions : "") + " " + argMountPoint;
		ShellResult result = ROOTFW.runShell(RootFW.mkCmd(mount));
		
		return result.getResultCode() == 0 ? true : false;
	}
	
	public ArrayList<MountCollection> getMounts() {
		FileData filedata = readFile("/proc/mounts");
		
		if (filedata != null) {
			String[] mounts = filedata.getData(), line;
			
			ArrayList<MountCollection> list = new ArrayList<MountCollection>();
			
			if (mounts != null) {
				for (int i=0; i < mounts.length; i++) {
					line = mounts[i].replaceAll("  ", "").trim().split(" ");
					
					list.add(new MountCollection(line[0], line[1], line[2], line[3].split(",")));
				}
				
				return list;
			}
		}
		
		return null;
	}
}
