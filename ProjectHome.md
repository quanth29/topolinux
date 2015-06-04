**IMPORTANT NOTICE**

**From January 15, 2013 it will no longer be possible to upload files for download on Google Code. The latest versions of these packages can be found on the TopoDroid site
https://sites.google.com/site/speleoapps/home**

**After August 2015 this size will not be updated any longer. The data contained at that time will remain available until early 2016, when the site will be shut down.**


---


TopoLinux consists of cave surveying applications
for Linux PC and Android devices.

**TopoDroid** is an Open Source Android app to make cave surveys with the DistoX.

Features:
  * communication with the DistoX device(s),
  * support A3 and X310 DistoX
  * data stored in a SQLite DB.
  * surveys management: stations entry, notes and comments.
  * exported survey formats: Therion, Compass, Survex, VisualTopo, DXF, csv.
  * imported survey formats: Therion.
  * sketch drawing, Therion-wise with points (symbols), lines and areas. Saved as th2 files, PNG images, and DXF files.
  * user-defined drawing symbols.
  * save whole survey as ZIP archive, and restore from it.
  * 3D display (using Cave3D).
  * photoes and GPS localization (using GPSaverage).
  * internal sensors measures and external measures.
  * DistoX calibration.

Any help is appreciated.

TopoDroid discussion forum: https://groups.google.com/d/forum/topodroid

TopoDroid website: https://sites.google.com/site/speleoapps/home



---


**Cave3D** is an Android 3D visualizer for Therion centerline data
(and splay shots). It is necessary for 3D display by TopoDroid.


---


**Proj4** is an Android app to convert geo coordinates between different
coordinate systems. It is based on the proj4 library, and coordinate systems
must be specified with the proj4 syntax.


---


**InGrigna** is an Android app to access the speleological database
of the Grigne from the field. The data cover caves, sources,
local maps (CTR), reports, publications. Scans of printed material, photoes, and surveys
are included.
The coordinate reference system is the local kilometric Gauss-Boaga.


---


The **TopoLinux** project itself: a set of command line utilities for
PC to work with the DistoX,

**QTopo** consists of two graphical programs for PC,
_qtshot_ and _qtcalib_, based on the Qt4 libraries.

**qtshot** is the graphical interface for surveying with the
[DistoX](http://paperless.bheeb.ch/).
The survey data can be exported in
[Therion](http://therion.speleo.sk/) format (th file),
as well as in other formats (Survex, Compass, PocketTopo).

The interface to draw Therion scraps around the centerline
is a minimal Therion drawing editor.
It supports several Therion point types,
line types and area types. Others can be easily added if needed.
Multiple plots (th2 files) can be handled, each with one or more scraps.
The drawings are saved as th2 files, and can be exported as PNG
image files.

**qtcalib** is the graphical interface to calibrate the DistoX.

NOTES
  * The PC programs have been developed for Linux, but they compile also on Windows (MSVC 2008).
  * The source package in the download area contains the latest contributions.
  * Linux and Windows precompiled packages are not really uptodate, and may have bugs and missing features: check the CHANGES logfile (in the wiki) and the package date.
  * The browsable sources are not really uptodate either.
  * Support for Qt3 has been discontinued early 2011.
