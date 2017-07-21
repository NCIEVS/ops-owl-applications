#!/usr/bin/perl
## Rob Wynne, MSC
## Remove rdf:datatype from exported OWL file

use strict;
open( my $owlfile, $ARGV[0] ) or die "Couldn't open OWL file!\n";
open my $out, '>', $ARGV[0]."-removedDatatypes.owl" or die "Couldn't create output file!\n";

my @owlLines = <$owlfile>;
my $owlLineCount = @owlLines;
my $i;

for( $i = 0; $i < $owlLineCount; $i++ ) {
  if($owlLines[$i] =~ /.*rdf:datatype.*/) {
    $owlLines[$i] =~ s/\srdf:datatype="http:\/\/www\.w3\.org\/2001\/XMLSchema#string\"//;
  }
}

print "Printing output...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $out $owlLines[$i];
}
