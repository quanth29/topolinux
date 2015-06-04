## TopoDroid translation ##

TopoDroid comes with titles and messages in English.
This page describes how to add support for another
language to TopoDroid


### Get TopoDroid sources ###

Get the TopoDroid sources for the branch
you want to translate. In particular get the
resources (directory "res").

### Translate the strings ###

First you need to translate three files
  * res/values/strings.xml
  * res/values/array.xml
  * and the symbols files in the res/raw/symbols zip

The "strings.xml" file is straightforword, just use a
proper encoding (tag in the first line).

The "array.xml" file also needs the proper encoding.
Translate only the TypeValues, and leave the Types
in English.

Symbols files are included in the res/raw/symbols zip file.
You need to extract the symbols files and add a line
to each with the name in the target language.
The line must start with "name-xx" where xx is the iso code
of the language. Names must not contain spaces: use _underscore_ for the space.
At the moment names are parsed with iso-8859-1 encoding.
Contact me if your language uses another encoding.
Finally recreate the zip archive.

For the syntax of symbol files see [TopoDroidSymbols](TopoDroidSymbols.md)


### Check the translation ###

You may want to build your own TopoDroid
just to check that the translation files are correct
and there is no mistake.

The translation of "strings.xml" and "array.xml" must be put
in the directory "res/values-xx" where xx is the iso code of
the language.

The new symbols zip must replace the old one and
must be named "res/raw/symbols".

Make sure TopoDroid default language presentation (English)
is not affected by the translation.


### Submit the translation ###

Send me your translation files (marco.corvi@gmail.com).

I can test only the defaut (English) version.

If you have translated the latest branch (currently 2.3),
I will include it in the build that I put on [TopoDroid](https://sites.google.com/site/speleoapps/home) site.


### Maintaining a translation ###

As TopoDroid evolves, some strings are changed, new strings are added,
while others are dropped.
Usually there are only a few changes between one version and the next.

New strings are added to existing translations as comments.
For example
```
<!-- TODO string name="...">...</string -->
```
To update a translation these comments must be replaced with the
proper strings.

Certain strings do not need to be translated. Put a comment in their place, such as,
```
<!-- OK string name="...">...</string -->
```

You should keep the last English string files that you used for your
translation and diff the current ones against them.
You can then propagate the differences to your translation.