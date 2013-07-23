/** @file SketchShapePath.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: cross-shape path
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130308 created
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.graphics.PointF;
import android.graphics.Path;

class SketchShapePath
{
  Path mPath;
  ArrayList<PointF> mPts;

  SketchShapePath()
  {
    mPath = new Path();
    mPts  = new ArrayList< PointF >();
  }

  void addPoint( float x, float y )
  {
    if ( mPts.size() == 0 ) {
      mPath.moveTo( x, y );
    } else {
      mPath.lineTo( x, y );
    }
    mPts.add( new PointF(x,y) );
  }

  void closePath()
  {
    if ( mPts.size() > 0 ) {
      PointF p = mPts.get( 0 );
      mPath.lineTo( p.x, p.y );
    }
  }

}
