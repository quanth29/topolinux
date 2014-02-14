/* @file DrawingPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130204 type DRAWING_PATH_NAME
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.FloatMath;
import android.util.Log;

/**
 */
public class DrawingPath implements ICanvasCommand
{
  public static final int DRAWING_PATH_FIXED   = 0; // leg
  public static final int DRAWING_PATH_SPLAY   = 1; // splay
  public static final int DRAWING_PATH_GRID    = 2; // grid
  public static final int DRAWING_PATH_STATION = 3; // station point
  public static final int DRAWING_PATH_POINT   = 4; // drawing point
  public static final int DRAWING_PATH_LINE    = 5;
  public static final int DRAWING_PATH_AREA    = 6;
  public static final int DRAWING_PATH_NAME    = 7; // station name

  Path path;
  Path mTransformedPath;
  Paint mPaint;
  int mType;
  // private float x1, y1, x2, y2; // endpoint scene coords 
  // private int dir; // 0 x1 < x2, 1 y1 < y2, 2 x2 < x1, 3 y2 < y1

  float cx, cy; // midpoint scene coords
                 
  DistoXDBlock mBlock;
  //SEL SelectionPoint mSelectionPoint;

  DrawingPath( int type )
  {
    mType = type;
    mBlock = null;
    //SEL mSelectionPoint = null;
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
  }

  DrawingPath( int type, DistoXDBlock blk )
  {
    mType = type;
    mBlock = blk; 
    //SEL mSelectionPoint = null;
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
  }

  public void setPaint( Paint paint ) { mPaint = paint; }

  // x10, y10 first endpoint scene coords
  // x20, y20 second endpoint
  public void setEndPoints( float x10, float y10, float x20, float y20 )
  {
    // x1 = x10;
    // y1 = y10;
    // x2 = x20;
    // y2 = y20;
    // dir = ( Math.abs( x2-x1 ) >= Math.abs( y2-y1 ) )?
    //          ( (x2 > x1)? 0 : 2 ) : ( (y2>y1)? 1 : 3 );
    // d = FloatMath.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    cx = (x20+x10)/2;
    cy = (y20+y10)/2;
  }

  float distance( float x, float y )
  {
    if ( mBlock == null ) return 1000.0f; // a large number
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "distance to " + x + " " + y + " " + cx + " " + cy );
    return (float)( Math.abs( x - cx ) + Math.abs( y - cy ) );
  }

  // public int type() { return mType; }

  public void draw( Canvas canvas )
  {
    if ( mType == DRAWING_PATH_AREA ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingPath::draw area" );
      path.close();
      canvas.save();
      canvas.clipPath( path );
      canvas.drawPaint( mPaint );
      canvas.restore();
    } else {
      canvas.drawPath( path, mPaint );
    }
  }

  public void draw( Canvas canvas, Matrix matrix )
  {
    // if ( mType == DRAWING_PATH_AREA ) {
    //   // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingPath::draw[matrix] area" );
    //   path.close();
    // }
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingPath::draw[matrix] " + mPaint );
    mTransformedPath = new Path( path );
    mTransformedPath.transform( matrix );
    if ( mType == DRAWING_PATH_AREA ) {
      canvas.save();
      canvas.clipPath( mTransformedPath );
      canvas.drawPaint( mPaint );
      canvas.restore();
    } else {
      canvas.drawPath( mTransformedPath, mPaint );
    }
  }

  public void setOrientation( double angle ) { }

  public String toTherion() { return new String("FIXME"); }

  public void undo()
  {
    // TODO this would be changed later
  }

  // public void transform( Matrix matrix )
  // {
  //   path.transform( matrix );
  // }
}