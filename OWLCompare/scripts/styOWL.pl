# Rob Wynne, MSC
# Generate an OWL file based on
# classes with specific Semantic Type(s)

use strict;
open( my $input, $ARGV[0]) or die "Couldn't open owl file!\n";
open my $debug, '>', "debug.txt" or die "Coudln't create debug file!\n";
my $filename = $ARGV[1];
my $i=0;
my @owlLines = <$input>;
my $owlLineCount = @owlLines;

print "Removing classes outside the branch...\n";
for( $i=0; $i < $owlLineCount; $i++ ) {
  my $begin;
  my $end;
  my $space;
  if( $owlLines[$i] =~ /\s\s\s\s<owl:(Class|DeprecatedClass) rdf:about=\"#(.*)">\s*/ ) {
      my $classType = $1;
      my $className = $2;
      my $keep = "false";
      $begin = $i-5; ## remove the comment and newlines, too
      while( $owlLines[$i] !~ /^\s\s\s\s<\/owl:$classType>$/ ) {
        if( $owlLines[$i] =~ /.*>(Cell or Molecular Dysfunction|Disease or Syndrome|Finding|Mental or Behavioral Dysfunction|Neoplastic Process|Pathologic Function)<\/Semantic_Type>.*/ ) {
            $keep = "true"
        }
        $i++;
      }
      $end = $i;
      if( $keep eq "false" ) {
          print $debug "Removing $className\n";
          my $j = $begin;
          $owlLines[$j] = q{};
          while( ++$j <= $end ) {
            $owlLines[$j] = q{};
          }
      }
  }
}

open my $out, '>', $filename or die "Couldn't create output file!\n";
print "Output to file $filename\n";
foreach(@owlLines) {
  print $out $_;
}