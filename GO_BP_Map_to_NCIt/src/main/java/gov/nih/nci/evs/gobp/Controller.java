package gov.nih.nci.evs.gobp;

import gov.nih.nci.evs.gobp.extract.ExtractBranch;
import gov.nih.nci.evs.gobp.extract.GO_to_OWL;
import gov.nih.nci.evs.gobp.merge.MergeController;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

public class Controller {
/*
 * This is the master controller of all the other pieces
 * 
 * Go.obo to OWL
prettyPrint
ExtractBranches
Remove extra targets
Massaging
compare to previous extract
Take any diff concepts and map to NCIt
Report out map in a reviewable form
Take in review and create a batch file update for NCIt or worklist, as appropriate
 * 
 */
	
	final static String go_namespace = "http://purl.obolibrary.org/obo/go.owl";
	// final static String go_namespace = "http://purl.obolibrary.org/obo";
	
	final static String nci_namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
	
	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.Controller.class);

	/**
	 * Main method of the entire GO converting, extracting and mapping program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Pass in the location of the input file
		// eventually - pass in "b" for obo or "w" for owl
		// call configuration to get the header file and any other configurable
		// items
		try {
			// TODO make previous file optional. Better error checking
			// Check that files exist early on?
			String fileLoc = args[0];
			String previousFileString = args[1];
			String ncitFileString = args[2];
			
			// check that the format is valid

			String tempLoc = fileLoc;
			if (!fileLoc.startsWith("file")) {
				tempLoc = "file://" + fileLoc;
			}
			URI uri = new URI(tempLoc);

			//check that they've passed in an actual filename
			File file = new File(uri);
			if (file.isFile())
			{

				String mappingFile = file.getParent() + "/Mapping";
				//send to the OBO processor.  It will be checked for valid format there.
				//TODO possibly pass back the string location of the new file
				// TODO fix this to pass in the file we created??
				String owlFile = new GO_to_OWL(go_namespace).load(fileLoc);
				
				/*
				 * do extraction and pretty it up
				 * Remove the kind, if present
				 * 
				 * TODO Leave open for ability to switch to OWL2 file when ready
				 * TODO The presence of "intersection_of" indicates necessary and sufficient - a defined class
				 */
				
				//TODO replace with real identifies for biological process
				String extractionFile = ExtractBranch.extract(owlFile,
						go_namespace, "biological_process");
				
				
				
				//Diff current GO to previous
				// if (!extractionFile.startsWith("file")) {
				// extractionFile = "file://" + extractionFile;
				// }
				// URI currentFile = URI.create(extractionFile);
				// if (!previousFileString.startsWith("file")) {
				// previousFileString = "file://" + previousFileString;
				// }
				// URI previousFile = URI.create(previousFileString);
				// URI printDiff = URI.create(extractionFile + "_diff.txt");
				// new gov.nih.nci.evs.gobp.diff.Diff_GO_BP(currentFile,
				// previousFile, go_namespace,
				// printDiff);
				// new NCIt_to_GO_autoMap(extractionFile, ncitFileString,
				// mappingFile);
				
				String exportedcsv = mappingFile + "/Thesaurus_exported.csv";
				String mergeOutput = mappingFile + "/MergedOutput.owl";
				new MergeController(ncitFileString, extractionFile,
						exportedcsv, mergeOutput);

			}
			else {
				logger.error("File location not valid.  Location passed in: " + fileLoc);
			}
			
			// new NCIt_to_GO_autoMap();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("You must pass in a file location");
			logger.error(
					"Program needs a current GO file and a previous extraction.  File location required",
					e);
		} catch (URISyntaxException e) {

			logger.error("FIle location must be a valid URI." , e);
			e.printStackTrace();
		} 
 catch (IOException e) {
			logger.error("Unable to access file", e);
			e.printStackTrace();
		}

	}
	


}
