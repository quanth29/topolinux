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
 * 20140422 iso
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.io.File;

import android.graphics.Paint;
import android.graphics.Path;
import android.content.res.Resources;

// import android.util.Log;

class SymbolPointLibrary
{
  ArrayList< SymbolPoint > mPoint;    // enabled points
  ArrayList< SymbolPoint > mAnyPoint; // all points
  int mPointLabelIndex;
  int mPointDangerIndex;
  int mPointNr;
  int mAnyPointNr;


  SymbolPointLibrary( Resources res )
  {
    // Log.v(  TopoDroidApp.TAG, "cstr SymbolPointLibrary()" );
    mPoint = new ArrayList< SymbolPoint >();
    mAnyPoint = new ArrayList< SymbolPoint >();
    mPointLabelIndex  = -1;
    mPointDangerIndex = -1;
    loadSystemPoints( res );
    loadUserPoints();
    makeEnabledList();
  }

  int size() { return mPoint.size(); }

  boolean pointHasText( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return false;
    return mPoint.get(k).mHasText;
  }

  boolean hasPoint( String th_name )
  {
    for ( SymbolPoint p : mPoint ) {
      if ( p.hasThName( th_name ) ) {
        return true;
      }
    }
    return false;
  }

  boolean hasAnyPoint( String th_name )
  {
    for ( SymbolPoint p : mAnyPoint ) {
      if ( p.hasThName( th_name ) ) {
        return true;
      }
    }
    return false;
  }

  // boolean removePoint( String th_name ) 
  // {
  //   for ( SymbolPoint p : mPoint ) {
  //     if ( p.hasThName( th_name ) ) {
  //       mPoint.remove( p );
  //       TopoDroidApp.mData.setSymbolEnabled( "p_" + th_name, false );
  //       return true;
  //     }
  //   }
  //   return false;
  // }

  SymbolPoint getPoint( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get( k );
  }

  SymbolPoint getAnyPoint( int k ) 
  {
    if ( k < 0 || k >= mAnyPointNr ) return null;
    return mAnyPoint.get( k );
  }

  String getPointName( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getName( );
  }


  String getPointThName( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getThName( );
  }

  Paint getPointPaint( int k ) 
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getPaint( );
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
    // Log.v(  TopoDroidApp.TAG, "SymbolPointLibrary::resetOrientations()" );
    for ( int k=0; k < mPointNr; ++k ) mPoint.get(k).resetOrientation();
  }

  void rotateGrad( int k, double a )
  {
    if ( k < 0 || k >= mPointNr ) return;
    mPoint.get(k).rotateGrad( a );
  }

  Path getPointPath( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getPath( );
  }

  Path getPointOrigPath( int k )
  {
    if ( k < 0 || k >= mPointNr ) return null;
    return mPoint.get(k).getOrigPath( );
  }

  int pointCsxLayer( int k )
  {
    if ( k < 0 || k >= mPointNr ) return -1;
    return mPoint.get(k).mCsxLayer;
  }

  int pointCsxType( int k )
  {
    if ( k < 0 || k >= mPointNr ) return -1;
    return mPoint.get(k).mCsxType;
  }

  int pointCsxCategory( int k )
  {
    if ( k < 0 || k >= mPointNr ) return -1;
    return mPoint.get(k).mCsxCategory;
  }

  String pointCsx( int k )
  {
    if ( k < 0 || k >= mPointNr ) return "";
    return mPoint.get(k).mCsx;
  }

  // ========================================================================

  final String p_label = "moveTo 0 3 lineTo 0 -6 lineTo -3 -6 lineTo 3 -6";

  private void loadSystemPoints( Resources res )
  {
    SymbolPoint symbol;
    // Log.v(  TopoDroidApp.TAG, "SymbolPointLibrary::loadSystemPoints()" );
    mPointLabelIndex = mPoint.size();
    symbol = new SymbolPoint( res.getString(R.string.thp_label), "label", 0xffffffff, p_label, false, true );
    symbol.mCsxLayer = 6;
    symbol.mCsxType  = 8;
    symbol.mCsxCategory = 81;
    mAnyPoint.add( symbol );

    mAnyPointNr = mAnyPoint.size();
  }

  void loadUserPoints()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";
    // Charset.forName("ISO-8859-1")

    File dir = new File( TopoDroidApp.APP_POINT_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolPoint symbol = new SymbolPoint( file.getPath(), locale, iso );
        if ( ! hasAnyPoint( symbol.getThName() ) ) {
          mAnyPoint.add( symbol );
          symbol.setEnabled( TopoDroidApp.mData.isSymbolEnabled( "p_" + symbol.getThName() ) );
        }
      }
      mAnyPointNr = mAnyPoint.size();
    } else {
      dir.mkdirs( );
    }
    // Log.v(  TopoDroidApp.TAG, "SymbolPointLibrary::loadUserPoints() size " + mPointNr + " " + mAnyPointNr );
  }

  void makeEnabledList()
  {
    mPoint.clear();
    for ( SymbolPoint symbol : mAnyPoint ) {
      if ( symbol.mEnabled ) {
        if ( symbol.getThName().equals("label") ) mPointLabelIndex = mPoint.size();
        if ( symbol.getThName().equals("danger" ) ) mPointDangerIndex = mPoint.size();
        mPoint.add( symbol );
      }
    }
    mPointNr = mPoint.size();
  }
      
}    
