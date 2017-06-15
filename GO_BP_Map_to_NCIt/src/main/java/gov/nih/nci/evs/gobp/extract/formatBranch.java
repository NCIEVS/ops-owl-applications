package gov.nih.nci.evs.gobp.extract;

import java.io.File;

import org.apache.log4j.Logger;



public class formatBranch {

	private final String owlInput = "";
	private String classList = "";
	final private Logger logger = Logger.getLogger(gov.nih.nci.evs.gobp.extract.formatBranch.class);
	private StripType stripType;

	/**
	 * 
	 * @param owlFileInput
	 * @param branchFileInput
	 * @param stripType
	 */
	public formatBranch(String owlFilePath, String branchFilePath, StripType stripType) {
		File owlInputFile = new File(owlFilePath);
		File branchFile = new File(branchFilePath);
		
	}

	/**
	 * 
	 * @param owlFileInput
	 * @param branchFileInput
	 */
	public formatBranch(String owlFilePath, String branchFilePath) {
		
     	this(owlFilePath, branchFilePath, StripType.inward);
		
	}

	/**
	 * Builds a set of restrictions to drop, as specified by the stripType
	 * 
	 * print "Building restriction drop map...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * if( $owlLines[$i] =~ /\s\s\s\s<owl:ObjectProperty rdf:about=\"#(.*)"\/>\s ) {
	 * my $value = $1;
	 * $restrictionDropMap{$value} = 1;
	 * $owlLines[$i] = q{};
	 * print $debug "Added to restriction drop map $value\n";
	 * }
	 * }
	 */
	public void buildRestrictionDropMap() {
		// TODO - implement formatBranch.buildRestrictionDropMap
		
	}

	/**
	 * load in the classes from the owlFileInput
	 * 
	 * print "Configuring class map...\n";
	 * foreach(@classes) {
	 * my $value = $_;
	 * chomp($value);
	 * $value =~ s/\s//g; #remove whitespace, if any, from tabbed hierarchy
	 * $classMap{$value} = 1;
	 * print $debug "Added to class map $value\n";
	 * }
	 */
	public void configureClassMap() {
		// TODO - implement formatBranch.configureClassMap
		
	}

	/**
	 * remove the classes not in the branch list
	 * 
	 * print "Removing classes outside the branch...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * my $begin;
	 * my $end;
	 * my $space;
	 * if( $owlLines[$i] =~ /\s\s\s\s<owl:(Class|DeprecatedClass) rdf:about=\"#(.*)">\s ) {
	 * my $classType = $1;
	 * my $className = $2;
	 * $begin = $i-5; ## remove the comment and newlines, too
	 * while( $owlLines[$i] !~ /^\s\s\s\s<\/owl:$classType>$/ ) {
	 * $i++;
	 * }
	 * $end = $i;
	 * if( !exists($classMap{$className}) ) {
	 * print $debug "Removing $className\n";
	 * my $j = $begin;
	 * $owlLines[$j] = q{};
	 * while( ++$j <= $end ) {
	 * $owlLines[$j] = q{};
	 * }
	 * }
	 * }
	 * }
	 */
	public void removingClassesOutsideBranch() {
		// TODO - implement formatBranch.removingClassesOutsideBranch
		
	}

	/**
	 * remove disjoint statements referring to other branches
	 * 
	 * print "Removing disjointWith statements based on class file...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * if( $owlLines[$i] =~ /\s*<owl:disjointWith rdf:resource="#(.*)"\/>\s ) {
	 * my $value = $1;
	 * if( !exists($classMap{$value}) ) {
	 * print $debug "Removing disjointWith value $value\n";
	 * $owlLines[$i] = q{};
	 * }
	 * }
	 * }
	 */
	public void removeDisjointStatements() {
		// TODO - implement formatBranch.removeDisjointStatements
		
	}

	/**
	 * remove unwanted roles from role groups
	 * 
	 * print "Checking for unwanted restrictions within role groups...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * if( $owlLines[$i] =~ /\s\s\s\s<owl:Class rdf:about=\"#(.*)">\s ) {
	 * my $hasRG = "false";
	 * while( $owlLines[$i] !~ /^\s\s\s\s<\/owl:Class>$/ ) {
	 * if( $owlLines[$i] =~ /\s*<owl:unionOf\s ) {
	 * $hasRG = "true";
	 * }
	 * if( $owlLines[$i] =~ /\s*<owl:someValuesFrom rdf:resource="#(.*)"\/>/ ) {
	 * my $value = $1;
	 * if( !exists($classMap{$value}) ) {
	 * if( $hasRG eq "true" ) {
	 * print $debug "RG dissection possibly needed at line $i\n";
	 * }
	 * }
	 * }
	 * $i++
	 * }
	 * }
	 * }
	 */
	public void disectRoleGroups() {
		// TODO - implement formatBranch.disectRoleGroups
		
	}

	/**
	 * Remove parents that are not in the current branch
	 * 
	 * if( $owlLines[$i] =~ /\s*<rdfs:subClassOf rdf:resource="#(.*)"\/>\s ) {
	 * my $value = $1;
	 * if( !exists($classMap{$value}) ) {
	 * print $debug "Removing subclass axiom with value $value in class $className at line $i\n";
	 * $owlLines[$i] = q{};
	 * }
	 * }
	 */
	public void removeParent() {
		// TODO - implement formatBranch.removeParent
		
	}

	/**
	 * Check each restriction, but subClass and equivalentClass, and check if the target is within the desired branch set.  If not, discard.  If so, put it into a hashmap of either all, some or has.
	 * 
	 * f( $stripFlag eq "i" ) {
	 * print "Removing unwanted restrictions, applying simple restrictions, adding subClassOfs...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * if( $owlLines[$i] =~ /(\s*)<owl:Class rdf:about=\"#(.*)">\s ) {
	 * while($owlLines[$i] !~ /.*<\/rdfs:label>.) {
	 * $i++;
	 * }
	 * my $pointOfSubClassInsertion = $i;
	 * my $pointOfRestrictionInsertion = $i;
	 * my $space = $1;
	 * my $className = $2;
	 * my %allRestrictionMap;
	 * my %someRestrictionMap;
	 * my %hasRestrictionMap;
	 * my @superclasses;
	 * my $superRemoval = "false";
	 * while( $owlLines[$i] !~ /$space<\/owl:Class>\s ) {
	 * if( $owlLines[$i] =~ /\s*<rdfs:subClassOf rdf:resource="#(.*)"\/>\s ) {
	 * my $value = $1;
	 * if( !exists($classMap{$value}) ) {
	 * print $debug "Removing subclass axiom with value $value in class $className at line $i\n";
	 * $owlLines[$i] = q{};
	 * }
	 * }
	 * if( $owlLines[$i] =~ /(\s*)(<owl:equivalentClass>|<rdfs:subClassOf>)\s ) {
	 * my $inSpace = $1;
	 * my $type = $2;
	 * my $removal = "false";
	 * my $begin = $i;
	 * my $end;
	 * $i++;
	 * while( $owlLines[$i] !~ /$inSpace(<\/owl:equivalentClass>|<\/rdfs:subClassOf>)\s ) {
	 * if( $owlLines[$i] =~ /\s*<rdf:Description rdf:about="#(.*)"\/>\s ) {
	 * my $superclass = $1;
	 * if( exists($classMap{$superclass}) ) {
	 * push @superclasses, $superclass;
	 * }
	 * else {
	 * $removal = "true";
	 * $superRemoval = "true";
	 * }
	 * }
	 * if( $owlLines[$i] =~ /\s*<owl:Restriction>\s ) {
	 * $i++;
	 * if( $owlLines[$i] =~ /\s*<owl:onProperty rdf:resource="#(.*)"\/>\s ) {
	 * my $restriction = $1;
	 * my $value;
	 * $i++;
	 * ##TODO - Cardinality!
	 * if( $owlLines[$i] =~ /\s*<owl:(allValuesFrom|someValuesFrom|hasValue) rdf:resource="#(.*)"\/>/ ) {
	 * my $qualifiedType = $1;
	 * $value = $2;
	 * if( !exists($classMap{$value}) ) {
	 * print $debug "Removing restriction $restriction with value $value in class $className at line $i\n";
	 * $removal = "true";
	 * $superRemoval = "true";
	 * }
	 * else {
	 * if( $type =~ /<owl:equivalentClass>/ ) {
	 * if( $qualifiedType eq "allValuesFrom" ) {
	 * push @{$allRestrictionMap{$restriction}}, $value;
	 * }
	 * if( $qualifiedType eq "someValuesFrom" ) {
	 * push @{$someRestrictionMap{$restriction}}, $value;
	 * }
	 * if( $qualifiedType eq "hasValue" ) {
	 * push @{$hasRestrictionMap{$restriction}}, $value;
	 * }
	 * }
	 * else {
	 * ##do nothing, it's a plain restriction to keep
	 * }
	 * }
	 * $i++;
	 * }
	 * }
	 * }
	 * $i++;
	 * }
	 * $end = $i;
	 * if( $removal eq "true" ) {
	 * my $j = $begin;
	 * while( $j <= $end ) {
	 * $owlLines[$j] = q{};
	 * $j++;
	 * }
	 * }
	 * }
	 * $i++;
	 * }
	 */
	public void evaluateRestrictions() {
		// TODO - implement formatBranch.evaluateRestrictions
		
	}

	/**
	 * Take the restriction hashmaps we have build and write them into the evaluation in proper form
	 * 
	 * if( $superRemoval eq "true" ) {
	 * foreach(@superclasses) {
	 * $owlLines[$pointOfSubClassInsertion] .= " <rdfs:subClassOf rdf:resource=\"#$_\"/>\n";
	 * }
	 * my $size = keys %someRestrictionMap;
	 * if( $size > 0 ) {
	 * for my $restrictionKey ( sort keys %someRestrictionMap ) {
	 * foreach( @{$someRestrictionMap{$restrictionKey}} ) {
	 * my $value = $_;
	 * $owlLines[$pointOfRestrictionInsertion] .= " <rdfs:subClassOf>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:onProperty rdf:resource=\"#$restrictionKey\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:someValuesFrom rdf:resource=\"#$value\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </rdfs:subClassOf>\n";
	 * }
	 * }
	 * }
	 * $size = keys %allRestrictionMap;
	 * if( $size > 0 ) {
	 * for my $restrictionKey ( sort keys %allRestrictionMap ) {
	 * foreach( @{$allRestrictionMap{$restrictionKey}} ) {
	 * my $value = $_;
	 * $owlLines[$pointOfRestrictionInsertion] .= " <rdfs:subClassOf>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:onProperty rdf:resource=\"#$restrictionKey\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:allValuesFrom rdf:resource=\"#$value\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </rdfs:subClassOf>\n";
	 * }
	 * }
	 * }
	 * $size = keys %hasRestrictionMap;
	 * if( $size > 0 ) {
	 * for my $restrictionKey ( sort keys %hasRestrictionMap ) {
	 * foreach( @{$hasRestrictionMap{$restrictionKey}} ) {
	 * my $value = $_;
	 * $owlLines[$pointOfRestrictionInsertion] .= " <rdfs:subClassOf>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:onProperty rdf:resource=\"#$restrictionKey\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " <owl:hasValue rdf:resource=\"#$value\"\/>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </owl:Restriction>\n";
	 * $owlLines[$pointOfRestrictionInsertion] .= " </rdfs:subClassOf>\n";
	 * }
	 * }
	 * }
	 * }
	 * }
	 * }
	 * }
	 */
	public void addRestriction() {
		// TODO - implement formatBranch.addRestriction
		
	}

	/**
	 * If stripType is stripped, remove all restrictions except subClassOf within the branch
	 * 
	 * elsif( $stripFlag eq "s" ) {
	 * print "Removing all restrictions, preserving rdf:Descriptions as subClassOfs...\n";
	 * for( $i=0; $i < $owlLineCount; $i++ ) {
	 * if( $owlLines[$i] =~ /(\s*)<owl:Class rdf:about=\"#(.*)">\s ) {
	 * while($owlLines[$i] !~ /.*<\/rdfs:label>.) {
	 * $i++;
	 * }
	 * my $pointOfSubClassInsertion = $i+1;
	 * my $space = $1;
	 * my $className = $2;
	 * my @superclasses;
	 * while( $owlLines[$i] !~ /$space<\/owl:Class>\s ) {
	 * if( $owlLines[$i] =~ /\s*<rdfs:subClassOf rdf:resource="#(.*)"\/>\s ) {
	 * my $value = $1;
	 * if( !exists($classMap{$value}) ) {
	 * print $debug "Removing subclass axiom with value $value in class $className at line $i\n";
	 * $owlLines[$i] = q{};
	 * }
	 * }
	 * if( $owlLines[$i] =~ /(\s*)(<owl:equivalentClass>)\s ) {
	 * my $inSpace = $1;
	 * my $type = $2;
	 * my $removal = "false";
	 * my $begin = $i;
	 * my $end;
	 * $i++;
	 * ## save only the subclasses
	 * while( $owlLines[$i] !~ /$inSpace(<\/owl:equivalentClass>)\s ) {
	 * if( $owlLines[$i] =~ /\s*<rdf:Description rdf:about="#(.*)"\/>\s ) {
	 * my $superclass = $1;
	 * if( exists($classMap{$superclass}) ) {
	 * push @superclasses, $superclass;
	 * }
	 * }
	 * $i++;
	 * }
	 * $end = $i;
	 * my $j = $begin;
	 * while( $j <= $end ) {
	 * $owlLines[$j] = q{};
	 * $j++;
	 * }
	 * }
	 * ## erase the restrictions
	 * elsif( $owlLines[$i] =~ /(\s*)(<rdfs:subClassOf>)\s ) {
	 * my $inSpace = $1;
	 * while( $owlLines[$i] !~ /$inSpace(<\/rdfs:subClassOf>)\s ) {
	 * $owlLines[$i] = q{};
	 * $i++;
	 * }
	 * $owlLines[$i] = q{};
	 * }
	 * $i++;
	 * }
	 * foreach(@superclasses) {
	 * $owlLines[$pointOfSubClassInsertion] .= " <rdfs:subClassOf rdf:resource=\"#$_\"/>\n";
	 * }
	 * }
	 * }
	 * }
	 */
	public void stripAllRestrictions() {
		// TODO - implement formatBranch.stripAllRestrictions
		
	}

}