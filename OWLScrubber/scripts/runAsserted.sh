# This script runs the asserted file processing steps one by one.
# Pass in the name of the current and previous asserted files
echo The current asserted file to be processed is $1


#classify with Pellet
echo CLASSIFYING WITH PELLET
cd ./pellet/pellet-2.2.2
sh pellet.sh unsat $1
cd /app/protege/Processing

#ProtegeKbQA
echo ----------------------------
echo PERFORMING PROTEGEKBQA
cd ./ProtegeKbQA
QAOutput=$1_QAOutput.txt
echo The output file is $QAOutput
/usr/jdk1.6.0_18/bin/java -jar ./build/owlnciqa.jar -c config/nciowlqa.properties -i $1 -o $QAOutput
cd /app/protege/Processing

#Scrub asserted for FTP and Flat
echo -----------------------------
echo RUNNING OWL SCRUBBER
cd OWLScrubber
FlatOutput=$1_Flat.txt
FtpOutput=$1_forFTP.owl
echo outputFile $FtpOutput
/usr/jdk1.6.0_18/bin/java -jar ./build/owlscrubber.jar -F $FlatOutput -C ./config/owlscrubber_FTP.properties -L ncicp -E -N $1 -O $FtpOutput


#formatOWL and convertByCode
echo ------------------------------
echo RUNNING FORMATOWL
cd scripts
perl formatOWL.pl $FtpOutput ../config/props_del_NCI.txt

FormattedOWL=$FtpOutput-fixedNS.owl
echo formatOwl Output $FormattedOWL
echo RUNNING CONVERTBYCODE
perl convertByCode.pl $FormattedOWL
ByCodeOutput=$FormattedOWL-byCode.owl
echo byCode output $ByCodeOutput
perl runIconv.pl $ByCodeOutput
IconvOutput=$ByCodeOutput-iconv.owl
echo iConv output $IconvOutput
cd /app/protege/Processing

#classify with Pellet
echo -------------------------------
echo CLASSIFY WITH PELLET
cd ./pellet/pellet-2.2.2
sh pellet.sh unsat -v $IconvOutput
cd /app/protege/Processing

echo -------------------------------
echo PROCESSING COMPLETE
