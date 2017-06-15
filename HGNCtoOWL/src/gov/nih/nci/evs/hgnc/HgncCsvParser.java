package gov.nih.nci.evs.hgnc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class HgncCsvParser {

	// private static Properties columns = new Properties();
	// private static Properties delimiters = new Properties();
	// private static Properties specialistDatabases = new Properties();

	public HgncCsvParser(File file) throws Exception {
		try{
		readFile(file);
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	private Vector<String> header = null;
	private final Vector<Vector<String>> lineByLineData = new Vector<Vector<String>>();

	private void readFile(File file) throws Exception {
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
//		Scanner reader = new Scanner(file, "UTF-8");
		Pattern p = Pattern.compile("\t");
//		String line = reader.nextLine(); // first line is a header
		String line = reader.readLine();
		header = parseHeader(line, p);
		Vector<String> data = new Vector<String>();
//		while (reader.hasNextLine()) {
//			line = reader.nextLine();
//			data.add(line);
//			lineByLineData.add(tokenizeString(line, p));
//		}
		
		while ((line=reader.readLine())!=null) {
//			line = reader.nextLine();
			data.add(line);
			lineByLineData.add(tokenizeString(line, p));
		}
		if (lineByLineData.size() < 1) {
			reader.close();
			throw new FileNotFoundException();
		}
		boolean isValidData = validateAgainstHeader();
		if (!isValidData) {
			reader.close();
			throw new Exception();
		}
		reader.close();
		}
		catch (Exception e){
			
			System.out.println("Error in readFile");
			throw e;
		}
	}

	public static Vector<String> tokenizeString(String inputString,
	        Pattern delimiter) {
//		if(inputString.contains("HGNC:5480")){
//			System.out.println("Debug statement here");
//		}
		Vector<String> tokens = new Vector<String>();
		Scanner lineReader = new Scanner(inputString);
		lineReader.useDelimiter(delimiter);
		while (lineReader.hasNext()) {
			String value = lineReader.next();
			String valueNoQuote = value.replace("\"", "").trim();
			tokens.add(valueNoQuote);
		}
		lineReader.close();
		return tokens;
	}
	
	public static Vector<String> parseHeader(String inputString, Pattern delimiter){
		Vector<String> tokens = tokenizeString(inputString, delimiter);
//		Vector<String> headerMap = new Vector<String>();
		//Map the file headers to presentation headers that we have structured.
		
//		return headerMap;
		return tokens;
	}

	public Vector<String> getHeader() {
		return header;
	}

	public Vector<Vector<String>> getData() {
		return lineByLineData;
	}

	/**
	 * Checks each line to make sure it has the same number of tokens as the
	 * header If not, then the tokenizing has gone wrong.
	 * 
	 * @return
	 */
	private boolean validateAgainstHeader() {
		for (Vector<String> line : lineByLineData) {
			if (line.size() > header.size()) {
				System.out.println("Invalid data at " + line.elementAt(0)
				        + ". More data than there are columns");
				return false;
			}
			if (line.size() < 1) {
				System.out.println("Invalid data.  Empty line");
				return false;
			}
			if (line.size() < (header.size() / 2)) {
				System.out.println("Too few data fields at "
				        + line.elementAt(0) + ".  Data is " + line.size());
				return false;
			}
			if (line.size() != header.size()) {
				System.out.println("Warning: Data is " + line.size()
				        + " fields long.  Header is " + header.size()
				        + " long at " + line.elementAt(0));
			}
			if(header.size() - line.size()>4 ){
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("ERROR: There is something seriously wrong with " + line.elementAt(0));
			}
		
//			System.out.println("Read " + line.elementAt(0));
			if(line.elementAt(0).equals("HGNC:5480")){
				System.out.println("Debug statement here");
			}
					
		}
		
		return true;
	}
	
	

}
