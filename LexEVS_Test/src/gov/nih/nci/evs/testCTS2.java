package gov.nih.nci.evs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.codingSchemes.CodingScheme;
//import org.lexevs.cts2.LexEvsCTS2;


public class testCTS2 {
    
//example    
//http://lexevscts2.nci.nih.gov/lexevscts2/resolvedvaluesets?matchvalue=AdAM&filtercomponent=resourceName&format=json

//http://lexevscts2.nci.nih.gov/lexevscts2/codesystemversions

//    http://lexevscts2.nci.nih.gov/lexevscts2/resolvedvaluesets?format=json
//    http://lexevscts2.nci.nih.gov/lexevscts2/resolvedvaluesets?format=json&maxtoreturn=400
        
//    http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC%20ADaM%20Date%20Imputation%20Flag%20Terminology/definition/c38261e5?format=json

//    {"href":"http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC ADaM Date Imputation Flag Terminology/definition/c38261e5/resolution/1",
//             "resolvedValueSetURI":"http://ncit:C81223","resolvedHeader":{"resolutionOf":{"valueSetDefinition":{"content":"c38261e5",
//               "uri":"http://ncit:C81223","href":"http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC ADaM Date Imputation Flag Terminology/definition/c38261e5"},
//               "valueSet":{"content":"CDISC ADaM Date Imputation Flag Terminology"}}}},

//    http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC%20ADaM%20Date%20Imputation%20Flag%20Terminology/definition/c38261e5/resolution/1?format=json
    
//    {"iteratableResolvedValueSet":{"resolutionInfo":{"resolutionOf":{"valueSetDefinition":{"content":"c38261e5",
//        "uri":"http://ncit:C81223","href":"http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC ADaM Date Imputation Flag Terminology/definition/c38261e5"},
//        "valueSet":{"content":"CDISC ADaM Date Imputation Flag Terminology"}}},"entryList":[{"designation":"Day Imputed",
//         "uri":"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C81212",
//         "href":"http://lexevscts2.nci.nih.gov/lexevscts2/codesystem/NCI_Thesaurus/version/14.04d/entity/NCI_Thesaurus:C81212",
//         "namespace":"NCI_Thesaurus","name":"C81212"},{"designation":"Year Month Day Imputed",
//         "uri":"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C81210",
//         "href":"http://lexevscts2.nci.nih.gov/lexevscts2/codesystem/NCI_Thesaurus/version/14.04d/entity/NCI_Thesaurus:C81210",
//         "namespace":"NCI_Thesaurus","name":"C81210"},{"designation":"Month Day Imputed",
//         "uri":"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C81211",
//         "href":"http://lexevscts2.nci.nih.gov/lexevscts2/codesystem/NCI_Thesaurus/version/14.04d/entity/NCI_Thesaurus:C81211",
//         "namespace":"NCI_Thesaurus","name":"C81211"}],"complete":"COMPLETE","numEntries":3,
//         "heading":{"resourceRoot":"valueset/CDISC ADaM Date Imputation Flag Terminology/definition/c38261e5/resolution/1",
//         "resourceURI":"http://lexevscts2.nci.nih.gov/lexevscts2/valueset/CDISC ADaM Date Imputation Flag Terminology/definition/c38261e5/resolution/1",
//         "parameterList":[{"arg":"format","val":"json"}],"accessDate":"May 29, 2014 12:06:06 PM"}}}  
    
    
//    http://lexevscts2.nci.nih.gov/lexevscts2/codesystem/NCI_Thesaurus/entity/C81212?format=xml

     
     
    /**
     * Created with IntelliJ IDEA.
     * User: m029206
     * Date: 9/20/12
     * Time: 11:48 AM.
     */

     private String path;
     
        public  void getValueSets(){
            String uri = path + "/resolvedvaluesets?format=json";
            
            URL url;
            try {
                url = new URL(uri);
     
     
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : The HTTP error code is : "
                            + connection.getResponseCode());
                }
         
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));
     
                String output;              
                output = br.readLine();
                if (output.startsWith("{\"ResolvedValueSetDirectory")){
                    System.out.println("SUCCESS: Resolved value set listing retrieved from CTS2");
                }else {
                    System.out.println("FAILURE: CTS2 did not return value set list");
                }
//                while ((output = br.readLine()) != null) {
//                    System.out.println(output);
//                }
     
     
            connection.disconnect();
     
     
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
     
     
        public  void getValueSet(){
            String uri = path + "/resolvedvaluesets?matchvalue=Sequence&filtercomponent=resourceName&format=xml";
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(uri);
     
     
     
                     connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "text/xml");
                if (connection.getResponseCode() != 200) {
                    throw new RuntimeException("FAILURE : The HTTP error code is : "
                            + connection.getResponseCode());
                }
     
     
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));
     
     
                String output;
//                System.out.println("\nOutput from CTS2 Service .... \n");
                
                //get the XML declaration out of the way
                br.readLine();
                output = br.readLine();
                if (output.startsWith("<ResolvedValueSetDirector")) {
                    System.out.println("SUCCESS: Value set query returned from CTS2");
                } else {
                    System.out.println("FAILURE: CTS2 did not return query results");
                }
                
//                while ((output = br.readLine()) != null) {
//                    System.out.println(output);
//                }
     
     
     
     
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
        }
     
     
        public static void main(String[] args){
     
            testCTS2 client =  new testCTS2();
            client.getValueSets();
            client.getValueSet();
     
        }
        
        public void doTests(){
        	try{
            this.getValueSets();
            this.getValueSet();
        	} catch (Exception ex){
        		ex.printStackTrace();
        	}
        }
    
    public testCTS2(String path){
        this.path = path;

    }
    
    public testCTS2(){
        this.path =
                "http://lexevscts2.nci.nih.gov/lexevscts2";

    }


//    private void cts2api() {
//    	LexEvsCTS2 cts2 = org.lexevs.cts2.LexEvsCTS2Impl.defaultInstance();
//
//        org.lexevs.cts2.query.CodeSystemQueryOperation csQueryOp = new org.lexevs.cts2.LexEvsCTS2Impl().getQueryOperation().getCodeSystemQueryOperation();
//        CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
//        versionOrTag.setTag("PRODUCTION");
//        CodingScheme result = csQueryOp.getCodeSystemDetails("NCI_Thesaurus", versionOrTag);
//    	
//    }

    
    
}
