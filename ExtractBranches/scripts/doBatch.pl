#!/usr/bin/perl
# Rob Wynne, LMCO
# 2012
# Create multiple branches from NCIt based on a config file.
# Modify system calls as necessary.
# For now, this needs to be run on a *nix or possibly Mac OS
# According to CBIIT tech stack, this script utilizes Apache ANT


use strict;
use warnings;

open( my $input, $ARGV[0]) or die "Couldn't open batch branch file!\n Run as:\n > ./doBatch.pl ExtractThese.txt\n";

my @branches = <$input>;
foreach my $branch (@branches) {
  chomp $branch;
  print "Configuring ExtractBranches for $branch branch.\n";
# You can edit what's below
  system("rm config/extractbranches.properties");
  system("echo namespace=http\://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl >> config/extractbranches.properties");
  system("echo branchesFile=config/ExtractBranches.txt >> config/extractbranches.properties");
  system("echo fileURI=file\:///h1/wynner/ExtractBranches_owl2/ThesaurusInf-120827-12.08d.owl-forProduction.owl-fixedNS.owl >> config/extractbranches.properties");
  system("echo saveURI=file\:///h1/wynner/ExtractBranches_owl2/$branch.owl >> config/extractbranches.properties");
  system("echo oneByOne=false >> config/extractbranches.properties");
# Do not edit below here

  system("rm config/ExtractBranches.txt");
  system("echo $branch >> config/ExtractBranches.txt");

  print "Running ExtractBranches.\n";
  my @info = `ant run`;

  foreach my $i (@info) {
      if( $i =~ /.*Total time:.*/ ) {
        my $filename = "$branch.owl";
	
	`./removeDuplicates.pl $filename`;
	print "Duplicate restrictions removed on $filename\n";
	$filename .= "-removedDuplicates.owl";	

        print "Intermediate file $filename saved.\n";
        `./formatBranch.pl -i $filename BranchList.txt`;
        print "Inward raw saved.\n";

        `./formatBranch.pl -s $filename BranchList.txt`;
        print "Stripped saved.\n";

        `./formatBranch.pl -k $filename BranchList.txt`;
        print "Kept saved.\n";
      }
  }
}

print "All branches output.\n";
