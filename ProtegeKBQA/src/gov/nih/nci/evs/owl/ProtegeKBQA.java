/**
 * This application runs QA on OWL files exported from Protege. It uses the OWL
 * API to parse the data for QA. This program was converted from
 * OntylogKBQA.cpp, which was used to perform the same function on Ontylog
 * files.
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
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
		System.out.println("java -jar path/owlnciqa.jar");
		System.out.println();
		System.out.println("-c --config\t\tPath to configuration file");
		System.out.println("-i --input\t\tURI of vocabulary file (optional)");
		System.out.println("-o --output\t\tURI of output file(optional)");
		System.out.println();
		System.out.println("If input and output not passed in as parameters, they must be specified in config file");
		System.exit(0);
	}

	/** The ontology. */
	// private OWLOntology ontology;
	private OWLKb ontology;

	/** The pw. */
	private PrintWriter pw;

	/** The ontology namespace. */
	private String ontologyNamespace;


	/** The stys. */
	private Vector<String> stys;

	private Vector<String> styPairs;

	private URI retiredBranch;

	// HaspMaps storing the values that need to be reported
	// list of sources that don't require a Contributing_Source property
	/** The ignore sources. */
	private Vector<String> ignoreSources;
	// Hashmap stores all instances of Preferred_Name that appears in more than
	// one concept.
	/** The duplicate_pn. */
	final HashMap<String, String> duplicate_pn = new HashMap<String, String>();
	// HashMap as holding bin for Preferred_Name, used to detect duplicates
	/** The pn_tbl. */
	final HashMap<String, String> pn_tbl = new HashMap<String, String>();
	// Hashmap stores all instances of NCI|PT FULL-SYN that appears in more than
	// one concept.
	/** The duplicate_pt. */
	final HashMap<String, String> duplicate_pt = new HashMap<String, String>();
	// HashMap as holding bin for PT, used to detect duplicates
	/** The pt_tbl. */
	final HashMap<String, String> pt_tbl = new HashMap<String, String>();
	// Hashmap stores all instances of bunk characters in properties
	/** The badchar_newline. */
	final HashMap<String, String> badchar_newline = new HashMap<String, String>();
	// HashMap stores instances of concepts with @
	/** The badchar_at. */
	final HashMap<String, String> badchar_at = new HashMap<String, String>();
	// HashMap stores instances of concepts with pipes
	/** The badchar_pipe. */
	final HashMap<String, String> badchar_pipe = new HashMap<String, String>();
	/** The badchar_tab */
	final HashMap<String, String> badchar_tab = new HashMap<String, String>();

	// HashMap stores instances of concepts with duplicate roles.
	/** The duplicate_role. */
	final HashMap<String, String> duplicate_role = new HashMap<String, String>();

	// HashMap stores instances of concepts with duplicate properties.
	/** The duplicate_property. */
	final HashMap<String, String> duplicate_property = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one DEFINITION
	/** The multiple_ def. */
	final HashMap<String, String> multiple_DEF = new HashMap<String, String>();
	// HashMap stores instances of concepts with
	/** The fully quoted definition **/
	final HashMap<String, String> quoted_DEF = new HashMap<String, String>();
	/** The no_ def. */
	final HashMap<String, String> no_DEF = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one NCI|PT FULL-SYN
	/** The multiple_ pt. */
	final HashMap<String, String> multiple_PT = new HashMap<String, String>();
	// HashMap stores instances of concepts with no PT
	/** The no_ pt. */
	final HashMap<String, String> no_PT = new HashMap<String, String>();

	/** The antiquated_no pt. */
	final HashMap<String, String> antiquated_noPT = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one Preferred_Name
	/** The multiple_ pn. */
	final HashMap<String, String> multiple_PN = new HashMap<String, String>();
	// HashMap stores instances of concepts with no Preferred_Name
	/** The no_ pn. */
	final HashMap<String, String> no_PN = new HashMap<String, String>();

	// HashMap stores instances of concepts with bad Semantic_Type
	/** The bad_semantictypes. */
	final HashMap<String, String> bad_semantictypes = new HashMap<String, String>();
	// HashMap stores instances of concepts with other than one Semantic_Type
	/** The multiple_ st. */
	final HashMap<String, String> multiple_ST = new HashMap<String, String>();
	// HashMap stores instances of concepts with no Semantic Type
	/** The no_ st. */
	final HashMap<String, String> no_ST = new HashMap<String, String>();

	// HashMap stores instances of Def_Curator concepts w/o Def or Special
	// Review Names
	/** The no_ def review. */
	final HashMap<String, String> no_DefReview = new HashMap<String, String>();

	// HashMap stores instances of Definitions with def-source NCI-DEFCURATOR
	// without a drug editor
	final HashMap<String, String> no_DefCurator = new HashMap<String, String>();

	/** Drug Dictionary Editors */
	private Vector<String> drugEditors;

	// HashMap stores all instances of classes without at least one full syn for
	// a contributing source (exception: ignoreSources)
	/** The no_ syn for contributing source. */
	final HashMap<String, String> no_SynForContributingSource = new HashMap<String, String>();

	// HashMap stores all instances of classes without at least one contributing
	// source for a full syn (exception: ignoreSources)
	/** The no_ contributing source for syn. */
	final HashMap<String, String> no_ContributingSourceForSyn = new HashMap<String, String>();

	// HashMap stores all concepts where the NCI|PT FULL-SYN and Preferred_Name
	// don't match
	/** The nomatch_pnpt. */
	final HashMap<String, String> nomatch_pnpt = new HashMap<String, String>();

	// HashMap stores all concepts where there is not ALT_DEFINITION to match
	// the Contributing_Source
	// Only bother checking this if there is a definition present
	/** The no_alt_def. */
	final HashMap<String, String> no_alt_def = new HashMap<String, String>();

	// Hashmap stores all retired concepts that don't have a correct
	// Concept_Status
	/** The bad cs. */
	final HashMap<String, String> badCS = new HashMap<String, String>();

	// Hashmap stored concepts that improperly have Concept_Status
	// Retired_Concept
	final HashMap<String, String> badCS_active = new HashMap<String, String>();

	// Hashmap stores all instances of FDA UNII concepts without UNII codes.
	/** the missing UNII codes **/
	final HashMap<String, String> missingUNII = new HashMap<String, String>();

	// Hashmap stores all instances of FDA UNII concepts without FDA PT.
	/** the missing UNII PTs **/
	final HashMap<String, String> missingUNII_PT = new HashMap<String, String>();

	// Stores all instances where the PreferredName appears to be pasted into
	// another property
	final HashMap<String, String> PNbug = new HashMap<String, String>();

	// Stores all instances where the property or qualifier value is an empty
	// string
	final HashMap<String, String> emptyValue = new HashMap<String, String>();

	// Stores the character map for replacement
	final HashMap<Character, UnicodeConverter> symbolMap = new HashMap<Character, UnicodeConverter>();

	// Stores the results of the check for high bit characters
	final HashMap<String, String> highBitCharacters = new HashMap<String, String>();

	// Stores the results of the check for reolaced high bit characters
	final HashMap<String, String> replacedHighBitCharacters = new HashMap<String, String>();

	// Count the number of missing alt-defs per contributing source
	final HashMap<String, Integer> altDefSourceCount = new HashMap<String, Integer>();

	// Gather the FULL_SYNS with an LLT term-group that are not MedDRA
	final HashMap<String, String> lltNotMedDRA = new HashMap<String, String>();

	//Value Sets without contributing source (P322)
	final HashMap<String,String> vsNoCS = new HashMap<String, String>();

	final HashMap<String,String> emptyValueSet = new HashMap<String, String>();

	// reads the System properties to get the location of the
	// nciqaowl.properties file
	/** The sys prop. */
	private static Properties sysProp = System.getProperties();

	Messages messages;

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
		String c1;
		// in case c is null?
		c1 = "" + c;
		Vector<Property> properties;
		properties = cls.getProperties();
		for (final Property property : properties) {
			if (property.getValue().contains(c1)) {
				if (c == '@') {
					this.badchar_at.put(cls.getCode() + " " + cls.getName(), property.getValue());
				} else if (c == '\n') {
					this.badchar_newline.put(cls.getCode() + " " + cls.getName(), property.getValue());
				} else if (c == '|') {
					// New property - Value_Set_Location - will include pipes
					if (!property.getCode().equals(messages.getString("ProtegeKBQA.Value_Set_Location"))) {
						this.badchar_pipe.put(cls.getCode() + " " + cls.getName(), property.getValue());
					}
				} else if (c == '\t') {
					this.badchar_tab.put(cls.toString(), property.getValue());
				}
			}
		}
	}

	private void checkContributingSourceAndAltDef(ConceptProxy cls) {
		// check that concepts with a contributing source and a Definition
		// should also have an Alt-def
		// ignoreSources.contains(synSource)
		final Vector<Property> contributingSources = cls
				.getProperties(messages.getString("ProtegeKBQA.Contributing_Source"));

		final Vector<Property> defs = cls.getProperties(messages.getString("ProtegeKBQA.Definition"));
		// concepts with FDA_UNII_Code should not check for FDA alt-def
		final Vector<Property> unii_codes = cls.getProperties(messages.getString("ProtegeKBQA.FDA_UNII_Code"));
		boolean hasUNII = cls.getProperties().size() > 0;

		final String sCode = cls.getCode();
		List<String> exclude = Arrays.asList("UCUM", "MedDRA", "ICH", "HL7", "NCPDP"); //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		// if there are contributing sources and definitions
		if (contributingSources.size() > 0) {
			// check if there are any alt_defs.
			final Vector<Property> alt_defs = cls.getProperties(messages.getString("ProtegeKBQA.Alt_Definition"));
			if ((alt_defs.size() > 0) && (contributingSources.size() > 0)) {

				// check to see if the alt_def corresponds to the contributing
				// sources
				for (final Property source : contributingSources) {
					// Check if the source is on the ignore list
					if (!this.ignoreSources.contains(source.getValue())) {
						boolean hasSource = false;
						for (final Property def : alt_defs) {
							if (def.getValue().contains("<def-source>" + source.getValue() + "</def-source>")) {
								hasSource = true;
							} else if (def.getQualifier(messages.getString("ProtegeKBQA.Def_Source")) != null
									&& def.getQualifier(messages.getString("ProtegeKBQA.Def_Source")).getValue()
											.equals(source.getValue())) {
								hasSource = true;
							} else if (source.getValue().equals("FDA") && hasUNII) {
								hasSource = true;
							}
						}
						if (!hasSource) {
							// UCUM, MedDRA, ICH, HL7 and NCPDP will never have
							// alt-def

							if (!exclude.contains(source.getValue())) {

								this.no_alt_def.put(sCode, "No ALT_DEFINITION for " + source.getValue());
								if (altDefSourceCount.containsKey(source.getValue())) {
									Integer tempInt = altDefSourceCount.get(source.getValue()) + 1;
									altDefSourceCount.put(source.getValue(), tempInt);
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
				if (!hasUNII || contributingSources.size() != 1 || !contributingSources.get(0).getValue().equals("FDA")) {
					if (contributingSources.size() > 1) {
						boolean hasSource = true;
						for (final Property source : contributingSources) {
							// check each source against the ignore list. If any are
							// not on it, then it should have an alt-def
							if (!this.ignoreSources.contains(source.getValue())) {
								if (hasUNII && source.getValue().equals("FDA")) {
									// if the source is FDA and there is a
									// UNII_Code, discard
								} else {

									if (!exclude.contains(source.getValue())) {
										hasSource = false;
										if (altDefSourceCount.containsKey(source.getValue())) {
											Integer tempInt = altDefSourceCount.get(source.getValue()) + 1;
											altDefSourceCount.put(source.getValue(), tempInt);
										} else {
											altDefSourceCount.put(source.getValue(), 1);
										}
									}
								}
							}
						}
						if (!hasSource) {
							this.no_alt_def.put(sCode, "0 ALT_DEFINTIONs");
						}
					} else {
						// only one contributing source and no alt-defs
						if (!this.ignoreSources.contains(contributingSources.get(0).getValue())) {

							if (!exclude.contains(contributingSources.get(0).getValue())) {

								this.no_alt_def.put(sCode,
										"No ALT_DEFINITION for " + contributingSources.get(0).getValue());
								if (altDefSourceCount.containsKey(contributingSources.get(0).getValue())) {
									Integer tempInt = altDefSourceCount.get(contributingSources.get(0).getValue()) + 1;
									altDefSourceCount.put(contributingSources.get(0).getValue(), tempInt);
								} else {
									altDefSourceCount.put(contributingSources.get(0).getValue(), 1);
								}
							}
						}
					}
				} else {
					// if there is only one CS and it is FDA, and there is a
					// UNII_Code, don't add it.
				}

			}
		}
	}

	private boolean checkContributingSourceAndAltDef(ConceptProxy cls, Property source) {
		// check that concepts with a contributing source and a Definition
		// should also have an Alt-def
		// ignoreSources.contains(synSource)
		// final Vector<Property> contributingSources = cls
		// .getProperties(messages.getString("ProtegeKBQA.Contributing_Source"));

		final Vector<Property> defs = cls.getProperties(messages.getString("ProtegeKBQA.Definition"));
		// concepts with FDA_UNII_Code should not check for FDA alt-def
		final Vector<Property> unii_codes = cls.getProperties(messages.getString("ProtegeKBQA.FDA_UNII_Code"));
		boolean hasUNII = cls.getProperties().size() > 0;

		final String sCode = cls.getCode();
		List<String> exclude = Arrays.asList("UCUM", "MedDRA", "ICH", "HL7", "NCPDP"); //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		// if there are contributing sources and definitions
		// if (contributingSources.size() > 0) {
		// check if there are any alt_defs.
		final Vector<Property> alt_defs = cls.getProperties(messages.getString("ProtegeKBQA.Alt_Definition"));
		if (alt_defs.size() > 0) {

			// check to see if the alt_def corresponds to the contributing
			// sources
			// for (final Property source : contributingSources) {
			// Check if the source is on the ignore list
			if (!this.ignoreSources.contains(source.getValue())) {
				// boolean hasSource = false;
				for (final Property def : alt_defs) {
					// if (def.getValue().contains("<def-source>" +
					// source.getValue() + "</def-source>")) {
					// hasSource = true;
					// }
					if (def.getQualifier(messages.getString("ProtegeKBQA.Def_Source")) != null
							&& def.getQualifier(messages.getString("ProtegeKBQA.Def_Source")).getValue()
									.equals(source.getValue())) {
						return true;
					} else if (source.getValue().equals("FDA") && hasUNII) {
						return true;
					}
				}
				// if (!hasSource) {
				// UCUM, MedDRA, ICH, HL7 and NCPDP will never have
				// alt-def

				if (!exclude.contains(source.getValue())) {

					this.no_alt_def.put(sCode, "No ALT_DEFINITION for " + source.getValue());
					if (altDefSourceCount.containsKey(source.getValue())) {
						Integer tempInt = altDefSourceCount.get(source.getValue()) + 1;
						altDefSourceCount.put(source.getValue(), tempInt);
					} else {
						altDefSourceCount.put(source.getValue(), 1);
					}
					return false;
				}
				// }
			}
			// }
		} else {
			// if there is one contributing source of FDA and there is a
			// UNII_Code, discard
			if (hasUNII && source.getValue().equals("FDA")) {
				// if there is only one CS and it is FDA, and there is a
				// UNII_Code, don't add it.
				return true;
			}
			// else if (contributingSources.size() > 1) {
			// boolean hasSource = true;
			//// for (final Property source : contributingSources) {
			// // check each source against the ignore list. If any are
			// // not on it, then it should have an alt-def
			// if (!this.ignoreSources.contains(source.getValue())) {
			// if (hasUNII && source.getValue().equals("FDA")) {
			// // if the source is FDA and there is a
			// // UNII_Code, discard
			// return true;
			// }
			// if (!exclude.contains(source.getValue())) {
			// hasSource = false;
			// if (altDefSourceCount.containsKey(source.getValue())) {
			// Integer tempInt = altDefSourceCount.get(source.getValue()) + 1;
			// altDefSourceCount.put(source.getValue(), tempInt);
			// } else {
			// altDefSourceCount.put(source.getValue(), 1);
			// }
			// this.no_alt_def.put(sCode, "0 ALT_DEFINTIONs");
			// return false;
			// }
			// }
			//// }
			//
			// }
			// else {
			// only one contributing source and no alt-defs
			if (this.ignoreSources.contains(source.getValue())) {

				return true;
			}

			if (exclude.contains(source.getValue())) {
				return true;
			}

			this.no_alt_def.put(sCode, "No ALT_DEFINITION for " + source.getValue());
			if (altDefSourceCount.containsKey(source.getValue())) {
				Integer tempInt = altDefSourceCount.get(source.getValue()) + 1;
				altDefSourceCount.put(source.getValue(), tempInt);
			} else {
				altDefSourceCount.put(source.getValue(), 1);
			}

			// }

		}
		return false;
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

		final Vector<Property> v = c.getProperties(messages.getString("ProtegeKBQA.Definition"));
		final Property semType = c.getProperty(messages.getString("ProtegeKBQA.Semantic_Type"));
		if (v.size() < 1) {
			this.no_DEF.put(c.getCode() + "_" + c.getName(), semType != null ? semType.getValue() : "Null Semantic Type");
		} else if (v.size() > 1) {
			this.multiple_DEF.put(c.getCode() + "_" + c.getName(), Integer.toString(v.size()) + " DEFs");
		}

		// TODO build a metric to count the number of concepts with and without
		// definitions
	}

	private void checkDEFINITIONValue(ConceptProxy c) {
		final Vector<Property> v = c.getProperties(messages.getString("ProtegeKBQA.Definition"));
		for (final Property def : v) {
			final String defValue = def.getValue();
			if (defValue.startsWith("\"") && defValue.endsWith("\"")) { //$NON-NLS-2$
				final String defQuote = "Definition is fully quoted " + def.getValue();
				this.quoted_DEF.put(c.getCode() + "_" + c.getName(), defQuote);
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
		final Vector<Property> curators = cls.getProperties(messages.getString("ProtegeKBQA.Def_Curator"));
		if (curators.size() > 0) {
			final Vector<Property> defs = cls.getProperties(messages.getString("ProtegeKBQA.Definition"));
			for (final Property def : defs) {
				final String reviewerName = getQualifierValue(def,
						messages.getString("ProtegeKBQA.Definition_Reviewer_Name"));
				if ((reviewerName.equalsIgnoreCase(messages.getString("ProtegeKBQA.Special_Review"))
						|| reviewerName.equalsIgnoreCase(messages.getString("ProtegeKBQA.Default_Review")))) {
					return;
				}
				if (drugEditors.contains(reviewerName)) {
					return;
				}
				no_DefCurator.put(cls.getCode() + " " + cls.getName(), reviewerName);
			}
		}
	}

	private void checkDefCurator(ConceptProxy cls) {
		// TODO Externalize strings.
		Vector<Property> defs = cls.getProperties("P97");
		for (Property def : defs) {
			Vector<Qualifier> quals = def.getQualifiers();
			boolean hasDefCurator = false;
			String editor = null;
			for (Qualifier qual : quals) {
				if (qual.getName().equals("Definition Source") && qual.getValue().equals("NCI-DEFCURATOR")) {
					hasDefCurator = true;
				} else if (qual.getName().equals("Definition_Reviewer_Name")) {
					editor = qual.getValue();
				}
			}
			if (hasDefCurator && !drugEditors.contains(editor)) {
				no_DefCurator.put(cls.getCode() + " " + cls.getName(), editor);
			}
		}
	}

	// private String getReviewerName(Property def) {
	// for(Qualifier qual: def.getQualifiers()){
	// if(qual.getCode().equals(messages.getString("ProtegeKBQA.Definition_Reviewer_Name"))){
	// return qual.getValue();
	// }
	// }
	// return "";
	//
	//// String name = "";
	//// if (def.contains("<ncicp:Definition_Reviewer_Name>")) {
	//// try {
	//// final int beginning = def
	//// .indexOf("<ncicp:Definition_Reviewer_Name>");
	//// final int end = def
	//// .indexOf("</ncicp:Definition_Reviewer_Name>");
	//// name = def.substring(beginning + 32, end);
	//// } catch (final Exception e) {
	//// e.printStackTrace();
	//// System.out.println("Parse error at: " + def);
	//// }
	//// }
	//// return name;
	// }

	private String getQualifierValue(Property prop, String qualCode) {
		for (Qualifier qual : prop.getQualifiers()) {
			// System.out.println("Qual code " + qual.getCode());
			if (qual.getCode().equals(qualCode)) {
				return qual.getValue();
			}
		}
		return "";

	}

	// private String getDefinitionValue(String def) {
	// String name = "";
	// if (def.contains("<ncicp:Definition_Reviewer_Name>")) {
	// try {
	// final int beginning = def.indexOf("<ncicp:def-definition>");
	// final int end = def.indexOf("</ncicp:def-definition>");
	// name = def.substring(beginning + 22, end);
	// } catch (final Exception e) {
	// e.printStackTrace();
	// System.out.println("Parse error at: " + def);
	// }
	// }
	// return name;
	// }

	private void checkDuplicateProperties(ConceptProxy c) {
		final Vector<Property> props = c.getProperties();
		final Vector<Property> propMap = new Vector<Property>();
		for (final Property prop : props) {
			if (!propMap.contains(prop)) {
				propMap.add(prop);
			} else {
				this.duplicate_property.put(c.getCode() + ":" + c.getName(), prop.getCode() + ":" + prop.getValue());
			}
		}
	}

	/**
	 * Check duplicate roles.
	 * 
	 * @param cls
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
			final String roleDef = roleName + " " + roleTarget;
			if (!roleMap.contains(roleDef)) {
				roleMap.add(roleDef);
			} else {
				this.duplicate_role.put(roleDef, cls.getCode() + " " + cls.getName());
			}
		}
	}

	private void checkHighBitCharacters(ConceptProxy cls) {
		// pw.println("Property values containing High Bit characters \n");
		Vector<Property> properties;
		properties = cls.getProperties();
		int unexpectedNumber = 1;
		int replacedNumber = 1;
		boolean hasIssue = false;
		for (final Property property : properties) {
			StringBuilder propertyValue = new StringBuilder(property.getValue());
			for (Qualifier qual : property.getQualifiers()) {
				propertyValue.append(" ").append(qual.getValue());
			}

			String highBits = checkHighBitCharacters(propertyValue.toString());

			if (highBits.length() > 0) {
				String bitReport = "Property: " + property.getName() + "\t" + highBits + "Property: "
						+ property.getValue();
				if (bitReport.contains("Unexpected Character")) {
					highBitCharacters.put(cls.getCode() + " " + cls.getName() + " issue "+unexpectedNumber, bitReport);
					unexpectedNumber= unexpectedNumber+1;
				} else {
					replacedHighBitCharacters.put(cls.getCode() + " " + cls.getName()+ " issue "+replacedNumber, bitReport);
					replacedNumber = replacedNumber+1;
				}

			}

		}

	}

	private String checkHighBitCharacters(String propertyValue) {
		// Check each character and swap out any unicode in the map.
		// Toss exception if non-swappable character found
		StringBuilder returnString = new StringBuilder();
		final int len = propertyValue.length();
		for (int i = 0; i < len; i++) {
			final Integer iPosition = new Integer(i + 1);
			final char c = propertyValue.charAt(i);
			final int cast = (int)c;
//			final int cast = (int) propertyValue.charAt(i);
			if (cast >= 32 && cast <= 126) {
				// No problems. These are accepted characters
				// No need to go iterating through the unicode converter
			} else if (cast >= 127 && cast <= 191) {
				if (symbolMap.containsKey(c)) // magic number for em-dash
				{
					returnString.append("Character replaced ").append("Char: " //$NON-NLS-2$
					).append(propertyValue.charAt(i)).append(" Windows-1252 code:").append(cast).append(" " //$NON
							// -NLS-2$
					).append(symbolMap.get(c).getCharDescription()).append(" Position: ").append(iPosition).append("  " +
							"  \t");
					// throw new Exception("Replaced character " +
					// Character.toString(c) + " " +
					// symbolMap.get(c).getCharDescription());

				} else {
					// Allow string to pass through, but note it.
					// throw new Exception("Unexpected character " +
					// Character.toString(c));
					returnString.append("Unexpected Character ").append("Char: ").append(propertyValue.charAt(i)).append(" Windows-1252:" //$NON-NLS-1$
					).append(cast).append(" Position: ").append(iPosition).append("   \t "); //$NON-NLS-2$
				}

			} else if (cast == 215 || cast == 247) {
				returnString.append("Unexpected Character ").append("Char: ").append(propertyValue.charAt(i)).append(
						" Windows-1252:" //$NON-NLS-1$
				).append(cast).append(" Position: ").append(iPosition).append("   \t "); //$NON-NLS-2$
			}

		}

		return returnString.toString();
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
		// final Vector<String> syns = getNCIPTFullSyn(cls);
		Vector<String> syns = getFullSynBySourceAndGroup(cls, "NCI", "PT");
		if (syns.size() < 1) {
			syns = getFullSynBySourceAndGroup(cls, "NCI", "HD");
		}
		if (syns.size() < 1) {
			syns = getFullSynBySourceAndGroup(cls, "CTRM", "PT");
		}
		if (syns.size() < 1) {
			// if the concept has an AQ Full-Syn, this can replace the NCI|PT
			// final Vector<String> aqs = getAQ_CTRMFullSyn(cls);
			final Vector<String> aqs = getFullSynByGroup(cls, "AQ");
			if (aqs.size() == 0) {
				this.no_PT.put(cls.getCode(), Integer.toString(syns.size()) + " PTs");
			} else {
				this.antiquated_noPT.put(cls.getCode(), Integer.toString(syns.size()) + " PTs");
			}
		} else if (syns.size() > 1) {
			this.multiple_PT.put(cls.getCode(), Integer.toString(syns.size()) + " PTs");
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
		final Vector<Property> pn = cls.getProperties(messages.getString("ProtegeKBQA.PreferredName"));

		// String sCode = getConceptCode(cls);
		if (pn.size() < 1) {
			this.no_PN.put(cls.getCode()+"_"+cls.getName(), Integer.toString(pn.size()) + " PNs");
		} else if (pn.size() > 1) {
			this.multiple_PN.put(cls.getCode()+"_"+cls.getName(), Integer.toString(pn.size()) + " PNs");
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

		// final Vector<String> syns = getNCIPTFullSyn(cls);
		Vector<String> syns = getFullSynBySourceAndGroup(cls, "NCI", "PT");
		if (syns.size() == 0) {
			syns = getFullSynBySourceAndGroup(cls, "NCI", "HD");
		}
		if (syns.size() == 0) {
			syns = getFullSynBySourceAndGroup(cls, "NCI", "AQ");
		}
		final Property pn = cls.getProperty(messages.getString("ProtegeKBQA.PreferredName"));
		if (pn == null || syns.size() == 0) {
			System.out.println("Unable to do FullSynMatch for " + cls.getCode());
		} else {
			for (final String pt : syns) {
				if (!pt.equals(pn.getValue())) {
					this.nomatch_pnpt.put(cls.getCode() + "_" + cls.getName(), "PN:" + pn.getValue() + " PT:" + pt);
				}
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

		final Vector<Property> props = cls.getProperties(messages.getString("ProtegeKBQA.FULL_SYN"));

		for (final Property prop : props) {
			final String pt = prop.getValue();
			// if (pt.indexOf("<ncicp:term-source>NCI</ncicp:term-source>") > 0
			// & pt.indexOf("<ncicp:term-group>PT</ncicp:term-group>") > 0) {
			// if (!this.pt_tbl.containsKey(pt)) {
			// // if the list of pt's doesn't include this, then add it
			// this.pt_tbl.put(pt, cls.getCode());
			// } else {
			// // if the list of pt's includes this, we have a duplicate
			// // record the id's of the two classes for reporting
			// final String firstCode = this.pt_tbl.get(pt);
			// final String storeCode = firstCode + " " + cls.getCode();
			// this.duplicate_pt.put(XML2Pipe(pt), storeCode);
			// }
			// } else if (prop.getQualifiers().size() > 0) {
			final Qualifier source = prop.getQualifier(messages.getString("ProtegeKBQA.Term_Source"));
			final Qualifier group = prop.getQualifier(messages.getString("ProtegeKBQA.Term_Group"));
			if (source != null && group != null) {
				if (source.getValue().equals("NCI") && group.getValue().equals("PT")) {
					if (!this.pt_tbl.containsKey(pt)) {
						this.pt_tbl.put(pt, cls.getCode());
					} else {
						final String firstCode = this.pt_tbl.get(pt);
						final String storeCode = firstCode + " " + cls.getCode();
						this.duplicate_pt.put(XML2Pipe(pt), storeCode);
					}
				}
			}
			// }
		}

	}

	/**
	 * Check same preferred name.
	 * 
	 * @param cls
	 *            the cls
	 */
	private void checkSamePreferredName(ConceptProxy cls) {

		final Vector<Property> properties = cls.getProperties(messages.getString("ProtegeKBQA.PreferredName"));

		if (properties != null) {
			for (final Property prop : properties) {
				final String pn = prop.getValue();

				if (!this.pn_tbl.containsKey(pn)) {
					this.pn_tbl.put(pn, cls.getCode());
				} else {
					final String firstCode = this.pn_tbl.get(pn);
					final String storeCode = firstCode + " " + cls.getCode();
					this.duplicate_pn.put(pn, storeCode);
				}
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

		final Vector<Property> styValues = cls.getProperties(messages.getString("ProtegeKBQA.Semantic_Type"));

		if (styValues.size() < 1) {

			// check if retired
			if (!cls.isRetired()) {

				this.no_ST.put(cls.getCode()+"_"+cls.getName(), Integer.toString(styValues.size()) + " SemanticTypes");
			}
		} else if (styValues.size() > 1) {
			if (!checkSemanticPairs(styValues)) {
				StringBuilder semanticTypeValues = new StringBuilder();
				for (final Property prop : styValues) {
					semanticTypeValues.append(prop.getValue()).append("|");
				}
				semanticTypeValues = new StringBuilder(semanticTypeValues.substring(0, semanticTypeValues.length() - 1));
				this.multiple_ST.put(cls.getCode()+"_"+cls.getName(),
						Integer.toString(styValues.size()) + " SemanticTypes " + semanticTypeValues);
			}
		}
	}

	private boolean checkSemanticPairs(Vector<Property> styValues) {

		Collections.sort(styValues);
		StringBuilder styValue = new StringBuilder();
		for (final Property prop : styValues) {
			styValue.append(prop.getValue()).append("|");
		}
		styValue = new StringBuilder(styValue.substring(0, styValue.length() - 1));

		for (final String group : this.styPairs) {
			if (group.equals(styValue.toString())) {
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
		final Vector<Property> props = cls.getProperties(messages.getString("ProtegeKBQA.Semantic_Type"));
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

		final Vector<Property> syns = cls.getProperties(messages.getString("ProtegeKBQA.FULL_SYN"));
		final Vector<Property> contributingSources = cls
				.getProperties(messages.getString("ProtegeKBQA.Contributing_Source"));
		if (contributingSources.size() > 0 && syns.size() > 0) {
			final HashMap<String, String> synMap = getSynSources(syns);
			for (final Property contributingSource : contributingSources) {

				if (!(this.ignoreSources.contains(contributingSource.getValue()))) {
					boolean hasSource = false;
					for (final Property syn : syns) {
//						if (syn.getValue().contains(
//								"<ncicp:term-source>" + contributingSource.getValue() + "</ncicp:term-source>")) {
//							hasSource = true;
//						} 
						 if (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Source")) != null) {
							if (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Source")).getValue()
									.equals(contributingSource.getValue())) {
								hasSource = true;
							}
							//Special case for CTCAE - term source is CTCAE 3 but CS is CTCAE
							if(contributingSource.getValue().equals("CTCAE")){
								if (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Source")).getValue()
										.contains(contributingSource.getValue())) {
									hasSource = true;
								}
							}
						}
					}
					if (!hasSource) {
						boolean hasDef = checkContributingSourceAndAltDef(cls, contributingSource);
						boolean hasValueSet = checkPreferredNameToSource(cls,contributingSource.getValue());
						if(!hasDef && !hasValueSet){
						this.no_SynForContributingSource.put(cls.getCode(), contributingSource.getValue());
						}
					}

				}
			}
			for (final Property syn : syns) {
				final String fullSyn = concatenateFullSyn(syn);
				final String synSource = synMap.get(fullSyn);
				boolean debub = this.ignoreSources.contains(synSource);
				if (!(synSource.equals("NCI"))) {
					if (!(synSource.equals("")) && !this.ignoreSources.contains(synSource)) {
						boolean found = false;
						for (final Property contributingSource : contributingSources) {
							if (contributingSource.getValue().equals(synSource)) {
								found = true;
							}
						}
						if (!found) {
							this.no_ContributingSourceForSyn.put(cls.getCode(), syn.getValue() + "|" + synSource);

						}
					}
				}
			}
		}
	}
	
	
	private boolean checkPreferredNameToSource(ConceptProxy cls,String source){
		//If the PT is "CDISC Value Set of Something", then we can consider the contributing source to have a match
		final Vector<Property> vsl = cls.getProperties(messages.getString("ProtegeKBQA.PreferredName"));
		for(Property prop:vsl){
			if(prop.getValue().contains(source)){
				return true;
			}
		}
		return false;
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

		final Vector<Property> unii_syn = cls.getProperties(messages.getString("ProtegeKBQA.FULL_SYN"));
		final Vector<Property> subsets = cls.getProperties(messages.getString("ProtegeKBQA.Concept_In_Subset"));
		boolean hasFULL_SYN = false;
		boolean hasUNII_Code = true;
		for (final Property subset : subsets) {
			if (subset.getValue().contains(messages.getString("ProtegeKBQA.FDA_UNII_Code_Terminology"))) {
				final String unii_code = cls.getProperty(messages.getString("ProtegeKBQA.FDA_UNII_Code")).getValue();
				if (unii_code == null || unii_code.length() == 0) {
					hasUNII_Code = false;
				}
				for (final Property syn : unii_syn) {
					// if (syn.getValue().contains("FDA")) {
					// hasFULL_SYN = true;
					// }
					if (getQualifierValue(syn, messages.getString("ProtegeKBQA.term_source")).equals("FDA")) {
						hasFULL_SYN = true;
					}

				}
				if (!hasFULL_SYN || !hasUNII_Code) {
					String message = " ";
					if (!hasFULL_SYN) {
						message = message.concat("Missing FDA Full Syn ");
					}
					if (!hasUNII_Code) {
						message = message.concat("Missing UNII Code ");
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
			System.out.println("Error in PrintWriter");
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
					if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("--config")) {
						this.configFile = args[++i];
					} else if (args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("--input")) {
						physicalURI = URI.create(args[++i]);
					} else if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("--output")) {
						outputFile = URI.create(args[++i]);
					} else {
						printHelp();
					}
				}
			} else {
				printHelp();
			}

			if (this.configFile == null) {
				this.configFile = sysProp.getProperty("nciowlqa.properties");
			}
			System.out.println("Config file at: " + this.configFile);

			final Properties props = new Properties();
			props.load(new FileInputStream(this.configFile));
			this.ontologyNamespace = props.getProperty("namespace");
			if (physicalURI == null) {
				physicalURI = URI.create(props.getProperty("physicalURI"));
			}
			System.out.println("Input file is " + physicalURI.toString());
			this.stys = setFromConfig(props.getProperty("semantictypefile"));
			this.styPairs = setFromConfig(props.getProperty("semanticPairsFile"));
			this.ignoreSources = setFromConfig(props.getProperty("ignorefile"));
			this.drugEditors = setFromConfig(props.getProperty("drugeditorsfile"));
			this.retiredBranch = URI.create(props.getProperty("deprecatedConceptBranch"));
			this.messages = new Messages(props.getProperty("messagesLocation"));

			if (outputFile == null) {
				outputFile = URI.create(props.getProperty("outputfile"));
			}
			if (outputFile.toString().length() == 0) {
				System.out.println("No output file specified");
				printHelp();
			}
			System.out.println("Output file is " + outputFile.toString());

			readSymbolMap(props.getProperty("symbolMap"));

			configPrintWriter(outputFile);
			this.ontology = new OWLKb(physicalURI, this.ontologyNamespace);

		} catch (final java.io.FileNotFoundException e) {
			System.out.println("Error in reading config files");
			e.printStackTrace();
			System.exit(1);
		} catch (final IllegalArgumentException e) {
			System.out.println("Error in parameter");
			e.printStackTrace();
			System.exit(1);
		} catch (final Exception e) {
			System.out.println("Error in reading ontology");
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
		return URI.create(this.ontologyNamespace + "#" + className);
	}

	private Vector<String> getAQ_CTRMFullSyn(ConceptProxy c) {
		final String propertyname = messages.getString("ProtegeKBQA.FULL_SYN");
		final Vector<String> out = new Vector<String>();
		final Vector<Property> v = c.getProperties(propertyname);
		for (final Property prop : v) {
			final String pt = prop.getValue();
			if (pt.indexOf("ncicp:") > 0) {
				if (pt.indexOf("<ncicp:term-source>CTRM</ncicp:term-source>") > 0
						|| pt.indexOf("<ncicp:term-group>AQ</ncicp:term-group>") > 0
						|| pt.indexOf("<ncicp:term-group>HD</ncicp:term-group>") > 0) {
					final int start = pt.indexOf("<ncicp:term-name>") + 17;
					final int end = pt.indexOf("</ncicp:term-name>");
					String fullSyn = pt.substring(start, end);
					if (fullSyn.indexOf("&amp;") > 0) {
						fullSyn = removeAmpersandReplacement(fullSyn);
					}
					out.add(fullSyn);
				}
			} else {

				if (prop.getQualifier(messages.getString("ProtegeKBQA.Term_Source")).equals("CTRM") //$NON-NLS-2$
						|| prop.getQualifier(messages.getString("ProtegeKBQA.Term_Group")).getValue().equals("AQ")
						|| prop.getQualifier(messages.getString("ProtegeKBQA.Term_Group")).getValue().equals("HD")) {
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}

	private Vector<String> getFullSynBySourceAndGroup(ConceptProxy c, String source, String group) {
		final Vector<String> out = new Vector<String>();
		// System.out.println("getFullSynBySourceAndGroup");
		for (Property prop : c.getProperties()) {
			// System.out.println("Property " + prop.getName());
			String sourceQual = getQualifierValue(prop, messages.getString("ProtegeKBQA.Term_Source"));
			String groupQual = getQualifierValue(prop, messages.getString("ProtegeKBQA.Term_Group"));
			if (sourceQual != null && groupQual != null) {
				// System.out.println(sourceQual + " " + groupQual);
				if (sourceQual.equals(source) && groupQual.equals(group)) {
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}

	private Vector<String> getFullSynBySource(ConceptProxy c, String source) {
		final Vector<String> out = new Vector<String>();
		for (Property prop : c.getProperties()) {
			String sourceQual = getQualifierValue(prop, messages.getString("ProtegeKBQA.Term_Source"));
			if (sourceQual != null) {
				if (sourceQual.equals(source)) {
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}

	private Vector<String> getFullSynByGroup(ConceptProxy c, String group) {
		final Vector<String> out = new Vector<String>();
		for (Property prop : c.getProperties()) {
			String groupQual = getQualifierValue(prop, messages.getString("ProtegeKBQA.Term_Group"));
			if (groupQual != null) {
				if (groupQual.equals(group)) {
					out.add(prop.getValue());
				}
			}
		}
		return out;
	}

	private Vector<String> getNCIPTFullSyn(ConceptProxy c) {
		final String propertyname = messages.getString("ProtegeKBQA.FULL_SYN");
		final Vector<String> out = new Vector<String>();
		final Vector<Property> v = c.getProperties(propertyname);
		for (final Property prop : v) {
			final String pt = prop.getValue();
			if (pt.indexOf("<ncicp:term-source>NCI</ncicp:term-source>") > 0
					& pt.indexOf("<ncicp:term-group>PT</ncicp:term-group>") > 0) {
				final int start = pt.indexOf("<ncicp:term-name>") + 17;
				final int end = pt.indexOf("</ncicp:term-name>");
				String fullSyn = pt.substring(start, end);
				if (fullSyn.indexOf("&amp;") > 0) {
					fullSyn = removeAmpersandReplacement(fullSyn);
				}
				out.add(fullSyn);
			} else {
				boolean isPT = false, isNCI = false;
				for (final Qualifier qual : prop.getQualifiers()) {
					if (qual.getValue().equals(messages.getString("ProtegeKBQA.PT"))) {
						isPT = true;
					}
					if (qual.getValue().equals("NCI")) {
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
			String source = getQualifierValue(synProp, messages.getString("ProtegeKBQA.Term_Source"));

			//
			// final int beginning = syn.indexOf("<ncicp:term-source>");
			// final int end = syn.indexOf("</ncicp:term-source>");
			// if (beginning > 0 && end > 20) {
			// source = syn.substring(beginning + 19, end);
			// } else if
			// (synProp.getQualifier(messages.getString("ProtegeKBQA.Term_Source"))
			// != null) {
			// source =
			// synProp.getQualifier(messages.getString("ProtegeKBQA.Term_Source")).getValue();
			// // System.out.println("Bad property " + syn);
			// }
			final String fullSyn = concatenateFullSyn(synProp);
			// if (beginning > 0) {
			// source = syn.substring(beginning + 19, end);
			// } else {
			// final Vector<Qualifier> quals = synProp.getQualifiers();
			// for (final Qualifier qual : quals) {
			// if
			// (qual.getName().equals(messages.getString("ProtegeKBQA.Term_Source")))
			// {
			// source = qual.getValue();
			// }
			// }
			// }

			synMap.put(fullSyn, source != null ? source : "");

		}
		return synMap;
	}

	// private String concatenateFullSyn(String FSvalue) {
	// //
	// <ncicp:ComplexTerm><ncicp:term-name>11-Cyclopropyl-5,11-dihydro-4-methyl-6H-dipyrido(3,2-b:2',3'-e)(1,4)diazepin-6-one</ncicp:term-name><ncicp:term-group>SN</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source></ncicp:ComplexTerm>
	//
	// String source = "";
	// int beginning = FSvalue.indexOf("<ncicp:term-source>");
	// int end = FSvalue.indexOf("</ncicp:term-source>");
	// source = FSvalue.substring(beginning + 19, end);
	//
	// String group = "";
	// beginning = FSvalue.indexOf("<ncicp:term-group>");
	// end = FSvalue.indexOf("</ncicp:term-group>");
	// group = FSvalue.substring(beginning + 18, end);
	//
	// String value = "";
	// beginning = FSvalue.indexOf("<ncicp:term-name>");
	// end = FSvalue.indexOf("</ncicp:term-name>");
	// value = FSvalue.substring(beginning + 17, end);
	//
	//
	// return source + "|" + group + "|" + value; //$NON-NLS-2$
	// }

	private String concatenateFullSyn(Property synProp) {
		String source="";
		String group="";
		String value=synProp.getValue();

		final Vector<Qualifier> quals = synProp.getQualifiers();
		for (final Qualifier qual : quals) {
			if (qual.getCode().equals(messages.getString("ProtegeKBQA.Term_Source"))) {
				source = qual.getValue();
			} else if (qual.getCode().equals(messages.getString("ProtegeKBQA.Term_Group"))) {
				group = qual.getValue();
			}

		}

		return source + "|" + group + "|" + value; //$NON-NLS-2$
	}

	/**
	 * Perform qa.
	 */
	private void performQA() {

		// Retired concepts should not be considered for QA
		// qaRetiredConcepts();
		boolean metricsGood = checkMetrics();
		if (!metricsGood) {
			System.out.println("Metrics Failed");
			pw.println("Metrics failed");
			pw.close();
			System.exit(0);
		}

		// System.out.println( messages.getString("ProtegeKBQA.Term_Source"));
		// System.out.println( messages.getString("ProtegeKBQA.Term_Group"));

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
				checkAltsWithNCI(cls);
				checkDefCurator(cls);
//				checkContributingSouceAndAltDef(cls);
				checkSemanticTypes(cls);
				checkSemanticTypeExistenceAndUniqueness(cls);
				checkFDA_UNII(cls);
				checkCharacter(cls, '\n');
				checkCharacter(cls, '@');
				checkCharacter(cls, '|');
				checkCharacter(cls, '\t');
				checkPreferredNameBug(cls);
				checkForEmptyQualifiers(cls);
				cls.unloadProperties();
			}
		}
		checkForValueSetSources();
		// now print out all the issues discovered.
		printReport();

		// close printwriter
		this.pw.close();
	}

	private boolean checkMetrics() {

		// check axiom count
		int x = ontology.getAxiomCount();
		if (ontology.getAxiomCount() == 0) {
			return false;
		}
		if (ontology.getAxiomCount() < 2000000 || ontology.getAxiomCount() > 3000000) {
			return false;
		}
		x = ontology.getGciCount();
//		if (ontology.getGciCount() == 0) {
//			return false;
//		}

		x=ontology.getHiddenGciCount();
		if (ontology.getHiddenGciCount() > 0) {
			return false;
		}

		x=ontology.getMultiInheritanceCount();
		if (ontology.getMultiInheritanceCount() == 0) {
			return false;
		}

		if (ontology.getDLExpressivity() == null) {
			return false;
		}
		if (!ontology.getDLExpressivity().equals("SH")) {
			return false;
		}

		return ontology.getRootConceptCodes().size() == 21;
	}

	private boolean assessResults() {
		boolean QApassed = false;

		return QApassed;
	}

	/**
	 * Prints the report.
	 */
	@SuppressWarnings({ "rawtypes" })
	private void printReport() {

		this.pw.println("Replaced high bit characters: " + this.replacedHighBitCharacters.size());
		List sortedReturn = sortHashMapByKey(this.replacedHighBitCharacters);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing an incorrect Semantic_Type: " + this.bad_semantictypes.size());
		sortedReturn = sortHashMapByKey(this.bad_semantictypes);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with no Semantic_Type property: " + this.no_ST.size());
		sortedReturn = sortHashMapByKey(this.no_ST);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Retired Concepts with bad Concept_Status: " + this.badCS.size());
		sortedReturn = sortHashMapByKey(this.badCS);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Active concepts with retired Concept_Status: " + this.badCS_active.size());
		sortedReturn = sortHashMapByKey(this.badCS_active);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with duplicate roles within: " + this.duplicate_role.size());
		sortedReturn = sortHashMapByKey(this.duplicate_role);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with duplicate properties within: " + this.duplicate_property.size());
		sortedReturn = sortHashMapByKey(this.duplicate_property);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with multiple NCI|PT FULL-SYN properties: " + this.multiple_PT.size());
		sortedReturn = sortHashMapByKey(this.multiple_PT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with quotes around entire definition: " + this.quoted_DEF.size());
		sortedReturn = sortHashMapByKey(this.quoted_DEF);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with no Preferred_Name property: " + this.no_PN.size());
		sortedReturn = sortHashMapByKey(this.no_PN);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with multiple Preferred_Name properties: " + this.multiple_PN.size());
		sortedReturn = sortHashMapByKey(this.multiple_PN);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts where the NCI|PT and Preferred_Name don't match: " + this.nomatch_pnpt.size());
		sortedReturn = sortHashMapByKey(this.nomatch_pnpt);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Possible instances of Preferred_Name / Last property bug: " + this.PNbug.size());
		sortedReturn = sortHashMapByKey(this.PNbug);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"Concepts with no NCT|PT FULL-SYN property (Excludes HD, AQ and CTRM concepts): " + this.no_PT.size());
		sortedReturn = sortHashMapByKey(this.no_PT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Empty properties and qualifiers:" + this.emptyValue.size());
		sortedReturn = sortHashMapByKey(this.emptyValue);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("High bit characters: " + this.highBitCharacters.size());
		sortedReturn = sortHashMapByKey(this.highBitCharacters);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character @: " + this.badchar_at.size());
		sortedReturn = sortHashMapByKey(this.badchar_at);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character \\n : " + this.badchar_newline.size());
		sortedReturn = sortHashMapByKey(this.badchar_newline);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing character | : " + this.badchar_pipe.size());
		sortedReturn = sortHashMapByKey(this.badchar_pipe);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Property values containing \\t : " + this.badchar_tab.size());
		sortedReturn = sortHashMapByKey(this.badchar_tab);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"Concepts where the FDA_UNII_Code or FDA PT are missing for UNII Concepts: " + this.missingUNII.size());
		sortedReturn = sortHashMapByKey(this.missingUNII);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with an LLT FULL_SYN that is not MedDRA: " + this.lltNotMedDRA.size());
		sortedReturn = sortHashMapByKey(this.lltNotMedDRA);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Concepts with multiple DEFINITION properties: " + this.multiple_DEF.size());
		sortedReturn = sortHashMapByKey(this.multiple_DEF);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println("Def_Curator concepts without proper Definition_Review_Name: " + this.no_DefReview.size());
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
		this.pw.println("Value Set concepts with no Contributing Source or multiple Contributing Sources: " + this.vsNoCS.size());
		sortedReturn = sortHashMapByKey(this.vsNoCS);
		for(final  Object o:sortedReturn){
			this.pw.println(o.toString());
		}

//TODO		this.pw.println();
//		this.pw.println("Empty value sets");
//		sortedReturn= sortHashMapByKey(this.emptyValueSet);
//		for (final Object o:sortedReturn){
//			this.pw.println(o.toString());
//		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("*  Concepts with multiple, potentially conflicting, Semantic_Type properties: "
				+ this.multiple_ST.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");

		sortedReturn = sortHashMapByKey(this.multiple_ST);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("Preferred Names duplicated between concepts: " + this.duplicate_pn.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.duplicate_pn);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("NCI|PT duplicated between concepts: " + this.duplicate_pt.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.duplicate_pt);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("Contributing source concepts without proper ALT_DEFINITION: " + this.no_alt_def.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.altDefSourceCount);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}
		sortedReturn = sortHashMapByKey(this.no_alt_def);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("Contributing sources with no matching FULL_SYN: " + this.no_SynForContributingSource.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.no_SynForContributingSource);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("FULL_SYNs with no matching contributing source: " + this.no_ContributingSourceForSyn.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.no_ContributingSourceForSyn);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("Antiquated, Header or CTRM concepts with no NCT|PT FULL-SYN property : "
				+ this.antiquated_noPT.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
		sortedReturn = sortHashMapByKey(this.antiquated_noPT);
		for (final Object o : sortedReturn) {
			this.pw.println(o.toString());
		}

		this.pw.println();
		this.pw.println(
				"********************************************************************************************************");
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println("Concepts with no DEFINITION property, sorted by Semantic Type: " + this.no_DEF.size());
		this.pw.println(
				"*                                                                                                      *");
		this.pw.println(
				"********************************************************************************************************");
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
		} finally {
			// Closing the streams
			try {
				assert buff != null;
				buff.close();
				assert configFile!=null;
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
			buff = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8.name()));
			boolean eof = false;
			while (!eof) {
				final String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else if (!line.startsWith("#")) {
					UnicodeConverter uc = new UnicodeConverter(line);
					symbolMap.put(uc.getUnicodeChar(), uc);
					v.add(line);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				assert buff!=null;
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
		return s.replace("&amp;", "&");
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

		// final URI branchToDelete = createURI(this.retiredBranch);
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
//		final Vector<ConceptProxy> retiredBranchClasses = new Vector<ConceptProxy>();

		final ConceptProxy retiredRoot = this.ontology.getConcept(this.retiredBranch);
//		retiredBranchClasses.add(retiredRoot);
		final Vector<URI> retiredConceptCodes = retiredRoot.getAllDescendantCodes();
		if (retiredConceptCodes != null) {
			for (final URI code : retiredConceptCodes) {
				final ConceptProxy concept = this.ontology.getConcept(code);
//				retiredBranchClasses.add(concept);
				final Vector<Property> v = concept.getProperties("Concept_Status");
				if (v == null || v.size() == 0) {
					this.badCS.put(code.getFragment(), "no Concept Status");
				} else if (v.size() > 1) {
					if (!isRetiredInSet(v)) {
						this.badCS.put(code.getFragment(), "Multiple Concept Status, no Retired Status");
					}

				} else if (!v.get(0).getValue().equals("Retired_Concept")) {
					this.badCS.put(code.getFragment(), "Concept Status = " + v.get(0).getValue());
				}
			}
		}

		for (final URI code : this.ontology.getAllConceptCodes()) {
			final ConceptProxy activeConcept = this.ontology.getConcept(code);
			final boolean retired = this.ontology.isDeprecated(code);
			if (!retired) {
				final Vector<Property> v = activeConcept.getProperties("Concept_Status");
				for (final Property prop : v) {
					if (prop.getValue().equals("Retired_Concept")) {
						this.badCS_active.put(code.getFragment(), "Active marked Retired_Concept");
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
			if (status.getValue().equals("Retired_Concept")) {
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
		final List<Map.Entry<String, String>> list = new Vector<Map.Entry<String, String>>(hm.entrySet());

		final Comparator byValue = (Comparator<Map.Entry<String, String>>) (entry, entry1) -> {
			// Return 0 for a match, -1 for less than and +1 for more then
			return (entry.getValue().compareTo(entry1.getValue()));
		};

		final Comparator byKey = (Comparator<Map.Entry<String, String>>) (entry, entry1) -> {
			// Return 0 for a match, -1 for less than and +1 for more then
			return (entry.getKey().compareTo(entry1.getKey()));
		};

		list.sort(new CompositeComparator(byKey, byValue));

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
		final List<Map.Entry<String, String>> list = new Vector<Map.Entry<String, String>>(hm.entrySet());

		final Comparator byValue = (Comparator<Map.Entry<String, String>>) (entry, entry1) -> {
			// Return 0 for a match, -1 for less than and +1 for more then
			if (entry.getValue() == null) {
				return -1;
			} else if (entry1.getValue() == null) {
				return +1;
			} else {
				return (entry.getValue().compareTo(entry1.getValue()));
			}
		};

		final Comparator byKey = new Comparator<Map.Entry<String, String>>() {
			@Override
			public int compare(Map.Entry<String, String> entry, Map.Entry<String, String> entry1) {
				// Return 0 for a match, -1 for less than and +1 for more then
				return (entry.getKey().compareTo(entry1.getKey()));
			}
		};

//		final Comparator byKey = new Comparator<Map.Entry<String, String>>() {
//			@Override
//			public int compare(Map.Entry<String, String> entry, Map.Entry<String, String> entry1) {
//				// Return 0 for a match, -1 for less than and +1 for more then
//				return (entry.getKey().compareTo(entry1.getKey()));
//			}
//		};

		// Sort the list using an annonymous inner class implementing Comparator
		// for the compare method

		list.sort(new CompositeComparator(byValue, byKey));

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
		// "<term-name>Basal Transcription
		// Factor</term-name><term-group>PT</term-group><term-source>NCI</term-source>";

		// <ncicp:ComplexTerm><ncicp:term-name>Third</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>NCI</ncicp:term-source></ncicp:ComplexTerm>

		// Find the first ">"
		int n = pattern.indexOf(">");
		StringBuilder retstr = new StringBuilder();
		int lcv = 0;
		while (n != -1) {
			String tag = pattern.substring(0, n + 1);
			pattern = pattern.substring(n + 1, pattern.length());
			// Find the first "<" after the first ">"
			n = pattern.indexOf("<");
			if (n == -1) {
				break;
			}

			// Grab the value between the ">" and "<"
			final String value = pattern.substring(0, n);

			// Check to see if any value was actually returned
			if (value.length() > 0) {
				retstr.append(value);
				retstr.append("|");
				lcv++;
			}

			pattern = pattern.substring(n);

			n = pattern.indexOf(">");
			if (n == -1) {
				break;
			}

			tag = pattern.substring(0, n + 1);

		}

		if (lcv > 0) {
			retstr = new StringBuilder(retstr.substring(0, retstr.length() - 1));
		} else {
			retstr = new StringBuilder(pattern);
		}
		return (retstr.toString());
	}

	private void checkPreferredNameBug(ConceptProxy c) {
		// checks to see if there are any properties that exactly match the
		// preferred name.
		final String propertyName = messages.getString("ProtegeKBQA.PreferredName");
		final String code = c.getCode();
		Property propPreferredName = c.getProperty(propertyName);
		if (propPreferredName != null) {
			final String preferredName = c.getProperty(propertyName).getValue();
			final Vector<Property> properties = c.getProperties();
			for (final Property prop : properties) {
				if (prop.getValue().equals(preferredName)) {
					if (!prop.getCode().equals(messages.getString("ProtegeKBQA.PreferredName"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.FULL_SYN"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.rdfs_Label"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.Legacy_Name"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.Display_Name"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.Has_Salt_Form"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.NICHD_Hierarchy_Term"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.Semantic_Type"))
							&& !prop.getCode().equals(messages.getString("ProtegeKBQA.Maps_To"))) {
						this.PNbug.put(code+"_"+c.getName(), prop.getCode() + " " + prop.getValue());
					} else if (prop.getCode().equals(messages.getString("ProtegeKBQA.Semantic_Type"))) {
						// check to see if it is a legitimate semantic type. If
						// not,
						// report
						if (!this.stys.contains(prop.getValue())) {
							this.PNbug.put(code+"_"+c.getName(), prop.getCode() + " " + prop.getValue());
						}
					}
				}
			}
		} else {
			System.out.println("Preferred Name bug not checked on " + code);
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
						this.emptyValue.put(code+"_"+c.getName(), prop.getCode() + " " + qual.getName());
					}
				}
			}
		}
	}

	private void checkMedDRA_LLT(ConceptProxy cls) {
		// This will check that all LLT full-syns have a MedDRA source.
		final Vector<Property> fullsyns = cls.getProperties(messages.getString("ProtegeKBQA.FULL_SYN"));
		for (final Property syn : fullsyns) {
			if (getQualifierValue(syn, messages.getString("ProtegeKBQA.Term_Group")).equals("LLT")
					&& !(getQualifierValue(syn, messages.getString("protegeKBQA.Term_Source")).equals("MedDRA"))) {
				lltNotMedDRA.put(cls.getCode()+"_"+cls.getName(), syn.getValue());

				// if (syn.getValue().contains(
				// "<ncicp:term-group>LLT</ncicp:term-group>")) {
				// if (syn.getValue().contains(
				// "<ncicp:term-source>MedDRA</ncicp:term-source>")) {
				// // is MedDRA
				//
				// } else {
				// lltNotMedDRA.put(cls.getCode(), syn.getValue());
				// }
				// } else if
				// (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Group"))
				// != null) {
				// if
				// (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Group")).getValue().equals(messages.getString("ProtegeKBQA.LLT")))
				// { //$NON-NLS-2$
				// if
				// (syn.getQualifier(messages.getString("ProtegeKBQA.Term_Source")).getValue()
				// .equals("MedDRA")) {
				// // isMedDRA
				// } else {
				// lltNotMedDRA.put(
				// cls.getCode(),
				// syn.getQualifier(messages.getString("ProtegeKBQA.Term_Source"))
				// + " " //$NON-NLS-2$
				// + syn.getValue());
				// }
				// }
			}
		}
	}

	private void checkAltsWithNCI(ConceptProxy c) {
		if (c.getCode().contentEquals("C28944")) {
			String debug = "Stop here";
		}
		String altDef = null;
		Vector<Property> v = c.getProperties(messages.getString("ProtegeKBQA.Alt_Definition"));
		Vector<Property> results = new Vector<Property>();
		for (Property def : v) {
			Qualifier source = def.getQualifier(messages.getString("ProtegeKBQA.Def_Source"));
			if (source != null && source.getValue().equals("NCI")) {
				System.out.println("Adding Alt-Def with NCI source" + c.getCode());
				results.add(def);
			}
		}
		for (final Property def : results) {
			if (altDef == null) {
				altDef = def.getValue();
			} else {
				altDef = altDef.concat("|" + def.getValue());
			}
		}
//		if (altDef != null) {
//			// this.altDefNCI.put(c.getCode(), altDef);
//		}
	}

	private void checkForValueSetSources(){
		//Checks that all publishable value sets have a source
	//Should have Publish_Value_Set = true
	//Start with C54443 “Terminology Subset”
		final ConceptProxy subsetRoot = this.ontology.getConcept("C54443");
		Vector<URI> subsetConcepts = subsetRoot.getAllDescendantCodes();

		if (subsetConcepts != null) {
			for (final URI code : subsetConcepts) {
				final ConceptProxy concept = this.ontology.getConcept(code);
//				retiredBranchClasses.add(concept);
				final Vector<Property> publish = concept.getProperties(messages.getString("ProtegeKBQA.Publish_Value_Set"));
				if (publish != null && publish.size() ==1) {
					if (publish.get(0).getValue().toUpperCase().equals("YES")) {
						checkEmptyValueSets(concept);
						final Vector<Property> v = concept.getProperties(messages.getString("ProtegeKBQA.Contributing_Source"));
						if (v == null || v.size() == 0) {
							this.vsNoCS.put(code.getFragment(), "no Contributing Source " + concept.getName());
						} else if (v.size() > 1) {
							this.vsNoCS.put(code.getFragment(), "Multiple Contributing Sources " + concept.getName());
						}
					}
				} else if (publish != null && publish.size()>1){
					this.vsNoCS.put(code.getFragment(),"Multiple Publish_Value_Set " + concept.getName());
				}
			}
			}

	}

	private void checkEmptyValueSets(ConceptProxy concept) {
		//Assumptions: this concept is a value set and set to publish
		//Does it have any Concept_In_Subset incoming associations?
		//TODO this does not work.  The error is in the OWLKb
//		Vector<Association> incoming = concept.getIncomingAssociations();
//		if(incoming ==null | incoming.size()==0){
//			this.emptyValueSet.put(concept.getCode(), concept.getName());
//		}


	}

}
