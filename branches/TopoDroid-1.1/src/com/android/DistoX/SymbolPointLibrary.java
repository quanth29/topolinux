/** @file SymbolPointLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol library
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121211 locale
 * 20121215 avoid double th-name symbol
 */
package com.android.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.io.File;

import android.graphics.Paint;
import android.graphics.Path;
import android.content.res.Resources;

// import android.util.Log;

class SymbolPointLibrary
{
  // static final String TAG = "DistoX";

  ArrayList< SymbolPoint > mPoint;
  int mPointLabelIndex;
  int mPointDangerIndex;
  int mPointNr;


  SymbolPointLibrary( Resources res )
  {
    // Log.v( TAG, "cstr SymbolPointLibrary()" );
    mPoint = new ArrayList< SymbolPoint >();
    mPointLabelIndex  = -1;
    mPointDangerIndex = -1;
    loadSystemPoints( res );
    loadUserPoints();
  }

  // int size() { return mPoint.size(); }

  boolean pointHasText( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return false;
    return mPoint.get(k).mHasText;
  }

  SymbolPoint getPoint( String th_name ) 
  {
    for ( SymbolPoint p : mPoint ) {
      if ( p.hasThName( th_name ) ) return p;
    }
    return null;
  }

  SymbolPoint getPoint( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get( k );
  }

  String getPointName( int k, boolean flip ) 
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getName( flip );
  }

  String getPointName( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getName( );
  }


  String getPointThName( int k, boolean flip )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getThName( flip );
  }

  Paint getPointPaint( int k, boolean flip ) 
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getPaint( flip );
  }
  
  boolean canRotate( int k )
  {
    if ( k < 0 || k >= mPointNr ) return false;
    return mPoint.get(k).mOrientable;
  }

  double getPointOrientation( int k )
  {
    if ( k < 0 || k >= mPointNr ) return 0.0;
    return mPoint.get(k).mOrientation;
  }

  void resetOrientations()
  {
    // Log.v( TAG, "SymbolPointLibrary::resetOrientations()" );
    for ( int k=0; k < mPointNr; ++k ) mPoint.get(k).resetOrientation();
  }

  void rotateGrad( int k, double a )
  {
    if ( k < 0 || k >= mPointNr ) return;
    SymbolPoint pt = mPoint.get(k);
    if ( pt.canFlip() ) {
      if ( Math.abs( a ) > 90 ) {
        pt.mOrientation += 180.0; 
        if ( pt.mOrientation > 270 ) pt.mOrientation = 0;
      }
    } else {
      pt.rotateGrad( a );
    }
  }

  void setPointFlip( int k, boolean flip )
  {
    if ( k < 0 || k >= mPointNr ) return;
    SymbolPoint pt = mPoint.get(k);
    if ( pt.canFlip() ) {
      pt.mOrientation = flip ? 180 : 0;
    }
  }

  boolean canFlip( int k )
  {
    if ( k < 0 || k >= mPointNr ) return false;
    return mPoint.get(k).canFlip();
  }

  boolean getPointFlip( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return false;
    return mPoint.get(k).getFlip();
  }

  Path getPointPath( int k, boolean flip )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getPath( flip );
  }

  Path getPointPath( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getPath( );
  }


  // ========================================================================

  final String p_label = "moveTo 0 3 lineTo 0 -6 lineTo -3 -6 lineTo 3 -6";

  // final String p_air_draught = "moveTo -4 7 lineTo 0 3 lineTo 0 -10 lineTo 4 -6 moveTo 0 -10 lineTo -4 -6 moveTo -4 4 lineTo 0 0";
  // final String p_anchor = "moveTo 0 5 lineTo 0 -5 addCircle 0 0 5";
  // final String p_blocks = "moveTo 0 0 lineTo 10 2 lineTo 8 -8 lineTo -2 -6 lineTo 2 3 lineTo 4 -5 lineTo 0 0";
  // final String p_clay = "moveTo -6 0 cubicTo -6 -1 -4 -3 -3 -3 cubicTo -2 -3 0 -1 0 0 cubicTo 0 1 2 3 3 3 cubicTo 4 3 6 1 6 0";
  // final String p_choke = "moveTo -4 4 lineTo 4 -4 moveTo -4 -4 lineTo 4 4";
  // final String p_continuation = "moveTo 0 3 lineTo 0 0 cubicTo 0 -1 1 -2 2 -3 cubicTo 3 -4 1 -6 0 -6 cubicTo -1 -6 -3 -3 -3 -4 moveTo 0 7 lineTo 0 5";
  // final String p_crystal = "moveTo -5 0 lineTo 5 0 moveTo 0 -5 lineTo 0 5 lineTo 5 0 lineTo 0 -5 lineTo -5 0 lineTo 0 5"; 
  // final String p_danger = "moveTo 0 0 lineTo 2 -8 lineTo -2 -8 lineTo 0 0 addCircle 0 4 2";
  // final String p_debris = "moveTo 0 0 lineTo 4 1 lineTo 2 -3 lineTo 0 0 lineTo -4 0 lineTo -2 -4 lineTo 0 0";
  // final String p_dig = "moveTo -5 -2 lineTo -1 -6 moveTo -3 -4 lineTo 4 3 moveTo 5 -2 lineTo 1 -6 moveTo 3 -4 lineTo -4 3";
  // final String p_entrance = "moveTo 0 -6 lineTo -3 3 lineTo 3 3 lineTo 0 -6";
  // final String p_flowstone = "moveTo -7 -3 lineTo -2 -3 moveTo 2 -3 lineTo 7 -3 moveTo -3 3 lineTo 3 3";
  // final String p_gradient = "moveTo 0 8 lineTo 0 -4 lineTo -2 -2 lineTo 2 -2 lineTo 0 -4";
  // final String p_guano = "moveTo -5 0 cubicTo -4 -2 -1 -2 0 0 lineTo 0 6 moveTo 5 0 cubicTo 4 -2 1 -2 0 0";
  // final String p_gypsum = "moveTo -5 0 lineTo 0 -5 lineTo 0 5 lineTo 5 0 lineTo -5 0";
  // final String p_helictite = "moveTo 0 -7 lineTo 0 7 moveTo -3 -5 lineTo -3 0 lineTo 3 0 lineTo 3 5";
  // final String p_ice = "moveTo -5 0 lineTo 5 0 moveTo 0 -5 lineTo 0 5";
  // final String p_low_end = "moveTo -6 -2 lineTo 6 -2 moveTo -6 2 lineTo 6 2";
  // final String p_moonmilk = "moveTo -8 1 cubicTo -7 -1 -4 -5 -3 -2 cubicTo -2 -5 2 -5 3 -2 cubicTo 4 -5 7 -1 8 1";
  // final String p_narrow_end = "moveTo -2 -6 lineTo -2 6 moveTo 2 -6 lineTo 2 6";
  // final String p_pebbles = "addCircle -3 1 2 addCircle 4 3 2 addCircle 3 -3 2";
  // final String p_pillar = "moveTo -3 -7 lineTo 0 -4 lineTo 0 4 lineTo -3 7 moveTo 0 4 lineTo 3 7 moveTo 0 -4 lineTo 3 -7";
  // final String p_popcorn = "moveTo -7 4 lineTo 7 4 moveTo -6 4 lineTo -6 -1 addCircle -6 -2 1 moveTo 0 4 lineTo 0 -1 addCircle 0 -2 1 moveTo 6 4 lineTo 6 -1 addCircle 6 -2 1";
  // final String p_sand = "moveTo -7 3 lineTo -5 3 lineTo -6 1 lineTo -7 3 moveTo 7 3 lineTo 5 3 lineTo 6 1 lineTo 7 3 moveTo -1 -3 lineTo -1 -3 lineTo 0 -5 lineTo -1 -3";
  // final String p_sink = "moveTo -5 -3 cubicTo -5 -1 -2 3 0 3 cubicTo 2 3 5 -1 5 -3";
  // final String p_snow = "moveTo 0 -5 lineTo 0 5 moveTo -4 -3 lineTo 4 3 moveTo -4 3 lineTo 4 -3";
  // final String p_spring = "moveTo -5 3 cubicTo -5 1 -2 -3 0 -3 cubicTo 2 -3 5 1 5 3";
  // final String p_stalactite = "moveTo -5 -5 lineTo 0 0 lineTo 5 -5 moveTo 0 0 lineTo 0 8";
  // final String p_stalagmite = "moveTo -5 5 lineTo 0 0 lineTo 5 5 moveTo 0 0 lineTo 0 -8";
  // final String p_sodastraw = "moveTo -6 -2 lineTo 6 -2 moveTo -4 -2 lineTo -4 2 moveTo 0 2 lineTo 0 6 moveTo 4 -2 lineTo 4 4";
  // final String p_water = "moveTo -6 -3 lineTo -1 -3 moveTo 2 -3 lineTo 6 -3 moveTo -4 0 lineTo 1 0 moveTo 3 0 lineTo 7 0 moveTo -3 3 lineTo 5 3";
  // final String p_waterflow = "moveTo 0 3 cubicTo -1 3 -1 0 0 0 cubicTo 1 0 1 -3 0 -3 cubicTo -1 -3 -1 -6 0 -6 cubicTo 1 -6 1.5 -6.5 1.5 -7.5 cubicTo 1.5 -8.5 0 -9 0 -10 lineTo 4 -6 moveTo 0 -10 lineTo -4 -6";


  private void loadSystemPoints( Resources res )
  {
    // Log.v( TAG, "SymbolPointLibrary::loadSystemPoints()" );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_air_draught), "air-draught", 0xff6996ff, p_air_draught, true ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_water_flow),  "water-flow",  0xff0066ff, p_waterflow, true ) );
    mPointLabelIndex = mPoint.size();
    mPoint.add( new SymbolPoint( res.getString(R.string.thp_label),       "label",       0xffffffff, p_label, false, true ) );
    // mPointDangerIndex = mPoint.size();
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_danger), "label -text \"!\"",0xffff0000, p_danger, false ) );


    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_water),       "water",       0xff0000ff, p_water, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_sink),       "sink",        0xff0000ff, p_sink,
    //                              res.getString(R.string.thp_spring),      "spring",      0xff0000ff, p_spring ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_anchor),      "anchor",      0xffffffff, p_anchor, false ) );

    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_entrance),    "entrance",    0xffccffcc, p_entrance, true ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_gradient),    "gradient",    0xff33ff66, p_gradient, true ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_continuation),"continuation",0xff99ff99, p_continuation, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_narrow-end),  "narrow-end",  0xff33cc33, p_narrow_end, 
    //                              res.getString(R.string.thp_low-end),     "low-end",     0xff33cc33, p_low_end ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_dig),         "dig",         0xffff33cc, p_dig, 
    //                              res.getString(R.string.thp_choke),  "breakdown-choke",  0xff33cc33, p_choke ) );

    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_blocks),      "blocks",      0xffffcc66, p_blocks, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_debris),      "debris",      0xff33cc33, p_debris, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_pebbles),     "pebbles",     0xffffcc66, p_pebbles, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_clay),        "clay",        0xffffcc66, p_clay, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_sand),        "sand",        0xffffcc66, p_sand, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_ice),         "ice",         0xffffffff, p_ice, 
    //                              res.getString(R.string.thp_snow),        "snow",        0xffffffff, p_snow ) );

    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_stalactite),  "stalactite",  0xffccff66, p_stalactite,
    //                              res.getString(R.string.thp_stalagmite),  "stalagmite",  0xffccff66, p_stalagmite ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_crystal),     "crystal",     0xffcccccc, p_crystal, 
    //                              res.getString(R.string.thp_gypsum),      "gypsum",      0xffffffff, p_gypsum ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_flowstone),   "flowstone",   0xffccff66, p_flowstone, 
    //                              res.getString(R.string.thp_moonmilk),    "moonmilk",    0xffffffcc, p_moonmilk ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_helictite),   "helictite",   0xffccff66, p_helictite, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_pillar),      "pillar",      0xffccff66, p_pillar, false ) );
    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_popcorn),     "popcorn",     0xffccff66, p_popcorn, 
    //                              res.getString(R.string.thp_soda-straw),  "soda-straw",  0xffccff66, p_sodastraw ) );

    // mPoint.add( new SymbolPoint( res.getString(R.string.thp_guano),       "guano",       0xffcc6633, p_guano, false ) );

    mPointNr = mPoint.size();
  }

  private void loadUserPoints()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);

    File dir = new File( TopoDroidApp.APP_POINT_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolPoint symbol = new SymbolPoint( file.getPath(), locale );
        if ( getPoint( symbol.getThName() ) == null ) { // NOTE avoid points with same therion_name ?
          // if ( symbol.getThName().equals("label") ) mPointLabelIndex = mPoint.size();
          if ( symbol.getThName().equals("danger" ) ) mPointDangerIndex = mPoint.size();
          mPoint.add( symbol );
        }
      }
      mPointNr = mPoint.size();
    } else {
      dir.mkdirs( );
    }
    // Log.v( TAG, "SymbolPointLibrary::loadUserPoints() size " + mPointNr );
  }
      
}    
