/* @file MissingSymbols.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid missing drawing symbols collections
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130219 created (extracted from DrawingSurface)
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.content.res.Resources;

import android.util.Log;

/**
 */
public class MissingSymbols 
{
  TreeSet< String > mMissingPoint;
  TreeSet< String > mMissingLine;
  TreeSet< String > mMissingArea;

  public MissingSymbols( )
  {
    mMissingPoint = new TreeSet< String >();
    mMissingLine  = new TreeSet< String >();
    mMissingArea  = new TreeSet< String >();
  }

  void resetSymbolLists()
  {
    mMissingPoint.clear();
    mMissingLine.clear();
    mMissingArea.clear();
  }

  void addPoint( String type )
  {
    mMissingPoint.add( type );
  }

  void addLine( String type )
  {
    mMissingLine.add( type );
  }

  void addArea( String type )
  {
    mMissingArea.add( type );
  }


  boolean isOK() 
  {
    return mMissingPoint.size() == 0 && mMissingLine.size() == 0 && mMissingArea.size() == 0;
  }


  public String getMessage( Resources res )
  {
    String prev = "";
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( "%s\n",  res.getString( R.string.missing_warning ) );
    if ( mMissingPoint.size() > 0 ) {
      pw.format( "%s:", res.getString( R.string.missing_point ) );
      for ( String p : mMissingPoint ) {
        if ( ! p.equals(prev) ) pw.format( " %s", p );
      }
      pw.format( "\n");
    }
    if ( mMissingLine.size() > 0 ) {
      pw.format( "%s:", res.getString( R.string.missing_line ) );
      prev = "";
      for ( String p : mMissingLine ) {
        if ( ! p.equals(prev) ) pw.format( " %s", p );
      }
      pw.format( "\n");
    }
    if ( mMissingArea.size() > 0 ) {
      pw.format( "%s:", res.getString( R.string.missing_area ) );
      prev = "";
      for ( String p : mMissingArea ) {
        if ( ! p.equals(prev) ) pw.format( " %s", p );
      }
      pw.format( "\n");
    }
    pw.format( "%s\n",  res.getString( R.string.missing_hint ) );
    return sw.getBuffer().toString();
  }
}
