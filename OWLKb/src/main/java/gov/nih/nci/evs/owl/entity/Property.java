/**
 * National Cancer Institute Center for Bioinformatics
 *
 * OWLKb gov.nih.nci.evs.owl Property.java Apr 22, 2009
 */
package gov.nih.nci.evs.owl.entity;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.interfaces.PropertyInterface;
import gov.nih.nci.evs.owl.meta.PropertyDef;
import java.net.URI;
import java.util.Collections;
import java.util.Vector;

import org.semanticweb.owlapi.model.IRI;

/**
 * The Class Property.
 *
 * @author safrant
 */
public class Property implements PropertyInterface, Comparable<Property> {
	protected final OWLKb api;

	/** The code. */
	// private String code;

	private String complexValue;

	// /** the domain of the property i.e. rdfs, ncit, go, etc **/
	// private String domain;

	/** The name. */
	// private String name;

	// PropertyType propertyType = PropertyType.GENERAL;

	private PropertyDef propertyDef;

	/** The qualifiers. */
	private Vector<Qualifier> qualifiers = new Vector<Qualifier>();

	/** The value. */
	private String value;
	
	
	
	
	/**
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static int compare(Property o1, Property o2) {
		try {
//			String code1 = o1.toString();
//			String code2 = o2.toString();
//			int out = code1.compareTo(code2);
//			return out;
			
			return o1.compareTo(o2);
		} catch (Exception e) {
			return 0;
		}
	}

	private static int hash(int aSeed, int hashableint) {
		return (11 * aSeed) + hashableint;
	}

	public static int hash(int aSeed, String hashableString) {
		int result = aSeed;
		if (hashableString == null) {
			result = Property.hash(result, 0);
		} else {

			result = Property.hash(result, hashableString.hashCode());
		}
		return result;
	}

//	static boolean isComplex(String propertyValue) {
//		if (propertyValue.startsWith("<ncicp:")) return true;
//		// TODO check further for axioms.
//		return false;
//	}


	// private boolean complexProperty = false;

	public Property(PropertyDef propDef, String propertyValue, OWLKb inApi) {
		this.propertyDef = propDef;
		this.api = inApi;
			this.value = propertyValue;
	}
	

	public Property(PropertyDef propDef, String propertyValue, OWLKb inApi, Vector<Qualifier> quals) {
		this.propertyDef = propDef;
		this.api = inApi;
			this.value = propertyValue;
			this.qualifiers=quals;
			Collections.sort(this.qualifiers);
	}

	public Property(URI propertyCode, String propertyName,
	        String propertyValue,  OWLKb inApi) {
		this(new PropertyDef(IRI.create(propertyCode), propertyName), propertyValue,
		        inApi);

	}

	public Property(URI propertyCode, String propertyName, String propertyValue, OWLKb inApi, Vector<Qualifier> quals){
		this(new PropertyDef(IRI.create(propertyCode), propertyName), propertyValue,
		        inApi, quals);
	}

/*
 * @deprecated  
 * 
 * Cannot work.  Need to have hook into original annotation
 */
@Deprecated
//	public void addQualifier(Qualifier qual) {
//
//		this.qualifiers.add(qual);
////		this.api.addQualifierToProperty(this, qual);
//		Collections.sort(this.qualifiers);
//	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Property that) {
		try {
			String code1 = this.toString();
			String code2 = that.toString();	
//			int out = code1.compareTo(code2);
			int out = this.getCode().compareTo(that.getCode());
			if(out==0){
				out = this.getValue().compareTo(that.getValue());
			}
			if(out==0){
				out = this.getQualifierString().compareTo(that.getQualifierString());
			}
			return out;
		} catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof Property)) return false;
		Property thatProperty = (Property) that;
		return this.equals(thatProperty);
	}

	/**
	 * @param foreignProp
	 * @return true if properties are equal
	 */
	public boolean equals(Property foreignProp) {
		boolean isEqual = false;
		try {
			if (this.getCode().equals(foreignProp.getCode())
			        && this.getValue().equals(foreignProp.getValue())
			        ) {
				// //This is meaningless until we start using actual Qualifiers
				 Vector<Qualifier> myQual = getQualifiers();
				 Vector<Qualifier> foreignQuals = foreignProp.getQualifiers();
				 if ((myQual != null) && (foreignQuals != null)){
//				 isEqual = CollectionUtils.isEqualCollection(myQual,
//				 foreignQuals);
					 for(Qualifier qual:myQual){
						 if(!foreignQuals.contains(qual)){
							 return false;
						 }
					 }
					 for(Qualifier qual:foreignQuals){
						 if(!myQual.contains(qual)){
							 return false;
						 }
					 }
				 }
				isEqual = true;
			}
		} catch (Exception e) {
			// TODO add better error
			e.printStackTrace();
		}

		return isEqual;
	}

	public PropertyDef getPropertyDef() {
		return propertyDef;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return this.propertyDef.getCode();
	}

	public String getComplexValue() {
		return this.complexValue;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.propertyDef.getName();
	}

	public URI getNamespace() {
		return this.propertyDef.getNamespace();
	}

	public PropertyType getPropertyType() {
		return this.propertyDef.getPropertyType();
	}

	public Qualifier getQualifier(String id) {
//		if (this.qualifiers == null) {
//			this.loadQualifiers();
//		}
//		System.out.println("Checking quals for "+ id);
		if (this.qualifiers != null) {
			for (Qualifier qual : this.qualifiers) {
//				System.out.println("Checking qual "+ qual.getCode());
				if (qual.getCode().equals(id)) return qual;
			}
		}
		return null;
	}

	/**
	 * Gets the qualifiers.
	 *
	 * @return the qualifiers
	 */
	public Vector<Qualifier> getQualifiers() {
//		if (this.qualifiers == null) {
//			this.loadQualifiers();
//		}
		return this.qualifiers;
	}
	
	public String getQualifierString(){
		String qualifiers = "";
		for (Qualifier qual: this.qualifiers){
			qualifiers = qual.getName() + ":" + qual.getValue() + " ";
		}
		return qualifiers;
	}

	@Override
    public URI getRange() {
	    return this.propertyDef.getRange();
    }
	


	public URI getURI() {
		return this.propertyDef.getIRI().toURI();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		int result = this.getCode().length();
		result = Property.hash(result, this.getName());
		result = Property.hash(result, this.getNamespace().toString());
		result = Property.hash(result, this.getValue());
		for (Qualifier qual : this.getQualifiers()) {
			result = Property.hash(result, qual.hashCode());
		}
		return result;

	}

	public boolean isComplexProperty() {
		if (this.qualifiers.size() > 0) return true;
		return false;
	}

//	private boolean isEqualProperty(Property that) {
//		return (this.getCode() == null ? that.getCode() == null : this
//		        .getCode().equals(that.getCode())
//		        && (this.getNamespace() == null ? that.getNamespace() == null : this
//		                .getNamespace().equals(that.getNamespace()))
//		        && (this.getName().equals(that.getName()))
//		        && (this.getPropertyType().toString().equals(that
//		                .getPropertyType().toString()))
//		        && (this.getValue().equals(that.getValue())) && (// TODO check
//		                                                         // if
//		                                                         // qualifiers
//		                                                         // are equal
//		        this.getQualifiers().size() == that.getQualifiers().size())
//
//				);
//	}

	/**
	 * Load qualifiers. This is only needed while we have our funky qualifiers.
	 * Can be ditched once we go OWL2
	 *
	 */
//	private void loadComplexQualifiers() {
//		// TODO Go to OWLAPI and get the Qualifiers for the given Concept
//		// Trim off <ncicp:ComplexTerm> first, then pass to subprocess?
//		String trimmedString = this.value
//		        .substring(this.value.indexOf("><") + 1);
//		trimmedString = trimmedString.substring(0,
//		        trimmedString.lastIndexOf("><"));
//		String[] qualArray = trimmedString.split("><ncicp:");
//
//		//
//		//
//		if (this.getName().equals("FULL_SYN")) {
//			this.loadFSQuals(qualArray);
//		} else if (this.getName().contains("DEFINITION")) {
//			this.loadDefQuals(qualArray);
//		} else if (this.getName().contains("GO")) {
//			this.loadGOQuals(qualArray);
//		} else if (this.getName().contains("Maps_To")) {
//			this.loadMapsToQuals(qualArray);
//		}
//
//		//
//
//		// Property
//		// String tempValue = value;
//		// tempValue = tempValue.replace("&", "&amp;");
//		// // Vector<Qualifier> temp_qualifiers = new Vector<Qualifier>();
//		// try {
//		// Element node =
//		// DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new
//		// ByteArrayInputStream(tempValue.getBytes())).getDocumentElement();
//		// NodeList cpElements = node.getChildNodes();
//		// for(int i=0;i<cpElements.getLength();i++){
//		// Node childNode = cpElements.item(i);
//		// String qualName = childNode.getNodeName().replace("ncicp:", "");
//		// String nodeValue = childNode.getNodeValue();
//		// Node firstChild = childNode.getFirstChild();
//		// String qualValue = firstChild.getNodeValue();
//		// if(qualName.compareTo("def-definition")==0 ||
//		// qualName.compareTo("term-name")==0 ||
//		// qualName.compareTo("go-term")==0){
//		// this.value = qualValue;
//		// } else {
//		// Qualifier qual = new Qualifier(qualName, qualValue);
//		// qualifiers.add(qual);}
//		// }
//		// } catch (SAXException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (IOException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (ParserConfigurationException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (QualifierException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//	}

//	private void loadDefQuals(String[] qualArray) {
//		try {
//			this.setPropertyType(PropertyType.DEFINITION);
//			for (String qual : qualArray) {
//				String qualName = qual.substring(0, qual.indexOf(">"));
//				qualName = qualName.replace("ncicp:", "");
//				qualName = qualName.replace("<", "");
//				String qualValue = qual.substring(qual.indexOf(">") + 1,
//				        qual.lastIndexOf("<"));
//				if (qualName.equals("def-definition")) {
//					this.value = qualValue;
//				} else {
//					Qualifier qualifier;
//
//					qualifier = new Qualifier(qualName, qualValue);
//
//					this.qualifiers.add(qualifier);
//				}
//			}
//		} catch (PropertyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void loadFSQuals(String[] qualArray) {
//		try {
//			this.setPropertyType(PropertyType.SYNONYM);
//			for (String qual : qualArray) {
//				String qualName = qual.substring(0, qual.indexOf(">"));
//				qualName = qualName.replace("ncicp:", "");
//				qualName = qualName.replace("<", "");
//				String qualValue = qual.substring(qual.indexOf(">") + 1,
//				        qual.lastIndexOf("<"));
//				if (qualName.equals("term-name")) {
//					this.value = qualValue;
//				} else {
//					Qualifier qualifier;
//
//					qualifier = new Qualifier(qualName, qualValue);
//
//					this.qualifiers.add(qualifier);
//				}
//			}
//		} catch (PropertyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private void loadGOQuals(String[] qualArray) {
//		try {
//			for (String qual : qualArray) {
//				String qualName = qual.substring(0, qual.indexOf(">"));
//				qualName = qualName.replace("ncicp:", "");
//				qualName = qualName.replace("<", "");
//				String qualValue = qual.substring(qual.indexOf(">") + 1,
//				        qual.lastIndexOf("<"));
//
//				if (qualName.equals("go-term")) {
//					this.value = qualValue;
//				} else {
//					Qualifier qualifier;
//
//					qualifier = new Qualifier(qualName, qualValue);
//
//					this.qualifiers.add(qualifier);
//				}
//			}
//		} catch (PropertyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void loadMapsToQuals(String[] qualArray) {
//		try {
//			for (String qual : qualArray) {
//				String qualName = qual.substring(0, qual.indexOf(">"));
//				qualName = qualName.replace("ncicp:", "");
//				qualName = qualName.replace("<", "");
//				String qualValue = qual.substring(qual.indexOf(">") + 1,
//				        qual.lastIndexOf("<"));
//
//				if (qualName.equals("Target_Term")) {
//					this.value = qualValue;
//				} else {
//					Qualifier qualifier;
//
//					qualifier = new Qualifier(qualName, qualValue);
//
//					this.qualifiers.add(qualifier);
//				}
//			}
//		} catch (PropertyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//

//	
//
//@Deprecated
//	public void removeQualiferByNameValue(String name, String value) {
//		Vector<Qualifier> removeQuals = new Vector<Qualifier>();
//		for (Qualifier qual : this.qualifiers) {
//			if (qual.getName().equals(name) && qual.getValue().equals(value)) {
//				removeQuals.add(qual);
//			}
//		}
//
//		for (Qualifier remove : removeQuals) {
//			this.removeQualifier(remove);
//		}
//	}
//
//@Deprecated
//	public void removeQualifier(Qualifier qual) {
//		int i = this.qualifiers.indexOf(qual);
//		this.qualifiers.remove(i);
//
//	}
//	
//@Deprecated
//	public void removeQualifierByName(String qualName) {
//		removeQualifierByName(URI.create(qualName));
//	}
//
//@Deprecated
//    public void removeQualifierByName(URI qualID) {
//		Vector<Qualifier> removeQuals = new Vector<Qualifier>();
//		for (Qualifier qual : this.qualifiers) {
//			if (qual.getName().equals(qualID)) {
//				removeQuals.add(qual);
//			}
//		}
//
//		for (Qualifier remove : removeQuals) {
//			this.removeQualifier(remove);
//		}
//	    
//    }

	// public void setDomain(String domain) {
	// this.propertyDef.setDomain(domain);
	// ;
	// }
	//
	// public void setName(String name) {
	// this.propertyDef.setName(name);
	// ;
	// }

	public void setCode(String code) {
		this.propertyDef.setCode(code);
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyDef.setPropertyType(propertyType);
	}



	public void setURI(URI uri) {
		this.propertyDef.setIRI(IRI.create(uri));
	}

	public void setValue(String value) {

		this.value = value;
	}

	@Override
	public String toString() {
		String out = "";
		out = this.getCode() + " " + this.getName() + " " + this.getValue();
		// Once we actually have qualifiers, add this back in
		 for (Qualifier qual : getQualifiers()){
		 out = out + "  " + qual.toString();
		 }
		return out;
	}


	public void setQualifiers(Vector<Qualifier> quals) {
		// TODO Auto-generated method stub
		//TODO Does not save to main KB
		this.qualifiers = quals;
	}
}
