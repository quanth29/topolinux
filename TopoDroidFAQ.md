# TopoDroid FAQ #

### How can I install TopoDroid ? ###

The simplest way: go to Google Play and install it.
This way you get the very latest version.

Alternative way nr. 1: allow "Unknow sources" (Settings | Applications).
Download the latest debug build from https://sites.goggle.com/speleoapps
to your android and open it with the installer.

Alternative way nr. 2: get the latest sources from this site, build the apk and install it. Beware that the sources are not updated as regularly as the apks.

### What is the required Android version to run TopoDroid ? ###

TopoDroid is build with minimum Android version 8 (ie, Froyo, 2.2).
If your device has an older version of Android it will not
install it.

This does not mean that TopoDroid cannot run on your device.
Rather I cannot test TopoDroid for older version of Android.
You can download the sources and build TopoDroid for your version of
Android, or you can ask me a special build with a lower minimum version.


### Why TopoDroid does not connect to the DistoX ? ###

Make sure that your device is paired with the DistoX.

Turn on the DistoX.
Start the "Settings" app. Go to "Wireless and Networks | Bluetooth settings".
If your DistoX is already listed among the Bluetooth devices, tap it and pair with it: enter the PIN "0000" (four zeros).

If the DistoX is not listed, scan for devices. Wait a little: bluetooth scanning takes a while. The DistoX should compare in the list.
If it does not you are in really bad luck: get another android.
Tap on the DistoX entry and select to pair with it.

Now the DistoX is also paired and the android can communicate via Bluetooth with it. TopoDroid should work with the DistoX.

Note: make sure that TopoDroid is talking to the correct DistoX device.
TopoDroid talks to one DistoX at a time: the address of the DistoX is
displayed in the "device window", at the top.


### Where did the drawing symbols (points, lines and areas) go (v. 1.1) ? ###

Since v. 1.1 TopoDroid has only very few predefined drawing symbols. The
other symbols must be loaded from file. TopoDroid looks for symbol files in the
subdirectories "point", "line", and "area" of the directory "TopoDroid/symbol".

See [TopoDroidSymbols](TopoDroidSymbols.md).


### Why TopoDroid saves the survey (data and sketches) in Therion format ? ###

Mainly because Therion is my primary tool to put together the maps of
cave surveys. The real answer is that the Therion language provides
the syntax to specify a cave drawing properly.

Survey data can be exported also in other formats: Compass, Survex, VisualTopo, PocketTopo, and as DXF and CSV files


### Why loops do not close in the plots ? ###

TopoDroid does not do loop closure by deafult.
Working with the DistoX loop usually close rather well and it is
good to visually check the loop closure error by assigning dfferent names
to the closing stations.

If you enable loop closure, TopoDroid can compute the
closure errors and display them in the stats.
However, it is likely that you need to specify which shot of the
loop must not be used in the extended profile sketches.
As this is an "advanced" task


### Why are areas only single-line bordered ? ###

Mainly to avoid the complexity of selecting a multi-line border.
A multi-line border is also more brittle due to missing intersections
or multiple intersections. There has been a discussion in the Therion
user list and the single-line solution turned out to be the preferable
way for areas.

Note that TopoDroid needs the first point repeated at the end and may
display a "strange" error dialog.
The repeated point is not required by the therion language, but if it
is missing xtherion complains.


### How can I erase the TopoDroid data ? ###

TopoDroid keeps the survey data and the calibration data
in a SQLite database, namely in the file _/mnt/sdcard/TopoDroid/distox3.db_

You can edit this database with any SQLite manager. I use the
_aSQLiteManager_ app, which is free. You can also pull the database
to your PC, edit it, and push it back to the android.

Scrap, annotation and exports files are stored in subdirectories
in _/mnt/sdcard/TopoDroid/_. These files are not cleared automatically.
You should clear up these directories now and then.



### What can i do if TopoDroid crashes ? ###

You can file a crash report which will go to Google Play.
I will get it and with luck it has enough information to fix it

Better.
If the crash is reproducible get a "logcat" filtered on "DistoX".
If there is something pointing to an invalid value that caused
the crash, send me the logcat.

Even better.
Before doing a logcat, activate the logs that seem relevant to
what you did. The logs are disabled by default.
If the logcat has only system messages, send me an email:
describe what you did and how the app crashed. I can build a
version of the app with debug info enabled in the section that
caused the problem.


### How can I help TopoDroid ? ###

There are several ways you can help TopoDroid:
  * sending me an email with your comments. Telling me what you think of it, even if you do not use it. This will surely help me to improve the program.
  * installing it to try out, and letting me know what you dislike (bad features) and what you would like (missing features).
  * you may [donate](http://marcocorvi.altervista.org/marco/donate0.htm) money. This is the easiest way to tell me that you are interested in it and want it to continue. All the funding will go in buying new hardware to test TopoDroid.
  * writing some documentaton. This requires more time on your side but it also means that you are more involved in it.
  * writing the documentation for other languages.
  * internationalizing TopoDroid, ie, translating the strings in other languages. So far (2012-11) only english and spanish are provided.
  * enrolling to the user mail-list and writing questions and comments.
  * developing the software. Definitely this is a major involvement, and requires a great deal of time.

Please contact me if you want to help with documentation that may go on this website, or with a translation, or with program development.


### How can I be assured that TopoDroid will continue ? ###

If I get enough feedback, one way or another, I will feel
sort of obliged to put a better effort to keep TopoDroid going,
because it would mean tha some people rely on it.

If other developpers get involved, that will be an even stronger
basis for the project.