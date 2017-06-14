## Rob Wynne, LMCO
##
## Convert an NCIT by name OWL file (from OWLScrubber/OWLAPI output) to by code

use strict;
use LWP::Simple;
use URI;
my $url=URI->new($ARGV[0]);
print $url->path();
open (my $owlfile, $url->path()) or die "Couldn't open OWL file";
##open( my $owlfile, $ARGV[0] ) or die "Couldn't open OWL file!\n";
open my $out, '>', $url->path."-byCode.owl" or die "Couldn't create output file!\n";
open my $debug, '>', "debug.txt" or die "Couldn't create debug file!\n";

my @owlLines = <$owlfile>;
my $owlLineCount = @owlLines;
my %map;
my $i;

for($i = 0; $i < $owlLineCount; $i++) {
  if( $owlLines[$i] =~ /(.*)<owl:(Class|DeprecatedClass|DatatypeProperty|FunctionalProperty|ObjectProperty|AnnotationProperty) rdf:about="#(.*)">.*\n/ ) {
    my $space = $1;
    my $type = $2;
    my $name = $3;
    my $code = "";
    $i++;
    while( $owlLines[$i] !~ /$space<\/owl:$type>.*\n/ ) {
      if( $owlLines[$i] =~ /.*<owl:equivalentClass>.*\n/ ) {
        while( $owlLines[$i] !~ /.*<\/owl:equivalentClass>.*\n/ ) {
          $i++;
        }
      }
      if( $owlLines[$i] =~ /.*<rdfs:subClassOf>.*/ ) {
        while( $owlLines[$i] !~ /.*<\/rdfs:subClassOf>.*/ ) {
          $i++;
        }
      }
      if( $owlLines[$i] =~ /.*<code rdf:datatype.*>(.*)<\/code>.*\n/ ) {
        $code = $1;
      }
      $i++;
    }
    if( $code eq "" ) {
      print "WARNING: No code for concept $name\n";
    }
    else {
      $map{$name} = $code;
    }
  }
}

for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)<owl:(Class|DeprecatedClass|DatatypeProperty|FunctionalProperty|ObjectProperty|AnnotationProperty) rdf:about="#(.*)">.*\n/ ) {
      my $space = $1;
      my $type = $2;
      my $name = $3;
      if( exists $map{$name} ) {
        $owlLines[$i] = $space."<owl:".$type." rdf:about=\"#".$map{$name}."\">\n";
      }
      else {
        print "WARNING: Unconverted class $name\n";
      }
  }
# RWW - Keep code property
#   elsif( $owlLines[$i] =~ /.*<code.*<\/code>.*\n/ ) {
#     $owlLines[$i] = "";
#   }
  elsif( $owlLines[$i] =~ /(.*)(<.*)="#(.*)("\/>.*\n)/ ) {
    my $space = $1;
    my $beginning = $2;
    my $name = $3;
    my $end = $4;
    if( exists $map{$name} ) {
      $owlLines[$i] = $space.$beginning."=\"#".$map{$name}.$end;
    }
    else {
      print "WARNING: Unconverted axiom \n\t$owlLines[$i]";
    }
  }
  ## RWW 091006 - complex properties
  elsif( $owlLines[$i] =~ /(\s*>.*ncicp:Complex.*><\/)([A-Za-z0-9_-]*)(.*\n)/ ) {
    my $beginning = $1;
    my $property = $2;
    my $end = $3;
    if( exists $map{$property} ) {
      $owlLines[$i] = $beginning.$map{$property}.$end;
    }
  }
  elsif( $owlLines[$i] =~ /(.*<)([A-Za-z_0-9-]*)(.*<\/)([A-Za-z0-9_-]*)(>.*\n)/ ) {
    my $beginning = $1;
    my $property = $2;
    my $filler = $3;
    my $propertyEnd = $4;  ## Really though, this should be the same as $property.
    my $end = $5;
    if( exists $map{$property} ) {
      $owlLines[$i] = $beginning.$map{$property}.$filler.$map{$property}.$end;
    }
  }
  elsif( $owlLines[$i] =~ /(\s*>.*<\/)([A-Za-z0-9_-]*)(.*\n)/ ) {
    my $beginning = $1;
    my $property = $2;
    my $end = $3;
    if( exists $map{$property} ) {
      $owlLines[$i] = $beginning.$map{$property}.$end;
    }
  }
  elsif( $owlLines[$i] =~ /(\S*<\/)([A-Za-z_0-9-]*)(.*\n)/ ) {
    my $beginning = $1;
    my $property = $2;
    my $end = $3;
    if( exists $map{$property} ) {
      $owlLines[$i] = $beginning.$map{$property}.$end;
    }
  }
  elsif( $owlLines[$i] =~ /[^>]/ ) {
    if( $owlLines[$i] =~ /(.*<)([A-Za-z_0-9-]*)(.*\n)/ ) {
      my $beginning = $1;
      my $property = $2;
      my $end = $3;
      if( exists $map{$property} ) {
        $owlLines[$i] = $beginning.$map{$property}.$end;
      }
    }
  }
  else {
    ##do nothing
  }
}

## Convert association values
for( $i = 0; $i < $owlLineCount; $i++ ) {
  if( $owlLines[$i] =~ /(.*)(<A[0-9]+ rdf:resource=".*#)(.*)("\/>.*\n)/ ) {
    my $space = $1;
    my $beginning = $2;
    my $value = $3;
    my $end = $4;
    if( exists $map{$value} ) {
        $owlLines[$i] = $space.$beginning.$map{$value}.$end;
    }
    else {
      print "WARNING: Unconverted association with value $value\n";
    }
  }
}

# Uncomment me to remove datatype bloat.
# Note: This was tried for a load, but didn't seem to help
# peformance or representation in LexBIG
# 
# for( $i = 0; $i < $owlLineCount; $i++ ) {
#   if( $owlLines[$i] =~ /(.*)( rdf:datatype=".*")\n/ ) {
#     print $debug $owlLines[$i];
#     $owlLines[$i] = $1."\n";
#   }
#   elsif( $owlLines[$i] =~ /(.*)( rdf:datatype=".*")(.*)\n/ ) {
#     print $debug $owlLines[$i];
#     $owlLines[$i] = $1.$3."\n";
#   }
# }

## Create output
for( $i = 0; $i < $owlLineCount; $i++ ) {
  print $out $owlLines[$i];
}

# for my $key ( sort keys %map ) {
#         my $value = $map{$key};
#         print $debug "$key => $value\n";
# }