/**
 * National Cancer Institute Center for Bioinformatics
 * 
 * OWLDiff
 * gov.nih.nci.evs.owl
 * OWLDiff.java
 * Jun 12, 2009
 *
 */
/** <!-- LICENSE_TEXT_START -->
 The OWLDiff Copyright 2009 Science Applications International Corporation (SAIC)
 Copyright Notice.  The software subject to this notice and license includes both human readable source code form and machine readable, binary, object code form (the EVSAPI Software).  The EVSAPI Software was developed in conjunction with the National Cancer Institute (NCI) by NCI employees and employees of SAIC.  To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 This OWLDiff Software License (the License) is between NCI and You.  You (or Your) shall mean a person or an entity, and all other entities that control, are controlled by, or are under common control with the entity.  Control for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.
 This License is granted provided that You agree to the conditions described below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights in the OWLDiff Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly display, publicly perform, and prepare derivative works of the EVSAPI Software; (ii) distribute and have distributed to and by third parties the EVSAPI Software and any modifications and derivative works thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such rights to further third parties.  For sake of clarity, and not by way of limitation, NCI shall have no right of accounting or right of payment from You or Your sublicensees for the rights granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions and the disclaimer and limitation of liability of Article 6, below.  Your redistributions in object code form must reproduce the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials provided with the distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: This product includes software developed by SAIC and the National Cancer Institute.  If You do not include such end-user documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" Science Applications International Corporation and "SAIC" to endorse or promote products derived from this Software.  This License does not authorize You to use any trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and into any third party proprietary programs.  However, if You incorporate the Software into third party proprietary programs, You agree that You are solely responsible for obtaining any permission from such third parties required to incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including without limitation Your end-users, of their obligation to secure any required permissions from such third parties before incorporating the Software into such third party proprietary software programs.  In the event that You fail to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to the extent prohibited by law, resulting from Your failure to obtain such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * <!-- LICENSE_TEXT_END -->
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * @author safrant
 */
public class OWLDiff {
	// private static String defaultNamespace =
	// "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	OWLKb current = null;
	OWLKb previous = null;
	String namespace = null;
	URI currentFile = null;
	URI previousFile = null;
	URI outputFile = null;
	boolean inChangeSet = true;
	boolean byName = false;
	// static String outfile = "./OwlDiffOutput.txt";
	// boolean changeset = true;
	/** The pw. */
	private PrintWriter pw;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OWLDiff diff = new OWLDiff();

		try {
			diff.configure(args);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void configure(String[] args) throws Exception {
		// if (args.length > 0) {
		String configFile = "./config/owldiff.properties";
		try {
			for (int i = 0; i < args.length; i++) {
				String option = args[i];
				if (option.equalsIgnoreCase("--help")
				        || option.equalsIgnoreCase("-H")) {
					printHelp();
				} else if (option.equalsIgnoreCase("-U")
				        || option.equalsIgnoreCase("--Unique")) {
					inChangeSet = false;
				} else if (option.equalsIgnoreCase("-I")
				        || option.equalsIgnoreCase("--input")) {
					currentFile = new URI(args[++i]);
				} else if (option.equalsIgnoreCase("-p")
				        || option.equalsIgnoreCase("--previous")) {
					previousFile = new URI(args[++i]);
				} else if (option.equalsIgnoreCase("-o")
				        || option.equalsIgnoreCase("--output")) {
					outputFile = new URI(args[++i]);
				} else if (option.equalsIgnoreCase("-c")
				        || option.equalsIgnoreCase("--Config")) {
					configFile = args[++i];
				}

				else {
					printHelp();
				}

			}

			System.out.println("Config file at " + configFile);
			Properties props = new Properties();
			props.load(new FileInputStream(configFile));
			this.namespace = props.getProperty("namespace");
			if (currentFile == null || currentFile.toString().length() < 0) {
				this.currentFile = new URI(props.getProperty("current"));
			}
			if (previousFile == null || previousFile.toString().length() < 0) {
				this.previousFile = new URI(props.getProperty("previous"));
			}
			if (outputFile == null || outputFile.toString().length() < 0) {
				this.outputFile = new URI(props.getProperty("output"));
			}
			if (previousFile == null || currentFile == null
			        || previousFile.toString().length() < 1
			        || currentFile.toString().length() < 1) {
				printHelp();
			}
			configureKb(previousFile, currentFile);
			this.configPrintWriter(outputFile);
			pw.println("Diff of two OWL files");
			pw.println("Current File is " + currentFile.toString());
			pw.println("Previous File is " + previousFile.toString());
			pw.println();
			pw.println("--------------------------------------------");
			if (inChangeSet) {
				this.doChangeset();
			} else {
				this.findUnique();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to configure.  Aborting.");
			printHelp();
		}

	}

	/**
	 * Prints the help.
	 */
	public void printHelp() {
		System.out.println("");
		// System.out.println("Usage: OWLScrubber [OPTIONS] ... [OWL] [OUTPUT FILE]");
		System.out.println("Usage: ant run -Darguments=\"[OPTIONS]\"");
		System.out
		        .println("If not passed in as parameters, the input and output URIs must be specified in config file");
		System.out.println(" ");
		// System.out.println("  -N, --byName\t\tThe input files are byName (default is byCode)");
		System.out
		        .println("  -C, --Config\t\tRelative path to config file (default ./config/owldiff.properties)");
		System.out
		        .println("  -U, --Unique\t\tFind unique concepts (default is changeset)");
		System.out.println("  -H, --Help\t\tDisplay this help");
		System.out.println("  -I, --Input\t\tURI of current file (optional)");
		System.out
		        .println("  -P, --Previous\t\tURI of previous file (optional)");
		System.out.println("  -O, --Output\t\tURI of output file (optional)");
		System.out.println("");
		System.exit(1);
	}

	private void doChangeset() {
		// owlKb1.diffOntology(owlKb2, pw);
		diffOntology();
	}

	private void findUnique() {
		newConcepts();
		// TODO write methods to find unique classes
	}

	/**
	 * New concepts.
	 */
	private void newConcepts() {
		// List all new concepts
		// List all deleted concepts
		// These two could be calculated from the concept count Maps
		try {
			final Set<URI> currentClasses =  current
			        .getAllConceptCodes();
			final Set<URI> previousClasses = previous
			        .getAllConceptCodes();
			final Set<URI> extra = compareSet(currentClasses,
			        previousClasses);
			printNewConcepts(extra);
			final Set<URI> missing = compareSet(previousClasses,
			        currentClasses);
			printDeletedConcepts(missing);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Program exiting");
		}
	}

	private void printNewConcepts(Set<URI> extra) {
		Iterator<URI> codeIter = extra.iterator();
		pw.println("Concepts in current vocab that are not in previous");
		while (codeIter.hasNext()) {
			pw.println();
			ConceptProxy concept = current.getConcept(codeIter.next());
			Vector<String> conceptOutput = conceptSummary(concept);
			for (String s : conceptOutput) {
				pw.println(s);
			}
			pw.flush();
			conceptOutput = new Vector<String>();
		}
	}

	private void printDeletedConcepts(Set<URI> missing) {
		Iterator<URI> codeIter = missing.iterator();
		pw.println("Concepts in previous vocab that are not in current");
		while (codeIter.hasNext()) {
			pw.println();
			ConceptProxy concept = previous.getConcept(codeIter.next());
			Vector<String> conceptOutput = conceptSummary(concept);
			for (String s : conceptOutput) {
				pw.println(s);
			}
			pw.flush();
			conceptOutput = new Vector<String>();
		}
	}

	private Vector<String> compareVector(final Vector<String> firstSet,
	        final Vector<String> secondSet) {
		final Vector<String> extraValues = new Vector<String>();
		final Iterator<String> firstIt = firstSet.iterator();
		String item;
		while (firstIt.hasNext()) {
			item = firstIt.next();
			if (!secondSet.contains(item)) {
				extraValues.add(item);
			}
		}
		return extraValues;
	}
	
	private Set<URI> compareSet(final Set<URI> firstSet,
	        final Set<URI> secondSet) {
		final Set<URI> extraValues = new HashSet<URI>();
		final Iterator<URI> firstIt = firstSet.iterator();
		URI item;
		while (firstIt.hasNext()) {
			item = firstIt.next();
			if (!secondSet.contains(item)) {
				extraValues.add(item);
			}
		}
		return extraValues;
	}

	private void configureKb(URI previousFile, URI currentFile) {
		current = new OWLKb(currentFile, namespace);
		if (current == null) {
			System.out.println("Unable to instantiate OWLKb for current URI");
			System.exit(0);
		}
		previous = new OWLKb(previousFile, namespace);
		if (previous == null) {
			System.out.println("Unable to instantiate OWLKb for previous URI");
			System.exit(0);
		}

	}

	/**
	 * Config_pw.
	 * 
	 * @param fileLoc
	 *            the file loc
	 */
	private void configPrintWriter(final URI fileLoc) {
		try {
			final File file = new File(fileLoc);
			pw = new PrintWriter(file);
		} catch (final Exception e) {
			System.out.println("Error in PrintWriter");
		}
	}

	/**
	 * Diff ontology.
	 * 
	 * @param kb2
	 *            the knowledgebase to compare against
	 * @param pw
	 *            the pw
	 */
	private void diffOntology() {

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
//		HashMap<URI, ConceptProxy> firstSetConcepts = current
//		        .getAllConcepts();
		Set<URI> codes = current.getAllConceptCodes();
		Set<URI> previousCodes = previous.getAllConceptCodes();
		// HashMap<String, ConceptProxy> secondSetConcepts = new
		// HashMap<String,ConceptProxy>();
//		Set<URI> codes = firstSetConcepts.keySet();
		Vector<String> diff = new Vector<String>();
		pw.println(" ");
		pw.println("-------------------------------------");
		pw.println("Diff of concepts");
		pw.println("");
		for (URI code : codes) {
			try{
//			ConceptProxy firstConcept = firstSetConcepts.get(code);
			ConceptProxy secondConcept = previous.getConcept(code);
			if(previousCodes.contains(code))
			{
				diff = diff(code);
			}
			else {
				pw.println(" ");
				pw.println(" ");
				pw.println("-----------------------------------------------------");
				pw.println(" ");
				pw.println("Concept has no match");
				diff = currentConceptSummary(code);
			}
//			if (secondConcept == null
//			        || secondConcept.getProperties().size() == 0) {
//				pw.println(" ");
//				pw.println(" ");
//				pw.println("-----------------------------------------------------");
//				pw.println(" ");
//				pw.println("Concept has no match");
//				diff = currentConceptSummary(code);
//			} else {
//				diff = diff(code, code);
//			}
			for (String s : diff) {
				pw.println(s);
			}
			pw.flush();
			diff = new Vector<String>();}
			catch(Exception e){
				previous.getConcept(code).getProperties();
			}
		}
		// flip and look for concepts in kb2 that aren't in sceondSetConcepts
		codes = previous.getAllConcepts().keySet();
		for (URI code : codes) {
			if (current.getConcept(code) == null
			        || current.getConcept(code).getProperties().size() == 0) {
				pw.println(" ");
				pw.println(" ");
				pw.println("-----------------------------------------------------");
				pw.println(" ");
				pw.println("New Concept");
				// diff = kb2.getConcept(code).diff(null);
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
	
	
	private Vector<String> diff(URI code){


		Vector<String> diff = new Vector<String>();
		boolean changesmade = false;

		diff.add(" ");
		diff.add(" ");
		diff.add("-----------------------------------------------------");
		diff.add(" ");
		diff.add("Code: " + code.getFragment() + " " + current.getConceptNameByCode(code));
		Vector<URI> myParents = current.getParentCodesForConcept(code);
		Vector<URI> yourParents = previous.getParentCodesForConcept(code);
		
		boolean match = false;
		for (URI myParent : myParents) {
			for (URI yourParent : yourParents) {
				if (myParent.equals(yourParent)) {
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
		
		
		Vector<Role> yourAssociations = previous.getRolesForSource(code);

		Vector<Role> myAssociations = current.getRolesForSource(code);
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
		
		
			Vector<Property> myProperties = current.getAnnotationPropertiesForConcept(code);
			Vector<Property> yourProperties = previous.getAnnotationPropertiesForConcept(code);

			match = false;
			for (Property myProperty : myProperties) {
				for (Property yourProperty : yourProperties) {
					if (myProperty.equals(yourProperty)) {
						match = true;
					}
				}
				if (!match) {
					String propertyString = "";
					propertyString = myProperty.toString();
					diff.add(">>    Property:      " + myProperty.toString());
					changesmade = true;
				} else {
					diff.add("      Property:      " + myProperty.toString());
				}
				match = false;
			}
			
			

		
		
		/** Switch now and compare the other way.
		 * 
		 */
		
		// Parents
		diff.add(" ");
		diff.add(" ");
		for (URI yourParent : yourParents) {
			for (URI myParent : myParents) {
				if (yourParent.equals(myParent)) {
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
				if (myParent.equals(yourParent)) {
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
		try {
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
		} catch (Exception e) {
			c1.getProperties();
			c2.getProperties();
		}
		// change in Roles - alphabetical (These may be subsumed in parents)

		// Switch now and compare the other way.
		// Parents
		diff.add(" ");
		diff.add(" ");
		for (URI yourParent : yourParents) {
			for (URI myParent : myParents) {
				if (yourParent.equals(myParent)) {
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
	
	private Vector<String> currentConceptSummary(URI c1) {
		Vector<String> diff = new Vector<String>();
		diff.add("Code: " + c1.getFragment());
		// change in immediate parent
		Vector<URI> myParents = current.getParentCodesForConcept(c1);
		for (URI myParent : myParents) {
			ConceptProxy parent = current.getConcept(myParent);
			// diff.add("        Defining Concept:     " + myParent);
			diff.add("        Defining Concept:     " + parent.getCode() + " "
			        + parent.getName());
		}
		Vector<Property> myProperties = current.getAnnotationPropertiesForConcept(c1);
		for (Property myProperty : myProperties) {
			diff.add("         Property:    " + myProperty.toString());
		}
		return diff;

	}
	

}
