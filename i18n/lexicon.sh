#!/bin/sh
#
grep "lexicon(\"" ../QTcalib*/*.cpp ../QTcalib*/*.h ../QTshot*/*.h ../QTshot*/*.cpp \
  | sed -e 's/lexicon/\nlexicon/mg' \
  | grep lexicon \
  | sed -e 's/lexicon(\"//' \
  | sed -r -e 's/".*//' \
  | sort | uniq > lexicon

cat language-en.txt | sort | awk '{ print $1 }' | sort > lexicon-en
cat language-it.txt | sort | awk '{ print $1 }' | sort > lexicon-it

grep Insert ../utils/Language.cpp | sed -e 's/  Insert( "//' | sed -r -e 's/".*//' | sort | uniq > lexicon-00

