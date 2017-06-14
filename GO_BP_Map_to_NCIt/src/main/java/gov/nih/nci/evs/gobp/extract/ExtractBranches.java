/*
 * Rob Wynne, MSC
 *
 */

package gov.nih.nci.evs.gobp.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/** The reasoner. */

/**
 * 
 * echo The scrubbed asserted filename to be processed is $1
 *
 * sh ExtractBranches.sh -i $1 -r C17828 perl formatBranch.pl -k $filePath
 * BranchList.txt $branchDir"Biological_Process.owl" mv BranchList.txt
 * $branchDir"Biological_Process.txt"
 **/

public class ExtractBranches {
	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.extract.ExtractBranches.class);

	private static Properties sysProp = System.getProperties();
	OWLOntologyManager manager;
	OWLOntology ontology;
	OWLDataFactory factory;
	String ontologyNamespace = null;
	IRI physicalIRI = null;
	IRI ontologyIRI;
//	String outputFile;
	String configFile = null;
	String rootNode = null;
	boolean oneByOne = false;
	Vector<String> branches = new Vector<String>();
	HashMap<IRI, Integer> uris = new HashMap<IRI, Integer>();
	private OWLReasoner reasoner;
	private final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	private PrintWriter pw;
	String dir = System.getProperty("user.dir").replace("\\", "/").replace(" ", "%20");
	
	public enum OSType {
		Windows, MacOS, Linux, Other
	};

	public OSType detectedOS = null;
	OSType os = getOperatingSystemType();	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExtractBranches extract = new ExtractBranches();
		long start = System.currentTimeMillis();		
		extract.configure(args);
		extract.run();
		System.out.println("Finished extraction in "
		        + (System.currentTimeMillis() - start) / 1000 + " seconds.");		
	}
	
	public void configure(String[] args) {
		configFile = sysProp.getProperty("CONFIG_FILE");
		logger.info("Reading config file at " + configFile);
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(configFile));
				
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					String option = args[i];
					if (option.equalsIgnoreCase("--help")) {
						printHelp();
					}
					else if( option.equalsIgnoreCase("-N")
							|| option.equalsIgnoreCase("--namespace")) {
						ontologyNamespace = args[++i];
					}
					else if (option.equalsIgnoreCase("-R")
					        || option.equalsIgnoreCase("--root")) {
						rootNode = args[++i];
					}
					else if (option.equalsIgnoreCase("-I")
					        || option.equalsIgnoreCase("--input")) {
						physicalIRI = buildIRI(args[++i]);
					}
					else {
						System.err.println("Invalid option: " + option);
						printHelp();
					}
				}
			}
			else {
				System.err.println("No arguments provided. Will read all configurations from extractbranches.properties.");
				logger.info("No arguments provided. Will read all configurations from extractbranches.properties.");
			}
			
			if( ontologyNamespace == null ) {
				ontologyNamespace = props.getProperty("namespace");
				if( ontologyNamespace == null ) {
					System.err.println("ontologyNamespace not provided.  Program will abort.");
					logger.error("No ontology namespace provided.  Program end");
					printHelp();
				}
			}			
			if( rootNode == null ) {
				rootNode = props.getProperty("rootNode");
				if( rootNode == null ) {
					System.err.println("rootNode not provided.  Program will abort.");
					logger.error("No root node provided.  Program end");
					printHelp();
				}
			}
			if( physicalIRI == null ) {
				physicalIRI = buildIRI(props.getProperty("fileURI"));
				if( physicalIRI == null ) {
					System.err.println("Input URI not provided.  Program will abort.");
					logger.error("No input URI provided.  Program end");
					printHelp();
				}
			}
			ontologyIRI = IRI.create(ontologyNamespace);
			branches.add(rootNode.trim());
            configPrintWriter("BranchList.txt");			

		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.print("Unable to find file extractbranches.properties.  Program will abort.");
			logger.error("Unable to find config file.  Program end");
			System.exit(1);
		}
		try {
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntology(physicalIRI);
		} catch (OWLException e) {
			e.printStackTrace();
			System.out
					.print("Difficulty loading OWL file.  Check filename is correct.");
			logger.error("Unable to load OWL file.  Check filename is correct.  Program end");
			System.exit(1);
		}
		System.out.println("Finished configuring.");
		printConfig();
	}
	
	public IRI buildIRI(String filename) {
		IRI val = null;
		filename = filename.replace("\\", "/").replace(" ", "%20");
		if( filename.startsWith("file:///") ) {
			val = IRI.create(filename);
		}
		else {
			if( os.equals(OSType.Linux) || os.equals(OSType.MacOS) ) {
				if( filename.startsWith("/")) {
					val = IRI.create("file://" + filename); //dir begins with a / and is absolute
				}
				else {
					val = IRI.create("file:///" + dir + "/" + filename);
				}
			}
			else if( os.equals(OSType.Windows) ) {
				if( filename.contains(":")) // assume path is absolute
					val = IRI.create("file:///" + filename);
				else //assume path is relative
					val = IRI.create("file:///" + dir + "/" + filename);
			}
			else {
				System.err.println("Undetected OS.  Program will abort.");
				printHelp();
			}
		}
		return val;
	}

    private void configPrintWriter(String outputfile) {
            try {
                    File file = new File(outputfile);
                    pw = new PrintWriter(file);
            } catch (Exception e) {
                    System.out.println("Error in PrintWriter");
			logger.error("Error in PrintWriter " + e.getStackTrace());
            }
    }
    
	public void printConfig() {
		System.err.println("namespace: " + ontologyNamespace);
		System.err.println("rootNode: " + rootNode);
		System.err.println("fileURI: " + physicalIRI.toString());
//		System.err.println("output OWL " +  );
	}
	
	public void printHelp() {
		System.out.println("");
		System.out
		        .println("Usage: ExtractBranches [OPTIONS]");
		System.out.println(" ");
		System.out.println("  -I, --input\t\tURI to input OWL");
		System.out
				.println("  -N, --namespace\tThe namespace of the OWL");		
		System.out
		        .println("  -R, --root\t\tRoot concept of extraction (class name/IRI fragment)");
		System.out.println();
		System.exit(1);		
	}
	
	public OSType getOperatingSystemType() {
		if (detectedOS == null) {
		  String tOS = System.getProperty("os.name", "generic").toLowerCase();
		  if ((tOS.indexOf("mac") >= 0) || (tOS.indexOf("darwin") >= 0)) {
		    detectedOS = OSType.MacOS;
		  } else if (tOS.indexOf("win") >= 0) {
		    detectedOS = OSType.Windows;
		  } else if (tOS.indexOf("nux") >= 0) {
		    detectedOS = OSType.Linux;
		  } else {
		    detectedOS = OSType.Other;
		    System.err.println("Unsupported OS detected: " + tOS);
		  }
		}
		return detectedOS;
	}	

	public void run() {
		if (branches.size() > 0) {
			for (String branch : branches) {
				IRI classIRI = createIRI(branch);
				uris.put(classIRI, 1);
                pw.println(classIRI.getFragment());
			}
			extractBranch(uris);
		}
		pw.close();
	}

	public IRI createIRI(String className) {
		return IRI.create(ontologyNamespace + "#" + className);
	}

	public void extractBranch(HashMap<IRI, Integer> classURIs) {
		startToldReasoner();
		for (OWLClass cls : ontology.getClassesInSignature()) {
			if(cls.getIRI().getFragment().equals("GO_0008150")){
				@SuppressWarnings("unused")
				int debug = 0;
			}
			if (classURIs.containsKey(cls.getIRI())) {
				if (!oneByOne) {
					Vector<OWLClass> descendants = getSubClasses(cls, false);
					if (descendants != null) {
						for (OWLClass odesc : descendants) {
							// Add descendants to new ontology
//							addClassToExtraction(odesc);
						}
					} else {
						// do nothing
					}
//					addClassToExtraction(cls);
					break;
				} else {
					// System.out.println("Trying to add " +
					// cls.getURI().toString() );
//					addClassToExtraction(cls);
				}
			}
		}
	
	}

	public void stopReasoner() {
		this.reasoner.dispose();
	}

	public void startToldReasoner() {
		System.out.println("Starting TOLD reasoner.");

		this.reasoner = reasonerFactory.createReasoner(this.ontology);
		reasoner.getUnsatisfiableClasses();
		System.out.println("Finished computing class hierarchy.");
	}

	private Vector<OWLClass> getSubClasses(OWLClass cls, boolean directOnly) {
		Vector<OWLClass> vChildren = new Vector<OWLClass>();
		if (reasoner != null) {
			for (OWLClass subCls : reasoner.getSubClasses(cls, directOnly)
					.getFlattened()) {
				if (!vChildren.contains(subCls)) {
					vChildren.add(subCls);
					pw.println(subCls.getIRI().getFragment());
				}
			}
		} else {

			Set<OWLClassExpression> ods = cls.getSubClasses(ontology);
			OWLClassExpression[] children = ods
					.toArray(new OWLClassExpression[ods.size()]);
			if (children.length == 0) return null;
			for (OWLClassExpression child : children) {
				vChildren.add(child.asOWLClass());
			}
			if (!directOnly) {
				for (int i = 0; i < vChildren.size(); i++) {
					Vector<OWLClass> w = getSubClasses(vChildren.elementAt(i)
							.asOWLClass(), false);
					if (w != null) {
						for (int j = 0; j < w.size(); j++) {
							if (w.elementAt(j) != null
									&& !vChildren.contains(w.elementAt(j))) {
								vChildren.add(w.elementAt(j));
							}
						}
					}
				}
			}
		}
		return vChildren;
	}

	public void setBranches(String filename) {
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
					branches.add(line.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
