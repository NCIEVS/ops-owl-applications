package gov.nih.nci.evs;

import gov.nih.nci.camod.util.EvsTreeUtil;
import gov.nih.nci.camod.util.RemoteServerUtil;
import gov.nih.nci.evs.security.SecurityToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.MetadataPropertyList;
import org.LexGrid.LexBIG.DataModel.Collections.NCIChangeEventList;
import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.SystemReleaseList;
import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.types.CodingSchemeVersionStatus;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
import org.LexGrid.LexBIG.DataModel.NCIHistory.NCIChangeEvent;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.LexBIG.Exceptions.LBResourceUnavailableException;
import org.LexGrid.LexBIG.History.HistoryService;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeGraph;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.ActiveOption;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.SearchDesignationOption;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceMetadata;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.Utility.LBConstants;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Properties;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.commonTypes.Text;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService;
import org.LexGrid.lexevs.metabrowser.MetaBrowserService.Direction;
import org.LexGrid.lexevs.metabrowser.model.BySourceTabResults;
import org.LexGrid.naming.SupportedProperty;
import org.LexGrid.versions.CodingSchemeVersion;
import org.LexGrid.versions.SystemRelease;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;

public class testDataIntegrity {

	static LexEVSService lbSvc;
//	static LexEVSApplicationService lbSvc;
	static TreeMap<String, CodingScheme> codingSchemeMap = new TreeMap<String, CodingScheme>();
	static List<String> valueSetList = null;
	Vector<String> resolvedValueSets = new Vector<String>();

	private static boolean checkCodingSchemeMap() {
		// If the codingSchemeMap is empty, try to load it.
		try {
			if (codingSchemeMap.size() == 0) {
				getCodingSchemes();
			}
			// If it is still empty, then return false
			if (codingSchemeMap.size() == 0) {
				System.out.println("FAILURE Unable to load coding Scheme");
				// logger.warn("Unable to load coding Scheme");
				return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println("FAILURE Unable to load coding Scheme");
			// logger.warn("Unable to load coding Scheme");
			return false;
		}

	}

	private static boolean checkValueSetMap() {
		// If the codingSchemeMap is empty, try to load it.
		try {
			if (valueSetList == null || valueSetList.size() == 0) {
				getValueSets();
			}
			// If it is still empty, then return false
			if (valueSetList == null || valueSetList.size() == 0) {
				System.out.println("FAILURE : Unable to load Value Set");
				// logger.warn("Unable to load coding Scheme");
				return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println("FAILURE : Unable to load Value Set");
			// logger.warn("Unable to load coding Scheme");
			return false;
		}

	}

	private static void getCodingSchemes() throws LBInvocationException {
		HashMap<String, CodingScheme> tmpMap = new HashMap<String, CodingScheme>();
		CodingSchemeRenderingList csrl = lbSvc.getSupportedCodingSchemes();
		lbSvc = registerMeddraSecurityToken(lbSvc);
		CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
		for (CodingSchemeRendering csr : csrs) {
			Boolean isActive = csr.getRenderingDetail().getVersionStatus()
					.equals(CodingSchemeVersionStatus.ACTIVE);
			if (isActive != null && isActive.equals(Boolean.TRUE)) {
				CodingSchemeSummary css = csr.getCodingSchemeSummary();
				String formalName = css.getFormalName();
				String representsVersion = css.getRepresentsVersion();
				CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
				vt.setVersion(representsVersion);
				CodingScheme scheme = null;
				String nameTag = formalName + " " + representsVersion;

				try {
					scheme = lbSvc.resolveCodingScheme(formalName, vt);
					if (scheme != null) {
						tmpMap.put(nameTag, scheme);
					}
				} catch (Exception e) {
					// If there is no good formal name
					String uri = css.getCodingSchemeURI();
					try {
						scheme = lbSvc.resolveCodingScheme(uri, vt);
						if (scheme != null) {
							tmpMap.put(nameTag, scheme);
						}
					} catch (Exception e1) {
						System.out
						.println("FAILURE Could not resolve coding scheme "
								+ nameTag);
					}
				}
			}
		}

		codingSchemeMap = new TreeMap<String, CodingScheme>(tmpMap);
	}

	private static void getValueSets() {
		LexEVSValueSetDefinitionServices vsSvc = lbSvc
				.getLexEVSValueSetDefinitionServices();

		valueSetList = vsSvc.listValueSetDefinitionURIs();
	}

	private static Boolean isEntityInValueSet(
			LexEVSValueSetDefinitionServices vsd_service, String entityCode,
			URI entityCodeNamespace, URI valueSetDefinitionURI,
			String valueSetDefinitionRevisionId,
			AbsoluteCodingSchemeVersionReferenceList csVersionList,
			String versionTag) {

		try {
			AbsoluteCodingSchemeVersionReference acsvr = vsd_service
					.isEntityInValueSet(entityCode, entityCodeNamespace,
							valueSetDefinitionURI,
							valueSetDefinitionRevisionId, csVersionList,
							versionTag);

			if (acsvr != null)
				return Boolean.TRUE;
		} catch (Exception ex) {
			return Boolean.FALSE;
			// ex.printStackTrace();
		}

		return Boolean.FALSE;
	}

	private static List<String> listValueSetsWithEntityCode(String entityCode,
			String codeNamespaceURI, String version, String versionTag) {

		// System.out.println("entityCode: " + entityCode);
		// System.out.println("codeNamespaceURI: " + codeNamespaceURI);
		// System.out.println("version: " + version);
		// System.out.println("versionTag: " + versionTag);

		try {
			LexEVSValueSetDefinitionServices vsd_service = lbSvc
					.getLexEVSValueSetDefinitionServices();
			AbsoluteCodingSchemeVersionReferenceList csVersionList = new AbsoluteCodingSchemeVersionReferenceList();

			AbsoluteCodingSchemeVersionReference vAbsoluteCodingSchemeVersionReference = new AbsoluteCodingSchemeVersionReference();
			vAbsoluteCodingSchemeVersionReference
			.setCodingSchemeURN(codeNamespaceURI);
			vAbsoluteCodingSchemeVersionReference
			.setCodingSchemeVersion(version);
			csVersionList
			.addAbsoluteCodingSchemeVersionReference(vAbsoluteCodingSchemeVersionReference);

			List<String> vsd_uri_list = new ArrayList();
			String valueSetDefinitionRevisionId = null;
			List list = vsd_service.listValueSetDefinitionURIs();
			for (int i = 0; i < list.size(); i++) {
				String valueSetDefinitionURI = (String) list.get(i);
				int j = i + 1;
				System.out.println("(" + j + ") " + valueSetDefinitionURI);

				Boolean bool_obj = isEntityInValueSet(vsd_service, entityCode,
						new URI(codeNamespaceURI), new URI(
								valueSetDefinitionURI),
								valueSetDefinitionRevisionId, csVersionList, versionTag);

				if (bool_obj != null && bool_obj.equals(Boolean.TRUE)) {
					vsd_uri_list.add(valueSetDefinitionURI);
				}
			}
			return vsd_uri_list;

		} catch (Exception ex) {
			System.out.println("FAILURE : Error caching value sets");
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Read config file.
	 * 
	 * @param filename
	 *            the filename
	 * 
	 * @return the vector< string>
	 */
	private static Vector<String> readConfigFile(String filename) {
		Vector<String> v = new Vector<String>();
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
					v.add(line.trim());
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

	private static LexEVSService registerMeddraSecurityToken(
			LexEVSService lexevsService) {
		String token = "10382";

		lexevsService = registerSecurityToken(lexevsService, "MedDRA", token);
		lexevsService = registerSecurityToken(lexevsService, "MedDRA (Medical Dictionary for Regulatory Activities Terminology)", token);

		return lexevsService;
		
	}
	
	private static LexEVSApplicationService registerMeddraSecurityToken_2(
			LexEVSApplicationService lexevsService) {
		String token = "10382";

		lexevsService = registerSecurityToken_2(lexevsService, "MedDRA", token);
		lexevsService = registerSecurityToken_2(lexevsService, "MedDRA (Medical Dictionary for Regulatory Activities Terminology)", token);

		return lexevsService;
		
	}
	
	public static LexEVSApplicationService registerSecurityToken_2(
			LexEVSApplicationService lexevsService, String codingScheme, String token) {
		SecurityToken securityToken = new SecurityToken();
		securityToken.setAccessToken(token);
		Boolean retval = null;
		try {
			retval = lexevsService.registerSecurityToken(codingScheme,
					securityToken);
		} catch (Exception e) {
			System.out
			.println("FAILURE : Registration of SecurityToken failed.");
		}
		return lexevsService;
	}

	public static LexEVSService registerSecurityToken(
			LexEVSService lexevsService, String codingScheme, String token) {
		SecurityToken securityToken = new SecurityToken();
		securityToken.setAccessToken(token);
		Boolean retval = null;
		try {
			retval = lexevsService.registerSecurityToken(codingScheme,
					securityToken);
		} catch (Exception e) {
			System.out
			.println("FAILURE : Registration of SecurityToken failed.");
		}
		return lexevsService;
	}

	public testDataIntegrity(String configFilesLocation) {
		lbSvc = RemoteServerUtil.createLexBIGService();
//		lbSvc = RemoteServerUtil.createLexEVSApplicationService();
		resolvedValueSets = readConfigFile(configFilesLocation + "ResolvedValueSets.txt");
	}

	public testDataIntegrity(String address, String configFilesLocation) {
		lbSvc = RemoteServerUtil.createLexEVSService(address);
//		lbSvc = RemoteServerUtil.createLexEVSApplicationService(address);
//		System.out.println("Testing server " + address);
		resolvedValueSets = readConfigFile(configFilesLocation + "ResolvedValueSets.txt");
	}

	public void CacheValueSetDefinition_NCIt() {
		System.out.println("*************************************");
		System.out.println("*        Caching value sets         *");
		System.out.println("* Note: this may take an hour or so *");
		System.out.println("* 63923 takes a long time by itself *");
		System.out.println("*************************************");
		// TODO Find way to either pass in version or check all versions
		try {
			LexEVSValueSetDefinitionServices vsSvc = lbSvc
					.getLexEVSValueSetDefinitionServices();
			Vector<CodingScheme> schemes = new Vector<CodingScheme>();

			Set<String> keys = codingSchemeMap.keySet();
			for (String key : keys) {
				CodingScheme scheme = codingSchemeMap.get(key);
				if (scheme.getCodingSchemeName().equals("NCI_Thesaurus")) {
					// if (scheme.getIsActive()) {
					schemes.add(scheme);
					// }
				}
			}

			// String version = null;
			String versionTag = "PRODUCTION";
			CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
			csvt.setTag(versionTag);
			CodingScheme scheme = lbSvc.resolveCodingScheme("NCI_Thesaurus", csvt);
			

			String entityCode = "C48333";
			// String CodeNamespaceURI =
			// "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
			List<String> list;
//			for (CodingScheme scheme : schemes) {
				String namespace = scheme.getCodingSchemeURI();
				String version = scheme.getRepresentsVersion();
				
				list = listValueSetsWithEntityCode(entityCode, namespace,
						version, null);
				URI namespaceURI = new URI(namespace);
				AbsoluteCodingSchemeVersionReferenceList csVersionList = new AbsoluteCodingSchemeVersionReferenceList();
				AbsoluteCodingSchemeVersionReference vACSVR = new AbsoluteCodingSchemeVersionReference();
				vACSVR.setCodingSchemeURN(namespace);
				vACSVR.setCodingSchemeVersion(version);
				csVersionList.addAbsoluteCodingSchemeVersionReference(vACSVR);
				list = vsSvc.listValueSetsWithEntityCode(entityCode,
						namespaceURI, csVersionList, version);
				if (list.size() > 0) {
					System.out.println("Success: Caching completed for "
							+ scheme.getFormalName() + " " + version + " "
							+ list.size());

				} else {
					System.out.println("FAILURE : No list returned for "
							+ scheme.getFormalName() + " " + version);
				}

//			}

			// URI entityCodeNamespace = new URI(CodeNamespaceURI);
			// AbsoluteCodingSchemeVersionReferenceList csVersionList = new
			// AbsoluteCodingSchemeVersionReferenceList();
			// AbsoluteCodingSchemeVersionReference vACSVR = new
			// AbsoluteCodingSchemeVersionReference();
			// vACSVR.setCodingSchemeURN(CodeNamespaceURI);
			// vACSVR.setCodingSchemeVersion(version);
			// csVersionList.addAbsoluteCodingSchemeVersionReference(vACSVR);
			// list = vsSvc.listValueSetsWithEntityCode(entityCode,
			// entityCodeNamespace, csVersionList, null);

			// if (list.size() > 0) {
			// System.out
			// .println("Success: Caching completed. " + list.size());
			//
			// } else {
			// System.out.println("FAILURE : No list returned");
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cacheValeSetDefinition_NDFRT() {
		try {
			LexEVSValueSetDefinitionServices vsSvc = lbSvc
					.getLexEVSValueSetDefinitionServices();

			String version = "";
			String versionTag = "PRODUCTION";

			String entityCode = "N0000011165";
			String CodeNamespaceURI = "http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#";

			listValueSetsWithEntityCode(entityCode, CodeNamespaceURI, version,
					null);

			URI entityCodeNamespace = new URI(CodeNamespaceURI);
			AbsoluteCodingSchemeVersionReferenceList csVersionList = new AbsoluteCodingSchemeVersionReferenceList();
			AbsoluteCodingSchemeVersionReference vACSVR = new AbsoluteCodingSchemeVersionReference();
			vACSVR.setCodingSchemeURN(CodeNamespaceURI);
			vACSVR.setCodingSchemeVersion(version);
			csVersionList.addAbsoluteCodingSchemeVersionReference(vACSVR);
			List<String> list = vsSvc.listValueSetsWithEntityCode(entityCode,
					entityCodeNamespace, csVersionList, version);

			if (list.size() > 0) {
				System.out
				.println("Success: Caching completed. " + list.size());

			} else {
				System.out.println("FAILURE : No list returned");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int searchConcepts_A_He_Mouse(String vocabName, String vocabVersion) {
		String searchTerm = "A/He Mouse";
		// System.out.println("Search for " + searchTerm);
		// RemoteServerUtil rsu = new RemoteServerUtil();
		// LexEVSService appService = RemoteServerUtil.createLexBIGService();
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.exactMatch.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			// for (int i = 0; i < crl.getResolvedConceptReferenceCount(); i++)
			// {
			// Entity concept = crl.getResolvedConceptReference(i).getEntity();
			// System.out.println("Code: " + concept.getEntityCode()
			// + " PreferredName: "
			// + concept.getEntityDescription().getContent());
			// }
			return crl.getResolvedConceptReferenceCount();

		} catch (Exception ex) {
			System.out.println("searchConcepts_A_He_Mouse " + vocabName
					+ " and " + vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}

	public int searchConcepts_C9047(String vocabName, String vocabVersion) {
		String searchTerm = "C9047";
		// String searchTerm = "CDR0000037764";
		// System.out.println("Search for " + searchTerm);
		ConceptReference cref = new ConceptReference();
		cref.setConceptCode(searchTerm);
		ConceptReferenceList ncrl = new ConceptReferenceList();
		ncrl.addConceptReference(cref);
		// RemoteServerUtil rsu = new RemoteServerUtil();
		// LexEVSService appService = RemoteServerUtil.createLexBIGService();
		
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;

		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToCodes(ncrl);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			// for (int i = 0; i < crl.getResolvedConceptReferenceCount(); i++)
			// {
			// Entity concept = crl.getResolvedConceptReference(i).getEntity();
			// System.out.println("Code: " + concept.getEntityCode()
			// + " PreferredName: "
			// + concept.getEntityDescription().getContent());
			// }
			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_C9047 for " + vocabName
					+ " and " + vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}

	public int searchConcepts_FDA(String vocabName, String vocabVersion) {
		String searchTerm = "FDA";
		// System.out.println("Search for " + searchTerm);
		// RemoteServerUtil rsu = new RemoteServerUtil();
		// LexEVSService appService = RemoteServerUtil.createLexBIGService();
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			//			cvt.setVersion(vocabVersion);
			cvt.setTag("PRODUCTION");
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			// for (int i = 0; i < crl.getResolvedConceptReferenceCount(); i++)
			// {
			// ResolvedConceptReference rcr = crl
			// .getResolvedConceptReference(i);
			// Entity concept = rcr.getEntity();
			// if (concept != null) {
			// System.out.println("Code: " + concept.getEntityCode()
			// + " PreferredName: "
			// + concept.getEntityDescription().getContent());
			// } else {
			// System.out.println("Code: " + rcr.getCode()
			// + " PreferredName: " + rcr.getEntityDescription());
			// }
			// }
			return crl.getResolvedConceptReferenceCount();

		} catch (Exception ex) {
			System.out.println("searchConcepts_FDA " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}

	public int searchConcepts_green(String vocabName, String vocabVersion) {
		String searchTerm = "green";
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_gene " + vocabName + " and "
					+ vocabVersion + " throws Exception " + ex);
			ex.printStackTrace();
		}
		return 0;
	}
	
	
	public int searchConcepts_gene(String vocabName, String vocabVersion) {
		String searchTerm = "gene";
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_gene " + vocabName + " and "
					+ vocabVersion + " throws Exception " + ex);
			ex.printStackTrace();
		}
		return 0;
	}

	public int searchConcepts_oral(String vocabName, String vocabVersion) {
		String searchTerm = "oral";
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_oral " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}

	public int searchConcepts_to(String vocabName, String vocabVersion) {
		String searchTerm = "to";
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_to " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}
	
	public int searchConcepts_Number(String vocabName, String vocabVersion) {
		String searchTerm = "Number";
		
		
		//TODO remove this when testing done
//				lbSvc = registerMeddraSecurityToken_2(lbSvc);
				
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return 0;
		}

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.contains.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_Number " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}
	
	public int searchConcepts_name(String vocabName, String vocabVersion) {
		String searchTerm = "name";
		System.out
		.println("******************************************************");
		System.out
		.println("-------------Search Concepts for name-----------------");
		System.out
		.println("Test successfull if all lines have more than 0 objects");
		System.out
		.println("******************************************************");

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setTag(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.exactMatch.name(), null);
			nodes = nodes.restrictToStatus(ActiveOption.ALL, null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);
			Vector<String> definitions = new Vector<String>();
			for(int i=0; i<crl.getResolvedConceptReferenceCount();i++){
				Entity concept = crl.getResolvedConceptReference(i).getEntity();
				Definition[] defs = concept.getDefinition();
				for (int j=0;j < defs.length; j++){
					Definition tempDef = defs[j];
					String defText = tempDef.getValue().getContent();
					definitions.add(concept.getEntityCode()+ " " + tempDef.getSource(0).getContent() + " " +   defText);
				}
			}
			
			for (String def:definitions){
				System.out.println(def);
			}
			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_name " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}
	
	public int searchConcepts_blood(String vocabName, String vocabVersion) {
		String searchTerm = "blood";
		System.out
		.println("******************************************************");
		System.out
		.println("-------------Search Concepts for blood----------------");
		System.out
		.println("Test successfull if all lines have more than 0 objects");
		System.out
		.println("******************************************************");
		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setTag(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.exactMatch.name(), null);

			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			return crl.getResolvedConceptReferenceCount();
		} catch (Exception ex) {
			System.out.println("searchConcepts_name " + vocabName + " and "
					+ vocabVersion + " throws Exception = " + ex);
		}
		return 0;
	}
	
	
	
	public void testCodingSchemeMetadata() {
		boolean isValid = checkCodingSchemeMap();
		if (!isValid) {
			System.out.println("Unable to load coding Scheme");
			return;
		}
		System.out
		.println("******************************************************");
		System.out
		.println("--------------Coding Scheme Metadata------------------");
		System.out
		.println("Test successfull if all lines have more than 0 objects");
		System.out
		.println("******************************************************");
		Set<String> codingSchemeKeys = codingSchemeMap.keySet();
		for (String key : codingSchemeKeys) {
			CodingScheme scheme = codingSchemeMap.get(key);
			String codingSchemeName = scheme.getCodingSchemeName();
			String version = scheme.getRepresentsVersion();
			try {
				LexBIGServiceMetadata lbsm = lbSvc.getServiceMetadata();
				lbsm = lbsm.restrictToCodingScheme(Constructors
						.createAbsoluteCodingSchemeVersionReference(
								scheme.getCodingSchemeURI(), version));
				MetadataPropertyList mdpl = lbsm.resolve();
				int metadatacount = mdpl.getMetadataPropertyCount();
				if (metadatacount > 0) {
					System.out.println("SUCCESS " + codingSchemeName + " "
							+ version + " metadata objects "
							+ mdpl.getMetadataPropertyCount());
				} else if (!resolvedValueSets.contains(codingSchemeName.trim())){
					
					System.out.println("FAILURE ! Metadata not found for "
							+ codingSchemeName + " " + version);
				}
				// System.out.println("*********************");
			} catch (Exception e) {
				System.out.println("FAILURE : Metadata search generated error");
			}

		}
	}



	public void testHierarchy() {

		String scheme = "NCI Thesaurus";
		System.out.println("***********************");
		System.out.println("Testing hierarchy for NCIt");
		System.out.println("***********************");
		String code = "C2870";
		ConceptReference cr = ConvenienceMethods.createConceptReference(code,
				scheme);

		// Perform the query ...
		NameAndValue nv1 = new NameAndValue();
		NameAndValueList nvList = new NameAndValueList();
		nv1.setName("Disease_Has_Associated_Anatomic_Site");
		nvList.addNameAndValue(nv1);
		NameAndValue nv2 = new NameAndValue();
		nv2.setName("Gene_Associated_With_Disease");
		nvList.addNameAndValue(nv2);
		NameAndValue nv3 = new NameAndValue();
		nv3.setName("subClassOf");
		nvList.addNameAndValue(nv3);
		NameAndValue nv4 = new NameAndValue();
		nv4.setName("R100");
		nvList.addNameAndValue(nv4);

		CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
		vt.setTag("PRODUCTION");

		try {
			// Relationships To
			// System.out.println("Get relationships To " + code);
			ResolvedConceptReferenceList matches = lbSvc
					.getNodeGraph(scheme, vt, null)
					.restrictToAssociations(nvList, null)
					.resolveAsList(cr, false, true, 1, 1, new LocalNameList(),
							null, null, 1024);

			if (matches.getResolvedConceptReference().length > 0) {
				ResolvedConceptReference ref = matches
						.enumerateResolvedConceptReference().nextElement();
int count = ref.getTargetOf().getAssociationCount();
if (count > 1){
				System.out.println("Success : Relationships found to " + code);
}
				// Print the associations
				// AssociationList targetof = ref.getTargetOf();
				// if (targetof != null) {
				// Association[] associations = targetof.getAssociation();
				// for (Association assoc : associations) {
				// AssociatedConcept[] acl = assoc.getAssociatedConcepts()
				// .getAssociatedConcept();
				// for (AssociatedConcept ac : acl) {
				// EntityDescription ed = ac.getEntityDescription();
				// System.out.println("\t\t"
				// + ac.getConceptCode()
				// + "/"
				// + (ed == null ? "**No Description**" : ed
				// .getContent()));
				// }
				// }
				// }
			} else {
				System.out.println("FAILURE : No relationships found to "
						+ code);
			}

			// Rlationships From
			// System.out.println("Get relationships From " + code);
			matches = lbSvc
					.getNodeGraph(scheme, vt, null)
					.restrictToAssociations(nvList, null)
					.resolveAsList(cr, true, false, 1, 1, new LocalNameList(),
							null, null, 1024);

			if (matches.getResolvedConceptReference().length > 0) {
				ResolvedConceptReference ref = matches
						.enumerateResolvedConceptReference().nextElement();
				int count = ref.getSourceOf().getAssociationCount();
				if (count > 1){
				System.out
				.println("Success : Relationships found from " + code);}
				// Print the associations
				// AssociationList sourceof = ref.getSourceOf();
				// if (sourceof != null) {
				// Association[] associations = sourceof.getAssociation();
				// for (Association assoc : associations) {
				// AssociatedConcept[] acl = assoc.getAssociatedConcepts()
				// .getAssociatedConcept();
				// for (AssociatedConcept ac : acl) {
				// EntityDescription ed = ac.getEntityDescription();
				// System.out.println("\t\t"
				// + ac.getConceptCode()
				// + "/"
				// + (ed == null ? "**No Description**" : ed
				// .getContent()));
				// }
				// }
				// }
			} else {
				System.out.println("FAILURE : No relationships found from "
						+ code);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testHistory() {
		String NCIt = "NCI Thesaurus";
		String NCIm = "NCI Metathesaurus";
		try {
			System.out.println("***********************");
			System.out.println("Testing History");
			System.out.println("***********************");
			HistoryService hs = lbSvc.getHistoryService(NCIt);

			// Test getBaselines
			// System.out.println("List all baselines");
			SystemReleaseList srl = hs.getBaselines(null, null);
			if (srl.getSystemReleaseCount() > 10) {
				System.out
				.println("Success : NCI Thesaurus history baselines retrieved");
			} else {
				System.out
				.println("FAILURE : NCI Thesaurus history baselines not found");
			}

			String hold = srl.getSystemRelease(0).getReleaseURI();
			hold = srl.getSystemRelease(28).getReleaseURI();
			SystemRelease sr = hs.getEarliestBaseline();
			hold = sr.getEntityDescription().getContent();
			long time = sr.getReleaseDate().getTime();
			CodingSchemeVersion csv = hs.getConceptCreationVersion(Constructors.createConceptReference("C49239", null));
			hold = csv.getReleaseURN();
			hold = csv.getVersion();
			time = csv.getVersionDate().getTime();
			hold = csv.getEntityDescription().getContent();

			// Test get edit actions
			// System.out.println("***********************");
			// System.out.println("Edit actions ");
			NCIChangeEventList ncel = hs.getEditActionList(
					Constructors.createConceptReference("C7696", null), null,
					null);
			if (ncel.getEntryCount() > 5) {
				System.out
				.println("Success : NCI Thesaurus history edits retrieved");
			} else {
				System.out
				.println("FAILURE : NCI Thesaurus history edits not found");
			}


			// System.out.println("****************************");
			// System.out.println("Testing NCI Metathesaurus History");
			// System.out.println("****************************");
			hs = lbSvc.getHistoryService(NCIm);
			// System.out.println("List all baselines");
			srl = hs.getBaselines(null, null);
			if (srl.getSystemReleaseCount() > 10) {
				System.out
				.println("Success : Metathesaurus history baselines retrieved");
			} else {
				System.out
				.println("FAILURE : MetaThesaurus history baselines not found");
			}
			// System.out.println(ObjectToString.toString(hs.getBaselines(null,
			// null)));
			// System.out.println("***********************");
			// System.out.println("Edit actions ");

			ncel = hs.getEditActionList(
					Constructors.createConceptReference("C0359583", null),
					null, null);
			if (ncel.getEntryCount() > 0) {
				System.out
				.println("Success : Metathesaurus history edits retrieved");
			} else {
				System.out
				.println("FAILURE : Metathesaurus history edits not found");
			}

				
				String[] ret_info = new String[2];
				
				try {
					

					Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2005-01-01");
					Date endDate = new Date();
					NCIChangeEventList cel = hs.getEditActionList(Constructors.createConceptReference("CL225712", null),startDate,endDate);
					cel.getEntryCount();
					Iterator<NCIChangeEvent> celIter = (Iterator<NCIChangeEvent>) cel.iterateEntry();
					while (celIter.hasNext()){
						NCIChangeEvent ce = celIter.next();
						if (ce.getEditaction().name().equals("RETIRE")){
							ret_info[0] = ce.getEditDate().toLocaleString();
							ret_info[1] = ce.getReferencecode();
							//System.out.println("Retirement date "+ ret_info[0]);
							//System.out.println("Reference concept " + ret_info[1]);
						} else if (ce.getEditaction().name().equals("MERGE")){
							ret_info[0] = ce.getEditDate().toLocaleString();
							ret_info[1] = ce.getReferencecode();
						}
					}
				} catch (Exception ex) {
//					_logger.error("getRetirementDate for " + conCode + "  throws Exception = " + ex.toString());
					System.out.println("getRetirementDate throws Exception = " + ex);
				}

		    

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testListCodingSchemes() {
		System.out.println("************************************************");
		System.out.println("-------------List of CodingSchemes--------------");
		System.out.println("Successful if each vocab has more than 0 concepts");
		System.out.println("NOTE: Mappings will have 0 concepts");
		System.out.println("************************************************");
		boolean isValid = checkCodingSchemeMap();
		if (!isValid)
			return;
		Set<String> codingSchemeKeys = codingSchemeMap.keySet();

		for (String key : codingSchemeKeys) {

			CodingScheme scheme = codingSchemeMap.get(key);
			if(scheme.getApproxNumConcepts()!=null){
 			long numberConcepts = scheme.getApproxNumConcepts();
			if (numberConcepts > 0) {
				System.out.println("Success Name: "
						+ scheme.getCodingSchemeName() + " Version: "
						+ scheme.getRepresentsVersion()
						+ " Approximate # Concepts: "
						+ scheme.getApproxNumConcepts().toString());
			} else {
				if (scheme.getCodingSchemeName().toUpperCase().contains("_TO_")) {
					System.out.println("Success Map: "
							+ scheme.getCodingSchemeName() + " Version: "
							+ scheme.getRepresentsVersion()
							+ " Approximate # Concepts: "
							+ scheme.getApproxNumConcepts().toString());
				} else {
					System.out.println("FAILURE ! No concepts in "
							+ scheme.getCodingSchemeName() + " "
							+ scheme.getRepresentsVersion());
				}
			}

			} else {
				System.out.println("Failure "							
			                + scheme.getCodingSchemeName() + " Version: "
							+ scheme.getRepresentsVersion()
							+ " Approximate # Concepts: NULL");
			}
		}
			

	}

	public void testListValueSets() {

		System.out.println("***********************");
		System.out.println("Testing Value Set List");
		System.out.println("***********************");
		boolean isValid = checkValueSetMap();
		if (!isValid) {
			System.out.println("FAILURE: Could not retrieve value sets");
			return;
		}
		if (valueSetList.size() > 100) {
			System.out.println("Success: Value Set list retrieved");
		} else {
			System.out.println("FAILURE : Value Set list not retrieved");
		}
		// LexEVSValueSetDefinitionServices vsSvc = lbSvc
		// .getLexEVSValueSetDefinitionServices();
		// System.out.println("***************************");
		// System.out.println("---List of ValueSets---");
		// System.out.println("***************************");
		// try {
		// for (String vsUri : valueSetList) {
		// System.out.println("vsURI " + vsUri);
		// ValueSetDefinition vsd = vsSvc.getValueSetDefinition(
		// URI.create(vsUri), "");
		// System.out.println("vsName " + vsd.getValueSetDefinitionName());
		// System.out.println("************");
		// }
		// } catch (Exception e) {
		// System.out
		// .println("FAILURE : Problem retrieving list of  value sets");
		// }

	}
	
	
	public void testSupportedProperties() {
		try{
			System.out.println("***************************************************");
			System.out
			.println("------------------Supported Property Search-------------");
			System.out.println("Successful if found.");
			System.out
			.println("***************************************************");
			CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
			vt.setTag("PRODUCTION");
			
			CodingScheme scheme = lbSvc.resolveCodingScheme(
					"NCI MetaThesaurus", vt);
			
			Enumeration<? extends SupportedProperty> props =  scheme.getMappings().enumerateSupportedProperty();
			if (props.hasMoreElements()){
				System.out.println("SUCCESS - properties listed");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testLocalNames(String filename) {
		try {
			System.out
			.println("***************************************************");
			System.out
			.println("------------------Local Name Search-------------");
			System.out.println("Successful if found.");
			System.out
			.println("***************************************************");
			CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
			vt.setTag("PRODUCTION");
			registerMeddraSecurityToken(lbSvc);
			// CodingScheme scheme = null;
			Vector<String> LocalNames = readConfigFile(filename);
			for (int i = 0; i < LocalNames.size(); i++) {
				String[] tmpHold = LocalNames.get(i).split("\\|");
				String codingSchemeName = tmpHold[0].trim();
				String checkLocalName = tmpHold[1].trim();
				try {
					CodingScheme scheme = lbSvc.resolveCodingScheme(
							codingSchemeName, vt);

					if (scheme == null) {
						System.out
						.println("FAILURE : Failed to get vocabulary "
								+ codingSchemeName);
						return;
					}
					String[] localNames = scheme.getLocalName();
					
					boolean nameFound = false;
					for (String localName : localNames) {
						if (localName.equals(checkLocalName)) {
							nameFound = true;
						}
					}
					if (nameFound) {
						System.out.println("Success: Local Name "
								+ checkLocalName + " found in "
								+ codingSchemeName);
					} else {
						System.out.println("FAILURE ! Local name "
								+ checkLocalName + " not found in "
								+ codingSchemeName);
					}
				} catch (Exception e) {
					System.out.println("FAILURE : Coding Scheme not found "
							+ codingSchemeName);
				}
			}
		} catch (Exception e) {
			System.out
			.println("FAILURE LocalNames.txt not found or unreadable");
		}
	}

	public void testName_and_Synonym() {
		System.out.println("*************************");
		System.out.println("* Name & Synonym Search *");
		System.out.println("*************************");
		EvsTreeUtil treeUtil = new EvsTreeUtil(lbSvc);

		String code = "C12434";
		String description = treeUtil.getEVSPreferedDescription(code);
		String details = treeUtil.getConceptDetails(code);
		if (description.length() > 0) {
			System.out
			.println("Success: Name retrieval successful for " + code);
		} else {
			System.out.println("FAILURE : Name retrieval failure for " + code);
		}
		if (details.length() > 0) {
			System.out.println("Success: Synonym retrieval successful for "
					+ code);
		} else {
			System.out.println("FAILURE : Name retrieval failure for " + code);
		}
		// System.out.println(descriptionNCI);
		// System.out.println(detailsNCI);

		code = "ZFA:0100000";
		description = treeUtil.getEVSPreferedDescription(code);
		details = treeUtil.getConceptDetails(code);
		if (description.length() > 0) {
			System.out
			.println("Success: Name retrieval successful for " + code);
		} else {
			System.out.println("FAILURE : Name retrieval failure for " + code);
		}
		if (details.length() > 0) {
			System.out.println("Success: Synonym retrieval successful for "
					+ code);
		} else {
			System.out.println("FAILURE : Name retrieval failure for " + code);
		}
		// System.out.println(description);
		// System.out.println(details);
	}

	public void testQuickSearch() {
		boolean isValid = checkCodingSchemeMap();
		if (!isValid) {
			System.out.println("Unable to load coding Scheme");
			return;
		}
		System.out
		.println("***************************************************");
		System.out
		.println("------------------Coding Scheme Search-------------");
		System.out
		.println("Successful if all lines return more than 0 concepts");
		System.out
		.println("***************************************************");
		Set<String> codingSchemeKeys = codingSchemeMap.keySet();
		for (String key : codingSchemeKeys) {
			CodingScheme scheme = codingSchemeMap.get(key);
			String codingSchemeName = scheme.getCodingSchemeName();
			String codingSchemeVersion = scheme.getRepresentsVersion();
			int returnCount = 0;
			if (!resolvedValueSets.contains(codingSchemeName.trim())){
			// System.out.println(codingSchemeName + " " + codingSchemeVersion);
			returnCount = searchConcepts_gene(codingSchemeName,
					codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_oral(codingSchemeName, codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_C9047(codingSchemeName,
							codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_A_He_Mouse(codingSchemeName,
							codingSchemeVersion);
			returnCount = returnCount + searchConcepts_green(codingSchemeName, codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_FDA(codingSchemeName, codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_to(codingSchemeName, codingSchemeVersion);
			returnCount = returnCount
					+ searchConcepts_Number(codingSchemeName, codingSchemeVersion);
			if (returnCount > 0) {
				System.out.println("Success " + codingSchemeName + " "
						+ codingSchemeVersion + " concepts returned: "
						+ returnCount);
			} else {
				System.out.println("FAILURE " + codingSchemeName
						+ " returned no results");

			}
		}}

	}

	public void testSearchDoubleMetaphone() {
		System.out
		.println("************************************");
		System.out
		.println("-----------Metaphone Search--------");
		System.out
		.println("Successful if results returned");
		System.out
		.println("************************************");
		String searchTerm = "breast canser";
		System.out.println("Search for " + searchTerm);
		ConceptReference cref = new ConceptReference();
		cref.setConceptCode(searchTerm);
		ConceptReferenceList ncrl = new ConceptReferenceList();
		ncrl.addConceptReference(cref);

		//TODO remove this when testing done
//		lbSvc = registerMeddraSecurityToken_2(lbSvc);
		
		
		lbSvc = registerMeddraSecurityToken(lbSvc);
		if (lbSvc == null) {
			System.out.println("lbSvc == null???");
			return;
		}

		String vocabTag = "PRODUCTION";
		String vocabName = "NCI Metathesaurus";
		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();

			cvt.setTag(vocabTag);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);

			nodes = nodes.restrictToMatchingDesignations(searchTerm,
					SearchDesignationOption.ALL,
					LBConstants.MatchAlgorithms.DoubleMetaphoneLuceneQuery
					.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);

			if (crl.getResolvedConceptReferenceCount() > 10) {
				System.out.println("Success: Metaphone results returned");
			} else {
				System.out.println("FAILURE : No metaphone results returned");
			}

		} catch (Exception ex) {
			System.out.println("testsearchDoubleMetaphone throws Exception = "
					+ ex);
		}
	}

	public void testSearchValueSetForTerm() {
		System.out.println("*****************************");
		System.out.println("* Search Value Set for Term *");
		System.out.println("*****************************");
		LexEVSValueSetDefinitionServices vsSvc = lbSvc
				.getLexEVSValueSetDefinitionServices();
		String valueSet = "http://evs.nci.nih.gov.valueset/C54577";
		String matchText = "probe";

		// String valueSet = "http://ncit:C100110";
		// String matchText = "Opioid";

		try {
			ResolvedValueSetCodedNodeSet rvs_cns = vsSvc
					.getValueSetDefinitionEntitiesForTerm(matchText,
							LBConstants.MatchAlgorithms.contains.name(),
							new URI(valueSet), null, null);

			CodedNodeSet cns = null;
			AbsoluteCodingSchemeVersionReferenceList csvrList = null;
			ResolvedConceptReferenceList rcrList = null;

			if (rvs_cns != null) {
				cns = rvs_cns.getCodedNodeSet();
				csvrList = rvs_cns.getCodingSchemeVersionRefList();
				rcrList = cns
						.resolveToList(null, null, null, null, false, 1024);
				if (rcrList.getResolvedConceptReferenceCount() > 1) {
					System.out.println("Success: Search value set " + valueSet
							+ " for term " + matchText);
				}
			} else {
				System.out
				.println("FAILURE : No results returned in search for term "
						+ matchText + "in valueSet " + valueSet);
			}
		} catch (URISyntaxException e) {
			System.out.println("Bad path to service");
		} catch (Exception e) {

			System.out.println("FAILURE : Error in search value set for term "
					+ matchText + " in valueSet " + valueSet);
			e.printStackTrace();
			System.out.println();
		}
	}

	public void testSearchValueSetForCode() {
		System.out.println("*****************************");
		System.out.println("* Search Value Set for Code *");
		System.out.println("*****************************");
		LexEVSValueSetDefinitionServices vsSvc = lbSvc
				.getLexEVSValueSetDefinitionServices();
		String valueSet = "http://ncit:C54577";
		String matchText = "C50300";
		// System.out.println("Search Value Set " + valueSet + " for term "
		// + matchText);

		try {
			ResolvedValueSetCodedNodeSet rvs_cns = vsSvc
					.getValueSetDefinitionEntitiesForTerm(matchText,
							LBConstants.MatchAlgorithms.exactMatch.name(),
							new URI(valueSet), null, null);

			
			
			CodedNodeSet cns = null;
			AbsoluteCodingSchemeVersionReferenceList csvrList = null;
			ResolvedConceptReferenceList rcrList = null;

			if (rvs_cns != null) {
				cns = rvs_cns.getCodedNodeSet();
				csvrList = rvs_cns.getCodingSchemeVersionRefList();
				rcrList = cns
						.resolveToList(null, null, null, null, false, 1024);
				if (rcrList.getResolvedConceptReferenceCount() > 1) {
					System.out.println("Success: Search value set " + valueSet
							+ " for term " + matchText);
				}
			} else {
				System.out
				.println("FAILURE : No results returned in search for term "
						+ matchText + "in valueSet " + valueSet);
			}
		} catch (URISyntaxException e) {
			System.out.println("Bad path to service");
		} catch (Exception e) {

			System.out.println("FAILURE : Error in search value set for term "
					+ matchText + " in valueSet " + valueSet);
			e.printStackTrace();
			System.out.println();
		}
	}

	public void testValueSetsWithEntityCode() {

		LexEVSValueSetDefinitionServices vsSvc = lbSvc
				.getLexEVSValueSetDefinitionServices();
		// System.out.println("****************");
		String matchCode = "C50300";
		String codingSchemeURI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
		// String versionTag = "PRODUCTION";

		try {

			// URI entityCodeNamespace = new URI(codingSchemeURI);
			AbsoluteCodingSchemeVersionReferenceList incsvrl = new AbsoluteCodingSchemeVersionReferenceList();

			Set<String> codingSchemeKeys = codingSchemeMap.keySet();
			for (String key : codingSchemeKeys) {
				CodingScheme scheme = codingSchemeMap.get(key);
				if (scheme.getCodingSchemeName().equals("NCI_Thesaurus")) {
					incsvrl.addAbsoluteCodingSchemeVersionReference(Constructors
							.createAbsoluteCodingSchemeVersionReference(
									scheme.getCodingSchemeURI(),
									scheme.getRepresentsVersion()));
					System.out.println("Adding " + scheme.getCodingSchemeName()
							+ " " + scheme.getCodingSchemeURI() + " "
							+ scheme.getRepresentsVersion());
				}

			}
//
//			AbsoluteCodingSchemeVersionReference vACSVR = incsvrl
//					.getAbsoluteCodingSchemeVersionReference(0);

			// CacheValueSetDefinition_NCIt(versionTag);

			System.out.println("******************************************");
			System.out.println("*       Search Value Set for Code        *");
			System.out.println("* Note: this test may take an hour or so *");
			System.out.println("******************************************");
			// vACSVR.setCodingSchemeURN(codingSchemeURI);
			// incsvrl.addAbsoluteCodingSchemeVersionReference(vACSVR);

			// AbsoluteCodingSchemeVersionReferenceList incsvrl = new
			// AbsoluteCodingSchemeVersionReferenceList();
			//
			// incsvrl.addAbsoluteCodingSchemeVersionReference(Constructors
			// .createAbsoluteCodingSchemeVersionReference(
			// scheme.getCodingSchemeURI(),
			// "12.02d"));

//			URI entityCodeNamespace = new URI("NCI_Thesaurus");
			URI entityCodeNamespace = new URI(codingSchemeURI);
			List<String> uris = vsSvc.listValueSetsWithEntityCode(matchCode,
					null, incsvrl, null);

			// AbsoluteCodingSchemeVersionReferenceList incsvrl = new
			// AbsoluteCodingSchemeVersionReferenceList();
			// incsvrl.addAbsoluteCodingSchemeVersionReference(Constructors
			// .createAbsoluteCodingSchemeVersionReference(
			// "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#",
			// "12.01f"));
			// System.out.println("This method needs fixed badly");
			//
			// System.out.println("List value sets containing " + matchCode);
			// List<String> uris = vsSvc.listValueSetsWithEntityCode(matchCode,
			// null, incsvrl, null);
			if (uris.size() >= 2) {
				// System.out.println("Success: List values sets containing code "
				// + matchCode + " " + versionTag);
				System.out.println("Success: List values sets containing code "
						+ matchCode);
			} else {
				// System.out
				// .println("FAILURE : Could not search value sets for code "
				// + matchCode + " " + versionTag);
				System.out
				.println("FAILURE : Could not search value sets for code "
						+ matchCode);
			}

			// for (String uri : uris) {
			// System.out.println(uri);
			// }
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FAILURE : Error in search value sets for code");
		}
	}

	public void getTheCUIs() {
		// Calls up NCIt and queries for the UMLS_CUI and NCI_META_CUI
		// properties

		String searchTerm = "C12435";
		String vocabName = "NCI_Thesaurus";
		String vocabVersion = "12.02d";
		ConceptReference cref = new ConceptReference();
		cref.setConceptCode(searchTerm);
		ConceptReferenceList ncrl = new ConceptReferenceList();
		ncrl.addConceptReference(cref);

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToCodes(ncrl);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);
			Entity concept = crl.getResolvedConceptReference(0).getEntity();
			Property[] properties = concept.getAllProperties();
			for (Property prop : properties) {
				if (prop.getPropertyName().equals("UMLS_CUI")
						|| prop.getPropertyName().equals("NCI_META_CUI")) {
					String cui = prop.getValue().getContent();
					System.out.println(cui);
				}
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void getBySource() {
		System.out
		.println("***************************************************");
		System.out
		.println("*               Search Meta by Source             *");
		System.out
		.println("* Note: If this hangs, it means index not present *");
		System.out
		.println("***************************************************");
		String CUI = "C0332221";
		String sab = "SNOMEDCT";
		try {
			Map<String, List<BySourceTabResults>> map = null;
			MetaBrowserService mbs = (MetaBrowserService) lbSvc
					.getGenericExtension("metabrowser-extension");
			map = mbs.getBySourceTabDisplay(CUI, sab, null, Direction.SOURCEOF);
			if (map.size() > 0) {
				System.out
				.println("Success: Results returned from Metathesaurus SOURCEOF search 1");
			}

			String sab2 = "NCI";
			map = mbs
					.getBySourceTabDisplay(CUI, sab2, null, Direction.SOURCEOF);
			if (map.size() > 0) {
				System.out
				.println("Success: Results returned from Metathesaurus SOURCEOF search 2");
			}
		} catch (Exception e) {
			System.out.println("FAILURE: MetaBrowser source tab");

		}
	}

	public void testTransitiveClosure(){
		/*
		 * Check for all descendants of a given concept
		 *         
		 */

		System.out.println("***********************");
		System.out.println("Testing transitive closure");
		System.out.println("***********************");
		String scheme = "NCI Thesaurus";
		String code = "C20181";
		ConceptReference cr = ConvenienceMethods.createConceptReference(code,
				scheme);

		CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
		vt.setTag("PRODUCTION");

		try {
			NameAndValueList nvList = Constructors.createNameAndValueList("subClassOf");
			CodedNodeGraph cng =  lbSvc.getNodeGraph(scheme, vt, null);
			cng.restrictToAssociations(nvList, null);

			CodedNodeSet nodeset = cng.toNodeList(ConvenienceMethods.createConceptReference(code, scheme), false, true,-1, -1);


			CodedNodeSet.AnonymousOption restrictToAnonymous = CodedNodeSet.AnonymousOption.NON_ANONYMOUS_ONLY;
			nodeset = nodeset.restrictToAnonymous(restrictToAnonymous);

			ResolvedConceptReferencesIterator iterator = nodeset.resolve(null, null,  null,null, false);
			while (iterator.hasNext()){
				ResolvedConceptReference rcr = iterator.next();
				String codeReturn = rcr.getCode();
				System.out.println(codeReturn);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getTransitiveClosure() {
		//Kim's version
		ResolvedConceptReferencesIterator iterator = null;
		try {
			String codingSchemeName = "NCI Thesaurus";
			String code = "C20181";
			String associationName = "subClassOf";
			boolean resolveForward = false;
			boolean excludeAnonymous = true;

			CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
			csvt.setTag("PRODUCTION");

			ConceptReference graphFocus = new ConceptReference();
			graphFocus.setConceptCode(code);

			CodedNodeGraph cng = lbSvc.getNodeGraph(codingSchemeName, csvt, null);
			NameAndValueList asso_list =
					Constructors.createNameAndValueList(new String[] { associationName }, null);
			cng = cng.restrictToAssociations(asso_list, null);

			boolean resolveBackward = false;
			if (!resolveForward) {
				resolveBackward = true;
			}

			int resolveAssociationDepth = -1;
			int maxReturns = -1;

			CodedNodeSet cns = cng.toNodeList(graphFocus, resolveForward, resolveBackward,
					resolveAssociationDepth, maxReturns);

			if (excludeAnonymous) {
				CodedNodeSet.AnonymousOption restrictToAnonymous = CodedNodeSet.AnonymousOption.NON_ANONYMOUS_ONLY;
				cns = cns.restrictToAnonymous(restrictToAnonymous);
			}

			iterator = cns.resolve(null, null, null, null, false);
			while (iterator.hasNext()){
				ResolvedConceptReference rcr = iterator.next();
				String codeReturn = rcr.getCode();
				System.out.println(codeReturn);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void getDomainAndRangeOfRole(){
		//        LexBIGService lbs = LexBIGServiceImpl.defaultInstance();
		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		//        csvt.setVersion(css.getRepresentsVersion());
		csvt.setTag("PRODUCTION");
		try {
			LocalNameList lcllist = Constructors.createLocalNameList("association");
			PropertyType[] types = {PropertyType.GENERIC};
			CodedNodeSet cns = lbSvc.getNodeSet("NCI Thesaurus", csvt, lcllist);
			cns.restrictToMatchingProperties(null, types, "Role_Has_Range", "LuceneQuery", null);
			ResolvedConceptReferencesIterator nodeRefs = cns.resolve(null, null, null, null, true);
			while (nodeRefs.hasNext()) {
				ResolvedConceptReference rcr = nodeRefs.next();
				System.out.println("Code: " + rcr.getConceptCode());
				System.out.println("\tCoding Scheme Name...: " + rcr.getCodingSchemeName());
				System.out.println("\tCoding Scheme URI....: " + rcr.getCodingSchemeURI());
				System.out.println("\tCoding Scheme Version: " + rcr.getCodingSchemeVersion());
				System.out.println("\tCode Namespace...... : "
						+ (rcr.getCodeNamespace() != null ? rcr.getCodeNamespace() : "<default>"));
				System.out.println("\tCode Description.... : "
						+ (rcr.getEntityDescription() != null ? rcr.getEntityDescription().getContent() : ""));
				System.out.println("");
				System.out.println("\t Properties");
				for(Property p: rcr.getEntity().getAllProperties()){
					System.out.println("\tPropertyName: " + p.getPropertyName());
					System.out.println("\tValue:  " + p.getValue().getContent());
				}
				String typeString = "";
				for (Iterator<? extends String> types1 = rcr.iterateEntityType(); types1.hasNext();)
					typeString += (types1.next() + (types1.hasNext() ? "," : ""));
				System.out.println("\tCode Entity Types... : " + typeString);

			}
		} catch (LBInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBResourceUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}

	public void testFullSynBySource(){
		try {
						String searchTerm = "gene";		
						
						CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
						//        csvt.setVersion(css.getRepresentsVersion());
						csvt.setTag("PRODUCTION");
			CodedNodeSet nodeSet = lbSvc.getNodeSet("HUBt", csvt, null);

			//Tell the api that you want to get back only the PRESENTATION type properties                   
			CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];                
			types[0] = CodedNodeSet.PropertyType.PRESENTATION;

			LocalNameList sourceLnL = new LocalNameList();
			sourceLnL.addEntry("CAHUB");
			
			LocalNameList propLnL = new LocalNameList();
			propLnL.addEntry("FULL_SYN");

			nodeSet = nodeSet.restrictToMatchingProperties(propLnL,types,sourceLnL,null, null,searchTerm,LBConstants.MatchAlgorithms.contains.name(),null);

			ResolvedConceptReferenceList rcl = nodeSet.resolveToList(null, null, null, 100);
			int count = rcl.getResolvedConceptReferenceCount();

			for (int i=0; i<rcl.getResolvedConceptReferenceCount();i++){
				ResolvedConceptReference rcr = rcl.getResolvedConceptReference(i);
				Entity entity = rcr.getReferencedEntry();
				Presentation[] presProps = entity.getPresentation();
				for(int y=0;y<presProps.length;y++){
					Presentation pres = presProps[y];
					if(pres.getPropertyName().equals("FULL_SYN")&& pres.getRepresentationalForm().equals("PT") && pres.getSource(0).getContent().equals("CAHUB")){
					System.out.println(pres.getValue().getContent());					
					}
				}
				
				Definition[] defs  = entity.getDefinition();
				for(int y=0;y<defs.length;y++){
					Definition def = defs[y];
					if (def.getPropertyName().equals("DEFINITION")&&def.getSource(0).getContent().equals("NCI")){
						System.out.println(def.getValue().getContent());
					}
				}
			}

		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void test_caHUB() throws LBException{

		String scheme= "HUBt";
		String tag = "PRODUCTION";
		
		Set<String> results = new HashSet<String>();
//		LexBIGService lbs = getLexBIGService();
		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		csvt.setTag(tag);
		//		if (versionP) {
		//		    csvt.setVersion(version);
		//		} else {
		//		    csvt.setTag(version);
		//		}

		CodedNodeSet cns = lbSvc.getCodingSchemeConcepts(scheme, csvt);

		// constrain: concept property 'caHUB_Study' is non null
		cns = cns.restrictToProperties
				(ConvenienceMethods.createLocalNameList
						(new String[] {"caHUB_Study" }), null);

		// now get the concepts
		ResolvedConceptReferenceList matches
		= cns.resolveToList(null, null, null, 800);

		// Analyze the result ...
		int ln = matches.getResolvedConceptReferenceCount();
		System.out.println("Number of concepts received: " + ln);
		if (ln > 0) {
			for (int jj = 0; jj < ln; jj++) {
				ResolvedConceptReference cr
				= matches.getResolvedConceptReference(jj);
				Entity node = cr.getEntity();
				results.add(cr.getEntity().getEntityCode());
				printEntityProps(node);
			}
		}
		//		return results;

		for (String code:results){
			System.out.println("Code: " + code);
		}
	}

	public void printEntityProps(Entity node) {
		Property[] props = node.getAllProperties();
		String propName;
		String propVal;

		Property[] props2 = node.getProperty();
		
		// print regular properties
		for (int i = 0; i < props.length; i++) {
			Property prop = props[i];
			propName = prop.getPropertyName();
			System.out.println(new StringBuffer()
			.append( propName)
			.append(": ")
			.append(prop.getValue().getContent()).toString());

			// pull any qualifiers.
			PropertyQualifier[] pQuals = prop.getPropertyQualifier();
			System.out.println(prop.getPropertyType());
			if (pQuals != null) {
				for (int j = 0; j < pQuals.length; j++) {
					PropertyQualifier pQual = pQuals[j];
					System.out.println("    Qual: " +
							pQual.getPropertyQualifierName()
							+ " : "  + pQual.getValue().getContent());


				}
			}
		}}





	public void testCTR_queries(){
		String evsScheme = "NDF-RT";
		String sdtmValue = "Thiazolidinedione";
		CodingSchemeVersionOrTag tag = new CodingSchemeVersionOrTag();
        CodedNodeSet codeNodeSet = null;
        LocalNameList entityTypes = new LocalNameList();
    	entityTypes.addEntry("concept");
    	
    	try{
    		LexBIGService evsService = lbSvc;
        codeNodeSet = evsService.getNodeSet(evsScheme, tag,entityTypes);
        if (codeNodeSet != null) {
        	codeNodeSet =  codeNodeSet.restrictToMatchingDesignations(sdtmValue,
                    null, LBConstants.MatchAlgorithms.exactMatch.name(), null);
        }
        ResolvedConceptReferenceList matches
	    = codeNodeSet.resolveToList(null, null, null, 100);
		int ln = matches.getResolvedConceptReferenceCount();
		System.out.println("Number of concepts received: " + ln);
		if (ln > 0) {
		    for (int jj = 0; jj < ln; jj++) {
			ResolvedConceptReference cr
			    = matches.getResolvedConceptReference(jj);
			Entity node = cr.getEntity();
			System.out.println(cr.getEntity().getEntityCode());
		    }
		}
    	
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}

	} 
	
	
	public void testDefinitionSource(){

		String searchTerm = "C12435";
		String vocabName = "NCI_Thesaurus";
		String vocabTag = "PRODUCTION";
		ConceptReference cref = new ConceptReference();
		cref.setConceptCode(searchTerm);
		ConceptReferenceList ncrl = new ConceptReferenceList();
		ncrl.addConceptReference(cref);

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setTag(vocabTag);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToCodes(ncrl);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);
			//codes are unique in NCIt, so you will only get one entity in the list
			Entity concept = crl.getResolvedConceptReference(0).getEntity();
			Definition[] defs = concept.getDefinition();
			for (Definition def : defs) {
				//Each definition in NCIt will only have one source.
				Source[] sources = def.getSource();
				Source defSource = sources[0];
				String source = defSource.getContent();
				System.out.println("Definition source "+ source);
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void testVersion(){

		String searchTerm = "C12435";
		String vocabName = "NCI_Thesaurus";
		String vocabVersion = "17.02d";
		ConceptReference cref = new ConceptReference();
		cref.setConceptCode(searchTerm);
		ConceptReferenceList ncrl = new ConceptReferenceList();
		ncrl.addConceptReference(cref);

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToCodes(ncrl);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);
			//codes are unique in NCIt, so you will only get one entity in the list
			Entity concept = crl.getResolvedConceptReference(0).getEntity();
			Definition[] defs = concept.getDefinition();
			for (Definition def : defs) {
				//Each definition in NCIt will only have one source.
				Source[] sources = def.getSource();
				Source defSource = sources[0];
				String source = defSource.getContent();
				System.out.println("Definition source "+ source);
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
	public void test_utf8() {
		String scheme= "Glass.owl";
		String tag = "PRODUCTION";
		Set<String> results = new HashSet<String>();
//		LexBIGService lbs = getLexBIGService();
		CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
		csvt.setTag(tag);


		try {
			CodedNodeSet cns = lbSvc.getCodingSchemeConcepts(scheme, csvt);
			// now get the concepts
			ResolvedConceptReferenceList matches
			= cns.resolveToList(null, null, null, 800);
			int ln = matches.getResolvedConceptReferenceCount();
			System.out.println("Number of concepts received: " + ln);
			if (ln > 0) {
				for (int jj = 0; jj < ln; jj++) {
					ResolvedConceptReference cr
					= matches.getResolvedConceptReference(jj);
					Entity node = cr.getEntity();
					results.add(cr.getEntity().getEntityCode());
					printEntityProps(node);
				}
			}
			
			
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        LocalNameList entityTypes = new LocalNameList();
    	entityTypes.addEntry("concept");
    	CodedNodeSet codeNodeSet = null;
    	String sdtmValue = "我能吞下玻璃而不伤身体。";
//    	String sdtmValue = "glass";
    	PropertyType[] types = {PropertyType.GENERIC};
    	try {
			codeNodeSet = lbSvc.getNodeSet(scheme, csvt,entityTypes);
	        if (codeNodeSet != null) {
//	        	codeNodeSet =  codeNodeSet.restrictToMatchingDesignations(sdtmValue,
//	                    null, LBConstants.MatchAlgorithms.contains.name(), null);
				LocalNameList propLnL = new LocalNameList();
				propLnL.addEntry("I_Can_Eat_Glass");
	        	codeNodeSet = codeNodeSet.restrictToMatchingProperties(propLnL,types,null,null, null,sdtmValue,LBConstants.MatchAlgorithms.contains.name(),null);
	            ResolvedConceptReferenceList matches
	    	    = codeNodeSet.resolveToList(null, null, null, 100);
	    		int ln = matches.getResolvedConceptReferenceCount();
	    		System.out.println("Number of concepts received: " + ln);
	    		if (ln > 0) {
	    		    for (int jj = 0; jj < ln; jj++) {
	    			ResolvedConceptReference cr
	    			    = matches.getResolvedConceptReference(jj);
	    			Entity node = cr.getEntity();
	    			System.out.println(cr.getEntity().getEntityCode());
	    		    }
	    		}
	        }
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}

	public void testMetathesaurusMapping(){
		CodedNodeSet nodeSet;
		try {
			nodeSet = lbSvc.getNodeSet("NCI MetaThesaurus", null, null);

		 
		//Tell the api that you want to get back only the PRESENTATION type properties                   
		CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];                
		types[0] = CodedNodeSet.PropertyType.PRESENTATION;
		 
		//Now create a qualifier list containing the code you wish to search                                          
		NameAndValueList qualifierList = new NameAndValueList();                  
		NameAndValue nv = new NameAndValue();                  
		nv.setName("source-code");
		nv.setContent("RID1543");                
		qualifierList.addNameAndValue(nv);
		 
		 
		nodeSet = nodeSet.restrictToProperties(null,types,null,null, qualifierList);
		ResolvedConceptReferenceList rcrl = nodeSet.resolveToList(null, null, null, 10);
		Vector<String> metaCUIs = new Vector<String>();
		for(int i=0; i< rcrl.getResolvedConceptReferenceCount();i++){
			ResolvedConceptReference rcr = rcrl.getResolvedConceptReference(i);
			String metaCUI = rcr.getCode();
			metaCUIs.add(metaCUI);
		}

		LocalNameList lnl = new LocalNameList();
		lnl.addEntry("UMLS_CUI");
		lnl.addEntry("NCI_META_CUI");
		for(String metaCUI:metaCUIs){
			boolean found = matchAttributeValue(lnl,metaCUI);
		}
		
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testCodingSchemeSearchability() {
		boolean isValid = checkCodingSchemeMap();
		if (!isValid) {
			System.out.println("Unable to load coding Scheme");
			return;
		}
		System.out
		.println("***************************************************");
		System.out
		.println("------------------Coding Scheme Searchability-------------");
		System.out
		.println("Successful if all lines have a version");
		System.out
		.println("***************************************************");
		Set<String> codingSchemeKeys = codingSchemeMap.keySet();
		for (String key : codingSchemeKeys) {
			CodingScheme scheme = codingSchemeMap.get(key);
			String codingSchemeName = scheme.getCodingSchemeName();
			String codingSchemeVersion = scheme.getRepresentsVersion();

			if (codingSchemeVersion !=null && codingSchemeVersion.length() > 0) {
				System.out.println("Success " + codingSchemeName + " "
						+ codingSchemeVersion 
						);
			} else {
				System.out.println("FAILURE " + codingSchemeName
						+ codingSchemeVersion);

			}
		}

	}
	
	
	private boolean matchAttributeValue(LocalNameList lnl, String value){
		try {
			CodedNodeSet cns = lbSvc.getCodingSchemeConcepts("NCI_Thesaurus", null);
			CodedNodeSet matches = cns.restrictToMatchingProperties(lnl, null, value,LBConstants.MatchAlgorithms.contains.name() , null);
			int count = matches.resolveToList(null, null, null, 0).getResolvedConceptReferenceCount();
			if (count>0) return true;
			return false;
		} catch (LBException e) {
			return false;
		}
	}
}
