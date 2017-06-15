#!/usr/bin/perl
## Rob Wynne, MSC
##
## Remove duplicate restrictions from the
## inferred OWL file that has been processed
## by the OWL API.

use strict;
use URI;
my $url=URI->new($ARGV[0]);
print $url->path();
open (my $input, $url->path()) or die "Couldn't open OWL file";

my $start = time();

## open( my $input, $ARGV[0]) or die "Couldn't open owl file!\n";
open my $debug, '>', "debugDuplicates.txt" or die "Coudln't create debug file!\n";

my $i=0;
my @owlLines = <$input>;

## In every owl:Class, the equivalentClass comes before
## the simple restrictions.
for( $i = 0; $i < @owlLines; $i++ ) {
  if( $owlLines[$i] =~ /(\s*)<owl:Class rdf:about="#(.*)">.*/ ) {
    my $space = $1;
    my $className = $2;
    $i++;
    my %someRestrictionMap;
    my %allRestrictionMap;
    my %hasRestrictionMap;
#    my $eqCount = 0;  No class has more than 1 equivalent class.
    while( $owlLines[$i] !~ /$space<\/owl:Class>.*/ ) {
      if( $owlLines[$i] =~ /(\s*)<owl:equivalentClass>.*/ ) {
#        $eqCount++;
        my $inSpace = $1;
        while( $owlLines[$i] !~ /$inSpace<\/owl:equivalentClass>.*/ ) {
          if( $owlLines[$i] =~ /(\s*)<owl:Restriction>.*/ ) {
            my $resSpace = $1;
            my $restrictionName;
            my $restrictionType;
            my $restrictionValue;
            while($owlLines[$i] !~ /$resSpace<\/owl:Restriction>.*/ ) {
              if( $owlLines[$i] =~ /\s*<owl:onProperty rdf:resource="#(.*)"\/>.*/ ) {
                $restrictionName = $1;
              }
              if( $owlLines[$i] =~ /\s*<owl:(allValuesFrom|someValuesFrom|hasValue) rdf:resource="#(.*)"\/>.*/ ) {
                $restrictionType = $1;
                $restrictionValue = $2;
              }
              $i++;
            }
            if( $restrictionType eq "someValuesFrom" ) {
              push @{$someRestrictionMap{$restrictionName}}, $restrictionValue;
            }
            elsif( $restrictionType eq "allValuesFrom" ) {
              push @{$allRestrictionMap{$restrictionName}}, $restrictionValue;
            }
            elsif( $restrictionType eq "hasValue" ) {
              push @{$hasRestrictionMap{$restrictionName}}, $restrictionValue;
            }
          }
          $i++;
        }
      }
      if( $owlLines[$i] =~ /(\s*)<rdfs:subClassOf>.*/ ) {
        my $inSpace = $1;
        my $remove = "false";
        my $beginning = $i;
        my $end;
        my $restrictionName;
        my $restrictionType;
        my $restrictionValue;

        while( $owlLines[$i] !~ /$inSpace<\/rdfs:subClassOf>.*/ ) {
          if( $owlLines[$i] =~ /\s*<owl:onProperty rdf:resource="#(.*)"\/>.*/ ) {
            $restrictionName = $1;
          }
          if( $owlLines[$i] =~ /\s*<owl:(allValuesFrom|someValuesFrom|hasValue) rdf:resource="#(.*)"\/>.*/ ) {
            $restrictionType = $1;
            $restrictionValue = $2;
          }
          $i++
        }
        
        $end = $i;

        if( $restrictionType eq "someValuesFrom" ) {
          if( exists($someRestrictionMap{$restrictionName}) ) {
            foreach( @{$someRestrictionMap{$restrictionName}} ) {
              my $value = $_;
              if( $restrictionValue eq $value ) {
                print $debug "$className: Removing $restrictionName -> $value\n";
                $remove = "true"
              }
            }
          }
        }
        elsif( $restrictionType eq "allValuesFrom" ) {
          if( exists($allRestrictionMap{$restrictionName}) ) {
            foreach( @{$allRestrictionMap{$restrictionName}} ) {
              my $value = $_;
              if( $restrictionValue eq $value ) {
                print $debug "$className: Removing $restrictionName -> $value\n";
                $remove = "true"
              }
            }
          }
        }
        elsif( $restrictionType eq "hasValue" ) {
          if( exists($hasRestrictionMap{$restrictionName}) ) {
            foreach( @{$hasRestrictionMap{$restrictionName}} ) {
              my $value = $_;
              if( $restrictionValue eq $value ) {
                print $debug "$className: Removing $restrictionName -> $value\n";
                $remove = "true"
              }
            }
          }
        }
        
        if( $remove eq "true" ) {
          for( my $j = $beginning; $j <= $end; $j++ ) {
            $owlLines[$j] = q{};
          }
        }
      }
      $i++;
    }
  }
}

open my $out, '>', $url->path."-removedDuplicates.owl" or die "Couldn't create output file!\n";
foreach(@owlLines) {
  print $out $_;
}

my $end = time();
my $runtime = ($end - $start);
print "Finished in $runtime seconds.\n";
