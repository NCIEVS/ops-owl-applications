/* Generate CDISC reports from the Protege OWL export.
 * NCI/EVS with CDISC
 * Robert W. Wynne II (Medical Science & Computing)
 */

package gov.nih.nci.evs.cdisc;


import gov.nih.nci.evs.owl.data.OWLKb;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;


//import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.reportwriter.formatter.AsciiToExcelFormatter;

public class GenerateCDISC {
	
	OWLKb kb = null;
	private final String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	private static String gTermSource="CDISC";
	boolean checkShortNameLength = true;
	
	/**
	 * @param args OWL file location and report root concept
	 */
	public static void main(String args[]) {

		GenerateCDISC report = new GenerateCDISC();
		long start = System.currentTimeMillis();		
		System.out.println("Initializing OWLKb...");
		report.init(args[0]);
		System.out.println("Generating report...");
		if (args.length==3){
			gTermSource = args[2];
		}
		//TODO: Iterate over a list of reports to generate	
		report.generate(args[1]);
		System.out.println("Finished report in "
		        + (System.currentTimeMillis() - start) / 1000 + " seconds.");		
	}
	
	/**
	 * @param filename
	 */
	public void init(String filename) {
		kb = new OWLKb(filename, namespace);
	}
	
	public URI createURI(String code) {
		URI uri = null;
		try {
			uri = new URI(namespace + "#" + code);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;	
	}
	
	/**
	 * @param root
	 * 			The root concept to report on
	 */
	public void generate(String root) {
		PrintWriter pw = null;
		File fil = null;
		ConceptProxy conceptProxy = kb.getConcept(createURI(root));
		Property PN = conceptProxy.getProperty("P108");
		String rootName = PN.getValue();
//		String rootName = kb.getConcept(createURI(root)).getProperty("P108").getValue();
		
		//TODO: This may become CDISC_COA_Terminology - which is now referred to as QRS (Preferred_Name change)
		if( root.equals("CDISC_Questionnaire_Terminology") || root.equals("CDISC_Functional_Test_Terminology") || 
				root.equals("CDISC_Clinical_Classification_Terminology") || root.equals("CDISC_COA_Terminology")) {
			checkShortNameLength = false;
		}
		
		try {
			fil = new File(rootName + ".txt");
			pw = new PrintWriter(fil);
		} catch(Exception e) {
			System.out.println("Couldn't create output file.");
			System.exit(0);
		}		
		
		URI rootURI = null;
		try {
			rootURI = new URI(namespace + "#" + root);
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		HashMap<String,String> codelist2NCIAB = new HashMap<String,String>();
		HashMap<String,String> codelist2NCIPT = new HashMap<String,String>();
		HashMap<String,String> codelist2CDISCPT = new HashMap<String,String>();
		HashMap<String,String> codelist2CDISCSY = new HashMap<String,String>();
		TreeMap<String,String> cdiscsy2Codelist = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER); 
		HashMap<String,String> codelist2Extensible = new HashMap<String,String>();
		HashMap<String,String> codelist2Code = new HashMap<String,String>();
		HashMap<String,String> codelist2Def = new HashMap<String,String>();
		HashMap<String,ArrayList<String>> codelist2Elements = new HashMap<String,ArrayList<String>>();
		Vector<URI> codelistConcepts = kb.getAllDescendantsForConcept(rootURI);
		
		//Glossary has no children
		if(codelistConcepts.size()==0){
			codelistConcepts.add(rootURI);
		}
		codelistConcepts.remove("Nothing");


		
		for(URI codelistConcept : codelistConcepts ) {
			// System.out.println(codelistConcept);
//			Vector<String> synonyms = kb.getPropertyValues(codelistConcept, "FULL_SYN");
			Vector<Property> synonyms = kb.getConcept(codelistConcept).getProperties("P90");
			boolean hasSyn=false;
			String syn="";
			for( Property synonym : synonyms ) {
				String termName = synonym.getValue();
				String termSource = "";
				String termGroup = "";
				Vector<Qualifier> quals = synonym.getQualifiers();
				for( Qualifier qual : quals ) {
					if( qual.getName().equals("Term Source")) {
						termSource = qual.getValue();
					}
					if( qual.getName().equals("Term Type")) {
						termGroup = qual.getValue();
					}
				}
				if( termSource.equals("NCI") && termGroup.equals("PT") ) {
					codelist2NCIPT.put(codelistConcept.getFragment(), termName);
				}
				if( termSource.equals("NCI") && termGroup.equals("AB") ) {
					codelist2NCIAB.put(codelistConcept.getFragment(), termName);
				}

				//Should only take PT or SY, not both.

				if( termSource.equals(gTermSource) && termGroup.equals("PT")) {
					codelist2CDISCPT.put(codelistConcept.getFragment(), termName);
					//Per Erin: codelist submission value cannot be more than 8 characters in length
					if( checkShortNameLength && termName.length() > 8 ) {
						System.out.println("Warning: Codelist Submission Value (CDISC PT) over 8 characters - " + codelistConcept + " (" + termName + ")");
					}
					if( !cdiscsy2Codelist.containsKey(termName) && !hasSyn) {
						cdiscsy2Codelist.put(termName, codelistConcept.getFragment());
						hasSyn=true;
						syn=termName;
					}
					else {
						System.out.println("There was an issue adding synonym " + termName);
					}
				}
				if( termSource.equals(gTermSource) && termGroup.equals("SY") ) {
						codelist2CDISCSY.put(codelistConcept.getFragment(), termName);
					if( !cdiscsy2Codelist.containsKey(termName)) {
						//CDISC SY has precedence over PT (per editor).  If PT loaded, remove it.
						if(hasSyn){
							cdiscsy2Codelist.remove(syn);
							cdiscsy2Codelist.put(termName, codelistConcept.getFragment());
							hasSyn = true;
						}else {
							cdiscsy2Codelist.put(termName, codelistConcept.getFragment());
							hasSyn = true;
						}
					}
					else {
						System.out.println("There was an issue adding synonym " + termName);
					}
				}				
			}
			
			Vector<Property> exLists = kb.getConcept(codelistConcept).getProperties("P361");		
			//Vector<String> exLists = kb.getPropertyValues(codelistConcept, "Extensible_List");
			if( exLists.size() == 0 ) {
				System.out.println("No Extensible_List!\n\tCodelist concept: " + codelistConcept);
			}
			else if( exLists.size() > 1 ) {
				System.out.println("Multiple Extensible_List!\n\tCodelist concept: " + codelistConcept);
			}
			else {
				codelist2Extensible.put(codelistConcept.getFragment(), exLists.elementAt(0).getValue());
			}
			
			boolean foundDef = false;
			for( Property altDef : kb.getConcept(codelistConcept).getProperties("P325") ) {
				Vector<Qualifier> quals = altDef.getQualifiers();
				for( Qualifier qual : quals ) {
					//def-source
					if( qual.getName().equals("Definition Source") && qual.getValue().equals("CDISC")) {
						codelist2Def.put(codelistConcept.getFragment(), altDef.getValue());
						foundDef = true;
					}
				}
			}
			if( !foundDef ) {
				System.out.println("No CDISC Definition!\n\tCodelist concept: " + codelistConcept);
			}
			
			codelist2Code.put(codelistConcept.getFragment(), kb.getConcept(codelistConcept).getCode());
			

		}
		
		System.out.println("Done phase 1");


		
		HashMap<URI,ConceptProxy> concepts =  kb.getAllConcepts();
		int m = 0;
		for( URI concept : concepts.keySet()) {
			m++;
			if( m % 100 == 0 ) {
//				System.out.println(m + " processed");
			}
			try{
			Vector<Association> assocs = kb.getAssociationsForSource(concept);
			

			for(Relationship assoc : assocs ) {

				if (assoc.getName().equals("Concept_In_Subset")) {
					String element = assoc.getSource().getCode();
					String codelistId = assoc.getTarget().getCode();
					URI codelistURI = createURI(codelistId);
					if( codelistConcepts.contains(codelistURI) ) {
						if( codelist2Elements.containsKey(codelistId) ) {
							ArrayList<String> tmp = codelist2Elements.get(codelistId);
							if( !tmp.contains(element) ) {
								tmp.add(element);
							}
							codelist2Elements.put(codelistId, tmp);
						}
						else {
							ArrayList<String> tmp = new ArrayList<String>();
							tmp.add(element);							
							codelist2Elements.put(codelistId, tmp);
						}
					}
				}			
			}} catch (Exception e){
				System.out.println("Error in "+ concept.toString());
				e.printStackTrace();
			}
		}
		
		System.out.println("Done phase 2");

		
		
		String[] header = { "Code", "Codelist Code", "Codelist Extensible (Yes/No)", "Codelist Name", "CDISC Submission Value", "CDISC Synonym(s)", "CDISC Definition", "NCI Preferred Term"};
		for( int i = 0; i < header.length; i++ ) {
			pw.print(header[i]);
			if( i + 1 < header.length ) pw.print("\t");
			else pw.print("\n");
		}
		
//		sheet.createFreezePane(0, 1, 0, 1);		
		
		for( String codelistName : cdiscsy2Codelist.keySet() ) {
			String codelistConcept = cdiscsy2Codelist.get(codelistName);
					
			
			pw.print(codelist2Code.get(codelistConcept) + "\t");
			pw.print("" + "\t");
			pw.print(codelist2Extensible.get(codelistConcept) + "\t");
			pw.print(codelist2CDISCSY.get(codelistConcept) + "\t");
			pw.print(codelist2CDISCPT.get(codelistConcept) + "\t");
			pw.print(codelist2CDISCSY.get(codelistConcept) + "\t");
			pw.print(codelist2Def.get(codelistConcept) + "\t");
			pw.print(codelist2NCIPT.get(codelistConcept) + "\n");
			
			ArrayList<String> elements = codelist2Elements.get(codelistConcept);

			System.out.println(codelistName);
			if(codelistName.equals("Pool for Integration")){
				String debug="stop here";
			}

			if(elements!=null) {
				//Don't report retired elements
				for (int i = 0; i < elements.size(); i++) {
					URI elementURI = null;
					try {
						elementURI = new URI(namespace + "#" + elements.get(i));
					}
					catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (kb.isDeprecated(elementURI)) {
						elements.remove(i);
					}
				}

//			TreeMap<String,String> submission2Element = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
				TreeMap<String, String> submission2Element = new TreeMap<String, String>();
				HashMap<String, String> element2Code = new HashMap<String, String>();
				HashMap<String, String> element2CodelistCode = new HashMap<String, String>();
				HashMap<String, ArrayList<String>> element2Synonyms = new HashMap<String, ArrayList<String>>();
				HashMap<String, String> element2Definition = new HashMap<String, String>();
				HashMap<String, String> element2PreferredName = new HashMap<String, String>();

				for (String element : elements) {
					URI elementConcept = null;
					try {
						elementConcept = new URI(namespace + "#" + element);
					}
					catch (URISyntaxException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					element2Code.put(element, kb.getConcept(elementConcept).getCode());
					element2CodelistCode.put(element, codelist2Code.get(codelistConcept));
					element2PreferredName.put(element, kb.getConcept(elementConcept).getProperty("P108").getValue());

					Vector<Property> submissionValues = new Vector<Property>();
					ArrayList<String> cdiscSynonyms = new ArrayList<String>();

					Vector<Property> synonyms = kb.getConcept(elementConcept).getProperties("P90");
					for (Property synonym : synonyms) {
						String cdiscSy = synonym.getValue();
						String termSource = "";
						String termGroup = "";
						Vector<Qualifier> quals = synonym.getQualifiers();
						for (Qualifier qual : quals) {
							if (qual.getName().equals("Term Source")) {
								termSource = qual.getValue();
							}
							if (qual.getName().equals("Term Type")) {
								termGroup = qual.getValue();
							}
						}
						if ((termSource.equals(gTermSource)) && termGroup.equals("PT")) {
							submissionValues.add(synonym);
						}
						if ((termSource.equals(gTermSource)) && termGroup.equals("SY")) {
							cdiscSynonyms.add(cdiscSy);
						}
					}

					Collections.sort(cdiscSynonyms);
					element2Synonyms.put(element, cdiscSynonyms);
					if (submissionValues.size() > 1) {
						boolean found = false;
						for (Property possibleSubmissionValue : submissionValues) {
							Vector<Qualifier> quals = possibleSubmissionValue.getQualifiers();
							for (Qualifier qual : quals) {
								if (qual.getName().equals("Source Code") && qual.getValue().equals(codelist2NCIAB.get(codelistConcept))) {
									submission2Element.put(possibleSubmissionValue.getValue(), element);
									found = true;
								}
							}
//						if( possibleSubmissionValue.contains("<ncicp:source-code>" + codelist2NCIAB.get(codelistConcept) + "</ncicp:source-code>") ) {
//							submission2Element.put(getQualVal(possibleSubmissionValue, "ncicp:term-name"), element);
//							found = true;
//						}
						}
						if (!found) {
							System.out.print("No submission value!\n\tCodelist concept: " + codelistConcept + "\n\tElement concept: " + element + "\n");
						}
					} else try {
						Property submissionValue = submissionValues.elementAt(0);
						submission2Element.put(submissionValue.getValue(), element);
					}
					catch (Exception e) {
						System.out.print("No submission value!\n\tCodelist concept: " + codelistConcept + "\n\tElement concept: " + element + "\n");
					}


					Vector<Property> definitions = kb.getConcept(elementConcept).getProperties("P325");
					for (Property definition : definitions) {
						Vector<Qualifier> quals = definition.getQualifiers();
						for (Qualifier qual : quals) {
							if (qual.getName().equals("Definition Source") && (qual.getValue().equals(gTermSource))) {
								element2Definition.put(element, definition.getValue());
							}
						}
//					if( definition.contains("<ncicp:def-source>CDISC</ncicp:def-source>") ) {
//						element2Definition.put(element, getQualVal(definition, "ncicp:def-definition"));
//					}
					}
				}

				//check if submission2Element is null or empty
				ArrayList<String> keys = new ArrayList<String>();
				keys.addAll(submission2Element.keySet());
				Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

				for (String submission : keys) {
					ArrayList<String> cdiscSynonyms = element2Synonyms.get(submission2Element.get(submission));
					String cellFormattedSynonyms = new String("");
					for (int i = 0; i < cdiscSynonyms.size(); i++) {
						cellFormattedSynonyms = cellFormattedSynonyms.concat(cdiscSynonyms.get(i));
						if (i + 1 < cdiscSynonyms.size()) cellFormattedSynonyms = cellFormattedSynonyms.concat("; ");
					}


					pw.print(element2Code.get(submission2Element.get(submission)) + "\t");
					pw.print(element2CodelistCode.get(submission2Element.get(submission)) + "\t");
					pw.print("" + "\t");
					pw.print(codelistName + "\t");
					pw.print(submission + "\t");
					pw.print(cellFormattedSynonyms);
					pw.print("\t" + element2Definition.get(submission2Element.get(submission)) + "\t");
					pw.print(element2PreferredName.get(submission2Element.get(submission)) + "\n");
				}

			}
			
		}		

		
		pw.close();
		
		AsciiToExcelFormatter formatter = new AsciiToExcelFormatter();
		try {
			formatter.convert(fil.toString(), "\t", fil.toString().replace(".txt", ".xls"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	/**
	 * @param fs Entire complex property
	 * @param qualName The qualifier name we want the value for
	 * @return qualifier value
	 */
	public String getQualVal(String fs, String qualName) {
		String val = new String("");
		int qualLength = qualName.length();
		qualLength += 2;
		int begin = fs.lastIndexOf("<" + qualName + ">");
		int last = fs.indexOf("</" + qualName + ">");
		val = fs.substring(begin+qualLength, last);
		val = val.trim().replace("<![CDATA[", "").replace("]]>", "");
		return val;
	}


}
