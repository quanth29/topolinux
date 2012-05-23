/** @file FixedInfo.java
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
 * 20120522 rename FixedInfo
 */
package com.android.DistoX;

class FixedInfo
{
  String name;     // station name
  double lat;      // latitude [decimal deg]
  double lng;      // longitude [decimal deg]
  double alt;      // altitude [m]
  String comment;

  public FixedInfo( String n, double latitude, double longitude, double altitude, String cmt )
  {
    name = n;
    lat = latitude;
    lng = longitude;
    alt = altitude;
    comment = cmt;
  }

  public FixedInfo( String n, double latitude, double longitude, double altitude )
  {
    name = n;
    lat = latitude;
    lng = longitude;
    alt = altitude;
    comment = "";
  }
}
