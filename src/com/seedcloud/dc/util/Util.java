package com.seedcloud.dc.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Util {
	public static Map<String,String> getSettingsFromPropertiesFile(String filePath) {
		Properties prop = new Properties();
		Map<String,String> settingsMap = new HashMap<String,String>();
		try {
			prop.load(new FileInputStream(filePath));
			settingsMap.put("usengrams",
					prop.getProperty("usengrams"));

			settingsMap.put("numberoffeatures",
					prop.getProperty("numberoffeatures"));


			return settingsMap;
			
		} catch (IOException io) {
			return null;
		}
		
	}
}
