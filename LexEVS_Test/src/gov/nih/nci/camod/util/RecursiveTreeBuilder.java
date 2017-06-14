/*******************************************************************************
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
 *  		http://www.eclipse.org/legal/epl-v10.html
 * 
 *  		
 *******************************************************************************/
package gov.nih.nci.camod.util;

//import gov.nih.nci.evs.domain.DescLogicConcept;
//import gov.nih.nci.evs.query.EVSQueryImpl;
//import gov.nih.nci.system.applicationservice.EVSApplicationService;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.LexGrid.LexBIG.DataModel.Collections.AssociatedConceptList;
import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeGraph;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;

/**
 * Utility class to build a Tree from a root node, following to the closure of
 * the graph. This is an alternative to using EVSQuery 'getTree' methods and
 * specifying a '-1' depth.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 * 
 */
public class RecursiveTreeBuilder {

	// private static Logger log = Logger.getLogger(RecursiveTreeBuilder.class
	// .getName());
	private final LexEVSService evsSvc;

	public RecursiveTreeBuilder(LexEVSService evsSvc) {
		this.evsSvc = evsSvc;
	}

	/**
	 * Generates the entire Tree from a given root node. Levels are added by
	 * recursive calls to EVS, so return limits and memory issues may be avoided
	 * when building very large trees.
	 * 
	 * @param vocabularyName
	 * @param rootCode
	 * @param direction
	 * @param isaFlag
	 * @param attributes
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	public DefaultMutableTreeNode getTree(String vocabularyName,
	        String rootCode, boolean direction, boolean isaFlag,
	        int attributes, Vector roles) throws Exception {

		// log.info("Started building tree for root node: " + rootCode + ".");
		DefaultMutableTreeNode returnTree = buildTree(vocabularyName, rootCode,
		        direction, isaFlag, attributes, roles);
		// log.info("Finished building tree for root node: " + rootCode + ".");

		return returnTree;
	}

	/**
	 * Recursively builds the Tree, adding one level at a time.
	 * 
	 * @param vocabularyName
	 * @param rootCode
	 * @param direction
	 * @param isaFlag
	 * @param attributes
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	protected DefaultMutableTreeNode buildTree(String vocabularyName,
	        String rootCode, boolean direction, boolean isaFlag,
	        int attributes, Vector roles) throws Exception {
		DefaultMutableTreeNode node = resolveOneLevel(vocabularyName, rootCode,
		        direction, isaFlag, attributes, roles);
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) node
		        .getRoot()).clone();
		rootNode.removeAllChildren();

		ResolvedConceptReference rcr = (ResolvedConceptReference) node
		        .getUserObject();

		// log.debug("Processing " + node.getChildCount() + " children of "
		// + rootCode);
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node
			        .getChildAt(i);
			ResolvedConceptReference c = (ResolvedConceptReference) childNode
			        .getUserObject();
			rootNode.add(buildTree(vocabularyName, c.getCode(), direction,
			        isaFlag, attributes, roles));
		}

		return rootNode;
	}

	/**
	 * Resolves the given code one level deep.
	 * 
	 * @param vocabularyName
	 * @param rootCode
	 * @param direction
	 * @param isaFlag
	 * @param attributes
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	protected DefaultMutableTreeNode resolveOneLevel(String vocabularyName,
	        String rootCode, boolean direction, boolean isaFlag,
	        int attributes, Vector roles) throws Exception {
		// log.debug("Resolving one level of code: " + rootCode);
		// EVSQueryImpl query = new EVSQueryImpl();
		// query.getTree(vocabularyName, rootCode, direction, isaFlag,
		// attributes,
		// 1, roles);
		// List results = evsSvc.evsSearch(query);

		// DefaultMutableTreeNode returnNode = (DefaultMutableTreeNode) results
		// .get(0);
		// return returnNode;

		ConvenienceMethods.createProductionTag();
		CodedNodeGraph cng = evsSvc.getNodeGraph(vocabularyName,
		        ConvenienceMethods.createProductionTag(), "relations");
		ConceptReference cref = ConvenienceMethods.createConceptReference(
		        rootCode, vocabularyName);

		ResolvedConceptReference[] rcr = cng.resolveAsList(cref, direction,
		        isaFlag, 1, 1, null, null, null, -1)
		        .getResolvedConceptReference();

		// ResolvedConceptReference[] rcr_false = cng.resolveAsList(cref, false,
		// isaFlag, 1, 1, null, null, null, -1)
		// .getResolvedConceptReference();

		DefaultMutableTreeNode returnNode = new DefaultMutableTreeNode(rcr[0]);

		return returnNode;
	}

	protected void printLevelNext(LexBIGServiceConvenienceMethods lbscm,
	        String scheme, CodingSchemeVersionOrTag csvt, String hierarchyID,
	        String code, int maxDistance, int currentDistance)
	        throws LBException {
		if (maxDistance < 0 || currentDistance < maxDistance) {
			StringBuffer indent = new StringBuffer();
			for (int i = 0; i <= currentDistance; i++)
				indent.append("    ");

			AssociationList associations = lbscm.getHierarchyLevelNext(scheme,
			        csvt, hierarchyID, code, false, null);
			for (int i = 0; i < associations.getAssociationCount(); i++) {
				Association assoc = associations.getAssociation(i);
				AssociatedConceptList concepts = assoc.getAssociatedConcepts();
				for (int j = 0; j < concepts.getAssociatedConceptCount(); j++) {
					AssociatedConcept concept = concepts
					        .getAssociatedConcept(j);
					String nextCode = concept.getConceptCode();
					String nextDesc = concept.getEntityDescription()
					        .getContent();
					// log.debug(indent + assoc.getDirectionalName() + "->"
					// + nextCode + ":" + nextDesc);
					printLevelNext(lbscm, scheme, csvt, hierarchyID, nextCode,
					        maxDistance, currentDistance + 1);
				}
			}
		}
	}
}
