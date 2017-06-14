// Copyright (c) 2006 ScenPro, Inc.

// $Header: /share/content/gforge/sentinel/sentinel/src/gov/nih/nci/cadsr/sentinel/audits/AuditReport.java,v 1.4 2007-07-19 15:26:44 hebell Exp $
// $Name: not supported by cvs2svn $

package gov.nih.nci.cadsr.sentinel.audits;

//import gov.nih.nci.cadsr.sentinel.database.DBAlert;

/**
 * @author lhebel
 *
 */
public abstract class AuditReport
{
    /**
     * Constructor
     * 
     * @param db_ the database connection to the caDSR
     */
//    public AuditReport(DBAlert db_)
//    {
//        _db = db_;
//    }

    /**
     * Constructor
     *
     */
    public AuditReport()
    {
    }
    
    /**
     * Set the database connection object.
     * 
     * @param db_ the database connection.
     */
//    public void setDB(DBAlert db_)
//    {
//        _db = db_;
//    }
    
    /**
     * Return the title for the report.
     * 
     * @return the report title.
     */
    abstract public String getTitle();
    
    /**
     * Get the report details. Each array entry is a row in the formatted HTML table. Columns
     * for each row are separated using the _colSeparator variable defined in this class. If it where
     * a double colon, "::", e.g. "Data Element::Heart::12345"
     * represents three columns. All array entries in the report must have the same number
     * of columns.
     * 
     * @return the report details formatted as described above.
     */
    abstract public String[] getReportRows();
    
    /**
     * Should the row count be displayed?
     * 
     * @return true to display the row count.
     */
    abstract public boolean okToDisplayCount();
    
    /**
     * Tell the report writer to right justify the last column.
     * 
     * @return true to right justify the last column of the report rows.
     */
    abstract public boolean rightJustifyLastColumn();

    /**
     * Construct the row header for the table section.
     * 
     * @param title_
     *        The title of the section.
     * @param count_
     *        The count of rows in the section or -1.
     * @param cols_ the number of columns in the report
     * @param num_ the report number
     * @return The composite table row string.
     */
    public static String formatHeader(String title_, int count_, int cols_, int num_)
    {
        String text = "<tr><td colspan=\"" + cols_ + "\" style=\"border-bottom: solid black 1px\"><b>"
            + "<div onclick=\"reporta" + num_ + ".style.display = 'block'; reportb" + num_ + ".style.display = 'none';\"><span class=\"action\">[Hide]</span> "
            + title_;
        if (count_ > 0)
            text += " (" + (count_ - 1) + " items)";
        return text + "</div></b></td></tr>\n";
    }
    
    /**
     * Construct the division section for a report.
     * 
     * @param title_ the title of the section
     * @param num_ the report number
     * @return the div tag
     */
    public static String formatSectionTop(String title_, int num_)
    {
        String text = "<div id=\"reporta" + num_
            + "\" class=\"report\" onclick=\"reportb" + num_
            + ".style.display = 'block'; reporta" + num_
            + ".style.display = 'none';\"><span class=\"action\">[View]</span> " + title_ + "</div>\n"
            + "<div id=\"reportb" + num_ + "\" style=\"display: none\">\n";

        return text;
    }

    /**
     * Close the section division
     * 
     * @return the closing div tag
     */
    public static String formatSectionBottom()
    {
        return "</div>\n";
    }

    /**
     * Form the table rows for the list provided.
     * 
     * @param list_
     *        The list of row entries.
     * @return The HTML table rows.
     */
    public static String formatRows(String list_[])
    {
        String text = "";
        int scnt = 0;
        for (int ndx = 0; ndx < list_.length; ++ndx)
        {
            if (list_[ndx] != null)
            {
                String stripe = ((scnt % 2) == 0) ? " style=\"background-color: #ccffff\""
                    : "";
                String temp = "<tr" + stripe + "><td>"
                    + list_[ndx].replace(AuditReport._ColSeparator, "</td><td>") + "</td></tr>";
                text = text + temp.replaceAll("\\n", "<br>") + "\n";
                ++scnt;
            }
        }
        return text;
    }
    
    /**
     * Get the split pattern matching the reserved column separator string.
     * 
     * @return the split pattern.
     */
    public static String getSplitPattern()
    {
        // Build the pattern used to split the report lines into columns.
        String splitPattern = "";
        for (int i = 0; i < AuditReport._ColSeparator.length(); ++i)
        {
            splitPattern += "[" + AuditReport._ColSeparator.charAt(i) +  "]";
        }
        
        return splitPattern;
    }
    
    /**
     * The data column separator reserved string.
     */
    public static final String _ColSeparator = "=_=";
    
    /**
     * The database connection to the caDSR.
     */
//    protected DBAlert _db;
}
