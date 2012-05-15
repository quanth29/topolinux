/** @file DistoXFix.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid fixed stations (GPS-localized stations)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

class DistoXFix
{
  String name;
  double lat;
  double lng;
  double alt;

  public DistoXFix( String n, double latitude, double longitude, double altitude )
  {
    name = n;
    lat = latitude;
    lng = longitude;
    alt = altitude;
  }
}
