## How to convert TopoDroid zip from DB 7 to DB 8 ##

This page describes how to convert zip archives produced with
TopoDroid 1.0.7 for TopoDroid v. 1.1.0.

# Details #

TopoDroid v. 1.0.7 uses the database version 7, namely "distox7.db".
Version 1.1.0 uses the database version 8, with file "distox8.db".
The two databases have different table definitions for "photoes", "plots"
and "shots". To convert the sql file in a zip archive
made with TopoDroid 1.0.7 so that it can be loaded in TopoDroid 1.1.0
one need to add values for the new fields.
One must also modify the DB version number in the file "manifest".

Here is a simple perl script that does these.
```
   mkdir tmp
   cd tmp
   unzip ../archive.zip
   ../distox7to8.pl
   zip ../archive.zip ./
```

---
```
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
open( OUT, ">x");
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
system( "mv x manifest" );

open( DAT, "survey.sql") or usage and die "Cannot find file \"survey.sql\"\n";
open( OUT, ">y");
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
system( "mv y survey.sql" );
```