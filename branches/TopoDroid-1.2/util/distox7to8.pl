#!/usr/bin/perl
#
# convert sqlite3 command from distox7db to distox8db
#
sub usage()
{
  print "Convert sqlite3 command from distox7db to distox8db\n\n";
  print "Usage: \n";
  print "  - unzip the archive in a temporary directory\n";
  print "  - cd to the temporary directory\n";
  print "  - execute this script\n";
  print "  - recreate the archive\n";
}

open( DAT, "manifest") or usage() and die "Cannot find file \"manifest\"\n";
open( OUT, ">xxx");
$line = <DAT>; # TopoDroid version
print OUT $line;
$line = <DAT>; # DB version
chop $line;
( $line == "7" ) or die "Database version is \"$line\". It should be \"7\"\n";
print OUT "8\n";
$line = <DAT>;
print OUT $line;
close DAT;
close OUT;
system( "mv xxx manifest" );

open( DAT, "survey.sql") or usage and die "Cannot find file \"survey.sql\"\n";
open( OUT, ">yyy");
while ( $line = <DAT> ) {
  chop $line;
  @v = split(/ /, $line);
  if ( $v[2] eq "photos" ) {
    print OUT "$v[0] $v[1] $v[2] $v[3] $v[4] $v[5] $v[6] $v[7] $v[8] \"0000-00-00\", $v[9] );\n"; 
  } elsif ( $v[2] eq "plots" ) {
    print OUT "$v[0] $v[1] $v[2] $v[3] $v[4] $v[5] $v[6] $v[7] $v[8] $v[9] $v[10], 0.00, 0.00, 1.00 );\n";
  } elsif ( $v[2] eq "shots" ) {
    print OUT "$v[0] $v[1] $v[2] $v[3] $v[4] $v[5] $v[6] $v[7] $v[8] $v[9] $v[10] $v[11] $v[12] $v[13] 0, $v[14] $v[15] );\n";
  } else {
    print OUT "$line\n";
  }
}
close DAT;
close OUT;
system( "mv yyy survey.sql" );
