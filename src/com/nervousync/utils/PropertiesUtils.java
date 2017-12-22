/*
 * Copyright © 2003 - 2010 Nervousync Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of 
 * Nervousync Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the 
 * license agreement you entered into with Nervousync Studio.
 */
package com.nervousync.utils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nervousync.commons.core.Globals;

/**
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Mar 5, 2010 11:03:51 AM $
 */
public final class PropertiesUtils {

	private transient static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);
	
	private PropertiesUtils() {
		
	}

	public static Hashtable<String, String> convertPropertiesToHashtable(String propertiesFilePath) {
		return convertPropertiesToHashtable(propertiesFilePath, null);
	}
	
	public static Hashtable<String, String> convertPropertiesToHashtable(String propertiesFilePath, 
			Hashtable<String, String> messageMap) {
		Properties properties = loadProperties(propertiesFilePath);
		
		return convertPropertiesToHashtable(properties, messageMap);
	}
	
	public static Hashtable<String, String> convertPropertiesToHashtable(URL url) {
		return convertPropertiesToHashtable(url, null);
	}
	
	public static Hashtable<String, String> convertPropertiesToHashtable(URL url, Hashtable<String, String> messageMap) {
		return convertPropertiesToHashtable(loadProperties(url), messageMap);
	}
	
	public static Hashtable<String, String> convertPropertiesToHashtable(Properties properties, 
			Hashtable<String, String> messageMap) {
		if (messageMap == null) {
			messageMap = new Hashtable<String, String>();
		}

		if (properties != null) {
			Enumeration<Object> enumeration = properties.keys();
			
			while (enumeration.hasMoreElements()) {
				String key = (String)enumeration.nextElement();
				String value = properties.getProperty(key);
				
				messageMap.put(key, value);
			}
			
		}
		
		return messageMap;
	}
	
	public static Properties convertStringToProperties(String propertiesContent) {
		Properties properties = new Properties();
		InputStream inputStream = null;
		if (propertiesContent != null) {
			inputStream = new ByteArrayInputStream(propertiesContent.getBytes());
			
			try {
				if (propertiesContent.startsWith("<")) {
					properties.loadFromXML(inputStream);
				} else {
					properties.load(inputStream);
				}
				
				inputStream.close();
				inputStream = null;
			} catch (IOException e) {
				properties = new Properties();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
						inputStream = null;
					} catch (IOException e) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Close input stream error! ");
						}
					}
				}
			}
		}
		return properties;
	}
	
	public static Properties loadProperties(String propertiesFilePath) {
		try {
			URL url = FileUtils.getURL(propertiesFilePath);
			return loadProperties(url);
		} catch (Exception e) {
			return new Properties();
		}
	}
	
	public static Properties loadProperties(URL url) {
		InputStream inputStream = null;
		try {
			String fileName = url.getFile();
			String fileExtName = StringUtils.getFilenameExtension(fileName);
			inputStream = url.openStream();
			if (fileExtName.equalsIgnoreCase("xml")) {
				return loadProperties(inputStream, true);
			} else {
				return loadProperties(inputStream, false);
			}
		} catch (Exception e) {
			if (PropertiesUtils.LOGGER.isDebugEnabled()) {
				PropertiesUtils.LOGGER.debug("Load properties error! ", e);
			}
			return new Properties();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					if (PropertiesUtils.LOGGER.isDebugEnabled()) {
						PropertiesUtils.LOGGER.debug("Close input stream error! ", e);
					} else {
						PropertiesUtils.LOGGER.warn("Close input stream error! ");
					}
				}
			}
		}
	}
	
	public static Properties loadProperties(InputStream inputStream, boolean isXML) {
		Properties properties = new Properties();
		try {
			if (isXML) {
				properties.loadFromXML(inputStream);
			} else {
				properties.load(inputStream);
			}
			
			return properties;
		} catch (Exception e) {
			if (PropertiesUtils.LOGGER.isDebugEnabled()) {
				PropertiesUtils.LOGGER.debug("Load properties error! ", e);
			}
			return new Properties();
		}
	}
	
	public static boolean modifyProperties(String propertiesFilePath, Map<String, String> modifyMap, String comment) {
		FileOutputStream fileOutputStream = null;
		try {
			String fileExtName = StringUtils.getFilenameExtension(propertiesFilePath);
			
			Properties modifyProperties = loadProperties(propertiesFilePath);
			
			Iterator<String> keySet = modifyMap.keySet().iterator();
			
			while(keySet.hasNext()) {
				String key = keySet.next();
				String value = modifyMap.get(key);
				if (value != null) {
					modifyProperties.setProperty(key, value);
				}
			}
			
			fileOutputStream = new FileOutputStream(propertiesFilePath, false);
			
			if (fileExtName.equalsIgnoreCase("xml")) {
				modifyProperties.storeToXML(fileOutputStream, comment, "UTF-8");
			} else if (fileExtName.equalsIgnoreCase("properties")) {
				modifyProperties.store(fileOutputStream, comment);
			} else {
				throw new Exception("Properties file error");
			}
			
			return true;
		} catch (Exception e) {
			if (PropertiesUtils.LOGGER.isDebugEnabled()) {
				PropertiesUtils.LOGGER.debug("Modify properties error! ", e);
			}
			return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.flush();
					fileOutputStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public static Properties modifyProperties(Properties properties, Map<String, String> modifyMap) {
		Iterator<Object> keySet = properties.keySet().iterator();
		
		while(keySet.hasNext()) {
			String key = (String)keySet.next();
			String value = modifyMap.get(key);
			
			if (value != null) {
				properties.setProperty(key, value);
			}
		}
		
		return properties;
	}
	
	public static boolean saveProperties(Properties properties, String propertiesFilePath, String comment) {
		FileOutputStream fileOutputStream = null;
		try {
			String filePath = 
					propertiesFilePath.substring(0, propertiesFilePath.lastIndexOf(Globals.DEFAULT_PAGE_SEPARATOR));
			FileUtils.makeHome(filePath);
			String fileExtName = StringUtils.getFilenameExtension(propertiesFilePath);

			fileOutputStream = new FileOutputStream(propertiesFilePath, false);
			
			if (fileExtName.equalsIgnoreCase("xml")) {
				properties.storeToXML(fileOutputStream, comment, "UTF-8");
			} else if (fileExtName.equalsIgnoreCase("properties")) {
				properties.store(fileOutputStream, comment);
			} else {
				throw new Exception("Properties file error");
			}
			
			return true;
		} catch (Exception e) {
			if (PropertiesUtils.LOGGER.isDebugEnabled()) {
				PropertiesUtils.LOGGER.debug("Save properties error! ", e);
			}
			return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.flush();
					fileOutputStream.close();
				} catch (IOException ex) {
					
				}
			}
		}
	}
	
	public static String getPropertiesValue(String propertiesFilePath, String keyName) {
		if (keyName == null) {
			return null;
		}
		
		Properties properties = loadProperties(propertiesFilePath);
		
		if (properties == null) {
			return null;
		}
		
		return properties.getProperty(keyName);
	}
}