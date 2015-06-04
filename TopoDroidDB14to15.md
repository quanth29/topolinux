### TopoDroid DB from v. 14 to v. 15 ###

From version 2.2.7 TopoDroid uses the database v. 15.
This differs from v. 14 only in the table "surveys"
which has a column for the declination.

This page describes how to upgrade the database distox14.sqlite
to v.15, without loosing your data.

[1](1.md) download the database to a PC. If you prefer to work on your
android make a backup copy.

[2](2.md) apply the following SQLite commands:
```
  .mode insert surveys
  .output x.tmp
  select id,name,day,team,0.0,comment from surveys;
  .output stdout
  drop table surveys;
  create table surveys ( id INTEGER, name TEXT, day TEXT, team TEXT, 
     declination DOUBLE, comment TEXT );
  .read x.tmp
```

Make sure you can write the temporary file "x.tmp".
The easiest way is to write the above command in a file, say "14-15.sql", and send it to sqlite:
```
   sqlite3 disto14.sqlite < 14-15.sql
```

You can also read it from the sqlite prompt:
```
   sqlite> .read 14-15.sql
```

[3](3.md) Delete the temporary file "x.tmp" and rename the database to distox15.sqlite:
```
   mv distox14.sqlite distox15.sqlite
```

[4](4.md) Upload the new database to the android.