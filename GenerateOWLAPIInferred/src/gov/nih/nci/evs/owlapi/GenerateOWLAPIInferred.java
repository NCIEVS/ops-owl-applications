/* Rob Wynne, MSC
 * 
 * Generate the inferred version of NCI Thesaurus
 * using a Protege 5 baseline export.
 * 
 */


package gov.nih.nci.evs.owlapi;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.owlapi.PelletReasonerFactory;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI; 
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException; 
import org.semanticweb.owlapi.model.OWLOntologyManager; 
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class GenerateOWLAPIInferred {

	OWLOntologyManager man;
	OWLOntology ontology;	
	PelletReasoner reasoner;
//	Set<OWLClass> ancestors = new HashSet<>();
	String inputFile;
	String outputFile;
	

	public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
		GenerateOWLAPIInferred infer = new GenerateOWLAPIInferred();
		infer.run(args);
	}

//	public void getSuperClasses(OWLClass c) {
//		Set<OWLClass> s1 = reasoner.getSuperClasses(c, false).getFlattened();
//		for( OWLClass cls : s1 ) {
//			getSuperClasses(cls);
//			if( !ancestors.contains(cls)  && !cls.isTopEntity() ) {
//				ancestors.add(cls);
//			}
//		}
//	}
	
	public String generateOutputFileName(String inputFile){
		String outFile = "";
		int pathEnd = inputFile.lastIndexOf("/");
		String path = inputFile.substring(0, pathEnd);
		String oldFileName = inputFile.substring(pathEnd);
		String newFileName = oldFileName.replace("Thesaurus", "ThesaurusInf");
		outFile = path + newFileName;
		return outFile;
	}

	public void run(String[] args) {

		try{
			if(args.length==1){
				//input the name of the file to be inferred as a URI
				inputFile = args[0];
				URI inputURI = new URI(inputFile);
				//output will be the name with "Inf" inserted
				//example:  input=Thesaurus-13.01d.owl output=ThesaurusInf-13.01d.owl	
				//TODO - clean this up so it doesn't require the dash
//				outputFile = inputFile.replace("Thesaurus-", "ThesaurusInf-");
				outputFile = generateOutputFileName(inputFile);
//				saveInferred();
			}
		}
		catch (URISyntaxException ue){
			System.out.println("Input file must be in the form of a URI.  Example: file:///app/protege/Processing/data/Thesaurus-yy.ddx.owl");
			System.exit(0);
		}				
		
		man = OWLManager.createOWLOntologyManager();
		
		System.out.println("Generating inferred ontology from " + inputFile);
		
		try {
			ontology = man.loadOntologyFromOntologyDocument(IRI.create((new File(new URI(inputFile)))));
		} catch (OWLOntologyCreationException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Finished loading ont...");

		System.out.println("Creating reasoner...");
		reasoner = PelletReasonerFactory.getInstance().createReasoner( ontology );
//		reasoner = new Reasoner.ReasonerFactory().createReasoner(ontology);
		System.out.println("Checking consistency...");
		if( !reasoner.isConsistent() ) {
			//fail
			System.out.println("Ontology inconsistent.");
		}		


		int size = ontology.getClassesInSignature().size();
		int i = 1;

		for (OWLClass c : ontology.getClassesInSignature()) {
			if( i % 10000 == 0 ) {
				System.out.println(i + " of " + size + " classes processed.");
			}
			if( !reasoner.isSatisfiable(c)) {
				System.out.println(c.toString() + " is not satisfiable.");
			}
//			if(c.getIRI().getFragment().equals("C4910")) {
			Set<OWLClass> ancestors = reasoner.getSuperClasses(c, false).getFlattened();
			ancestors.remove(c);
				for(OWLClass cls : ancestors ) {
					for(OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(cls)) {
						OWLClassExpression oce = ax.getSuperClass();
						if ( oce.getObjectPropertiesInSignature().size() > 0 ) {
							if(oce.isAnonymous()) {
								OWLSubClassOfAxiom entailedAxiom = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, oce);
						        if (oce instanceof OWLNaryBooleanClassExpression) {
						            OWLNaryBooleanClassExpression logicalClass = (OWLNaryBooleanClassExpression) oce;
						            for (OWLClassExpression operand : logicalClass.getOperands()) {
						                if (!operand.isAnonymous()) {
//						                	LexEVS code here, should already be anonymous
//						                    OWLClass op = operand.asOWLClass();
//						                    String targetNameSpace = getNameSpace(op);
//						                    AssociationTarget opTarget = CreateUtils.createAssociationTarget(getLocalName(op), targetNameSpace);
//						                    relateAssociationSourceTarget(assocManager.getSubClassOf(), source, opTarget);
						                } else if  (operand instanceof OWLRestriction) {
						                    // Operand defines a restriction placed on the anonymous
						                    // node...
//						                    OWLRestriction op = (OWLRestriction) operand;
						                	entailedAxiom = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, operand);
						                	man.addAxiom(ontology, entailedAxiom);						                	
//						                    processRestriction(op, cls, c);
//						                    System.out.println("Restriction " + op.toString());
						                } else if (operand instanceof OWLNaryBooleanClassExpression || operand instanceof OWLObjectComplementOf){
						                    //Still has some classes to process that are intersections or unions of or complements of.
						                    processInnerNAryExpression(operand, entailedAxiom, c);
						              }						            	
						            	
						            }
						        }
//								OWLSubClassOfAxiom entailedAxiom = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, oce);
								man.addAxiom(ontology, entailedAxiom);
							}
						}
	
					}
					for(OWLEquivalentClassesAxiom ax : ontology.getEquivalentClassesAxioms(cls)) {
						Set<OWLClassExpression> descs = new HashSet<>(ax.getClassExpressions());
						descs.remove(c);
						for(OWLClassExpression oce : descs) {
							if( oce.getObjectPropertiesInSignature().size() > 0 ) {
								if(oce.isAnonymous()) {
									OWLSubClassOfAxiom entailedAxiom = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, oce);									
							        if (oce instanceof OWLNaryBooleanClassExpression) {
							            OWLNaryBooleanClassExpression logicalClass = (OWLNaryBooleanClassExpression) oce;
							            for (OWLClassExpression operand : logicalClass.getOperands()) {
							                if (!operand.isAnonymous()) {
//							                	LexEVS code here, should already be anonymous
//							                    OWLClass op = operand.asOWLClass();
//							                    String targetNameSpace = getNameSpace(op);
//							                    AssociationTarget opTarget = CreateUtils.createAssociationTarget(getLocalName(op), targetNameSpace);
//							                    relateAssociationSourceTarget(assocManager.getSubClassOf(), source, opTarget);
							                } else if  (operand instanceof OWLRestriction) {
							                    // Operand defines a restriction placed on the anonymous
							                    // node...
							                    OWLRestriction op = (OWLRestriction) operand;
							                    entailedAxiom = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, operand);
							                    man.addAxiom(ontology, entailedAxiom);							                    
//							                    System.out.println("Restriction " + op.toString());
							                } else if (operand instanceof OWLNaryBooleanClassExpression || operand instanceof OWLObjectComplementOf){
							                    //Still has some classes to process that are intersections or unions of or complements of.
							                    processInnerNAryExpression(operand, entailedAxiom, c);
							              }						            	
							            	
							            }
							        }									
									man.addAxiom(ontology, entailedAxiom);
								}
							}
						}
					}
				}
//		    }
			i++;
		}
		System.out.println(size + " classes processed.");
		System.out.println("Saving inferred file as " + outputFile);
		try {
			man.saveOntology(ontology, IRI.create((new File(new URI(outputFile)))));
		} catch (OWLOntologyStorageException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		// Terminate the worker threads used by the reasoner. 
		reasoner.dispose(); 
	}
	

    
    private void processInnerNAryExpression(OWLClassExpression operand, OWLSubClassOfAxiom oce, OWLClass c) {
        if (operand instanceof OWLObjectIntersectionOf) {
            for (OWLClassExpression innerOperand : ((OWLNaryBooleanClassExpression) operand).getOperands()) {
                if (innerOperand instanceof OWLRestriction) {
                    OWLRestriction op = (OWLRestriction) innerOperand;
                    OWLSubClassOfAxiom ax = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, innerOperand);
                    man.addAxiom(ontology, ax);
                } else {
                    processInnerNAryExpression(innerOperand, oce, c);
                }
            }
        } else if (operand instanceof OWLObjectUnionOf) {
        	//it's a RG
 //       	System.out.println("Found a UnionOf, it must be a RG so apply " + oce.toString());
 //		    this doesn't work exactly since it's no longer an equivalent class
 //       	man.addAxiom(ontology, oce);
            for (OWLClassExpression innerOperand : ((OWLNaryBooleanClassExpression) operand).getOperands()) {
                if (innerOperand instanceof OWLRestriction) {
                	//this never seems to happen
                    OWLRestriction op = (OWLRestriction) innerOperand;
                    OWLSubClassOfAxiom ax = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, innerOperand);
                    man.addAxiom(ontology, ax);
                } else {
                    processInnerNAryExpression(innerOperand, oce, c);
                }
            }
        }else if (operand instanceof OWLObjectComplementOf){
          OWLClassExpression innerOperand =  ((OWLObjectComplementOf) operand).getOperand();
                if (innerOperand instanceof OWLRestriction) {
                    OWLRestriction op = (OWLRestriction) innerOperand;
                    OWLSubClassOfAxiom ax = man.getOWLDataFactory().getOWLSubClassOfAxiom(c, innerOperand);
                    man.addAxiom(ontology, ax);
                } 
                else if (innerOperand instanceof OWLNaryBooleanClassExpression) {
                    processInnerNAryExpression(innerOperand, oce, c);
                }
                else {
                    System.out.println("Inner " + innerOperand.toString());
                }
            }
    }    


}
