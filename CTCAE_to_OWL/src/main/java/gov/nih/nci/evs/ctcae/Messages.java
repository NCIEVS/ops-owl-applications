package gov.nih.nci.evs.ctcae;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {


	
	private static final String BUNDLE_NAME = "column"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	public Messages(String fileLoc) {
		
		try {
		File file = new File(fileLoc);
		URL[] urls={file.toURI().toURL()};
		ClassLoader loader = new URLClassLoader(urls);
		RESOURCE_BUNDLE = ResourceBundle
		        .getBundle(BUNDLE_NAME,Locale.getDefault(), loader);
		} catch (MalformedURLException e){// TODO Auto-generated catch block
			e.printStackTrace();}
	}
	
	Messages() {
		RESOURCE_BUNDLE = ResourceBundle
		        .getBundle(BUNDLE_NAME);
	}

	public String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
