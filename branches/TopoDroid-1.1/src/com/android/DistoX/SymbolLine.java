/** @file SymbolLine.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121210 added mHasEffect flag
 * 20121211 locale
 */
package com.android.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.ComposePathEffect;
import android.graphics.DashPathEffect;
import android.graphics.PathDashPathEffect;
import android.graphics.PathDashPathEffect.Style;
import android.graphics.Matrix;

// import android.util.Log;

public class SymbolLine
{
  // static final String TAG = "DistoX";

  String mName;
  String mThName;
  Paint  mPaint;
  Paint  mRevPaint;
  boolean mHasEffect;

  // width = 1
  // no effect
  SymbolLine( String name, String th_name, int color )
  {
    init( name, th_name, color, 1 );
  }

  // no effect
  SymbolLine( String name, String th_name, int color, int width )
  {
    init( name, th_name, color, width );
  }

  SymbolLine( String name, String th_name, int color, PathEffect effect_dir, PathEffect effect_rev )
  {
    init( name, th_name, color, 4 );
    mPaint.setPathEffect( effect_dir );
    mRevPaint.setPathEffect( effect_rev );
  }

  private void init( String name, String th_name, int color, int width )
  {
    mName   = name;
    mThName = th_name;
    mPaint  = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( color );
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( width );
    mRevPaint = new Paint (mPaint );
    mHasEffect = false;
  }

  SymbolLine( String filepath, String locale ) 
  {
    readFile( filepath, locale );
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol line
   *      name NAME
   *      th_name THERION_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      width WIDTH
   *      effect
   *      endeffect
   *      endsymbol
   */
  void readFile( String filename, String locale )
  {
    // Log.v( TAG, "load line file " + filename );
    float unit = TopoDroidApp.mUnit;
    String name    = null;
    String th_name = null;
    mHasEffect = false;
    int color  = 0;
    int alpha  = 0xcc;
    int width  = 1;
    Path path_dir = null;
    Path path_rev = null;
    DashPathEffect dash = null;
    PathDashPathEffect effect = null;
    PathDashPathEffect rev_effect = null;
    boolean moved_to = false;
    float xmin=0, xmax=0;
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line;
      while ( (line = br.readLine()) != null ) {
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
  	  } else if ( vals[k].equals("width") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      width = Integer.parseInt( vals[k] );
            }
  	  } else if ( vals[k].equals("dash") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
              float[] x = new float[2];
  	      x[0] = Float.parseFloat( vals[k] );
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                x[1] = Float.parseFloat( vals[k] );
                dash = new DashPathEffect( x, 0 );
              }
            }
  	  } else if ( vals[k].equals("effect") ) {
            path_dir = new Path();
            // path_dir.moveTo(0,0);
            moved_to = false;
            while ( (line = br.readLine() ) != null ) {
              line.trim();
              vals = line.split(" ");
              s = vals.length;
              k = 0;
  	      while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                if ( vals[k].equals("moveTo") ) {
                  if ( ! moved_to ) {
  	            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	            if ( k < s ) {
  	              float x = Float.parseFloat( vals[k] );
  	              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              if ( k < s ) {
  	                 float y = Float.parseFloat( vals[k] );
                         path_dir.moveTo( x*unit, y*unit );
                         xmin=xmax=x;
                         moved_to = true;
                      }
                    }
                  }
                } else if ( vals[k].equals("lineTo") ) { 
  	          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	          if ( k < s ) {
  	            float x = Float.parseFloat( vals[k] );
  	            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	            if ( k < s ) {
  	              float y = Float.parseFloat( vals[k] );
                      path_dir.lineTo( x*unit, y*unit );
                      if ( x < xmin ) xmin = x;
                      else if ( x > xmax ) xmax = x;
                    }
                  }
                } else if ( vals[k].equals("endeffect") ) {
                  path_dir.close();
                  path_rev = new Path( path_dir );
                  effect = new PathDashPathEffect( path_dir, (xmax-xmin)*unit, 0, PathDashPathEffect.Style.MORPH );
                  Matrix m = new Matrix();
                  m.postRotate( 180 );
                  path_rev.transform( m );
                  rev_effect = new PathDashPathEffect( path_rev, (xmax-xmin)*unit, 0, PathDashPathEffect.Style.MORPH );
                  break;
                }
              }
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
              mRevPaint = new Paint( mPaint );
              if ( effect != null ) {
                mHasEffect = true;
                // mPaint.setStrokeWidth( 4 );
                // mRevPaint.setStrokeWidth( 4 );
                if ( dash != null ) {
                  mPaint.setPathEffect( new ComposePathEffect( effect, dash ) );
                  mRevPaint.setPathEffect( new ComposePathEffect( rev_effect, dash ) );
                } else {
                  mPaint.setPathEffect( effect );
                  mRevPaint.setPathEffect( rev_effect );
                }
              } else if ( dash != null ) {
                mPaint.setPathEffect( dash );
                mRevPaint.setPathEffect( dash );
              } else {
                mPaint.setStrokeWidth( width );
                mRevPaint.setStrokeWidth( width );
              }
  	    }
          }
        }
      }
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch( IOException e ) {
      // FIXME
    }
  }

}

