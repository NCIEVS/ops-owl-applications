package gov.nih.nci.evs.gobp.merge;

import gov.nih.nci.evs.gobp.print.PrintOWL1;
import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.owl.proxy.RoleProxy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This class is meant to read the Merge tags in NCIt and pull in the GO content
 * to match.
 * 
 * 
 * 
 * @author safrant
 *
 */
public class MergeController {
	final static String go_namespace = "http://purl.obolibrary.org/obo/go.owl";
	final static String go_range_item = "http://purl.obolibrary.org/obo/go.owl#biological_process";
	final static Vector<URI> go_range = new Vector<URI>();

	final static String nci_namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.merge.MergeController.class);

	private OWLKb ncit;
	private OWLKb gobp;
	/*
	 * Read in the GO and NCIt files. Look at the mapping file and find the
	 * relevant concepts in each
	 * 
	 * Read NCIt and find the merge tags, read the go code Search GO and find
	 * the information for that GO code. Craft an rdf:about concept with NCIt
	 * information and GO code ?? do I need to specify subClassOf, or is it
	 * sufficient to rely on GO? How will the exported file look?
	 */

	public MergeController(String ncitFilePath, String goFilePath,
			String queryFile,
			String outputFilePath) {
		try {
			go_range.add(URI.create(go_range_item));

		Set<String> rawConfig = readConfigFile(queryFile);
		HashMap<String, String> mapFile = processConfig(rawConfig);

		ncit = new OWLKb(ncitFilePath, nci_namespace);
			ncit.getAllConcepts();
		// we are searching for editor notes that reference being mapped to GO.

		// Do I need anything from GO other than the parent code?
		gobp = new OWLKb(goFilePath, go_namespace);
			gobp.getAllConcepts();

		Set<String> keySet = mapFile.keySet();
			for (String nciCode : keySet) {
				// entityRenamer(nciCode, mapFile.get(nciCode));
				if (nciCode == null) {
					String debug = "Stop here";
				}
				
				URI nciURI = URI.create(nci_namespace + "#" + nciCode);
			    String goCode = mapFile.get(nciCode);
			    goCode = goCode.replace(":", "_");
				URI goURI = URI.create(go_namespace + "#" + goCode);
				processMerge(nciURI, goURI);
		}

			// TODO for any roles with range of nci:Biological_Process, add a
			// range of GO#biological_process

			ncit.saveOntology("file:///Users/safrant/EVS/data/GOBP/Thesaurus_Merged.owl");
			ncit = null;
			ncit = new OWLKb(
					"file:///Users/safrant/EVS/data/GOBP/Thesaurus_Merged.owl",
					nci_namespace);
			// ncit.reloadOntology();
			new PrintOWL1(ncit,
					"file:///Users/safrant/EVS/data/GOBP/Thesaurus_Merged_pretty.owl");

			new PrintOWL1(ncit, outputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processMerge(URI nciCode, URI goCode) {

		// For each entry in the map file I will need to create the merged
		// concept and
		// to retire the existing concept

		// Retrieve the NCIt from OWLKb.
//		goCode = goCode.replace(":", "_");

		ConceptProxy nciConcept = ncit.getConcept(nciCode);
		ConceptProxy goConcept = gobp.getConcept(goCode);

		if (goCode.toString().contains("GO_0000003")) {
			String debug = "stop here";
		}

		Vector<URI> goParentCodes = goConcept.getParentURI();
		if (goParentCodes.size() < 1) {
			// Check if this is a legitimate GO concept that was listed
			logger.info("GO concept being referenced does not exist: "
					+ nciCode + " references " + goCode);
			return;
		}

		// Capture the properties

		// Create new concept in the ncit OWLKb with an rdf:about referencing GO

		logger.info("Creating " + goCode + " merge concept as referenced by "
				+ nciCode);

		ConceptProxy newConcept = ncit.createConcept(goCode,
  "");
		for (Property prop : nciConcept.getProperties()) {
			newConcept.addProperty(prop);
		}


		for (URI parentCode : goParentCodes) {
			newConcept.addParent(parentCode);
		}

		// Vector<Role> roles = nciConcept.getRoles();
		// for (Role role : roles) {
		// // Pretty sure I need to do some owlkb creation coding
		// // newConcept.addRole(role);
		// }
		// Gilberto not sure if we need to keep these.

		// We definitely need to keep incoming restrictions.
		// Remove the old axiom and add a new to the existing concept?
		// Temporarily - just add the new axiom
		Vector<Role> incomingRoles = ncit.getRolesForTarget(nciConcept);
		for (Role role : incomingRoles) {
			Concept sourceConcept = role.getSource();
			// ConceptProxy sourceProxy = new ConceptProxy(
			// sourceConcept.getCode(), ncit);
			ConceptProxy sourceProxy = ncit.getConcept(sourceConcept.getURI());
			RoleProxy rProxy = new RoleProxy(role.getCode(), this.ncit);
			rProxy.setRange(go_range);
			Role newRole = new Role(rProxy, sourceProxy, newConcept,
					Role.RoleModifier.SOME);

			sourceConcept.addRole(newRole);
		}
		// TODO Retire the ncit concept

	}

	public MergeController(URI ncitFilePath, URI goFilePath, URI queryFile,
			URI outputFilePath) {
		OWLKb ncit = new OWLKb(ncitFilePath.getPath(), nci_namespace);
		
		OWLKb gobp = new OWLKb(goFilePath.getPath(), go_namespace);
	}

	public static void main(String[] args) {
		if (args.length == 4) {
			try {
				// new MergeController(new URI(args[0]), new URI(args[1]),
				// new URI(args[2]), new URI(args[3]));

				new MergeController(args[0], args[1], args[2], args[3]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Need 3 parameters");
		}

	}

	private static Set<String> readConfigFile(String filename) {

		Set<String> v = new TreeSet<String>();
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
					v.add(line);
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
		if (!v.isEmpty()) {
			System.out.println("File size read in " + v.size());
			return v;
		}
		return null;
	}

	private HashMap<String, String> processConfig(Set<String> rawConfig) {
		// Instance,Superclass(es),"""http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Editor_Note"""

		// config file should have 2 columns - Concept id and Editor Note
		HashMap<String, String> mapsFile = new HashMap<String, String>();
		Vector<String> removeDups = new Vector<String>();
		for (String line : rawConfig) {
			String[] tokenizedLine = line.split(",");
			if (tokenizedLine.length > 1) {
			String id = tokenizedLine[0];
				String mapsTo = tokenizedLine[1];
				String[] mapsArray = mapsTo.split("\\|");
				boolean mapFound = false;
				for(int i=0; i<mapsArray.length; i++) {
					String mapItem = mapsArray[i];
					if (mapItem.contains("Maps")) {
						if(mapFound == true)
						{
							String dupMap = parseId(id) + " " + mapsTo;
							logger.error("Concept maps to multiple GO concepts.  Please handle manually "
									+ dupMap);
						}else {
							
						mapFound = true;
						String goMap = parseMapsTo(mapsArray[0]);
						if (mapsFile.containsValue(goMap)) {

							Entry<String, String> entry = getEntryByValue(mapsFile,
									goMap);
							removeDups.add(entry.getKey());
							String dupMap = entry.getKey() + " & " + parseId(id)
									+ " map to " + parseMapsTo(mapsArray[0]);
							// mapsFile.remove(entry);
							logger.error("Multiple NCIt concepts map to the same GO concept. Please handle manually. "
									+ dupMap);
							} else if (goMap.length() > 0) {
								mapsFile.put(parseId(id), goMap);
								parseMapsTo(mapsArray[0]);
						}
						
						}
					}
				}
				
				

//				if (mapsArray[0].contains("Maps")) {
//					String goMap = parseMapsTo(mapsArray[0]);
//					if (mapsFile.containsValue(goMap)) {
//
//						Entry<String, String> entry = getEntryByValue(mapsFile,
//								goMap);
//						removeDups.add(entry.getKey());
//						String dupMap = entry.getKey() + " & " + parseId(id)
//								+ " map to " + parseMapsTo(mapsArray[0]);
//						// mapsFile.remove(entry);
//						logger.error("Multiple NCIt concepts map to the same GO concept. Please handle manually. "
//								+ dupMap);
//					} else {
//					mapsFile.put(parseId(id), parseMapsTo(mapsArray[0]));
//					}
				// }
			}
		}

		for (String key : removeDups) {
			mapsFile.remove(key);
		}
		return mapsFile;
	}

	public static Entry<String, String> getEntryByValue(
			HashMap<String, String> map, String value) {
		for (Entry<String, String> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry;
			}
		}
		return null;
	}

	private String parseId(String fullId) {
		String id = "";
		if (fullId.contains("#")) {
			id = fullId.substring(fullId.indexOf("#") + 1);
		} else {
			id = fullId;
		}

		if (id.contains("\"")) {
			id = id.replace("\"", "");

		}
		return id;
	}

	private String parseMapsTo(String mapsTo) {
		// Take in the mapsTo string and find the GO code within it.
		// Possible incoming strings look like Maps to GO:0008610|NCIt BP need
		// to keep
		// or Maps to GO:0009056
		String goCode = "";
		String[] tempList = mapsTo.split("\\|");
		for (int i = 0; i < tempList.length; i++) {
			String checkString = tempList[i];
			if (checkString.contains("Maps to")) {
				goCode = (String) checkString.substring(checkString
						.indexOf("GO"));
				goCode = goCode.replace("\"", "");
			}
		}

		return goCode;

	}

	private void entityRenamer(URI nciCode, URI goCode) {
		// Concept.changeIRI(newIRI);
		if (goCode.toString().contains("GO_0042427")) {
			String debug = "true";
		}
		ConceptProxy nciConcept = ncit.getConcept(nciCode);
		ConceptProxy goConcept = gobp.getConcept(goCode);
		nciConcept.changeURI(goConcept.getURI());
	}
}
