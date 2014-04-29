#!/usr/bin/perl
#
# compare strings file
# usage: strings.pl <en-strings_file> <xx-strings_file>
#
# reports:
#    strings that are missing form xx-strings
#    strings that appear in xx-strings but are missing in en-strings
#
open( EN, "$ARGV[0]" ) or die "Cannot open reference strings file $ARGV[0]\n";
open( XX, "$ARGV[1]" ) or die "Cannot open strings file $ARGV[1]\n";

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
    $en_strings{ $name } = 1;
    $in_comment = 2;
  } else {
    $en_strings{ $name } = $in_comment;
  }
}
close EN;
   
$in_comment = 2;
while ( $line = <XX> ) {
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
    $xx_strings{ $name } = 1;
    $in_comment = 2;
  } else {
    $xx_strings{ $name } = $in_comment;
  }
}
close XX;

# print "Checking xx against en\n";
foreach $k (sort(keys(%xx_strings))) {
  $en_check = $en_strings{ $k };
  $xx_check = $xx_strings{ $k };
  # print "$k XX <$xx_check> EN <$en_check>\n";
  if ( "$en_check" ne "$xx_check" ) {
    # print "$k <$xx_check> <$en_check>\n";
    if ( $en_check eq "" ) {
      if ( $xx_check eq "1" ) {
        $comment_missing{ $k } = 1;
      } elsif ( $xx_check eq "2" ) {
        $active_missing{ $k } = 1;
      }
    } elsif ( $en_check eq "1" ) {
      if ( $xx_check eq "2" ) {
        $active_comment{ $k } = 1;
      }
    } elsif ( $en_check eq "2" ) {
      if ( $xx_check eq "1" ) {
        $comment_active{ $k } = 1;
      }
    }
  }
}


# print "Checking en against xx\n";
foreach $k (keys(%en_strings)) {
  $en_check = $en_strings{ $k };
  $xx_check = $xx_strings{ $k };
  if ( "$en_check" ne "$xx_check" ) {
    # print "$k <$xx_check> <$en_check>\n";
    if ( $en_check eq "1" ) {
      if ( $xx_check eq "" ) {
        $missing_comment{ $k } = 1;
      } elsif ( $xx_check eq "2" ) {
        $active_comment{ $k } = 1;
      }
    } elsif ( $en_check eq "2" ) {
      if ( $xx_check eq "" ) {
        $missing_active{ $k } = 1;
      } elsif ( $xx_check eq "1" ) {
        $comment_active{ $k } = 1;
      }
    }
  }
}

print "xx COMMENT - en MISSING\n";
foreach $k (keys(%comment_missing)) {
  print "$k\n";
}
print "xx ENABLED - en MISSING\n";
foreach $k (keys(%active_missing)) {
  print "$k\n";
}
print "xx COMMENT - en ENABLED\n";
foreach $k (keys(%comment_active)) {
  print "$k\n";
}
print "xx ENABLED - en COMMENT\n";
foreach $k (keys(%active_comment)) {
  print "$k\n";
}
print "xx MISSING - en ENABLED\n";
foreach $k (keys(%missing_active)) {
  print "$k\n";
}
print "xx MISSING - en COMMENT\n";
foreach $k (keys(%missing_comment)) {
  print "$k\n";
}

