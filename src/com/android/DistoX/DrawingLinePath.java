/* @file DrawingLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line-path (lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120621 attribute "outline" and "options"
 * 20130829 line point(s) shift
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

import android.util.FloatMath;
import android.util.Log;

/**
 */
public class DrawingLinePath extends DrawingPath
{
  static final int OUTLINE_OUT = 1;
  static final int OUTLINE_IN = -1;
  static final int OUTLINE_NONE = 0;
  static final int OUTLINE_UNDEF = -2;

  int mLineType;
  boolean mClosed;
  boolean mReversed;
  int mOutline; 
  String mOptions;
  private float alpha0, alpha1;  // temporary
  private BezierPoint c1, c2;

  ArrayList< LinePoint > points; 

  public DrawingLinePath( int type )
  {
    super( DrawingPath.DRAWING_PATH_LINE );
    // DrawingBrushPaths.makePaths( );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingLinePath cstr type " + type );
    mLineType = type;
    mClosed   = false;
    mReversed = false;
    mOutline  = ( mLineType == DrawingBrushPaths.mLineLib.mLineWallIndex )? OUTLINE_OUT : OUTLINE_NONE;
    mOptions  = null;
    
    points  = new ArrayList< LinePoint >();
    path    = new Path();
    setPaint( DrawingBrushPaths.getLinePaint( type, mReversed ) );
  }

  boolean splitAt( float x, float y, DrawingLinePath line1, DrawingLinePath line2 ) // x,y scene point
  {
    line1.mOutline = mOutline;
    line1.mOptions = mOptions;
    line1.mReversed = mReversed;
    int kmin = -1;
    float dmin = 100000f;
    int k = 0;
    for ( LinePoint pt : points ) {
      float d = Math.abs( pt.mX - x ) + Math.abs( pt.mY - y );
      if ( d < dmin ) { dmin = d; kmin = k; }
      ++ k;
    }
    if ( kmin <= 0 && kmin >= points.size() - 1 ) return false;
    k = 0;
    LinePoint lp = points.get(k);
    line1.addStartPoint( lp.mX, lp.mY );
    for ( ; k < kmin; ++k ) {
      lp = points.get(k);
      if ( lp.has_cp ) {
        line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line1.addPoint( lp.mX, lp.mY );
      }
    }
    lp = points.get(k);
    if ( lp.has_cp ) {
      line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
    } else {
      line1.addPoint( lp.mX, lp.mY );
    }
    line2.addStartPoint( lp.mX, lp.mY );
    for ( ; k < points.size(); ++k ) {
      lp = points.get(k);
      if ( lp.has_cp ) {
        line2.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line2.addPoint( lp.mX, lp.mY );
      }
    }
    return true;
  }

  public void addStartPoint( float x, float y ) // x,y scene point
  {
    points.add( new LinePoint(x,y) );
    path.moveTo( x, y );
  }

  public void addPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.lineTo( x, y );
    // addTick( x, y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    points.add( new LinePoint( x1,y1, x2,y2, x,y ) );
    path.cubicTo( x1,y1, x2,y2, x,y );
    // addTick( x, y );
  }

  void shiftTo( float x0, float y0, float dx, float dy )
  {
    Log.v("DistoX", "shift line " + x0 + " " + y0 + " by " + dx + " " + dy );
    float d0 = 1000f; // FIXME
    int k0 = -1;
    for ( int k=0; k<points.size(); ++k ) {
      LinePoint pt = points.get( k );
      float d = Math.abs( pt.mX - x0 ) + Math.abs( pt.mY - y0 );
      if ( d < d0 ) {
        d0 = d;
        k0 = k;
      }
    }
    d0 += TopoDroidApp.mLineShift;
    for ( int k=k0; k<points.size(); ++k ) {
      LinePoint lp = points.get( k );
      float d = Math.abs( lp.mX - x0 ) + Math.abs( lp.mY - y0 );
      if ( d >= d0 ) break;
      float zx = (1 - d/d0)*dx;
      float zy = (1 - d/d0)*dy;
      if ( lp.has_cp ) {
        lp.mX1 += zx;
        lp.mY1 += zy;
        lp.mX2 += zx;
        lp.mY2 += zy;
      } 
      lp.mX += zx;
      lp.mY += zy;
    }
    for ( int k=k0-1; k>=0; --k ) {
      LinePoint lp = points.get( k );
      float d = Math.abs( lp.mX - x0 ) + Math.abs( lp.mY - y0 );
      if ( d >= d0 ) break;
      float zx = (1 - d/d0)*dx;
      float zy = (1 - d/d0)*dy;
      if ( lp.has_cp ) {
        lp.mX1 += zx;
        lp.mY1 += zy;
        lp.mX2 += zx;
        lp.mY2 += zy;
      } 
      lp.mX += zx;
      lp.mY += zy;
    }
    retracePath();
  }

  void retracePath()
  {
    ArrayList< LinePoint > oldpoints = points;
    Path oldpath = path;
    points  = new ArrayList< LinePoint >();
    path    = new Path();
    for ( LinePoint lp : oldpoints ) {
      if ( points.size() == 0 ) {
        addStartPoint( lp.mX, lp.mY );
      } else {  
        if ( lp.has_cp ) {
          addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
        } else {
          addPoint( lp.mX, lp.mY );
        }
      }
    }
  }

  @Override
  float distance( float x, float y )
  {
    float dist = 1000f; // FIXME
    for ( LinePoint pt : points ) {
      float d = Math.abs( pt.mX - x ) + Math.abs( pt.mY - y );
      if ( d < dist ) dist = d;
    }
    return dist;
  }

  void setReversed( boolean reversed )
  {
    if ( reversed != mReversed ) {
      mReversed = reversed;
      // retracePath();
      setPaint( DrawingBrushPaths.getLinePaint( mLineType, mReversed ) );
    }
  }

  public int lineType() { return mLineType; }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line %s", DrawingBrushPaths.getLineThName(mLineType) );
    if ( mClosed ) {
      pw.format(" -close on");
    }
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
      if ( mOutline == OUTLINE_IN ) {
        pw.format(" -outline in");
      } else if ( mOutline == OUTLINE_NONE ) {
        pw.format(" -outline none");
      }
    } else {
      if ( mOutline == OUTLINE_IN ) {
        pw.format(" -outline in");
      } else if ( mOutline == OUTLINE_OUT ) {
        pw.format(" -outline out");
      }
    }
    if ( mReversed ) {
      pw.format(" -reversed on");
    }
    if ( mOptions != null && mOptions.length() > 0 ) {
      pw.format(" %s", mOptions );
    }
    pw.format("\n");

    for ( LinePoint pt : points ) {
      pt.toTherion( pw );
    }
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineSlopeIndex ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }


}

