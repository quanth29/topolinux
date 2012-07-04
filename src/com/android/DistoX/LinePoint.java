/** @file LinePoint.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid drawing: a point on a line
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.android.DistoX;

import java.io.PrintWriter;
import java.util.Locale;

public class LinePoint extends BezierPoint
{
  private static final float toTherion = TopoDroidApp.TO_THERION;

  // public float mX;
  // public float mY;
  float mX1; // first control point
  float mY1;
  float mX2; // second control point
  float mY2;
  boolean has_cp;
  
  public LinePoint( float x, float y )
  {
    super( x, y );
    // mX = x;
    // mY = y;
    has_cp = false;
  }

  public LinePoint( float x1, float y1, float x2, float y2, float x, float y )
  {
    super( x, y );
    // mX  = x;
    // mY  = y;
    mX1 = x1;
    mY1 = y1;
    mX2 = x2;
    mY2 = y2;
    has_cp = true;
  }


  public void toTherion( PrintWriter pw )
  {
    if ( has_cp ) {
      pw.format(Locale.ENGLISH, "  %.2f %.2f %.2f %.2f %.2f %.2f\n",
        mX1*toTherion, -mY1*toTherion,
        mX2*toTherion, -mY2*toTherion,
        mX*toTherion, -mY*toTherion );
    } else {
      pw.format(Locale.ENGLISH, "  %.2f %.2f\n", mX*toTherion, -mY*toTherion );
    }
  }
}
