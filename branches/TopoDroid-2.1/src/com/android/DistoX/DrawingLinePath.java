/* @file DrawingLinePath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: line-path (lines)
 *
 * The line path id DrawingPath.mPath
 *
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
public class DrawingLinePath extends DrawingPointLinePath
{
  static final int OUTLINE_OUT = 1;
  static final int OUTLINE_IN = -1;
  static final int OUTLINE_NONE = 0;
  static final int OUTLINE_UNDEF = -2;

  // static int mCount = 0;
  // int mCnt;
  int mLineType;
  boolean mReversed;
  int mOutline; 
  String mOptions;

  public DrawingLinePath( int line_type )
  {
    // visible = true,  closed = false
    super( DrawingPath.DRAWING_PATH_LINE, true, false );
    // DrawingBrushPaths.makePaths( );
    // mCnt = ++ mCount;
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingLinePath " + mCnt + " cstr type " + line_type );

    mLineType = line_type;
    mReversed = false;
    mOutline  = ( mLineType == DrawingBrushPaths.mLineLib.mLineWallIndex )? OUTLINE_OUT : OUTLINE_NONE;
    mOptions  = null;
    setPaint( DrawingBrushPaths.getLinePaint( line_type, mReversed ) );
  }

  boolean splitAt( LinePoint lp0, DrawingLinePath line1, DrawingLinePath line2 ) // x,y scene point
  {
    line1.mOutline  = mOutline;
    line1.mOptions  = mOptions;
    line1.mReversed = mReversed;
    line2.mOutline  = mOutline;
    line2.mOptions  = mOptions;
    line2.mReversed = mReversed;

    int k0 = mPoints.indexOf( lp0 );
    if ( k0 <= 0 ) return false;

    LinePoint lp = mPoints.get( 0 );
    line1.addStartPoint( lp.mX, lp.mY );
    for ( int k=1; k<k0; ++ k ) {
      lp = mPoints.get(k);
      if ( lp.has_cp ) {
        line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line1.addPoint( lp.mX, lp.mY );
      }
    }
    lp = mPoints.get(k0);
    if ( lp.has_cp ) {
      line1.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
    } else {
      line1.addPoint( lp.mX, lp.mY );
    }

    line2.addStartPoint( lp.mX, lp.mY );
    for (int k=k0+1; k < mPoints.size(); ++k ) {
      lp = mPoints.get(k);
      if ( lp.has_cp ) {
        line2.addPoint3( lp.mX1, lp.mY1, lp.mX2, lp.mY2, lp.mX, lp.mY );
      } else {
        line2.addPoint( lp.mX, lp.mY );
      }
    }

    // Log.v( TopoDroidApp.TAG, "line " + mCnt + " split: " + mPoints.size() + " --> " + line1.mPoints.size() + " + " + line2.mPoints.size() );
    return true;
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

    for ( LinePoint pt : mPoints ) {
      pt.toTherion( pw );
    }
    if ( mLineType == DrawingBrushPaths.mLineLib.mLineSlopeIndex ) {
      pw.format("  l-size 40\n");
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }


}
