.mode insert surveys
.output x.tmp
select id,name,day,team,0.0,comment from surveys;
.output stdout
drop table surveys;
create table surveys ( id INTEGER, name TEXT, day TEXT, team TEXT, declination DOUBLE, comment TEXT );
.read x.tmp
