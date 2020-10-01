package gov.nih.nci.evs.owl.visitor;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
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

/**
 * https://github.com/owlcollab/owltools/blob/master/OWLTools-Core/src/main/java
 * /owltools/graph/AxiomAnnotationTools.java#L329
 *
 *
 * Visitor which returns a new axiom of the same type with the new annotations.
 *
 */
public class AxiomAnnotationsChanger implements OWLAxiomVisitorEx<OWLAxiom> {

	private final Set<OWLAnnotation> annotations;
	private final OWLDataFactory factory;

	public AxiomAnnotationsChanger(Set<OWLAnnotation> annotations,
			OWLDataFactory factory) {
		this.annotations = annotations;
		this.factory = factory;
	}

	@Override
	public OWLAxiom visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		return this.factory.getOWLSubAnnotationPropertyOfAxiom(
				axiom.getSubProperty(), axiom.getSuperProperty(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLAnnotationPropertyDomainAxiom axiom) {
		return this.factory.getOWLAnnotationPropertyDomainAxiom(
				axiom.getProperty(), axiom.getDomain(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLAnnotationPropertyRangeAxiom axiom) {
		return this.factory.getOWLAnnotationPropertyRangeAxiom(
				axiom.getProperty(), axiom.getRange(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
		return this.factory.getOWLSubClassOfAxiom(axiom.getSubClass(),
				axiom.getSuperClass(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		return this.factory.getOWLNegativeObjectPropertyAssertionAxiom(
				axiom.getProperty(), axiom.getSubject(), axiom.getObject(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		return this.factory.getOWLAsymmetricObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
		return this.factory.getOWLReflexiveObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {
		return this.factory.getOWLDisjointClassesAxiom(
				axiom.getClassExpressions(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyDomainAxiom axiom) {
		return this.factory.getOWLDataPropertyDomainAxiom(axiom.getProperty(),
				axiom.getDomain(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
		return this.factory.getOWLObjectPropertyDomainAxiom(
				axiom.getProperty(), axiom.getDomain(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		return this.factory.getOWLEquivalentObjectPropertiesAxiom(
				axiom.getProperties(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		return this.factory.getOWLNegativeDataPropertyAssertionAxiom(
				axiom.getProperty(), axiom.getSubject(), axiom.getObject(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDifferentIndividualsAxiom axiom) {
		return this.factory.getOWLDifferentIndividualsAxiom(
				axiom.getIndividuals(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDisjointDataPropertiesAxiom axiom) {
		return this.factory.getOWLDisjointDataPropertiesAxiom(
				axiom.getProperties(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
		return this.factory.getOWLDisjointObjectPropertiesAxiom(
				axiom.getProperties(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
		return this.factory.getOWLObjectPropertyRangeAxiom(axiom.getProperty(),
				axiom.getRange(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {
		return this.factory.getOWLObjectPropertyAssertionAxiom(
				axiom.getProperty(), axiom.getSubject(), axiom.getObject(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
		return this.factory.getOWLFunctionalObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSubObjectPropertyOfAxiom axiom) {
		return this.factory.getOWLSubObjectPropertyOfAxiom(
				axiom.getSubProperty(), axiom.getSuperProperty(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {
		return this.factory.getOWLDisjointUnionAxiom(axiom.getOWLClass(),
				axiom.getClassExpressions(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDeclarationAxiom axiom) {
		return this.factory.getOWLDeclarationAxiom(axiom.getEntity(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLAnnotationAssertionAxiom axiom) {
		return this.factory.getOWLAnnotationAssertionAxiom(axiom.getProperty(),
				axiom.getSubject(), axiom.getValue(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
		return this.factory.getOWLSymmetricObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyRangeAxiom axiom) {
		return this.factory.getOWLDataPropertyRangeAxiom(axiom.getProperty(),
				axiom.getRange(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLFunctionalDataPropertyAxiom axiom) {
		return this.factory.getOWLFunctionalDataPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLEquivalentDataPropertiesAxiom axiom) {
		return this.factory.getOWLEquivalentDataPropertiesAxiom(
				axiom.getProperties(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
		return this.factory.getOWLClassAssertionAxiom(
				axiom.getClassExpression(), axiom.getIndividual(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
		return this.factory.getOWLEquivalentClassesAxiom(
				axiom.getClassExpressions(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDataPropertyAssertionAxiom axiom) {
		return this.factory.getOWLDataPropertyAssertionAxiom(
				axiom.getProperty(), axiom.getSubject(), axiom.getObject(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
		return this.factory.getOWLTransitiveObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		return this.factory.getOWLIrreflexiveObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSubDataPropertyOfAxiom axiom) {
		return this.factory.getOWLSubDataPropertyOfAxiom(
				axiom.getSubProperty(), axiom.getSuperProperty(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		return this.factory.getOWLInverseFunctionalObjectPropertyAxiom(
				axiom.getProperty(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSameIndividualAxiom axiom) {
		return this.factory.getOWLSameIndividualAxiom(axiom.getIndividuals(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLSubPropertyChainOfAxiom axiom) {
		return this.factory.getOWLSubPropertyChainOfAxiom(
				axiom.getPropertyChain(), axiom.getSuperProperty(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLInverseObjectPropertiesAxiom axiom) {
		return this.factory.getOWLInverseObjectPropertiesAxiom(
				axiom.getFirstProperty(), axiom.getSecondProperty(),
				this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLHasKeyAxiom axiom) {
		return this.factory.getOWLHasKeyAxiom(axiom.getClassExpression(),
				axiom.getDataPropertyExpressions(), this.annotations);
	}

	@Override
	public OWLAxiom visit(OWLDatatypeDefinitionAxiom axiom) {
		return this.factory.getOWLDatatypeDefinitionAxiom(axiom.getDatatype(),
				axiom.getDataRange(), this.annotations);
	}

	@Override
	public OWLAxiom visit(SWRLRule rule) {
		return this.factory.getSWRLRule(rule.getBody(), rule.getHead(),
				this.annotations);
	}

}