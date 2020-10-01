package gov.nih.nci.evs.owl;

import gov.nih.nci.evs.owl.data.OWLKb;

import java.net.URI;
import java.net.URISyntaxException;

public class ConvertNCIt2OWL2 {
	final String ontologyNamespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";

	public ConvertNCIt2OWL2(URI url, boolean toCode) throws URISyntaxException {

		String path = url.getRawPath();
		String newFile = path + "2";

		OWLKb owlKb1 = new OWLKb(url, this.ontologyNamespace);
		if (toCode) {
			owlKb1.convertToCode();
		}

		owlKb1.saveOntology(newFile);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		boolean convertToCode = false;

		try {

			if (args.length > 1) {
				if (args[1].toUpperCase().equals("T")
				        || (args[1].toUpperCase().equals("TRUE"))) {
					convertToCode = true;
				}
			}
			if (args.length > 0) {
				String urlString = args[0];
				URI url = new URI(urlString);
				new ConvertNCIt2OWL2(url, convertToCode);
			} else {
				System.out
				        .println("Must pass in the url to the original owl file");
			}

		} catch (URISyntaxException e) {

			System.out.println("URI is malformed " + args[0]);
		}

	}

	public static String draftOutputFileName(URI url) {
		// Will be writing the output to the same directory, but with a new name
		String outputURL = "";
		String[] pathArray = url.toString().split("/");
		int last = pathArray.length;
		String originalFileName = pathArray[last - 1];
		String newFileName = "owl2_" + originalFileName;
		CharSequence newPathName = url.toString().subSequence(0,
		        url.toString().lastIndexOf("/"));
		outputURL = newPathName.toString() + "/" + newFileName;

		return outputURL;
	}

}
