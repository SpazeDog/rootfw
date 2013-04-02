package com.spazedog.rootfw.containers;

public class FileStat extends FileInfo {
	private String GROUPNAME;
	private String USERNAME;
	private String ATIME;
	private String MTIME;
	private String CTIME;
	private Integer BLOCKS;
	private Integer IOBLOCK;
	private Integer INODE;
	private String MM;
	private String MMTYPE;
	
	public FileStat(String argName, String argType, String argUserId, String argGroupId, String argUserName, String argGroupName, String argPerms, String argStringPerms, String argLink, Long argSize, String argAtime, String argMtime, String argCtime, Integer argBlocks, Integer argIOBlock, Integer argINode, String argMM, String argMMType) {
		super(argName, argType, argUserId, argGroupId, argPerms, argStringPerms, argLink, argSize, argMM);
		
		GROUPNAME = argGroupName;
		USERNAME = argUserName;
		ATIME = argAtime;
		MTIME = argMtime;
		CTIME = argCtime;
		BLOCKS = argBlocks;
		IOBLOCK = argIOBlock;
		INODE = argINode;
		MM = argMM;
		MMTYPE = argMMType;
	}
	
	public String getUserName() {
		return USERNAME;
	}
	
	public String getGroupName() {
		return GROUPNAME;
	}
	
	public String getAtime() {
		return ATIME;
	}
	
	public String getMtime() {
		return MTIME;
	}
	
	public String getCtime() {
		return CTIME;
	}
	
	public Integer getBlocks() {
		return BLOCKS;
	}
	
	public Integer getIOBlock() {
		return IOBLOCK;
	}
	
	public Integer getInode() {
		return INODE;
	}
	
	public String getMM() {
		return MM;
	}
	
	public String getMMType() {
		return MMTYPE;
	}
}