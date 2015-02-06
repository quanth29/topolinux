/* @file DrawingLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line-path (lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

/**
 */
public class DrawingLinePath extends DrawingPath
{
  // private static final String TAG = "DistoX";

  private int mLineType;
  private boolean mClosed;
  private float alpha0, alpha1;  // temporary
  private BezierPoint c1, c2;

  private ArrayList< LinePoint > points; 

  public DrawingLinePath( int type )
  {
    super( DrawingPath.DRAWING_PATH_LINE );
    DrawingBrushPaths.makePaths( );
    // Log.v( TAG, "new DrawingLinePath type " + type );
    mLineType = type;
    mClosed = false;
    points  = new ArrayList< LinePoint >();
    path    = new Path();
    setPaint( DrawingBrushPaths.linePaint[ type ] );
  }

  public void addTick( float x, float y, float dx, float dy )
  {
    path.lineTo( x+dx, y+dy );
    path.moveTo( x, y );
  }

  public void addStartPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.moveTo( x, y );
  }

  public void addPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.lineTo( x, y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    points.add( new LinePoint( x1,y1, x2,y2, x,y ) );
    path.cubicTo( x1,y1, x2,y2, x,y );
    if ( mLineType ==  DrawingBrushPaths.LINE_PIT 
      || mLineType ==  DrawingBrushPaths.LINE_CHIMNEY
      || mLineType ==  DrawingBrushPaths.LINE_SLOPE ) {
      float dx = x - x2;
      float dy = y - y2;
      float d = dx*dx + dy*dy;
      if ( d > 0.0f ) {
        d = 5.0f / (float)Math.sqrt( d );
        dx *= d;
        dy *= d;
        if ( mLineType ==  DrawingBrushPaths.LINE_PIT ) {
          addTick( x, y, dy, -dx );
        } else if ( mLineType ==  DrawingBrushPaths.LINE_SLOPE ) {
          addTick( x, y, 3*dy, -3*dx );
        } else {
          addTick( x, y, -dy, dx );
        }
      }
    }
  }

  // public void setLineType( int t ) { mLineType = t; }
  public int lineType() { return mLineType; }

  public ArrayList< LinePoint > getPoints() { return points; }

  public int size() { return points.size(); }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( mClosed ) {
      pw.format("line %su -close on \n", DrawingBrushPaths.lineName[mLineType] );
    } else {
      pw.format("line %s\n", DrawingBrushPaths.lineName[mLineType] );
    }
    for ( LinePoint pt : points ) {
      pt.toTherion( pw );
    }
    if ( mLineType == DrawingBrushPaths.LINE_SLOPE ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }


}

