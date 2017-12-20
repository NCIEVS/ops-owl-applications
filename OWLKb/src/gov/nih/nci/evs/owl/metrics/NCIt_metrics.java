package gov.nih.nci.evs.owl.metrics;

import org.semanticweb.owlapi.metrics.AxiomCount;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.metrics.HiddenGCICount;
import org.semanticweb.owlapi.metrics.IntegerValuedMetric;
import org.semanticweb.owlapi.metrics.NumberOfClassesWithMultipleInheritance;
import org.semanticweb.owlapi.model.OWLOntology;

public class NCIt_metrics {

	
	//if the subclass is anonymous 
	//then the subclass axiom is known as a General Concept Inclusion - GCI
	private int gciCount=0;
	private int logicalAxiomCount=0;
	OWLOntology ontology;
	private int axiomCount = 0;
	private int multipleInherit = 0;
	private String dlExpressivity = "";
	
	
	public NCIt_metrics(OWLOntology o){
		this.ontology = o;
		axiomCount = new AxiomCount(ontology).getValue();
		gciCount = new HiddenGCICount(ontology).getValue();
		multipleInherit = new NumberOfClassesWithMultipleInheritance(ontology).getValue();
		dlExpressivity = new DLExpressivity(ontology).recomputeMetric();
	}

	public int getAxiomCount(){
		return axiomCount;
	}
	
	public int getGciCount(){
		return gciCount;
	}
	
	public int getMultipleInheritanceCount(){
		return multipleInherit;
	}
	
	public String getDLexpressivity(){
		return dlExpressivity;
	}
	
}
