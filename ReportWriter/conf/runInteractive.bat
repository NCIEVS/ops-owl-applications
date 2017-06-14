ECHO OFF
set OCP=%CLASSPATH%
set lib=./lib

set CLASSPATH=%lib%/client.jar;%lib%/xerces.jar;%lib%/log4j-1.2.8.jar;%lib%/spring.jar;%lib%/commons-logging.jar;%lib%/hibernate3.jar;%lib%/reportwriter.jar

java -Xmx500M -classpath %CLASSPATH% gov.nih.nci.evs.report.ReportWriter -i .\config.txt

set CLASSPATH=%OCP%