/** @file PointButton.java
 *
 * @author marco corvi
 * @date dec 2013
 *
 * @brief TopoDroid drawing: button for a line type
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.content.Context;

import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Path;

import android.widget.Button;

class PointButton extends Button 
{
  private Paint mPaint = new Paint();
  private Paint mPathPaint;
  private Path  mPath;

  public PointButton(Context context, Paint paint, Path path )
  {
    super(context);
    setBackgroundColor( Color.BLACK );
    setPadding(5, 5, 5, 5 );
    setMinimumWidth( 80 );
    setMinimumHeight( 30 );
    mPathPaint = paint;
    mPath = new Path( path );
  }

  public void onDraw(Canvas canvas) 
  {
    // draw the button background
    mPath.offset( getWidth()/2, getHeight()/2 );
    canvas.drawPath( mPath, mPathPaint );
  }
}

