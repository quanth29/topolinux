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
  // static final String TAG = "DistoX";

  ArrayList< SymbolArea > mArea;
  int mAreaNr;

  SymbolAreaLibrary( Resources res )
  {
    // Log.v( TAG, "cstr SymbolAreaLibrary()" );
    mArea = new ArrayList< SymbolArea >();
    loadSystemAreas( res );
    loadUserAreas();
  }

  // int size() { return mArea.size(); }

  SymbolArea getArea( int k ) 
  {
    if ( k < 0 || k >= mAreaNr ) return null;
    return mArea.get( k );
  }

  SymbolArea getArea( String th_name ) 
  {
    for ( SymbolArea a : mArea ) {
      if ( th_name.equals( a.mThName ) ) return a;
    }
    return null;
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
  
  // ========================================================================

  private void loadSystemAreas( Resources res )
  {
    mArea.add( new SymbolArea( res.getString( R.string.tha_water ),  "water",  0x660000ff ) );

    mAreaNr = mArea.size();
  }

  private void loadUserAreas()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);

    File dir = new File( TopoDroidApp.APP_AREA_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolArea symbol = new SymbolArea( file.getPath(), locale );
        if ( getArea( symbol.mThName ) == null ) {
          mArea.add( symbol );
        }
      }
      mAreaNr = mArea.size();
    } else {
      dir.mkdirs( );
    }
  }
      
}    
