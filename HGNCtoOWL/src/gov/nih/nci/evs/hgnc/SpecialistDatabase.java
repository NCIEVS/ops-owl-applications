package gov.nih.nci.evs.hgnc;

import java.net.MalformedURLException;
import java.net.URL;

public class SpecialistDatabase {

	private static String propertyKey = "lsdb";
	private String dbName;
	private URL dbURL;
	
	public SpecialistDatabase(String name, URL url){
		this.dbName = name;
		this.dbURL = url;
	}
	
	public SpecialistDatabase(String name, String url){
		try {
			//If they pass in a string, make sure it is a valid URL
			this.dbName = name;
			dbURL = new URL(url);
		} catch (MalformedURLException e) {
			System.out.println("invalid URL for Specialist Database entry "+ name);
		}
	}
	
	
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public URL getDbURL() {
		return dbURL;
	}
	public void setDbURL(URL dbURL) {
		this.dbURL = dbURL;
	}
	
	
}
