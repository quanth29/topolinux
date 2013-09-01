/** @file SketchSection.java
 *
 * @author marco corvi
 * @date jul 2013
 *
 * @brief TopoDroid 3d sketch: cross section
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20130310 created
 * 20130830 SECTION_STATION type
 */
package com.android.DistoX;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Matrix;

import android.util.Log;

class SketchSection
{
  final static int SECTION_NONE = 0;
  final static int SECTION_VERT = 1;
  final static int SECTION_HORIZ = 2;
  final static int SECTION_STATION = 3;

  int mType; // V_SECTION or H_SECTION
  SketchSectionSet mSet;  // the section set of this section

  float  mPosition;   // "position" on the shot line
  Vector mBasePoint;  // "center" of the section
  Line3D mLine;       // contour of the section (ArrayList of Vector) world coords

  // cstr
  SketchSection( SketchSectionSet set, float x, Vector p, Line3D line )
  {
    mSet = set;
    mType = set.mType;
    mPosition  = x;
    mBasePoint = p;
    mLine = line;
  }

  /** copy cstr for SECTION_STATION type
   */
  SketchSection( SketchSectionSet set, SketchBorder border, SketchSurface surface )
  {
    mSet = set;
    mType = set.mType;
    mPosition = 0;
    mBasePoint = border.getCenter();
    mLine = new Line3D();

    ArrayList<SketchSide> sides = border.sides; // insert border vertices in the line
    SketchSide s0 = sides.get( 0 );
    SketchSide s1 = sides.get( 1 );
    int v1 = 0;
    int v2 = 0;
    if ( s0.v1 == s1.v1 || s0.v1 == s1.v2 ) {
      v1 = s0.v2;
      v2 = s0.v1;
    } else { // s0.v2 == s1.v1 || s0.v2 == s1.v2 )
      v1 = s0.v1;
      v2 = s0.v2;
    }
    mLine.addPoint( surface.getVertex(v1) );
    for ( int k = 1; k < sides.size(); ++k ) {
      mLine.addPoint( surface.getVertex(v2) );
      s1 = sides.get( k );
      v2 = ( s1.v1 == v2 ) ? s1.v2 : s1.v1 ;
    }
  }

  void reverseLine()
  {
    Line3D line = new Line3D();
    ArrayList< Vector > pts = mLine.points;
    int k = pts.size() - 1;
    for ( ; k >= 0; --k ) {
      line.addPoint( pts.get(k) );
    }
    mLine = line;
  }

  Vector getNormal()
  {
    return Vector.computeNormal( mLine.points );
  }

  public void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info, Paint paint )
  {
    Path  path = new Path();
    boolean first = true;

    PointF q = new PointF();
    for ( Vector p : mLine.points ) {
      // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
      info.worldToSceneOrigin( p.x, p.y, p.z, q );
      if ( first ) {
          path.moveTo( q.x, q.y );
          first = false;
      } else {
          path.lineTo( q.x, q.y );
      }
    }
    // if ( mClosed && mLine.points.size() > 2 ) { // FIXME SOON
    //   Vector p = mLine.points.get(0);
    //   info.worldToSceneOrigin( p.x, p.y, p.z, q );
    //   path.lineTo( q.x, q.y );
    // }
    path.transform( matrix );
    canvas.drawPath( path, paint );
  }

}
