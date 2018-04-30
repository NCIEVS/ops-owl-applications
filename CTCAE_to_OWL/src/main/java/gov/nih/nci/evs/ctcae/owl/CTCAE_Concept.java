package gov.nih.nci.evs.ctcae.owl;

import java.net.URI;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Concept;

public class CTCAE_Concept extends Concept {
	
	/**A CTCAE concept should have the following properties:
	 * id which will match the rdfs:label
	 * rdfs:label
	 * MedDRA_Code
	 * DEFINITION
	 * CTCAE|PT FULL_SYN
	 * MedDRA|LLT FULL_SYNs   Optional
	 * Preferred_Name
	 * NCIt_Code
	 * code - based on Ecode
	 * Navigational_Note - optional
	 * ?MedDRA SOC?
	 * 
	 * Grades will have the Is_Grade restriction
	 * Grades will have subClassOf the general class
	 * 
	 * Hierarchy will be AE_by_SOC>SOC>Class>Grade
	 * 				     AE_by_Grade>list of grades
	 * 
	 * 
	 **/

	public CTCAE_Concept(URI conceptCode, String name, OWLKb inApi) {
		super(conceptCode, name, inApi);
		// TODO Auto-generated constructor stub
	}
	
	

}
