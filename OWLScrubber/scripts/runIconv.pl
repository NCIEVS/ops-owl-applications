use strict;
my $owl = $ARGV[0];
$owl =~ s/file:\/\///g;
system("iconv -futf-8 -tASCII//translit $owl > $owl-iconv.owl");