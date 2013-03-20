/* @file DrawingAreaPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: area-path (areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20120725 TopoDroidApp log
 * 20121210 symbol area lib
 * 20121225 added "visible" border attribute
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

// import android.util.Log;

/**
 */
public class DrawingAreaPath extends DrawingPath
{
  private static int area_id_cnt = 0;
  // private static final String TAG = "DistoX";

  private String makeId() 
  {
    ++ area_id_cnt;
    String ret = "a" + area_id_cnt;
    return ret;
  }

  int mAreaType;
  int mAreaCnt;
  private float alpha0, alpha1;  // temporary
  private BezierPoint c1, c2;
  boolean mVisible; // visible border

  ArrayList< LinePoint > points; 

  public DrawingAreaPath( int type, String id, boolean visible )
  {
    super( DrawingPath.DRAWING_PATH_AREA );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingAreaPath cstr type " + type + " id " + id );
    mAreaType = type;
    if ( id != null ) {
      mAreaCnt = Integer.parseInt( id.substring(1) );
      if ( mAreaCnt > area_id_cnt ) area_id_cnt = mAreaCnt;
    } else {
      ++area_id_cnt;
      mAreaCnt = area_id_cnt;
    }
    points  = new ArrayList< LinePoint >();
    path    = new Path();
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mAreaNr ) {
      setPaint( DrawingBrushPaths.getAreaPaint( mAreaType ) );
    }
    mVisible = visible;
  }

  public void addStartPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.moveTo( x, y );
    // Log.v(TAG, "area start " + x + " " + y );
  }

  public void addPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.lineTo( x, y );
    // Log.v(TAG, "area point " + x + " " + y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    points.add( new LinePoint( x1,y1, x2,y2, x,y ) );
    path.cubicTo( x1,y1, x2,y2, x,y );
    // Log.v(TAG, "area cubic " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + x + " " + y );
  }

  float distance( float x, float y )
  {
    float dist = 1000f; // FIXME
    for ( LinePoint pt : points ) {
      float d = Math.abs( pt.mX - x ) + Math.abs( pt.mY - y );
      if ( d < dist ) dist = d;
    }
    return dist;
  }

  public void close() 
  {
    path.close();
    // Log.v(TAG, "area close path" );
  }

  public void setAreaType( int t ) 
  {
    mAreaType = t;
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mAreaNr ) {
      setPaint( DrawingBrushPaths.getAreaPaint( mAreaType ) );
    }
  }

  public int areaType() { return mAreaType; }

  // public ArrayList< LinePoint > getPoints() { return points; }

  // public int size() { return points.size(); }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id a%d -close on ", mAreaCnt );
    if ( ! mVisible ) pw.format("-visibility off ");
    pw.format("\n");
    for ( LinePoint pt : points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    pw.format("area %s\n", DrawingBrushPaths.getAreaThName( mAreaType ) );
    pw.format("  a%d\n", mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

}

