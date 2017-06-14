#!/usr/bin/perl
#
# Script:    coundDb.pl
# Author:    Joanne Wong (joanne.f.wong@lmco.com)
#
# Remarks:   This script is used to generate counts from the MySQL database.
#            It outputs the following to STDOUT:
#            test_name|test_value[,test_value2..n]|test_count|
#
# Version Information:
#   01/22/2010 JFW (): Add batch sampling capability (-f [filename])
#   01/07/2010 JFW (): Add sampling capability (-s "tallyname:tallyvalue")
#   10/28/2009 JFW (): Update for LexBIG 5.1
#   09/16/2008 JFW (): First version
#
use DBI();
use strict 'vars';
use strict 'subs';

#
# Handle Options
#
our @ARGS;
our $db_opt;
our $file_opt = "";
our $count_opt = "Count"; # can be "Count" or "Sample"
our $source_opt="";
our $sample_opt=0;
our $verbose = 0;

while (@ARGV) {
    my $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);
    }
    if ($arg =~ /^-d/) {
      $db_opt = shift(@ARGV);
    }
    elsif ($arg =~ /^-h/) {
      $source_opt = shift(@ARGV);;
    }
    elsif ($arg =~ /^-f/) {
      $file_opt = shift(@ARGV);
      $count_opt = "Sample";
    }
    elsif ($arg =~ /^-s/) {
      $count_opt = "Sample";
      $sample_opt = shift(@ARGV);
    }
    elsif ($arg =~ /^-v/) {
        $verbose = 1;
    }
}

#
# Initial variables and filehandles via config files
#

my $queryfile = "$ENV{QA_HOME}/etc/db".$count_opt."Queries".$source_opt.".dat";
my $propfile = "$ENV{QA_HOME}/etc/db.prop";

open PROP, "$propfile" or die "Cannot open database properties file: $propfile";

# Get database parameters
while(<PROP>) {
  chomp;
  my ($param,$value) = split(/=/,$_);
  next unless ($param =~ "^MYSQL" || $param =~ "^LEXBIG");
  ${$param} = $value;
}

our ($MYSQL_DB, $MYSQL_HOST, $MYSQL_PORT, $MYSQL_USER, $MYSQL_PWD, $MYSQL_PREFIX, $LEXBIG_PREFIX);

# Override default DB if option is selected.
if ($db_opt) {
  $MYSQL_PREFIX = $db_opt;
}

# Connect to the database.
my $dbh = DBI->connect("DBI:mysql:database=$MYSQL_DB;host=$MYSQL_HOST;port=$MYSQL_PORT;mysql_use_result=1",
                       "$MYSQL_USER", "$MYSQL_PWD",
                       {'RaiseError' => 1});

#
# Main procedure
#

# Get database queries and print out results.
if ($file_opt) {
    open EXT, "$file_opt" or die "Cannot open file $file_opt";

    while (<EXT>) {
        chomp;
        my ($sample_query_name, $sample_fields) = split(/:/,$_);
        open IN, "$queryfile" or die "Cannot open input file: $queryfile\n";
        while(<IN>) {
            chomp;
	    my ($query_name,$query_type,$query) = split(/\|/,$_);
	    if ($sample_query_name eq $query_name) {
	        &PrintSampleSet($query_name,$query,$sample_fields) unless $query eq "";
	    }
	    else {
	        next;
	    }           
        }
        close IN;
    }
    close EXT;
}

else {
    open IN, "$queryfile" or die "Cannot open input file: $queryfile\n";
    while(<IN>) {
        chomp;
        my ($query_name,$query_type,$query) = split(/\|/,$_);
        if($sample_opt || $file_opt) {
  	    my ($sample_query_name, $sample_fields) = split(/:/,$sample_opt);
	    if ($sample_query_name eq $query_name) {
                &PrintSampleSet($query_name,$query,$sample_fields) unless $query eq "";
	    }
	    else {
	        next;
	    }
        }
        else {
	    &PrintResultSet($query_name,$query_type,$query) unless $query eq ""; # skip placeholder queries
        }
    }
    close IN;
}


$dbh->disconnect();



####### Local Procedures #######

sub PrintResultSet {
    my ($query_name, $query_type, $query) = @_;
   
#    print "query to prepare is $query\n";
#    print eval(qq("$query"));
#    print "\n";
    
    my $sth=$dbh->prepare(eval(qq("$query"))) || die $DBI::err.": ".$DBI::errstr;

    $| = 1; # autoflush buffers

    $sth->execute || die DBI::err.": ".$DBI::errstr; 

    do {
	my @row = ();
	while (@row= $sth->fetchrow_array())  {
        if($verbose) {
        	print scalar(localtime)."     ";
        }

        # print the name of this check and relevant table fields
	    print "$query_name|";
	    my $field;
	    my $count;
	    foreach $field (0..$#row) {
		if($field == 0) {
		    $count = $row[$field]; 
		}
		elsif ($field == $#row) {
		    print $row[$field]."|";
		}
		else {
		    print $row[$field].","; 
		}
	    }
        
        # for queries which return counts but not field-based tallies, add extra |
        # otherwise, append count to end of line, as well as $query_type
	    if ($#row == 0) {
		print "|$count|\n"; 
	    }
	    else {
		print "$count|\n"; #print count
	    }


	}
    } until (!$sth->more_results);

    $sth->finish();

    return 1;
}



sub PrintSampleSet {
  my ($query_name, $query, $sample_fields) = @_;
  my @sample_fields = split(/,/,$sample_fields); # split sample options into an array

  my $sth=$dbh->prepare(eval(qq("$query"))) || die $DBI::err.": ".$DBI::errstr;
  $sth->execute(@sample_fields) || die DBI::err.": ".$DBI::errstr;
    
  do {
     my @row = ();
    while (@row= $sth->fetchrow_array())  {
	if($verbose) {
	    print scalar(localtime)."     ";
	}

        # print the name of this check and relevant table fields
	print "$query_name|$sample_fields|";
	my $field;
	foreach $field (0..$#row) {
	    print $row[$field]."|";
         }
         print "\n";         
     }
   
   } until (!$sth->more_results);

    $sth->finish();
    return 1;
}


########################################################################################
# Help & Usage Procedures
#######################################################################################

sub PrintHelp {

    PrintUsage();
    print qq{
 Options:
       -[-]help    : On-line help
    };
}

sub PrintUsage {
    print qq{This script has the following usage:

    dbCount.pl [-d database] [-s sample:spec] [-f sample_file] [-h source-specific extension]
    
    When run with no parameters, it will run simple counts on the database specified in \$QA_HOME/etc/db.conf.

    It requires the presence of two configuration files:
    
    \$QA_HOME/etc/dbCountQueries.dat
    \$QA_HOME/etc/db.prop
    };
}
