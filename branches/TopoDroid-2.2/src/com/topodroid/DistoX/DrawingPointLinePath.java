/* @file DrawingPointLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line of points
 *
 * The area border (line) path id DrawingPath.mPath
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20120725 TopoDroidApp log
 * 20121210 symbol area lib
 * 20121225 added "visible" border attribute
 * 201312   method to make the path sharp, straight (section line) and insert point
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import android.util.FloatMath;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

import android.util.Log;

/**
 */
public class DrawingPointLinePath extends DrawingPath
{
  boolean mVisible; // visible line
  boolean mClosed;
  ArrayList< LinePoint > mPoints;      // points (scene coordinates)
  private LinePoint mPrevPoint = null; // previous point while constructing the line

  public DrawingPointLinePath( int path_type, boolean visible, boolean closed )
  {
    super( path_type ); // DrawingPath.DRAWING_PATH_AREA );
    mPoints  = new ArrayList< LinePoint >();
    mPath    = new Path();
    mVisible = visible;
    mClosed  = closed;
  }

  void makeSharp( boolean reduce )
  {
    for ( LinePoint lp : mPoints ) {
      lp.has_cp = false;
    }
    if ( reduce ) {
      int size = mPoints.size();
      if ( size > 2 ) {
        ArrayList pts = new ArrayList< LinePoint >();
        LinePoint prev = mPoints.get( 0 );
        pts.add( prev );
        LinePoint next = mPoints.get( 1 );
        for ( int k = 2; k < size; ++k ) {
          LinePoint pt = next;
          next = mPoints.get(k);
          float x1 = pt.mX - prev.mX;
          float y1 = pt.mY - prev.mY;
          float x2 = next.mX - pt.mX;
          float y2 = next.mY - pt.mY;
          float cos = (x1*x2 + y1*y2)/(float)(Math.sqrt((x1*x1+y1*y1)*(x2*x2+y2*y2)));
          if ( cos < 0.7 ) pts.add( pt );
        }
        pts.add( next );
        mPoints = pts;
      }
    }
    retracePath();
  }



  void makeStraight( boolean with_arrow )
  {
    // Log.v( TopoDroidApp.TAG, "make straight with arrow " + with_arrow + " size " + mPoints.size() );
    if ( mPoints.size() < 2 ) return;
    LinePoint first = mPoints.get( 0 );
    LinePoint last  = mPoints.get( mPoints.size() - 1 );
    mPoints.clear();
    mPath = new Path();
    addStartPoint( last.mX, last.mY );
    addPoint( first.mX, first.mY );
    if ( with_arrow ) {
      float dy =   first.mX - last.mX;
      float dx = - first.mY + last.mY;
      float d = 10.0f / FloatMath.sqrt( dx*dx + dy*dy );
      dx *= d;
      dy *= d;
      addPoint( first.mX+dx, first.mY+dy );
    }
    // Log.v( TopoDroidApp.TAG, "make straight final size " + mPoints.size() );
  }
    
  public void addStartPoint( float x, float y ) 
  {
    mPrevPoint = new LinePoint(x,y, null);
    mPoints.add( mPrevPoint );
    mPath.moveTo( x, y );
  }

  public void addPoint( float x, float y ) 
  {
    mPrevPoint = new LinePoint(x,y,mPrevPoint);
    mPoints.add( mPrevPoint );
    mPath.lineTo( x, y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    mPrevPoint = new LinePoint( x1,y1, x2,y2, x,y, mPrevPoint );
    mPoints.add( mPrevPoint );
    mPath.cubicTo( x1,y1, x2,y2, x,y );
  }

  LinePoint insertPointAfter( float x, float y, LinePoint lp )
  {
    int index = mPoints.indexOf(lp);
    if ( index < mPoints.size() ) ++index; // insert before next point
    LinePoint next = lp.next;
    LinePoint pp = new LinePoint(x, y, lp );
    pp.next = next;
    mPoints.add(index, pp);
    retracePath();
    return pp;
  }

  void retracePath()
  {
    int size = mPoints.size();
    if ( size == 0 ) return;
    mPath   = new Path();
    LinePoint lp = mPoints.get(0);
    mPath.moveTo( lp.mX, lp.mY );
    for ( int k=1; k<size; ++k ) {
      lp = mPoints.get(k);
      if ( lp.has_cp ) {
        mPath.cubicTo( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        mPath.lineTo( lp.mX, lp.mY );
      }
    }
  }

  float distance( float x, float y )
  {
    float dist = 1000f; // FIXME
    for ( LinePoint pt : mPoints ) {
      double dx = x - pt.mX;
      double dy = y - pt.mY;
      float d = (float)( Math.sqrt( dx*dx + dy*dy ) );
      if ( d < dist ) dist = d;
    }
    return dist;
  }

  public void close() 
  {
    mPath.close();
    // Log.v( TopoDroidApp.TAG, "area close path" );
  }

  // public ArrayList< LinePoint > getPoints() { return mPoints; }

  // public int size() { return mPoints.size(); }

  @Override
  public void draw( Canvas canvas )
  {
    super.draw( canvas );
    // Path path = new Path();
    // path.addCircle( 0, 0, 1, Path.Direction.CCW );
    // for ( LinePoint lp : mPoints ) {
    //   Path path1 = new Path( path );
    //   path1.offset( lp.mX, lp.mY );
    //   canvas.drawPath( path1, DrawingBrushPaths.highlightPaint );
    // }
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    super.draw( canvas, matrix );
    // Path path = new Path();
    // path.addCircle( 0, 0, 1, Path.Direction.CCW );
    // for ( LinePoint lp : mPoints ) {
    //   Path path1 = new Path( path );
    //   path1.offset( lp.mX, lp.mY );
    //   path1.transform( matrix );
    //   canvas.drawPath( path1, DrawingBrushPaths.highlightPaint );
    // }
  }
}

