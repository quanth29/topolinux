/* @file DrawingBrushPaths.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: brushes (points and lines)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120614 new points: end (narrow-end, low-end) pebbles, snow
 * 20120614 new area: snow
 * 20120619 new line: overhang
 * 20120725 TopoDroidApp log
 * 20121109 new point: dig
 * 20121113 new point: sink/spring
 * 20121115 new point: crystal, merged together points ice/snow
 * 20121122 new points: moonmilk (overloaded to flowstone), choke (overloaded to dig)
 * 20121201 using SymbolPoint class
 * 20121212 clearPaths()
 * 20121220 reload symbols (to add new symbols)
 * 20120108 station symbol (a red dot)
 * 20131119 area color getter
 * 20131210 second grid paint
 * 20140305 symbol units changes take immadiate effect (N.B. old symbols must still redrawn)
 */
package com.android.DistoX;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;

import android.content.res.Resources;

// import android.util.Log;

import java.lang.Math;


/**
 * gereric brush 
 */
public class DrawingBrushPaths
{
  public static final int STROKE_WIDTH_CURRENT = 1;
  public static final int STROKE_WIDTH_FIXED   = 1;
  public static final int STROKE_WIDTH_PREVIEW = 1;

  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib = null;
  static SymbolAreaLibrary  mAreaLib = null;
  static SymbolPoint mStationSymbol = null;
  static boolean mReloadSymbols = false; // whether to reload symbols

  public static String getPointName( int idx )
  {
    return mPointLib.getPointName( idx );
  }

  public static String getPointThName( int index )
  {
    return mPointLib.getPointThName( index );
  }

  public static Paint getPointPaint( int index )
  {
    return mPointLib.getPointPaint( index );
  }

  public static boolean pointHasText( int index )
  {
    return mPointLib.pointHasText( index );
  }


  public static boolean canRotate( int index ) { return mPointLib.canRotate( index ); }

  public static double getPointOrientation( int index ) { return mPointLib.getPointOrientation( index ); }

  public static void resetPointOrientations( ) { mPointLib.resetOrientations(); }

  public static void rotateRad( int index, double a )
  {
    a = a * TopoDroidUtil.RAD2GRAD;
    rotateGrad( index, a );
  }

  public static int getPointLabelIndex() { return mPointLib.mPointLabelIndex; }

  public static void rotateGrad( int index, double a ) { mPointLib.rotateGrad( index, a ); }


  // --------------------------------------------------------------------------
  // LINES

  public static final int highlightColor = 0xffff9999;
  public static final int highlightFill  = 0x6600cc00;

  public static String getLineName( int idx )
  {
    return mLineLib.getLineName( idx );
  }

  public static String getLineThName( int index )
  {
    return mLineLib.getLineThName( index );
  }

  public static Paint getLinePaint( int index, boolean reversed )
  {
    return mLineLib.getLinePaint( index, reversed );
  }

  // -----------------------------------------------------------------------
  // AREAS


  public static String getAreaName( int idx )
  {
    return mAreaLib.getAreaName( idx );
  }

  public static String getAreaThName( int index )
  {
    return mAreaLib.getAreaThName( index );
  }

  public static Paint getAreaPaint( int index )
  {
    return mAreaLib.getAreaPaint( index );
  }

  public static int getAreaColor( int index )
  {
    return mAreaLib.getAreaColor( index );
  }

  // --------------------------------------------------------------------------

  public static Paint highlightPaint  = null;
  public static Paint highlightPaint2 = null;
  public static Paint fixedShotPaint  = null;
  public static Paint fixedSplayPaint = null;
  public static Paint fixedGridPaint  = null;
  public static Paint fixedGrid10Paint  = null;
  public static Paint fixedStationPaint  = null;
  public static Paint duplicateStationPaint = null;

  // ===========================================================================

  public static Path getPointPath( int i ) { return mPointLib.getPointPath( i ); }

  public static Path getPointOrigPath( int i ) { return mPointLib.getPointOrigPath( i ); }

  public static void makePaths( Resources res )
  {
    if ( mStationSymbol == null ) {
      mStationSymbol = new SymbolPoint( "station", "station", 0xffff6633, 
        "addCircle 0 0 0.4 moveTo -3.0 1.73 lineTo 3.0 1.73 lineTo 0.0 -3.46 lineTo -3.0 1.73", false );
    }
    if ( mPointLib == null ) mPointLib = new SymbolPointLibrary( res );
    if ( mLineLib == null ) mLineLib = new SymbolLineLibrary( res );
    if ( mAreaLib == null ) mAreaLib = new SymbolAreaLibrary( res );

    if ( mReloadSymbols ) {
      mPointLib.loadUserPoints();
      mLineLib.loadUserLines();
      mAreaLib.loadUserAreas();
      mReloadSymbols = false;
    }
  }

  public static void reloadPointLibrary( Resources res )
  {
    mPointLib = new SymbolPointLibrary( res );
  }

  public static void doMakePaths()
  {

    highlightPaint = new Paint();
    highlightPaint.setDither(true);
    highlightPaint.setColor( highlightColor );
    highlightPaint.setStyle(Paint.Style.STROKE);
    highlightPaint.setStrokeJoin(Paint.Join.ROUND);
    highlightPaint.setStrokeCap(Paint.Cap.ROUND);
    highlightPaint.setStrokeWidth( STROKE_WIDTH_CURRENT );

    highlightPaint2 = new Paint();
    highlightPaint2.setDither(true);
    highlightPaint2.setColor( highlightFill );
    highlightPaint2.setStyle(Paint.Style.FILL);
    highlightPaint2.setStrokeJoin(Paint.Join.ROUND);
    highlightPaint2.setStrokeCap(Paint.Cap.ROUND);
    highlightPaint2.setStrokeWidth( STROKE_WIDTH_CURRENT );

    fixedShotPaint = new Paint();
    fixedShotPaint.setDither(true);
    fixedShotPaint.setStyle(Paint.Style.STROKE);
    fixedShotPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedShotPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedShotPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedShotPaint.setColor(0xFFbbbbbb); // light gray

    fixedSplayPaint = new Paint();
    fixedSplayPaint.setDither(true);
    fixedSplayPaint.setStyle(Paint.Style.STROKE);
    fixedSplayPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedSplayPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedSplayPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedSplayPaint.setColor(0xFF666666); // dark gray

    fixedGridPaint = new Paint();
    fixedGridPaint.setDither(true);
    fixedGridPaint.setStyle(Paint.Style.STROKE);
    fixedGridPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedGridPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedGridPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedGridPaint.setColor(0x99666666); // very dark gray

    fixedGrid10Paint = new Paint();
    fixedGrid10Paint.setDither(true);
    fixedGrid10Paint.setStyle(Paint.Style.STROKE);
    fixedGrid10Paint.setStrokeJoin(Paint.Join.ROUND);
    fixedGrid10Paint.setStrokeCap(Paint.Cap.ROUND);
    fixedGrid10Paint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedGrid10Paint.setColor(0x99999999); // not so dark gray

    fixedStationPaint = new Paint();
    fixedStationPaint.setDither(true);
    fixedStationPaint.setStyle(Paint.Style.STROKE);
    fixedStationPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedStationPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedStationPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedStationPaint.setColor(0xFFFF3333); // very dark red

    duplicateStationPaint = new Paint();
    duplicateStationPaint.setDither(true);
    duplicateStationPaint.setStyle(Paint.Style.STROKE);
    duplicateStationPaint.setStrokeJoin(Paint.Join.ROUND);
    duplicateStationPaint.setStrokeCap(Paint.Cap.ROUND);
    duplicateStationPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    duplicateStationPaint.setColor(0xFF3333FF); // very dark blue

    // DEBUG
    
    // debugRed = new Paint();
    // debugRed.setDither(true);
    // debugRed.setStyle(Paint.Style.STROKE);
    // debugRed.setStrokeJoin(Paint.Join.ROUND);
    // debugRed.setStrokeCap(Paint.Cap.ROUND);
    // debugRed.setStrokeWidth( STROKE_WIDTH_FIXED );
    // debugRed.setColor(0xFFFF0000); // red

    // debugGreen = new Paint();
    // debugGreen.setDither(true);
    // debugGreen.setStyle(Paint.Style.STROKE);
    // debugGreen.setStrokeJoin(Paint.Join.ROUND);
    // debugGreen.setStrokeCap(Paint.Cap.ROUND);
    // debugGreen.setStrokeWidth( STROKE_WIDTH_FIXED );
    // debugGreen.setColor(0xFF00FF00); // green

    // debugBlue = new Paint();
    // debugBlue.setDither(true);
    // debugBlue.setStyle(Paint.Style.STROKE);
    // debugBlue.setStrokeJoin(Paint.Join.ROUND);
    // debugBlue.setStrokeCap(Paint.Cap.ROUND);
    // debugBlue.setStrokeWidth( STROKE_WIDTH_FIXED );
    // debugBlue.setColor(0xFF0000FF); // blue

  }

}
