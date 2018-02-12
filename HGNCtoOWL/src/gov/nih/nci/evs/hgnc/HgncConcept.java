package gov.nih.nci.evs.hgnc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

public class HgncConcept {

	String code = "";
	// Vector<String> family;
	String parent = null;
	Vector<String> nonDelimitedProperties = new Vector<>();
	Vector<String> delimitedProperties = new Vector<>();
	HashMap<String, Vector<String>> tokenizedDelimitedProperties = new HashMap<>();
	HashMap<String, Vector<String>> tokenizeNonSpecialistProperties = new HashMap<>();
	HashMap<String, String> properties = new HashMap<>();
	HashMap<String, String> simpleProperties = new HashMap<>();
	HashMap<String, String> specialistDatabaseIds = new HashMap<>();
	HashSet<String> lsdb = new HashSet<>();
	HashMap<String, String> specialistDatabaseLinks = new HashMap<>();
	HashSet<SpecialistDatabase> lsdbs = new HashSet<>();

	public HgncConcept(HashMap<String, String> propertyValueList) {
		// Assume that HGNC ID is the identifier
		// code = propertyValueList.get("HGNC ID");
		// TODO externalize string
		code = propertyValueList.get("hgnc_id");
		properties = propertyValueList;
		separateDelimitedProperties();
		delimitDelimitedProperties();
		loadSimpleProperties();
		// loadSpecialistDatabase();
		// family = tokenizedDelimitedProperties.get("Gene Family Name");
		// if (family == null) {
		// family = new Vector<String>();
		// }
		// if (propertyValueList.get("Approved Symbol") != null) {
		// if (propertyValueList.get("Approved Symbol").contains("~withdrawn"))
		// {
		// family.add("Withdrawn");
		// }
		// }
		// parent = propertyValueList.get("Locus Type");
		// TODO - externalize string
		parent = propertyValueList.get("locus_type");
	}

	private void separateDelimitedProperties() {
		Set<String> keys = properties.keySet();
		for (String key : keys) {
			String keyUnderscored = key.replace(" ", "_");
			if (HgncToOwl.getDelimitedColumns().containsKey(keyUnderscored)) {
				delimitedProperties.add(key);
			} else {
				nonDelimitedProperties.add(key);
			}
		}
	}

	private void delimitDelimitedProperties() {
		for (String key : delimitedProperties) {
			// String keyUnderscored = key.replace(" ", "_");
			String delimiterName = (String) HgncToOwl.getDelimitedColumns().get(HgncToOwl.underscoredString(key));
			String delimiter = (String) HgncToOwl.getDelimiters().get(delimiterName);
			String value = properties.get(key);
			if (value != null && value.length() > 0) {
				Vector<String> delimitedString = new Vector<>();
				;
				// TODO externalize string
				// if (key.contains("lsdb")) {
				if (delimiterName.equals("NVP")) {
					// System.out.println(value);
					loadSpecialistDatabase(value);

				} else {
					Pattern p = Pattern.compile(delimiter);
					delimitedString = HgncCsvParser.tokenizeString(value, p);
					tokenizedDelimitedProperties.put(key, delimitedString);
				}

			}
		}
	}

	private void loadSimpleProperties() {
		for (String key : nonDelimitedProperties) {
			String value = properties.get(key);
			if (value != null && value.length() > 0) {
				// System.out.println( this.code + "\t" + key + "\t" + value);
				simpleProperties.put(key, value);
			}
		}
	}

	private void loadSpecialistDatabase(String value) {
		String delimiter = "\\|";
		Vector<String> delimitedString = new Vector<>();
		String[] result = value.split(delimiter);
		if (result.length % 2 == 0) {
			// These are name/value pairs. If an odd number then something is
			// wrong
			for (int x = 0; x < result.length; x++) {
				specialistDatabaseIds.put(result[x], result[x + 1]);
				lsdb.add(result[x] + "|" + result[x + 1]);

				lsdbs.add(new SpecialistDatabase(result[x], result[x + 1]));
				x++;
			}
		} else {
			System.out.println("Error parsing " + value);
		}
		// for (String element : result)
		// // System.out.println(x + " " + result[x]);
		// delimitedString.add(element.trim());
	}

	private void loadSpecialistDatabase() {
		String prefix = (String) HgncToOwl.getSpecialistDatabases().get("name");
		// System.out.println("Start LOAD: " + this.code);
		Set<String> propNames = tokenizedDelimitedProperties.keySet();
		HashMap<String, String> values = new HashMap<>();
		Properties test = HgncToOwl.getSpecialistDatabases();
		for (String propName : propNames) {
			if (propName.startsWith(prefix)) {
				Vector<String> propValues = tokenizedDelimitedProperties.get(propName);
				// System.out.println("\t" + propName + "\t" + propValues);
				for (Integer i = 0; i < propValues.size(); i++) {
					String value = propValues.get(i);
					if (value != null && value.trim().length() > 0) {
						// if (HugoToOwl.getSpecialistDatabases().size() ==
						// propValues
						// .size()) {
						// if( this.code.equals("HGNC:31499")) {
						// System.out.println("\t\t" + i.toString() + " " +
						// (String) HugoToOwl
						// .getSpecialistDatabases().getProperty(
						// i.toString()) + "\t" + value);
						// }
						if (propName.equals("Specialist Database Links")) {
							this.specialistDatabaseLinks
									.put(HgncToOwl.getSpecialistDatabases().getProperty(i.toString()), value);
						} else if (propName.equals("Specialist Database IDs")) {
							this.specialistDatabaseIds.put(HgncToOwl.getSpecialistDatabases().getProperty(i.toString()),
									value);
						} else {
							System.out.println(
									"A new Specialist Database category has been added.  Modify HugoConcept.java.");
						}
						// } else {
						// if (i == 0) {
						// System.out.println("\t\t2 " + (String) HugoToOwl
						// .getSpecialistDatabases().getProperty(
						// i.toString()) + "\t" + "blank");
						// values.put((String) HugoToOwl
						// .getSpecialistDatabases().getProperty(
						// i.toString()), " ");
						// } else {
						// System.out.println("\t\t3 " + (String) HugoToOwl
						// .getSpecialistDatabases().getProperty(
						// i.toString()) + "\t" + propValues
						// .get(i - 1));
						// values.put((String) HugoToOwl
						// .getSpecialistDatabases().getProperty(
						// i.toString()), propValues
						// .get(i - 1));
						// }
						// }
					}
				}
			} else {
				// add to nonSpecialist delimitedList
				tokenizeNonSpecialistProperties.put(propName, tokenizedDelimitedProperties.get(propName));
			}
		}

		// Remove the SpecialistProperties from the regular delimited Properties
		// list

	}

	public HashMap<String, String> getSimpleProperties() {
		return simpleProperties;
	}

	public HashMap<String, Vector<String>> getDelimitedProperties() {
//		return tokenizeNonSpecialistProperties;
		return tokenizedDelimitedProperties;
	}

	public HashMap<String, String> getSpecialistDatabaseIds() {
		return specialistDatabaseIds;
	}

	public HashMap<String, String> getSpecialistDatabaseLinks() {
		return specialistDatabaseLinks;
	}

	public String getParent() {
		return parent;
	}

	public HashSet<SpecialistDatabase> getSpecialistDatabase() {
		return lsdbs;
	}

	public HashSet<String> getLsdb() {
		return lsdb;
	}

}
