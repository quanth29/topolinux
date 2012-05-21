/* @file DrawingLabelPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: label-point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.android.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

// import android.util.Log;

/**
 */
public class DrawingLabelPath extends DrawingPointPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;
  public String mText;
  // private Paint paint;

  public DrawingLabelPath( String text, float x, float y )
  {
    super( DrawingBrushPaths.POINT_LABEL, x, y );
    mText = text;
    // setPaint( DrawingBrushPaths.pointPaint[ DrawingBrushPaths.POINT_LABEL ] );
    // mPaint = DrawingBrushPaths.pointPaint[ DrawingBrushPaths.POINT_LABEL ];
    // paint = new Paint();
    // paint.setDither(true);
    // paint.setColor( 0xffffffff );
    // paint.setStyle(Paint.Style.STROKE);
    // paint.setStrokeJoin(Paint.Join.ROUND);
    // paint.setStrokeCap(Paint.Cap.ROUND);
    // paint.setStrokeWidth( STROKE_WIDTH_CURRENT );

    path = new Path();
    path.moveTo( 0, 0 );
    path.lineTo( 20*mText.length(), 0 );
    path.offset( x, y );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // Log.v( "DistoX", "LABEL " + mText );
    canvas.drawTextOnPath( mText, path, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // Log.v( "DistoX", "LABEL " + mText );
    mTransformedPath = new Path( path );
    mTransformedPath.transform( matrix );
    canvas.drawTextOnPath( mText, mTransformedPath, 0f, 0f, mPaint );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "point %.2f %.2f label -text \"%s\"", xpos()*toTherion, -ypos()*toTherion, mText );
    return sw.getBuffer().toString();
  }

}

