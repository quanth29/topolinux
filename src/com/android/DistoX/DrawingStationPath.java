/* @file DrawingStationPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid drawing: station point 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created
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
public class DrawingStationPath extends DrawingPath
{
  // static final String TAG = "DistoX";
  private static float toTherion = TopoDroidApp.TO_THERION;

  float mXpos;
  float mYpos;
  protected int mScale;       //! symbol scale
  String mName;               // station name


  public DrawingStationPath( String name, float x, float y, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    mXpos = x;
    mYpos = y;
    mName = name;

    mScale = DrawingPointPath.SCALE_NONE; // scale
    path = null;
    setScale( scale );
    mPaint = DrawingBrushPaths.mStationSymbol.mPaint;
    // Log.v( TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  public DrawingStationPath( DrawingStationName st, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    mXpos = st.mX;
    mYpos = st.mY;
    mName = st.mName;

    mScale = DrawingPointPath.SCALE_NONE; // scale
    path = null;
    setScale( scale );
    mPaint = DrawingBrushPaths.mStationSymbol.mPaint;
    // Log.v( TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      // station point does not have text
      float f = 1.0f;
      switch ( mScale ) {
        case DrawingPointPath.SCALE_XS: f = 0.60f;
        case DrawingPointPath.SCALE_S:  f = 0.77f;
        case DrawingPointPath.SCALE_L:  f = 1.30f;
        case DrawingPointPath.SCALE_XL: f = 1.70f;
      }
      Matrix m = new Matrix();
      m.postScale(f,f);
      path = new Path( DrawingBrushPaths.mStationSymbol.mPath );
      path.transform( m );
      path.offset( mXpos, mYpos );
    }  
  }
      
  // int getScale() { return mScale; }

  // public void setPos( float x, float y ) 
  // {
  //   mXpos = x;
  //   mYpos = y;
  // }

  // public void setPointType( int t ) { mPointType = t; }
  // public int pointType() { return mPointType; }

  // public double xpos() { return mXpos; }
  // public double ypos() { return mYpos; }

  // public double orientation() { return mOrientation; }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    // Log.v( "DistoX", "toTherion() Point " + mPointType + " orientation " + mOrientation + " flip " +
    //                  mFlip + " flippable " +
    //                  DrawingBrushPaths.canFlip( mPointType ) );

    pw.format(Locale.ENGLISH, "point %.2f %.2f station -name %s\n", mXpos*toTherion, -mYpos*toTherion, mName );
    return sw.getBuffer().toString();
  }
}

