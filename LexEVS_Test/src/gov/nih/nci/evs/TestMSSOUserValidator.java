package gov.nih.nci.evs;

import gov.nih.nci.evs.security.SecurityToken;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.naming.SupportedAssociationQualifier;

/**
 * @author Northrop Grumman Information Systems
 * @version 1.0
 * @author kim.ong@ngc.com
 */


public class TestMSSOUserValidator
{
	//public final static String MedDRA_SCHEME_51 = "MedDRA (Medical Dictionary for Regulatory Activities Terminology)";
	public final static String MedDRA_SCHEME_51 = "MedDRA";
	public final static String MedDRA_SCHEME_60 = "MedDRA";

	public final static String MedDRA_VERSION = "15.1";

    public TestMSSOUserValidator()
    {

    }


    public static LexBIGService createLexBIGService(String serviceUrl) {
        try {
            if (serviceUrl == null || serviceUrl.compareTo("") == 0 || serviceUrl.compareToIgnoreCase("null") == 0) {
                LexBIGService lbSvc = LexBIGServiceImpl.defaultInstance();
                return lbSvc;
            }

            LexEVSApplicationService lexevsService =
                (LexEVSApplicationService) ApplicationServiceProvider
                    .getApplicationServiceFromUrl(serviceUrl, "EvsServiceInfo");

            return lexevsService;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }


    public static CodingScheme resolveCodingScheme(LexBIGService lbSvc, String formalname, String version) {
        CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
        if (version != null) versionOrTag.setVersion(version);
        try {
			System.out.println("Resolving " + formalname + " (version: " + version + ") without a token...");
        	CodingScheme cs = lbSvc.resolveCodingScheme(formalname, versionOrTag);
        	if (cs == null) return null;
			System.out.println("lbSvc.resolveCodingScheme returns coding scheme name: " + cs.getCodingSchemeName());
		} catch (Exception ex) {
			System.out.println("Unable to resolve " + formalname + " (version: " + version + ")");
			ex.printStackTrace();
		}
		return  null;
	}


    public static LexEVSApplicationService registerSecurityToken(
        LexEVSApplicationService lexevsService, String codingScheme,
        String token) {
        SecurityToken securityToken = new SecurityToken();
        securityToken.setAccessToken(token);
        Boolean retval = null;
        try {
            retval =
                lexevsService
                    .registerSecurityToken(codingScheme, securityToken);
        } catch (Exception e) {
            System.out.println("WARNING: Registration of SecurityToken failed.");
            e.printStackTrace();
        }
        return lexevsService;
    }


    public static LexBIGService createLexBIGService(String serviceUrl,
        String codingScheme, String token) {

		if (token == null) {
			return createLexBIGService(serviceUrl);
		}

        SecurityToken securityToken = new SecurityToken();
        securityToken.setAccessToken(token);

        return createLexBIGService(serviceUrl, codingScheme, securityToken);
    }


    public static LexBIGService createLexBIGService(String serviceUrl,
        String codingScheme, SecurityToken securityToken) {

        try {
            if (serviceUrl == null || serviceUrl.compareTo("") == 0 || serviceUrl.compareToIgnoreCase("null") == 0) {
                LexBIGService lbSvc = LexBIGServiceImpl.defaultInstance();
                return lbSvc;
            }

            LexEVSApplicationService lexevsService =
                (LexEVSApplicationService) ApplicationServiceProvider
                    .getApplicationServiceFromUrl(serviceUrl, "EvsServiceInfo");

            Boolean retval = false;
            retval = lexevsService.registerSecurityToken(codingScheme, securityToken);

            if (!retval) {
				System.out.println("Unable to register security token " + securityToken.getAccessToken() + " ???");
				return null;
			} else {
				System.out.println("Security token " + securityToken.getAccessToken() + " registered.");
			}

            return lexevsService;

        } catch (Exception e) {
            System.out.println("Unable to connected to " + serviceUrl);
            e.printStackTrace();
        }
        return null;
    }



    public static CodingScheme resolveCodingScheme(String serviceURL, String formalname, String version, String token) {
		System.out.println("\nserviceURL: " + serviceURL);
		System.out.println(formalname);
		System.out.println(version);
		System.out.println(token);

        LexBIGService lbSvc = createLexBIGService(serviceURL, formalname, token);
        if (lbSvc == null) return null;

        CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
        if (version != null) versionOrTag.setVersion(version);

        CodingScheme cs = null;
        try {
	        cs = lbSvc.resolveCodingScheme(formalname, versionOrTag);
	        System.out.println("Resolved " + formalname + " (version: " + version + ")");
		} catch (Exception ex) {
			System.out.println("Unable to resolve " + formalname + " (version: " + version + ")");
			ex.printStackTrace();
		}
        return cs;
	}

	public static void dumpCodingSchemeMetadata(CodingScheme cs) {
		System.out.println("Coding scheme URI: " + cs.getCodingSchemeURI());
		System.out.println("Coding scheme name: " + cs.getCodingSchemeName());

		Vector v = getSupportedAssociationQualifier(cs);
		if (v != null) {
			System.out.println("Association qualifiers: " + v.size());
		}
		for (int i=0; i<v.size(); i++) {
			String s = (String) v.elementAt(i);
			System.out.println("\tasso qualifier: " + s);
		}

		v = getProperties(cs);
		if (v != null) {
			System.out.println("Properties: " + v.size());
			for (int i=0; i<v.size(); i++) {
				String s = (String) v.elementAt(i);
				System.out.println("\tproperty: " + s);
			}
		}
	}


	public static Vector<String> getSupportedAssociationQualifier(CodingScheme cs)
	{
        Vector<String> v = new Vector<String>();
        try {
			org.LexGrid.naming.SupportedAssociationQualifier[] supportedAssociationQualifiers
			    = cs.getMappings().getSupportedAssociationQualifier();
			if (supportedAssociationQualifiers == null) return null;
			for (int i=0; i<supportedAssociationQualifiers.length; i++)
			{
				SupportedAssociationQualifier q = supportedAssociationQualifiers[i];
				v.add(q.getLocalId());
			}
			return v;
	    } catch (Exception e) {
			return null;
		}
	}

	public static Vector getProperties(CodingScheme cs)
	{
		Vector v = new Vector();
        try {
			org.LexGrid.commonTypes.Properties properties = cs.getProperties();
			for (int i=0; i<properties.getPropertyCount(); i++)
			{
				Property q = properties.getProperty(i);
				v.add(q.getPropertyName());
			}
			return v;
	    } catch (Exception e) {
			return null;
		}
	}


    public static void main(String[] args)
    {
		System.out.println("===========================================");
		TestMSSOUserValidator test = new TestMSSOUserValidator();
		String serviceUrl = null;
		serviceUrl = "http://lexevsapi60-qa.nci.nih.gov/lexevsapi60";
//		serviceUrl = "http://ncias-d499-v.nci.nih.gov:29080/lexevsapi60";

		String codingSchemeName = MedDRA_SCHEME_60;
		String version = MedDRA_VERSION;

		String token = "10382";
		CodingScheme cs = null;

        CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
        if (version != null) versionOrTag.setVersion(version);

		cs = TestMSSOUserValidator.resolveCodingScheme(serviceUrl, codingSchemeName, version, token);
		if (cs != null) {
			System.out.println("Success: ");
			TestMSSOUserValidator.dumpCodingSchemeMetadata(cs);
		} else {
			System.out.println("Failed.");
		}

        System.out.println("===========================================");
		token = "99999";

		cs = TestMSSOUserValidator.resolveCodingScheme(serviceUrl, codingSchemeName, version, token);
		if (cs != null) {
			System.out.println("Success: ");
			TestMSSOUserValidator.dumpCodingSchemeMetadata(cs);
		} else {
			System.out.println("Failed.");
		}

        System.out.println("===========================================");
        cs = TestMSSOUserValidator.resolveCodingScheme(serviceUrl, codingSchemeName, version, null);

		if (cs != null) {
			System.out.println("Success: ");
			TestMSSOUserValidator.dumpCodingSchemeMetadata(cs);
		} else {
			System.out.println("Failed.");
		}
    }
}

