package gov.nih.nci.evs.hgnc;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class HgncOntology {

	private HgncCsvParser cvsParser = null;
	private Vector<HgncConcept> concepts = new Vector<HgncConcept>();
	private HashMap<String, String> locus = new HashMap<String, String>();

	public HgncOntology(HgncCsvParser in_cvsParser) {
		cvsParser = in_cvsParser;
		createConcepts();
	}

	private void createConcepts() {

		Vector<String> header = cvsParser.getHeader();
		// loop through the data. Create a new concept for each line.
		for (Vector<String> values : cvsParser.getData()) {
			HashMap<String, String> propertyValueList = new HashMap<String, String>();
			for (int i = 0; i < header.size(); i++) {
				if (i < values.size()) {
					
					propertyValueList.put(header.get(i), values.get(i));
				} else {
					propertyValueList.put(header.get(i), null);
				}
			}
			loadLocusType_LocusGroup(propertyValueList);
			HgncConcept concept = new HgncConcept(propertyValueList);
			concepts.add(concept);
		}

	}

	public Vector<HgncConcept> getConcepts() {
		return concepts;
	}

	private void loadLocusType_LocusGroup(HashMap<String, String> valueList) {
		Set<String> keySet = valueList.keySet();
		String locusType = "", locusGroup = "";
		for (String key : keySet) {
			if (key.equals("Locus Type") || key.equals("locus_type")) {
				locusType = valueList.get(key);
			}
			if (key.equals("Locus Group")|| key.equals("locus_group")) {
				locusGroup = valueList.get(key);
			}
		}
		if (!locus.containsKey(locusType)) {
			locus.put(locusType, locusGroup + "_group");
		}
	}

	public HashMap<String, String> getLocusHierarchy() {
		return locus;
	}

}
