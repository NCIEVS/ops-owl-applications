#This is a sample shell script for running the OWLAPI inference.  Paths will need to be adjusted for your environment
#The parameter should be the name and path of the file to be inferred, in URI format  
/usr/jdk1.7.0_79/bin/java -Xmx15000M -jar ./owlapi-infer.jar $1