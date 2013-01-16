/* @file PlotInfo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch metadata
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120522 renamed PlotInfo
 */
package com.android.DistoX;

// import android.util.Log;

class PlotInfo
{
  public static final String[] plotType = {
    "V-SECTION",  // vertical cross section
    "PLAN",       // plan
    "EXTENDED",   // extended elevation
    "H-SECTION"   // horizontal cross-section
  };

  public long surveyId; // survey id
  public long id;       // plot id
  public String name;   // name of the plot
  public int type;      // type of the plot
  public String start;  // base station
  public String view;   // viewed station
  public float xoffset; // display X-offset
  public float yoffset; // display Y-offset
  public float zoom;    // display zoom

  // void dump()
  // {
  //   Log.v("DistoX", surveyId + "-" + id + " " + name + " type " + type + " start " + start );
  // }

  public void setId( long i, long sid )
  {
    id = i;
    surveyId = sid;
  }

  public String getTypeString() 
  {
    return plotType[ type ];
  }

  public static int getTypeValue( String type )
  {
    if ( type.equals("V-SECTION") ) return (int)TopoDroidApp.PLOT_V_SECTION;
    if ( type.equals("PLAN") )      return (int)TopoDroidApp.PLOT_PLAN;
    if ( type.equals("EXTENDED") )  return (int)TopoDroidApp.PLOT_EXTENDED;
    if ( type.equals("H-SECTION") ) return (int)TopoDroidApp.PLOT_H_SECTION;
    return (int)TopoDroidApp.PLOT_PLAN;
  }

}
