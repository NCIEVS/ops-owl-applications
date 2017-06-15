package gov.nih.nci.evs.gobp.map;

import gov.nih.nci.evs.gobp.print.LexGrid2LogMap;
import gov.nih.nci.evs.owl.data.OWLKb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;


public class NCIt_to_GO_autoMap {

	private org.apache.log4j.Logger logger = Logger.getLogger(gov.nih.nci.evs.gobp.map.NCIt_to_GO_autoMap.class);
	private TreeSet<MapElement> mapSet;
	private String goNamespace = "http://purl.obolibrary.org/obo/go.owl";
	private String nciNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	private OWLKb goOWLKb, nciOWLKb;

	public NCIt_to_GO_autoMap(String onto1_iri, String onto2_iri,
			String outputFile_iri) {
		// TODO this is for debugging
		try {
			// String onto1_iri =
			// "file:/Users/safrant/Downloads/Example_UsingLogMapFromApplication/oaei2012_FMA_small_overlapping_nci.owl";
			// String onto2_iri =
			// "file:/Users/safrant/Downloads/Example_UsingLogMapFromApplication/oaei2012_NCI_small_overlapping_fma.owl";
			// String outputFile_iri =
			// "file:/Users/safrant/EVS/data/GO/LogDemo.txt";


			System.out.println("Mapping saved to : " + outputFile_iri);
			doLogMap(onto1_iri, onto2_iri, outputFile_iri);

			//
			// URI nci = new URI(
			// "file:///Users/safrant/EVS/data/Thesaurus/Biological_Process.owl");
			// loadNCI(nci);
			// URI go = new URI(
			// "file:///Users/safrant/EVS/data/GO/go_extract_owl1.owl");
			// loadGO(go);

			// } catch (URISyntaxException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// mapSet = new TreeSet<MapElement>();
		// throw new UnsupportedOperationException();
	}

	public NCIt_to_GO_autoMap(String onto1_iri, String onto2_iri,
			String outputFile_iri, String previousMap_iri) {
		// this(onto1_iri, onto2_iri, outputFile_iri);

		String outputFile = outputFile_iri + "/logmap2_mappings.txt";

		// doLogMapGoldStandardCompare(
		// onto1_iri,
		// onto2_iri,
		// "/Users/safrant/Workspaces/GO_BP/GO_BP_Map_to_NCIt/GoldStandard.txt",
		// true, true, outputFile);


		checkMeta(onto1_iri);

		doMapDiff(outputFile, previousMap_iri);
	}

/**
 * This will take the current NCIt from somewhere, either latest export or Protege
 * and map it to the pretty GO branch.
 * 
 * We want the go from NCIt to GO since we are primarily interested in how NCIt is defined
 * by GO concepts, not the reverse.  
 * 
 * We want to filter the map based on changes that have occurred in GO.
 * 1. GO additions - See if any existing NCIt concept can be replaced by the new GO concept
 * 2. GO deletions - Check if any concepts point to the missing concept and flag it as urgent change
 * 3. GO obsolete - Check for suggested replacements and report to editor
 * 4. GO edits - check to see if any concepts map to it and if the map is still valid (terms still exist)
 * 5. GO edits deux - check to see if the edit creates any valid mappings that were missed before
 * 
 * 
 * input - pretty GO branch changes file and NCIt
 * Output - report of NCIt concepts that may need edits and suggested matches
 * The report should have slots to allow editors to easily approve a match and to enter alternatives 
 * if they disapprove.  Excel?  Web form?  It should be controlled so we can take their input and process it
 * 
 * Should this check the current GO mapping file and eliminate or separately sort matches that are already there?
 * 
 */
	// public NCIt_to_GO_autoMap(URI nciURL, URI goURL) {
	// loadGO(goURL);
	// loadNCI(nciURL);
	// mapSet = new TreeSet<MapElement>();
	// }
	
	private void loadNCI(URI url) {
		//load NCIt from the specified url
		//Need to decide if this will be a file or lexevs.  
		//Will it only be BP branch?
		nciOWLKb = new OWLKb(url, nciNamespace);
	}
	
	private void loadGO(URI url) {
		//It is likely we will just be passing this in from elsewhere in the app
		goOWLKb = new OWLKb(url, goNamespace);
	}
	
	private void checkMeta(String onto1_iri) {
		try {
			// TODO since meta takes a long time, this should only be searched
			// on codes in the diff
			loadGO(new URI(onto1_iri));
			Set<URI> goCodes = goOWLKb.getAllConceptCodes();
			Set<String> newCodes = new TreeSet<String>();
			for (URI code : goCodes) {
				if (code.toString().contains("GO_")) {
					String newCode = code.getFragment().replace("GO_", "GO:");
					newCodes.add(newCode);
				} else {
					newCodes.add(code.getFragment());
				}
			}

			// TODO check newCodes vs existing map. Only test concepts without a
			// valid map
			// Need to look at score? If have a previous meta-colocation with a
			// -1 then skip? What if there are new options in meta?

			// printCodeList(newCodes);
			Vector<MapElement> metaMap = new MetaColocation(newCodes, "GO",
					"NCI", false).getMaps();

			// TODO - need to switch from GO and NCI to namespaces to do
			// comparison

			for (MapElement element : metaMap) {
				element.setScore(goNamespace);
				element.setTarget(nciNamespace);
			}

			LexGrid2LogMap.printLogMap("./MetaMap.txt", metaMap);
			System.out.println("Output printed to ./MetaMap.txt");

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void printCodeList(Set<String> newCodes) {
		try {
			PrintWriter pw;
			pw = new PrintWriter(new File("./GO_Code_List.txt"));

			for (String code : newCodes) {

				pw.println(code);
			}

			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doLogMap(String onto1_iri, String onto2_iri,
			String outputFile_iri) {
		LogMap2_Matcher logmap2 = new LogMap2_Matcher(onto1_iri, onto2_iri,
				outputFile_iri, true);

		// LogMap2_Matcher logmap2 = new LogMap2_Matcher(
		// "file:///Users/safrant/EVS/data/Thesaurus/Biological_Process.owl",
		// "file:///Users/safrant/EVS/data/GO/go_extract_owl1.owl");
		Set<MappingObjectStr> logmap2_mappings = logmap2.getLogmap2_Mappings();
	}

	private void doLogMapGoldStandardCompare(String onto1_iri,
			String onto2_iri, String goldStandard_file, boolean doIntersection,
			boolean do2Step, String goldOutputIRI) {
		OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1;
		try {
			onto1 = onto_manager.loadOntology(IRI.create(onto1_iri));

		OWLOntology onto2 = onto_manager.loadOntology(IRI.create(onto2_iri));
		MappingsReaderManager readerManager = new MappingsReaderManager(goldStandard_file,"TXT");
		Set<MappingObjectStr> input_mappings = readerManager.getMappingObjects();
		
			LogMap2_RepairFacility logmap2_repair = new LogMap2_RepairFacility(
					onto1, onto2, input_mappings, doIntersection, do2Step,
					true, goldOutputIRI);

			Set<MappingObjectStr> logmap2_mappings = logmap2_repair
					.getCleanMappings();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doExactMatch(){
		//iterate through GO synonyms and search against NCIt using exactMatch.
		//Create a MapElement for matches and put it into mapSet

	}
	
	private void doContainsMatch(){
		//iterate through GO synonyms and search against NCIt using containsMatch.
		//Create a MapElement for matches and put it into mapSet
		//possibly check mapSet to see if there already is an exactMatch for a given match.
		// if (mapSet.contains(new MapElement()));
	}
	
	private void doLucenceMatch(){
		//iterate through GO synonyms and search against NCIt using luceneMatch.
		//Create a MapElement for matches and put it into mapSet
	}
	
	private void doColocationMatch(){
		//Query Metathesaurus for the GO code.  See if there are any NCIt codes in the same concept
		//Would this use only the diff, since Meta lags so much?
		//perhaps it could be parameter based and use all GO codes

		Vector<URI> goCodes = (Vector<URI>) goOWLKb.getAllConceptCodes();
		MetaColocation meta = new MetaColocation("GO", "NCI");
		if (goCodes != null) {
			for (URI code : goCodes) {
				Vector<MapElement> metaResult = meta.findAndResolveCode(code.getFragment());
				for (MapElement element : metaResult) {
					// mapSet.add(parseMetaResult(metaResult));
					mapSet.add(element);
				}
			}
		}
	}
	
	private MapElement parseMetaResult(String metaResult) {
		MapElement me = new MapElement();

		// getting a files that is tab and space delimited
		// TODO will need to improve that format soon.
		return me;
	}

	private void sortMatches(){
		//If a GO code has multiple matches to a single NCIt code, keep only the best match in the mapSet
		/*
		 * Example:
		 * GO:1234 	exact 	  	C1234
		 * GO:1234  	contains	C1234
		 * GO:1234	lucene		C1234
		 *  
		 *  
		 * Might not be needed if native TreeSet sorting handles everything
		 */
	}

	private void doMapDiff(String currentMap_iri, String previousMap_iri) {
		// Takes the previous and current maps and points out differences.
		// possibly take in a third file of approved or discarded maps?

		// possibly save map with a "-1" score if rejected. When time comes to
		// load to LexBIG, those are discarded.

		TreeSet<MapElement> finalMap = new TreeSet<MapElement>();
		TreeSet<MapElement> mapForReview = new TreeSet<MapElement>();
		TreeSet<MapElement> previousElements = new TreeSet<MapElement>();

		TreeSet<MapElement> current = readMap(currentMap_iri);
		TreeSet<MapElement> previous = readMap(previousMap_iri);
		for (MapElement element : current) {
			if (previous.contains(element)) {
				finalMap.add(element);
			} else {
				mapForReview.add(element);
			}
		}

		System.out.println(finalMap.size() + " Elements in final map");

		System.out.println(mapForReview.size()
				+ " Elements in current that are not in previous");
		for (MapElement element : mapForReview) {
			System.out.println(element.toString());
		}

		// TODO if previous map has a match that is not included in current
		// check to see if source or target has changed
		// If they haven't then propagate match to new map.

		System.out.println();

		for (MapElement element : previous) {
			if (!current.contains(element)) {
				previousElements.add(element);
				// TODO check diff of concepts and see if they have changed.
				// TODO check to see if the only difference is the score

			}
		}
		System.out.println(previousElements.size()
				+ " Elements in previous that are not in current");
		for (MapElement element : previousElements) {
			System.out.println(element.toString());
		}

	}

	private static TreeSet<MapElement> readMap(String filename) {
		TreeSet<MapElement> v = new TreeSet<MapElement>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {

					v.add(parseMapElementLine(line));
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
				System.out.println(filename + " not found or unreadable");
				e.printStackTrace();
			}
		}
		if (!v.isEmpty())
			return v;
		return null;
	}

	public static MapElement parseMapElementLine(String mapString) {
		
		String[] mapParts = mapString.split("\\|");
		String[] sourceParts = mapParts[0].split("#");
		String[] targetParts = mapParts[1].split("#");
		float score = Float.parseFloat(mapParts[3]);
		MapElement mapElement = new MapElement(sourceParts[0], sourceParts[1],
				targetParts[0], targetParts[1], MatchTypeEnum.goldMatch,
				Mapping.parseMapping(mapParts[2]), score);
		return mapElement;
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new NCIt_to_GO_autoMap(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			new NCIt_to_GO_autoMap(args[0], args[1], args[2], args[3]);
		} else {
			// TODO provide real text
			System.out
					.println("Put instructions for what arguments should be passed in");
		}
	}
}
