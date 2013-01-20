package com.spazedog.rootfw.containers;

import java.util.ArrayList;

public class ShellCommand {
	private ArrayList<String[]> CMD = new ArrayList<String[]>();
	private ArrayList<Integer> RESULT = new ArrayList<Integer>();
	
	private static String[] BINARIES = {"busybox", "toolbox"};
	
	public static ShellCommand makeCompatibles(String argCommands) {
		return makeCompatibles(new String[] {argCommands}, null);
	}
	
	public static ShellCommand makeCompatibles(String[] argCommands) {
		return makeCompatibles(argCommands, null);
	}
	
	public static ShellCommand makeCompatibles(String argCommands, Integer[] argResults) {
		return makeCompatibles(new String[] {argCommands}, argResults);
	}
	
	public static ShellCommand makeCompatibles(String[] argCommands, Integer[] argResults) {
		ShellCommand command = new ShellCommand(0);
		
		for (int x=0; x < argCommands.length; x++) {
			for (int y=0; y < BINARIES.length; y++) {
				command.addCommand(BINARIES[y] + " " + argCommands[x] + " 2>/dev/null");
			}
			
			command.addCommand(argCommands[x] + " 2>/dev/null");
		}
		
		if (argResults != null) {
			for (int i=0; i < argResults.length; i++) {
				command.addResult(argResults[i]);
			}
		}
		
		return command;
	}
	
	public static String[] getCompatibleBinaries() {
		return BINARIES;
	}
	
	public ShellCommand() {}
	
	public ShellCommand(Integer argResult) {
		RESULT.add(argResult);
	}
	
	public ShellCommand addCommand(String argCmd) {
		CMD.add(new String[] {argCmd});
		
		return this;
	}
	
	public ShellCommand addCommand(String[] argCmd) {
		CMD.add(argCmd);
		
		return this;
	}
	
	public ShellCommand addResult(Integer argResult) {
		RESULT.add(argResult);
		
		return this;
	}
	
	public Integer getCommandLength() {
		return CMD.size();
	}
	
	public Integer getResultLength() {
		return RESULT.size();
	}
	
	public String[] getCommand(Integer index) {
		return CMD.get(index);
	}
	
	public Integer getResult(Integer index) {
		return RESULT.get(index);
	}
}
