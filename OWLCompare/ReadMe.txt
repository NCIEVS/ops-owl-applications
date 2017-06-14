OWLCompare README
=================

Build OWLCompare:

1) Open a command window in this directory
2) ant clean
3) ant compile
4) ant jar

Running the Program:

1) Read and configure the owlcompare.properties file found in the config directory.  
2) Modify propertyMatch and propertyOutput config files as needed.
3) Open a command line, and run OWLCompare with the following usage...

Usage: OWLCompare [OPTIONS] ... [INPUT OWL] [OUTPUT]

  -E, --explanationFile         path to match explanation file output
  -I, --input                   path to match input file
  -M, --propertyMatchFile       path to property match file
  -P, --propertyPrintFile       path to property print file
  -R, --includeRetired          include retired branch for matching
  -S, --simpleMatching          strict case insensitive (simple) matching

Examples:

C:\> OWLCompare Thesaurus.owl myoutput.txt

C:\> OWLCompare -R -S -I ./someotherdirectory/matchInput.txt Thesaurus.owl myoutput2.txt


propertyMatch.txt
-----------------
This file can contain a list of valid Annotation property names,
or names separated by tab followed by tag name.  If searching
multiple properties, the list is OR'd. To match class names, use RDF:ID.

Examples:

Match both Preferred Name and FULL_SYN term-names:
Preferred_Name
FULL_SYN	term-name

Match both UMLS_CUI and NCI_META_CUI:
UMLS_CUI
NCI_META_CUI

Match class names (soon to be code):
RDF:ID

