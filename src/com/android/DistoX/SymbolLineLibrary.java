/** @file SymbolLineLibrary.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol library
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

class SymbolLineLibrary
{
  // static final String TAG = "DistoX";

  ArrayList< SymbolLine > mLine;
  int mLineWallIndex;
  int mLineSlopeIndex;
  int mLineNr;

  SymbolLineLibrary( Resources res )
  {
    // Log.v( TAG, "cstr SymbolLineLibrary()" );
    mLine = new ArrayList< SymbolLine >();
    mLineWallIndex = -1;
    mLineSlopeIndex = -1;
    loadSystemLines( res );
    loadUserLines();
  }

  // int size() { return mLine.size(); }

  SymbolLine getLine( int k ) 
  {
    if ( k < 0 || k >= mLineNr ) return null;
    return mLine.get( k );
  }

  SymbolLine getLine( String th_name ) 
  {
    for ( SymbolLine l : mLine ) {
      if ( th_name.equals( l.mThName ) ) return l;
    }
    return null;
  }

  String getLineName( int k )
  {
    if ( k < 0 || k >= mLineNr ) return null;
    return mLine.get(k).mName;
  }

  String getLineThName( int k )
  {
    if ( k < 0 || k >= mLineNr ) return null;
    return mLine.get(k).mThName;
  }

  Paint getLinePaint( int k, boolean reversed )
  {
    if ( k < 0 || k >= mLineNr ) return null;
    return reversed ? mLine.get(k).mRevPaint : mLine.get(k).mPaint;
  }
  
  // ========================================================================

  private void loadSystemLines( Resources res )
  {
    mLineWallIndex = mLine.size();
    mLine.add( new SymbolLine( res.getString( R.string.thl_wall ),  "wall",  0xffff0000, 2 ) );

    mLineNr = mLine.size();
  }

  private void loadUserLines()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);

    File dir = new File( TopoDroidApp.APP_LINE_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolLine symbol = new SymbolLine( file.getPath(), locale );
        if ( getLine( symbol.mThName ) == null ) {
          // if ( symbol.mThName.equals("wall") )  mLineWallIndex = mLine.size();
          if ( symbol.mThName.equals("slope") ) mLineSlopeIndex = mLine.size();
          mLine.add( symbol );
        }
      }
      mLineNr = mLine.size();
    } else {
      dir.mkdirs( );
    }
  }
      
}    
