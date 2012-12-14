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
  // static final String TAG = "DistoX";


  public static final int STROKE_WIDTH_CURRENT = 1;
  public static final int STROKE_WIDTH_FIXED   = 1;
  public static final int STROKE_WIDTH_PREVIEW = 1;

  static SymbolPointLibrary mPointLib = null;
  static SymbolLineLibrary  mLineLib = null;
  static SymbolAreaLibrary  mAreaLib = null;

  public static String getPointName( int idx, boolean flip )
  {
    return mPointLib.getPointName( idx, flip );
  }

  public static String getPointThName( int index, boolean flip )
  {
    return mPointLib.getPointThName( index, flip );
  }

  public static Paint getPointPaint( int index, boolean flip )
  {
    return mPointLib.getPointPaint( index, flip );
  }

  public static boolean pointHasText( int index )
  {
    return mPointLib.pointHasText( index );
  }


  public static boolean canRotate( int index ) { return mPointLib.canRotate( index ); }

  public static double getPointOrientation( int index ) { return mPointLib.getPointOrientation( index ); }

  public static boolean canFlip( int index ) { return mPointLib.canFlip( index ); }
  
  public static boolean getFlip( int index ) { return mPointLib.getPointFlip( index ); }

  public static void resetPointOrientations( ) { mPointLib.resetOrientations(); }

  public static void rotateRad( int index, double a )
  {
    a = a * TopoDroidApp.RAD2GRAD_FACTOR;
    rotateGrad( index, a );
  }

  public static int getPointLabelIndex() { return mPointLib.mPointLabelIndex; }

  public static void setFlip( int index, boolean flip ) { mPointLib.setPointFlip( index, flip ); }

  public static void rotateGrad( int index, double a ) { mPointLib.rotateGrad( index, a ); }


  // --------------------------------------------------------------------------
  // LINES

  public static final int highlightColor = 0xffff9999;

  // public static final int LINE_ARROW    = 0;
  // public static final int LINE_BORDER   = 1;
  // public static final int LINE_CHIMNEY  = 2;
  // public static final int LINE_CONTOUR  = 3;
  // public static final int LINE_OVERHANG = 4;
  // public static final int LINE_PIT      = 5;
  // public static final int LINE_ROCK     = 6;
  // public static final int LINE_SLOPE    = 7;
  // public static final int LINE_WALL     = 8;
  // public static final int LINE_MAX      = 9;

  // // NOTE if these change change also thl_XXX strings
  // public static final String[] lineThName = {
  //   "arrow",
  //   "border",
  //   "chimney",
  //   "contour",
  //   "overhang",
  //   "pit",
  //   "rock-border",
  //   "slope",
  //   "wall",
  //   "undef"
  // };

  // public static final int[] lineTick = { // 0: none, 1: one, -1: all
  //   1, // arrow
  //   0, // border
  //  -1, // chimney
  //   1, // contour
  //  -1, // overhang
  //  -1, // pit
  //   0, // rock-border
  //  -1, // slope
  //   0, // wall
  //   0
  // };

  // public static String[] lineLocalName;

  // public static final int[] lineColor = {
  //   0xffcccccc, // arrow
  //   0xff00cc00, // border
  //   0xffcc00cc, // chimney
  //   0xff33cc66, // contour
  //   0xff9900ff, // overhang
  //   0xffff00ff, // pit
  //   0xff66ffcc, // rock-border
  //   0xffffcc00, // slope
  //   0xffff0000, // wall
  //   0xffffffff
  // };

  // public static Paint[] linePaint  = null;

  // private static void makeLinePaints()
  // {
  //   linePaint  = new Paint[ LINE_MAX ];
  //   for (int k=0; k< LINE_MAX; ++k ) {
  //     linePaint[k] = new Paint();
  //     linePaint[k].setDither(true);
  //     linePaint[k].setColor( lineColor[k] );
  //     linePaint[k].setStyle(Paint.Style.STROKE);
  //     linePaint[k].setStrokeJoin(Paint.Join.ROUND);
  //     linePaint[k].setStrokeCap(Paint.Cap.ROUND);
  //     linePaint[k].setStrokeWidth( (k == LINE_WALL)? 2 *STROKE_WIDTH_CURRENT 
  //                                                    : STROKE_WIDTH_CURRENT );
  //     if ( k == LINE_CHIMNEY || k == LINE_OVERHANG ) {
  //       linePaint[k].setPathEffect(new DashPathEffect(new float[] {15,5}, 0));
  //     }
  //   }
  // }

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

  // --------------------------------------------------------------------------

  public static Paint highlightPaint  = null;
  public static Paint fixedShotPaint  = null;
  public static Paint fixedSplayPaint = null;
  public static Paint fixedGridPaint  = null;
  public static Paint fixedStationPaint  = null;

  // ===========================================================================

  public static Path getPointPath( int i, boolean flip ) 
  { 
    return mPointLib.getPointPath( i, flip );
  }

  public static Path getPointPath( int i )
  { 
    return mPointLib.getPointPath( i ); 
  }

  // public static void doMakeThNames( Resources res )
  // {
  //   lineLocalName = new String[LINE_MAX];
  //   lineLocalName[LINE_ARROW]   = res.getString( R.string.thl_arrow );
  //   lineLocalName[LINE_BORDER]  = res.getString( R.string.thl_border );
  //   lineLocalName[LINE_CHIMNEY] = res.getString( R.string.thl_chimney );
  //   lineLocalName[LINE_CONTOUR] = res.getString( R.string.thl_contour );
  //   lineLocalName[LINE_OVERHANG]= res.getString( R.string.thl_overhang );
  //   lineLocalName[LINE_PIT]     = res.getString( R.string.thl_pit );
  //   lineLocalName[LINE_ROCK]    = res.getString( R.string.thl_rock_border );
  //   lineLocalName[LINE_SLOPE]   = res.getString( R.string.thl_slope );
  //   lineLocalName[LINE_WALL]    = res.getString( R.string.thl_wall );
  // }

  public static void makePaths( Resources res )
  {
    if ( mPointLib == null ) mPointLib = new SymbolPointLibrary( res );
    if ( mLineLib == null ) mLineLib = new SymbolLineLibrary( res );
    if ( mAreaLib == null ) mAreaLib = new SymbolAreaLibrary( res );
  }

  public static void clearPaths( )
  {
    mPointLib = null;
    mLineLib = null;
    mAreaLib = null;
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
    fixedGridPaint.setColor(0xFF333333); // very dark gray

    fixedStationPaint = new Paint();
    fixedStationPaint.setDither(true);
    fixedStationPaint.setStyle(Paint.Style.STROKE);
    fixedStationPaint.setStrokeJoin(Paint.Join.ROUND);
    fixedStationPaint.setStrokeCap(Paint.Cap.ROUND);
    fixedStationPaint.setStrokeWidth( STROKE_WIDTH_FIXED );
    fixedStationPaint.setColor(0xFFFF3333); // very dark red

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
