package gov.nih.nci.evs.owl.visitor;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class AssociationVisitor implements OWLAnnotationObjectVisitor,
        OWLAxiomVisitor {

	boolean isAssociation = false;
	IRI assocCode;
	IRI targetCode;

	public boolean isAssociation() {
		return this.isAssociation;
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		// TODO Auto-generated method stub
//		System.out.println("visit OWLAnnotationAssertionAxiom");
		this.assocCode = axiom.getProperty().getIRI();
		OWLAnnotationValue target = axiom.getValue();
		AssociationValueVisitor visitor = new AssociationValueVisitor();
		target.accept(visitor);
		this.targetCode = visitor.getIRI();
		this.isAssociation = true;
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(IRI iri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLAnonymousIndividual individual) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLLiteral literal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLAnnotation node) {
		// System.out.println("Visit OWLAnnotation");
		this.assocCode = node.getProperty().getIRI();
		OWLAnnotationValue target = node.getValue();
		AssociationValueVisitor visitor = new AssociationValueVisitor();
		target.accept(visitor);
		this.targetCode = visitor.getIRI();
		this.isAssociation = true;
	}

	public IRI getAssociationCode() {
		return this.assocCode;
	}

	public IRI getTargetCode() {
		return this.targetCode;
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(SWRLRule rule) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		// TODO Auto-generated method stub

	}

}
