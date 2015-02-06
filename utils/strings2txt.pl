#!/usr/bin/perl
#
# extract itext file from strings files
# usage: strings.pl
#
# english string file is ../res/values/strings.xml
# other languages strings files in ../int18/values-XX/strings.xml
#
#
use Encode qw(decode encode);
$warn = $ARGV[0];

$EN = "../res/values/strings.xml";
@LANG = qw' de es fr it pt ';

open( EN, $EN ) or die "Cannot open reference EN strings file $EN\n";
open( OUT, '>strings.txt');

# binmode EN,  ':encoding(UTF-8)';
# binmode OUT, ':encoding(UTF-8)';

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
  $value = $name;
  $value =~ s/<\/.*$//;
  $value =~ s/^.*>//;
  $name =~ s/".*$//;
  if ( $in_comment == 2 ) {
    # print "<$name> $value\n";
  } else {
    $comment{$name} = 1;
  }
  if ( $warn ) {
    print "en <$name> $value\n";
  }
  $en{$name} = $value;

  if ( $in_comment == 3 ) {
    $in_comment = 2;
  }
}
close EN;

# no strict 'refs';

for ( $k=0; $k<@LANG; ++$k ) {
  $lang = $LANG[$k];
  $DAT = "../int18/values-" . $lang . "/strings.xml";
  if ( $warn ) {
    print "PROCESSING $lang: $DAT\n";
  }

  if ( open( DAT, "$DAT") ) {
    while ( $line = <DAT> ) {
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
      $value = $name;
      $value =~ s/<\/.*$//;
      $value =~ s/^.*>//;
      $name =~ s/".*$//;
      if ( $in_comment == 3 ) {
        $in_comment = 2;
      }
      
      if ( not $en{$name} ) {
        if ( $warn ) {
          print "WARNING $lang <$name>\n";
        }
      } else {
        if ( $line =~ /TODO/ ) {
          $value = $value . " TODO";
        } elsif ( $line =~ /OK/ ) {
          $value = $value . " OK";
        } elsif ( $line =~ /!--/ ) {
          $value = $value . " COMMENTED";
        }

        # $lang -> {$name} = decode('iso-8859-1', $value, Encode::FB_CROAK);
        $lang -> {$name} = $value;
        # if ( $warn ) {
        #   $v = $lang->{$name};
        #   print "ADDING $lang <$name> $v\n";
        # }
      }

    }
    close DAT;
  } else {
    print "Cannot open  $DAT \n";
  }
}

if ( $warn ) {
  print "---------------------- BEGIN HERE ------------------\n";
}

foreach $name ( keys(%en) ) {
  if ( $comment{$name} ) {
    print OUT "# TAG $name IGNORE\n";
  } else {
    print OUT "TAG $name\n";
  }
  print OUT "EN $en{$name}\n";
  for ( $k=0; $k<@LANG; ++$k ) {
    $lang = $LANG[$k];
    # $v = encode('iso-8859-1', $lang->{$name}, Encode::FB_CROAK);
    $v = $lang->{$name};
    print OUT "$lang $v\n";
  }
  print OUT "\n";
}
   
