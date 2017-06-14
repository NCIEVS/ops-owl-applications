package gov.nih.nci.evs.report;

/*
 * Report Writer: Developed by Northrop Grumman Information Technology (NGIT)
 */

import gov.nih.nci.evs.report.data.RWLexevsReader;
import gov.nih.nci.evs.report.data.RWOwlReader;
import gov.nih.nci.evs.report.writer.WriteReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ReportWriter {
	// static final String DEFAULT_SOURCE = "FDA";

	// static final String DEFAULT_ASSOCIATION_NAME = "Concept_In_Subset";

	// static final String ROOT_CONCEPT = "Terminology_Subset";

	static final int MATCH_LIMIT = 100000;

	ReportWriterConfiguration config;

	// private LexEVSService appService;

	// private EVSQuery evsQuery;

	private Vector<String> flags = new Vector<String>();

	private HashMap<String, String> optionMap;
	// private HashMap<String, String> propertyMap;

	private HashMap<Integer, String> selectSubset;

	// private Integer subsetKey = 0;

	private ReportWriterConcept[] RWconcepts = null;



	// CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();

	/**
	 * 
	 * @param parameters
	 */
	public ReportWriter(Vector<String> parameters) {
		// Invocation without parameters will result in a help message
		this.optionMap = getDefaultOptionMap();
		// this.propertyMap = new HashMap<String, String>();
		PropertyConfigurator.configure("conf/client_log4j.properties");

		// this.csvt.setTag("PRODUCTION");
		setFlags();
		int i = 0;
		String parameter = parameters.elementAt(i);
		char firstcharacter = parameter.charAt(0);
		if (firstcharacter == '-') {
			String flag = parameter.substring(1);
			String file_location = parameters.elementAt(i + 1);

			if (flag.compareTo("f") == 0) {
				// Read configuration data from file
				this.optionMap.put(flag, file_location);
				// System.out.println(file_location);
				readParametersFromFile();
				// initialize();
			} 
//			else if (flag.compareTo("i") == 0) {
//				// get configuration data from user
//				this.optionMap.put("f", file_location);
//				this.optionMap.put(flag, file_location);
//				readParametersFromFile();
//				requestParametersFromUser();
//			}
		} else {
			System.out
			.println("ERROR: invalid command line flag or argument input "
					+ parameter);
			showOptions();
			System.exit(1);
		}


		System.out.println("Generating report -- please wait...");

		System.out.println("Retrieving concepts from "
				+ this.config.getVocabulary());
		System.out.println("This may take several minutes ");
		
		try{
			reportConcepts();

			if (this.RWconcepts != null){
			WriteReport writer = new WriteReport(this.config, this.RWconcepts);
			}
			else {
				System.out.println("No concepts returned.  Please check config file for correct parameters");
			}
		}
		catch(Exception e){
			System.out.println("Unable to load ");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 *
	 */
	private void requestParametersFromUser() {
		// get httpURL
		String key = "U";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}
		setParameters();
		// get subset to search on

		key = "q";
		if (this.optionMap.containsKey(key)) {
			// getValueSetOptions();
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getSubset(key, value);
			} else {
				getSubset(key, null);
			}
		} else {
			getSubset(key, null);
		}

		// get output format
		key = "o";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// if the user chose Define.xml, then the NCI properties are all "N"
		if (this.optionMap.get("o").compareTo("D") == 0) {
			this.optionMap.put("p", "N");
			this.optionMap.put("d", "N");
			this.optionMap.put("s", "N");
			setParameters();
			return;
		}

		// get print NCI PT
		key = "p";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get print NCI Definition
		key = "d";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get print NCI Synonym
		key = "s";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get source to search for
		key = "c";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get whether to print parent
		key = "x";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get whether to print children
		key = "z";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get name of properties file
		key = "i";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}

		// get name of output file
		key = "n";
		if (this.optionMap.containsKey(key)) {
			String value = this.optionMap.get(key);
			if (value.compareTo("") != 0) {
				getParameter(key, value);
			} else {
				getParameter(key, null);
			}
		} else {
			getParameter(key, null);
		}
		setParameters();
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 */
	private void getSubset(String key, String defaultValue) {
		// This method presents the subset list to the user
		// and gets their input on what they want to select
		// Validate default value against available subsets
		// Validate users entry against number of items in the list
		String msg = "Available subsets to choose from: \n Num  Name";
		System.out.println(msg);
		for (int i = 0; i < this.selectSubset.size(); i++) {
			msg = String.valueOf(i) + "  " + this.selectSubset.get(i);
			System.out.println(msg);
		}
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader stdin = new BufferedReader(isr);

		String input = "";
		String ret_Subset = "";
		while (input.compareTo("") == 0) {
			msg = "\n Please enter the number for the subset you wish to choose";
			System.out.println(msg);
			if (defaultValue != null) {
				msg = "Or press enter to accept default value of ";
				System.out.println(msg);
				System.out.println(defaultValue);
			}
			msg = "Or, type 'E' to exit this application.";
			System.out.println(msg);
			msg = "Note:  choosing a subset with children will also select all the children";
			System.out.println(msg);
			boolean isValid = false;
			try {
				input = stdin.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// check if the user entered a quit symbol
			String case_input = input.toUpperCase();
			if (case_input.compareTo("E") == 0) {
				System.exit(0);
			}

			// check if the user selected the default
			input.trim();

			if (input.compareTo("") == 0 && defaultValue != null) {
				ret_Subset = defaultValue;
				input = "0";
				isValid = true;
			} else {
				Integer intInput = new Integer(input);
				if (this.selectSubset.containsKey(intInput))
					// check if input is a match to one of our numbers
				{
					isValid = true;
					ret_Subset = this.selectSubset.get(intInput);
				}
			}
			// If input value is invalid, loop back to the top
			if (!isValid) {
				input = "";
				msg = "I'm sorry.  Your input was invalid";
				System.out.println(msg);
			}

		}

		// add the trimmed subset name to the optionMap

		this.optionMap.put(key, ret_Subset.trim());
	}

	// private boolean getValueSetOptions() {
	// LexEVSValueSetDefinitionServices vsd_service = this.appService
	// .getLexEVSValueSetDefinitionServices();
	// boolean successful = false;
	// // List list = vsd_service.listValueSetDefinitionURIs();
	//
	// @SuppressWarnings("rawtypes")
	// List evsResults = vsd_service.listValueSetDefinitionURIs();
	// DefaultMutableTreeNode ret_DefaultMutableTreeNode = null;
	// // evsResults = (List) appService.evsSearch(evsQuery);
	// if (evsResults != null && evsResults.size() != 0) {
	// // Object[] objs = evsResults.toArray();
	// ret_DefaultMutableTreeNode = (DefaultMutableTreeNode) evsResults
	// .get(0);
	// loadSubsetNode(ret_DefaultMutableTreeNode, 0);
	// loadSubsetTree(ret_DefaultMutableTreeNode, 1);
	// successful = true;
	// }
	// return successful;
	//
	// }

	// /**
	// *
	// * @return
	// */
	// private boolean getSubsetOptions() {
	// selectSubset = new HashMap<Integer, String>();
	// EVSQuery evsQuery = new EVSQueryImpl();
	// boolean successful = false;
	// boolean direction = true; // navigate down
	// boolean isA = true;
	// int ASD = 0; // get no properties or roles
	// int levels = -1; // get all levels
	// Vector<String> roles = new Vector<String>();
	// roles.add("");
	// // evsQuery.getChildConcepts(vocabulary,rootSubset,false);
	// evsQuery.getTree(vocabulary, rootSubset, direction, isA, ASD, levels,
	// roles);
	// try {
	// List evsResults = new ArrayList();
	// DefaultMutableTreeNode ret_DefaultMutableTreeNode = null;
	// evsResults = (List) appService.evsSearch(evsQuery);
	// if (evsResults != null && evsResults.size() != 0) {
	// Object[] objs = evsResults.toArray();
	// ret_DefaultMutableTreeNode = (DefaultMutableTreeNode) evsResults
	// .get(0);
	// loadSubsetNode(ret_DefaultMutableTreeNode, 0);
	// loadSubsetTree(ret_DefaultMutableTreeNode, 1);
	// successful = true;
	// }
	//
	// } catch (ApplicationException e) {
	// successful = false;
	// e.printStackTrace();
	// }
	// return successful;
	// // get node of available subsets from caCORE
	// // this will have the effect of validating the URL
	// // Present list to user as an outline
	// // number each item on the list
	// // Validate users entry against number of items in the list
	// }
	//

	/**
	 * 
	 * @param key
	 * @param default_value
	 */
	private void getParameter(String key, String default_value) {
		// Display value as default for entry
		boolean param_avail = false;
		while (!param_avail) {
			String value = promptUserForParameter(key, default_value);
			if (key.compareTo("U") != 0 && key.compareTo("n") != 0) {
				value = value.toUpperCase();
			}
			if (value.compareTo("") != 0) {
				param_avail = true;
				this.optionMap.put(key, value);
			}
		}
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private String promptUserForParameter(String key, String value) {
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader stdin = new BufferedReader(isr);

		String input = "";
		while (input.compareTo("") == 0) {
			String msg = "\nPlease specify " + getParamterDescription(key);
			System.out.println(msg);
			if (value != null) {
				msg = "Or press enter to accept default value of :\n" + value;
				System.out.println(msg);
			}
			msg = "Or, type 'E' to exit this application.";
			System.out.println(msg);

			boolean isValid = false;
			try {
				input = stdin.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}

			input.trim();
			// check if the user entered a quit symbol
			if (key != "U" && key != "n") {
				input = input.toUpperCase();
			}
			if (input.compareTo("E") == 0 || input.compareTo("e") == 0) {
				System.exit(0);
			}

			// check if the user selected the default

			if (input.compareTo("") == 0 && value != null) {
				input = value;
				isValid = true;
			} else {
				isValid = validateInput(key, input);
			}

			// If input value is invalid, loop back to the top
			if (!isValid) {
				input = "";
				msg = "I'm sorry.  Your input was invalid";
				System.out.println(msg);
			}
		}
		return input;
	}

	/**
	 * 
	 * @param key
	 * @param input
	 * @return
	 */
	private boolean validateInput(String key, String input) {
		boolean isValid = false;

		String[] validValues = getValidValues(key);
		for (int i = 0; i < validValues.length; i++) {
			if (validValues[i].compareTo(input.toUpperCase()) == 0) {
				isValid = true;
			}
			if (validValues[i].compareTo("Not applicable") == 0) {
				isValid = true;
			}
		}

		return isValid;
	}

	/**
	 * 
	 *
	 */
	private void setFlags() {
		this.flags.add("U"); // URL
		this.flags.add("q"); // queried subset
		this.flags.add("p"); // print NCI properties
		this.flags.add("d"); // print NCI definitions
		this.flags.add("s"); // print NCI synonyms
		this.flags.add("o"); // output format
		this.flags.add("n"); // name of output file
		this.flags.add("f"); // used to pass in location of defaults file.
		// Optional

	}

	/**
	 * 
	 * @param name2val_hashmap
	 * @param node
	 */
	private void traverse(HashMap<String, String> name2val_hashmap, Node node) {
		int type = node.getNodeType();
		if (type == Node.ELEMENT_NODE) {
			String nodename = node.getNodeName();
			NodeList children = node.getChildNodes();
			if (children != null && children.item(0) != null) {
				String nodevalue = children.item(0).getNodeValue();
				if (nodevalue != null) {
					name2val_hashmap.put(nodename, nodevalue);
				}
			}
		}
		NodeList children = node.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				traverse(name2val_hashmap, children.item(i));
			}
		}
	}

	/**
	 * 
	 * @param xml
	 * @return
	 */
	private HashMap<String, String> parseXML(String xml) {
		xml = "<?xml version=\"1.0\"?><fake>" + xml + "</fake>";
		HashMap<String, String> name2val_hashmap = new HashMap<String, String>();

		try {
			DOMParser parser = new DOMParser();
			BufferedReader reader = new BufferedReader(new StringReader(xml));
			InputSource inputsource = new InputSource(reader);
			parser.parse(inputsource);
			Document document = parser.getDocument();
			traverse(name2val_hashmap, document);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name2val_hashmap;
	}

	/**
	 * 
	 *
	 */
	private void setParameters() {

		// this.propMapLocation = this.optionMap.get("i");
		HashMap<String, String> propertyMap = readPropertiesFromFile();
		this.config = new ReportWriterConfiguration(this.optionMap, propertyMap);
	}



	private Vector<ReportWriterConcept> searchReportWriterConcepts() {
		Vector<ReportWriterConcept> ret_RWConcept_array = new Vector<ReportWriterConcept>();

		// At this point call to the Reader to get the ReportWriterConcept

		try{
		if (config.isLoadTypeOwl()){
			RWOwlReader reader = new RWOwlReader(this.config);
			ret_RWConcept_array = reader.searchReportWriterConcepts();
		}
		else {
			// if setup is LexevsReader
			RWLexevsReader reader = new RWLexevsReader(this.config);
			ret_RWConcept_array = reader.searchReportWriterConcepts();
		}}
		catch(Exception e){
			System.out.println("Unable to open data source. Please check the URL \"U:\" parameter ");
			System.exit(0);
		}

		return ret_RWConcept_array;
	}

	/**
	 * 
	 * @param subsetName
	 */
	private void reportConcepts() {


		Vector<ReportWriterConcept> rwcV = searchReportWriterConcepts();

		if (rwcV != null) {
			addToConcepts(rwcV);
		}


	}

	private void addToConcepts(Vector<ReportWriterConcept> input) {
		int mainSize = 0;
		if (this.RWconcepts == null) {
			this.RWconcepts = new ReportWriterConcept[input.size()];
		} else {
			mainSize = this.RWconcepts.length;
			int mainIterator = this.RWconcepts.length + input.size();
			this.RWconcepts = resizeArray(this.RWconcepts, mainIterator);
		}
		Iterator<ReportWriterConcept> iter = input.iterator();
		int i = 0;
		while (iter.hasNext()) {
			this.RWconcepts[mainSize + i] = iter.next();
			i++;
		}

		// for (int i = 0; i < inputArray.length; i++) {
		// RWconcepts[mainSize + i] = inputArray[i];
		// }
	}

	/**
	 * 
	 * @param oldArray
	 * @param newSize
	 * @return
	 */
	private static ReportWriterConcept[] resizeArray(
			ReportWriterConcept[] oldArray, int newSize) {
		int oldSize = oldArray.length;

		ReportWriterConcept[] newArray = new ReportWriterConcept[newSize];
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) {
			System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		}
		return newArray;
	}

	/**
	 * 
	 *
	 */
	public static void outputFlags() {
		Vector<String> flags = new Vector<String>();
		flags.add("U");
		flags.add("q");
		flags.add("o");
		flags.add("p");
		flags.add("d");
		flags.add("s");
		flags.add("i");
		for (int i = 0; i < flags.size(); i++) {
			String flag = flags.elementAt(i);
			System.out
			.println("-" + flag + ": " + getParamterDescription(flag));
		}
	}

	/**
	 * 
	 */
	public static void showOptions() {
		System.out.println("");
		System.out.println("Usage: java ReportWriter -f \"config file location\"");
//		System.out.println("    Or java ReportWriter -i");
//		System.out.println("Please refer to the documentation for details.\n");
	}

	/**
	 * 
	 * @return
	 */
	private static HashMap<String, String> getDefaultOptionMap() {

		HashMap<String, String> optionMap = new HashMap<String, String>();
		optionMap.put("U", "");
		optionMap.put("q", "");
		optionMap.put("owl", "");
		optionMap.put("o", "");
		optionMap.put("p", "");
		optionMap.put("d", "");
		optionMap.put("s", "");
		optionMap.put("i", "");
		optionMap.put("o", "");
		optionMap.put("par", "");
		optionMap.put("chi", "");

		return optionMap;
	}

	/**
	 * 
	 * @param flag
	 * @return
	 */
	public static String getParamterDescription(String flag) {
		if (flag.compareTo("U") == 0)
			return ("The URL location of the caCORE server to query (required)");
		else if (flag.compareTo("q") == 0)
			return ("URI of subset to query");
		else if (flag.compareTo("c") == 0)
			return ("Source to query");
		else if (flag.compareTo("o") == 0)
			return ("output format: D for CDISC define.xml, T for text, X for XML");
		else if (flag.compareTo("p") == 0)
			return ("Print NCI Preferred Term: [Y]es, [N]o, [O]nly if no source preferred term");
		else if (flag.compareTo("d") == 0)
			return ("Print NCI Definition: [Y]es, [N]o, [O]nly if no source definition");
		else if (flag.compareTo("s") == 0)
			return ("Print NCI Synonym: [Y]es, [N]o, [O]nly if no source synonyms");
		else if (flag.compareTo("n") == 0)
			return (" desired name for output file");
		else if (flag.compareTo("f") == 0)
			return ("Use configuration file");
		else if (flag.compareTo("i") == 0)
			return ("Use properties file");
		else if (flag.compareTo("x") == 0)
			return ("Display parent: [Y]es, [N]o");
		else if (flag.compareTo("z") == 0)
			return ("Display children: [Y]es, [N]o");
		return "";
	}

	/**
	 * 
	 * @param flag
	 * @return
	 */
	public static String[] getValidValues(String flag) {
		String[] values = { "", "", "" };
		if (flag.compareTo("U") == 0) {
			values[0] = "Not applicable";
		} else if (flag.compareTo("q") == 0) {
			values[0] = "Not applicable";
		} else if (flag.compareTo("o") == 0) {
			values[0] = "D";
			values[1] = "T";
			values[2] = "X";
		} else if (flag.compareTo("p") == 0) {
			values[0] = "Y";
			values[1] = "N";
			values[2] = "O";
		} else if (flag.compareTo("d") == 0) {
			values[0] = "Y";
			values[1] = "N";
			values[2] = "O";
		} else if (flag.compareTo("s") == 0) {
			values[0] = "Y";
			values[1] = "N";
			values[2] = "O";
		} else if (flag.compareTo("n") == 0) {
			values[0] = "Not applicable";
		} else if (flag.compareTo("par") == 0) {
			values[0] = "Y";
			values[1] = "N";
		} else if (flag.compareTo("chi") == 0) {
			values[0] = "Y";
			values[1] = "N";
		} else if (flag.compareTo("owl")==0){
			values[0] = "Y";
			values[1] = "N";
		} else if (flag.compareTo("d1")==0){
			values[0] = "Not applicable";
		} else if (flag.compareTo("d2")==0){
			values[0] = "Not applicable";
		} else if (flag.compareTo("c")==0){
			values[0] = "Not applicable";
		} else if (flag.compareTo("i")==0){
			values[0] = "Not applicable";
		}
		return values;
	}

	/**
	 * 
	 */
	private void readParametersFromFile() {
		String filename = "";
		try {
			filename = this.optionMap.get("f");
			filename.trim();
			if (filename.compareTo("") == 0) {
				System.out
				.println("WARNING: configuration file is not specified, it is ignored.");
			}
			// System.out.println("filename in method" + filename);
			BufferedReader inFile = new BufferedReader(new FileReader(filename));
			// System.out.println("file reader opened");
			String line = "EOF";
			while ((line = inFile.readLine()) != null) { // System.out.println("Made
				// it into the while
				// loop");
				// System.out.println(line);
				char firstcharacter = '#';
				if (line.length() > 0) {
					firstcharacter = line.charAt(0);
				}
				// System.out.println("comment found");
				if (firstcharacter != '#') {
					int n = line.indexOf(":");
					if (n != -1) {
						String flag = line.substring(0, n);
						String parameter = line.substring(n + 1, line.length());
						parameter.trim();
						// if (parameter.length()>0 && flag.length()>0 &&
						// !optionMap.containsKey(flag))
						if (parameter.length() > 0 && flag.length() > 0) {
							if (validateInput(flag, parameter)){
							this.optionMap.put(flag, parameter);}
							else {
								System.out.println("Invalid input in config file. "+ flag + ":" + parameter);
								System.exit(0);
							}
						}
					}
				}
			}

			inFile.close();
			setParameters();

		} catch (Exception e) {
			System.out.println("WARNING: Unable to open " + filename
					+ " -- configuration file ignored.");
			e.printStackTrace();
		}

	}

	private HashMap<String, String> readPropertiesFromFile() {
		String filename = "";
		HashMap<String, String> propertyMap = new LinkedHashMap<String, String>();
		try {
			filename = this.optionMap.get("i");
			filename.trim();
			if (filename.compareTo("") == 0) {
				System.out
				.println("WARNING: properties file is not specified, it is ignored.");
			}
			BufferedReader inFile = new BufferedReader(new FileReader(filename));
			String line = "EOF";
			while ((line = inFile.readLine()) != null) { // System.out.println("Made
				// it into the while
				// loop");
				// System.out.println(line);
				char firstcharacter = '#';
				if (line.length() > 0) {
					firstcharacter = line.charAt(0);
				}

				if (firstcharacter != '#') {
					int n = line.indexOf(":");
					if (n != -1) {
						String flag = line.substring(0, n);
						String parameter = line.substring(n + 1, line.length());
						parameter = parameter.trim();
						// if (parameter.length()>0 && flag.length()>0 &&
						// !optionMap.containsKey(flag))
						if (parameter.length() > 0 && flag.length() > 0) {
							propertyMap.put(flag, parameter);
						}
					}
				}
			}

			inFile.close();
			return propertyMap;

		} catch (Exception e) {
			System.out.println("WARNING: Unable to open " + filename
					+ " -- properties file ignored.");
//			e.printStackTrace();
			return propertyMap;
		}
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// -f C:\eclipse\workspace\ReportWriter\conf\config.dat
		// -i
		if (args.length == 0) {
			showOptions();
			System.exit(0);
		}
		Vector<String> parameters = new Vector<String>();
		for (int i = 0; i < args.length; i++) {
			parameters.add(args[i]);
		}
		ReportWriter reportwriter = new ReportWriter(parameters);
	}



	/**
	 * 
	 * @author Tracy M Safran
	 * 
	 */
	public static class RWConceptSorter implements
	Comparator<ReportWriterConcept> {
		@Override
		public int compare(ReportWriterConcept obj1, ReportWriterConcept obj2) {
			ReportWriterConcept concept1 = obj1;
			ReportWriterConcept concept2 = obj2;
			int result = 0;
			String str1 = " ";
			String str2 = "  ";
			if (concept1.getName() != null) {
				str1 = concept1.getName();
			}
			if (concept2.getName() != null) {
				str2 = concept2.getName();
			}

			result = str1.compareTo(str2);
			return result;
		}

	}

}
