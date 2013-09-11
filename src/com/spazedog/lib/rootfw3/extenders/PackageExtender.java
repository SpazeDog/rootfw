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

package com.spazedog.lib.rootfw3.extenders;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.spazedog.lib.rootfw3.RootFW;
import com.spazedog.lib.rootfw3.RootFW.ExtenderGroupTransfer;
import com.spazedog.lib.rootfw3.containers.BasicContainer;
import com.spazedog.lib.rootfw3.interfaces.ExtenderGroup;

public class PackageExtender {
	public static class Packages implements ExtenderGroup {
		protected RootFW mParent;
		
		/**
		 * This is used internally by {@link RootFW} to get a new instance of this class. 
		 */
		public static ExtenderGroupTransfer getInstance(RootFW parent, ExtenderGroupTransfer transfer) {
			return transfer.setInstance((ExtenderGroup) new Packages(parent));
		}
		
		/**
		 * Create a new instance of this class.
		 * 
		 * @param parent
		 *     A reference to the {@link RootFW} instance
		 */
		private Packages(RootFW parent) {
			mParent = parent;
		}
		
		/**
		 * Used by RootFW to tell the extender that someone has asked for an instance. 
		 * This is useful because RootFW saves instances, and therefore we can't be sure that the constructor is called. 
		 */
		@Override
		public void onExtenderReconfigure() {}
		
		/**
		 * Get a list of all packages defined in Android's packages.xml file. 
		 * This will only provide you with package names, versions and paths, and you will need 
		 * to use Android's Package Manager to get more details. However this will also provide 
		 * information about the base system package of updated packages. 
		 */
		public PackageDetails[] getPackageList() {
			String[] lines = mParent.file("/data/system/packages.xml").readMatches("package ").trim().getArray();
			Map<String, PackageDetails> list = new HashMap<String, PackageDetails>();
			
			if (lines != null && lines.length > 0) {
				try {
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					
					XmlPullParser parser = factory.newPullParser();
					
					for (int i=0; i < lines.length; i++) {
						String curLine = lines[i].trim();
						Boolean updated = false;
						
						if (curLine.startsWith("<package ") || (updated = curLine.startsWith("<updated-package "))) {
							parser.setInput(new StringReader(curLine));
							
							while (!("" + parser.getName()).contains("package") && parser.getEventType() != XmlPullParser.START_TAG) {
								parser.next();
							}
							
							String attrName = parser.getAttributeValue(null, "name");
							String attrPath = parser.getAttributeValue(null, "codePath");
							Integer attrVersion = Integer.parseInt(parser.getAttributeValue(null, "version"));
							
							if (!list.containsKey(attrName)) {
								list.put(attrName, new PackageDetails());
							}
							
							PackageDetails mainDetails = list.get(attrName);
							
							if (updated) {
								PackageDetails updateDetails = new PackageDetails();
								
								updateDetails.mName = attrName;
								updateDetails.mPath = attrPath;
								updateDetails.mVersion = attrVersion;
								
								mainDetails.mUpdate = true;
								mainDetails.mUpdatedPackage = updateDetails;
								
							} else {
								mainDetails.mName = attrName;
								mainDetails.mPath = attrPath;
								mainDetails.mVersion = attrVersion;
							}
						}
					}
					
				} catch (Throwable e) {}
			}
			
			return list.size() == 0 ? null : 
				list.values().toArray( new PackageDetails[ list.size() ] );
		}
	}
	
	/**
	 * This is a container to store package information from 
	 * Android's packages.xml file.
	 */
	public static class PackageDetails extends BasicContainer {
		private String mName;
		private String mPath;
		private Boolean mUpdate = false;
		private PackageDetails mUpdatedPackage;
		private Integer mVersion;
		
		/**
		 * @return 
		 *     The package name (Not the label)
		 */
		public String name() {
			return mName;
		}
		
		/**
		 * @return 
		 *     The path to the package file
		 */
		public String path() {
			return mPath;
		}
		
		/**
		 * @return 
		 *     <code>True</code> if the package is an updated system package
		 */
		public Boolean isUpdate() {
			return mUpdate;
		}
		
		/**
		 * @return 
		 *     The package version
		 */
		public Integer version() {
			return mVersion;
		}
		
		/**
		 * @return 
		 *     The {@link PackageDetails} instance of the original updated system package (If this package is an update to a system package)
		 */
		public PackageDetails updatedPackage() {
			return mUpdatedPackage;
		}
	}
}
