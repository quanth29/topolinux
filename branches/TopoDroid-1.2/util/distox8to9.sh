#!/bin/sh
#
echo "create table devices ( address TEXT, head INTEGER, tail INTEGER  );" | sqlite3 distox8.db 

mv distox8.db distox9.db

