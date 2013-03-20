/** @file SymbolPointBasic.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid basic symbol point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 */
package com.android.DistoX;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Paint;
import android.graphics.Path;

class SymbolPointBasic
{
  public Paint mPaint;
  public Path mPath;
  public String mThName;
  public String mName;

  SymbolPointBasic( String name, String th_name, int color, String path )
  {
    mName   = name;
    mThName = th_name;
    makePaint( color );
    if ( path != null ) {
      makePath( path );
    } else {
      makePath( );
    }
  }

  private void makePath()
  {
    mPath = new Path();
    mPath.moveTo(0,0);
  }

  /* Make the path from its string description
   * The path string description is composed of the following directives
   *     - "moveTo X Y"
   *     - "lineTo X Y"
   *     - "cubicTo X1 Y1 X2 Y2 X Y"
   *     - "addCircle X Y R"
   */
  private void makePath( String path )
  {
    float unit = TopoDroidApp.mUnit;
    mPath = new Path();
    String[] vals = path.split(" ");
    int s = vals.length;
    for ( int k = 0; k<s; ++k ) {
      float x0=0, y0=0, x1=0, y1=0, x2=0, y2=0;
      if ( "moveTo".equals( vals[k] ) ) {
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) {
          y0 = Float.parseFloat( vals[k] );
          mPath.moveTo( x0*unit, y0*unit );
        }
      } else if ( "lineTo".equals( vals[k] ) ) {
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { 
          y0 = Float.parseFloat( vals[k] ); 
          mPath.lineTo( x0*unit, y0*unit );
        }
      } else if ( "cubicTo".equals( vals[k] ) ) {
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x1 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { y1 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x2 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { 
          y2 = Float.parseFloat( vals[k] ); 
          mPath.cubicTo( x0*unit, y0*unit, x1*unit, y1*unit, x2*unit, y2*unit );
        }
      } else if ( "addCircle".equals( vals[k] ) ) {
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }
        ++k; while ( k < s && vals[k].length() == 0 ) ++k;
        if ( k < s ) {
          x1 = Float.parseFloat( vals[k] );
          mPath.addCircle( x0*unit, y0*unit, x1*unit, Path.Direction.CCW );
        }
      }
    }
  }

  private void makePaint( int color )
  {
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( color );
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( 1 );
  }

}
