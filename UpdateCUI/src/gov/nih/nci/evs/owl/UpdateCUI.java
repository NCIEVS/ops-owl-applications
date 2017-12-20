/*
 * Rob Wynne, MSC
 */

package gov.nih.nci.evs.owl;

import java.net.URI;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;


public class UpdateCUI {
	
	OWLKb kb;

	String mapFile;
	String inputOWL;
	HashMap<String,String> cuiMap = new HashMap<String,String>();
	private final String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
    private URI physicalURI; 
    private URI saveURI;
    private boolean makeBatch = false;
    private final String typeConstant = "http://www.w3.org/2001/XMLSchema#string";
    PrintWriter pw;
    

    public UpdateCUI(String owlFile, String map) {
    	mapFile = map;
    	inputOWL = owlFile;
    	kb = new OWLKb(inputOWL, namespace);
		setMapFromFile(mapFile);
		File input = new File(inputOWL);     		
    }
	
	
	public static void main(String[] args) {
		 UpdateCUI update = new UpdateCUI(args[0], args[1]);
		 update.run();
	}
	
	private void config_pw(String fileLoc){
		try{
		File file = new File(fileLoc);
		pw = new PrintWriter(file);
		}
		catch (Exception e)
		{
			System.out.println("Error in PrintWriter");
		}
	}	
	
	public void run() {
		makeBatchFile();
	}

	public void makeBatchFile() {
		try{
			
		//unknowingly having multiples is a case that should be accounted for			
		Vector<String> cuis = null;
		boolean retired;
		boolean mapContainsClass;
		boolean allow;
		String currentCui;
		String newCui;
		
		File file = new File("./cui_batch_delete.txt");
		File file2 = new File("./cui_batch_new.txt");
		File file3 = new File("./cui_batch_special_review.txt");
		PrintWriter deleteRecords = new PrintWriter(file);
		PrintWriter newRecords = new PrintWriter(file2);
		PrintWriter retiredRecords = new PrintWriter(file3);
		Set<URI> concepts =  kb.getAllConcepts().keySet();
		
		for(URI concept : concepts ) {
			cuis = new Vector<String>();
			newCui = "";
			String id = concept.getFragment();
			mapContainsClass = cuiMap.containsKey(id);
			if( mapContainsClass ) {
				newCui = cuiMap.get(id);
			}
			retired = kb.isRetired(concept);			
			for( String cui : kb.getPropertyValues(concept, new URI(namespace + "#P207"))) {
				cuis.add(cui);
			}
			for( String cui : kb.getPropertyValues(concept, new URI(namespace + "#P208"))) {
				cuis.add(cui);
			}
			currentCui = null;
			allow = true;
			if( cuis != null && cuis.size() > 0 ) {
				if( cuis.size() > 1 ) {
					allow = false;
					// System.out.println("Warning: " + id + " has more than one CUI.  Not processed.");
				}
				else {
					currentCui = cuis.elementAt(0);
				}
			}
			if( allow ) {
				if( currentCui != null &&  mapContainsClass) {
					if(!currentCui.equals(newCui)) {
						if( !retired ) {
							writeRecord(id, currentCui, "delete", deleteRecords);
							writeRecord(id, newCui, "new", newRecords);						
						}
						else {
							writeRecord(id, currentCui, "delete", retiredRecords);
						}
					}
				}
				else if( currentCui != null && !mapContainsClass) {
					// The class is likely retired.  Delete the CUI properties.
					writeRecord(id, currentCui, "delete", retiredRecords);
				}
				else if( currentCui == null && mapContainsClass) {
					// It's new
					if( !retired ) {
						writeRecord(id, newCui, "new", newRecords);
					}
				}
				else if( currentCui == null && !mapContainsClass) {
					// I don't have to do anything
				}
			}
			else {
				//handle concepts that have multiple cuis
				if( cuis.contains(newCui) ) {
					//remove the bad
					for( String cui: cuis ) {
						currentCui = cui;
						if( !cui.equals(newCui) ) {
							writeRecord(id, currentCui, "delete", deleteRecords);
						}
					}
				}
				else {
					//remove them all, add the new
					for( String cui : cuis ) {
						currentCui = cui;
						if( !retired ) {
							writeRecord(id, currentCui, "delete", deleteRecords);						
						}
						else {
							writeRecord(id, currentCui, "delete", retiredRecords);
						}
					}
					writeRecord(id, newCui, "new", newRecords);				
				}
			}
		}
	
		deleteRecords.close();
		newRecords.close();
		retiredRecords.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error in PrintWriter");
		}		
	}
	
	private void writeRecord(String id, String val, String action, PrintWriter out) {
		out.println(id + "\t" + action + "\t" + getPropertyCode(val) + "\t" + val );
		out.flush();
	}
	
	private String getPropertyCode(String val) {
		if( val.contains("CL") ) {
			return "P208";  //NCI_META_CUI
		}
		else {
			return "P207";  //UMLS_CUI
		}
	}

    public void setMapFromFile(String filename)
    {
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
		  configFile = new FileReader(filename);
		  buff = new BufferedReader(configFile);
		  boolean eof = false;
		  while (!eof) {
			String line = buff.readLine();
			if (line == null)
			  eof = true;
			else {
				String[] values = line.split("\\|");

				String classid = values[0];
				String cui = values[1];
				
				if( !cuiMap.containsKey(classid) ) {
					cuiMap.put(classid, cui);				
				}
				else
					System.out.println("Duplicate cui entry for class " + classid + " at line " + line + " - Ignored.");
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        finally{
            // Closing the streams
            try{
               buff.close();
               configFile.close();
            }catch(Exception e){
               e.printStackTrace();
            }
        }
	}	

    public Vector<String> readConfigFile(String filename)
    {
    	Vector<String> v = new Vector<String>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
		  configFile = new FileReader(filename);
		  buff = new BufferedReader(configFile);
		  boolean eof = false;
		  while (!eof) {
			String line = buff.readLine();
			if (line == null)
			  eof = true;
			else
			  v.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        finally{
            // Closing the streams
            try{
               buff.close();
               configFile.close();
            }catch(Exception e){
               e.printStackTrace();
            }
        }
        if( !v.isEmpty() ) {
        	return v;
        }
        else
        	return null;
	}
	
}
