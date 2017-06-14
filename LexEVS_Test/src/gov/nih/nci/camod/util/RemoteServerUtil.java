package gov.nih.nci.camod.util;

//package gov.nih.nci.evs.reportwriter.utils;

//import gov.nih.nci.system.applicationservice.EVSApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.Properties;

import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSDistributed;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
import org.lexgrid.valuesets.impl.LexEVSValueSetDefinitionServicesImpl;

// TODO: Auto-generated Javadoc
/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
 * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 * "This product includes software developed by NGIT and the National Cancer Institute."
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
 * wherever such third-party acknowledgments normally appear.
 * 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
 * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
 * the recipient to use any trademarks owned by either NCI or NGIT
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 * NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 * 
 *          Modification history Initial implementation kim.ong@ngc.com
 * 
 */

public class RemoteServerUtil {

	/** The _service info. */
	static private String _serviceInfo = "EvsServiceInfo";

	/** The system properties. */
	private final Properties systemProperties = null;
	static String serviceURL = "";
	

	/**
	 * Instantiates a new remote server util.
	 */
	public RemoteServerUtil() {

	}

	/**
	 * Establish a remote LexBIG connection.
	 * 
	 * @return the EVS application service
	 */
	public static LexEVSService createLexBIGService() {
		LexEVSService lbSvc = null;

		try {
			// *********************QA Servers ********************
			// String serviceUrl =
			// "http://ncias-q541-v.nci.nih.gov:29080/lexevsapi60";
			// String serviceUrl =
			// "http://ncias-q532-v.nci.nih.gov:29080/lexevsapi60";

			// DataQA
			// String serviceUrl =
			// "http://ncias-q599-v.nci.nih.gov:29080/lexevsapi60";

			// ********************Stage Servers **************************
//			 String serviceUrl =
//			 "http://lexevsapi60-stage.nci.nih.gov/lexevsapi60";

			// String serviceUrl =
			// "http://ncias-s672.nci.nih.gov:29080/lexevsapi60";
			// String serviceUrl =
			// "http://ncias-s692.nci.nih.gov:29080/lexevsapi60";

			// **************Prod Servers**********************
			 serviceURL = "http://lexevsapi60.nci.nih.gov/lexevsapi60";

			// String serviceUrl =
			// "http://ncias-p673.nci.nih.gov:29080/lexevsapi60";
//			String serviceUrl = "http://ncias-p674.nci.nih.gov:29080/lexevsapi60";

			// ****************Dev Servers****************

			// String serviceUrl =
			// "http://ncias-d488-v.nci.nih.gov:29080/lexevsapi60";
			// String serviceUrl =
			// "http://ncias-d499-v.nci.nih.gov:29080/lexevsapi60";

			// ReportWriterProperties.getProperty(ReportWriterProperties.EVS_SERVICE_URL);
			lbSvc = (LexEVSService) ApplicationServiceProvider
			        .getApplicationServiceFromUrl(serviceURL, _serviceInfo);
			System.out.println("Server being tested: " + serviceURL);
			return lbSvc;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Establish a remote LexBIG connection.
	 * 
	 * @param url
	 *            the url
	 * 
	 * @return the EVS application service
	 */
	public static LexEVSService createLexEVSService(String url) {
		LexEVSService lbSvc = null;

		try {
			lbSvc = (LexEVSService) ApplicationServiceProvider
			        .getApplicationServiceFromUrl(url, _serviceInfo);
			System.out.println("Server being tested: " + url);
			return lbSvc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static LexEVSApplicationService createLexEVSApplicationService(String url) {
		LexEVSApplicationService lbSvc = null;

		try {
			lbSvc =  (LexEVSApplicationService) ApplicationServiceProvider
			        .getApplicationServiceFromUrl(url, _serviceInfo);
			System.out.println("Server being tested: " + url);
			return  lbSvc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public static LexEVSApplicationService createLexEVSApplicationService() {
		LexEVSApplicationService lbSvc = null;
		String url =
				 "http://lexevsapi60-stage.nci.nih.gov/lexevsapi60";
		try {
			lbSvc =  (LexEVSApplicationService) ApplicationServiceProvider
			        .getApplicationServiceFromUrl(url, _serviceInfo);
			System.out.println("Server being tested: " + url);
			return  lbSvc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static LexBIGService createLexBIGService(String url){
		LexBIGService lbSvc = null;
		try{
			lbSvc = (LexBIGService) ApplicationServiceProvider.getApplicationServiceFromUrl(url);
			System.out.println("Server being tested: "+ url);
			return lbSvc;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
    public static LexEVSValueSetDefinitionServices getLexEVSValueSetDefinitionServices(String serviceUrl) {
		if (serviceUrl == null || serviceUrl.compareTo("") == 0 || serviceUrl.compareToIgnoreCase("null") == 0) {
			return LexEVSValueSetDefinitionServicesImpl.defaultInstance();
		}
		try {
			LexEVSDistributed distributed =
				(LexEVSDistributed)
				ApplicationServiceProvider.getApplicationServiceFromUrl(serviceUrl, "EvsServiceInfo");

			LexEVSValueSetDefinitionServices vds = distributed.getLexEVSValueSetDefinitionServices();
			return vds;
		} catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}

    public static LexEVSValueSetDefinitionServices createValueSetDefinitionServices() {
		try {
            if (serviceURL == null || serviceURL.compareTo("") == 0 || serviceURL.compareToIgnoreCase("null") == 0) {
				return LexEVSValueSetDefinitionServicesImpl.defaultInstance();
			}

			return getLexEVSValueSetDefinitionServices(serviceURL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
	}
	
}
