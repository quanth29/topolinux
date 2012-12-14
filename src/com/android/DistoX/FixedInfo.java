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
 * 20120531 added toString 
 * 20120603 added toLocString
 */
package com.android.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

/** fixed (GPS) point
 * Note the order of data: LONGITUDE - LATITUDE - ALTITUDE
 */
class FixedInfo
{
  Long   id;       // fixed id
  String name;     // station name, or whatever
  double lng;      // longitude [decimal deg]
  double lat;      // latitude [decimal deg]
  double alt;      // altitude [m]
  String comment;

  public FixedInfo( long _id, String n, double longitude, double latitude, double altitude, String cmt )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = altitude;
    comment = cmt;
  }

  public FixedInfo( long _id, String n, double longitude, double latitude, double altitude )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = altitude;
    comment = "";
  }

  public String toLocString()
  {
    return double2ddmmss( lng ) + " " + double2ddmmss( lat ) + " " + Integer.toString( (int)(alt) );
  }

  public String toString()
  {
    return name + " " + double2ddmmss( lng ) + " " + double2ddmmss( lat ) + " " + Integer.toString( (int)(alt) );
  }

  static String double2ddmmss( double x )
  {
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) );
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format( "%d:%02d:%02d.%02d", dp, mp, sp, ds );
    return swp.getBuffer().toString();
  }

}
