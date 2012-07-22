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

// import android.util.Log;

/**
 */
public class DrawingStation extends DrawingPointPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;

  String mName;
  float mX;
  float mY;

  // private Paint paint;

  public DrawingStation( String n, float x, float y )
  {
    super( DrawingBrushPaths.POINT_LABEL,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    mName = n;
    mX = x; // scene coordinate
    mY = y; 
    
    path = new Path();
    path.moveTo( 0, 0 );
    path.lineTo( 20*mName.length(), 0 );
    path.offset( mX, mY );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // Log.v( "DistoX", "LABEL " + mName );
    canvas.drawTextOnPath( mName, path, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // Log.v( "DistoX", "LABEL " + mName );
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
