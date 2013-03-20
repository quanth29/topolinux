/** @file SelectionPoint.java
 *
 * @author marco corvi
 * @date feb 2013
 * -------------------------------------------------
 *  CHANGES
 */
package com.android.DistoX;

import android.util.FloatMath;
import android.util.Log;


class SelectionPoint
{
  float x;
  float y;
  float mDistance;
  DrawingPath item;

  // void dump()
  // {
  //   Log.v("DistoX", "point-" + item.mType + " " + x + " " + y );
  // }

  SelectionPoint( float x0, float y0, DrawingPath it )
  {
    x = x0;
    y = y0;
    item = it;
  }

  void evalDistance( float xx, float yy )
  {
    mDistance = FloatMath.sqrt( (x-xx)*(x-xx) + (y-yy)*(y-yy) );
  }
}
