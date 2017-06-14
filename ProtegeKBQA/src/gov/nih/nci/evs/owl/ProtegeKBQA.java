/**
 * This application runs QA on OWL files exported from Protege. It uses the OWL
 * API to parse the data for QA. This program was converted from
 * OntylogKBQA.cpp, which was used to perform the same function on Ontylog
 * files.
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

@SuppressWarnings({ "unchecked", "rawtypes" })
class CompositeComparator implements Comparator {
	private final Comparator major;
	private final Comparator minor;

	public CompositeComparator(Comparator major, Comparator minor) {
		this.major = major;
		this.minor = minor;
	}

	@Override
	public int compare(Object o1, Object o2) {
		final int result = this.major.compare(o1, o2);
		if (result != 0) {
			return result;
		} else {
			return this.minor.compare(o1, o2);
		}
	}
}

/**
 * The Class ProtegeKBQA.
 * 
 * @author safrant
 */
@SuppressWarnings("unused")
public class ProtegeKBQA {

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {

		final ProtegeKBQA qaRun = new ProtegeKBQA();

		qaRun.configure(args);
		qaRun.performQA();
	}

	public void printHelp() {
		System.out.println("java -jar path/owlnciqa.jar"); //$NON-NLS-1$
		System.out.println();
		System.out.println("-c --config\t\tPath to configuration file"); //$NON-NLS-1$
		System.out.println("-i --input\t\tURI of vocabulary file (optional)"); //$NON-NLS-1$
		System.out.println("-o --output\t\tURI of output file(optional)"); //$NON-NLS-1$
		System.out.println();
		System.out
		        .println("If input and output not passed in as parameters, they must be specified in config file"); //$NON-NLS-1$
		System.exit(0);
	}

	/** The ontology. */
	// private OWLOntology ontology;
	private OWLKb ontology;

	/** The pw. */
	private PrintWriter pw;

	/** The ontology namespace. */
	private String ontologyNamespace;

	/** The manager. */

	/** The stys. */
	private Vector<String> stys;

	private Vector<String> styPairs;

	private URI retiredBranch;

	// HaspMaps storing the values that need to be reported
	// list of sources that don't require a Contributing_Source property
	/** The ignore sources. */
	private Vector<String> ignoreSources;
	// Haspmap stores all instances of Preferred_Name that appears in more than
	// one concept.
	/** The duplicate_pn. */
	HashMap<String, String> duplicate_pn = new HashMap<String, String>();
	// HashMap as holding bin for Preferred_Name, used to detect duplicates
	/** The pn_tbl. */
	HashMap<String, String> pn_tbl = new HashMap<String, String>();
	// Hashmap stores all instances of NCI|PT FULL-SYN that appears in more than
	// one concept.
	/** The duplicate_pt. */
	HashMap<String, String> duplicate_pt = new HashMap<String, String>();
	// HashMap as holding bin for PT, used to detect duplicates
	/** The pt_tbl. */
	HashMap<String, String> pt_tbl = new HashMap<String, String>();
	// Hashmap stores all instances of bunk characters in properties
	/** The badchar_newline. */
	HashMap<String, String> badchar_newline = new HashMap<String, String>();
	// HashMap stores instances of concepts with @
	/** The badchar_at. */
	HashMap<String, String> badchar_at = new HashMap<String, String>();
	// HashMap stores instances of concepts with pipes
	/** The badchar_pipe. */
	HashMap<String, String> badchar_pipe = new HashMap<String, String>();
	/** The badchar_tab */
	HashMap<String, String> badchar_tab = new HashMap<String, String>();

	// HashMap stores instances of concepts with duplicate roles.
	/** The duplicate_role. */
	HashMap<String, String> duplicate_role = new HashMap<String, String>();

	// HashMap stores instances of concepts with duplicate properties.
	/** The duplicate_property. */
	HashMap<String, String> duplicate_property = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one DEFINITION
	/** The multiple_ def. */
	HashMap<String, String> multiple_DEF = new HashMap<String, String>();
	// HashMap stores instances of concepts with
	/** The fully quoted definition **/
	HashMap<String, String> quoted_DEF = new HashMap<String, String>();
	/** The no_ def. */
	HashMap<String, String> no_DEF = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one NCI|PT FULL-SYN
	/** The multiple_ pt. */
	HashMap<String, String> multiple_PT = new HashMap<String, String>();
	// HashMap stores instances of concepts with no PT
	/** The no_ pt. */
	HashMap<String, String> no_PT = new HashMap<String, String>();

	/** The antiquated_no pt. */
	HashMap<String, String> antiquated_noPT = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one Preferred_Name
	/** The multiple_ pn. */
	HashMap<String, String> multiple_PN = new HashMap<String, String>();
	// HashMap stores instances of concepts with no Preferred_Name
	/** The no_ pn. */
	HashMap<String, String> no_PN = new HashMap<String, String>();

	// HashMap stores instances of concepts with bad Semantic_Type
	/** The bad_semantictypes. */
	HashMap<String, String> bad_semantictypes = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one Semantic_Type
	/** The multiple_ st. */
	HashMap<String, String> multiple_ST = new HashMap<String, String>();
	// HashMap stoes instances of concepts with no Semantic Type
	/** The no_ st. */
	HashMap<String, String> no_ST = new HashMap<String, String>();

	// HashMap stores instances of Def_Curator concepts w/o Def or Special
	// Review Names
	/** The no_ def review. */
	HashMap<String, String> no_DefReview = new HashMap<String, String>();
	
	// HashMap stores instances of Definitions with def-source NCI-DEFCURATOR
	// without a drug editor
	HashMap<String, String> no_DefCurator = new HashMap<String, String>();
	
	/** Drug Dictionary Editors */
	private Vector<String> drugEditors;

	// HashMap stores all instances of classes without at least one full syn for
	// a contributing source (exception: ignoreSources)
	/** The no_ syn for contributing source. */
	HashMap<String, String> no_SynForContributingSource = new HashMap<String, String>();

	// HashMap stores all instances of classes without at least one contributing
	// source for a full syn (exception: ignoreSources)
	/** The no_ contributing source for syn. */
	HashMap<String, String> no_ContributingSourceForSyn = new HashMap<String, String>();

	// HashMap stores all concepts where the NCI|PT FULL-SYN and Preferred_Name
	// don't match
	/** The nomatch_pnpt. */
	HashMap<String, String> nomatch_pnpt = new HashMap<String, String>();

	// HashMap stores all concepts where there is not ALT_DEFINITION to match
	// the Contributing_Source
	// Only bother checking this if there is a definition present
	/** The no_alt_def. */
	HashMap<String, String> no_alt_def = new HashMap<String, String>();

	// Hashmap stores all retired concepts that don't have a correct
	// Concept_Status
	/** The bad cs. */
	HashMap<String, String> badCS = new HashMap<String, String>();

	// Hashmap stored concepts that improperly have Concept_Status
	// Retired_Concept
	HashMap<String, String> badCS_active = new HashMap<String, String>();

	// Hashmap stores all instances of FDA UNII concepts without UNII codes.
	/** the missing UNII codes **/
	HashMap<String, String> missingUNII = new HashMap<String, String>();

	// Hashmap stores all instances of FDA UNII concepts without FDA PT.
	/** the missing UNII PTs **/
	HashMap<String, String> missingUNII_PT = new HashMap<String, String>();

	// Stores all instances where the PreferredName appears to be pasted into
	// another property
	HashMap<String, String> PNbug = new HashMap<String, String>();

	// Stores all instances where the property or qualifier value is an empty
	// string
	HashMap<String, String> emptyValue = new HashMap<String, String>();

	// Stores the character map for replacement
	HashMap<Character, UnicodeConverter> symbolMap = new HashMap<Character, UnicodeConverter>();

	// Stores the results of the check for high bit characters
	HashMap<String, String> highBitCharacters = new HashMap<String, String>();

	// Stores the results of the check for reolaced high bit characters
	HashMap<String, String> replacedHighBitCharacters = new HashMap<String, String>();

	// Count the number of missing alt-defs per contributing source
	HashMap<String, Integer> altDefSourceCount = new HashMap<String, Integer>();

	// Gather the FULL_SYNS with an LLT term-group that are not MedDRA
	HashMap<String, String> lltNotMedDRA = new HashMap<String, String>();

	// reads the System properties to get the location of the
	// nciqaowl.properties file
	/** The sys prop. */
	private static Properties sysProp = System.getProperties();

	/** The config file. */
	String configFile = null;

	/**
	 * Instantiates a new protege kbqa.
	 */
	public ProtegeKBQA() {

	}

	public ProtegeKBQA(OWLKb ontology) {
		this.ontology = ontology;
	}

	private void checkCharacter(ConceptProxy cls, char c) {
		String c1 = new String();
		// in case c is null?
		c1 = "" + c; //$NON-NLS-1$
		Vector<Property> properties = new Vector<Property>();
		properties = cls.getProperties();
		for (final Property property : properties) {
			if (property.getValue().contains(c1)) {
				if (c == '@') {
					this.badchar_at.put(cls.getCode(), property.getValue());
				} else if (c == '\n') {
					this.badchar_newline.put(cls.getCode(),
					        property.getValue());
				} else if (c == '|') {
					// New property - Value_Set_Location - will include pipes
					if (!property.getCode().equals(Messages.getString("ProtegeKBQA.Value_Set_Location"))) { //$NON-NLS-1$
						this.badchar_pipe.put(cls.getCode(),
						        property.getValue());
					}
				} else if (c == '\t') {
					this.badchar_tab.put(cls.toString(), property.getValue());
				}
			}
		}
	}

	private void checkContributingSouceAndAltDef(ConceptProxy cls) {
		// check that concepts with a contributing source and a Definition
		// should also have an Alt-def
		// ignoreSources.contains(synSource)
		final Vector<Property> contributingSources = cls
		        .getProperties(Messages.getString("ProtegeKBQA.Contributing_Source")); //$NON-NLS-1$

		final Vector<Property> defs = cls.getProperties(Messages.getString("ProtegeKBQA.Definition")); //$NON-NLS-1$
		// concepts with FDA_UNII_Code should not check for FDA alt-def
		final Vector<Property> unii_codes = cls.getProperties(Messages.getString("ProtegeKBQA.FDA_UNII_Code")); //$NON-NLS-1$
		boolean hasUNII = cls.getProperties().size() > 0;

		final String sCode = cls.getCode();
		List<String> exclude = Arrays.asList("UCUM", //$NON-NLS-1$
		        "MedDRA", "ICH", "HL7", "NCPDP"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		
		// if there are contributing sources and definitions
		if (contributingSources.size() > 0) {
			// check if there are any alt_defs.
			final Vector<Property> alt_defs = cls
			        .getProperties(Messages.getString("ProtegeKBQA.Alt_Definition")); //$NON-NLS-1$
			if ((alt_defs.size() > 0) && (contributingSources.size() > 0)) {

				// check to see if the alt_def corresponds to the contributing
				// sources
				for (final Property source : contributingSources) {
					// Check if the source is on the ignore list
					if (!this.ignoreSources.contains(source.getValue())) {
						boolean hasSource = false;
						for (final Property def : alt_defs) {
							if (def.getValue().contains(
							        "<def-source>" + source.getValue() //$NON-NLS-1$
							                + "</def-source>")) { //$NON-NLS-1$
								hasSource = true;
							} else if (def.getQualifier(Messages.getString("ProtegeKBQA.Def_Source")) != null //$NON-NLS-1$
							        && def.getQualifier(Messages.getString("ProtegeKBQA.Def_Source")) //$NON-NLS-1$
							                .getValue()
							                .equals(source.getValue())) {
								hasSource = true;
							} else if (source.getValue().equals("FDA") //$NON-NLS-1$
							        && hasUNII) {
								hasSource = true;
							}
						}
						if (!hasSource) {
							// UCUM, MedDRA, ICH, HL7 and NCPDP will never have
							// alt-def

							if (!exclude.contains(source.getValue())) {

								this.no_alt_def.put(
								        sCode,
								        "No ALT_DEFINITION for " //$NON-NLS-1$
								                + source.getValue());
								if (altDefSourceCount.containsKey(source
								        .getValue())) {
									Integer tempInt = altDefSourceCount
									        .get(source.getValue()) + 1;
									altDefSourceCount.put(source.getValue(),
									        tempInt);
								} else {
									altDefSourceCount.put(source.getValue(), 1);
								}
							}
						}
					}
				}
			} else {
				// if there is one contributing source of FDA and there is a
				// UNII_Code, discard
				if (hasUNII && contributingSources.size() == 1
				        && contributingSources.get(0).getValue().equals("FDA")) { //$NON-NLS-1$
					// if there is only one CS and it is FDA, and there is a
					// UNII_Code, don't add it.
				} else if (contributingSources.size() > 1) {
					boolean hasSource = true;
					for (final Property source : contributingSources) {
						// check each source against the ignore list. If any are
						// not on it, then it should have an alt-def
						if (!this.ignoreSources.contains(source.getValue())) {
							if (hasUNII && source.getValue().equals("FDA")) { //$NON-NLS-1$
								// if the source is FDA and there is a
								// UNII_Code, discard
							} else {

								
								if (!exclude.contains(source.getValue())) {
									hasSource = false;
									if (altDefSourceCount.containsKey(source
									        .getValue())) {
										Integer tempInt = altDefSourceCount
										        .get(source.getValue()) + 1;
										altDefSourceCount.put(
										        source.getValue(), tempInt);
									} else {
										altDefSourceCount.put(
										        source.getValue(), 1);
									}
								}
							}
						}
					}
					if (!hasSource) {
						this.no_alt_def.put(sCode, "0 ALT_DEFINTIONs"); //$NON-NLS-1$
					}
				} else {
					// only one contributing source and no alt-defs
					if (!this.ignoreSources.contains(contributingSources.get(0)
					        .getValue())) {

						if (!exclude.contains(contributingSources.get(0)
						        .getValue())) {

							this.no_alt_def.put(sCode, "No ALT_DEFINITION for " //$NON-NLS-1$
							        + contributingSources.get(0).getValue());
							if (altDefSourceCount
							        .containsKey(contributingSources.get(0)
							                .getValue())) {
								Integer tempInt = altDefSourceCount
								        .get(contributingSources.get(0)
								                .getValue()) + 1;
								altDefSourceCount.put(contributingSources
								        .get(0).getValue(), tempInt);
							} else {
								altDefSourceCount.put(contributingSources
								        .get(0).getValue(), 1);
							}
						}
					}
				}

			}
		}
	}

	/**
	 * Check definition existence and uniqueness.
	 * 
	 * @param c
	 *            the c
	 */
	private void checkDEFINITIONExistanceAndUniquness(ConceptProxy c) {
		// Check for concepts that have more than 1 DEFINITION property
		// or that have no DEFINITION property

		final Vector<Property> v = c.getProperties(Messages.getString("ProtegeKBQA.Definition")); //$NON-NLS-1$
		final Property semType = c.getProperty(Messages.getString("ProtegeKBQA.Semantic_Type")); //$NON-NLS-1$
		if (v.size() < 1) {
			this.no_DEF.put(c.getCode(), semType!=null ? semType.getValue() : "Null Semantic Type");
		} else if (v.size() > 1) {
			this.multiple_DEF.put(c.getCode(), new Integer(v.size()).toString()
			        + " DEFs"); //$NON-NLS-1$
		}

		// TODO build a metric to count the number of concepts with and without
		// definitions
	}

	private void checkDEFINITIONValue(ConceptProxy c) {
		final Vector<Property> v = c.getProperties(Messages.getString("ProtegeKBQA.Definition")); //$NON-NLS-1$
		for (final Property def : v) {
			final String defValue = def.getValue();
			if (defValue.startsWith("\"") && defValue.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
				final String defQuote = "Definition is fully quoted " //$NON-NLS-1$
				        + def.getValue();
				this.quoted_DEF.put(c.getCode(), defQuote);
			}
		}
	}

	/**
	 * Check drug dictionary.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkDrugDictionary(ConceptProxy cls) {
		// If there is a Def_Curator="NCI Drug Dictionary" then the
		// DEFINITION_Reviewer_Name
		// should be "SPECIAL_Review" or "DEFAULT_review"
		final Vector<Property> curators = cls.getProperties(Messages.getString("ProtegeKBQA.Def_Curator")); //$NON-NLS-1$
		if (curators.size() > 0) {
			final Vector<Property> defs = cls.getProperties(Messages.getString("ProtegeKBQA.Definition")); //$NON-NLS-1$
			for (final Property def : defs) {
				final String reviewerName = getQualifierValue(def, Messages.getString("ProtegeKBQA.Definition_Reviewer_Name"));
				if (!(reviewerName.equalsIgnoreCase(Messages.getString("ProtegeKBQA.Special_Review")) || reviewerName //$NON-NLS-1$
				        .equalsIgnoreCase(Messages.getString("ProtegeKBQA.Default_Review")))) { //$NON-NLS-1$
					this.no_DefReview.put(cls.getCode(), def.getValue());
				}
			}
		}
	}

//	private String getReviewerName(Property def) {
//		for(Qualifier qual: def.getQualifiers()){
//			if(qual.getCode().equals(Messages.getString("ProtegeKBQA.Definition_Reviewer_Name"))){
//				return qual.getValue();
//			}
//		}
//		return "";
//		
////		String name = ""; //$NON-NLS-1$
////		if (def.contains("<ncicp:Definition_Reviewer_Name>")) { //$NON-NLS-1$
////			try {
////				final int beginning = def
////				        .indexOf("<ncicp:Definition_Reviewer_Name>"); //$NON-NLS-1$
////				final int end = def
////				        .indexOf("</ncicp:Definition_Reviewer_Name>"); //$NON-NLS-1$
////				name = def.substring(beginning + 32, end);
////			} catch (final Exception e) {
////				e.printStackTrace();
////				System.out.println("Parse error at: " + def); //$NON-NLS-1$
////			}
////		}
////		return name;
//	}
	
	private String getQualifierValue(Property prop, String qualCode) {
		for(Qualifier qual: prop.getQualifiers()){
			if(qual.getCode().equals(qualCode)){
				return qual.getValue();
			}
		}
		return "";
		
	}

//	private String getDefinitionValue(String def) {
//		String name = ""; 
//		if (def.contains("<ncicp:Definition_Reviewer_Name>")) { //$NON-NLS-1$
//			try {
//				final int beginning = def.indexOf("<ncicp:def-definition>"); //$NON-NLS-1$
//				final int end = def.indexOf("</ncicp:def-definition>"); //$NON-NLS-1$
//				name = def.substring(beginning + 22, end);
//			} catch (final Exception e) {
//				e.printStackTrace();
//				System.out.println("Parse error at: " + def); //$NON-NLS-1$
//			}
//		}
//		return name;
//	}

	private void checkDuplicateProperties(ConceptProxy c) {
		final Vector<Property> props = c.getProperties();
		final Vector<Property> propMap = new Vector<Property>();
		for (final Property prop : props) {
			if (!propMap.contains(prop)) {
				propMap.add(prop);
			} else {
				this.duplicate_property.put(c.getCode()+":"+c.getName(), prop.getCode() + ":" + prop.getValue());
			}
		}
	}

	/**
	 * Check duplicate roles.
	 * 
	 * @param c
	 *            the c
	 */
	private void checkDuplicateRoles(ConceptProxy cls) {
		// Check that the same role does not occur twice in a concept

		final Vector<Role> roles = cls.getRoles();
		// Vector<String> v = getRestrictions(c);
		// String code = getConceptCode(c);
		final String storeCode = cls.getCode();
		final Vector<String> roleMap = new Vector<String>();

		for (final Role role : roles) {
			final String roleName = role.getRelation().getName();
			final String roleTarget = role.getTarget().getCode();
			final String roleDef = roleName + " " + roleTarget; //$NON-NLS-1$
			if (!roleMap.contains(roleDef)) {
				roleMap.add(roleDef);
			} else {
				this.duplicate_role.put(roleDef, cls.getCode());
			}
		}
	}

	private void checkHighBitCharacters(ConceptProxy cls) {
		// pw.println("Property values containing High Bit characters \n");
		Vector<Property> properties = new Vector<Property>();
		properties = cls.getProperties();
		boolean hasIssue = false;
		for (final Property property : properties) {
			String propertyValue = property.getValue();
			for (Qualifier qual:property.getQualifiers()){
				propertyValue = propertyValue +" "+ qual.getValue();
			}

			String highBits = checkHighBitCharacters(propertyValue);

			if (highBits.length() > 0) {
				String bitReport = "Property: " + property.getName() + "\t" //$NON-NLS-1$
				        + highBits + "Property: " + property.getValue(); //$NON-NLS-1$
				if (bitReport.contains("Unexpected Character")) { //$NON-NLS-1$
					highBitCharacters.put(cls.getCode(), bitReport);
				} else {
					replacedHighBitCharacters.put(cls.getCode(), bitReport);
				}

			}


		}

	}

	private String checkHighBitCharacters(String propertyValue) {
		// Check each character and swap out any unicode in the map.
		// Toss exception if non-swappable character found
		String returnString = ""; //$NON-NLS-1$
		final int len = propertyValue.length();
		for (int i = 0; i < len; i++) {
			final Integer iPosition = new Integer(i + 1);
			final char c = propertyValue.charAt(i);
			final int cast = c;
			if (cast >= 32 && cast <= 126) {
				// No problems. These are accepted characters
				// No need to go iterating through the unicode converter
			} else if (symbolMap.containsKey(c)) {
				returnString = returnString + "Character replaced " + "Char: " //$NON-NLS-1$ //$NON-NLS-2$
				        + propertyValue.charAt(i) + " Ascii code:" + cast + " " //$NON-NLS-1$ //$NON-NLS-2$
				        + symbolMap.get(c).getCharDescription() + " Position: " //$NON-NLS-1$
				        + iPosition + "    \t"; //$NON-NLS-1$
				// throw new Exception("Replaced character " +
				// Character.toString(c) + " " +
				// symbolMap.get(c).getCharDescription());
			} else {
				// Allow string to pass through, but note it.
				// throw new Exception("Unexpected character " +
				// Character.toString(c));
				returnString = returnString + "Unexpected Character " //$NON-NLS-1$
				        + "Char: " + propertyValue.charAt(i) + " Ascii code:" //$NON-NLS-1$ //$NON-NLS-2$
				        + cast + " Position: " + iPosition + "   \t "; //$NON-NLS-1$ //$NON-NLS-2$
			}

		}

		return returnString;
	}

	/**
	 * Check ncipt full syn existence and uniqueness.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkNCIPTFullSynExistenceAndUniqueness(ConceptProxy cls) {
		// Check that there is one and only one Full-Syn NCI|PT
		// If AQ or CTRM concept, flag it as antiquated. Perhaps separate them
		// into own HashMap?
//		final Vector<String> syns = getNCIPTFullSyn(cls);
		 Vector<String> syns=getFullSynBySourceAndGroup(cls,"NCI","PT");
		 if(syns.size()<1){
			 syns = getFullSynBySourceAndGroup(cls,"NCI","HD");
		 }
		 if(syns.size()<1){
			 syns= getFullSynBySourceAndGroup(cls, "CTRM", "PT");
		 }
		if (syns.size() < 1) {
			// if the concept has an AQ Full-Syn, this can replace the NCI|PT
//			final Vector<String> aqs = getAQ_CTRMFullSyn(cls);
			final Vector<String> aqs = getFullSynByGroup(cls,"AQ");
			if (aqs.size() == 0) {
				this.no_PT.put(cls.getCode(),
				        new Integer(syns.size()).toString() + " PTs"); //$NON-NLS-1$
			} else {
				this.antiquated_noPT.put(cls.getCode(),
				        new Integer(syns.size()).toString() + " PTs"); //$NON-NLS-1$
			}
		} else if (syns.size() > 1) {
			this.multiple_PT.put(cls.getCode(),
			        new Integer(syns.size()).toString() + " PTs"); //$NON-NLS-1$
		}
	}

	/**
	 * Check pn existence and uniqueness.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkPNExistenceAndUniqueness(ConceptProxy cls) {
		// Check that concept has one and only one Preferred Name
		// Vector<String> pn = getPropertyValues(cls, "Preferred_Name");
		final Vector<Property> pn = cls.getProperties(Messages.getString("ProtegeKBQA.PreferredName")); //$NON-NLS-1$

		// String sCode = getConceptCode(cls);
		if (pn.size() < 1) {
			this.no_PN.put(cls.getCode(), new Integer(pn.size()).toString()
			        + " PNs"); //$NON-NLS-1$
		} else if (pn.size() > 1) {
			this.multiple_PN.put(cls.getCode(),
			        new Integer(pn.size()).toString() + " PNs"); //$NON-NLS-1$
		}
	}

	/**
	 * Check pn full syn match.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkPNFullSynMatch(ConceptProxy cls) {
		// check that value of PreferredTerm is the same as Full-Syn NCI|PT

//		final Vector<String> syns = getNCIPTFullSyn(cls);
		final Vector<String> syns = getFullSynBySourceAndGroup(cls,"NCI","PT");
		final Property pn = cls.getProperty(Messages.getString("ProtegeKBQA.PreferredName")); //$NON-NLS-1$
		for (int i = 0; i < syns.size(); i++) {
			final String pt = syns.get(i);
			if (!pt.equals(pn.getValue())) {
				this.nomatch_pnpt.put(cls.getCode(), "PN:" + pn.getValue() //$NON-NLS-1$
				        + " PT:" + pt); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Check for different concepts that have the same Full-Syn NCI|PT
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkSameAtoms(ConceptProxy cls) {
		// Check for different concepts that have the same Full-Syn NCI|PT

		final Vector<Property> props = cls.getProperties(Messages.getString("ProtegeKBQA.FULL_SYN")); //$NON-NLS-1$

		for (final Property prop : props) {
			final String pt = prop.getValue();
//			if (pt.indexOf("<ncicp:term-source>NCI</ncicp:term-source>") > 0 //$NON-NLS-1$
//			        & pt.indexOf("<ncicp:term-group>PT</ncicp:term-group>") > 0) { //$NON-NLS-1$
//				if (!this.pt_tbl.containsKey(pt)) {
//					// if the list of pt's doesn't include this, then add it
//					this.pt_tbl.put(pt, cls.getCode());
//				} else {
//					// if the list of pt's includes this, we have a duplicate
//					// record the id's of the two classes for reporting
//					final String firstCode = this.pt_tbl.get(pt);
//					final String storeCode = firstCode + " " + cls.getCode(); //$NON-NLS-1$
//					this.duplicate_pt.put(XML2Pipe(pt), storeCode);
//				}
//			} else if (prop.getQualifiers().size() > 0) {
				final Qualifier source = prop.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")); //$NON-NLS-1$
				final Qualifier group = prop.getQualifier(Messages.getString("ProtegeKBQA.Term_Group")); //$NON-NLS-1$
				if (source != null && group != null) {
					if (source.getValue().equals("NCI") //$NON-NLS-1$
					        && group.getValue().equals("PT")) { //$NON-NLS-1$
						if (!this.pt_tbl.containsKey(pt)) {
							this.pt_tbl.put(pt, cls.getCode());
						} else {
							final String firstCode = this.pt_tbl.get(pt);
							final String storeCode = firstCode + " " //$NON-NLS-1$
							        + cls.getCode();
							this.duplicate_pt.put(XML2Pipe(pt), storeCode);
						}
					}
				}
//			}
		}

	}

	/**
	 * Check same preferred name.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkSamePreferredName(ConceptProxy cls) {

		final Vector<Property> properties = cls.getProperties(Messages.getString("ProtegeKBQA.Preferred_Name")); //$NON-NLS-1$

		for (final Property prop : properties) {
			final String pn = prop.getValue();

			if (!this.pn_tbl.containsKey(pn)) {
				this.pn_tbl.put(pn, cls.getCode());
			} else {
				final String firstCode = this.pn_tbl.get(pn);
				final String storeCode = firstCode + " " + cls.getCode(); //$NON-NLS-1$
				this.duplicate_pn.put(pn, storeCode);
			}
		}

	}

	/**
	 * Check semantic type existence and uniqueness.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkSemanticTypeExistenceAndUniqueness(ConceptProxy cls) {
		// Check that any values for the Semantic_Type property are valid by
		// comparing them to an external semanticType config file.

		final Vector<Property> styValues = cls.getProperties(Messages.getString("ProtegeKBQA.Semantic_Type")); //$NON-NLS-1$

		if (styValues.size() < 1) {

			// check if retired
			if (!cls.isRetired()) {

				this.no_ST.put(cls.getCode(),
				        new Integer(styValues.size()).toString()
				                + " SemanticTypes"); //$NON-NLS-1$
			}
		} else if (styValues.size() > 1) {
			if (!checkSemanticPairs(styValues)) {
				String semanticTypeValues = ""; //$NON-NLS-1$
				for (final Property prop : styValues) {
					semanticTypeValues = semanticTypeValues + prop.getValue()
					        + "|"; //$NON-NLS-1$
				}
				semanticTypeValues = semanticTypeValues.substring(0,
				        semanticTypeValues.length() - 1);
				this.multiple_ST.put(cls.getCode(),
				        new Integer(styValues.size()).toString()
				                + " SemanticTypes " + semanticTypeValues); //$NON-NLS-1$
			}
		}
	}

	private boolean checkSemanticPairs(Vector<Property> styValues) {

		Collections.sort(styValues);
		String styValue = ""; //$NON-NLS-1$
		for (final Property prop : styValues) {
			styValue = styValue + prop.getValue() + "|"; //$NON-NLS-1$
		}
		styValue = styValue.substring(0, styValue.length() - 1);

		for (final String group : this.styPairs) {
			if (group.equals(styValue)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check semantic types.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkSemanticTypes(ConceptProxy cls) {
		// Check that any values for the Semantic_Type property are valid by
		// comparing them to an external semanticType config file.
		final Vector<Property> props = cls.getProperties(Messages.getString("ProtegeKBQA.Semantic_Type")); //$NON-NLS-1$
		for (final Property prop : props) {
			if (!this.stys.contains(prop.getValue())) {
				this.bad_semantictypes.put(cls.getCode(), prop.getValue());
			}
		}
	}

	/**
	 * Check term source.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkTermSource(ConceptProxy cls) {
		// Each Contributing_Source should have a matching FULL_SYN term source
		// Each FULL_SYN term source should have a matching Contributing_Source
		// ignoreSources.contains(synSource)

		final Vector<Property> syns = cls.getProperties(Messages.getString("ProtegeKBQA.FULL_SYN")); //$NON-NLS-1$
		final Vector<Property> contributingSources = cls
		        .getProperties(Messages.getString("ProtegeKBQA.Contributing_Source")); //$NON-NLS-1$
		if (contributingSources.size() > 0 && syns.size() > 0) {
			final HashMap<String, String> synMap = getSynSources(syns);
			for (final Property contributingSource : contributingSources) {

				if (!(this.ignoreSources
				        .contains(contributingSource.getValue()))) {
					boolean hasSource = false;
					for (final Property syn : syns) {
						if (syn.getValue().contains(
						        "<ncicp:term-source>" //$NON-NLS-1$
						                + contributingSource.getValue()
						                + "</ncicp:term-source>")) { //$NON-NLS-1$
							hasSource = true;
						} else if (syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")) != null) { //$NON-NLS-1$
							if (syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")).getValue() //$NON-NLS-1$
							        .equals(contributingSource.getValue())) {
								hasSource = true;
							}
						}
					}
					if (!hasSource) {
						this.no_SynForContributingSource.put(cls.getCode(),
						        contributingSource.getValue());
					}

				}
			}
			for (final Property syn : syns) {
				final String fullSyn = concatenateFullSyn(syn);
				final String synSource = synMap.get(fullSyn);
				boolean debub = this.ignoreSources.contains(synSource);
				if (!(synSource.equals("NCI"))) { //$NON-NLS-1$
					if (!(synSource.equals("")) //$NON-NLS-1$
					        && !this.ignoreSources.contains(synSource)) {
						boolean found = false;
						for (final Property contributingSource : contributingSources) {
							if (contributingSource.getValue().equals(synSource)) {
								found = true;
							}
						}
						if (!found) {
							this.no_ContributingSourceForSyn.put(cls.getCode(),
							        syn.getValue() + "|" + synSource); //$NON-NLS-1$

						}
					}
				}
			}
		}
	}

	/**
	 * Check FDA UNII
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkFDA_UNII(ConceptProxy cls) {
		// need to pull all concepts with Concept_In_Subset association to
		// C63923
		// FDA Established Names and Unique Ingredient Identifier Codes
		// Terminology
		// Within these concepts, check for a FDA UNII Code property
		// Within these concepts, check for a PT with source FDA. With matching
		// source code?

		final Vector<Property> unii_syn = cls.getProperties(Messages.getString("ProtegeKBQA.FULL_SYN")); //$NON-NLS-1$
		final Vector<Property> subsets = cls.getProperties(Messages.getString("ProtegeKBQA.Concept_In_Subset")); //$NON-NLS-1$
		boolean hasFULL_SYN = false;
		boolean hasUNII_Code = true;
		for (final Property subset : subsets) {
			if (subset.getValue().contains(Messages.getString("ProtegeKBQA.FDA_UNII_Code_Terminology"))) { //$NON-NLS-1$
				final String unii_code = cls.getProperty(Messages.getString("ProtegeKBQA.FDA_UNII_Code")) //$NON-NLS-1$
				        .getValue();
				if (unii_code == null || unii_code.length() == 0) {
					hasUNII_Code = false;
				}
				for (final Property syn : unii_syn) {
//					if (syn.getValue().contains("FDA")) { //$NON-NLS-1$
//						hasFULL_SYN = true;
//					}
					if (getQualifierValue(syn, Messages.getString("ProtegeKBQA.term_source")).equals("FDA")){
						hasFULL_SYN = true;
					}
					
					
				}
				if (!hasFULL_SYN || !hasUNII_Code) {
					String message = " "; //$NON-NLS-1$
					if (!hasFULL_SYN) {
						message = message.concat("Missing FDA Full Syn "); //$NON-NLS-1$
					}
					if (!hasUNII_Code) {
						message = message.concat("Missing UNII Code "); //$NON-NLS-1$
					}
					this.missingUNII.put(cls.toString(), message);
				}
			}
		}

	}
	
	
	

	/**
	 * Config print writer.
	 * 
	 * @param outputfile
	 *            the outputfile
	 * @throws Exception
	 */
	private void configPrintWriter(URI outputfile) throws Exception {
		try {
			final File file = new File(outputfile);
			// this.pw = new PrintWriter(file);
			this.pw = new PrintWriter(file, StandardCharsets.UTF_8.name());

		} catch (final Exception e) {
			System.out.println("Error in PrintWriter"); //$NON-NLS-1$
			throw e;
		}
	}

	/**
	 * Configure.
	 */
	private void configure(String[] args) {
		URI physicalURI = null;
		URI outputFile = null;
		try {

			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].equalsIgnoreCase("-c") //$NON-NLS-1$
					        || args[i].equalsIgnoreCase("--config")) { //$NON-NLS-1$
						this.configFile = args[++i];
					} else if (args[i].equalsIgnoreCase("-i") //$NON-NLS-1$
					        || args[i].equalsIgnoreCase("--input")) { //$NON-NLS-1$
						physicalURI = URI.create(args[++i]);
					} else if (args[i].equalsIgnoreCase("-o") //$NON-NLS-1$
					        || args[i].equalsIgnoreCase("--output")) { //$NON-NLS-1$
						outputFile = URI.create(args[++i]);
					} else {
						printHelp();
					}
				}
			} else {
				printHelp();
			}

			if (this.configFile == null) {
				final String filename = sysProp
				        .getProperty(Messages.getString("ProtegeKBQA.1")); //$NON-NLS-1$
				this.configFile = filename;
			}
			System.out.println("Config file at: " + this.configFile); //$NON-NLS-1$

			final Properties props = new Properties();
			props.load(new FileInputStream(this.configFile));
			this.ontologyNamespace = props.getProperty("namespace"); //$NON-NLS-1$
			if (physicalURI == null) {
				physicalURI = URI.create(props.getProperty("physicalURI")); //$NON-NLS-1$
			}
			System.out.println("Input file is " + physicalURI.toString());
			this.stys = setFromConfig(props.getProperty("semantictypefile")); //$NON-NLS-1$
			this.styPairs = setFromConfig(props
			        .getProperty("semanticPairsFile")); //$NON-NLS-1$
			this.ignoreSources = setFromConfig(props.getProperty("ignorefile")); //$NON-NLS-1$
			this.retiredBranch = URI.create(props.getProperty("deprecatedConceptBranch")); //$NON-NLS-1$

			if (outputFile == null) {
				outputFile = URI.create(props.getProperty("outputfile")); //$NON-NLS-1$
			}
			if (outputFile == null || outputFile.toString().length() == 0) {
				System.out.println("No output file specified"); //$NON-NLS-1$
				printHelp();
			}
			System.out.println("Output file is " + outputFile.toString()); //$NON-NLS-1$

			readSymbolMap(props.getProperty("symbolMap")); //$NON-NLS-1$

			configPrintWriter(outputFile);
			this.ontology = new OWLKb(physicalURI, this.ontologyNamespace);

		} catch (final java.io.FileNotFoundException e) {
			System.out.println("Error in reading config files"); //$NON-NLS-1$
			e.printStackTrace();
			System.exit(1);
		} catch (final IllegalArgumentException e) {
			System.out.println("Error in parameter"); //$NON-NLS-1$
			e.printStackTrace();
			System.exit(1);
		} catch (final Exception e) {
			System.out.println("Error in reading ontology"); //$NON-NLS-1$
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Creates the uri.
	 * 
	 * @param className
	 *            the class name
	 * @return the uRI
	 */
	public URI createURI(String className) {
		return URI.create(this.ontologyNamespace + "#" + className); //$NON-NLS-1$
	}

	private Vector<String> getAQ_CTRMFullSyn(ConceptProxy c) {
		final String propertyname = Messages.getString("ProtegeKBQA.FULL_SYN"); //$NON-NLS-1$
		final Vector<String> out = new Vector<String>();
		final Vector<Property> v = c.getProperties(propertyname);
		for (final Property prop : v) {
			final String pt = prop.getValue();
			if (pt.indexOf("ncicp:") > 0) { //$NON-NLS-1$
				if (pt.indexOf("<ncicp:term-source>CTRM</ncicp:term-source>") > 0 //$NON-NLS-1$
				        || pt.indexOf("<ncicp:term-group>AQ</ncicp:term-group>") > 0 //$NON-NLS-1$
				        || pt.indexOf("<ncicp:term-group>HD</ncicp:term-group>") > 0) { //$NON-NLS-1$
					final int start = pt.indexOf("<ncicp:term-name>") + 17; //$NON-NLS-1$
					final int end = pt.indexOf("</ncicp:term-name>"); //$NON-NLS-1$
					String fullSyn = pt.substring(start, end);
					if (fullSyn.indexOf("&amp;") > 0) { //$NON-NLS-1$
						fullSyn = removeAmpersandReplacement(fullSyn);
					}
					out.add(fullSyn);
				}
			} else {

				if (prop.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")).equals("CTRM") //$NON-NLS-1$ //$NON-NLS-2$
				        || prop.getQualifier(Messages.getString("ProtegeKBQA.Term_Group")).getValue() //$NON-NLS-1$
				                .equals("AQ") //$NON-NLS-1$
				        || prop.getQualifier(Messages.getString("ProtegeKBQA.Term_Group")).getValue() //$NON-NLS-1$
				                .equals("HD")) { //$NON-NLS-1$
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}
	
	private Vector<String> getFullSynBySourceAndGroup(ConceptProxy c, String source, String group){
		final Vector<String> out = new Vector<String>();
		for(Property prop:c.getProperties()){
			String sourceQual = getQualifierValue(prop,Messages.getString("ProtegeKBQA.Term_Source") );
			String groupQual = getQualifierValue(prop, Messages.getString("ProtegeKBQA.Term_Group"));
			if(sourceQual!=null && groupQual !=null){
			if(sourceQual.equals(source) &&
					groupQual.equals(group)){
				out.add(prop.getValue());
			}}
		}
		return out;
	}
	
	
	private Vector<String> getFullSynBySource(ConceptProxy c, String source){
		final Vector<String> out = new Vector<String>();
		for(Property prop:c.getProperties()){
			String sourceQual = getQualifierValue(prop,Messages.getString("ProtegeKBQA.Term_Source") );
			if(sourceQual!=null ){
			if(sourceQual.equals(source)){
				out.add(prop.getValue());
			}}
		}
		return out;
	}
	
	private Vector<String> getFullSynByGroup(ConceptProxy c, String group){
		final Vector<String> out = new Vector<String>();
		for(Property prop:c.getProperties()){
			String groupQual = getQualifierValue(prop, Messages.getString("ProtegeKBQA.Term_Group"));
			if( groupQual !=null){
			if(	groupQual.equals(group)){
				out.add(prop.getValue());
			}}
		}
		return out;
	}

	private Vector<String> getNCIPTFullSyn(ConceptProxy c) {
		final String propertyname = Messages.getString("ProtegeKBQA.FULL_SYN"); //$NON-NLS-1$
		final Vector<String> out = new Vector<String>();
		final Vector<Property> v = c.getProperties(propertyname);
		for (final Property prop : v) {
			final String pt = prop.getValue();
			if (pt.indexOf("<ncicp:term-source>NCI</ncicp:term-source>") > 0 //$NON-NLS-1$
			        & pt.indexOf("<ncicp:term-group>PT</ncicp:term-group>") > 0) { //$NON-NLS-1$
				final int start = pt.indexOf("<ncicp:term-name>") + 17; //$NON-NLS-1$
				final int end = pt.indexOf("</ncicp:term-name>"); //$NON-NLS-1$
				String fullSyn = pt.substring(start, end);
				if (fullSyn.indexOf("&amp;") > 0) { //$NON-NLS-1$
					fullSyn = removeAmpersandReplacement(fullSyn);
				}
				out.add(fullSyn);
			} else {
				boolean isPT = false, isNCI = false;
				for (final Qualifier qual : prop.getQualifiers()) {
					if (qual.getValue().equals(Messages.getString("ProtegeKBQA.PT"))) { //$NON-NLS-1$
						isPT = true;
					}
					if (qual.getValue().equals("NCI")) { //$NON-NLS-1$
						isNCI = true;
					}
				}
				if (isNCI && isPT) {
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}

	/**
	 * Gets the syn sources.
	 * 
	 * @param syns
	 *            the syns
	 * @return the syn sources
	 */
	private HashMap<String, String> getSynSources(Vector<Property> syns) {
		final HashMap<String, String> synMap = new HashMap<String, String>();
		for (final Property synProp : syns) {
			
			final String syn = synProp.getValue();
			String source = getQualifierValue(synProp, Messages.getString("ProtegeKBQA.Term_Source"));
			
//			
//			final int beginning = syn.indexOf("<ncicp:term-source>"); //$NON-NLS-1$
//			final int end = syn.indexOf("</ncicp:term-source>"); //$NON-NLS-1$
//			if (beginning > 0 && end > 20) {
//				source = syn.substring(beginning + 19, end);
//			} else if (synProp.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")) != null) { //$NON-NLS-1$
//				source = synProp.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")).getValue(); //$NON-NLS-1$
//				// System.out.println("Bad property " + syn);
//			}
			final String fullSyn = concatenateFullSyn(synProp);
//			if (beginning > 0) {
//				source = syn.substring(beginning + 19, end);
//			} else {
//				final Vector<Qualifier> quals = synProp.getQualifiers();
//				for (final Qualifier qual : quals) {
//					if (qual.getName().equals(Messages.getString("ProtegeKBQA.Term_Source"))) { //$NON-NLS-1$
//						source = qual.getValue();
//					}
//				}
//			}



			synMap.put(fullSyn, source!=null ? source : "" );
			
		}
		return synMap;
	}

//	private String concatenateFullSyn(String FSvalue) {
//		// <ncicp:ComplexTerm><ncicp:term-name>11-Cyclopropyl-5,11-dihydro-4-methyl-6H-dipyrido(3,2-b:2',3'-e)(1,4)diazepin-6-one</ncicp:term-name><ncicp:term-group>SN</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source></ncicp:ComplexTerm>
//
//		String source = ""; //$NON-NLS-1$
//		int beginning = FSvalue.indexOf("<ncicp:term-source>"); //$NON-NLS-1$
//		int end = FSvalue.indexOf("</ncicp:term-source>"); //$NON-NLS-1$
//		source = FSvalue.substring(beginning + 19, end);
//
//		String group = ""; //$NON-NLS-1$
//		beginning = FSvalue.indexOf("<ncicp:term-group>"); //$NON-NLS-1$
//		end = FSvalue.indexOf("</ncicp:term-group>"); //$NON-NLS-1$
//		group = FSvalue.substring(beginning + 18, end);
//
//		String value = ""; //$NON-NLS-1$
//		beginning = FSvalue.indexOf("<ncicp:term-name>"); //$NON-NLS-1$
//		end = FSvalue.indexOf("</ncicp:term-name>"); //$NON-NLS-1$
//		value = FSvalue.substring(beginning + 17, end);
//		
//
//		return source + "|" + group + "|" + value; //$NON-NLS-1$ //$NON-NLS-2$
//	}

	private String concatenateFullSyn(Property synProp) {
		String source = ""; //$NON-NLS-1$
		String group = ""; //$NON-NLS-1$
		String value = ""; //$NON-NLS-1$

		final Vector<Qualifier> quals = synProp.getQualifiers();
		for (final Qualifier qual : quals) {
			if (qual.getName().equals(Messages.getString("ProtegeKBQA.Term_Source"))) { //$NON-NLS-1$
				source = qual.getValue();
			} else if (qual.getName().equals(Messages.getString("ProtegeKBQA.Term_Group"))) { //$NON-NLS-1$
				group = qual.getValue();
			}

			value = synProp.getValue();

		}

		return source + "|" + group + "|" + value; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Perform qa.
	 */
	private void performQA() {

		// Retired concepts should not be considered for QA
		// qaRetiredConcepts();

		// This will loop through the classes, writing the findings to a set of
		// global HashMaps or Vectors. At the end, will write out the results to
		// the Output file.
		for (final URI code : this.ontology.getAllConceptCodes()) {

			final ConceptProxy cls = this.ontology.getConcept(code);
			// if (cls.getCode().equals("Mouse_Nasal_Sinuses")){
			// int debug=0;
			// }
			final Vector<Property> mProps = cls.getProperties();
			if (!cls.isRetired()) {
				checkHighBitCharacters(cls);
				checkSamePreferredName(cls);
				checkSameAtoms(cls);
				checkDuplicateRoles(cls);
				checkDuplicateProperties(cls);
				checkDEFINITIONExistanceAndUniquness(cls);
				checkDEFINITIONValue(cls);
				checkPNExistenceAndUniqueness(cls);
				checkPNFullSynMatch(cls);
				checkNCIPTFullSynExistenceAndUniqueness(cls);
				checkTermSource(cls);
				checkMedDRA_LLT(cls);
				checkDrugDictionary(cls);
				checkDefCurator(cls);
				checkContributingSouceAndAltDef(cls);
				checkSemanticTypes(cls);
				checkSemanticTypeExistenceAndUniqueness(cls);
				checkFDA_UNII(cls);
				checkCharacter(cls, '\n');
				checkCharacter(cls, '@');
				checkCharacter(cls, '|');
				checkCharacter(cls, '\t');
				checkPreferredNameBug(cls);
				checkForEmptyQualifiers(cls);
			}
		}

		// now print out all the issues discovered.
		printReport();

		// close printwriter
		this.pw.close();
	}

	/**
	 * Prints the report.
	 */
	@SuppressWarnings({ "rawtypes" })
	private void printReport() {

		this.pw.println("Replaced high bit characters: " //$NON-NLS-1$
		        + this.replacedHighBitCharacters.size());
		List sortedReturn = sortHashMapByKey(this.replacedHighBitCharacters);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		
		this.pw.println();
		this.pw.println("Property values containing an incorrect Semantic_Type: " //$NON-NLS-1$
		        + this.bad_semantictypes.size());
		sortedReturn = sortHashMapByKey(this.bad_semantictypes);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with no Semantic_Type property: " //$NON-NLS-1$
		        + this.no_ST.size());
		sortedReturn = sortHashMapByKey(this.no_ST);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Retired Concepts with bad Concept_Status: " //$NON-NLS-1$
		        + this.badCS.size());
		sortedReturn = sortHashMapByKey(this.badCS);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Active concepts with retired Concept_Status: " //$NON-NLS-1$
		        + this.badCS_active.size());
		sortedReturn = sortHashMapByKey(this.badCS_active);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with duplicate roles within: " //$NON-NLS-1$
		        + this.duplicate_role.size());
		sortedReturn = sortHashMapByKey(this.duplicate_role);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with duplicate properties within: " //$NON-NLS-1$
		        + this.duplicate_property.size());
		sortedReturn = sortHashMapByKey(this.duplicate_property);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with multiple NCI|PT FULL-SYN properties: " //$NON-NLS-1$
		        + this.multiple_PT.size());
		sortedReturn = sortHashMapByKey(this.multiple_PT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with quotes around entire definition: " //$NON-NLS-1$
		        + this.quoted_DEF.size());
		sortedReturn = sortHashMapByKey(this.quoted_DEF);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with no Preferred_Name property: " //$NON-NLS-1$
		        + this.no_PN.size());
		sortedReturn = sortHashMapByKey(this.no_PN);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with multiple Preferred_Name properties: " //$NON-NLS-1$
		        + this.multiple_PN.size());
		sortedReturn = sortHashMapByKey(this.multiple_PN);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts where the NCI|PT and Preferred_Name don't match: " //$NON-NLS-1$
		        + this.nomatch_pnpt.size());
		sortedReturn = sortHashMapByKey(this.nomatch_pnpt);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		

		this.pw.println();
		this.pw.println("Possible instances of Preferred_Name / Last property bug: " //$NON-NLS-1$
		        + this.PNbug.size());
		sortedReturn = sortHashMapByKey(this.PNbug);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		

		this.pw.println();
		this.pw.println("Concepts with no NCT|PT FULL-SYN property (Excludes HD, AQ and CTRM concepts): " //$NON-NLS-1$
		        + this.no_PT.size());
		sortedReturn = sortHashMapByKey(this.no_PT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Empty properties and qualifiers:" //$NON-NLS-1$
		        + this.emptyValue.size());
		sortedReturn = sortHashMapByKey(this.emptyValue);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		
		this.pw.println();
		this.pw.println("High bit characters: " + this.highBitCharacters.size()); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.highBitCharacters);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character @: " //$NON-NLS-1$
		        + this.badchar_at.size());
		sortedReturn = sortHashMapByKey(this.badchar_at);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character \\n : " //$NON-NLS-1$
		        + this.badchar_newline.size());
		sortedReturn = sortHashMapByKey(this.badchar_newline);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character | : " //$NON-NLS-1$
		        + this.badchar_pipe.size());
		sortedReturn = sortHashMapByKey(this.badchar_pipe);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing \\t : " //$NON-NLS-1$
		        + this.badchar_tab.size());
		sortedReturn = sortHashMapByKey(this.badchar_tab);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}


		this.pw.println();
		this.pw.println("Concepts where the FDA_UNII_Code or FDA PT are missing for UNII Concepts: " //$NON-NLS-1$
		        + this.missingUNII.size());
		sortedReturn = sortHashMapByKey(this.missingUNII);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with an LLT FULL_SYN that is not MedDRA: " //$NON-NLS-1$
		        + this.lltNotMedDRA.size());
		sortedReturn = sortHashMapByKey(this.lltNotMedDRA);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}




		this.pw.println();
		this.pw.println("Concepts with multiple DEFINITION properties: " //$NON-NLS-1$
		        + this.multiple_DEF.size());
		sortedReturn = sortHashMapByKey(this.multiple_DEF);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}


		this.pw.println();
		this.pw.println("Def_Curator concepts without proper Definition_Review_Name: " //$NON-NLS-1$
		        + this.no_DefReview.size());
		sortedReturn = sortHashMapByKey(this.no_DefReview);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		
		this.pw.println();
		this.pw.println("NCI-DEFCURATOR sources Definitions without proper Definition_Reviewer_Name: "
				+ this.no_DefCurator.size());
		sortedReturn = sortHashMapByKey(this.no_DefCurator);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("*  Concepts with multiple Semantic_Type properties: " //$NON-NLS-1$
		        + this.multiple_ST.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$

		sortedReturn = sortHashMapByKey(this.multiple_ST);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("Preferred Names duplicated between concepts: " //$NON-NLS-1$
		        + this.duplicate_pn.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.duplicate_pn);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("NCI|PT duplicated between concepts: " //$NON-NLS-1$
		        + this.duplicate_pt.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.duplicate_pt);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}


		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("Contributing source concepts without proper ALT_DEFINITION: " //$NON-NLS-1$
		        + this.no_alt_def.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.altDefSourceCount);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		sortedReturn = sortHashMapByKey(this.no_alt_def);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("Contributing sources with no matching FULL_SYN: " //$NON-NLS-1$
		        + this.no_SynForContributingSource.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.no_SynForContributingSource);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("FULL_SYNs with no matching contributing source: " //$NON-NLS-1$
		        + this.no_ContributingSourceForSyn.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.no_ContributingSourceForSyn);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("Antiquated, Header or CTRM concepts with no NCT|PT FULL-SYN property : " //$NON-NLS-1$
		        + this.antiquated_noPT.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByKey(this.antiquated_noPT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("Concepts with no DEFINITION property, sorted by Semantic Type: " //$NON-NLS-1$
		        + this.no_DEF.size());
		this.pw.println("*                                                                                                      *"); //$NON-NLS-1$
		this.pw.println("********************************************************************************************************"); //$NON-NLS-1$
		sortedReturn = sortHashMapByValue(this.no_DEF);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
	}

	/**
	 * Read config file.
	 * 
	 * @param filename
	 *            the filename
	 * @return the vector< string>
	 */
	public Vector<String> readConfigFile(String filename) {
		final Vector<String> v = new Vector<String>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				final String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					v.add(line);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if (!v.isEmpty()) {
			return v;
		} else {
			return null;
		}
	}

	public void readSymbolMap(String filename) {
		final Vector<String> v = new Vector<String>();
		// FileReader configFile = null;
		BufferedReader buff = null;
		try {
			// configFile = new FileReader(filename);
			// buff = new BufferedReader(configFile);
			buff = new BufferedReader(new InputStreamReader(
			        new FileInputStream(filename),
			        StandardCharsets.UTF_8.name()));
			boolean eof = false;
			while (!eof) {
				final String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else if (!line.startsWith("#")) { //$NON-NLS-1$
					UnicodeConverter uc = new UnicodeConverter(line);
					symbolMap.put(uc.getUnicodeChar(), uc);
					v.add(line);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		finally {
			// Closing the streams
			try {
				buff.close();
				// configFile.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		// if (!v.isEmpty()) {
		// //start loading symbolMap
		// for (String line:v){
		// String[] symbolArray = line.split("\\|", 0);
		// if(symbolArray.length==4){
		// char[] toss = symbolArray[2].toCharArray();
		// char[] replace = symbolArray[1].toCharArray();
		//
		// char[] charArray=line.toCharArray();
		// for(int i=0; i<charArray.length;i++){
		// String r = String.format("\\u%04x", (int) charArray[i]);
		// System.out.println(r);
		// }
		// symbolMap.put(toss[0], replace[0]);}
		// }
		//
		// }
	}

	/**
	 * Removes the ampersand replacement.
	 * 
	 * @param s
	 *            the s
	 * @return the string
	 */
	public String removeAmpersandReplacement(String s) {// The Full-Syn value
		// substitutes the &
		// with &amp;
		// This messes up the attempt to compare preferred_name with
		// the Full-Syn. This method replaces &amp; with the &
		// Broken into separate methods for ease of reuse.
		final String ret = s.replace("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		return ret;
	}

	private void removeBranch(URI classURI) {
		this.ontology.removeBranch(classURI);
	}

	/**
	 * Qa retired concepts.
	 */
	private void qaRetiredConcepts() {
		// Check that concepts were properly retired
		// All retired concepts should have a status of "Retired Concept"

		// this will remove the retired kind from the ontology before performing
		// QA.
		// Retired concepts clutter the report and errors there are not going to
		// be fixed.
		// URI branchToDelete = createURI("Retired_Kind");

//		final URI branchToDelete = createURI(this.retiredBranch);
		this.ontology.setDeprecatedBranch(this.retiredBranch);
		checkRetiredConceptStatus(this.retiredBranch);
		removeBranch(this.retiredBranch);
	}

	/**
	 * Check retired concept status.
	 * 
	 * @param classURI
	 *            the class uri
	 */
	private void checkRetiredConceptStatus(URI classURI) {
		final Vector<ConceptProxy> retiredBranchClasses = new Vector<ConceptProxy>();

		final ConceptProxy retiredRoot = this.ontology
		        .getConcept(this.retiredBranch);
		retiredBranchClasses.add(retiredRoot);
		final Vector<URI> retiredConceptCodes = retiredRoot
		        .getAllDescendantCodes();
		if (retiredConceptCodes != null) {
			for (final URI code : retiredConceptCodes) {
				final ConceptProxy concept = this.ontology.getConcept(code);
				retiredBranchClasses.add(concept);
				final Vector<Property> v = concept
				        .getProperties("Concept_Status"); //$NON-NLS-1$
				if (v == null || v.size() == 0) {
					this.badCS.put(code.getFragment(), "no Concept Status"); //$NON-NLS-1$
				} else if (v.size() > 1) {
					if (!isRetiredInSet(v)) {
						this.badCS.put(code.getFragment(),
						        "Multiple Concept Status, no Retired Status"); //$NON-NLS-1$
					}

				} else if (!v.get(0).getValue().equals("Retired_Concept")) { //$NON-NLS-1$
					this.badCS.put(code.getFragment(), "Concept Status = " //$NON-NLS-1$
					        + v.get(0).getValue());
				}
			}
		}

		for (final URI code : this.ontology.getAllConceptCodes()) {
			final ConceptProxy activeConcept = this.ontology.getConcept(code);
			final boolean retired = this.ontology.isDeprecated(code);
			if (!retired) {
				final Vector<Property> v = activeConcept
				        .getProperties("Concept_Status"); //$NON-NLS-1$
				for (final Property prop : v) {
					if (prop.getValue().equals("Retired_Concept")) { //$NON-NLS-1$
						this.badCS_active.put(code.getFragment(),
						        "Active marked Retired_Concept"); //$NON-NLS-1$
					}
				}
			}

		}

	}

	/**
	 * @param statusSet
	 * @return whether statusSet has a Retired_Concept record If a retired
	 *         concept has mutiple Concept_Status records, we check to make sure
	 *         one of those records is Retired_Concept. If so, the test passes.
	 */
	private boolean isRetiredInSet(Vector<Property> statusSet) {
		boolean isRetired = false;
		for (final Property status : statusSet) {
			if (status.getValue().equals("Retired_Concept")) { //$NON-NLS-1$
				isRetired = true;
			}
		}
		return isRetired;
	}

	/**
	 * Sets the from config.
	 * 
	 * @param f
	 *            the f
	 * @return the vector< string>
	 */
	private Vector<String> setFromConfig(String f) {
		Vector<String> stys = new Vector<String>();
		stys = readConfigFile(f);
		return stys;
	}

	/**
	 * Sets the kB.
	 * 
	 * @param ontology
	 *            the new kB
	 */
	private void setKB(OWLKb ontology) {
		this.ontology = ontology;
	}

	/**
	 * Sort hash map by key.
	 * 
	 * @param hm
	 *            the hm
	 * @return the list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	List sortHashMapByKey(HashMap hm) {
		// Get a list of the entries in the map
		final List<Map.Entry<String, String>> list = new Vector<Map.Entry<String, String>>(
		        hm.entrySet());

		final Comparator byValue = new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry,
			        Map.Entry<String, String> entry1) {
				// Return 0 for a match, -1 for less than and +1 for more then
				return (entry.getValue().compareTo(entry1.getValue()));
			}
		};

		final Comparator byKey = new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry,
			        Map.Entry<String, String> entry1) {
				// Return 0 for a match, -1 for less than and +1 for more then
				return (entry.getKey().compareTo(entry1.getKey()));
			}
		};

		java.util.Collections.sort(list,
		        new CompositeComparator(byKey, byValue));

		return list;

	}

	/**
	 * Sort hash map by value.
	 * 
	 * @param hm
	 *            the hm
	 * @return the list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	List sortHashMapByValue(HashMap hm) {
		// Get a list of the entries in the map
		final List<Map.Entry<String, String>> list = new Vector<Map.Entry<String, String>>(
		        hm.entrySet());

		final Comparator byValue = new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry,
			        Map.Entry<String, String> entry1) {
				// Return 0 for a match, -1 for less than and +1 for more then
				if (entry.getValue() == null) {
					return -1;
				} else if (entry1.getValue() == null) {
					return +1;
				} else {
					return (entry.getValue().compareTo(entry1.getValue()));
				}
			}
		};

		final Comparator byKey = new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry,
			        Map.Entry<String, String> entry1) {
				// Return 0 for a match, -1 for less than and +1 for more then
				return (entry.getKey().compareTo(entry1.getKey()));
			}
		};

		// Sort the list using an annonymous inner class implementing Comparator
		// for the compare method

		java.util.Collections.sort(list,
		        new CompositeComparator(byValue, byKey));

		return list;

	}

	/**
	 * Tokenize.
	 * 
	 * @param pattern0
	 *            the pattern0
	 * @return the vector< string>
	 */
	private Vector<String> tokenize(String pattern0) {
		final Vector<String> v = new Vector<String>();
		final StringTokenizer st = new StringTokenizer(pattern0);
		while (st.hasMoreTokens()) {
			v.add(st.nextToken());
		}
		return v;
	}

	/**
	 * XM l2 pipe.
	 * 
	 * @param pattern
	 *            the pattern
	 * @return the string
	 */
	private String XML2Pipe(String pattern) {
		// string pattern =
		// "<term-name>Basal Transcription Factor</term-name><term-group>PT</term-group><term-source>NCI</term-source>";

		// <ncicp:ComplexTerm><ncicp:term-name>Third</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source></ncicp:ComplexTerm>

		// Find the first ">"
		int n = pattern.indexOf(">"); //$NON-NLS-1$
		String retstr = ""; //$NON-NLS-1$
		int lcv = 0;
		while (n != -1) {
			String tag = pattern.substring(0, n + 1);
			pattern = pattern.substring(n + 1, pattern.length());
			// Find the first "<" after the first ">"
			n = pattern.indexOf("<"); //$NON-NLS-1$
			if (n == -1) {
				break;
			}

			// Grab the value between the ">" and "<"
			final String value = pattern.substring(0, n);

			// Check to see if any value was actually returned
			if (value.length() > 0) {
				retstr = retstr + value;
				retstr = retstr + "|"; //$NON-NLS-1$
				lcv++;
			}

			pattern = pattern.substring(n, pattern.length());

			n = pattern.indexOf(">"); //$NON-NLS-1$
			if (n == -1) {
				break;
			}

			tag = pattern.substring(0, n + 1);

		}

		if (lcv > 0) {
			retstr = retstr.substring(0, retstr.length() - 1);
		} else {
			retstr = pattern;
		}
		return (retstr);
	}

	private void checkPreferredNameBug(ConceptProxy c) {
		// checks to see if there are any properties that exactly match the
		// preferred name.
		final String propertyName = Messages.getString("ProtegeKBQA.PreferredName"); //$NON-NLS-1$
		final String code = c.getCode();
		Property propPreferredName = c.getProperty(propertyName);
		if(propPreferredName!=null){
		final String preferredName = c.getProperty(propertyName).getValue();
		final Vector<Property> properties = c.getProperties();
		for (final Property prop : properties) {
			if (prop.getValue().equals(preferredName)) {
				if (!prop.getCode().equals(Messages.getString("ProtegeKBQA.PreferredName")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.FULL_SYN")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.rdfs_Label")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.Legacy_Name")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.Display_Name")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.Has_Salt_Form")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.NICHD_Hierarchy_Term")) //$NON-NLS-1$
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.Semantic_Type"))
				        && !prop.getCode().equals(Messages.getString("ProtegeKBQA.Maps_To"))) { //$NON-NLS-1$
					this.PNbug
					        .put(code, prop.getCode() + " " + prop.getValue()); //$NON-NLS-1$
				} else if (prop.getCode().equals(Messages.getString("ProtegeKBQA.Semantic_Type"))) { //$NON-NLS-1$
					// check to see if it is a legitimate semantic type. If not,
					// report
					if (!this.stys.contains(prop.getValue())) {
						this.PNbug.put(code,
						        prop.getCode() + " " + prop.getValue()); //$NON-NLS-1$
					}
				}
			}
		}} else {
			System.out.println("Preferred Name bug not checked on "+ code);
		}
	}

	private void checkForEmptyQualifiers(ConceptProxy c) {
		final Vector<Property> properties = c.getProperties();
		final String code = c.getCode();
		for (final Property prop : properties) {
			if (prop.getValue().length() == 0) {
				this.emptyValue.put(code, prop.getCode());
			}
			final Vector<Qualifier> qualifiers = prop.getQualifiers();
			if (qualifiers != null) {
				for (final Qualifier qual : qualifiers) {
					if (qual.getValue().length() == 0) {
						this.emptyValue.put(code,
						        prop.getCode() + " " + qual.getName()); //$NON-NLS-1$
					}
				}
			}
		}
	}

	private void checkMedDRA_LLT(ConceptProxy cls) {
		// This will check that all LLT full-syns have a MedDRA source.
		final Vector<Property> fullsyns = cls.getProperties(Messages.getString("ProtegeKBQA.FULL_SYN")); //$NON-NLS-1$
		for (final Property syn : fullsyns) {
			if(getQualifierValue(syn, Messages.getString("ProtegeKBQA.Term_Group")).equals("LLT") &&
					!(getQualifierValue(syn,Messages.getString("protegeKBQA.Term_Source")).equals("MedDRA"))){
				lltNotMedDRA.put(cls.getCode(), syn.getValue());
			
			
//			if (syn.getValue().contains(
//			        "<ncicp:term-group>LLT</ncicp:term-group>")) { //$NON-NLS-1$
//				if (syn.getValue().contains(
//				        "<ncicp:term-source>MedDRA</ncicp:term-source>")) { //$NON-NLS-1$
//					// is MedDRA
//
//				} else {
//					lltNotMedDRA.put(cls.getCode(), syn.getValue());
//				}
//			} else if (syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Group")) != null) { //$NON-NLS-1$
//				if (syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Group")).getValue().equals(Messages.getString("ProtegeKBQA.LLT"))) { //$NON-NLS-1$ //$NON-NLS-2$
//					if (syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")).getValue() //$NON-NLS-1$
//					        .equals("MedDRA")) { //$NON-NLS-1$
//						// isMedDRA
//					} else {
//						lltNotMedDRA.put(
//						        cls.getCode(),
//						        syn.getQualifier(Messages.getString("ProtegeKBQA.Term_Source")) + " " //$NON-NLS-1$ //$NON-NLS-2$
//						                + syn.getValue());
//					}
//				}
			}
		}
	}
	
	private void checkDefCurator(ConceptProxy cls) {
		//TODO Externalize strings. 
		Vector<Property> defs = cls.getProperties("P97");
		for( Property def : defs ) {
			Vector<Qualifier> quals = def.getQualifiers();
			boolean hasDefCurator = false;
			String editor = null;
			for( Qualifier qual : quals ) {
				if( qual.getName().equals("Definition Source") && qual.getValue().equals("NCI-DEFCURATOR") ) {
					hasDefCurator = true;
				}
				else if( qual.getName().equals("Definition_Reviewer_Name") ) {
					editor = qual.getValue();
				}
			}
			if( hasDefCurator && !drugEditors.contains(editor) ) {
				no_DefCurator.put(cls.getCode(), editor);
			}
		}
	}

}
