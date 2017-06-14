package gov.hhs.fda.ctr.etl.meddra;

import java.util.HashSet;
import java.util.Set;
/**
 * 
 * @author marwahah
 *
 */
public class MedDRAHeirarchy {
	
	Set<MedDRALowerTerm> lowerLevelTerms = new HashSet<MedDRALowerTerm>();
	
	String AEPT;
	String AEPTCD;
	String AEHLT;
	String AEHLTCD;
	String AEHLGT;
	String AEHLGTCD;
	String AESOC;
	String AESOCCD;
	
	
	public void addAELLT(String aELLT, String aELLTCD) {
		lowerLevelTerms.add(new MedDRALowerTerm(aELLT,aELLTCD));
	}
	

	public Set<MedDRALowerTerm> getLowerLevelTerms() {
		return lowerLevelTerms;
	}


	public void setLowerLevelTerms(Set<MedDRALowerTerm> lowerLevelTerms) {
		this.lowerLevelTerms = lowerLevelTerms;
	}



	public String getAEPT() {
		return AEPT;
	}
	public void setAEPT(String aEPT) {
		AEPT = aEPT;
	}
	public String getAEPTCD() {
		return AEPTCD;
	}
	public void setAEPTCD(String aEPTCD) {
		AEPTCD = aEPTCD;
	}
	public String getAEHLT() {
		return AEHLT;
	}
	public void setAEHLT(String aEHLT) {
		AEHLT = aEHLT;
	}
	public String getAEHLTCD() {
		return AEHLTCD;
	}
	public void setAEHLTCD(String aEHLTCD) {
		AEHLTCD = aEHLTCD;
	}
	public String getAEHLGT() {
		return AEHLGT;
	}
	public void setAEHLGT(String aEHLGT) {
		AEHLGT = aEHLGT;
	}
	public String getAEHLGTCD() {
		return AEHLGTCD;
	}
	public void setAEHLGTCD(String aEHLGTCD) {
		AEHLGTCD = aEHLGTCD;
	}
	public String getAESOC() {
		return AESOC;
	}
	public void setAESOC(String aESOC) {
		AESOC = aESOC;
	}
	public String getAESOCCD() {
		return AESOCCD;
	}
	public void setAESOCCD(String aESOCCD) {
		AESOCCD = aESOCCD;
	}
	
	public String toString() {
		return " AEPT:"+AEPT+" AEPTCD:"+AEPTCD+" AEHLT:"+AEHLT+" AEHLTCD:"+AEHLTCD+" AEHLGT:"+AEHLGT+" AEHLGTCD:"+AEHLGTCD+" AESOC:"+AESOC+" AESOCCD:"+AESOCCD+"\n"+lowerLevelTerms;
	}
	
	

}
