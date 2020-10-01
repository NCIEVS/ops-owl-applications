package gov.nih.nci.evs.owl.visitor;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitor;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

public class AssociationValueVisitor implements OWLAnnotationValueVisitor {

	String code = "";
	IRI iri;

	@Override
	public void visit(IRI inIri) {

		this.code = inIri.getFragment();
		this.iri = inIri;
	}

	@Override
	public void visit(OWLAnonymousIndividual individual) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLLiteral literal) {
		// TODO Auto-generated method stub

	}

	public String getCode() {
		return this.code;
	}

	public IRI getIRI() {
		return this.iri;
	}

}
