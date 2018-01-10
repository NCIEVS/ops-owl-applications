package gov.nih.nci.evs.ctcae.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class MedDRA_SOC_parser {

	HashMap<String,String> socLookUp = new HashMap<String,String>();
	
	public HashMap<String,String> getSocLookUp() {
		return socLookUp;
	}

	public MedDRA_SOC_parser(File meddraFile) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		Vector<String> lineByLineData = new Vector<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(meddraFile));
			// Scanner reader = new Scanner(file, "UTF-8");
			Pattern p = Pattern.compile("\\$");
			// String line = reader.nextLine(); // first line is a header
			String line = null;
			while ((line = reader.readLine()) != null) {


				tokenizeString(line, p);

			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void  tokenizeString(String inputString, Pattern delimiter) {


		Vector<String> tokens = new Vector<>();
		Scanner lineReader = new Scanner(inputString);
		lineReader.useDelimiter(delimiter);
		String mCode = lineReader.next();
		String soc = lineReader.next();
		lineReader.close();
		socLookUp.put(soc, mCode);
	}
	
}
