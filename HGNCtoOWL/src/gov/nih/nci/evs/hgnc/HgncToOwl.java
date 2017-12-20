package gov.nih.nci.evs.hgnc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class HgncToOwl {
	private static Properties delimiters = new Properties();
	private static Properties columns = new Properties();
	private static Properties specialistDatabases = new Properties();
	private static Properties mainConfig = new Properties();
	private static URI saveURI = null;
	private File file = null;
	private static Properties sysProp = System.getProperties();

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String configFileName = null;

		HgncToOwl hto = new HgncToOwl();

		boolean propsValid=false;
		if (args.length>1){
		    propsValid=hto.configure(args);
		} else if (args.length ==1){
		    propsValid=hto.configure(args[0]);
		} else {
		    System.out.println("No parameters entered");
		    printHelp();
		}

		if (propsValid) {
			hto.processHugo();
		} else {
		    System.out.println("Error in arguments");
		    printHelp();
		}
	}

	private HgncToOwl() {

	}
	
	private boolean configure(String[] args){
	    try{
	        boolean propertiesValid = true;
	        String configFile=null;
	        String source=null;
	        String target=null;
//            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    String option = args[i];
                if (option.equalsIgnoreCase("-c")){
                        configFile = args[++i];
                    } else if (option.equalsIgnoreCase("-s")){
                        source = args[++i];
                    } else if (option.equalsIgnoreCase("-t")){
                        target = args[++i];
                    } else {
                        System.out.println("Invalid input parameters");
                        for (int j=0; j<args.length;j++){
                            System.out.println(args[j]);
                        }
                        printHelp();
                    }
                }

//            }
            if (!configFileAddressValid(configFile)){
                System.out.println("configFile address not valid");
                printHelp();
            }
	        if (source !=null){
	            file = readCsvFile(source);
	        } else {
	            String inputFile = mainConfig.getProperty("source");
	            file = readCsvFile(inputFile);
	        }
	        if (target !=null){
	            saveURI = new URI(target);
	        } else {
	            String outputFile = mainConfig.getProperty("target");
	            saveURI = new URI(outputFile);
	        }
	        propertiesValid = configureData();
	        return propertiesValid;
	    } catch (Exception e){
	        e.printStackTrace();
	        return false;
	    }
	}

	private boolean configure(String mainPropertyFile) {

		try {
			boolean propertiesValid = true;
            if (!configFileAddressValid(mainPropertyFile)){
                System.out.println("configFile address not valid");
                System.exit(1);
            }
			String inputFile = mainConfig.getProperty("source");
			file = readCsvFile(inputFile);

			String outputFile = mainConfig.getProperty("target");
			saveURI = new URI(outputFile);

			propertiesValid = configureData();
			
//			String columnsFileName = mainConfig.getProperty("columns");
//			String columnsPath = new URI(columnsFileName).getPath();
//			columns = readProperties(columnsPath);
//
//			String delimitersFileName = mainConfig.getProperty("delimiters");
//			String delimitersPath = new URI(delimitersFileName).getPath();
//			delimiters = readProperties(delimitersPath);
//
//			String specialistFileName = mainConfig.getProperty("specialist");
//			String specialistPath = new URI(specialistFileName).getPath();
//			specialistDatabases = readProperties(specialistPath);
			return propertiesValid;
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}
	
	private boolean configFileAddressValid(String configFile){
	    boolean isValid=true;
        if (configFile == null) {
            final String filename = sysProp.getProperty("HgncToOwl.properties");
            configFile = filename;
        }

        
        // If config file name still null
        if (configFile == null) {
            System.out.println("Must provide location of config file");
            isValid = false;
        }
	    
        mainConfig = readProperties(configFile);
	    return isValid;
	}
	
	private boolean configureData(){
	    boolean isValid=true;
        try {
        String columnsFileName = mainConfig.getProperty("columns");
        String columnsPath;
        columnsPath = new URI(columnsFileName).getPath();

        columns = readProperties(columnsPath);

        String delimitersFileName = mainConfig.getProperty("delimiters");
        String delimitersPath = new URI(delimitersFileName).getPath();
        delimiters = readProperties(delimitersPath);

        String specialistFileName = mainConfig.getProperty("specialist");
        String specialistPath = new URI(specialistFileName).getPath();
        specialistDatabases = readProperties(specialistPath);
	}         catch (URISyntaxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        isValid = false;
    }
    return isValid;    
	}


	public void processHugo() {
		try {
			HgncCsvParser cvsParser = new HgncCsvParser(file);
			HgncOntology hugoOntology = new HgncOntology(cvsParser);
			HgncOwlWriter owlWriter = new HgncOwlWriter(hugoOntology, saveURI);
		} catch (Exception e) {
			System.out.println("Error reading in CSV file.  Program ending");
			e.printStackTrace();
			System.exit(0);
		}
	}



	public static File readCsvFile(String fileLocation) {
		File file = checkValidURI(fileLocation);
		if (file == null) {
			file = checkValidPath(fileLocation);
		}
		if (file == null) {
			System.out.println("Not a valid file location");
			System.exit(0);
		}
		return file;
	}

	public static File checkValidURI(String fileLoc) {
		try {
			URI uri = new URI(fileLoc);
			return new File(uri);
		} catch (Exception e) {
			return null;
		}
	}

	public static File checkValidPath(String fileLoc) {
		try {
			File file = new File(fileLoc);
			return file;
		} catch (Exception e) {
			return null;
		}
	}

	public static Properties readProperties(String configFile) {
		Properties propFile = new Properties();
		try {
			FileInputStream instream = new FileInputStream(configFile);
			propFile.load(instream);
			instream.close();
		} catch (FileNotFoundException e) {
			System.out.println("No " + configFile + " found");
			printHelp();
		} catch (IOException e) {
			System.out.println("Problem reading " + configFile);
		} catch (Exception e) {
			System.out.println("Unexpected error reading " + configFile);
		}
		return propFile;
	}

	public static Properties getDelimitedColumns() {
		return columns;
	}

	public static Properties getDelimiters() {
		return delimiters;
	}

	public static Properties getSpecialistDatabases() {
		return specialistDatabases;
	}

	public void setColumns(Properties in_Columns) {
		columns = in_Columns;
	}

	public void setDelimiters(Properties in_Delimiters) {
		delimiters = in_Delimiters;
	}

	public void setSpecialistDatabases(Properties in_Specialist) {
		specialistDatabases = in_Specialist;
	}

	public static String underscoredString(String input) {

		return input.trim().replace(" ", "_").replace("(", "_")
		        .replace(")", "_").replace(",", "").replace(":", "_");
	}

	public static String processToURI(String path) {
		String step1 = path.trim().replace("\\", "/").replace(" ", "%20");
		String step2 = "file:///" + step1;
		return step2;
	}
	
    /**
     * Prints the help.
     */
    public static void printHelp() {
        
    //TODO adjust for HGNC
        System.out.println("");
        System.out.println("Usage: HgncToOwl [OPTIONS] ");
        System.out.println("  If letting a config file handle everything, just pass in the path and name of the config");
        System.out.println("      Example:  HgncToOWL ./config/HgncToOwl.properties");
        System.out.println(" ");
        System.out.println("  If passing in file names, you need to tag the config file with -c");
        System.out.println("      Example: HgncToOwl -c ./config/HgncToOwl.properties -i file:///app/protege/data/hgnc_complete.txt");
        System.out.println(" ");        
        System.out.println("  -c [relative path] The location of the config file");
        System.out.println("  -s [full path to hgnc raw text] The path to the raw text downloaded from Hugo");
        System.out.println("  -t [full path to output file]  The path and name to store the owl file");
        System.out.println("");
        System.exit(1);
    }
}