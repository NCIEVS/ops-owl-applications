/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl Qualifier.java Apr 22, 2009
 */
package gov.nih.nci.evs.owl.entity;

import java.net.URI;
import java.util.Vector;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.data.OwlApiLayer;
import gov.nih.nci.evs.owl.exceptions.PropertyException;
import gov.nih.nci.evs.owl.interfaces.PropertyInterface;
import gov.nih.nci.evs.owl.meta.PropertyDef;
import gov.nih.nci.evs.owl.meta.QualifierMeta;

/**
 * @author safrant Qualifier is an item that can modify a Property, RoleDef or
 *         Association
 */
public class Qualifier extends Property{

@Override
    public PropertyDef getPropertyDef() {
	    // TODO Auto-generated method stub
	    return super.getPropertyDef();
    }

	@Override
    public String getCode() {
	    // TODO Auto-generated method stub
	    return super.getCode();
    }

	@Override
    public String getName() {
	    // TODO Auto-generated method stub
	    return super.getName();
    }

	@Override
    public URI getNamespace() {
	    // TODO Auto-generated method stub
	    return super.getNamespace();
    }

	@Override
    public PropertyType getPropertyType() {
	    // TODO Auto-generated method stub
	    return super.getPropertyType();
    }

	@Override
    public URI getRange() {
	    // TODO Auto-generated method stub
	    return super.getRange();
    }

	@Override
    public URI getURI() {
	    // TODO Auto-generated method stub
	    return super.getURI();
    }

	@Override
    public String getValue() {
	    // TODO Auto-generated method stub
	    return super.getValue();
    }

	@Override
    public int hashCode() {
	    // TODO Auto-generated method stub
	    return super.hashCode();
    }



	//	private String qValue;
	private QualifierMeta.QualifierType qType;

//	/**
//	 * Create empty qualifier
//	 *
//	 */
//	public Qualifier() {
//
//		this.qValue = "";
//	}
	


	/**
	 * Create qualifier by copying a passed in qualifier by value
	 *
	 * @param qual
	 * @throws QualifierException
	 */
	public Qualifier(Qualifier qual, OWLKb inApi) throws PropertyException {
		super(qual.getPropertyDef(), qual.getValue(), inApi);

		this.setType(qual.qType);
	}

	/**
	 * Create qualifier by passing in a name and value
	 *
	 * @param name
	 * @param value
	 *            Will create default qualifier type of PROPERTY
	 * @throws QualifierException
	 */
	public Qualifier(URI code, String name, String value,OWLKb inAPI) throws PropertyException {
		super(code,name,value,inAPI);

		this.setType(QualifierMeta.QualifierType.PROPERTY);
	}

	/**
	 * Create qualifier by passing in a name and value
	 *
	 * @param name
	 * @param value
	 * @param type
	 * @throws QualifierException
	 */
	public Qualifier(URI code, String name, String value, OWLKb inAPI, QualifierMeta.QualifierType type)
	        throws PropertyException {
		super(code,name,value,inAPI);

		this.setType(type);
	}

	public Qualifier(PropertyDef propD, String value, OWLKb api) {
	    // TODO Auto-generated constructor stub
		super(propD, value, api);
    }

	/**
	 * @param qual
	 * @return boolean - True if input Qualifier matches this, false otherwise
	 * @throws QualifierException
	 */
	public boolean equals(Qualifier qual) throws PropertyException {
		try {
			if ((this.getName().equals(qual.getName()))
			        && (this.getValue().equals(qual.getValue()))
			        && (this.qType.equals(qual.getType()))) return true;
			return false;
		} catch (Exception e) {
			throw new PropertyException(e.getMessage());
		}
	}


	/**
	 * @return type of qualifier (Property, RoleDef or Association)
	 */
	public QualifierMeta.QualifierType getType() {
		return this.qType;
	}



	/**
	 * Less than.
	 *
	 * @param qual
	 *            the qual
	 *
	 * @return true if the name (or value if names equal) of this Qualifier is
	 *         less than the Qualifier input. Used for sorting. Does not include
	 *         type as different qualifier types should not be appearing in the
	 *         same set
	 *
	 * @throws QualifierException
	 *             the qualifier exception
	 */
	public boolean lessThan(Qualifier qual) throws PropertyException {
		try {
			int compareName = this.getName().compareTo(qual.getName());
			int compareValue = this.getValue().compareTo(qual.getValue());
			if (compareName < 0) return true;
			else if (compareName == 0) {
				if (compareValue < 0) return true;
			}

			return false;
		} catch (Exception e) {
			throw new PropertyException(e.getMessage());
		}
	}



	/**
	 * @param type
	 *            (Property, RoleDef or Association)
	 * @throws QualifierException
	 */
	public void setType(QualifierMeta.QualifierType type)
	        throws PropertyException {
		try {
			this.qType = type;
		} catch (Exception e) {
			throw new PropertyException(e.getMessage());
		}
	}



	@Override
	public String toString() {
		String out = "";
		out = this.getName() + ":" + this.getValue();
		return out;
	}




}
