#!/usr/bin/perl
#
# Script:    diffReport.pl
# Author:    Brian Carlsen
#
# Remarks:   This script is used to generate diffs between
#            two QA tables with the following fields
#                test_name,test_value,test_count,adj_count,adj_authority,adj_reason
#            The last three fields may be blank or missing if
#            there is no adjustment
#
# Version Information:
#   10/06/2011 BAC (): make qaModelType-aware
#   01/21/2010 BAC (): Better collation
#   09/09/2008 BAC (): First version
#   01/08/2013 JFW (): Strip CR (\r) line endings from input files
#   01/11/2013 JFW (): Modify output column sizes to %20s%80s%10s%10s%10s
#
use strict 'vars';
use strict 'subs';

#
# Handle Options
#
our @ARGS;
our $type = ".*";
while (@ARGV) {
    my $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);

    } elsif ($arg eq "-t" ) {
		$type = shift(@ARGV);
    }
}

#
# Handle Params
#
our $qa1;
our $qa2;
if (scalar(@ARGS) == 2) {
    ($qa1, $qa2) = @ARGS;
} else {
    print "Incorrect number of arguments :".(scalar(@ARGS))."\n";
    PrintUsage();
    exit(1);
}

#
# Cache data from $qa1
#
our %map1 = ();
our %adj1 = ();
our $F;
open ($F,"$qa1") || die "Could not open $qa1: $? $!\n";
while (<$F>) {
    chomp; s/\r$//;
    my ($name, $val, $ct, $adj_ct, $adj_auth, $reason) = split /\|/;
	if ($val =~ /$type$/i) {
      ${$map1{$name}}{$val} = "$ct";
      if ($adj_ct) {
          ${$adj1{$name}}{$val} = {"auth" => $adj_auth, "reason" => $reason, "count" => $adj_ct};
      }
    }
}
close($F);

#
# Cache data from $qa2
#
our %map2 = ();
our %adj2 = ();
open ($F,"$qa2") || die "Could not open $qa2: $? $!\n";
while (<$F>) {
    chomp; s/\r$//;
    my ($name, $val, $ct, $adj_ct, $adj_auth, $reason) = split /\|/;
	if ($val =~ /$type$/i) {
      ${$map2{$name}}{$val} = "$ct";
      if ($adj_ct) {
          ${$adj2{$name}}{$val} = {"auth" => $adj_auth, "reason" => $reason, "count" => $adj_ct};
      }
    }
}
close($F);

#
# Declare variables used for comparisons
#
our $ignorePattern = "(NOT LOADED|IGNORE|INFO ONLY)";
our %ignore = ();
our %diffmap = ();
our $key1 = "";
our $key2 = "";

#
# Add blanks to missing map2 entries
#
foreach $key1 (sort keys %map1) {
    foreach $key2 (sort keys %{$map1{$key1}}) {
        if (${$adj1{$key1}}{$key2}->{"reason"} =~ /$ignorePattern/) {
            unshift @{$ignore{$key1}}, $key2;
        } elsif (! defined ${$map2{$key1}}{$key2}) {
             ${$map2{$key1}}{$key2} = "";
             #  unshift @{$diffmap{$key1}}, $key2;
        }
    }
}

#
# Add blanks to missing map1 entries
#
foreach $key1 (sort keys %map2) {
    foreach $key2 (sort keys %{$map2{$key1}}) {
        if (${$adj2{$key1}}{$key2}->{"reason"} =~ /$ignorePattern/) {
            unshift @{$ignore{$key1}}, $key2;
        } elsif (! defined ${$map1{$key1}}{$key2}) {
            ${$map1{$key1}}{$key2} = "";
            #unshift @{$diffmap{$key1}}, $key2;
        }
    }
}


#
# Report comparisons between shared keys, account for adjustments
#
print "\n\n";
foreach $key1 (sort keys %map1) {
    foreach $key2 (sort keys %{$map1{$key1}}) {
        # To show ONLY differences, include this clause in the following "if" statement
        # && ${$map1{$key1}}{$key2} != ${$map2{$key1}}{$key2}) {
        if (defined ${$map2{$key1}}{$key2}) {
            unshift @{$diffmap{$key1}}, $key2;
        }
    }
}

#
# Report Results
#
if (scalar(%diffmap) == 0) {
    print "    There are no diffs between $qa1 and $qa2 where keys match\n";
} else {
    print "     Comparison between $qa1 and $qa2\n\n";

    printf "    %20s%80s%10s%10s%10s\n", ("Test","Value","#1","#2","#1-#2");
    printf "    %20s%80s%10s%10s%10s\n", ("-------------------",
					  "--------------------------------------------------------------------------",
					  "---------","---------","---------");
    foreach $key1 (sort keys %diffmap) {
        foreach $key2 (sort @{$diffmap{$key1}}) {

            # Ignore things with the ignore pattern
            next if ${$adj1{$key1}}{$key2}->{"reason"} =~ /$ignorePattern/;
            next if ${$adj2{$key1}}{$key2}->{"reason"} =~ /$ignorePattern/;
            # Get first count and adjustments
            my $x = ${$map1{$key1}}{$key2};
            my $xadj = ${$adj1{$key1}}{$key2}->{"count"};
            my $xsign = ($xadj > 0 ? "+":"");
            # Get second count and adjustments
            my $y = ${$map2{$key1}}{$key2};
            my $yadj = ${$adj2{$key1}}{$key2}->{"count"};
            my $ysign = ($yadj > 0 ? "+":"");
            # Get differences (including adjusted difference)
            my $z = $x - $y;
            my $zadj = ($x+$xadj) - ($y+$yadj);
            # Set display values
            if ($xadj) {
                $x = "$x$xsign$xadj";
            }
            if ($yadj) {
                $y = "$y$ysign$yadj";
            }
            printf "    %20s%80s%10s%10s%10s\n", ($key1, $key2, $x, $y, $zadj);
            # Show adjustments to first count
            if ($xadj != 0) {
                my $xauth = ${$adj1{$key1}}{$key2}->{"auth"};
                my $xreason = ${$adj1{$key1}}{$key2}->{"reason"};
                print "       **$qa1 adjusted by $xsign$xadj ($xauth): $xreason\n";
            }
            # Show adjustments to second count
            if ($yadj != 0) {
                my $yauth = ${$adj2{$key1}}{$key2}->{"auth"};
                my $yreason = ${$adj2{$key1}}{$key2}->{"reason"};
                print "       **$qa2 adjusted by $ysign$yadj ($yauth): $yreason\n";
            }
        }
    }
}

if (%ignore) {
    print "\n\n    The following entries from $qa1 are explicitly ignored\n\n";
    foreach $key1 (sort keys %ignore) {
        foreach $key2 (sort @{$ignore{$key1}}) {
            my $x = ${$map1{$key1}}{$key2};
            if ($x) {
                printf "    %20s%80s%10s%20s\n",($key1, $key2, $x, ${$adj1{$key1}}{$key2}->{"reason"});
            }
        }
    }
}



if (%ignore) {
    print "\n    The following entries from $qa2 are explicitly ignored\n\n";
    foreach $key1 (sort keys %ignore) {
        foreach $key2 (sort @{$ignore{$key1}}) {
            my $x = ${$map2{$key1}}{$key2};
            if ($x) {
                printf "    %20s%80s%10s%20s\n",($key1, $key2, $x, ${$adj2{$key1}}{$key2}->{"reason"});
            }
        }
    }
}

exit(0);

########################################################################################
# Help & Usage Procedures
#######################################################################################

sub PrintHelp {

    PrintUsage();
    print qq{
 <qa table 1>      : a name|value|count|adj_ct|adj_auth|adj_reason formatted table
 <qa table 2>      : a name|value|count|adj_ct|adj_auth|adj_reason formatted table

 Output is written as a text report.  This can easily be restructured to output
 either wiki formatted text, full HTML, or XML.

 Options:
       -t <type>   : qaModelType: content, metadata, anonymous
       -v[ersion]  : Print version information.
       -[-]help    : On-line help
};
}

sub PrintUsage {
    print qq{This script has the following usage:

    diffReport.pl [-t <content|anonymous|metadata>] <qa table 1> <qa table 2>
};
}
