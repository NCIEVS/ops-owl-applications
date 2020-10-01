package gov.nih.nci.evs.owl.meta;

import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.interfaces.PropertyDefInterface;

import java.net.URI;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

public class PropertyDef implements PropertyDefInterface {

	// OWLKb api;
	String code;
	URI domain;
	URI range;
	String name;
//	URI uri;
	IRI iri;
	PropertyType type;

	Vector<PropertyDef> parents;
	Vector<String> pickList = null;

	// public PropertyDef(String propDomain,String propCode) throws
	// PropertyException {
	// this.code = propCode;
	// this.type = PropertyType.GENERAL;
	// // this.api = api1;
	// // this.domain = api1.getDefaultNamespace();
	// // try {
	//
	// // parents = api1.getParentsForProperty(propCode);
	// // String name = api1.getPropertyNameByCode(propCode);
	// // this.setName(name);
	// // TODO check if there is a picklist, then load it
	//
	// // } catch (Exception e) {
	// // System.out
	// // .println("debug - property threw error during instantiation "
	// // + propCode);
	// // }
	// }

	// public PropertyDef(String propDomain,String propCode, PropertyType
	// propType) {
	// this.code = propCode;
	// this.type = propType;
	// }

//	public PropertyDef(IRI iri) throws PropertyException {
//		this(iri, "", PropertyType.GENERAL);
//
////		 try {
////
////		 parents = api1.getParentsForProperty(propCode);
////		 String name = api1.getPropertyNameByCode(propCode);
////		 this.setName(name);
////		// TODO check if there is a picklist, then load it
////
////		 } catch (Exception e) {
////		 System.out
////		 .println("debug - property threw error during instantiation "
////		+ propCode);
////		 }
//	}
//
//	public PropertyDef(IRI propURI, PropertyType propType)
//	        throws PropertyException {
//		this(propURI, "", propType);
//
//	}

	public PropertyDef(IRI propURI, String propName) {
		this(propURI, propName, PropertyType.GENERAL);

	}

	public PropertyDef(IRI iri, String propName, PropertyType propType) {
		this(iri, propName, new Vector<PropertyDef>(), propType);
		// Vector<PropertyDef> parents = new Vector<PropertyDef>();
		//
		// this.domain = propURI.getPath();
		// this.code = propURI.getFragment();
		// this.name = propName;
		// this.type = propType;
		// this.uri = propURI;
	}

	public PropertyDef(IRI propURI, String propName,
	        Vector<PropertyDef> propParents) {
		this(propURI, propName, propParents, PropertyType.GENERAL);

	}

	public PropertyDef(IRI propURI, String propName,
	        Vector<PropertyDef> propParents, PropertyType propType) {
		this.name = propName;
		this.parents = propParents;
		this.type = propType;
		this.iri = propURI;

		if (propURI.toString().contains("#")) {
			this.domain = URI.create(propURI.getNamespace());
			this.code = propURI.getFragment();
		} else {
			String propUS = propURI.toString();
			int lastSlash = propUS.lastIndexOf("/");
			this.domain = URI.create(propUS.substring(0, lastSlash));
			this.code = propUS.substring(lastSlash + 1);
		}
	}

	@Override
	public int compare(PropertyDef propDef) {
		return this.getCode().compareTo(propDef.getCode());
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public URI getNamespace() {
		return this.domain;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IRI getIRI() {
		return this.iri;
	}
	
	@Override
	public URI getRange(){
		return this.range;
	}

	@Override
	public Vector<PropertyDef> getParents() {
		return this.parents;
	}

	@Override
	public boolean hasParent() {
		if (this.parents.size() > 0) return true;
		return false;
	}

	@Override
	public boolean isEqual(PropertyDef propDef) {
		if (this.code.equals(propDef.code)
		        && this.domain.equals(propDef.getNamespace())) return true;
		return false;
	}

	@Override
	public void setCode(String inCode) {
		this.code = inCode;
	}

	// public void setDomain(String inDomain) {
	// this.domain = inDomain;
	// }
	//
	// public void setName(String inName) {
	// this.name = inName;
	// }

	@Override
	public void setParents(Vector<PropertyDef> parents) {
		this.parents = parents;

	}

	@Override
	public void setIRI(IRI iri) {
		this.iri = iri;
	}

	@Override
	public PropertyType getPropertyType() {
		return this.type;
	}

	@Override
	public void setPropertyType(PropertyType inType) {
		this.type = inType;

	}

}
