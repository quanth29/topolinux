/* @file DrawingPointPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120706 Therion "scale" option
 * 20121113 sink/spring points toTherion
 * 20121122 overloaded points snow/ice flowstone/moonmilk dig/choke crystal/gypsum
 */

package com.android.DistoX;

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
  // static final String TAG = "DistoX";
  private static float toTherion = TopoDroidApp.TO_THERION;

  static final int SCALE_NONE = -3; // used to force scaling
  static final int SCALE_XS = -2;
  static final int SCALE_S  = -1;
  static final int SCALE_M  = 0;
  static final int SCALE_L  = 1;
  static final int SCALE_XL = 2;

  float mXpos;
  float mYpos;
  int mPointType;
  protected int mScale;       //! symbol scale
  String mOptions;
  double mOrientation;
  boolean mFlip;


  public DrawingPointPath( int type, float x, float y, int scale, String options )
  {
    super( DrawingPath.DRAWING_PATH_POINT );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    mXpos = x;
    mYpos = y;
    mOptions = options;
    mScale   = SCALE_NONE;
    mOrientation = 0.0;
    if ( DrawingBrushPaths.canRotate( type ) ) {
      // TopoDroidApp.Log(TopoDroidApp.LOG_PATH, "new point type " + type + " setOrientation to " + DrawingBrushPaths.orientation(type) );
      setOrientation( DrawingBrushPaths.orientation(type) );
    }
    setPaint( DrawingBrushPaths.pointPaint[ mPointType ] );
    if ( mPointType != DrawingBrushPaths.POINT_LABEL ) {
      setScale( scale );
    } else {
      mScale = SCALE_M;
    }
  }

  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      if ( mPointType != DrawingBrushPaths.POINT_LABEL ) {
        float f = 1.0f;
        switch ( mScale ) {
          case SCALE_XS: f = 0.60f;
          case SCALE_S:  f = 0.77f;
          case SCALE_L:  f = 1.30f;
          case SCALE_XL: f = 1.70f;
        }
        Matrix m = new Matrix();
        m.postScale(f,f);
        path = new Path( DrawingBrushPaths.get( mPointType ) );
        path.transform( m );
        path.offset( mXpos, mYpos );
      }
    }  
  }
      
  int getScale() { return mScale; }
      

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
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + mPointType + " setOrientation " + angle );
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
    } else if ( mPointType == DrawingBrushPaths.POINT_END ) {
      if ( ( mOrientation > 45 && mOrientation < 135 ) || 
           ( mOrientation > 225 && mOrientation < 305 ) ) {
        pw.format(Locale.ENGLISH, "point %.2f %.2f low-end", mXpos*toTherion, -mYpos*toTherion );
      } else {
        pw.format(Locale.ENGLISH, "point %.2f %.2f narrow-end", mXpos*toTherion, -mYpos*toTherion );
      }
      
    } else if ( mPointType == DrawingBrushPaths.POINT_DANGER ) {
      pw.format(Locale.ENGLISH, "point %.2f %.2f label -text \"!\"", 
         mXpos*toTherion, -mYpos*toTherion );
    } else {
      pw.format(Locale.ENGLISH, "point %.2f %.2f %s", 
         mXpos*toTherion, -mYpos*toTherion, 
         DrawingBrushPaths.pointThName[mPointType] );
      if ( mOrientation != 0.0 ) {
        // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "point.toTherion type " + mPointType + " orientation " + mOrientation );
        pw.format(Locale.ENGLISH, " -orientation %.2f", mOrientation);
      }
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

