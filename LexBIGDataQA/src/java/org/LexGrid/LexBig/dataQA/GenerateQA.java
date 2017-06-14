package org.LexGrid.LexBig.dataQA;
/*
 * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.eclipse.org/legal/epl-v10.html
 *
 */


import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedData;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.DataModel.NCIHistory.NCIChangeEvent;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.History.HistoryService;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.ActiveOption;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.PropertyQualifier;
import org.LexGrid.concepts.Comment;
import org.LexGrid.concepts.Definition;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.LexGrid.concepts.PropertyLink;
import org.LexGrid.naming.SupportedHierarchy;


/**
 * Example showing how to find concept properties and associations based on a
 * code.
 */
public class GenerateQA {

	private static final String SEPARATOR = "|";
	
	private static Map<String,String> mappings ;
	
	static {
		mappings = new HashMap<String, String>();
		mappings.put("(\\b)true(\\b)", "$1Y$2");
		mappings.put("(\\b)false(\\b)", "$1N$2");

	}

	private Map<String, Map<String, Integer>> tallys = new HashMap<String, Map<String, Integer>>();

	private Map<String, Integer> counts = new HashMap<String, Integer>();
	
	private Map<String, String> meta = new HashMap<String, String>();

	
	private Map<String, String> propTypes = new HashMap<String,String>();
	
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMM");

	public GenerateQA() {
		super();
		counts.put("codeCount", 0);
		counts.put("nameCount", 0);
		counts.put("defCount", 0);
		counts.put("commentCount", 0);
		counts.put("instrCount", 0);
		counts.put("propCount", 0);
		counts.put("propLinkCount", 0);
		counts.put("relCount", 0);
		counts.put("relDataCount", 0);
		counts.put("relPropCount", 0);
		counts.put("propPropCount", 0);
		
		counts.put("relTypeCount",0);
		counts.put("sourceCount",0);
		counts.put("propTypeCount",0);

		counts.put("languageCount",0);
		counts.put("propLinkTypeCount",0);
		counts.put("propPropTypeCount",0);
		counts.put("ttyCount",0);
		counts.put("relPropTypeCount",0);
		
		counts.put("changeCount", 0);
		

		tallys.put("nameTally", new HashMap<String, Integer>());
		tallys.put("codeTally", new HashMap<String, Integer>());
		tallys.put("propTally", new HashMap<String, Integer>());
		tallys.put("propLinkTally", new HashMap<String, Integer>());
		tallys.put("propPropTally", new HashMap<String, Integer>());
		tallys.put("relTally", new HashMap<String, Integer>());
		tallys.put("relDataTally", new HashMap<String, Integer>());
		tallys.put("relPropTally", new HashMap<String, Integer>());
		tallys.put("defTally", new HashMap<String, Integer>());
		tallys.put("changeTally", new HashMap<String, Integer>());
	}

	/**
	 * Entry point for processing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			new GenerateQA().run();
		} catch (Exception e) {
			Util.displayAndLogError("REQUEST FAILED !!!", e);
		}
	}

	/**
	 * Process the provided code.
	 * 
	 * @throws LBException
	 */
	public void run() throws LBException, URISyntaxException {
		CodingSchemeSummary css = Util.promptForCodeSystem();
		String code = "";
		if (css != null) {
			LexBIGService lbSvc = LexBIGServiceImpl.defaultInstance();
			String scheme = css.getCodingSchemeURI();
			CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
			csvt.setVersion(css.getRepresentsVersion());

			printProps(code, lbSvc, scheme, csvt);
		}
	}

	/**
	 * Display properties for the given code.
	 * 
	 * @param code
	 * @param lbSvc
	 * @param scheme
	 * @param csvt
	 * @return
	 * @throws LBException
	 */
	protected boolean printProps(String code, LexBIGService lbSvc,
			String scheme, CodingSchemeVersionOrTag csvt) throws LBException, URISyntaxException {
		// Perform the query ...
		
		  ResolvedConceptReferencesIterator matches = lbSvc
		  .getCodingSchemeConcepts(scheme, csvt).restrictToStatus(
		  ActiveOption.ALL, null).resolve(null, null, null);

		  HistoryService history = lbSvc.getHistoryService(scheme);

			counts.put("codeCount", matches.numberRemaining());
		   
		   CodingScheme cs = lbSvc.resolveCodingScheme(scheme, csvt);

		   NCIChangeEvent[] nce = history.getEditActionList(null, new URI(cs.getCodingSchemeURI() + ":" + cs.getRepresentsVersion())).getEntry();
		   
		   counts.put("changeCount", nce.length);
			Map<String, Integer> changeTally = tallys.get("changeTally");
		   
		   for(NCIChangeEvent event : nce) {
				String key = event.getEditaction().value() + "," + dateFormatter.format(event.getEditDate());
				if (changeTally.containsKey(key))
					changeTally.put(key, changeTally.get(key) + 1);
				else
					changeTally.put(key, 1);
			   
		   }
		   
	        counts.put("relTypeCount", cs.getMappings().getSupportedAssociationCount());	
	        counts.put("relPropTypeCount", cs.getMappings().getSupportedAssociationQualifierCount());	
	        counts.put("sourceCount", cs.getSourceCount());
	        counts.put("propTypeCount", cs.getMappings().getSupportedPropertyTypeCount());
	        counts.put("languageCount", cs.getMappings().getSupportedLanguageCount());
	        counts.put("propLinkTypeCount", cs.getMappings().getSupportedPropertyLinkCount());
	        counts.put("propPropTypeCount", cs.getMappings().getSupportedPropertyQualifierTypeCount());
	        counts.put("ttyCount", cs.getMappings().getSupportedRepresentationalFormCount());
	        SupportedHierarchy[] sh = cs.getMappings().getSupportedHierarchy();
	        if(sh.length > 0)
	        	meta.put("isaRelValue",sh[0].getLocalId());
		   
		  /*
			ConceptReferenceList crefs =
				ConvenienceMethods
					.createConceptReferenceList(new String[] { code }, scheme);	

			ResolvedConceptReferenceList matches =
				lbSvc.getCodingSchemeConcepts(scheme, csvt)
					.restrictToStatus(ActiveOption.ALL, null)
					.restrictToCodes(crefs).resolveToList(null,null,null, 1);
			
			if (matches.getResolvedConceptReferenceCount() > 0) {
				ResolvedConceptReference ref =
					(ResolvedConceptReference) matches
						.enumerateResolvedConceptReference().nextElement();
		*/
		// Analyze the result ...

		while (matches.hasNext()) {
			ResolvedConceptReference ref = matches.next();
			Entity entry = ref.getReferencedEntry();
			Map<String, Integer> codeTally = tallys.get("codeTally");
			String key = entry.getStatus() + "," + entry.getIsActive() + "," + entry.isIsAnonymous() + ",Content";
			if (codeTally.containsKey(key))
				codeTally.put(key, codeTally.get(key) + 1);
			else
				codeTally.put(key, 1);
			
			Map<String, Integer> nameTally = tallys.get("nameTally");
			propTypes.clear();
			for (Presentation presentation : entry.getPresentation()) {
				key = presentation.getDegreeOfFidelity() + ","
						+ presentation.getIsPreferred() + ","
						+ presentation.getLanguage() + ","
						+ presentation.getRepresentationalForm() + ","
						+ ref.getCodingSchemeName()
						+ ",Content";
				if (nameTally.containsKey(key))
					nameTally.put(key, nameTally.get(key) + 1);
				else
					nameTally.put(key, 1);
				propTypes.put(presentation.getPropertyId(), presentation.getPropertyName());
			}
			counts.put("nameCount", counts.get("nameCount")
					+ entry.getPresentationCount());
			counts.put("defCount", counts.get("defCount")
					+ entry.getDefinitionCount());
			counts.put("commentCount", counts.get("commentCount")
					+ entry.getCommentCount());
			counts.put("propCount", counts.get("propCount")
					+ entry.getPropertyCount());
			counts.put("propLinkCount", counts.get("propLinkCount")
					+ entry.getPropertyLinkCount());
			

			Map<String, Integer> defTally = tallys.get("defTally");
			key = ref.getCodingSchemeName() + ",Content";
			if (defTally.containsKey(key))
				defTally.put(key, defTally.get(key) + entry.getDefinitionCount());
			else
				defTally.put(key, entry.getDefinitionCount());
			
			Map<String, Integer> propTally = tallys.get("propTally");
			Map<String, Integer> propPropTally = tallys
			.get("propPropTally");
			
			for (Property property : entry.getProperty()) {
				key = property.getLanguage() + ","
						+ property.getPropertyName() + ","
						+ ref.getCodingSchemeName()
						+ ",Content";
				if (propTally.containsKey(key))
					propTally.put(key, propTally.get(key) + 1);
				else
					propTally.put(key, 1);
				propTypes.put(property.getPropertyId(), property.getPropertyName());
				if(property.getPropertyQualifier() != null) {
					for(PropertyQualifier qualifier : property.getPropertyQualifier()) {
						key = property.getPropertyName() 
								+ qualifier.getPropertyQualifierType()
								+ qualifier.getPropertyQualifierName() 
								+ ",Content";
						if (propPropTally.containsKey(key))
							propPropTally.put(key, propPropTally.get(key) + 1);
						else
							propPropTally.put(key, 1);
						counts.put("propPropCount", counts.get("propPropCount") + 1);								
					}
				}
			}
			for(Definition definition : entry.getDefinition()) {
					key = "null,def_reference," + ref.getCodingSchemeName() + ",Content"; 
					if (propTally.containsKey(key))
						propTally.put(key, propTally.get(key) + definition.getSourceCount());
					else
						propTally.put(key, definition.getSourceCount());
					propTypes.put(definition.getPropertyId(), definition.getPropertyName());
			}
			
			for(Comment comment : entry.getComment()) {
				propTypes.put(comment.getPropertyId(), comment.getPropertyName());
			}
			ResolvedConceptReferenceList refList = lbSvc.getNodeGraph(scheme, csvt, null).resolveAsList(
					ConvenienceMethods.createConceptReference(ref
							.getConceptCode(), scheme), true, false, 1, 1,
					new LocalNameList(), null, null, 1024);

			// Analyze the result ...
			if (refList.getResolvedConceptReferenceCount() > 0) {
				ref = (ResolvedConceptReference) refList
						.enumerateResolvedConceptReference().nextElement();
				Map<String, Integer> propLinkTally = tallys
				.get("propLinkTally");
				for (PropertyLink property : ref.getReferencedEntry()
						.getPropertyLink()) {
					key = property.getPropertyLink() + ","
							+ propTypes.get(property.getSourceProperty()) + ","
							+ propTypes.get(property.getTargetProperty())
							+ ",Content";
					if (propLinkTally.containsKey(key))
						propLinkTally.put(key, propLinkTally.get(key) + 1);
					else
						propLinkTally.put(key, 1);
				}
				
				if (ref.getSourceOf() != null) {
					for (Association association : ref.getSourceOf()
							.getAssociation()) {
						Map<String, Integer> relTally = tallys
								.get("relTally");
						Map<String, Integer> relDataTally = tallys
								.get("relDataTally");
						Map<String, Integer> relPropTally = tallys
						.get("relPropTally");
						for (AssociatedConcept refConcept : association
								.getAssociatedConcepts().getAssociatedConcept()) {
							key = refConcept.getCodingSchemeName()+ ","
									+ association.getAssociationName()
									+ ",Content";
							if (relTally.containsKey(key))
								relTally.put(key, relTally.get(key) + 1);
							else
								relTally.put(key, 1);
							counts.put("relCount", counts.get("relCount") + 1);
							if(refConcept.getAssociationQualifiers() != null) {
								for(NameAndValue qualifier : refConcept.getAssociationQualifiers().getNameAndValue()) {
									key = qualifier.getName() + ",Content";
									if (relPropTally.containsKey(key))
										relPropTally.put(key, relPropTally.get(key) + 1);
									else
										relPropTally.put(key, 1);
									counts.put("relPropCount", counts.get("relPropCount") + 1);								
								}
							}
						}
						if(association.getAssociatedData() != null) {
							for (AssociatedData data : association
									.getAssociatedData().getAssociatedData()) {
								key = ref.getCodingSchemeName() + ","
								+ association.getAssociationName() + ","
								+ association.getDirectionalName() + "," + data.getId();
								if (relDataTally.containsKey(key))
									relDataTally.put(key,
											relDataTally.get(key) + 1);
								else
									relDataTally.put(key, 1);
								counts.put("relDataCount", counts.get("relDataCount") + 1);								
							}
						}
					}
				}
			}
		}
		
		  

		for (Map.Entry<String, Map<String, Integer>> names : tallys.entrySet()) {
			for (Map.Entry<String, Integer> values : names.getValue()
					.entrySet()) {
				String value = values.getKey();
				for(String regex: mappings.keySet()) {
				value = value.replaceAll(regex, mappings.get(regex));
				}
				Util
						.displayMessage(new StringBuffer().append(
								names.getKey()).append(SEPARATOR).append(
								value).append(SEPARATOR).append(
								values.getValue()).append(SEPARATOR).toString());
			}
		}
		for (Map.Entry<String, Integer> names : counts.entrySet()) {
			Util.displayMessage(new StringBuffer().append(names.getKey())
					.append(SEPARATOR).append(SEPARATOR).append(names.getValue()).append(SEPARATOR).toString());
		}
		for (Map.Entry<String, String> names : meta.entrySet()) {
			Util.displayMessage(new StringBuffer().append(names.getKey())
					.append(SEPARATOR).append(SEPARATOR).append(names.getValue()).append(SEPARATOR).toString());
		}
		return true;
	}

}
