package gov.nih.nci.evs.automated;

public class AutoRunner {

	public static void main(String[] args) {

		//Stub class to work out automated run and reporting.
		//We want to detect "stop conditions" on daily exports and prevent upload of files to destinations
		
		/**
		    Incorrect Semantic Type values
			Active concepts with Concept_Status of "Retired"
			Absurd summary numbers
			Deleted concepts
			Unretired concepts
			Concepts that have been retired with no retire records
			Concepts that have no history records
			Concepts that have been merged (have a merge history record and are the losing concept) but are not in the Retired branch
			Concepts that have been retired (have a retire history record) but are not in the Retired branch
			
			The absurd summary numbers is really the only one applicable to a daily automated load. 
			Need new tests:
			
			# of concepts
			root nodes?
			# properties
			# role definitions
			# axioms
			
			
		 */
		

	}
	
	public AutoRunner(){
		
	}
	
	private void inputOWL(){
		//read in current and previous files? 
		//Or record previous metrics to file and compare?
		
	}

	private boolean checkNumberConcepts(){
		boolean numberConceptsOK=false;
		//load into owlapi and count concepts?
		
		return numberConceptsOK;
	}
	
	private boolean checkRootNodes(){
		boolean rootNodesOK=false;
		
		return rootNodesOK;
	}
	
	private boolean checkProperties(){
		boolean propOK=false;
		
		return propOK;
	}
	
	private boolean checkRoles(){
		boolean rolesOK=false;
		
		return rolesOK;
	}
	
	private boolean checkAxioms(){
		boolean axiomsOK=false;
		
		return axiomsOK;
	}
}
