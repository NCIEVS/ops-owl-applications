package gov.nih.nci.evs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValueSet {
	private String className;
	private String code;
	private int noCCode;	
	private String name;
	private String fileName;
	private String definition;
	private Vector<String> sources = new Vector<String>();
	private Vector<String> locations = new Vector<String>();
	
	public ValueSet(String className, String code, String name, String definition, Vector<String> sources, Vector<String> locations) {
		this.className = className;
		this.code = code;
		this.noCCode = Integer.parseInt(code.replace("C", ""));		
		this.name = name;
		this.fileName = className + ".xml";
		this.definition = definition;
		this.sources = sources;
		this.locations = locations;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	public Vector<String> getSources() {
		return sources;
	}
	
	public Vector<String> getLocations() {
		return locations;
	}
	
	public int getNoCCode() {
		return noCCode;
	}
	
	public String getFilename() {
		return fileName;
	}
	
	public boolean equals(ValueSet vs) {
		boolean equal = true;
		try{
		if( !this.code.equals(vs.getCode()) || !this.name.equals(vs.getName()) || !this.definition.equals(vs.getDefinition()) ) {
			equal = false;
		}}
		catch(java.lang.NullPointerException x){
			return false;
		}
	
		for( String source : this.sources ) {
			if( !vs.getSources().contains(source) ) {
				equal = false;
				break; //seen enough
			}
		}
		
		for( String source : vs.getSources() ) {
			if( !this.sources.contains(source) ) {
				equal = false;
				break;
			}
		}
		
		for( String location : this.locations ) {
			if( !vs.getLocations().contains(location) ) {
				equal = false;
				break; //seen enough
			}
		}
		
		for( String location : vs.getLocations() ) {
			if( !this.locations.contains(location) ) {
				equal = false;
				break;
			}
		}
		
		return equal;
	
	}
	
 	public void printValueSetDefinition() {
 		if( definition == null ) {
 			System.out.println("Missing definition!: " + className);
 		}
 		else {
			try {
				File fil = new File(fileName);
				
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("lgVD:valueSetDefinition");
				doc.appendChild(rootElement);
				rootElement.setAttribute("xmlns:lgBuiltin", "http://LexGrid.org/schema/2010/01/LexGrid/builtins");
				rootElement.setAttribute("xmlns:lgCommon", "http://LexGrid.org/schema/2010/01/LexGrid/commonTypes");
				rootElement.setAttribute("xmlns:lgCon", "http://LexGrid.org/schema/2010/01/LexGrid/concepts");
				rootElement.setAttribute("xmlns:lgCS", "http://LexGrid.org/schema/2010/01/LexGrid/codingSchemes");
				rootElement.setAttribute("xmlns:lgNaming", "http://LexGrid.org/schema/2010/01/LexGrid/naming");
				rootElement.setAttribute("xmlns:lgRel", "http://LexGrid.org/schema/2010/01/LexGrid/relations");
				rootElement.setAttribute("xmlns:lgVD", "http://LexGrid.org/schema/2010/01/LexGrid/valueSets");
				rootElement.setAttribute("xmlns:lgVer", "http://LexGrid.org/schema/2010/01/LexGrid/versions");
				rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				rootElement.setAttribute("xsi:schemaLocation", "http://LexGrid.org/schema/2010/01/LexGrid/codingSchemes  http://LexGrid.org/schema/2010/01/LexGrid/codingSchemes.xsd");
				rootElement.setAttribute("isActive", "true");
				rootElement.setAttribute("status", "1");
				rootElement.setAttribute("valueSetDefinitionURI", "http://evs.nci.nih.gov/valueset/" + code);
				rootElement.setAttribute("valueSetDefinitionName", name);
				rootElement.setAttribute("defaultCodingScheme", "NCI_Thesaurus");
				rootElement.setAttribute("conceptDomain", "Intellectual Product");
				
				Element lgCommonOwner = doc.createElement("lgCommon:owner");
				lgCommonOwner.appendChild(doc.createTextNode("NCI"));
				rootElement.appendChild(lgCommonOwner);
				
				Element lgCommonEntityDescription = doc.createElement("lgCommon:entityDescription");
				lgCommonEntityDescription.appendChild(doc.createCDATASection(definition));
				rootElement.appendChild(lgCommonEntityDescription);
				
				Element lgVDMappings = doc.createElement("lgVD:mappings");
				rootElement.appendChild(lgVDMappings);
				
				Element lgNamingSupportedCodingScheme = doc.createElement("lgNaming:supportedCodingScheme");
				lgNamingSupportedCodingScheme.setAttribute("localId", "NCI_Thesaurus");
				lgNamingSupportedCodingScheme.setAttribute("uri", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#");
				lgNamingSupportedCodingScheme.setAttribute("isImported", "true");
				lgNamingSupportedCodingScheme.appendChild(doc.createTextNode("NCI_Thesaurus"));
				lgVDMappings.appendChild(lgNamingSupportedCodingScheme);
				
				Element lgNamingSupportedConceptDomain = doc.createElement("lgNaming:supportedConceptDomain");
				lgNamingSupportedConceptDomain.setAttribute("localId", "Intellectual Product");
				lgVDMappings.appendChild(lgNamingSupportedConceptDomain);
				
				Element lgNamingSupportedNamespace = doc.createElement("lgNaming:supportedNamespace");
				lgNamingSupportedNamespace.setAttribute("localId", "NCI_Thesaurus");
				lgNamingSupportedNamespace.setAttribute("uri", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#");
				lgNamingSupportedNamespace.setAttribute("equivalentCodingScheme", "NCI_Thesaurus");
				lgNamingSupportedNamespace.appendChild(doc.createTextNode("NCI_Thesaurus"));
				lgVDMappings.appendChild(lgNamingSupportedNamespace);
				
				for(String source : sources ) {
					Element lgNamingSupportedSource = doc.createElement("lgNaming:supportedSource");
					lgNamingSupportedSource.setAttribute("localId", source);
					lgNamingSupportedSource.appendChild(doc.createTextNode(source));
					lgVDMappings.appendChild(lgNamingSupportedSource);
				}
				
				for(String location : locations ) {
					Element lgVDSource = doc.createElement("lgVD:source");
					lgVDSource.appendChild(doc.createTextNode(location));
					rootElement.appendChild(lgVDSource);
				}
				
				//What is this Element used for?
				Element lgVDProperties = doc.createElement("lgVD:properties");
				rootElement.appendChild(lgVDProperties);
				
				Element lgVDDefinitionEntry = doc.createElement("lgVD:definitionEntry");
				lgVDDefinitionEntry.setAttribute("ruleOrder", "0");
				lgVDDefinitionEntry.setAttribute("operator", "OR");
				rootElement.appendChild(lgVDDefinitionEntry);
				
				Element lgVDEntityReference = doc.createElement("lgVD:entityReference");
				lgVDEntityReference.setAttribute("entityCode", code);
				lgVDEntityReference.setAttribute("entityCodeNamespace", "NCI_Thesaurus");
				lgVDEntityReference.setAttribute("referenceAssociation", "Concept_In_Subset");
				lgVDEntityReference.setAttribute("transitiveClosure", "true");
				lgVDEntityReference.setAttribute("leafOnly", "true");
				lgVDEntityReference.setAttribute("targetToSource", "true");
				lgVDDefinitionEntry.appendChild(lgVDEntityReference);
				
				//write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(fil);
				
				//Output to console for testing
				//StreamResult result = new StreamResult(System.out);
				
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				transformer.transform(source,  result);
				
				System.out.println("File saved!: " + fil.getName() );
	
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 	}
	
	

}
