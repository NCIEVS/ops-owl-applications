package gov.nih.nci.evs.owl.interfaces;

import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.exceptions.PropertyException;

import java.net.URI;
import java.util.Vector;

public interface PropertyInterface {


	public String getCode();

	public String getName();

	public String getComplexValue();
	
	public URI getRange();

	public Qualifier getQualifier(String name);

	public Vector<Qualifier> getQualifiers();

	public String getValue();

	public boolean isComplexProperty();


	// public void setCode(String code);

	// public void setDomain(String domain);

	public void setURI(URI uri);

	public void setPropertyType(PropertyType propertyType);



	public void setValue(String value) throws PropertyException;

	@Override
	public String toString();

	public URI getURI();

	URI getNamespace();

}
