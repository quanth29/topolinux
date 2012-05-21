/* @file DrawingAreaPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: area-path (areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;

/**
 */
public class DrawingAreaPath extends DrawingPath
{
  // private static final String TAG = "DistoX";
  private static int area_id_cnt = 0;

  private String makeId() 
  {
    ++ area_id_cnt;
    String ret = "a" + area_id_cnt;
    return ret;
  }

  private int mAreaType;
  private int mAreaCnt;
  private float alpha0, alpha1;  // temporary
  private BezierPoint c1, c2;

  private ArrayList< LinePoint > points; 

  public DrawingAreaPath( int type, String id )
  {
    super( DrawingPath.DRAWING_PATH_AREA );
    // Log.v( TAG, "new DrawingAreaPath type " + type );
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
    if ( mAreaType < DrawingBrushPaths.AREA_MAX ) {
      setPaint( DrawingBrushPaths.areaPaint[ mAreaType ] );
    }
  }

  public void addStartPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.moveTo( x, y );
  }

  public void addPoint( float x, float y ) 
  {
    points.add( new LinePoint(x,y) );
    path.lineTo( x, y );
  }

  public void addPoint3( float x1, float y1, float x2, float y2, float x, float y ) 
  {
    points.add( new LinePoint( x1,y1, x2,y2, x,y ) );
    path.cubicTo( x1,y1, x2,y2, x,y );
  }

  public void close() 
  {
    path.close();
  }

  public void setAreaType( int t ) 
  {
    mAreaType = t;
    if ( mAreaType < DrawingBrushPaths.AREA_MAX ) {
      setPaint( DrawingBrushPaths.areaPaint[ mAreaType ] );
    }
  }
  public int areaType() { return mAreaType; }

  public ArrayList< LinePoint > getPoints() { return points; }

  public int size() { return points.size(); }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id a%d -close on \n", mAreaCnt );
    for ( LinePoint pt : points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    pw.format("area %s\n", DrawingBrushPaths.areaName[mAreaType] );
    pw.format("  a%d\n", mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

}

