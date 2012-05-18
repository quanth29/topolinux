/** @file DistoXFix.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid fixed stations (GPS-localized stations)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120516 cstr
 */
package com.android.DistoX;

class DistoXFix
{
  String name;     // station name
  double lat;      // latitude [decimal deg]
  double lng;      // longitude [decimal deg]
  double alt;      // altitude [m]
  String comment;

  public DistoXFix( String n, double latitude, double longitude, double altitude, String cmt )
  {
    name = n;
    lat = latitude;
    lng = longitude;
    alt = altitude;
    comment = cmt;
  }

  public DistoXFix( String n, double latitude, double longitude, double altitude )
  {
    name = n;
    lat = latitude;
    lng = longitude;
    alt = altitude;
    comment = "";
  }
}
