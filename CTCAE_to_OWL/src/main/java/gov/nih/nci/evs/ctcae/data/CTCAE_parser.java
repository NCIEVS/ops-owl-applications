package gov.nih.nci.evs.ctcae.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Read the Excel file and hold data for processing into objects.
 * 
 * @author safrant
 *
 */

public class CTCAE_parser {

	Vector<String> headers = new Vector<String>();
	HashMap<String, HashMap<String, String>> rowData = new HashMap<String, HashMap<String, String>>();

	public HashMap<String, HashMap<String, String>> getRowData() {
		return rowData;
	}

	public CTCAE_parser(File file) throws Exception {

		try {
			InputStream inputStream = new FileInputStream(file);

			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(3);
			Iterator rows = sheet.rowIterator();
			XSSFRow row;

			// take the header row
			parseHeader((XSSFRow) rows.next());

			while (rows.hasNext()) {
				HashMap<String, String> singleRow = parseRow((XSSFRow) rows.next());
				String rowName = singleRow.get("CTCAE Term");
				rowData.put(rowName, singleRow);
			}

			inputStream.close();
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

	private HashMap<String, String> parseRow(XSSFRow row) {
		XSSFCell cell;
		Iterator cells = row.cellIterator();
		HashMap<String, String> cellValues = new HashMap<String, String>();
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
		if (cellValues.size() != 13) {
			String debug = "stop here";
		}
		System.out.println();
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

			// if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING)
			// {
			// System.out.print(cell.getStringCellValue()+" ");
			// }
			// else
			// {
			// System.out.println("Unable to parse header");
			// }
		}
		// System.out.println();

	}

}
