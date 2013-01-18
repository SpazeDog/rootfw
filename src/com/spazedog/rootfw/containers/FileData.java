package com.spazedog.rootfw.containers;

public final class FileData {
	private String[] DATA;
	
	public FileData(String[] argData) {
		DATA = argData;
	}
	
	public String[] getData() {
		return DATA;
	}
	
	public String getAssembled() {
		return getAssembled("\n", false);
	}
	
	public String getAssembled(String argAssembler, Boolean argSkipEmptyLines) {
		String data = "";
		
		for (int i=0; i < DATA.length; i++) {
			if (!argSkipEmptyLines || DATA[i].trim().length() > 0) {
				data += (i > 0 ? argAssembler : "") + DATA[i];
			}
		}
		
		return data;
	}
	
	public String getFirstLine() {
		return getLine(1, true);
	}
	
	public String getLastLine() {
		return getLine(-1, true);
	}
	
	public String getLine(Integer argNumber, Boolean argSkipEmptyLines) {
		if (getLength() > 0 && argNumber != 0) {
			Integer num = argNumber < 0 ? (DATA.length + argNumber) : argNumber-1;
			
			while(num >= 0 && (DATA.length-1) >= num) {
				if ((DATA.length-1) >= num) {
					if (!argSkipEmptyLines || DATA[num].trim().length() > 0) {
						return DATA[num].trim();
					}
					
					num = argNumber < 0 ? (num - 1) : (num + 1);
				}
			}
		}
		
		return null;
	}
	
	public Integer getLength() {
		if (DATA.length == 1 && DATA[0].trim().length() == 0) {
			return 0;
		}
		
		return DATA.length;
	}
}
