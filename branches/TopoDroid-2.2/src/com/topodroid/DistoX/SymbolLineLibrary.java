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
 * 20140422 iso
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.ArrayList;
import java.io.File;

import android.graphics.Paint;
import android.content.res.Resources;

import android.util.Log;

class SymbolLineLibrary
{
  ArrayList< SymbolLine > mLine;
  ArrayList< SymbolLine > mAnyLine;
  int mLineWallIndex;
  int mLineSlopeIndex;
  int mLineSectionIndex;
  int mLineNr;
  int mAnyLineNr;

  SymbolLineLibrary( Resources res )
  {
    // Log.v( "TopoDroid", "cstr SymbolLineLibrary()" );
    mLine = new ArrayList< SymbolLine >();
    mAnyLine = new ArrayList< SymbolLine >();
    mLineWallIndex    = -1;
    mLineSlopeIndex   = -1;
    mLineSectionIndex = -1;
    loadSystemLines( res );
    loadUserLines();
    makeEnabledList();
  }

  // int size() { return mLine.size(); }

  SymbolLine getLine( int k ) 
  {
    if ( k < 0 || k >= mLineNr ) return null;
    return mLine.get( k );
  }

  SymbolLine getAnyLine( int k ) 
  {
    if ( k < 0 || k >= mAnyLineNr ) return null;
    return mAnyLine.get( k );
  }

  boolean hasLine( String th_name ) 
  {
    for ( SymbolLine l : mLine ) {
      if ( th_name.equals( l.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  boolean hasAnyLine( String th_name ) 
  {
    for ( SymbolLine l : mAnyLine ) {
      if ( th_name.equals( l.mThName ) ) {
        return true;
      }
    }
    return false;
  }

  boolean removeLine( String th_name ) 
  {
    for ( SymbolLine l : mLine ) {
      if ( th_name.equals( l.mThName ) ) {
        mLine.remove( l );
        TopoDroidApp.mData.setSymbolEnabled( "l_" + th_name, false );
        return true;
      }
    }
    return false;
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
    if ( mAnyLine.size() > 0 ) return;
    SymbolLine symbol = new SymbolLine( res.getString( R.string.thl_wall ),  "wall",  0xffff0000, 2 );
    mAnyLine.add( symbol );
    mAnyLineNr = mAnyLine.size();
  }

  void loadUserLines()
  {
    String locale = "name-" + Locale.getDefault().toString().substring(0,2);
    String iso = "ISO-8859-1";
    // String iso = "UTF-8";
    // if ( locale.equals( "name-es" ) ) iso = "ISO-8859-1";

    File dir = new File( TopoDroidApp.APP_LINE_PATH );
    if ( dir.exists() ) {
      File[] files = dir.listFiles();
      for ( File file : files ) {
        SymbolLine symbol = new SymbolLine( file.getPath(), locale, iso );
        if ( ! hasAnyLine( symbol.mThName ) ) {
          mAnyLine.add( symbol );
          symbol.setEnabled( TopoDroidApp.mData.isSymbolEnabled( "l_" + symbol.mThName ) );
        }
      }
      mAnyLineNr = mAnyLine.size();
    } else {
      dir.mkdirs( );
    }
  }
      
  void makeEnabledList()
  {
    mLine.clear();
    mLineWallIndex    = -1;
    mLineSlopeIndex   = -1;
    mLineSectionIndex = -1;
    for ( SymbolLine symbol : mAnyLine ) {
      TopoDroidApp.mData.setSymbolEnabled( "l_" + symbol.mThName, symbol.mEnabled );
      if ( symbol.mEnabled ) {
        if ( symbol.mThName.equals("wall") )  mLineWallIndex = mLine.size();
        if ( symbol.mThName.equals("slope") ) mLineSlopeIndex = mLine.size();
        if ( symbol.mThName.equals("section") ) mLineSectionIndex = mLine.size();
        mLine.add( symbol );
      }
    }
    mLineNr = mLine.size();
    // Log.v( TopoDroidApp.TAG, "lines " + mLineNr + " wall " + mLineWallIndex + " slope " + mLineSlopeIndex + " section " + mLineSectionIndex );
  }
}    
