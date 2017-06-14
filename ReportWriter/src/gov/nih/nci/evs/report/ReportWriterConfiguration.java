package gov.nih.nci.evs.report;

import java.util.HashMap;


public class ReportWriterConfiguration {

	private String httpURL = "";
	
	private String subset = "";
	
	private String outputPrefix = ".//output//";

	private String outputFile = "";

	private String vocabulary = "NCI_Thesaurus";

	private String outputFormat = "";

	private String printPT = "";

	private String printDef = "";

	private String printSyn = "";

	private boolean printParent = false;
	private boolean printChildren = false;
	
	private String delimiter = "$";
	private String internalDelimiter = "|";
	

	private String source = "";
	private HashMap<String,String> propertyMap = new HashMap<String,String>();
	
	private boolean loadTypeOwl = false;

	//TODO  should the propertyMap be and array to maintain the original column order
	public ReportWriterConfiguration(HashMap<String, String> optionMap,HashMap<String, String> propertyMap) {

		
		this.httpURL = optionMap.get("U");
		this.subset = optionMap.get("q");
		this.outputFormat = optionMap.get("o");
		this.vocabulary = "NCI_Thesaurus";
		this.printPT = optionMap.get("p");
		this.printDef = optionMap.get("d");
		this.printSyn = optionMap.get("s");
		this.outputFile = optionMap.get("n");
		this.source = optionMap.get("c");
		this.printChildren = false;
		this.printParent = false;
		this.setDelimiter(optionMap.get("d1"));
		this.setInternalDelimiter(optionMap.get("d2"));
		
		if(optionMap.get("par").toUpperCase().compareTo("Y")==0){
			this.printParent = true;}
		if(optionMap.get("chi").toUpperCase().compareTo("Y")==0){
			this.printChildren = true;}
		if(optionMap.get("owl").toUpperCase().compareTo("Y")==0){
			this.loadTypeOwl=true;
		}
		
		if(propertyMap!=null){
		  this.propertyMap = propertyMap;
		}

		
	}
	
	public boolean isLoadTypeOwl(){
		return loadTypeOwl;
	}

	public String getHttpURL() {
		return httpURL;
	}

	public void setHttpURL(String httpURL) {
		this.httpURL = httpURL;
	}

	public String getOutputPrefix() {
		return outputPrefix;
	}

	public void setOutputPrefix(String outputPrefix) {
		this.outputPrefix = outputPrefix;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getVocabulary() {
		return vocabulary;
	}

	public void setVocabulary(String vocabulary) {
		this.vocabulary = vocabulary;
	}

	public String getSubset() {
		return subset;
	}

	public void setSubset(String subset) {
		this.subset = subset;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
		if (this.outputFormat.compareTo("T") == 0) {
			this.outputFile = this.outputPrefix + this.outputFile + ".txt";
		} else {
			this.outputFile = this.outputPrefix + this.outputFile + ".xml";
		}
	}

	public String getPrintPT() {
		return printPT;
	}

	public void setPrintPT(String printPT) {
		this.printPT = printPT;
	}

	public String getPrintDef() {
		return printDef;
	}

	public void setPrintDef(String printDef) {
		this.printDef = printDef;
	}

	public String getPrintSyn() {
		return printSyn;
	}

	public void setPrintSyn(String printSyn) {
		this.printSyn = printSyn;
	}

	public boolean getPrintParent() {
		return printParent;
	}

	public void setPrintParent(boolean printParent) {
		this.printParent = printParent;
	}

	public boolean getPrintChildren() {
		return printChildren;
	}

	public void setPrintChildren(boolean printChildren) {
		this.printChildren = printChildren;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}



	public HashMap<String, String> getPropertyMap() {
		return propertyMap;
	}


	public String getDelimiter() {
		return delimiter;
	}


	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}


	public String getInternalDelimiter() {
		return internalDelimiter;
	}


	public void setInternalDelimiter(String internalDelimiter) {
		this.internalDelimiter = internalDelimiter;
	}


	public boolean getUseOWL(){
		return loadTypeOwl;
	}
	
	public void setUseOWL(boolean useOWL){
		this.loadTypeOwl = useOWL;
	}
}
