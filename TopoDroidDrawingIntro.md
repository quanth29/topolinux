# TopoDroid Drawing #

Drawing in TopoDroid is done a-la Therion.
So some knowledge of "the Therion approach" is useful to
do good sketches. Here is a brief introduction to how Therion
views the cave maps.

> - cave maps  are split in sketches (**scrap** in therion jargon). For small caves a plan scrap and an extended elevation scrap are usually enough.

> - there are three types of objects in the sketches: **points** (iconic symbols, like those for the speleothems or the wiggling arrow for water-flow), **lines** (cave wall, pit, etc.) and **areas** (for example a pond). In Therion labels can be either point or lines (to write the label along a curve); TopoDroid has only point labels.

> - to draw a point you choose the symbol and tap on the screen where you want to place it. For labels you must also enter the label text in the coming dialog.

> - to draw a line, you choose the type and drag a path on the screen which becomes the line. The most important line type is "wall" which defines the cave walls. The drawing is clipped in Therion to the "inside" of the cave, therefore lines extending beyond the walls will be drawn in Therion only up to the wall. Obviously certain symbols, in particular labels, are not clipped.

> - to draw an area, you choose the type, and draw the line that borders the area. Topodroid closes the border by joining the two ends with a straight segment. Areas are clipped in Therion to the "inside" of the cave.

> - for the extended section, for each shot, you have to choose whether to draw it to the left or to the right (there is a third option: "vertical"; Therion has other option(s), but these are for advanced complex drawings).


## Remarks ##

TopoDroid attach a start station to a sketch: this station is placed at the origin of the canvas and the rest of the centerline is computed from it.

Each sketch comprises a plan view and an extended section view.
Cross sections are added by drawing "section" lines to one of the two views.
See [TopoDroidCrossSections](TopoDroidCrossSections.md) for details.

The statistics are referred to the start station. In particular the depth is measured from this station.

The scrap files are saved in the "th2" directory of TopoDroid.
They are named composing the survey name with the scrap name: for example
the file of the scrap "p1" of the survey "my\_cave" is "my\_cave-p1.th2".

All the files are inserted in the zip archive without the directory name
in the path. Therefore, when you unzip the archive you get the "th" file
and the "th2" files in the same directory.

If you map small caves, you can do all the work in the cave.
When you get back, you create the zip archive of the survey, download it to the pc, unzip it, add a thconfig file with two lines:
```
    source your_cave.th
    export map -proj plan -o your_cave.pdf
```
You must also uncomment the input of the th2 files in the th.

Then run therion on it, and you have your pdf map.
The quality depends on your ability to draw sketches, of course.