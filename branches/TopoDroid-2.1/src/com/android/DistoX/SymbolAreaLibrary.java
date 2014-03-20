/** @file SymbolAreaLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol library
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121211 locale
 * 20131119 area color getter
 */
package com.android.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.io.File;

import android.graphics.Paint;
import android.content.res.Resources;

// import android.util.Log;

class SymbolAreaLibrary
{
  ArrayList< SymbolArea > mArea;
  ArrayList< SymbolArea > mAnyArea;
  int mAreaNr;
  int mAnyAreaNr;

  SymbolAreaLibrary( Resources res )
  {
    // Log.v(  TopoDroidApp.TAG, "cstr SymbolAreaLibrary()" );
    mArea = new ArrayList< SymbolArea >();
    mAnyArea = new ArrayList< SymbolArea >();
    loadSystemAreas( res );
    loadUserAreas();
    makeEnabledList();
  }

  // int size() { return mArea.size(); }

  SymbolArea getArea( int k ) 
  {
    if ( k < 0 || k >= mAreaNr ) return null;
    return mArea.get( k );
  }

  SymbolArea getAnyArea( int k ) 
  {
    if ( k < 0 || k >= mAnyAreaNr ) return null;
    return mAnyArea.get( k );
  }

  boolean hasArea( String th_name ) 
  {
    for ( SymbolArea a : mArea ) {
      if ( th_name.equals( a.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  boolean hasAnyArea( String th_name ) 
  {
    for ( SymbolArea a : mAnyArea ) {
      if ( th_name.equals( a.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  boolean removeArea( String th_name ) 
  {
    for ( SymbolArea a : mArea ) {
      if ( th_name.equals( a.mThName ) ) {
        mArea.remove( a );
        a.setEnabled( false );
        TopoDroidApp.mData.setSymbolEnabled( "a_" + th_name, false );
        return true;
      }
    }
    return false;
  }

  String getAreaName( int k )
  {
    if ( k < 0 || k >= mAreaNr ) return null;
    return mArea.get(k).mName;
  }

  String getAreaThName( int k )
  {
    if ( k < 0 || k >= mAreaNr ) return null;
    return mArea.get(k).mThName;
  }

  Paint getAreaPaint( int k )
  {
    if ( k < 0 || k >= mAreaNr ) return null;
    return mArea.get(k).mPaint;
  }

  int getAreaColor( int k )
  {
    if ( k < 0 || k >= mAreaNr ) return 0xffffffff; // white
    return mArea.get(k).mColor;
  }
  
  // ========================================================================

  private void loadSystemAreas( Resources res )
  {
    // Log.v( TopoDroidApp.TAG, "load system areas");
    SymbolArea symbol = new SymbolArea( res.getString( R.string.tha_water ),  "water",  0x660000ff );
    mAnyArea.add( symbol );
    mAnyAreaNr = mAnyArea.size();
  }

  void loadUserAreas()
  {
    // Log.v( TopoDroidApp.TAG, "load user areas");
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);

    File dir = new File( TopoDroidApp.APP_AREA_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolArea symbol = new SymbolArea( file.getPath(), locale );
        if ( ! hasAnyArea( symbol.mThName ) ) {
          mAnyArea.add( symbol );
          symbol.setEnabled( TopoDroidApp.mData.isSymbolEnabled( "a_" + symbol.mThName ) );
        }
      }
      mAnyAreaNr = mAnyArea.size();
    } else {
      dir.mkdirs( );
    }
  }
      

  void makeEnabledList()
  {
    mArea.clear();
    // Log.v( TopoDroidApp.TAG, "make enabled list : " + mAnyArea.size() );
    for ( SymbolArea symbol : mAnyArea ) {
      TopoDroidApp.mData.setSymbolEnabled( "a_" + symbol.mThName, symbol.mEnabled );
      // Log.v( TopoDroidApp.TAG, "area symbol " + symbol.mThName + " enabled " + symbol.mEnabled );
      if ( symbol.mEnabled ) {
        mArea.add( symbol );
      }
    }
    mAreaNr = mArea.size();
  }

}    
