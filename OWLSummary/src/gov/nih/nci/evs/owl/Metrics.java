package gov.nih.nci.evs.owl;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import gov.nih.nci.evs.owl.data.OWLKb;
import gov.nih.nci.evs.owl.entity.Property;
import gov.nih.nci.evs.owl.entity.Qualifier;
import gov.nih.nci.evs.owl.proxy.ConceptProxy;

public class Metrics {

    OWLKb owlApi = null;
    HashMap<String, Integer> fullSynRawCount = new HashMap<String,Integer>();
    HashMap<String,Integer> fullSynGroupedCount = new HashMap<String,Integer>();
    int conceptCount=0;
    
    public HashMap<String, Integer> getFullSynRawCount() {
        return fullSynRawCount;
    }

    
    public HashMap<String, Integer> getFullSynGroupedCount() {
        return fullSynGroupedCount;
    }


    
    public Metrics(OWLKb inOwlApi){
        //count all full_syns, count all for each source
        //calculate % of each source full_syn over all Full_Syns
        //Ex: we have 10,000 fS total, and 4500 FULL_SYN for FDA.
        //FDA will be 45% of all FULL_SYNS
        
        //count all concepts and tally up by source
        //
        //Ex: we have 10,000 concepts. FDA has synonyms in 2000
        //NCI has synonyms in 10,0000
        //CDISC has synonyms in 1000
        
        //count all concepts, then tally up by combined sources
        //calculate % for each combined source
        //Ex: we have 10,000 concepts.  4000 of them have just NCI as source
        //NCI will be 40% of all concepts
        //There are 1000 concepts that have FDA+NCI as sources
        //The combined FDA+NCI make up 10% of all concepts
        
        
        /**Will need the following tallies
         * 1. All concepts - flat number from owlApi
         * 2. # of fullsyns for each source  - HashMap, keyed by source?
         * 3. # of concepts for each concatenated source list  - HashMap, keyed by source combo?
         */
        

         this.owlApi = inOwlApi;
         countFullSyns();
         debugPrintCounts();
        
    }

    //We are looking for FullSyns in NCIt.  Since we are working with a known dataset
    //    we are going to throw in some magic numbers
    //    The code for FULL_SYN is P90
    //    The full name is http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN
    private void countFullSyns (){
        HashMap<URI, ConceptProxy> concepts = owlApi.getAllConcepts();

        conceptCount = concepts.size();
        
        String propId = "P90";
        String propName = "FULL_SYN";
        String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
        Set<URI> conceptCodes = concepts.keySet();
        
        for (URI conceptCode:conceptCodes){
            TreeSet<String> sourceSet = new TreeSet<String>();
            ConceptProxy cp = concepts.get(conceptCode);
            Vector<Property> fullsyns = cp.getProperties(propId);
            //code to check if this is a byName file
            if (fullsyns==null || fullsyns.size() == 0){
                fullsyns = cp.getProperties("FULL_SYN");
            }
            if (fullsyns==null || fullsyns.size() == 0){
                System.out.println("Not able to find FULL_SYN");
                return;
            }
            for (Property fullsyn:fullsyns){
                Qualifier qual = fullsyn.getQualifier("term-source");
                if(qual ==null){
                	qual = fullsyn.getQualifier("Term Source");
                }
                //OK, now we have a qual with a source.  
                //we need to add a value to the source count
                if(fullSynRawCount.get(qual.getValue()) == null){
                    fullSynRawCount.put(qual.getValue(), new Integer(1));
                } else
                {
                    Integer sourceCount = fullSynRawCount.get(qual.getValue());
                    sourceCount++;
                    fullSynRawCount.put(qual.getValue(), sourceCount);
                }
                //we also need to add the source name to an internal treeset which will become a string
                if (!sourceSet.contains(qual.getValue())){
                    sourceSet.add(qual.getValue());
                }
            }
            //convert sourceSet into a String
            String sourceString = "";
            for(String source:sourceSet){
                sourceString = sourceString + source + "+";
            }
            //trim off the last "+"
            sourceString = sourceString.substring(0, sourceString.length()-1);
            
            if (fullSynGroupedCount.get(sourceString) ==null){
                fullSynGroupedCount.put(sourceString, new Integer(1));
            }else {
                Integer groupedCount = fullSynGroupedCount.get(sourceString);
                groupedCount++;
                fullSynGroupedCount.put(sourceString, groupedCount);
            }
        }
    }
    
    
    private void debugPrintCounts(){
        Set<String> keySet= fullSynRawCount.keySet();
        System.out.println("Raw Count");
        for (String key:keySet){
            System.out.println(key+"\t"+ fullSynRawCount.get(key));
        }
        
        System.out.println("Grouped Count");
        keySet= fullSynGroupedCount.keySet();
        for (String key:keySet){
            System.out.println(key+"\t"+ fullSynGroupedCount.get(key));
        }
    }
    
}
