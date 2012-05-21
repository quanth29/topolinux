/* @file DrawingPointPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
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
import java.util.Locale;

/**
 */
public class DrawingPointPath extends DrawingPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;

  private float mXpos;
  private float mYpos;
  private int mPointType;
  private double mOrientation;

  public DrawingPointPath( int type, float x, float y )
  {
    super( DrawingPath.DRAWING_PATH_POINT );
    // Log.v( "DistoX", "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    mXpos = x;
    mYpos = y;
    mOrientation = 0.0;
    if ( DrawingBrushPaths.canRotate( type ) ) {
      // Log.v("DistoX", "new point type " + type + " setOrientation to " + DrawingBrushPaths.orientation(type) );
      setOrientation( DrawingBrushPaths.orientation(type) );
    }
    setPaint( DrawingBrushPaths.pointPaint[ type ] );
    if ( type != DrawingBrushPaths.POINT_LABEL ) {
      path = new Path( DrawingBrushPaths.get( type ) );
      path.offset( x, y );
    }
  }

  // public void setPos( float x, float y ) 
  // {
  //   mXpos = x;
  //   mYpos = y;
  // }

  // public void setPointType( int t ) { mPointType = t; }
  public int pointType() { return mPointType; }

  public double xpos() { return mXpos; }
  public double ypos() { return mYpos; }

  // public double orientation() { return mOrientation; }

  @Override
  public void setOrientation( double angle ) 
  { 
    // Log.v( "DistoX", "Point " + mPointType + " setOrientation " + angle );
    mOrientation = angle; 
    while ( mOrientation >= 360.0 ) mOrientation -= 360.0;
    while ( mOrientation < 0.0 ) mOrientation += 360.0;
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( mPointType == DrawingBrushPaths.POINT_STAL ) {
      if ( mOrientation > 90 && mOrientation < 270 ) {
        pw.format(Locale.ENGLISH, "point %.2f %.2f stalagmite", mXpos*toTherion, -mYpos*toTherion );
      } else {
        pw.format(Locale.ENGLISH, "point %.2f %.2f stalactite", mXpos*toTherion, -mYpos*toTherion );
      }
    } else if ( mPointType == DrawingBrushPaths.POINT_DANGER ) {
      pw.format(Locale.ENGLISH, "point %.2f %.2f label -text \"!\"", 
         mXpos*toTherion, -mYpos*toTherion );
    } else {
      pw.format(Locale.ENGLISH, "point %.2f %.2f %s", 
         mXpos*toTherion, -mYpos*toTherion, 
         DrawingBrushPaths.pointName[mPointType] );
      if ( mOrientation != 0.0 ) {
        // Log.v( "DistoX", "point.toTherion type " + mPointType + " orientation " + mOrientation );
        pw.format(Locale.ENGLISH, " -orientation %.2f", mOrientation);
      }
    }
    pw.format("\n");
    return sw.getBuffer().toString();
  }

}

