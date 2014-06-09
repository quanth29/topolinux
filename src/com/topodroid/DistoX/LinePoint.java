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
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
import java.util.Locale;

// import android.util.FloatMath;

public class LinePoint extends BezierPoint
{
  private static final float toTherion = TopoDroidApp.TO_THERION;

  // public float mX;
  // public float mY;
  float mX1; // first control point (to the right of the previous LinePoint)
  float mY1;
  float mX2; // second control point (to the left of this LinePoint)
  float mY2;
  boolean has_cp;
  LinePoint next;  // next LinePoint on the line

  void shiftBy( float dx, float dy )
  {
    mX += dx;
    mY += dy;
    if ( has_cp ) {
      // mX1 += dx;
      // mY1 += dy;
      mX2 += dx;
      mY2 += dy;
    }
    if ( next != null && next.has_cp ) {
      next.mX1 += dx;
      next.mY1 += dy;
    }
  }

  public LinePoint( LinePoint lp, LinePoint prev )
  { 
    mX = lp.mX;
    mY = lp.mY;
    mX1 = lp.mX1;
    mY1 = lp.mY1;
    mX2 = lp.mX2;
    mY2 = lp.mY2;
    has_cp = lp.has_cp;
    next = null;
    if ( prev != null ) prev.next = this;
  }
  
  public LinePoint( float x, float y, LinePoint prev )
  {
    super( x, y );
    // mX = x;
    // mY = y;
    has_cp = false;
    next = null;
    if ( prev != null ) prev.next = this;
  }

  public LinePoint( float x1, float y1, float x2, float y2, float x, float y, LinePoint prev )
  {
    super( x, y );
    // mX  = x;
    // mY  = y;
    mX1 = x1;
    mY1 = y1;
    mX2 = x2;
    mY2 = y2;
    has_cp = true;
    next = null;
    if ( prev != null ) prev.next = this;
  }

  // float distance( float x, float y )
  // {
  //   return FloatMath.sqrt( (x-mX)*(x-mX) + (y-mY)*(y-mY) );
  // }


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
