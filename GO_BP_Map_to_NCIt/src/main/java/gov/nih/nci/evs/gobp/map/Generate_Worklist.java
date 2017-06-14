package gov.nih.nci.evs.gobp.map;

import org.apache.log4j.Logger;

public class Generate_Worklist {

	private org.apache.log4j.Logger logger = Logger.getLogger(gov.nih.nci.evs.gobp.map.Generate_Worklist.class);

	public Generate_Worklist() {
		// TODO - implement Generate_Worklist.Generate_Worklist
		throw new UnsupportedOperationException();
	}
/**
 * This should take the reviewed mapping file and turn it into worklists
 * This could be in the form of batch edits or an actual sheet with a list of changes to be made
 * 
 * input :  Map review sheet
 * Output:  worksheets and/or batch edits
 * 
 * 
 * Note:  We haven't decided if we will reimport GO each time or just add new GO changes somehow
 * 
 * If we reimport a new GO each month, we will need to reassert relationships to the GO concepts.  This can be done
 * via batch edit
 * 
 * Let's say X is a GO concept and Y is a NCIt concept.  X' is a changed GO concept
 * 
 *  	If Y refers to X, then we need to reassert the existing reference
 *  	If Y refers to X', then we need to review and reassert the existing reference
 */
}
