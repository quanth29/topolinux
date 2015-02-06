#!/usr/bin/perl
#
# check th2 file for multiple outline
#
open( TH2, $ARGV[0] ) || die "Cannot open th2 file \"$ARGV[0]\"\n";

$cnt = 0;
while ( $line = <TH2> ) {
  next if ( not $line =~ /line wall/ );
  $k = 0;
  <TH2>; # skip the first line
  $prev = "";
  while ( $line = <TH2> ) {
    last if ( $line =~ /endline/ );
    $line =~ s/^\s+//;
    $line =~ s/\s+$//;
    if ( length($prev) > 0 ) {
      ${$wall[$cnt]}[$k] = $prev;
      ++ $k;
      for ( $c = 0; $c < $cnt; ++$c ) {
        for ( $kk = 0; $kk < $size[$c]; ++$kk ) {
          if ( ${$wall[$c]}[$kk] eq $prev ) {
            print "$cnt $c ${$wall[$c]}[$kk] $prev\n";
            last;
          }
        }
      }
    }
    $prev = $line;
  }
  $size[$cnt] = $k;
  ++$cnt;
}
close TH2
