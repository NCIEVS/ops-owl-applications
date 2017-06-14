package gov.nih.nci.evs.report.writer;

import gov.nih.nci.evs.report.ReportWriterConcept;
import gov.nih.nci.evs.report.ReportWriterConfiguration;
import gov.nih.nci.evs.report.ReportWriterProperty;
import gov.nih.nci.evs.report.ReportWriter.RWConceptSorter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;



public class WriteReport {
	
	String delimiter = "\t";
	String internalDelimiter = "$";
	
	public WriteReport(ReportWriterConfiguration inConfig,
			ReportWriterConcept[] rWconcepts2) {
		this.config = inConfig;
		this.outputFile = config.getOutputFile();
		this.hset = new HashSet<String>();
		this.RWconcepts = rWconcepts2;
		delimiter = config.getDelimiter();
		internalDelimiter = config.getInternalDelimiter();
		generateReport();
	}

	private PrintWriter out;
	private ReportWriterConcept[] RWconcepts = null;
	private String outputFile;
	private HashSet<String> hset;
//	private HashMap<String, String> optionMap;
	ReportWriterConfiguration config;
	
	/**
	 * 
	 *
	 */
	private void generateReport() {
		this.out = openPrintWriter();
		if (this.out == null) {
			System.out.println("ERROR: Unable to open " + this.outputFile
					+ " -- program aborts.");
			System.exit(1);
		}
		this.hset.clear();


		if (config.getOutputFormat().compareTo("X") == 0) {
			generateXmlReport();
		} else if (config.getOutputFormat().compareTo("T") == 0) {
			generateTextReport();
		} else {
			generateDefineReport();
		}

	}
	
	/**
	 * 
	 * @return
	 */
	private PrintWriter openPrintWriter() {
		if (this.outputFile.compareTo("") == 0) {
			System.out
			.println("ERROR: Output file is not specified -- program aborts.");
			System.exit(1);
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(this.outputFile)));
			return out;
		} catch (Exception e) {
		}
		return null;
	}

	
	/**
	 * 
	 *
	 */
	private void generateTextReport() {
		// generates delimited flat file

		String header = "Code";

		if (config.getPropertyMap().size() > 0) {
			Set<String> keys = config.getPropertyMap().keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				header = header + "\t" + iter.next();
			}

		}


		if (config.getPrintParent()){
			header = header + "\tParents";
		}

		if (config.getPrintChildren()){
			header = header + "\tChildren";
		}

		this.out.println(header);

		Arrays.sort(this.RWconcepts, new RWConceptSorter());

		if (this.RWconcepts == null) {
			System.out.println("No results retrieved.");
			System.exit(0);
		}
		System.out.println("Number of concepts found: "
				+ this.RWconcepts.length);

		System.out.println("Writing to file.  This may take several minutes");

		for (int i = 0; i < this.RWconcepts.length; i++) {
			ReportWriterConcept c = this.RWconcepts[i];
			reportConceptText(c);
		}
		this.out.close();
		System.out.println("Output file " + this.outputFile + " generated.");
	}
	
	/**
	 * 
	 *
	 */
	private void generateXmlReport() {

		this.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.out.println("<!DOCTYPE EVSOutput SYSTEM \".\\EVSOutput.dtd\">");
		this.out.println("<EVSOutput>");
		Arrays.sort(this.RWconcepts, new RWConceptSorter());
		if (this.RWconcepts == null) {
			System.out.println("No concepts found for this subset "
					+ config.getSubset());
		} else {

			System.out.println("Number of concepts found: "
					+ this.RWconcepts.length);
			for (int i = 0; i < this.RWconcepts.length; i++) {
				ReportWriterConcept c = this.RWconcepts[i];
				reportConceptXML(c);
			}
			this.out.println("</EVSOutput>");
			this.out.close();
			System.out
			.println("Output file " + this.outputFile + " generated.");
		}
	}
	
	


	/**
	 * 
	 *
	 */
	private void generateDefineReport() {
		// generates tab delimited flat file

		try {
			String xmlVersion = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";

			this.out.println(xmlVersion);
			String startTag = "<CodeList OID=\"2.16.840.1.113883.3.26.1.1.1\" Name=\"NCI_Thesaurus\" DataType=\"text\">";
			String endTag = "</CodeList>";

			this.out.println(startTag);
			Arrays.sort(this.RWconcepts, new RWConceptSorter());
			// Arrays.sort(mainResultArray,new DescLogicSorter());
			if (this.RWconcepts == null) {
				System.out.println("No results retrieved.");
				System.exit(0);
			}
			System.out.println("Number of concepts found: "
					+ this.RWconcepts.length);

			System.out
			.println("Writing to file.  This may take several minutes");
			// for (int i=0; i<mainResultArray.length; i++)
			// {
			// DescLogicConcept c = mainResultArray[i];
			// reportConceptDefine(c);
			// }
			for (int i = 0; i < this.RWconcepts.length; i++) {
				ReportWriterConcept c = this.RWconcepts[i];
				reportConceptDefine(c);
				this.out.flush();
			}

			this.out.println(endTag);
			this.out.close();
			System.out
			.println("Output file " + this.outputFile + " generated.");
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			this.cat.error(e.getStackTrace());
		}
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	private boolean reportConceptDefine(ReportWriterConcept concept) {
		if (concept == null) return false;
		String startCodeTag = "<CodeListItem CodedValue=\"";
		String startNameTag = "\">\n\t<Decode>\n\t\t<TranslatedText xml:lang=\"en\">";
		String endTag = "</TranslatedText>\n\t</Decode>\n</CodeListItem>\n";
		String conceptname = concept.getName();
		String conceptcode = concept.getCode();
		String termName = "";

		if (reported(conceptname)) return false;

		boolean termExists = true;
		String error_term = "source name not found";
//		ReportWriterProperty preferredTerm = concept.getSourcePT();

//		if (preferredTerm == null) {
//			termName = error_term;
//			termExists = false;
//		} else {
//			termName = preferredTerm.getParsedValue();
//		}

		if (termExists) {
			this.out.print(startCodeTag + conceptcode + startNameTag + conceptname
					+ endTag);
		}
		this.out.println();
		this.out.flush();
		// }
		return true;
	}
	
	/**
	 * 
	 * @param conceptname
	 * @return
	 */
	private boolean reported(String conceptname) {
		if (this.hset.contains(conceptname)) return true;
		this.hset.add(conceptname);
		return false;
	}

	// term|nci code|definition
	/**
	 * 
	 * @param concept
	 * @return
	 */
	private boolean reportConceptText(ReportWriterConcept concept) {
		if (concept == null) return false;

		String conceptName = concept.getName();
		String conceptCode = concept.getCode();

		if (reported(conceptName))
			return false;
		else if (conceptCode.startsWith("@A")||conceptName.length()==0)
			// filtering out the anonymous concepts
			return false;


		this.out.print(conceptCode);
		if (config.getPropertyMap().size() > 0) {
			Set<String> keys = config.getPropertyMap().keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				//Property printing time
				String propName = iter.next();
				String propSearch = config.getPropertyMap().get(propName);
				
				String propValue = "";
				this.out.print("" + delimiter+ "");
				
				Vector<ReportWriterProperty> prop = new Vector<ReportWriterProperty>();
				String[] propArray = propSearch.split("\t");
				if (propArray.length>2){
					//this property has qualifiers.  That makes it a complex property
					//Get the property as a complex property search.
					prop = concept.getComplexProperty(propArray);
				}
				else {
					prop = concept.getProperty(propSearch);
				}
				
				Iterator<ReportWriterProperty> propIter = prop.iterator();
				while (propIter.hasNext()){
					ReportWriterProperty internalProp = propIter.next();
					propValue = propValue + internalProp.getValue() + internalDelimiter;
				}
				//trim off the last delimiter
				if (propValue.length()>2){
				    propValue = propValue.substring(0, propValue.length()-1);}
				else{
					propValue = "No "+propName+ " found";
				}
				this.out.print(propValue);
				
			}

		}


		if(config.getPrintParent()){
			//print Parents
			this.out.print(delimiter);
			Vector<ReportWriterConcept> parents = concept.getParents();
			Iterator<ReportWriterConcept> iter = parents.iterator();
			String parentString = "";
			while (iter.hasNext()){
				parentString = parentString + iter.next().getCode();
				parentString = parentString + ",";
			}
			if (parentString.length()>1){
			out.print(parentString.substring(0, parentString.length()-2));
			}
			else
			{
				out.print("No parents");
			}

		}
		//print Children
		if(config.getPrintChildren()){
			this.out.print(delimiter);
			Vector<ReportWriterConcept> children = concept.getChildren();
			Iterator<ReportWriterConcept> iter = children.iterator();
			String childrenString = "";
			while (iter.hasNext()){
				childrenString = childrenString + iter.next().getCode();
				childrenString = childrenString + "$";
			}
			if (childrenString.length()>1){
			out.print(childrenString.substring(0, childrenString.length()-2));
			}
			else
			{
				out.print("No children");
			}
		}

		this.out.println();
		this.out.flush();
		// }
		return true;
	}

	/**
	 * 
	 * @param concept
	 * @return
	 */
	private boolean reportConceptXML(ReportWriterConcept concept) {
		if (concept == null) return false;

		String conceptName = concept.getName();
		String conceptCode = concept.getCode();
		String definitionValue = "";
		String termName = "";
		String termCode = "";

		if (reported(conceptName))
			return false;
		else if (conceptCode.startsWith("@A")||conceptName.length()==0)
			// filtering out the anonymous concepts
			return false;
		
		boolean termExists = true;

		this.out.println("\t<Concept ConceptCode=\"" + conceptCode + "\">");
		this.out.println("\t\t<ConceptName xml:lang=\"en\">" + conceptName
				+ "</ConceptName>");
		
		if (config.getPropertyMap().size() > 0) {
			Set<String> keys = config.getPropertyMap().keySet();
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				//TODO Need to insert underlines to replace spaces in column names
				String propName = iter.next();
				String propSearch = config.getPropertyMap().get(propName);
				propName = propName.replace(" ", "_");
				
				String propValue = "";
				

				Vector<ReportWriterProperty> prop = new Vector<ReportWriterProperty>();
				String[] propArray = propSearch.split("\t");
				if (propArray.length>2){
					//this property has qualifiers.  That makes it a complex property
					//Get the property as a complex property search.
					prop = concept.getComplexProperty(propArray);
				}
				else {
					prop = concept.getProperty(propSearch);
				}
				Iterator<ReportWriterProperty> propIter = prop.iterator();
				while (propIter.hasNext()){
					ReportWriterProperty internalProp = propIter.next();
					propValue = propValue+"\t\t\t<"+propName+">"+internalProp.getValue() +"</"+propName+">\n";
				}
				this.out.print(propValue);
			}
		}
		
		
		
//		this.out.println("\t\t<SourceSyns>");
//		if (synExists) {
//			for (Iterator<ReportWriterProperty> iter = synonyms.iterator(); iter
//					.hasNext();) {
//				ReportWriterProperty syn = iter.next();
//
//				this.out.println("\t\t\t<SourceSyn>");
//				this.out.println("\t\t\t\t<SynValue>" + syn.getParsedValue()
//						+ "</SynValue>");
//				this.out.println("\t\t\t\t<SynType>" + syn.getType()
//						+ "</SynType>");
//				if (syn.getCode().compareTo("") != 0) {
//					this.out.println("\t\t\t\t<SynCode>" + syn.getCode()
//							+ "</SynCode>");
//				}
//				this.out.println("\t\t\t</SourceSyn>");
//			}
//		} else {
//			this.out.println("\t\t\t<SourceSyn>");
//			this.out.println("\t\t\t\t<SynValue>" + synonymValue
//					+ "</SynValue>");
//			this.out.println("\t\t\t</SourceSyn>");
//		}
//		this.out.println("\t\t</SourceSyns>");

		String term_source = "NCI";
		termExists = true;

//		if ((config.getPrintPT().compareTo("Y")) == 0
//				|| (config.getPrintPT().compareTo("O") == 0 && termExists == false)) {
//			preferredTerm = concept.getNCIPT();
//			if (preferredTerm == null) {
//				termName = error_term;
//				termExists = false;
//			} else {
//				termName = preferredTerm.getParsedValue();
//			}
//			this.out.println("\t\t<NCIName>" + termName + "</NCIName>");
//		}

//		if ((config.getPrintDef().compareTo("Y")) == 0
//				|| (config.getPrintDef().compareTo("O") == 0 && defExists == false)) {
//			definition = concept.getNciDef();
//			if (definition == null) {
//				definitionValue = error_def;
//				defExists = false;
//			} else {
//				definitionValue = definition.getParsedValue();
//			}
//			this.out.println("\t\t<NCIDef>" + scrubValue(definitionValue)
//					+ "</NCIDef>");
//		}

//		if ((config.getPrintSyn().compareTo("Y")) == 0
//				|| (config.getPrintSyn().compareTo("O") == 0 && synExists == false)) {
//			synonyms = concept.getNciSynonyms();
//			if ((synonyms == null) || (synonyms.size() == 0)) {
//				synonymValue = error_syn;
//				synExists = false;
//			}
//
//			this.out.println("\t\t<NCISyns>");
//			if (synExists) {
//				for (Iterator<ReportWriterProperty> iter = synonyms.iterator(); iter
//						.hasNext();) {
//					ReportWriterProperty syn = iter.next();
//
//					this.out.println("\t\t\t<NCISyn>");
//					this.out.println("\t\t\t\t<NCISynValue>"
//							+ syn.getParsedValue() + "</NCISynValue>");
//					this.out.println("\t\t\t\t<NCISynType>" + syn.getType()
//							+ "</NCISynType>");
//					this.out.println("\t\t\t</NCISyn>");
//				}
//			} else {
//				this.out.println("\t\t\t<NCISyn>");
//				this.out.println("\t\t\t\t<NCISynValue>" + synonymValue
//						+ "</NCISynValue>");
//				this.out.println("\t\t\t</NCISyn>");
//			}
//
//			this.out.println("\t\t</NCISyns>");
//		}

		// }
		this.out.println("\t</Concept>");
		return true;
	}
	/**
	 * This takes a definition value and removes any funky characters that could
	 * screw up the XML
	 * 
	 * @param inValue
	 * @return
	 */
	private String scrubValue(String inValue) {

		String tempString = inValue;
		tempString = tempString.replaceAll("<", "&lt;");
		tempString = tempString.replaceAll("&", "&amp;");

		return tempString;

	}
}
