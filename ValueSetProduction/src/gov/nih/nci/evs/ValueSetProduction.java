package gov.nih.nci.evs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import gov.nih.nci.evs.owl.data.OWLKb;

public class ValueSetProduction {
	
	private final String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	private String version = null;
	private Vector<ValueSet> oldValueSets = new Vector<ValueSet>();
	private Vector<ValueSet> newValueSets = new Vector<ValueSet>();
	private Vector<Change> changes = new Vector<Change>();
	private Vector<ValueSetReport> reports = new Vector<ValueSetReport>();

	public static void main(String[] args) {

		long start = System.currentTimeMillis();
		ValueSetProduction produce = new ValueSetProduction();
		System.out.println("Configuring...");
		produce.configure(args);
		System.out.println("Initializing...");
		produce.init(args[0], args[1], args[2]);
		System.out.println("Running diff...");
		produce.diff();
		System.out.println("Printing files...");
		produce.printFiles();
		System.out.println("Finished production in "
		        + (System.currentTimeMillis() - start) / 1000 + " seconds.");
		
		
		
//		Vector<String> sources = new Vector<String>();
//		Vector<String> locations = new Vector<String>();
//		
//		sources.add("CDISC");
//		sources.add("FDA");
//		
//		locations.add("TVS_FDA_Component");
//		locations.add("TVS_CDISC_Component");
//		
//		
//		ValueSet test = new ValueSet("CDISC_Clinical_Classification_APACHE_II_Test_Code_Terminology", "C12095", "name", "definition", sources, locations);
//		test.printValueSetDefinition();
	}
	
	public void configure(String[] args) {
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				String option = args[i];
				if (option.equalsIgnoreCase("--help")) {
					printHelp();
				}
			}
		}
	}
	
	public void printHelp() {
		//print help
	}
	
	public void init(String filenameNew, String filenameOld, String ver) {
		newValueSets = buildValueSets(filenameNew);
		if( filenameOld != null && !filenameOld.equals("none") ) {
			oldValueSets = buildValueSets(filenameOld);
		}
		else {
			oldValueSets = null;
		}
		reports = buildReports(filenameNew);
		version = ver;
		
		Collections.sort(newValueSets, new Comparator<ValueSet>() {
			public int compare(final ValueSet vs1, final ValueSet vs2) {
			    return vs1.getNoCCode() - vs2.getNoCCode();
			}	
		});
		
		if (oldValueSets !=null){
		Collections.sort(oldValueSets, new Comparator<ValueSet>() {
			public int compare(final ValueSet vs1, final ValueSet vs2) {
			    return vs1.getNoCCode() - vs2.getNoCCode();
			}	
		});}
		
	}
	
	public Vector<ValueSetReport> buildReports(String filename) {
		Vector<ValueSetReport> vec = new Vector<ValueSetReport>();
		OWLKb kb = new OWLKb(filename, namespace);
		
		Vector<URI> concepts = kb.getAllDescendantsForConcept(createURI("C54443"));
		Vector<URI>	unusedConcepts = kb.getAllDescendantsForConcept(createURI("C103175"));			
		concepts.removeAll(unusedConcepts);
		
		for(URI concept : concepts ) {
			if(concept.getFragment().equals("C66741")){
				@SuppressWarnings("unused")
                String debug = "true";
			}
			// P372 == Publish_Value_Set
			// P374 == Value_Set_Location
			if( (kb.getPropertyValues(concept, createURI("P372")).contains("Yes") ) && 
				(kb.getPropertyValues(concept, createURI("P374")).size() > 0) ) {
				String name = kb.getSolePropertyValue(concept, createURI("P108")); // Preferred_Name
				String code = kb.getSolePropertyValue(concept, createURI("NHC0")); // code
				Vector<String> sources = kb.getPropertyValues(concept, createURI("P322")); // Contributing_Source
				Vector<String> ftpLocations = kb.getPropertyValues(concept, createURI("P374")); // Value_Set_Location
				ValueSetReport vsr = new ValueSetReport(name, code, ftpLocations, sources);
				vec.add(vsr);
			}
		}
		
		return vec;
	}
	
	public URI createURI(String id) {
		URI uri = null;
		try {
			uri = new URI(namespace + "#" + id);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}
	
	public Vector<ValueSet> buildValueSets(String filename) {
		Vector<ValueSet> vec = new Vector<ValueSet>();
		
		OWLKb kb = new OWLKb(filename, namespace);

		Vector<URI> concepts = kb.getAllDescendantsForConcept(createURI("C54443"));
		Vector<URI>	unusedConcepts = kb.getAllDescendantsForConcept(createURI("C103175"));			
		concepts.removeAll(unusedConcepts);
		
		for(URI concept : concepts ) {
			String publish = kb.getSolePropertyValue(concept, createURI("P372")); // Publish_Value_Set
			if( publish != null && publish.toUpperCase().equals("YES"))  {
				String className = concept.getFragment();
				String code = kb.getSolePropertyValue(concept, createURI("NHC0")); // code
			if (code.equals("C66741")){
				@SuppressWarnings("unused")
                String debug = "true";
			}
				String name = kb.getSolePropertyValue(concept, createURI("P108")); // Preferred_Name
				Vector<String> definitions = kb.getPropertyValues(concept, createURI("P97")); // DEFINITION
				Vector<String> descriptions = kb.getPropertyValues(concept, createURI("P376")); // Term_Browser_Value_Set_Description
				String definition = null;
				for( String def : definitions ) {
					definition = def;
				}
				for( String desc : descriptions ) {
					definition = desc;
				}
				Vector<String> contributingSources = kb.getPropertyValues(concept, createURI("P322")); // Contributing_Source
				Vector<String> tvsLocations = kb.getPropertyValues(concept, createURI("P373")); // TVS_Location
				
				ValueSet vs = new ValueSet(className, code, name, definition, contributingSources, tvsLocations);
				vec.add(vs);
				
//				System.out.println(className + "\t" + code + "\t" + name + "\t" + definition + "\t" + contributingSources.toString() + "\t" + tvsLocations.toString());
			}
		}
		return vec;
	}
	
	public void diff() {
		if( oldValueSets == null ) {
			//do nothing for now, eventually grab all new for changes
			for( ValueSet newVS : newValueSets ) {
				changes.add(new Change("ADD", newVS));
			}
		}
		else {
			
			//compare new to old
			for( ValueSet newVS : newValueSets ) {
				boolean changed = false;
				boolean found = false;
				for( ValueSet oldVS : oldValueSets ) {
					if( newVS.getCode().equals(oldVS.getCode()) ) {
						found = true;
						//check equivalence
						if( !newVS.equals(oldVS) ) {
							changed = true;
							break;
						}
						break;
					}
				}
				if( changed == true ) {
					changes.add(new Change("UPDATE", newVS));
				}
				else if(found == true) {
					//value sets are equal, no change
				}
				else {
					//the value set is new
					changes.add(new Change("ADD", newVS));
				}
			}
			
			//compare old to new
			for( ValueSet oldVS : oldValueSets ) {
				boolean changed = false;
				boolean found = false;
				for(ValueSet newVS : newValueSets ) {
					if( oldVS.getCode().equals(newVS.getCode()) ) {
						found = true;
						//check equivalence
						if( !oldVS.equals(newVS) ) {
							changed = true;
							break;
						}
						break;
					}
				}
				if( changed == true ) {
					//this case should already be taken care of
				}
				else if(found == true) {
					//value sets are equal, no change
				}
				else {
					//the value set has been removed
					changes.add(new Change("REMOVE", oldVS));
				}
			}
			
		}
		
	}
	
	public void printFiles() {
		
		File reportConfigFile = new File("value_set_report_config.txt");
		try {
			PrintWriter pw = new PrintWriter(reportConfigFile);
			for( ValueSetReport report : reports ) {
				for( String loc : report.getFtpLocations() ) {
					pw.println(report.getName() + "|" + report.getURI() + "|" + loc);
					pw.flush();
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File rvsFile = new File("RVS_NCIt_" + version + ".sh");
		try {
			PrintWriter pw = new PrintWriter(rvsFile);
			
			for( ValueSet newVS : newValueSets ) {
				pw.println("./LoadResolvedValueSetDefinition.sh -a -l \"NCI_Thesaurus::" + version + 
							"\" -vsTag \"PRODUCTION\" -u " + "http://evs.nci.nih.gov/valueset/" + newVS.getCode() );
				pw.flush();
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		File removeFile = new File("Remove_Definitions_" + version + ".sh"); 
		File addFile = new File("Load_Definitions_" + version + ".sh");
		try {
			PrintWriter pw = new PrintWriter(removeFile);
			PrintWriter pw2 = new PrintWriter(addFile);
			for( Change c : changes ) {
				if( c.getChangeType().equals("ADD") ) {
					c.getValueSet().printValueSetDefinition();
					pw2.println("./LoadValueSetDefinition.sh -in file:///path/to/file/" + c.getValueSet().getFilename() );
					pw2.flush();
				}
				else if( c.getChangeType().equals("UPDATE") ) {
					c.getValueSet().printValueSetDefinition();
					pw.println("./RemoveValueSetDefinition.sh -u http://evs.nci.nih.gov/valueset/" + c.getValueSet().getCode());
					pw2.println("./LoadValueSetDefinition.sh -in file:///path/to/file/" + c.getValueSet().getFilename() );
					pw.flush();
					pw2.flush();
				}
				else if( c.getChangeType().equals("REMOVE") ) {
					pw.println("./RemoveValueSetDefinition.sh -u http://evs.nci.nih.gov/valueset/" + c.getValueSet().getCode());
					pw.flush();
				}
			}
			pw.close();			
			pw2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
