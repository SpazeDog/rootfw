package com.spazedog.rootfw.containers;

public final class FileInfo {
	private String TYPE;
	private String USER;
	private String GROUP;
	private String PERMS;
	private String STRINGPERMS;
	private String LINK;
	
	public FileInfo(String argType, String argUser, String argGroup, String argPerms, String argStringPerms, String argLink) {
		TYPE = argType;
		USER = argUser;
		GROUP = argGroup;
		PERMS = argPerms;
		STRINGPERMS = argStringPerms;
		LINK = argLink;
	}
	
	public String getType() {
		return TYPE;
	}
	
	public String getUser() {
		return USER;
	}
	
	public String getGroup() {
		return GROUP;
	}
	
	public String getPermissions() {
		return PERMS;
	}
	
	public String getPermissionString() {
		return STRINGPERMS;
	}
	
	public String getLink() {
		return LINK;
	}
}
