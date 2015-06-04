## Upgrading TopoDroid Database from v.13 to v.14 ##

TopoDroid database v.14 differs from v.13 by a column in the table "plots".

TopoDroid v. 2.1.8 will not load zip archives made with previous versions
of the database.



## Details ##

Download the database v. 13:
  * adb pull /mnt/sdcard/topodroid/distox13.sqlite

Add the following column to the table "plots"
  * sqlite3 distox13.sqlite
  * > alter table plots add column azimuth REAL;
  * > .quit

Rename the database
  * mv distox13.sqlite distox14.sqlite

Upload the database
  * adb push distox14.sqlite /mnt/sdcard/topodroid