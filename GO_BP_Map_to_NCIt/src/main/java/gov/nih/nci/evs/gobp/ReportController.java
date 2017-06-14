package gov.nih.nci.evs.gobp;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This is meant to pull a report showing 1. The concepts in the NCI BP tree
 * that have other concepts pointing to them 2. The concepts in the NCI BP tree
 * tagged with external stakeholders (def-source, term-source,
 * contributing_source, etc)
 **/
public class ReportController {
	private static Vector<String> bpConceptCodes = new Vector<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// open the owl file into owlapi and pull up the BP tree. check for any
		// axioms that target concepts within BP.
		// Separate into sources within and without BP

		// Check for axioms using the source properties and see if there are any
		// concepts in BP that use these.

		String bp = "Biological_Process";
		if (args.length == 1) {
			String fileLoc = args[0];
			try {
				URI fileURL = new URI(fileLoc);
				OWLKb owlkb = new OWLKb(fileURL,
						"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl");
				bpConceptCodes = owlkb.getAllDescendantsForConcept(bp);
				Collections.sort(bpConceptCodes);
				getThirdPartyStakeholders(owlkb);
				getAssociationsPointingOut(owlkb);
				getRolesPointingIn(owlkb);
				getRolesPointingOut(owlkb);

			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out
						.println("Please provide a valid URL for the file to be processed");
			}
		} else {
			System.out
					.println("Please provide the location of the Thesaurus file to be processed.");
		}

	}

	private static void getAssociationsPointingIn(OWLKb owlkb) {
		for (String conceptCode : bpConceptCodes) {
			Vector<Association> associations = owlkb
					.getAssociationsForTarget(owlkb.getConcept(conceptCode));

			List BPConceptsWithIncomingAssocs = new List();
			if (associations.size() > 0) {
				BPConceptsWithIncomingAssocs.add(conceptCode);
			}

			for (Association association : associations) {
				association.getSourceCode();
				System.out.println(association.getSourceCode() + "\t"
						+ association.getName() + "\t"
						+ association.getTargetCode());
			}
		}
	}

	private static void getAssociationsPointingOut(OWLKb owlkb) {
		try {

			Vector<String> BPConceptsWithOutgoingAssocs = new Vector<String>();
		for (String conceptCode : bpConceptCodes) {
			Vector<Association> associations = owlkb
					.getAssociationsForSource(owlkb.getConcept(conceptCode));


			if (associations.size() > 0) {
				BPConceptsWithOutgoingAssocs.add(conceptCode);
			}

		}

			PrintWriter pw = new PrintWriter("AssociationsPointedOut.txt");
			pw.println("There are " + BPConceptsWithOutgoingAssocs.size()
					+ " concepts with outgoing associations");
			for (String code : BPConceptsWithOutgoingAssocs) {
				pw.println(code);
			}
			pw.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Could not create output file");
		}
	}

	private static void getRolesPointingIn(OWLKb owlkb) {

		try {
			PrintWriter pw = new PrintWriter("RolesPointedIn.txt");
			Vector<String> BPConceptsWithIncomingRoles = new Vector<String>();
			Vector<String> BPConceptsWithOutsideRoles = new Vector<String>();
			Vector<String> BPConceptsWithBPRoles = new Vector<String>();
			TreeSet<String> ConceptsWithRolesPointedToBP = new TreeSet<String>();
		for (String conceptCode : bpConceptCodes) {


			Vector<Role> roles = owlkb.getRolesForTarget(owlkb
					.getConcept(conceptCode));

				if (roles.size() > 0) {
					BPConceptsWithIncomingRoles.add(conceptCode);

			}

				boolean hasExternalRoles = false;
				for (Role role : roles) {
					if (!bpConceptCodes.contains(role.getSourceCode())) {
						hasExternalRoles = true;
						ConceptsWithRolesPointedToBP.add(role.getSourceCode());
					}

					// pw.println(role.getSourceCode() + "\t" + role.getName()
					// + "\t" + role.getTargetCode());
				}

				if (hasExternalRoles) {
					BPConceptsWithOutsideRoles.add(conceptCode);
				} else if (!hasExternalRoles && roles.size() > 0) {
					BPConceptsWithBPRoles.add(conceptCode);
				}
			}
			//
			// pw.println("There are " + BPConceptsWithIncomingRoles.size()
			// + " with incoming roles");
			// for (String code : BPConceptsWithIncomingRoles) {
			// pw.println(code);
			// }

			pw.println();
			pw.println("There are " + BPConceptsWithOutsideRoles.size()
					+ " concepts with incoming roles from other branches");
			for (String code : BPConceptsWithOutsideRoles) {
				pw.println(code);
			}

			pw.println();
			pw.println("There are "
					+ BPConceptsWithBPRoles.size()
					+ " concepts with incoming roles only from within this branch");
			for (String code : BPConceptsWithBPRoles) {
				pw.println(code);
			}

			pw.println();
			pw.println("There are " + ConceptsWithRolesPointedToBP.size()
					+ " concepts that point to BP concepts");
			for (String code : ConceptsWithRolesPointedToBP) {
				pw.println(code);
			}

			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not create output file");
		}
	}

	private static void getRolesPointingOut(OWLKb owlkb) {

		try {
			PrintWriter pw = new PrintWriter("RolesPointedOut.txt");
			TreeSet<String> sameBranch = new TreeSet<String>();
			TreeSet<String> otherBranch = new TreeSet<String>();
			for (String conceptCode : bpConceptCodes) {

				ConceptProxy concept = owlkb.getConcept(conceptCode);
				if (concept.isDefined()) {
					pw.println(conceptCode + " is Defined");
				}
				Vector<Role> roles = owlkb.getRolesForSource(owlkb
						.getConcept(conceptCode));



				for (Role role : roles) {
					if (bpConceptCodes.contains(role.getTargetCode())) {
						sameBranch.add(conceptCode);
					} else {
						otherBranch.add(conceptCode);
					}

					// pw.println(role.getSourceCode() + "\t" + role.getName()
					// + "\t" + role.getTargetCode());
				}
			}


			pw.println("There are " + sameBranch.size()
					+ " with roles to other BP concepts");
			for (String code : sameBranch) {
				pw.println(code);
			}
			pw.println();
			pw.println("There are " + otherBranch.size()
					+ " with roles to outside concepts");
			for (String code : otherBranch) {
				pw.println(code);
			}

			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not create output file");
		}
	}

	private static void getThirdPartyStakeholders(OWLKb owlkb) {


		TreeSet<String> conceptsWithStakeholders = new TreeSet<String>();
		try {
			PrintWriter pw = new PrintWriter("HasStakeholders.txt");
			// get ALT_DEFS
			// get FULL_SYN SYs
			// get Contributing_Source
			for (String code : bpConceptCodes) {
				boolean hasStakeholder = false;
				ConceptProxy concept = owlkb.getConcept(code);
				Vector<Property> cs = concept
						.getProperties("Contributing_Source");
				if (cs.size() > 0) {
					hasStakeholder = true;
				}

				cs = concept.getProperties("Project_Name");
				if (cs.size() > 0) {
					hasStakeholder = true;
				}
				if (hasStakeholder) {
					conceptsWithStakeholders.add(code);
				}

			}

			pw.println();
			pw.println("There are " + conceptsWithStakeholders.size()
					+ " with outside stakeholders");
			for (String code : conceptsWithStakeholders) {
				pw.println(code);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not create output file");
		}


	}

}
