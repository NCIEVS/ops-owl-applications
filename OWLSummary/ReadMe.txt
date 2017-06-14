This application relies on a config file to run.  This config file, owlsummary.properties, tells where to find other needed files and where
to dump output.  You may pass in the configuration file one of three ways

1. Pass it in as an argument when you call the OWLSummary main method. Example:  java -cp "cp" OWLSummary "path"
   
2. Set a system property. Example: java -cp "cp" -Dowlsummary.properties="path" OWLSummary

3. Set the property in the build.xml and run using "ant run"

4. Pass the path to the config file into ant.  Example: ant -Dowlsummary.properties="path" run



Within the owlsummary.properties file there are 2 required lines:
ontology_current=<uri to current Thesaurus.owl>
outputfile=<path and filename to save Summary output.  Typically Summary.txt>

There are also 2 optional lines:

This one will tell the application to compare 2 owl files and output the diff count to the listed output file (above)
ontology_previous=<uri to previous Thesaurus.owl>

This one will tell the application that you want a detailed list of new concepts, retreed concepts, retired concepts, and
concepts that have changed definition.
detailsfile=<path and filename to save Details output.  Typically Details.txt>


example of owlsummary.properties with all 4 lines present:
ontology_current=file\:\/\/\/C:\/data\/ThesaurusInf-ForPublication-byCode-08.11d.owl
ontology_previous=file\:\/\/\/C:\/data\/ThesaurusInf-ForPublication-byCode-08.10e.owl
outputfile=C:\\MyEclipse_workspace\\OWLSummary\\Summary.txt
detailsfile=C:\\MyEclipse_workspace\\OWLSummary\\Details.txt