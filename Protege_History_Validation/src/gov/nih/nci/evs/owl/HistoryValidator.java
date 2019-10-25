/**
 * National Cancer Institute Center for Bioinformatics
 * 
 * Protege_History_Validation
 * gov.nih.nci.owl
 * HistoryValidator.java
 * Aug 11, 2009
 *
 */
/** <!-- LICENSE_TEXT_START -->
 The Protege_History_Validation Copyright 2009 Science Applications International Corporation (SAIC)
 Copyright Notice.  The software subject to this notice and license includes both human readable source code form and machine readable, binary, object code form (the EVSAPI Software).  The EVSAPI Software was developed in conjunction with the National Cancer Institute (NCI) by NCI employees and employees of SAIC.  To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 This Protege_History_Validation Software License (the License) is between NCI and You.  You (or Your) shall mean a person or an entity, and all other entities that control, are controlled by, or are under common control with the entity.  Control for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.
 This License is granted provided that You agree to the conditions described below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights in the Protege_History_Validation Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly display, publicly perform, and prepare derivative works of the EVSAPI Software; (ii) distribute and have distributed to and by third parties the EVSAPI Software and any modifications and derivative works thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such rights to further third parties.  For sake of clarity, and not by way of limitation, NCI shall have no right of accounting or right of payment from You or Your sublicensees for the rights granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions and the disclaimer and limitation of liability of Article 6, below.  Your redistributions in object code form must reproduce the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials provided with the distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: This product includes software developed by SAIC and the National Cancer Institute.  If You do not include such end-user documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" Science Applications International Corporation and "SAIC" to endorse or promote products derived from this Software.  This License does not authorize You to use any trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and into any third party proprietary programs.  However, if You incorporate the Software into third party proprietary programs, You agree that You are solely responsible for obtaining any permission from such third parties required to incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including without limitation Your end-users, of their obligation to secure any required permissions from such third parties before incorporating the Software into such third party proprietary software programs.  In the event that You fail to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to the extent prohibited by law, resulting from Your failure to obtain such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * <!-- LICENSE_TEXT_END -->
 */
package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.Change.ChangeType;
import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author safrant
 */
public class HistoryValidator {
	private static String defaultNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	// reads the System properties to get the location of the
	// ProtegeHistoryQA.properties file
	/** The sys prop. */
	private static Properties sysProp = System.getProperties();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HistoryValidator diff = new HistoryValidator();
		try {
			diff.configure(args);

		} catch (Exception e) {
			e.printStackTrace();
		}

		diff.validateHistory(true);
		diff.validateHistory(false);
		diff.closePrintWriter();
	}

	/** The config file. */
	private String configFile = null;

	/** The pw. */
	private PrintWriter pw;
	URI conceptHistory = null;
	OWLKb current = null;
	URI currentFile = null;
	HistoryFile evs_history = null;
	URI evsHistory = null;
	HistoryFile history = null;
	String namespace = null;
	URI outfile = null;
	OWLKb previous = null;
	URI previousFile = null;
	URI retiredRoot = URI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C28428");
	
//	HashMap<String,String> currentCodeMap = new HashMap<String,String>();
//	HashMap<String,String> previousCodeMap = new HashMap<String,String>();
	URI codePropertyURI = URI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#NHC0");

	private void checkEVSHistory() {

	}

	private void checkForCreates(boolean isConceptHistory) {

		HashSet<URI> noCreateRecord = new HashSet<URI>();
		Collection<URI> diff = difference(current.getAllConcepts(), previous
				.getAllConcepts());
		
		
		for (URI code : diff) {
//			String NCICode = current.getPropertyValues(code, codePropertyURI).get(0);
//			String NCIcode = current.getNCIConceptCodeFromByName(code);
			HistoryConcept hConcept;
			if (isConceptHistory) {
				hConcept = history.getHistoryConcept(code.getFragment());
			} else {
				hConcept = evs_history.getHistoryConcept(code.getFragment());
			}
			if (hConcept != null) {
				if (!hConcept.checkForCreateRecord()) {
					noCreateRecord.add(code);
				}
			} else {
				noCreateRecord.add(code);
			}
		}

		pw.println("Concepts that have been created with no create records");

		Set<URI> treeset = new TreeSet<URI>(noCreateRecord);
		for (URI code : treeset) {
			String conceptName = current.getConceptNameByCode(code);
//			String NCICode = current.getPropertyValues(code, codePropertyURI).get(0);

			pw.println("  " + code.getFragment() + " " + conceptName);
		}

		pw.flush();
		

		noCreateRecord = new HashSet<URI>();
		for (URI conceptCode : current.getAllConcepts().keySet()){
			try{
//			String NCICode = current.getPropertyValues(conceptCode, codePropertyURI).get(0);
			HistoryConcept hConcept;
			if (isConceptHistory) {
				hConcept = history.getHistoryConcept(conceptCode.getFragment());
			} else {
				hConcept = evs_history.getHistoryConcept(conceptCode.getFragment());
			}
			if (hConcept != null) {
				if (!hConcept.checkForCreateRecord()) {
                    //C25839 is first record with a create - 2002-12-18 in evs-history
                    int intConceptCode = new Integer(conceptCode.getFragment().substring(1));
				    if(!isConceptHistory && intConceptCode>25829) {
                        noCreateRecord.add(conceptCode);
                    }
				}
			} else {
				noCreateRecord.add(conceptCode);
			}
			}
			catch(Exception e){
				System.out.println("checkForCreates: No match found for "+ conceptCode + " in current");
			}
		}
		
		pw.println();
		pw.println("Concepts that exist with no create records");
		
		treeset = new TreeSet<URI>(noCreateRecord);
		for (URI code : treeset) {
			String conceptName = current.getConceptNameByCode(code);
//			String NCICode = current.getPropertyValues(code, codePropertyURI).get(0);
			pw.println("  " + code.getFragment() + " " + conceptName);
		}

		pw.flush();
		
	}

	private void checkForMerges(boolean isConceptHistory) {
		pw.println("\nConcepts that have been merged but not properly retired");
		Vector<HistoryConcept> mergedConcepts = new Vector<HistoryConcept>();

		if (isConceptHistory) {
			mergedConcepts = history.getMerges();
		} else {
			mergedConcepts = evs_history.getMerges();
		}

		for (HistoryConcept hConcept : mergedConcepts) {
			String conCode = hConcept.conceptCode;
			Vector<HistoryRecord> hRecords = hConcept
					.getHistoryRecordsByType(ChangeType.MERGE);

			for (HistoryRecord hRecord : hRecords) {
				String refCode = hRecord.getReferenceCode();
				if (!refCode.equals(conCode)) {
					// Check if the concept is now retired
					boolean isRetired = hConcept.checkForRetireRecord();
					if (isRetired) {
						// check if the concept has a Retire parent
//						String NCIcode = currentCodeMap.get(conCode);

						try{
						URI conceptURI = current.createURI(conCode, defaultNamespace);
						boolean deprecated = current.isDeprecated(conceptURI);
						
						// boolean nowDeprecated =
						// current.isDeprecated(conCode);
						if (!deprecated) {

							if (!current.conceptExists(conceptURI)) {
								pw
										.println(conCode + " merged to "
												+ refCode + " but " + conCode
												+ " does not exist");
							} else {
								pw.println(conCode + " merged to " + refCode
										+ " but not in Retired branch ");
							}
						}
						}
						catch (Exception e){
							System.out.println("checkForMerges: No match found for "+ conCode + " in current");
						}
					} else {
						pw.println(refCode + " merged to " + conCode
								+ " but has no retire record");
					}
				}
			}
		}
		pw.flush();
	}

	private void checkForModifies(boolean isConceptHistory) {
		HashSet<URI> noModifyRecord = new HashSet<URI>();
		for (URI conceptCode : current.getAllConcepts().keySet()) {
			ConceptProxy c1 = current.getConcept(conceptCode);
			ConceptProxy c2 = previous.getConcept(conceptCode);

			// If the changes vector is empty, then there were no edits.
			// Check to see if there is a history record anyway
			HistoryConcept hConcept;
//			String NCICode = c1.getCode();

			if (isConceptHistory) {
				hConcept = history.getHistoryConcept(conceptCode.getFragment());
			} else {
				hConcept = evs_history.getHistoryConcept(conceptCode.getFragment());
			}
			// If the changes vector is not empty, then there should be a
			// history record
			if(c1!=null && c2!=null) {
			int different = c1.compareTo(c2);
			if (different != 0) {
				// There were changes. Is there a history record?
				if (hConcept != null) {
					if (!hConcept.checkForModifyRecord()) {
						noModifyRecord.add(conceptCode);
					}
				} else {
					noModifyRecord.add(conceptCode);
				}

			}}
			else {
				String debug="true";
			}
		}
		pw.println("\nConcepts that have been modified with no modify records");
		pw.println();
		for (URI code : noModifyRecord) {
			String conceptName = current.getConceptNameByCode(code);
			pw.println("  " + code + " " + conceptName);
		}

		pw.flush();
	}

	private void checkForRetires(boolean isConceptHistory) {
		pw
				.println("\nConcepts that have a retire record but are not in Retired branch");
		Vector<HistoryConcept> retiredConcepts;
		if (isConceptHistory) {
			retiredConcepts = history.getRetired();
		} else {
			retiredConcepts = evs_history.getRetired();
		}
		for (HistoryConcept hConcept : retiredConcepts) {
			String conCode = hConcept.conceptCode;
			// check if the concept has a Retire parent
			try{
				URI conceptURI = current.createURI(conCode, defaultNamespace);
//			String NCIcode = currentCodeMap.get(conceptURI);
			boolean deprecated = current.isDeprecated(conceptURI);
			if (!deprecated) {
				
				if (!current.conceptExists(conceptURI)) {
					pw.println(conCode
							+ " has retire record, but does not exist");
				} else {
					pw.println(conCode
							+ " has retire record, but is not in retired kind");
				}
			}
			} catch(Exception e){
				System.out.println("checkForRetires: No match found for " + conCode + " in current");
				pw.println(conCode
						+ " has retire record, but does not exist");
			}
		}

		// check if retired concept has a retirement record.
		HashSet<URI> noRetireRecord = new HashSet<URI>();
		HashSet<URI> noHistoryRecord = new HashSet<URI>();
		for (URI conceptCode : current.getAllConcepts().keySet()) {
			try{
//			String NCICode = current.getPropertyValues(conceptCode, codePropertyURI).get(0);
			// If the changes vector is empty, then there were no edits.
			// Check to see if there is a history record anyway
			HistoryConcept hConcept;
			if (isConceptHistory) {
				hConcept = history.getHistoryConcept(conceptCode.getFragment());
			} else {
				hConcept = evs_history.getHistoryConcept(conceptCode.getFragment());
			}
			boolean isRetired = true;
			if (hConcept == null) {
				noHistoryRecord.add(conceptCode);
				isRetired = false;
			} else {
				isRetired = hConcept.checkForRetireRecord();
			}
			// If the changes vector is not empty, then there should be a
			// history record
			// int different = c1.compareTo(c2);

			if (current.isDeprecated(conceptCode)) {
				if (!isRetired) {
					noRetireRecord.add(conceptCode);
				}
			}
			} catch(Exception e){
				System.out.println("checkForRetires: No match found for " + conceptCode.getFragment() + " in current");
				if (conceptCode.getFragment().startsWith("C")){
				pw.println(conceptCode
						+ " has retire record, but does not exist");}
			}
		}
		pw.println("\nConcepts in Retired branch with no retire records");
		for (URI code : noRetireRecord) {
			String conceptName = current.getConceptNameByCode(code);
//			String conceptCode = current.getPropertyValues(code, codePropertyURI).get(0);
			pw.println("  " + code.getFragment() + " " + conceptName);
		}

		pw.flush();

		pw.println();
		pw.println();
		pw.println("Concepts that have no history records");
		for (URI code : noHistoryRecord) {
			String conceptName = current.getConceptNameByCode(code);
//			String conceptCode = current.getPropertyValues(code, "code").get(0);
			pw.println(" " + code.getFragment() + " " + conceptName);

		}
		pw.flush();
	}

	private void checkForSplits(boolean isConceptHistory) {

		pw.println();
		pw.println();
		pw
				.println("Concepts that have been split, but the new concept was not properly created.");
		Vector<HistoryConcept> splitConcepts;
		if (isConceptHistory) {
			splitConcepts = history.getSplits();
		} else {
			splitConcepts = evs_history.getSplits();
		}

		for (HistoryConcept hConcept : splitConcepts) {
			String conCode = hConcept.conceptCode;
			Vector<HistoryRecord> hRecords = hConcept
					.getHistoryRecordsByType(ChangeType.SPLIT);
			for (HistoryRecord hRecord : hRecords) {
				String refCode = hRecord.getReferenceCode();
				if (!refCode.equals(conCode)) {
					// Check if the concept is now retired
					HistoryConcept createdConcept = history
							.getHistoryConcept(refCode);
					if (createdConcept == null) {
						pw.println(refCode + " split from " + conCode
								+ " but new concept does not exist");
					} else {
						boolean isCreated = createdConcept
								.checkForCreateRecord();

						if (!isCreated) {
							pw.println(refCode + " split from " + conCode
									+ " but has no create record");
						}
					}
				}
			}
		}
		pw.flush();
	}

	/**
	 * @param outputFile
	 */
	private void configPrintWriter(URI outputFile) {
		try {
			final File file = new File(outputFile);
//			pw = new PrintWriter(file);
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8")),true);
		} catch (final Exception e) {
			System.out.println("Error in PrintWriter");
		}
	}

	private void configure(String[] args) {
		try {
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String option = args[i];
					if (option.equalsIgnoreCase("--help")) {
						printHelp();
					} else if (option.equalsIgnoreCase("-u")
							|| option.equalsIgnoreCase("--current")) {
						currentFile = new URI(args[++i]);
					} else if (option.equalsIgnoreCase("-p")
							|| option.equalsIgnoreCase("--previous")) {
						previousFile = new URI(args[++i]);
					} else if (option.equalsIgnoreCase("-c")
							|| option.equalsIgnoreCase("--concept_history")) {
						conceptHistory = new URI(args[++i]);
					} else if (option.equalsIgnoreCase("-e")
							|| option.equalsIgnoreCase("--evs_history")) {
						evsHistory = new URI(args[++i]);
					} else if (option.equalsIgnoreCase("-o")
							|| option.equalsIgnoreCase("--output")) {
						outfile = new URI(args[++i]);
					} else if (option.equalsIgnoreCase("-g")
							|| option.equalsIgnoreCase("--configFile")) {
						configFile = args[++i];
					}
				}
			}

			Properties props = new Properties();
			if (configFile == null) {
				final String filename = sysProp
						.getProperty("ProtegeHistoryQA.properties");
				configFile = filename;
			} 
			//if no configFile address has been entered, do a default
			if (configFile == null) {
				configFile = "./config/ProtegeHistoryQA.properties";
			}
			System.out.println("Config file at: " + configFile);
			props.load(new FileInputStream(configFile));
			namespace = props.getProperty("DefaultNamespace");
//			retiredRoot = props.getProperty("Retired_Root");

			// If parameters were not passed in, then try and read them from
			// config file
			if (currentFile == null) {
				currentFile = new URI(props.getProperty("current"));
			}
			if (previousFile == null) {
				previousFile = new URI(props.getProperty("previous"));
			}
			if (outfile == null) {
				outfile = new URI(props.getProperty("output"));
			}
			if (conceptHistory == null) {
				conceptHistory = new URI(props.getProperty("concept_history"));
			}
			if (evsHistory == null) {
				evsHistory = new URI(props.getProperty("evs_history"));
			}

			System.out.println("Output printed to " + outfile.toString());

			configPrintWriter(outfile);
			pw.println("Diff of two OWL files");
			pw.println("Current File is " + currentFile.toString());
			pw.println("Previous File is " + previousFile.toString());
			pw.println("evs_history file is " + evsHistory.toString());
			pw.println("concept_history file is " + conceptHistory.toString());

			pw.println();
			pw.println("--------------------------------------------");

			configureKb();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to configure.  Aborting.");
			System.exit(1);
		}
	}


	private void configureKb() {
		current = new OWLKb(currentFile, namespace);
		if (current == null) {
			System.out.println("Unable to instantiate OWLKb for current URI");
			System.exit(0);
		}

		previous = new OWLKb(previousFile, namespace);
		if (previous == null) {
			System.out.println("Unable to instantiate OWLKb for previous URI");
			System.exit(0);
		}
		// C28428 is the code for Retired_Concepts
		previous.setDeprecatedBranch(retiredRoot);
		current.setDeprecatedBranch(retiredRoot);

		history = new HistoryFile(conceptHistory, Change.HistoryType.CONCEPT);
		if (history == null) {
			System.out.println("Unable to read history file");
			System.exit(0);
		}

		evs_history = new HistoryFile(evsHistory, Change.HistoryType.EVS);
		if (evs_history == null) {
			System.out.println("Unable to read EVS history file");
			System.exit(0);
		}
		
		//Create id to NCICode map. - In case we have passed in a byName file
		//This will make sure we have a code to match against history files
		
		Set<URI> currentCodes = current.getAllConceptCodes();
//		for(URI code:currentCodes) {
//			try{
////			String NCICode = current.getPropertyValues(code, "code").get(0);
//			currentCodeMap.put(code.getFragment(), code);}
//			catch(Exception e){
//				System.out.println("configureKb: No match found for " + code + " in current");
//			}
//		}
		
		
		Set<URI> previousCodes = previous.getAllConceptCodes();
//		for(URI code:previousCodes) {
//			try{
////			String NCICode = previous.getPropertyValues(code, "code").get(0);
//			previousCodeMap.put(code.getFragment(), code);}
//			catch(Exception e){
//				System.out.println("configureKb: No match found for " + code + " in previous");
//			}
//		}
	}

	@SuppressWarnings("unchecked")
	private Collection<URI> difference(
			HashMap<URI, ConceptProxy> hashMap1,
			HashMap<URI, ConceptProxy> hashMap2) {
		Collection<URI> result = CollectionUtils.disjunction(hashMap1
				.keySet(), hashMap2.keySet());
		return result;
	}

	private void printHelp() {
		System.out.println("");
		System.out.println("Usage: java -jar historyvalidator.jar [OPTIONS] ");
		System.out
				.println("The input and output URIs are specified in owlscrubber.properties");
		System.out.println(" ");
		System.out
				.println("  -G [configFile], --configFile [configFile]\tTells where to find ProtegeHistoryQA.properties file");
		System.out
				.println("  -P [previousURI], --Previous [previousURI]\t\tURI of previous Thesaurus owl file");
		System.out
				.println("  -U [currentURI], --Current [currentURI]\t\tURI of current Thesaurus owl file");
		System.out
				.println("  -C [conceptHistoryURI], --concept_history [conceptHistoryURI]\t\tURI of concept_history file");
		System.out
				.println("  -E [EvsHistoryURI], --evs_history [EvsHistoryURI]\t\tURI of evs_history file");
		System.out
				.println("  -O [outputURI], --Output [outputURI]\t\tURI to write the output file");
		System.out.println("");
		System.exit(1);
	}

	private void validateHistory(boolean isConceptHistory) {
		if (isConceptHistory) {
			pw.println("Comparison to Concept History");
		} else {
			pw.println();
			pw.println();
			pw.println("**********************************************");
			pw.println("Comparison to EVS History");
			pw.println();
		}

		// concepts that have been created
		checkForCreates(isConceptHistory);

		// Compare the two kb and see if there are history records to match
		checkForModifies(isConceptHistory);

		// See if there are history records without any matching edits

		// check for splits in history. Make sure there is a new concept to
		// match
		// the non-paired reference code
		checkForSplits(isConceptHistory);

		// check for merges in history. Make sure there is a retire record and a
		// deprecated concept to match.
		checkForMerges(isConceptHistory);

		// check for retires in history. Maker sure there is a retire record to
		// match.
		checkForRetires(isConceptHistory);

		pw.flush();
	}

	private void closePrintWriter() {
		pw.close();
	}
}
