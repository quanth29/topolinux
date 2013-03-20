/** @file SymbolArea.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121211 locale
 */
package com.android.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Paint;
// import android.graphics.Path;

class SymbolArea
{
  String mName;
  String mThName;
  Paint mPaint;

  /** 
   * color 0xaarrggbb
   */
  SymbolArea( String name, String th_name, int color )
  {
    mName = name;
    mThName = th_name;
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( color );
    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( 1 );
  }

  SymbolArea( String filepath, String locale )
  {
    readFile( filepath, locale );
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol area
   *      name NAME
   *      th_name THERION_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      endsymbol
   */
  void readFile( String filename, String locale )
  {
    // Log.v( TAG, "SymbolPoint::readFile " + filename );
  
    String name    = null;
    String th_name = null;
    int color      = 0;
    int alpha      = 0x66;
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line;
      line = br.readLine();
      while ( line != null ) {
        line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
  	  if ( vals[k].equals("symbol") ) {
  	    name    = null;
  	    th_name = null;
  	    color   = 0x00000000;
  	  } else if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      name = vals[k];
  	    }
  	  } else if ( vals[k].equals("th_name") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      th_name = vals[k];
  	    }
  	  } else if ( vals[k].equals("color") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      color = Integer.decode( vals[k] );
            }
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      alpha = Integer.decode( vals[k] );
  	    }
  	  } else if ( vals[k].equals("endsymbol") ) {
  	    if ( name == null ) {
  	    } else if ( th_name == null ) {
  	    } else {
              mName   = name;
              mThName = th_name;
              mPaint  = new Paint();
              mPaint.setDither(true);
              mPaint.setColor( color );
              mPaint.setAlpha( alpha );
              mPaint.setStyle(Paint.Style.STROKE);
              mPaint.setStrokeJoin(Paint.Join.ROUND);
              mPaint.setStrokeCap(Paint.Cap.ROUND);
              mPaint.setStrokeWidth( 1 );
  	    }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch( IOException e ) {
      // FIXME
    }
  }
}
