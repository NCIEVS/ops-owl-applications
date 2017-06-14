#!/usr/bin/perl
#
# Script:  countRrf.pl
# Author:  Nels Olson, Joanne Wong
#
# Remarks:   This script is used to generate QA counts for a set of RRF files.
#
# Version Information:
#   2013/04/?? JFW (): TODO: Fix relCount/Tally functionality for all sg_types
#   2013/04/17 JFW (): Make samples for relTally correct "order," i.e.
#                      AUI1 -> targetCode and AUI2 -> sourceCode.
#   2013/04/02 JFW (): Add SIBs back to relCounts and relTallies.
#                      Fix bug that counted all Rels as self-referencing.
#                      Add sampling capability for *TypeCount.
#   2013/03/26 JFW (): Fix codeToPreferredAuiMap ranking to actually work.
#   2013/03/25 JFW (): Remove ISPREF from (non-CUI-based) AUI ranking. 
#                      Fix computeLRR to use STT=~/V.+/ instead of STT=V.
#   2013/03/14 JFW (): Fix sampling for nameTally/codeTally.
#   2012/05/11 JFW (): Implement isPref ranking and %auiToRankMap hash.
#                      MRRANK.RRF is now cached and there is sub computeLRR()
#   2012/05/09 JFW (): Fix STY counting to use codeSurrogate; implement
#                      CUI to code map multi-hash.
#   2012/04/27 JFW (): Count/sample UMLS_CUI properties.
#   2012/04/25 JFW (): Count REL as metadata code.
#   2012/04/16 JFW (): Add %codeSeen to uniq codes (RRF is not sorted by code).
#   2012/04/13 JFW (): Remove Meta UI,STYPE,source-related rels and propProps.
#   2012/04/11 JFW (): Remove SRC data for single-source loads.
#   2012/04/05 JFW (): Remove change{Count,Tally} and change 
#                      {rel,prop}{Count,Tally} to use codeSurrogate.
#   2012/04/02 JFW (): Add argument to use code, source_aui, source_cui, or
#                      source_dui as entityCode surrogate. Add AUI map.
#   2012/03/27 JFW (): Branched this file to count single-source RRF only.
#   2010/06/03 NEO (): Update to count all RELs except SIBs (and add back ATUI
#                      counts for definitions, which got inadvertently removed)
#   2010/05/25 NEO (): Update to use $ATN as 1st param for propPropTally and to
#                      make propPropTally entries with 2nd param(type)=source
#   2010/04/07 NEO (): Update to make count spec more closely match DB counts
#   2010/04/01 NEO (): Modify to use incorrect "preferred-rel" algorithm that
#                      hopefully matches what the LexBIG loader actually does
#   2010/03/18 NEO (): Update to keep only the preferred direction of each rel
#   2009/12/21 NEO (): Update to conform to new counts.html spec
#   2009/04/30 NEO (): First version
#
#

use strict 'vars';
use strict 'subs';

#
# Handle Options
#
our @ARGS;
our $verbose = 0;
our $sample = 0;
our $sampleSpec = '';
our $sampleFile = '';

while (@ARGV) {
  my $arg = shift(@ARGV);
  push (@ARGS, $arg) && next unless $arg =~ /^-/;

  if ($arg eq "-help" || $arg eq "--help") {
    PrintHelp() && exit(0);
  }

  if ($arg eq "-s" || $arg eq "--s") {
    $sample = 1;
    $sampleSpec = shift(@ARGV);
  }
  if ($arg eq "-f" || $arg eq "--f") {
    $sample = 2;
    $sampleFile = shift(@ARGV);
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
our $codeType = "";
our $RRFdir = "";
our $codingSchemeName = "";
if (scalar(@ARGS) == 3) {
  ($codeType, $RRFdir, $codingSchemeName) = @ARGS;
} else {
  print "Incorrect number of arguments\n";
  PrintUsage();
  exit(1);
}


our $codeCount= 0;
our %codeTally = ();
our %codeSeen = ();
our $commentCount= 0;
our $defCount = 0;
our %defTally = ();
our $instrCount = 0;
our $nameCount = 0;
our %nameTally = ();
our $propCount = 0;
our %propTally = ();
our $propPropCount = 0;
our %propPropTally = ();
our $propLinkCount = 0;
our %propLinkTally = ();
our $relCount = 0;
our %relTally = ();
our $relDataCount = 0;
our %relDataTally = ();
our $relPropCount = 0;
our %relPropTally = ();
our $relTypeCount = 0;
our $relPropTypeCount = 0;
our $isaRelValue = "";
our $languageTypeCount = 1;
our $propTypeCount = 0;
our $propLinkTypeCount = 0;
our $propPropTypeCount = 0;
our $ttyCount = 0;
our $sourceCount = 0;
#our $changeCount = 0;
#our %changeTally = ();
our %propTypeSeen = ();
our %propPropTypeSeen = ();
our %relTypeSeen = ();
our %relMetadataTypeSeen = ();
our %relPropTypeSeen = ();
our %ttySeen = ();
our %sourceSeen = ();

our %rels = ();
our @sampleSpecs = ();
our %toSample;
our %sampleTypes = ();
our %STR;
our %CUI2;
our $vals;
our %inv = ();

our %auiToCodeMap = (); # generally should be 1-1, so flat hash.
our %codeToPreferredAuiMap = (); # also 1-1 flat hash in the other direction.
our %cuiToCodeMap = (); # generally 1-n, so multi hash
our %codeToCuiMap = (); # generally 1-n, so multi hash.
our %termgroupToRankMap=(); # SAB/TTY => rank
our %auiToRankFields = (); # aui to computed rank hash. may need its own function
our %srcCUIs = ();


if ($sample == 2) {
  open(SAMPLESPECS, $sampleFile) || die "can't open $sampleFile, stopped";
  while (<SAMPLESPECS>) {
    chomp;
    push(@sampleSpecs, $_);
  }
} elsif ($sample == 1) {
  @sampleSpecs = ($sampleSpec);
}

foreach $sampleSpec (@sampleSpecs) {
  my ($tallyName, $tallyFieldValues) = ($sampleSpec =~ /^([^:]*)\:(.*)/);
  $sampleTypes{$tallyName} = 1;
  $toSample{"$tallyName|$tallyFieldValues"} = 1;
#  print " [DEBUG] adding $tallyName|$tallyFieldValues to sample\n";
#  print " [DEBUG] $toSample{\"$tallyName|$tallyFieldValues\"}\n";

}

open(DOC, "$RRFdir/MRDOC.RRF") || die "can't find MRDOC.RRF, stopped";
while (<DOC>) {
  my ($DOCKEY, $VALUE, $TYPE, $EXPL) = split(/[|\n]/);
  if (($DOCKEY eq "REL" && $TYPE eq "rel_inverse") ||
      ($DOCKEY eq "RELA" && $TYPE eq "rela_inverse")) {
    die "inconsistent inverses, stopped"
      if ($inv{$VALUE} ne "" && $inv{$VALUE} ne $EXPL)
      || ($inv{$EXPL} ne "" && $inv{$EXPL} ne $VALUE);
    $inv{$VALUE} = $EXPL;
    $inv{$EXPL} = $VALUE;
  }
}



# cache MRRANK.RRF
# needed for anything involving MRCONSO.RRF (to compute precedence)
# we could make it conditional on needing MRCONSO, but it's small anyway.
open(RANK, "$RRFdir/MRRANK.RRF") || die "can't find MRRANK.RRF, stopped";
while (<RANK>) {
    chomp;
    my ($RANK,$SAB,$TTY,$SUPPRESS) = split(/[|\n]/);
    my $termgroup = $SAB . "/" . $TTY;
    $termgroupToRankMap{$termgroup} = $RANK+0 unless $SAB eq 'SRC';

}


# AUI ranking requires pre-processing MRCONSO.RRF before actually counting anything.
# this run grabs AUIs and their ranking fields, and maps code(Surrogates) to the highest-ranking AUI.
open(CONSO, "$RRFdir/MRCONSO.RRF") || die "can't find MRCONSO.RRF, stopped";
while (<CONSO>) {
    chomp;
    my ($CUI,$LAT,$TS,$LUI,$STT,$SUI,$ISPREF,$AUI,$SAUI,$SCUI,$SDUI,$SAB,$TTY,$CODE,$STR,$SRL,$SUPPRESS,$CVF) = split(/[|\n]/);
    my %codeHash = (CUI => $CUI, AUI => $AUI, SAUI => $SAUI, SCUI => $SCUI, SDUI => $SDUI, CODE => $CODE);
    my $codeSurrogate = $codeHash{$codeType};
    
    # ignore ISPREF by always setting it to 0, unless comparing based on CUI.
    $ISPREF = 0 unless ($codeType eq 'CUI');

    my $termgroupRank = $termgroupToRankMap{$SAB . "/" . $TTY};
    my $LRR = &computeLRR($TS,$STT);
    my $suiRank = 100000000 - substr($SUI, 1); # lower SUIs rank higher
    my $auiRank = 100000000 - substr($AUI, 1); # lower AUIs rank higher

    # AUIs will be ranked by 5 elements in descending order: 
    # ISPREF, termgroup rank, LRR, SUI rank, and AUI rank.
    $auiToRankFields{$AUI} =  [ ( ($ISPREF eq 'Y') ? 1 : 0),
			       $termgroupRank,
			       $LRR,
			       $suiRank,
			       $auiRank];

    # iterate through the stored rank fields for each AUI.
    # if the element being compared is equal, go on to the next one.
    if (exists $codeToPreferredAuiMap{$codeSurrogate}) {
	my $existingAui = $codeToPreferredAuiMap{$codeSurrogate};
	my @currentHashValues = @{ $auiToRankFields{$AUI} };
	my @existingHashValues = @{ $auiToRankFields{$existingAui} };

#        print "[DEBUG] using currentHashValues: " . join(",",@currentHashValues) . " for AUI $AUI\n";
#        print "[DEBUG] using existingHashValues: " . join(",",@existingHashValues) . " for AUI $existingAui\n";

	for (my $i=0; $i < (scalar @currentHashValues); $i++) {
#            print "[DEBUG] challenging $AUI ($currentHashValues[$i]) vs. $existingAui ($existingHashValues[$i]) for $codeSurrogate\n";
	    if ($currentHashValues[$i] < $existingHashValues[$i]) {
#		print "[DEBUG] retain existing AUI\n";
		last;
	    }
	    elsif ($currentHashValues[$i] > $existingHashValues[$i]) {
		$codeToPreferredAuiMap{$codeSurrogate} = $AUI;
#		print "[DEBUG] $codeSurrogate maps better to AUI $AUI based on $currentHashValues[$i] vs. $existingHashValues[$i]\n";
		last;
	    }
            # else: if they're equal, go on to check the next value.
        }
    }
    else {
	$codeToPreferredAuiMap{$codeSurrogate} = $AUI;
#	print "[DEBUG] Adding map from $codeSurrogate to AUI $AUI\n";
    }
}
close (CONSO);

# now that we've cached AUI ranks, we need to open MRCONSO.RRF again to process counts.
# in single-source processing, we now ALWAYS need to process MRCONSO.RRF for non-name/code sampling
# in order to get codeSurrogates and corresponding AUIs.

open(CONSO, "$RRFdir/MRCONSO.RRF") || die "can't find MRCONSO.RRF, stopped";
while (<CONSO>) {

  ### this is the section that ALWAYS happens.
  #
  chomp;

  my ($CUI,$LAT,$TS,$LUI,$STT,$SUI,$ISPREF,$AUI,$SAUI,$SCUI,$SDUI,$SAB,$TTY,$CODE,$STR,$SRL,$SUPPRESS,$CVF) = split(/[|\n]/);
  my %codeHash = (CUI => $CUI, AUI => $AUI, SAUI => $SAUI, SCUI => $SCUI, SDUI => $SDUI, CODE => $CODE); 
  my $codeSurrogate = $codeHash{$codeType};

  # skip SRC concepts. store their CUIs so we can skip them in other files too.
  if ($SAB eq 'SRC' ){
      $srcCUIs{$CUI} = 'SRC';
      next;
  }

  # store all the necessary UI maps
  else {
      $auiToCodeMap{$AUI} = $codeSurrogate;
      $cuiToCodeMap{$CUI}{$codeSurrogate}++;
  }
  #
  ###

  ### this is the conditional section
  #

  if (!$sample || $sampleTypes{"nameTally"} || $sampleTypes{"codeTally"} 
        || ($sample && $toSample{"propTally|ENG,UMLS_CUI,$codingSchemeName,Content"}) 
    || $sampleTypes{"propTypeCount"} )
{

    # set $pref based on AUI ranking from previous run of MRCONSO.RRF
    my $pref = ($codeToPreferredAuiMap{$codeSurrogate} eq $AUI) ? 'Y' : 'N';


    &process("name", "null,$pref,$LAT,$TTY,textualPresentation,$codingSchemeName,Content","$codeSurrogate|$STR");
    &process("code", "null,Y,N,Content", "$codeSurrogate") unless $codeSeen{$codeSurrogate}++;

    if ($sample) {
      if ( $toSample{"propTally|ENG,UMLS_CUI,$codingSchemeName,Content"}) {
	print "propTally|ENG,UMLS_CUI,$codingSchemeName,Content|$codeSurrogate|$CUI\n" 
	  unless $codeToCuiMap{$codeSurrogate}{$CUI}++;
      }
    }
    else {
      ++$propCount, ++$propTally{"ENG,UMLS_CUI,$codingSchemeName,Content"} 
          unless $codeToCuiMap{$codeSurrogate}{$CUI}++;
      ++$ttyCount unless $ttySeen{$TTY}++;
      ++$sourceCount unless $sourceSeen{$SAB}++;
    }
}
  #
  ###

}


if (!$sample || $sampleTypes{"defTally"} || $sampleTypes{"propPropTally"} 
    || $sampleTypes{"propTypeCount"} || $sampleTypes{"propPropTypeCount"}
) {
open(DEF, "$RRFdir/MRDEF.RRF") || die "can't find MRDEF.RRF, stopped";
  while (<DEF>) {
    chomp;

    my ($CUI,$AUI,$ATUI,$SATUI,$SAB,$DEF,$SUPPRESS,$CVF) = split(/[|\n]/);
    next if (exists($srcCUIs{$CUI}));
    my $codeSurrogate = $auiToCodeMap{$AUI};

    &process("def", "$codingSchemeName,Content", "$codeSurrogate|$DEF");
    # for definition,source, we put the propPropValue where ATN should be
    &process("propProp", "definition,qualifier,SUPPRESS,Content",
      "$codeSurrogate|$DEF|$SUPPRESS") if $SUPPRESS ne "" && $SUPPRESS ne "N";
  }
}

if (!$sample || $sampleTypes{"propTally"} || $sampleTypes{"propPropTally"} 
    || $sampleTypes{"propTypeCount"} )
{
  open(STY, "$RRFdir/MRSTY.RRF") || die "can't find MRSTY.RRF, stopped";
  while (<STY>) {
    chomp;

    my ($CUI,$TUI,$STN,$STY,$ATUI,$CVF) = split(/[|\n]/);
    next if (exists($srcCUIs{$CUI}));
    my $codeSurrogate;
    for $codeSurrogate (keys %{$cuiToCodeMap{$CUI}}) {
	&process("prop", "null,Semantic_Type,$codingSchemeName,Content",
		 "$codeSurrogate|$STY");
	&process("prop", "null,TUI,$codingSchemeName,Content",
		 "$codeSurrogate|$TUI");
    }
  }
}

#if (!$sample || $sampleTypes{"propPropTally"}) {
#  open(HIER, "$RRFdir/MRHIER.RRF") || die "can't find MRHIER.RRF, stopped";
#  while (<HIER>) {
#    chomp;
#
#    my ($CUI,$AUI,$CXN,$PAUI,$SAB,$RELA,$PTR,$HCD,$CVF) = split(/[|\n]/);
#
#    my $STR = "";
#    $STR = $STR{$AUI} if $toSample{"propPropTally|$vals"};
#    &process("propProp", "presentation,qualifier,HCD,Content", "$CUI|$STR|$HCD")
#      if $HCD ne "";
#  }
#}

if (!$sample || $sampleTypes{"relTally"} || $sampleTypes{"relPropTally"}
    || $toSample{"codeTally|null,Y,N,Metadata"}
    || $sampleTypes{"relTypeCount"} || $sampleTypes{"relPropTypeCount"} || $sampleTypes{"propPropTypeCount"}
) {
  open(REL, "$RRFdir/MRREL.RRF") || die "can't find MRREL.RRF, stopped";
  while (<REL>) {
    chomp;
    my ($CUI1,$AUI1,$STYPE1,$REL,$CUI2,$AUI2,$STYPE2,$RELA,$RUI,$SRUI,$SAB,$SL,$RG,$DIR,$SUPPRESS,$CVF) = split(/[|\n]/);

    # skip rels connected to SRC concepts
    next if (exists($srcCUIs{$CUI1}) || exists($srcCUIs{$CUI2}));

    my $codeSurrogate1 = $auiToCodeMap{$AUI1};
    my $codeSurrogate2 = $auiToCodeMap{$AUI2};

    if (!$sample) {
      if ($REL eq "CHD") {
	$isaRelValue = $REL;
      }
    }

    # TRY: reversing $codeSurrogate1 and $codeSurrogate2 to see if samples are correct.

    # general case
    &process("rel", "$codingSchemeName,$REL,Content", "$codeSurrogate2|$codeSurrogate1");

    # relProp conditional cases
    &process("relProp", "rela,Content", "$codeSurrogate2|$codeSurrogate1|$RELA")
	if $RELA ne "";
    &process("relProp", "RG,Content", "$codeSurrogate2|$codeSurrogate1|$RG")
	if $RG ne "";
    &process("relProp", "SUPPRESS,Content", "$codeSurrogate2|$codeSurrogate1|$SUPPRESS")
      if $SUPPRESS ne "" && $SUPPRESS ne "N";
    &process("relProp", "self-referencing,Content", "$codeSurrogate2|$codeSurrogate1|true")
        if $codeSurrogate1 eq $codeSurrogate2;

    # deal with rel names being loaded as metadata codes
    if (!$sample) {
      ++$codeCount,++$codeTally{"null,Y,N,Metadata"} unless $relMetadataTypeSeen{$REL}++;
    } else {
       print "codeTally|null,Y,N,Metadata|$REL\n" 
	   unless ($relMetadataTypeSeen{$REL}++ || !$toSample{"codeTally|null,Y,N,Metadata"});
    }
  }
}

if (!$sample || $sampleTypes{"propTally"} || $sampleTypes{"propPropTally"} ||
    $sampleTypes{"relPropTally"} 
    || $sampleTypes{"propTypeCount"} )
{
  open(SAT, "$RRFdir/MRSAT.RRF") || die "can't find MRSAT.RRF, stopped";
  while (<SAT>) {
    chomp;

    my ($CUI,$LUI,$SUI,$METAUI,$STYPE,$CODE,$ATUI,$SATUI,$ATN,$SAB,$ATV,$SUPPRESS,$CVF) = split(/[|\n]/);

    # skip attributes connected to SRC concepts
    next if (exists($srcCUIs{$CUI}));

    my $codeSurrogate = $cuiToCodeMap{$CUI};

    # currently, this code ignores RUI attributes and accounts for AUI,CODE,SAUI/SCUI/SDUI attributes
    if ($STYPE eq 'AUI') {
	$codeSurrogate = $auiToCodeMap{$METAUI};
    }
    elsif ($STYPE eq 'RUI') {
	next;
    }
    elsif ($STYPE eq 'CODE' || $STYPE eq 'SAUI' || $STYPE eq 'SCUI' || $STYPE eq 'SDUI') {
	if ($STYPE eq $codeType) {
	    $codeSurrogate = $METAUI;
	}
	else {
	    $codeSurrogate = $CODE;
	}
    }
    else {
        next;
    }
      &process("prop", "null,$ATN,$codingSchemeName,Content", "$codeSurrogate|$ATV");
      &process("propProp", "$ATN,qualifier,SUPPRESS,Content",
        "$codeSurrogate|$ATV|$SUPPRESS") if $SUPPRESS ne "" && $SUPPRESS ne "N";
  }
}

exit 0 if $sample;

#
# Write results
#
our $key;
print "codeCount||$codeCount|\n";
foreach $key (sort keys %codeTally) {
  print "codeTally|$key|$codeTally{$key}|\n";
}
print "commentCount||$commentCount|\n";
print "defCount||$defCount|\n";
foreach $key (sort keys %defTally) {
  print "defTally|$key|$defTally{$key}|\n";
}
print "instrCount||$instrCount|\n";
print "nameCount||$nameCount|\n";
foreach $key (sort keys %nameTally) {
  print "nameTally|$key|$nameTally{$key}|\n";
}
print "propCount||$propCount|\n";
foreach $key (sort keys %propTally) {
  print "propTally|$key|$propTally{$key}|\n";
}
print "propPropCount||$propPropCount|\n";
foreach $key (sort keys %propPropTally) {
  print "propPropTally|$key|$propPropTally{$key}|\n";
}
print "propLinkCount||$propLinkCount|\n";
foreach $key (sort keys %propLinkTally) {
  print "propLinkTally|$key|$propLinkTally{$key}|\n";
}
print "relCount||$relCount|\n";
foreach $key (sort keys %relTally) {
  print "relTally|$key|$relTally{$key}|\n";
}
print "relDataCount||$relDataCount|\n";
foreach $key (sort keys %relDataTally) {
  print "relDataTally|$key|$relDataTally{$key}|\n";
}
print "relPropCount||$relPropCount|\n";
foreach $key (sort keys %relPropTally) {
  print "relPropTally|$key|$relPropTally{$key}|\n";
}
print "relTypeCount||$relTypeCount|\n";
print "relPropTypeCount||$relPropTypeCount|\n";
print "isaRelValue||$isaRelValue|\n";
print "languageTypeCount||$languageTypeCount|\n";
print "propTypeCount||$propTypeCount|\n";
print "propLinkTypeCount||$propLinkTypeCount|\n";
print "propPropTypeCount||$propPropTypeCount|\n";
print "ttyCount||$ttyCount|\n";
print "sourceCount||$sourceCount|\n";


sub process {
  my($type, $vals, $sampleVals) = @_;
  my($tallyName, $countName) = ("${type}Tally", "${type}Count");
  my($typeCountName,$typeSeenName) = ("${type}TypeCount", "${type}TypeSeen");
  my(@vals) = split(/,/, $vals);
  my($typeVal) = ($type eq "propProp") ? $vals[2] :
		 ($type eq "relProp") ? $vals[0] :
		 ($type eq "prop" || $type eq "rel") ? $vals[1] : "";
  if (!$sample) {
    ++$$countName;
    ++$$tallyName{$vals};
  } else {
    print "${type}Tally|$vals|$sampleVals|\n" if $toSample{"${type}Tally|$vals"};
    print "${type}Count||$sampleVals|\n" if $toSample{"${type}Count|"}; 
  }

#  print " [DEBUG] type is $type, typeVal is $typeVal\n";

  # handle *TypeCount separately
  if ($typeVal ne "") {
    if (!$sample) {
      $$typeCountName++ unless $$typeSeenName{$typeVal}++;
    }
    elsif ($toSample{"${type}TypeCount|"}) {
      print "${type}TypeCount||$typeVal|\n" unless $$typeSeenName{$typeVal}++; 
    }
  }


}



# compute LRR based on TS and STT
sub computeLRR {

    # bitmask TS/STT values to calculate LRR
    my %lrrBitmask = ('P' => 4, 'S' => 2, 'PF' => 1, 'V' => 0);

    my ($TS,$STT) = @_;

    # all V* values are = 0
    if($STT =~ /^V.+$/) {
       $STT = 'V';
    }

    my $LRR = $lrrBitmask{$TS} + $lrrBitmask{$STT}; 
 
    return $LRR;
}

############################################################################
# Help & Usage Procedures
############################################################################

sub PrintHelp {

  PrintUsage();
  print qq{
 <RRF dir>            : The directory in which the MR*.RRF files can be found
 <Coding scheme name> : The name of the coding scheme (e.g. "NCI MetaThesaurus")

 Output is written as a text report.

 Options:
       -v          : verbose
       -vv         : more verbose
       -[-]help    : On-line help
};
}

sub PrintUsage {
  print qq{This script has the following usage:

    countRrf.pl <CODE|SAUI|SCUI|SDUI> <RRF dir> <Coding scheme name>
};
}

