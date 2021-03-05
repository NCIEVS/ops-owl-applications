/*
 * National Cancer Institute Center for BioInformatices 2009 OWLSummary.java
 * 
 * Tracy Safran SAIC
 * 
 * The program is meant to take two versions of the NCI Thesaurus and analyze them for differences.
 * It checks to see if the "Header" declarations have changed, such as new or removed properties, roles
 *      or associations.
 * It then examines the concepts themselves for changes of import such as retreeing or changed definitions.
 * 
 * It generates two files: 
 *     The Summary file is a count of how many properties/roles/associations are present
 *     in the vocabulary then does a breakdown of the number of properties/roles/associations found under each
 *     root concept.
 *     The Details file is a list of changes that have impact on the meaning of the concepts.  It lists concepts
 *     with changed Preferred_Names, definitions, changed parents and entirely changed root concepts.
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;

/**
 * The Class OWLSummary.
 */
public class OWLSummary {
	/**
	 * The Enum headerField. Use to identify the type of object we are looking
	 * at.
	 */
	private enum headerField {

		/** The Associations. */
		Associations,
		/** The Concepts. */
		Concepts,
		/** The Namespaces. */
		Namespaces,
		/** The Properties. */
		Properties,
		/** The Roles. */
		Roles,
		/** The Roots. */
		Roots
	}

	// reads the System properties to get the location of the
	// owlsummary.properties file. The location can also be passed
	// in as a parameter, but this allows the value to be set by ant
	/** The sys prop. */
	private static Properties sysProp = System.getProperties();

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(final String[] args) {
		try {
			final OWLSummary summary = new OWLSummary();

			summary.configure(args);
			summary.performSummary();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** The config file. */
	private String configFile = null;

	/** The current vocabulary */
	private SummaryObject current;

	/** The filename for the current vocabulary. */
	private String currentFilename = null;
	
	/** Whether to do the summary file */
	private boolean doSummary = false;

	/** Whether or not to generate the Details report */
	private boolean doDetails = false;

	/** Whether to do a diff on two files or just a summary on one */
	private boolean doDiff = false;
	
	/** Whether to do metrics on the FULL_SYN sources **/
	private boolean doMetrics = false;

	/**
	 * The header diff. Hashset to store the count of each headerField within
	 * the vocabulary
	 */
	private final HashMap<headerField, Integer> headerDiff = new HashMap<headerField, Integer>();

	/** The previous vocabulary */
	private SummaryObject previous;;

	/** The filename for the previous vocabulary */
	private String previousFilename = null;

	/** The Printwriter for the Summary file */
	private PrintWriter pw;

	/** The Printwriter for the Diff file */
	private PrintWriter pwDiff;
	
	/** The Printwriter for the Metrics file */
	private PrintWriter pwMetrics;

	/**
	 * Configure.
	 * 
	 * @return true if two files passed in, to indicate we are doing a diff
	 * @throws Exception
	 *             the exception
	 */
	public void configure(String[] args) throws Exception {
		String summaryFile = null;
		String detailsFile = null;
		String metricsFile = null;
		Properties props = new Properties();
		try {
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equalsIgnoreCase("-c")
							|| args[i].equalsIgnoreCase("--config")) {
						configFile = args[++i];
					} else if (args[i].equalsIgnoreCase("-i")
							|| args[i].equalsIgnoreCase("--input")) {
						currentFilename = args[++i];
					} else if (args[i].equalsIgnoreCase("-p")
							|| args[i].equalsIgnoreCase("--previous")) {
						previousFilename = args[++i];
					} else if (args[i].equalsIgnoreCase("-s")
							|| args[i].equalsIgnoreCase("--summary")) {
						summaryFile = args[++i];
					} else if (args[i].equalsIgnoreCase("-d")
							|| args[i].equalsIgnoreCase("--details")) {
						detailsFile = args[++i];
					} else if (args[i].equalsIgnoreCase("-m")
					    || args[i].equalsIgnoreCase("--metrics")) {
					    metricsFile = args[++i];
					} else {
						printHelp();
					}
				}
			} else {
				printHelp();
			}

			// If there was no configFile location passed in a parameter, check
			// system properties
			if (configFile == null) {
				final String filename = sysProp
						.getProperty("owlsummary.properties");
				configFile = filename;
			}

			if (configFile != null) {
				System.out.println("Config file at: " + configFile);
				// Read the configFile into Properties

				FileInputStream instream = new FileInputStream(configFile);
				props.load(instream);
				instream.close();
			} else {
				System.out.println("No config file specified");
			}

			// Get location to write Summary file and initialize Printwriter
			if (summaryFile == null) {
				summaryFile = props.getProperty("outputfile");
			}
			if (summaryFile != null && summaryFile.length()>0){
			    doSummary = true;
			configPrintWriter(summaryFile);
			}

			// Check to see if they want to generate a details file.
			// If they pass in a location for the Details output, then they
			// want us to do details.
			if (detailsFile == null) {
				detailsFile = props.getProperty("detailsfile");
			}
			if (detailsFile != null && detailsFile.length() > 0) {
				doDetails = true;
				configPrintWriterDiff(detailsFile);
			}
			
	         // Check to see if they want to generate a metrics file.
            // If they pass in a location for the Metrics output, then they
            // want us to do metrics.
            if (metricsFile == null) {
                metricsFile = props.getProperty("metricsfile");
            }
            if (metricsFile != null && metricsFile.length() > 0) {
                doMetrics = true;
                configPrintWriterMetrics(metricsFile);
            }

			// Load the current vocabulary into a SummaryObject
			if (currentFilename == null) {
				currentFilename = props.getProperty("ontology_current");
			}
			System.out.println("Loading current vocabulary " + currentFilename);
			URI test = new URI(currentFilename);
			current = new SummaryObject(new URI(currentFilename));

			// If they passed in a previous file, load it and prepare to do a
			// diff
			try {
				if (previousFilename == null) {
					previousFilename = props.getProperty("ontology_previous");
				}
				if (previousFilename != null && previousFilename.length() > 0) {
					final URI previousFileURI = new URI(previousFilename);
					System.out.println("**************************");
					System.out.println("Loading previous vocabulary "
							+ previousFilename);
					previous = new SummaryObject(previousFileURI);
					doDiff = true;
				}
			} catch (final NullPointerException e) {
				// No error. There is simply no second file.
				System.out
						.println("Only one file input.  No diff being performed.");
			} catch (final java.lang.IllegalArgumentException e) {
				// Treat the run as if the "previous" vocab filename was never
				// passed in
				System.out.println("Error reading previous ontology from URL");
			}

		} catch (final FileNotFoundException e) {
			System.out.println("File not found");
			throw new Exception("File not found");
		} catch (final IOException e) {
			System.out.println("Trouble reading config file");
			throw new Exception("Trouble reading config file");
		} catch (final Exception e) {
			System.out.println("Trouble instantiating configuration");
			throw new Exception("Error in configuration");
		}

	}

	/**
	 * Do dual file summary.
	 */
	public final void doDualFileSummary() {
		final HashMap<headerField, Integer> headerCounts = doHeaderCounts(current);
		printHeaderCounts(headerCounts);
		pw.flush();
		System.out.println("Finished Header counts");

		doHeaderDiff();
		pw.flush();
		System.out.println("Finished Header diff");

		doEntityCounts(current);
		pw.flush();
		System.out.println("Finished entity counts");

		doEntityDiff();
		pw.flush();
		System.out.println("Finished entity diff");

		pw.close();
		
		if (doDetails) {
			doEntityDetails();
		}
		
		if (pwDiff != null) {
			pwDiff.close();
		}
	}

	/**
	 * Do single file summary.
	 */
	public final void doSingleFileSummary() {
		final HashMap<headerField, Integer> headerCounts = doHeaderCounts(current);
		printHeaderCounts(headerCounts);
		pw.flush();
		System.out.println("Finished Header counts");

		doEntityCounts(current);
		System.out.println("Finished entity counts");
		pw.close();
	}

	/**
	 * Prints the help.
	 */
	public void printHelp() {
		System.out.println("");
		System.out.println("Usage: OWLSummary [OPTIONS] ");
		System.out
				.println("  -C [configFile]\tRelative path to owlsummary.properties file (optional)");
		System.out.println("  -I, --Input\t\tURI of OWL file to be summarized");
		System.out
				.println("  -P, --Previous\tURI of previous OWL file if diff desired (optional)");
		System.out.println("  -S, --Summary\t\tURI to print Summary File");
		System.out
				.println("  -D, --Details\t\tURI to print Details File (optional)");
		System.out.println("");
		System.exit(1);
	}

	/**
	 * Compare definitions.
	 * 
	 * @param firstMap
	 *            the first map
	 * @param secondMap
	 *            the second map
	 * @return the hash map< string, string>
	 */
	private HashMap<URI, String> compareDefinitions(
			final HashMap<URI, String> firstMap,
			final HashMap<URI, String> secondMap) {
		final HashMap<URI, String> changedDefinitions = new HashMap<URI, String>();
		final Set<URI> c = firstMap.keySet();
		final Iterator<URI> iter = c.iterator();
		URI key;

		// definition changed, note it.
		while (iter.hasNext()) {
			key = iter.next();
			String secondDef = secondMap.get(key);
			String firstDef = firstMap.get(key);
			String fullDef = "";
			if (firstDef == null) {
				firstDef = "";
			}
			if (secondDef == null) {
				secondDef = "";
			}

			if (!secondDef.equals(firstDef)) {

				fullDef = firstDef + "\t" + secondDef;
				// Need to record concept, oldDef and newDef
				changedDefinitions.put(key, fullDef);
			}

		}
		return changedDefinitions;
	}

	/**
	 * Compare hash set.
	 * 
	 * @param firstSet
	 *            the first set
	 * @param secondSet
	 *            the second set
	 * @return the hash set< string>
	 */
	private HashSet<String> compareHashSet(final HashSet<String> firstSet,
			final HashSet<String> secondSet) {
		final HashSet<String> extraValues = new HashSet<String>();
		final Iterator<String> firstIt = firstSet.iterator();
		String item;
		while (firstIt.hasNext()) {
			item = firstIt.next();
			if (!secondSet.contains(item)) {
				extraValues.add(item);
			}
			// else {
			// extraValues.add(item);

			// }
		}
		return extraValues;
	}

	/**
	 * Compare map.
	 * 
	 * @param firstMap
	 *            the first map
	 * @param secondMap
	 *            the second map
	 * @return the hash map< string, string>
	 */
	private HashMap<String, String> compareMap(
			final HashMap<String, String> firstMap,
			final HashMap<String, String> secondMap) {
		final HashMap<String, String> extraValues = new HashMap<String, String>();
		final TreeSet<String> c = new TreeSet<String>(firstMap.keySet());
		final Iterator<String> iter = c.iterator();
		String key;
		while (iter.hasNext()) {
			key = iter.next();
			if (!secondMap.containsKey(key)) {
				extraValues.put(key, firstMap.get(key));
			}
		}
		return extraValues;
	}
	
	 
	private HashMap<URI, String> compareURIMap(
			final HashMap<URI, String> firstMap,
			final HashMap<URI, String> secondMap) {
		final HashMap<URI, String> extraValues = new HashMap<URI, String>();
		final TreeSet<URI> c = new TreeSet<URI>(firstMap.keySet());
		final Iterator<URI> iter = c.iterator();
		URI key;
		while (iter.hasNext()) {
			key = iter.next();
			if (!secondMap.containsKey(key)) {
				extraValues.put(key, firstMap.get(key));
			}
		}
		return extraValues;
	}

	/**
	 * Compare parents.
	 * 
	 * @param currentIn
	 *            the current
	 * @param previousIn
	 *            the previous
	 * @return the string
	 */
	private String compareParents(final Vector<URI> currentIn,
			final Vector<URI> previousIn) {
		String changedParents = "";

		URI item;
		boolean match = true;
		String previousPar = "";
		String currentPar = "";

		// converting to TreeSet will cause the parents to be sorted
		// alphabetically
		final TreeSet<URI> currentParents = new TreeSet<URI>(currentIn);
		final TreeSet<URI> previousParents = new TreeSet<URI>(previousIn);
		final Iterator<URI> firstIt = currentParents.iterator();
		int count = 0;
		while (firstIt.hasNext()) {

			item = firstIt.next();
			if (count > 0) {
				previousPar = previousPar + " | ";
			}
			previousPar = previousPar + item.getFragment();
			count++;
			if (!previousParents.contains(item)) {
				match = false;
			}

		}

		count = 0;
		match = true;
		final Iterator<URI> secondIter = previousParents.iterator();
		while (secondIter.hasNext()) {
			item = secondIter.next();
			if (count > 0) {
				currentPar = currentPar + " | ";
			}
			currentPar = currentPar + item.getFragment();
			count++;
			if (!currentParents.contains(item)) {
				match = false;
			}

		}

		if (!match) {
			changedParents = currentPar + "\t" + previousPar;
		}
		return changedParents;
	}

	/**
	 * Compare set.
	 * 
	 * @param firstSet
	 *            the first set
	 * @param secondSet
	 *            the second set
	 * @return the hash set< string>
	 */
	private HashSet<String> compareSet(final Set<String> firstSet,
			final Set<String> secondSet) {
		final HashSet<String> extraValues = new HashSet<String>();
		if ((firstSet != null)) {
			final Iterator<String> firstIt = firstSet.iterator();
			String item;
			while (firstIt.hasNext()) {
				item = firstIt.next();
				if (!secondSet.contains(item)) {
					extraValues.add(item);
				}

			}
		}
		return extraValues;
	}
	
	private HashSet<URI> compareURISet(final Set<URI> firstSet,
			final Set<URI> secondSet) {
		final HashSet<URI> extraValues = new HashSet<URI>();
		if ((firstSet != null)) {
			final Iterator<URI> firstIt = firstSet.iterator();
			URI item;
			while (firstIt.hasNext()) {
				item = firstIt.next();
				if (!secondSet.contains(item)) {
					extraValues.add(item);
				}

			}
		}
		return extraValues;
	}

	/**
	 * Compare set.
	 * 
	 * @param firstSet
	 *            the first set
	 * @param secondSet
	 *            the second set
	 * @return the hash set< string>
	 */
	private Vector<String> compareSet(final Vector<String> firstSet,
			final Vector<String> secondSet) {
		final Vector<String> extraValues = new Vector<String>();
		if(firstSet!=null && secondSet != null){
		if ((firstSet != null)) {
			final Iterator<String> firstIt = firstSet.iterator();
			String item;
			while (firstIt.hasNext()) {
				item = firstIt.next();
				if (!secondSet.contains(item)) {
					extraValues.add(item);
				}

			}
		}}
		return extraValues;
	}
	


//	private Vector<String> compareVector(final Vector<String> firstSet,
//			final Vector<String> secondSet) {
//		final Vector<String> extraValues = new Vector<String>();
//		final Iterator<String> firstIt = firstSet.iterator();
//		String item;
//		while (firstIt.hasNext()) {
//			item = firstIt.next();
//			if (!secondSet.contains(item)) {
//				extraValues.add(item);
//			}
//		}
//		return extraValues;
//	}
	
	@SuppressWarnings("unchecked")
	private Collection<URI> difference(
			HashMap<URI, ConceptProxy> hashMap1,
			HashMap<URI, ConceptProxy> hashMap2) {
		Collection<URI> result = CollectionUtils.disjunction(hashMap1
				.keySet(), hashMap2.keySet());
		return result;
	}
	
	private Vector<URI> compareURIVector(final Vector<URI> firstSet,
			final Vector<URI> secondSet) {
		
		
		if(firstSet ==null && secondSet == null){
			return new Vector<URI>();
		} else if (firstSet == null){
			return secondSet;
		} else if (secondSet ==null) {
			return firstSet;
		}
		
		Vector<URI> firstTest = new Vector<URI>();
		firstTest.addAll(firstSet);
		firstTest.removeAll(secondSet);
		return firstTest;
		
		
//		final Vector<URI> extraValues = new Vector<URI>();
//		final Iterator<URI> firstIt = firstSet.iterator();
//		URI item;
//		while (firstIt.hasNext()) {
//			item = firstIt.next();
//			if (!secondSet.contains(item)) {
//				extraValues.add(item);
//			}
//		}
//		return extraValues;
	}

	/**
	 * Concepts changing defs.
	 */
	private void conceptsChangingDefs() {
		System.out.println("Starting concepts changing definitions");
		// List concepts with definition changed - show old and new definition
		// Painful text compare concept by concept.
		final HashMap<URI, String> currentDefs = current.getConceptAndDef();
		final HashMap<URI, String> previousDefs = previous
				.getConceptAndDef();

		final HashMap<URI, String> changed = compareDefinitions(currentDefs,
				previousDefs);

		printDefinitions(changed);
	}

	/**
	 * Concepts changing kinds.
	 */
	private void conceptsChangingKinds() {
		System.out.println("Starting concepts changing kinds");
		// List concepts with a changed kind
		// This can be done by comparing two versions of root.
		// If concept has disappeared from a root, look it up and find
		// what the new root is.
		final HashMap<URI, Vector<URI>> currentConceptsPerKind = current
				.getConceptsPerKind();
		final HashMap<URI, Vector<URI>> previousConceptsPerKind = previous
				.getConceptsPerKind();
		Set<URI> keySet = currentConceptsPerKind.keySet();
		Iterator<URI> iter = keySet.iterator();

		final HashMap<URI, Vector<URI>> movedConcepts = new HashMap<URI, Vector<URI>>();
		final HashMap<URI, HashMap<URI, URI>> movedConceptsMap = new HashMap<URI, HashMap<URI, URI>>();

		// Look through kinds and find what concepts have been removed
		while (iter.hasNext()) {
			final URI missingFromKind = iter.next();
			// exclude Retired Kind
			if (!missingFromKind.equals("Retired Concept")
					&& !missingFromKind.equals("C28428")) {
				final Vector<URI> currentKind = currentConceptsPerKind
						.get(missingFromKind);
				final Vector<URI> previousKind = previousConceptsPerKind
						.get(missingFromKind);
				final Vector<URI> missing = compareURIVector(previousKind,
						currentKind);
				movedConcepts.put(missingFromKind, missing);
			}
		}

		// Determine where those concepts are now
		// Loop through the kinds in the movedConcepts Map
		keySet = movedConcepts.keySet();
		iter = keySet.iterator();
		while (iter.hasNext()) {
			final URI keyOldKind = iter.next();
			final HashMap<URI, URI> currentLocation = new HashMap<URI, URI>();
			// Loop through the concepts per kind in the missing Map
			final Vector<URI> missing = movedConcepts.get(keyOldKind);
			final Iterator<URI> conceptIt = missing.iterator();
			while (conceptIt.hasNext()) {
				final URI conceptCode = conceptIt.next();
				// Check to see what Kind this concept is in now.
				final URI newKind = current.whatKindIsThis(conceptCode);
				// exclude Retired Kind
				if (newKind != null) {
					if (!newKind.getFragment().equals("Retired Concept")
							&& !newKind.getFragment().equals("C28428")) {
						currentLocation.put(conceptCode, newKind);
					}
				} else {
					System.out.println(conceptCode + " resulted in a null kind");
				}
			}
			movedConceptsMap.put(keyOldKind, currentLocation);
		}

		printChangedKinds(movedConceptsMap);

	}

	/**
	 * Concepts changing parents.
	 */
	private void conceptsChangingParents() {
		System.out.println("Starting concepts changing parents");
		final HashMap<URI, HashMap<URI, String>> changedParentsPerRoot = new HashMap<URI, HashMap<URI, String>>();

		HashMap<URI, RootConcept> roots = current.getRootMap();
		Set<URI> rootIter = roots.keySet();
		for (URI rootCode : rootIter) {
			RootConcept rootConcept = roots.get(rootCode);
			final Iterator<URI> currentConceptIter = rootConcept
					.getAllDescendantCodes().iterator();
			while (currentConceptIter.hasNext()) {
				HashMap<URI, String> parentMap = new HashMap<URI, String>();
				URI conceptCode = currentConceptIter.next();
				Vector<URI> currentParents = current
						.getConceptParents(conceptCode);
				Vector<URI> previousParents = previous
						.getConceptParents(conceptCode);
				String changedParents = null;
				if (previousParents != null) {
					changedParents = compareParents(currentParents,
							previousParents);
				}
				if (changedParents != null && changedParents.length() > 0) {
					parentMap.put(conceptCode, changedParents);
				}
				changedParentsPerRoot.put(rootCode, parentMap);
			}

		}

		printChangedParents(changedParentsPerRoot);
	}

	/**
	 * Concepts changing Preferred_Name
	 */
	private void conceptsChangingPreferredNames() {
		// Just like the above, but with Preferred_Name and output by Semantic
		// Type
		System.out.println("Starting concepts changing preferred names");
		final HashMap<URI, String> currentPreferredNames = current
				.getConceptAndPreferredName();
		final HashMap<URI, String> previousPreferredNames = previous
				.getConceptAndPreferredName();

		// this method is generic
		final HashMap<URI, String> changed = compareDefinitions(
				currentPreferredNames, previousPreferredNames);

		final HashMap<URI, Vector<String>> currentSemanticTypes = current
				.getSemanticTypes();

		printPreferredNames(changed, currentSemanticTypes, pwDiff);
	}

	/**
	 * Config_pw.
	 * 
	 * @param fileLoc
	 *            the file loc
	 */
	private void configPrintWriter(final String fileLoc) throws Exception {
		try {
			final File file = new File(new URI(fileLoc));
			// pw = new PrintWriter(file);
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8")),true);
		} catch (final Exception e) {
			System.out.println("Error in SummaryFile PrintWriter ");
			e.printStackTrace();
			throw new Exception("Unable to create PrintWriter for Summary File");
		}
	}

	/**
	 * Config_pw_d.
	 * 
	 * @param fileLoc
	 *            the file loc
	 */
	private void configPrintWriterDiff(String fileLoc) {
		try {
			final File file = new File(new URI(fileLoc));
			// pwDiff = new PrintWriter(file);
			pwDiff = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8")),true);

		} catch (final Exception e) {
			System.out.println("Error in PrintWriter");
		}
	}

	   /**
     * Config_pw_d.
     * 
     * @param fileLoc
     *            the file loc
     */
    private void configPrintWriterMetrics(String fileLoc) {
        try {
            final File file = new File(new URI(fileLoc));
            // pwDiff = new PrintWriter(file);
            pwMetrics = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8")));

        } catch (final Exception e) {
            System.out.println("Error in PrintWriter");
        }
    }
	
	
	/**
	 * Do things per kind. Pass in the HashMap of "things", such as properties,
	 * roles, concepts, etc. and compare to the HashMap from the previous
	 * version.
	 * 
	 * @param currentKind
	 *            the current kind
	 * @param previousKind
	 *            the previous kind
	 * @return the hash map< string, hash map< string, integer>>
	 */
	private HashMap<URI, HashMap<URI, Integer>> doThingsPerKind(
			final HashMap<URI, HashMap<URI, Integer>> currentKind,
			final HashMap<URI, HashMap<URI, Integer>> previousKind) {

		// Build a HashMap to hold the results of the diff
		final HashMap<URI, HashMap<URI, Integer>> thingDiff = new HashMap<URI, HashMap<URI, Integer>>();

		// Iterate through current HashMap and see if the previous HashMap has
		// that same thing
		int mapsize = currentKind.size();
		Iterator<Entry<URI, HashMap<URI, Integer>>> testIter = currentKind
				.entrySet().iterator();
		for (int i = 0; i < mapsize; i++) {
			Map.Entry<URI, HashMap<URI, Integer>> entry = testIter.next();
			URI key1 = entry.getKey();
			HashMap<URI, Integer> currentThing = entry.getValue();
			if (previousKind.containsKey(key1)) {
				final HashMap<URI, Integer> previousThing = previousKind
						.get(key1);
				final HashMap<URI, Integer> diffThing = doDiff(currentThing,
						previousThing);
				thingDiff.put(key1, diffThing);
			}
		}

		// check that nothing appeared in previous that is missing now.
		mapsize = previousKind.size();
		testIter = previousKind.entrySet().iterator();
		for (int i = 0; i < mapsize; i++) {
			Map.Entry<URI, HashMap<URI, Integer>> entry = testIter.next();
			URI key1 = entry.getKey();
			final HashMap<URI, Integer> previousThing = entry.getValue();
			if (!currentKind.containsKey(key1)) {
				final HashMap<URI, Integer> negatedThing = negate(previousThing);
				thingDiff.put(key1, negatedThing);
			}
		}
		return thingDiff;
	}

	/**
	 * Negate.
	 * 
	 * @param thing
	 *            the thing
	 * @return the hash map< string, integer>
	 */
	private HashMap<URI, Integer> negate(final HashMap<URI, Integer> thing) {
		final HashMap<URI, Integer> negative = new HashMap<URI, Integer>();
		final Set<URI> item = thing.keySet();
		final Iterator<URI> iter = item.iterator();
		Integer currentCount, negatedCount;
		while (iter.hasNext()) {
			URI key = iter.next();
			currentCount = thing.get(key);
			negatedCount = currentCount * (-1);
			negative.put(key, negatedCount);
		}
		return negative;
	}

	/**
	 * New concepts.
	 */
	private void newConcepts() {
		// List all new concepts
		// List all deleted concepts
		// These two could be calculated from the concept count Maps
		System.out.println("Starting new concepts");
		try {
			final Vector<URI> currentClasses = current.getAllConceptCodes();
			final Vector<URI> previousClasses = previous
					.getAllConceptCodes();
			final Vector<URI> extra = compareURIVector(currentClasses,
					previousClasses);
			printNewConcepts(extra);
			final Vector<URI> missing = compareURIVector(previousClasses,
					currentClasses);
			printDeletedConcepts(missing);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Program exiting");
		}
	}

	/**
	 * Prints the changed kinds.
	 * 
	 * @param moveMap
	 *            the move map

	 */
	private void printChangedKinds(
			final HashMap<URI, HashMap<URI, URI>> moveMap) {
		pwDiff.println("Concepts with changed roots "
				+ "(excluding retirements/unretirements) found in  "
				+ currentFilename);
		pwDiff.println("Code\tName\tCurrent Root\tName\tPrevious Root\tName");

		final TreeSet<URI> oldKinds = new TreeSet<URI>(
				previous.getRootConceptNames());
		final Iterator<URI> oldIter = oldKinds.iterator();

		while (oldIter.hasNext()) {
			final URI oldKind = oldIter.next();
			final HashMap<URI, URI> concepts = moveMap.get(oldKind);

			if (concepts != null && concepts.size() > 0) {
				final TreeSet<URI> conceptSet = new TreeSet<URI>(
						concepts.keySet());

				final Iterator<URI> conceptCodes = conceptSet.iterator();
				while (conceptCodes.hasNext()) {
					final URI code = conceptCodes.next();
					final URI currentKind = concepts.get(code);
					final String currentKindName = current
							.getPreferredName(currentKind);
					final String previousKindName = current
							.getPreferredName(oldKind);
					final String name = current.getPreferredName(code);
					pwDiff.println(code + "\t" + name + "\t" + currentKind
							+ "\t" + currentKindName + "\t" + oldKind + "\t"
							+ previousKindName);
				}
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the deleted concepts.
	 * 
	 * @param concepts
	 *            the concepts

	 */
	private void printDeletedConcepts(final Vector<URI> concepts) {
		pwDiff.println("Deleted concepts present in " + previousFilename
				+ " but not in " + currentFilename);
		pwDiff.println("Previous Root\tRoot Name\tCode\tName");
		final HashMap<URI, Vector<URI>> previousCpk = previous
				.getConceptsPerKind();
		final TreeSet<URI> rootSet = new TreeSet<URI>(
				previousCpk.keySet());
		final Iterator<URI> rootIter = rootSet.iterator();
		final TreeSet<URI> sortConcepts = new TreeSet<URI>(concepts);
		while (rootIter.hasNext()) {
			final URI root = rootIter.next();
			final Iterator<URI> iter = sortConcepts.iterator();
			while (iter.hasNext()) {
				final URI searchCode = iter.next();
				final Vector<URI> conceptSet = previousCpk.get(root);
				if (conceptSet.contains(searchCode)) {
					final String name = previous.getPreferredName(searchCode);
					final String rootName = previous.getPreferredName(root);
					pwDiff.println(root.getFragment() + "\t" + rootName + "\t" + searchCode.getFragment()
							+ "\t" + name);
				}
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the new concepts.
	 * 
	 * @param concepts
	 *            the concepts

	 */
	private void printNewConcepts(final Vector<URI> concepts) {
		pwDiff.println("New concepts present in " + currentFilename
				+ " but not in " + previousFilename);
		pwDiff.println("Root\tRootName\tCode\tName");
		final HashMap<URI, Vector<URI>> currentCpk = current
				.getConceptsPerKind();
		final TreeSet<URI> rootSet = new TreeSet<URI>(currentCpk.keySet());
		final Iterator<URI> rootIter = rootSet.iterator();
		while (rootIter.hasNext()) {
			final URI root = rootIter.next();
			final Iterator<URI> iter = concepts.iterator();
			while (iter.hasNext()) {
				final URI searchCode = iter.next();
				final TreeSet<URI> conceptSet = new TreeSet<URI>(
						currentCpk.get(root));
				if (conceptSet.contains(searchCode)) {
					final String rootname = current.getPreferredName(root);
					final String name = current.getPreferredName(searchCode);
					pwDiff.println(root.getFragment() + "\t" + rootname + "\t" + searchCode.getFragment()
							+ "\t" + name);
				}
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the retired concepts.
	 * 
	 * @param concepts
	 *            the concepts

	 */
	private void printRetiredConcepts(final Vector<URI> concepts) {
		pwDiff.println("Concepts retired in " + currentFilename
				+ " but not in " + previousFilename);
		pwDiff.println("Previous Root\tRoot Name\tCode\tName");
		final HashMap<URI, Vector<URI>> previousCpk = previous
				.getConceptsPerKind();
		final TreeSet<URI> rootSet = new TreeSet<URI>(
				previousCpk.keySet());
		final Iterator<URI> rootIter = rootSet.iterator();
		while (rootIter.hasNext()) {
			final URI root = rootIter.next();
			final Iterator<URI> iter = concepts.iterator();
			while (iter.hasNext()) {
				final URI searchCode = iter.next();
				final TreeSet<URI> conceptSet = new TreeSet<URI>(
						previousCpk.get(root));
				if (conceptSet.contains(searchCode)) {
					final String rootName = current.getPreferredName(root);
					final String name = current.getPreferredName(searchCode);
					pwDiff.println(root.getFragment() + "\t" + rootName + "\t" + searchCode.getFragment()
							+ "\t" + name);
				}
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the unretired concepts.
	 * 
	 * @param concepts
	 *            the concepts

	 */
	private void printUnretiredConcepts(final Vector<URI> concepts) {
		pwDiff.println("Concepts retired in " + previousFilename
				+ " but not in " + currentFilename);
		pwDiff.println("Current Root\tRootName\tCode\tName");
		final HashMap<URI, Vector<URI>> currentCpk = current
				.getConceptsPerKind();
		final TreeSet<URI> rootSet = new TreeSet<URI>(currentCpk.keySet());
		final Iterator<URI> rootIter = rootSet.iterator();
		while (rootIter.hasNext()) {
			final URI root = rootIter.next();
			final Iterator<URI> iter = concepts.iterator();
			while (iter.hasNext()) {
				final URI searchCode = iter.next();
				final TreeSet<URI> conceptSet = new TreeSet<URI>(
						currentCpk.get(root));
				if (conceptSet.contains(searchCode)) {
					final String name = current.getPreferredName(searchCode);
					final String rootName = current.getPreferredName(root);
					pwDiff.println(root.getFragment() + "\t" + rootName + "\t" + searchCode.getFragment()
							+ "\t" + name);
				}
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Retired concepts.
	 */
	private void retiredConcepts() {
		// List all retired concepts
		// List all unretired concepts
		// These will be done by comparing Retired_Kind concepts between
		// versions
		// Have any been added? Have any disappeared?
		final HashMap<URI, Vector<URI>> currentConceptsPerKind = current
				.getConceptsPerKind();
		final HashMap<URI, Vector<URI>> previousConceptsPerKind = previous
				.getConceptsPerKind();
		Vector<URI> currentRetiredConcepts = currentConceptsPerKind
				.get("Retired Concept");
		if (currentRetiredConcepts == null) {
			currentRetiredConcepts = currentConceptsPerKind.get("Retired_Kind");
		}
		if (currentRetiredConcepts == null) {
			currentRetiredConcepts = currentConceptsPerKind.get("C28428");
		}
		Vector<URI> previousRetiredConcepts = previousConceptsPerKind
				.get("Retired Concept");
		if (previousRetiredConcepts == null) {
			previousRetiredConcepts = previousConceptsPerKind
					.get("Retired_Kind");
		}
		if (previousRetiredConcepts == null) {
			previousRetiredConcepts = previousConceptsPerKind.get("C28428");
		}
		final Vector<URI> extra = compareURIVector(currentRetiredConcepts,
				previousRetiredConcepts);
		printRetiredConcepts(extra);
		final Vector<URI> missing = compareURIVector(previousRetiredConcepts,
				currentRetiredConcepts);
		printUnretiredConcepts(missing);
	}

	/**
	 * Do diff.
	 * 
	 * @param newMap
	 *            the new map
	 * @param oldMap
	 *            the old map
	 * @return the hash map< string, integer>
	 */
	final HashMap<URI, Integer> doDiff(
			final HashMap<URI, Integer> newMap,
			final HashMap<URI, Integer> oldMap) {
		final HashMap<URI, Integer> diffMap = new HashMap<URI, Integer>();
		Integer countCurrent, countPrevious, countDiff;
		URI key;

		// Iterate through the current map and compare to the same item
		// in the previous to see if the count has changed. Put the results
		// in the diff map
		Set<URI> set = newMap.keySet();
		Iterator<URI> iter = set.iterator();
		while (iter.hasNext()) {
			key = iter.next();
			countCurrent = newMap.get(key);
			countPrevious = oldMap.get(key);
			if (countCurrent == null) {
				countCurrent = 0;
			}
			if (countPrevious == null) {
				countPrevious = 0;
			}
			countDiff = countCurrent - countPrevious;
			diffMap.put(key, countDiff);
		}

		// check that nothing appears in previous map that is not in current
		set = oldMap.keySet();
		iter = set.iterator();
		while (iter.hasNext()) {
			key = iter.next();
			countPrevious = oldMap.get(key);
			if (!diffMap.containsKey(key)) {
				// if an item is found missing, add to map and record
				// it's count as negative (current has -10 widgets compared to
				// previous)
				diffMap.put(key, countPrevious * (-1));
			}
		}

		return diffMap;
	}

	/**
	 * Do entity counts.
	 * 
	 * @param summary
	 *            the summary
	 */
	final void doEntityCounts(final SummaryObject summary) {
		// count the various entities per root
		// count concepts per kind, separate defined from primitive
		// count property instances
		// count role instances (place in source kind)
		// count association instances (place in source kind)

		System.out.println("Starting entity counts");
		final HashMap<URI, Integer> conceptCounts = summary
				.getConceptCountsPerKind();
		final HashMap<URI, Integer> defined = summary
				.getDefinedConceptCountsPerKind();
		printConceptCounts(conceptCounts, defined);

		// loop through classes and get property urls
		// enter url as key to hashmap. If not found, add to hashmap
		// the value should be the count on instances of the property
		// if key found, get the value, increment by one, then put back
		// except this won't track per concept kind.
		System.out.println("printProperties");
		final HashMap<URI, HashMap<URI, Integer>> propertyCounts = summary
				.getPropertyCountPerKind();
		printProperties(propertyCounts);

		System.out.println("printRoles");
		final HashMap<URI, HashMap<URI, Integer>> roleCounts = summary
				.getRoleCountPerKind();
		printRoles(roleCounts);

		System.out.println("printAssociations");
		final HashMap<URI, HashMap<URI, Integer>> assocCounts = summary
				.getAssociationCountPerKind();
		printAssociations(assocCounts);
		System.out.println("finished entity counts");
	}

	/**
	 * Do entity details.
	 */
	final void doEntityDetails() {
		System.out.println("Starting entity details");
		newConcepts();
		System.out.println("Finished computing new concepts");

		retiredConcepts();
		System.out.println("Finished computing retired and unretired concepts");

		conceptsChangingKinds();
		System.out.println("Finished computing concepts that changed roots");

		conceptsChangingParents();
		System.out.println("Finished computing concepts that changed parents");

		conceptsChangingDefs();
		System.out
				.println("Finished computing concepts with changed definitions");

		conceptsChangingPreferredNames();
		System.out
				.println("Finished reporting concepts with changed Preferred_Name");

	}

	/**
	 * Do entity diff.
	 */
	final void doEntityDiff() {
		// change in number of concepts per kind
		// separate by defined vs primitive
		System.out.println("Starting entity diff");
		final HashMap<URI, Integer> currentConceptCount = current
				.getConceptCountsPerKind();
		final HashMap<URI, Integer> previousConceptCount = previous
				.getConceptCountsPerKind();
		HashMap<URI, Integer> conceptDiff = doDiff(currentConceptCount,
				previousConceptCount);

		final HashMap<URI, Integer> definedCurrentConceptCount = current
				.getDefinedConceptCountsPerKind();
		final HashMap<URI, Integer> definedPreviousConceptCount = previous
				.getDefinedConceptCountsPerKind();
		HashMap<URI, Integer> definedConceptDiff = doDiff(
				definedCurrentConceptCount, definedPreviousConceptCount);

		printConceptDiff(conceptDiff, definedConceptDiff);

		// change in property instance count per kind
		final HashMap<URI, HashMap<URI, Integer>> currentProperties = current
				.getPropertyCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> previousProperties = previous
				.getPropertyCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> propertyDiff = doThingsPerKind(
				currentProperties, previousProperties);
		printPropertyDiff(propertyDiff);

		// change in role instance count per kind
		final HashMap<URI, HashMap<URI, Integer>> currentRoles = current
				.getRoleCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> previousRoles = previous
				.getRoleCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> roleDiff = doThingsPerKind(
				currentRoles, previousRoles);
		printRoleDiff(roleDiff);

		// change in Associations instance count per kind
		final HashMap<URI, HashMap<URI, Integer>> currentAssocs = current
				.getAssociationCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> previousAssocs = previous
				.getAssociationCountPerKind();
		final HashMap<URI, HashMap<URI, Integer>> assocDiff = doThingsPerKind(
				currentAssocs, previousAssocs);
		printAssocDiff(assocDiff);
		System.out.println("Finished entity diff");
	}

	/**
	 * Do header counts.
	 * 
	 * @param summary
	 *            the summary
	 * @return the HashMap<headerField, integer>
	 */
	final HashMap<headerField, Integer> doHeaderCounts(
			final SummaryObject summary) {
		// count the various header definitions
		// count the number of namespaces
		// count the number of kinds (semantic types?) = subClassOf
		// rdf:resource="#NCI_Kind"
		// count the number or roles = owl:ObjectProperty
		// count the number of associations = owl:AnnotationProperty
		// count the number of properties = owl:DatatypeProperty
		// count the number of qualifiers = not defined.

		// Load the Header HashMaps so that we can count them.
		final HashMap<headerField, Integer> headerCounts = new HashMap<headerField, Integer>();

		headerCounts.put(headerField.Roles, summary.getRoleMap().size());
		headerCounts
				.put(headerField.Associations, summary.getAssocMap().size());
		headerCounts.put(headerField.Properties, summary.getPropertyMap()
				.size());

		// Need to implement namespace loading in SummaryObject
		headerCounts.put(headerField.Namespaces, 0);

		headerCounts.put(headerField.Roots, summary.getRootMap().size());
		headerCounts.put(headerField.Concepts, summary.getConceptCount());

		return headerCounts;
	}

	/**
	 * Do header diff.
	 */
	final void doHeaderDiff() {
		// determine what fields in the header have changed
		// change in number of namespaces
		// list new or deleted namespaces
		// change in number of kinds (semantic types?)
		// list new or deleted kinds
		// change in number or roles
		// list new or deleted roles
		// change in number of associations
		// list new or deleted associations
		// change in number of qualifiers
		// list new or deleted qualifiers

		final HashMap<headerField, Integer> headerCurrent = doHeaderCounts(current);
		final HashMap<headerField, Integer> headerPrevious = doHeaderCounts(previous);

		// loop through headerCurrent, comparing keys to headerPrevious.
		// //compute diffs and load into headerDiff
		headerField key;
		Integer countCurrent, countPrevious, countDiff;

		final Set<headerField> set = headerCurrent.keySet();
		Iterator<headerField> iter = set.iterator();
		while (iter.hasNext()) {
			countCurrent = 0;
			countPrevious = 0;
			key = iter.next();
			countCurrent = headerCurrent.get(key);
			countPrevious = headerPrevious.get(key);
			countDiff = countCurrent - countPrevious;
			headerDiff.put(key, countDiff);
		}

		printHeaderDiff(headerDiff);
		// Check the counts. if the diff is anything other than 0, find out
		// what the actual header item is that is different.
		final TreeSet<headerField> tSet = new TreeSet<headerField>(
				headerDiff.keySet());
		iter = tSet.iterator();
		while (iter.hasNext()) {
			key = iter.next();
			if (headerDiff.get(key) != 0) {
				switch (key) {
				case Roles:
					// check the roles map
					HashMap<URI, String> extra = compareURIMap(
							current.getRoleMap(), previous.getRoleMap());
					if (extra.size() > 0) {
						printURIHashMap("Roles added", extra);
					}
					HashMap<URI, String> missing = compareURIMap(
							previous.getRoleMap(), current.getRoleMap());
					if (missing.size() > 0) {
						printURIHashMap("Roles removed", missing);
					}
					break;
				case Associations:
					// check the associations Map
					extra = compareURIMap(current.getAssocMap(),
							previous.getAssocMap());
					if (extra.size() > 0) {
						printURIHashMap("Associations added", extra);
					}
					missing = compareURIMap(previous.getAssocMap(),
							current.getAssocMap());
					if (missing.size() > 0) {
						printURIHashMap("Associations removed", missing);
					}
					break;
				case Properties:
					// check the properties map
					extra = compareURIMap(current.getPropertyMap(),
							previous.getPropertyMap());
					if (extra.size() > 0) {
						printURIHashMap("Properties added", extra);
					}
					missing = compareURIMap(previous.getPropertyMap(),
							current.getPropertyMap());
					if (missing.size() > 0) {
						printURIHashMap("Properties removed", missing);
					}
					break;
				case Namespaces:
					// check the namepaces map
					HashSet<String> extraS = compareSet(
							current.getNamespaces(), previous.getNamespaces());
					if (extraS.size() > 0) {
						printHashSet("Namespaces added", extraS);
					}
					HashSet<String> missingS = compareSet(
							previous.getNamespaces(), current.getNamespaces());
					if (missingS.size() > 0) {
						printHashSet("Namespaces removed", missingS);
					}
					break;
				case Roots:
					// check the roots map
					HashSet<URI> extraU = compareURISet(current.getRootConceptNames(),
							previous.getRootConceptNames());
					if (extraU.size() > 0) {
						printURIHashSet("Root Concepts added", extraU);
					}
					HashSet<URI> missingU = compareURISet(previous.getRootConceptNames(),
							current.getRootConceptNames());
					if (missingU.size() > 0) {
						printURIHashSet("Root Concepts removed", missingU);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Perform summary.
	 */
	final void performSummary() {
		try {
			// configure the vocabs. If only one vocab passed in, just do header
			// count and stop.
			if (doDiff) {
				doDualFileSummary();
			} 
			if (doSummary && ! doDiff) {
				doSingleFileSummary();
			}
			if(doMetrics){
			    doMetrics();
			}
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("Program exiting");
		}
	}
	
	private void doMetrics() {
	    //This will call the Metrics class and pass in needed information to
	    // perform various counts on the NCI Thesaurus
	    // Metrics are performed only on the current file
	    current.doMetrics();
	}

	/**
	 * Prints the assoc diff.
	 * 
	 * @param assocDiff
	 *            the assoc diff
	 */
	final void printAssocDiff(
			final HashMap<URI, HashMap<URI, Integer>> assocDiff) {

		pw.println("Association count diff per root for file "
				+ currentFilename + " minus " + previousFilename);
		final TreeSet<URI> assocSet = new TreeSet<URI>(current
				.getAssocMap().keySet());
		final Iterator<URI> assocIter = assocSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next().getFragment());
		}
		pw.print("\tCumulative");
		pw.println();

		while (assocIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI assocCode = assocIter.next();
			final String assocName = current.getAssocMap().get(assocCode);
			Integer assocCumulative = 0;
			pw.print(assocCode.getFragment() + "\t" + assocName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> assocMap = assocDiff.get(root);
				Integer assocCount = 0;
				if (assocMap != null) {
					assocCount = assocMap.get(assocCode);
					if (assocCount == null) {
						assocCount = 0;
					}
				}

				assocCumulative = assocCumulative + assocCount;
				pw.print(assocCount + "\t");
			}
			pw.print(assocCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the associations.
	 * 
	 * @param assocCounts
	 *            the assoc counts
	 */
	final void printAssociations(
			final HashMap<URI, HashMap<URI, Integer>> assocCounts) {

		pw.println("Association counts per root for file " + currentFilename);
		final TreeSet<URI> assocSet = new TreeSet<URI>(current
				.getAssocMap().keySet());
		final Iterator<URI> assocIter = assocSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next().getFragment());
		}
		pw.print("\tCumulative");
		pw.println();

		while (assocIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI assocCode = assocIter.next();
			final String assocName = current.getAssocMap().get(assocCode);
			Integer assocCumulative = 0;
			pw.print(assocCode + "\t" + assocName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> assocMap = assocCounts.get(root);
				Integer assocCount = assocMap.get(assocCode);
				if (assocCount == null) {
					assocCount = 0;
				}
				assocCumulative = assocCumulative + assocCount;
				pw.print(assocCount + "\t");
			}
			pw.print(assocCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the changed parents.
	 * 
	 * @param changedParents
	 *            the changed parents

	 */
	final void printChangedParents(
			final HashMap<URI, HashMap<URI, String>> changedParents) {
		pwDiff.println("Concepts that have been retreed in " + currentFilename
				+ " relative to " + previousFilename);
		pwDiff.println("Code\tName\tKind\tCurrent Parents\tPrevious Parents");
		final TreeSet<URI> rootSet = new TreeSet<URI>(
				changedParents.keySet());
		final Iterator<URI> rootIter = rootSet.iterator();
		while (rootIter.hasNext()) {
			final URI rootName = rootIter.next();
			final HashMap<URI, String> concepts = changedParents
					.get(rootName);
			final TreeSet<URI> conceptSet = new TreeSet<URI>(
					concepts.keySet());
			final Iterator<URI> conceptIter = conceptSet.iterator();
			while (conceptIter.hasNext()) {
				final URI code = conceptIter.next();
				final String changes = concepts.get(code);
				final String name = current.getPreferredName(code);

				pwDiff.println(code.getFragment() + "\t" + name + "\t" + rootName + "\t"
						+ changes);
			}
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the concept counts.
	 * 
	 * @param conceptCounts
	 *            the concept counts
	 * @param defined
	 *            the defined
	 */
	final void printConceptCounts(final HashMap<URI, Integer> conceptCounts,
			final HashMap<URI, Integer> defined) {

		// Use TreeSet because it implements SortedSet.
		// This should put roots in order alphabetically
		pw.println("Concept counts per root for file " + currentFilename
				+ " (count of defined concepts in second row)");
		final TreeSet<URI> tSet = new TreeSet<URI>(current.getRootMap()
				.keySet());
		Iterator<URI> iter = tSet.iterator();
		while (iter.hasNext()) {
			pw.print(iter.next().getFragment() + "\t");
		}
		pw.println();
		iter = tSet.iterator();
		while (iter.hasNext()) {
			final URI key = iter.next();
			pw.print(conceptCounts.get(key).toString() + "\t");
		}
		pw.println();
		iter = tSet.iterator();
		while (iter.hasNext()) {
			final URI key = iter.next();
			pw.print(defined.get(key).toString() + "\t");
		}

		pw.println();
	}

	/**
	 * Prints the concept diff.
	 * 
	 * @param conceptCounts
	 *            the concept counts
	 * @param defined
	 *            the defined
	 */
	final void printConceptDiff(final HashMap<URI, Integer> conceptCounts,
			final HashMap<URI, Integer> defined) {

		pw.println("Concept count diff per root for file " + currentFilename
				+ " (count of defined concepts in second row)");
		final TreeSet<URI> tSet = new TreeSet<URI>(current.getRootMap()
				.keySet());
		Iterator<URI> iter = tSet.iterator();
		while (iter.hasNext()) {
			pw.print(iter.next().getFragment() + "\t");
		}
		pw.println();
		iter = tSet.iterator();
		while (iter.hasNext()) {
			final URI key = iter.next();
			pw.print(conceptCounts.get(key).toString() + "\t");
		}
		pw.println();
		iter = tSet.iterator();
		while (iter.hasNext()) {
			final URI key = iter.next();
			pw.print(defined.get(key).toString() + "\t");
		}

		pw.println();
		pw.println();
	}

	/**
	 * Prints the definitions.
	 * 
	 * @param definitions
	 *            the definitions

	 */
	final void printDefinitions(final HashMap<URI, String> definitions) {
		// loop through and print changed definitions
		pwDiff.println("Concepts with changed definitions");
		pwDiff.println("Code\tName\tNew_Definition\tOld_Definition");
		final TreeSet<URI> keySet = new TreeSet<URI>(definitions.keySet());
		final Iterator<URI> iter = keySet.iterator();
		String fullDef = new String();
		while (iter.hasNext()) {
			final URI key = iter.next();
			fullDef = definitions.get(key);
			final String name = current.getPreferredName(key);
			pwDiff.println(key.getFragment() + "\t" + name + "\t" + fullDef);
		}
		pwDiff.println();
		pwDiff.flush();
	}

	/**
	 * Prints the hash map.
	 * 
	 * @param comment
	 *            the comment
	 * @param map
	 *            the map

	 */
//	final void printHashMap(final String comment,
//			final HashMap<String, String> map, final PrintWriter writer) {
//		writer.println(comment);
//		if (map != null) {
//			writer.println(map.toString());
//		}
//		writer.println();
//		writer.flush();
//	}
	
	final void printURIHashMap(final String comment,
			final HashMap<URI, String> map) {
		pw.println(comment);
		if (map != null) {
			pw.println(map.toString());
		}
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the hash set.
	 * 
	 * @param comment
	 *            the comment
	 * @param set
	 *            the set

	 */
	final void printHashSet(final String comment, final HashSet<String> set) {
		pw.println(comment);
		if (set != null) {
			pw.println(set.toString());
		}
		pw.println();
		pw.flush();
	}

	final void printURIHashSet(final String comment, final HashSet<URI> set) {
		pw.println(comment);
		if (set != null) {
			pw.println(set.toString());
		}
		pw.println();
		pw.flush();
	}
	
	/**
	 * Prints the header counts.
	 * 
	 * @param headerCounts
	 *            the header counts
	 */
	final void printHeaderCounts(
			final HashMap<headerField, Integer> headerCounts) {

		pw.println("Header definition counts for file " + currentFilename);
		pw.println("Concepts\tNamespaces\tRoots\tRoles\tProperties\tAssociations");
		String printOut = headerCounts.get(headerField.Concepts).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ headerCounts.get(headerField.Namespaces).toString();
		printOut = printOut + "\t";
		printOut = printOut + headerCounts.get(headerField.Roots).toString();
		printOut = printOut + "\t";
		printOut = printOut + headerCounts.get(headerField.Roles).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ headerCounts.get(headerField.Properties).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ headerCounts.get(headerField.Associations).toString();
		pw.println(printOut);
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the header diff.
	 * 
	 * @param inHeaderDiff
	 *            the header diff
	 */
	final void printHeaderDiff(final HashMap<headerField, Integer> inHeaderDiff) {

		pw.println("Header diff for file " + currentFilename + " minus "
				+ previousFilename);
		pw.println("Concepts\tNamespaces\tRoots\tRoles\tProperties\tAssociations");
		String printOut = inHeaderDiff.get(headerField.Concepts).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ inHeaderDiff.get(headerField.Namespaces).toString();
		printOut = printOut + "\t";
		printOut = printOut + inHeaderDiff.get(headerField.Roots).toString();
		printOut = printOut + "\t";
		printOut = printOut + inHeaderDiff.get(headerField.Roles).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ inHeaderDiff.get(headerField.Properties).toString();
		printOut = printOut + "\t";
		printOut = printOut
				+ inHeaderDiff.get(headerField.Associations).toString();
		pw.println(printOut);
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the preferred names.
	 * 
	 * @param preferredNames
	 *            the preferred names
	 * @param semanticTypes
	 *            the semantic types
	 * @param writer
	 *            the writer
	 */
	final void printPreferredNames(
			final HashMap<URI, String> preferredNames,
			final HashMap<URI, Vector<String>> semanticTypes,
			final PrintWriter writer) {
		// loop through and print properties with changed Preferred_Name
		writer.println("Concepts with changed Preferred_Name");
		writer.println("Code\tCurrent Semantic_Type\tNew Preferred_Name\tOld Preferred_Name");
		final TreeSet<URI> keySet = new TreeSet<URI>(
				preferredNames.keySet());
		final Iterator<URI> iter = keySet.iterator();

		String fullPN = new String();
		while (iter.hasNext()) {
			final URI key = iter.next();
			fullPN = preferredNames.get(key);
			if (semanticTypes.containsKey(key)) {
				Vector<String> stys = new Vector<String>();
				stys = semanticTypes.get(key);
				if (stys != null) {
					for (String sty : stys) {
						writer.println(key.getFragment() + "\t" + sty + "\t" + fullPN);
					}
				}
			}

			else {
				writer.println(key.getFragment() + "\t\t" + fullPN);
			}
		}
		writer.println();
		writer.flush();
	}

	/**
	 * Prints the properties.
	 * 
	 * @param propertyCounts
	 *            the property counts
	 */
	final void printProperties(
			final HashMap<URI, HashMap<URI, Integer>> propertyCounts) {

		pw.println("Property counts per root for file " + currentFilename);
		final TreeSet<URI> propSet = new TreeSet<URI>(current
				.getPropertyMap().keySet());
		final Iterator<URI> propIter = propSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next().getFragment());
		}
		pw.print("\tCumulative");
		pw.println();

		while (propIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI propCode = propIter.next();
			final String propName = current.getPropertyMap().get(propCode);
			Integer propCumulative = 0;
			pw.print(propCode.getFragment() + "\t" + propName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> propMap = propertyCounts
						.get(root);
				Integer propertyCount = propMap.get(propCode);
				if (propertyCount == null) {
					propertyCount = 0;
				}
				propCumulative = propCumulative + propertyCount;
				pw.print(propertyCount + "\t");
			}
			pw.print(propCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the property diff.
	 * 
	 * @param propertyDiff
	 *            the property diff
	 */
	final void printPropertyDiff(
			final HashMap<URI, HashMap<URI, Integer>> propertyDiff) {

		pw.println("Property count diff per root for file " + currentFilename
				+ " minus " + previousFilename);
		final TreeSet<URI> propSet = new TreeSet<URI>(current
				.getPropertyMap().keySet());
		final Iterator<URI> propIter = propSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next().getFragment());
		}
		pw.print("\tCumulative");
		pw.println();

		while (propIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI propCode = propIter.next();
			final String propName = current.getPropertyMap().get(propCode);
			Integer propCumulative = 0;
			pw.print(propCode.getFragment() + "\t" + propName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> propMap = propertyDiff.get(root);
				if (propMap != null) {
					Integer propertyCount = propMap.get(propCode);
					if (propertyCount == null) {
						propertyCount = 0;
					}
					propCumulative = propCumulative + propertyCount;
					pw.print(propertyCount + "\t");
				}
			}
			pw.print(propCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();

	}

	/**
	 * Prints the role diff.
	 * 
	 * @param roleDiff
	 *            the role diff
	 */
	final void printRoleDiff(
			final HashMap<URI, HashMap<URI, Integer>> roleDiff) {

		pw.println("Role count diff per root for file " + currentFilename
				+ " minus " + previousFilename);
		final TreeSet<URI> roleSet = new TreeSet<URI>(current
				.getRoleMap().keySet());
		final Iterator<URI> roleIter = roleSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next());
		}
		pw.print("\tCumulative");
		pw.println();

		while (roleIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI roleCode = roleIter.next();
			final String roleName = current.getRoleMap().get(roleCode);
			Integer roleCumulative = 0;
			pw.print(roleCode.getFragment() + "\t" + roleName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> roleMap = roleDiff.get(root);
				if (roleMap != null) {
					Integer roleCount = roleMap.get(roleCode);
					if (roleCount == null) {
						roleCount = 0;
					}
					pw.print(roleCount + "\t");
					roleCumulative = roleCumulative + roleCount;
				}
			}
			pw.print(roleCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	/**
	 * Prints the roles.
	 * 
	 * @param roleCounts
	 *            the role counts
	 */
	final void printRoles(
			final HashMap<URI, HashMap<URI, Integer>> roleCounts) {

		pw.println("Role counts per root for file " + currentFilename);
		final TreeSet<URI> roleSet = new TreeSet<URI>(current
				.getRoleMap().keySet());
		final Iterator<URI> roleIter = roleSet.iterator();
		final TreeSet<URI> rootSet = new TreeSet<URI>(current
				.getRootMap().keySet());
		Iterator<URI> rootIter = rootSet.iterator();
		pw.print("\t");
		while (rootIter.hasNext()) {
			pw.print("\t" + rootIter.next().getFragment());
		}
		pw.print("\tCumulative");
		pw.println();

		while (roleIter.hasNext()) {
			rootIter = rootSet.iterator();
			final URI roleCode = roleIter.next();
			final String roleName = current.getRoleMap().get(roleCode);
			Integer roleCumulative = 0;
			pw.print(roleCode.getFragment() + "\t" + roleName + "\t");
			while (rootIter.hasNext()) {
				final URI root = rootIter.next();
				final HashMap<URI, Integer> roleMap = roleCounts.get(root);
				Integer roleCount = roleMap.get(roleCode);
				if (roleCount == null) {
					roleCount = 0;
				}
				pw.print(roleCount + "\t");
				roleCumulative = roleCumulative + roleCount;
			}
			pw.print(roleCumulative);
			pw.println();
		}
		pw.println();
		pw.flush();

	}

}
