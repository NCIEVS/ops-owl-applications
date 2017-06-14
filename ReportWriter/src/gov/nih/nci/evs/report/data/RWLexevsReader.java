package gov.nih.nci.evs.report.data;


import gov.nih.nci.evs.report.ReportWriterConcept;
import gov.nih.nci.evs.report.ReportWriterLexEVSConcept;
import gov.nih.nci.evs.report.ReportWriterConfiguration;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.AssociatedConceptList;
import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.LexBIG.caCore.interfaces.LexEVSService;
import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.concepts.Entity;
import org.LexGrid.valueSets.ValueSetDefinition;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;


public class RWLexevsReader {
	//Use LexEVS to read in data and create ReportWriterConcept
	private LexEVSService appService;
	ReportWriterConfiguration config;
	int subsetKey = 0;
	HashMap<Integer, String> selectSubset = new HashMap<Integer, String>();

	CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();

	public RWLexevsReader(){
		this.csvt.setTag("PRODUCTION");
	}

	public RWLexevsReader(ReportWriterConfiguration config) {
		this.csvt.setTag("PRODUCTION");
		this.config = config;
		connect(config.getHttpURL());
	}

	private boolean getValueSetOptions() {
		boolean successful = false;
	
		try{
		
		LexEVSValueSetDefinitionServices vsd_service = this.appService
				.getLexEVSValueSetDefinitionServices();

		// List list = vsd_service.listValueSetDefinitionURIs();

		@SuppressWarnings("rawtypes")
		List evsResults = vsd_service.listValueSetDefinitionURIs();
		DefaultMutableTreeNode ret_DefaultMutableTreeNode = null;
		// evsResults = (List) appService.evsSearch(evsQuery);
		if (evsResults != null && evsResults.size() != 0) {
			// Object[] objs = evsResults.toArray();
			ret_DefaultMutableTreeNode = (DefaultMutableTreeNode) evsResults
					.get(0);
			loadSubsetNode(ret_DefaultMutableTreeNode, 0);
			loadSubsetTree(ret_DefaultMutableTreeNode, 1);
			successful = true;
		}}
		catch(Exception e){
			System.out.println("Problem resolving Value Set ");
		}
		return successful;

	}

	private Vector<ReportWriterConcept> getSubConcepts(String code)
			throws LBInvocationException, LBParameterException, LBException {
		ConceptReference cr = ConvenienceMethods.createConceptReference(code,
				this.config.getVocabulary());

		NameAndValue nv1 = new NameAndValue();
		NameAndValueList nvList = new NameAndValueList();
		nv1.setName("subClassOf");
		nvList.addNameAndValue(nv1);

		ResolvedConceptReferenceList matches = this.appService
				.getNodeGraph(this.config.getVocabulary(), this.csvt, null)
				.restrictToAssociations(nvList, null)
				.resolveAsList(cr, false, true, 1, 1, new LocalNameList(),
						null, null, 1024);

		ResolvedConceptReference[] references = matches.getResolvedConceptReference();
		int i = 0;
		Vector<ReportWriterConcept> children = new Vector<ReportWriterConcept>();
		while (i < references.length){
			ResolvedConceptReference ref = matches.getResolvedConceptReference(i);
			Entity ent = ref.getEntity();
			ReportWriterLexEVSConcept rwConcept = new ReportWriterLexEVSConcept(ent, config.getSource());

			children.add(rwConcept);
			i++;
		}

		return children;

	}

	private Vector<ReportWriterConcept> getSuperConcepts(String code)
	{
		
		getParents(code);
		ConceptReference cr = ConvenienceMethods.createConceptReference(code,
				this.config.getVocabulary());

		NameAndValue nv1 = new NameAndValue();
		NameAndValueList nvList = new NameAndValueList();
		nv1.setName("subClassOf");
		nvList.addNameAndValue(nv1);

		try{
			ResolvedConceptReferenceList matches = this.appService
					.getNodeGraph(this.config.getVocabulary(), this.csvt, null)
					.restrictToAssociations(nvList, null)
					.resolveAsList(cr, true, false, 1, 1, new LocalNameList(),
							null, null, 1024);

			//		return matches.getResolvedConceptReference();
			ResolvedConceptReference[] references = matches.getResolvedConceptReference();

			int i = 0;
			Vector<ReportWriterConcept> parents = new Vector<ReportWriterConcept>();
			while (i < references.length){
				ResolvedConceptReference ref = matches.getResolvedConceptReference(i);
				Entity ent = ref.getEntity();
				ReportWriterLexEVSConcept rwConcept = new ReportWriterLexEVSConcept(ent, config.getSource());

				parents.add(rwConcept);
				i++;
			}

			return parents;
		}
		catch(Exception ex){
			System.out.println("Concept " + code);
			ex.printStackTrace();
			return null;
		}
	}
	
	
	private Vector<ReportWriterConcept> getParents(String code){
		try {
			String scheme = "NCI Thesaurus";
			String relation = "subClassOf";
	        // Perform the query ...
			NameAndValue nv1 = new NameAndValue();
			NameAndValueList nvList = new NameAndValueList();
			nv1.setName("Disease_Has_Associated_Anatomic_Site");
			nvList.addNameAndValue(nv1);
			NameAndValue nv2 = new NameAndValue();
			nv2.setName("Gene_Associated_With_Disease");
			nvList.addNameAndValue(nv2);
			NameAndValue nv3 = new NameAndValue();
			nv3.setName("subClassOf");
			nvList.addNameAndValue(nv3);
			NameAndValue nv4 = new NameAndValue();
			nv4.setName("R100");
			nvList.addNameAndValue(nv4);
	 
			ConceptReference cr = ConvenienceMethods.createConceptReference(code,
					scheme);
			ResolvedConceptReferenceList matches = appService
					.getNodeGraph(scheme, csvt, null)
					.restrictToAssociations(nvList, null)
					.resolveAsList(cr, false, true, 1, 1, new LocalNameList(),
							null, null, 1024);
	 
	        // Analyze the result ...
	        if (matches.getResolvedConceptReferenceCount() > 1) {
	            ResolvedConceptReference ref = (ResolvedConceptReference) matches.enumerateResolvedConceptReference()
	                    .nextElement();
	 
	            // Print the associations
	            AssociationList targetof = ref.getTargetOf();
	            Association[] associations = targetof.getAssociation();
	            for (int i = 0; i < associations.length; i++) {
	                Association assoc = associations[i];
	                AssociatedConcept[] acl = assoc.getAssociatedConcepts().getAssociatedConcept();
	                for (int j = 0; j < acl.length; j++) {
	                    AssociatedConcept ac = acl[j];
	                    EntityDescription ed = ac.getEntityDescription();
//	                    Util.displayMessage("\t\t" + ac.getConceptCode() + "/"
//	                            + (ed == null ? "**No Description**" : ed.getContent()));
	                }
	            }
	        }
		} catch (LBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public class EntitySorter implements Comparator<Entity> {
		@Override
		public int compare(Entity obj1, Entity obj2) {
			Entity concept1 = obj1;
			Entity concept2 = obj2;
			int result = 0;

			String str1 = concept1.getEntityDescription().getContent();
			String str2 = concept2.getEntityDescription().getContent();

			result = str1.compareTo(str2);
			return result;
		}
	}

	/**
	 * 
	 * @param node
	 * @param level
	 */
	private void loadSubsetNode(DefaultMutableTreeNode node, int level) {

		Entity dlconcept = (Entity) node.getUserObject();
		String spaces = "";
		for (int i = 0; i < level; i++) {
			spaces = spaces + "   ";
		}
		String conceptName = spaces
				+ dlconcept.getEntityDescription().getContent();
		selectSubset.put(subsetKey, conceptName);
		subsetKey++;

	}

	/**
	 * 
	 * @param root
	 * @param level
	 */
	private void loadSubsetTree(DefaultMutableTreeNode root, int level) {
		int length = root.getChildCount();

		if (length == 0) return;

		for (int i = 0; i < length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root
					.getChildAt(i);
			loadSubsetNode(node, level);
			loadSubsetTree(node, level + 1);
		}
	}


	public Vector<ReportWriterConcept> searchReportWriterConcepts() {
		Vector<ReportWriterConcept> ret_RWConcept_array = new Vector<ReportWriterConcept>();

		//At this point call to the Reader to get the ReportWriterConcept

//		getValueSetOptions();
		LexEVSValueSetDefinitionServices vsd_service = this.appService
				.getLexEVSValueSetDefinitionServices();
		URI valueSet;
		try {
			valueSet = new URI(config.getSubset());

			ValueSetDefinition vsd = vsd_service.getValueSetDefinition(
					valueSet, null);

			if (vsd==null){
				return null;
			}
			

			CodingScheme scheme = this.appService.resolveCodingScheme("NCI_Thesaurus", csvt);
			String version = scheme.getRepresentsVersion();
			AbsoluteCodingSchemeVersionReferenceList finalList = new AbsoluteCodingSchemeVersionReferenceList();
			AbsoluteCodingSchemeVersionReferenceList incsvrl = vsd_service.getCodingSchemesInValueSetDefinition(valueSet);
			for (int i=0; i < incsvrl.getAbsoluteCodingSchemeVersionReferenceCount(); i++){
			   AbsoluteCodingSchemeVersionReference acsvr = incsvrl.getAbsoluteCodingSchemeVersionReference(i);
			   if (acsvr.getCodingSchemeVersion().equals(version)){
				   finalList.addAbsoluteCodingSchemeVersionReference(acsvr);
			   }
			}
			
			
			ResolvedValueSetDefinition rsvd = vsd_service
					.resolveValueSetDefinition(vsd, finalList, null, null);

			ResolvedConceptReferencesIterator rcri = rsvd
					.getResolvedConceptReferenceIterator();
			while (rcri.hasNext()) {
				ResolvedConceptReference rcr = rcri.next();
				Entity entity = rcr.getEntity();
				ReportWriterLexEVSConcept rwc = new ReportWriterLexEVSConcept(entity,
						config.getSource());
				
				String code = entity.getEntityCode();
				//filter out anonymous classes
				if (! code.contains("@A")){
				//get parents or children here?
				if (config.getPrintParent()){
					
					Vector<ReportWriterConcept> parents = getSuperConcepts(code);
					rwc.setParents(parents);
				}
				if (config.getPrintChildren()){
					Vector<ReportWriterConcept> children = getSubConcepts(code);
					rwc.setChildren(children);
				}}
				ret_RWConcept_array.add(rwc);
			}

		} catch (LBException e) {
			
			System.out.println("Unexpected error.  Please check value set name and LexEVS address. If correct, check that the LexEVS service is operating.");
		} catch (URISyntaxException e1) {
			System.out.println("Unable to resolve value set name.  Please check the name");
		} catch (Exception e){
			System.out.println("Unable to open value set.  Please check the name");
		}

		return ret_RWConcept_array;
	}


	/**
	 * 
	 * @param httpURL
	 * @return
	 */
	private boolean connect(String httpURL) {
		// System.out.println("URL" + httpURL);

		try {
			this.appService = (LexEVSService) ApplicationServiceProvider
					.getApplicationServiceFromUrl(httpURL, "EvsServiceInfo");

			if (this.appService == null) {
				System.out.println("Unable to connect to " + httpURL
						+ " -- please check URL and try again.");
				return false;
			}
			System.out.println("ApplicationService instance created.");
			return true;
		} catch (Exception e) {
			System.out.println("Unable to connect to " + httpURL
					+ " -- please check URL and try again.");
			e.printStackTrace();
			return false;
		}
	}


}
