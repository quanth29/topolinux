## DISCLAIMER ##

This code is provided under GPL v.2 licence.
It as provided as it is, with no guarantee.
I do NOT take resoponsability for any loss of your data.

This software is VERY experimental and unstable.
If you plan to use it for a real cave survey,
you should be ready to use TopoLinux command line tools
to recover your data, in case something goes wrong.
I repeat: I do NOT take resoponsability for any loss
of your data.

## IMPORTANT NOTICE ##

TopoDroid v. 0.7.X uses the database "distox4.db".
TopoDroid v. 1.0.X uses the database "distox6.db".
The two databases have different table schema.


## INSTALL and COMPILE ##

To install the compiled app on a device, copy (or download) the latest apk on the
sdcard.

If you really want to compile the package from the sources you need an android SDK.
Edit the AndroidMakefile.xml to suit your device.
Then compile and install
```
   $ ant debug
   # adb install -r bin/DistoX-debug.apk
```

TopoDroid requires bluetooth, and cannot run on a droid emulator.
You need a real device.

On the first run TopoDroid creates the directory TopoDroid,
wher it puts its subdirectories, and makes an empty database.
Your data will be saved here and will not be erased when you uninstall the app.
You should download the data to your PC now and then, and clean them from the
Android.
Starting with v. 1.0 you can
delete all the data and files of a survey (or a calibration) from TopoDroid:
make a backup on a PC before deleting a survey.

The location of the base directory TopoDroid, can be set with the program
configuration options. By default it is placed in the "external storage".

## HOWTO (v. 1.0) ##

When you start TopoDroid the first time you are presented with a greeting screen.
This will not show again; to see is select the "About" menu.
The main TopoDroid window shows the list of surveys or that of calibrations.
You can toggle between the two list by tapping the buttons on the top.

The main widow menus:
  * DistoX
  * New
  * Import
  * More (Help, Options, About)

The "DistoX" menu opens the Device window. With this you can manage DistoX:
list the paired devices, scan for new ones, reset TopoDroid BlueTooth.
TopoDroid connects to one device at a time: the current device can be changed by selecting a new one from the list of paired (or scanned) devices.
The name of the current DistoX device is displayed in the title bar.

The data transfer between the DistoX and TopoDroid is always initiated by TopoDroid on user request. In particular when you need to get some more data
from the DistoX, you press the Download menu, and wait for the data to be downloaded. The downloaded data go into the active survey or the active calibration depending on the type of the data.

The menu "New" creates a new survey/calibration. The menu "Import" is used to import an external Therion survey or the zip archive of a previous survey.

Tapping a survey in the list opens the Survey window. This displays the survey infos
(name, date, team, description) and a list of buttons:
  * Save: save any change to the info in the database
  * Open: opens the Shots window
  * Export: export the survey (available formats: Therion, Compass, VisualTopo, Survex)
  * Zip: creates a zip archive of the survey and all associated files
  * 3D: display the 3D
  * Notes: edit survey annotations
  * Location: manage fixed stations
  * Photos: ...
  * Delete: delete the survey and all associated data and files


The Shots window displays the list of survey shots. There are three types of shots:
leg (white) with both FROM and TO stations, splay (blue) with only FROM station, and blank (red) with no station. Groups of consecutive close shots are taken to make a centerline leg and usually only the first is displayed. The display of splay and blank shots can be turned off. Each shot is shown as
```
   id <from-to> distance compass clino [extend]
```

Station names can include digits and letters, as well as symbols.
To assign the stations to a shot tap on the shot and enter the stations in the shot dialog that appears. TopoDroid guesses the station names from those of the previous shot, so you have to make a change only if these are incorrect. Furthermore there is a menu (Survey|Number) to assign the FROM station to splay shots automatically, using that of the following leg.

The extend is one of '<' (left), '>' (right) or '|' vertical (enclosed in square brackets). The shot extend can be changed in the shot dialog.

An asterisk "`*`" follows the _extend_ if the shot is marked as "duplicate".
The duplicate flag can be toggled in the shot dialog. A dash "`-`" is used for "surface" shots.

With the shot dialog you can also add a brief note (comment) to the shot. Shots with comment are denoted in the list with an "N".

The "Scraps" menu lists the scraps of the current survey. Tapping on one of them opend the Drawing window.

On a scrap you can draw lines, areas, and points (speleo symbols). These are the analogue of the Therion lines, areas, and points. Currently only a few line, area, and point types are supported.

Available point typesi (Nov. 2011):
  * air-draught (orientable)
  * anchor
  * blocks
  * clay
  * continuation
  * crystal
  * danger
  * debris
  * dig
  * end: either narrow-end or low-end
  * entrance (orientable)
  * flowstone
  * gradient (orientable)
  * ice
  * label (with text)
  * pebbles
  * popcorn
  * sand
  * snow
  * stal: either stalactite or stalagmite (it can be rotated by 180 degrees)
  * water
  * water-flow (orientable)

Available line types (Nov. 2011):
  * wall (red)
  * pit (pink)
  * chimney (dark pink)
  * overhank (dark blue)
  * arrow (white)
  * rock-border (gray)
  * border (green)

Available area type:
  * blocks (dark brown)
  * clay (dark green)
  * debris (brown)
  * ice (grey)
  * pebbels (green)
  * sand (green)
  * water (blue)

The buttons on the drawing window allow to
  * toggle between moving the canvas or drawing on it
  * undo the last drawn object
  * zoom in/out
  * select the point symbol
  * select the line type
  * select the area type

Additionally there are the menus
  * redo
  * save the plot (scrap) as therion th2 file
  * export the plot as PNG image
  * toggle the display of the drawing references
  * stats
  * delete

With the "Calibs" tab of TopoDroid window you manage calibrations.
Selecting a calibration of the list, opens the Calibration window with the calibration info (name, date, device, description), and the buttons
  * Save
  * Open
  * Delete

Opening the calibration display the Calibration Data window.
The calibration data are displayed in the list as
```
   id <group> compass clino roll error
```
The group is used to define groups of data with the same direction: to assign
the group tap on the data line and edit the group name in the dialog.
The groups can be guessed with a menu in the "Calibs" option.
The calibration is computed with another menu in the "Calibs" option:
the calibration result is displayed, together with the average error,
and other infos. After the calibration has been computed it can be written
to the DistoX device.


The "Options" option allows to set some TopoDroid configuration options
and save them.
  * tolerance among centerline data to define a single shot
  * calibration minimum error
  * calibration maximum number of iterations
  * tolerance among calibration data to belong to the same group

## FURTHER DOCS ##

[TopoDroid Tips](TopoDroidTips.md)

[TopoDroid FAQ](TopoDroidFAQ.md)

[TopoDroid Troubleshooting](TopoDroidTroubleshooting.md)

[TopoDroid Working devices](TopoDroidDevices.md)

[TopoDroid DB](TopoDroidDB.md)

... adn the PDF docs in the download section.

## HELP AND BUGS REPORT ##

If you have a crash or the app does not work as expected, please send me
a logcat-dump. To generate a logcat-dump, connect a desktop with Android SDK
to yout device and run "adb logcat | grep DistoX".

If you discover a bug or a problem please file an issue on this site.

There is a discussion group at groups.google.com/d/forum/topodroid
wheer you can ask questions, and send comments and suggestions.
If you prefer feel free to drop me an email:
marco dot corvi at gmail dot com