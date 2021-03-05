package gov.nih.nci.evs;

import java.util.Vector;


public class ValueSetReport {
	private String name;
	private String uri;
	private Vector<String> sources = new Vector<String>();
	private Vector<String> ftpLocations = new Vector<String>();
	private final String ftpSite = new String("ftp://ftp1.nci.nih.gov/pub/cacore/");
	
	public ValueSetReport(String name, String code, Vector<String> ftpLocations, Vector<String> sources) {
		this.name = name;
		String urlStart = "http://evs.nci.nih.gov/valueset/";
		String sourcePath = "";
		if(sources.size()==1){
			sourcePath = sources.get(0) + "/";
		} else if (sources.size()==2){
			if (sources.contains("NICHD")&& sources.contains("CDISC")){
				sourcePath = "CDISC/";
			} else {
				System.out.println("Error, multiple sources for " + code);
			}

		} else if (sources.size()>2){
			System.out.println("Error, multiple sources for " + code);
		}
		this.uri = urlStart + sourcePath + code;
		for( String loc : ftpLocations ) {
			if( !loc.equals("null|null") ) {
				this.ftpLocations.add(ftpSite + loc);
			}
			else {
				this.ftpLocations.add(loc);
			}
		}
		for( String source : sources ) {
			this.sources.add(source);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getURI() {
		return uri;
	}
	
	public Vector<String> sources() {
		return sources;
	}
	
	public Vector<String> getFtpLocations() {
		return ftpLocations;
	}
	
}
