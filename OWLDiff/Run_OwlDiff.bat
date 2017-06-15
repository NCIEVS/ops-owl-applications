@echo off
REM 
REM parameters:
REM   -i urn of older file
REM   -p urn of newer file
REM   -U (optional) to display only Unique concepts. If left out, will default to printing changeset
REM   -o Location and name of output file
REM 
REM
java -Xmx1500m -cp "..\lib\owlapi-bin.jar;..\lib\owlkb.jar" gov.nih.nci.evs.owl.OWLDiff -i "file:///O:/EVS_Thesaurus/Baseline/09.06e/ThesaurusInf-ForPublication-09.06e-byCode.owl" -p "file:///O:/EVS_Thesaurus/Baseline/09.07e/ThesaurusInf-ForPublication-09.07e.owl-fixedNS.owl-NoKinds.owl-byCode.owl" -o "./OwlDiffOutput.txt" 

To see only the changes and filter out everything else, use grep.
Sample command: grep --file=diffClean.txt Diff1105e-1104d.txt > cleanedDiff1105e-1104d.txt
