package gov.nih.nci.evs.owl.proxy;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.interfaces.PropertyInterface;

import java.net.URI;
import java.util.Vector;

public class PropertyProxy implements PropertyInterface {
	OWLKb api;
	private String code;
	private PropertyInterface pi;
	private String name;
	private URI uri;

	public PropertyProxy(URI uri, OWLKb api1) {
		this.pi.setURI(uri);
		// this.code = code;
		this.api = api1;
	}

	public PropertyProxy(URI uri, String literal,
            OWLKb owlKb) throws PropertyException {
	    this.pi.setURI(uri);
	    this.pi.setValue(literal);
	    
    }

	public Property getProperty() {
		return (Property) this.pi;
	}


	public int compare(Property propProxy) {
		if (propProxy != null) return this.getProperty().compareTo(
		        propProxy);
		return this.getProperty().compareTo(null);
	}

	@Override
	public String getCode() {

		return this.code;
	}

	@Override
	public String getName() {

		return this.name;
	}



//	@Override
//	public void addQualifier(Qualifier qual) {
//		// TODO Auto-generated method stub
//
//	}

	public int compareTo(Property o) {

		if (o != null) return this.getProperty().compareTo(o);
		return this.getProperty().compareTo(null);
	}

	@Override
	public String getComplexValue() {
		return this.pi.getComplexValue();
	}

	@Override
	public URI getNamespace() {

		return this.pi.getNamespace();
	}

	@Override
	public Qualifier getQualifier(String name) {
		return this.pi.getQualifier(name);
	}

	@Override
	public Vector<Qualifier> getQualifiers() {
		return this.pi.getQualifiers();
	}

	@Override
	public String getValue() {
		return this.pi.getValue();
	}

	@Override
	public boolean isComplexProperty() {
		return this.pi.isComplexProperty();
	}

//	@Override
//	public void removeQualiferByNameValue(String name, String value) {
//		this.pi.removeQualiferByNameValue(name, value);
//	}
//
//	@Override
//	public void removeQualifier(Qualifier qual) {
//		this.pi.removeQualifier(qual);
//	}

//	@Override
//	public void removeQualifierByName(URI qualName) {
//		this.pi.removeQualifierByName(qualName);
//
//	}

	// @Override
	// // public void setCode(String code) {
	// // pi.setCode(code);
	// //
	// // }
	//
	// @Override
	// public void setDomain(URI domain) {
	// pi.setDomain(domain);
	// }


	@Override
	public void setPropertyType(PropertyType propertyType) {
		this.pi.setPropertyType(propertyType);

	}

//	@Override
//	public void setQualifiers(Vector<Qualifier> quals) {
//		this.pi.setQualifiers(quals);
//
//	}

	@Override
	public void setValue(String value) throws PropertyException {
		this.pi.setValue(value);

	}

	@Override
	public void setURI(URI uri) {
		this.pi.setURI(uri);
	}

	@Override
	public URI getURI() {
		return this.pi.getURI();
	}

	@Override
    public URI getRange() {
	    return this.pi.getURI();
    }

}
