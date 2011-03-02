#!/bin/sh
#
echo "extracting lexicon strings from QTopo into file \"lexicon\""
grep "lexicon(\"" ../QTcalib*/*.cpp ../QTcalib*/*.h ../QTshot*/*.h ../QTshot*/*.cpp \
  | sed -e 's/lexicon/\nlexicon/mg' \
  | grep lexicon \
  | sed -e 's/lexicon(\"//' \
  | sed -r -e 's/".*//' \
  | sort | uniq > lexicon

echo "extracting languages lexicon strings into files \"lexicon-xx\""
cat language-en.txt | sort | awk '{ print $1 }' | sort > lexicon-en
cat language-it.txt | sort | awk '{ print $1 }' | sort > lexicon-it

echo "extracting lexicon strings from the file \"Language.cpp\" in file \"lexicon-00\""
grep Insert ../utils/Language.cpp | sed -e 's/  Insert( "//' | sed -r -e 's/".*//' | sort | uniq > lexicon-00

