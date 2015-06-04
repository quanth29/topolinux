# TopoDroid tips #

Tips and tricks to work with TopoDroid profitably.


## DistoX ##

DistoX devices are identified by their BlueTooth addresses.
Once you have selected a DistoX device, that device appear in the
options of the "DistoX" menu, so that you can go to the device
management interface with three taps (menus, the "DistoX" menu and
device option).

TopoDroid can be either connected or disconnected to a DistoX.
When it is connected it downloads the data. These are assigned to the
current survey or the current calibration. It is therefore **important** to
open a survey (and/or a calibration) before you connect to the DistoX.
Otherwise your data will be lost.

The other device functions (reading and writing the calibration, reading the
head/tail of the dat a queue, toggle the device in calibration mode)
works only when the device is "disconnected".

## Survey ##

Survey management is rather complex. TopoDroid offers a few options that
determine how the survey data are displayed. In particular TopoDroid
distinguishes between centerline shots (two or more consecutive shots
with similar values) and splay shots. Furthermore a shot is "blank" if its
stations have not been specified.

TopoDroid can automatically assign the "from" station to the splay shots between
two centerline shots. In this case the splay shots get the "from" station from the
second centerline shot. Therefore it's a good practice to take the splay shots
**before** the centerline shots when surveyin in the cave.

TopoDroid can export the survey data as Therion "th" file. The filename is the
same as the survey name (plus the extension ".th"), and the files are saved
in the "th" dircetory.

The exported Therion file misses a lot of important commands, in particular the
input command for the scrap files.

## Plots (scraps) ##

With TopoDroid you can draw survey scketches in the cave. TopoDrois scketches are
compliant Therion scraps, and should be drawn as such. Therion scraps have three
categories of drawing elements: points, lines and areas. Points are used for point-like features, mostly conventional speleological map symbols. Lines are linear features, like the walls of the cave. Areas are regions in the map filled with something, for example water, or blocks.

It is **important** that drawings are done Therion-style. In particular the
wall lines must circle the cave on the left, and the pits must leave the
drop on the left.

TopoDroid supports only a limited set of therion points, lines and areas. Furthermore, it does not supports many points and lines attributes (e.g. subtypes, alignment, size and scale), and areas are bounded by a single line. Therefore it can be used only to draw simple scraps. However any missing info can be saved in the
survey annotations or as short text in the scrap. If you need additional Therion symbols send an email.

The scraps are saved in therion format, and can be used to create preliminary maps with _therion_. However they should be edited in _xtherion_ to fill the missing details, and, in particular, to clean up the cave outline. Scrap join's and the organization of the map structure are not managed by TopoDroid.

The scrap, saved in Therion ".th2" format, are stored in the "th2" directory.
The name of the scrap files is composed of the survey name and the scrap name.
Try to use short names for surveys and for scraps. I use the cave cadastral code (or the nickname) followed by two digits for the survey and "p1"
"p2", ..., "s1" and so on for the scraps. For example scrap "p1" of survey "164801" gets stored in the file "164801-p1.th2"

Starting version 0.2 TopoDroid can use cubic splines (as Therion). If necessary the lines can be easily edited with xtherion.



## 3D viewer ##

TopoDroid does not have 3D displaying capabilities. The therion files
of the centerline can be displayed in 3D using Cave3D.

If you keep a main th file for the cave you are surveying, and you add
an equate command to it to tie the survey you are making with the previous
surveys, you can get a 3D view of the cave and the survey while
surveying.

## "Cave mode" ##

Battery charge is limited and you need to save it as much as possible when
you are caving. If you do not turn off radio emission, your phone wil continuosly
try to pair with your service provider, but it will fail because the phone signal does not enter caves enough. This will drain the battery charge in a few hours.

When you enter the cave switch your phone off. Turn it on when you start
the survey. Phones have a special "airplane mode" that lets you turn all radio
off. Sometimes WiFi can be left on, Turn it off as you do not need it.

Of course you need bluetooth to communicate with the DistoX.
There is an android app that lets you turn bluetooth on in "airplane mode".
In my case when I start TopoDroid in "airplane mode" i get a message asking if I
want to turn on bluetooth: I say "yes" and I get bluetooth in "airplane mode" :-)

Don't forget to turn your android off again when you are done with the survey, so that is has some charge when you get out of the cave.