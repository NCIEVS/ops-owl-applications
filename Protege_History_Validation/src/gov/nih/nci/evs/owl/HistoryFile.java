/**
 * National Cancer Institute Center for Bioinformatics
 * 
 * Protege_History_Validation
 * gov.nih.nci.owl
 * HistoryFile.java
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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * @author safrant
 * 
 */
public class HistoryFile {

	HashMap<String, HistoryConcept> historyMap = new HashMap<String, HistoryConcept>();
	Change.HistoryType type;

	/**
	 * @param fileLoc
	 */
	public HistoryFile(URI fileLoc, Change.HistoryType type) {
		try {
			this.type=type;
			if(type== Change.HistoryType.CONCEPT) {
				readBufferedConceptHistory(fileLoc);
			} else {
				readBufferedEVSHistory(fileLoc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Change.HistoryType getHistoryType(){
		return this.type;
	}

	private void readBufferedConceptHistory(URI fileLoc){
		try (Stream<String> stream = Files.lines(Paths.get(fileLoc))) {

				stream.forEach(line -> parseConceptHistoryLine(line));

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	}

	private void readBufferedEVSHistory(URI fileLoc){
		try (Stream<String> stream = Files.lines(Paths.get(fileLoc))) {

			stream.forEach(line -> parseEVSHistoryLine(line));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parseConceptHistoryLine(String historyLine){
		//code edit date ref

		String delimiter = "\t";
		if(historyLine.indexOf(delimiter)<1) {
			delimiter = "\\|";
		}
		String[] historyArray = historyLine.split(delimiter);
		if (historyArray.length>=3){
		String code = historyArray[0];
		int arrayPosition = 1;

//		//If this is the pipe delimited format then the second position will be empty
//			//We will need to advance to the third.
//		if(historyArray[1].length()==0){
//			arrayPosition =2;
//		}

		String changeType = historyArray[1];
		String stringDate = historyArray[2];
		Date date = parseHistoryDate(stringDate);
		String refCode = null;
		if(historyArray.length==4) {
			refCode = historyArray[3].trim();
		}
		if (historyMap.containsKey(code)) {
			historyMap.get(code).addHistoryRecord(date,
					Change.toChangeType(changeType), refCode);
			// System.out.println("Add record to concept " + code);
		} else {
			HistoryConcept concept = new HistoryConcept(code);
			concept.addHistoryRecord(date, Change
					.toChangeType(changeType), refCode);
			historyMap.put(code, concept);
			// System.out.println("Add concept " + code);
		}
		}
	}

	private void parseEVSHistoryLine(String historyLine){
		// Date editor code PN edit ref(opt)
		String delimiter = "\t";
		if(historyLine.indexOf(delimiter)<1) {
			delimiter = "\\|";
		}
		String[] historyArray = historyLine.split(delimiter);
		//check to make sure we have all 5 values
		if (historyArray.length>=5){
			String code = historyArray[2];
			String changeType = historyArray[4];
			String stringDate = historyArray[0];
			Date date = parseHistoryDate(stringDate);
			String refCode = "null";
			if(historyArray.length==6){
				//There is a reference code
				refCode = historyArray[5];
			}
			if (historyMap.containsKey(code)) {
				historyMap.get(code).addHistoryRecord(date,
						Change.toChangeType(changeType), refCode.trim());
				// System.out.println("Add record to concept " + code);
			} else {
				HistoryConcept concept = new HistoryConcept(code);
				concept.addHistoryRecord(date, Change
						.toChangeType(changeType), refCode);
				historyMap.put(code, concept);
				// System.out.println("Add concept " + code);
			}
		}


	}

	/*
	 * Date is passed in as string  Is converted to actual
	 * java.util.Date
	 *
	 * 2003-08-12
	 * 2019-08-26 09:38:50
	 *
	 */
	private Date parseHistoryDate(String s) {
		try {
//			if(s.startsWith("20")){
//				return Date.valueOf(s);
//			}

			String year = s.substring(0, 4);
			String month = s.substring(6, 7);
			String day = s.substring(9, 10);
//			String year = "20" + s.substring(7);
//			String day = s.substring(0, 2);
//			String shortMonth = s.substring(3, 6);
//			String month = convertToDigiMonth(shortMonth.toLowerCase());
			// Needs to be passed in as yyyy-mm-dd
			String wholedate = year + "-" + month + "-" + day;
			String date = s.substring(0, 10);
			Date theDate = Date.valueOf(date);
			return theDate;
		} catch (Exception e) {
			return new Date(System.currentTimeMillis());
		}
	}

	private String convertToDigiMonth(String shortMonth) {
		if (shortMonth.equals("jan"))
			return "01";
		if (shortMonth.equals("feb"))
			return "02";
		if (shortMonth.equals("mar"))
			return "03";
		if (shortMonth.equals("apr"))
			return "04";
		if (shortMonth.equals("may"))
			return "05";
		if (shortMonth.equals("jun"))
			return "06";
		if (shortMonth.equals("jul"))
			return "07";
		if (shortMonth.equals("aug"))
			return "08";
		if (shortMonth.equals("sep"))
			return "09";
		if (shortMonth.equals("oct"))
			return "10";
		if (shortMonth.equals("nov"))
			return "11";
		if (shortMonth.equals("dec"))
			return "12";
		return "01";
	}

	/**
	 * @param conceptCode
	 * @return
	 */
	public HistoryConcept getHistoryConcept(String conceptCode) {
		return historyMap.get(conceptCode);
	}

	public boolean historyRecordExists(String conceptCode) {
		HistoryConcept test = getHistoryConcept(conceptCode);
		if (test == null)
			return false;
		return true;
	}

	public Vector<HistoryConcept> getSplits() {
		Vector<HistoryConcept> splitHistory = new Vector<HistoryConcept>();
		Set<String> codes = historyMap.keySet();
		for (String code : codes) {
			HistoryConcept concept = historyMap.get(code);
			Vector<HistoryRecord> records = concept
					.getHistoryRecordsByType(ChangeType.SPLIT);
			if (records.size() > 0) {
				splitHistory.add(concept);
			}
		}
		return splitHistory;
	}

	public Vector<HistoryConcept> getMerges() {
		Vector<HistoryConcept> mergeHistory = new Vector<HistoryConcept>();
		Set<String> codes = historyMap.keySet();
		for (String code : codes) {
			HistoryConcept concept = historyMap.get(code);
			Vector<HistoryRecord> records = concept
					.getHistoryRecordsByType(ChangeType.MERGE);
			if (records.size() > 0) {
				mergeHistory.add(concept);
			}
		}
		return mergeHistory;
	}

	public Vector<HistoryConcept> getCreates() {
		Vector<HistoryConcept> splitHistory = new Vector<HistoryConcept>();
		Set<String> codes = historyMap.keySet();
		for (String code : codes) {
			HistoryConcept concept = historyMap.get(code);
			Vector<HistoryRecord> records = concept
					.getHistoryRecordsByType(ChangeType.CREATE);
			if (records.size() > 0) {
				splitHistory.add(concept);
			}
		}
		return splitHistory;
	}

	public Vector<HistoryConcept> getRetired() {
		Vector<HistoryConcept> splitHistory = new Vector<HistoryConcept>();
		Set<String> codes = historyMap.keySet();
		for (String code : codes) {
			HistoryConcept concept = historyMap.get(code);
			Vector<HistoryRecord> records = concept
					.getHistoryRecordsByType(ChangeType.RETIRE);
			if (records.size() > 0) {
				splitHistory.add(concept);
			}
		}
		return splitHistory;
	}

}
