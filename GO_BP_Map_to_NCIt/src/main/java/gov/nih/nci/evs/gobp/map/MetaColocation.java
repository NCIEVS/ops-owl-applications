package gov.nih.nci.evs.gobp.map;

import gov.nih.nci.evs.gobp.RemoteServerUtil;
import gov.nih.nci.evs.gobp.print.LexGrid2LogMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Collections.AssociatedConceptList;
import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.apache.log4j.Logger;

public class MetaColocation {
	private LexEVSService evsService;
	public static final String _service = "EvsServiceInfo";
	private String source;
	private String target;
	boolean checkNeighbors = false;
	private Vector<MapElement> maps;
	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.map.MetaColocation.class);

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		boolean checkNearby = false;
		if (args.length >= 4) {
			checkNearby = Boolean.parseBoolean(args[3]);
		}
		if (args.length >= 3) {
			Set<String> codeList = readConfigFile(args[0]);
			String source1 = args[1];
			String source2 = args[2];
			Vector<MapElement> mapOutput = new MetaColocation(codeList,
					source1, source2, checkNearby).getMaps();
			LexGrid2LogMap.printLogMap(
"./MetaColocation.txt", mapOutput);
		} else {
			System.out
					.println("Three parameters required - files address for code list, SAB of source, and SAB of target");
			System.out
					.println("Optional fourth parameter is a boolean specifying whether broader and narrower concepts should be searched");
		}

	}

	private static Set<String> readConfigFile(String filename) {
		Set<String> v = new TreeSet<String>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					v.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				System.out.println(filename + " not found or unreadable");
				e.printStackTrace();
			}
		}
		if (!v.isEmpty())
			return v;
		return null;
	}

	public MetaColocation(String source1, String source2) {
		this(source1, source2, false);
	}

	public MetaColocation(String source1, String source2, boolean checkNearby) {
		try {
			this.source = source1;
			this.target = source2;
			this.checkNeighbors = checkNearby;
			setUp();

		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MetaColocation(String conceptCode, String source1, String source2) {
		this(conceptCode, source1, source2, false);

	}

	public MetaColocation(String conceptCode, String source1, String source2,
			boolean checkNearby) {

		try {
			setUp();
			findAndResolveCode(conceptCode, source1, source2, checkNearby);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Vector<MapElement> findAndResolveCode(String code) {
		return findAndResolveCode(code, this.source, this.target,
				this.checkNeighbors);
	}

	private Vector<MapElement> findAndResolveCode(String code, String source1,
			String source2, boolean checkNearby) {
		ResolvedConceptReferenceList rcrl;
		String parsedEntity = "";
		Vector<MapElement> maps = new Vector<MapElement>();
		try {
			rcrl = searchForSourceCode(code.trim(), source1);

			if (rcrl.getResolvedConceptReferenceCount() > 0) {
				ResolvedConceptReference ref = rcrl
						.enumerateResolvedConceptReference().nextElement();

				Entity entry = ref.getReferencedEntry();
				if (entry != null) {
				maps = parseEntity(entry, code, source1,
 source2, (float) 0.75);
				}
				// parsedEntity = parseEntity(entry, source1, source2);
				// parsedEntity = code + "\t " + parsedEntity;
				//
				// System.out.println(parsedEntity);

				// MapElement element = new MapElement(source1, code, source2,
				// "",
				// MatchType.colocationMatch,
				// Mapping.equivalent, (float) 1.0);
				// for (MapElement element : map) {
				// maps.add(element);
				// }

				if (checkNearby && maps.size() < 1) {
					Vector<MapElement> parents = getParents(entry, code,
							source1, source2);
					for (MapElement element : parents) {
						maps.add(element);
					}
					Vector<MapElement> children = getChildren(entry, code,
							source1, source2);
					for (MapElement element : children) {
						maps.add(element);
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return maps;
	}

	public MetaColocation(Set<String> codeList, String source1, String source2) {
		this(codeList, source1, source2, false);

	}

	public MetaColocation(Set<String> codeList, String source1, String source2,
			boolean checkNearby) {

		setUp();

		maps = new Vector<MapElement>();
		try {

			int i = 0;
			for (String code : codeList) {
				i++;
				if (i % 10 == 0) {
					System.out.println("Analyzing concept " + i + " out of "
							+ codeList.size());
				}
				Vector<MapElement> mapsToCode = findAndResolveCode(code,
						source1, source2, checkNearby);

				for (MapElement element : mapsToCode) {
					maps.add(element);
				}
			}

		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Vector<MapElement> getMaps() {
		return maps;
	}

	public void setCheckNeighbors(boolean checkNeighbors) {
		this.checkNeighbors = checkNeighbors;
	}

	public Vector<MapElement> getChildren(Entity metaConcept,
			String sourceCode, String source1,
			String source2) {
		NameAndValue nv = new NameAndValue();
		nv.setName("RN");
		NameAndValue nv2 = new NameAndValue();
		nv2.setName("CHD");
		NameAndValueList nvl = new NameAndValueList();
		nvl.addNameAndValue(nv);
		nvl.addNameAndValue(nv2);

		return getAssociatedConcepts(metaConcept, sourceCode, source1, source2,
				nvl,
				Mapping.narrower_than);
	}

	public Vector<MapElement> getAssociatedConcepts(Entity metaConcept,
			String sourceCode,
			String source1, String source2, NameAndValueList nvl,
			Mapping mapType) {
		String code = metaConcept.getEntityCode();
		Vector<MapElement> parents = null;
		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		csvt.setTag("PRODUCTION");
		try {
			// evsService.getNodeGraph("NCI Metathesaurus", csvt,null);

			// resolveOneLevel("NCI Metathesaurus", code, true, true, 0, null);

			// NameAndValue nv = new NameAndValue();
			// nv.setName("RB");
			// NameAndValue nv2 = new NameAndValue();
			// nv2.setName("PAR");
			// NameAndValueList nvl = new NameAndValueList();
			// nvl.addNameAndValue(nv);
			// nvl.addNameAndValue(nv2);
			//

			LexBIGService svc = RemoteServerUtil.createLexBIGService();
			ResolvedConceptReferenceList matches = svc
					.getNodeGraph("NCI MetaThesaurus", csvt, null)
					.restrictToAssociations(nvl, null)
					.resolveAsList(
							ConvenienceMethods.createConceptReference(code,
									"NCI MetaThesaurus"), false, true, 1, 1,
							new LocalNameList(), null, null, 1024);
			if (matches.getResolvedConceptReferenceCount() > 0) {
				// start loading the graph
				svc.resolveCodingScheme("NCI Metathesaurus", null);
				ResolvedConceptReference ref = matches
						.enumerateResolvedConceptReference().nextElement();
				AssociationList targetof = ref.getTargetOf();
				if (targetof != null) {
					Association[] associations = targetof.getAssociation();
					for (int i = 0; i < associations.length; i++) {
						Association assoc = associations[i];
						// System.out.println(assoc.getAssociationName());
						// AssociatedConcept[] acl =
						// assoc.getAssociatedConcepts()
						// .getAssociatedConcept();

						AssociatedConceptList acl2 = assoc
								.getAssociatedConcepts();
						acl2.getAssociatedConceptCount();


						AssociatedConcept acTest = acl2
								.enumerateAssociatedConcept().nextElement();
						AssociatedConcept[] acl = acl2.getAssociatedConcept();
						for (int j = 0; j < acl.length; j++) {
							AssociatedConcept ac = acl[j];
							EntityDescription ed = ac.getEntityDescription();
							Entity ent = ac.getReferencedEntry();
							parents = parseEntity(ent, sourceCode, source1,
									source2,
									mapType);


						}
					}
				}
			}
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parents;
	}

	public Vector<MapElement> getAssociatedConcepts(Entity metaConcept,
			String sourceCode,
			String source1, String source2, NameAndValueList nvl) {

		return getAssociatedConcepts(metaConcept, sourceCode, source1, source2,
				nvl,
				Mapping.equivalent);

		// String code = metaConcept.getEntityCode();
		// String[] parents = null;
		// CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		// csvt.setTag("PRODUCTION");
		// try {
		// // evsService.getNodeGraph("NCI Metathesaurus", csvt,null);
		//
		// // resolveOneLevel("NCI Metathesaurus", code, true, true, 0, null);
		//
		// // NameAndValue nv = new NameAndValue();
		// // nv.setName("RB");
		// // NameAndValue nv2 = new NameAndValue();
		// // nv2.setName("PAR");
		// // NameAndValueList nvl = new NameAndValueList();
		// // nvl.addNameAndValue(nv);
		// // nvl.addNameAndValue(nv2);
		// //
		//
		// ResolvedConceptReferenceList matches = evsService
		// .getNodeGraph("NCI MetaThesaurus", csvt, null)
		// .restrictToAssociations(nvl, null)
		// .resolveAsList(
		// ConvenienceMethods.createConceptReference(code,
		// "NCI MetaThesaurus"), false, true, 1, 1,
		// new LocalNameList(), null, null, 1024);
		// if (matches.getResolvedConceptReferenceCount() > 0) {
		// // start loading the graph
		// ResolvedConceptReference ref = matches
		// .enumerateResolvedConceptReference().nextElement();
		// AssociationList targetof = ref.getTargetOf();
		// if (targetof != null) {
		//
		// Association[] associations = targetof.getAssociation();
		// for (int i = 0; i < associations.length; i++) {
		// Association assoc = associations[i];
		// // System.out.println(assoc.getAssociationName());
		// AssociatedConcept[] acl = assoc.getAssociatedConcepts()
		// .getAssociatedConcept();
		// for (int j = 0; j < acl.length; j++) {
		// AssociatedConcept ac = acl[j];
		// EntityDescription ed = ac.getEntityDescription();
		// Entity ent = ac.getReferencedEntry();
		// Vector<MapElement> parsedEntities = parseEntity(
		// ent, source1,
		// source2);
		// // System.out
		// // .println("\t"
		// // + ac.getConceptCode()
		// // + "/"
		// // + assoc.getAssociationName()
		// // + "/"
		// // + (ed == null ? "**No Description**"
		// // : ed.getContent()) + " "
		// // + parsedEntity);
		//
		// }
		// }
		// }
		// }
		// } catch (LBException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return parents;
	}

	public Vector<MapElement> getParents(Entity metaConcept, String sourceCode,
			String source1,
			String source2) {

		NameAndValue nv = new NameAndValue();
		nv.setName("RB");
		NameAndValue nv2 = new NameAndValue();
		nv2.setName("PAR");
		NameAndValueList nvl = new NameAndValueList();
		nvl.addNameAndValue(nv);
		nvl.addNameAndValue(nv2);

		return getAssociatedConcepts(metaConcept, sourceCode, source1, source2,
				nvl,
				Mapping.broader_than);
	}

	public void setUp() {
		evsService = RemoteServerUtil.createLexBIGService();
		// evsService = RemoteServerUtil.createLexEVSService();
	}

	public void setUp(String url) {
		evsService = RemoteServerUtil.createLexEVSService(url);
	}

	private Vector<MapElement> parseEntity(Entity entity, String sourceCode,
			String source1, String source2, Mapping mapType) {
		return parseEntity(entity, sourceCode, source1, source2, mapType,
				(float) 0.5);

		// Vector<MapElement> maps = new Vector<MapElement>();
		// String printMe = entity.getEntityCode();
		// printMe = printMe + " " + entity.getEntityDescription().getContent();
		// HashMap<String, String> goSyns = findPresentation(
		// entity.getPresentation(), source1);
		// HashMap<String, String> nciSyns = findPresentation(
		// entity.getPresentation(), source2);
		//
		// if (nciSyns.size() > 0) {
		// Set<String> keys = nciSyns.keySet();
		// for (String key : keys) {
		// MapElement element = new MapElement(source1, sourceCode,
		// source2, key, MatchType.colocationMatch, mapType,
		// (float) (0.50));
		// maps.add(element);
		// }
		//
		// }
		// return maps;

	}

	private Vector<MapElement> parseEntity(Entity entity, String sourceCode,
			String source1, String source2, Mapping mapType, float score) {
		Vector<MapElement> maps = new Vector<MapElement>();
		String printMe = entity.getEntityCode();
		printMe = printMe + " " + entity.getEntityDescription().getContent();
		HashMap<String, String> goSyns = findPresentation(
				entity.getPresentation(), source1);
		HashMap<String, String> nciSyns = findPresentation(
				entity.getPresentation(), source2);

		if (nciSyns.size() > 0) {
			Set<String> keys = nciSyns.keySet();
			for (String key : keys) {
				MapElement element = new MapElement(source1, sourceCode,
						source2, key, MatchTypeEnum.colocationMatch, mapType,
 score);
				maps.add(element);
			}

		}
		return maps;

	}

	private Vector<MapElement> parseEntity(Entity entity, String sourceCode,
			String source1, String source2) {
		// TODO actually parse this and discover any NCIt codes. Create a
		// MapElement for each
		// Vector<MapElement> maps = new Vector<MapElement>();
		// String printMe = entity.getEntityCode();
		// printMe = printMe + " " + entity.getEntityDescription().getContent();
		// HashMap<String,String> goSyns =
		// findPresentation(entity.getPresentation(),
		// source1);
		// HashMap<String,String> nciSyns =
		// findPresentation(entity.getPresentation(),
		// source2);
		//
		// if (nciSyns.size() > 0) {
		// Set<String> keys = nciSyns.keySet();
		// for(String key:keys) {
		// MapElement element = new
		// MapElement(source1,entity.getEntityCode(),source2,key,MatchType.colocationMatch,Mapping.equivalent,(float)
		// (0.75));
		// maps.add(element);
		// }
		//
		// }
		// return maps;

		return parseEntity(entity, sourceCode, source1, source2,
				Mapping.equivalent);
	}

	private Vector<MapElement> parseEntity(Entity entity, String sourceCode,
			String source1, String source2, float score) {

		return parseEntity(entity, sourceCode, source1, source2,
				Mapping.equivalent, score);
	}

	private HashMap<String, String> findPresentation(
			Presentation[] presentation, String sourceString) {
		// Vector<String> sourcePresentations = new Vector<String>();
		HashMap<String, String> syns = new HashMap<String, String>();
		for (int i = 0; i < presentation.length; i++) {
			// String parsedPresentation = "";
			Presentation pres = presentation[i];
			Source[] source = pres.getSource();
			for (int j = 0; j < source.length; j++) {
				if (source[j].getContent().equals(sourceString)) {
					// parsedPresentation = parsedPresentation +
					// pres.getValue().getContent();
					PropertyQualifier[] quals = pres.getPropertyQualifier();
					for (int k = 0; k < quals.length; k++) {
						if (quals[k].getPropertyQualifierName().equals(
								"source-code")) {
							// parsedPresentation = parsedPresentation + " "
							// + quals[k].getValue().getContent() + " "
							// + pres.getValue().getContent();
							syns.put(quals[k].getValue().getContent(), pres
									.getValue().getContent());
						}
					}
				}
			}

			// if (parsedPresentation.length() > 0) {
			// sourcePresentations.add(parsedPresentation);
			// }
		}
		// return sourcePresentations;
		return syns;
	}

	public ResolvedConceptReferenceList searchForSourceCode(String sourceCode,
			String vocab) throws LBException, IndexOutOfBoundsException {

		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		csvt.setTag("PRODUCTION");

		CodedNodeSet nodeSet = evsService.getNodeSet("NCI Metathesaurus", csvt,
				null);

		// Tell the api that you want to get back only the PRESENTATION type
		// properties
		CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];
		types[0] = CodedNodeSet.PropertyType.PRESENTATION;

		// Now create a qualifier list containing the code you wish to search
		NameAndValueList qualifierList = new NameAndValueList();
		NameAndValue nv = new NameAndValue();
		nv.setName("source-code");

		nv.setContent(sourceCode);
		qualifierList.addNameAndValue(nv);

		// Specify the source code should come from the NCI source
		LocalNameList LnL = new LocalNameList();

		LnL.addEntry(vocab);

		nodeSet = nodeSet.restrictToProperties(null, types, LnL, null,
				qualifierList);

		return nodeSet.resolveToList(null, null,
 LnL,
				new PropertyType[] { PropertyType.PRESENTATION }, false, 5);

	}

}
