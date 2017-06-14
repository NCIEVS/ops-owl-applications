package gov.nih.nci.cadsr.history;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import gov.nih.nci.system.client.ApplicationServiceProvider;

import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.NCIChangeEventList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.SystemReleaseList;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.DataModel.NCIHistory.NCIChangeEvent;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.History.HistoryService;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.versions.CodingSchemeVersion;
import org.LexGrid.versions.EditHistory;
import org.LexGrid.versions.SystemRelease;



public class CaDSRHistory {
    String evsURL = "http://lexevsapi60.nci.nih.gov/lexevsapi60";
    LexBIGService lbSvc;
    String vocabName = "NCI Thesaurus";
    public CaDSRHistory(){
    
    try
    {
//    	service = (LexBIGService)ApplicationServiceProvider.getApplicationService("EvsServiceInfo");
    	lbSvc = (LexEVSService) ApplicationServiceProvider
		        .getApplicationServiceFromUrl(evsURL, "EvsServiceInfo");
    	
    	//example of active concept with no splits
    	getConceptStatus("C3114");
    	//example retired concept with single reference code
    	getConceptStatus("C33220");
    	//example retired concept with multiple reference codes
    	getConceptStatus("C23492");
    	//example merged concept
    	getConceptStatus("C73753");
    	//example split concept
		getSplitsForConcept("C70848");
    }
    catch (Exception ex)
    {
        
        ex.printStackTrace();

    }
    }
    
    
    private void getConceptStatus(String conCode){

		try {
			
			ConceptReference cref = new ConceptReference();
			cref.setConceptCode(conCode);
			ConceptReferenceList ncrl = new ConceptReferenceList();
			ncrl.addConceptReference(cref);
			CodingSchemeVersionOrTag cvt = new CodingSchemeVersionOrTag();
			cvt.setTag("PRODUCTION");
			CodedNodeSet nodes = lbSvc.getNodeSet(vocabName, cvt, null);
			nodes = nodes.restrictToCodes(ncrl);
			ResolvedConceptReferenceList crl = nodes.resolveToList(null, null,
					null, 20);
			ResolvedConceptReference ref = crl.getResolvedConceptReference(0);
			getDefinition(ref);

			if (!ref.getEntity().getIsActive()){
				getRetirementDate(conCode);
			}


		} catch (Exception ex) {
			System.out.println("searchConcepts_C9047 for " + vocabName
					+ "  throws Exception = " + ex);
		}
    }
    
    private void getRetirementDate(String conCode){
		try {

			HistoryService hs = lbSvc.getHistoryService(vocabName);

			Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2005-01-01");
			Date endDate = new Date();
			NCIChangeEventList cel = hs.getEditActionList(Constructors.createConceptReference(conCode, null),startDate,endDate);
			cel.getEntryCount();
			Iterator<NCIChangeEvent> celIter = (Iterator<NCIChangeEvent>) cel.iterateEntry();
			while (celIter.hasNext()){
				NCIChangeEvent ce = celIter.next();
				if (ce.getEditaction().name().equals("RETIRE")){
					System.out.println("Retirement date "+ ce.getEditDate().toLocaleString());
					System.out.println("Reference concept " + ce.getReferencecode());
				}
				else if (ce.getEditaction().name().equals("MERGE")){
					System.out.println("Merge date "+ ce.getEditDate().toLocaleString());
					System.out.println("Reference concept " + ce.getReferencecode());
				}
				
			}
		} catch (Exception ex) {
			System.out.println("searchConcepts_C9047 for " + vocabName
					+ "  throws Exception = " + ex);
		}
    }
    
    
    private void getSplitsForConcept(String conCode){
    	try{
    		HistoryService hs = lbSvc.getHistoryService(vocabName);
    		NCIChangeEventList cel = hs.getDescendants(Constructors.createConceptReference(conCode, null));
    		if (cel.getEntryCount()>0){
    			Iterator<NCIChangeEvent> celIter = (Iterator<NCIChangeEvent>) cel.iterateEntry();
    			while (celIter.hasNext()){
    				NCIChangeEvent ce = celIter.next();
    				if (ce.getEditaction().name().equals("SPLIT")){
    					System.out.println("Split date "+ ce.getEditDate().toLocaleString());
    					System.out.println("Reference concept " + ce.getReferencecode());
    				}
    			}
    		}
    	}
    	catch(LBException e){
    		e.printStackTrace();
    	}
    }
    
    
    private void getDefinition(ResolvedConceptReference ref){
    	Entity entity = ref.getEntity();
    	Definition[] defs = entity.getDefinition();
    	for(int i=0; i<defs.length; i++){
    		Definition def = defs[i];
    		if (def.getPropertyName().equals("DEFINITION")){
    			//Is a main definition
    			System.out.println("Definition: " + def.getValue().getContent());
    		}
    		else {
    			System.out.println("Alternate definition: " + def.getValue().getContent());
    		}
    	}
    	
    	String preferredName = entity.getEntityDescription().getContent();
    	System.out.println("Preferred Name: "+ preferredName);
    }
}
