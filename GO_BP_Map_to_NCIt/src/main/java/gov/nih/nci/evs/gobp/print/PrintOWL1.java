package gov.nih.nci.evs.gobp.print;

/*
 * This is meant to print an OWL1 compatible version of the extraction
 */

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.PropertyType;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.entity.Role;
import gov.nih.nci.evs.owl.entity.Role.RoleModifier;
import gov.nih.nci.evs.owl.meta.PropertyDef;
import gov.nih.nci.evs.owl.meta.RoleDef;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
/**
 * We want GO to show up in the NCI edit tab, and be searchable simultaneously with NCIt
 * To accomplish that, we need to have GO formatted as follows:
 * 	1.  Term becomes rdfs:label (already handled by Obo2Owl)
 * 	2.  syn becomes FULL_SYN and [] becomes xml tag qualifiers
 *  3.  definition becomes Definition and [] becomes xml tag qualifiers
 *  4.  Term becomes Preferred_Name.
 *  5.  id becomes code (not finalized)
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

public class PrintOWL1 {
	private BufferedWriter bw;

	private File f;
	boolean is_NCIt = true;
	final private Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.print.PrintOWL1.class);
	// private PrintWriter pw;
	private BufferedWriter pw;

	public PrintOWL1(OWLKb owlKb, String outputURL) throws IOException {
		this(owlKb, outputURL, "./config/headerCode.txt");
	}

	public PrintOWL1(OWLKb owlKb, String outputURL, String configFile)
			throws IOException {
		String ns = owlKb.getDefaultNamespace();
		if (!ns.contains("Thesaurus")) {
			is_NCIt = false;
		}

		File file = new File(outputURL);
		if (!file.canWrite()) {
			System.out.println("Can't write");
		}

		String tempLoc = outputURL;
		if (!outputURL.startsWith("file")) {
			tempLoc = "file://" + outputURL;
		}
		URL url = new URL(tempLoc);
		// URL url = new URL(outputURL);
		String path = url.getPath();

		try {

			// pw = new PrintWriter(file);
			// pw = new PrintWriter(new BufferedWriter(new FileWriter(file)),
			// true);
			// pw = new PrintWriter(new File(outputURL));
			pw = new BufferedWriter(new FileWriter(path, true));

			// boolean errorFound = pw.checkError();
			// pw = new BufferedWriter(new FileWriter(file));
			// TODO this is not working
			// owlKb = removeObsoleteClasses(owlKb);

			printHeader(configFile);
			printObjectPropertyDeclarations(owlKb);
			printAnnotationPropertyDeclarations(owlKb);
			printConcepts(owlKb);
			printTail();
			pw.flush();

			pw.close();
		} catch (FileNotFoundException e) {
			logger.error("Unable to find file for writing: " + outputURL);
			throw e;
		} catch (IOException e) {
			logger.error("Unable to open file for writing: " + outputURL);
			throw e;
		} catch (Exception e) {
			logger.error("Something bad: ", e);
		}
	}

	private String checkCDATA(String value) {
		String tempValue = value;
		if ((value.contains("&") || value.contains("<"))
				&& !value.contains("CDATA")) {
			tempValue = "<![CDATA[" + value + "]]>";
		}
		return tempValue;
	}

	private void printAnnotationPropertyDeclarations(OWLKb owlKb) {
		// print DatatypeProperty declarations

		HashMap<URI, String> propMap = owlKb.getAllProperties();
		Set<URI> keySet = propMap.keySet();
		try {
			for (URI propCode : keySet) {
				String printme = "";
				if (propCode.equals("DEFINITION")
						|| propCode.equals("FULL_SYN")
						|| propCode.equals("Preferred_Name")) {
					printme = "<owl:AnnotationProperty rdf:about=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"
							+ propCode + "\">" + "\n";
				} else {
					printme = "<owl:AnnotationProperty rdf:ID=\"" + propCode
							+ "\">" + "\n";
				}

				PropertyDef property = owlKb.getPropertyForCode(propCode);

				printme = printme + "<rdfs:label>" + property.getName()
						+ "</rdfs:label>" + "\n";

				// if property is an AnnotationProperty

				// printme = printme
				// +
				// "<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#DatatypeProperty\"/>"
				// + "\n";
				//
				// // if property is a DatatypeProperty
				// printme = printme
				// +
				// "<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#AnnotationProperty\"/>"
				// + "\n";
				// printPropertyPicklist(property);
				// TODO print other annotations on property
				printme = printme + "</owl:AnnotationProperty>\n\n";
				// pw.println(printme);
				pw.write(printme + System.getProperty("line.separator"));
			}
			pw.flush();
		} catch (Exception e) {

		}

		// <owl:AnnotationProperty
		// rdf:about="http://purl.obolibrary.org/obo/go.owl#RO_0002161">
		// <rdfs:label
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">never_in_taxon</rdfs:label>
		// <hasDbXref
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">RO:0002161</hasDbXref>
		// <hasOBONamespace
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">external</hasOBONamespace>
		// <id
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">never_in_taxon</id>
		// <shorthand
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">never_in_taxon</shorthand>
		// <is_metadata_tag
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean">true</is_metadata_tag>
		// <is_class_level
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#boolean">true</is_class_level>
		// </owl:AnnotationProperty>

	}

	private void printAssociationDeclarations() {

		// print Associations as ObjectProperty with type AnnotationProperty
		// map<long, OntyxAssociationDef>::iterator pAssoc =
		// m_association.begin();
		// for( ; pAssoc != m_association.end(); ++pAssoc ) {
		// outFile << "<owl:ObjectProperty rdf:ID=\"" <<
		// pAssoc->second.getCode() << "\">" << endl
		// << "<rdfs:label>" << pAssoc->second.getName() << "</rdfs:label>" <<
		// endl
		// <<
		// "<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#AnnotationProperty\"/>"
		// << endl;
		// conceptnamefordoc = pAssoc->second.getName() + "_Association";
		// if( conceptNameInKB(conceptnamefordoc) ) {
		// docConcept = getConcept(conceptnamefordoc);
		// docProperties = docConcept.getProperties();
		// }
		// else
		// docProperties.clear();
		// if( docProperties.size() > 0 ) {
		// for( pTConProp = docProperties.begin(); pTConProp
		// !=docProperties.end(); ++pTConProp ) {
		// string mname = pTConProp->second.getName();
		// OntyxPropertyDef tmpProp = getPropertyDef(mname);
		// string mcode = tmpProp.getCode();
		// outFile << "<" << mcode << ">" <<
		// verifyOutString(pTConProp->second.getValue())
		// << "</" << mcode << ">" << endl;
		// }
		// }
		// outFile << "</owl:ObjectProperty>" << endl << endl;
		// }
		// TODO - for NCIt these are things like Concept_is_Subset. For GO it is
		// subsets like goslim_yeast
		// <inSubset
		// rdf:resource="http://purl.obolibrary.org/obo/go.owl#goslim_yeast"/>

		// <owl:AnnotationProperty
		// rdf:about="http://purl.obolibrary.org/obo/go.owl#inSubset">
		// <rdfs:label
		// rdf:datatype="http://www.w3.org/2001/XMLSchema#string">in_subset</rdfs:label>
		// </owl:AnnotationProperty>

	}

	private void printAssociations() {
		// need to output associations as object properties
		// multimap<long, OntyxAssociation> conAssoc =
		// pCon->second.getAssociations();
		// multimap<long, OntyxAssociation>::const_iterator pConAssoc =
		// conAssoc.begin();
		// string assocName = "", assocValue = "";
		// for( ; pConAssoc != conAssoc.end(); ++pConAssoc ) {
		// string aname = pConAssoc->second.getName();
		// OntyxAssociationDef tmpAssoc = getAssociationDef(aname);
		// string acode = tmpAssoc.getCode();
		// outFile << "<" << acode << " rdf:resource=\"#" <<
		// getConcept(pConAssoc->second.getValue()).getCode() << "\"/>" << endl;
		// }
	}

	private void printComplexProperty(Property property) throws IOException {
		Vector<Qualifier> qualifiers = property.getQualifiers();

		// if FULL_SYN print this
		if (property.getPropertyType().equals(PropertyType.SYNONYM)) {
			printSynonym(property);
		} else if (property.getCode().contains("FULL_SYN")) {
			// when coming from GO they don't always get the SYNONYM type
			printSynonym(property);
		}

		else if (property.getPropertyType().equals(PropertyType.DEFINITION)) {
			printDefinition(property);
		} else {
			// pw.print("<" + property.getCode() +
			// " rdf:parseType=\"Literal\">");
			// String tempValue = property.getValue();
			// if (property.getValue().contains("&")
			// || property.getValue().contains("<")) {
			// tempValue = "<![CDATA[" + property.getValue() + "]]>";
			// }

			pw.write("<" + property.getCode() + " rdf:parseType=\"Literal\">");
			String printme = "<ncicp:ComplexProperty><ncicp:value>";
			printme = printme + checkCDATA(property.getValue());
			printme = printme + "</ncicp:value>";

			// for (Qualifier qual : qualifiers) {
			// printme = printme + "<ncicp:" + qual.getName() + "><![CDATA["
			// + qual.getValue() + "]]></ncicp:" + qual.getName()
			// + ">";
			// }

			for (Qualifier qual : qualifiers) {
				// tempValue = qual.getValue();
				// if (qual.getValue().contains("&")
				// || qual.getValue().contains("<")) {
				// tempValue = "<![CDATA[" + qual.getValue() + "]]>";
				// }
				printme = printme + "<ncicp:" + qual.getName() + ">"
						+ checkCDATA(qual.getValue()) + "</ncicp:"
						+ qual.getName() + ">";
			}

			printme = printme + "</ncicp:ComplexProperty>";

			pw.write(printme);
			pw.write("</" + property.getCode() + ">"
					+ System.getProperty("line.separator"));
		}

	}

	private void printConcepts(OWLKb owlKb) {

		HashMap<URI, ConceptProxy> concepts = owlKb.getAllConcepts();
		Set<URI> codes = concepts.keySet();
		try {
			for (URI code : codes) {

				if (code.getFragment().contains("GO_")) {
					@SuppressWarnings("unused")
					String debug = "True";
				}

				if (code.getFragment().contains("BASP1_Gene") || code.getFragment().contains("TYMP_Gene")) {
					@SuppressWarnings("unused")
					String debug = "True";
				}

				ConceptProxy concept = concepts.get(code);
				String ns = concept.getNamespace();
				// if (concept.isDeprecated()) {
				// pw.println("<owl:DeprecatedClass rdf:ID=\"" + code + "\">");
				// } else {
				// pw.println("<owl:Class rdf:ID=\"" + code + "\">");
				// }

				if (ns.equals(owlKb.getDefaultNamespace())) {
					printLocalConcept(concept);
				} else {
					printReferencedConcept(concept);
				}

				String label = concept.getName();
				// pw.println("<rdfs:label>" + label + "</rdfs:label>");

				// String tempValue = label;
				// if ((label.contains("&") || label.contains("<"))
				// && !label.contains("CDATA")) {
				// tempValue = "<![CDATA[" + label + "]]>";
				// }

				if (label.length() > 0) {
					pw.write("<rdfs:label>" + checkCDATA(label)
							+ "</rdfs:label>"
							+ System.getProperty("line.separator"));
				}

				// if (label.contains("CDATA")) {
				// pw.write("<rdfs:label>" + label + "</rdfs:label>"
				// + System.getProperty("line.separator"));
				// } else if (label.length() > 0) {
				// pw.write("<rdfs:label><![CDATA[" + label
				// + "]]></rdfs:label>"
				// + System.getProperty("line.separator"));
				// }

				printParents(concept);

				printRoles(concept);

				printRoleGroups();

				printEquivalentClasses();

				printGenericProperties(concept);

				printAssociations();

				if (concept.isDeprecated()) {
					// pw.println("</owl:DeprecatedClass>");
					pw.write("</owl:DeprecatedClass>\n");
				} else {
					// pw.println("</owl:Class>");
					pw.write("</owl:Class>"
							+ System.getProperty("line.separator"));
				}
				// pw.println();
				pw.newLine();
				pw.flush();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printDefinition(Property property) throws IOException {

		Vector<Qualifier> qualifiers = property.getQualifiers();
		String printme = "";
		if (is_NCIt) {
			printme = printme + "<" + property.getCode()
					+ " rdf:parseType=\"Literal\">";
		} else {
			printme = printme + "<nci:" + property.getCode()
					+ " rdf:parseType=\"Literal\">";
		}

		printme = printme + "<ncicp:ComplexDefinition><ncicp:def-definition>";

		printme = printme + checkCDATA(property.getValue());

		printme = printme + "</ncicp:def-definition>";

		// String tempValue = property.getValue();

		// if (property.getValue().contains("CDATA")) {
		// printme = printme
		// + "<ncicp:ComplexDefinition><ncicp:def-definition>";
		//
		// printme = printme + property.getValue();
		//
		// printme = printme + "</ncicp:def-definition>";
		// } else {
		//
		// if (property.getValue().contains("&")
		// || property.getValue().contains("<")) {
		// tempValue = "<![CDATA[" + property.getValue() + "]]>";
		// }
		// printme = printme
		// + "<ncicp:ComplexDefinition><ncicp:def-definition>";
		//
		// printme = printme + tempValue;
		//
		// printme = printme + "</ncicp:def-definition>";
		// }

		// pw.print("]]></def-definition>");
		for (Qualifier qual : qualifiers) {

			printme = printme + "<ncicp:" + qual.getName() + ">"
					+ checkCDATA(qual.getValue()) + "</ncicp:" + qual.getName()
					+ ">";

			// if (qual.getValue().contains("CDATA")) {
			// printme = printme + "<ncicp:" + qual.getName() + ">"
			// + qual.getValue() + "</ncicp:" + qual.getName() + ">";
			// } else {
			// printme = printme + "<ncicp:" + qual.getName() + "><![CDATA["
			// + qual.getValue() + "]]></ncicp:" + qual.getName() + ">";
			// // pw.print("<" + qual.getName() + "><![CDATA[" + qual.getValue()
			// // + "]]></" + qual.getName() + ">");
			// }

			// tempValue = qual.getValue();
			// if (qual.getValue().contains("&") ||
			// qual.getValue().contains("<")) {
			// tempValue = "<![CDATA[" + qual.getValue() + "]]>";
			// }
			// printme = printme + "<ncicp:" + qual.getName() + ">"
			// + tempValue
			// + "</ncicp:" + qual.getName() + ">";
		}

		printme = printme + "</ncicp:ComplexDefinition>";
		// pw.print("</ncicp:ComplexDefinition>");

		if (is_NCIt) {
			printme = printme + "</" + property.getCode() + ">";
		} else {
			printme = printme + "</nci:" + property.getCode() + ">";
		}
		// pw.print(printme);
		pw.write(printme + System.getProperty("line.separator"));
	}

	private void printEquivalentClasses() {
		// else { // concept is defined, assume there are >= 2 conditions, and
		// the "parent" is not a kind, i.e. it's deep in tree
		// outFile << "<owl:equivalentClass>" << endl
		// << "\t" << "<owl:Class>" << endl
		// << "\t\t" << "<owl:intersectionOf rdf:parseType=\"Collection\">" <<
		// endl;
		//
		// set<string> conPar = pCon->second.getParents();
		// set<string>::const_iterator pConPar = conPar.begin();
		// for( ; pConPar != conPar.end(); ++pConPar ) {
		// OntyxConcept tmpConcept = getConcept(*pConPar);
		// string mcon = tmpConcept.getCode();
		// outFile << "\t\t\t" << "<owl:Class rdf:about=\"#" << mcon << "\"/>"
		// << endl;
		// }
		// if( pCon->second.getNumRoles() > 0 ) {
		// multimap<long, OntyxRole> conRoles = pCon->second.getRoles();
		// multimap<long, OntyxRole>::const_iterator pConRoles =
		// conRoles.begin();
		// for( ; pConRoles != conRoles.end(); ++pConRoles ) {
		// if( pConRoles->second.getRolegroup() == 0 ) { // take care of ontylog
		// role groups, do ungrouped roles first
		// string rname = pConRoles->second.getName();
		// OntyxRoleDef rrole = getRoleDef(rname);
		// string rcode = rrole.getCode();
		// outFile << "\t\t\t" << "<owl:Restriction>" << endl
		// << "\t\t\t\t" << "<owl:onProperty rdf:resource=\"#" << rcode <<
		// "\"/>" << endl;
		// if( pConRoles->second.getModifier() == "all" ) // use "some" by
		// default, dismiss "poss"
		// outFile << "\t\t\t\t" << "<owl:allValuesFrom rdf:resource=\"#" <<
		// getConcept(pConRoles->second.getValue()).getCode() << "\"/>" << endl;
		// else
		// outFile << "\t\t\t\t" << "<owl:someValuesFrom rdf:resource=\"#" <<
		// getConcept(pConRoles->second.getValue()).getCode() << "\"/>" << endl;
		// outFile << "\t\t\t" << "</owl:Restriction>" << endl;
		// }
		// }
		// if( pCon->second.hasRolegroups() ) { // ontylog role groups, do
		// grouped roles now
		// if( pCon->second.getNumRoleGroups() > 1 )
		// outFile << "\t\t\t" << "<owl:Class>" << endl
		// << "\t\t\t" << "<owl:unionOf rdf:parseType=\"Collection\">" << endl;
		// multimap<long, OntyxRole> conRoles = pCon->second.getRoles();
		// multimap<long, OntyxRole>::const_iterator pConRoles;
		// for(long groupCounter = 1; groupCounter <=
		// pCon->second.getNumRoleGroups(); ++groupCounter) {
		// outFile << "\t\t\t" << "<owl:Class>" << endl
		// << "\t\t\t\t" << "<owl:intersectionOf rdf:parseType=\"Collection\">"
		// << endl;
		// for( pConRoles = conRoles.begin(); pConRoles != conRoles.end();
		// ++pConRoles ) {
		// if( pConRoles->second.getRolegroup() == groupCounter ) {
		// string rname = pConRoles->second.getName();
		// OntyxRoleDef rrole = getRoleDef(rname);
		// string rcode = rrole.getCode();
		// outFile << "\t\t\t\t\t" << "<owl:Restriction>" << endl
		// << "\t\t\t\t\t\t" << "<owl:onProperty rdf:resource=\"#" << rcode <<
		// "\"/>" << endl;
		// if( pConRoles->second.getModifier() == "all" )
		// outFile << "\t\t\t\t\t\t" << "<owl:allValuesFrom rdf:resource=\"#" <<
		// getConcept(pConRoles->second.getValue()).getCode() << "\"/>" << endl;
		// else
		// outFile << "\t\t\t\t\t\t" << "<owl:someValuesFrom rdf:resource=\"#"
		// << getConcept(pConRoles->second.getValue()).getCode() << "\"/>" <<
		// endl;
		// outFile << "\t\t\t\t\t" << "</owl:Restriction>" << endl;
		// }
		// }
		// outFile << "\t\t\t\t" << "</owl:intersectionOf>" << endl
		// << "\t\t\t" << "</owl:Class>" << endl;
		// }
		// if( pCon->second.getNumRoleGroups() > 1 )
		// outFile << "\t\t\t" << "</owl:unionOf>" << endl
		// << "\t\t\t" << "</owl:Class>" << endl;
		// }
		// }
		// outFile << "\t\t" << "</owl:intersectionOf>" << endl
		// << "\t" << "</owl:Class>" << endl
		// << "</owl:equivalentClass>" << endl;
		// }

	}

	private void printGenericProperties(ConceptProxy concept)
			throws IOException {

		Vector<Property> properties = concept.getProperties();
		for (Property property : properties) {
			if (property.isComplexProperty()) {
				printComplexProperty(property);
			} else {
				// remove label and FULL_SYN. Will need to fix this in OWL
				// converter, but this is a workaround for now
				// String tempValue = property.getValue();
				// if (property.getValue().contains("&")
				// || property.getValue().contains("<")) {
				// tempValue = "<![CDATA[" + property.getValue() + "]]>";
				// }

				if (property.getCode().equals("FULL_SYN")
						|| property.getCode().equals("label")) {
					// System.out.println("FULL-SYN with no quals not printed");
				} else if (property.getCode().equals("Preferred_Name")
						&& !is_NCIt) {

					pw.write("<nci:"
							+ property.getCode()
							+ " rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
							+ checkCDATA(property.getValue()) + "</nci:"
							+ property.getCode() + ">" + "\n");
				} else {
					pw.write("<"
							+ property.getCode()
							+ " rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
							+ checkCDATA(property.getValue()) + "</"
							+ property.getCode() + ">" + "\n");
				}
			}
		}

	}

	private void printHeader(String configFile) {
		// header is OK already. Just copy it from extraction?
		try {
			BufferedReader bfr = new BufferedReader(new FileReader(configFile));
			for (String line = bfr.readLine(); line != null; line = bfr
					.readLine()) {
				pw.append(line + "\n");
			}
			bfr.close();
			pw.flush();
		} catch (FileNotFoundException e) {
			logger.error("Unable to find header file");

		} catch (IOException e) {
			logger.error("Problem reading from header file");
		} catch (Exception e) {
			logger.error("Error printing header ", e);
		}

	}

	private void printLocalConcept(ConceptProxy concept) throws IOException {
		if (concept.isDeprecated()) {
			// pw.println("<owl:DeprecatedClass rdf:ID=\"" + concept.getCode()
			// + "\">");
			pw.write("<owl:DeprecatedClass rdf:ID=\"" + concept.getCode()
					+ "\">" + System.getProperty("line.separator"));
		} else {
			// pw.println("<owl:Class rdf:ID=\"" + concept.getCode() + "\">");
			pw.write("<owl:Class rdf:ID=\"" + concept.getCode() + "\">"
					+ System.getProperty("line.separator"));
		}
	}

	private void printObjectPropertyDeclarations(OWLKb owlKb) {

		// print object property declarations
		try {
			HashMap<URI, String> roles = owlKb.getAllRoles();
			Set<URI> rolekey = roles.keySet();
			for (URI key : rolekey) {
				RoleDef role = owlKb.getRoleForCode(key);
				String printme = "";
				printme = "<owl:ObjectProperty rdf:ID=\"" + role.getCode()
						+ "\">" + "\n";
				printme = printme + "<rdfs:label>" + role.getName()
						+ "</rdfs:label>" + "\n";

				// see if role has parents
				Vector<RoleDef> parentRoles = role.getParents();
				for (RoleDef parentRole : parentRoles) {
					printme = printme + "\t"
							+ "<rdfs:subPropertyOf rdf:resource=\""
							+ parentRole.getCode() + "\"/>" + "\n";
				}
				// TODO pull out and print annotations on the role
				Vector<URI> range;
				Vector<URI> domain;
				range = role.getRange();
				domain = role.getDomain();
				for (URI rangeText : range) {
					printme = printme + "<rdfs:range rdf:resource=\""
							+ rangeText + "\"/>" + "\n";
				}
				for (URI domainText : domain) {
					printme = printme + "<rdfs:domain rdf:resource=\""
							+ domainText + "\"/>" + "\n";
				}
				printme = printme + "</owl:ObjectProperty>" + "\n\n";
				// pw.print(printme);
				pw.write(printme);

			}
			pw.flush();
		} catch (Exception e) {
			logger.error("Bad thing happened", e);
		}
		// pw.flush();
		/**
		 * Do we need to worry about any other annotations on the role? For
		 * example: <owl:ObjectProperty
		 * rdf:about="http://purl.obolibrary.org/obo/go.owl#RO_0002211">
		 * <rdfs:label
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string">regulates
		 * </rdfs:label> <hasDbXref
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
		 * >RO:0002211</hasDbXref> <hasOBONamespace
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
		 * >external</hasOBONamespace> <shorthand
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string"
		 * >regulates</shorthand> <id
		 * rdf:datatype="http://www.w3.org/2001/XMLSchema#string">regulates</id>
		 * <owl:propertyChainAxiom rdf:parseType="Collection"> <rdf:Description
		 * rdf:about="http://purl.obolibrary.org/obo/go.owl#RO_0002211"/>
		 * <rdf:Description
		 * rdf:about="http://purl.obolibrary.org/obo/go.owl#BFO_0000050"/>
		 * </owl:propertyChainAxiom> </owl:ObjectProperty>
		 **/

		// if( conceptNameInKB(conceptnamefordoc) ) {
		// docConcept = getConcept(conceptnamefordoc);
		// docProperties = docConcept.getProperties();
		// }
		// else
		// docProperties.clear();
		// if( docProperties.size() > 0 ) {
		// for( pTConProp = docProperties.begin(); pTConProp
		// !=docProperties.end(); ++pTConProp ) {
		// string mname = pTConProp->second.getName();
		// OntyxPropertyDef tmpProp = getPropertyDef(mname);
		// string mcode = tmpProp.getCode();
		// outFile << "<" << mcode << ">" <<
		// verifyOutString(pTConProp->second.getValue())
		// << "</" << mcode << ">" << endl;
		// }
		// }

	}

	private void printParents(ConceptProxy concept) throws IOException {
		Vector<URI> parentCodes = concept.getParentCodes();
		if (parentCodes.size() < 1) {
			String debug = "True";
			parentCodes = concept.getParentCodes();
		}

		for (URI code : parentCodes) {
			if (code.getFragment().contains("GO_")) {
				String debug = "True";
			}
			
			pw.write("<rdfs:subClassOf rdf:resource=\"" + code + "\"/>"
					+ System.getProperty("line.separator"));

//			if (code.contains("#")) {
//				pw.write("<rdfs:subClassOf rdf:resource=\"" + code + "\"/>"
//						+ System.getProperty("line.separator"));
//			} else {
//
//				pw.write("<rdfs:subClassOf rdf:resource=\""
//						+ concept.getNamespace() + "#" + code + "\"/>"
//						+ System.getProperty("line.separator"));
//			}
		}

	}

	private void printPropertyPickList(PropertyDef property) {
		// TODO - we don't deal with this at all in OWLKb. Add code for that
		// if( !pProp->second.hasPickList() )
		// outFile <<
		// "<rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#string\"/>"
		// << endl;
		// else {
		// vector<string> picklist = pProp->second.getPickList();
		// vector<string>::iterator pPicklist = picklist.begin();
		// outFile << "<rdfs:range>" << endl
		// << "\t" << "<owl:DataRange>" << endl
		// << "\t" << "<owl:oneOf>" << endl;
		// while( pPicklist != picklist.end() ) {
		// outFile << "\t\t" << "<rdf:List>" << endl
		// << "\t\t" <<
		// "<rdf:first rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
		// << *pPicklist << "</rdf:first>" << endl;
		// if( ++pPicklist != picklist.end() )
		// outFile << "\t\t" << "<rdf:rest>" << endl;
		// else
		// outFile << "\t\t" <<
		// "<rdf:rest rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>"
		// << endl
		// << "\t\t" << "</rdf:List>" << endl;
		// }
		// pPicklist = picklist.begin();
		// ++pPicklist; // need to advance it 'cause the last element in the
		// listing above gets an </rdf:List>
		// for( ; pPicklist != picklist.end(); ++pPicklist ) {
		// outFile << "\t\t" << "</rdf:rest>" << endl
		// << "\t\t" << "</rdf:List>" << endl;
		// }
		// outFile << "\t" << "</owl:oneOf>" << endl
		// << "\t" << "</owl:DataRange>" << endl
		// << "</rdfs:range>" << endl;
		// }
	}

	private void printReferencedConcept(ConceptProxy concept)
			throws IOException {
		if (concept.isDeprecated()) {
			// pw.println("<owl:DeprecatedClass rdf:about=\"" +
			// concept.getCode()
			// + "\">");
			pw.write("<owl:DeprecatedClass rdf:about=\""
					+ concept.getNamespace() + "#" + concept.getCode() + "\">"
					+ System.getProperty("line.separator"));
		} else {
			// pw.println("<owl:Class rdf:about=\"" + concept.getCode() +
			// "\">");
			pw.write("<owl:Class rdf:about=\"" + concept.getNamespace() + "#"
					+ concept.getCode() + "\">"
					+ System.getProperty("line.separator"));
		}
	}

	private void printRoleGroups() {
		// if( pCon->second.hasRolegroups() ) { // ontylog role groups, do
		// grouped roles now
		// outFile << "<rdfs:subClassOf>" << endl
		// << "\t" << "<owl:Class>" << endl;
		// if( pCon->second.getNumRoleGroups() > 1 )
		// outFile << "\t\t" << "<owl:unionOf rdf:parseType=\"Collection\">" <<
		// endl;
		// for(long groupCounter = 1; groupCounter <=
		// pCon->second.getNumRoleGroups(); ++groupCounter) {
		// if( pCon->second.getNumRoleGroups() > 1 ) {
		// outFile << "\t\t\t" << "<owl:Class>" << endl;
		// }
		// outFile << "\t\t\t\t" <<
		// "<owl:intersectionOf rdf:parseType=\"Collection\">" << endl;
		// for( pConRoles = conRoles.begin(); pConRoles != conRoles.end();
		// ++pConRoles ) {
		// if( pConRoles->second.getRolegroup() == groupCounter ) {
		// outFile << "\t\t\t\t\t" << "<owl:Restriction>" << endl;
		// string rname = pConRoles->second.getName();
		// OntyxRoleDef rrole = getRoleDef(rname);
		// string rcode = rrole.getCode();
		// outFile << "\t\t\t\t\t\t" << "<owl:onProperty rdf:resource=\"#" <<
		// rcode << "\"/>" << endl;
		// if( pConRoles->second.getModifier() == "all" )
		// outFile << "\t\t\t\t\t\t" << "<owl:allValuesFrom rdf:resource=\"#" <<
		// getConcept(pConRoles->second.getValue()).getCode() << "\"/>" << endl;
		// else // default is some
		// outFile << "\t\t\t\t\t\t" << "<owl:someValuesFrom rdf:resource=\"#"
		// << getConcept(pConRoles->second.getValue()).getCode() << "\"/>" <<
		// endl;
		// outFile << "\t\t\t\t\t" << "</owl:Restriction>" << endl;
		// }
		// }
		// outFile << "\t\t\t\t" << "</owl:intersectionOf>" << endl;
		// if( pCon->second.getNumRoleGroups() > 1 ) {
		// outFile << "\t\t\t" << "</owl:Class>" << endl;
		// }
		// }
		// if( pCon->second.getNumRoleGroups() > 1 )
		// outFile << "\t\t" << "</owl:unionOf>" << endl;
		// outFile << "\t" << "</owl:Class>" << endl
		// << "</rdfs:subClassOf>" << endl;
		// }
	}

	private void printRoles(ConceptProxy concept) throws IOException {
		Vector<Role> roles = concept.getRoles();

		for (Role role : roles) {
			// pw.println("<rdfs:subClassOf>");
			// pw.println("\t" + "<owl:Restriction>");
			// pw.println("\t\t" + "<owl:onProperty rdf:resource=\"#"
			// + role.getCode() + "\"/>");
			// if (role.getRoleModifier().equals(RoleModifier.ALL)) {
			// pw.println("\t\t" + "<owl:allValuesFrom rdf:resource=\"#"
			// + role.getTargetCode() + "\"/>");
			// } else {
			// pw.println("\t\t" + "<owl:someValuesFrom rdf:resource=\"#"
			// + role.getTargetCode() + "\"/>");
			// }
			// pw.println("\t" + "</owl:Restriction>");
			// pw.println("</rdfs:subClassOf>");

			pw.write("<rdfs:subClassOf>\n"
					+ System.getProperty("line.separator"));
			pw.write("\t" + "<owl:Restriction>\n"
					+ System.getProperty("line.separator"));
			pw.write("\t\t" + "<owl:onProperty rdf:resource=\"#"
					+ role.getCode() + "\"/>\n"
					+ System.getProperty("line.separator"));
			if (role.getRoleModifier().equals(RoleModifier.ALL)) {
				pw.write("\t\t" + "<owl:allValuesFrom rdf:resource=\""
						+ role.getTarget().getNamespace() + "#"
						+ role.getTargetCode() + "\"/>"
						+ System.getProperty("line.separator"));
			} else {
				pw.write("\t\t" + "<owl:someValuesFrom rdf:resource=\""
						+ role.getTarget().getNamespace() + "#"
						+ role.getTargetCode() + "\"/>"
						+ System.getProperty("line.separator"));
			}
			pw.write("\t" + "</owl:Restriction>"
					+ System.getProperty("line.separator"));
			pw.write("</rdfs:subClassOf>\n"
					+ System.getProperty("line.separator"));
		}

	}

	private void printSynonym(Property property) throws IOException {

		String printme = "";
		if (is_NCIt) {
		printme = printme + "<" + property.getCode()
				+ " rdf:parseType=\"Literal\">";
		} else {
			printme = printme + "<nci:" + property.getCode()
					+ " rdf:parseType=\"Literal\">";
		}
		// if (property.getValue().contains("CDATA")) {
		printme = printme
				+ "<ncicp:ComplexTerm xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\"><ncicp:term-name>";
		printme = printme + checkCDATA(property.getValue());
		printme = printme + "</ncicp:term-name>";
		// } else {
		//
		// printme = printme
		// +
		// "<ncicp:ComplexTerm xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\"><ncicp:term-name><![CDATA[";
		// printme = printme + property.getValue();
		// printme = printme + "]]></ncicp:term-name>";
		// }

		Vector<Qualifier> qualifiers = property.getQualifiers();
		for (Qualifier qual : qualifiers) {
			// if (qual.getValue().contains("CDATA")) {

			if (qual.getName().equals("hasSynonymType")) {
				printme = printme + "<ncicp:term-group>"
						+ checkCDATA(qual.getValue()) + "</ncicp:term-group>";
			} else if (qual.getName().equals("hasDbXref")) {
				printme = printme + "<ncicp:" + qual.getName() + ">"
						+ checkCDATA(qual.getValue()) + "</ncicp:"
						+ qual.getName() + ">";
			} else {
				printme = printme + "<ncicp:" + qual.getName() + ">"
						+ qual.getValue() + "</ncicp:" + qual.getName() + ">";
			}

			// } else {
			//
			// if (qual.getName().equals("hasSynonymType")) {
			// printme = printme + "<ncicp:term-group>" + qual.getValue()
			// + "</ncicp:term-group>";
			// } else {
			// printme = printme + "<ncicp:" + qual.getName() + "><![CDATA["
			// + qual.getValue() + "]]></ncicp:" + qual.getName()
			// + ">";
			// }
			// }
			// pw.print("<" + qual.getName() + "><![CDATA[" + qual.getValue()
			// + "]]></" + qual.getName() + ">");
		}
		// if FULL_SYN print this

		printme = printme + "</ncicp:ComplexTerm>";
		// pw.print("</ncicp:ComplexTerm>");
		if (is_NCIt) {
		printme = printme + "</" + property.getCode() + ">";
		} else {
			printme = printme + "</nci:" + property.getCode() + ">";
		}
		// pw.print(printme);
		pw.write(printme + System.getProperty("line.separator"));

	}

	private void printTail() {
		// TODO Auto-generated method stub
		// pw.println("</rdf:RDF>");
		try {
			pw.write("</rdf:RDF>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private OWLKb removeObsoleteClasses(OWLKb owlkb) {
		/*
		 * These need to be left in the base extraction in order to have access
		 * to replaced_by for mapping update We need to remove them before
		 * import into protege
		 */

		owlkb.removeBranch("Deprecated");
		return owlkb;
	}

	/**
	 * OWLKB version
	 *
	 *
	 *
	 * void OntyxKb::writeOWLFile (const string & filename, const string
	 * headerFile, bool noProps) // flag = true:renumber // if noProps is true,
	 * properties with names derived from roles are not exported
	 *
	 * // 060813 since we have been deleting properties from a file listing,
	 * will make the "noProps" argument // irrelevant and will change the
	 * signature in the future, the bool noProps doesn't make a difference // as
	 * of 060813. { set<string> kind_names; // string roleprefix = "o"; // for
	 * object property // string propprefix = "d"; // for datatype property
	 *
	 * // map<string, string> validrolenames; // map<string, string>
	 * validpropnames; // string validname = "";
	 *
	 *
	 * set<string> hasValueDatatypes;
	 * hasValueDatatypes.insert("Gene_In_Chromosomal_Location");
	 * hasValueDatatypes.insert("Allele_In_Chromosomal_Location");
	 * hasValueDatatypes
	 * .insert("Allele_Absent_From_Wild-type_Chromosomal_Location");
	 * hasValueDatatypes.insert("Gene_Has_Physical_Location");
	 *
	 * ifstream inFile; inFile.open(headerFile.c_str()); if( !inFile.good() ) {
	 * cerr << "Can't find/open file '" << headerFile.c_str() <<
	 * "'.  Will EXIT." << endl; exit(0); } string inputLine;
	 *
	 * string trailer = "</rdf:RDF>"; ofstream outFile;
	 * outFile.open(filename.c_str());
	 *
	 * while( getline(inFile, inputLine), !inFile.eof() ) outFile << inputLine
	 * << endl;
	 *
	 * string conceptnamefordoc = ""; OntyxConcept docConcept; multimap<long,
	 * OntyxProperty> docProperties; multimap<long, OntyxProperty>::iterator
	 * pTConProp;
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 * //print kinds map<long, OntyxKind>::iterator pKind0; map<long,
	 * OntyxKind>::iterator pKind = m_kind.begin(); for( ; pKind !=
	 * m_kind.end(); ++pKind) { isValidOWL(pKind->second.getKindname());
	 * kind_names.insert(pKind->second.getCode()); string conname, condef = "",
	 * consemtype = "", lcode; OntyxConcept tmpConcept; lcode =
	 * pKind->second.getCode(); conname = pKind->second.getKindname(); outFile
	 * << "<owl:Class rdf:ID=\"" << pKind->second.getCode() << "\">" << endl <<
	 * '\t' << "<rdfs:label>" << conname << "</rdfs:label>" << endl; if(
	 * conceptNameInKB(pKind->second.getKindname())) { // is there a concept
	 * with the same name tmpConcept = getConcept(pKind->second.getKindname());
	 *
	 * multimap<long, OntyxProperty> conProp = tmpConcept.getProperties();
	 * multimap<long, OntyxProperty>::const_iterator pConProp = conProp.begin();
	 * string propString, propName = ""; for( ; pConProp != conProp.end();
	 * ++pConProp ) { propString = verifyOutString(pConProp->second.getValue());
	 * string mname = pConProp->second.getName(); OntyxPropertyDef tmpProp =
	 * getPropertyDef(mname); string mcode = tmpProp.getCode(); if(
	 * pConProp->second.hasQualifier() ) { // 080623 if qualifiers exist, print
	 * CDATA tag outFile << "<" << mcode << ">" << "<![CDATA[<value>" <<
	 * propString << "</value>"; vector<OntyxQualifier> vqual =
	 * pConProp->second.getQualifiers(); vector<OntyxQualifier>::iterator pvqual
	 * = vqual.begin(); for( ; pvqual != vqual.end(); ++pvqual ) { outFile <<
	 * "<qual>"; outFile << "<qual-name>" << pvqual->getName() <<
	 * "</qual-name>"; outFile << "<qual-value>" << pvqual->getValue() <<
	 * "</qual-value>"; outFile << "</qual>"; } outFile << "]]></" << mcode <<
	 * ">" << endl; } else outFile << "<" << mcode << ">" << propString << "</"
	 * << mcode << ">" << endl; } } for( pKind0 = m_kind.begin(); pKind0 !=
	 * m_kind.end(); ++pKind0 ) { if( pKind0->second.getKindname() !=
	 * pKind->second.getKindname() ) outFile << '\t' <<
	 * "<owl:disjointWith rdf:resource=\"#" << pKind0->second.getCode() <<
	 * "\"/>" << endl; } outFile << "</owl:Class>" << endl << endl; }
	 *
	 *
	 *
	 * outFile << trailer << endl; outFile.close(); }
	 *
	 *
	 */

}
