/** @file AreaButton.java
 *
 * @author marco corvi
 * @date dec 2013
 *
 * @brief TopoDroid drawing: button for an area type
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201312   created
 */
package com.android.DistoX;

import android.content.Context;

import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Path;

import android.widget.Button;

class AreaButton extends Button 
{
  private Paint mPaint = new Paint();
  private Paint mPathPaint;
  private Path  mPath;

  public AreaButton(Context context, Paint paint )
  {
    super(context);
    setBackgroundColor( Color.BLACK );
    setPadding(5, 5, 5, 5 );
    setMinimumWidth( 80 );
    setMinimumHeight( 30 );
    mPathPaint = paint;
    mPath = new Path();
    mPath.addCircle( 0, 0, 10, Path.Direction.CCW );
  }

  public void onDraw(Canvas canvas) 
  {
    // draw the button background
    mPath.offset( getWidth()/2, getHeight()/2 );
    canvas.drawPath( mPath, mPathPaint );
  }
}

