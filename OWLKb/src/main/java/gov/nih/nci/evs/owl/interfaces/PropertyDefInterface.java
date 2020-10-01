package gov.nih.nci.evs.owl.interfaces;

import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.meta.PropertyDef;

import java.net.URI;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

public interface PropertyDefInterface {

	public int compare(PropertyDef propDef);

	public String getCode();

	public String getName();

	public URI getNamespace();
	
	public URI getRange();

	public PropertyType getPropertyType();

	public Vector<PropertyDef> getParents();

	public boolean hasParent();

	public boolean isEqual(PropertyDef prop);

	public void setCode(String inCode);

	// public void setDomain(String inDomain) ;
	//
	// public void setName(String inName);


	public void setParents(Vector<PropertyDef> parents);

	public void setPropertyType(PropertyType inType);

	IRI getIRI();

	void setIRI(IRI iri);

}
