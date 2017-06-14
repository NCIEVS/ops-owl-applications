package gov.nih.nci.evs.gobp.print;

import gov.nih.nci.evs.gobp.map.LogMapCell;
import gov.nih.nci.evs.gobp.map.MapElement;
import gov.nih.nci.evs.gobp.map.Mapping;
import gov.nih.nci.evs.gobp.map.MatchTypeEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LexGrid2LogMap extends DefaultHandler {

	String LexEVSMapFileName;
	LogMapCell cell;
	List<LogMapCell> cellList;
	Vector<MapElement> mapAtoms = new Vector<MapElement>();

	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.print.LexGrid2LogMap.class);

	public static void main(String args[]) {
		LexGrid2LogMap map = new LexGrid2LogMap(args[0]);
	}

	public LexGrid2LogMap(String fileName) {
		this(fileName, "./GoldStandard.txt");
		// this.LexEVSMapFileName = fileName;
		// cellList = new ArrayList<LogMapCell>();
		// // parseSAX();
		// parseDOM();
		// printLogMap();
		// if (mapAtoms.size() > 0) {
		// // start printing a LogMap formatted map
		// }

	}

	public LexGrid2LogMap(String fileName, String outputFileName) {
		this.LexEVSMapFileName = fileName;
		cellList = new ArrayList<LogMapCell>();
		// parseSAX();
		parseDOM();
		printLogMap(outputFileName);
	}

	private void parseDOM() {
		SAXBuilder saxBuilder = new SAXBuilder();
		File inputFile = new File(this.LexEVSMapFileName);

		Document document;
		try {
			document = saxBuilder.build(inputFile);

			Element ele = document.getRootElement();
			ele.getChildren("relations");
			List<Element> childList = ele.getChildren();
			Element test1 = ele.getChild("relations");
			List<Element> test2 = ele.getChildren("relations");
			for (Element child : childList) {
				if (child.getName().equals("relations")) {
					List<Element> mapsElement = child.getChildren();
					for (Element childCheck : mapsElement) {
						System.out.println(childCheck.getName());
						if (childCheck.getName().equals(
"associationPredicate")) {
							List<Element> maps = childCheck.getChildren();
							if (maps.size() > 0)
								processMaps(maps);
						}
					}
				}
				System.out.println(child.getName());
			}

			List<Element> childElement = ele.getChildren("relations");
			for (Element child : childElement) {
				child.getName();
			}
		} catch (JDOMException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void processMaps(List<Element> mapList) {
		for (Element map : mapList) {
			// TODO - replace colon with underscore?
			String sourceCode = map.getAttributeValue("sourceEntityCode");
			String sourceNS = map
					.getAttributeValue("sourceEntityCodeNamespace");
			Element target = (Element) map.getChildren().get(0);
			String targetCode = target.getAttributeValue("targetEntityCode");
			String targetNS = target
					.getAttributeValue("targetEntityCodeNamespace");
			List<Element> qualifiers = target.getChildren();
			String match = "", score = "";
			for (Element qualifier : qualifiers) {
				if (qualifier.getAttributeValue("associationQualifier").equals(
						"rel")) {
					match = getQualifierText(qualifier);
				} else if (qualifier.getAttributeValue("associationQualifier")
						.equals("score")) {
					score = getQualifierText(qualifier);
				}
			}
			MapElement localElement = new MapElement();
			localElement.setMapping(Mapping.parseMapping(match));
			localElement.setScore(new Float(score));
			localElement.setSource(sourceNS);
			localElement.setSourceCode(sourceCode);
			localElement.setTarget(targetNS);
			localElement.setTargetCode(targetCode);
			localElement.setMatchType(MatchTypeEnum.goldMatch);
			mapAtoms.add(localElement);
		}
	}

	private String getQualifierText(Element qual0) {
		String qualText = "";
		List<Element> textElement = qual0.getChildren();
		Element qualValueElement = textElement.get(0);
		String qualValue = qualValueElement.getContent(0).getValue();
		return qualValue;
		// return textElement.get(0).getAttributeValue("qualifierText");

	}

	private void parseSAX() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			parser.parse(LexEVSMapFileName, this);

		} catch (ParserConfigurationException e) {

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startElement(String s, String s1, String elementName,
			Attributes attributes) {
		boolean isRel = false;
		boolean isScore = false;
		if (elementName.equals("lgRel:source")) {
			cell = new LogMapCell();
			isRel = false;
			isScore = false;
			cell.setSourceEntityNamespace1(attributes
					.getValue("sourceEntityCodeNamespace"));
			cell.setSourceEntityCode1(attributes.getValue("sourceEntityCode"));

			// load in qualifiers somehow

			cellList.add(cell);
		} else if (elementName.equals("lgRel:target")) {
			cell.setTargetEntityNamespace2("targetEntityCodeNamespace");
			cell.setTargetEntityCode2(attributes.getValue("targetEntityCode"));
		} else if (elementName.equals("lgRel:associationQualification")) {
			if (attributes.getValue("associationQualifier").equals("rel")) {
				isRel = true;
				isScore = false;
			} else if (attributes.getValue("associationQualifier").equals(
					"score")) {
				isScore = true;
				isRel = false;
			}
		} else if (elementName.equals("qualifierText")) {
			if (isScore) {

			} else if (isRel) {

			}
		}
		cellList.add(cell);
	}

	private void printLogMap(String outputFileName) {
		// for (LogMapCell cellPrint : cellList) {
		// System.out.println(cellPrint.toString());
		// }
		
		// printLogMap("./GoldStandard.txt", this.mapAtoms);
		printLogMap(outputFileName, this.mapAtoms);
//		try {
//			PrintWriter pw;
//			pw = new PrintWriter(new File("./GoldStandard.txt"));
//
//			for (MapElement map : mapAtoms) {
//				String printMe = "http://purl.obolibrary.org/obo/go.owl#"
//						+ map.getSourceCode() + "|";
//				printMe = printMe
//						+ "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"
//						+ map.getTargetCode() + "|";
//				printMe = printMe + "=|" + map.getScore() + "|CLS";
//				pw.println(printMe);
//			}
//
//			pw.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}
	
	public static void printLogMap(String url, Vector<MapElement> mapVector) {
		try {
			System.out.println("Printing to " + url);
			PrintWriter pw;
			pw = new PrintWriter(new File(url));
			
			for (MapElement map : mapVector) {
				String printMe = "http://purl.obolibrary.org/obo/go.owl#"
						+ map.getSourceCode() + "|";
				printMe = printMe
						+ "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#"
						+ map.getTargetCode() + "|";
				printMe = printMe + "=|" + map.getScore() + "|CLS";
				pw.println(printMe);
			}

			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		}



}
