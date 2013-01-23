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

package com.spazedog.rootfw.containers;

public class FileData {
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
