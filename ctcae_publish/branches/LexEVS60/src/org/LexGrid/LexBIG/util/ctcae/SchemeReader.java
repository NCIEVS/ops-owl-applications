/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
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
package org.LexGrid.LexBIG.util.ctcae;

import java.util.Arrays;
import java.util.Comparator;

import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeGraph;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.concepts.Definition;
import org.apache.commons.lang.StringEscapeUtils;


/**
 * Helper class to read concepts from the LexBIG repository for
 * processing.
 */
public class SchemeReader {
	// Used to locate the System Organ Class concept.
	private static String SOC_TEXT = "Adverse Event by System Organ Class";
	
	// Used to maintain state during processing.
	private LexBIGService lbsvc_ = null;
	private CodingSchemeSummary css_ = Util.promptForCodeSystem();
	private ResolvedConceptReference[] categories_ = null;
	private int categoryPosition_ = 0;

	public SchemeReader() {
		super();
		init();
	}

	/**
	 * Initialize the reader by prompting the user for the CTCAE
	 * coding scheme and preparing to iterate over published events.
	 */
	protected void init() {
		String csName = null;
		CodingSchemeVersionOrTag csvt = null;
		if (css_ != null) {
			try {
				// Fetch general information used to reference the
				// CTCAE source in the LexBIG repository.
				lbsvc_ = LexBIGServiceImpl.defaultInstance();
				csName = css_.getCodingSchemeURI();
				csvt = new CodingSchemeVersionOrTag();
				csvt.setVersion(css_.getRepresentsVersion());

				// Prime the list of event categories to feed to the
				// writer on request.  Sort the items by the primary
				// text description.
				CodedNodeGraph cng = lbsvc_.getNodeGraph(csName, csvt, null);
				cng.restrictToAssociations(
					Constructors.createNameAndValueList("subClassOf"), null);
				ConceptReference categoryRoot = getCategoryRoot(csName, csvt);
				ResolvedConceptReference[] temp = cng.resolveAsList(
					categoryRoot,
					false, true, // navigating in reverse
					3, 3, // resolve root nodes and relations to their grandchildren
					null, null, // no restrictions on property name or type
					null, null, // no sort or filter; handled by reader
					-1) // no limit on # items returned
						.getResolvedConceptReference();
				
				// If the search started from a specific node instead of
				// an implied root, the categories will be one level deep.
				// This will be typical when loading from OWL source.
				// Otherwise, the categories are at the first level.
				// This will be typical when loading from the spreadsheet.
				// Try and accommodate both here, but long term OWL is
				// likely to be the only source provided.
				categories_ = temp;
				if (temp.length > 0 && categoryRoot != null) {
					Association[] associations = temp[0].getTargetOf().getAssociation();
					if (associations.length > 0) {
						categories_ = new ResolvedConceptReference[associations[0].getAssociatedConcepts().getAssociatedConceptCount()];
						int i = 0;
						for (AssociatedConcept ac : associations[0].getAssociatedConcepts().getAssociatedConcept()) {
							categories_[i++] = ac;
						}
					}
				}
				sortReferences(categories_);
				
			} catch (LBException e) {
				Util.displayAndLogError(e);
			}
		}
	}
	
	/**
	 * Returns meta-information for the selected code system.
	 */
	public CodingSchemeSummary getCodingSchemeSummary() {
		return css_;
	}
	
	/**
	 * Returns a string representing version of the selected
	 * code system.
	 */
	public String getCodingSchemeVersion() {
		return css_ == null ? "<unavailable>" : css_.getRepresentsVersion();		
	}
	
	/**
	 * Returns the concept representing the root of all system
	 * organ class concepts (each of which represents a CTCAE category),
	 * or null if not found.  In order to be found, the node must
	 * have a presentation that exactly matches the SOC_TEXT defined on
	 * the class.
	 */
	public ConceptReference getCategoryRoot(String csName, CodingSchemeVersionOrTag csvt) throws LBException {
		ConceptReference root = null;
		CodedNodeSet cns = lbsvc_.getNodeSet(csName, csvt, null);
		cns.restrictToMatchingProperties(null,
				new PropertyType[] {PropertyType.PRESENTATION},
				SOC_TEXT, "exactMatch", null);
		
		ResolvedConceptReferenceList nodes = cns.resolveToList(null, null, null, 1);
		if (nodes.getResolvedConceptReferenceCount() > 0)
			root = nodes.getResolvedConceptReference(0);
		return root;
	}
	
	/**
	 * Return the next published category, sorted according to
	 * primary text description.
	 * @return The category; null if none could be retrieved
	 * or if the list of retrieved values has been exhausted.
	 */
	public ResolvedConceptReference getNextCategory() {
		ResolvedConceptReference event = null;
		if (categories_ != null && categoryPosition_ < categories_.length)
			event = categories_[categoryPosition_++];
		return event;
	}
	
	/**
	 * Returns the adverse events for the given category.
	 * @return The array of adverse event concepts associated
	 * with the category.
	 */
	public ResolvedConceptReference[] getAdverseEvents(ResolvedConceptReference category) {
		AssociationList associationList = category.getTargetOf();
		if (associationList != null && associationList.getAssociationCount() > 0) {
			ResolvedConceptReference[] events =
				associationList.getAssociation(0).getAssociatedConcepts().getAssociatedConcept();
			sortReferences(events);
			return events;
		}
		return new ResolvedConceptReference[0];
	}
	
	/**
	 * Returns the grade level (1-5) for the given concept,
	 * or 0 if no grade info is found.
	 */
	protected int getGradeLevel(AssociatedConcept gradeConcept) {
		// Should ideally base this off relationship to a representative
		// grade concept.  For now, we rely on simple
		// lexical match against grade for the code or entity
		// description.
		int level = 0;
		String code = gradeConcept.getCode().toLowerCase();
		EntityDescription desc = gradeConcept.getEntityDescription();
		String text = desc == null ? "" : desc.getContent().toLowerCase();
		if (code.contains("grade_1_") || text.startsWith("grade 1"))
			level = 1;
		else if (code.contains("grade_2_") || text.startsWith("grade 2"))
			level = 2;
		else if (code.contains("grade_3_") || text.startsWith("grade 3"))
			level = 3;
		else if (code.contains("grade_4_") || text.startsWith("grade 4"))
			level = 4;
		else if (code.contains("grade_5_") || text.startsWith("grade 5"))
			level = 5;
		return level;
	}
	
	/**
	 * Returns the grade text for the given concept,
	 * taken from an assigned definition if available
	 * or the entity description otherwise.
	 */
	protected String getGradeText(AssociatedConcept gradeConcept) {
		String text = null;
		try {
			Definition defn = null;
			for (Definition d : gradeConcept.getEntity().getDefinition()) {
				defn = d;
				if (d.isIsPreferred()) break;
			}
			if (defn != null)
				text = defn.getValue().getContent();
		} catch (Exception e) {
		}
		if (text == null)
			text = gradeConcept.getEntityDescription().getContent();
		return StringEscapeUtils.unescapeXml(text);
	}
	
	/**
	 * Close the reader, freeing any allocated resources.
	 */
	public void close() {
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Sorts the given set of references by entity description,
	 * using code as tie-breaker.
	 */
	protected void sortReferences(ResolvedConceptReference[] refs) {
		Arrays.sort(refs, new Comparator<ResolvedConceptReference>() {	
			public int compare(ResolvedConceptReference o1, ResolvedConceptReference o2) {
				EntityDescription d1 = o1.getEntityDescription();
				EntityDescription d2 = o2.getEntityDescription();
				String text1 = d1 != null ? d1.getContent().toLowerCase() : "";
				String text2 = d2 != null ? d2.getContent().toLowerCase() : "";
				
				// Special case to move 'other' items to bottom of AE terms.
				if (text2.contains("other, specify"))
					return -1;
				if (text1.contains("other, specify"))
					return 1;
				
				// Otherwise, alphabetize.
				int i = text1.compareTo(text2);
				return i != 0 ? i
					: o1.getCode().compareTo(o2.getCode());
			}
		});
	}
}