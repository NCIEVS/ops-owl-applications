package gov.hhs.fda.ctr.etl.meddra;

import org.junit.Test;

public class MedDRAHeirarchyCacheTest {

	@Test
	public void testMedDRAHeirarchyCache() {
		MedDRAHeirarchyCache cache = new MedDRAHeirarchyCache();
		// cache.cacheMedDRAHeirarchy("14.1");
		cache.getMedDRAHeirarchy("Stomach Upset", "14.1");
	}

	public void main(String[] args) {
		testMedDRAHeirarchyCache();
	}
}
