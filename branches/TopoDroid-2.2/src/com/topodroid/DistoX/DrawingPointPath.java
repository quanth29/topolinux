/* @file DrawingPointPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
 *        type DRAWING_PATH_POINT
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120706 Therion "scale" option
 * 20121113 sink/spring points toTherion
 * 20121122 points snow/ice flowstone/moonmilk dig/choke crystal/gypsum
 * 20130829 (re)set orientation
 * 201311   removed the flag for overloaded point symbols
 */

package com.topodroid.DistoX;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

// import android.util.Log;

/**
 */
public class DrawingPointPath extends DrawingPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;

  static final int SCALE_NONE = -3; // used to force scaling
  static final int SCALE_XS = -2;
  static final int SCALE_S  = -1;
  static final int SCALE_M  = 0;
  static final int SCALE_L  = 1;
  static final int SCALE_XL = 2;

  // float mXpos;                // scene coords
  // float mYpos;
  int mPointType;
  protected int mScale;       //! symbol scale
  String mOptions;
  double mOrientation;


  public DrawingPointPath( int type, float x, float y, int scale, String options )
  {
    super( DrawingPath.DRAWING_PATH_POINT );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    cx = x;
    cy = y;
    mOptions = options;
    mScale   = SCALE_NONE;
    mOrientation = 0.0;
    if ( DrawingBrushPaths.canRotate( type ) ) {
      mOrientation = DrawingBrushPaths.getPointOrientation(type);
    }
    setPaint( DrawingBrushPaths.getPointPaint( mPointType ) );
    if ( ! DrawingBrushPaths.pointHasText( mPointType ) ) {
      mScale = scale;
    } else {
      mScale = SCALE_M;
    }
    resetPath( 0 );
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation );
  }

  @Override
  void shiftBy( float dx, float dy )
  {
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
  }

  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      resetPath( 0 );
    }
  }

  int getScale() { return mScale; }
      

  private void resetPath( int off )
  {
    Matrix m = new Matrix();
    if ( DrawingBrushPaths.canRotate( mPointType ) ) {
      m.postRotate( (float)mOrientation + off );
    }
    if ( ! DrawingBrushPaths.pointHasText( mPointType ) ) {
      float f = 1.0f;
      switch ( mScale ) {
        case SCALE_XS: f = 0.60f;
        case SCALE_S:  f = 0.77f;
        case SCALE_L:  f = 1.30f;
        case SCALE_XL: f = 1.70f;
      }
      m.postScale(f,f);
    }

    makePath( DrawingBrushPaths.getPointOrigPath( mPointType ), m, cx, cy );
  }
      

  // public void setPos( float x, float y ) 
  // {
  //   cx = x;
  //   cy = y;
  // }

  // public void setPointType( int t ) { mPointType = t; }
  public int pointType() { return mPointType; }

  // public double xpos() { return cx; }
  // public double ypos() { return cy; }

  // public double orientation() { return mOrientation; }

  @Override
  public void setOrientation( double angle ) 
  { 
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + mPointType + " setOrientation " + angle );
    // Log.v( TopoDroidApp.TAG, "Point::setOrientation " + angle );
    mOrientation = angle; 
    while ( mOrientation >= 360.0 ) mOrientation -= 360.0;
    while ( mOrientation < 0.0 ) mOrientation += 360.0;
    resetPath( 0 );
  }

  public String getText() { return null; }

  public void setText( String text ) { }

  public void shiftTo( float x, float y ) // x,y scene coords
  {
    mPath.offset( x-cx, y-cy );
    cx = x;
    cy = y;
  }

  float distance( float x, float y )
  {
    double dx = x - cx;
    double dy = y - cy;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    // Log.v( TopoDroidApp.TAG, "toTherion() Point " + mPointType + " orientation " + mOrientation );

    pw.format(Locale.ENGLISH, "point %.2f %.2f %s", cx*toTherion, -cy*toTherion, 
                              DrawingBrushPaths.getPointThName(mPointType) );
    if ( mOrientation != 0.0 ) {
      // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "point.toTherion type " + mPointType + " orientation " + mOrientation );
      pw.format(Locale.ENGLISH, " -orientation %.2f", mOrientation);
    }

    toTherionOptions( pw );
    pw.format("\n");

    return sw.getBuffer().toString();
  }

  protected void toTherionOptions( PrintWriter pw )
  {
    if ( mScale != SCALE_M ) {
      switch ( mScale ) {
        case SCALE_XS: pw.format( " -scale xs" ); break;
        case SCALE_S:  pw.format( " -scale s" ); break;
        case SCALE_L:  pw.format( " -scale l" ); break;
        case SCALE_XL: pw.format( " -scale xl" ); break;
      }
    }

    if ( mOptions != null && mOptions.length() > 0 ) {
      pw.format(" %s", mOptions );
    }
  }

}

