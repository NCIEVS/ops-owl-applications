#This will run the inferred file through the processsing steps one by one
#Pass in the name of the current and previous inferred files
echo The inferred file to be processed is $1
echo The previous inferred file to be diffed is $2
echo the start date for monthly history in yyyy-mm-dd form is $3
echo the end date for monthly history in yyyy-mm-dd form is $4
echo ------------------------------------------------------------
#PRODUCTION FILE

#Run OWLSUMMARY on raw files
echo RUN OWLSUMMARY ON RAW FILES
cd OWLSummary
/usr/jdk1.6.0_18/bin/java -jar ./build/owlsummary.jar -i $1 -p $2 -S file:///app/protege/Processing/data/rawSummary.txt
cd /app/protege/Processing

#Scrub inferred file for Production
echo SCRUBBING INFERRED FILE FOR PRODUCTION
cd OWLScrubber
ProdOutput=$1-forProduction.owl
echo the output file is $ProdOutput
/usr/jdk1.6.0_18/bin/java -jar ./build/owlscrubber.jar -C ./config/owlscrubber_nci.properties -L ncicp -E -N $1 -O $ProdOutput

#run through formatOWL and convertByCode
echo ---------------------------------
echo RUNNING FORMATOWL
cd scripts
perl formatOWL.pl $ProdOutput ../config/props_del_NCI.txt

FormattedProd=$ProdOutput-fixedNS.owl
echo formatOwl Output $FormattedProd

echo RUNNING REMOVE DUPLICATE RESTRICTIONS
perl removeDuplicateRestrictions.pl $FormattedProd

RemovedDups=$FormattedProd-removedDuplicates.owl
echo removedDuplicates Output $RemovedDups

echo RUNNING CONVERTBYCODE
perl convertByCode.pl $RemovedDups
ByCodeOutput=$RemovedDups-byCode.owl
echo byCode output $ByCodeOutput
echo RUNNING ICONV
perl runIconv.pl $ByCodeOutput
IconvOutput=$ByCodeOutput-iconv.owl
cd /app/protege/Processing

echo --------------------------------
echo EXPORT HISTORY
#Export history
cd ExportHistory
sh runHistory.sh $3 $4
cd /app/protege/Processing

echo RUN HISTORY VALIDATION
#run through HistoryQA
cd HistoryValidation
currentPath=pwd
currentURI=file:///$currentPath
outputHistory=$1_HistoryValidation.txt
/usr/jdk1.6.0_18/bin/java -jar ./build/historyvalidation.jar -g ./config/ProtegeHistoryQA.properties -u $1 -p $2 -o $outputHistory
cd /app/protege/Processing


#Run through OWLDiff and grep
echo -------------------------------
echo RUN OWLDIFF
cd OWLDiff
outputDiff=file:///app/protege/Processing/data/OWLDiff.txt
/usr/jdk1.6.0_18/bin/java -jar ./build/owldiff.jar -i $1 -p $2 -o $outputDiff

grep --file=./config/diffClean.txt ../data/OWLDiff.txt > ../data/Grepped_OwlDiff.txt
cd /app/protege/Processing/

#****************************************
#MEME FILE

#scrub inferred file for MEME
echo -------------------------------
echo SCRUBBING INFERRED FILE FOR MEME
cd OWLScrubber
MemeOutput=$1-forMEME.owl
echo The output file is $MemeOutput
/usr/jdk1.6.0_18/bin/java -jar ./build/owlscrubber.jar -c ./config/owlscrubber_meme.properties -L ncicp -E -N $1 -O $MemeOutput


#run through formatOWL and convertByCode
echo --------------------------------
echo RUNNING FORMATOWL
cd scripts
perl formatOWL.pl $MemeOutput ../config/props_del_MEME.txt

FormattedMeme=$MemeOutput-fixedNS.owl

echo RUNNING REMOVE DUPLICATE RESTRICTIONS
perl removeDuplicateRestrictions.pl $FormattedMeme

RemovedDupsMeme=$FormattedMeme-removedDuplicates.owl
echo removedDuplicates Output $RemovedDupsMeme

echo RUNNING CONVERTYBYCODE
perl convertByCode.pl $RemovedDupsMeme
ByCodeMeme=$RemovedDupsMeme-byCode.owl
echo RUNNING ICONV
perl runIconv.pl $ByCodeMeme
IconvOutput=$ByCodeMeme-iconv.owl
cd /app/protege/Processing

#run through OWLSummary
echo ---------------------------------
echo RUN OWLSUMMARY
cd OWLSummary
previousFile=$2-forMEME.owl-fixedNS.owl-removedDuplicates.owl-byCode.owl
echo Previous processed file is $previousFile
/usr/jdk1.6.0_18/bin/java -jar ./build/owlsummary.jar -i $ByCodeMeme -p $previousFile -S file:///app/protege/Processing/data/Summary.txt -D file:///app/protege/Processing/data/Details.txt


