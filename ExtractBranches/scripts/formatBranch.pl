#!/usr/bin/perl
## Rob Wynne, MSC
## April 6th, 2012
##
## Last revised: September 10, 2014
##
## Format a Thesaurus publication.
## Remove outlying
## classes based on a config file.  Re-assert
## restrictions as necessary (option i).  Or, remove all
## restrictions entirely (option s).  Or, keep them
## all (option k).
##
## Limitations:
## maxCardinality, minCardinality, and owl:cardinality
## needs implemented

use strict;

my $start = time();

open( my $input, $ARGV[1]) or die "Couldn't open owl file!\n";
open( my $classFile, $ARGV[2]) or die "Couldn't open class file!\n";
open my $debug, '>', "debug.txt" or die "Coudln't create debug file!\n";

my $i=0;
my @owlLines = <$input>;
my @classes = <$classFile>;
my %classMap;
my %restrictionDropMap;
my %nsmap;
my $owlLineCount = @owlLines;
my $stripFlag;
my $style;
my $ncitns = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
my $filename = $ARGV[3];

if( $ARGV[0] =~ /\-([KkSsIi])/ ) {
  my $arg = $1;
  if( lc($arg) eq "i" ) { $stripFlag = "i"; $style = "inward"; }
  elsif( lc($arg) eq "s" ) { $stripFlag = "s"; $style = "stripped"; }
  elsif( lc($arg) eq "k" ) { $stripFlag = "k"; $style = "kept"; }
}
else {
  die "Invalid restrictions option, please use -i (keep inward branch restrictions), -s (strip all), -k (keep all restrictions)\n";
}

print "Configuring class map...\n";
foreach(@classes) {
  my $value = $_;
  chomp($value);
  $value =~ s/\s//g; #remove whitespace, if any, from tabbed hierarchy
  $classMap{$value} = 1;
  print $debug "Added to class map $value\n";
}

print "Building restriction drop map...\n";
for( $i=0; $i < $owlLineCount; $i++ ) {
     if( $owlLines[$i] =~ /\s{4}<owl:ObjectProperty rdf:about=\".*#(.*)"\/>\s*/ ) {
       my $value = $1;
       $restrictionDropMap{$value} = 1;
       $owlLines[$i] = q{};
       print $debug "Added to restriction drop map $value\n";
     }
}

print "Removing classes outside the branch...\n";
for( $i=0; $i < $owlLineCount; $i++ ) {
  my $begin;
  my $end;
  my $space;
  if( $owlLines[$i] =~ /\s{4}<owl:(Class|DeprecatedClass) rdf:about=\".*#(.*)">\s*/ ) {
      my $classType = $1;
      my $className = $2;
      $begin = $i-5; ## remove the comment and newlines, too
      while( $owlLines[$i] !~ /^\s\s\s\s<\/owl:$classType>$/ ) {
        $i++;
      }
      $end = $i;
      if( !exists($classMap{$className}) ) {
          print $debug "Removing $className\n";
          my $j = $begin;
          $owlLines[$j] = q{};
          while( ++$j <= $end ) {
            $owlLines[$j] = q{};
          }
      }
  }
}

print "Removing axioms outside the branch... \n";
for( $i=0; $i < $owlLineCount; $i++ ) {
  my $begin;
  my $end;
  my $space;
  if( $owlLines[$i] =~ /\s{4}<owl:Axiom>\s*/ ) {
    $begin = $i;
    $i++;
    if( $owlLines[$i] =~ /\s{8}<owl:annotatedSource rdf:resource=\".*#(.*)"\/>\s*/ ) {
      my $className = $1;
      while( $owlLines[$i] !~ /^\s{4}<\/owl:Axiom>$/ ) {
        $i++;
      }
      $end = $i;
      if( !exists($classMap{$className}) ) {
        print $debug "Removing axiom for $className\n";
        my $j = $begin;
        $owlLines[$j] = q{};
        while( ++$j <= $end ) {
          $owlLines[$j] = q{};
        }
      }
    }
  }
}

print "Removing disjointWith statements based on class file...\n";
for( $i=0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /\s*<owl:disjointWith rdf:resource=".*#(.*)"\/>\s*/ ) {
    my $value = $1;
    if( !exists($classMap{$value}) ) {
       print $debug "Removing disjointWith value $value\n";
      $owlLines[$i] = q{};
    }
  }
}

##TODO - add below
print "Checking for unwanted restrictions within role groups...\n";
for( $i=0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /\s{4}<owl:Class rdf:about=\".*#(.*)">\s*/ ) {
    my $hasRG = "false";
      while( $owlLines[$i] !~ /^\s{4}<\/owl:Class>$/ ) {
        if( $owlLines[$i] =~ /\s*<owl:unionOf\s*/ ) {
          $hasRG = "true";
        }
        if( $owlLines[$i] =~ /\s*<owl:someValuesFrom rdf:resource=".*#(.*)"\/>/ ) {
          my $value = $1;
          if( !exists($classMap{$value}) ) {
            if( $hasRG eq "true" ) {
              print $debug "RG dissection possibly needed at line $i\n";
            }
          }
        }
        $i++
      }
  }
}


if( $stripFlag eq "i" ) {
  print "Removing unwanted restrictions, applying simple restrictions, adding subClassOfs...\n";
  for( $i=0; $i < $owlLineCount; $i++ ) {
    if( $owlLines[$i] =~ /(\s*)<owl:Class rdf:about=\".*#(.*)">\s*/ ) {
      while($owlLines[$i] !~ /.*<\/rdfs:label>.*/) {
        $i++;
      }
      my $pointOfSubClassInsertion = $i;
      my $pointOfRestrictionInsertion = $i;
      my $space = $1;
      my $className = $2;
      my %allRestrictionMap;
      my %someRestrictionMap;
      my %hasRestrictionMap;
      my @superclasses;
      my $superRemoval = "false";
      while( $owlLines[$i] !~ /$space<\/owl:Class>\s*/ ) {
        if( $owlLines[$i] =~ /\s*<rdfs:subClassOf rdf:resource=".*#(.*)"\/>\s*/ ) {
          my $value = $1;
          if( !exists($classMap{$value}) ) {
            print $debug "Removing subclass axiom with value $value in class $className at line $i\n";
            $owlLines[$i] = q{};
          }
        }
        if( $owlLines[$i] =~ /(\s*)(<owl:equivalentClass>|<rdfs:subClassOf>)\s*/ ) {
          my $inSpace = $1;
          my $type = $2;
          my $removal = "false";
          my $begin = $i;
          my $end;
          $i++;
            while( $owlLines[$i] !~ /$inSpace(<\/owl:equivalentClass>|<\/rdfs:subClassOf>)\s*/ ) {
              if( $owlLines[$i] =~ /\s*<rdf:Description rdf:about=".*#(.*)"\/>\s*/ ) {
                my $superclass = $1;
                if( exists($classMap{$superclass}) ) {
                    push @superclasses, $superclass;
                }
                else {
                  $removal = "true";
                  $superRemoval = "true";
                }
              }
              if( $owlLines[$i] =~ /\s*<owl:Restriction>\s*/ ) {
                $i++;
                if( $owlLines[$i] =~ /\s*<owl:onProperty rdf:resource=".*#(.*)"\/>\s*/ ) {
                  my $restriction = $1;
                  my $value;
                  $i++;
                  ##TODO - Cardinality!
                  if( $owlLines[$i] =~ /\s*<owl:(allValuesFrom|someValuesFrom|hasValue) rdf:resource=".*#(.*)"\/>/ ) {
                    my $qualifiedType = $1;
                    $value = $2;
                    if( !exists($classMap{$value}) ) {
                      print $debug "Removing restriction $restriction with value $value in class $className at line $i\n";
                      $removal = "true";
                      $superRemoval = "true";
                    }
                    else {
                      if( $type =~ /<owl:equivalentClass>/ ) {
                        if( $qualifiedType eq "allValuesFrom" ) {
                            push @{$allRestrictionMap{$restriction}}, $value;
                        }
                        if( $qualifiedType eq "someValuesFrom" ) {
                            push @{$someRestrictionMap{$restriction}}, $value;
                        }
                        if( $qualifiedType eq "hasValue" ) {
                            push @{$hasRestrictionMap{$restriction}}, $value;
                        }
                      }
                      else {
                        ##do nothing, it's a plain restriction to keep
                      }
                    }
                    $i++;
                  }
                }
              }
              $i++;
            }
          $end = $i;
          if( $removal eq "true" ) {
            my $j = $begin;
            while( $j <= $end ) {
              $owlLines[$j] = q{};
              $j++;
            }
          }
        }
        $i++;
      }
      if( $superRemoval eq "true" ) {
        foreach(@superclasses) {
          $owlLines[$pointOfSubClassInsertion] .= "        <rdfs:subClassOf rdf:resource=\"$ncitns#$_\"/>\n";
        }
        my $size = keys %someRestrictionMap;
        if( $size > 0 ) {
          for my $restrictionKey ( sort keys %someRestrictionMap ) {
            foreach( @{$someRestrictionMap{$restrictionKey}} ) {
              my $value = $_;
              $owlLines[$pointOfRestrictionInsertion] .= "        <rdfs:subClassOf>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            <owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:onProperty rdf:resource=\"$ncitns#$restrictionKey\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:someValuesFrom rdf:resource=\"$ncitns#$value\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            </owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "        </rdfs:subClassOf>\n";
            }
          }
        }
        $size = keys %allRestrictionMap;
        if( $size > 0 ) {
          for my $restrictionKey ( sort keys %allRestrictionMap ) {
            foreach( @{$allRestrictionMap{$restrictionKey}} ) {
              my $value = $_;
              $owlLines[$pointOfRestrictionInsertion] .= "        <rdfs:subClassOf>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            <owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:onProperty rdf:resource=\"$ncitns#$restrictionKey\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:allValuesFrom rdf:resource=\"$ncitns#$value\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            </owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "        </rdfs:subClassOf>\n";
            }
          }
        }
        $size = keys %hasRestrictionMap;
        if( $size > 0 ) {
          for my $restrictionKey ( sort keys %hasRestrictionMap ) {
            foreach( @{$hasRestrictionMap{$restrictionKey}} ) {
              my $value = $_;
              $owlLines[$pointOfRestrictionInsertion] .= "        <rdfs:subClassOf>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            <owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:onProperty rdf:resource=\"$ncitns#$restrictionKey\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "                <owl:hasValue rdf:resource=\"$ncitns#$value\"\/>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "            </owl:Restriction>\n";
              $owlLines[$pointOfRestrictionInsertion] .= "        </rdfs:subClassOf>\n";
            }
          }
        }
      }
    }
  }
}

elsif( $stripFlag eq "s" ) {
    print "Removing all restrictions, preserving rdf:Descriptions as subClassOfs...\n";
    for( $i=0; $i < $owlLineCount; $i++ ) {
      if( $owlLines[$i] =~ /(\s*)<owl:Class rdf:about=\".*#(.*)">\s*/ ) {
        while($owlLines[$i] !~ /.*<\/rdfs:label>.*/) {
          $i++;
        }
        my $pointOfSubClassInsertion = $i+1;
        my $space = $1;
        my $className = $2;
        my @superclasses;
        while( $owlLines[$i] !~ /$space<\/owl:Class>\s*/ ) {
          if( $owlLines[$i] =~ /\s*<rdfs:subClassOf rdf:resource=".*#(.*)"\/>\s*/ ) {
            my $value = $1;
            if( !exists($classMap{$value}) ) {
              print $debug "Removing subclass axiom with value $value in class $className at line $i\n";
              $owlLines[$i] = q{};
            }
          }
          if( $owlLines[$i] =~ /(\s*)(<owl:equivalentClass>)\s*/ ) {
            my $inSpace = $1;
            my $type = $2;
            my $removal = "false";
            my $begin = $i;
            my $end;
            $i++;
            ## save only the subclasses
            while( $owlLines[$i] !~ /$inSpace(<\/owl:equivalentClass>)\s*/ ) {
              if( $owlLines[$i] =~ /\s*<rdf:Description rdf:about=".*#(.*)"\/>\s*/ ) {
                my $superclass = $1;
                if( exists($classMap{$superclass}) ) {
                    push @superclasses, $superclass;
                }
              }
              $i++;
            }
            $end = $i;
            my $j = $begin;
            while( $j <= $end ) {
              $owlLines[$j] = q{};
              $j++;
            }
          }
          ## erase the restrictions
          elsif( $owlLines[$i] =~ /(\s*)(<rdfs:subClassOf>)\s*/ ) {
            my $inSpace = $1;
            while( $owlLines[$i] !~ /$inSpace(<\/rdfs:subClassOf>)\s*/ ) {
              $owlLines[$i] = q{};
              $i++;
            }
            $owlLines[$i] = q{};
          }
          $i++;
        }
        foreach(@superclasses) {
          $owlLines[$pointOfSubClassInsertion] .= "        <rdfs:subClassOf rdf:resource=\"$ncitns#$_\"/>\n";
        }
      }
    }
}
elsif( $stripFlag eq "k" ) {
  ##nada
}

for( $i=0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /<\/rdf:RDF>.*/ ) {
    my $loc = $i;
    $loc--;
    while($owlLines[$loc] !~ /.*<\/owl:Class>.*/ ) {
      $owlLines[$loc] = q{};
      $loc--;
    }
  }
}

open my $out, '>', $filename or die "Couldn't create output file!\n";
print "Output to file $filename\n";
foreach(@owlLines) {
  print $out $_;
}

my $end = time();
my $runtime = ($end - $start);
print "Finished in $runtime seconds.\n";
