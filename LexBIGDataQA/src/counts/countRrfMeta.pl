#!/usr/bin/perl
#
# Script:  countRrfMeta.pl
# Author:  Nels Olson, Joanne Wong
#
# Remarks:   This script is used to generate QA counts for a set of RRF files.
#
# Version Information:
#   2014/03/25 JFW (): Change "Active" in nameTally to "null"
#                      Change "textualPresentation" in nameTally,relTally to "presentation"
#                      Change propTally to have language=ENG
#                      Remove self-referential rels from Counts and Tallies
#   2012/04/25 JFW (): Count REL as metadata code.
#   2012/04/17 JFW (): Added textualPresentation to nameCount/Tally.
#   2012/03/27 JFW (): Branched to countRrfMeta.pl for counting NCI Meta.
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
# e.g. "countRrfMeta.pl RRF 'NCI MetaThesaurus' >countA.txt"

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
our $RRFdir = "";
our $codingSchemeName = "";
if (scalar(@ARGS) == 2) {
  ($RRFdir, $codingSchemeName) = @ARGS;
} else {
  print "Incorrect number of arguments\n";
  PrintUsage();
  exit(1);
}


our $codeCount= 0;
our %codeTally = ();
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
our $changeCount = 0;
our %changeTally = ();
our %propTypeSeen = ();
our %propPropTypeSeen = ();
our %relMetadataTypeSeen = ();
our %relTypeSeen = ();
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

if (!$sample || $sampleTypes{"nameTally"} || $sampleTypes{"codeTally"} ||
    $sampleTypes{"propPropTally"}) {
  my $pCUI = '';
  open(CONSO, "$RRFdir/MRCONSO.RRF") || die "can't find MRCONSO.RRF, stopped";
  while (<CONSO>) {
    chomp;

    my ($CUI,$LAT,$TS,$LUI,$STT,$SUI,$ISPREF,$AUI,$SAUI,$SCUI,$SDUI,$SAB,$TTY,$CODE,$STR,$SRL,$SUPPRESS,$CVF) = split(/[|\n]/);

#    $STR{$AUI} = $STR if $sampleTypes{"propPropTally"} ||
#			 $sampleTypes{"propLinkTally"};

    if (!$sample) {
      ++$ttyCount unless $ttySeen{$TTY}++;
      ++$sourceCount unless $sourceSeen{$SAB}++;
    }

    my $pref = ($TS eq 'P' && $STT eq 'PF' && $ISPREF eq 'Y') ? 'Y' : 'N';
    &process("name", "null,$pref,$LAT,$TTY,presentation,$codingSchemeName,Content",
      "$CUI|$STR");
    # for presentation,source, we put the propPropValue where ATN should be
    &process("propProp", "presentation,source,$SAB,Content", "$CUI|$STR|$SAB");
    &process("propProp", "presentation,qualifier,LUI,Content",
      "$CUI|$STR|$LUI") if $LUI ne "";
    &process("propProp", "presentation,qualifier,SUI,Content",
      "$CUI|$STR|$SUI") if $SUI ne "";
    &process("propProp", "presentation,qualifier,AUI,Content",
      "$CUI|$STR|$AUI") if $AUI ne "";
    &process("propProp", "presentation,qualifier,SAUI,Content",
      "$CUI|$STR|$SAUI") if $SAUI ne "";
    &process("propProp", "presentation,qualifier,SCUI,Content",
      "$CUI|$STR|$SCUI") if $SCUI ne "";
    &process("propProp", "presentation,qualifier,SDUI,Content",
      "$CUI|$STR|$SDUI") if $SDUI ne "";
    &process("propProp", "presentation,qualifier,SUPPRESS,Content",
      "$CUI|$STR|$SUPPRESS") if $SUPPRESS ne "" && $SUPPRESS ne "N";
    &process("propProp", "presentation,qualifier,source-code,Content",
      "$CUI|$STR|$CODE") if $CODE ne "";
    &process("propProp", "presentation,qualifier,mrrank,Content",
      "$CUI|$SAB|$TTY");
    if ($CUI ne $pCUI) {
      $pCUI = $CUI;
      &process("code", "null,Y,N,Content", "$CUI");
    }
  }
}

if (!$sample || $sampleTypes{"defTally"} || $sampleTypes{"propPropTally"}) {
open(DEF, "$RRFdir/MRDEF.RRF") || die "can't find MRDEF.RRF, stopped";
  while (<DEF>) {
    chomp;

    my ($CUI,$AUI,$ATUI,$SATUI,$SAB,$DEF,$SUPPRESS,$CVF) = split(/[|\n]/);

    &process("def", "$codingSchemeName,Content", "$CUI|$DEF");
    # for definition,source, we put the propPropValue where ATN should be
    &process("propProp", "definition,source,$SAB,Content", "$CUI|$DEF|$SAB");
    &process("propProp", "definition,qualifier,AUI,Content", "$CUI|$DEF|$AUI");
    &process("propProp", "definition,qualifier,ATUI,Content",
      "$CUI|$DEF|$ATUI");
    &process("propProp", "definition,qualifier,SUPPRESS,Content",
      "$CUI|$DEF|$SUPPRESS") if $SUPPRESS ne "" && $SUPPRESS ne "N";
  }
}

if (!$sample || $sampleTypes{"propTally"} || $sampleTypes{"propPropTally"}) {
  open(STY, "$RRFdir/MRSTY.RRF") || die "can't find MRSTY.RRF, stopped";
  while (<STY>) {
    chomp;

    my ($CUI,$TUI,$STN,$STY,$ATUI,$CVF) = split(/[|\n]/);

    &process("prop", "ENG,Semantic_Type,$codingSchemeName,Content",
      "$CUI|$STY");
#    &process("propProp", "STY,qualifier,ATUI,Content", "$CUI|$STY|$ATUI");
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
#   || $sampleTypes{"propLinkTally"}
    || $toSample{"codeTally|null,Y,N,Metadata"}
                                    ) {
  open(REL, "$RRFdir/MRREL.RRF") || die "can't find MRREL.RRF, stopped";
  while (<REL>) {
    chomp;

    my ($CUI1,$AUI1,$STYPE1,$REL,$CUI2,$AUI2,$STYPE2,$RELA,$RUI,$SRUI,$SAB,$SL,$RG,$DIR,$SUPPRESS,$CVF) = split(/[|\n]/);
     if ($REL ne "SIB") {
      if (!$sample) {
	if ($RELA =~ /^is.*a$/) {
	  $isaRelValue = $RELA;
	}
      }

      # ignore self-referencing relProp for counts and tallies
      #&process("relProp", "self-referencing,Content", "$CUI1|$CUI2|true")
      next if $CUI1 eq $CUI2;
     
      &process("rel", "$codingSchemeName,$REL,Content", "$CUI1|$CUI2");
      &process("relProp", "source,Content", "$CUI1|$CUI2|$SAB");
      &process("relProp", "source-aui,Content", "$CUI1|$CUI2|$AUI1")
        if $AUI1 ne "";
      &process("relProp", "target-aui,Content", "$CUI1|$CUI2|$AUI2")
        if $AUI2 ne "";
      &process("relProp", "STYPE1,Content", "$CUI1|$CUI2|$STYPE1")
        if $STYPE1 ne "";
      &process("relProp", "STYPE2,Content", "$CUI1|$CUI2|$STYPE2")
        if $STYPE2 ne "";
      &process("relProp", "rela,Content", "$CUI1|$CUI2|$RELA")
        if $RELA ne "";
      &process("relProp", "RUI,Content", "$CUI1|$CUI2|$RUI")
        if $RUI ne "";
      &process("relProp", "SRUI,Content", "$CUI1|$CUI2|$SRUI")
        if $SRUI ne "";
      &process("relProp", "RG,Content", "$CUI1|$CUI2|$RG")
        if $RG ne "";
      &process("relProp", "SUPPRESS,Content", "$CUI1|$CUI2|$SUPPRESS")
        if $SUPPRESS ne "" && $SUPPRESS ne "N";

     # deal with rel names being loaded as metadata codes
      if (!$sample) {
	  ++$codeCount,++$codeTally{"null,Y,N,Metadata"} unless $relMetadataTypeSeen{$REL}++;
      } else {
	  print "codeTally|null,Y,N,Metadata|$REL\n" unless ($relMetadataTypeSeen{$REL}++ || !$toSample{"codeTally|null,Y,N,Metadata"});
      }


    }
  }
}

if (!$sample || $sampleTypes{"propTally"} || $sampleTypes{"propPropTally"} ||
    $sampleTypes{"relPropTally"}) {
  open(SAT, "$RRFdir/MRSAT.RRF") || die "can't find MRSAT.RRF, stopped";
  while (<SAT>) {
    chomp;

    my ($CUI,$LUI,$SUI,$METAUI,$STYPE,$CODE,$ATUI,$SATUI,$ATN,$SAB,$ATV,$SUPPRESS,$CVF) = split(/[|\n]/);

# treat RUI props the same as CUI props
#    if ($STYPE eq "RUI") {
#      my $CUI2 = $CUI2{$METAUI};
#      &process("relProp", "$ATN,Content", "$CUI|$CUI2|$ATV");
#    } else {
      &process("prop", "ENG,$ATN,$codingSchemeName,Content", "$CUI|$ATV");
      &process("propProp", "$ATN,qualifier,METAUI,Content",
        "$CUI|$ATV|$METAUI");
      &process("propProp", "$ATN,qualifier,STYPE,Content",
        "$CUI|$ATV|$STYPE");
#      &process("propProp", "$ATN,qualifier,ATUI,Content",
#        "$CUI|$ATV|$ATUI");
      &process("propProp", "$ATN,source,$SAB,Content",
        "$CUI|$ATV|$SAB");
      &process("propProp", "$ATN,qualifier,SUPPRESS,Content",
        "$CUI|$ATV|$SUPPRESS") if $SUPPRESS ne "" && $SUPPRESS ne "N";
#    }
  }
}

if (!$sample || $sampleTypes{"changeTally"}) {
  open(CUI, "$RRFdir/MRCUI.RRF") || die "can't find MRCUI.RRF, stopped";
  while (<CUI>) {
    chomp;

    my ($CUI1,$VER,$REL,$RELA,$MAPREASON,$CUI2,$MAPIN) = split(/[|\n]/);

    my $type = ($REL eq "SY") ? "merge" : "retire";
    &process("change", "$type,$VER", "$CUI1|$CUI2");
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
print "changeCount||$changeCount|\n";
foreach $key (sort keys %changeTally) {
  print "changeTally|$key|$changeTally{$key}|\n";
}

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
    if ($typeVal ne "") {
      $$typeCountName++ unless $$typeSeenName{$typeVal}++;
    }
  } else {
    print "${type}Tally|$vals|$sampleVals|\n" if $toSample{"${type}Tally|$vals"};
  }
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

    countRrf.pl <RRF dir> <Coding scheme name>
};
}

