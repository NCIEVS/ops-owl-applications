The property names MUST match the column names from the HGNC download, with any spaces 
replaced by underscores.  Example: Property Names would be entered as Property_Names.

The value tells what kind of delimiter is used within the column. The possible values are:

CD = Comma Delimited  -  A,B,C,D
QCD = Quoted Comma Delimited  -  "A","B","C","D"
HCD = Hyperlink Comma Delimited  -  --> <!--,--> <!--,--><a href="http:\\link.com">Foo</a><!--,--> <!--

Any other values will be ignored.

To check on what the current set of header properties are, and to see if the Specialist Database IDs need 
to be updated, check here: http://www.genenames.org/data/gdlw_doc.html