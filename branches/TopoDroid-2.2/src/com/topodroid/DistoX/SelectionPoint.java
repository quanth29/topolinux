/** @file SelectionPoint.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief a point in the selection set
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *  CHANGES
 * 20131120 added LinePoint
 */
package com.topodroid.DistoX;

import android.util.FloatMath;
// import android.util.Log;


class SelectionPoint
{
  // scene coord (x, y )
  // for DrawingStationName (x,y) = st.(mX,mY)
  // for DrawingPath        (x,y) = (cx,cy)
  // for DrawingStationPath (x,y) = path.(mXpos,mYpos)
  // for DrawingPointPath   (x,y) = path.(mXpos,mYpos)
  // for DrawingLinePath    (x.y) = midpoint between each two line points
  // for DrawingAreaPath    (x,y) = midpoint between each two border points

  float mDistance;
  DrawingPath mItem;
  LinePoint   mPoint;

  int type() { return mItem.mType; }
  // DRAWING_PATH_FIXED   = 0; // leg
  // DRAWING_PATH_SPLAY   = 1; // splay
  // DRAWING_PATH_GRID    = 2; // grid
  // DRAWING_PATH_STATION = 3; // station point
  // DRAWING_PATH_POINT   = 4; // drawing point
  // DRAWING_PATH_LINE    = 5;
  // DRAWING_PATH_AREA    = 6;
  // DRAWING_PATH_NAME    = 7; // station name

  SelectionPoint( DrawingPath it, LinePoint pt )
  {
    mItem = it;
    mPoint = pt;
    mDistance = 0.0f;
  }

  // float X() 
  // {
  //   if ( mPoint != null ) return mPoint.mX;
  //   return mItem.cx;
  // }

  // distance from a scene point (xx, yy)
  float distance( float xx, float yy )
  {
    if ( mPoint != null ) return mPoint.distance( xx, yy );
    return mItem.distance( xx, yy );
  }

  void shiftBy( float dx, float dy )
  {
    if ( mPoint != null ) {
      mPoint.shiftBy( dx, dy );
      DrawingPointLinePath item = (DrawingPointLinePath)mItem;
      item.retracePath();
    } else if ( mItem.mType == DrawingPath.DRAWING_PATH_POINT ) {
      mItem.shiftBy( dx, dy );
    }
  }

}
