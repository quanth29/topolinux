/** @file SymbolPoint.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol
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
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.Log;

class SymbolPoint 
{
  // static final String TAG = "DistoX";

  boolean mHasText;
  boolean mOrientable;
  double mOrientation;      // orientation [degrees]
  SymbolPointBasic mPoint1; // basic point
  SymbolPointBasic mPoint2; // overloading point

  // boolean hasText() { return mHasText; }
  // boolean canRotate() { return mOrientable; }
  boolean canFlip() { return mPoint2 != null; }
  // double orientation() { return mOrientation; }

  String getName( boolean flip ) 
  {
    if ( mPoint2 == null ) return mPoint1.mName;
    return flip? mPoint2.mName : mPoint1.mName; 
  }

  String getName( ) 
  {
    if ( mPoint2 == null ) return mPoint1.mName;
    return ( mOrientation > 90 )? mPoint2.mName : mPoint1.mName; 
  }

  String getDxf( boolean flip )
  {
    if ( mPoint2 == null ) return mPoint1.mDxf;
    return ( flip )? mPoint2.mDxf : mPoint1.mDxf; 
  }

  String getDxf( )
  {
    if ( mPoint2 == null ) return mPoint1.mDxf;
    return ( mOrientation > 90 )? mPoint2.mDxf : mPoint1.mDxf; 
  }

  String getThName( boolean flip ) 
  { 
    if ( mPoint2 == null ) return mPoint1.mThName;
    return flip? mPoint2.mThName : mPoint1.mThName; 
  }

  boolean hasThName( String th_name ) 
  {
    return ( th_name.equals( mPoint1.mThName ) ) 
           || ( mPoint2 != null && th_name.equals( mPoint2.mThName ) );
  } 

  String getThName( ) 
  { 
    if ( mPoint2 == null ) return mPoint1.mThName;
    return ( mOrientation > 90 )? mPoint2.mThName : mPoint1.mThName; 
  }

  Path getPath( boolean flip )
  { 
    if ( mPoint2 == null ) return mPoint1.mPath;
    return flip? mPoint2.mPath : mPoint1.mPath;
  }

  Path getPath( )
  { 
    if ( mPoint2 == null ) return mPoint1.mPath;
    return ( mOrientation > 90 ) ? mPoint2.mPath : mPoint1.mPath;
  }

  Path getOrigPath( boolean flip )
  {
    if ( mPoint2 == null ) return mPoint1.mOrigPath;
    return flip? mPoint2.mOrigPath : mPoint1.mOrigPath;
  }
 
  Path getOrigPath( )
  {
    if ( mPoint2 == null ) return mPoint1.mOrigPath;
    return ( mOrientation > 90 ) ? mPoint2.mOrigPath : mPoint1.mOrigPath;
  }
 
  Paint getPaint( boolean flip )
  {
    if ( mPoint2 == null ) return mPoint1.mPaint;
    return flip? mPoint2.mPaint : mPoint1.mPaint;
  }

  SymbolPoint( String filename, String locale )
  {
    mPoint1 = null;
    mPoint2 = null;
    mOrientable = false;
    mHasText = false;
    mOrientation = 0.0;
    readFile( filename, locale );
  }

  SymbolPoint( String n1, String tn1, int c1, String p1, String n2, String tn2, int c2, String p2 )
  {
    mOrientable = true;
    mHasText = false;
    mOrientation = 0.0;
    mPoint1 = new SymbolPointBasic( n1, tn1, c1, p1 );
    mPoint2 = new SymbolPointBasic( n2, tn2, c2, p2 );
  }

  SymbolPoint( String n1, String tn1, int c1, String p1, boolean orientable )
  {
    mOrientable = orientable;
    mHasText = false;
    mOrientation = 0.0;
    mPoint1 = new SymbolPointBasic( n1, tn1, c1, p1 );
    mPoint2 = null;
  }

  SymbolPoint( String n1, String tn1, int c1, String p1, boolean orientable, boolean has_text )
  {
    mOrientable = orientable;
    mHasText = has_text;
    mOrientation = 0.0;
    mPoint1 = new SymbolPointBasic( n1, tn1, c1, p1 );
    mPoint2 = null;
  }

  void flip( ) 
  {
    if ( mPoint2 != null ) {
      if ( mOrientation > 90 ) {
        mOrientation = 0;
      } else {
        mOrientation = 180;
      }
    }
  }

  boolean getFlip() 
  {
    return mPoint2 != null && mOrientation > 90;
  }

  void rotateGrad( double a )
  {
    if ( mOrientable ) {
      // Log.v( TAG, "SymbolPoint::rotateGrad orientation " + mOrientation + " rotation " + a );
      mOrientation += a;
      if ( mOrientation > 360.0 ) mOrientation -= 360.0;
      if ( mOrientation < 0.0 )   mOrientation += 360.0;
      Matrix m = new Matrix();
      m.postRotate( (float)(a) );
      mPoint1.mPath.transform( m );
    }
  }

  void resetOrientation()
  {
    if ( mOrientable && mOrientation != 0.0 ) {
      Matrix m = new Matrix();
      m.postRotate( (float)(-mOrientation) );
      mPoint1.mPath.transform( m );
      mOrientation = 0.0;
    }
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol point
   *      name NAME
   *      th_name THERION_NAME
   *      orientation yes|no
   *      color 0xHHHHHH_COLOR
   *      path
   *        MULTILINE_PATH_STRING
   *      endpath
   *      endsymbol
   */
  void readFile( String filename, String locale )
  {
    // Log.v( TAG, "SymbolPoint::readFile " + filename + " locale " + locale );
 
    String name    = null;
    String th_name = null;
    int color      = 0;
    String path    = null;
    int cnt = 0;

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
          if ( vals[k].equals("symbol") ) {
            name = null;
            th_name = null;
            color = 0x00000000;
            path = null;
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
          } else if ( vals[k].equals("orientation") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mOrientable = ( vals[k].equals("yes") || vals[k].equals("1") );
              }
            }
          } else if ( vals[k].equals("has_text") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mHasText = ( vals[k].equals("yes") || vals[k].equals("1") );
              }
            }
          } else if ( vals[k].equals("color") ) {
            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
            if ( k < s ) {
              color = Integer.decode( vals[k] );
              color |= 0xff000000;
            }
          } else if ( vals[k].equals("path") ) {
            path = br.readLine();
            if ( path != null ) {
              while ( ( line = br.readLine() ) != null ) {
                if ( line.startsWith( "endpath" ) ) break;
                path = path + " " + line;
              }
            }
          } else if ( vals[k].equals("endsymbol") ) {
            if ( name == null ) {
            } else if ( th_name == null ) {
            } else if ( path == null ) {
            } else {
              if ( cnt == 0 ) {
                mPoint1 = new SymbolPointBasic( name, th_name, color, path );
              } else if ( cnt == 1 ) {
                if ( mOrientable == true ) {
                  // ERROR point1 is orientable
                } else {
                  mPoint2 = new SymbolPointBasic( name, th_name, color, path );
                  mOrientable = true;
                }
              } else {
                // ERROR only two points max
              }
              ++ cnt;
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
    mOrientation = 0.0;
  }
}
