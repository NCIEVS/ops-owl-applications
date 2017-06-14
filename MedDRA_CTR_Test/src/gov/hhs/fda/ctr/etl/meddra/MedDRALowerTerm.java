package gov.hhs.fda.ctr.etl.meddra;
/**
 * 
 * @author marwahah
 *
 */
public class MedDRALowerTerm {
	
	String LLT;
	String LLTCD;
	
	public MedDRALowerTerm(){
		
	}
	
    public MedDRALowerTerm(String llt, String lltcd){
		this.LLT =llt;
		this.LLTCD = lltcd;
	}
	
	public String getLLT() {
		return LLT;
	}
	public void setLLT(String lLT) {
		LLT = lLT;
	}
	public String getLLTCD() {
		return LLTCD;
	}
	public void setLLTCD(String lLTCD) {
		LLTCD = lLTCD;
	}
	
	public String toString() {
		return "LLT:"+LLT+" LLTCD:"+LLTCD;
	}
	
	

}
