package gov.hhs.fda.ctr.etl.meddra;

import gov.nih.nci.evs.security.SecurityToken;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;

/**
 * 
 * @author marwahah
 * 
 */

public class Service {

	// static final String EVS_SERVICE_URL =
	// "http://lexevsapi60-dataqa.nci.nih.gov/lexevsapi60";
	// static final String EVS_SERVICE_URL =
	// "http://ncias-q599-v.nci.nih.gov:29080/lexevsapi60";

	// static final String EVS_SERVICE_URL =
	// "http://lexevsapi60-stage.nci.nih.gov/lexevsapi60";

	// static final String EVS_SERVICE_URL =
	// "http://ncias-p674.nci.nih.gov:29080/lexevsapi60";
	static final String EVS_SERVICE_URL = "http://lexevsapi60.nci.nih.gov/lexevsapi60";

	// static final String EVS_SERVICE_URL =
	// "http://lexevsapi60-qa.nci.nih.gov/lexevsapi60";
	// static final String EVS_SERVICE_URL =
	// "http://ncias-q532-v.nci.nih.gov:29080/lexevsapi60";
	// static final String EVS_SERVICE_URL =
	// "http://ncias-q541-v.nci.nih.gov:29080/lexevsapi60";

	// static final String EVS_SERVICE_URL =
	// "http://lexevsapi60-dev.nci.nih.gov/lexevsapi60";
	// static final String EVS_SERVICE_URL =
	// "http://ncias-d488-v.nci.nih.gov:29080/lexevsapi60";
	static final String EVS_SERVICE_NAME = "EvsServiceInfo";
	static final String EVS_MEDDRA_SCHEME = "MedDRA";
	static final String EVS_MEDDRA_ID = "10382";

	/**
		 * 
		 */
	private static ApplicationService SERVICE;

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	static LexEVSApplicationService getInstance() throws Exception {
		if (SERVICE == null) {
			SERVICE = ApplicationServiceProvider.getApplicationServiceFromUrl(
			        Service.EVS_SERVICE_URL, Service.EVS_SERVICE_NAME);
		}

		return (LexEVSApplicationService) SERVICE;
	}

	/**
	 * 
	 * @param scheme
	 * @param key
	 * @return
	 */
	static boolean register(String scheme, String key) {
		boolean successful = false;

		SecurityToken token = new SecurityToken();

		token.setAccessToken(key);
		// token.setPassword("Bab9KFAH");
		try {
			Boolean result = getInstance().registerSecurityToken(scheme, token);

			if (result != null) {
				successful = result;
			}
		} catch (Exception ex) {
			// TODO: Log exception
		}

		return successful;
	}
}
