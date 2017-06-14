#!/usr/bin/perl
#
# Script:    rulesObo.pl
# Author:  Brian Carlsen
#
# Remarks:   This script is used to generate QA counts for an obo file.
#
# Version Information:
#   09/09/2008 BAC (): First version
#
use strict 'vars';
use strict 'subs';

#
# Handle Options
#
our @ARGS;
our $verbose = 0;
while (@ARGV) {
    my $arg = shift(@ARGV);

    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);
    }
    if ($arg eq "-v" || $arg eq "--v") {
        $verbose = 1;
    }
    if ($arg eq "-vv" || $arg eq "--vv") {
        $verbose = 2;
    }
}

#
# Handle Params
#
our $srcFile;
our $source;
if (scalar(@ARGS) == 2) {
    ($srcFile, $source) = @ARGS;
} else {
    print "Incorrect number of arguments\n";
    PrintUsage();
    exit(1);
}

our $currentId = "";
our %ids = ();
our %codeHasIsaRule = ();
our %codeHasPnRule = ();
our %nameHasCodeRule = ();
our %defHasCodeRule = ();
our %commentHasCodeRule = ();
our %propHasCodeRule = ();
our %relHasCode1Rule = ();
our %relHasCode2Rule = ();

our %objectProp = ();
our %dataTypeProp = ();
our $base = "";
our $baseFlag = 0;
our %rels = ();

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
open($CMD, "$java $class $srcFile |");
while (<$CMD>) {

    #
    # Clean up steps
    #
    chomp;
    s/\\//g;

    my ($key, $type, $value) = ("", "", "");
    if (/^<(.*)> <(.*)> <(.*)> .$/ ||
        /^<(.*)> <(.*)> \"(.*)\" .$/ ||
        /^_:(.*) <(.*)> <(.*)> .$/ ||
        /^<(.*)> <(.*)> _:(.*) .$/ ) {
        ($key, $type, $value) = ($1, $2, $3);
    }

    print "$_\n" if $verbose > 1;
    print "key = $key\n  type = $type\n    value = $value\n" if $verbose > 1;

    #
    # Establish base URL
    #
    if (!$baseFlag &&
        $type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
        $value eq "http://www.w3.org/2002/07/owl#Ontology") {
        $baseFlag = 1;
        $base = "$key";
	print "base = $base\n" if $verbose;
    }

    #
    # Build up object property (rel) labels
    #
    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	   $value eq "http://www.w3.org/2002/07/owl#ObjectProperty") {
        $objectProp{"$key"} = 1;
    }

    elsif ($type eq "http://www.w3.org/2000/01/rdf-schema#label" &&
	   defined $objectProp{$key}) {
        $objectProp{"$key"} = $value;
	print "Object Property: $key = $value\n" if $verbose;
    }

    #
    # Build up data type (prop) labels
    #
    elsif ($type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
	   $value eq "http://www.w3.org/2002/07/owl#DatatypeProperty") {
        $dataTypeProp{"$key"} = 1;
    }

    elsif ($type eq "http://www.w3.org/2000/01/rdf-schema#label" &&
	   defined $dataTypeProp{$key}) {
        $dataTypeProp{"$key"} = $value;
	print "DataType Property: $key = $value\n" if $verbose;
    }

    elsif ($type eq "http://www.w3.org/2002/07/owl#onProperty" &&
           defined $objectProp{$value}) {
        $rels{$key} = $objectProp{$value};
	print "Anonymous rel class mapped to REL value: $key\n" if $verbose > 1;
    }

    elsif ($key =~ /$base\#(.*)/ &&
        $type eq "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" &&
        $value eq "http://www.w3.org/2002/07/owl#Class") {
	$currentId = $1;
        $codeHasIsaRule{$currentId} = 1;
        $codeHasPnRule{$currentId} = 1;
        $ids{$currentId} = 1;
        delete $relHasCode2Rule{$currentId};
	print "Code found: $1\n" if $verbose;
     }

    elsif ($key =~ /$base\#.*/ &&
	   $type eq "http://www.w3.org/2000/01/rdf-schema#subClassOf" &&
	   $value  =~ /$base\#(.*)/) {
	my $id2 = $1;
	$key  =~ /$base\#(.*)/;
       	my $id1 = $1;
	print "is_a rel found: $id1, $id2, ($value)\n" if $verbose > 1;
	delete $codeHasIsaRule{$currentId};

	if ($currentId ne $id1) {
	    $relHasCode1Rule{$id2} = 1;
	}

	if (!$ids{$id2}) {
	    $relHasCode2Rule{$id2} = 1;
	}
    }

    #
    # Rels are represented as anonymous subclasses with a R# label
    # and a someValuesFrom restriction.  Here, identify the subClassOf
    # part and verify that the key matches the current id
    #
    elsif ($key =~ /$base\#.*/ &&
           $type eq "http://www.w3.org/2000/01/rdf-schema#subClassOf" &&
	   $value =~ /[A-Za-z0-9]*/) {
	$key =~ /$base\#(.*)/;
	print "is_a rel to anonymous rel class found: $1 $value\n" if $verbose > 1;
	if ($currentId ne $1) {
	    $relHasCode1Rule{$1} = 1;
	}
    }

    elsif (defined $rels{$key} &&
           $type eq "http://www.w3.org/2002/07/owl#someValuesFrom" &&
	   $value =~  /$base\#(.*)/) {
	delete $rels{$key};
       	my $id2 = $1;
	print "ID2 of rel found: $id2\n" if $verbose > 1;
	if (!$ids{$id2}) {
	    $relHasCode2Rule{$id2} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "consider") {
	print "consider found: $1\n" if $verbose > 1;
 	if ($currentId ne $1) {
	    $propHasCodeRule{"consider:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "replaced_by") {
	print "replaced_by found: $1\n" if $verbose > 1;
 	if ($currentId ne $1) {
	    $propHasCodeRule{"replaced_by:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
	   $dataTypeProp{"$type"} eq "Preferred_Name") {
	print "PN found: $1\n" if $verbose > 1;
	if ($currentId eq $1) {
	    delete $codeHasPnRule{$currentId};
	} else {
	    $nameHasCodeRule{$1} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "Synonym") {
	print "synonym found: $1\n" if $verbose > 1;

	if ($currentId ne $1) {
	    $nameHasCodeRule{$1} = 1;
	}
        if ($currentId ne $1 && $value =~ /<qual><qual-name>dbxref/) {
	    $propHasCodeRule{"sy_reference:$1"} = 1;
        }
	
        if ($currentId ne $1 && $value =~ /<qual><qual-name>systematic-synonym/) {
	    $propHasCodeRule{"sy_qualifier:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
           $dataTypeProp{"$type"} eq "DEFINITION") {
	print "definition found: $1\n" if $verbose > 1;

	if ($currentId ne $1) {
	    $defHasCodeRule{$key} = 1;
	}
        if ($currentId ne $1 && $value =~ /<qual><qual-name>dbxref/) {
	    $propHasCodeRule{"db_reference:$1"} = 1;
        }
        if ($value =~ /OBSOLETE/) {
	    # obsolete concepts do not require isa rels
            delete $codeHasIsaRule{$1};
        }

    }
    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "comment") {
	print "comment found: $1\n" if $verbose > 1;
	if ($currentId ne $1) {
	    $commentHasCodeRule{$1} = 1;
	}
    }
    
    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "namespace") {
	print "namespace found: $1\n" if $verbose > 1;
 	if ($currentId ne $1) {
	    $propHasCodeRule{"namespace:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
        $dataTypeProp{"$type"} eq "subset") {
	print "subset found: $1\n" if $verbose > 1;
 	if ($currentId ne $1) {
	    $propHasCodeRule{"subset:$1"} = 1;
	}
    }
    
 
    elsif ($key =~ /$base\#(.*)/ &&
	   $dataTypeProp{"$type"} eq "alt_id") {
	print "alt_id found: $1\n" if $verbose > 1;
	if ($currentId ne $1) {
	    $propHasCodeRule{"alt_id:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
	   $dataTypeProp{"$type"} eq "disjoint_from") {
	print "disjoint_from found: $1\n" if $verbose > 1;
	if ($currentId ne $1) {
	    $propHasCodeRule{"disjoint_from:$1"} = 1;
	}
    }

    elsif ($key =~ /$base\#(.*)/ &&
	   $dataTypeProp{"$type"} eq "xref") {
	print "xref found: $1\n" if $verbose > 1;
	if ($currentId ne $1) {
	    $propHasCodeRule{"Database_References:$1"} = 1;
	}
    }
    

}
close($CMD);

#
# Write results
#
our $key;

if (scalar(%codeHasIsaRule) > 0) {
 
    foreach $key (sort keys %codeHasIsaRule) {
	print "codeHasIsa|ontology|fail|$key|\n"
    }
} else {
	print "codeHasIsa|ontology|pass||\n"
}


if (scalar(%codeHasPnRule) > 0) {
 
    foreach $key (sort keys %codeHasPnRule) {
	print "codeHasPn|ontology|fail|$key|\n"
    }
} else {
	print "codeHasPn|ontology|pass||\n"
}

if (scalar(%nameHasCodeRule) > 0) {
 
    foreach $key (sort keys %nameHasCodeRule) {
	print "nameHasCode|refIntegrity|fail|$key|\n"
    }
} else {
	print "nameHasCode|refIntegrity|pass||\n"
}

if (scalar(%defHasCodeRule) > 0) {
 
    foreach $key (sort keys %defHasCodeRule) {
	print "defHasCode|refIntegrity|fail|$key|\n"
    }
} else {
	print "defHasCode|refIntegrity|pass||\n"
}

if (scalar(%commentHasCodeRule) > 0) {
 
    foreach $key (sort keys %commentHasCodeRule) {
	print "commentHasCode|refIntegrity|fail|$key|\n"
    }
} else {
	print "commentHasCode|refIntegrity|pass||\n"
}

print "instrHasCode|refIntegrity|pass||\n";

if (scalar(%propHasCodeRule) > 0) {
 
    foreach $key (sort keys %propHasCodeRule) {
	print "propHasCode|refIntegrity|fail|$key|\n"
    }
} else {
	print "propHasCode|refIntegrity|pass||\n"
}

print "propLinkHasProps|refIntegrity|pass||\n";

if (scalar(%relHasCode1Rule) > 0) {
 
    foreach $key (sort keys %relHasCode1Rule) {
	print "relHasCode1|refIntegrity|fail|$key|\n"
    }
} else {
	print "relHasCode1|refIntegrity|pass||\n"
}

if (scalar(%relHasCode2Rule) > 0) {
 
    foreach $key (sort keys %relHasCode2Rule) {
	print "relHasCode2|refIntegrity|fail|$key|\n"
    }
} else {
	print "relHasCode2|refIntegrity|pass||\n"
}

print "relDataHasCode|refIntegrity|pass||\n";
print "relPropHasRel|refIntegrity|pass||\n";


############################################################################
# Help & Usage Procedures
############################################################################

sub PrintHelp {

    PrintUsage();
    print qq{
 <input file>      : An obo file to count
 <source>          : The source value

 Output is written as a text report.  

 Options:
       -v          : verbose
       -vv         : more verbose
       -[-]help    : On-line help
};
}

sub PrintUsage {
    print qq{This script has the following usage:

    rulesOboOwl.pl <input file> <source>
};
}

