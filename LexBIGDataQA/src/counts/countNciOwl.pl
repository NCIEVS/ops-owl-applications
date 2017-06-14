#!/usr/bin/perl
#
# Script:  countNciOwl.pl
# Author:  Nels Olson
#
# Remarks:   This script is used to generate QA counts for an NCI Thesaurus
#            OWL file.
#
# Version Information:
#   09/21/2011 NEO (): Update to support rel, relProp, and relType sampling;
#                      fixed a bug that omitted subClassOf from relTypeCount
#   09/16/2011 NEO (): Fixed bug in parsing of Jena output
#   09/15/2011 NEO (): Update to support defTally sampling and to strip
#                      whole-line comments before sending to the Jena parser to
#                      work around Jena's not allowing comments containing "--"
#   03/11/2011 NEO (): Update to count various omitted items like domain, range,
#                      subPropertyOf, type, FULL_SYN PTs & DesignNote as comment
#   03/10/2011 NEO (): Update to count "defined" in codeTally, count anonymous
#                      things, include presentation type in nameTally, and
#                      support nameTally sampling
#   02/23/2011 NEO (): Update to support some kinds of sampling
#   12/23/2009 NEO (): Update to handle new "ncicp:" complex-property format
#   10/15/2009 NEO (): Update to conform to new counts.html spec
#   06/05/2009 NEO (): First version
use strict 'vars';
use strict 'subs';

#
# Handle Options
#
our @ARGS;
our $verbose = 0;
our $sample = 0;
our $sampleSpec = '';
our $sampleFile = '';
our $debug = 0;
while (@ARGV) {
    my $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);
    }

    if ($arg eq "-s" || $arg eq "--s") {
      $sample = 1;
      $sampleSpec = shift(@ARGV);
    }
    if ($arg eq "-f" || $arg eq "--f") {
      $sample = 2;
      $sampleFile = shift(@ARGV);
    }
    if ($arg eq "-v" || $arg eq "--v") {
        $verbose = 1;
    }
    if ($arg eq "-vv" || $arg eq "--vv") {
        $verbose = 2;
    }
    if ($arg eq "-vvv" || $arg eq "--vvv") {
        $verbose = 3;
    }
    if ($arg eq "-d" || $arg eq "--d") {
        $debug = 1;
    }
}

#
# Handle Params
#
our $srcFile = "";
our $source = "";
if (scalar(@ARGS) == 2) {
    ($srcFile, $source) = @ARGS;
} else {
    print "Incorrect number of arguments\n";
    PrintUsage();
    exit(1);
}



our $qaModelType = "";
our $codeCount= 0;
our %codeTally = ();
our $commentCount= 0;
our $defCount = 0;
our %defTally = ();
our $instrCount = 0;
our $nameCount = 0;
our %nameTally = ();
our $propCount = 0;
our %propTally = ();
our $propLinkCount = 0;
our %propLinkTally = ();
our $propPropCount = 0;
our %propPropTally = ();
our $relCount = 0;
our %relTally = ();
our $relDataCount = 0;
our %relDataTally = ();
our $relPropCount = 0;
our %relPropTally = ();
our $relTypeCount = 0;
our $relPropTypeCount = 0;
our $isaRelValue = "";
our $languageTypeCount = 1;
our $propTypeCount = 0;
our $propLinkTypeCount = 0;
our $propPropTypeCount = 0;
our $ttyCount = 0;
our $sourceCount = 0;
our $changeCount = 0;
our %changeTally = ();
our %objectProp = ();
our %dataTypeProp = ();
our $base = "";
our $baseFlag = 0;
our %rel = ();
our %codeFlags = ();
our %seen = ();
our %relsSeen = ();
our %propTypeSeen = ();
our %propPropTypeSeen = ();
our %relTypeSeen = ();
our %relPropTypeSeen = ();
our %ttySeen = ();
our %sourceSeen = ();
our $thisClass = '';
our $equivClass = '';
our @sampleSpecs = ();
our %toSample;
our %sampleTypes = ();
our %toPrint = ();

if ($sample == 2) {
  open(SAMPLESPECS, $sampleFile) || die "can't open $sampleFile, stopped";
  while (<SAMPLESPECS>) {
    chomp;
    push(@sampleSpecs, $_);
  }
} elsif ($sample == 1) {
  @sampleSpecs = ($sampleSpec);
}

foreach $sampleSpec (@sampleSpecs) {
  my ($tallyName, $tallyFieldValues) = ($sampleSpec =~ /^([^:]*)\:(.*)/);
  $sampleTypes{$tallyName} = 1;
  $toSample{"$tallyName|$tallyFieldValues"} = 1;
}

print "languageTypeCount||en|\n" if $sampleTypes{"languageTypeCount"};

#
# TODO: see if all of these are really needed
#
$ENV{"CLASSPATH"} = "$ENV{QA_HOME}/lib/antlr-2.7.5.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/arq-extra.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/arq.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/commons-logging-1.1.1.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/concurrent.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/icu4j_3_4.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/iri.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/jena.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/json.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/log4j-1.2.12.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/lucene-core-2.3.1.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/stax-api-1.0.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/wstx-asl-3.0.0.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/xercesImpl.jar";
$ENV{"CLASSPATH"} .= ":$ENV{QA_HOME}/lib/xml-apis.jar";

#
# Set java stuff
#
our $java = "$ENV{JAVA_HOME}/bin/java -server -Xms200M -Xmx1000M ";
our $class = "jena.rdfparse";
our $CMD;

# do preliminary pass to cache property codes
open($CMD, "sed '/^ *<\!--.*--> *\$/d' $srcFile | $java $class |");
while (<$CMD>) {
    #
    # Clean up steps
    #
    chomp;
    s/\\//g;
    
    my ($key, $type, $value) = ("", "", "");
    if (/^<([^>]*)> <([^>]*)> <(.*)> .$/ || 
	/^<([^>]*)> <([^>]*)> \"(.*)\" .$/ ||
	/^<([^>]*)> <([^>]*)> \"(.*)\"\^\^<http[^>]*XML(Literal|Schema#string)> .$/ ||
        /^_:([^ ]*) <([^>]*)> <(.*)> .$/ ||
        /^<([^>]*)> <([^>]*)> _:(.*) .$/ ) {
	($key, $type, $value) = ($1, $2, $3);
    }

    #
    # Establish base URL
    #
    if (!$baseFlag &&
	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#Ontology") {
	$baseFlag = 1;
	$base = $key;
	print "base = $base\n" if $verbose;
    }

    #
    # Build up object property (rel) labels
    #
    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#ObjectProperty") {
	$objectProp{$key} = 1;
    }

    elsif ($type eq "http://www.w3.org/2000/01/rdf-schema#label" &&
	defined $objectProp{$key}) {
	$objectProp{$key} = $value;
	print "Object property  $key: $value\n" if $verbose;
    }

    #
    # Build up data type (prop) labels
    #
    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#DatatypeProperty") {
	if ($key =~ /$base\#(.*)/) {
	    $dataTypeProp{$key} = $1;
	}
    }

    elsif ($type eq "http://www.w3.org/2000/01/rdf-schema#label" &&
	defined $dataTypeProp{$key}) {
	$dataTypeProp{$key} = $value;
	print "Data type property  $key: $value\n" if $verbose;
    }
   
    elsif ($key =~ /$base\#(.*)/ &&
	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#Class") {
	# stop preliminary pass when we reach the Class definitions
	last;
    }
}

our $CMD2;
open($CMD2, "sed '/^ *<\!--.*--> *\$/d' $srcFile | $java $class |");
while (<$CMD2>) {
    #
    # Clean up steps
    #
    chomp;
    s/\\//g;
    
    my ($key, $type, $value) = ("", "", "");
    if (/^<([^>]*)> <([^>]*)> <(.*)> .$/ || 
	/^<([^>]*)> <([^>]*)> \"(.*)\" .$/ ||
	/^<([^>]*)> <([^>]*)> \"(.*)\"\^\^<http[^>]*XML(Literal|Schema#string)> .$/ ||
        /^_:([^ ]*) <([^>]*)> <(.*)> .$/ ||
        /^<([^>]*)> <([^>]*)> _:(.*) .$/ ) {
	($key, $type, $value) = ($1, $2, $3);
    }

    print "$_\n" if $verbose>2;
    print "key = $key\n  type = $type\n    value=$value\n" if $verbose>1 && $key ne "";

    if ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#ObjectProperty") {
	$qaModelType="Metadata";

	&process("prop", "en,type,$source,$qaModelType", 
	         "$thisClass|ObjectProperty");
	if ($key =~ /$base\#(.*)/) {
	    &processType("relType", $objectProp{$key});
	    unless ($seen{$1}++) {
	      $codeFlags{$1} = "Y,N";
	      &process("code", "null,Y,N,$qaModelType", $1);
	    }
	}
    }

    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#AnnotationProperty") {
	$qaModelType="Metadata";
	&process("prop", "en,type,$source,$qaModelType", 
	         "$thisClass|AnnotationProperty");
	if ($key =~ /$base\#(.*)/) {
	    unless ($seen{$1}++) {
	      $codeFlags{$1} = "Y,N";
	      &process("code", "null,Y,N,$qaModelType", $1);
	    }
	}
    }

    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#TransitiveProperty") {
	$qaModelType="Metadata";
	&process("prop", "en,type,$source,$qaModelType", 
	         "$thisClass|TransitiveProperty");
	if ($key =~ /$base\#(.*)/) {
	    unless ($seen{$1}++) {
	      $codeFlags{$1} = "Y,N";
	      &process("code", "null,Y,N,$qaModelType", $1);
	    }
	}
    }

    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#DatatypeProperty") {
	$qaModelType="Metadata";
	&process("prop", "en,type,$source,$qaModelType", 
	         "$thisClass|DatatypeProperty");
	if ($key =~ /$base\#(.*)/) {
	    &processType("propType", $dataTypeProp{$key});
	    unless ($seen{$1}++) {
	      $codeFlags{$1} = "Y,N";
	      &process("code", "null,Y,N,$qaModelType", $1);
	    }
	}
    }

    elsif ($type =~ m@^http://www\.w3\.org/2000/01/rdf-schema#(domain|range|subPropertyOf)$@) {
	my $atn = $1;
	my $val = $1 if $value =~ /$base\#(.*)/;
	&process("prop", "en,$atn,$source,$qaModelType", "$thisClass|$val");
    }

    elsif ($type eq "http://www.w3.org/2002/07/owl#onProperty" &&
	   defined $objectProp{$value}) {
	$rel{$key} = $objectProp{$value};
	print "Relationship  $key: $objectProp{$value}\n" if $verbose;
    }

    elsif (defined $rel{$key} &&
	   $type eq "http://www.w3.org/2002/07/owl#someValuesFrom" &&
# substitute the next two commented lines (1 & 5 lines down) for the lines
# following them to count equivClass items that duplicate subClassOf items
#	   !$relsSeen{"$thisClass$equivClass|$rel{$key}|$value"}++) {
	   !$relsSeen{"$thisClass|$rel{$key}|$value"}++) {
	my $rel = $rel{$key};
	my $target = $1 if $value =~ /$base\#(.*)/;
#	&process("rel", "$source,$rel,$qaModelType", "$thisClass-$equivClass|$target");
	&process("rel", "$source,$rel,$qaModelType", "$thisClass|$target");
	&process("relProp", "owl:someValuesFrom,$qaModelType", "$thisClass|$target|");

#	&processType("relPropType", "owl:someValuesFrom");
    }

    elsif (defined $rel{$key} &&
	   $type eq "http://www.w3.org/2002/07/owl#allValuesFrom" &&
	   !$relsSeen{"$thisClass|$rel{$key}|$value"}++) {
	my $rel = $rel{$key};
	my $target = $1 if $value =~ /$base\#(.*)/;
	&process("rel", "$source,$rel,$qaModelType", "$thisClass|$target");
	&process("relProp", "owl:allValuesFrom,$qaModelType", "$thisClass|$target|");

#	&processType("relPropType", "owl:allValuesFrom");
    }

    elsif (defined $objectProp{$type}) {
	# this is just to handle "AnnotationProperty" rels (codes A1-A10)
	my $rel = $objectProp{$type};
	my $target = $1 if $value =~ /$base\#(.*)/;
	&process("rel", "$source,$rel,$qaModelType", "$thisClass|$target");
	if ($type =~ /$base\#(.*)/) {
	    &processType("propType", $rel);
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#Class") {
	$qaModelType="Content";
	$thisClass = $1;
	&process("prop", "en,type,$source,$qaModelType", "$thisClass|Class");
	unless ($seen{$thisClass}++) {
	    $codeFlags{$thisClass} = "Y,N";
	    &process("code", "null,Y,N,$qaModelType", $thisClass);
	}
	$equivClass = '';
	%relsSeen = ();
	# count a "primitive" property that gets generated for each concept
	&process("prop", "en,primitive,$source,$qaModelType","$thisClass|true");
    }

#    elsif ($key =~ /^jA/ &&
#	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
#	$value =~ /^http:\/\/www.w3.org\/2002\/07\/owl#(.*)/) {
#	print "Anon: $1 $key\n" if $verbose==1;
#    }

    elsif ($key =~ /^(jA.*)/ &&
	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") {
# don't set qaModelType, as future triples will still be for the parent class
#	$qaModelType = "Anonymous";
	my $code = $1;
	my $val = $1 if $value =~ /http:\/\/www.w3.org\/2002\/07\/owl#(.*)/;
	&process("prop", "en,type,$source,Anonymous", "$code|$val");
	if (!$seen{$code}++) {
	    $codeFlags{$code} = "Y,N";
	    &process("code", "null,Y,N,Anonymous", $code);
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
	$type eq "http://www.w3.org/2002/07/owl#equivalentClass") {
	my $code = $1;
	$equivClass = $value;
	if ($seen{$code}++) {
	  &deProcess("code", "null,$codeFlags{$code},$qaModelType", $code);
	}
	$codeFlags{$code} = "Y,Y" if $codeFlags{$code} eq "";
	$codeFlags{$code} =~ s/.$/Y/;
	&process("code", "null,$codeFlags{$code},$qaModelType", $code);
    }

    elsif ($key =~ /$base\#(.*)/ &&
	$type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	$value eq "http://www.w3.org/2002/07/owl#DeprecatedClass") {
	my $code = $1;
	$qaModelType="Content";
	&process("prop", "en,type,$source,$qaModelType",
	         "$code|DeprecatedClass");
	if ($seen{$code}++) {
	  &deProcess("code", "null,$codeFlags{$code},$qaModelType", $code);
	}
	$codeFlags{$code} = "N,N" if $codeFlags{$code} eq "";
	$codeFlags{$code} =~ s/^./N/;
	&process("code", "null,$codeFlags{$code},$qaModelType", $code);
#	print STDERR $code, "\n" if $debug==1;
    }

    elsif ($key =~ /$base\#(.*)/ &&
	$dataTypeProp{$type} eq "FULL_SYN") {
	my $code = $1;
	my ($term) = ($value =~ m@<ncicp:term-name>(.*)</ncicp:term-name>@);
#	if ($value !~ m@<ncicp:term-group>PT</ncicp:term-group>@ || $value !~ m@<ncicp:term-source>NCI</ncicp:term-source>@) {
	    if ($value =~ m@<ncicp:term-group>(.*)</ncicp:term-group>@) {
		&process("name", "null,N,en,$1,FULL_SYN,$source,$qaModelType",
		         "$code|$term");
	    } else {
		&process("name", "null,N,en,null,FULL_SYN,$source,$qaModelType",
		         "$code|$term");
	    }
#	}
	if ($value =~ m@<ncicp:term-source>(.*)</ncicp:term-source>@) {
	    $propPropCount++;
	    $propPropTally{"presentation,source,$1,$qaModelType"}++;
	    &processType("propPropType", "source");
	    &processType("source", $1);
	}
	if ($value =~ m@<ncicp:term-group>(.*)</ncicp:term-group>@) {
	    &processType("tty", $1);
	}
	if ($value =~ m@<ncicp:subsource-name>(.*)</ncicp:subsource-name>@) {
	    $propPropCount++;
	    $propPropTally{"presentation,qualifier,subsource-name,$qaModelType"}++;
	    &processType("propPropType", "subsource-name");
	}
	if ($value =~ m@<ncicp:source-code>(.*)</ncicp:source-code>@) {
	    $propPropCount++;
	    $propPropTally{"presentation,qualifier,source-code,$qaModelType"}++;
	    &processType("propPropType", "source-code");
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
        ($dataTypeProp{$type} eq "Preferred_Name" || $dataTypeProp{$type} eq
	 "Display_Name" || $dataTypeProp{$type} eq "NDFRT_Name")) {
	my $pref = ($dataTypeProp{$type} eq "Preferred_Name") ? "Y" : "N";
	&process("name",
	         "null,$pref,en,null,$dataTypeProp{$type},$source,$qaModelType",
		 "$1|$value");
    }

    elsif ($key =~ /$base\#(.*)/ &&
	$type eq "http://www.w3.org/2000/01/rdf-schema#label") {
	&process("name", "null,N,en,null,label,$source,$qaModelType",
		 "$1|$value");
    }

    elsif ($key =~ /$base\#(.*)/ && $dataTypeProp{$type} eq "Synonym") {
	# don't count Synonyms because they're already counted as FULL_SYNs
    }

    elsif ($key =~ /$base\#(.*)/ && $dataTypeProp{$type} eq "DesignNote") {
	# only count these where qaModelType eq "Content"?
	++$commentCount;
    }

    elsif ($key =~ /$base\#(.*)/ &&
	   $type eq "http://www.w3.org/2000/01/rdf-schema#subClassOf" &&
	   $value =~ /$base\#.*/) {
	$thisClass = $1 if $key =~ /$base\#(.*)/;
	$equivClass = '';
	$isaRelValue = "subClassOf";
	my $target = $1 if $value =~ /$base\#(.*)/;
	&process("rel", "$source,subClassOf,$qaModelType","$thisClass|$target");
    }

    elsif ($key =~ /$base\#(.*)/ &&
	   $dataTypeProp{$type} =~ /^(ALT_)?(LONG_)?DEFINITION$/) {
	my $code = substr($key, length($base)+1);
	my $def = $value;
	$def = $1 if $def =~ /<ncicp:def-definition>(.*)<\/ncicp:def-definition>/;
	$def =~ s/\&lt\;/</g;
	$def =~ s/\&gt\;/>/g;
	$def =~ s/\&amp\;/\&/g;
	&process("def", "$source,$qaModelType", "$code|$def");
	if ($value =~ m@<ncicp:def-source>(.*)</ncicp:def-source>@) {
	    $propPropCount++;
	    $propPropTally{"definition,source,$1,$qaModelType"}++;
	    &processType("propPropType", "source");
	    &processType("source", $1);
	}
	if ($value =~ m@<ncicp:attr>(.*)</ncicp:attr>@) {
	    $propPropCount++;
	    $propPropTally{"definition,qualifier,attr,$qaModelType"}++;
	    &processType("propPropType", "attr");
	}
    }

    # all other Data Type properties
    elsif ($key =~ /$base\#(.*)/ &&
	   defined $dataTypeProp{$type}) {
	&process("prop", "en,$dataTypeProp{$type},$source,$qaModelType",
	         "$thisClass|$value");
#	    if ($debug && $dataTypeProp{$type} eq 'Unit') {
#		if ($key =~ /$base\#(.*)/) {
#		    print STDERR $1, "\n";
#		}
#	    }
	if ($dataTypeProp{$type} eq "GO_Annotation") {
	    if ($value =~ m@<ncicp:go-evi>(.*)</ncicp:go-evi>@) {
		$propPropCount++;
		$propPropTally{"GO_Annotation,qualifier,go-evi,$qaModelType"}++;
	        &processType("propPropType", "go-evi");
	    }
	    if ($value =~ m@<ncicp:go-id>(.*)</ncicp:go-id>@) {
		$propPropCount++;
		$propPropTally{"GO_Annotation,qualifier,go-id,$qaModelType"}++;
	        &processType("propPropType", "go-id");
	    }
	    if ($value =~ m@<ncicp:go-source>(.*)</ncicp:go-source>@) {
		$propPropCount++;
		$propPropTally{"GO_Annotation,qualifier,go-source,$qaModelType"}++;
	        &processType("propPropType", "go-source");
	    }
	    if ($value =~ m@<ncicp:source-date>(.*)</ncicp:source-date>@) {
		$propPropCount++;
		$propPropTally{"GO_Annotation,qualifier,source-date,$qaModelType"}++;
	        &processType("propPropType", "source-date");
	    }
	}
    }
    
    else {
	if ($verbose==1) {
	    if ($key eq '') {
		print "UNUSED TRIPLE: $_\n";
	    } else {
		print "UNUSED TRIPLE:\n";
		print " key = $key\n  type = $type\n    value=$value\n";
	    }
	}
	if ($verbose==2) {
	    print "UNUSED TRIPLE: $_\n";
	    print " key = $key\n  type = $type\n    value=$value\n";
	}
    }

}
close($CMD2);

our %month = ('Jan','01','Feb','02','Mar','03','Apr','04','May','05','Jun','06',
              'Jul','07','Aug','08','Sep','09','Oct',10,'Nov',11,'Dec',12);

open(HIST, "cumulative_history.txt");
while (<HIST>) {
    our($x,$x,$type,$date) = split(/[|\n]/);
    our($d,$m,$y) = split(/-/, $date);
    $m = $month{$m};
    $changeCount++;
    $changeTally{"$type,20$y$m"}++;
}

if ($sample) {
  foreach our $key (keys %toPrint) {
    print "$key\n" if $toPrint{$key};
  }
  exit 0;
}

#
# Write results
#

our $key;
print "codeCount||$codeCount|\n";
foreach $key (sort keys %codeTally) {
    print "codeTally|$key|$codeTally{$key}|\n";
}
print "commentCount||$commentCount|\n";
print "defCount||$defCount|\n";
foreach $key (sort keys %defTally) {
    print "defTally|$key|$defTally{$key}|\n";
}
print "instrCount||$instrCount|\n";
print "nameCount||$nameCount|\n";
foreach $key (sort keys %nameTally) {
    print "nameTally|$key|$nameTally{$key}|\n";
}
print "propCount||$propCount|\n";
foreach $key (sort keys %propTally) {
    print "propTally|$key|$propTally{$key}|";
    if ($key =~ /\@/) {
	print "-$propTally{$key}|AUTO|INFO ONLY|";
    }
    print "\n";

}
print "propPropCount||$propPropCount|\n";
foreach $key (sort keys %propPropTally) {
    print "propPropTally|$key|$propPropTally{$key}|\n";
}
print "propLinkCount||$propLinkCount|\n";
foreach $key (sort keys %propLinkTally) {
    print "propLinkTally|$key|$propLinkTally{$key}|\n";
}
print "relCount||$relCount|\n";
foreach $key (sort keys %relTally) {
    print "relTally|$key|$relTally{$key}|\n";
}
print "relDataCount||$relDataCount|\n";
foreach $key (sort keys %relDataTally) {
    print "relDataTally|$key|$relDataTally{$key}|\n";
}
print "relPropCount||$relPropCount|\n";
foreach $key (sort keys %relPropTally) {
    print "relPropTally|$key|$relPropTally{$key}|\n";
}
print "relTypeCount||$relTypeCount|\n";
print "relPropTypeCount||$relPropTypeCount|\n";
print "isaRelValue||$isaRelValue|\n";
print "languageTypeCount||$languageTypeCount|\n";
print "propTypeCount||$propTypeCount|\n";
print "propLinkTypeCount||$propLinkTypeCount|\n";
print "propPropTypeCount||$propPropTypeCount|\n";
print "ttyCount||$ttyCount|\n";
print "sourceCount||$sourceCount|\n";
print "changeCount||$changeCount|\n";
foreach $key (sort keys %changeTally) {
    print "changeTally|$key|$changeTally{$key}|\n";
}

sub process {
  my($type, $vals, $sampVals) = @_;
  my($tallyName, $countName) = ("${type}Tally", "${type}Count");
  my($typeCountName,$typeSeenName) = ("${type}TypeCount", "${type}TypeSeen");
  my(@vals) = split(/,/, $vals);
  my($typeVal) = ($type eq "propProp") ? $vals[2] :
                 ($type eq "relProp") ? $vals[0] :
                 ($type eq "prop" || $type eq "rel") ? $vals[1] : "";
  if (!$sample) {
    ++$$countName;
    ++$$tallyName{$vals};
    if ($typeVal ne "") {
      $$typeCountName++ unless $$typeSeenName{$typeVal}++;
    }
  } else {
    $toPrint{"$tallyName|$vals|$sampVals|"}=1 if $toSample{"$tallyName|$vals"};
    unless ($$typeSeenName{$typeVal}++) {
      $toPrint{"$typeCountName||$typeVal|"}=1 if $sampleTypes{$typeCountName};
    }
  }
}

sub deProcess {
  my($type, $vals, $sampVals) = @_;
  my($tallyName, $countName) = ("${type}Tally", "${type}Count");
  my($typeCountName,$typeSeenName) = ("${type}TypeCount", "${type}TypeSeen");
  my(@vals) = split(/,/, $vals);
  my($typeVal) = ($type eq "propProp") ? $vals[2] :
                 ($type eq "relProp") ? $vals[0] :
                 ($type eq "prop" || $type eq "rel") ? $vals[1] : "";
  if (!$sample) {
    --$$countName;
    --$$tallyName{$vals};
#    if ($typeVal ne "") {
#      $$typeCountName++ unless $$typeSeenName{$typeVal}++;
#    }
  } else {
    $toPrint{"$tallyName|$vals|$sampVals|"}=0 if $toSample{"$tallyName|$vals"};
  }
}

# process xxTypeCount items such as relTypeCount as well as {tty,source}Count
sub processType {
  my($type, $sampVals) = @_;  # e.g., type = "relType" or "tty"
  my($countName) = ("${type}Count");      # e.g., countName=relTypeCount
  my($seenName) = ("${type}Seen");    # e.g., seenName=relTypeSeen

  unless ($$seenName{$sampVals}++) {
    ++$$countName if !$sample;
    $toPrint{"$countName||$sampVals|"}=1 if $sampleTypes{$countName};
  }
}


############################################################################
# Help & Usage Procedures
############################################################################

sub PrintHelp {

    PrintUsage();
    print qq{
 <input file>      : An NCI Thesaurus OWL file to count
 <source>          : The source-name value to use

 Output is written as a text report.  

 Options:
       -v          : verbose
       -vv         : more verbose
       -[-]help    : On-line help
};
}

sub PrintUsage {
    print qq{This script has the following usage:

    countNciOwl.pl <input file> <source>
};
}

