## TopoDroid troubleshooting ##


### TopoDroid crashes ###

It can happen !!!

TopoDroid is developed using English locale and
names without spaces. It is possible that another locale, or names
containing spaces break the application. If this is the case, please
send me a detailed description of what you did and what happened, and
i will try to fix the problem. Attach any file that might be useful.

You may also try to reproduce the crash working with English locale
and name composed of only the characters of the English alphabeth [a-z and
A-Z] and digits [0-9].


### TopoDroid does not detect DistoX ###

**Before you can download anything from the DistoX this must be paired with your android device.**

You may try to detect and pair your android with the DistoX
using the "Settings" app. Under the item Wireless and Network,
select Bluetooth and scan for devices.

If this app fails there is no chance that TopoDroid will detect
the DistoX. After the Settings app can pair to the DistoX, you should
be able to see it with TopoDroid in the list of "Paired devices".

If the app Settings cannot detect and pair with the DistoX, there is a
problem with your android BlueTooth.


### TopoDroid does not download the data from the DistoX ###

If you chose the continuous connection and connect the DistoX bluetooth icon
should blink. If it does not there is a connection problem. Try switching off and
on the DistoX and press the "Reset" menu of TopoDroid device window.

When TopoDroid is in continuos connection mode and you press the "Download" menu
of the shot window you get the message 'head tail io failure'. The reason is that
on-demand download (the "Download" menu) will not work when TopoDroid is
in continuous connection mode.

On-demand (batch) connection mode is the preferred way.
In continuous connection mode topodroid stays connected to the DistoX:
in this case the DistoX transmits a data as soon as it is avilable
(and topodroid receives it and stores it in the db, but DOES NOT show
on the window, until the user refreshes the list of shots).

There is a problem with continuos connection mode:
the DistoX can go off or just go away and get out of reach,
in this case Topodroid "thinks" it is connected until the system
implementation of the bluetooth realizes that the connection has broken
and informs topodroid.
There is a timeout for this and, unfortunately, it is rather long (about
half a minute on my device).
There is more: what to do when the system timed out and raised the
problem ? Should topodroid retry to connect ? And if so, at what rate ?

You should consider also that the connection is using the battery, and charge is
a limiting factor in the cave.

Therefore the on-demand connection and download ( which connects to
the DistoX only when the user requires so, reads how many new data
are there, and downloads those only ) is preferred.

To work in on-demand connection mode just _do not connect_ topodroid to the distox,
only select the distox device you are working with.
Then in the shot window press the menu "Download".
When it has finished it tells you a brief message with the number of
downloaded shots, and they are automatically added to the list in the window.


### TopoDroid does not show the shots ###

Make sure that the "Hide blank shots" submenu of "Survey" is not checked.

You can always inspect the database, with a SQLite app to check whether the
data have been downloaded and are there.