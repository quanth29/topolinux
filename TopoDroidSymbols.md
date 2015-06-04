## TopoDroid drawing symbols ##

Starting v. 1.1 TopoDroid comes with only very few preinstalled symbols.
You must install all the other symbols (points, lines, and areas) that you need to draw the sketches.

This allows you to customize the symbol picker dialogs including only the
symbols that you use, so that you do not have to get also symbols that
are not important, and the interface is less cluttered.

This also allows users to define their own symbols, in case it is not
provided.


### Symbol files ###

At start-up TopoDroid loads user' symbols reading the files
in the directories "symbol/point", 'symbol/line", and "symbol/area", for
points, lines, and areas, respectively.
These are subdirectories of the TopoDroid home directory.

You install a symbol by copying the symbol file in the proper directory.
You uninstall a symbol by removing the file (or moving it to another place).

Symbol files can be downloaded from the source tree in this website.

You can also write your own symbol files, and share them with other
users. If you send me your files i will put them in the surces.

Symbol files are plain text files with one directive per line.

### Point symbols ###

The pre-installed point symbols are: "label", "danger", "air-draught", and
"water-flow". All the other symbols must be installed in the "symbol/point"
directory.

The syntax of a point symbol file is
```
  symbol point
    name NAME
    th_name TH_NAME
    color 0xRRGGBB
    orientation YES_NO
    csurvey LAYER TYPE CATEGORY
    path
      MULTILINE_PATH_DIRECTIVES
    endpath
  endsymbol
```

Indentation is not necessary. Lowercase words are keywords, uppercase words
are the respective values. In the TopoDroid interface the point is shown with
name "name", while in the sketch file it is saved with "th\_name" (the Therion
name). The "color" is specified with the RGB (red, green, blue) components
as a single hex-number. For example "0xff0000" is full red. The value of the orientation can be either "yes" or "no". By default it is "no". It specifies
whether the point has an orientation (like "water-flow") or not (like "stalactite").

The "csurvey" attributes are used in the export of sketches into cSurvey format.
The LAYER must be 6, as point symbols go on cSurvey layer 6. Type and category must follow cSurvey semantics.

The commands between "path" and "endpath" specify the point shape. The possible directives are
  * "moveTo X Y"
  * "lineTo X Y"
  * "cubicTo X1 Y1 X2 Y2 X Y"
  * "addCircle X Y R"

The point path should stay enclosed in the square [-10,10]x[-10,10].

If you put two points without orientation one after the other in the same
file, you get a "flippable" symbol, such as "stal" which used to stay for
both "stalactite" and "stalagmite".


### Line symbols ###

The only pre-installed line symbol is "wall". All the other symbols must be installed in the "symbol/line" directory.

Line symbol files can be very simple, such as for "border". Lines like "pit"
and "overhang" require more complex specifications.
The syntax is
```
  symbol line
    name NAME
    th_name TH_NAME
    color 0xRRGGBB 0xAA
    width WIDTH
    csurvey LAYER TYPE CATEGORY PEN
    dash ON OFF
    effect
      MULTILINE_EFFECT_DIRECTIVES
    endeffect
  endsymbol
```

The directives "dash" and "effect" can be omitted.

The "color" dircetive allows to specify also the alpha value (transparency);
the default is 0xff (255) which means "not transparent".

The "csurvey" values are used to export sketches into cSurvey format.
The LAYER can be 2, 3, 4, 5 or 6, according to the cSurvey layer for the line type. Type, category, and pen must follow cSurvey semantics.

The "dash" is used to get a dashed line: ON and OFF are the length of the
portions that are drawn and not-drawn, respectively.

The "effect" is used to draw the line with a special brush. Its directives
must be a "moveTo 0 0" followed by the "lineTo X Y" that describe the contour of the brush path. This is automatically closed to (0,0).

If you specify both "dash" and "effect", "dash" is applied first and "effect" second.


### Area symbols ###

The only pre-installed line symbol is "water". All the other symbols must be installed in the "symbol/area" directory.

The syntax of area symbol files is very simple (the semantics is the same
as for line symbols),
```
  symbol area
    name NAME
    th_name TH_NAME
    color 0xRRGGBB 0xAA
    csurvey LAYER
  endsymbol
```

The "csurvey" values are used to export sketches into cSurvey format.
The LAYER must be 1.