package gov.nih.nci.evs.owl;

import org.semanticweb.owlapi.model.IRI;

enum PROPERTY_ALIAS {
	CODE(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#NHC0")),
	PREFERRED_NAME(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P108")),
	DEFINITION(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#97")),
	FULL_SYN(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90")),
	DISPLAY_NAME(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P107")),
	SEMANTIC_TYPE(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P106")),
	CONCEPT_STATUS(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P310"));
	
	private IRI iri;
	
	PROPERTY_ALIAS(IRI iri){
		this.iri = iri;
	}
	
	public IRI iri() {
		return iri;
	}
	
}
