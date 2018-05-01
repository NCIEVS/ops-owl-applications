package gov.nih.nci.evs.ctcae.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class MedDRA_LLT_parser {

	HashMap<String,Vector<MedDRA_LLT>> lltLookUp = new HashMap<String,Vector<MedDRA_LLT>>();
	HashMap<String, Vector<MedDRA_LLT>> medLookUp = new HashMap<String,Vector<MedDRA_LLT>>();
	
	public HashMap<String, Vector<MedDRA_LLT>> getLltLookUp() {
		return lltLookUp;
	}
	public HashMap<String, Vector<MedDRA_LLT>> getMedLookUp(){
		return medLookUp;
	}

	public MedDRA_LLT_parser(File meddraFile) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		Vector<MedDRA_LLT> lineByLineData = new Vector<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(meddraFile));
			// Scanner reader = new Scanner(file, "UTF-8");
			Pattern p = Pattern.compile("\\$");
			// String line = reader.nextLine(); // first line is a header
			String line = null;
			while ((line = reader.readLine()) != null) {
				// line = reader.nextLine();
				MedDRA_LLT item = tokenizeString(line, p);
				lineByLineData.add(item);
				if (lltLookUp.get(item.getLltCode()) != null){
					//Add to existing vector
					Vector<MedDRA_LLT> meds =  lltLookUp.get(item.getLltCode());
					meds.addElement(item);
					lltLookUp.put(item.getLltCode(),meds);
				} else {
					//create new Vector
					Vector<MedDRA_LLT> meds = new Vector<MedDRA_LLT>();
					meds.add(item);
					lltLookUp.put(item.getLltCode(), meds);
				}
				if (medLookUp.get(item.getMeddraCode())!= null){
					Vector<MedDRA_LLT> meds = medLookUp.get(item.getMeddraCode());
					meds.addElement(item);
					medLookUp.put(item.getMeddraCode(), meds);
				} else {
					Vector<MedDRA_LLT> meds = new Vector<MedDRA_LLT>();
					meds.add(item);
					medLookUp.put(item.getMeddraCode(), meds);
				}
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
	
	public MedDRA_LLT tokenizeString(String inputString, Pattern delimiter) {


		Vector<String> tokens = new Vector<>();
		Scanner lineReader = new Scanner(inputString);
		lineReader.useDelimiter(delimiter);
		String mCode = lineReader.next();
		String soc = lineReader.next();
		String lCode = lineReader.next();
		lineReader.close();
		return new MedDRA_LLT(mCode, soc, lCode);
//		while (lineReader.hasNext()) {
//			tokens.add(lineReader.next());
//		}

		
	}


}
