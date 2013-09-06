package com.spazedog.lib.rootfw3.containers;

import java.util.HashMap;
import java.util.Map;

public abstract class BasicContainer {
	private Map<String, Object> mObjects = new HashMap<String, Object>();
	
	public void putObject(String name, Object object) {
		mObjects.put(name, object);
	}
	
	public Object getObject(String name) {
		return mObjects.get(name);
	}
}
