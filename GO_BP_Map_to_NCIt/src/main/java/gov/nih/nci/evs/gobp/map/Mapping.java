package gov.nih.nci.evs.gobp.map;

public enum Mapping {
	equivalent, broader_than, narrower_than, related;

	public static Mapping parseMapping(String map) {
		switch (map) {
		case "=":
		case "SY":
			return Mapping.equivalent;
		case ">":
			return Mapping.broader_than;
		case "<":
			return Mapping.narrower_than;
		default:
			return Mapping.related;
		}
	}
}