package gov.nih.nci.evs.owl.metrics;

import org.semanticweb.owlapi.metrics.AxiomCount;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.metrics.GCICount;
import org.semanticweb.owlapi.metrics.HiddenGCICount;
import org.semanticweb.owlapi.metrics.IntegerValuedMetric;
import org.semanticweb.owlapi.metrics.NumberOfClassesWithMultipleInheritance;
import org.semanticweb.owlapi.metrics.ReferencedIndividualCount;
import org.semanticweb.owlapi.metrics.UnsatisfiableClassCountMetric;
import org.semanticweb.owlapi.model.OWLOntology;

public class NCIt_metrics {

	
	//if the subclass is anonymous 
	//then the subclass axiom is known as a General Concept Inclusion - GCI
	private int hiddenGciCount=0;
	private int gciCount;
	private int logicalAxiomCount=0;
	OWLOntology ontology;
	private int axiomCount = 0;
	private int multipleInherit = 0;
	private String dlExpressivity = "";
	private int referencedIndividuals = 0;
	private int unsatisfiableClassCount = 0;
	
	
	public NCIt_metrics(OWLOntology o){
		this.ontology = o;
		axiomCount = new AxiomCount(ontology).getValue();
		hiddenGciCount = new HiddenGCICount(ontology).getValue();
		gciCount = new GCICount(ontology).getValue();
		multipleInherit = new NumberOfClassesWithMultipleInheritance(ontology).getValue();
		dlExpressivity = new DLExpressivity(ontology).recomputeMetric();
		referencedIndividuals = new ReferencedIndividualCount(ontology).recomputeMetric();
	}

	public int getAxiomCount(){
		return axiomCount;
	}
	
	public int getGciCount(){
		return gciCount;
	}

	public int getHiddenGciCount(){
		return hiddenGciCount;
	}
	
	public int getMultipleInheritanceCount(){
		return multipleInherit;
	}
	
	public String getDLexpressivity(){
		return dlExpressivity;
	}

	public int getReferencedIndividualsCount() { return referencedIndividuals;}
}
