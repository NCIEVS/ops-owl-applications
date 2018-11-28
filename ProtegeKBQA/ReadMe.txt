This application takes NCI Thesaurus and checks it against several criteria to determine if it requires any fixes.  
All issues are reported out as tab-delimited text, which is then loaded into Excel and presented to the editors.

A series of config files controls the program

nciowlqa.properties 
   - lists the locations of all the other config files
   - tells where to find the Thesaurus OWL file needed QA
   - tells where to put the location of the output report
   - specifies the namespace of the vocabulary
   - specifies the identifier for the Retired branch
   
drugeditors.dat = only some editors are permitted to edit drug concepts. The program compares edit logs to the approved list

ignoresources.dat - some business rules don't apply to content pulled from specified outside sources

messages.properties - Externalization of strings checked within the program

semanticpairs.dat - one test is to detect concepts with multiple semantic types.  Some semantic combos are valid, so shouldn't be reported

semantictype.dat - list of all valid semantic types. Any variants found in OWL file are reported

Suppress.txt - Allows some tests and/or test cases to be suppressed from reporting (Not in Use)

symbolMap.txt - Some unicode and ASCII characters cannot be represented correctly in our server, so our replaced and reported.
