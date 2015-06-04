## InGrigna app ##

The InGrigna app is an Android app to browse the Speleological database
of InGrigna. It is not a GIS.


### The database ###

The Speleological database of InGrigna contains data of
  * the caves of the Grigne area: survey data, maps, photoes, description (over 1000)
  * the speleological published works (articles, books, etc.) (more than 500)
  * the cadastrial data
  * sources in the area
  * the InGrigna activity reports since 2007
  * maps and other material

There are cross references between caves and publications, and in particular
publications containing maps of caves. The activity reports are geo-referenced and cross-referenced with caves.


### The app ###

The app interface is a Google map with overlaid a portion of the CTR map and the
cave and source positions.

The cave marks carry a color/shape code. If the cave is "vertical" the mark is
a circle, if it is "horizontal" the mark is a semicircle. The color codes are (N.B. color codes have changed with v. 1.2)
  * grey: re-visited, no continuation
  * violet: not revisited yet
  * red: need digging
  * yellow: to revisit, need digging
  * green: to revisit with less snow,
  * dark green: to revisit
  * light blue: continue
  * blue: in exploration

Tapping on a cave mark opens the "cave dialog" with a summary of the data of the
cave and buttons to access the relevant documentation:
  * description
  * entrance photo
  * survey maps (either as image or pdf)
  * survey 3D viewer
  * bibliography
  * survey bibliography
There is also a button to center the map on the cave, a button to edit the cave description, and a button to take a picture of the entrance of the cave.

If there are more than one cave near the map point where you tap, you are presented with the list of the caves to choose one from.

The interface has four buttons for query (short click) or for listing within a range from the map center (long click):
  * caves
  * reports
  * articles
  * sources
Caves can be searched by the cadastrial number, the name, the length, or the depth. Reports and articles by the date, the author, or the title (topic).
Sources by the id, name, or rate of flow.
[N.B. query on strings are not implemented yet ](.md)

The app menus:
  * Locate
  * Overlays: open a dialog to load the overlays
  * Save center: store away the map center
  * Legend: see the cave color legend
  * Move to: move to a position
  * Coord convert: coordinate conversion (between Gauss Boaga and Long./Lat)
  * Options: app preferences

The app preferences include:
  * the location of the current center (east, north)
  * the default range for geo-queries
  * the tapping range (focus)
  * whether to overlay caves
  * whether to overlay sources
  * whether to overlay the CTR map
  * whether to get the satellite Google map
  * whether to get the street Google map


### How to make your own database ###

The database contains the following tables:
  * config ( key, value )
  * maps ( key, value )
  * caves ( code, east, north, alt, length, depth, rev\_code, rev\_year, rev\_note, name )
  * articles ( aid, code, year, author, pub, pdf, title )
  * reports ( code, east north range author title )
  * sources ( id, east, north, alt, q, cont, name )
  * surveys ( article, cave )
  * artrefs ( article, cave, type )
  * reprefs ( report, cave )

Caves are referenced by their code, a four-letter string (usually the cadastrial number). Most of the files related to a cave have a name that starts with the cave code.

Articles have a unique integer id (_aid_) as well as a unique string code. The id is used to reference the article in the other tables. Articles are related to caves: this relation has also a type (exploration, note, citation, etc.).
A specia table, _surveys_, is used to list articles with a map of the cave.

Reports have a unique code composed by the report date and a (incremental) latter, eg, 2012-10-25a. Reports are georefernced to the circle where the activity reported took place.
Reports are also related to the caves relevant to the report activity.

The _config_ table contains important configuration info:
  * base-dir base directory of the DB files
  * cave-foto directory of the photo of the cave entrances
  * cave-rilievi directory with the cave pdf maps. The file for cave ABCD must be named ABCD.pdf
  * cave-schede directory with the description of the cave. The file for the cave ABCD must be named ABCD.xml
  * cave-therion directory with the cave data (in therion format). The file for the cave ABCD is named ABCD.th
  * cave-gifs directory with the cave map (as PNG images). The file for the cave ABCD is named ABCD.png
  * biblio directory with the descriptio of the articles (abstracts). The file for article 123 has name 123.xml
  * report directory with the report files. The file for report 2012-10-25a has name 2012-10-25a (although it is an xml file)
  * note directory for the annotations written during a trip
  * source-foto directory fo rthe photos of the sources

The maps table contains the info for the local maps (CTRs):
  * ctr directory with the map files (PNG images)
  * ctr-nr-east number of longitude coordinates of the maps corners
  * ctr-nr-north number of latitude coordinates of the maps corners
  * ctr-east-0 ... values of the longitude coordinates
  * ctr-north-0 ... values of the latitude coordinates
  * ctr-file-0-0 ... file name (first index is east, second index is north). There must be (ctr-nr-east - 2)x(ctr-nr-north - 2) files