# Cave3D #

Cave3D is an Android app that displays 3D views of cave centerlines.


## Version 3.0 ##

Cave3D displays 3D views of survey data in Therion format.
The opened file is parsed as it were "source-d" in Therion, ie,
recursively going to the files that are "input-ed".

The app displays centerline, splay shots (no LRUD), and
stations.

The display can be
  * zoomed in/out
  * moved,
  * rotated.

The cave centerline is displayed in perspective view.
The camera viewing angle has only two degrees of freedom (azimuth
and clino, no roll). To changed the clino rotate the view
vertically; to change the azimuth rotate it horizontally.

The reference frame can be displayed either as horizontal grid
(blue along north, green along east), or three-axis frame (pink
vertical). The horizontal frame lines are lighter when viewed from
underneath.

The display of splay shots (grey) can be toggled on and off, as
can the diaplay of the station.

The cave can be colored by depth, by survey, or by distance from
the camera (default).


## Note on version 1.0 ##

Version 3.0 uses Android canvas for the display.

Version 1.0 uses the openGL. It can show either the perspective view
or the orthogonal view, but it cannot display the station names.