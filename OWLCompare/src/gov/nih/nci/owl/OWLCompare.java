/*
 * Rob Wynne, LMCO
 * 
 * OWL input comes directly from Protege.
 * 
 */

package gov.nih.nci.owl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLCompare {

	OWLOntologyManager manager;
	OWLOntology ontology;
	String ontologyNamespace;

	String inputOWL = null;
	URI physicalURI = null;
	boolean ignoreRetired = true;
	boolean useStemming = true;
	boolean useToken = false;
	boolean allowDuplicates = true;
	boolean compareActiveOnly = false;
	boolean generateExcel = false;
	String configFile = new String("./config/owlcompare.properties");
	String matchFile = new String("./input.txt");
	String propertyMatchFile = new String("./propertyMatch.txt");
	String propertyPrintFile = new String("./propertyOutput.txt");
	String pwFile = new String("./output.txt");
	String classMatchDelimiter = new String("\t");
	private final String delimiters = new String("| ,-(){}[]*&@\\\t/.:;!?_\"'");
	PrintWriter pw;
	TreeMap<String, ArrayList<OWLClass>> compareMap = new TreeMap<String, ArrayList<OWLClass>>(
	        String.CASE_INSENSITIVE_ORDER);
	TreeMap<String, String> compareMapStemmed = new TreeMap<String, String>(
	        String.CASE_INSENSITIVE_ORDER);
	TreeMap<String, ArrayList<OWLClass>> propertiesAndClasses = new TreeMap<String, ArrayList<OWLClass>>();
	Vector<Vector<String>> propertiesToMatch = new Vector<Vector<String>>();
	Vector<String> propertiesToPrint = new Vector<String>();
	Vector<String> allInput = new Vector<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OWLCompare compare = new OWLCompare();
		long start = System.currentTimeMillis();
		compare.configure(args);
		long start2 = System.currentTimeMillis();
		compare.runCompare();
		System.out.println("Finished compare in "
		        + (System.currentTimeMillis() - start2) / 1000 + " seconds.");
		System.out.println("Total with OWL load "
		        + (System.currentTimeMillis() - start) / 1000 + " seconds.");
	}

	public void printHelp() {
		System.out.println("");
		System.out
		        .println("Usage: OWLCompare [OPTIONS] ... [OWL] [OUTPUT FILE]");
		System.out.println(" ");
		System.out
		        .println("  -A, --active\t\tcompare active content (ignore retired concepts)");
		System.out
		        .println("  -D, --ignoreDups\tignore duplicates (sorted output)");
		System.out.println("  -E, --excel\t\tgenerate Excel output");
		System.out.println("  -I, --input\t\tpath to match input text file");
		System.out
		        .println("  -M, --propertyMatch\tpath to property match config file");
		System.out
		        .println("  -P, --propertyPrint\tpath to property print config file");
		System.out
		        .println("  -S, --simpleMatching\tstrict case insensitive (simple) matching");
		System.out.println();
		System.out
		        .println("  -T, --tokenMatch\tuse tokenized strings without stemming");
		System.exit(1);
	}

	public void configure(String[] args) {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String option = args[i];
				if (option.equalsIgnoreCase("--help")) {
					printHelp();
				}
				if (option.equalsIgnoreCase("-A")
				        || option.equalsIgnoreCase("--active")) {
					compareActiveOnly = true;
				}
				if (option.equalsIgnoreCase("-C")
				        || option.equalsIgnoreCase("--delimiter")) {
					classMatchDelimiter = "\n";
				}
				if (option.equalsIgnoreCase("-D")
				        || option.equalsIgnoreCase("--ignoreDups")) {
					allowDuplicates = false;
				}
				if (option.equalsIgnoreCase("-E")
				        || option.equalsIgnoreCase("--excel")) {
					generateExcel = true;
				}
				if (option.equalsIgnoreCase("-M")
				        || option.equalsIgnoreCase("--propertyMatchFile")) {
					propertyMatchFile = args[++i];
				}
				if (option.equalsIgnoreCase("-I")
				        || option.equalsIgnoreCase("--input")) {
					matchFile = args[++i];
				}
				if (option.equalsIgnoreCase("-P")
				        || option.equalsIgnoreCase("--propertyPrintFile")) {
					propertyPrintFile = args[++i];
				}
				if (option.equalsIgnoreCase("-Z")
				        || option.equalsIgnoreCase("--includeRetired")) {
					ignoreRetired = false;
				}
				if (option.equalsIgnoreCase("-S")
				        || option.equalsIgnoreCase("--simpleMatching")) {
					useStemming = false;
				}
				if (option.equalsIgnoreCase("-T")
				        || option.equalsIgnoreCase("--tokenized")) {
					useToken = true;
				} else {
					if (i == args.length - 2) {
						inputOWL = option;
						File input = new File(inputOWL);
						if (input.exists()) {
							String pathForURI = "file://"
							        + input.getAbsolutePath();
							pathForURI = pathForURI.replace("\\", "/");
							pathForURI = pathForURI.replace(" ", "%20");
							physicalURI = URI.create(pathForURI);
						} else {
							System.err.println("!! Invalid OWL file (" + option
							        + ").");
							printHelp();
						}
					}
					if (i == args.length - 1) {
						pwFile = option;
					}
				}
			}
		}

		// configFile = sysProp.getProperty("CONFIG_FILE");
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(configFile));

			ontologyNamespace = props.getProperty("namespace");

			setPropertyMatch(propertyMatchFile);
			setPropertyPrint(propertyPrintFile);

			config_pw(pwFile);
			setMapFromFile(matchFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.out
			        .print("Unable to find file owlcompare.properties.  Program will abort.");
			System.exit(1);
		}
		try {
			System.out.println("Loading OWL file.");
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
		} catch (OWLException e) {
			e.printStackTrace();
			System.out
			        .print("Difficulty loading OWL file.  Check filename in command line.");
			System.exit(1);
		}
		System.out.println("Finished configuring.");
	}

	private void config_pw(String fileLoc) {
		try {
			File file = new File(fileLoc);
			pw = new PrintWriter(file);
		} catch (Exception e) {
			System.out.println("Error in PrintWriter");
		}
	}

	public void runCompare() {
		// Loop through each class of the ontology, returning axiom
		// values of properties or property qualifiers we want matched.
		// Then, check for the existence of a key in the compareMap matching any
		// of these values.
		// Where a key exists (match), tack an OWLClass object on as a value.
		int classCount = 0;
		boolean allowClass = true;
		Set<OWLClass> ocls = ontology.getReferencedClasses();
		for (OWLClass c : ocls) {

			classCount++;
			if (classCount % 10000 == 0 || classCount == ocls.size()) {
				System.out.println("Scanned " + classCount + " of "
				        + ocls.size() + " classes");
			}

			allowClass = true;
			if (compareActiveOnly) {
				allowClass = !isRetired(c);
			}

			if (allowClass) {
				Vector<String> v = getPropertyValues(c);
				if (v != null) {
					// TODO: Allow flagged option on match type (e.g., Porter,
					// soundslike, etc.)
					for (String value : v) {
						ArrayList<OWLClass> list = propertiesAndClasses
						        .get(value.toUpperCase());
						if (list != null) {
							if (!list.contains(c)) {
								list.add(c);
								propertiesAndClasses.put(value.toUpperCase(),
								        list);
							}
						} else {
							ArrayList<OWLClass> newList = new ArrayList<OWLClass>();
							newList.add(c);
							propertiesAndClasses.put(value.toUpperCase(),
							        newList);
						}
					}
					/*
					 * Vector<String> m = getMatchingKeys(v, c); if( m != null )
					 * { //tack class onto arraylist of each matching key for(
					 * String match : m ) { setMap(c, match); } }
					 */
				}
			}
		}
		getMatches();
		printMatches();
		if (generateExcel) {
			// printExcel();
			printExcel_multiline();
		}
		pw.close();
		// explanationPw.close();
	}

	public void printExcel() {
		String filename = pwFile.replace(".txt", ".xls");
		try {
			File file = new File(filename);
			FileOutputStream out = new FileOutputStream(file);
			Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet();
			Row r = null;
			Cell c = null;
			CellStyle cs = wb.createCellStyle();
			CellStyle cs2 = wb.createCellStyle();
			CellStyle cs3 = wb.createCellStyle();
			Font f = wb.createFont();
			Font f2 = wb.createFont();
			Font f3 = wb.createFont();

			f.setFontHeightInPoints((short) 10);

			f2.setFontHeightInPoints((short) 10);
			f2.setColor(Font.COLOR_RED);
			f2.setBold(true);

			f3.setFontHeightInPoints((short) 12);
			f3.setBold(true);

			// set the font
			cs.setFont(f);
			cs2.setFont(f2);
			cs3.setFont(f3);
			// wb.setSheetName(0, inputOWL);

			String[] s;
			if (!allowDuplicates) {
				s = compareMap.keySet().toArray(
				        new String[compareMap.keySet().size()]);
			} else {
				s = allInput.toArray(new String[allInput.size()]);
			}

			int rownum = 0;
			r = sheet.createRow(rownum);

			// pw.println("Match candidate\tMatch type\tMatching RDF:ID\tClass Attributes");
			// pure laziness here...
			c = r.createCell(0);
			c.setCellValue("Match candidate");
			c.setCellStyle(cs3);
			c = r.createCell(1);
			c.setCellValue("Match type");
			c.setCellStyle(cs3);
			c = r.createCell(2);
			c.setCellValue("Matching RDF:ID");
			c.setCellStyle(cs3);
			c = r.createCell(3);
			c.setCellValue("Class Attributes");
			c.setCellStyle(cs3);

			rownum++;
			for (String key : s) {
				int cellnum = 0;
				r = sheet.createRow(rownum);
				c = r.createCell(cellnum);
				ArrayList<OWLClass> list = compareMap.get(key);
				// pw.print( key );
				c.setCellValue(key);
				c.setCellStyle(cs);
				c = r.createCell(++cellnum);
				if (list != null) {
					if (list.size() == 1) {
						// pw.print("\tSINGLE-MATCH\t");
						c.setCellValue("SINGLE-MATCH");
					} else if (list.size() > 1) {
						// pw.print("\tMULTI-MATCH\t");
						c.setCellValue("MULTI-MATCH");
					}
					boolean retired = false;
					for (OWLClass clz : list) {
						// TODO: getAscendant, code
						// pw.print(classMatchDelimiter + clz);
						// pw.print("\t" + c);
						c = r.createCell(++cellnum);
						c.setCellValue(clz.toString());
						retired = isRetired(clz);
						if (retired) {
							c.setCellStyle(cs2);
						} else {
							c.setCellStyle(cs);
						}
						if (propertiesToPrint.size() > 0) {
							for (String propertyName : propertiesToPrint) {
								Vector<String> properties = new Vector<String>();
								properties = getProperties(clz, propertyName);
								for (String property : properties) {
									Cell c1 = r.createCell(++cellnum);
									Cell c2 = r.createCell(++cellnum);
									c1.setCellValue(propertyName);
									c2.setCellValue(property);
									if (retired) {
										c1.setCellStyle(cs2);
										c2.setCellStyle(cs2);
									} else {
										c1.setCellStyle(cs);
										c2.setCellStyle(cs);
									}
								}
							}
						}
					}
				} else {
					// pw.print("\tNO-MATCH");
					c.setCellValue("NO-MATCH");
					c.setCellStyle(cs);
				}
				// pw.print("\n");
				rownum++;
			}
			wb.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in FileOutputStream");
		}

	}

	public void printExcel_multiline() {
		String filename = pwFile.replace(".txt", ".xls");
		try {
			File file = new File(filename);
			FileOutputStream out = new FileOutputStream(file);
			Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet();
			Row r = null;
			Cell c = null;
			CellStyle cs = wb.createCellStyle();
			CellStyle cs2 = wb.createCellStyle();
			CellStyle cs3 = wb.createCellStyle();
			Font f = wb.createFont();
			Font f2 = wb.createFont();
			Font f3 = wb.createFont();

			f.setFontHeightInPoints((short) 10);

			f2.setFontHeightInPoints((short) 10);
			f2.setColor(Font.COLOR_RED);
			f2.setBold(true);

			f3.setFontHeightInPoints((short) 12);
			f3.setBold(true);

			// set the font
			cs.setFont(f);
			cs2.setFont(f2);
			cs3.setFont(f3);
			// wb.setSheetName(0, inputOWL);

			String[] s;
			if (!allowDuplicates) {
				s = compareMap.keySet().toArray(
				        new String[compareMap.keySet().size()]);
			} else {
				s = allInput.toArray(new String[allInput.size()]);
			}

			int rownum = 0;
			r = sheet.createRow(rownum);

			// pw.println("Match candidate\tMatch type\tMatching RDF:ID\tClass Attributes");
			// pure laziness here...
			c = r.createCell(0);
			c.setCellValue("Match candidate");
			c.setCellStyle(cs3);
			c = r.createCell(1);
			c.setCellValue("Match type");
			c.setCellStyle(cs3);
			c = r.createCell(2);
			c.setCellValue("Matching RDF:ID");
			c.setCellStyle(cs3);
			c = r.createCell(3);
			c.setCellValue("Class Attributes");
			c.setCellStyle(cs3);

			rownum++;
			for (String key : s) {
				ArrayList<OWLClass> list = compareMap.get(key);
				if (list != null) {
					boolean retired = false;
					for (OWLClass clz : list) {
						int cellnum = 0;
						r = sheet.createRow(rownum);
						c = r.createCell(cellnum);

						// pw.print( key );
						c.setCellValue(key);
						c.setCellStyle(cs);
						c = r.createCell(++cellnum);

						if (list.size() == 1) {
							// pw.print("\tSINGLE-MATCH\t");
							c.setCellValue("SINGLE-MATCH");
						} else if (list.size() > 1) {
							// pw.print("\tMULTI-MATCH\t");
							c.setCellValue("MULTI-MATCH");
						}

						c = r.createCell(++cellnum);
						c.setCellValue(clz.toString());
						retired = isRetired(clz);
						if (retired) {
							c.setCellStyle(cs2);
						} else {
							c.setCellStyle(cs);
						}
						if (propertiesToPrint.size() > 0) {
							for (String propertyName : propertiesToPrint) {
								Vector<String> properties = new Vector<String>();
								properties = getProperties(clz, propertyName);
								for (String property : properties) {
									Cell c1 = r.createCell(++cellnum);
									Cell c2 = r.createCell(++cellnum);
									c1.setCellValue(propertyName);
									c2.setCellValue(property);
									if (retired) {
										c1.setCellStyle(cs2);
										c2.setCellStyle(cs2);
									} else {
										c1.setCellStyle(cs);
										c2.setCellStyle(cs);
									}
								}
							}
						}
						rownum++;
					}
				} else {
					int cellnum = 0;
					r = sheet.createRow(rownum);
					c = r.createCell(cellnum);

					// pw.print( key );
					c.setCellValue(key);
					c.setCellStyle(cs);
					c = r.createCell(++cellnum);
					// pw.print("\tNO-MATCH");
					c.setCellValue("NO-MATCH");
					c.setCellStyle(cs);
					rownum++;
				}
				// pw.print("\n");
				rownum++;
			}
			wb.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in FileOutputStream");
		}

	}

	public void printMatches() {
		String[] s;
		if (!allowDuplicates) {
			s = compareMap.keySet().toArray(
			        new String[compareMap.keySet().size()]);
		} else {
			s = allInput.toArray(new String[allInput.size()]);
		}

		pw.println("Match candidate\tMatch type\tMatching RDF:ID\tClass Attributes");
		for (String key : s) {
			ArrayList<OWLClass> list = compareMap.get(key);
			pw.print(key);
			if (list != null) {
				if (list.size() == 1) {
					pw.print("\tSINGLE-MATCH\t");
				} else if (list.size() > 1) {
					pw.print("\tMULTI-MATCH\t");
				}
				for (OWLClass c : list) {
					// TODO: getAscendant, code
					pw.print(classMatchDelimiter + c);
					// pw.print("\t" + c);
					if (propertiesToPrint.size() > 0) {
						for (String propertyName : propertiesToPrint) {
							printProperties(c, propertyName);
						}
					}
				}
			} else {
				pw.print("\tNO-MATCH");
			}
			pw.print("\n");
		}
	}

	public void printProperties(OWLClass c, String propertyName) {
		Vector<String> properties = new Vector<String>();
		properties = getProperties(c, propertyName);
		for (String property : properties) {
			pw.print("\t" + propertyName + "\t" + property);
		}
	}

	public Vector<String> getProperties(OWLClass c, String property) {
		Vector<String> v = new Vector<String>();
		for (OWLAnnotation anno : c.getAnnotations(ontology)) {
			String annotationValue = anno.toString();
			if (annotationValue.contains("Annotation(" + property)) {
				// get property value, return the new value
				int beginning = annotationValue.indexOf("(");
				int end = annotationValue.indexOf("^^");
				String value = annotationValue.substring(
				        beginning + property.length() + 3, end - 1);
				v.add(value);
				/*
				 * //ONLY FOR DEF-SOURCE FDA if(
				 * value.contains("<def-source>FDA</def-source>") ) {
				 * v.add(value); }
				 */
				// System.out.println(property + " match value is: " + value);
			}
		}
		return v;
	}

	public void setMap(OWLClass c, String match) {
		ArrayList<OWLClass> list = compareMap.get(match);
		if (list != null) {
			if (!list.contains(c)) {
				list.add(c);
				compareMap.put(match, list);
			}
		} else {
			ArrayList<OWLClass> newList = new ArrayList<OWLClass>();
			newList.add(c);
			compareMap.put(match, newList);
		}
	}

	public void getMatches() {
		if (useStemming || useToken) {
			Set<Map.Entry<String, String>> entrySet = compareMapStemmed
			        .entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				ArrayList<OWLClass> tokenList = new ArrayList<OWLClass>();
				ArrayList<OWLClass> possibleValue = propertiesAndClasses
				        .get(entry.getValue());
				if (possibleValue != null) {
					ArrayList<OWLClass> newList = new ArrayList<OWLClass>(
					        possibleValue);
					for (OWLClass singleTokenClass : newList) {
						tokenList.add(singleTokenClass);
					}
				}
				String[] tokens = entry.getValue().split(",");

				for (String token : tokens) {
					possibleValue = propertiesAndClasses.get(token);
					if (possibleValue != null) {
						ArrayList<OWLClass> newList = new ArrayList<OWLClass>(
						        possibleValue);
						for (OWLClass singleTokenClass : newList) {
							tokenList.add(singleTokenClass);
						}
					}

				}
				if (tokenList.size() > 0) {
					compareMap.put(entry.getKey(), tokenList);
				}
			}
		}

		else {
			Set<Map.Entry<String, ArrayList<OWLClass>>> entrySet = compareMap
			        .entrySet();
			for (Map.Entry<String, ArrayList<OWLClass>> entry : entrySet) {
				ArrayList<OWLClass> possibleValue = propertiesAndClasses
				        .get(entry.getKey().toUpperCase());
				if (possibleValue != null) {
					ArrayList<OWLClass> newList = new ArrayList<OWLClass>(
					        possibleValue);
					compareMap.put(entry.getKey(), newList);
				}

			}
		}
	}

	// private Vector<String> getMatchingKeys(Vector<String> candidates,
	// OWLClass c) {
	//
	// Vector<String> matchingKeys = new Vector<String>();
	//
	// if( useStemming ) {
	// Set<Map.Entry<String, String>> entrySet = compareMapStemmed.entrySet();
	// for( Map.Entry<String, String> entry : entrySet ) {
	// if( candidates.contains(entry.getValue()) ) {
	// matchingKeys.add(entry.getKey());
	// explanationPw.println("A filler value in class " + c +
	// " matches the stemmed value of " + entry.getKey() + " (" +
	// entry.getValue() + ")");
	// explanationPw.println("\tcandidates -");
	// for(String candidate : candidates) {
	// explanationPw.println("\t\t" + candidate);
	// }
	// }
	// }
	// }
	// else {
	// for( String candidate : candidates ) {
	// for( String key : compareMap.keySet() ) {
	// if( candidate.equalsIgnoreCase(key)) {
	// matchingKeys.add(key);
	// }
	// }
	//
	// }
	// }
	//
	// if( !matchingKeys.isEmpty() ) {
	// return matchingKeys;
	// }
	// else
	// return null;
	//
	// }

	// Tokenize, then stem
	private String stemIt(String s) {
		// tString.cpp line 70
		if (s.length() < 4) {
			String stemmedString = new String(s);
			return stemmedString;
		}

		StringTokenizer st = new StringTokenizer(s.toLowerCase(), delimiters);
		int size = st.countTokens();
		int i = 0;
		String[] stemmedString = new String[size];
		if (st.countTokens() > 0) {
			while (st.hasMoreTokens()) {
				String token = new String(stemHelper(st.nextToken()));
				if (token != null && !token.equals(" ") && !token.equals("of")
				        && !token.equals("the") && !token.equals("and")
				        && !token.equals("or")) {
					stemmedString[i] = new String(token);
				} else {
					stemmedString[i] = new String();
				}
				i++;
			}
			Arrays.sort(stemmedString);
		}
		return arrayToString(stemmedString, ",");
	}

	private String tokenIt(String s) {
		if (s.length() < 4) {
			String tokenedString = new String(s);
			return tokenedString.toUpperCase();
		}

		StringTokenizer st = new StringTokenizer(s.toLowerCase(), delimiters);
		int size = st.countTokens();
		int i = 0;
		String[] tokenedString = new String[size];
		if (st.countTokens() > 0) {
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token != null && !token.equals(" ") && !token.equals("of")
				        && !token.equals("the") && !token.equals("and")
				        && !token.equals("or")) {
					tokenedString[i] = new String(token).toUpperCase();
				} else {
					tokenedString[i] = new String();
				}
				i++;
			}
			Arrays.sort(tokenedString);
		}
		return arrayToString(tokenedString, ",");
	}

	private String stemHelper(String s) {
		char[] c = s.toCharArray();
		Stemmer theStemmer = new Stemmer();
		for (char element : c) {
			theStemmer.add(element);
		}
		theStemmer.stem();
		return theStemmer.toString();
	}

	public static String arrayToString(String[] a, String separator) {
		StringBuffer result = new StringBuffer();
		if (a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	private boolean isRetired(OWLClass c) {
		boolean retired = false;
		for (OWLAnnotation anno : c.getAnnotations(ontology)) {
			String annotationValue = anno.toString();
			if (annotationValue
			        .contains("Annotation(Concept_Status \"Retired_Concept")) {
				retired = true;
				break; // I've seen enough
			}
		}
		return retired;
	}

	private Vector<String> getPropertyValues(OWLClass c) {
		Vector<String> v = new Vector<String>();
		String property = new String("");
		String qualifier = new String("");

		for (OWLAnnotation anno : c.getAnnotations(ontology)) {
			String annotationValue = anno.toString();
			for (Vector<String> targetProperty : propertiesToMatch) {
				if (targetProperty.size() > 1) {
					property = targetProperty.elementAt(0);
					qualifier = targetProperty.elementAt(1);
				} else {
					property = targetProperty.elementAt(0);
					qualifier = null;
				}
				if (property.equalsIgnoreCase("RDF:ID")) {
					v.add(c.toString());
				} else if (annotationValue.contains("Annotation(" + property
				        + " ")) {
					// get property value, return the new value
					int beginning = annotationValue.indexOf("(");

					// If using an OWL file that is not a direct Protege export
					// (i.e., unprocessed/scrubbed)
					// then these comments should be switched.
					// int end = annotationValue.indexOf("^^");
					int end = annotationValue.indexOf("^^");

					// if(end < 0)
					// {
					// end = annotationValue.indexOf(")");
					// }
					// if (end > 0)
					// {
					// //do nothing
					// }
					String value = annotationValue.substring(beginning
					        + property.length() + 3, end - 1);
//					if (qualifier != null) {
//						// get the qualifier value from a complex property
//						beginning = annotationValue.indexOf("<" + qualifier
//						        + ">");
//						end = annotationValue.indexOf("</" + qualifier + ">");
//						value = annotationValue.substring(
//						        beginning + qualifier.length() + 2, end);
//					}
					if (value.contains("&amp;")) {
						value = removeAmpersandReplacement(value);
					}
					if (value.contains("&apos;")) {
						value = removeAposReplacement(value);
					}
					if (useStemming) {
						v.add(stemIt(value));
					} else {
						v.add(value);
					}
					// System.out.println(property + " match value is: " +
					// value);
				}
			}
		}

		return v;
	}

	// from ProtegeKBQA
	public String removeAmpersandReplacement(String s) {// The Full-Syn value
		                                                // substitutes the &
		                                                // with &amp;
		                                                // This messes up the
		                                                // attempt to compare
		                                                // preferred_name with
		                                                // the Full-Syn. This
		                                                // method replaces &amp;
		                                                // with the &
		                                                // Broken into separate
		                                                // methods for ease of
		                                                // reuse.
		String ret = s.replace("&amp;", "&");
		return ret;
	}

	public String removeAposReplacement(String s) {
		// Some chemical names have &apos; instead of '
		String ret = s.replace("&apos;", "'");
		return ret;
	}

	// Modified Kim's method readConfigFile
	public void setMapFromFile(String filename) {
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {
					allInput.add(line);
					if (!compareMap.containsKey(line)) {
						compareMap.put(line, null);
						if (useStemming) {
							compareMapStemmed.put(line, stemIt(line));
						} else if (useToken) {
							compareMapStemmed.put(line, tokenIt(line));
						}
					} else if (!allowDuplicates)
						pw.println("Duplicate match candidate " + line
						        + " - Ignored.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setPropertyMatch(String filename) {
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {
					if (!line.equals("\n") && !line.equals("")) {
						Vector<String> p = new Vector<String>();
						if (line.contains("\t")) {
							String[] values = line.split("\t");
							p.add(values[0]);
							p.add(values[1]);
						} else
							p.add(line);
						propertiesToMatch.add(p);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setPropertyPrint(String filename) {
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null)
					eof = true;
				else {
					// TODO: Allow printing of qualifiers
					//
					// Vector<String> p = new Vector<String>();
					// if( line.contains("\t") ) {
					// String[] values = line.split("\t");
					// p.add(values[0]);
					// p.add(values[1]);
					// }
					// else
					// p.add(line);
					if (!line.equals("\n") && !line.equals(""))
						propertiesToPrint.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
