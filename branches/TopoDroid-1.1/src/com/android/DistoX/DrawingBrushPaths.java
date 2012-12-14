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
 */
package com.android.DistoX;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;

import android.content.res.Resources;

import java.lang.Math;


/**
 * gereric brush 
 */
public class DrawingBrushPaths
{
  public static final int STROKE_WIDTH_CURRENT = 1;
  public static final int STROKE_WIDTH_FIXED   = 1;
  public static final int STROKE_WIDTH_PREVIEW = 1;

  public static final int POINT_AIR       = 0;
  public static final int POINT_ANCHOR    = 1;
  public static final int POINT_BLOCK     = 2;
  public static final int POINT_CLAY      = 3;
  public static final int POINT_CONT      = 4;
  public static final int POINT_DANGER    = 5;
  public static final int POINT_DEBRIS    = 6;
  public static final int POINT_END       = 7;
  public static final int POINT_ENTRANCE  = 8;
  public static final int POINT_FLOWSTONE = 9;
  public static final int POINT_GRADIENT  = 10;
  public static final int POINT_HELICTITE = 11;
  public static final int POINT_ICE       = 12;
  public static final int POINT_LABEL     = 13;
  public static final int POINT_PEBBLES   = 14;
  public static final int POINT_PILLAR    = 15;
  public static final int POINT_POPCORN   = 16;
  public static final int POINT_SAND      = 17;
  public static final int POINT_SNOW      = 18;
  public static final int POINT_STAL      = 19;
  public static final int POINT_WATER     = 20;
  public static final int POINT_WATERFLOW = 21;
  public static final int POINT_MAX       = 22;

  // NOTE if these change change also thp_XXX strings
  public static final String[] pointThName = {
    "air-draught", "anchor",    "blocks", "clay",     "continuation",
    "danger",      "debris",    "end",    "entrance", "flowstone",
    "gradient",    "helictite", "ice",    "label",    "pebbles",
    "pillar",      "popcorn",   "sand",   "snow",     "stal",
    "water",       "water-flow",  "undef"
  };

  public static String[] pointLocalName;

  public static final int[] pointColor = {
    0xff6666ff, 0xffffffff, 0xffffcc00, 0xffffcc00, 0xffffffff, 
    0xffffffff, 0xffffcc00, 0xffff0000, 0xffff0000, 0xffffff00,
    0xff00ff00, 0xffffff33, 0xffffffff, 0xffffffff, 0xffffcc00,
    0xffffcc00, 0xffffcc00, 0xffffff00, 0xffffffff, 0xffffff00,
    0xff0033ff, 0xff0000ff, 0xffcccccc
  };

  public static final boolean[] mRotable = {
    true,  false, false, false, false,
    false, false, true,  true,  false,
    true,  false, false, false, false,
    false, false, false, false, true,
    false, true,  false
  };

  public static final double[] mOrientation = {
    0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0, 0.0, 0.0,
    0.0, 0.0, 0.0
  };

  public static final int highlightColor = 0xffff9999;

  public static final int LINE_ARROW    = 0;
  public static final int LINE_BORDER   = 1;
  public static final int LINE_CHIMNEY  = 2;
  public static final int LINE_CONTOUR  = 3;
  public static final int LINE_OVERHANG = 4;
  public static final int LINE_PIT      = 5;
  public static final int LINE_ROCK     = 6;
  public static final int LINE_SLOPE    = 7;
  public static final int LINE_WALL     = 8;
  public static final int LINE_MAX      = 9;

  // NOTE if these change change also thl_XXX strings
  public static final String[] lineThName = {
    "arrow",
    "border",
    "chimney",
    "contour",
    "overhang",
    "pit",
    "rock-border",
    "slope",
    "wall",
    "undef"
  };

  public static final int[] lineTick = { // 0: none, 1: one, -1: all
    1, // arrow
    0, // border
   -1, // chimney
    1, // contour
   -1, // overhang
   -1, // pit
    0, // rock-border
   -1, // slope
    0, // wall
    0
  };

  public static String[] lineLocalName;

  public static final int[] lineColor = {
    0xffcccccc, // arrow
    0xff00cc00, // border
    0xffcc00cc, // chimney
    0xff33cc66, // contour
    0xff9900ff, // overhang
    0xffff00ff, // pit
    0xff66ffcc, // rock-border
    0xffffcc00, // slope
    0xffff0000, // wall
    0xffffffff
  };

  public static final int AREA_BLOCKS  = 0;
  public static final int AREA_CLAY    = 1;
  public static final int AREA_DEBRIS  = 2;
  public static final int AREA_ICE     = 3;
  public static final int AREA_PEBBLES = 4;
  public static final int AREA_SAND    = 5;
  public static final int AREA_SNOW    = 6;
  public static final int AREA_WATER   = 7;
  public static final int AREA_MAX     = 8;

  // NOTE if these change change also tha_XXX strings
  public static final String[] areaThName = {
    "blocks",
    "clay",
    "debris",
    "ice",
    "pebbles",
    "sand",
    "snow",
    "water",
    "undef"
  };
 
  public static String[] areaLocalName;

  public static final int[] areaColor = {
    0x66ff3333, // blocks
    0x6699ff00, // clay
    0x66ff6666, // debris
    0x66cccccc, // ice
    0x66ff9900, // pebbles
    0x66ffff66, // sand
    0x66ffffff, // snow
    0x660000ff, // water
    0x66000000
  };

  public static Path[]  paths      = null;
  public static Paint[] pointPaint = null;
  public static Paint[] linePaint  = null;
  public static Paint[] areaPaint  = null;
  public static Paint highlightPaint  = null;
  public static Paint fixedShotPaint  = null;
  public static Paint fixedSplayPaint = null;
  public static Paint fixedGridPaint  = null;
  public static Paint fixedStationPaint  = null;

  public static boolean canRotate( int index ) { return mRotable[index]; }

  public static double orientation( int index ) { return mOrientation[index]; }

  public static void resetPointOrientations( )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "resetPointOrientations()" );
    for ( int k=0; k<=POINT_MAX; ++k ) mOrientation[k] = 0.0;
    paths = null;
    makePaths( );
  }

  public static void rotateRad( int index, double a )
  {
    a = a * TopoDroidApp.RAD2GRAD_FACTOR;
    rotateGrad( index, a );
  }

  public static void rotateGrad( int index, double a )
  {
    if ( index == POINT_STAL ) {
      if ( Math.abs(a) < 90.0 ) return;
      if ( a >= 90.0 ) a = 180.0;
      else a = -180.0;
    } else if ( index == POINT_END ) {
      if ( Math.abs(a) < 90.0 ) return;
      if ( a >= 90.0 ) a = 90.0;
      else a = -90.0;
    }
    Matrix m = new Matrix();
    mOrientation[index] += a;
    if ( mOrientation[index] > 360.0 ) mOrientation[index] -= 360.0;
    if ( mOrientation[index] < 0.0 )   mOrientation[index] += 360.0;
    m.postRotate( (float)(a) );
    paths[index].transform( m );
  }


  public static Path[] getPaths( ) 
  {
    makePaths( );
    return paths;
  }

  public static Path get( int i )
  {
    if ( i < 0 || i >= POINT_MAX ) return null;
    makePaths( );
    return paths[ i ];
  }

  public static void doMakeThNames( Resources res )
  {
    pointLocalName = new String[POINT_MAX];
    pointLocalName[POINT_AIR]       = res.getString( R.string.thp_air_draught );
    pointLocalName[POINT_ANCHOR]    = res.getString( R.string.thp_anchor );
    pointLocalName[POINT_BLOCK]     = res.getString( R.string.thp_blocks );
    pointLocalName[POINT_CLAY]      = res.getString( R.string.thp_clay );
    pointLocalName[POINT_CONT]      = res.getString( R.string.thp_continuation );
    pointLocalName[POINT_DANGER]    = res.getString( R.string.thp_danger );
    pointLocalName[POINT_DEBRIS]    = res.getString( R.string.thp_debris );
    pointLocalName[POINT_END]       = res.getString( R.string.thp_end );
    pointLocalName[POINT_ENTRANCE]  = res.getString( R.string.thp_entrance );
    pointLocalName[POINT_FLOWSTONE] = res.getString( R.string.thp_flowstone );
    pointLocalName[POINT_GRADIENT]  = res.getString( R.string.thp_gradient );
    pointLocalName[POINT_HELICTITE] = res.getString( R.string.thp_helictite );
    pointLocalName[POINT_ICE]       = res.getString( R.string.thp_ice );
    pointLocalName[POINT_LABEL]     = res.getString( R.string.thp_label );
    pointLocalName[POINT_PEBBLES]   = res.getString( R.string.thp_pebbles );
    pointLocalName[POINT_PILLAR]    = res.getString( R.string.thp_pillar );
    pointLocalName[POINT_POPCORN]   = res.getString( R.string.thp_popcorn );
    pointLocalName[POINT_SAND]      = res.getString( R.string.thp_sand );
    pointLocalName[POINT_SNOW]      = res.getString( R.string.thp_snow );
    pointLocalName[POINT_STAL]      = res.getString( R.string.thp_stal );
    pointLocalName[POINT_WATER]     = res.getString( R.string.thp_water );
    pointLocalName[POINT_WATERFLOW] = res.getString( R.string.thp_water_flow );

    lineLocalName = new String[LINE_MAX];
    lineLocalName[LINE_ARROW]   = res.getString( R.string.thl_arrow );
    lineLocalName[LINE_BORDER]  = res.getString( R.string.thl_border );
    lineLocalName[LINE_CHIMNEY] = res.getString( R.string.thl_chimney );
    lineLocalName[LINE_CONTOUR] = res.getString( R.string.thl_contour );
    lineLocalName[LINE_OVERHANG]= res.getString( R.string.thl_overhang );
    lineLocalName[LINE_PIT]     = res.getString( R.string.thl_pit );
    lineLocalName[LINE_ROCK]    = res.getString( R.string.thl_rock_border );
    lineLocalName[LINE_SLOPE]   = res.getString( R.string.thl_slope );
    lineLocalName[LINE_WALL]    = res.getString( R.string.thl_wall );

    areaLocalName = new String[AREA_MAX];
    areaLocalName[AREA_BLOCKS]  = res.getString( R.string.tha_blocks );
    areaLocalName[AREA_CLAY]    = res.getString( R.string.tha_clay );
    areaLocalName[AREA_DEBRIS]  = res.getString( R.string.tha_debris );
    areaLocalName[AREA_ICE]     = res.getString( R.string.tha_ice );
    areaLocalName[AREA_PEBBLES] = res.getString( R.string.tha_pebbles );
    areaLocalName[AREA_SAND]    = res.getString( R.string.tha_sand );
    areaLocalName[AREA_SNOW]    = res.getString( R.string.tha_snow );
    areaLocalName[AREA_WATER]   = res.getString( R.string.tha_water );
  }

  public static void makePaths( )
  {
    if ( paths == null ) doMakePaths();
  }

  public static void doMakePaths()
  {
    float unit = TopoDroidApp.mUnit;
    paths = new Path[ POINT_MAX ];
    pointPaint = new Paint[ POINT_MAX ];
    linePaint  = new Paint[ LINE_MAX ];
    areaPaint  = new Paint[ AREA_MAX ];

    paths[ POINT_AIR ] = new Path();
    paths[ POINT_AIR ].moveTo(-4*unit,  7*unit);
    paths[ POINT_AIR ].lineTo( 0*unit,  3*unit);
    paths[ POINT_AIR ].lineTo( 0*unit,-10*unit);
    paths[ POINT_AIR ].lineTo( 4*unit, -6*unit);
    paths[ POINT_AIR ].moveTo( 0*unit,-10*unit);
    paths[ POINT_AIR ].lineTo(-4*unit, -6*unit);
    paths[ POINT_AIR ].moveTo(-4*unit,  4*unit);
    paths[ POINT_AIR ].lineTo( 0*unit,  0*unit);

    paths[ POINT_ANCHOR ] = new Path();
    paths[ POINT_ANCHOR ].moveTo(  0*unit,  5*unit );
    paths[ POINT_ANCHOR ].lineTo(  0*unit, -5*unit );
    paths[ POINT_ANCHOR ].addCircle(  0*unit,  0*unit, 5*unit, Path.Direction.CCW );
    // paths[ POINT_ANCHOR ].cubicTo( 2*unit, 5*unit, 5*unit, 2*unit, 5*unit, 0*unit );
    // paths[ POINT_ANCHOR ].cubicTo( 5*unit,-2*unit, 2*unit,-5*unit, 0*unit,-5*unit );
    // paths[ POINT_ANCHOR ].lineTo(  0*unit, 5*unit );
    // paths[ POINT_ANCHOR ].cubicTo(-2*unit, 5*unit,-5*unit, 2*unit,-5*unit, 0*unit );
    // paths[ POINT_ANCHOR ].cubicTo(-5*unit,-2*unit,-2*unit,-5*unit, 0*unit,-5*unit );

    paths[ POINT_BLOCK ] = new Path();
    paths[ POINT_BLOCK ].moveTo(  0*unit,  0*unit );
    paths[ POINT_BLOCK ].lineTo( 10*unit,  2*unit );
    paths[ POINT_BLOCK ].lineTo(  8*unit, -8*unit );
    paths[ POINT_BLOCK ].lineTo( -2*unit, -6*unit );
    paths[ POINT_BLOCK ].lineTo(  2*unit,  3*unit );
    paths[ POINT_BLOCK ].lineTo(  4*unit, -5*unit );
    paths[ POINT_BLOCK ].lineTo(  0*unit,  0*unit );

    paths[ POINT_CLAY ] = new Path();
    paths[ POINT_CLAY ].moveTo( -6*unit,  0*unit );
    paths[ POINT_CLAY ].cubicTo( -6*unit, -1*unit, -4*unit, -3*unit, -3*unit, -3*unit );
    paths[ POINT_CLAY ].cubicTo( -2*unit, -3*unit,  0*unit, -1*unit,  0*unit,  0*unit );
    paths[ POINT_CLAY ].cubicTo(  0*unit,  1*unit,  2*unit,  3*unit,  3*unit,  3*unit );
    paths[ POINT_CLAY ].cubicTo(  4*unit,  3*unit,  6*unit,  1*unit,  6*unit,  0*unit );

    paths[ POINT_CONT ] = new Path();
    paths[ POINT_CONT ].moveTo(  0*unit,  3*unit );
    paths[ POINT_CONT ].lineTo(  0*unit,  0*unit );
    paths[ POINT_CONT ].cubicTo( 0*unit, -1*unit,  1*unit, -2*unit,  2*unit, -3*unit );
    paths[ POINT_CONT ].cubicTo( 3*unit, -4*unit,  1*unit, -6*unit,  0*unit, -6*unit );
    paths[ POINT_CONT ].cubicTo(-1*unit, -6*unit, -3*unit, -3*unit, -3*unit, -4*unit );
    paths[ POINT_CONT ].moveTo(  0*unit,  7*unit );
    paths[ POINT_CONT ].lineTo(  0*unit,  5*unit );

    // paths[ POINT_CROSS ] = new Path();
    // paths[ POINT_CROSS ].moveTo( -5*unit, 5*unit );
    // paths[ POINT_CROSS ].lineTo(  5*unit,-5*unit );
    // paths[ POINT_CROSS ].moveTo( -5*unit,-5*unit );
    // paths[ POINT_CROSS ].lineTo(  5*unit, 5*unit );

    paths[ POINT_DANGER ] = new Path();
    paths[ POINT_DANGER ].moveTo(  0*unit, 0*unit );
    paths[ POINT_DANGER ].lineTo(  2*unit, -8*unit );
    paths[ POINT_DANGER ].lineTo( -2*unit, -8*unit );
    paths[ POINT_DANGER ].lineTo(  0*unit, 0*unit );
    paths[ POINT_DANGER ].addCircle(  0*unit,  4*unit, 2*unit, Path.Direction.CCW );

    paths[ POINT_DEBRIS ] = new Path();
    paths[ POINT_DEBRIS ].moveTo( 0*unit, 0*unit );
    paths[ POINT_DEBRIS ].lineTo( 4*unit, 1*unit );
    paths[ POINT_DEBRIS ].lineTo( 2*unit,-3*unit );
    paths[ POINT_DEBRIS ].lineTo( 0*unit, 0*unit );
    paths[ POINT_DEBRIS ].lineTo(-4*unit, 0*unit );
    paths[ POINT_DEBRIS ].lineTo(-2*unit,-4*unit );
    paths[ POINT_DEBRIS ].lineTo( 0*unit, 0*unit );

    paths[ POINT_END ] = new Path();
    paths[ POINT_END ].moveTo( -2*unit, -6*unit );
    paths[ POINT_END ].lineTo( -2*unit,  6*unit );
    paths[ POINT_END ].moveTo(  2*unit, -6*unit );
    paths[ POINT_END ].lineTo(  2*unit,  6*unit );

    paths[ POINT_ENTRANCE ] = new Path();
    paths[ POINT_ENTRANCE ].moveTo(  0*unit, -6*unit );
    paths[ POINT_ENTRANCE ].lineTo( -3*unit,  3*unit );
    paths[ POINT_ENTRANCE ].lineTo(  3*unit,  3*unit );
    paths[ POINT_ENTRANCE ].lineTo(  0*unit, -6*unit );

    paths[ POINT_FLOWSTONE ] = new Path();
    paths[ POINT_FLOWSTONE ].moveTo( -7*unit, -3*unit );
    paths[ POINT_FLOWSTONE ].lineTo( -2*unit, -3*unit );
    paths[ POINT_FLOWSTONE ].moveTo(  2*unit, -3*unit );
    paths[ POINT_FLOWSTONE ].lineTo(  7*unit, -3*unit );
    paths[ POINT_FLOWSTONE ].moveTo( -3*unit,  3*unit );
    paths[ POINT_FLOWSTONE ].lineTo(  3*unit,  3*unit );

    paths[ POINT_GRADIENT ] = new Path();
    paths[ POINT_GRADIENT ].moveTo(  0*unit,  8*unit );
    paths[ POINT_GRADIENT ].lineTo(  0*unit, -4*unit );
    paths[ POINT_GRADIENT ].lineTo( -2*unit, -2*unit );
    paths[ POINT_GRADIENT ].lineTo(  2*unit, -2*unit );
    paths[ POINT_GRADIENT ].lineTo(  0*unit, -4*unit );

    paths[ POINT_HELICTITE ] = new Path();
    paths[ POINT_HELICTITE ].moveTo(  0*unit,-7*unit );
    paths[ POINT_HELICTITE ].lineTo(  0*unit, 7*unit );
    paths[ POINT_HELICTITE ].moveTo( -3*unit,-5*unit );
    paths[ POINT_HELICTITE ].lineTo( -3*unit, 0*unit );
    paths[ POINT_HELICTITE ].lineTo(  3*unit, 0*unit );
    paths[ POINT_HELICTITE ].lineTo(  3*unit, 5*unit );

    paths[ POINT_ICE ] = new Path();
    paths[ POINT_ICE ].moveTo( -5*unit, 0*unit );
    paths[ POINT_ICE ].lineTo(  5*unit, 0*unit );
    paths[ POINT_ICE ].moveTo(  0*unit,-5*unit );
    paths[ POINT_ICE ].lineTo(  0*unit, 5*unit );

    paths[ POINT_LABEL ] = new Path();
    paths[ POINT_LABEL ].moveTo(  0*unit,  3*unit );
    paths[ POINT_LABEL ].lineTo(  0*unit, -6*unit );
    paths[ POINT_LABEL ].lineTo( -3*unit, -6*unit );
    paths[ POINT_LABEL ].lineTo(  3*unit, -6*unit );

    paths[ POINT_PEBBLES ] = new Path();
    paths[ POINT_PEBBLES ].addCircle( -3*unit,  1*unit, 2*unit, Path.Direction.CCW );
    paths[ POINT_PEBBLES ].addCircle(  4*unit,  3*unit, 2*unit, Path.Direction.CCW );
    paths[ POINT_PEBBLES ].addCircle(  3*unit, -3*unit, 2*unit, Path.Direction.CCW );

    paths[ POINT_PILLAR ] = new Path();
    paths[ POINT_PILLAR ].moveTo( -3*unit,-7*unit );
    paths[ POINT_PILLAR ].lineTo(  0*unit,-4*unit );
    paths[ POINT_PILLAR ].lineTo(  0*unit, 4*unit );
    paths[ POINT_PILLAR ].lineTo( -3*unit, 7*unit );
    paths[ POINT_PILLAR ].moveTo(  0*unit, 4*unit );
    paths[ POINT_PILLAR ].lineTo(  3*unit, 7*unit );
    paths[ POINT_PILLAR ].moveTo(  0*unit,-4*unit );
    paths[ POINT_PILLAR ].lineTo(  3*unit,-7*unit );

    paths[ POINT_POPCORN ] = new Path();
    paths[ POINT_POPCORN ].moveTo( -7*unit, 4*unit );
    paths[ POINT_POPCORN ].lineTo(  7*unit, 4*unit );
    paths[ POINT_POPCORN ].moveTo( -6*unit, 4*unit );
    paths[ POINT_POPCORN ].lineTo( -6*unit, -1*unit );
    paths[ POINT_POPCORN ].addCircle( -6*unit, -2*unit, 1*unit, Path.Direction.CCW );
    paths[ POINT_POPCORN ].moveTo(  0*unit, 4*unit );
    paths[ POINT_POPCORN ].lineTo(  0*unit, -1*unit );
    paths[ POINT_POPCORN ].addCircle(  0*unit, -2*unit, 1*unit, Path.Direction.CCW );
    paths[ POINT_POPCORN ].moveTo(  6*unit, 4*unit );
    paths[ POINT_POPCORN ].lineTo(  6*unit, -1*unit );
    paths[ POINT_POPCORN ].addCircle(  6*unit, -2*unit, 1*unit, Path.Direction.CCW );

    paths[ POINT_SAND ] = new Path();
    paths[ POINT_SAND ].moveTo( -7*unit,  3*unit );
    paths[ POINT_SAND ].lineTo( -5*unit,  3*unit );
    paths[ POINT_SAND ].lineTo( -6*unit,  1*unit );
    paths[ POINT_SAND ].lineTo( -7*unit,  3*unit );
    paths[ POINT_SAND ].moveTo(  7*unit,  3*unit );
    paths[ POINT_SAND ].lineTo(  5*unit,  3*unit );
    paths[ POINT_SAND ].lineTo(  6*unit,  1*unit );
    paths[ POINT_SAND ].lineTo(  7*unit,  3*unit );
    paths[ POINT_SAND ].moveTo( -1*unit, -3*unit );
    paths[ POINT_SAND ].lineTo(  1*unit, -3*unit );
    paths[ POINT_SAND ].lineTo(  0*unit, -5*unit );
    paths[ POINT_SAND ].lineTo( -1*unit, -3*unit );

    paths[ POINT_SNOW ] = new Path();
    paths[ POINT_SNOW ].moveTo(  0*unit, -5*unit );
    paths[ POINT_SNOW ].lineTo(  0*unit,  5*unit );
    paths[ POINT_SNOW ].moveTo( -3*unit, -4*unit );
    paths[ POINT_SNOW ].lineTo(  3*unit,  4*unit );
    paths[ POINT_SNOW ].moveTo( -3*unit,  4*unit );
    paths[ POINT_SNOW ].lineTo(  4*unit, -4*unit );

    paths[ POINT_STAL ] = new Path();
    paths[ POINT_STAL ].moveTo( -5*unit, -5*unit );
    paths[ POINT_STAL ].lineTo(  0*unit,  0*unit );
    paths[ POINT_STAL ].lineTo(  5*unit, -5*unit );
    paths[ POINT_STAL ].moveTo(  0*unit,  0*unit );
    paths[ POINT_STAL ].lineTo(  0*unit,  8*unit );

    paths[ POINT_WATER ] = new Path();
    paths[ POINT_WATER ].moveTo( -6*unit, -3*unit );
    paths[ POINT_WATER ].lineTo( -1*unit, -3*unit );
    paths[ POINT_WATER ].moveTo(  2*unit, -3*unit );
    paths[ POINT_WATER ].lineTo(  6*unit, -3*unit );
    paths[ POINT_WATER ].moveTo( -4*unit,  0*unit );
    paths[ POINT_WATER ].lineTo(  1*unit,  0*unit );
    paths[ POINT_WATER ].moveTo(  3*unit,  0*unit );
    paths[ POINT_WATER ].lineTo(  7*unit,  0*unit );
    paths[ POINT_WATER ].moveTo( -3*unit,  3*unit );
    paths[ POINT_WATER ].lineTo(  5*unit,  3*unit );

    paths[ POINT_WATERFLOW ] = new Path();
    paths[ POINT_WATERFLOW ].moveTo( 0*unit, 3*unit );
    paths[ POINT_WATERFLOW ].cubicTo( -1*unit, 3*unit,-1*unit,  0*unit, 0*unit,  0*unit );
    paths[ POINT_WATERFLOW ].cubicTo(  1*unit, 0*unit, 1*unit, -3*unit, 0*unit, -3*unit );
    paths[ POINT_WATERFLOW ].cubicTo( -1*unit, -3*unit, -1*unit, -6*unit, 0*unit, -6*unit );
    paths[ POINT_WATERFLOW ].cubicTo(  1*unit, -6*unit, 1.5f*unit, -6.5f*unit, 1.5f*unit, -7.5f*unit );
    paths[ POINT_WATERFLOW ].cubicTo(  1.5f*unit, -8.5f*unit, 0*unit, -9*unit, 0*unit, -10*unit );
    paths[ POINT_WATERFLOW ].lineTo(  4*unit, -6*unit );
    paths[ POINT_WATERFLOW ].moveTo(  0*unit, -10*unit );
    paths[ POINT_WATERFLOW ].lineTo( -4*unit, -6*unit );

    int k;
    for ( k=0; k<POINT_MAX; ++k ) {
      pointPaint[k] = new Paint();
      pointPaint[k].setDither(true);
      pointPaint[k].setColor( pointColor[k] );
      pointPaint[k].setStyle(Paint.Style.STROKE);
      pointPaint[k].setStrokeJoin(Paint.Join.ROUND);
      pointPaint[k].setStrokeCap(Paint.Cap.ROUND);
      pointPaint[k].setStrokeWidth( STROKE_WIDTH_CURRENT );
    }
    for ( k=0; k< LINE_MAX; ++k ) {
      linePaint[k] = new Paint();
      linePaint[k].setDither(true);
      linePaint[k].setColor( lineColor[k] );
      linePaint[k].setStyle(Paint.Style.STROKE);
      linePaint[k].setStrokeJoin(Paint.Join.ROUND);
      linePaint[k].setStrokeCap(Paint.Cap.ROUND);
      linePaint[k].setStrokeWidth( (k == LINE_WALL)? 2 *STROKE_WIDTH_CURRENT 
                                                     : STROKE_WIDTH_CURRENT );
      if ( k == LINE_CHIMNEY || k == LINE_OVERHANG ) {
        linePaint[k].setPathEffect(new DashPathEffect(new float[] {15,5}, 0));
      }
    }
    for ( k=0; k< AREA_MAX; ++k ) {
      areaPaint[k] = new Paint();
      areaPaint[k].setDither(true);
      areaPaint[k].setColor( areaColor[k] );
      areaPaint[k].setStyle(Paint.Style.STROKE);
      areaPaint[k].setStrokeJoin(Paint.Join.ROUND);
      areaPaint[k].setStrokeCap(Paint.Cap.ROUND);
      areaPaint[k].setStrokeWidth( STROKE_WIDTH_CURRENT );
    }

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

  }

}
