package gov.nih.nci.evs.ctcae.data;

public class MedDRA_LLT {

	private String meddraCode;
	private String LLT;
	private String lltCode;
	
	
	public MedDRA_LLT(String mCode, String soc, String lCode){
		this.meddraCode=mCode;
		this.LLT=soc;
		this.lltCode=lCode;
	}
	public String getMeddraCode() {
		return meddraCode;
	}
	public String getLLT() {
		return LLT;
	}
	public String getLltCode() {
		return lltCode;
	}

	public String toString(){
		return meddraCode + " " + LLT + " " + lltCode;
	}
	
}
