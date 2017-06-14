package gov.hhs.fda.ctr.common;

import gov.hhs.fda.ctr.common.exception.PropertyFileLoadException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author Tim Stone
 */
public class CTRProperties {
	
	private static final Logger log = Logger.getLogger(CTRProperties.class.getName());
	
	/**
	 * 
	 */
	private static long lastModified;
	
	/**
	 * 
	 */
	private static Properties properties;
	
	/**
	 * 
	 * @param property
	 * @return
	 * @throws PropertyFileLoadException
	 */
	public static String getProperty(String property) throws PropertyFileLoadException {
		return getProperty(property, null);
	}
	
	/**
	 * 
	 * @param property
	 * @param defaultValue
	 * @return
	 * @throws PropertyFileLoadException
	 */
	public static String getProperty(String property, String defaultValue) throws PropertyFileLoadException {
		File propertiesFile = new File(CTRConstants.COM_PROPERTIES_FILE);
		if(!propertiesFile.isFile()){
			propertiesFile = new File(CTRConstants.COM_PROPERTIES_FILE_PENTAHO);
		}
		
		if (properties == null || propertiesFile.lastModified() > lastModified) {
			properties = new Properties();
			
			try {
				InputStream is = new FileInputStream(propertiesFile); 
				
				properties.load(is);
				is.close();
			} catch (FileNotFoundException ex) {
				throw new PropertyFileLoadException(String.format(
						"The properties file %s does not exist or is not readable",
						propertiesFile.getAbsolutePath()
				), ex);
			} catch (IOException ex) {
				throw new PropertyFileLoadException(String.format(
						"An error was encountered while attempting to read the properties file %s",
						propertiesFile.getAbsolutePath()
				), ex);
			}
		}
		
		log.debug(property + " = " + properties.getProperty(property, defaultValue));
		
		return properties.getProperty(property, defaultValue);
	}
}
