package gov.nih.nci.evs.ctcae.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.nih.nci.evs.ctcae.Messages;

/**
 * Read the Excel file and hold data for processing into objects.
 * 
 * Separate row for each grade.  Grades can be grouped by MedDRA Code
 * 
 * @author safrant
 *
 */

public class CTCAE_parser {
	
	Vector<String> headers = new Vector<String>();
	TreeMap<String, TreeMap<String, String>> rowData = new TreeMap<String, TreeMap<String, String>>();
	TreeMap<String,String> socNameLookup = new TreeMap<String,String>();
	public static Messages messages;
	
	public TreeMap<String, TreeMap<String, String>> getRowData() {
		return rowData;
	}

	public CTCAE_parser(File file) throws Exception {
		this.messages = new Messages("./config/");
		try {
			
			InputStream inputStream = new FileInputStream(file);

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(1);
			Iterator rows = sheet.rowIterator();
			XSSFRow row;

			// take the header row
			parseHeader((XSSFRow) rows.next());

			while (rows.hasNext()) {
				TreeMap<String, String> singleRow = parseRow((XSSFRow) rows.next());
				String rowName = singleRow.get(messages.getString("id"));
				rowData.put(rowName, singleRow);
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
		}

	}

	private TreeMap<String, String> parseRow(XSSFRow row) {
		XSSFCell cell;
		Iterator cells = row.cellIterator();
		TreeMap<String, String> cellValues = new TreeMap<String, String>();
		Iterator<String> headerIt = headers.iterator();

		while (cells.hasNext()) {
			cell = (XSSFCell) cells.next();
			cell.setCellType(CellType.STRING);
			String header=headerIt.next();
			System.out.print(header + " " + cell.getStringCellValue() + " ");
			if (!cell.getStringCellValue().trim().equals("-")) {
				cellValues.put(header, cell.getStringCellValue());
			}

		}
		System.out.println();
		if (cellValues.size() != 10) {
			String id = cellValues.get(messages.getString("id"));
			String name = cellValues.get(messages.getString("NCI_PT"));
			if(name.contains(", CTCAE")){
			name = name.substring(0, name.lastIndexOf(","));
			}
			socNameLookup.put(id, name);
			String debug = "stop here";
		}
		
		return cellValues;
	}

	private void parseHeader(XSSFRow headerRow) {
		// TODO Auto-generated method stub
		
		Iterator cells = headerRow.cellIterator();

		while (cells.hasNext()) {
			XSSFCell cell = (XSSFCell) cells.next();
			// cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			cell.setCellType(CellType.STRING);
			if (cell.getStringCellValue() != null) {
				headers.add(cell.getStringCellValue());
				// System.out.print(cell.getStringCellValue()+" ");
			}

		}


	}
	
	public String socName(String code){
		return socNameLookup.get(code);
	}

}
