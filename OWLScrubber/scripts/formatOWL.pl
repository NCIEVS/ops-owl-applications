## Rob Wynne, LMCO
##
use strict;
use LWP::Simple;
use URI;
open( my $properties, $ARGV[1] ) or die "Couldn't open properties file!\n";
my $url=URI->new($ARGV[0]);
open( my $owlfile, $url->path) or die "Couldn't open OWL file!\n";
open my $out, '>', $url->path."-fixedNS.owl" or die "Couldn't create output file!\n";

##open( my $owlfile, $ARGV[0] ) or die "Couldn't open OWL file!\n";
##open( my $properties, $ARGV[1] ) or die "Couldn't open properties file!\n";
##open my $out, '>', $ARGV[0]."-fixedNS.owl" or die "Couldn't create output file!\n";
## open $test, '>', "test.txt" or die "Couldn't open test file!\n";

my @owlLines = <$owlfile>;
my @propertyLines = <$properties>;
my $owlLineCount = @owlLines;
my %nsmap;
my $i = 0;
my $j = 0;
my $k = 0;
my @langs = ("", "de", "en", "es", "fr", "it", "nl", "pt", "ru" );
my $propRegEx = "DEFINITION|ALT_DEFINITION|FULL_SYN|GO_Annotation";
my @tags = ("def-definition","def-source","attr","Definition_Reviewer_Name","Definition_Review_Date","go-term","go-id","go-evi","source-date","go-source","term-name","term-group","term-source","source-code");
my @complexProps = ("ComplexTerm", "ComplexDefinition", "ComplexGOAnnotation" );
my $langCount = @langs;
my $tagCount = @tags;
my $complexCount = @complexProps;
my $xsdName = "ncicp";

# for( $i = 0; $i < $langCount; $i++ ) {
#   if( $langs[$i] eq "" ) {
#     ## do nothing
#   }
#   else {
#     $langs[$i] = " xml:lang=&quot;$langs[$i]&quot;";
#   }
# }
# 
# ## To make it clear this is the reverse of @langs, I'm using an
# ## underscore in the variable name instead of camel casing.
# my @langs_flop;
#
# for( $i = 0; $i < $langCount; $i++ ) {
#   my $temp;
#   $temp = $langs[$i];
#   $temp =~ s/&quot;/\"/g;
#   push @langs_flop, $temp;
# }
#
# print @langs;
# print @langs_flop;

chomp(@propertyLines);

## Header stuff
## Reset default NS and base
## Remove Thesaurus NS declaration
## Map all NS's for entity expansion
## Reset Thesaurus NS expansions to base
## Remove annotation property declarations
for( $i = 0; $i < $owlLineCount; $i++ ) {
#   if( $owlLines[$i] =~ /(<rdf:RDF xmlns).*/ ) {
#     print "Renaming rdf:RDF xmnlns...\n";
#     $owlLines[$i] = $1."=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#\"\n";
#   }
#   elsif( $owlLines[$i] =~ /(.*xml:base)(.*\n)/ ) {
#     print "Renaming xml:base...\n";
#     $owlLines[$i] = $1."=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl\"\n";
#   }
#   if( $owlLines[$i] =~ /.*xmlns:Thesaurus.*/ ) {
#     print "Removing Thesaurus xmlns...\n";
#     $owlLines[$i] = "";
#   }
  if( $owlLines[$i] =~ /.*xmlns:(.*)="(.*)".*/ ) {
##  print "Assigning entity $1 = $2\n";
    $nsmap{$1} = $2;
  }
#   elsif( $owlLines[$i] =~ /(.*)(="&Thesaurus;)(.*\n)/ ) {
# ##    print "$_\n\t$1=\"#$3";
#     $owlLines[$i] = $1."=\"#".$3;
#   }
#   elsif( $owlLines[$i] =~ /(.*)("#)(.*\n)/ ) {
#     $owlLines[$i] = $1."\"http://protege.stanford.edu/plugins/owl/protege#".$3
#   }
  if( $owlLines[$i] =~ /(.*)<owl:AnnotationProperty rdf:about="#(.*)"\/>.*\n/ ) {
    foreach(@propertyLines) {
      if( $_ eq $2 ) {
        print "Removing AnnotationProperty declaration $2\n";
        $owlLines[$i] = "";
      }
    }
  }

  if( $owlLines[$i] =~ /(.*)<owl:(AnnotationProperty|DatatypeProperty) rdf:about="#(.*)">.*\n/ ) {
      my $classType = $2;
      while( $owlLines[$i] !~ /(.*)<\/owl:$classType>.*\n/ ) {
#         if( $owlLines[$i] =~ /.*<rdf:type rdf:resource=".*(AnnotationProperty|DatatypeProperty)"\/>.*\n/ ) {
#           $owlLines[$i] = "";
#         }
        if( $owlLines[$i] =~ /(.*)<rdfs:range rdf:resource=".*\/>.*\n/ ) {
          $owlLines[$i] = "";
        }
        elsif( $owlLines[$i] =~ /(.*)<rdfs:range>.*\n/ ) {
          my $space = $1;
#          $owlLines[$i] = "$space<rdfs:range rdf:resource=\"http:\/\/www.w3.org\/2001\/XMLSchema#string\"\/>\n";
          $owlLines[$i] = "";
          $i++;
          while( $owlLines[$i] !~ /.*<\/rdfs:range>.*\n/ ) {
            $owlLines[$i] = "";
            $i++;
          }
          $owlLines[$i] = "";
        }
        $i++;
      }
  }

  # Ranges with a picklist of values changed to string.



}

## uh, yeah, slow.  what was i thinking?
# Expand entities
# print "Expanding entities...\n";
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#      for( $j = 0; $j < @nskey; $j++ ) {
#           if( $owlLines[$i] =~ /(.*)(="&$nskey[$j];)(.*\n)/ ) {
#               $owlLines[$i] = "$1\=\"$nsvalue[$j]$3";
#           }
#      }
# }

print "Expanding entities...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)="&(.*;)(.*".*\n)/ ) {
    my $first = $1;
    my $key = $2;
    my $last = $3;
    $key =~ s/;//;
    if( exists $nsmap{$key} ) {
        $owlLines[$i] = "$first=\"$nsmap{$key}$last";
#        print $owlLines[$i];
    }
  }
}

print "Removing CDATA...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /.*CDATA.*/ ) {
    $owlLines[$i] =~ s/&lt;!\[CDATA\[//g;
    $owlLines[$i] =~ s/\]\]>//g;
  }
}

print "Removing owl#Thing...\n";
#<rdfs:subClassOf rdf:resource="http://www.w3.org/2002/07/owl#Thing"/>
for( $i = 0; $i < $owlLineCount; $i++) {
	if ($owlLines[$i] =~ /.*owl#Thing.*/){
#		print $owlLines[$i];
		$owlLines[$i] = "";
	}
}

print "Removing xmlns:Thesaurus...\n";
# Making the assumption that the header will be less than 1000 lines long
for( $i = 0; $i < $owlLineCount; $i++) {
	if ($owlLines[$i] =~ /.*xmlns:Thesaurus.*/){
#		print $owlLines[$i];
		$owlLines[$i] = "";
	}
}

## Render literals
print "Rendering literals...\n";


for( $i = 0; $i < $owlLineCount; $i++ ) {
 $owlLines[$i] =~ s/rdf:datatype=\".*XMLLiteral\"/rdf:parseType=\"Literal\"/;
  if( $owlLines[$i] =~ /.*$xsdName:Complex.*/ ) {
          $owlLines[$i] =~ s/&lt;$xsdName:/<$xsdName:/g;
          $owlLines[$i] =~ s/&lt;\/$xsdName:/<\/$xsdName:/g;
  }
  if( $owlLines[$i] =~ /.*$xsdName:(.*)(>| ).*/ ) {
          $owlLines[$i] =~ s/&lt;$xsdName:/<$xsdName:/g;
          $owlLines[$i] =~ s/&lt;\/$xsdName:/<\/$xsdName:/g;
#     if( $type eq "def-definition" || $type eq "term-name" ) {
#         $owlLines[$i] =~ s/&amp;/&/g;
#         $owlLines[$i] =~ s/>/><![CDATA[/;
#         $owlLines[$i] =~ s/<\//]]><\//;
#     }
  }
}

# Remove QNames for Thesaurus
# print "Removing Thesaurus QNames...\n";
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#   if( $owlLines[$i] =~ /(.*)(<Thesaurus:)(.*\n)/ ) {
#     $owlLines[$i] = $1."<".$3;
#   }
#   if( $owlLines[$i] =~ /(.*)(<\/Thesaurus:)(.*\n)/ ) {
#     $owlLines[$i] = $1."</".$3;
#   }
# }

## Fix associations
print "Fixing associations...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)<(.*) rdf:datatype="http:\/\/www.w3.org\/2001\/XMLSchema#anyURI".*\n/ ) {
    my $spacing = $1;
    my $tag = $2;
    $owlLines[$i] = "";
    $i++;
    ## It should...
    if( $owlLines[$i] =~ /.*>(.*)<\/$tag>.*\n/ ) {
        $owlLines[$i] = $spacing."<$tag rdf:resource=\"".$1."\"\/>\n";
    }
  }
}

## Fix DeprecatedClasses
print "Fixing DeprecatedClasses...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)<owl:Class rdf:about="#(.*)">\n/ ) {
    my $backTrack = $i;
    my $spacing = $1;
    my $concept = $2;
    my $hasRetired = "false";
    while( $owlLines[$i] !~ /$spacing<\/owl:Class>\n/ ) {
      $i++;
      if( $owlLines[$i] =~ /(.*)<rdfs:subClassOf rdf:resource="#Retired_Concept.*"\/>\n/ ) {
         $hasRetired = "true";
      }
      if( $owlLines[$i] =~ /.*<rdf:type rdf:resource="http:\/\/www.w3.org\/2002\/07\/owl#Class"\/>\n/ ) {
         $owlLines[$i] = "";
      }
    }
    if( $hasRetired eq "true" ) {
      $owlLines[$backTrack] = "$spacing<owl:DeprecatedClass rdf:about=\"#$concept\">\n";
      $owlLines[$i] = "$spacing    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Class\"/>\n$spacing</owl:DeprecatedClass>\n";
    }
  }
}

# print "Removing empty qualifiers...\n";
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#   $owlLines[$i] =~ s/<$xsdName:[a-z-]+><\/$xsdName:[a-z-]+>//g;
# }

print "Fixing introduced comments...\n";
my $startComment = "<!--";
my $endComment = "-->";
for( $i = 0;  $i <$owlLineCount; $i++) {
	if ($owlLines[$i] =~ /.$startComment(.*?)$endComment/) {
		$owlLines[$i]='';
	}
}
print "Printing output...\n";
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $out $owlLines[$i];
}