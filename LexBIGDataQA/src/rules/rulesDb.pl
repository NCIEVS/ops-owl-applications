#!/usr/bin/perl
#
# Script:    rulesDb.pl
# Author:    Joanne Wong (joanne.f.wong@lmco.com)
#
# Remarks:   This script is used to check rules in the MySQL database.
#            It outputs the following to STDOUT:
#            test_name|test_value[,test_value2..n]|test_count|
#
# Version Information:
#   11/21/2008 JFW (): First version
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
our $verbose = 0;
while (@ARGV) {
    my $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-help" || $arg eq "--help") {
        PrintHelp() && exit(0);
    }
    elsif ($arg =~ /^-d/) {
      $db_opt = shift(@ARGV);
    }
    elsif ($arg =~ /^-f/) {
      $file_opt = shift(@ARGV);
    }
    elsif ($arg =~ /^-v/) {
	$verbose = 1;
    }
}

#
# Initial variables and filehandles via config files
#

my $queryfile = "$ENV{QA_HOME}/etc/dbRulesQueries".$file_opt.".dat";
my $propfile = "$ENV{QA_HOME}/etc/db.prop";

open IN, "$queryfile" or die "Cannot open input file: $queryfile\n";
open PROP, "$propfile" or die "Cannot open database properties file: $propfile";

# Get database parameters
while(<PROP>) {
  chomp;
  my ($param,$value) = split(/=/,$_);
  next unless ($param =~ "MYSQL");
  ${$param} = $value;
}

our ($MYSQL_DB, $MYSQL_HOST, $MYSQL_PORT, $MYSQL_USER, $MYSQL_PWD, $MYSQL_PREFIX);

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
while(<IN>) {
  chomp;
  my ($query_name,$query_type,$query) = split(/\|/,$_);
  &PrintResultSet($query_name,$query_type,$query) unless $query eq ""; # skip placeholder queries
}

# Disconnect from the database.
$dbh->disconnect();
close IN;


####### Local Procedures #######

sub PrintResultSet ($query_name,$query_type,$query) {
  my ($query_name, $query_type, $query) = @_;
  my $sth=$dbh->prepare(eval(qq("$query"))) || die $DBI::err.": ".$DBI::errstr;

  $| = 1; # autoflush buffers

  if (my $rows = $sth->execute) {    
       if($verbose) {
         print scalar(localtime)."     ";
       }
       if ($rows==0) {
           print "$query_name|$query_type|pass||\n";
       }
       else {
             do {
	       while (my $row= $sth->fetchrow_array())  {
	         print "$query_name|$query_type|fail|$row|\n";
	       }
	   } until (!$sth->more_results);
       }
  }
  else {
     die DBI::err.": ".$DBI::errstr; 
  }
  $sth->finish;

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

    rulesDb.pl [-d database] [-f Owl|Obo|OwlGO|OboGO]
    
    It requires the presence of two configuration files:
    
    \$QA_HOME/etc/dbRulesQueries.dat
    \$QA_HOME/etc/db.prop
    };
}
