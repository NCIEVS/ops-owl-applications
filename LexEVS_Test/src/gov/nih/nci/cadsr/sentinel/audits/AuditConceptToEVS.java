// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/sentinel/sentinel/src/gov/nih/nci/cadsr/sentinel/audits/AuditConceptToEVS.java,v 1.11 2008-05-15 17:35:48 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.sentinel.audits;

import gov.nih.nci.cadsr.sentinel.database.DBProperty;
import gov.nih.nci.cadsr.sentinel.tool.ConceptItem;
import gov.nih.nci.camod.util.RemoteServerUtil;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.SortOptionList;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
//import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.LexBIG.Utility.LBConstants.MatchAlgorithms;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.apache.log4j.Logger;

/**
 * This class compares the caDSR Concepts table to the referenced EVS Concepts. If the concept code or name is not
 * valid an appropriate message is returned. Concepts which match EVS are not reported.
 * 
 * @author lhebel
 *
 */
public class AuditConceptToEVS extends AuditReport
{

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.sentinel.tool.AuditReport#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "caDSR / EVS Concept Inconsistencies";
    }

    public String[] getReportRows()
    {
        Vector<String> msgs = validate();
        String[] rows = new String[msgs.size()];
        for (int i = 0; i < rows.length; ++i)
        {
            rows[i] = msgs.get(i);
        }
        return rows;
    }
    
    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.sentinel.tool.AuditReport#getReportRows()
     */
    public String[] getReportRows(LexEVSService service)
    {
        Vector<String> msgs = validate(service);
        String[] rows = new String[msgs.size()];
        for (int i = 0; i < rows.length; ++i)
        {
            rows[i] = msgs.get(i);
        }
        return rows;
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.sentinel.tool.AuditReport#okToDisplayCount()
     */
    @Override
    public boolean okToDisplayCount()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see gov.nih.nci.cadsr.sentinel.tool.AuditReport#rightJustifyLastColumn()
     */
    @Override
    public boolean rightJustifyLastColumn()
    {
        return false;
    }

    /**
     * Parse the DBProperties and create appropriate EVSVocab objects.
     * 
     * @param props_ the property (key/value pairs)
     * @return the equivalent vocab objects
     */
    private EVSVocab[] parseProperties(DBProperty[] props_)
    {
        Vector<EVSVocab> vocabs = new Vector<EVSVocab>();
        String defProp = "";

        // Get defaults
        for (int i = 0; i < props_.length; ++i)
        {
            String[] text = props_[i]._key.split("[.]");
            if (text[2].equals("ALL"))
            {
                if (text[3].equals("PROPERTY") && text[4].equals("DEFINITION"))
                    defProp = props_[i]._value;
                break;
            }
        }

        // Process vocab data
        EVSVocab vocab = null;
        
        vocab = new EVSVocab();
        vocab._display = "MetaThesaurus";
        vocab._vocab = "MetaThesaurus";
        vocab._source = "NCI_META_CUI";
        vocab._source2 = "UMLS_CUI";
        vocab._ed = new MetaTh(vocab);
        vocabs.add(vocab);

        String vDisplay = null;
        String vAccess = null;
        String vName = null;
        String vDefProp = null;
        String vSearch = null;
        String vSource = null;
        String last = null;
        for (int i = 0; i < props_.length; ++i)
        {
            String[] text = props_[i]._key.split("[.]");
            if (!text[2].equals("ALL"))
            {
                if (last == null)
                    last = text[2];

                if (!last.equals(text[2]))
                {
                    vocab = new EVSVocab();
                    vocab._display = vDisplay;
                    vocab._vocab = vName;
                    vocab._access = vAccess;
                    vocab._preferredDefinitionProp = (vDefProp == null) ? defProp : vDefProp;
                    vocab._preferredNameProp = (vSearch == null) ? vDisplay : vSearch;
                    vocab._ed = new NonMetaTh(vocab);
                    vocab._source = vSource;
                    vocabs.add(vocab);
                    vDisplay = null;
                    vName = null;
                    vAccess = null;
                    vDefProp = null;
                    vSearch = null;
                    vSource = null;
                    last = text[2];
                }

                if (text.length == 4)
                {
                    if (text[3].equals("DISPLAY"))
                        vDisplay = props_[i]._value;
                    else if (text[3].equals("EVSNAME"))
                        vName = props_[i]._value;
                    else if (text[3].equals("ACCESSREQUIRED"))
                        vAccess = props_[i]._value;
                    else if (text[3].equals("VOCABCODETYPE"))
                        vSource = props_[i]._value;
                }
                else if (text.length == 5)
                {
                    if (text[3].equals("PROPERTY"))
                    {
                        if (text[4].equals("DEFINITION"))
                        {
                            vDefProp = props_[i]._value;
                        }
                        else if (text[4].equals("NAMESEARCH"))
                        {
                            vSearch = props_[i]._value;
                        }
                    }
                }
            }
        }

        vocab = new EVSVocab();
        vocab._display = vDisplay;
        vocab._vocab = vName;
        vocab._access = vAccess;
        vocab._preferredDefinitionProp = (vDefProp == null) ? defProp : vDefProp;
        vocab._preferredNameProp = (vSearch == null) ? vDisplay : vSearch;
        vocab._ed = new NonMetaTh(vocab);
        vocab._source = vSource;
        vocabs.add(vocab);

        EVSVocab[] rs = new EVSVocab[vocabs.size()];
        for (int i = 0; i < rs.length; ++i)
        {
            rs[i] = vocabs.get(i);
        }
        return rs;
    }
    
    private class EVSVocab
    {
        /**
         * Constructor
         */
        public EVSVocab()
        {
        }
        
        /**
         */
        public String _vocab;
        
        /**
         */
        public String _display;

        /**
         */
        public String _preferredNameProp;

        /**
         */
        public String _preferredDefinitionProp;

        /**
         */
        public String _source;

        /**
         */
        public String _source2;
        
        /**
         */
        public EVSData _ed;
        
        /**
         * 
         */
        public String _access;
    }
    
    private abstract class EVSData
    {
        /**
         * Constructor
         * 
         * @param vocab_ the vocabulary description 
         */
        public EVSData(EVSVocab vocab_)
        {
            reset();
            _vocab = vocab_;
        }
        
        /**
         * Reset the data elements to empty.
         *
         */
        public void reset()
        {
            _msg = "";
            _flag = true;
        }

        /**
         * Determine the recommended concept name when it is missing.
         * 
         * @return the recommended concept name
         */
        abstract public String recommendName();

        /**
         * Search EVS for the Concept Code.
         * 
         * @param query_ the EVSQuery defined by the caCORE API
         */
        abstract public CodedNodeSet search(LexEVSService service_);

        /**
         * Validate the Concept Code, Concept Name and Concept Definition.
         */
        abstract public void validate();

        /**
         * The messages from the validate() method.
         */
        public String _msg;

        /**
         * The recommended name for a Concept.
         */
        public String _name;

        /**
         * The caDSR Concept record to validate.
         */
        public ConceptItem _rec;

        /**
         * The validation flag, true indicates the Name does not match.
         */
        public boolean _flag;

        /**
         * The concept list returned by the caCORE API EVS Query.
         */
        public List _cons;

        /**
         * The name property list use to validate the concept name.
         */
        public EVSVocab _vocab;
    }
    
    private class MetaTh extends EVSData
    {
        /**
         * Constructor
         * 
         * @param vocab_ the EVS vocab description 
         */
        public MetaTh(EVSVocab vocab_)
        {
            super(vocab_);
        }

        @Override
        public CodedNodeSet search(LexEVSService service_)
        {
        	CodedNodeSet cns = null;
        	try {
				cns = service_.getNodeSet("NCI Metathesaurus", null, null);
				
				ConceptReferenceList crefs = ConvenienceMethods.createConceptReferenceList(new String[]{_rec._preferredName},"NCI Metathesaurus");
				cns.restrictToCodes(crefs);
				
//				cns = cns.restrictToMatchingProperties(
//								Constructors.createLocalNameList("conceptCode"), 
//								null, 
//								_rec._preferredName, 
//								MatchAlgorithms.exactMatch.name(), 
//								null
//							);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return cns;
        }

        @Override
        public String recommendName()
        {
        	EVSConcept obj = (EVSConcept) _cons.get(0);
        	if (obj != null) {
        		return obj.preferredName;
        	}
            return "";
        }

        @Override
        public void validate()
        {
        	// The search returns a list of results so process all.
            for (int i = 0; i < _cons.size(); ++i)
            {
                // The objects are Meta Thesaurus
            	EVSConcept temp = (EVSConcept) _cons.get(i);
                
                // Check the default name.
                if (_rec._longName.compareToIgnoreCase(temp.preferredName) == 0)
                {
                    _flag = false;
                    break;
                }
                
                // Check the synonyms if the default name didn't match.
                List value = temp.synonyms;
                if (value.indexOf(_rec._longName) > -1)
                {
                    _flag = false;
                    break;
                }
            }
            // We didn't find a name so recommend one.
            if (_flag)
                _name = recommendName();

            // Must have a definition source to proceed.
            if (_rec._definitionSource == null || _rec._definitionSource.length() == 0)
            {
                boolean defFlag = false;
                for (int i = 0; i < _cons.size(); ++i)
                {
                    // Need definitions.
                    EVSConcept temp = (EVSConcept) _cons.get(i);
                    Definition[] defs = temp.definitions;
                    if (defs != null && defs.length > 0)
                    {
                        defFlag = true;
                        break;
                    }
                }
                // The caDSR definition source is missing and EVS has possible definitions
                if (defFlag)
                    _msg += formatMsg(_MSG002);
                
                // EVS has no definitions for this term and the caDSR contains definition text
                else if (_rec._preferredDefinition.length() > 0)
                    _msg += formatMsg(_MSG003);
            }
            else
            {
                // Process the search results again only this time for the definition.
                boolean srcFlag = true;
                boolean defFlag = true;
                boolean defCol = true;
                String defSource = null;
                int full = 0;
                for (int i = 0; i < _cons.size() && full < 3 && srcFlag && defFlag; ++i)
                {
                    // Need definitions.
                    EVSConcept temp = (EVSConcept) _cons.get(i);
                    Definition[] defs = temp.definitions;
                    if (defs != null && defs.length > 0)
                    {
                        defCol = false;

                        // Check the definition source and definition text.
                        for (Definition def : defs)
                        {
                            full = 0;
                            org.LexGrid.commonTypes.Source[] defsors = def.getSource();
                            for (org.LexGrid.commonTypes.Source defsor: defsors) {
                            	if (defsor != null && defsor.getContent().equals(_rec._definitionSource))
                                {
                                    srcFlag = false;
                                    full += 1;
                                }
                            	
                            	if (def.getValue().getContent().equals(_rec._preferredDefinition))
                                {
                                    if (defsor != null)
                                        defSource = defsor.getContent();
                                    defFlag = false;
                                    full += 2;
                                }
                                if (full == 3)
                                    break;
                            }
                        }
                    }
                }
                
                // Did we find everything?
                if (full == 3)
                    return;

                if (defCol)
                {
                    // No definitions exist in EVS and the caDSR contains a definition source [{0}]
                    _msg += formatMsg(_MSG004, _rec._definitionSource);
                    return;
                }

                if (srcFlag)
                {
                    // Definition Source [{0}] does not exist for this Concept
                    if (defFlag)
                        _msg += formatMsg(_MSG005, _rec._definitionSource);
                    
                    // Definition matches source [{0}] but was expecting source to be [{1}]
                    else if (defSource != null)
                        _msg += formatMsg(_MSG006, defSource, _rec._definitionSource);
                    
                    // Definition matches unnamed source but was expecting source to be [{0}]
                    else
                        _msg += formatMsg(_MSG007, _rec._definitionSource);
                }

                // Definition does not match EVS. [{0}]
                else if (defFlag)
                    _msg += formatMsg(_MSG008, _rec._definitionSource);
                
                // Definition and Source found for concept but Definition matches source [{0}] and expecting source [{1}]
                else
                    _msg += formatMsg(_MSG009, defSource, _rec._definitionSource);
            }
        }
    }
    
    private class NonMetaTh extends EVSData
    {
        /**
         * Constructor
         * 
         * @param vocab_ the EVS vocab description 
         */
        public NonMetaTh(EVSVocab vocab_)
        {
            super(vocab_);
        }

        @Override
        public CodedNodeSet search(LexEVSService service_)
        {
        	CodedNodeSet cns = null;
            try {
				cns = service_.getNodeSet(_vocab._vocab, null, null);
				
				ConceptReferenceList crefs = ConvenienceMethods.createConceptReferenceList(new String[]{_rec._preferredName},_vocab._vocab);
				cns.restrictToCodes(crefs);
//				cns = cns.restrictToMatchingProperties(
//								Constructors.createLocalNameList("conceptCode"), 
//								null, 
//								_rec._preferredName, 
//								MatchAlgorithms.exactMatch.name(), 
//								null
//							);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return cns;
        }

        @Override
        public String recommendName()
        {
            EVSConcept obj = (EVSConcept) _cons.get(0);

            if (obj.preferredName != null)
                return obj.preferredName;
            
            return "no recommendations available";
        }

        @Override
        public void validate()
        {
            try
            {
                for (int i = 0; i < _cons.size(); ++i)
                {
                    List collection = null;
                    EVSConcept temp = (EVSConcept) _cons.get(i);
                    if (temp.preferredName.compareToIgnoreCase(_rec._longName) == 0)
                    {
                        _flag = false;
                        break;
                    }
                        
                    // In the off chance none of the current defined attributes contain the name, perhaps
                    //  an attribute was removed from the Curation Tool options after a concept was
                    // copied from EVS.
                    collection = temp.properties;
                    for (int n = 0; n < collection.size(); ++n)
                    {
                        org.LexGrid.commonTypes.Property prop = (org.LexGrid.commonTypes.Property) collection.get(n);
                        if (_rec._longName.compareToIgnoreCase(prop.getValue().getContent()) == 0)
                        {
                            // The collection.contains() test above doesn't always catch matching property names because of case.
                            if (preferredName.equals(prop.getPropertyName()) || _vocab._preferredNameProp.compareToIgnoreCase(prop.getPropertyName()) == 0)
                            {
                                _flag = false;
                                break;
                            }

                            // Name matches on property {0} but expected to match on property {1}
                            _msg += formatMsg(_MSG010, prop.getPropertyName(),  ((_vocab._preferredNameProp == null) ? "(default)" : _vocab._preferredNameProp));
                            _flag = false;
                            break;
                        }
                    }
                }
                if (_flag)
                    _name = recommendName();
            }
            catch (ClassCastException ex)
            {
                // Mislabeled, should be a MetaThesaurus Concept
                _msg += formatMsg(_MSG011);
                _flag = false;
                _logger.warn(ex.toString());
                
                // Can't continue if this exception occurs.
                return;
            }

            boolean srcFlag = true;
            boolean defFlag = true;
            for (int i = 0; i < _cons.size() && srcFlag && defFlag; ++i)
            {
            	EVSConcept temp = (EVSConcept) _cons.get(i);
                Definition[] definitions = temp.definitions;
                for (Definition def : definitions)
                {
                	if (def.isIsPreferred() != null && def.isIsPreferred()) {
                		srcFlag = false;
                		org.LexGrid.commonTypes.Source[] sources = def.getSource();
                		if (sources == null || sources.length == 0) {
                			if  (_rec._definitionSource != null && _rec._definitionSource.length() > 0)
                                _msg += formatMsg(_MSG012, _rec._definitionSource);
                		}
                		else {
                			if  (_rec._definitionSource == null || _rec._definitionSource.length() == 0)
                                _msg += formatMsg(_MSG013, sources[0].getContent());
                            else
                            {
                            	boolean srcExists = false;
                            	for (org.LexGrid.commonTypes.Source src: sources) {
                            		if (src.getContent().equals(_rec._definitionSource)) {
                            			srcExists = true;
                            			break;
                            		}
                            	}
                            	
                            	if (!srcExists) {
                            		_msg += formatMsg(_MSG014, _rec._definitionSource, sources[0].getContent());
                            	}   
                            }
                		}
                		
                		if (def.getValue().getContent().equalsIgnoreCase(_rec._preferredDefinition)) {
                			defFlag = false;
                		}
                	}
                }
            }
            if (srcFlag)
            {
                // No definitions exist in EVS for property [{0}] can not compare definitions
                if (_rec._preferredDefinition.length() > 0)
                    _msg += formatMsg(_MSG015, _vocab._preferredDefinitionProp);
            }
            else if (defFlag)
            {
                // Definition does not match EVS
                if (_rec._definitionSource == null)
                    _msg += formatMsg(_MSG016);
                
                // Definition does not match EVS [{0}]
                else
                    _msg += formatMsg(_MSG017, _rec._definitionSource);
            }
        }
    }
    
    private class EVSConcept  {
    	  public String preferredName;
    	  public String code;
    	  public List synonyms;
    	  public List<org.LexGrid.commonTypes.Property> properties;
    	  public Definition[] definitions;
    }
    
    
    private Vector<String> validate()
    {
        // Get the EVS URL and establish the application service.
//      String evsURL = _db.selectEvsUrl();
    	Vector<String> msgs = new Vector<String>();
        msgs.add(formatTitleMsg());
      String evsURL = "http://lexevsapi6-stage.nci.nih.gov/lexevsapi64";
      
      LexEVSService service;
      try
      {
////      	service = (LexBIGService)ApplicationServiceProvider.getApplicationService("EvsServiceInfo");
     	service = (LexEVSService) ApplicationServiceProvider
			        .getApplicationServiceFromUrl(evsURL, "EvsServiceInfo");
      	
      	service = RemoteServerUtil.createLexEVSService(evsURL);
      	return validate(service);
			
      }
      catch (Exception ex)
      {
          msgs.add("EVS API URL " + evsURL + " " + ex.toString());
          StackTraceElement[] list = ex.getStackTrace();
          for (int i = 0; i < list.length; ++i)
              msgs.add(list[i].toString());
          return msgs;
      }
    }
    /**
     * Validate the caDSR Concepts against EVS
     * 
     * @return exception, error and information messages
     */
    private Vector<String> validate(LexEVSService service)
    {
        // Seed message list with column headings.
        Vector<String> msgs = new Vector<String>();
        msgs.add(formatTitleMsg());

        // Get all the Concepts from the caDSR.
//        Vector<ConceptItem> concepts = _db.selectConcepts();
        Vector<ConceptItem> concepts = selectConcepts();
        EVSVocab[] vocabs = parseProperties(selectEVSVocabs());

  

        // Check each concept with EVS.
        String msg = null;
        String name = null;
        int count = 0;
        for (ConceptItem rec : concepts)
        {
            // Reset loop variables.
            msg = "";
            if (rec._preferredDefinition.toLowerCase().startsWith("no value exists"))
                rec._preferredDefinition = "";

            // Show status messages when debugging.
            if ((count % 100) == 0)
            {
                _logger.debug("Completed " + count + " out of " + concepts.size() + " (" + (count * 100 / concepts.size()) + "%) . Message/Failure count " + msgs.size() + " (" + String.valueOf(msgs.size() * 100 / concepts.size()) + "%)");
            }
            ++count;

            EVSVocab vocab = null;
            while (true)
            {
                // Missing EVS Source
                if (rec._evsSource == null || rec._evsSource.length() == 0)
                {
                    msg += formatMsg(_MSG020);
                    break;
                }

                // Determine the desired vocabulary using the EVS source value in caDSR.
                // This translation should be in the tool options table or the data content of the EVS
                // source column should use the standard vocabulary abbreviation.
                for (int i = 0; i < vocabs.length; ++i)
                {
                    if (rec._evsSource.equals(vocabs[i]._source))
                    {
                        vocab = vocabs[i];
                        break;
                    }
                }
                if (vocab == null)
                {
                    if (rec._evsSource.equals(vocabs[0]._source2))
                        vocab = vocabs[0];
                }

                // Unknown EVS Source {0}
                if (vocab == null)
                {
                    msg += formatMsg(_MSG021, rec._evsSource);
                    break;
                }
                
                // Missing Concept Code
                if (rec._preferredName== null || rec._preferredName.length() == 0)
                {
                    msg += formatMsg(_MSG022);
                    break;
                }

                EVSData ed = vocab._ed;
                ed.reset();
                ed._rec = rec;

                CodedNodeSet cns = ed.search(service);
                
                try
                {
                    // Get the attributes for the concept code. 
                    ed._cons = resolveNodeSet(cns, true);
                }
                catch (ApplicationException ex)
                {
                    // Invalid concept code
                    if (ex.toString().indexOf("Invalid concept code") > -1)
                    {
                        msg += formatMsg(_MSG023);
                        _logger.warn(ex.toString());
                        break;
                    }
                    
                    // Invalid concept ID
                    else if (ex.toString().indexOf("Invalid conceptID") > -1)
                    {
                        msg += formatMsg(_MSG024);
                        _logger.warn(ex.toString());
                        break;
                    }
                    
                    // An unexpected exception occurred so record it and terminate the validation.
                    else
                    {
                        msg += formatMsg(ex.toString());
                        // msgs.add(msg);
                        // _logger.error(ex.toString());
                        // return msgs;
                        break;
                    }
                }
                catch (Exception ex)
                {
                    msgs.add(ex.toString());
                    StackTraceElement[] list = ex.getStackTrace();
                    for (int i = 0; i < list.length; ++i)
                        msgs.add(list[i].toString());
                    return msgs;
                }

                // Failed to retrieve EVS concept
                if (ed._cons.size() == 0)
                {
                    msg += formatMsg(_MSG025);
                    break;
                }
                    
                // Missing Concept Long Name, recommend using [{0}]
                if (rec._longName == null || rec._longName.length() == 0)
                {
                    msg += formatMsg(_MSG026, ed.recommendName());
                    break;
                }

                // Assume we will not match the concept name.
                boolean flag = true;
                name = null;

                // Validate data.
                ed.validate();
                flag = ed._flag;
                name = ed._name;
                msg += ed._msg;
                
                // The name of the concept in the caDSR doesn't match anything in EVS for this concept code.
                if (flag)
                {
                    // Concept name does not match EVS
                    if (name == null)
                        msg += formatMsg(_MSG018);
                    
                    // Concept name does not match EVS, expected [{0}]
                    else
                        msg += formatMsg(_MSG019, name);
                }

                break;
            }

            // If something happened during the validation, record the message and continue with the next concept.
            if (msg.length() > 0)
            {
                msg = formatMsg(rec, vocab, msg);
                msgs.add(msg);
                if (msgs.size() >= _maxMsgs)
                {
                    msgs.add(formatMaxMsg());
                    break;
                }
            }
        }

        // Return all the messages, the validation processing is complete.
        return msgs;
    }
    
    public List<EVSConcept> resolveNodeSet(CodedNodeSet cns, boolean includeRetiredConcepts) throws Exception {
		
		if (!includeRetiredConcepts) {
			cns.restrictToStatus(CodedNodeSet.ActiveOption.ACTIVE_ONLY, null);
		}
		CodedNodeSet.PropertyType propTypes[] = new CodedNodeSet.PropertyType[2];
		propTypes[0] = CodedNodeSet.PropertyType.PRESENTATION;
		propTypes[1] = CodedNodeSet.PropertyType.DEFINITION;
		
		SortOptionList sortCriteria = Constructors.createSortOptionList(new String[]{"matchToQuery"});
		
		ResolvedConceptReferencesIterator results = cns.resolve(sortCriteria, null,new LocalNameList(), propTypes, true);
		
		return getEVSConcepts(results);
	}
    
    private List<EVSConcept> getEVSConcepts(ResolvedConceptReferencesIterator rcRefIter) throws Exception {
    	List<EVSConcept> evsConcepts = new ArrayList<EVSConcept>();
    	if (rcRefIter != null) {
    		while (rcRefIter.hasNext()) {
    			evsConcepts.add(getEVSConcept(rcRefIter.next()));
    		}
    	}
    	return evsConcepts;
    }
    
    private EVSConcept getEVSConcept(ResolvedConceptReference rcRef) {
		EVSConcept evsConcept = new EVSConcept();
		evsConcept.code = rcRef.getCode();
		
		Entity entity = rcRef.getEntity();
		evsConcept.preferredName = rcRef.getEntityDescription().getContent();
		evsConcept.definitions = entity.getDefinition();
		setPropsAndSyns(evsConcept, entity);
		
		return evsConcept;
	}
	
	private void setPropsAndSyns(EVSConcept evsConcept, Entity entity) {
		List<Property> properties = new ArrayList<Property>();
		List<String> synonyms = new ArrayList<String>();
		
		if (entity != null) {
			org.LexGrid.commonTypes.Property[] entityProps = entity.getAllProperties();
			for (org.LexGrid.commonTypes.Property entityProp: entityProps) {
				
				if (entityProp instanceof Presentation) {
					properties.add(entityProp);
				}
				else {
					String propName = entityProp.getPropertyName();
					String propValue = entityProp.getValue().getContent();
					
					if (propName.equalsIgnoreCase("FULL_SYN") || propName.equalsIgnoreCase("Synonym")) {
						synonyms.add(propValue);
					}
				}
			}
		}
		
		evsConcept.properties = properties;
		evsConcept.synonyms = synonyms;
	}
    
    private static String formatMsg(String msg_, String ... subs_)
    {
        String text = msg_;
        for (int i = 0; i < subs_.length; ++i)
        {
            String temp = "{" + i +  "}";
            text = text.replace(temp, subs_[i]);
        }
        return "\n" + text;
    }
    
    private String formatMsg(ConceptItem rec_, EVSVocab vocab_, String msg_)
    {
        return rec_._longName + AuditReport._ColSeparator
        + rec_._publicID + AuditReport._ColSeparator 
        + rec_._version + AuditReport._ColSeparator
        + ((vocab_ == null) ? "" : vocab_._display) + AuditReport._ColSeparator 
        + rec_._preferredName + AuditReport._ColSeparator
            + ((msg_.charAt(0) == '\n') ? msg_.substring(1) : msg_);
    }
    
    private String formatMaxMsg()
    {
        return "*** Maximum Messages ***" + AuditReport._ColSeparator
        + "***" + AuditReport._ColSeparator
        + "***" + AuditReport._ColSeparator
        + "***" + AuditReport._ColSeparator
        + "***" + AuditReport._ColSeparator
        + "The Error Message maximum limit [" + _maxMsgs + "] has been reached, report truncated.";
    }
    
    private String formatTitleMsg()
    {
        return "Concept" + AuditReport._ColSeparator
        + "Public ID" + AuditReport._ColSeparator
        + "Version" + AuditReport._ColSeparator
        + "Vocabulary" + AuditReport._ColSeparator
        + "Concept Code" + AuditReport._ColSeparator
        + "Message";
    }
    
    private static final String preferredName = "Preferred_Name";
    
    private static final String _MSG001 = "Mislabeled as a MetaThesaurus Concept.";
    private static final String _MSG002 = "The caDSR definition source is missing and EVS has possible definitions.";
    private static final String _MSG003 = "EVS has no definitions for this term and the caDSR contains definition text.";
    private static final String _MSG004 = "No definitions exist in EVS and the caDSR contains a definition source [{0}].";
    private static final String _MSG005 = "Definition Source [{0}] does not exist for this Concept.";
    private static final String _MSG006 = "Definition matches source [{0}] but was expecting source to be [{1}]";
    private static final String _MSG007 = "Definition matches unnamed source but was expecting source to be [{0}]";
    private static final String _MSG008 = "Definition does not match EVS. [{0}]";
    private static final String _MSG009 = "Definition and Source found for concept but Definition matches source [{0}] and expecting source [{1}]";
    private static final String _MSG010 = "Name matches on property [{0}] but expected to match on property [{1}]";
    private static final String _MSG011 = "Mislabeled, should be a MetaThesaurus Concept.";
    private static final String _MSG012 = "The EVS definition source is missing, caDSR is [{0}].";
    private static final String _MSG013 = "The caDSR definition source is missing, EVS is [{0}].";
    private static final String _MSG014 = "The caDSR definition source [{0}] does not match EVS [{1}]";
    private static final String _MSG015 = "No definitions exist in EVS for property [{0}] can not compare definitions.";
    private static final String _MSG016 = "Definition does not match EVS.";
    private static final String _MSG017 = "Definition does not match EVS [{0}].";
    private static final String _MSG018 = "Concept name does not match EVS";
    private static final String _MSG019 = "Concept name does not match EVS, expected [{0}].";
    private static final String _MSG020 = "Missing EVS Source";
    private static final String _MSG021 = "Unknown EVS Source [{0}]";
    private static final String _MSG022 = "Missing Concept Code";
    private static final String _MSG023 = "Invalid concept code.";
    private static final String _MSG024 = "Invalid concept ID.";
    private static final String _MSG025 = "Failed to retrieve EVS concept";
    private static final String _MSG026 = "Missing Concept Long Name, recommend using [{0}]";
    
    private static final int _maxMsgs = 200;
    private static final Logger _logger = Logger.getLogger(AuditConceptToEVS.class.getName());
    
    
    
	public static DBProperty[] selectEVSVocabs(){
		
		int x = 10;
		DBProperty[] props = new DBProperty[66];
		DBProperty prop = new DBProperty("NCI Thesaurus","EVS.VOCAB.02.DISPLAY");
		props[0] = prop;
		prop = new DBProperty("NCI Thesaurus","EVS.VOCAB.02.EVSNAME");
		props[1] = prop;
		prop = new DBProperty("NCI Thesaurus","EVS.VOCAB.02.EVSNAME");
		props[2] = prop;
		prop = new DBProperty("FULL_SYN","EVS.VOCAB.02.PROPERTY.NAMESEARCH");
		props[3] = prop;
		prop = new DBProperty("NCI_CONCEPT_CODE","EVS.VOCAB.02.VOCABCODETYPE");
		props[4] = prop;
		prop = new DBProperty("CTCAE","EVS.VOCAB.04.DISPLAY");
		props[5] = prop;
		prop = new DBProperty("Common Terminology Criteria for Adverse Events","EVS.VOCAB.04.EVSNAME");
		props[6] = prop;
		prop = new DBProperty("DEFINITION","EVS.VOCAB.04.PROPERTY.DEFINITION");
		props[7] = prop;
		prop = new DBProperty("Synonym","EVS.VOCAB.04.PROPERTY.NAMESEARCH");
		props[8] = prop;
		prop = new DBProperty("CTCAE_CODE","EVS.VOCAB.04.VOCABCODETYPE");
		props[9] = prop;
		prop = new DBProperty("GO","EVS.VOCAB.06.DISPLAY");
		props[10] = prop;
		prop = new DBProperty("Gene Ontology","EVS.VOCAB.06.EVSNAME");
		props[11] = prop;
		prop = new DBProperty("GO_CODE","EVS.VOCAB.06.VOCABCODETYPE");
		props[12] = prop;
		prop = new DBProperty("HGNC","EVS.VOCAB.08.DISPLAY");
		props[13] = prop;
		prop = new DBProperty("HUGO Gene Nomenclature Committee Ontology","EVS.VOCAB.08.EVSNAME");
		props[14] = prop;
		prop = new DBProperty("HUGO_CODE","EVS.VOCAB.08.VOCABCODETYPE");
		props[15] = prop;
		prop = new DBProperty("HL7","EVS.VOCAB.10.DISPLAY");
		props[16] = prop;
		prop = new DBProperty("HL7 Reference Information Model","EVS.VOCAB.10.EVSNAME");
		props[17] = prop;
		prop = new DBProperty("HL7_CODE","EVS.VOCAB.10.VOCABCODETYPE");
		props[18] = prop;
		prop = new DBProperty("ICD-9-CM","EVS.VOCAB.12.DISPLAY");
		props[19] = prop;
		prop = new DBProperty("ICD-9-CM","EVS.VOCAB.12.DISPLAY");
		props[20] = prop;
		prop = new DBProperty("International Classification of Diseases, Ninth Revision","EVS.VOCAB.12.EVSNAME");
		props[21] = prop;
		prop = new DBProperty("ICD-9_CM_CODE","EVS.VOCAB.12.VOCABCODETYPE");
		props[22] = prop;
		prop = new DBProperty("ICD-10_","EVS.VOCAB.14.DISPLAY");
		props[23] = prop;
		prop = new DBProperty("ICD-10","EVS.VOCAB.14.EVSNAME");
		props[24] = prop;
		prop = new DBProperty("ICD-10_CODE","EVS.VOCAB.14.VOCABCODETYPE");
		props[25] = prop;
		prop = new DBProperty("ICD-10-CM","EVS.VOCAB.16.DISPLAY");
		props[26] = prop;
		prop = new DBProperty("International Classification of Diseases, 10th Edition, Clinical Modification","EVS.VOCAB.16.EVSNAME");
		props[27] = prop;
		prop = new DBProperty("IDC-10_CM_CODE","EVS.VOCAB.16.VOCABCODETYPE");
		props[28] = prop;
		prop = new DBProperty("LOINC","EVS.VOCAB.18.DISPLAY");
		props[29] = prop;
		prop = new DBProperty("Logical Observation Identifier Names and Codes","EVS.VOCAB.18.EVSNAME");
		props[30] = prop;
		prop = new DBProperty("LOINC_CODE","EVS.VOCAB.18.VOCABCODETYPE");
		props[31] = prop;
		prop = new DBProperty("10382","EVS.VOCAB.20.ACCESSREQUIRED");
		props[32] = prop;
		prop = new DBProperty("MedDRA","EVS.VOCAB.20.DISPLAY");
		props[33] = prop;
		prop = new DBProperty("MedDRA (Medical Dictionary for Regulatory Activities Terminology)","EVS.VOCAB.20.EVSNAME");
		props[34] = prop;
		prop = new DBProperty("definition","EVS.VOCAB.20.PROPERTY.DEFINITION");
		props[35] = prop;
		prop = new DBProperty("MEDDRA_CODE","EVS.VOCAB.20.VOCABCODETYPE");
		props[36] = prop;
		prop = new DBProperty("MGED","EVS.VOCAB.22.DISPLAY");
		props[37] = prop;
		prop = new DBProperty("The MGED Ontology","EVS.VOCAB.22.EVSNAME");
		props[38] = prop;
		prop = new DBProperty("NCI_MO_CODE","EVS.VOCAB.22.VOCABCODETYPE");
		props[39] = prop;
		prop = new DBProperty("NCI Metathesaurus","VS.VOCAB.24.DISPLAY");
		props[40] = prop;
		prop = new DBProperty("NCI Metathesaurus","EVS.VOCAB.24.EVSNAME");
		props[41] = prop;
		prop = new DBProperty("NPO","EVS.VOCAB.28.DISPLAY");
		props[42] = prop;
		prop = new DBProperty("Nanoparticle Ontology","EVS.VOCAB.28.EVSNAME");
		props[43] = prop;
		prop = new DBProperty("NPO_CODE","EVS.VOCAB.28.VOCABCODETYPE");
		props[44] = prop;
		prop = new DBProperty("OBI","EVS.VOCAB.30.DISPLAY");
		props[45] = prop;
		prop = new DBProperty("Ontology for Biomedical Investigations","EVS.VOCAB.30.EVSNAME");
		props[46] = prop;
		prop = new DBProperty("OBI_CODE","EVS.VOCAB.30.VOCABCODETYPE");
		props[47] = prop;
		prop = new DBProperty("RadLex","EVS.VOCAB.32.DISPLAY");
		props[48] = prop;
		prop = new DBProperty("Radiology Lexicon","EVS.VOCAB.32.EVSNAME");
		props[49] = prop;
		prop = new DBProperty("RADLEX_CODE","EVS.VOCAB.32.VOCABCODETYPE");
		props[50] = prop;
		prop = new DBProperty("SNOMED","EVS.VOCAB.34.DISPLAY");
		props[51] = prop;
		prop = new DBProperty("SNOMED Clinical Terms","EVS.VOCAB.34.EVSNAM");
		props[52] = prop;
		prop = new DBProperty("SNOMED_CODE","EVS.VOCAB.34.VOCABCODETYPE");
		props[53] = prop;
		prop = new DBProperty("UMLS SemNet","EVS.VOCAB.36.DISPLAY");
		props[54] = prop;
		prop = new DBProperty("UMLS Semantic Network","EVS.VOCAB.36.EVSNAME");
		props[55] = prop;
		prop = new DBProperty("UMLS_SEMNET_CODE","EVS.VOCAB.36.VOCABCODETYPE");
		props[56] = prop;
		prop = new DBProperty("VA_NDFRT","EVS.VOCAB.38.DISPLAY");
		props[57] = prop;
		prop = new DBProperty("National Drug File - Reference Terminology","EVS.VOCAB.38.EVSNAME");
		props[58] = prop;
		prop = new DBProperty("MeSH_Definition","EVS.VOCAB.38.PROPERTY.DEFINITION");
		props[59] = prop;
		prop = new DBProperty("VA_NDF_CODE","EVS.VOCAB.38.VOCABCODETYPE");
		props[60] = prop;
		prop = new DBProperty("Zebrafish","EVS.VOCAB.40.DISPLAY");
		props[61] = prop;
		prop = new DBProperty("Zebrafish","EVS.VOCAB.40.EVSNAM");
		props[62] = prop;
		prop = new DBProperty("synonym","EVS.VOCAB.40.PROPERTY.NAMESEARCH");
		props[63] = prop;
		prop = new DBProperty("ZEBRAFISH_CODE","EVS.VOCAB.40.VOCABCODETYPE");
		props[64] = prop;
		prop = new DBProperty("DEFINITION","EVS.VOCAB.ALL.PROPERTY.DEFINITION");
		props[65] = prop;
		
		return props;
		

		
		
//        String select = "select opt.value, opt.property from sbrext.tool_options_view_ext opt where opt.tool_name = 'CURATION' and ("
//                + "opt.property like 'EVS.VOCAB.%.PROPERTY.NAMESEARCH' or "
//                + "opt.property like 'EVS.VOCAB.%.EVSNAME' or "
//                + "opt.property like 'EVS.VOCAB.%.DISPLAY' or "
//                + "opt.property like 'EVS.VOCAB.%.PROPERTY.DEFINITION' or "
//                + "opt.property like 'EVS.VOCAB.%.VOCABCODETYPE' or "
//                + "opt.property like 'EVS.VOCAB.%.ACCESSREQUIRED' "
//                + ") order by opt.property";
//        
//        DBProperty[] props = new DBProperty[rs._data.length];
//        for (int i = 0; i < rs._data.length; ++i)
//        {
//            props[i] = new DBProperty(rs._data[i]._label, rs._data[i]._val);;
//        }
//        return props;
		
	}
	
	public static Vector<ConceptItem> selectConcepts(){
		Vector<ConceptItem> conItems = new Vector<ConceptItem>();
	
//		conItems.add(parseConcept("CON_IDSEQ,CON_ID,VERSION,EVS_SOURCE,PREFERRED_NAME,LONG_NAME,DEFINITION_SOURCE,PREFERRED_DEFINITION"));
		conItems.add(parseConcept("B918227F-B1E1-6EA7-E040-BB89AD434A76,3379200,1,UMLS_CUI,C1723325,(131)I-ch81C6,,No Value Exists"));
		conItems.add(parseConcept("AD8D2096-2319-66E7-E040-BB89AD4364A0,3284213,1,NCI_META_CUI,CL409124,(CS).CD3+CD4+,,No Value Exists"));
		conItems.add(parseConcept("AD8D2096-22F3-66E7-E040-BB89AD4364A0,3284211,1,NCI_META_CUI,CL409131,(CS).CD3+CD8A+,,No Value Exists"));
		conItems.add(parseConcept("FD0F852B-0497-7140-E034-0003BA3F9857,2322161,1,NCI_META_CUI,CL225712,+1,,No value exists."));
		conItems.add(parseConcept("F37D0428-D938-6787-E034-0003BA3F9857,2204580,1,NCI_META_CUI,CL209433,/week,No value exists.,No value exists."));
		conItems.add(parseConcept("2CABC045-4CC8-1DB3-E044-0003BA3F9857,2615778,1,NCI_CONCEPT_CODE,C1072,1,1-Dimethylhydrazine,NCI, A clear, colorless, flammable, hygroscopic liquid with a fishy smell that emits toxic fumes of nitrogen oxides when heated to decomposition, and turns yellow upon contact with air. 1,1-Dimethylhydrazine is mainly used as a high-energy fuel in jets and rockets, but is also used in chemical synthesis, in photography and to control the growth of vegetation. This substance is also found in tobacco products. Exposure to 1,1-dimethylhydrazine results in irritation of skin, eyes and mucous membranes, and can affect liver and central nervous system. 1,1-Dimethylhydrazine is reasonably anticipated to be a human carcinogen. (NCI05)"));
		conItems.add(parseConcept("AF1E934A-ACED-5C19-E040-BB89AD436F1A,3293855,1,UMLS_CUI,C0043867,1,24,25-trihydroxyergocalciferol,,No Value Exists"));
		conItems.add(parseConcept("F37D0428-BF60-6787-E034-0003BA3F9857,2202926,1,NCI_CONCEPT_CODE,C957,10-Deacetyltaxol,NCI,An analog of paclitaxel with antineoplastic activity. 10-Deacetyltaxol binds to and stabilizes the resulting microtubules, thereby inhibiting microtubule disassembly which results in cell- cycle arrest at the G2/M phase and apoptosis."));
		conItems.add(parseConcept("F37D0428-D9CC-6787-E034-0003BA3F9857,2204617,1,NCI_CONCEPT_CODE,C2250,10-Propargyl-10-Deazaaminopterin,NCI,A folate analogue inhibitor of dihydrofolate reductase (DHFR) exhibiting high affinity for reduced folate carrier-1 (RFC-1) with antineoplastic and immunosuppressive activities. Pralatrexate selectively enters cells expressing RFC-1; intracellularly, this agent is highly polyglutamylated and competes for the folate binding site of DHFR, blocking tetrahydrofolate synthesis, which may result in depletion of nucleotide precursors; inhibition of DNA, RNA and protein synthesis; and apoptotic tumor cell death. Efficient intracellular polyglutamylation of pralatrexate results in higher intracellular concentrations compared to non-polyglutamylated pralatrexate, which is more readily effuxed by the MRP (multidrug resistance protein) drug efflux pump. RFC-1, an oncofetal protein expressed at highest levels during embryonic development, may be over-expressed on the cell surfaces of various cancer cell types."));
		conItems.add(parseConcept("2CBEBCC7-4863-4D0A-E044-0003BA3F9857,2619400,1,NCI_CONCEPT_CODE,C14567,101 Mouse,NCI,Inbr: 66. Genet: A^w. Origin: Dunn, 1936. (Jackson Labs/Festing)"));
		conItems.add(parseConcept("2CBEBCD1-4280-4D0E-E044-0003BA3F9857,2619401,1,NCI_CONCEPT_CODE,C37318,101/H Mouse,NCI,definition pending"));
		conItems.add(parseConcept("2CBEBCD7-5234-4D12-E044-0003BA3F9857,2619402,1,NCI_CONCEPT_CODE,C37319,101/Rl Mouse,NCI,definition pending"));
		conItems.add(parseConcept("F37D0428-DA78-6787-E034-0003BA3F9857,2204660,1,NCI_CONCEPT_CODE,C13530,10p,NCI,Proximal (short) arm of chromosome 10"));
		conItems.add(parseConcept("F37D0428-D9DC-6787-E034-0003BA3F9857,2204621,1,NCI_CONCEPT_CODE,C13531,10q,NCI,Distal (long) arm of chromosome 10"));
		conItems.add(parseConcept("32BC687C-543F-59F8-E044-0003BA3F9857,2653825,1,NCI_CONCEPT_CODE,C67132,10th Grade Completion,NCI,Indicates that 10th grade is the highest level of educational achievement."));
		conItems.add(parseConcept("AE2F8C5C-4E8C-B65D-E040-BB89AD435A1F,3288004,1,NCI_CONCEPT_CODE,C98262,10th Growth Percentile,NCI,An indication that an individual ranks the same or more than 10 percent of the reference population for a given attribute."));
		conItems.add(parseConcept("84AD8CAD-B9E8-FCC0-E040-BB89AD4326A3,3070942,1,RADLEX_CODE,C13616,11p15,NCI,A chromosome band present on 11p"));
		conItems.add(parseConcept("F37D0428-C974-6787-E034-0003BA3F9857,2203571,1,NCI_CONCEPT_CODE,C13389,11q,NCI,Distal (long) arm of chromosome 11"));
		conItems.add(parseConcept("7D3EEEA8-98B8-1554-E040-BB89AD431463,2988113,1,RADLEX_CODE,C13390,11q23,NCI,A chromosome band present on 11q"));
		conItems.add(parseConcept("32BC6880-031F-59FC-E044-0003BA3F9857,2653826,1,NCI_CONCEPT_CODE,C67133,11th Grade Completion,NCI,Indicates that 11th grade is the highest level of educational achievement."));
		conItems.add(parseConcept("6FB37056-20A9-9C64-E040-BB89AD431A26,2923201,1,NCI_CONCEPT_CODE,C41441,12-Allyldeoxoartemisinin,NCI,A semi-synthetic analogue of Artemisinin - a sesquiterpene lactone extracted from the dry leaves of Artemisia Annua (sweet wormwood) used as anti-malaria agent. Limited data is available on Artemisinin antineoplastic activity."));
		conItems.add(parseConcept("2CBEBCDC-7A29-4D19-E044-0003BA3F9857,2619403,1,NCI_CONCEPT_CODE,C15151,129 Mouse,NCI,Inbr and colour depends on substrain (see below). Origin: Dunn 1928 from crosses of coat colour stocks from English fanciers and a chinchilla stock from Castle. This strain has a common origin with strain 101. Most substrains carry the white-bellied agouti gene AW though only a subset have the agouti pattern as many carry albino or chinchilla and/or the pink-eyed dilution gene, p, which is derived from Asian mice of the Mus musculus type (see also strains SJL, P/J and FS/Ei). (Jackson Labs/Festing)"));
		conItems.add(parseConcept("2CBEBCE2-020A-4D1F-E044-0003BA3F9857,2619404,1,NCI_CONCEPT_CODE,C37320,129/Sv Mouse,NCI,Derived by Dunn (1928) from a mouse/chinchilla cross, the 129/Sv substrain has been recognized as a member of the Parental subgroup of substrains. Strain 129/SvJ was genetically contaminated in 1978 by an unknown strain and differs from other 129 substrains at 25% of SSLP genetic markers, therefore a nomenclature re-designation of 129cX/Sv has been suggested."));
		conItems.add(parseConcept("2CBEBCE6-938C-4D23-E044-0003BA3F9857,2619405,1,NCI_CONCEPT_CODE,C37321,129P1/Re Mouse,NCI,definition pending"));
		conItems.add(parseConcept("2CBEBCEB-3DD6-4D27-E044-0003BA3F9857,2619406,1,NCI_CONCEPT_CODE,C37322,129P1/ReJ Mouse,NCI,definition pending"));
		conItems.add(parseConcept("2CBEBCEF-6DC2-4D2B-E044-0003BA3F9857,2619407,1,NCI_CONCEPT_CODE,C37323,129P1/ReJLacFib Mouse,NCI,definition pending"));
			
		
		
		return conItems;
		
		
		
//		String select = "SELECT con_idseq, con_id, version, evs_source, preferred_name, long_name, definition_source, preferred_definition "
//	            + "FROM sbrext.concepts_view_ext WHERE asl_name NOT LIKE 'RETIRED%' "
//	            + "ORDER BY upper(long_name) ASC";
//        list = new Vector<ConceptItem>();
//        while (rs.next())
//        {
//            ConceptItem rec = new ConceptItem();
//            rec._idseq = rs.getString(1);
//            rec._publicID = rs.getString(2);
//            rec._version = rs.getString(3);
//            rec._evsSource = rs.getString(4);
//            rec._preferredName = rs.getString(5);
//            rec._longName = rs.getString(6);
//            rec._definitionSource = rs.getString(7);
//            rec._preferredDefinition = rs.getString(8);
//            list.add(rec);
//        }
		


	}
	
	private static ConceptItem parseConcept(String conceptText){

		String[] conceptArray = conceptText.split(",");
		ConceptItem rec = new ConceptItem();
		rec._idseq = conceptArray[0];
		rec._publicID = conceptArray[1];
		rec._version = conceptArray[2];
		rec._evsSource = conceptArray[3];
		rec._preferredName = conceptArray[4];
		rec._longName = conceptArray[5];
		rec._definitionSource = conceptArray[6];
		rec._preferredDefinition = conceptArray[7];
		return rec;
	}
}
