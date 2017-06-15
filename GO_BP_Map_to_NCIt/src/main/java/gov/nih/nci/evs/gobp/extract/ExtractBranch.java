package gov.nih.nci.evs.gobp.extract;

import gov.nih.nci.evs.gobp.print.PrintOWL1;
import gov.nih.nci.evs.owl.data.OWLKb;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class ExtractBranch {

	private static org.apache.log4j.Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.extract.ExtractBranch.class);

/**
 * Don't look at me like that.  We are extracting BP from GO.  We know we are extracting
 * BP and only BP.  We also know that we want only internal roles - anything pointing to an outside
 * entity should be discarded
 * 
 * Input - the pretty OWL we created from the source.
 * Output - a pretty branch of biological processes
 */
	
	public static String extract(String owlFileLoc, String namespace,
			String focusConcept) throws IOException {
		//Take the focus concept and extract to a new ontology with that as the root
		String outputFileLoc;
		String path = owlFileLoc.substring(0,
				owlFileLoc.lastIndexOf(System.getProperty("file.separator")));

		if (!owlFileLoc.startsWith("file")) {
			owlFileLoc = "file://" + owlFileLoc;
		}

		OWLKb owlkb = new OWLKb(owlFileLoc, namespace);
		DateFormat dateFormat = new SimpleDateFormat("yy.MM.dd.HHmm");
		Date date = new Date();
		String sDate = dateFormat.format(date);
		
		outputFileLoc = path + "/Extraction_" + sDate + ".owl";
		
		//create a new ontology
		//read old ontology and find the root concept.  
		owlkb.removeBranch("GO_0003674");
		owlkb.removeBranch("GO_0005575");
		owlkb.removeBranch("Deprecated");
		//Get the descendants of the root and add each to the new ontology
		// owlkb.saveOntology(outputFileLoc);
		//Examine the old ontology and grab the axioms.  Any role axioms only keep if domain and range are in the new ontology
		
		//print out the extraction

		logger.info("Extraction done.  Printing to " + outputFileLoc);
		new PrintOWL1(owlkb, outputFileLoc, "./config/go_headerCode.txt"); // TODO
																			// make
																			// this
																			// a
																			// parameter
		return outputFileLoc;
	}
	
	
	public static void cleanBranches(OWLKb owlkb) {
		// remove any outgoing roles or references

	}

}
