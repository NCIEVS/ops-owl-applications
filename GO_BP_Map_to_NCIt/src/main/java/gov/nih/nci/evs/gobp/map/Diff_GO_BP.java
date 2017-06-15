package gov.nih.nci.evs.gobp.map;

import gov.nih.nci.evs.gobp.print.PrintOWL1;
import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

public class Diff_GO_BP {
/*
 * This will diff the current BP branch to the previous and determine if there are any 
 * differences.  
 * 
 * Options:
 * 
 * 1. Keep the previous BP file and do a simple file diff
 * 2. Search each concept against the LexBIG API - comparing only the fields we thing are important
 * 3. Compare to what is currently in Protege
 * 
 * Input - current BP file and something
 * Output - report of all concepts that have changed.
 */
	
	
	/*
	 * Important things for the diff
	 * 
	 * New concepts
	 * Newly retired/obsolete concepts
	 * New synonyms on current concepts
	 *        syns on currently mapped concepts
	 *        syns on concepts not mapped
	 */
	
//	String goNamespace;
	
	final private Logger logger = Logger.getLogger(gov.nih.nci.evs.gobp.map.Diff_GO_BP.class);
	
	OWLKb current = null;
	OWLKb previous = null;
	
	/**
	 * 
	 * @param currentPath
	 * @param previousPath
	 * @param go_Namespace
	 * @param outputFile
	 */
	public Diff_GO_BP(URI currentPath, URI previousPath, String go_Namespace, URI outputFile){
//		goNamespace = go_Namespace;

		if (!currentPath.toString().startsWith("file")) {
			String tempString = "file://" + currentPath.toString();
			currentPath = URI.create(tempString);
		}

		if (!previousPath.toString().startsWith("file")) {
			String tempString = "file://" + previousPath.toString();
			previousPath = URI.create(tempString);
		}
		
		configureKb(currentPath, previousPath, go_Namespace, outputFile);
	}
	

	private void configureKb(URI currentFile, URI previousFile, String goNamespace, URI outputFile) {
		try {
			boolean filesLoaded = true;
			current = new OWLKb(currentFile, goNamespace);
			if (current == null) {
				System.out.println("Unable to instantiate OWLKb for current URI");
				logger.error("Unable to instatiate OWLKb for current IRI");
				filesLoaded = false;
			}
			previous = new OWLKb(previousFile, goNamespace);
			if (previous == null) {
				System.out.println("Unable to instantiate OWLKb for previous URI");
				logger.error("Unable to instatiate OWLKb for previous IRI");
				filesLoaded=false;
			}

			if(filesLoaded){

				new PrintOWL1(current,  "/Users/safrant/EVS/data/GO/go_EVS.owl");
				diffOntology(outputFile);

			} else { 
				logger.error("Unable to load owl file");
			}
		} catch (FileNotFoundException e) {
			logger.error("Unable to write output file" , e);
		} catch (IOException e) {

			logger.error("Unable to write output file" , e);
		}
	}

	
	/**
	 * Diff ontology.
	 * 
	 * @param kb2
	 *            the knowledgebase to compare against
	 * @param pw
	 *            the pw
	 * @throws FileNotFoundException 
	 */
	private void diffOntology(URI outputFile) throws FileNotFoundException {
		PrintWriter pw;
		final File file = new File(outputFile);
		pw = new PrintWriter(file);
		// Diff the header
		// Compare roles here vs there
		HashMap<URI, String> roles = current.getAllRoles();
		HashMap<URI, String> foreignRoles = previous.getAllRoles();
		Set<URI> keySet = roles.keySet();
		pw.println("Diff of Roles");
		for (URI key : keySet) {
			if (!foreignRoles.containsKey(key)) {
				pw.println(">>   " + key + " " + roles.get(key));
			} else {
				pw.println("     " + key + " " + roles.get(key));
			}
		}
		pw.println(" ");
		keySet = foreignRoles.keySet();
		for (URI key : keySet) {
			if (!roles.containsKey(key)) {
				pw.println("<<   " + key + " " + foreignRoles.get(key));
			} else {
				pw.println("     " + key + " " + foreignRoles.get(key));
			}
		}

		HashMap<URI, String> associations = current.getAllAssociations();
		HashMap<URI, String> foreignAssociations = previous
				.getAllAssociations();
		keySet = associations.keySet();
		pw.println(" ");
		pw.println("Diff of Associations");
		for (URI key : keySet) {
			if (!foreignAssociations.containsKey(key)) {
				pw.println(">>   " + key + " " + associations.get(key));
			} else {
				pw.println("     " + key + " " + associations.get(key));
			}
		}
		pw.println(" ");
		keySet = foreignAssociations.keySet();
		for (URI key : keySet) {
			if (!associations.containsKey(key)) {
				pw.println("<<   " + key + " " + foreignAssociations.get(key));
			} else {
				pw.println("     " + key + " " + foreignAssociations.get(key));
			}
		}

		HashMap<URI, String> properties = current.getAllProperties();
		HashMap<URI, String> foreignProperties = previous.getAllProperties();
		keySet = properties.keySet();
		pw.println(" ");
		pw.println("Diff of Properties");
		for (URI key : keySet) {
			if (!foreignProperties.containsKey(key)) {
				pw.println(">>   " + key + " " + properties.get(key));
			} else {
				pw.println("     " + key + " " + properties.get(key));
			}
		}
		pw.println(" ");
		keySet = foreignProperties.keySet();
		for (URI key : keySet) {
			if (!properties.containsKey(key)) {
				pw.println("<<   " + key + " " + foreignProperties.get(key));
			} else {
				pw.println("     " + key + " " + foreignProperties.get(key));
			}
		}

		pw.flush();

		// Diff the concepts
		HashMap<URI, ConceptProxy> firstSetConcepts = current
				.getAllConcepts(); 
		// HashMap<String, ConceptProxy> secondSetConcepts = new
		// HashMap<String,ConceptProxy>();
		Set<URI> codes = firstSetConcepts.keySet();
		Vector<String> diff = new Vector<String>();
		pw.println(" ");
		pw.println("-------------------------------------");
		pw.println("Diff of concepts");
		pw.println("");
		for (URI code : codes) {
			ConceptProxy firstConcept = firstSetConcepts.get(code);
			ConceptProxy secondConcept = previous.getConcept(code);
			if (secondConcept == null
					|| secondConcept.getProperties().size() == 0) {
				pw.println(" ");
				pw.println(" ");
				pw
						.println("-----------------------------------------------------");
				pw.println(" ");
				pw.println("New concept");
				diff = diff(firstConcept, null);
			} else {
				diff = diff(firstConcept, secondConcept);
			}
			for (String s : diff) {
				pw.println(s);
			}
			pw.flush();
			diff = new Vector<String>();
		}
		// flip and look for concepts in kb2 that aren't in sceondSetConcepts
		codes = previous.getAllConcepts().keySet();
		for (URI code : codes) {
			if (current.getConcept(code) == null
					|| current.getConcept(code).getProperties().size() == 0) {
				pw.println(" ");
				pw.println(" ");
				pw
						.println("-----------------------------------------------------");
				pw.println(" ");
				pw.println("Concept has no match");
				//Determine if this was merged to something - and where.
				
				diff = diff(previous.getConcept(code), null);
			}
			for (String s : diff) {
				pw.println(s);
			}
			pw.flush();
			diff = new Vector<String>();
		}

		pw.close();
	}
	
	/**
	 * @param c2
	 * @return Vector of concept details, indicating changes
	 */
	private Vector<String> diff(ConceptProxy c1, ConceptProxy c2) {
		// TODO Sort the output somehow?
		if (c2 == null) return conceptSummary(c1);

		Vector<String> diff = new Vector<String>();
		boolean changesmade = false;

		diff.add(" ");
		diff.add(" ");
		diff.add("-----------------------------------------------------");
		diff.add(" ");
		diff.add("Code: " + c1.getCode() + " " + c1.getName());
		// System.out.println(code);
		// if (code.equals("Oxidation"))
		// {
		// System.out.println("gotit");
		// }

		// change in root node first
		// TODO how to detect change in root node

		// change in immediate parent
		Vector<URI> myParents = c1.getParentCodes();
		Vector<URI> yourParents = c2.getParentCodes();
		boolean match = false;
		for (URI myParent : myParents) {
			for (URI yourParent : yourParents) {
				if (myParent.toString().equalsIgnoreCase(yourParent.toString())) {
					match = true;
				}
			}
			ConceptProxy parent = current.getConcept(myParent);
			if (!match) {
				diff.add(">>    Defining Concept:    " + parent.getCode() + " "
						+ parent.getName());
				changesmade = true;
			} else {
				diff.add("      Defining Concept:    " + parent.getCode() + " "
						+ parent.getName());
			}
			match = false;
		}

		// change in Associations - alphabetical
		// Vector<Relationship> myAssociations = c1.getAssociations();
		// Vector<Relationship> yourAssociations = c2.getAssociations();

		Vector<Role> yourAssociations = c2.getRolesAndEquivalents();
		// Vector<Relationship> yourAssociations = c2.getRoles();
		// if(yourAssociations.size()>0){
		// String stop ="Checking roles";
		// }
		// Vector<Relationship> myAssociations = c1.getRoles();
		Vector<Role> myAssociations = c1.getRolesAndEquivalents();
		match = false;
		for (Relationship myAssociation : myAssociations) {
			for (Relationship yourAssociation : yourAssociations) {
				if (myAssociation.isEqual(yourAssociation)) {
					match = true;
				}
			}
			if (!match) {
				diff.add(">>    Association:        "
						+ myAssociation.getRelation().getCode() + " "
						+ myAssociation.getRelation().getName() + " "
						+ myAssociation.getTarget().getCode() + " "
						+ myAssociation.getTarget().getName());
				changesmade = true;
			} else {
				diff.add("      Association:       "
						+ myAssociation.getRelation().getCode() + " "
						+ myAssociation.getRelation().getName() + " "
						+ myAssociation.getTarget().getCode() + " "
						+ myAssociation.getTarget().getName());
			}
			match = false;
		}

		// change in properties in the following order
		// Preferred_Name, Semantic_Type, Full-Syn, Definition, Alt-Definition
		// NCI_Meta_Cui, UMLS_CUI, other properties alphabetical.
		// build it so there is no error if some of these properties are
		// missing.
		// Should be able to run this diff on any generic OWL file
		Vector<Property> myProperties = c1.getProperties();
		Vector<Property> yourProperties = c2.getProperties();
		match = false;
		for (Property myProperty : myProperties) {
			for (Property yourProperty : yourProperties) {
				if (myProperty.equals(yourProperty)) {
					match = true;
				}
			}
			if (!match) {
				diff.add(">>    Property:      " + myProperty.toString());
				changesmade = true;
			} else {
				diff.add("      Property:      " + myProperty.toString());
			}
			match = false;
		}

		// change in Roles - alphabetical (These may be subsumed in parents)

		// Switch now and compare the other way.
		// Parents
		diff.add(" ");
		diff.add(" ");
		for (URI yourParent : yourParents) {
			for (URI myParent : myParents) {
				if (yourParent.toString().equalsIgnoreCase(myParent.toString())) {
					match = true;
				}
			}
			ConceptProxy parent = previous.getConcept(yourParent);
			if (!match) {
				diff.add("<<    Defining Concept:    " + parent.getCode() + " "
						+ parent.getName());
				changesmade = true;
			} else {
				diff.add("      Defining Concept:     " + parent.getCode()
						+ " " + parent.getName());
			}
			match = false;
		}

		// change in Associations - alphabetical
		match = false;
		for (Relationship yourAssociation : yourAssociations) {
			for (Relationship myAssociation : myAssociations) {
				if (myAssociation.isEqual(yourAssociation)) {
					match = true;
				}
			}
			if (!match) {
				diff.add("<<    Association:        "
						+ yourAssociation.getRelation().getCode() + " "
						+ yourAssociation.getRelation().getName() + " "
						+ yourAssociation.getTarget().getCode() + " "
						+ yourAssociation.getTarget().getName());
				changesmade = true;
			} else {
				diff.add("      Association:         "
						+ yourAssociation.getRelation().getCode() + " "
						+ yourAssociation.getRelation().getName() + " "
						+ yourAssociation.getTarget().getCode() + " "
						+ yourAssociation.getTarget().getName());
			}
			match = false;
		}

		// Properties
		match = false;
		for (Property yourProperty : yourProperties) {
			for (Property myProperty : myProperties) {
				if (yourProperty.equals(myProperty)) {
					match = true;
				}
			}
			if (!match) {
				diff.add("<<    Property:      " + yourProperty.toString());
				changesmade = true;
			} else {
				diff.add("      Property:      " + yourProperty.toString());
			}
			match = false;
		}

		if (changesmade) return diff;
		return new Vector<String>();
	}
	
	private Vector<String> conceptSummary(ConceptProxy c1) {
		Vector<String> diff = new Vector<String>();
		diff.add("Code: " + c1.getCode());
		// change in immediate parent
		Vector<URI> myParents = c1.getParentCodes();
		for (URI myParent : myParents) {
			ConceptProxy parent = current.getConcept(myParent);
			// diff.add("        Defining Concept:     " + myParent);
			diff.add("        Defining Concept:     " + parent.getCode() + " "
					+ parent.getName());
		}
		Vector<Property> myProperties = c1.getProperties();
		for (Property myProperty : myProperties) {
			diff.add("         Property:    " + myProperty.toString());
		}
		return diff;

	}
	
	private void filterDiff(){
		//Sample command: grep --file=diffClean.txt Diff1105e-1104d.txt > cleanedDiff1105e-1104d.txt
	}

	private void evaluateDiff(){
		/**Things we are concerned about
		 1.  Retired.  Since we are working with an extraction, retired will be simply gone. Do we want to look at original GO file to 
		 find what the replaced_by value is?
		 
		 2. Merged.  These concepts will also be gone but will have an alt_id listed in the new concept
		     Also, a concept with new altid is likely subject of a new merge
		 
		 3. Computational definition changes - 
		 3a.  New or removed role (Association).
		 3b.  New or removed Defining concept
		 
		 4.  Textual definition changes - not including qualifiers
		 
		 5. New concepts
		 
		 6. Concepts that are not already mapped that have new matchable synonyms?
		 */
	}

}
