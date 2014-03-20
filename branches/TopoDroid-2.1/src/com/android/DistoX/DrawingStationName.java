/* @file DrawingStationName.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing station name (this is not a station point) 
*        type: DRAWING_PATH_NAME
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import android.util.Log;

/**
 */
public class DrawingStationName extends DrawingPointPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;

  String mName; // station name
  // float mX;     // scene coordinates (cx, cy)
  // float mY;
  boolean mDuplicate; // whether this is a duplicated station

  public DrawingStationName( String n, float x, float y, boolean duplicate )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType

    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingStationName cstr " + n + " " + x + " " + y );
    if ( duplicate ) mPaint = DrawingBrushPaths.duplicateStationPaint;
    mName = n;
    cx = x; // scene coordinate
    cy = y; 
    mDuplicate = duplicate;
    
    makeStraightPath( 0, 0, 20*mName.length(), 0, cx, cy );
  }

  float distance( float x, float y )
  { 
    double dx = x - cx;
    double dy = y - cy;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingStationName::draw LABEL " + mName );
    canvas.drawTextOnPath( mName, mPath, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingStationName::draw[matrix] LABEL " + mName );
    mTransformedPath = new Path( mPath );
    mTransformedPath.transform( matrix );
    canvas.drawTextOnPath( mName, mTransformedPath, 0f, 0f, mPaint );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "point %.2f %.2f station -name \"%s\"", cx*toTherion, -cy*toTherion, mName );
    return sw.getBuffer().toString();
  }
}
