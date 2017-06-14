#/bin/csh -f

echo lb6_`mysql -hcbdb-q2001 -P3667 -ulex60data -pl360\#dayta lex60 < $QA_HOME/etc/getLatestNci.sql | grep -v "prefix"`
