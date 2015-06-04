# Proj4 #

Proj4 is an app to convert between coordinate reference systems (CRS).
It is based on the proj4 library.

## Coordinate syntax ##

Altitude (Z coordinate) is always in meters.
The X and Y coordinates can be in meters or in degrees
depending on the CRS used. You can use decimal degrees or
degrees,minutes.seconds.

## CRS syntax ##

Proj4 comes with only a few CRS, but
you can add all the CRS that you need.
To do this you must give a nickname to the new CRS
and write its spec in the proj4 syntax.

For example: "+proj=tmerc +lat\_0=0 +lon\_0=9 +k=0.9996 +x\_0=1500000 +y\_0=0 +no\_defs +a=6378388 +rf=297 +towgs84=-104.1,-49.1,-9.9,0.971,-2.917,0.714,-11.68 +to\_meter=1

See the proj4 man for details.

## API ##

The Proj4 app can be called by other apps to compute
a coordinate conversion.

Calling intent extras:
  * "version" Proj4 version (currently "1.1")
  * "cs\_from" the nickname of the CRS of the input coordinates (eg. "Long-Lat")
  * "longitude" X coordinate
  * "latitude" Y coordinate
  * "altitude" Z coordinate
  * "cs\_to" the nickname of the CRS of the output coordinates [optional](optional.md)

The output CRS can be changed in the interface before computing the coordinate
conversion.

Return intent extras:
  * "cs\_to" the nickname of the CRS of the output coordinates
  * "longitude" X coordinate
  * "latitude" Y coordinate
  * "altitude" Z coordinate

