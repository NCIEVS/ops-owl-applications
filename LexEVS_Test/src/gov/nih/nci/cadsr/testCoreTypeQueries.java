/**
 * National Cancer Institute Center for Bioinformatics
 * 
 * LexEVS_Test_42
 * gov.nih.nci.cadsr
 * testCoreTypeQueries.java
 * Oct 8, 2009
 *
 */
/** <!-- LICENSE_TEXT_START -->
 The LexEVS_Test_42 Copyright 2009 Science Applications International Corporation (SAIC)
 Copyright Notice.  The software subject to this notice and license includes both human readable source code form and machine readable, binary, object code form (the EVSAPI Software).  The EVSAPI Software was developed in conjunction with the National Cancer Institute (NCI) by NCI employees and employees of SAIC.  To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 This LexEVS_Test_42 Software License (the License) is between NCI and You.  You (or Your) shall mean a person or an entity, and all other entities that control, are controlled by, or are under common control with the entity.  Control for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.
 This License is granted provided that You agree to the conditions described below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights in the LexEVS_Test_42 Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly display, publicly perform, and prepare derivative works of the EVSAPI Software; (ii) distribute and have distributed to and by third parties the EVSAPI Software and any modifications and derivative works thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such rights to further third parties.  For sake of clarity, and not by way of limitation, NCI shall have no right of accounting or right of payment from You or Your sublicensees for the rights granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions and the disclaimer and limitation of liability of Article 6, below.  Your redistributions in object code form must reproduce the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials provided with the distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: This product includes software developed by SAIC and the National Cancer Institute.  If You do not include such end-user documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" Science Applications International Corporation and "SAIC" to endorse or promote products derived from this Software.  This License does not authorize You to use any trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and into any third party proprietary programs.  However, if You incorporate the Software into third party proprietary programs, You agree that You are solely responsible for obtaining any permission from such third parties required to incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including without limitation Your end-users, of their obligation to secure any required permissions from such third parties before incorporating the Software into such third party proprietary software programs.  In the event that You fail to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to the extent prohibited by law, resulting from Your failure to obtain such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * <!-- LICENSE_TEXT_END -->
 */
package gov.nih.nci.cadsr;

import gov.nih.nci.cadsr.cdecurate.tool.EVS_Bean;
import gov.nih.nci.cadsr.cdecurate.tool.EVS_METACODE_Bean;
import gov.nih.nci.cadsr.cdecurate.tool.EVS_UserBean;
import gov.nih.nci.cadsr.sentinel.audits.AuditConceptToEVS;
import gov.nih.nci.camod.util.RemoteServerUtil;
import gov.nih.nci.evs.security.SecurityToken;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.SortOptionList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.ActiveOption;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.SearchDesignationOption;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.Utility.LBConstants;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.LexGrid.naming.Mappings;
import org.LexGrid.naming.SupportedHierarchy;
import org.apache.log4j.Logger;

/**
 * @author safrant
 * 
 */
public class testCoreTypeQueries {

	private org.apache.log4j.Logger logger = Logger
	        .getLogger(gov.nih.nci.cadsr.testCoreTypeQueries.class);

	LexEVSService evsService = null;
	// constant variables
	/**
	 * constant value for empty data to get the vocab attribute from the user
	 * bean according to what is needed
	 */
	public static final int VOCAB_NULL = 0;
	/** constant value to return display vocab name */
	public static final int VOCAB_DISPLAY = 1;
	/** constant value to return database vocab orgin */
	public static final int VOCAB_DBORIGIN = 2;
	/** constant value to return database vocab name */
	public static final int VOCAB_NAME = 3;
	public static final String META_VALUE = "MetaValue";
	// private static final LexEVSService appService = null;

	// private static final String _service = "EvsServiceInfo";
	// private static final String serviceUrl =
	// "http://lexevsapi51-stage.nci.nih.gov/lexevsapi51";

	EVS_UserBean m_eUser = new EVS_UserBean();

	public testCoreTypeQueries(LexEVSService lbSvc) {
		this.evsService = lbSvc;
		this.getMetathesaurusMapping();
		this.doConceptQuery();
		this.test64Transition();
		this.testDoConceptQuery();
		this.testMapping();
		this.testMetaSearch();
		this.testSentinelTool();
	}
	
	public testCoreTypeQueries(String serviceURL){
		this(RemoteServerUtil.createLexEVSService(serviceURL));
	}

	/**
	 * This method will test searchDescLogicConcepts
	 * 
	 */
	public static void searchDescLogicConcepts_NCI(LexEVSService appService) {
		System.out.println("***********************");
		System.out.println("* caCORE type search  *");
		System.out.println("***********************");
		String vocabName = "NCI_Thesaurus";
		String searchTerm = "apoptosis";
		// RemoteServerUtil rsu = new RemoteServerUtil();
		// LexEVSService appService = RemoteServerUtil.createLexBIGService();
		if (appService == null) {
			System.out.println("lbSvc == null???");
			System.exit(0);
		}
		List evsResults = new ArrayList();

		try {
			if (appService == null) {
				System.out.println("appService is null");
				return;
			}
			CodedNodeSet nodes = appService.getNodeSet(vocabName,
			        ConvenienceMethods.createProductionTag(), null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
			        SearchDesignationOption.ALL,
			        LBConstants.MatchAlgorithms.startsWith.name(), null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
			        null, 20);

			// for (int i = 0; i < crl.getResolvedConceptReferenceCount(); i++)
			// {
			// Entity concept = crl.getResolvedConceptReference(i).getEntity();
			// System.out.println("Code: " + concept.getEntityCode()
			// + " PreferredName: "
			// + concept.getEntityDescription().getContent());
			// }
			if (crl.getResolvedConceptReferenceCount() > 5) {
				System.out.println("Success for caCORE type search on "
				        + vocabName + " " + searchTerm);
			}

		} catch (Exception ex) {
			System.out
			        .println("FAILURE: testsearchDescLogicConcepts_NCI throws Exception = "
			                + ex);
		}
	}

	public void testDoConceptQuery() {
		// this.evsService = evsService;
		String vocabAccess = "";
		// String termStr = "RID1543";
		String termStr = "C12434";
		String dtsVocab = "NCI Thesaurus";
		// String dtsVocab = "RadLex";
		String sSearchIn = "Names";
		// String sSearchIn = "ConCode";
		String sPropIn = "UMLS_CUIs";
		String vocabType = "PropType";
		String sSearchAC = "";
		ResolvedConceptReferenceList lstResult = null;
		// List lstResult1 = null;
		// List lstResult2= new ArrayList();

		String algorithm = getAlgorithm(termStr);
		termStr = cleanTerm(termStr);

		try {
			// check if valid dts vocab
			dtsVocab = getVocabAttr(dtsVocab, testCoreTypeQueries.VOCAB_NULL,
			        testCoreTypeQueries.VOCAB_NAME); // "",
			// "vocabName");
			if (dtsVocab.equals(testCoreTypeQueries.META_VALUE)) // "MetaValue"))
			System.out.println("Metathesaurus");

			this.registerSecurityToken((LexEVSApplicationService) evsService);

			CodedNodeSet nodeSet = evsService.getNodeSet(dtsVocab, null, null);

			if (sSearchIn.equals("ConCode")) {

				ConceptReferenceList crefs = ConvenienceMethods
				        .createConceptReferenceList(new String[] { termStr },
				                "NCI Thesaurus");
				nodeSet = nodeSet.restrictToCodes(crefs);

			} else if (sSearchIn.equals("subConcept"))
			// query.getChildConcepts(dtsVocab, termStr);
			try {

				HashMap<String, ResolvedConceptReference> hSubs = returnSubConcepts(
				        termStr, dtsVocab);

				lstResult = new ResolvedConceptReferenceList();
				Iterator<String> iter = hSubs.keySet().iterator();
				while (iter.hasNext()) {
					String code = iter.next();
					ResolvedConceptReference ac = hSubs.get(code);
					if (code != null && !code.equals(termStr)) {
						lstResult.addResolvedConceptReference(ac);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			else {
				if (vocabType.equals("") || vocabType.equals("NameType")) // do
																		  // concept
																		  // name
																		  // search
				nodeSet = nodeSet.restrictToMatchingDesignations(termStr, // the
																		  // text
																		  // to
																		  // match
				        CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY, // whether
																			 // to
																			 // search
																			 // all
																			 // designation,
																			 // only
																			 // Preferred
																			 // or
																			 // only
																			 // Non-Preferred
				        algorithm, // the match algorithm to use
				        null); // the language to match (null matches all)
				else if (vocabType.equals("PropType")) { // do concept prop
														 // search
					// GF32446 this cause Semantic_Type to not to be included
					LocalNameList lnl = new LocalNameList();
					lnl.addEntry(sPropIn);
					nodeSet = nodeSet.restrictToMatchingProperties(lnl, // the
																		// Property
																		// Name
																		// to
																		// match
					        null, // the Property Type to match (null matches
								  // all)
					        termStr, // the text to match
					        algorithm, // the match algorithm to use
					        null);// the language to match (null matches all)
					System.out.println("doConceptQuery nodeSet retrieved");
					// logger.debug("EVSSearch:doConceptQuery() nodeSet retrieved from lexEVS.");
					// //GF29786 - geting the concept list from lexevs based on
					// synonyms done (old way???)
				}
			}
			// call the evs to get resutls
			if (!sSearchIn.equals("subConcept")) {
				LocalNameList lnl = new LocalNameList();
				Hashtable hType = MockEVSUserBean.getMetaCodeType();
				Iterator iter = hType.keySet().iterator();
				while (iter.hasNext()) {
					String propName = (String) iter.next();
					System.out.println("EVSSearch:doConceptQuery() propName ["
					        + propName + "]");
					lnl.addEntry(propName);
				}

				if (sSearchAC.equals("ParentConceptVM")) lstResult = nodeSet
				        .resolveToList(null, // Sorts used to sort results (null
											 // means sort by match score)
				                lnl, // PropertyNames to resolve (null resolves
									 // all)
				                new CodedNodeSet.PropertyType[] {
				                        PropertyType.DEFINITION,
				                        PropertyType.PRESENTATION }, // PropertyTypess
																	 // to
																	 // resolve
																	 // (null
																	 // resolves
																	 // all)
																	 // //PropertyTypess
																	 // to
																	 // resolve
																	 // (null
																	 // resolves
																	 // all)
				                100 // cap the number of results returned (-1
									// resolves all)
				        );
				else
					lstResult = nodeSet.resolveToList(null, // Sorts used to
															// sort results
															// (null means sort
															// by match score)
					        null, // PropertyNames to resolve (null resolves
								  // all)
					        new CodedNodeSet.PropertyType[] {
					                PropertyType.DEFINITION,
					                PropertyType.PRESENTATION }, // PropertyTypess
																 // to resolve
																 // (null
																 // resolves
																 // all)
					        100 // cap the number of results returned (-1
								// resolves all)
					        );

			}
		} catch (Exception ex) {
			// logger.error(evsService.toString()
			// + " :conceptNameSearch lstResults: " + ex.toString(), ex);
			ex.printStackTrace();
		}
		System.out.println(lstResult.getResolvedConceptReferenceCount());
	}

	private String getAlgorithm(String termStr) {

		String algorithm = "exactMatch";

		boolean starts = false;
		boolean ends = false;
		boolean contains = false;
		boolean multiple = false;
		boolean embed = false;

		termStr = termStr.trim();
		ends = termStr.startsWith("*"); // Term ends with rest of the string
		starts = termStr.endsWith("*"); // Term starts with rest of the string

		contains = termStr.substring(1, termStr.length() - 1).indexOf(" *") >= 0
		        || termStr.substring(1, termStr.length() - 1).indexOf("* ") >= 0;
		if (!contains) embed = termStr.substring(1, termStr.length() - 1)
		        .indexOf("*") >= 0;

		multiple = termStr.indexOf(' ') > 0;

		if (starts) {
			algorithm = "startsWith";
		}
		if (contains || ends) {
			algorithm = "contains"; // GF29786
		}
		if (multiple && starts && ends) {
			algorithm = "nonLeadingWildcardLiteralSubString";
		}
		if (multiple && starts && ends && contains) {
			algorithm = "contains"; // GF29786
		}

		return algorithm;
	}

	private static String cleanTerm(String termStr) {
		termStr = termStr.trim();
		termStr = termStr.replace("*", "");
		return termStr;
	}

	private HashMap<String, ResolvedConceptReference> returnSubConcepts(
	        String code, String scheme) throws LBException {
		HashMap<String, ResolvedConceptReference> ret = new HashMap<String, ResolvedConceptReference>();

		CodingScheme cs = evsService.resolveCodingScheme(scheme, null);
		boolean forwardNavigable = cs.getMappings().getSupportedHierarchy()[0]
		        .isIsForwardNavigable();
		String relation = returnAssociations(cs);

		// Perform the query ...
		NameAndValue nv = new NameAndValue();
		NameAndValueList nvList = new NameAndValueList();
		nv.setName(relation);
		nvList.addNameAndValue(nv);

		ResolvedConceptReferenceList matches = evsService
		        .getNodeGraph(scheme, null, null)
		        .restrictToAssociations(nvList, null)
		        .resolveAsList(
		                ConvenienceMethods.createConceptReference(code, scheme),
		                forwardNavigable, !forwardNavigable, 1, 1,
		                new LocalNameList(), null, null, 1024);

		// Analyze the result ...
		ret = getAssociatedConcepts(matches, true);
		return ret;
	}

	private String returnAssociations(CodingScheme cs) throws LBException {

		String ret = new String();

		Mappings mappings = cs.getMappings();
		SupportedHierarchy[] hierarchies = mappings.getSupportedHierarchy();
		SupportedHierarchy hierarchyDefn = hierarchies[0];
		String[] associationsToNavigate = hierarchyDefn.getAssociationNames();// associations

		for (String assn : associationsToNavigate) {
			if (assn.equals("subClassOf")) {
				ret = assn;
				// we prefer this association
				break;
			}
			if (assn.equals("is_a")) {
				ret = assn;
				break;
			}
			if (ret.length() == 0 && hierarchyDefn.getLocalId().equals("is_a")) ret = assn;

		}

		return ret;

	}

	private HashMap getAssociatedConcepts(ResolvedConceptReferenceList matches,
	        boolean resolveConcepts) {
		HashMap ret = new HashMap();

		if (matches.getResolvedConceptReferenceCount() > 0) {
			ResolvedConceptReference ref = (ResolvedConceptReference) matches
			        .enumerateResolvedConceptReference().nextElement();

			// Print the associations
			AssociationList targetof = ref.getTargetOf();
			if (targetof != null) {
				Association[] associations = targetof.getAssociation();
				for (int i = 0; i < associations.length; i++) {
					Association assoc = associations[i];
					if (assoc != null
					        && assoc.getAssociatedConcepts() != null
					        && assoc.getAssociatedConcepts()
					                .getAssociatedConcept() != null) { // blank
																	   // screen
																	   // due to
																	   // NPE on
																	   // superconcept
						AssociatedConcept[] acl = assoc.getAssociatedConcepts()
						        .getAssociatedConcept();
						for (int j = 0; j < acl.length; j++) {
							AssociatedConcept ac = acl[j];
							if (resolveConcepts) ret.put(ac.getCode(), ac);
							else
								ret.put(ac.getCode(), ac.getEntityDescription()
								        .getContent());
						}
					}
				}
			} else {

				AssociationList sourceOf = ref.getSourceOf();

				if (sourceOf != null) {
					Association[] associations = sourceOf.getAssociation();
					for (int i = 0; i < associations.length; i++) {
						Association assoc = associations[i];
						if (assoc != null
						        && assoc.getAssociatedConcepts() != null
						        && assoc.getAssociatedConcepts()
						                .getAssociatedConcept() != null) { // blank
																		   // screen
																		   // due
																		   // to
																		   // NPE
																		   // on
																		   // superconcept
							AssociatedConcept[] acl = assoc
							        .getAssociatedConcepts()
							        .getAssociatedConcept();
							for (int j = 0; j < acl.length; j++) {
								AssociatedConcept ac = acl[j];
								if (resolveConcepts) ret.put(ac.getCode(), ac);
								else
									ret.put(ac.getCode(), ac
									        .getEntityDescription()
									        .getContent());
							}
						}
					}
				}
			}
		}

		return ret;
	}
	
	public static LexEVSService registerSecurityToken(
	        LexEVSService lexevsService) throws Exception {

		// String token = "";
		// Hashtable ht = userBean.getVocab_Attr();
		// if(ht == null) {
		// throw new
		// Exception("Not able to register security token (The vocabulary returns NULL for the coding schema ["
		// + codingScheme + "]).");
		// }
		// EVS_UserBean eu = (EVS_UserBean) ht.get(codingScheme);

		String codingScheme = "MedDRA";
		String token = "10382";
		SecurityToken securityToken = new SecurityToken();
		securityToken.setAccessToken(token);
		Boolean retval = null;
		try {
			retval = lexevsService.registerSecurityToken(codingScheme,
			        securityToken);
			if (retval != null && retval.equals(Boolean.TRUE)) {
				System.out
				        .println("Registration of SecurityToken was successful.");
			} else {
				System.out
				        .println("WARNING: Registration of SecurityToken failed.");
			}
		} catch (Exception e) {
			System.out
			        .println("WARNING: Registration of SecurityToken failed.");
		}
		return lexevsService;
	}
	
	

	public static LexEVSApplicationService registerSecurityToken(
	        LexEVSApplicationService lexevsService) throws Exception {

		// String token = "";
		// Hashtable ht = userBean.getVocab_Attr();
		// if(ht == null) {
		// throw new
		// Exception("Not able to register security token (The vocabulary returns NULL for the coding schema ["
		// + codingScheme + "]).");
		// }
		// EVS_UserBean eu = (EVS_UserBean) ht.get(codingScheme);

		String codingScheme = "MedDRA";
		String token = "10382";
		SecurityToken securityToken = new SecurityToken();
		securityToken.setAccessToken(token);
		Boolean retval = null;
		try {
			retval = lexevsService.registerSecurityToken(codingScheme,
			        securityToken);
			if (retval != null && retval.equals(Boolean.TRUE)) {
				System.out
				        .println("Registration of SecurityToken was successful.");
			} else {
				System.out
				        .println("WARNING: Registration of SecurityToken failed.");
			}
		} catch (Exception e) {
			System.out
			        .println("WARNING: Registration of SecurityToken failed.");
		}
		return lexevsService;
	}

	/**
	 * get the vocab attributes from teh user bean using filter attr and filter
	 * value to check and return its equivaltnt attrs
	 * 
	 * @param eUser
	 *            EVS_userbean obtained from the database at login
	 * @param sFilterValue
	 *            string existing value
	 * @param filterAttr
	 *            int existing vocab name
	 * @param retAttr
	 *            int returning vocab name
	 * @return value from returning vocab
	 */
	public String getVocabAttr(String sFilterValue, int filterAttr, int retAttr) // String
																				 // sFilterAttr,
																				 // String
																				 // sRetAttr)
	{
		// "NCI Thesaurus",0,3
		// go back if origin is emtpy
		if (sFilterValue == null || sFilterValue.equals("")) return "";

		String sRetValue = sFilterValue;
		// Hashtable eHash = eUser.getVocab_Attr();
		Vector vVocabs = MockEVSUserBean.getVocabNameList();
		if (vVocabs == null) vVocabs = new Vector();
		// handle teh special case to make sure vocab for api query is valid
		if (filterAttr == testCoreTypeQueries.VOCAB_NULL) // (sFilterAttr ==
														  // null ||
														  // sFilterAttr.equals(""))
		{
			// it is valid vocab name
			if (vVocabs.contains(sFilterValue)) return sFilterValue; // found it
			// first check if filter value is from diplay vocab list
			Vector vDisplay = MockEVSUserBean.getVocabDisplayList();
			if (vDisplay != null && vDisplay.contains(sFilterValue)) {
				int iIndex = vDisplay.indexOf(sFilterValue);
				sRetValue = (String) vVocabs.elementAt(iIndex);
				return sRetValue; // found it
			}
			// filter it as dborigin
			filterAttr = testCoreTypeQueries.VOCAB_DBORIGIN; // sFilterAttr =
															 // "vocabDBOrigin";
		}

		// for (int i=0; i<vVocabs.size(); i++)
		// {
		// String sName = (String)vVocabs.elementAt(i);
		// // EVS_UserBean usrVocab = (EVS_UserBean)eHash.get(sName);
		// String sValue = "";
		// //check if the vocab is meta thesaurus
		// String sMeta = usrVocab.getIncludeMeta();
		// if (sMeta != null && !sMeta.equals("") && sMeta.equals(sFilterValue))
		// return testCoreTypeQueries.META_VALUE; // "MetaValue";
		// //get teh data from teh bean to match search
		// if (filterAttr == testCoreTypeQueries.VOCAB_DISPLAY) //
		// (sFilterAttr.equalsIgnoreCase("vocabDisplay"))
		// sValue = usrVocab.getVocabDisplay();
		// else if (filterAttr == testCoreTypeQueries.VOCAB_DBORIGIN)
		// //(sFilterAttr.equalsIgnoreCase("vocabDBOrigin"))
		// sValue = usrVocab.getVocabDBOrigin();
		// else if (filterAttr == testCoreTypeQueries.VOCAB_NAME)
		// //(sFilterAttr.equalsIgnoreCase("vocabName"))
		// sValue = usrVocab.getVocabName();
		// //do matching and return the value
		// // System.out.println(sFilterValue + " getvocab " + sValue);
		// // if (sFilterValue.equalsIgnoreCase(sValue)) //check it later
		// if (sFilterValue.contains(sValue))
		// {
		// //get its value from teh bean for the return attr
		// if (retAttr == testCoreTypeQueries.VOCAB_DISPLAY) //
		// (sRetAttr.equalsIgnoreCase("vocabDisplay"))
		// sRetValue = usrVocab.getVocabDisplay();
		// else if (retAttr == testCoreTypeQueries.VOCAB_DBORIGIN) //
		// (sRetAttr.equalsIgnoreCase("vocabDBOrigin"))
		// sRetValue = usrVocab.getVocabDBOrigin();
		// else if (retAttr == testCoreTypeQueries.VOCAB_NAME) //
		// (sRetAttr.equalsIgnoreCase("vocabName"))
		// sRetValue = usrVocab.getVocabName();
		// break;
		// }
		// }
		// return the first vocab if null
		// if ((sRetValue == null || sRetValue.equals("")) && vVocabs != null)
		// sRetValue = (String)vVocabs.elementAt(0);
		// System.out.println(sRetValue + sFilterValue + filterAttr + retAttr);
		if (sRetValue == null) sRetValue = "";
		return sRetValue;
	}

	public void testMapping() {
		// pass in term and vocab
		String MetaName = "NCI MetaThesaurus";
		String vocabAccess = "";
		String dtsVocab = "NCI Thesaurus";

		// String dtsVocab = "RadLex";
		// String sSearchIn = "Names";
		// String termStr = "blood";

		String sSearchIn = "ConCode";
		// String termStr = "RID1543";
		// String termStr = "C12434";
		String termStr = "C0007634";

		String sPropIn = "UMLS_CUIs";
		String vocabType = "PropType";
		String sSearchAC = "";
		String algorithm = LBConstants.MatchAlgorithms.exactMatch.name();
		ResolvedConceptReferenceList lstResult = null;

		// search for that term in Meta
		try {
			CodedNodeSet nodeSet = evsService.getNodeSet(MetaName, null, null);
			if (sSearchIn.equals("ConCode")) {

				ConceptReferenceList crefs = ConvenienceMethods
				        .createConceptReferenceList(new String[] { termStr },
				                MetaName);
				nodeSet = nodeSet.restrictToCodes(crefs);

			} else if (sSearchIn.equals("subConcept"))
			// query.getChildConcepts(dtsVocab, termStr);
			try {

				HashMap<String, ResolvedConceptReference> hSubs = returnSubConcepts(
				        termStr, MetaName);

				lstResult = new ResolvedConceptReferenceList();
				Iterator<String> iter = hSubs.keySet().iterator();
				while (iter.hasNext()) {
					String code = iter.next();
					ResolvedConceptReference ac = hSubs.get(code);
					if (code != null && !code.equals(termStr)) {
						lstResult.addResolvedConceptReference(ac);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			else {
				if (vocabType.equals("") || vocabType.equals("NameType")) // do
																		  // concept
																		  // name
																		  // search
				nodeSet = nodeSet.restrictToMatchingDesignations(termStr, // the
																		  // text
																		  // to
																		  // match
				        CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY, // whether
																			 // to
																			 // search
																			 // all
																			 // designation,
																			 // only
																			 // Preferred
																			 // or
																			 // only
																			 // Non-Preferred
				        algorithm, // the match algorithm to use
				        null); // the language to match (null matches all)
				else if (vocabType.equals("PropType")) { // do concept prop
														 // search
					// GF32446 this cause Semantic_Type to not to be included
					LocalNameList lnl = new LocalNameList();
					lnl.addEntry(sPropIn);
					PropertyType[] propTypeArray = new PropertyType[1];
					propTypeArray[0] = CodedNodeSet.PropertyType.PRESENTATION;
					nodeSet = nodeSet.restrictToMatchingProperties(null, // the
																		 // Property
																		 // Name
																		 // to
																		 // match
					        propTypeArray, // the Property Type to match (null
										   // matches all)
					        termStr, // the text to match
					        algorithm, // the match algorithm to use
					        null);// the language to match (null matches all)
					System.out.println("doConceptQuery nodeSet retrieved");
					// logger.debug("EVSSearch:doConceptQuery() nodeSet retrieved from lexEVS.");
					// //GF29786 - geting the concept list from lexevs based on
					// synonyms done (old way???)
				}
			}

			lstResult = nodeSet.resolveToList(null, null, null, 20);

			// If found, check for a Map to NCIt

			// Tell the api that you want to get back only the PRESENTATION type
			// properties
			CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];
			types[0] = CodedNodeSet.PropertyType.PRESENTATION;

			// Now create a qualifier list containing the code you wish to
			// search
			NameAndValueList qualifierList = new NameAndValueList();
			NameAndValue nv = new NameAndValue();
			nv.setName("source-code");
			nv.setContent("RID1543");
			qualifierList.addNameAndValue(nv);

			// Specify the source code should come from the NCI source
			// LocalNameList LnL = new LocalNameList();
			// LnL.addEntry("RADLEX");

			CodedNodeSet nodeSet2 = evsService.getNodeSet(dtsVocab, null, null);
			nodeSet2 = nodeSet2.restrictToProperties(null, types, null, null,
			        qualifierList);
			lstResult = nodeSet2.resolveToList(null, null, null, 20);

			// Now create a qualifier list containing the code you wish to
			// search
			qualifierList = new NameAndValueList();
			nv = new NameAndValue();
			nv.setName("source-code");
			nv.setContent("C12438");
			qualifierList.addNameAndValue(nv);

			// Specify the source code should come from the NCI source
			// LnL = new LocalNameList();
			// LnL.addEntry("NCI");

			CodedNodeSet nodeSet3 = evsService.getNodeSet(dtsVocab, null, null);
			nodeSet3 = nodeSet3.restrictToProperties(null, types, null, null,
			        qualifierList);
			lstResult = nodeSet3.resolveToList(null, null, null, 20);

			// If no map, return Meta CUI

			// If not found at all, search original source

		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void testMetaSearch() {
		Vector<EVS_Bean> vList = new Vector<EVS_Bean>();

		String searchIn = "MetaCode";
		String sMetaSource = "NCI";
		int iMetaLimit = 20;
		String sVocab = "";
		String termStr = "C12434";
		Vector<EVS_Bean> resultBeans = doMetaSearch(vList, termStr, searchIn, sMetaSource, iMetaLimit, sVocab);
		if(resultBeans.size()>0 ){
			System.out.println("Search by Meta source code successful");
		} else {
			System.out.println("Serach by Meta source code returned no results");
		}

		termStr = "gene";
		searchIn = "termSearch";
		resultBeans = doMetaSearch(vList, termStr, searchIn, sMetaSource, iMetaLimit, sVocab);
		if(resultBeans.size()>0 ){
			System.out.println("Search by Meta term successful");
		} else {
			System.out.println("Serach by Meta term returned no results");
		}

		searchIn = "ConCode";
		termStr = "C0017337";
		resultBeans = doMetaSearch(vList, termStr, searchIn, sMetaSource, iMetaLimit, sVocab);
		if(resultBeans.size()>0 ){
			System.out.println("Search by Meta CUI successful");
		} else {
			System.out.println("Serach by Meta CUI returned no results");
		}

	}

	/**
	 * @param vList
	 * @param termStr
	 * @param sSearchIn
	 * @param sMetaSource
	 * @param iMetaLimit
	 * @param sVocab
	 * @return
	 */
	private Vector<EVS_Bean> doMetaSearch(Vector<EVS_Bean> vList,
	        String termStr, String sSearchIn, String sMetaSource,
	        int iMetaLimit, String sVocab) {

		ResolvedConceptReferenceList concepts = null;

		if (vList == null) vList = new Vector<EVS_Bean>();
		try {
			if (termStr == null || termStr.equals("")) return vList;
			List metaResults = null;
			CodedNodeSet nodeSet = evsService.getNodeSet("NCI MetaThesaurus",
			        null, null);
			try {
				if (sSearchIn.equalsIgnoreCase("MetaCode")) { // do meta code
															  // specific to
															  // vocabulary
															  // source
					// In the NCI MetaThesaurus, fidning the 'source' of an
					// 'Atom' is equivalent to finding the
					// 'source' of a given Property of an Entity. Each CUI
					// (which is equivalent to an Entity in
					// LexEVS) may contain several Presentation Properties
					// (Atoms or AUI's of that CUI).
					// Each of these Presentation Properties is Qualified by a
					// 'source-code' Qualifier,
					// which reflects the code of this Atom in its original
					// source, and a 'source' qualifier,
					// which states the source itself that this Atom came from

					// query.searchSourceByAtomCode(termStr, sMetaSource);

					CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];
					types[0] = CodedNodeSet.PropertyType.PRESENTATION;
					
					NameAndValueList qualifierList = new NameAndValueList();
					NameAndValue nv = new NameAndValue();                  
					nv.setName("source-code");
					nv.setContent(termStr);                
					qualifierList.addNameAndValue(nv);
					 
					//Specify the source code should come from the NCI source
					LocalNameList LnL = new LocalNameList();
					LnL.addEntry(sMetaSource);
					 
					 
					nodeSet = nodeSet.restrictToProperties(null,types,LnL,null, qualifierList);
					
					
					

//					nodeSet = nodeSet.restrictToProperties(
//					        Constructors.createLocalNameList("propertyType"),
//					        types,
//					        Constructors.createLocalNameList(sMetaSource),
//					        null, null);

//					nodeSet = nodeSet.restrictToMatchingProperties(
//					        Constructors.createLocalNameList("value"), // the
//																	   // Property
//																	   // Name
//																	   // to
//																	   // match
//					        null, // the Property Type to match (null matches
//								  // all)
//					        termStr, // the text to match
//					        "contains", // the match algorithm to use
//					        null);// the language to match (null matches all)

				} else if (sSearchIn.equalsIgnoreCase("ConCode")) // meta cui
																  // search
				nodeSet = nodeSet.restrictToMatchingProperties(
				        Constructors.createLocalNameList("code"), // the
																  // Property
																  // Name to
																  // match
				        null, // the Property Type to match (null matches all)
				        termStr, // the text to match
				        "exactMatch", // the match algorithm to use
				        null // the language to match (null matches all)
				        );
				else
					// meta keyword search
					nodeSet = nodeSet
					        .restrictToMatchingDesignations(
					                termStr, // the text to match
					                CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY, // whether
																						 // to
																						 // search
																						 // all
																						 // designation,
																						 // only
																						 // Preferred
																						 // or
																						 // only
																						 // Non-Preferred
					                "contains", // the match algorithm to use
					                null); // the language to match (null
										   // matches all)

				SortOptionList sortCriteria = Constructors
				        .createSortOptionList(new String[] { "matchToQuery",
				                "code" });

				// Analyze the result ...

				concepts = nodeSet.resolveToList(sortCriteria, // Sorts used to
															   // sort results
															   // (null means
															   // sort by match
															   // score)
				        null, // PropertyNames to resolve (null resolves all)
				        null, // PropertyTypess to resolve (null resolves all)
				        100 // cap the number of results returned (-1 resolves
							// all)
				        );

			} catch (Exception ex) {
				System.out.println("doMetaSearch evsSearch: " + ex.toString());
				ex.printStackTrace();
			}
			if (concepts != null
			        && concepts.getResolvedConceptReferenceCount() > 0) {
				String sConName = "";
				String sConID = "";
				String sCodeType = "";
				String sSemantic = "";
				String sCodeSrc = "";
				int iLevel = 0;

				for (int i = 0; i < concepts.getResolvedConceptReferenceCount(); i++) {
					// Do this so only one result is returned on Meta code
					// search (API is dupicating a result)
					if (sSearchIn.equals("MetaCode") && i > 0) break;
					// get concept properties
					ResolvedConceptReference rcr = concepts
					        .getResolvedConceptReference(i);

					if (rcr != null) {
						Property[] props = rcr.getEntity().getProperty();
						Presentation[] presentations = rcr.getEntity()
						        .getPresentation();
						Definition[] definitions = rcr.getEntity()
						        .getDefinition();

						sConName = rcr.getEntityDescription().getContent();
						sConID = rcr.getCode();

						sCodeType = this.getNCIMetaCodeType(sConID, "byID");

						// get semantic types
						sSemantic = this.getMetaSemantics(props);
						// get preferred source code from atom collection
						sCodeSrc = this.getPrefMetaCode(presentations);

						// get definition attributes
						String sDefSource = "";
						String sDefinition = m_eUser.getDefDefaultValue();
						// add sepeate record for each definition
						if (definitions != null && definitions.length > 0) {
							for (Definition defType : definitions) {
								sDefinition = defType.getValue().getContent();
								sDefSource = defType.getSource()[0]
								        .getContent();

								EVS_Bean conBean = new EVS_Bean();
								conBean.setEVSBean(sDefinition, sDefSource,
								        sConName, sConName, sCodeType, sConID,
								        sVocab, sVocab, iLevel, "", "", "", "",
								        sSemantic, "", "");
								conBean.setPREF_VOCAB_CODE(sCodeSrc); // store
																	  // pref
																	  // code in
																	  // the
																	  // bean
								vList.addElement(conBean); // add concept bean
														   // to vector
							}
						} else {
							EVS_Bean conBean = new EVS_Bean();
							conBean.setEVSBean(sDefinition, sDefSource,
							        sConName, sConName, sCodeType, sConID,
							        sVocab, sVocab, iLevel, "", "", "", "",
							        sSemantic, "", "");
							conBean.setPREF_VOCAB_CODE(sCodeSrc); // store pref
																  // code in the
																  // bean
							vList.addElement(conBean); // add concept bean to
													   // vector
						}
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("doMetaSearch exception : " + ex.toString());
			ex.printStackTrace();
		}
		return vList;
	}

	/**
	 * @param conID
	 * @param ftrType
	 * @return
	 * @throws Exception
	 */
	private String getNCIMetaCodeType(String conID, String ftrType)
	        throws Exception {
		String sCodeType = "";
		// get the hash table of meta code property types
		Hashtable hType = m_eUser.getMetaCodeType();
		if (hType == null) hType = new Hashtable();
		// define code type according to the con id
		Enumeration enum1 = hType.keys();
		while (enum1.hasMoreElements()) {
			String sKey = (String) enum1.nextElement();
			EVS_METACODE_Bean metaBean = (EVS_METACODE_Bean) hType.get(sKey);
			if (metaBean == null) metaBean = new EVS_METACODE_Bean();
			String sMCode = metaBean.getMETACODE_TYPE();
			String sCFilter = metaBean.getMETACODE_FILTER().toUpperCase(); // (String)hType.get(sMCode);
			if (sCFilter == null) sCFilter = "";
			if (ftrType.equals("byID")) {
				// get the default value regardless
				if (sCFilter.equals("DEFAULT")) sCodeType = sMCode;
				// use the fitlered one if exists and leave
				else if (!sCFilter.equals("")
				        && conID.toUpperCase().indexOf(sCFilter) >= 0) {
					sCodeType = sMCode;
					break;
				}
			} else // by key
			{
				if (conID.toUpperCase().indexOf(sKey.toUpperCase()) >= 0) {
					sCodeType = sMCode;
					break;
				}
			}
		}
		return sCodeType;
	}

	/**
	 * to get the semantic value for meta thesarurs concept from the collection
	 * 
	 * @param mtcCon
	 *            MetaThesaurusConcept object
	 * @return sSemantic
	 */
	private String getMetaSemantics(Property[] properties) {
		String sSemantic = "";

		for (Property prop : properties) {
			String name = prop.getPropertyName();
			if (name != null && name.equals("Semantic_Type")) {
				if (!sSemantic.equals("")) sSemantic += "; ";
				sSemantic += prop.getValue().getContent();
			}
		}
		return sSemantic;
	}

	/**
	 * to get the NCI thesaurus code from the atom collection
	 * 
	 * @param mtcCon
	 *            MetaThesaurusConcept object
	 * @return sCode
	 */
	private String getPrefMetaCode(Presentation[] presentations) {
		String sCode = "";
		String prefSrc = "";
		for (Presentation pres : presentations) {
			Source[] sources = pres.getSource();
			prefSrc = m_eUser.getPrefVocabSrc();
			if (prefSrc != null) {
				for (Source src : sources) {
					String sConSrc = src.getContent();
					if (src != null && !sConSrc.equals("")) {
						if (sConSrc.contains(prefSrc)) System.out
						        .println("GOT THE PREFSRC");
						if (sCode == null) sCode = "";
					}
				}
			}
		}
		return sCode;
	}

	public void test64Transition() {

		String searchTerm = "name";
		String vocabVersion = "201604";
		String vocabName = "NCI Metathesaurus";

		try {
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setVersion(vocabVersion);
			CodedNodeSet nodes = evsService.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToMatchingDesignations(searchTerm,
			        SearchDesignationOption.ALL,
			        LBConstants.MatchAlgorithms.exactMatch.name(), null);
			nodes = nodes.restrictToStatus(ActiveOption.ALL, null);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
			        null, 20);
			Vector<String> definitions = new Vector<String>();
			for (int i = 0; i < crl.getResolvedConceptReferenceCount(); i++) {
				Entity concept = crl.getResolvedConceptReference(i).getEntity();
				Definition[] defs = concept.getDefinition();
				for (int j = 0; j < defs.length; j++) {
					Definition tempDef = defs[j];
					String defText = tempDef.getValue().getContent();
					definitions
					        .add(concept.getEntityCode() + " "
					                + tempDef.getSource(0).getContent() + " "
					                + defText);
				}
			}

			for (String def : definitions) {
				System.out.println(def);
			}
			System.out.println(crl.getResolvedConceptReferenceCount());
		} catch (Exception ex) {
			System.out.println("searchConcepts_name " + vocabName + " and "
			        + vocabVersion + " throws Exception = ");
			ex.printStackTrace();
		}
		// return 0;

	}

	private ResolvedConceptReferenceList doConceptQuery() {

		String vocabAccess = "";

		String sSearchIn = "Name";
		String vocabType = "NameType";
		String sPropIn = null;
		String sSearchAC = "ObjectQualifier";

		// String termStr = "blood" ;
		// String dtsVocab = "NCI Thesaurus" ;

		String termStr = "protein";
		String dtsVocab = "Ontology for Biomedical Investigations";
		String algorithm = "contains";

		ResolvedConceptReferenceList lstResult = null;
		List lstResult1 = null;
		List lstResult2 = new ArrayList();

		algorithm = getAlgorithm(termStr);
		termStr = cleanTerm(termStr);

		try {

			// check if valid dts vocab
			// dtsVocab = m_eBean.getVocabAttr( m_eUser, dtsVocab,
			// EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME ); // "",
			// // "vocabName");
			// if( dtsVocab.equals( "MetaValue" ) ) // "MetaValue"))
			// return lstResult;
			 evsService = this.registerSecurityToken( 
			 evsService );
			// if
			// (dtsVocab.equalsIgnoreCase("Ontology for Biomedical Investigations"))
			// dtsVocab = "obi";
			CodedNodeSet nodeSet = evsService.getNodeSet(dtsVocab, null, null); 
			if (sSearchIn.equals("ConCode")) {

				ConceptReferenceList crefs = ConvenienceMethods
				        .createConceptReferenceList(new String[] { termStr },
				                "NCI Thesaurus");
				nodeSet = nodeSet.restrictToCodes(crefs);

			} else if (sSearchIn.equals("subConcept"))
			// query.getChildConcepts(dtsVocab, termStr);
			try {

				HashMap<String, ResolvedConceptReference> hSubs = returnSubConcepts(
				        termStr, dtsVocab);

				lstResult = new ResolvedConceptReferenceList();
				Iterator<String> iter = hSubs.keySet().iterator();
				while (iter.hasNext()) {
					String code = iter.next();
					ResolvedConceptReference ac = hSubs.get(code);
					if (code != null && !code.equals(termStr)) {
						lstResult.addResolvedConceptReference(ac);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			else {
				if (vocabType.equals("") || vocabType.equals("NameType")) 
				nodeSet = nodeSet.restrictToMatchingDesignations(termStr, 
				        CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY, 
				        algorithm, 
				        null); 
				else if (vocabType.equals("PropType")) { // do concept prop
														 // search
					                                     // GF32446 this cause
														 // Semantic_Type to not
														 // to be included
					LocalNameList lnl = new LocalNameList();
					lnl.addEntry(sPropIn);
					nodeSet = nodeSet.restrictToMatchingProperties( // JT b4
																	// GF32723
					        lnl, // the Property Name to match
					        null, // the Property Type to match (null matches
								  // all)
					        termStr, // the text to match
					        algorithm, // the match algorithm to use
					        null);// the language to match (null matches all)
	
					logger.debug("EVSSearch:doConceptQuery() nodeSet retrieved from lexEVS."); 
				}
			}
			// call the evs to get resutls
			if (!sSearchIn.equals("subConcept")) {

				lstResult = nodeSet.resolveToList(null, 
				        null, 
				        null, 
				        100 
				        );

				nodeSet = evsService.getNodeSet(dtsVocab, null, null);
				nodeSet = nodeSet.restrictToMatchingDesignations(termStr,
				        SearchDesignationOption.ALL,
				        LBConstants.MatchAlgorithms.contains.name(), null);
				ResolvedConceptReferenceList crl = nodeSet.resolveToList(null,
				        null, null, 20);
				crl.getResolvedConceptReferenceCount();

			}

			// begin GF32723 just for troubleshooting, it does not fix/change
			// anything
			if (lstResult != null
			        && lstResult.getResolvedConceptReferenceCount() > 0) {
				logger.info("EVSSearch:doConceptQuery ["
				        + termStr
				        + "] EVS query results list size "
				        + lstResult.getResolvedConceptReferenceCount()
				        + " resolved 1st concept = ["
				        + lstResult.getResolvedConceptReference(0)
				                .getConceptCode() + "]");
			} else {
				logger.info("EVSSearch:doConceptQuery [" + termStr
				        + "] EVS query results list is NULL!");
			}
			// end GF32723 just for troubleshooting, it does not fix/change
			// anything
		} catch (Exception ex) {
			logger.error(evsService.toString()
			        + " :conceptNameSearch lstResults: " + ex.toString(), ex);
		}
		return lstResult;
	}
	
    public long getMetathesaurusMapping() {
    	String term = "MO_683";
    	String source = "MGED";
    	
//    	String term = "C12438";
//    	String source = null;
    	
//        if (evsService == null) {
//              throw new Exception("LexBIGService can not be NULL or empty.");
//        }
        boolean evsLookupDone = false;  //just started :)
//        LexEVSHelper.lbSvc = lbSvc;

        long count = 0;

        CodedNodeSet nodeSet;
        try {
              nodeSet = evsService.getNodeSet("NCI MetaThesaurus", null, null);

              // Tell the api that you want to get back only the PRESENTATION type
              // properties
              CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];
              types[0] = CodedNodeSet.PropertyType.PRESENTATION;

              // Now create a qualifier list containing the code you wish to
              // search
              NameAndValueList qualifierList = new NameAndValueList();
              NameAndValue nv = new NameAndValue();
              nv.setName("source-code");
              nv.setContent(term);
              qualifierList.addNameAndValue(nv);


              System.out.println("getMetathesaurusMapping: original source = [" + source + "] to be submitted to EVS for term [" + term + "]");
              if(source != null) {
//                if(source.equals("LOINC") || source.equals("LNC215")) {
//                      source = "LNC";
//                }
//                else
//                if(source.equals("Radlex")) {
//                      source = "RADLEX";
//                }
//                else
//                if(source.equals("SNOMED")) {
//                      source = "SNOMEDCT";
//                }
//                else
//                if(source.equals("OBI")) {
//                      source = "";      //GF32723 it is not a source but standalone vocabulary
//                }
//                System.out.println("getMetathesaurusMapping: IMPORTANT !!! modified source = [" + source + "] for term [" + term + "] to be submitted to EVS ...");
                  System.out.println("1 getMetathesaurusMapping: source modification for EVS disabled");
                  nodeSet = nodeSet.restrictToProperties(null, types, Constructors.createLocalNameList(source), null,
                        qualifierList);
            } else {
              nodeSet = nodeSet.restrictToProperties(null, types, null, null,
              qualifierList);
            }
              ResolvedConceptReferenceList rcrl = nodeSet.resolveToList(null,
                          null, null, 10);
              Vector<String> metaCUIs = new Vector<String>();
              for (int i = 0; i < rcrl.getResolvedConceptReferenceCount(); i++) {
                    ResolvedConceptReference rcr = rcrl
                                .getResolvedConceptReference(i);
                    String metaCUI = rcr.getCode();
                    metaCUIs.add(metaCUI);
              }

              LocalNameList lnl = new LocalNameList();
              lnl.addEntry("UMLS_CUI");
              lnl.addEntry("NCI_META_CUI");
              for (String metaCUI : metaCUIs) {
                    count++;
//                    boolean found = matchAttributeValue(lnl, metaCUI);
                    System.out.println("found count " + count);
              }

              evsLookupDone = true;
        } catch (LBException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
        }
        return count;
  }
    
    private void testSentinelTool(){
    	String[] report = new AuditConceptToEVS().getReportRows(evsService);
    	for(int i=0; i<report.length;i++){
    	System.out.println(report[i]);
    	}
    }

}
