#!/usr/bin/perl
#
# extract keys from strings file
# usage: strings.pl <en-strings_file>
#
#
open( EN, "$ARGV[0]" ) or die "Cannot open reference strings file $ARGV[0]\n";

$in_comment = 2;
while ( $line = <EN> ) {
  if ( $line =~ /<!--/ ) { 
    $in_comment = 1;
  }
  if ( $in_comment == 1 and $line =~ /-->/ ) {
    $in_comment = 3;
  }
  next if not $line =~ /name="/;
  chop $line;
  # print "LINE $line";
  $name = $line;
  $name =~ s/^.*name="//;
  $name =~ s/".*$//;
  if ( $in_comment == 3 ) {
    $in_comment = 2;
  }
  if ( $in_comment == 2 ) {
    print "$name\n";
  }
}
close EN;
   
