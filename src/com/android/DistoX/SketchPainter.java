/** @file SketchPainter.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130220 created
 */
package com.android.DistoX;

import android.graphics.Paint;

class SketchPainter
{
  public static final int redColor  = 0xffff3333;
  public static final int blueColor = 0xff3399ff;
  Paint whitePaint;
  Paint redPaint;
  Paint greenPaint;
  Paint bluePaint;
  Paint blackPaint;
  Paint previewPaint;
  // Paint topLinePaint;
  // Paint sideLinePaint;
  Paint borderLinePaint;
  Paint surfaceForPaint;
  Paint surfaceBackPaint;
  Paint areaPaint;

  SketchPainter()
  {
    makePaints();
  }

  Paint getLinePaint( int view ) 
  {
    return whitePaint;
  }

  private void makePaints()
  {
    whitePaint = new Paint();
    whitePaint.setColor(0xFFffffff);
    whitePaint.setStyle(Paint.Style.STROKE);
    whitePaint.setStrokeJoin(Paint.Join.ROUND);
    whitePaint.setStrokeCap(Paint.Cap.ROUND);
    whitePaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
    redPaint   = new Paint();
    redPaint.setDither(true);
    redPaint.setColor( 0xccff0000 );
    redPaint.setStyle(Paint.Style.STROKE);
    redPaint.setStrokeJoin(Paint.Join.ROUND);
    redPaint.setStrokeCap(Paint.Cap.ROUND);
    redPaint.setStrokeWidth( 1 );
    greenPaint   = new Paint();
    greenPaint.setDither(true);
    greenPaint.setColor( 0xcc00ff33 );
    greenPaint.setStyle(Paint.Style.STROKE);
    greenPaint.setStrokeJoin(Paint.Join.ROUND);
    greenPaint.setStrokeCap(Paint.Cap.ROUND);
    greenPaint.setStrokeWidth( 1 );
    bluePaint   = new Paint();
    bluePaint.setDither(true);
    bluePaint.setColor( 0xcc0000ff);
    bluePaint.setStyle(Paint.Style.STROKE);
    bluePaint.setStrokeJoin(Paint.Join.ROUND);
    bluePaint.setStrokeCap(Paint.Cap.ROUND);
    bluePaint.setStrokeWidth( 1 );
    blackPaint   = new Paint();
    blackPaint.setDither(true);
    blackPaint.setColor( 0xff00ffff);
    blackPaint.setStyle(Paint.Style.STROKE);
    blackPaint.setStrokeJoin(Paint.Join.ROUND);
    blackPaint.setStrokeCap(Paint.Cap.ROUND);
    blackPaint.setStrokeWidth( 1 );

    previewPaint = new Paint();
    previewPaint.setColor(0xFFC1C1C1);
    previewPaint.setStyle(Paint.Style.STROKE);
    previewPaint.setStrokeJoin(Paint.Join.ROUND);
    previewPaint.setStrokeCap(Paint.Cap.ROUND);
    previewPaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
    // topLinePaint = new Paint();
    // topLinePaint.setColor(0x99cc6633);
    // topLinePaint.setStyle(Paint.Style.STROKE);
    // topLinePaint.setStrokeJoin(Paint.Join.ROUND);
    // topLinePaint.setStrokeCap(Paint.Cap.ROUND);
    // topLinePaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
    // sideLinePaint = new Paint();
    // // sideLinePaint.setColor(0xFF3333ff);
    // sideLinePaint.setColor(0x99cc9900);
    // sideLinePaint.setStyle(Paint.Style.STROKE);
    // sideLinePaint.setStrokeJoin(Paint.Join.ROUND);
    // sideLinePaint.setStrokeCap(Paint.Cap.ROUND);
    // sideLinePaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
    borderLinePaint = new Paint();
    // borderLinePaint.setColor(0xFFcc3399);
    borderLinePaint.setColor(0x99ff0000);
    borderLinePaint.setStyle(Paint.Style.STROKE);
    borderLinePaint.setStrokeJoin(Paint.Join.ROUND);
    borderLinePaint.setStrokeCap(Paint.Cap.ROUND);
    borderLinePaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );

    surfaceForPaint = new Paint();
    surfaceForPaint.setColor(0x66666666);
    surfaceForPaint.setStyle(Paint.Style.FILL);
    // surfaceForPaint.setStyle(Paint.Style.STROKE);
    surfaceForPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceForPaint.setStrokeCap(Paint.Cap.ROUND);
    surfaceForPaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
    surfaceBackPaint = new Paint();
    surfaceBackPaint.setColor(0x44cc6633);
    surfaceBackPaint.setStyle(Paint.Style.FILL);
    surfaceBackPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceBackPaint.setStrokeCap(Paint.Cap.ROUND);
    surfaceBackPaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );

    areaPaint = new Paint();
    areaPaint.setColor(0x99ff6633);
    areaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    areaPaint.setStrokeJoin(Paint.Join.ROUND);
    areaPaint.setStrokeCap(Paint.Cap.ROUND);
    areaPaint.setStrokeWidth( DrawingBrushPaths.STROKE_WIDTH_PREVIEW );
  }
}
