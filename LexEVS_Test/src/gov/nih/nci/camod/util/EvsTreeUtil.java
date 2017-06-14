/**
 *  @author georgeda 
 *  
 *  $Id: EvsTreeUtil.java,v 1.13 2008/08/14 06:27:33 schroedn Exp $  
 *  
 *  $Log: EvsTreeUtil.java,v $
 *  Revision 1.13  2008/08/14 06:27:33  schroedn
 *  Check for null first
 *
 *  Revision 1.12  2008/01/15 19:31:28  pandyas
 *  Modified debug statements to build to dev tier
 *
 *  Revision 1.11  2008/01/14 21:04:56  pandyas
 *  Enabled logging for dev tier instability issue testing
 *
 *  Revision 1.10  2008/01/14 17:17:48  pandyas
 *  Added to dev instance to look at get Preferred Description error iwth caCORE
 *
 *  Revision 1.9  2007/08/27 15:38:08  pandyas
 *  hide debug code printout
 *
 *  Revision 1.8  2007/08/23 16:11:50  pandyas
 *  Removed extra code
 *
 *  Revision 1.7  2007/08/14 17:05:02  pandyas
 *  Bug #8414:  getEVSPreferredDiscription needs to be implemented for Zebrafish vocabulary source
 *
 *  Revision 1.6  2007/08/14 12:03:59  pandyas
 *  Implementing EVSPreferredName for Zebrafish models
 *
 *  Revision 1.5  2006/08/17 17:59:34  pandyas
 *  Defect# 410: Externalize properties files - Code changes to get properties
 *
 *  Revision 1.4  2006/04/21 13:42:12  georgeda
 *  Cleanup
 *
 *  Revision 1.3  2005/11/03 21:47:56  georgeda
 *  Changed EVS api
 *
 *  Revision 1.2  2005/09/22 13:04:31  georgeda
 *  Added app server call
 *
 *  Revision 1.1  2005/09/21 20:34:59  georgeda
 *  Create util for fetching/caching EVS data
 *
 *  
 */
package gov.nih.nci.camod.util;

import gov.nih.nci.camod.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.concepts.Entity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * Static helper class for caching EVS values.
 */
public class EvsTreeUtil {

	/** The Constant log. */
	static private final Log log = LogFactory.getLog(EvsTreeUtil.class);

	/** The our descriptions. */
	static private Map<String, String> ourDescriptions = new HashMap<String, String>();

	/** The Constant _service. */
	private static final String _service = "EvsServiceInfo";

	private static LexEVSService appService;

	/**
	 * Instantiates a new evs tree util.
	 */
	public EvsTreeUtil() {
	}

	public EvsTreeUtil(LexEVSService inAppService) {
		appService = inAppService;
	}

	/**
	 * Get a preferred name based on a concept code. Will return the cached
	 * value if it has been fetched before.
	 * 
	 * @param inConceptCode
	 *            the concept code to get the preferred name for.
	 * 
	 * @return the preferred name, or an empty string if something goes wrong.
	 */
	public synchronized String getEVSPreferedDescription(String inConceptCode) {
		log.debug("Entering getEVSPreferedDescription");

		String theDescription = "";
		String EVSTreeNameSpace = "";
		String DisplayNameTag = "";

		if (ourDescriptions.containsKey(inConceptCode)) {
			theDescription = ourDescriptions.get(inConceptCode);
		} else {
			try {
				log.debug("inConceptCode: " + inConceptCode);

				// Define parameters for Zebrafish namespace
				// Maybe a better way to do this, but I didn't want to send in
				// HttpServletRequest everywhere
				if (inConceptCode != null) {
					if (inConceptCode.contains("ZFA:")) {
						log.debug("Zebrafish modelSpecies");

						EVSTreeNameSpace = Constants.Evs.ZEBRAFISH_NAMESPACE;
						// DisplayNameTag =
						// Constants.Evs.DISPLAY_NAME_TAG_LOWER_CASE;
						DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG_ZF;
						// Define parameters for all NCI_Thesaurus namespace
					} else {
						log.debug("NOT Zebrafish modelSpecies");
						EVSTreeNameSpace = Constants.Evs.NAMESPACE;
						DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG;
					}
				}

				log.debug("EVSTreeNameSpace: " + EVSTreeNameSpace);

				LexEVSService theAppService = appService;
				log.debug("theAppService: " + theAppService.toString());
				// SecurityToken nullToken = new SecurityToken();
				// theAppService.registerSecurityToken("META", nullToken);

				// EVSQuery theConceptNameQuery = new EVSQueryImpl();
				// theConceptNameQuery.getConceptNameByCode(EVSTreeNameSpace,
				// inConceptCode);

				CodedNodeSet cns = theAppService.getCodingSchemeConcepts(
				        EVSTreeNameSpace, null);
				// First create a ConceptReferenceList to describe the Concept
				// to search for.
				// In this example we use the helper class 'ConvenienceMethods'.
				ConceptReferenceList crefs = ConvenienceMethods
				        .createConceptReferenceList(
				                new String[] { inConceptCode },
				                EVSTreeNameSpace);

				// Next, restrice the CodedNodeSet.
				cns.restrictToCodes(crefs);

				ResolvedConceptReferenceList matches = cns.resolveToList(null,
				        null, null, 1);

				// List theConceptNames = theAppService
				// .evsSearch(theConceptNameQuery);

				// Should only be one
				if (matches.getResolvedConceptReferenceCount() > 0) {
					String theDisplayName = matches
					        .getResolvedConceptReference(0)
					        .getEntityDescription().toString();

					ResolvedConceptReference cref = matches
					        .getResolvedConceptReference(0);
					Property[] props = cref.getEntity().getAllProperties();
					for (Property prop : props) {
						if (prop.getPropertyName().equals(DisplayNameTag)) {
							theDescription = prop.getValue().getContent()
							        .toString();
							break;
						}
					}
					ourDescriptions.put(inConceptCode, theDescription);
					// EVSQuery theDisplayNameQuery = new EVSQueryImpl();
					// theDisplayNameQuery.getPropertyValues(Constants.Evs.NAMESPACE,
					// theDisplayName, Constants.Evs.DISPLAY_NAME_TAG);
					// theDisplayNameQuery.getPropertyValues(EVSTreeNameSpace,
					// theDisplayName, DisplayNameTag);
					// theDisplayNameQuery.getPropertyValues(EVSTreeNameSpace,
					// inConceptCode, DisplayNameTag);
					// // Should only be one
					// List theDisplayNameList = theAppService
					// .evsSearch(theDisplayNameQuery);
					// log.debug("theDisplayNameList.size: "
					// + theDisplayNameList.size());

					// if (theDisplayNameList.size() > 0) {
					// theDescription = (String) theDisplayNameList.get(0);
					// log.debug("theDescription: " + theDescription);
					//
					// // Cache for next time
					// ourDescriptions.put(inConceptCode, theDescription);
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception getting preferred description: ", e);
			}
		}
		log.debug("Exiting getEVSPreferedDescription");

		return theDescription;
	}

	public String getConceptDetails(String code) {
		log.debug("EvsTreeUtil.getConceptDetails Entered: ");
		String scheme = "";
		String theDescription = "";

		if (code != null) {
			if (code.contains("ZFA")) {
				log.debug("Zebrafish modelSpecies");
				scheme = Constants.Evs.ZEBRAFISH_SCHEMA;
				// DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG_LOWER_CASE;
				// Define parameters for all NCI_Thesaurus schema
			} else {
				log.debug("NOT Zebrafish modelSpecies");
				scheme = Constants.Evs.NCI_SCHEMA;
				// DisplayNameTag = Constants.Evs.DISPLAY_NAME_TAG;
			}
		}

		Entity ce = getConceptByCode(scheme, null, null, code);
		if (ce == null) {
			log.info("Concept not found -- " + code);
		} else {
			log.info("Concept found -- " + code);
			log.debug("Concept log.debug+ ce.getEntityDescription().getContent()");

			int num_properties = 0;

			Property[] properties = ce.getPresentation();
			num_properties = num_properties + properties.length;

			theDescription = outputPropertyDetails(properties);
			log.debug("\n theDescription: " + theDescription);

			log.debug("\nTotal number of properties: " + num_properties
			        + "\n\n");
		}
		return theDescription;
	}

	public Entity getConceptByCode(String codingSchemeName, String vers,
	        String ltag, String code) {
		try {
			LexEVSService lbSvc = getAppService();
			// RemoteServerUtil rsu = new RemoteServerUtil();
			// LexEVSService lbSvc = rsu.createLexBIGService();
			if (lbSvc == null) {
				System.out.println("lbSvc == null???");
				return null;
			}

			CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
			versionOrTag.setVersion(vers);

			ConceptReferenceList crefs = createConceptReferenceList(
			        new String[] { code }, codingSchemeName);

			CodedNodeSet cns = null;

			cns = lbSvc.getCodingSchemeConcepts(codingSchemeName, versionOrTag);

			cns = cns.restrictToCodes(crefs);
			ResolvedConceptReferenceList matches = cns.resolveToList(null,
			        null, null, 1);

			if (matches == null) {
				System.out.println("Concept not found.");
				return null;
			}

			// Analyze the result ...
			if (matches.getResolvedConceptReferenceCount() > 0) {
				ResolvedConceptReference ref = matches
				        .enumerateResolvedConceptReference().nextElement();

				Entity entry = ref.getReferencedEntry();
				return entry;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static ConceptReferenceList createConceptReferenceList(
	        String[] codes, String codingSchemeName) {
		if (codes == null) {
			return null;
		}
		ConceptReferenceList list = new ConceptReferenceList();
		for (String code : codes) {
			ConceptReference cr = new ConceptReference();
			cr.setCodingSchemeName(codingSchemeName);
			cr.setConceptCode(code);
			list.addConceptReference(cr);
		}
		return list;
	}

	public static String outputPropertyDetails(Property[] properties) {
		log.debug("EvsTreeUtil.outputPropertyDetails Entered");

		String prop_value = "";
		String evsDisplayNameValue = "";

		for (Property property : properties) {
			String prop_name = property.getPropertyName();
			log.debug("property.getPropertyName(): "
			        + property.getPropertyName());
			prop_value = property.getValue().getContent();
			if (property.getPropertyName().equals(
			        Constants.Evs.DISPLAY_NAME_TAG)
			        || property.getPropertyName().equals(
			                Constants.Evs.PREFERRED_NAME_TAG)
			        || property.getPropertyName().equals(
			                Constants.Evs.DISPLAY_NAME_TAG_ZF)
			        || property.getPropertyType().equals("presentation")) {
				log.debug("property.getPropertyName(): "
				        + property.getPropertyName());
				evsDisplayNameValue = evsDisplayNameValue
				        + property.getValue().getContent() + " | ";
				log.debug("evsDisplayNameValue: " + evsDisplayNameValue);
			}
		}
		log.debug("EvsTreeUtil.outputPropertyDetails Exit ");
		log.debug("Final evsDisplayNameValue: " + evsDisplayNameValue);
		return evsDisplayNameValue;
	}

	public void setApplicationService(LexEVSService inAppService) {
		appService = inAppService;
	}

	private static LexEVSService getAppService() {
		if (appService != null) {
			return appService;
		}

		return RemoteServerUtil.createLexBIGService();

	}

	public void callGetZebrafishTreeFromEVS() {
		String vocabularyName = "Zebrafish";
		String rootNode = "ZFA:0100000";
		boolean direction = true;
		boolean isaFlag = true;
		Vector roles = null;
		String onlyLeaf = "true";
		String usePreferredName = "false";
		int attributeSetting = 0;
		// Vector semanticType = treeConfig.getSemanticType();
		String cacheFlag = "true";
		DefaultMutableTreeNode tree = null;
		try {
			// evsQuery = new EVSQueryImpl();

			if (vocabularyName.equalsIgnoreCase("go")) {
				roles = null; // per evs team.
			}

			log.debug("getTreeFromEvs.getTree: ");

			// New utility to get tree one level at a time
			RecursiveTreeBuilder treeBuilder = new RecursiveTreeBuilder(
			        getAppService());
			tree = treeBuilder.getTree(vocabularyName, rootNode, direction,
			        isaFlag, attributeSetting, roles);

			// populateTreeData(tree, vocabularyName);

			log.debug("getTreeFromEvs Print treeNode: ");
			printTree(tree);

		} catch (Exception e) {
			// e.printStackTrace();
			// MessageLog.printInfo(this.getClass().getName()
			// + ": Exception occured: " + e.getMessage());
			System.err.println("Error in getTree() - " + e.toString());

		}
	}

	public static void printTree(DefaultMutableTreeNode tree) {
		int childCount = tree.getChildCount();
		if (childCount > 0) {
			TreeNode node = tree.getFirstChild();
			node.getChildCount();
		}
	}

	public static void callGetDiagnosisTreeFromEVS() {
		// TODO Auto-generated method stub
		String vocabularyName = "NCI Thesaurus";
		String rootNode = "C27551";
		boolean direction = true;
		boolean isaFlag = true;
		Vector roles = null;
		String onlyLeaf = "true";
		String usePreferredName = "false";
		int attributeSetting = 0;
		// Vector semanticType = treeConfig.getSemanticType();
		String cacheFlag = "true";
		DefaultMutableTreeNode tree = null;
		try {
			// evsQuery = new EVSQueryImpl();

			if (vocabularyName.equalsIgnoreCase("go")) {
				roles = null; // per evs team.
			}

			log.debug("getTreeFromEvs.getTree: ");

			// New utility to get tree one level at a time
			RecursiveTreeBuilder treeBuilder = new RecursiveTreeBuilder(
			        getAppService());
			tree = treeBuilder.getTree(vocabularyName, rootNode, direction,
			        isaFlag, attributeSetting, roles);

			// populateTreeData(tree, vocabularyName);

			log.debug("getTreeFromEvs Print treeNode: ");
			// printTree(tree);

		} catch (Exception e) {
			// e.printStackTrace();
			// MessageLog.printInfo(this.getClass().getName()
			// + ": Exception occured: " + e.getMessage());
			System.err.println("Error in getTree() - " + e.toString());

		}
	}

}
