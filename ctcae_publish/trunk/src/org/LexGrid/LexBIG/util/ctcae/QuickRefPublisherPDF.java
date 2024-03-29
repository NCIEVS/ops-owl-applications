package org.LexGrid.LexBIG.util.ctcae;

import java.awt.Color;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
import org.LexGrid.LexBIG.DataModel.Core.Association;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.commonTypes.EntityDescription;
import org.LexGrid.commonTypes.Property;
import org.LexGrid.commonTypes.Text;
import org.LexGrid.concepts.Concept;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.SimpleCell;
import com.lowagie.text.SimpleTable;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Writes the CTCAE Quick-Reference in PDF document format based on input from
 * the LexBIG system.
 */

public class QuickRefPublisherPDF {
	// Property names on event concepts that should be printed as
	// values crossing column boundaries (compared as lower case).
	public static final List<String> MULTICELL_EVENT_PROPS = Arrays
	        .asList(new String[] { "definition" });

	// The number of columns in the category table and relative widths.
	public static final float[] COLWIDTHPCT = { 18.4f, 18.4f, 18.4f, 18.4f,
	        18.4f, 8.0f };

	// Possible column status values ...
	public static final int COLSTATUS_BEFORE = 0;
	public static final int COLSTATUS_GROUP = 1;

	// Possible column styles ...
	public static final int COLSTYLE_EMPTY = 0;
	public static final int COLSTYLE_CATEGORY_TITLE = 1;
	public static final int COLSTYLE_CATEGORY_HEADING = 2;
	public static final int COLSTYLE_CATEGORY_PAGE = 3;
	public static final int COLSTYLE_EVENT_CHOICE = 4;
	public static final int COLSTYLE_EVENT_NOTE = 5;
	public static final int COLSTYLE_EVENT_TEXT = 6;
	public static final int COLSTYLE_PUB_TEXT = 7;

	// Predefined color values ...
	public static final Color COLOR_COVER = new Color(240, 240, 240);
	public static final Color COLOR_PAGE = Color.white;

	// Input and output ...
	private Document document_ = null;
	private SchemeReader in_ = null;
	private PdfWriter pdfOut_ = null;

	// Variables to maintain state during processing ...
	private int chapterCount_ = 0;

	/**
	 * Entry point for processing.
	 * 
	 * @param args
	 *            No arguments required.
	 */
	public static void main(String[] args) {
		SchemeReader reader = null;
		QuickRefPublisherPDF writer = null;
		if (args.length > 0) {
			String lbprops = args[0];
			System.out.println(lbprops);
		}
		try {
			reader = new SchemeReader();
			writer = new QuickRefPublisherPDF(reader);
			writer.publish();

		} finally {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
		}
	}

	/**
	 * Initialize the writer based on the given reader as input. Output will be
	 * written as PDF.
	 * 
	 * @param reader
	 */
	public QuickRefPublisherPDF(SchemeReader reader) {
		super();
		in_ = reader;
		document_ = new Document();
		try {
			pdfOut_ = PdfWriter.getInstance(document_, new FileOutputStream(
			        "CTCAEQuickReference_5x7.pdf"));

			// Can be used to further secure the generated
			// pdf by password, etc.
			// pdfOut_.setEncryption(
			// null, // No user password
			// null, // No owner password
			// PdfWriter.ALLOW_COPY
			// | PdfWriter.ALLOW_MODIFY_ANNOTATIONS
			// | PdfWriter.ALLOW_PRINTING
			// | PdfWriter.ALLOW_SCREENREADERS
			// | PdfWriter.DO_NOT_ENCRYPT_METADATA,
			// PdfWriter.ENCRYPTION_AES_128);

			initDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the input and publish contents to the file specified when the
	 * writer was created.
	 */
	public void publish() {
		try {
			printFrontCover();
			printIntro();
			printBody();
			printBackCover();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the reader, freeing any allocated resources.
	 */
	public void close() {
		if (document_ != null && document_.isOpen())
			try {
				document_.close();
			} catch (Exception e) {
			}
		if (pdfOut_ != null)
			try {
				pdfOut_.close();
			} catch (Exception e) {
			}
	}

	/**
	 * Assigns metadata and characteristics common to all pages of the document.
	 */
	protected void initDocument() {
		document_.addAuthor("National Cancer Institute");
		document_.addCreationDate();
		document_
		        .addKeywords("CTCAE Common Terminology Criteria Adverse Events");
		document_.addSubject("Adverse Event Reporting");
		document_
		        .addTitle("Common Terminology Criteria for Adverse Events (CTCAE)");

		Rectangle r = PageSize.B6.rotate(); // 5 x 7
		r.setBackgroundColor(COLOR_COVER);
		r.setBorder(Rectangle.BOX);
		document_.setPageSize(r);
		document_.setMargins(0f, 0f, 2f, 2f);
		document_.open();
	}

	/**
	 * Print the title page of the document.
	 * <p>
	 * This is primarily static text, but version information is inserted from
	 * the selected ontology.
	 */
	protected void printFrontCover() throws Exception {
		Chapter frontCover = getChapter("CTCAE " + getCodingSchemeVersion());
		Paragraph p = new Paragraph(" \n ");
		frontCover.add(p);

		SimpleTable table = new SimpleTable();
		table.setAlignment(Table.ALIGN_CENTER);
		table.setBorderColor(Color.RED);
		table.setBorderWidth(1f);
		table.setCellpadding(2f);
		table.setWidthpercentage(88f);

		SimpleCell row = new SimpleCell(SimpleCell.ROW);
		SimpleCell cell = new SimpleCell(SimpleCell.CELL);

		Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 32,
		        Font.NORMAL);
		font.setColor(Color.RED);
		p = new Paragraph(
		        "\nCommon Terminology Criteria for Adverse Events (CTCAE)",
		        font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		p.setLeading(32f);
		cell.add(p);

		font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, Font.NORMAL);
		font.setColor(Color.RED);
		// p = new Paragraph("Version " + getCodingSchemeVersion() + "\n\n\n",
		// font);
		p = new Paragraph("Version 4.0\n", font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		cell.add(p);

		font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 14, Font.NORMAL);
		font.setColor(Color.RED);
		p = new Paragraph("Published: May 28, 2009 (v4.03: May 27, 2010)\n\n",
		        font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		cell.add(p);

		font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
		font.setColor(Color.RED);
		p = new Paragraph("U.S.DEPARTMENT OF HEALTH AND HUMAN SERVICES", font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		cell.add(p);

		font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.NORMAL);
		font.setColor(Color.RED);
		p = new Paragraph(
		        "National Institutes of Health\nNational Cancer Institute\n\n",
		        font);
		p.setAlignment(Paragraph.ALIGN_CENTER);
		cell.add(p);

		row.addElement(cell);
		table.addElement(row);

		frontCover.add(table);
		document_.setFooter(null);
		document_.add(frontCover);
	}

	/**
	 * Appends a pre-formatted introduction page to the quick reference output.
	 * <p>
	 * It is assumed the imported QuickRefIntroPage.pdf file is printed from
	 * QuickRefIntroPage.rtf (using a PDF writer such as PDF995) in ISO A6, B6,
	 * or C6 dimensions and scaled to fit the page.
	 */
	protected void printIntro() throws Exception {
		try {
			// Must add to document to insert directly from pdf
			// source (highest quality import). This can result
			// in a no-op or error if non-PDF writers are
			// assigned to the document.
			PdfReader reader = new PdfReader("QuickRefIntroPage.pdf");
			PdfImportedPage page = pdfOut_.getImportedPage(reader, 1);
			Image image = Image.getInstance(page);
			image.setRotationDegrees(270);
			image.setBorder(Rectangle.NO_BORDER);
			image.scaleToFit(document_.getPageSize().getWidth(), document_
			        .getPageSize().getHeight());

			Rectangle r = PageSize.B6.rotate(); // 5 x 7
			r.setBackgroundColor(COLOR_PAGE);
			r.setBorder(Rectangle.BOX);
			document_.setPageSize(r);

			document_.newPage();
			document_.setFooter(buildCategoryFooter(" "));
			document_.add(image);

			// Note: will not reach this point if running with
			// RTF writer; exception is issued.

		} catch (Exception e) {
			// Adding image to non-PDF writers will result in
			// an error; ignored.
		}
	}

	/**
	 * Prints a table containing information for each concept.
	 */
	protected void printBody() throws Exception {
		if (in_ == null)
			return;

		ResolvedConceptReference category = null;
		while ((category = in_.getNextCategory()) != null)
			try {
				// Build and add the table of adverse events
				// for this category ...
				String categoryText = category.getEntityDescription() != null ? category
				        .getEntityDescription().getContent()
				        : "<untitled category>";

				// Set a footer for the duration of the category.
				HeaderFooter categoryFooter = buildCategoryFooter(categoryText);

				// Build and add the category table.
				SimpleTable categoryTable = buildCategoryTable(category);
				if (pdfOut_ != null) {
					PdfPTable pdfTable = categoryTable.createPdfPTable();
					pdfTable.setHeaderRows(3);
					Chapter categoryChapter = getChapter(categoryText);
					categoryChapter.add(pdfTable);
					document_.setFooter(categoryFooter);
					// int pageNum = document_.getPageNumber();
					document_.setMarginMirroringTopBottom(true);
					document_.setMargins(0f, 0f, 32f, 6f);
					document_.newPage();
					document_.add(categoryChapter);

				}

			} catch (DocumentException e) {
				e.printStackTrace();
			}
		// Rectangle r = PageSize.B6.rotate(); // 5 x 7
		// r.setBackgroundColor(COLOR_COVER);
		// r.setBorder(Rectangle.BOX);
		// document_.setPageSize(r);
		// document_.newPage();
	}

	/**
	 * Print the final page of the document.
	 * <p>
	 * This is primarily images, representing publisher and barcode.
	 */
	protected void printBackCover() throws Exception {
		Chapter backCover = getChapter("Publication Information");
		DateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");

		// Spacers for alignment
		SimpleCell spacer_min = new SimpleCell(SimpleCell.CELL);
		spacer_min.setBorder(SimpleCell.NO_BORDER);
		spacer_min.setWidthpercentage(10f);

		SimpleCell spacer_max = new SimpleCell(SimpleCell.CELL);
		spacer_max.setBorder(SimpleCell.NO_BORDER);
		spacer_max.setWidthpercentage(30f);

		// HHS Logo
		SimpleCell row1 = new SimpleCell(SimpleCell.ROW);
		row1.setBorder(SimpleCell.NO_BORDER);
		row1.add(spacer_min);
		Image image = Image.getInstance("Image-hhs-logo.png");
		SimpleCell imageCell = new SimpleCell(SimpleCell.CELL);
		imageCell.setBorder(SimpleCell.NO_BORDER);
		imageCell.setHorizontalAlignment(SimpleCell.ALIGN_RIGHT);
		imageCell.setPadding_left(54f);
		imageCell.setPadding_right(0f);
		imageCell.setPadding_top(54f);
		imageCell.setPadding_bottom(0f);
		imageCell.add(image);
		row1.add(imageCell);
		row1.add(spacer_max);

		// NIH Logo
		image = Image.getInstance("Image-nih-logo.png");
		imageCell = new SimpleCell(SimpleCell.CELL);
		imageCell.setBorder(SimpleCell.NO_BORDER);
		imageCell.setHorizontalAlignment(SimpleCell.ALIGN_LEFT);
		imageCell.setPadding_left(0f);
		imageCell.setPadding_right(54f);
		imageCell.setPadding_top(54f);
		imageCell.setPadding_bottom(0f);
		imageCell.add(image);
		row1.add(imageCell);

		// NCI Logo
		SimpleCell row2 = new SimpleCell(SimpleCell.ROW);
		row2.setBorder(SimpleCell.NO_BORDER);
		row2.add(spacer_min);
		row2.add(spacer_max);
		image = Image.getInstance("Image-nci-logo.png");
		imageCell = new SimpleCell(SimpleCell.CELL);
		imageCell.setBorder(SimpleCell.NO_BORDER);
		imageCell.setHorizontalAlignment(SimpleCell.ALIGN_CENTER);
		imageCell.setPadding_left(18f);
		imageCell.setPadding_right(18f);
		imageCell.setPadding_top(0f);
		imageCell.setPadding_bottom(0f);
		imageCell.add(image);
		row2.add(imageCell);
		row2.add(spacer_max);

		// Pub text
		SimpleCell row3 = new SimpleCell(SimpleCell.ROW);
		row3.setBorder(SimpleCell.NO_BORDER);
		row3.add(spacer_min);
		SimpleCell text = getTextCell("NIH Publication No. 09-5410"
		        + "\nRevised " + "May 2010" + "\nReprinted " + "June 2010",
		        COLSTYLE_PUB_TEXT, 3, -1f);
		text.setBorder(SimpleCell.NO_BORDER);
		text.setHorizontalAlignment(SimpleCell.ALIGN_CENTER);
		row3.add(text);

		// Barcode
		SimpleCell row4 = new SimpleCell(SimpleCell.ROW);
		row4.setBorder(SimpleCell.NO_BORDER);
		row4.add(spacer_min);
		row4.add(spacer_max);
		row4.add(spacer_max);
		// image = Image.getInstance("Image-barcode.png");
		image = Image.getInstance("Image-emptybarcode.png");
		imageCell = new SimpleCell(SimpleCell.CELL);
		imageCell.setBorder(SimpleCell.NO_BORDER);
		imageCell.setHorizontalAlignment(SimpleCell.ALIGN_RIGHT);
		imageCell.setPadding_left(54f);
		imageCell.setPadding_right(0f);
		imageCell.setPadding_top(0f);
		imageCell.setPadding_bottom(0f);
		imageCell.add(image);
		row4.add(imageCell);

		SimpleTable table = new SimpleTable();
		table.setBorder(Table.NO_BORDER);
		table.setCellspacing(0f);
		table.setWidthpercentage(95f);
		table.add(row1);
		table.add(row2);
		table.add(row3);
		table.add(row4);

		backCover.add(table);

		Rectangle r = PageSize.B6.rotate(); // 5 x 7
		r.setBackgroundColor(COLOR_COVER);
		r.setBorder(Rectangle.BOX);
		document_.setPageSize(r);

		document_.setFooter(null);
		document_.newPage();
		document_.add(backCover);
	}

	/**
	 * Builds the table for an individual category, represented by the provided
	 * concept.
	 */
	protected SimpleTable buildCategoryTable(ResolvedConceptReference category) {
		SimpleTable table = new SimpleTable();
		try {
			// Write header information based on the resolved
			// concept, which represents the category ...
			table.setBorder(Table.BOX);
			table.setBorderColor(Color.BLACK);
			table.setBorderWidth(1.2f);
			table.setAlignment(Table.ALIGN_CENTER);
			table.setWidthpercentage(93f);

			buildCategoryHeaderRow1(table, category);
			buildCategoryHeaderRow2(table);
			buildCategoryHeaderRow3(table);

			// Iterate through all child concepts associated with
			// the category, printing additional rows as appropriate ...
			for (ResolvedConceptReference event : in_
			        .getAdverseEvents(category)) {
				buildEventRows(table, event);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * Builds the initial title row for the category concept.
	 * <p>
	 * The resulting row contains the category description (centered) and
	 * defines a column span of to to allow for pagination to be inserted as a
	 * second column.
	 */
	protected void buildCategoryHeaderRow1(SimpleTable table,
	        ResolvedConceptReference rcr) throws DocumentException {
		SimpleCell row = new SimpleCell(SimpleCell.ROW);
		row.setGrayFill(0.8f);

		String text = (rcr != null && rcr.getEntityDescription() != null) ? rcr
		        .getEntityDescription().getContent() : "<unavailable>";

		row.add(getTextCell(text, COLSTYLE_CATEGORY_TITLE, COLWIDTHPCT.length,
		        -1));
		table.add(row);
	}

	/**
	 * Builds the secondary header row for display of each category, containing
	 * grade label.
	 */
	protected void buildCategoryHeaderRow2(SimpleTable table)
	        throws DocumentException {
		SimpleCell row = new SimpleCell(SimpleCell.ROW);
		row.setGrayFill(0.8f);

		row.add(getTextCell("", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[0]));
		row.add(getTextCell("Grade", COLSTYLE_CATEGORY_HEADING, 5,
		        COLWIDTHPCT[1] + COLWIDTHPCT[2] + COLWIDTHPCT[3]
		                + COLWIDTHPCT[4] + COLWIDTHPCT[5]));

		table.add(row);
	}

	/**
	 * Builds header labels, providing a title for each column.
	 */
	protected void buildCategoryHeaderRow3(SimpleTable table)
	        throws DocumentException {
		SimpleCell row = new SimpleCell(SimpleCell.ROW);
		row.setGrayFill(0.8f);
		row.add(getTextCell("Adverse Event", COLSTYLE_CATEGORY_HEADING, 1,
		        COLWIDTHPCT[0]));
		row.add(getTextCell("1", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[1]));
		row.add(getTextCell("2", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[2]));
		row.add(getTextCell("3", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[3]));
		row.add(getTextCell("4", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[4]));
		row.add(getTextCell("5", COLSTYLE_CATEGORY_HEADING, 1, COLWIDTHPCT[5]));

		table.add(row);
	}

	/**
	 * Builds the footer to display for the duration of the category chapter.
	 */
	protected HeaderFooter buildCategoryFooter(String categoryText) {
		String footerDate = new SimpleDateFormat("MMMM dd, yyyy")
		        .format(new Date());
		StringBuffer footerText = new StringBuffer().append("CTCAE ").append(
		        getCodingSchemeVersion()).append(" - ").append(footerDate);

		// Font footFont = FontFactory.getFont(BaseFont.HELVETICA,
		// BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 6.0f);
		// Phrase footerStart = new Phrase(footerText.toString(), footFont);

		if (!StringUtils.isBlank(categoryText))
			footerText.append(" : ").append(categoryText);

		Paragraph footer = new Paragraph(footerText.toString(), FontFactory
		        .getFont(BaseFont.HELVETICA, BaseFont.WINANSI,
		                BaseFont.NOT_EMBEDDED, 6.0f));
		HeaderFooter hf = new HeaderFooter(footer, true);
		// Phrase footerEnd = new Phrase(categoryText, footFont);
		// HeaderFooter hf = new HeaderFooter(footerStart, footerEnd);

		footer.setSpacingBefore(0f);
		footer.setSpacingAfter(0f);
		hf.setBorder(0);
		hf.setAlignment(HeaderFooter.ALIGN_CENTER);
		return hf;
	}

	/**
	 * Inserts rows for an individual adverse event, represented by the provided
	 * concept.
	 */
	protected void buildEventRows(SimpleTable table,
	        ResolvedConceptReference event) throws DocumentException {

		// Build the first row, with event and grade text
		Concept c = event.getReferencedEntry();
		if (c != null) {

			// First column is from the entity description
			String[] columns = new String[7];
			EntityDescription desc = c.getEntityDescription();
			columns[0] = desc != null ? desc.getContent() : null;
			List<Property> multiCellProps = new ArrayList<Property>();

			// Grade columns are drawn from resolved children.
			// Note that we are navigating in reverse (getTargetOf)
			// since the association is subClassOf which moves
			// from child to parent.
			try {
				for (Association toGrades : event.getTargetOf()
				        .getAssociation()) {
					for (AssociatedConcept gradeConcept : toGrades
					        .getAssociatedConcepts().getAssociatedConcept()) {
						int gradeLevel = in_.getGradeLevel(gradeConcept);
						if (gradeLevel > 0 && gradeLevel < columns.length) {
							columns[gradeLevel] = in_
							        .getGradeText(gradeConcept);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Look for additional properties that cut across
			// cell boundaries.
			for (Property prop : c.getAllProperties()) {
				String propName = prop.getPropertyName().toLowerCase();
				if (MULTICELL_EVENT_PROPS.contains(propName))
					multiCellProps.add(prop);
			}

			// Insert grades into a single row with fixed columns ...
			SimpleCell row = new SimpleCell(SimpleCell.ROW);
			row.setBackgroundColor(Color.WHITE);
			if (columns[0] != null) {
				row.add(getTextCell(columns[0], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}
			if (columns[1] != null) {
				row.add(getTextCell(columns[1], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[1]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}
			if (columns[2] != null) {
				row.add(getTextCell(columns[2], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[2]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}
			if (columns[3] != null) {
				row.add(getTextCell(columns[3], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[3]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}
			if (columns[4] != null) {
				row.add(getTextCell(columns[4], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[4]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}
			if (columns[5] != null) {
				row.add(getTextCell(columns[5], COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[5]));
			} else {
				row.add(getTextCell(" - ", COLSTYLE_EVENT_TEXT, 1,
				        COLWIDTHPCT[0]));
			}

			// Insert miscellaneous descriptive properties as
			// additional rows spanning the table width ...
			for (Property p : multiCellProps) {
				Text pText = p.getValue();
				if (pText != null && pText.getContent() != null) {
					String annotation = StringUtils.capitalize(p
					        .getPropertyName().toLowerCase())
					        + ": " + pText.getContent().trim();
					annotation = StringEscapeUtils.unescapeXml(annotation);
					row.add(getTextCell(annotation, COLSTYLE_EVENT_NOTE,
					        columns.length, 100));
				}
			}
			table.add(row);
		}
	}

	/**
	 * Returns a new chapter, numbered sequentially in the order requested, with
	 * the given title text.
	 */
	protected Chapter getChapter(String text) {
		// Note: Use same color as cover so Chapter title and
		// number do not appear in mainline text, but are still
		// used to build navigational bookmarks and provide
		// structure for formatting.
		Paragraph chapterTitle = new Paragraph(text, FontFactory.getFont(
		        BaseFont.HELVETICA, 1, COLOR_COVER));
		chapterTitle.setAlignment(Chapter.ALIGN_CENTER);
		chapterTitle.setExtraParagraphSpace(0f);
		return new Chapter(chapterTitle, ++chapterCount_);
	}

	/**
	 * Returns a string representing version of the selected code system.
	 */
	protected String getCodingSchemeVersion() {
		return in_ == null ? "<unavailable>" : in_.getCodingSchemeVersion();
	}

	/**
	 * Returns a cell containing the given text in a certain style and width.
	 */
	protected SimpleCell getTextCell(String s, int style, int colspan,
	        float width) {
		SimpleCell cell = new SimpleCell(SimpleCell.CELL);
		cell.setBorderWidth(0.2f);
		cell.setPadding_top(0f);
		cell.setPadding_bottom(3f);
		cell.setPadding_left(2f);
		cell.setPadding_right(2f);

		Paragraph p;
		switch (style) {
		case COLSTYLE_CATEGORY_HEADING:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA_BOLD,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 6.0f));
			p.setAlignment(Element.ALIGN_CENTER);
			cell.add(p);
			cell.setBorder(SimpleCell.BOX);
			cell.setColspan(colspan);
			cell.setWidthpercentage(width);
			break;
		case COLSTYLE_CATEGORY_PAGE:
		case COLSTYLE_CATEGORY_TITLE:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA_BOLD,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 9.0f));
			p.setAlignment(Element.ALIGN_CENTER);
			cell.add(p);
			cell.setBorder(SimpleCell.BOX);
			cell.setColspan(colspan);
			break;
		case COLSTYLE_EMPTY:
			cell.setBorder(SimpleCell.BOX);
			break;
		case COLSTYLE_EVENT_CHOICE:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 6.0f));
			p.setAlignment(Element.ALIGN_CENTER);
			cell.add(p);
			cell.setBorder(SimpleCell.BOX);
			cell.setColspan(colspan);
			cell.setWidthpercentage(width);
			break;
		case COLSTYLE_EVENT_NOTE:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 6.0f));
			p.setAlignment(Element.ALIGN_LEFT);
			cell.add(p);
			p.setAlignment(Element.ALIGN_MIDDLE);
			cell.setBorder(SimpleCell.NO_BORDER);
			cell.setColspan(colspan);
			cell.setWidthpercentage(width);
			break;
		case COLSTYLE_EVENT_TEXT:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 6.0f));
			p.setAlignment(Element.ALIGN_LEFT);
			cell.add(p);
			cell.setBorder(SimpleCell.TOP | SimpleCell.LEFT | SimpleCell.RIGHT);
			cell.setColspan(colspan);
			cell.setWidthpercentage(width);
			break;
		case COLSTYLE_PUB_TEXT:
			p = new Paragraph(s, FontFactory.getFont(BaseFont.HELVETICA,
			        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED, 8.0f));
			p.setAlignment(Element.ALIGN_CENTER);
			cell.add(p);
			cell.setBorder(SimpleCell.NO_BORDER);
			cell.setColspan(colspan);
			cell.setWidthpercentage(width);
			break;
		}
		return cell;
	}
}