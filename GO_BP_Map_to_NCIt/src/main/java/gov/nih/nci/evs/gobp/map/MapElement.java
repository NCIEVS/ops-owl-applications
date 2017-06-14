package gov.nih.nci.evs.gobp.map;

import org.apache.log4j.Logger;


public class MapElement implements Comparable<MapElement>{
	private MatchTypeEnum matchType;
	private Mapping mapping;
	private String sourceCode;
	private String targetCode;
	private String sourceSynonym;
	private String targetSynonym;
	private float score;
	private String source;
	private String target;
	final static Logger logger = Logger
			.getLogger(gov.nih.nci.evs.gobp.map.MapElement.class);

	public MapElement(String source, String sourceCode, String target,
			String targetCode, MatchTypeEnum match, Mapping mapping, float score) {
		// TODO Auto-generated constructor stub
		this.source = source;
		this.sourceCode = sourceCode;
		this.target = target;
		this.targetCode = targetCode;
		this.matchType = match;
		this.setMapping(mapping);
		this.score = score;
	}

	public MapElement() {

	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}


	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public void setScore(String score) {
		this.score = Float.parseFloat(score);
	}

	
	public MatchTypeEnum getMatchType() {
		return matchType;
	}

	public void setMatchType(MatchTypeEnum matchType) {
		this.matchType = matchType;
	}
	public String getSourceCode() {
		return sourceCode;
	}
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
	public String getTargetCode() {
		return targetCode;
	}
	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}
	public String getSourceSynonym() {
		return sourceSynonym;
	}
	public void setSourceSynonym(String sourceSynonym) {
		this.sourceSynonym = sourceSynonym;
	}
	public String getTargetSynonym() {
		return targetSynonym;
	}
	public void setTargetSynonym(String targetSynonym) {
		this.targetSynonym = targetSynonym;
	}
	
	public boolean equals(MapElement comp){
		boolean same = false;
		if (this.sourceCode.equals(comp.getSourceCode()) && this.targetCode.equals(comp.getTargetCode())){
			same = true;
		} else {
			// TODO remove this debug statement
			System.out.println("False " + this.sourceCode + " "
					+ comp.getSourceCode());
		}
		
		return same;
	}
	
	/*use compare to sort map elements in a collection.
	   sort first by source code, then match type, then target code
	Example
	Source			MatchType	Target
	GO:1234		exact			C1234
	GO:1234		exact			C1235
	GO:1234		contains		C1231
	GO:1234		lucene			C1230
	GO:1234		lucene			C1236
	GO:1235		exact			C1234
	GO:1235		lucene			C1000
	GO:1235		colocation	C999
	
	*/
	public int compareTo(MapElement comp) {
		if(!this.sourceCode.equals(comp.getSourceCode())){
			return this.sourceCode.compareTo(comp.getSourceCode());
		}

		if (!this.targetCode.equals(comp.getTargetCode())) {
		return this.targetCode.compareTo(comp.getTargetCode());
		}
		
		if (!this.matchType.equals(comp.getMatchType())) {

			return compareMatchType(comp);
		}

		return 0;
	}
	
	public int compareMatchType(MapElement comp){
		//multiple by -1 because exactMatch is the best, but in enum is lowest value
		//We don't want to make exactMatch the highest because then we would have
		//    to re-sort the enum every time we added a new match type
		return this.matchType.compareTo(comp.getMatchType())*(-1);
	}

	public Mapping getMapping() {
		return mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}
	
	public String toString() {
		String mapString = this.source + "#" + this.sourceCode + "|"
				+ this.target + "#" + this.targetCode + "|" + this.mapping
				+ "|" + this.score;
		return mapString;
	}

}
