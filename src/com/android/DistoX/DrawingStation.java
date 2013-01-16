/* @file DrawingStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: station point
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
public class DrawingStation extends DrawingPointPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;

  String mName; // station name
  float mX;     // scene coordinates
  float mY;
  boolean mDuplicate; // whether this is a duplicated station

  public DrawingStation( String n, float x, float y, boolean duplicate )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingStation cstr " + n + " " + x + " " + y );
    if ( duplicate ) mPaint = DrawingBrushPaths.duplicateStationPaint;
    mName = n;
    mX = x; // scene coordinate
    mY = y; 
    mDuplicate = duplicate;
    
    path = new Path();
    path.moveTo( 0, 0 );
    path.lineTo( 20*mName.length(), 0 );
    path.offset( mX, mY );
  }

  float distance( float x, float y )
  { 
    Log.v( TopoDroidApp.TAG, " Station distance from " + x + " " + y + " at " + mX + " " + mY );
    return (float)( Math.abs(x-mX) + Math.abs(y-mY) );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingStation::draw LABEL " + mName );
    canvas.drawTextOnPath( mName, path, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingStation::draw[matrix] LABEL " + mName );
    mTransformedPath = new Path( path );
    mTransformedPath.transform( matrix );
    canvas.drawTextOnPath( mName, mTransformedPath, 0f, 0f, mPaint );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "point %.2f %.2f station -name \"%s\"", mX*toTherion, -mY*toTherion, mName );
    return sw.getBuffer().toString();
  }
}
