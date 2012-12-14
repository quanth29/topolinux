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

/**
 */
public class DrawingLabelPath extends DrawingPointPath
{
  private static float toTherion = TopoDroidApp.TO_THERION;
  public String mText;
  // private Paint paint;

  public DrawingLabelPath( String text, float x, float y, int scale, String options )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex, x, y, scale, options );
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
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingLabelPath::draw " + mText );
    canvas.drawTextOnPath( mText, path, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // TopoDroidApp.Log( TopoDroidApp.LOG_PATH, "DrawingLabelPath::draw[matrix] " + mText );
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
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

}

