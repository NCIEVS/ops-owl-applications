package gov.nih.nci.evs.report.data;

import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Concept;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;
import gov.nih.nci.evs.report.ReportWriterConcept;
import gov.nih.nci.evs.report.ReportWriterConfiguration;
import gov.nih.nci.evs.report.ReportWriterOwlConcept;

public class RWOwlReader {

	private ReportWriterConfiguration config;
	String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	OWLKb owlkb;
	
	public RWOwlReader(ReportWriterConfiguration config) {
		this.config = config;
//		read(config.getOwlfile());
		read(config.getHttpURL());
	}
	//use OWLKb to read in data and create ReportWriterConcept

	private void read(String owlfile) {

		IRI iri =  IRI.create(owlfile);
		
		owlkb = new OWLKb(iri, namespace);
		
	}

	public Vector<ReportWriterConcept> searchReportWriterConcepts() {
		Vector<ReportWriterConcept> ret_RWConcept_array = new Vector<ReportWriterConcept>();
		String searchCode = config.getSubset();
		Vector<String> descendants = owlkb.getAllDescendantsForConcept(searchCode);
		for(String descendant:descendants){
			ConceptProxy concept = owlkb.getConcept(descendant);
			ReportWriterOwlConcept rwConcept = new ReportWriterOwlConcept(concept, config.getSource());
			if (config.getPrintParent()){
				Vector<String> parentCodes = concept.getParentCodes();
				Vector<ReportWriterConcept> parents = getParents(parentCodes);
				rwConcept.setParents(parents);
			}
			if (config.getPrintChildren()){
				Vector<String> childCodes = concept.getChildren();
				Vector<ReportWriterConcept> children = getSubConcepts(childCodes);
				rwConcept.setChildren(children);
			}ret_RWConcept_array.add(rwConcept);}return ret_RWConcept_array;
		}

	private Vector<ReportWriterConcept> getSubConcepts(Vector<String> childCodes) {
	
		Vector<ReportWriterConcept> children = new Vector<ReportWriterConcept>();
		for(String childCode:childCodes){
			ConceptProxy concept = owlkb.getConcept(childCode);
			if(!concept.getCode().contains("owl:")){		
			ReportWriterConcept rwConcept = new ReportWriterOwlConcept(concept,"NCI");
			children.add(rwConcept);
			}
		}
		return children;
	}
	private Vector<ReportWriterConcept> getParents(Vector<String> parentCodes) {

		Vector<ReportWriterConcept> parents = new Vector<ReportWriterConcept>();
		for(String parentCode:parentCodes){
			ConceptProxy concept = owlkb.getConcept(parentCode);
			if(!concept.getCode().contains("owl:")){	
			ReportWriterConcept rwConcept = new ReportWriterOwlConcept(concept,"NCI");
			parents.add(rwConcept);
			}
		}
		return parents;
	}


	
	

}
