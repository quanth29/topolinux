/* @file DrawingAreaPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: area-path (areas)
 *
 * The area border (line) path id DrawingPath.mPath
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES 
 * 20120725 TopoDroidApp log
 * 20121210 symbol area lib
 * 20121225 added "visible" border attribute
 */
package com.topodroid.DistoX;

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
public class DrawingAreaPath extends DrawingPointLinePath
{
  private static int area_id_cnt = 0;

  private String makeId() 
  {
    ++ area_id_cnt;
    String ret = "a" + area_id_cnt;
    return ret;
  }

  int mAreaType;
  int mAreaCnt;
  // boolean mVisible; // visible border in DrawingPointLinePath

  public DrawingAreaPath( int type, String id, boolean visible )
  {
    // visible = ?,   closed = true
    super( DrawingPath.DRAWING_PATH_AREA, visible, true );
    // TopoDroidApp.Log( TopoDroidApp.LOG_PLOT, "DrawingAreaPath cstr type " + type + " id " + id );
    mAreaType = type;
    if ( id != null ) {
      mAreaCnt = 1;
      try {
        Integer.parseInt( id.substring(1) );
      } catch ( NumberFormatException e ) {
        TopoDroidApp.Log( TopoDroidApp.LOG_ERR, "DrawingAreaPath AreaCnt parse Int error: " + id.substring(1) );
      }
      if ( mAreaCnt > area_id_cnt ) area_id_cnt = mAreaCnt;
    } else {
      ++area_id_cnt;
      mAreaCnt = area_id_cnt;
    }
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mAreaNr ) {
      setPaint( DrawingBrushPaths.getAreaPaint( mAreaType ) );
    }
  }

  public void setAreaType( int t ) 
  {
    mAreaType = t;
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mAreaNr ) {
      setPaint( DrawingBrushPaths.getAreaPaint( mAreaType ) );
    }
  }

  public int areaType() { return mAreaType; }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id a%d -close on ", mAreaCnt );
    if ( ! mVisible ) pw.format("-visibility off ");
    pw.format("\n");
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    pw.format("area %s\n", DrawingBrushPaths.getAreaThName( mAreaType ) );
    pw.format("  a%d\n", mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

  @Override
  public void toCsurvey( PrintWriter pw )
  {
    int layer  = DrawingBrushPaths.getAreaCsxLayer( mAreaType );
    int type   = 3;
    int cat    = DrawingBrushPaths.getAreaCsxCategory( mAreaType );
    int pen    = DrawingBrushPaths.getAreaCsxPen( mAreaType );
    int brush  = DrawingBrushPaths.getAreaCsxBrush( mAreaType );

    // linetype: 0 line, 1 spline, 2 bezier
    pw.format("          <item layer=\"%d\" name=\"\" type=\"3\" category=\"%d\" linetype=\"1\" mergemode=\"0\">\n",
      layer, cat );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <brush type=\"%d\" />\n", brush);
    pw.format("            <points data=\"");
    boolean b = true;
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      float x = DrawingActivity.sceneToWorldX( pt.mX );
      float y = DrawingActivity.sceneToWorldY( pt.mY );
      pw.format("%.2f %.2f ", x, y );
      if ( b ) { pw.format("B "); b = false; }
    }
    pw.format("\" />\n");
    pw.format("          </item>\n");
  }

  @Override
  LinePoint next( LinePoint lp )
  {
    if ( lp == null ) return null;
    if ( lp.mNext == null ) return mFirst;
    return lp.mNext;
  }

  @Override
  LinePoint prev( LinePoint lp )
  {
    if ( lp == null ) return null;
    if ( lp.mPrev == null ) return mLast;
    return lp.mPrev;
  }
}

