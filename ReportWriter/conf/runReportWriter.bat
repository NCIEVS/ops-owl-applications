ECHO OFF

java -Xmx500M -classpath ./reportwriter.jar gov.nih.nci.evs.report.ReportWriter -f ./conf/config.txt
