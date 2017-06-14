package gov.hhs.fda.ctr.etl.meddra;

import java.util.HashSet;
import java.util.Set;

import org.LexGrid.LexBIG.DataModel.Collections.AssociatedConceptList;
import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBResourceUnavailableException;
import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.SearchDesignationOption;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.naming.Mappings;
import org.LexGrid.naming.SupportedHierarchy;

/**
 * 
 * @author marwahah
 * 
 */
public class MedDRAHeirarchyCache {

	private final static LocalNameList PROPERTY_RESTRICTION = new LocalNameList();
	public static final String PRIMARY_SOC_PROPERTY_NAME = "PRIMARY_SOC";

	static {
		PROPERTY_RESTRICTION.addEntry("PT_IN_VERSION");
	}

	public static String[] getHierarchyIDs(LexBIGService evsService,
	        String codingScheme, CodingSchemeVersionOrTag versionOrTag)
	        throws LBException {
		String[] hier = null;

		Set<String> ids = new HashSet<String>();
		SupportedHierarchy[] sh = null;
		try {
			sh = getSupportedHierarchies(evsService, codingScheme, versionOrTag);
			if (sh != null) {
				for (SupportedHierarchy element : sh) {
					ids.add(element.getLocalId());
				}

				// Cache and return the new value ...
				hier = ids.toArray(new String[ids.size()]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hier;
	}

	protected static SupportedHierarchy[] getSupportedHierarchies(
	        LexBIGService evsService, String codingScheme,
	        CodingSchemeVersionOrTag versionOrTag) throws LBException {
		// getLogger().logMethod(new Object[] {codingScheme, versionOrTag,});

		CodingScheme cs = evsService.resolveCodingScheme(codingScheme,
		        versionOrTag);
		if (cs == null) {
			throw new LBResourceUnavailableException(
			        "Coding scheme not found -- " + codingScheme);
		}
		Mappings mappings = cs.getMappings();
		return mappings.getSupportedHierarchy();
	}

	protected void printChain(Association assoc, int depth) throws Exception {
		StringBuffer indent = new StringBuffer();
		for (int i = 0; i <= depth; i++)
			indent.append("    ");
		AssociatedConceptList concepts = assoc.getAssociatedConcepts();
		for (int i = 0; i < concepts.getAssociatedConceptCount(); i++) {
			// Print focus of this branch ...
			AssociatedConcept concept = concepts.getAssociatedConcept(i);
			System.out
			        .println(new StringBuffer(indent)
			                .append(assoc.getAssociationName())
			                .append("->")
			                .append(concept.getConceptCode())
			                .append(':')
			                .append(concept.getEntityDescription() == null ? "NO DESCRIPTION"
			                        : concept.getEntityDescription()
			                                .getContent()).toString());
			// Find and recurse printing for next batch ...
			AssociationList nextLevel = concept.getSourceOf();
			if (nextLevel != null && nextLevel.getAssociationCount() != 0) {
				for (int j = 0; j < nextLevel.getAssociationCount(); j++) {
					printChain(nextLevel.getAssociation(j), depth + 1);
				}
			}
		}
	}

	protected MedDRAHeirarchy getMedDRAHeirarchyForLLT(Association assoc,
	        MedDRAHeirarchy heirarchy) throws Exception {
		AssociatedConceptList concepts = assoc.getAssociatedConcepts();
		for (int i = 0; i < concepts.getAssociatedConceptCount(); i++) {
			AssociatedConcept concept = concepts.getAssociatedConcept(i);
			// We have the PT and PTCD
			heirarchy.setAEPT(concept.getEntityDescription().getContent());
			heirarchy.setAEPTCD(concept.getConceptCode());
			Property[] properties = concept.getEntity().getProperty();
			String conceptSOCCD = "";
			for (Property prop : properties) {
				if (prop.getPropertyName().equals(PRIMARY_SOC_PROPERTY_NAME)) {
					System.out.println(prop.getPropertyName() + ":"
					        + prop.getValue().getContent());
					conceptSOCCD = prop.getValue().getContent();
				}
			}
			AssociationList nextLevel = concept.getSourceOf();
			if (nextLevel != null && nextLevel.getAssociationCount() != 0) {
				for (int j = 0; j < nextLevel.getAssociationCount(); j++) {
					heirarchy = getMedDRAHeirarchy(nextLevel.getAssociation(j),
					        heirarchy, 0);
					if (heirarchy.getAESOCCD().equalsIgnoreCase(conceptSOCCD)) {
						// heirarchy is found
						break;
					}
				}
			}
		}
		return heirarchy;
	}

	protected MedDRAHeirarchy getMedDRAHeirarchy(Association assoc,
	        MedDRAHeirarchy heirarchy, int depth) throws Exception {
		AssociatedConceptList concepts = assoc.getAssociatedConcepts();
		for (int i = 0; i < concepts.getAssociatedConceptCount(); i++) {
			AssociatedConcept concept = concepts.getAssociatedConcept(i);
			if (depth == 0) {
				// We have the HLT and HLTCD
				heirarchy.setAEHLT(concept.getEntityDescription().getContent());
				heirarchy.setAEHLTCD(concept.getConceptCode());
			} else if (depth == 1) {
				// We have the HLGT and HLGTCD
				heirarchy
				        .setAEHLGT(concept.getEntityDescription().getContent());
				heirarchy.setAEHLGTCD(concept.getConceptCode());
			} else if (depth == 2) {
				// We have SOC
				heirarchy.setAESOCCD(concept.getConceptCode());
				heirarchy.setAESOC(concept.getEntityDescription().getContent());
				return heirarchy;
			}
			// Get the parent (in case depth==0 or 1
			AssociationList nextLevel = concept.getSourceOf();
			if (nextLevel != null && nextLevel.getAssociationCount() != 0) {
				for (int j = 0; j < nextLevel.getAssociationCount(); j++) {
					heirarchy = getMedDRAHeirarchy(nextLevel.getAssociation(j),
					        heirarchy, depth + 1);

				}
			}
		}
		return heirarchy;
	}

	public MedDRAHeirarchy getMedDRAHeirarchy(String AETERM, String version) {
		MedDRAHeirarchy heirarchy = null;
		try {
			LexBIGService evsService = Service.getInstance();
			String license = Service.EVS_MEDDRA_ID;
			Service.register(Service.EVS_MEDDRA_SCHEME, license);
			/*
			 * ModuleDescriptionList mdl = evsService.getMatchAlgorithms(); for
			 * (int k=0;k<mdl.getModuleDescriptionCount();k++) {
			 * ModuleDescription md = mdl.getModuleDescription(k);
			 * System.out.println(md.getName()); }
			 */

			heirarchy = new MedDRAHeirarchy();
			LexBIGServiceConvenienceMethods lbscm = (LexBIGServiceConvenienceMethods) evsService
			        .getGenericExtension("LexBIGServiceConvenienceMethods");
			CodingSchemeVersionOrTag tag = new CodingSchemeVersionOrTag();
			tag.setVersion(version);

			CodingScheme cs = evsService.resolveCodingScheme("MedDRA", tag);
			System.out.println(cs.getCodingSchemeURI());
			System.out.println(cs.getRepresentsVersion());
			CodingScheme scheme = evsService.resolveCodingScheme(
			        Service.EVS_MEDDRA_SCHEME, tag);
			Mappings schemeMappings = scheme.getMappings();
			SupportedHierarchy[] heirarchies = schemeMappings
			        .getSupportedHierarchy();
			String medDRAHeirarchyId = heirarchies[0].getLocalId();
			LocalNameList typeList = new LocalNameList();
			typeList.addEntry("concept");
			CodedNodeSet codeNodeSet = evsService.getNodeSet(
			        Service.EVS_MEDDRA_SCHEME, tag, typeList);
			codeNodeSet = codeNodeSet.restrictToMatchingDesignations(AETERM,
			        SearchDesignationOption.PREFERRED_ONLY, "exactMatch", null);
			ResolvedConceptReferenceList concepts = codeNodeSet.resolveToList(
			        null, null, null, 1);
			for (ResolvedConceptReference concept : concepts
			        .getResolvedConceptReference()) {
				CodingSchemeVersionOrTag ver = new CodingSchemeVersionOrTag();
				ver.setVersion(cs.getRepresentsVersion());
				AssociationList associations = lbscm
				        .getHierarchyPathToRoot(
				                "MedDRA",
				                ver,
				                medDRAHeirarchyId,
				                concept.getConceptCode(),
				                false,
				                LexBIGServiceConvenienceMethods.HierarchyPathResolveOption.ALL,
				                null);
				// AssociationList associations = concept.getTargetOf();
				// AssociationList associations =
				// lbscm.getHierarchyLevelNext("urn:oid:2.16.840.1.113883.6.163",
				// null, "is_a", concept.getConceptCode(), true, null);

				// Association[] assocs = associations.getAssociation();
				// System.out.println(assocs[0].getAssociationName());

				String LLT = concept.getEntityDescription().getContent()
				        .toUpperCase();
				String LLTCD = concept.getConceptCode();
				heirarchy.addAELLT(LLT, LLTCD);

				heirarchy = getMedDRAHeirarchyForLLT(
				        associations.getAssociation(0), heirarchy);
				System.out.println(heirarchy);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return heirarchy;
	}

	public void cacheMedDRAHeirarchy(String version) {
		try {
			LexBIGService evsService = Service.getInstance();
			String license = Service.EVS_MEDDRA_ID;
			Service.register(Service.EVS_MEDDRA_SCHEME, license);

			LexBIGServiceConvenienceMethods lbscm = (LexBIGServiceConvenienceMethods) evsService
			        .getGenericExtension("LexBIGServiceConvenienceMethods");
			CodingSchemeVersionOrTag tag = new CodingSchemeVersionOrTag();
			CodedNodeSet termSearch = evsService.getCodingSchemeConcepts(
			        Service.EVS_MEDDRA_SCHEME, tag).restrictToProperties(
			        PROPERTY_RESTRICTION,
			        new PropertyType[] { PropertyType.GENERIC });
			tag.setVersion(version);
			// String schemeName = "2.16.840.1.113883.6.163"; //MedDRA
			// String schemeName =
			// "MedDRA (Medical Dictionary for Regulatory Activities Terminology)";
			CodingScheme scheme = evsService.resolveCodingScheme(
			        Service.EVS_MEDDRA_SCHEME, tag);
			Mappings schemeMappings = scheme.getMappings();
			SupportedHierarchy[] heirarchies = schemeMappings
			        .getSupportedHierarchy();
			String medDRAHeirarchyId = heirarchies[0].getLocalId();
			ResolvedConceptReferencesIterator termIterator = termSearch
			        .resolve(null, null, null);
			while (termIterator.hasNext()) {
				ResolvedConceptReferenceList concepts = termIterator.next(-1);

				for (ResolvedConceptReference concept : concepts
				        .getResolvedConceptReference()) {
					CodingSchemeVersionOrTag ver = new CodingSchemeVersionOrTag();
					ver.setVersion(version);
					AssociationList associations = lbscm
					        .getHierarchyPathToRoot(
					                Service.EVS_MEDDRA_SCHEME,
					                ver,
					                medDRAHeirarchyId,
					                concept.getConceptCode(),
					                false,
					                LexBIGServiceConvenienceMethods.HierarchyPathResolveOption.ALL,
					                null);
					// AssociationList associations = concept.getTargetOf();

					MedDRAHeirarchy heirarchy = new MedDRAHeirarchy();
					heirarchy.setAEPT(concept.getEntityDescription()
					        .getContent().toUpperCase());
					heirarchy.setAEPTCD(concept.getConceptCode());
					Property[] properties = concept.getEntity().getProperty();
					String conceptSOCCD = "";
					for (Property prop : properties) {
						if (prop.getPropertyName().equals(
						        PRIMARY_SOC_PROPERTY_NAME)) {
							System.out.println(prop.getPropertyName() + ":"
							        + prop.getValue().getContent());
							conceptSOCCD = prop.getValue().getContent();
						}
					}
					for (int j = 0; j < associations.getAssociationCount(); j++) {
						Association association = associations
						        .getAssociation(j);
						heirarchy = getMedDRAHeirarchy(association, heirarchy,
						        0);
						if (heirarchy.getAESOCCD().equalsIgnoreCase(
						        conceptSOCCD)) {
							// heirarchy is found
							break;
						}
					}
					AssociationList childAssocs = lbscm.getHierarchyLevelNext(
					        Service.EVS_MEDDRA_SCHEME, ver, medDRAHeirarchyId,
					        concept.getConceptCode(), false, null);
					;
					// Add the the Lower terms
					if (childAssocs != null) {
						for (int j = 0; j < childAssocs.getAssociationCount(); j++) {
							Association assoc = childAssocs.getAssociation(j);
							AssociatedConceptList sourceConcepts = assoc
							        .getAssociatedConcepts();
							for (int i = 0; i < sourceConcepts
							        .getAssociatedConceptCount(); i++) {
								AssociatedConcept sourceConcept = sourceConcepts
								        .getAssociatedConcept(i);
								String LLT = sourceConcept
								        .getEntityDescription().getContent();
								String LLTCD = sourceConcept.getConceptCode();
								heirarchy.addAELLT(LLT, LLTCD);
							}
						}
					}
					System.out.println(heirarchy);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

}
