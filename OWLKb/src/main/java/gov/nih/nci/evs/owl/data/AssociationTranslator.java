package gov.nih.nci.evs.owl.data;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.io.RDFNode;
import org.semanticweb.owlapi.io.RDFResource;
import org.semanticweb.owlapi.io.RDFResourceIRI;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.model.RDFGraph;
import org.semanticweb.owlapi.rdf.model.RDFTranslator;
import org.semanticweb.owlapi.util.IndividualAppearance;

public class AssociationTranslator extends RDFTranslator {

	@Nonnull private AssociationGraph assocGraph = new AssociationGraph();
	
	public AssociationTranslator(OWLOntologyManager manager,
            OWLOntology ontology, boolean useStrongTyping,
            IndividualAppearance occurrences) {
//	    super(manager, ontology, useStrongTyping, occurrences);
//        super(manager, ontology, useStrongTyping, occurrences,null,null,null);
        super(manager, ontology, useStrongTyping, occurrences);
    }
	
	
    public void addTriple(OWLAxiom ax, RDFResource subject, RDFResourceIRI pred, RDFNode object) {
        System.out.println(subject + " -> " + pred + " ->  " + object);
    }
    
    @Override
    protected void addTriple(@Nonnull RDFResource subject, @Nonnull RDFResourceIRI pred, @Nonnull RDFNode object) {
    	assocGraph.addTriple(new RDFTriple(checkNotNull(subject, "subject cannot be null"), checkNotNull(pred,
            "pred cannot be null"), checkNotNull(object, "object cannot be null")));
//        System.out.println(subject + " -> " + pred + " ->  " + object);
    }

    public AssociationGraph getAssocGraph() {
        return assocGraph;
    }

}
