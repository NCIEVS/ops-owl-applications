#!/usr/bin/perl
#
# Script:    countApi.pl
# Author:  Tun Tun Naing
#
# Remarks:   This script is used to generate QA counts using LexBIG API.
#
# Version Information:
#   09/17/2008 TTN (): First version

use strict;

#
# Handle Options
#
our @ARGS;
while (@ARGV) {
    my $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);
    }
}

#
# Set Defaults & Environment
#
unless ($ENV{LEXBIG_HOME}) {
    &PrintUsage;
    print "\nLEXBIG_HOME must be set\n";
    exit(1);
}

#
# Set java stuff
#
our $java = "$ENV{JAVA_HOME}/bin/java -server -Xms200M -Xmx2000M ";
our $class = "org.LexGrid.LexBig.dataQA.GenerateQA";

#
# edit classpath
#

#
# load lib/ jar files
#
opendir (LIB,"$ENV{LEXBIG_HOME}/runtime") || 
  die "Could not open $ENV{LEXBIG_HOME}/runtime: $! $?\n";
our @f = readdir(LIB);
close(LIB);
foreach my $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{LEXBIG_HOME}/runtime/$file"; }
}

opendir (LIB,"$ENV{QA_HOME}/lib") || 
  die "Could not open $ENV{QA_HOME}/lib: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach my $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{QA_HOME}/lib/$file"; }
}
system ("$java $class");


############################################################################
# Help & Usage Procedures
############################################################################

sub PrintHelp {

    PrintUsage();
    print qq{

 Output is written as a text report.  

 Options:
       -v[ersion]  : Print version information.
       -[-]help    : On-line help
};
}

sub PrintUsage {
    print qq{This script has the following usage:

    countApi.pl
};
}

