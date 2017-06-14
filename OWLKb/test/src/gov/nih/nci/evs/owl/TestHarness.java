/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl TestHarness.java May 7, 2009
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Association;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Relationship;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.meta.RoleDef;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author safrant
 *
 */
public class TestHarness {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the args
	 */
	public static void main(final String[] args) {
		final TestHarness tester = new TestHarness();
		tester.configure();
		tester.testAssociation();
		tester.testGetProperties();
		tester.testGetRolesAssocsForTarget();
		tester.testGetRolesForSource();
		tester.testIsDeprecated();
		tester.testRoleDomainAndRange();
	}

	// String url = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	String url = "file:///Users/safrant/EVS/data/OWL2/Thesaurus-170424-17.04d.owl";

	private final String ontologyNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	
	OWLKb owlKb1;

	// private final String ontologyNamespace =
	// "http://purl.bioontology.org/ontology/npo/";

	@SuppressWarnings({ "unused", "deprecation" })
	private void configure() {
		owlKb1 = new OWLKb(this.url, this.ontologyNamespace);

		HashMap<URI, String> props = owlKb1.getAllProperties();
		owlKb1.getAllAssociations();
		owlKb1.getAllRoles();
		
	}
	

	private void testAssociation(){
        URI testURI = null;
        URI testURI2 = null;
        try {
               testURI = new URI("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C100000");
               testURI2 = new URI("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C101859");
        } catch (URISyntaxException e2) {
                       // TODO Auto-generated catch block
                       e2.printStackTrace();
        }
        Vector<Association> assocs1 = owlKb1.getAssociationsForSource(testURI);
        if( assocs1.size() > 0 ) {
                       for( Association assoc1 : assocs1 ) {
                                      System.out.println(assoc1.getSourceCode() + " " + assoc1.getName() + " " + assoc1.getRelAndTarget() );
                       }
        }
        else {
                       System.out.println("No outgoing associations for " + testURI.getFragment());
        }
        
        assocs1 = owlKb1.getAssociationsForTarget(testURI);
        if( assocs1.size() > 0 ) {
                       for( Association assoc1 : assocs1 ) {
                                      System.out.println(assoc1.getSourceCode() + " " + assoc1.getName() + " " + assoc1.getRelAndTarget() );
                       }
        }
        else {
                       System.out.println("No outgoing associations for " + testURI.getFragment());
        }
        
        
        assocs1 = owlKb1.getAssociationsForSource(testURI2);
        if( assocs1.size() > 0 ) {
                       for( Association assoc1 : assocs1 ) {
                                      System.out.println(assoc1.getSourceCode() + " " + assoc1.getName() + " " + assoc1.getRelAndTarget() );
                       }
        }
        else {
                       System.out.println("No outgoing associations for " + testURI2.getFragment());
        }
        
        assocs1 = owlKb1.getAssociationsForTarget(testURI2);
        if( assocs1.size() > 0 ) {
                       for( Association assoc1 : assocs1 ) {
                                      System.out.println(assoc1.getSourceCode() + " " + assoc1.getName() + " " + assoc1.getRelAndTarget() );
                       }
        }
        else {
                       System.out.println("No outgoing associations for " + testURI2.getFragment());
        }
		
		
		
	}
	
	private void testGetRolesForSource(){
		owlKb1.getAllRoles();

		ConceptProxy concept0 = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49329");
		Vector<String> parentCodes = owlKb1
		        .getParentsForConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49329");
		Vector<Role> relTest1 = owlKb1.getRolesForSource(concept0);
		Vector<Role> relTest2 = owlKb1.getRolesForTarget(concept0);

		ConceptProxy concept = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C4872");
		Vector<Role> eqa = owlKb1.getEquivalentClassRoles(concept);
		parentCodes = owlKb1.getParentsForConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C4872");
		relTest1 = owlKb1.getRolesForSource(concept);
		relTest2 = owlKb1.getRolesForTarget(concept);

		// ConceptProxy concept = owlKb1.getConcept("C4872");
		// owlKb1.getParentsForConcept("C4872");
		// ConceptProxy concept = owlKb1.getConcept("NPO_686");
		// owlKb1.getParentsForConcept("NPO_686");
		Vector<Property> names = concept.getProperties();
		for (Property prop : names) {
			prop.getValue();
		}
		Vector<Role> r = owlKb1.getRolesForSource(concept);
		for (Role rel : r) {
			rel.getSource();
			RoleDef roleDef = (RoleDef) rel.getRelation();
			roleDef.isTransitive();
			// roleDef.isHierarchical();
			roleDef.getRange();
			roleDef.getDomain();
		}
	}
	
	private void testGetProperties(){

		ConceptProxy concept = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49424");
		Vector<Property> names = concept.getProperties();
		for (Property prop : names) {
			prop.getValue();
		}

		concept = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C49329");
		names = concept.getProperties();
		for (Property prop : names) {
			prop.getValue();
		}

		// concept = owlKb1.getConcept("CDISC_Questionnaire_Terminology");
		concept = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C106812");


	}
	
	private void testRoleDomainAndRange(){
		ConceptProxy concept2 = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28421");
		// ConceptProxy concept2 = owlKb1.getConcept("C38389");
		// // C38389
		// ConceptProxy concept2 = owlKb1.getConcept("NPO_1732");
		Vector<Property> names2 = concept2.getProperties();
		for (Property prop : names2) {
			prop.getValue();
		}
		Vector<Role> r2 = owlKb1.getRolesForSource(concept2);
		if (r2 != null) {
			for (Relationship rel : r2) {
				rel.getSource();
				RoleDef roleDef = (RoleDef) rel.getRelation();
				roleDef.isTransitive();
				// roleDef.isHierarchical();
				roleDef.getRange();
				roleDef.getDomain();
			}
		}

		r2 = owlKb1.getRolesForTarget(concept2);
		if (r2 != null) {
			for (Relationship rel : r2) {
				rel.getSource();
				RoleDef roleDef = (RoleDef) rel.getRelation();
				roleDef.isTransitive();
				// roleDef.isHierarchical();
				roleDef.getRange();
				roleDef.getDomain();
			}
		}
		
		concept2 = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C18369");
		names2 = concept2.getProperties();
		for (Property prop : names2) {
			prop.getValue();
		}
		r2 = owlKb1.getRolesForSource(concept2);
		if (r2 != null) {
			for (Relationship rel : r2) {
				rel.getSource();
				RoleDef roleDef = (RoleDef) rel.getRelation();
				roleDef.isTransitive();
				// roleDef.isHierarchical();
				roleDef.getRange();
				roleDef.getDomain();
			}
		}

		r2 = owlKb1.getRolesForTarget(concept2);
		if (r2 != null) {
			for (Relationship rel : r2) {
				rel.getSource();
				RoleDef roleDef = (RoleDef) rel.getRelation();
				roleDef.isTransitive();
				// roleDef.isHierarchical();
				roleDef.getRange();
				roleDef.getDomain();
			}
		}
	}
	
	private void testIsDeprecated(){

		try {
			ConceptProxy concept2 = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C38389");
			@SuppressWarnings("unused")
			boolean isDeprecated = owlKb1.isDeprecated(concept2.getCode());
			concept2 = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28421");
			isDeprecated = owlKb1.isDeprecated(concept2.getCode());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testGetRolesAssocsForTarget(){
		Vector<Association> assocTest = owlKb1
		        .getAssociationsForSource("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C106667");
		ConceptProxy concept = owlKb1
		        .getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C106667");
		Vector<Role> relTest = owlKb1.getRolesForSource(concept);
		relTest = owlKb1.getRolesForTarget(concept);
		assocTest = owlKb1.getAssociationsForTarget(concept);

		// concept = owlKb1.getConcept("CDISC_Questionnaire_Terminology");
		concept = owlKb1.getConcept("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C106812");

		Vector<Role> r1 = owlKb1.getRolesForTarget(concept);


	}

}
