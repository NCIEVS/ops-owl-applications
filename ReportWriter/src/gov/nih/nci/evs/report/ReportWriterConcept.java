package gov.nih.nci.evs.report;



import java.util.Vector;



public interface ReportWriterConcept {


//	public String getSource();
//	public ReportWriterProperty getSourcePT();
//	public ReportWriterProperty getNCIPT();
//	public ReportWriterProperty getSourceDef();
//	public ReportWriterProperty getNciDef();
//	public Vector<ReportWriterProperty> getSourceSynonyms();
//	public Vector<ReportWriterProperty> getNciSynonyms();
//	public ReportWriterProperty getPreferredTerm(String source);
//	public ReportWriterProperty getDefinition(String source);
//	public Vector<ReportWriterProperty> getSynonyms(String source);
	public String getName();
	public String getCode();
	public Vector<ReportWriterProperty> getProperty(String propName);
//	public Vector<ReportWriterProperty> getFullSynByType(String value);
	public Vector<ReportWriterConcept> getParents();
	public void setParents(Vector<ReportWriterConcept> parents);
	public Vector<ReportWriterConcept> getChildren();
	public void setChildren(Vector<ReportWriterConcept> children);
	public Vector<ReportWriterProperty> getComplexProperty(String[] propArray);
//	public Vector<ReportWriterProperty> getFullSynByQual(String qualName, String qualValue);

}
