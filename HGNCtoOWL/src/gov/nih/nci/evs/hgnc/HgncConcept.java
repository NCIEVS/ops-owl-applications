package gov.nih.nci.evs.hgnc;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

public class HgncConcept {

	String code = "";
	// Vector<String> family;
	String parent = null;
	Vector<String> nonDelimitedProperties = new Vector<String>();
	Vector<String> delimitedProperties = new Vector<String>();
	HashMap<String, Vector<String>> tokenizedDelimitedProperties = new HashMap<String, Vector<String>>();
	HashMap<String, Vector<String>> tokenizeNonSpecialistProperties = new HashMap<String, Vector<String>>();
	HashMap<String, String> properties = new HashMap<String, String>();
	HashMap<String, String> simpleProperties = new HashMap<String, String>();
	HashMap<String, String> specialistDatabaseIds = new HashMap<String, String>();
	HashMap<String, String> specialistDatabaseLinks = new HashMap<String, String>();

	public HgncConcept(HashMap<String, String> propertyValueList) {
		// Assume that HGNC ID is the identifier
//		code = propertyValueList.get("HGNC ID");
		//TODO externalize string
		code = propertyValueList.get("hgnc_id");
		properties = propertyValueList;
		separateDelimitedProperties();
		delimitDelimitedProperties();
		loadSimpleProperties();
		loadSpecialistDatabase();
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
//		parent = propertyValueList.get("Locus Type");
		//TODO - externalize string
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
			String delimiterName = (String) HgncToOwl.getDelimitedColumns()
			        .get(HgncToOwl.underscoredString(key));
			String delimiter = (String) HgncToOwl.getDelimiters().get(
			        delimiterName);
			String value = properties.get(key);
			if (value != null && value.length() > 0) {
				Vector<String> delimitedString;
				//TODO externalize string
				if (key.contains("lsdb")) {
					// System.out.println(value);
					delimitedString = new Vector<String>();
					if (value.endsWith(delimiter)) {
						value = value + " ";
					}
					String[] result = value.split(delimiter);
					for (String element : result)
						// System.out.println(x + " " + result[x]);
						delimitedString.add(element.trim());
				} else {
					Pattern p = Pattern.compile(delimiter);
					delimitedString = HgncCsvParser.tokenizeString(value, p);
				}
				tokenizedDelimitedProperties.put(key, delimitedString);
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

	private void loadSpecialistDatabase() {
		String prefix = (String) HgncToOwl.getSpecialistDatabases().get("name");
		// System.out.println("Start LOAD: " + this.code);
		Set<String> propNames = tokenizedDelimitedProperties.keySet();
		HashMap<String, String> values = new HashMap<String, String>();
		Properties test = HgncToOwl.getSpecialistDatabases();
		for (String propName : propNames) {
			if (propName.startsWith(prefix)) {
				Vector<String> propValues = tokenizedDelimitedProperties
				        .get(propName);
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
							this.specialistDatabaseLinks.put(
							        HgncToOwl.getSpecialistDatabases()
							                .getProperty(i.toString()), value);
						} else if (propName.equals("Specialist Database IDs")) {
							this.specialistDatabaseIds.put(
							        HgncToOwl.getSpecialistDatabases()
							                .getProperty(i.toString()), value);
						} else {
							System.out
							        .println("A new Specialist Database category has been added.  Modify HugoConcept.java.");
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
				tokenizeNonSpecialistProperties.put(propName,
				        tokenizedDelimitedProperties.get(propName));
			}
		}

		// Remove the SpecialistProperties from the regular delimited Properties
		// list

	}

	public HashMap<String, String> getSimpleProperties() {
		return simpleProperties;
	}

	public HashMap<String, Vector<String>> getDelimitedProperties() {
		return tokenizeNonSpecialistProperties;
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

}
