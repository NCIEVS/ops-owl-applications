package gov.nih.nci.evs.browser.utils;

import gov.nih.nci.camod.util.RemoteServerUtil;
import gov.nih.nci.evs.browser.utils.CodingSchemeDataUtils;
import gov.nih.nci.evs.browser.utils.ResolvedConceptReferencesIteratorWrapper;
import gov.nih.nci.evs.browser.utils.ValueSetSearchUtils;
import gov.nih.nci.evs.browser.utils.StringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.valueSets.ValueSetDefinition;
import org.apache.log4j.Logger;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;



/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by NGIT and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or NGIT
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 *          Modification history Initial implementation kim.ong@ngc.com
 *
 */

public class ValueSetDefUtils {

	private static Logger _logger = Logger.getLogger(ValueSetDefUtils.class);
	private List valueSetDefinitionURIList = null;
	LexBIGService lbSvc = null;
	LexEVSValueSetDefinitionServices vsd_service = null;
	HashMap vsdUri2NameMap = null;

	public ValueSetDefUtils(LexBIGService lbSvc,
	        LexEVSValueSetDefinitionServices vsd_service) {
		this.lbSvc = lbSvc;
		this.vsd_service = vsd_service;
		this.valueSetDefinitionURIList = this.vsd_service
		        .listValueSetDefinitionURIs();
		this.createVsdUri2NameMap();
	}

	public void createVsdUri2NameMap() {
		this.vsdUri2NameMap = new HashMap();
		for (int i = 0; i < this.valueSetDefinitionURIList.size(); i++) {
			String vsd_uri = (String) this.valueSetDefinitionURIList.get(i);
			ValueSetDefinition vsd = this.findValueSetDefinitionByURI(vsd_uri);
			String vsd_name = vsd.getValueSetDefinitionName();
			this.vsdUri2NameMap.put(vsd_uri, vsd_name);
		}
	}

	public String uri2Name(String uri) {
		return (String) this.vsdUri2NameMap.get(uri);
	}

	public void exportValueSetDefinitions(String directory_name) {

		String user_dir = System.getProperty("user.dir");
		String dir_pathname = user_dir + File.separator + directory_name;
		File dir = new File(dir_pathname);
		if (!dir.exists()) {
			System.out.println("Creating directory: " + directory_name);
			boolean result = false;
			try {
				dir.mkdir();
				result = true;
			} catch (SecurityException ex) {
				ex.printStackTrace();
				return;
			}
			if (result) {
				System.out.println("Directory " + directory_name + " created.");
			}
		}
		Vector error_vec = new Vector();
		PrintWriter pw = null;
		for (int i = 0; i < this.valueSetDefinitionURIList.size(); i++) {
			String vsd_uri = (String) this.valueSetDefinitionURIList.get(i);
			String vsd_name = (String) this.vsdUri2NameMap.get(vsd_uri);
			vsd_name = vsd_name.replaceAll(" ", "_");
			vsd_name = vsd_name.replaceAll("/", "_");
			vsd_name = vsd_name.replaceAll(":", "_");
			String xmlfile = dir_pathname + File.separator + vsd_name + ".txt";
			String t = this.valueSetDefinition2XMLString(vsd_uri);
			try {
				pw = new PrintWriter(xmlfile, "UTF-8");
				pw.println(t);
				System.out.println("XML file " + xmlfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
				error_vec.add(xmlfile);
			}
			finally {
				try {
					pw.close();
					int j = i + 1;
					System.out.println("(" + j + ") Output file " + xmlfile
					        + " generated.");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("\nErrors: ");
		for (int i = 0; i < error_vec.size(); i++) {
			int j = i + 1;
			String t = (String) error_vec.elementAt(i);
			System.out.println("(" + j + ") " + t);
		}
	}

	public String valueSetDefinition2XMLString(String uri) {
		String s = null;
		String valueSetDefinitionRevisionId = null;
		try {
			URI valueSetDefinitionURI = new URI(uri);
			StringBuffer buf = this.vsd_service.exportValueSetDefinition(
			        valueSetDefinitionURI, valueSetDefinitionRevisionId);
			s = buf.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}

	public ValueSetDefinition findValueSetDefinitionByURI(String uri) {
		String valueSetDefinitionRevisionId = null;
		try {
			ValueSetDefinition vsd = this.vsd_service.getValueSetDefinition(
			        new URI(uri), valueSetDefinitionRevisionId);
			return vsd;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public List listValueSetDefinitionURIs() {
		return this.vsd_service.listValueSetDefinitionURIs();
	}

	public Vector getResovedValueSetVersions() {
		CodingSchemeDataUtils csdu = new CodingSchemeDataUtils(this.lbSvc);
		List list = this.listValueSetDefinitionURIs();
		Vector v = new Vector();
		int j = 0;
		for (int i = 0; i < list.size(); i++) {
			String uri = (String) list.get(i);
			String name = this.uri2Name(uri);
			j++;
			String version = csdu.getVocabularyVersionByTag(uri, "PRODUCTION");
			v.add(uri + "|" + name + "|" + version);
			System.out
			        .println("(" + j + ")" + uri + "|" + name + "|" + version);
		}
		return v;
	}

	public ResolvedConceptReferencesIterator search(Vector<String> schemes,
	        Vector<String> versions, String matchText, int searchOption,
	        String algorithm) throws LBException {
		SimpleSearchUtils ssu = new SimpleSearchUtils(this.lbSvc);

		for (int i = 0; i < versions.size(); i++) {
			String version = versions.elementAt(i);
			if (version == null || version.compareTo("null") == 0) {
				version = null;
				versions.set(i, null);
			}
		}
		return ssu
		        .search(schemes, versions, matchText, searchOption, algorithm);
	}

	public ResolvedConceptReferencesIterator search(String scheme,
	        String version, String matchText, int searchOption, String algorithm)
	        throws LBException {
		SimpleSearchUtils ssu = new SimpleSearchUtils(this.lbSvc);
		Vector schemes = new Vector();
		schemes.add(scheme);
		Vector versions = new Vector();
		if (version == null || version.compareTo("null") == 0) {
			version = null;
		}
		versions.add(version);
		return ssu
		        .search(schemes, versions, matchText, searchOption, algorithm);
	}

	public ResolvedConceptReferencesIterator valueSetSearchAction(
	        String checked_vocabularies, String matchText, int searchOption,
	        String algorithm) {
		ResolvedConceptReferencesIterator iterator = null;
		try {
			iterator = new ValueSetSearchUtils(this.lbSvc)
			        .searchResolvedValueSetCodingSchemes(checked_vocabularies,
			                matchText, searchOption, algorithm);
			if (iterator == null) {
				System.out.println("Iterator == null -- no matches.");
			} else {
				try {
					int numRemaining = iterator.numberRemaining();
					if (numRemaining == 0) {
						System.out.println("No matches.");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return iterator;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		// String output_dir = args[0];
		LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
		LexEVSValueSetDefinitionServices vsd_service = RemoteServerUtil
		        .createValueSetDefinitionServices();
		ValueSetDefUtils valueSetDefUtils = new ValueSetDefUtils(lbSvc,
		        vsd_service);

		PrintWriter pw = null;
		String outputfile = "search_results_STAGE_2.txt";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");

			// CDISC SDTM Anatomical Location Terminology (version: )

			Vector v = valueSetDefUtils.getResovedValueSetVersions();
			for (int i = 0; i < v.size(); i++) {
				String t = (String) v.elementAt(i);
				System.out.println(t);

				Vector u = StringUtils.parseData(t);
				String scheme = (String) u.elementAt(1);
				String version = (String) u.elementAt(2);
				String matchText = "cell";
				int searchOption = SimpleSearchUtils.BY_NAME;
				String algorithm = "contains";
				pw.println("\nValue set: " + scheme + " (version: " + version
				        + ")");
				System.out.println("\nvalue set: " + scheme);
				try {
					ResolvedConceptReferencesIterator wrapper = valueSetDefUtils
					        .search(scheme, version, matchText, searchOption,
					                algorithm);
					ResolvedConceptReferencesIterator iterator = wrapper;
					if (iterator != null) {
						try {
							int numRemaining = iterator.numberRemaining();
							System.out.println("Number of matches: "
							        + numRemaining);
							pw.println("\tNumber of matches: " + numRemaining);
						} catch (Exception ex) {
							ex.printStackTrace();
							pw.println("Exception thrown: " + scheme);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
