#!/bin/sh
#
cat "./keys" |
while read line
do
  grep -q $line ../res/layout/* ../src/com/topodroid/DistoX/*.java ../res/xml/*
  if [ $? = 0 ]; then
    # echo "found $line"
    . 
  else
    echo "NOT FOUND $line"
  fi
done

