package gov.nih.nci.evs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ListIterator;

import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.commonTypes.Source;
import org.LexGrid.valueSets.ValueSetDefinition;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;

import gov.nih.nci.camod.util.RemoteServerUtil;


public class GetTVSValues {
	private LexEVSService evsService;
	public static final String _service = "EvsServiceInfo";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetTVSValues report = new GetTVSValues();
		try {
			report.run();
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// report.print();
	}
	
	public void run() throws LBException, URISyntaxException {
		evsService = RemoteServerUtil.createLexBIGService();
		LexEVSValueSetDefinitionServices vsd_service = evsService.getLexEVSValueSetDefinitionServices();
		List valueSetDefinitionURIList = vsd_service.listValueSetDefinitionURIs();
		ListIterator i = valueSetDefinitionURIList.listIterator();
		while( i.hasNext() == true ) {
			URI vuri = new URI(i.next().toString());
			ValueSetDefinition vsd = vsd_service.getValueSetDefinition(vuri, null);
			java.util.Enumeration<? extends Source> sourceEnum = vsd.enumerateSource();
			while(sourceEnum.hasMoreElements()) {
				System.out.println(vuri.toString() + "\t" + sourceEnum.nextElement().getContent());
			}
		}
	}
	
//	public void print() {
//		
//	}
	

}
