## TopoDroid and PocketTopo ##

It's hard to tell the difference between TopoDroid and PocketTopo,
especially because my knowledge of PocketTopo come only from the
available documentation.

Help to this page is appreciated.

### Survey data ###

It seems to me that the two programs have a different approach to the
organization of the survey data; they carry the footprint of
the PC programs that inspired them, namely Therion and TopoRobot, respectively.

Therion leaves the user quite free in the choice of the names for the stations;
it has an hierarchical organization in which a station belongs to a "survey",
which in turn can belong to another survey, and so on. There is non concept of cave, neither of cave systems: a cave is just a survey that may contain other
surveys, if the cave is complex. Rephrasing... in Therion "Everything is a survey".

TopoRobot has a two-level hierarchy: at the top level there are caves (directories), which contain surveys (files); the surveys have trips: rows of connected stations. The trip cannot have loops; the cave loops are made of
several trips (or portion of them). The trips of a cave must form a connected
network.

PocketTopo manages the survey work on an entire cave. TopoDroid is focussed on individual surveys, and does not address the problem of putting them together.

The workflow of the two programs reflects these differences.
In PocketTopo you work in a cave, opening a survey of it at a time and working
on the data of the survey. The TopoDroid basic unit is the "survey", these are supposed to be composed to form the cave suevey with another program (most likely on a PC).



**Fixed stations**

In PocketTopo the user can specify the world coordinates of stations
(in a coordinate system ?).

TopoDroid can assign world coordinates (longitude, latitude, and altitude) to stations, but these are not used by the program. They are however written in the
exported survey file, in particular in therion files.

**Export and import**

PocketTopo saves the survey in its own format ".top". Survey data can be exported as TopoRobot, Therion, and text files.
It can import TopoRobot files.

TopoDroid stores the survey data in a SQLite database. The data cen be
exported as Survex, Compass, Therion and VisualTopo files. It can import
Therion files. Surveys can also be archived in a zip file that can be
reloaded in the program. The zip file includes also the drawings, the
photos and all the data associated to the survey.


... to continue ...

### Sketch drawing ###

In PockeTopo the user can draw the survey outline and side-view.
The first is a plan map, the second is an extended (?) elevation map.
Cross-section can be added to the drawings.

Drawings is aided by the display of the centerline, the splay shots, the
station names,
and a reference grid. The drawings elements are lines, with a choice
among (six ?) different colors, and symbols (?).

PocketTopo has an "eraser" tool to delete lines.

PocketTopo drawings are included in the PocketTopo saved .top file.
The drawings can be exported as DXF files.

In TopoDroid the user can draw as many sketches as needed for each survey.
The sketch types are plan, extended elevation and cross-section.

Drawing aids are the centerline, the spaly shots, the station names, and
a reference grid. The drawing elements are Therion point symbols (about 24
symbols are supported), lines (about ten types), and areas (about 6 type).
Although TopoDroid does not support the full syntax of therion,
some symbol options are supported. Drawing items can be selected and deleted.

TopoDroid drawings are saved as therion files or PNG image files.

... to continue ...

### Calibration ###

Both TopoDroid and PocketTopo can toggle the DistoX in calibration mode,
calculate the coefficients of a calibration, and write them to the DistoX.

TopoDroid treats the calibration data in groups of "same" azimuth and inclination (with several policies for assigning the data to groups). PocketTopo consider the first sixteen data as
forming four groups of four data, and the following one as independent data.

The calibration algorithm is the same in both program, and the computation
of the calibration coefficients gives similar results.

TopoDroid has a graphical display of the spherical distribution of the calibration data, that can help the user to find direction that are not covered in the data set.

### DistoX connection ###

The connection to the DistoX depends not only on the two programs but also on the underlying systems.

In TopoDroid the data download is always initiated by the user (with a menu), and ends when the available data have been downloaded. I had problems keeping
the program always connected to the DistoX because of the timeout with which the system reported a failure in the connection. This was too long and the TopoDroid hanged annoyingly without downloading anything, before
realizing there was a problem and reporting it to the user (and trying to reconnect).

There are reports of communication problems between PocketTopo and the DistoX.
These are usually solved with a reset of the connection.
I do not have enough experience to tell more about this.

There are android devices that could not connect to the DistoX. This is a
problem of the device, because establishing a connection fails even for the
_Settings_ app.
There is a list of working and not-working devices. Hopefully this
list will become more and more complete as users provide their feedbacks.

### The Android environment ###

An app in the android environment can use other apps.
This integration allows to extend TopoDroid with the functionalities that
go one step beyond those of a survey program.
The use of the app _Cave3D_ for the 3D display of survey data is an
important addon for the in-cave surevy.

TopoDroid uses also the _Camera_ app to take snapshots
(need an independent source of light), and attach them to the survey.
Finally TopoDroid can register measures (sensor values) associating the
record to survey shot.

In a way TopoDroid is going off  from a survey app towards an app to collect data in the cave localized both in time (timestamped) and in space
(through the association to the survey shot).