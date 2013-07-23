/** @file SketchShapePoints.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: cross-shape point set
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

class SketchShapePoints
{
  ArrayList< PointF > pts;

  SketchShapePoints()
  {
    pts = new ArrayList< PointF > ();
  }

  void add( PointF p ) { pts.add(p); }

  int size() { return pts.size(); }

}
