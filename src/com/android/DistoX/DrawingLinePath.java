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
  private int mTick;

  ArrayList< LinePoint > points; 

  public DrawingLinePath( int type )
  {
    super( DrawingPath.DRAWING_PATH_LINE );
    DrawingBrushPaths.makePaths( );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingLinePath cstr type " + type );
    mLineType = type;
    mClosed   = false;
    mReversed = false;
    mOutline  = ( mLineType == DrawingBrushPaths.LINE_WALL )? OUTLINE_OUT : OUTLINE_NONE;
    mOptions  = null;
    
    points  = new ArrayList< LinePoint >();
    path    = new Path();
    setPaint( DrawingBrushPaths.linePaint[ type ] );
    mTick = DrawingBrushPaths.lineTick[mLineType];
  }


  public void addStartPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.moveTo( x, y );
  }

  public void addTick( float x, float y )
  {
    if ( mTick != 0 ) {
      if ( points.size() > 1 ) {
        LinePoint p = points.get( points.size() - 2 );
        float dx = x - p.mX;
        float dy = y - p.mY;
        float d = dx*dx + dy*dy;
        if ( d > 0.0f ) {
          d = (mReversed? -0.2f : 0.2f) * (float)Math.sqrt( d );
          path.lineTo( x+dy/d, y-dx/d );
          path.moveTo( x, y );
          mTick --;
        }
      }
    }
  }

  public void addPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.lineTo( x, y );
    addTick( x, y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    points.add( new LinePoint( x1,y1, x2,y2, x,y ) );
    path.cubicTo( x1,y1, x2,y2, x,y );
    addTick( x, y );
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

  void setReversed( boolean reversed )
  {
    if ( reversed != mReversed ) {
      mReversed = reversed;
      retracePath();
    }
  }

  // public void setLineType( int t ) 
  // { 
  //   if ( t != mLineType ) {
  //     mLineType = t;
  //     mOutline  = ( mLineType == DrawingBrushPaths.LINE_WALL )? OUTLINE_OUT : OUTLINE_NONE;
  //   }
  // }
  public int lineType() { return mLineType; }

  public ArrayList< LinePoint > getPoints() { return points; }

  public int size() { return points.size(); }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line %s", DrawingBrushPaths.lineThName[mLineType] );
    if ( mClosed ) {
      pw.format(" -close on");
    }
    if ( mLineType == DrawingBrushPaths.LINE_WALL ) {
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
    if ( mLineType == DrawingBrushPaths.LINE_SLOPE ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }


}

