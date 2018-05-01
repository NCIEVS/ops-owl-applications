package gov.nih.nci.evs.ctcae.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CTCAE_Hierarchy_Parser {
	
	String socParent="";
	String mainParent="";
	TreeMap<String, String> hierarchy = new TreeMap<String, String>();

	public CTCAE_Hierarchy_Parser(File file) throws IOException{
	try {
		InputStream inputStream = new FileInputStream(file);

		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		XSSFSheet sheet = workbook.getSheetAt(2);
		Iterator rows = sheet.rowIterator();
		XSSFRow row;



		while (rows.hasNext()) {
			TreeMap<String, String> singleRow = parseRow((XSSFRow) rows.next());
			String rowName = singleRow.get("NCIt Code");
//			rowData.put(rowName, singleRow);
		}

		inputStream.close();
		workbook.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw e;
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw e;
	}}
	
	private TreeMap<String, String> parseRow(XSSFRow row) {
		XSSFCell cell;
		Iterator cells = row.cellIterator();
		
		short colNumber = row.getLastCellNum();
//		Iterator<String> headerIt = headers.iterator();
		cell = (XSSFCell) cells.next();
		String code = "";
		
		switch(colNumber){
		case 1:
			//broad category - maybe discard
			break;
		case 2:
			//MedDRA SOC
			//Child of Adverse_Event_by_System_Organ_Class
			code = parseCode(cell.getStringCellValue());
			hierarchy.put(code, "C143163");
			socParent=code;
			break;
		case 3:
			//Main concept
			code = parseCode(cell.getStringCellValue());
			hierarchy.put(code, socParent);
			mainParent=code;
			break;
		case 4:
			//Grade
			code = parseCode(cell.getStringCellValue());
			hierarchy.put(code, mainParent);
			break;
		}

//		while (cells.hasNext()) {
//			cell = (XSSFCell) cells.next();
//			cell.setCellType(CellType.STRING);
////			String header=headerIt.next();
//			System.out.print(cell.getStringCellValue() + " ");
//			
//			if (!cell.getStringCellValue().trim().equals("-")) {
////				cellValues.put(header, cell.getStringCellValue());
//			}
//
//		}
//		System.out.println();
//		if (cellValues.size() != 10) {
//			String debug = "stop here";
//		}
		
		return hierarchy;
	}
	
	private String parseCode(String cellValue){
		String code = cellValue.substring(cellValue.lastIndexOf("(")+1, cellValue.lastIndexOf(")"));
		return code;
	}

	public TreeMap<String, String> getHierarchy() {
		// TODO Auto-generated method stub
		return hierarchy;
	}
}
