## TopoDroid Cross-Sections ##

This page describes cross-sections in TopoDroid v. 2.1.8.

In TopoDroid 2.1.8 each sketch comprises a plan view and an
extended elevation view. To draw a cross-section you must
draw a "section" line on one of the two views.
The section line is displayed as a straight dashed white segment.

The cross-section has an orientation; this is shown by the
tick on the left end of the section line.
If you draw the section line in the plan view the cross-section
is vertical and its direction is perpendicular to the section line.
If you draw the section line in the extended elevation view
the cross-section can be either vertical or horizontal, depending on
whether the inclination of the leg shot it crosses is below 60 degrees
or not. (This threshold can be modified with the options dialog).
In this case a vertical cross-section has orientation the azimuth of the
leg shot it crosses, while an horizontal cross-section is oriented
either downwards (-90 degrees) or upwards (+90 degrees).

The cross-section displays a 1 meter grid and the shots (both splay
and leg) at the station
behind the cross-section. For example if you draw a cross-section
across the leg 1-2 oriented from station 1 to station 2, ie, as if you were staying at
station 1 and looked at station 2, the cross-section dialog displays the
shots at station 2.

## Notes ##

If the section line does not cross any leg shot, or if it crosses
more than one leg shot, the program cannot find the station "behind"
the cross-section and no shot is displayed.